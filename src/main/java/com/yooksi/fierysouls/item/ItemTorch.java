package com.yooksi.fierysouls.item;

import com.yooksi.fierysouls.block.BlockTorch;
import com.yooksi.fierysouls.common.ResourceLibrary;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTorch extends ItemBlock
{
	public ItemTorch(Block block) 
	{
		super(block);
		this.setMaxDamage(-1);   // Disable vanilla damage and use "torchItemDamage" NBT value instead.
	}

	/** Check to see if an item is a real torch (both custom or vanilla torch). */
    public static boolean isItemTorch(Item item)
    {
    	return Block.getBlockFromItem(item) instanceof BlockTorch || item == Item.getItemFromBlock(Blocks.TORCH);
    }
    
    /** Check to see if an item is a lit torch (both custom or vanilla torch). */
    public static boolean isItemTorchLit(Item item)
    {
    	return item == Item.getItemFromBlock(ResourceLibrary.TORCH_LIT) || item == Item.getItemFromBlock(Blocks.TORCH);
    }
	
	/*
	 *  Called each tick as long the item is on a player inventory.
	 */
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    }
    
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List<String> info, boolean par4) 
	{
		info.add(com.mojang.realmsclient.gui.ChatFormatting.ITALIC + "A simple wooden torch.");
		info.add(com.mojang.realmsclient.gui.ChatFormatting.ITALIC + (isItemTorchLit(stack.getItem()) ? "It's on fire." : "It's extinguished."));
	}
}
