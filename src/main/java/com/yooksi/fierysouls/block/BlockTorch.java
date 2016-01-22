package com.yooksi.fierysouls.block;

import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorchLit;
import com.yooksi.fierysouls.entity.item.EntityItemTorch;
import com.yooksi.fierysouls.item.ItemTorch;

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
                double posX = (double)pos.getX() + ((double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D); 
                double posY = (double)pos.getY() + ((double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D); 
                double posZ = (double)pos.getZ() + ((double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D);
                
                // When the torch is dropped as an item it should be extinguished.
                // Create a custom NBT for the stack here before we create the new entity item.
   
                stack.setItem(ResourceLibrary.TORCH_UNLIT.getItem());             
                ItemTorch.createCustomItemNBTFromExisting(stack, tagCompound);
                
                EntityItem entityitem = new EntityItemTorch(worldIn, posX, posY, posZ, stack);
                entityitem.setDefaultPickupDelay(); worldIn.spawnEntityInWorld(entityitem);
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
