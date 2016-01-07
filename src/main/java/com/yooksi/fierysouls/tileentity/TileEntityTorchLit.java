package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.block.BlockTorchUnlit;

import net.minecraft.network.Packet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTorchLit extends TileEntityTorch
{
	// Should we call server to extinguish the torch on it's side?
	private boolean extinguishTorchOnServer;
	
	// Should we check if torch flame could spread fire?
	private static boolean updateFlameHazard;
	
	public TileEntityTorchLit() {}	
	public TileEntityTorchLit(final long totalWorldTime) 
	{
		super(totalWorldTime);
		this.extinguishTorchOnServer = false;
		this.updateFlameHazard = true;
	}
	@Override
	public final void update()
	{
		// When it's raining and the torch is directly exposed to rain it will start collecting humidity.
		// Update humidity only on SERVER, we don't really need to do this on client.
		
		if (!getWorld().isRemote && getWorld().getWorldInfo().isRaining() && this.getWorld().canBlockSeeSky(pos))
		{
			if (this.updateHumidityLevel(HUMIDITY_AMOUNT_PER_TICK) > HUMIDITY_THRESHOLD)
				this.extinguishTorch(true);
		}
		if (this.updateFlameHazard == true)
		{
			this.setRoofOnFire(getWorld(), pos);
			this.updateFlameHazard = false;
		}
	}
	// When this update is requested we check if the torch should set the object above it on fire.
	public void scheduleHazardUpdate()
	{
		this.updateFlameHazard = true;
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
	    if (!waitForClient || (waitForClient && getWorld().isRemote))
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
		    	
		    	if (getWorld().isRemote)
		    	{
		    		torchUnlit.scheduleSmolderingEffect();
		    		TileEntity torchLit = net.minecraft.server.MinecraftServer.getServer().getEntityWorld().getTileEntity(pos);  
		            if (torchLit != null && torchLit instanceof TileEntityTorchLit)
		            	((TileEntityTorchLit)torchLit).extinguishTorch(false);
		    	}
		    	else    // <-- This will be called on SERVER side.
		    	{
		    		torchUnlit.updateHumidityLevel(this.getHumidityLevel());
	    		    torchUnlit.torchAge = getWorld().getTotalWorldTime() - this.timeCreated;     
		    	}
	    	}       // Notify the client that it should extinguish the torch on it's side and call back.
	    }             
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
	
	// ====================================== NETWORK UTILITIES ==============================================
	
	/** These functions are used to update, write and read packets sent from SERVER to CLIENT. */ 
	// This will make the server call 'getDescriptionPacket' for a full data sync
	@SideOnly(Side.SERVER)
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
		nbtTag.setBoolean("extinguishTorchOnServer", this.extinguishTorchOnServer);	
		
		super.writeToNBT(nbtTag);  // <-- This will only update entity pos, writeToNBT here to do a full sync.
		
		return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
	}
	// Extracts data from a packet that was sent from the server. Called on client only.
	// Minecraft automatically sends a 'description packet' for the tile entity when it is first 
	// loaded on the client, and you can force it to resend one afterwards
	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, S35PacketUpdateTileEntity packet) 
	{
		readFromNBT(packet.getNbtCompound());
		if (getWorld().isRemote && packet.getNbtCompound().getBoolean("extinguishTorchOnServer"))
			this.extinguishTorch(true);
	} 
}
