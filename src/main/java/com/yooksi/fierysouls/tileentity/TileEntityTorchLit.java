package com.yooksi.fierysouls.tileentity;

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

public class TileEntityTorchLit extends TileEntityTorch
{
	private static final double DIMINISH_LIGHT_PER_INTERVAL = 0.025;   // Amount of light to diminish each update interval.
	private static final short DIMINISH_LIGHT_TIME_MARK = 1600;        // Diminishing light after remaining combustion equals this number.
    
	public boolean torchFireHazardUpdate = true;       
	private boolean updatingLightData= false;      // Used by server to notify client to start updating light value.
	private double torchLightLevel = 0;
	
	public TileEntityTorchLit() {}	
	public TileEntityTorchLit(final long totalWorldTime) 
	{
		super(totalWorldTime);
		torchLightLevel = BlockTorchLit.MAXIMUM_TORCH_LIGHT_LEVEL;
	}
	
	@Override
	public final void update()
	{
		// Update only at set intervals to reduce performance hits.
		if (updateTickCount++ < SharedDefines.MAIN_UPDATE_INTERVAL)
			return; else updateTickCount = 0;
		
		if (!getWorld().isRemote)
		{ 
			if (torchFireHazardUpdate == true)
				tryCatchFireOnNeighbour();
			
			if (updateCombustionDuration(SharedDefines.MAIN_UPDATE_INTERVAL * -1) <= 0)
				extinguishTorch();
			
			// Since we're not updating this data here handle light updates on client, we're done here.
			else if (getCombustionDuration() <= DIMINISH_LIGHT_TIME_MARK && !updatingLightData)
			{
				updatingLightData = true;
				markForUpdate();
			}
		    // When it's raining and the torch is directly exposed to rain it will start collecting humidity.
		    if (getWorld().getWorldInfo().isRaining() && getWorld().canBlockSeeSky(pos))
		    {
			    if (updateHumidityLevel(SharedDefines.MAIN_UPDATE_INTERVAL) >= SharedDefines.HUMIDITY_THRESHOLD)		   
			    	extinguishTorch();
		    }
		}
		else if (updatingLightData == true)
			updateLightLevel(DIMINISH_LIGHT_PER_INTERVAL);
	}
	
	/** Replace this torch with an unlit one and activate the smoldering effect. <br>
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
	
	/** Check to see if we should force the block above us to catch on fire.
	 *  If we roll positive the torch will assume the function of a fire block with limited spreading movement.
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
	
		java.util.Random rand = new java.util.Random();
		int natural_roll = rand.nextInt(100) + 1;            // 0% - 100% (1 - 100 roll)

		if (chancesToCatchFire >= natural_roll)
			return getWorld().setBlockState(neighbourPos, net.minecraft.init.Blocks.fire.getDefaultState());
		
		else return false;
	}	
	
	/** Decrease the level of light the torch emits in it's environment 
	 *  @param value this will be subtracted from the light level value. 
	 *  The more iterations of subtracting this value from the total light level it takes to fully round the number
	 *  the more it will take before the world get's notified that we changed the light value. Choose your base carefully.
	 * */
	@SideOnly(Side.CLIENT)
	protected void updateLightLevel(double value)
	{
		// Update data after truncating to 3 decimals and then check if the value is a round number.
		// To increase performance send render updates in world only if data is already rounded before casting int. 

		torchLightLevel = Math.round((torchLightLevel - value) * 1000.0) / 1000.0;	
		if (torchLightLevel == Math.ceil(torchLightLevel))
			worldObj.checkLight(this.pos);
	}
	public int getLightLevel()
	{
		// NOTE: Always round this value up not down to prevent unexpected updates until we're ready to send. 
		return (int)Math.floor(torchLightLevel);
	}
	
	/** Initialize light level data on client and start regularly updating it.
	 *  The initialization will only happen if light data needs to be updated.
	 *  Also request from world to update light renderer with new data.
	 */ 
	@SideOnly(Side.CLIENT)
	protected void recalculateLightLevel()
	{
		if (getCombustionDuration() < DIMINISH_LIGHT_TIME_MARK)
		{
			int ticksElapsed = DIMINISH_LIGHT_TIME_MARK - getCombustionDuration();
			updateLightLevel(ticksElapsed / SharedDefines.MAIN_UPDATE_INTERVAL * DIMINISH_LIGHT_PER_INTERVAL);
			
			worldObj.checkLight(this.pos);
			updatingLightData = true;
		}
	}
	
	// ====================================== NETWORK UTILITIES ==============================================
	
	/** 
	 * Gathers data into a packet that is to be sent to the client. Called on server only.<br>
	 * Place custom packet data you want to send to client here.
	 */
	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		
		nbtTag.setBoolean("startUpdatingLight", updatingLightData);
		
		return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(net.minecraft.network.NetworkManager net, S35PacketUpdateTileEntity packet) 
	{
		super.onDataPacket(net, packet);

		// Recalculate light data and send it to world once each time the world loads.
		if (updatingLightData == false && (updatingLightData = packet.getNbtCompound().getBoolean("startUpdatingLight")))
			recalculateLightLevel();
	}
}