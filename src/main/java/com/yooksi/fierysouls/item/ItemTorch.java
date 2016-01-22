package com.yooksi.fierysouls.item;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraft.world.World;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;

public class ItemTorch extends ItemBlock
{
	public ItemTorch(net.minecraft.block.Block block) 
	{
		super(block);
		this.setMaxDamage(-1);   // Disable damage for this item.
	}
	
	/** Use an existing NBTTC to update item stack NBT by extracting data directly from it. <p>
	 * <i><b>Note</b> that this does not create but just update an already existing NBT Tag Compound.</i>
	 *  
	 * @param stack Item stack to update
	 * @param newTagCompound NBT Tag Compound to extract data from
	 */
	public static void updateCustomItemNBTFromExisting(ItemStack stack, NBTTagCompound newTagCompound)
	{
		if (newTagCompound != null && stack.hasTagCompound())
		{
			NBTTagCompound itemTagCompound = stack.getTagCompound();
			itemTagCompound.setShort("humidityLevel", newTagCompound.getShort("humidityLevel"));
			itemTagCompound.setShort("combustionDuration", newTagCompound.getShort("combustionDuration"));
		}
	}
	
	/**
	 * Create a custom item NBT tag compound for a specific item stack. <br>
	 * <i>This makes the item stack carry the same information as <b>TileEntityTorch</b> and <b>EntityItemTorch</b>.
	 * 
	 * @param stack ItemStack to create a new NBT for
	 */
	public static void createCustomItemNBT(ItemStack stack)
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		
		tagCompound.setShort("humidityLevel", (short)0);
		tagCompound.setShort("combustionDuration", SharedDefines.MAX_TORCH_FLAME_DURATION);
		
		stack.setTagCompound(tagCompound);
	}
	
	/** 
	 * Create a custom item NBT tag compound for a specific item stack <b><i>from an existing tag compound</b></i>.<p>
	 * <i>This is an attempt at micro-optimizing because we could just pass the existing NBT to item <br> 
	 * without creating a new one, however this might save some memory as it will not carry useless data.</i>
	 *  
	 * @param stack ItemStack to create a new NBT for
	 * @param tagCompound Existing NBT tag compound to extract data from
	 */
	public static void createCustomItemNBTFromExisting(ItemStack stack, NBTTagCompound tagCompound)
	{
		if (tagCompound != null)
		{
			stack.setTagCompound(new NBTTagCompound());
			updateCustomItemNBTFromExisting(stack, tagCompound);
		}
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

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    	// TODO: When an item is added to the inventory from the creativeTab it ends up
    	//       without a proper custom NBT, so we do it here. Find a better way of handling this...
    	
    	if (!worldIn.isRemote && !stack.hasTagCompound())
    		createCustomItemNBT(stack);
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
