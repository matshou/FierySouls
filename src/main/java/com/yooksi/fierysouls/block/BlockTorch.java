package com.yooksi.fierysouls.block;

import javax.annotation.Nullable;

import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.entity.item.EntityItemTorch;
import com.yooksi.fierysouls.item.ItemTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockTorch extends net.minecraft.block.BlockTorch
{
	/**
	 * Drop the block as item when harvested by player. <br>
	 * Called when the player destroys the block by left-clicking it in survival mode. <p>
	 * 
	 * <i>Note: This method is not triggered in creative mode.</i>
	 * 
	 * @param worldIn The world instance where the player is harvesting the block
	 * @param player EntityPlayer harvesting the block
	 * @param pos Position of the block that's being harvested
	 * @param state Current state of the block
	 * @param te TileEntity that belongs to this block
	 */
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack)
    {
		player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.025F);
		
		if (!worldIn.isRemote && te != null && te instanceof TileEntityTorch)
		{
        	TileEntityTorch torchEntity = (TileEntityTorch)te;
			java.util.List<ItemStack> items = new java.util.ArrayList<ItemStack>();
            ItemStack itemstack = this.createStackedBlock(state);

            if (itemstack != null)
        	    items.add(itemstack);

            for (ItemStack item : items)
            {
            	// This is where we pass our custom NBT from TileEntity to the new ItemStack.
            	if (ItemTorch.isItemTorch(item.getItem(), false) && torchEntity != null)
					 ItemTorch.createCustomItemNBTFromExisting(item, torchEntity.saveDataToPacket(), worldIn.getTotalWorldTime());
          
            	spawnAsEntity(worldIn, pos, item);
            }
		}
    }

	/** 
	 *  Will be called when a block beneath the block is broken and the item is forced to drop. <br> 
	 *  For regular drops we should use {@link #harvestBlock}.
	 */
	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
		TileEntityTorch teTorch = (TileEntityTorch) TileEntityTorch.findTorchTileEntity(worldIn, pos);
    
		 if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
		 {
			 java.util.List<ItemStack> items = getDrops(worldIn, pos, state, fortune);
			 chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, fortune, chance, false, harvesters.get());

			 for (ItemStack item : items)
			 {
				// This is where we pass our custom NBT from TileEntity to the new ItemStack.
				 if (ItemTorch.isItemTorch(item.getItem(), false) && teTorch != null)
					 ItemTorch.createCustomItemNBTFromExisting(item, teTorch.saveDataToPacket(), worldIn.getTotalWorldTime());
				 
				 if (worldIn.rand.nextFloat() <= chance)
					 spawnAsEntity(worldIn, pos, item);			 
			 }
		 }
    }
}