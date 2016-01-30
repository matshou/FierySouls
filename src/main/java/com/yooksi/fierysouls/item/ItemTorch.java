package com.yooksi.fierysouls.item;

import com.yooksi.fierysouls.common.Utilities;
import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.world.World;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;

public class ItemTorch extends ItemBlock
{
	/** 
	 * Damage this item will inflict in combat. <br>
	 * This is what's written in <b>Pathfinder</b> about torches and combat: <p> 
	 * 
	 * <i>"If a torch is used in combat, treat it as a one-handed improvised weapon that deals <br>
	 * bludgeoning damage equal to that of a gauntlet of its size, plus 1 point of fire damage."</i>
	 */
	private static final float TORCH_FIRE_DAMAGE = 1.0F;
	
	/** 
	 *  Base chance value that is further modified and used as a value to roll against, <br>
	 *  to determine if the entity attacked should be set on fire.
	 */
	private static final int CHANCE_TO_SET_TARGET_ON_FIRE = 10;
	
	public ItemTorch(net.minecraft.block.Block block) 
	{
		super(block);
		this.setMaxDamage(-1);   // Disable vanilla damage and use "torchItemDamage" NBT value instead.

		if (block.equals(ResourceLibrary.TORCH_LIT.getBlock()))
			this.setMaxStackSize(1);
	}	
	
	 /**
     * "Current implementations of this method in child classes do not use the entry argument <br> 
     * beside ev. They just raise the damage on the stack." <p>
     * 
     * <i>Called when one entity attacks another with this item equipped.<br>
     * Deal fire damage to target and possibly set entity on fire.</i>
     *  
     * @param target The Entity being hit
     * @param attacker the attacking entity
     */
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
    {
		if (stack != null && ResourceLibrary.isItemLitTorch(stack.getItem()))
		{
			// TODO: Fire damage and chance of setting target on fire should
		    //       both be modified by entities 'fireResistance' value.
		
		    target.attackEntityFrom(net.minecraft.util.DamageSource.onFire, TORCH_FIRE_DAMAGE);
		
		    // The armor of the targets decreases the chance of setting on fire,
		    // count the combined value, not the type of armor.
		
		    int armorValue = target.getTotalArmorValue();
		    final int chanceToSetOnFire = CHANCE_TO_SET_TARGET_ON_FIRE - armorValue;
		
		    boolean setOnFire = Utilities.rollDiceAgainst(chanceToSetOnFire, 100, new java.util.Random());
		
		    if (!target.isImmuneToFire() && setOnFire)
			    target.setFire(2);                              // Deal 1pt of DOT to target	
		}
		
		return super.hitEntity(stack, target, attacker);
    }
    
	/**
     * Called when a Block is right-clicked with this Item. <p>
     * 
     * <i>If all goes well the item will be placed on the position right-clicked by player,<br>
     * and we will pass the humidity and combustion duration data to TileEntity.</i>
     *  
     * @param pos The block being right-clicked
     * @param side The side being right-clicked
     * @return True if the item has been successfully placed as block in the world.
     */
    @Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	final boolean wasBlockPlaced = super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
    	
