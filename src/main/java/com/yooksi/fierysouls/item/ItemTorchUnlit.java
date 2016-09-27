package com.yooksi.fierysouls.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTorchUnlit extends ItemTorch 
{
	public ItemTorchUnlit(Block block) 
	{
		super(block);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List<String> info, boolean par4) 
	{
		info.add(com.mojang.realmsclient.gui.ChatFormatting.ITALIC + "A simple wooden torch.");
		info.add(com.mojang.realmsclient.gui.ChatFormatting.ITALIC + "It's not burning.");
	}
}
