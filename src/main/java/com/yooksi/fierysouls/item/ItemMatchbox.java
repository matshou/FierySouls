package com.yooksi.fierysouls.item;

import com.yooksi.fierysouls.common.FierySouls;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemMatchbox extends Item
{
	public static ItemMatchbox localInstance;
	
	public ItemMatchbox() 
	{
		this.setMaxStackSize(1);     // Sets how much items of this type can fit in one slot
		this.setMaxDamage(25);      // How many matches do we get in the matchbox (item durability)
		this.setNoRepair();
		
		this.setCreativeTab(CreativeTabs.TOOLS);
		this.setCreativeTab(FierySouls.tabTorches);
		localInstance = this;
	}
}
