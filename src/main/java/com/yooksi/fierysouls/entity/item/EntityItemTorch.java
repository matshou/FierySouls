package com.yooksi.fierysouls.entity.item;

import com.yooksi.fierysouls.item.ItemTorch;
import com.yooksi.fierysouls.common.Utilities;
import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;

import net.minecraft.world.World;
import net.minecraft.util.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.item.EntityItem;

public final class EntityItemTorch extends EntityItem
{
	// This constructor is used when registering this custom entity with Forge
	public EntityItemTorch(World worldIn)
	{ 
		super(worldIn);
	}
	
	public EntityItemTorch(World worldIn, double x, double y, double z, ItemStack stack) 
	{
		super(worldIn, x, y, z, stack);
		this.setDefaultPickupDelay();
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
		
		if (!ItemTorch.shouldUpdateItem(itemTagCompound, worldObj.getTotalWorldTime()))
			return;
		
		// Currently we're only updating humidity and not combustion,
		// so there is no need to go further if humidity is at maximum value.
		
		if (ItemTorch.getItemHumidity(itemTagCompound) >= SharedDefines.HUMIDITY_THRESHOLD)
			return;
		
		final boolean isTorchLit = ResourceLibrary.isItemLitTorch(getEntityItem().getItem());
		
		// Check if the entity is in water first because rain doesn't matter
		// if we're submerged in a pool of water anyways.
			
		if (isInWater() /**&& isInsideOfMaterial(Material.water)*/)
		{
			ItemTorch.extinguishItemTorch(getEntityItem(), true);
		}
		else if (isTorchLit && ItemTorch.updateItemCombustion(itemTagCompound, SharedDefines.MAIN_UPDATE_INTERVAL * -1) < 1)
		{
			ItemTorch.extinguishItemTorch(getEntityItem(), false);
		}
		else if (worldObj.isRaining() && worldObj.canBlockSeeSky(getPosition()))
		{
			if (ItemTorch.updateItemHumidity(itemTagCompound, SharedDefines.MAIN_UPDATE_INTERVAL) >= SharedDefines.HUMIDITY_THRESHOLD)
			    ItemTorch.extinguishItemTorch(getEntityItem(), false);
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
	
	/**
	 * Create a new EntityItemTorch from a stack being dropped as an item from a block.<br>
	 * This method will be called as a chained override for Block.dropBlockAsItem by ItemTorch and BlockTorch.
	 * 
	 * @param worldIn Instance of the world where we want to create the entity
	 * @param pos Position where we want to spawn the entity
	 * @param stack ItemStack to create the entity from
	 * @param nbt Torch data to imprint the entity with <i>(comes from TileEntityTorch)</i>
	 * 
	 * @return An instance of the newly created entity
	 */
	public static EntityItemTorch createDroppedEntityItem(World worldIn, BlockPos pos, ItemStack stack, NBTTagCompound nbt)
	{
		 double posX = (double)pos.getX() + ((double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D); 
         double posY = (double)pos.getY() + ((double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D); 
         double posZ = (double)pos.getZ() + ((double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D);             
         
         ItemTorch.createCustomItemNBTFromExisting(stack, nbt, worldIn.getTotalWorldTime());
         return new EntityItemTorch(worldIn, posX, posY, posZ, stack);
	}
	
    /**
     *  Encode custom information information passed to us by TileEntity in an integer. <p>
     *  
     *  Medatada is composed of three segments, the first segment is any one digit integer used to
     *  prevent the lose of the following zeroes after it (if any) due to <i>string-integer</i> conversion.
     *  The following two segments are humidity and combustion values, each expressed in integers with a number
     *  of digits equal to their maximum values, if the current values have less digits this gap is filled with zeroes.   
     *  Here is an example: <p>
     *  
     *  <i>1<b> (prefix)</b> 0020<b> (humidity)</b> 320<b> (combustion)</b></i>
     *  @see {@link EntityItemTorch#disassembleMetadata}
     */
	@Deprecated
    private static int assembleMetadata(int humidityData, int durationData)
    {
    	String firstSegment, secondSegment;
    	int lengthDiff;
    	
    	String a = Utilities.convertToZeroDigits(SharedDefines.HUMIDITY_THRESHOLD);
        lengthDiff = a.length() - Utilities.getNumberOfDigits(humidityData);
        
    	firstSegment = (lengthDiff > 0) ? a.substring(0, lengthDiff) + humidityData : Integer.toString(humidityData);

    	String b = Utilities.convertToZeroDigits(SharedDefines.MAX_TORCH_FLAME_DURATION);
    	lengthDiff = b.length() - Utilities.getNumberOfDigits(durationData);
    	
    	secondSegment = (lengthDiff > 0) ? b.substring(0, lengthDiff) + durationData : Integer.toString(durationData);
    	
    	return Integer.parseInt("1" + firstSegment + secondSegment);
    }
	
	/**
	 * Reverse the process of assembling metadata. <br>
	 * Convert an integer into an array of custom information such as <i>humidity, duration, etc...</i>.<p>
	 * 
	 * An <b>example</b> of the disassembling process:<br>
	 * <i>metadata: "10034263" - dissasemble - "003", "4263"</i>
	 * 
	 * @return an array of information (elements stored in metadata) in integer format.
	 */
	@Deprecated
    public static int[] disassembleMetadata(int metadata) 
    {
    	final String sMetadata = Integer.toString(metadata);
    	int maxHumiditySize = Utilities.getNumberOfDigits(SharedDefines.HUMIDITY_THRESHOLD);
    	int maxDurationSize = Utilities.getNumberOfDigits(SharedDefines.MAX_TORCH_FLAME_DURATION);

    	if (sMetadata.length() != 1 + maxHumiditySize + maxDurationSize)
    		return null;

    	String sHumidity = sMetadata.substring(1, maxHumiditySize);
    	String sDuration = sMetadata.substring(maxHumiditySize + 1);
    	
    	int iHumidity = Integer.parseInt(sHumidity.substring(Utilities.indexOfAnyBut(sHumidity, "0")));
    	int iDuration = Integer.parseInt(sDuration.substring(Utilities.indexOfAnyBut(sDuration, "0")));
    	
    	return new int[]{iHumidity, iDuration};
    }
}