    	if (wasBlockPlaced == true && !worldIn.isRemote)
    	{		
    		net.minecraft.tileentity.TileEntity tileEntity = worldIn.getTileEntity(pos.offset(side));
    		if (tileEntity != null && tileEntity instanceof TileEntityTorch)
            {
            	TileEntityTorch torchTileEntity = (TileEntityTorch)tileEntity;
            	
            	// Update new tile entity with item shared custom data such as humidity and combustion duration 
            	// First prepare a data packet with native TileEntity data (this is needed to properly read the packet
            	// on TileEntity side), then update packet with info on item side and send it to TileEntityTorch.
            	
            	NBTTagCompound nbtDataPacket = torchTileEntity.saveDataToPacket();
            	torchTileEntity.readFromNBT(saveStackDataToPacket(stack, nbtDataPacket));
            }
    	}
    	return wasBlockPlaced;
    }
    
    /**
	 * Write NBT data to designated packet and return the updated data.<p>
	 * <i>Note that if the packet passed does not exists a new one will be created.</i>
	 * 
	 * @param stack ItemStack to be save data for <i>(cannot be null)</i>
	 * @param packet NBTTagCompound to save data to <i>(can be null)</i>
	 * @return The new and updated NBT tag compound
	 */
	private static NBTTagCompound saveStackDataToPacket(ItemStack stack, NBTTagCompound packet)
	{
		if (packet == null)
			packet = new NBTTagCompound();    // If no packet was selected create a new one
		
		NBTTagCompound itemStackData = stack.getTagCompound();
		packet.setShort("humidityLevel", itemStackData.getShort("humidityLevel"));
		packet.setShort("combustionDuration", itemStackData.getShort("combustionDuration")); 
		
		return packet;
	}

    /**
     * Determines if the player is switching between two item stacks.<br>
     * 
     * @param oldStack The old stack that was equipped
     * @param newStack The new stack
     * @param slotChanged Has the current equipped slot changed?
     * @return True to play the item change animation
     */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
    	// This is the ideal solution to prevent the first person item "bobbing" animation
    	// that happens when you update item metadata or NBT fields.
    	
    	if (!slotChanged && oldStack.getItem() == newStack.getItem())
    		return false;
    	
    	return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
    
    /**
     * Check to see if we should perform a full item update.<br>
     * Written specifically to be at the disposal of {@link #onUpdate}.
     *
     * @param stack ItemStack we would like to update <b>(unchecked)</b>
     * @param worldTime Total time elapsed from the creation of the world 
     * @return True if enough world time has elapsed
     */
    private static boolean shouldUpdateItem(ItemStack stack, long totalWorldTime)
    {
    	long lastUpdateTime = stack.getTagCompound().getLong("lastUpdateTime");
    	if (lastUpdateTime > 0 && totalWorldTime - lastUpdateTime >= SharedDefines.MAIN_UPDATE_INTERVAL)
    	{
    		stack.getTagCompound().setLong("lastUpdateTime", totalWorldTime);
    		return true;
    	}
    	else return false;
    }   
    
    /**
     * Called each tick as long the item is on a player inventory.<br> 
     * Uses by maps to check if is on a player hand and update it's contents.
     */
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    	if (!worldIn.isRemote && stack != null)
    	{
    		// TODO: When an item is added to the inventory from the creativeTab it ends up
        	// without a proper custom NBT, so we do it here. Find a better way of handling this...
        	
    		long totalWorldTime = worldIn.getTotalWorldTime();
    		
    		if (!stack.hasTagCompound())
    			createCustomItemNBT(stack, totalWorldTime);
    		
    		else if (!shouldUpdateItem(stack, totalWorldTime))
    			return;
    		
    		// Currently we're only updating humidity and not combustion,
    		// so there is no need to go further if humidity is at maximum value.
    		
    		if (getItemHumidity(stack) >= SharedDefines.HUMIDITY_THRESHOLD)
    			return;
    		
    		final short itemHumidity = (worldIn.isRaining() && worldIn.canBlockSeeSky(entityIn.getPosition()))
    				? updateItemHumidity(stack, SharedDefines.MAIN_UPDATE_INTERVAL) : 0;
    				
    		if (ResourceLibrary.isItemLitTorch(stack.getItem()))
    		{
    			// When lit torches are not placed in the hotbar, but in storage slots
    			// they should be extinguished - adds realism.
    			
    			if (itemSlot > 8 || updateItemCombustion(stack, SharedDefines.MAIN_UPDATE_INTERVAL * -1) < 1)
    				extinguishItemTorch(stack, false);
    		
    			else if (itemHumidity >= SharedDefines.HUMIDITY_THRESHOLD)
    				extinguishItemTorch(stack, false);
    		}
 
    		// Check 'isInWater' first to optimize the code a bit, boolean checks are the fastest. 
    		// The second check is a lengthy one and should not return true if the first one returns false.
    				
    		if (entityIn.isInWater() && entityIn.isInsideOfMaterial(net.minecraft.block.material.Material.water))
    		{
    			if (ResourceLibrary.isItemLitTorch(stack.getItem()))
    				extinguishItemTorch(stack, true);
    				
    			else setItemHumidity(stack, SharedDefines.HUMIDITY_THRESHOLD); 
    		}
    		else if (entityIn.isInLava() && itemSlot < 9)  // Light torches in hotbar on fire when player is in lava
    		{
    			if (ResourceLibrary.isItemUnlitTorch(stack.getItem()))
    				stack.setItem(ResourceLibrary.TORCH_LIT.getItem());
    		}
    	}
    }
    
    /**
     * Determines if the durability bar should be rendered for this item. <br>
     * Dependent on torch combustion duration value.
     *
     * @param stack The current Item Stack
     * @return True if it should render the 'durability' bar
     */
    public boolean showDurabilityBar(ItemStack stack)
    {
    	return this.getTorchItemDamage(stack) > 0;
    }
    	
    /**
     * Queries the percentage of the 'Durability' bar that should be drawn. <p>
     * 
     * <i>The percentage is dependent on combustion rather then ItemStack.itemDamage.<br>
     * The reason we don't use itemDamage is because we want to replace the torch when it burns out,<br>
     * and if we use itemDamage the item will get destroyed before we get the chance to replace the torch.</i>
     *
     * @see {@link #showDurabilityBar(ItemStack)}
     * @param stack The current ItemStack
     * @return 0 for 100% 1.0 for 0%
     */
    @Override
    public double getDurabilityForDisplay(ItemStack stack)
    {
    	return (double)this.getTorchItemDamage(stack) / (double)SharedDefines.MAX_TORCH_FLAME_DURATION;
    }
    
    /**
     * Get humidity value for this item from NBT storage.<p>
     * 
     * @throws java.lang.NullPointerException if item tag compound is not found
     * @param stack ItemStack to get the information from <b>(unchecked)</b>
     * @return Returns the humidity value from item NBT
     */
    private static short getItemHumidity(ItemStack stack)
    {
    	return stack.getTagCompound().getShort("humidityLevel");
    }
    /**
     * Get combustion duration value for this item from NBT storage.
     * 
     * @throws java.lang.NullPointerException if item tag compound is not found
     * @param stack ItemStack to get the information from <b>(unchecked)</b>
     * @return Returns combustion duration value from item NBT
     */
    private static short getItemCombustionDuration(ItemStack stack)
    {
    	return stack.getTagCompound().getShort("combustionDuration");
    }
    
    /**
     * How much material has this torch expended with combustion so far?
     * 
     * @see {@link #getDurabilityForDisplay}
     * @param stack ItemStack to get the information from
     * @return Amount of material combusted from stack NBT, <br> 
     *         -1 if item stack or NBT are <i>null</i>
     */
    private static short getTorchItemDamage(ItemStack stack)
    {
    	return (stack != null && stack.hasTagCompound()) ?
    			stack.getTagCompound().getShort("torchItemDamage") : -1;
    }
    
    /**
     * Called when the torch is submerged under water or is exposed to rain for too long. <p>
     * <i>Unlike the <b>TileEntityTorch</b> version, this method will not order smoke particle spawning.</i>
     * 
     * @param stack Torch ItemStack to extinguish
     * @param extinguishByWater If true; update the humidity value as well
     */
    public static void extinguishItemTorch(ItemStack stack, boolean extinguishByWater)
    {
    	if (stack != null && ResourceLibrary.isItemLitTorch(stack.getItem()))
    		stack.setItem(ResourceLibrary.TORCH_UNLIT.getItem());
    	
    	if (extinguishByWater == true)
    		setItemHumidity(stack, SharedDefines.HUMIDITY_THRESHOLD);
    }
    
    /**
     *  Set this item torch on fire. <br>
     *  <i>Nothing will happen if the item is too wet or has already burned out.</i>
     *  
     * @param stack ItemStack instance of our torch to set on fire
     */
    public static void lightItemTorch(ItemStack stack)
    {
    	boolean result = stack != null && stack.hasTagCompound();
    	if (result && getItemHumidity(stack) < SharedDefines.HUMIDITY_THRESHOLD)
    	{
    		if (getItemCombustionDuration(stack) > 0)
    			stack.setItem(ResourceLibrary.TORCH_LIT.getItem());
    	}
    }
     
    /**
     * Set the humidity level of an ItemStack to a new value. <p>
     * 
     * @param stack ItemStack that data we wish to update
     * @param value New value to update humidity to
     */
    public static void setItemHumidity(ItemStack stack, short value)
    {
    	if (stack != null && stack.hasTagCompound())
    		stack.getTagCompound().setShort("humidityLevel", value);
    }
    
    /**
     * Update the remaining time the item torch is allowed to burn.
     * 
     * @param stack ItemStack to update the time for
     * @param value Value to decrease the time for (expected to be a negative value)
     * @return The updated value or -1 if stack or NBT were not found
     */
    public static short updateItemCombustion(ItemStack stack, int value)
    {
    	if (stack != null && stack.hasTagCompound())
    	{	
    		short combustion = stack.getTagCompound().getShort("combustionDuration");
    		combustion += ((combustion + value > 0) ? value : combustion * -1);   // Keep the value unsigned; 
    		
    		stack.getTagCompound().setShort("combustionDuration", combustion);
    		
    		// Updating this under the assumption that the argument is a negative value
    		
    		short itemDamage = (short) (stack.getTagCompound().getShort("torchItemDamage") + (value * -1));
    		stack.getTagCompound().setShort("torchItemDamage", itemDamage);
    	    
    		return combustion;
    	}
    	else return -1;
    }
    
    /**
     *  Update the humidity of this item by updating a custom NBT field. 
     *  
     * @param stack ItemStack to update humidity for
     * @param value Value to increment the humidity for
     * @return The updated value or -1 if stack or NBT were not found
     */
    public static short updateItemHumidity(ItemStack stack, int value)
    {
    	if (stack != null && stack.hasTagCompound())
    	{
    		short humidity = getItemHumidity(stack);
    		humidity += ((humidity + value < SharedDefines.HUMIDITY_THRESHOLD) ? 
    				value : SharedDefines.HUMIDITY_THRESHOLD - humidity);
    	
    		stack.getTagCompound().setShort("humidityLevel", humidity);
    		return humidity;
    	}
    	else return -1;
    }
    
    /** 
	 * Use an existing NBT tag compound to update item stack NBT by extracting data directly from it. <p>
	 * <i><b>Note</b> that this does not create but just update an already existing NBT Tag Compound.</i>
	 *  
	 * @param stack Item stack to update
	 * @param newTagCompound NBT Tag Compound to extract data from
	 */
	private static void updateCustomItemNBTFromExisting(ItemStack stack, NBTTagCompound newTagCompound)
	{
		if (newTagCompound != null && stack.hasTagCompound())
		{
			NBTTagCompound itemTagCompound = stack.getTagCompound();
			itemTagCompound.setShort("humidityLevel", newTagCompound.getShort("humidityLevel"));
			
			// 'torchItemDamage' is a helper variable that we use so that we don't have to recalculate
			// how much material has the torch combusted so far on every GUI render update.
			
			short combustion = newTagCompound.getShort("combustionDuration");
			short itemDamage = (short) (SharedDefines.MAX_TORCH_FLAME_DURATION - combustion);
			
			itemTagCompound.setShort("combustionDuration", combustion);
			itemTagCompound.setShort("torchItemDamage", itemDamage);
		}
	}
	
	/** 
	 * Create a custom item NBT tag compound for a specific item stack <b><i>from an existing tag compound</b></i>.<p>
	 * 
	 * <i>This is an attempt at micro-optimizing because we could just pass the existing NBT to item <br> 
	 * without creating a new one, however this might save some memory as it will not carry useless data.</i>
	 *  
	 * @param stack ItemStack to create a new NBT for
	 * @param tagCompound Existing NBT tag compound to extract data from
	 * @param totalWorldTime Total world time since the world was created
	 */
	public static void createCustomItemNBTFromExisting(ItemStack stack, NBTTagCompound tagCompound, long totalWorldTime)
	{
		if (tagCompound != null && stack != null)
		{
			createCustomItemNBT(stack, totalWorldTime);
			updateCustomItemNBTFromExisting(stack, tagCompound);
		}
	}
	
	/**
	 * Create a custom item NBT tag compound for a specific item stack. <br>
	 * All the values will be initialized to default standards.<p>
	 * 
	 * <i>Use #createCustomItemNBTFromExisting if you already have NBT data for this item to inherit.</i>
	 *
	 * @see {@link #createCustomItemNBTFromExisting}
	 * @param totalWorldTime Total world time since the world was created
	 * @param stack ItemStack to create a new NBT for <b>(unchecked)</b>
	 */
	public static void createCustomItemNBT(ItemStack stack, long totalWorldTime)
	{	
		final short sNull = (short) 0;
		NBTTagCompound tagCompound = new NBTTagCompound();
		
		tagCompound.setShort("humidityLevel", sNull);
		tagCompound.setLong("lastUpdateTime", totalWorldTime);
		
		tagCompound.setShort("torchItemDamage", sNull);
		tagCompound.setShort("combustionDuration", SharedDefines.MAX_TORCH_FLAME_DURATION);
		
		stack.setTagCompound(tagCompound);
	}
    
	/**
     * This used to be 'display damage' but its really just 'aux' data in the ItemStack. <br>
     * If we return anything other then 0 for this item the texture will not be rendered properly.<p>
     * 
     * <i>Used to ensure custom metadata stays secure and not tampered with by MC code.<br>
     * Note that there is no need for this if we're not using metadata to store custom data.</i>
     */
	@Override
	public int getMetadata(ItemStack stack)
    {
		return /**stack.getItem() == this ? 0 :*/ super.getMetadata(stack);
    }
}
