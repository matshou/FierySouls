package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.block.BlockTorchUnlit;

import net.minecraft.network.Packet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTorchLit extends TileEntityTorch
{
	private static final short DIMINISH_LIGHT_TIME_MARK = 1600;        // Diminishing light after remaining combustion equals this number.
	private static final double DIMINISH_LIGHT_PER_INTERVAL = 0.025;   // Amount of light to diminish each update interval.
	
	private boolean extinguishTorchOnServer;   // Should we call server to extinguish torch instance
	private boolean updatingLightData;         // Used by server to notify client to start updating light value
	private boolean updateFlameHazard;         // Should we check if torch flame could spread fire?
	private double torchLightLevel;            // How much light does the torch emit?
	
	public TileEntityTorchLit() {}	
	public TileEntityTorchLit(final long totalWorldTime) 
	{
		super(totalWorldTime);
		this.extinguishTorchOnServer = false;
		this.updatingLightData = false;
		this.updateFlameHazard = true;
		
		this.torchLightLevel = BlockTorchLit.MAXIMUM_TORCH_LIGHT_LEVEL;
		/** DIMINISH_LIGHT_PER_INTERVAL = torchLightLevel / (DIMINISH_LIGHT_TIME_MARK / MAIN_UPDATE_INTERVAL); */
	}
	@Override
	public final void update()
	{
		// Update only at set intervals to reduce performance hits.
		if (this.updateTickCount++ < this.MAIN_UPDATE_INTERVAL)
			return; else this.updateTickCount = 0;
		
		if (!getWorld().isRemote)
		{ 
			// Once the torch has been ignited it will begin burning and producing light. 
			// The combustion process has a limited defined duration, after which the torch will extinguish.
			
			if (this.updateCombustionDuration(MAIN_UPDATE_INTERVAL * -1) <= 0)
				this.extinguishTorch(false);
			
			// Since we're not updating this data on client tell him that he should start
			// handling light updates on his side now, we're done here.
			
			else if (this.getCombustionDuration() <= DIMINISH_LIGHT_TIME_MARK && !this.updatingLightData)
			{
				this.updatingLightData = true;
				this.markForUpdate();
			}
		    // When it's raining and the torch is directly exposed to rain it will start collecting humidity.
			// Once it has collected enough humidity it will extinguish.
			
		    if (getWorld().getWorldInfo().isRaining() && this.getWorld().canBlockSeeSky(pos))
		    {
			    if (this.updateHumidityLevel(MAIN_UPDATE_INTERVAL) > HUMIDITY_THRESHOLD)
				    this.extinguishTorch(true);
		    }
		}
		else if (this.updatingLightData)
			this.updateLightLevel(this.DIMINISH_LIGHT_PER_INTERVAL);
		
		if (this.updateFlameHazard == true)
		{
			this.setRoofOnFire(getWorld(), pos);
			this.updateFlameHazard = false;
		}
	}
	/** Replace this torch with an unlit one and activate the smoldering effect.
	 *  This method acts like a proxy for a clone method in BlockTorchLit, always call this one first!
	 *  The way we handle the client and server side here is interesting, take a look at the code. 
	 *  
	 *  @param waitForClient Should we wait for the client to call this method first? Quite useful to enable
	 *                       client side smoke particle spawning. If true; SERVER - client - SERVER connection.
	 */
	public void extinguishTorch(final boolean waitForClient)
	{	
		// TODO: Think about further restructuring the following section:
		
	    if (!waitForClient || getWorld().isRemote)
	    {
	    	// This is the part where the blockstate gets updated and client entities are handled.
	    	// When we extinguish the torch the new tile entity should be initialized and we can pass our data to it.
	  
	    	if (BlockTorchLit.extinguishTorch(getWorld(), pos))
	    	{
	    		TileEntity torchEntity = getWorld().getTileEntity(pos);
		        if (torchEntity == null || !(torchEntity instanceof TileEntityTorchUnlit))
		        	return;

		    	TileEntityTorchUnlit torchUnlit = (TileEntityTorchUnlit)torchEntity;
		    	
		    	// Notify tile entity that it should start spawning smoke particles only on client side.
		    	// After that extinguish the torch server-side. This will finalize current tile entity destruction 
		    	// and update the new tile entity with needed info.
		    	
		    	if (!getWorld().isRemote)
		    	{
		    		torchUnlit.setCombustionDuration(this.getCombustionDuration());
		    		torchUnlit.updateHumidityLevel(this.getHumidityLevel());
	    		    torchUnlit.torchAge = getWorld().getTotalWorldTime() - this.timeCreated;     	
		    	}
		    	else if (waitForClient)
		    	{
		    		torchUnlit.scheduleSmolderingEffect();
		    		TileEntity torchLit = net.minecraft.server.MinecraftServer.getServer().getEntityWorld().getTileEntity(pos);  
		            if (torchLit != null && torchLit instanceof TileEntityTorchLit)
		            	((TileEntityTorchLit)torchLit).extinguishTorch(false);
		    	}
		    	else torchUnlit.scheduleSmolderingEffect();
	    	}      
	    }  // Notify the client that it should extinguish the torch on it's side and call back.   
	    else { this.extinguishTorchOnServer = true; this.markForUpdate(); }
	}
	/** Check to see if we should force the block above us to catch on fire.
	 *  If we roll positive the torch will assume the function of a fire block with limited spreading movement.
	 */
	private static boolean setRoofOnFire(World worldIn, BlockPos pos)
	{
		BlockPos neighbourPos = new BlockPos(pos.getX(), pos.getY() +1, pos.getZ());
		Block neighbourBlock = worldIn.getBlockState(neighbourPos).getBlock();
		
		if (neighbourBlock == net.minecraft.init.Blocks.air)   // More sensible then calling 'canBlockSeeSky'...
			return false;
		
		// TODO: Create more advanced parameters like taking into account 
		//       air humidity, strength of torch flame etc.
			
		final int chancesToCatchFire = neighbourBlock.getFlammability(worldIn, neighbourPos, net.minecraft.util.EnumFacing.DOWN);
		
		java.util.Random rand = new java.util.Random();
		int natural_roll = rand.nextInt(100) + 1;     // 0% - 100% (1 - 100 roll)
	
		// If a saving throw failed, set the top block on fire
		if (chancesToCatchFire >= natural_roll)
			return worldIn.setBlockState(neighbourPos, net.minecraft.init.Blocks.fire.getDefaultState());
		
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
		
		this.torchLightLevel = Math.round((torchLightLevel - value) * 1000.0) / 1000.0;	
		if (torchLightLevel == Math.ceil(torchLightLevel))
			this.worldObj.checkLight(this.pos);
	}
	public int getLightLevel()
	{
		// NOTE: Always round this value up not down to prevent unexpected updates until we're ready to send. 
		return (int)Math.floor(this.torchLightLevel);
	}
	
	/** Initialize light level data on client and start regularly updating it.
	 *  The initialization will only happen if light data needs to be updated.
	 *  Also request from world to update light renderer with new data.
	 */ 
	@SideOnly(Side.CLIENT)
	protected void recalculateLightLevel(short combustionDuration)
	{
		if (combustionDuration < DIMINISH_LIGHT_TIME_MARK)
		{
			int ticksElapsed = DIMINISH_LIGHT_TIME_MARK - combustionDuration;
			updateLightLevel(Math.floor(ticksElapsed / MAIN_UPDATE_INTERVAL * DIMINISH_LIGHT_PER_INTERVAL));
			
			this.worldObj.checkLight(this.pos);
			updatingLightData = true;
		}
	}
	
	/** When this update is requested we check if the torch should set the object above it on fire. */
	public void scheduleHazardUpdate()
	{
		this.updateFlameHazard = true;
	}
	
	// ====================================== NETWORK UTILITIES ==============================================
	
	/** These functions are used to update, write and read packets sent from SERVER to CLIENT. */ 
	// This will make the server call 'getDescriptionPacket' for a full data sync
	//@SideOnly(Side.SERVER)
	private void markForUpdate()
	{
		getWorld().markBlockForUpdate(pos);
		this.markDirty();
	}
	@Override
	// Gathers data into a packet that is to be sent to the client. Called on server only. 
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		super.writeToNBT(nbtTag);
		
		// These variables are used to notify the client that it should do something on it's side. 
		// They are also exclusive to TileEntityTorchLit, that's why they are here and not in writeToNBT.
		
		nbtTag.setBoolean("extinguishTorchOnServer", this.extinguishTorchOnServer);
		nbtTag.setBoolean("startUpdatingLight", this.updatingLightData);
		
		return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
	}
	// Extracts data from a packet that was sent from the server. Called on client only.
	// Minecraft automatically sends a 'description packet' for the tile entity when it is first 
	// loaded on the client, and you can force it to resend one afterwards
	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, S35PacketUpdateTileEntity packet) 
	{
		readFromNBT(packet.getNbtCompound());
		
		if (packet.getNbtCompound().getBoolean("extinguishTorchOnServer"))
			this.extinguishTorch(true);
	  
		this.updatingLightData = packet.getNbtCompound().getBoolean("startUpdatingLight");
	} 
}
