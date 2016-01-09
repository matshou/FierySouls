package com.yooksi.fierysouls.block;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.tileentity.TileEntityTorchLit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

// This block replaces the original Minecraft torch with a more realistic one. 

public class BlockTorchLit extends BlockTorch implements net.minecraft.block.ITileEntityProvider
{
	public BlockTorchLit() 
	{	
		this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
		this.setLightLevel(0.9375F);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, net.minecraft.entity.player.EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) 
	{
		// A lit torch can be extinguished if activated (right clicked) with no item equipped.
		// An unlit torch can be set on fire by activating it on an already lit torch. 
		
		ItemStack equippedItem = playerIn.getCurrentEquippedItem();
	    if (equippedItem == null)
	    {
	    	FierySouls.logger.info("Called to extinguish  22222222222!");
	    	
	    	TileEntity torchEntity = worldIn.getTileEntity(pos);
	        if (torchEntity != null && torchEntity instanceof TileEntityTorchLit)
	        	((TileEntityTorchLit)worldIn.getTileEntity(pos)).extinguishTorch(true);
	        
	        return true;
	    }
	    else if (equippedItem.getItem() == ResourceLibrary.TORCH_UNLIT.getItemInstance())
	    {
	    	// Remove the old (unlit) and place a new (lit) torch in our hands.
	    	// If we tried to light a torch stack; decrement the stack and move it to a new inventory slot
	        
	    	int torchStackSize = equippedItem.stackSize;
	    	playerIn.setCurrentItemOrArmor(0, new ItemStack(ResourceLibrary.TORCH_LIT.getItemInstance(), 1));
	    	
	    	if (torchStackSize > 1)
	    		playerIn.inventory.addItemStackToInventory(new ItemStack(ResourceLibrary.TORCH_UNLIT.getItemInstance(), torchStackSize - 1));  
	    
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
			return world.setBlockState(pos, ResourceLibrary.TORCH_UNLIT.getBlockInstance().getBlockState().getBaseState().withProperty(BlockTorch.FACING, facing)); 
	
		else return world.setBlockState(pos, ResourceLibrary.TORCH_UNLIT.getBlockInstance().getDefaultState());
	}
	
	@Override
	// This will disable objects from being able to be place on top of our torch
	public boolean isFullCube()
    {
		return false;
    }
	
	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) 
	{
		// When a block around has changed we should check if it should be set on fire
		
		TileEntity torchEntity = worldIn.getTileEntity(pos);
        if (torchEntity != null && torchEntity instanceof TileEntityTorchLit)
        	((TileEntityTorchLit)torchEntity).scheduleHazardUpdate();
		
		this.onNeighborChangeInternal(worldIn, pos, state);  // Vanilla Forge code, DON'T remove it!
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		FierySouls.logger.info("Created a lit torch entity!");
		return new TileEntityTorchLit(worldIn.getTotalWorldTime());
	}
}