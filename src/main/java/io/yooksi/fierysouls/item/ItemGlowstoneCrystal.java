package com.yooksi.fierysouls.item;

import com.yooksi.fierysouls.common.FierySouls;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemGlowstoneCrystal extends Item 
{
	public static ItemGlowstoneCrystal localInstance;
	
	public ItemGlowstoneCrystal() 
	{  
		this.setCreativeTab(FierySouls.tabTorches);
		localInstance = this;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List<String> info, boolean par4) 
	{
		info.add(com.mojang.realmsclient.gui.ChatFormatting.ITALIC + "A magical crystal that produces an eternal glow.");
	}
}
