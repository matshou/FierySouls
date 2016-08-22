package com.yooksi.fierysouls.item;

import com.yooksi.fierysouls.common.FierySouls;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemGlowstoneCrystal extends Item 
{
	public static ItemGlowstoneCrystal localInstance;
	
	public ItemGlowstoneCrystal() 
	{  
		this.setCreativeTab(CreativeTabs.MATERIALS);
		this.setCreativeTab(FierySouls.tabTorches);
		localInstance = this;
	}
}
