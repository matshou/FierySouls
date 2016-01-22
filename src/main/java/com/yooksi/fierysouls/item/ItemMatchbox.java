package com.yooksi.fierysouls.item;

import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.tileentity.TileEntityTorchUnlit;

import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.util.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;

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
		
		// If used on torch, light it on fire
		if (worldIn.getBlockState(pos).getBlock() == ResourceLibrary.TORCH_UNLIT.getBlock())
		{
			TileEntity entityTorch = worldIn.getTileEntity(pos);
    		if (entityTorch != null && entityTorch instanceof TileEntityTorchUnlit)
    		   ((TileEntityTorchUnlit)entityTorch).lightTorch();
		}			
		return true;  // Always allow the item to be used
	}
}
