package com.yooksi.fierysouls.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorchLit extends com.yooksi.fierysouls.block.BlockTorch 
{
	public static BlockTorchLit localInstance;
	
	public BlockTorchLit() 
	{	
		this.setCreativeTab(FierySouls.tabTorches);
		this.setLightLevel((float)(13.0F / 15.0F));  // TODO: Move this to a config file.
		localInstance = this;
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
			extinguishTorch(worldIn, pos);
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
		else return BlockTorch.isItemTorch(heldItem.getItem());
	}
	
	/** Extinguish the torch by updating 'blockstate' at world coordinates.
	 * 
	 * @param world The instance of the world the torch is located in.
	 * @param pos Coordinates of the torch in the world.
	 * @return True if we successfully extinguished the torch.
	 */
	public static boolean extinguishTorch(World world, BlockPos pos)
	{
		// Find out the direction the torch is facing
		EnumFacing facing = (EnumFacing)world.getBlockState(pos).getValue(BlockTorch.FACING);
					
		// If the torch is not facing up but is placed on the side of a block we have to take into
		// account facing sides, otherwise the torch will detach from the wall and turn into an item.
		
		world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.AMBIENT, 0.15F, 0.5F, false);
		
		if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) 
			return world.setBlockState(pos, ResourceLibrary.TORCH_UNLIT.getBlockState().getBaseState().withProperty(BlockTorch.FACING, facing)); 
	
		else return world.setBlockState(pos, ResourceLibrary.TORCH_UNLIT.getDefaultState());
	}
}