package com.yooksi.fierysouls.block;

import java.util.Random;
import javax.annotation.Nullable;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorchUnlit extends com.yooksi.fierysouls.block.BlockTorch
{	
	public static BlockTorchUnlit localInstance;
	
	public BlockTorchUnlit()
	{
		this.setCreativeTab(FierySouls.tabTorches);
		localInstance = this;
	}
	
	/** This function will be called every 'random' tick and is usually used
	 *  to spawn fire particles<br> from the top of the torch.
	 */
	@Override
    @SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		return;  // This will block the fire particle spawn on unlit torches.
	}
	
	/** This function is called by Minecraft whenever a block is right-clicked.
	 *  @return if return value is true the block held in hand will be placed as a block on the ground.
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		// For some reason sometimes both the SERVER and CLIENT pass a null value for 'heldItem', fix this:
		if (heldItem == null && playerIn.inventory.getCurrentItem() != null)
			heldItem = playerIn.inventory.getCurrentItem();
		
		if (heldItem == null) 
			return false;
		
		else if (BlockTorch.isItemTorchLit(heldItem.getItem()))
		{	
			lightTorch(worldIn, pos);
			return true;               // Let the calling function know that this block was successfully used,
			                          // and there is no need to spawn the item as block.
		}
		// Never allow a torch to be placed like this...
		else return BlockTorch.isItemTorch(heldItem.getItem());
	}
	
	/** Set the torch on fire by updating 'blockstate' at world coordinates.
	 * 
	 * @param world the instance of the world the torch is located in.
	 * @param pos coordinates of the torch in the world.
	 * @return true if we successfully lit the torch on fire.
	 */
	public static boolean lightTorch(World world, BlockPos pos)
	{
		// Find out the direction the torch is facing
		EnumFacing facing = (EnumFacing)world.getBlockState(pos).getValue(BlockTorch.FACING);
		
		// If the torch is not facing up but is placed on the side of a block we have to take into
		// account facing sides, otherwise the torch will detach from the wall and turn into an item.
		
		if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) 
			return world.setBlockState(pos, ResourceLibrary.TORCH_LIT.getBlockState().getBaseState().withProperty(BlockTorch.FACING, facing)); 
	
		else return world.setBlockState(pos, ResourceLibrary.TORCH_LIT.getDefaultState());
	}
}
