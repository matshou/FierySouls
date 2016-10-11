package com.yooksi.fierysouls.block;

import java.util.Iterator;
import java.util.Map;

import com.yooksi.fierysouls.common.Logger;
import com.yooksi.fierysouls.common.Utilities;
import com.yooksi.fierysouls.item.ItemTorch;

import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 *  The way our custom item torch emits light is by replacing air blocks
 *  <i>(at the current position of EntityPlayer or EntityItem)</i> with this block, and then removing
 *  it when the belonging entity is no longer present at the same location. At the moment this is the 
 *  only way I can think of doing this, as updating individual BlockPos light values in world did not work out.
 */
public class BlockTorchLight extends BlockAir 
{
	/** 
	 *  A map containing positions of all BlockTorchLight blocks in the world. 
	 *  The position keys are paired with their owning entities. Once the owner is no longer
	 *  present at the location of the slave block, the entry will be removed from the map. 
	 */
	private static java.util.HashMap<BlockPos, Entity> torchLightsInWorld;
	private static BlockTorchLight localInstance;
	
	public BlockTorchLight()     // Called only from ItemTorch
	{
		setTickRandomly(false);
		setLightLevel((float)(BlockTorchLit.MAX_TORCH_LIGHT_LEVEL / 15.0F));
		
		torchLightsInWorld = new java.util.HashMap<BlockPos, Entity>();
		localInstance = this;
	}
	
	/** 
	 *  Replace the block at position with this and create a new map entry for this torch light. <br>
	 *  The entry will be used to find out when we have to destroy the light.
	 *  
	 *  @param owner should be EntityPlayer or EntityItem, otherwise it will not be registered.
	 */
	@SideOnly(Side.CLIENT)
	public static void createNewTorchLight(Entity owner, BlockPos position)
	{	
		if (!(owner instanceof EntityPlayer || owner instanceof EntityItem))
		   Logger.error("Entity passed as argument should not own a torch light.", new IllegalArgumentException());
			
		else if (!torchLightsInWorld.containsKey(position) && owner.worldObj.setBlockState(position, localInstance.getDefaultState()))
		{
            /* 
			 *  Remove the last torch light entry that belongs to this entity.
			 *  This is the primary mechanism for removing unneeded entries.
			 */
			Iterator<Map.Entry<BlockPos, Entity>> iter;
			for (iter = torchLightsInWorld.entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<BlockPos, Entity> entry = iter.next();
				if (entry.getValue() != owner)
					continue;
					
				owner.worldObj.setBlockToAir(entry.getKey());
				iter.remove(); break;
			}   torchLightsInWorld.put(position, owner);
		}
	}

	/**
	 *  Remove the torch light entry from the map and change the block to a default air block.
	 */
	@SideOnly(Side.CLIENT)
	private static void destroyTorchLight(Entity owner, World worldIn, BlockPos pos)
	{
		torchLightsInWorld.remove(pos);
		worldIn.setBlockToAir(pos);
	}
		
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, java.util.Random rand)
    {
    	/*
    	 * This update method here is used primarily as a back-up mechanism,
    	 * to unregister torch lights and remove them from world.
    	 * 
    	 * It becomes very useful to cleanup when the player stop holding
    	 * a lit torch in his hand or the torch becomes extinguished.
    	 */
    	
    	Entity owner = torchLightsInWorld.get(pos);
    	if (owner == null)
    	{
    		Logger.warn("Item torch light with a missing Entity entry detected at " + Utilities.assembleBlockPositionLog(pos) + ".");
    		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState()); return;
    	}
    	
    	boolean isPosValid = pos != null && !(pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0);
        BlockPos ownerPos = owner.getPosition().offset(EnumFacing.UP);

    	if (!isPosValid || pos.getX() != ownerPos.getX() || pos.getY() != ownerPos.getY() || pos.getZ() != ownerPos.getZ())
    	{
    		destroyTorchLight(owner, worldIn, pos);    // The entity is no longer at this position
    	}
    	else if (owner instanceof EntityPlayer)
    	{
    		for (ItemStack item : owner.getHeldEquipment())
    		{
    			if (item != null && ItemTorch.isItemTorchLit(item.getItem(), true))
    				return;
    		}
    		// Player is no longer holding a lit torch in his hand
    		destroyTorchLight(owner, worldIn, pos);
    	}
    }
}