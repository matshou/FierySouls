package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.block.BlockTorch;
import com.yooksi.fierysouls.common.Utilities;
import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.common.SharedDefines;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.block.BlockFire;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class TileEntityTorchLit extends TileEntityTorch
{
	/** The amount of ticks this torch will burn before extinguishing itself. */
	public static int MAX_TORCH_FLAME_DURATION;

	/**
	 * Base value for calculating chances that a block will be set on fire by a torch. It's used to generate  <br> 
	 * a random number from 0 to this value. The higher this number, the lower the chances. <br>
	 * By default this value is set to 100 per second (20 ticks) and is affected by the update interval. <p>
	 * 
	 * <b>Example:</b> Planks have 20% chance to catch on fire each update interval when this value is set to 100, <br>
	 *             as the flammability of planks is set to 20 and we're generating a random number from 0 to 100. <p>
	 *  
	 * <i>Check {@link BlockFire#getFlammability} for a list of block flammability values.</i>
	 *  */
	public static int CATCH_FIRE_CHANCE_BASE = (int) (100 * (double)(20 / SharedDefines.MAIN_UPDATE_INTERVAL)); // Set the chance value to be 100 per second.
	
	// This constructor is NEEDED during entity loading by FML:
	public TileEntityTorchLit() {}
	
	public TileEntityTorchLit(long totalWorldTime) 
	{
		super(totalWorldTime);
	}
	
	@Override
	public void update()
	{
		if (!isTorchReadyForUpdate())
			return;
		
		if (!getWorld().isRemote)
		{
			// Game rule that defines whether fire should spread and naturally extinguish.
			if (getWorld().getGameRules().getBoolean("doFireTick"))
	        {
				BlockPos neighborPos = getPos().offset(EnumFacing.UP);
				Block neighborBlock = getWorld().getBlockState(neighborPos).getBlock();
				
				if (CATCH_FIRE_CHANCE_BASE > 0 && neighborBlock != Blocks.AIR)
				{
					for (EnumFacing face : EnumFacing.values())
						this.tryCatchFire(neighborBlock, neighborPos, CATCH_FIRE_CHANCE_BASE, face);
				}
	        }
			
			if (updateTorchCombustionTime(SharedDefines.MAIN_UPDATE_INTERVAL * -1) <= 0)
				extinguishTorch();
			
		    // When it's raining and the torch is directly exposed to rain it will start collecting humidity.
		    if (getWorld().isRaining() && getWorld().canBlockSeeSky(pos))
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
	private static boolean extinguishTorch(net.minecraft.world.World world, BlockPos pos)
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
	
	/** Try to catch the block above (<i>and sometimes those around</i>) the torch on fire. 
	 * 
	 *  @param targetBlock block we are trying to set on fire <i>(checking flammability of this block)</i>.
	 *  @param blockPos world position of the target block <i>(spawning fire on this location)</i>.
	 *  @param chance the higher this number the less likely it is that the block will be set on fire.
	 *  @param face the face of the block we will try to set on fire.
	 *  
	 *  @return true if we set the block on fire, false otherwise.
	 *  */
	private boolean tryCatchFire(Block targetBlock, BlockPos blockPos, int chance, EnumFacing face)
	{	
		if (face.getAxis().isHorizontal())
		{
			blockPos = blockPos.offset(face);
			Block topBlock = getWorld().getBlockState(blockPos).getBlock();
			Block supportBlock = getWorld().getBlockState(blockPos.down()).getBlock();

			if (supportBlock != Blocks.AIR)     // block one step below top block.
			{
				targetBlock = supportBlock;
				blockPos = blockPos.down();
				face = face.getOpposite();
				chance = Utilities.modifyChance(chance, -60); // -60% chance for difficulty.
			}
			else if (topBlock != Blocks.AIR)    // target block's horizontal neighbor.
			{
				targetBlock = topBlock;
				chance = Utilities.modifyChance(chance, -15); // -15% chance for difficulty.
			}
			else chance = Utilities.modifyChance(chance, 25); // +25% chance for visuals.
		}
		else if (face == EnumFacing.UP)
		{
			return false;   // torch flame cannot reach that side.
		}
		
		if (targetBlock.isFlammable(getWorld(), blockPos, face))
		{
			int flammability = targetBlock.getFlammability(getWorld(), blockPos, face);
			java.util.Random rand = new java.util.Random();
			
			if (rand.nextInt(chance) < flammability)
				return getWorld().setBlockState(blockPos, Blocks.FIRE.getDefaultState(), 3);
		}
		
		return false;
	}
}