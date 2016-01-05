package com.yooksi.fierysouls.item;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import com.yooksi.fierysouls.block.BlockTorchUnlit;
import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;

public class ItemMatchbox extends Item
{
	public ItemMatchbox() 
	{
		this.setMaxStackSize(1);     // Sets how much items of this type can fit in one slot
		this.setMaxDamage(25);      // How many matches do we get in the matchbox (item durability)
		this.setNoRepair();
		
		this.setCreativeTab(CreativeTabs.tabTools);
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		// The player uses one match to try to light something on fire:
		stack.damageItem(1, playerIn);
		
		// Can't start a fire on wet ground
		if (worldIn.getWorldInfo().isRaining() && worldIn.canSeeSky(pos))
		   return true;
		
		// If used on torch, light it on fire
		if (worldIn.getBlockState(pos).getBlock() == ResourceLibrary.TORCH_UNLIT.getBlockInstance())
			BlockTorchUnlit.lightTorch(worldIn, pos);
			
		return true;  // Always allow the item to be used
	}
}
