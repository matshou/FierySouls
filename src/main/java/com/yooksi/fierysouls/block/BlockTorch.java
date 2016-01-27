package com.yooksi.fierysouls.block;

import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorchLit;
import com.yooksi.fierysouls.entity.item.EntityItemTorch;
import com.yooksi.fierysouls.item.ItemTorch;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;

import net.minecraft.util.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockTorch extends net.minecraft.block.BlockTorch implements net.minecraft.block.ITileEntityProvider
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
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te)
    {
		if (!worldIn.isRemote && te != null && te instanceof TileEntityTorch)
		{
			player.triggerAchievement(net.minecraft.stats.StatList.mineBlockStatArray[getIdFromBlock(this)]);
		    player.addExhaustion(0.025F);

		    TileEntityTorch torchEntity = (TileEntityTorch)te;
		      
		    int fortune = net.minecraft.enchantment.EnchantmentHelper.getFortuneModifier(player);
		    java.util.List<ItemStack> items = getDrops(worldIn, pos, state, fortune);
	         
		    // Normally the super method would now call Block.dropBlockAsItem,
		    // however it does not pass the TileEntity reference to it and we need that here,
		    // so we're just going to invoke a part of #dropBlockAsItemWithChance to go around that.
		     
	        for (ItemStack item : items)
	        {
	        	if (Block.getBlockFromItem(item.getItem()) == this)
	        		spawnAsTorchEntity(worldIn, pos, item, torchEntity.saveDataToPacket());
	                	
	        	else super.spawnAsEntity(worldIn, pos, item);   // Spawn other items the default way
	        }
		}
		else super.harvestBlock(worldIn, player, pos, state, te);
    }
	
	/**
     * Spawns this Block's drops into the World as EntityItems.
     * The only thing we changed while overriding this method is that {@link Block#spawnAsEntity} 
     * has now been redirected to an internal method and we check for tile entity.
     *  
     * @param chance The chance that each Item is actually spawned (1.0 = always, 0.0 = never)
     * @param fortune The player's fortune level
     */
	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
		// Since the whole reason we're overriding this method is so we can inject
		// our own ItemEntity into the world, if we can't find the right tile entity we can't get the
		// tile entity data and there really is no need for any custom injection here.
		
		// NOTE: Do not call this method when harvesting the block, at this stage the TileEntity
		//       has already been removed from world and only exists as a passed reference. Call #harvestBlock method instead.
		
		net.minecraft.tileentity.TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity != null && tileEntity instanceof TileEntityTorch)
        {
        	if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) // do not drop items while restoring blockstates, prevents item dupe
        	{  
        		TileEntityTorch torchTileEntity = (TileEntityTorch)tileEntity;    		
        		java.util.List<ItemStack> items = getDrops(worldIn, pos, state, fortune);
	            
	            for (ItemStack item : items)
	            {
	            	if (Block.getBlockFromItem(item.getItem()) == this)
	            		spawnAsTorchEntity(worldIn, pos, item, torchTileEntity.saveDataToPacket());
	                
	            	else super.spawnAsEntity(worldIn, pos, item);   // Spawn other items the default way.
	            }
        	}
        }
        else super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }
	/**
     * Spawns the given ItemStack as an EntityItem into the World at the given position. <p>
     * Almost identical to {@link Block#spawnAsEntity}, difference being this method spawns a custom EntityItem.
     *
     * @param tagCompound NBTTagCompound that holds TileEntityTorch data.
     * @see from {@link #dropBlockAsItemWithChance} override method in this class.
     */
	private static void spawnAsTorchEntity(World worldIn, BlockPos pos, ItemStack stack, NBTTagCompound tagCompound)
	{
		// I don't exactly know why we're checking this but Block class does it too.
		if (worldIn.getGameRules().getGameRuleBooleanValue("doTileDrops")) 
		{
			if (!captureDrops.get())
            { 
                // When the torch is dropped as an item in this way 
				// it has a 50-50% chance of being extinguished
				
				if (worldIn.rand.nextInt(2) < 1)
                	stack.setItem(ResourceLibrary.TORCH_UNLIT.getItem());             
                    
                EntityItem entityitem = EntityItemTorch.createDroppedEntityItem(worldIn, pos, stack, tagCompound);
                worldIn.spawnEntityInWorld(entityitem);
            }
			else capturedDrops.get().add(stack);
		}
	}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		// Auto-generated method stub, will be overridden by child classes
		return null;
	}
}
