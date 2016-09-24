package com.yooksi.fierysouls.block;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.common.Utilities;
import com.yooksi.fierysouls.item.ItemTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorchLit;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorchLit extends com.yooksi.fierysouls.block.BlockTorch implements ITileEntityProvider
{
	public static BlockTorchLit localInstance;
	public static int MAX_TORCH_LIGHT_LEVEL;
	
	/** 
	 * If the block has changed internal light value then this should be set to true
	 * so we make a global light update for every lit torch in the world. 
	 */ 
	private boolean shouldUpdateLight = false;
	
	public BlockTorchLit() 
	{	
		this.setCreativeTab(FierySouls.tabTorches);
		updateBlockLightLevel();
		localInstance = this;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileEntityTorchLit(worldIn.getTotalWorldTime());
	}
	
    /** 
     * Primarily used to update light level after config values have changed. <br>
     * <b>Note:</b> <i>this will only update the light value of the blocks themselves, not those around them.</i>
     * 
     * @param newLightLevel the value is not checked for min-max, but make sure it's between 0 - 15.
     */
	public void updateBlockLightLevel(int newLightLevel)
	{
		MAX_TORCH_LIGHT_LEVEL = newLightLevel;
		updateBlockLightLevel();
		shouldUpdateLight = true;
	}
	private void updateBlockLightLevel()
	{
		this.setLightLevel((float)(MAX_TORCH_LIGHT_LEVEL / 15.0F));
	}
	
	/** This function will be called every 'random' tick and is usually used
	 *  to spawn fire particles<br> from the top of the torch.
	 */
	@Override
    @SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		super.randomDisplayTick(stateIn, worldIn, pos, rand);
		
		if (rand.nextInt(4) == 0) // Every random tick (20% chance per tick) play the fire ambient sound.
        {
			// TODO: The sound volume needs further testing here, I have a feeling like it's a bit too loud at times.
            worldIn.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, rand.nextFloat() / 4.0F,  rand.nextFloat() * 0.7F + 0.3F, false);
        }
		
		/* Check to see if we need to update the light values of adjacent blocks. 	
		 * Every lit torch will be affected by this and their lights will be updated.
		 * If the player is entering the world and we update like this there will be a 
		 * short time delay before the update takes affect on client side. */
		 
		if (shouldUpdateLight == true)
		{
			// TODO: Make the light update much faster.
			worldIn.checkLightFor(EnumSkyBlock.BLOCK, pos);
			shouldUpdateLight = false;
		}
		
		return;  // This will block the fire particle spawn on unlit torches.
	}
	
	/** This function is called by Minecraft whenever a block is right-clicked.
	 *  @return if return value is true the block held in hand will be placed as a block on the ground.
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		Item torchUnlit = Item.getItemFromBlock(ResourceLibrary.TORCH_UNLIT);
		
		// For some reason sometimes both the SERVER and CLIENT pass a null value for 'heldItem', fix this:
		if (heldItem == null && playerIn.inventory.getCurrentItem() != null)
			heldItem = playerIn.inventory.getCurrentItem();
		
		if (heldItem == null)
		{
			TileEntityTorchLit.findLitTorchTileEntity(worldIn, pos).extinguishTorch();
			return true;
		}
		else if (heldItem.getItem() == torchUnlit)
		{
			// If the equipped stack has more then one item, keep one item, light it on fire
	    	// and move the rest of the stack in a different inventory slot (auto-assigned)
	    	
			playerIn.replaceItemInInventory(playerIn.inventory.currentItem, new ItemStack(Item.getItemFromBlock(ResourceLibrary.TORCH_LIT)));
			
	    	if (heldItem.stackSize > 1)
	    	{
	    		heldItem.stackSize -= 1; 
	    		playerIn.inventory.addItemStackToInventory(heldItem);
	    	}
	    	
	    	return true;      // Let the calling function know that this block was successfully used,
                              // and there is no need to spawn the activation item as a block. 
		}
		// Never allow a torch to be placed like this...
		else return ItemTorch.isItemTorch(heldItem.getItem());
	}
	
	/** Extinguish the torch block. Find the torch tile entity and delegate the call.
	 * 
	 * @param world The instance of the world the torch is located in.
	 * @param pos Coordinates of the torch in the world.
	 * @return True if we successfully extinguished the torch.
	 */
	public static boolean extinguishTorch(World world, BlockPos pos)
	{
		return TileEntityTorchLit.findLitTorchTileEntity(world, pos).extinguishTorch();
	}
}