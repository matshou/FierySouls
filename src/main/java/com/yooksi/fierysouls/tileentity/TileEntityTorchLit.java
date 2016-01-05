package com.yooksi.fierysouls.tileentity;

import java.util.Random;

import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.block.BlockTorchUnlit;
import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTorchLit extends TileEntity implements IUpdatePlayerListBox
{
	// Time in the world this tile entity was created
	private final long timeCreated;
	
	// Timeout here is expressed in ticks. How long should we ideally wait for the client?
	private static int clientSyncTimeout = 20;
	
	public TileEntityTorchLit(final long totalWorldTime) 
	{
		this.timeCreated = totalWorldTime;
	}
	
	// This updates every tileEntity tick on both client and server side
	public final void update()
	{
		// When it's raining and the torch is directly exposed to rain it should get extinguished.
		// Make sure to not change blockstate until the client has done so first.
		
		if ((getWorld().getWorldInfo().isRaining() && this.getWorld().canBlockSeeSky(pos)))
		{
			
			// TODO: Make these values more dynamic and move them into a #define or a configuration file.
			
			// Don't extinguish the torch until the rain is strong enough to do so.
			// After we place the torch on rain, wait a bit before extinguishing it.
			
			if (getWorld().getRainStrength(1) > 0.85F && (getWorld().getTotalWorldTime() - this.timeCreated) > 80)
			    this.extinguishTorch(true);
		}	
		else if (this.updateFlameHazard == true)
		{
			this.updateFlameHazard = false;
			if (!getWorld().canBlockSeeSky(pos))
				this.setRoofOnFire(getWorld(), pos);
		}
	}
	// Should we check if torch flame could spread fire?
	private static boolean updateFlameHazard = true;
	
	public void scheduleHazardUpdate()
	{
		this.updateFlameHazard = true;
	}
	
	/** Replace it with an unlit one and activate the smoldering effect 
	 *  Do this on both side.SERVER and side.CLIENT
	 *  
	 *  @param waitForClient Should we delay this a bit and wait for client to sync? This is done when raining.
	 */
	public void extinguishTorch(final boolean waitForClient)
	{
		// TODO: Find a more elegant solution to solve this. A client-server sync via custom packet payload would be ideal!
		//
		// Once our torch meets the conditions to be extinguished the first one to find out is the SERVER.
		// The problem is that if we change the block it will remove both client and server tile entities,
		// which means the client tile entity will be destroyed without getting in sync and doing it's stuff client side.
		//
		// A bit of a dirty workaround here is to delay extinguishing the torch server side until (hopefully) the client comes back in sync.
		// How are they out of sync? When the weather changes (rain starts falling) the client gets notified about this AFTER the SERVER.
		// We will just have to wait a little bit until the client finds out it's raining.
		
		if (waitForClient && !getWorld().isRemote && this.clientSyncTimeout > 0)
			this.clientSyncTimeout -= 1; 

		// Again, make sure that the torch is first extinguished on CLIENT side; 
		// otherwise we will not see those smoke particle effects in play.
		
	    else if (BlockTorchLit.extinguishTorch(getWorld(), pos))
		{
			TileEntity torchEntity = getWorld().getTileEntity(pos);
		    if (torchEntity != null && torchEntity instanceof TileEntityTorchUnlit)
		    	((TileEntityTorchUnlit)torchEntity).shouldAddSmolderingEffect = true;
		}
	}
	
	/** Check to see if we should force the block above us to catch on fire.
	 *  If we roll positive the torch will assume the function of a fire block with limited spreading movement.
	 */
	private static boolean setRoofOnFire(World worldIn, BlockPos pos)
	{
		BlockPos neighbourPos = new BlockPos(pos.getX(), pos.getY() +1, pos.getZ());
		Block neighbourBlock = worldIn.getBlockState(neighbourPos).getBlock();
			
		// TODO: Create more advanced parameters like taking into account 
		//       air humidity, strength of torch flame etc.
			
		final int chancesToCatchFire = neighbourBlock.getFlammability(worldIn, neighbourPos, EnumFacing.DOWN);
		
		Random rand = new Random();
		int natural_roll = rand.nextInt(100) + 1;     // 0% - 100% (1 - 100 roll)
	
		// If a saving throw failed set the top block on fire
		if (chancesToCatchFire >= natural_roll)
		{
			worldIn.setBlockState(neighbourPos, Blocks.fire.getDefaultState());	
			return true;
		}
		else return false;
	}	
	
	/** These functions are used to update, write and read packets sent from SERVER to CLIENT. 
	 *  For now we don't have any use for these, might want to use them in the future.
	 
	// This will make the server call 'getDescriptionPacket' for a full data sync
	private void markForUpdate()
	{
		// It would seem these updates only work SERVER - CLLIENT,
		// so there is no need to do this on a client	
		
		if (!getWorld().isRemote)
		{
			getWorld().markBlockForUpdate(pos);
		    this.markDirty();
		}
	}
	@Override
	public Packet getDescriptionPacket() 
	{
		 NBTTagCompound nbtTag = new NBTTagCompound();
		 this.writeToNBT(nbtTag);
		 return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
	}
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) 
	{
		 readFromNBT(packet.getNbtCompound());
	}
	@Override
	public void writeToNBT(NBTTagCompound par1)
	{
		 par1.setBoolean("torchExtinguished", this.torchExtinguished);
	}  
	@Override
	public void readFromNBT(NBTTagCompound par1)
	{  
		this.torchExtinguished = par1.getBoolean("torchExtinguished");
	}*/
}
