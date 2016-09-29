package com.yooksi.fierysouls.entity.item;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.common.SharedDefines.TorchUpdateType;
import com.yooksi.fierysouls.item.ExtendedItemProperties;
import com.yooksi.fierysouls.item.ItemTorch;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class EntityItemTorch extends EntityItem
{
	public EntityItemTorch(World worldObj, double posX, double d0, double posZ, ItemStack stack) 
	{
		super(worldObj, posX, d0, posZ, stack);
	}

	/**
     * Called to update the entity's position/logic.
     */
	@Override
	public void onUpdate()
	{
		super.onUpdate();  
		
		final ExtendedItemProperties properties = ExtendedItemProperties.findExtendedPropertiesForItem(getEntityItem(), worldObj);
        final NBTTagCompound itemTagCompound = getEntityItem().getTagCompound();
		
		// TODO: Humidity should speed up item decay (decrease it's lifespan).
		
		// Update only at set intervals to reduce performance hits.   
		// When it's raining and the torch is directly exposed to rain it will start collecting humidity.
		
		if (getEntityWorld().isRemote || !ItemTorch.shouldUpdateItem(properties, worldObj.getTotalWorldTime()))
			return;
		
		// Currently we're only updating humidity and not combustion,
		// so there is no need to go further if humidity is at maximum value.

		if (ItemTorch.getItemHumidity(itemTagCompound) >= SharedDefines.TORCH_HUMIDITY_THRESHOLD)
			return;
		
		final boolean isTorchLit = ItemTorch.isItemTorchLit(getEntityItem().getItem(), false);
		
		// Check if the entity is in water first because rain doesn't matter
		// if we're submerged in a pool of water anyways.
			
		if (isInWater() /**&& isInsideOfMaterial(Material.water)*/)
		{
			ItemTorch.extinguishItemTorch(getEntityItem(), true, itemTagCompound);
		}
		else if (isTorchLit && ItemTorch.updateItemCombustionTime(itemTagCompound, TorchUpdateType.MAIN_UPDATE.getInterval() * -1) < 1)
		{
			ItemTorch.extinguishItemTorch(getEntityItem(), false, itemTagCompound);
		}
		else if (worldObj.isRaining() && worldObj.canBlockSeeSky(getPosition()))
		{
			if (ItemTorch.updateItemHumidity(itemTagCompound, TorchUpdateType.MAIN_UPDATE.getInterval()) >= SharedDefines.TORCH_HUMIDITY_THRESHOLD)
			    ItemTorch.extinguishItemTorch(getEntityItem(), false, itemTagCompound);
		}
	}
	
	/**
	 * Create a new EntityItemTorch from a stack being tossed <i>(dropped or drag-n-dropped)</i> in the world <br>
	 * and prepare it for being added in the world as a tossed item by initialize motion data and pickup delay. <p>
	 * 
	 * <i>The code is a slightly modified copy from <b>EntityPlayer.dropItem</b>.</i>
	 * 
	 * @param stack ItemStack to create EntityItem from
	 * @param player EntityPlayer tossing the item in the world
	 * @return newly created entity item already prepared to be added to the world
	 * 
	 * @see {@link net.minecraft.entity.player.EntityPlayer#dropItem}
	 *      {@link com.yooksi.fierysouls.common.EventHandler#itemEvent}
	 */
	public static EntityItemTorch createTossedEntityItem(ItemStack stack, net.minecraft.entity.player.EntityPlayer player)
	{
		double d0 = player.posY - 0.30000001192092896D + (double)player.getEyeHeight();
        EntityItemTorch entityTorch = new EntityItemTorch(player.worldObj, player.posX, d0, player.posZ, stack);
        entityTorch.setPickupDelay(40);
        
        java.util.Random rand = new java.util.Random();

        entityTorch.motionX = (double)(-MathHelper.sin(player.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float)Math.PI) * 0.3F);
        entityTorch.motionZ = (double)(MathHelper.cos(player.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float)Math.PI) * 0.3F);
        entityTorch.motionY = (double)(-MathHelper.sin(player.rotationPitch / 180.0F * (float)Math.PI) * 0.3F + 0.1F);
        
        float f = 0.02F * rand.nextFloat();
        float f1 = rand.nextFloat() * (float)Math.PI * 2.0F;
        
        entityTorch.motionX += Math.cos((double)f1) * (double)f;
        entityTorch.motionY += (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F);
        entityTorch.motionZ += Math.sin((double)f1) * (double)f;
        
        return entityTorch;
	}
}