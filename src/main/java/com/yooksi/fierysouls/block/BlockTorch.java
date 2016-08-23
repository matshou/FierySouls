package com.yooksi.fierysouls.block;

import com.yooksi.fierysouls.common.ResourceLibrary;

import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public abstract class BlockTorch extends net.minecraft.block.BlockTorch
{
	
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