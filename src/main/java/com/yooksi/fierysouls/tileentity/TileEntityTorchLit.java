package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.Utilities;
import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.SharedDefines;

import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.block.BlockTorchUnlit;

import net.minecraft.network.Packet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public final class TileEntityTorchLit extends TileEntityTorch
{
	/**
	 *  Start diminishing torch light after the remaining combustion 
	 *  time duration goes below this mark. <br> If this define is not set,
	 *  light will start diminishing as soon as the torch has been set on fire. <p>
	 *  
	 *  <i><b>Note:</b>  this is an optional feature, set to -1 to disable.</i>
	 */
	private static final short DIMINISH_LIGHT_TIME_MARK = 2000;
	
	/**
	 *  This is the minimum light level this torch can have before extinguishing. <br>
	 *  The illumination of this light level should be as dark possible while still looking natural.
	 */
	private static final double MINIMUM_TORCH_LIGHT_LEVEL = 5;

	/**
	 *  Amount of light to diminish each update interval. 
	 *  This value has to always return a round value (or close to one) after being multiplied
	 *  to the point of incrementing the base (first digit past the decimal mark). <p>
	 *  
	 *  Here are some examples of accepted values: <br>
	 *  <code> 1, 0.2, 0.005, 0.5, 0.020, 2.2, 5.025 </code>
	 * */
	@SuppressWarnings("unused")
	private static final double DIMINISH_LIGHT_PER_INTERVAL =
		(BlockTorchLit.MAXIMUM_TORCH_LIGHT_LEVEL - MINIMUM_TORCH_LIGHT_LEVEL) / ( ((DIMINISH_LIGHT_TIME_MARK > 0) ?
					DIMINISH_LIGHT_TIME_MARK : SharedDefines.MAX_TORCH_FLAME_DURATION) / SharedDefines.MAIN_UPDATE_INTERVAL);
    
	
	/* Used by server to notify client to start updating light value. */
	private boolean updatingLight = false;
	
	public boolean torchFireHazardUpdate = true;       
	public double torchLightLevel = 0;
	
	public TileEntityTorchLit() {}	
	public TileEntityTorchLit(final long totalWorldTime) 
	{
		super(totalWorldTime);
		torchLightLevel = BlockTorchLit.MAXIMUM_TORCH_LIGHT_LEVEL;
	}

	@Override
	public void update()
	{	
		if (!isTorchReadyForUpdate())
			return;
		
		else if (!getWorld().isRemote)
		{ 
			if (torchFireHazardUpdate == true)
				tryCatchFireOnNeighbour();
			
			if (updateTorchCombustionDuration(SharedDefines.MAIN_UPDATE_INTERVAL * -1) <= 0)
				extinguishTorch();
			
			// Since we're not updating this data here handle light updates on client, we're done here.
			else if (DIMINISH_LIGHT_TIME_MARK > 0 && getTorchCombustionDuration() <= DIMINISH_LIGHT_TIME_MARK && !updatingLight)
			{
				updatingLight = true;
				markForUpdate();
			}
			
		    // When it's raining and the torch is directly exposed to rain it will start collecting humidity.
		    if (getWorld().getWorldInfo().isRaining() && getWorld().canBlockSeeSky(pos))
		    {
			    if (updateTorchHumidityLevel(SharedDefines.MAIN_UPDATE_INTERVAL) >= SharedDefines.HUMIDITY_THRESHOLD)		   
			    	extinguishTorch();
		    }
		}
		else if (updatingLight == true)
			updateLightLevel(DIMINISH_LIGHT_PER_INTERVAL);
	}
	
	/** 
	 *  Replace this torch with an unlit one and activate the smoldering effect. <br>
	 *  This method acts like a proxy for a clone method in BlockTorchLit, always call this one first!
	 */
	public void extinguishTorch()
	{
	    if (!getWorld().isRemote && BlockTorchLit.extinguishTorch(getWorld(), pos))
	    {
	    	TileEntity torchEntity = getWorld().getTileEntity(pos);
	    	if (torchEntity != null && torchEntity instanceof TileEntityTorchUnlit)
	    	{
	    		TileEntityTorchUnlit torchUnlit = (TileEntityTorchUnlit)torchEntity;
	    	    torchUnlit.readFromNBT(saveDataToPacket());
	    		
	    	    // Set the torch 'smoldering' on server side, nothing will be seen but the client
	    	    // will want to update and the sides will sync, pulling the data from server.
	    	    
	    	    torchUnlit.setTorchSmoldering(true, getWorld().getTotalWorldTime());
		    }
	    }
	}  
	
	/** 
	 *  Check to see if we should force the block above us to catch on fire.
	 */
	private boolean tryCatchFireOnNeighbour()
	{
		torchFireHazardUpdate = false;
		net.minecraft.util.EnumFacing face = net.minecraft.util.EnumFacing.DOWN;
		
		net.minecraft.util.BlockPos neighbourPos = new net.minecraft.util.BlockPos(getPos().offset(face.getOpposite()));
		Block neighbourBlock = getWorld().getBlockState(neighbourPos).getBlock();
		
		if (neighbourBlock == net.minecraft.init.Blocks.air)   // More sensible then calling 'canBlockSeeSky'...
			return false;
		
		// TODO: Create more advanced parameters like taking into account 
		//       air humidity, strength of torch flame etc.
			
		final int chancesToCatchFire = neighbourBlock.getFlammability(getWorld(), neighbourPos, face);
	
		if (Utilities.rollDiceAgainst(chancesToCatchFire, 100, getWorld().rand))
		{
			return getWorld().setBlockState(neighbourPos, net.minecraft.init.Blocks.fire.getDefaultState());
		}
		else return false;
	}	
	
	/** 
	 *  Decrease the level of light the torch emits in it's environment. <p>
	 *  
	 *  <i><b>Note:</b> The more iterations of subtracting this value from the total light level <br>
	 *  it takes to fully round the number the more it will take before the world get's <br> notified
	 *  that we changed the light value.</i>
	 *  
	 *  @param value The value to be <b>subtracted</b> from the light level value. 
	 */
	@SideOnly(Side.CLIENT)
	private void updateLightLevel(double value)
	{
		// Update data after truncating decimals and checking if the value is a round number.
		// To increase performance send render updates in world only if data is already rounded before casting int. 

		int digits = Utilities.getNumberOfDigits(DIMINISH_LIGHT_PER_INTERVAL) - 1;
		torchLightLevel = Utilities.truncateDecimals(torchLightLevel - value, ((digits < 5) ? digits : 5));
        
		// Allow a small deviation to increase the range of values accepted.
		// Reset the light level to always get the same expected deviation.
		
		final double fTorchLightLevel = Math.floor(torchLightLevel);
		if (torchLightLevel - fTorchLightLevel < 0.02D)
		{
			torchLightLevel = fTorchLightLevel;
			getWorld().checkLightFor(net.minecraft.world.EnumSkyBlock.BLOCK, pos);
		}
	}
	public int getLightLevel()
	{
		return (int)Math.round(torchLightLevel);
	}
	
	/** 
	 *  Initialize light level data on client and start regularly updating it.
	 *  The initialization will only happen if <br> light data needs to be updated.
	 *  Also request from world to update light renderer with new data. <p>
	 *  
	 *  <i><b>Note:</b> when the world is entered and this entity is loaded from the NBT,
	 *  there will be a small delay before we can recalculate light data and update the world.
	 *  In the duration of this delay the light will be set to maximum default.</i> 
	 */ 
	@SideOnly(Side.CLIENT)
	private void recalculateLightLevel()
	{
		if (DIMINISH_LIGHT_TIME_MARK < 0 || getTorchCombustionDuration() < DIMINISH_LIGHT_TIME_MARK)
		{
		    @SuppressWarnings("unused")
			short timeMark = (DIMINISH_LIGHT_TIME_MARK < 0) ? SharedDefines.MAX_TORCH_FLAME_DURATION : DIMINISH_LIGHT_TIME_MARK;
			
		    int ticksElapsed =  timeMark - getTorchCombustionDuration();
			updateLightLevel(ticksElapsed / SharedDefines.MAIN_UPDATE_INTERVAL * DIMINISH_LIGHT_PER_INTERVAL);
			
			getWorld().checkLightFor(net.minecraft.world.EnumSkyBlock.BLOCK, pos);
		}
	}
	
	/** 
	 * Gathers data into a packet that is to be sent to the client. <br>
	 * Place custom packet data you want to send to client here. <p>
	 */
	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
			
		nbtTag.setBoolean("updateLight", updatingLight);
			
		return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
	}
		
	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(net.minecraft.network.NetworkManager net, S35PacketUpdateTileEntity packet) 
	{
		super.onDataPacket(net, packet);
		
		// Recalculate light data and send it to world once each time the world loads.
		if (updatingLight == false && (updatingLight = packet.getNbtCompound().getBoolean("updateLight")))
			recalculateLightLevel();
	}
}