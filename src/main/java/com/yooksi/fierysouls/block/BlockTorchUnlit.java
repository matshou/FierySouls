package com.yooksi.fierysouls.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.item.ItemTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorchUnlit;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorchUnlit extends com.yooksi.fierysouls.block.BlockTorch implements ITileEntityProvider
{	
	public static BlockTorchUnlit localInstance;
	
	public BlockTorchUnlit()
	{
		this.setCreativeTab(FierySouls.tabTorches);
		localInstance = this;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileEntityTorchUnlit(worldIn.getTotalWorldTime());
	}
	
	/** This function will be called every 'random' tick and is usually used
	 *  to spawn fire particles<br> from the top of the torch.
	 */
	@Override
    @SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		// TODO: - Add some options from here to the configuration file
        //       - Try to make the smoke appear more like it's coming from an extinguished torch then fire.
		//       - Adjust smoke depending on which side does a torch face.
				
		// When a lit torch has been extinguished it starts smoldering, emitting smoke particles for a short time duration.
		// Find the right tile entity and check if we should stop smoldering
		
		TileEntityTorchUnlit unlitTorch = TileEntityTorchUnlit.findUnlitTorchTileEntity(worldIn, pos, true);
		if (!unlitTorch.isTorchSmoldering())
			return;
		
		EnumFacing enumfacing = (EnumFacing)stateIn.getValue(FACING);
		double d0 = (double)pos.getX() + 0.5D;
	    double d1 = (double)pos.getY() + 0.7D;
	    double d2 = (double)pos.getZ() + 0.5D;
	        
	    double d3 = 0.22D;// + rand.nextDouble();
	    double d4 = 0.27D;

	    if (enumfacing.getAxis().isHorizontal())
	    {
	    	EnumFacing enumfacing1 = enumfacing.getOpposite();
	        worldIn.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, d0 + d4 * (double)enumfacing1.getFrontOffsetX(), d1 + d3, d2 + d4 * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D, new int[0]);
	    }
	    else worldIn.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
	}
	
	/** This function is called by Minecraft whenever a block is right-clicked.
	 *  @return if return value is true the block held in hand will be placed as a block on the ground.
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		// For some reason sometimes both the SERVER and CLIENT pass a null value for 'heldItem', fix this:
		if (heldItem == null && playerIn.inventory.getCurrentItem() != null)
			heldItem = playerIn.inventory.getCurrentItem();
		
		if (heldItem == null) 
			return false;
		
		else if (ItemTorch.isItemTorchLit(heldItem.getItem(), true))
		{	
			lightTorch(worldIn, pos);	
			return true;               // Let the calling function know that this block was successfully used,
			                          // and there is no need to spawn the item as block.
		}
		// Never allow a torch to be placed like this...
		else return ItemTorch.isItemTorch(heldItem.getItem(), true);
	}
	
	/** Set the torch on fire. Find the torch tile entity and delegate the call.
	 * 
	 * @param world the instance of the world the torch is located in.
	 * @param pos coordinates of the torch in the world.
	 * @return true if we successfully lit the torch on fire.
	 */
	public static boolean lightTorch(World world, BlockPos pos)
	{
		return TileEntityTorchUnlit.findUnlitTorchTileEntity(world, pos, true).lightTorch();
	}
}
