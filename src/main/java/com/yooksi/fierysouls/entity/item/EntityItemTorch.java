package com.yooksi.fierysouls.entity.item;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.item.ItemTorch;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class EntityItemTorch extends EntityItem
{
	public EntityItemTorch(World worldIn)
	{ 
		super(worldIn);
		setDefaultPickupDelay();
	}

	/**
     * Called to update the entity's position/logic.
     */
	@Override
	public void onUpdate()
	{
		super.onUpdate();
        final NBTTagCompound itemTagCompound = getEntityItem().getTagCompound();
        
		// TODO: Humidity should speed up item decay (decrease it's lifespan).
		
		// Update only at set intervals to reduce performance hits.   
		// When it's raining and the torch is directly exposed to rain it will start collecting humidity.
		
		if (getEntityWorld().isRemote || !ItemTorch.shouldUpdateItem(itemTagCompound, worldObj.getTotalWorldTime()))
			return;
		
		// Currently we're only updating humidity and not combustion,
		// so there is no need to go further if humidity is at maximum value.
		
		if (ItemTorch.getItemHumidity(itemTagCompound) >= SharedDefines.HUMIDITY_THRESHOLD)
			return;
		
		final boolean isTorchLit = ItemTorch.isItemTorchLit(getEntityItem().getItem(), false);
		
		// Check if the entity is in water first because rain doesn't matter
		// if we're submerged in a pool of water anyways.
			
		if (isInWater() /**&& isInsideOfMaterial(Material.water)*/)
		{
			ItemTorch.extinguishItemTorch(getEntityItem(), true);
		}
		else if (isTorchLit && ItemTorch.updateItemCombustionTime(itemTagCompound, SharedDefines.TorchUpdateTypes.MAIN_UPDATE.interval * -1) < 1)
		{
			ItemTorch.extinguishItemTorch(getEntityItem(), false);
		}
		else if (worldObj.isRaining() && worldObj.canBlockSeeSky(getPosition()))
		{
			if (ItemTorch.updateItemHumidity(itemTagCompound, SharedDefines.TorchUpdateTypes.MAIN_UPDATE.interval) >= SharedDefines.HUMIDITY_THRESHOLD)
			    ItemTorch.extinguishItemTorch(getEntityItem(), false);
		}
	}
}