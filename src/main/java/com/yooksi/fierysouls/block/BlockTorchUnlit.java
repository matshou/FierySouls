package com.yooksi.fierysouls.block;

import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.tileentity.TileEntityTorchUnlit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorchUnlit extends com.yooksi.fierysouls.block.BlockTorch
{	
	public BlockTorchUnlit() 
	{
		this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
	}
	@Override
	// This will disable objects from being able to be place on top of our torch
	public boolean isFullCube()
    {
		return false;
    }
	@Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, java.util.Random rand)
    {
		// TODO: - Add some options from here to the configuration file
		//       - Try to make the smoke appear more like it's coming from an extinguished torch then fire.
        //       - Adjust smoke depending on which side does a torch face.
		
		// When a lit torch has been extinguished it starts smoldering, emitting smoke particles for a short time duration.
		// Find the right tile entity and check if we should stop smoldering
		
		TileEntity torchEntity = worldIn.getTileEntity(pos);
		TileEntityTorchUnlit torchUnlit = ((torchEntity != null && torchEntity instanceof TileEntityTorchUnlit) 
				? (TileEntityTorchUnlit)torchEntity : null);
		
        if (torchUnlit == null || !torchUnlit.isTorchSmoldering())
        	return;
        	
        EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
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
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) 
	{
		// An unlit torch can be lit on fire by activating it with an already lit torch
		// No need to handle tile entities here, just replace the block
		
		net.minecraft.item.ItemStack equippedItem = playerIn.getCurrentEquippedItem();
		if (equippedItem == null) 
			return false;
		
		if (ResourceLibrary.isItemLitTorch(equippedItem.getItem()))
		{
			TileEntity entityTorch = worldIn.getTileEntity(pos);
    		if (entityTorch != null && entityTorch instanceof TileEntityTorchUnlit)
    		   ((TileEntityTorchUnlit)entityTorch).lightTorch();
			
			return true;      // Let the calling function know that this block was successfully used,
			                  // and there is no need to spawn the activation item as a block.
		}
		// Never allow a torch to be placed like this...
		else return (Block.getBlockFromItem(equippedItem.getItem()) instanceof BlockTorch); 
	}
	
	/** Set the torch on fire by updating 'blockstate' at world coordinates.
	 * 
	 * @param world The instance of the world the torch is located in.
	 * @param pos Coordinates of the torch in the world.
	 * @return True if we successfully lit the torch on fire.
	 */
	public static boolean lightTorch(World world, BlockPos pos)
	{
		// Find out the direction the torch is facing
		EnumFacing facing = (EnumFacing)world.getBlockState(pos).getValue(BlockTorch.FACING);
		
		// If the torch is not facing up but is placed on the side of a block we have to take into
		// account facing sides, otherwise the torch will detach from the wall and turn into an item.
		
		if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) 
			return world.setBlockState(pos, ResourceLibrary.TORCH_LIT.getBlock().getBlockState().getBaseState().withProperty(BlockTorch.FACING, facing)); 
	
		else return world.setBlockState(pos, ResourceLibrary.TORCH_LIT.getBlock().getDefaultState());
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityTorchUnlit(worldIn.getWorldTime());
	}
}