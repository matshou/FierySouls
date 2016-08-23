package com.yooksi.fierysouls.block;

import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;

import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.item.Item;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;

public abstract class BlockTorch extends net.minecraft.block.BlockTorch implements ITileEntityProvider
{
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileEntityTorch(worldIn.getTotalWorldTime());
	}
	
	/** Check to see if an item is a real torch (both custom or vanilla torch). */
    protected static boolean isItemTorch(Item item)
    {
    	return Block.getBlockFromItem(item) instanceof BlockTorch || item == Item.getItemFromBlock(Blocks.TORCH);
    }
    
    /** Check to see if an item is a lit torch (both custom or vanilla torch). */
    protected static boolean isItemTorchLit(Item item)
    {
    	return item == Item.getItemFromBlock(ResourceLibrary.TORCH_LIT) || item == Item.getItemFromBlock(Blocks.TORCH);
    }
}