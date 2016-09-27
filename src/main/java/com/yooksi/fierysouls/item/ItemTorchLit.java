package com.yooksi.fierysouls.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTorchLit extends ItemTorch 
{
	public ItemTorchLit(Block block) 
	{
		super(block);
		this.setMaxStackSize(1);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List<String> info, boolean par4) 
	{
		info.add(com.mojang.realmsclient.gui.ChatFormatting.ITALIC + "A simple wooden torch.");
		info.add(com.mojang.realmsclient.gui.ChatFormatting.ITALIC + "It's on fire.");
	}
}
