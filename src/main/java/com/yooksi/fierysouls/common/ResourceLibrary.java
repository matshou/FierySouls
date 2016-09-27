package com.yooksi.fierysouls.common;

import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.block.BlockTorchUnlit;
import com.yooksi.fierysouls.item.ItemGlowstoneCrystal;
import com.yooksi.fierysouls.item.ItemMatchbox;

/** This class is a storage of unique block and item instances placed here for ease of access. */
public class ResourceLibrary 
{
	public static final BlockTorchLit TORCH_LIT;
	public static final BlockTorchUnlit TORCH_UNLIT;
	
	public static final ItemMatchbox MATCHBOX;
	public static final ItemGlowstoneCrystal GLOWSTONE_CRYSTAL;
	
	static
	{
		TORCH_LIT = BlockTorchLit.localInstance;
		TORCH_UNLIT = BlockTorchUnlit.localInstance;
		
		MATCHBOX = ItemMatchbox.localInstance;
		GLOWSTONE_CRYSTAL = ItemGlowstoneCrystal.localInstance;
	}
}
