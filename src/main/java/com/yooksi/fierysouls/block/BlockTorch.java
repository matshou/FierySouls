package com.yooksi.fierysouls.block;

import javax.annotation.Nullable;

import com.yooksi.fierysouls.entity.item.EntityItemTorch;
import com.yooksi.fierysouls.item.ItemTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
		
		if (te != null && te instanceof TileEntityTorch)
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
					 ItemTorch.createCustomItemNBTFromExisting(item, worldIn, torchEntity.saveDataToPacket());
          
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
		TileEntityTorch teTorch = (TileEntityTorch) TileEntityTorch.findTorchTileEntity(worldIn, pos, true);
    
		 if (!worldIn.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
		 {
			 java.util.List<ItemStack> items = getDrops(worldIn, pos, state, fortune);
			 chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, fortune, chance, false, harvesters.get());

			 for (ItemStack item : items)
			 {
				// This is where we pass our custom NBT from TileEntity to the new ItemStack.
				if (ItemTorch.isItemTorch(item.getItem(), false) && teTorch != null)
				{
					ItemTorch.createCustomItemNBTFromExisting(item, worldIn, teTorch.saveDataToPacket());
					if (worldIn.rand.nextFloat() <= chance)
						spawnAsTorchEntity(worldIn, pos, item);
				}
				else if (worldIn.rand.nextFloat() <= chance)
					 spawnAsEntity(worldIn, pos, item);			 
			 }
		 }
    }
	
	/** 
	 *  A slightly modified version of {@link Block#spawnAsEntity} designed to create <br>
	 *  and spawn our own custom torch entity. 
	 */
	private void spawnAsTorchEntity(World worldIn, BlockPos pos, ItemStack stack)
	{
		if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops") && !worldIn.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
        {
            if (!captureDrops.get())
            {
            	float f = 0.5F;
                double d0 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
                double d1 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
                double d2 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
                
                EntityItem entityitem = new EntityItemTorch(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
                entityitem.setDefaultPickupDelay();
                worldIn.spawnEntityInWorld(entityitem);
            }
            else capturedDrops.get().add(stack);
        }
	}
}