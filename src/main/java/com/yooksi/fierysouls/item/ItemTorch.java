package com.yooksi.fierysouls.item;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.yooksi.fierysouls.block.BlockTorch;
import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.common.Utilities;
import com.yooksi.fierysouls.entity.item.EntityItemTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;

import com.yooksi.fierysouls.common.SharedDefines.TorchUpdateType;
import com.yooksi.fierysouls.common.SharedDefines.TorchActionType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTorch extends ItemBlock
{
	public ItemTorch(Block block) 
	{
		super(block);
		this.setMaxDamage(-1);   // Disable vanilla damage and use "torchItemDamage" NBT value instead.
	}
	
	/**
	 *  Called each tick as long the item is on a player inventory.
	 *  @param isSelected is true if the item is currently selected in the inventory hotbar <i>(includes off-hand slots)</i>
	 */
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    	// TODO: When an item is added to the inventory from the creativeTab it ends up
    	// without a proper custom NBT, so we do it here. Find a better way of handling this...
    	
    	if (!stack.hasTagCompound())
    		createCustomItemNBT(stack, worldIn);

    	else if (worldIn.isRemote == false)
    	{
    		/*  Periodic item NBT updates seem to reset the block breaking progress.
    		 *  If for some reason the player decided to dig with a torch he would 
    		 *  not be able to do what without some code magic.
    		 */
    		
    		final ExtendedItemProperties extendedProperties = ExtendedItemProperties.findExtendedPropertiesForItem(stack, worldIn);	
    		NBTTagCompound itemTagCompound = stack.getTagCompound();

    		final boolean isTorchItemLit = ItemTorch.isItemTorchLit(stack.getItem(), false);

    		if (isSelected)
    		{	
    			if (!extendedProperties.getBoolean("updateReroute")) 
    		    {
    			    if (Utilities.isPlayerBreakingBlock()) // The player has just STARTED breaking the block.
    			    {
    			    	extendedProperties.setLong("timeStartedBreakingBlock", worldIn.getTotalWorldTime());
    			    	extendedProperties.setBoolean("updateReroute", true);
    			    	itemTagCompound = extendedProperties;
 
    			        // Check if player is trying to break a block located underwater or behind a waterfall.
    			        this.useItemTorchInWorld(getItemTorchUseResult(entityIn, worldIn, null, null), stack, worldIn, (EntityPlayer) entityIn, null, extendedProperties);
    			    }
    		    }
   		        else if (!Utilities.isPlayerBreakingBlock())  // The player has just STOPPPED breaking the block.
		        {
   		        	updateCustomItemNBTFromExisting(stack, extendedProperties);
			        extendedProperties.setBoolean("updateReroute", false);
		        }
		        else  // Block breaking by player is currently IN PROGRESS,
		        {
		        	itemTagCompound = extendedProperties;
		        	
		        	// If we're breaking a block with a lit torch put the flame out after a few update intervals.
		            if (isTorchItemLit == true)
		            {
		        	    long lastUpdate = extendedProperties.getLong("timeStartedBreakingBlock");
		        	    if (lastUpdate > 0 && worldIn.getTotalWorldTime() - lastUpdate > TorchUpdateType.MAIN_UPDATE.getInterval() * 3)
		        		    extinguishItemTorch(stack, false, extendedProperties);
		            }
		        }
    		}
    		
    		if (!shouldUpdateItem(extendedProperties, worldIn.getTotalWorldTime()))
    			return;
    	
    		/*  Currently we're only updating humidity and not combustion,
		     *  so there is no need to go further if humidity is at maximum value.
		     */
    		if (getItemHumidity(itemTagCompound) >= SharedDefines.TORCH_HUMIDITY_THRESHOLD)
    			return;
	    
    		final int itemHumidity = (worldIn.isRaining() && worldIn.canBlockSeeSky(entityIn.getPosition())) ?
    				updateItemHumidity(itemTagCompound, TorchUpdateType.MAIN_UPDATE.getInterval()) 
    				: this.getItemHumidity(itemTagCompound);
    	
			if (isTorchItemLit == true)
			{
				/*  When lit torches are not placed in the hotbar, but in other slots;
			     *  they should be extinguished - adds realism.
			     */
				if (itemSlot > 8 || updateItemCombustionTime(itemTagCompound, TorchUpdateType.MAIN_UPDATE.getInterval() * -1) < 1)
					extinguishItemTorch(stack, false, itemTagCompound);
		
				else if (itemHumidity >= SharedDefines.TORCH_HUMIDITY_THRESHOLD)
					extinguishItemTorch(stack, false, itemTagCompound);
			}

			// Check 'isInWater' first to optimize the code a bit, boolean checks are the fastest. 
			// The second check is a lengthy one and should not return true if the first one returns false.

			if (entityIn.isInWater() && entityIn.isInsideOfMaterial(Material.WATER))
			{
				if (isTorchItemLit == true)
					extinguishItemTorch(stack, true, itemTagCompound);	
			
				else setItemHumidity(itemTagCompound, SharedDefines.TORCH_HUMIDITY_THRESHOLD); 
			}
    	}
    }    
    /**
     * Check to see if we should perform a full item update.<br>
     * Written specifically to be at the disposal of <b>onUpdate</b> in ItemTorch and EntityItemTorch.
     *
     * @throws java.lang.NullPointerException if item tag compound is <code>null</code>
     * @param itemNBT Map of item's custom data used for updating.
     * @param totalWorldTime Total time elapsed from the creation of the world. 
     * @return True if enough world time has elapsed.
     */
    public static boolean shouldUpdateItem(NBTTagCompound itemNBT, long totalWorldTime)
    {
    	long lastUpdateTime = itemNBT.getLong("lastUpdateTime");
    	if (lastUpdateTime > 0 && totalWorldTime - lastUpdateTime >= TorchUpdateType.MAIN_UPDATE.getInterval())
    	{
    		itemNBT.setLong("lastUpdateTime", totalWorldTime);
    		return true;
    	}
    	else return false;
    }
	
    /**
     * Called when a this Item is right-clicked in the air. <br>
     * <i>This will many times be called when the item is used on a block.</i>
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
    	// TODO: Find a way to RayTrace for entities between the player and the block.
    	
    	// This will more often then not be called when right-clicking a block as well.
    	// In this case #onItemUse will be called as well, which means we will be calling #useItemTorchInWorld
    	// way more times then we actually need, hence we optimize.
    	
    	Vec3d posVec = playerIn.getPositionEyes(1.0F);
		Vec3d lookVec = playerIn.getLook(1.0F);
		float reach = Minecraft.getMinecraft().playerController.getBlockReachDistance();
		
	    Vec3d destination = posVec.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);

		if (!worldIn.getBlockState(new BlockPos(destination)).getMaterial().blocksMovement())
	    	useItemTorchInWorld(getItemTorchUseResult(playerIn, worldIn, null, null), itemStackIn, worldIn, playerIn, hand, null);
    	
	   // I don't know the difference in outcomes of return results in this method, 
	   // so we should just return the default value.
	    return new ActionResult(EnumActionResult.PASS, itemStackIn);
    }
    
    /**
     * Called when a Block is right-clicked with this Item. <p>
     * 
     * <i>If all goes well the item will be placed on the position right-clicked by player,<br>
     * and we will pass the humidity and combustion duration data to TileEntity.</i>
     */
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
    	/*
    	 *  When clicked on a block within placement reach the onItemRightClick will sometimes not be called,
    	 *  and this means that we will not be able to check for material and extinguish/light torch.
    	 */
    	final TorchActionType action = getItemTorchUseResult(playerIn, worldIn, facing, pos.offset(facing));
    	useItemTorchInWorld(action, stack, worldIn, playerIn, hand, null);
    	
    	EnumActionResult result = (action == TorchActionType.NO_ACTION) ?
    		super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ) : EnumActionResult.FAIL;
    	
    	if (!worldIn.isRemote && result == EnumActionResult.SUCCESS)
    	{
    		net.minecraft.tileentity.TileEntity tileEntity = worldIn.getTileEntity(pos.offset(facing));
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
    	return result;
    }
    /**
     * Find out what will happen to the torch if used in the world, usually after a right-click. <br>
     * This method does not check if the torch will be placed as block, for that check {@link #onItemUse}.
     * 
     * @param carrier Entity carrying the torch in his inventory <i>(could be a non-player)</i>
     * @param facing the orientation of the block side being targeted by the torch.
     * @param targetPos BlockPos of the position <u>in front of the block</u> being targeted by the torch.
     */
    private static TorchActionType getItemTorchUseResult(Entity carrier, World worldIn, @Nullable EnumFacing facing, @Nullable BlockPos targetPos)
    {
    	Set<Material> materials = new HashSet<Material>();
	    materials.add(Material.LAVA); materials.add(Material.FIRE); materials.add(Material.WATER);

	    final double range = Minecraft.getMinecraft().playerController.getBlockReachDistance();
	
	    // The position passed as argument is one block in front of the real target.
	    BlockPos fixedPos = targetPos != null && facing != null ? targetPos.offset(facing.getOpposite()) : null;
	    
	    Material foundMaterial = getFirstMaterialItemWillTouchOnUse(carrier, worldIn, materials, range, facing, fixedPos);
        
	    if (foundMaterial == Material.LAVA || foundMaterial == Material.FIRE)
			return TorchActionType.TORCH_SET_ON_FIRE;
		
		else if (foundMaterial == Material.WATER)
			return TorchActionType.TORCH_EXTINGUISHED;
		
		else return TorchActionType.NO_ACTION; 
    }
    
    /**
     * Perform the given torch action in the world.
     *
     * @param action the type of action that the torch should perform.
     * @param playerIn the player carrying the torch in his inventory.
     * @param hand the hand the player is carrying the torch in <i>(will not do the arm swing if null)</i>
     * @param tag specified NBTTagCompound to use for data updating.
     */
    protected static void useItemTorchInWorld(TorchActionType action, ItemStack stack, World worldIn,  EntityPlayer playerIn, @Nullable EnumHand hand, @Nullable NBTTagCompound tag)
    {    	
        /*  If we're trying to use a torch on lava or fire light the torch on fire.
         *  On the other hand if it's water we're dipping the torch in, extinguish it.
         */
	    if (action == TorchActionType.TORCH_SET_ON_FIRE)
	    {		
	    	if (hand != null) playerIn.swingArm(hand);
	    	
		    if (stack.getItem() == Item.getItemFromBlock(ResourceLibrary.TORCH_UNLIT))
			    lightItemTorch(stack, playerIn, tag != null ? tag : stack.getTagCompound());
	    }
	    else if (action == TorchActionType.TORCH_EXTINGUISHED)
	    {
	    	if (hand != null) playerIn.swingArm(hand);
	    	extinguishItemTorch(stack, true, tag != null ? tag : stack.getTagCompound());
	    }
    }
   	
	/**
	 *  This method is intended to be called right after the player right-clicked with a torch, <br>
	 *  It will search for blocks made of specific materials in the direction where the player <br>
	 *  is looking, limiting the search with the specified range of item's reach.
	 *  
	 *  @param user Entity using the item <i>(this allows non-players to use this too)</i>
	 *  @param world Instance of the world the player and his item are in.
	 *  @param materials The material types to check if item is used on .
	 *  @param itemReach User defined reach of the item being used <i>(should be > 1)</i>
	 *  @param facing the orientation of the block side being targeted by the torch.
     *  @param targetPos BlockPos of the block being targeted by the torch.
     *
	 *  @return The first valid material found between the players eyes and a point determined by itemReach.
	 *  @throws java.lang.NullPointerException if EntityPlayer or World instances are <code>null</code>
	 */

    //@SideOnly(Side.CLIENT)
	protected static Material getFirstMaterialItemWillTouchOnUse(Entity user, World world, Set<Material> materials, double itemReach, @Nullable EnumFacing facing, @Nullable BlockPos targetPos)
	{
		final Vec3d posVec = user.getPositionEyes(1.0F);
		final Vec3d lookVec = user.getLook(1.0F);
		final byte scanSensitivity =  5;
		
		final Vec3d destinationVec = posVec.addVector(lookVec.xCoord * itemReach, lookVec.yCoord * itemReach, lookVec.zCoord * itemReach);
	    final Vec3d distanceVec = destinationVec.subtract(posVec);
	    
	    final int incrementRounds = (int) Math.round((double)itemReach * (double)scanSensitivity);
	    final Vec3d factorVec = new Vec3d(distanceVec.xCoord / incrementRounds, distanceVec.yCoord / incrementRounds, distanceVec.zCoord / incrementRounds);
	    
		/*
		 *  Due to the way block position works it can be difficult to scan all blocks
		 *  located in the world from point A to point B so we need to adjust the scan sensitivity
		 *  to include more iterations then just the range itself.
		 *  
		 *  Even with increased scan sensitivity it can still sometimes not be enough.
		 *  The problem sometimes occurs when the player is trying to use the item on the very edge
		 *  of some block, the adjacent block in the direction opposite from the hit side will not be scanned.
		 *  Dramatic increase of scan sensitivity would probably solve this issue, however this would
		 *  increase performance stress. The simple solution is to directly check for that block.  
		 */
		
	    Material targetBlockMaterial = targetPos != null && facing != null ? world.getBlockState(targetPos.offset(facing)).getMaterial() : null;
		
		for (int i = 0; i <= incrementRounds; i++)  // Manually traverse the vector
		{
			Vec3d vec32 = posVec.addVector(factorVec.xCoord * i, factorVec.yCoord * i, factorVec.zCoord * i);
		    BlockPos blockpos = new BlockPos(vec32.xCoord, vec32.yCoord, vec32.zCoord);

		    Block thisBlock = world.getBlockState(blockpos).getBlock();
		    Material blockMaterial = world.getBlockState(blockpos).getMaterial();
		  
		    if (materials.contains(blockMaterial))
		    	return blockMaterial;
		    
		    // We reached an obstacle, return the result we gather so far.
		    else if (blockMaterial.blocksMovement())
		    {
		    	if (targetBlockMaterial != null && materials.contains(targetBlockMaterial))
			    	return targetBlockMaterial;
		    	
		    	else return null;
		    }
		}
		// No collidable blocks or specified materials were found on the path.
		return null;  
	}
	
    /**
     * Called when the torch is submerged under water or is exposed to rain for too long. <p>
     * <i>Unlike the <b>TileEntityTorch</b> version, this method will not order smoke particle spawning.</i>
     * 
     * @param stack Torch ItemStack to extinguish
     * @param extinguishByWater If true; update the humidity value as well
     * @param tagCompound the NBTTagCompound to be used for updating data.
     */
    public static void extinguishItemTorch(ItemStack stack, boolean extinguishByWater, NBTTagCompound tagCompound)
    {
    	if (stack != null && ItemTorch.isItemTorch(stack.getItem(), false))
    		stack.setItem(Item.getItemFromBlock(ResourceLibrary.TORCH_UNLIT));

    	if (extinguishByWater == true)
    		setItemHumidity(tagCompound, SharedDefines.TORCH_HUMIDITY_THRESHOLD);
    }
	
    /**
     *  Set this item torch on fire. Nothing will happen if the item is too wet or has already burned out.
     *  @param stack ItemStack instance of our torch to set on fire.
     *  @param playerIn player that is holding the torch.
     *  @param tagCompound the NBTTagCompound to be used for updating data.
     */
    public static void lightItemTorch(ItemStack stack, EntityPlayer playerIn, NBTTagCompound tagCompound)
    {
    	final boolean result = stack != null && tagCompound != null && stack.getItem() instanceof ItemTorch;
    	if (result && getItemHumidity(tagCompound) < SharedDefines.TORCH_HUMIDITY_THRESHOLD)
    	{
    		if (getItemcombustionTime(stack) > 0)
    		{
    			// If the equipped stack has more then one item, keep one item, light it on fire
    	    	// and move the rest of the stack in a different inventory slot (auto-assigned)
    	    	
				ItemStack newTorchLit = new ItemStack(Item.getItemFromBlock(ResourceLibrary.TORCH_LIT));
				ItemTorch.createCustomItemNBTFromExisting(newTorchLit, playerIn.worldObj, tagCompound);

				// An off-hand item has the same inventory slot designation as the first hotbar slot,
				// and the 'inventory.currentItem' variable holds the value of the currently selected slot in the hotbar.
				
				if (ItemStack.areItemStacksEqual(stack, playerIn.getHeldItemOffhand()))
					playerIn.setHeldItem(EnumHand.OFF_HAND, newTorchLit);
				
				else playerIn.replaceItemInInventory(playerIn.inventory.currentItem, newTorchLit);
                
    			if (stack.stackSize > 1)
    	    	{
        			ItemStack oldTorchUnlit = new ItemStack(Item.getItemFromBlock(ResourceLibrary.TORCH_UNLIT), stack.stackSize - 1);
        			playerIn.inventory.addItemStackToInventory(oldTorchUnlit);
    	    	}
    		}
    	}
    }
    
	/**
     * Update the remaining time the item torch is allowed to burn.
     * 
     * @param itemNBT Map of item's custom data used for updating
     * @param value Value to decrease the time for <i>(cannot be 0)</i>
     * @return The updated value or -1 if stack or NBT were not found
     */
    public static int updateItemCombustionTime(NBTTagCompound itemNBT, int value)
    {
    	if (itemNBT != null && value != 0)
    	{	
    		int combustion = itemNBT.getInteger("combustionTime");
    		combustion += ((combustion + value > 0) ? value : combustion * -1);   // Keep the value unsigned; 
    		itemNBT.setInteger("combustionTime", combustion);
    		
    		short itemDamage = (short) (itemNBT.getShort("torchItemDamage") + (value * -1));
    		itemNBT.setShort("torchItemDamage", itemDamage);
    	    
    		return combustion;
    	}
    	else return -1;
    }
    
    /**
     *  Update the humidity of this item by updating a custom NBT field. 
     *  
     * @param itemNBT Map of item's custom data used for updating
     * @param value Value to increment the humidity for <i>(cannot be 0)</i>
     * @return The updated value or -1 if stack or NBT were not found
     */
    public static int updateItemHumidity(NBTTagCompound itemNBT, int value)
    {
    	if (itemNBT != null && value != 0)
    	{
    		int humidity = getItemHumidity(itemNBT);
    		humidity += ((humidity + value < SharedDefines.TORCH_HUMIDITY_THRESHOLD) ?
    				value : SharedDefines.TORCH_HUMIDITY_THRESHOLD - humidity);
    	
    		itemNBT.setInteger("humidityLevel", humidity);
    		return humidity;
    	}
    	else return -1;
    }
	
    /**
     * Set the humidity level of an ItemStack to a new value.
     * 
     * @param itemNBT Map of item's custom data used for updating
     * @param value New value to update humidity to <i>(has to be positive)</i>
     */
    public static void setItemHumidity(NBTTagCompound itemNBT, int value)
    {
    	if (itemNBT != null && value >= 0)
    		itemNBT.setInteger("humidityLevel", value);
    }
    
	/**
     * Get humidity value for this item from NBT storage.<p>
     * 
     * @throws java.lang.NullPointerException if item tag compound is <code>null</code>
     * @param itemNBT Map of item's custom data used for updating
     * @return Returns the humidity value from item NBT
     */
    public static short getItemHumidity(NBTTagCompound itemNBT)
    {
    	return itemNBT.getShort("humidityLevel");
    }
	
    /**
     * Get combustion time value for this item from NBT storage.
     * 
     * @throws java.lang.NullPointerException if item tag compound is <code>null</code>
     * @param stack ItemStack to get the information from
     * @return Returns combustion duration value from item NBT
     */
    protected static short getItemcombustionTime(ItemStack stack)
    {
    	return stack.getTagCompound().getShort("combustionTime");
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
   		packet.setInteger("humidityLevel", itemStackData.getInteger("humidityLevel"));
   		packet.setInteger("combustionTime", itemStackData.getInteger("combustionTime")); 

   		return packet;
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
			itemTagCompound.setInteger("humidityLevel", newTagCompound.getInteger("humidityLevel"));
			
			// 'torchItemDamage' is a helper variable that we use so that we don't have to recalculate
			// how much material has the torch combusted so far on every GUI render update.
			
			int combustion = newTagCompound.getInteger("combustionTime");
			short itemDamage = (short) (SharedDefines.MAX_TORCH_FLAME_DURATION - combustion);
			
			itemTagCompound.setInteger("combustionTime", combustion);
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
	 */
	public static void createCustomItemNBTFromExisting(ItemStack stack, World world, NBTTagCompound tagCompound)
	{
		if (tagCompound != null && stack != null)
		{
			createCustomItemNBT(stack, world);
			updateCustomItemNBTFromExisting(stack, tagCompound);
		}
	}
	
	/**
	 * Create a custom item NBT tag compound for a specific item stack. <br>
	 * All the values will be initialized to default standards.<p>
	 * 
	 * <i>Use {@link #createCustomItemNBTFromExisting} if you already have NBT data for this item to inherit.</i>
	 */
	public static void createCustomItemNBT(ItemStack stack, World world)
	{	
		NBTTagCompound tagCompound = new NBTTagCompound();
		stack.setTagCompound(tagCompound);
		
		tagCompound.setInteger("humidityLevel", 0);
		tagCompound.setShort("torchItemDamage", (short) 0);
		tagCompound.setInteger("combustionTime", SharedDefines.MAX_TORCH_FLAME_DURATION);
		
		if (!world.isRemote)  // Do only on Server to avoid duplicates.	
			ExtendedItemProperties.createExtendedPropertiesForItem(stack, world);
	}
    
   /**
    * How much material has this torch expended with combustion so far?
    * 
    * @see {@link #getDurabilityForDisplay}
    * @param stack ItemStack to get the information from
    * @return Amount of material combusted from stack NBT, <br> -1 if item stack or NBT are <i>null</i>
    */
   private static short getTorchItemDamage(ItemStack stack)
   {
	   return (stack != null && stack.hasTagCompound()) ? stack.getTagCompound().getShort("torchItemDamage") : -1;
   }
   
   /**
    * Determines if the durability bar should be rendered for this item.
    * Defaults to vanilla stack.isDamaged behavior.
    * But modders can use this for any data they wish.
    *
    * @param stack The current Item Stack
    * @return True if it should render the 'durability' bar.
    */
   @Override
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
    
	/** Check to see if an item is a real torch (both custom or vanilla torch). */
    public static boolean isItemTorch(Item item, boolean checkVanilla)
    {
    	final boolean isCustomTorch = Block.getBlockFromItem(item) instanceof BlockTorch;
    	return isCustomTorch || checkVanilla && item == Item.getItemFromBlock(Blocks.TORCH);
    }
    
    /** Check to see if an item is a lit torch (both custom or vanilla torch). */
    public static boolean isItemTorchLit(Item item, boolean checkVanilla)
    {
    	final boolean isCustomLit = item == Item.getItemFromBlock(ResourceLibrary.TORCH_LIT);
    	return isCustomLit || checkVanilla && item == Item.getItemFromBlock(Blocks.TORCH);
    }
}
