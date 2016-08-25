package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.block.BlockTorch;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.common.SharedDefines;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;

public class TileEntityTorchLit extends TileEntityTorch
{
	/** The amount of ticks this torch will burn before extinguishing itself. */
	public static int MAX_TORCH_FLAME_DURATION;    
	
	// This constructor is NEEDED during entity loading by FML:
	public TileEntityTorchLit() {}
	
	public TileEntityTorchLit(long totalWorldTime) 
	{
		super(totalWorldTime);
	}
	
	@Override
	public void update()
	{
		if (!getWorld().isRemote)
		{
			if (updateTorchCombustionTime(SharedDefines.MAIN_UPDATE_INTERVAL * -1) <= 0)
				extinguishTorch();
			
		    // When it's raining and the torch is directly exposed to rain it will start collecting humidity.
		    if (getWorld().getWorldInfo().isRaining() && getWorld().canBlockSeeSky(pos))
		    {
			    if (updateTorchHumidityLevel(SharedDefines.MAIN_UPDATE_INTERVAL) >= HUMIDITY_THRESHOLD)		   
			    	extinguishTorch();
		    }	
		}
	}
	
	/** 
	 *  Extinguish the torch <b>block</b>, handle data inheritance and activate the smoldering effect. <br>
	 *  <i> Here we will check for humidity and combustion duration as well as handle data inheritance.</i>
	 *  
	 *  @return true if we successfully extinguished the torch.
	 */
	public final boolean extinguishTorch()
	{
	    if (!getWorld().isRemote && extinguishTorch(getWorld(), pos))
	    {
	    	// Find the newly created unlit torch entity and transfer all important data to it.
	    	
	    	TileEntityTorchUnlit unlitTorch = (TileEntityTorchUnlit)findTorchTileEntity(getWorld(), pos);
	    	unlitTorch.setTorchSmoldering(true, getWorld().getTotalWorldTime());
	    	unlitTorch.readFromNBT(saveDataToPacket());
	    	return true;
	    }
	    return false;
	}
	
	/** Extinguish the torch by updating 'blockstate' at world coordinates. <p>
     *  <i>This method only handles adding the block and tileEntity in the world.</i> 
	 * 
	 * @param world the instance of the world the torch is located in.
	 * @param pos coordinates of the torch in the world.
	 * @return true if we successfully extinguished the torch.
	 */
	private static boolean extinguishTorch(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos)
	{
		// Find out the direction the torch is facing
		EnumFacing facing = (EnumFacing)world.getBlockState(pos).getValue(BlockTorch.FACING);
				
		world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.AMBIENT, 0.15F, 0.5F, false);
				
		// If the torch is not facing up but is placed on the side of a block we have to take into
		// account facing sides, otherwise the torch will detach from the wall and turn into an item.
				
		if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) 
			return world.setBlockState(pos, ResourceLibrary.TORCH_UNLIT.getBlockState().getBaseState().withProperty(BlockTorch.FACING, facing)); 
			
		else return world.setBlockState(pos, ResourceLibrary.TORCH_UNLIT.getDefaultState());
	}
}