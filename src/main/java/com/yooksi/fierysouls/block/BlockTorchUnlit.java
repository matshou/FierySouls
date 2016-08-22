package com.yooksi.fierysouls.block;

import java.util.Random;

import com.yooksi.fierysouls.common.FierySouls;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorchUnlit extends com.yooksi.fierysouls.block.BlockTorch
{	
	public static BlockTorchUnlit localInstance;
	
	public BlockTorchUnlit() 
	{
		this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.DECORATIONS);
		this.setCreativeTab(FierySouls.tabTorches);
		localInstance = this;
	}
	
	/** This function will be called every 'random' tick and is usually used
	 *  to spawn fire particles<br> from the top of the torch.
	 */
	@Override
    @SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		return;  // This will block the fire particle spawn on unlit torches.
	}
}
