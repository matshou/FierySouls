package com.yooksi.fierysouls.block;

import com.yooksi.fierysouls.common.FierySouls;

public class BlockTorchLit extends com.yooksi.fierysouls.block.BlockTorch 
{
	public static BlockTorchLit localInstance;
	
	public BlockTorchLit() 
	{	
		this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS);
		this.setCreativeTab(FierySouls.tabTorches);
		localInstance = this;
	}
}