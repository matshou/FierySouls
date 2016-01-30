package com.yooksi.fierysouls.block;

import com.yooksi.fierysouls.item.ItemTorch;
import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.entity.item.EntityItemTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorchLit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorchLit extends com.yooksi.fierysouls.block.BlockTorch 
{
	// TODO: Move this value to a configuration file.
	public static final byte MAXIMUM_TORCH_LIGHT_LEVEL = 13;
	
	public BlockTorchLit() 
	{	
		this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
		this.setLightLevel((float)(MAXIMUM_TORCH_LIGHT_LEVEL / 15.0F));
	}

	@Override
	// This will disable objects from being able to be place on top of our torch
	public boolean isFullCube()
    {
		return false;
    }
	
	/**
     * Get a light value for the block at the specified coordinates, normal ranges are between 0 and 15. <br>
     * <i>Return a custom light value from torch tile entity.</i>
     *
     * @param world The current world
     * @param pos Block position in world
     * @return Custom light value
     */
	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos)
    {
		TileEntity torchEntity = world.getTileEntity(pos);
        return (torchEntity != null && torchEntity instanceof TileEntityTorchLit) ? 
        		((TileEntityTorchLit)torchEntity).getLightLevel() : super.getLightValue(world, pos);
    }
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, net.minecraft.entity.player.EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) 
	{
		// A lit torch can be extinguished if activated (right clicked) with no item equipped.
		// An unlit torch can be set on fire by activating it on an already lit torch. 
		
		ItemStack equippedItem = playerIn.getCurrentEquippedItem();
	    if (equippedItem == null)
	    {
	    	TileEntity torchEntity = worldIn.getTileEntity(pos);
	        if (torchEntity != null && torchEntity instanceof TileEntityTorchLit)
	        	((TileEntityTorchLit)worldIn.getTileEntity(pos)).extinguishTorch();
	        
	        return true;
	    }
	    else if (ResourceLibrary.isItemUnlitTorch(equippedItem.getItem()))
	    {
	    	// If the equipped stack has more then one item, keep one item, light it on fire
	    	// and move the rest of the stack in a different inventory slot (auto-assigned)
	    	
	    	if (equippedItem.stackSize > 1)
	    	{
	    		playerIn.inventory.addItemStackToInventory(new ItemStack(ResourceLibrary.TORCH_UNLIT.getItem(), equippedItem.stackSize - 1));  
	    		equippedItem.stackSize = 1;
	    	}
	    	
	    	ItemTorch.lightItemTorch(equippedItem);
	    	
	    	return true;      // Let the calling function know that this block was successfully used,
	     	                  // and there is no need to spawn the activation item as a block. 
	    }
	    // Never allow a torch to be placed like this...
	    else return (Block.getBlockFromItem(equippedItem.getItem()) instanceof BlockTorch);
	}
	
	/** Extinguish the torch by updating 'blockstate' at world coordinates.
	 * 
	 * @param world The instance of the world the torch is located in.
	 * @param pos Coordinates of the torch in the world.
	 * @return True if we successfully extinguished the torch.
	 */
	public static boolean extinguishTorch(World world, BlockPos pos)
	{
		// Find out the direction the torch is facing
		EnumFacing facing = (EnumFacing)world.getBlockState(pos).getValue(BlockTorch.FACING);
					
		// If the torch is not facing up but is placed on the side of a block we have to take into
		// account facing sides, otherwise the torch will detach from the wall and turn into an item.
		
		if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) 
			return world.setBlockState(pos, ResourceLibrary.TORCH_UNLIT.getBlock().getBlockState().getBaseState().withProperty(BlockTorch.FACING, facing)); 
	
		else return world.setBlockState(pos, ResourceLibrary.TORCH_UNLIT.getBlock().getDefaultState());
	}

	@Override
	//@SideOnly(Side.SERVER)
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) 
	{		
	    TileEntity torchEntity = worldIn.getTileEntity(pos);
        if (torchEntity != null && torchEntity instanceof TileEntityTorchLit)
        	((TileEntityTorchLit)torchEntity).torchFireHazardUpdate = true;

		super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{		
		return new TileEntityTorchLit(worldIn.getTotalWorldTime());
	}
}