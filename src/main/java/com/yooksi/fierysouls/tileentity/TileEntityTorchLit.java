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
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTorchLit extends TileEntityTorch
{
	private static final double DIMINISH_LIGHT_PER_INTERVAL = 0.025;   // Amount of light to diminish each update interval.
	private static final short DIMINISH_LIGHT_TIME_MARK = 1600;        // Diminishing light after remaining combustion equals this number.
    
	private boolean extinguishTorchOnServer = false;   // Used by client to extinguish torch on server.
	public boolean torchFireHazardUpdate = true;       
	private boolean updatingLightData= false;          // Used by server to notify client to start updating light value.
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
		if (updateTickCount++ < MAIN_UPDATE_INTERVAL)
			return; else updateTickCount = 0;
		
		if (!getWorld().isRemote)
		{ 
			if (torchFireHazardUpdate == true)
				tryCatchFireOnNeighbour();
			
			if (updateCombustionDuration(MAIN_UPDATE_INTERVAL * -1) <= 0)
				extinguishTorch(false);
			
			// Since we're not updating this data here handle light updates on client, we're done here.
			else if (getCombustionDuration() <= DIMINISH_LIGHT_TIME_MARK && !updatingLightData)
			{
				updatingLightData = true;
				markForUpdate();
			}
		    // When it's raining and the torch is directly exposed to rain it will start collecting humidity.
		    if (getWorld().getWorldInfo().isRaining() && getWorld().canBlockSeeSky(pos))
		    {
			    if (updateHumidityLevel(MAIN_UPDATE_INTERVAL) >= HUMIDITY_THRESHOLD)		   
			    	extinguishTorch(true);
		    }
		}
		else if (updatingLightData == true)
			updateLightLevel(DIMINISH_LIGHT_PER_INTERVAL);
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
	    		TileEntityTorchUnlit torchUnlit = (TileEntityTorchUnlit)getWorld().getTileEntity(pos);
		    	
		    	// Notify tile entity that it should start spawning smoke particles only on client side.
		    	// After that extinguish the torch server-side. This will finalize current tile entity destruction 
		    	// and update the new tile entity with needed info.
		    	
		    	if (!getWorld().isRemote)
		    		torchUnlit.postInit(getCombustionDuration(), getHumidityLevel(), timeCreated);
		    		
		    	else if (waitForClient)
		    	{
		    		torchUnlit.setTorchSmoldering(true);
		    		MinecraftServer server = net.minecraft.server.MinecraftServer.getServer();
		    		TileEntityTorchLit torchLit = (TileEntityTorchLit)server.getEntityWorld().getTileEntity(pos);  
		            torchLit.extinguishTorch(false);
		    	}
		    	else torchUnlit.setTorchSmoldering(true);
	    	}      
	    }  
	    else { this.extinguishTorchOnServer = true; this.markForUpdate(); }
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
	protected void recalculateLightLevel(short combustionDuration)
	{
		if (combustionDuration < DIMINISH_LIGHT_TIME_MARK)
		{
			int ticksElapsed = DIMINISH_LIGHT_TIME_MARK - combustionDuration;
			updateLightLevel(Math.floor(ticksElapsed / MAIN_UPDATE_INTERVAL * DIMINISH_LIGHT_PER_INTERVAL));
			
			worldObj.checkLight(this.pos);
			updatingLightData = true;
		}
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
		this.writeToNBT(nbtTag);
		
		// These variables are used to notify the client that it should do something on it's side. 
		// They are also exclusive to TileEntityTorchLit, that's why they are here and not in writeToNBT.
		
		nbtTag.setBoolean("extinguishTorchOnServer", extinguishTorchOnServer);
		nbtTag.setBoolean("startUpdatingLight", updatingLightData);
		
		return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
	}
	// Extracts data from a packet that was sent from the server. Called on client only.
	// Minecraft automatically sends a 'description packet' for the tile entity when it is first 
	// loaded on the client, and you can force it to resend one afterwards
	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(net.minecraft.network.NetworkManager net, S35PacketUpdateTileEntity packet) 
	{
		this.readFromNBT(packet.getNbtCompound());
		updatingLightData = packet.getNbtCompound().getBoolean("startUpdatingLight");

		if (packet.getNbtCompound().getBoolean("extinguishTorchOnServer"))	
			extinguishTorch(true);
	}
}