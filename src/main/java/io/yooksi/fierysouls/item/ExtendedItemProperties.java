package com.yooksi.fierysouls.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.yooksi.fierysouls.common.Logger;
import com.yooksi.fierysouls.common.SharedDefines;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Use these extended properties when you want to store information that for some reason
 *  you are <br> unable to store in the regular ItemStack NBTTagCompounds.
 *  
 *  @deprecated Forge 12.18.1.2021 has added a {@link ItemTorch#shouldCauseBlockBreakReset} <br>
 *              callback to Item to prevent blocks from reseting on NBT updates. We no longer have any need <br>
 *              for special NBT's and reroute updates, just override that method and return false.
 */
@Deprecated
public class ExtendedItemProperties extends NBTTagCompound
{
	private static HashMap<ExtendedItemProperties, Fingerprint> ItemPropertiesMap = new HashMap<ExtendedItemProperties, Fingerprint>();
	
	private ExtendedItemProperties(ItemStack stack, World world)
	{ 
		/*  Manually copy contents from the stack NBTTagCompound for speed and safety.
		 *  Block code copied from ItemTorch.updateCustomItemNBTFromExisting.
		 */
		
		NBTTagCompound itemTagCompound = stack.getTagCompound();
		this.setInteger("humidityLevel", itemTagCompound.getInteger("humidityLevel"));	
		
		int combustion = itemTagCompound.getInteger("combustionTime");
		short itemDamage = (short) (SharedDefines.MAX_TORCH_FLAME_DURATION - combustion);
		
		this.setInteger("combustionTime", combustion);
		this.setShort("torchItemDamage", itemDamage);
		
		this.setLong("lastUpdateTime", world.getTotalWorldTime());
	}
	
	@Override 
	public int hashCode()
	{
		/*
		 *  If the hashCode() value of an object has changed since it was added to the HashSet, 
		 *  it seems to render the object unremovable. If we don't override this and return a constant 
		 *  value like identityHashCode we will not be able to remove elements from ItemPropertiesMap.
		 *  The value NBTTagCompound returns as hashCode seems to change after some time and that's not good.
		 */
		
    	return System.identityHashCode(this);
    }
	
	/**
	 *  Fingerprint is an object used to identify different item custom NBT tag compounds. <br>
	 *  It <i>should</i> be unique to every item NBT so use them to recognize items apart. <br>
	 *  Every fingerprint is expected to be found in item NBT values for comparison.
	 */
	private static class Fingerprint
	{
		private final String sFingerprint;
		
		/**
		 *  This is where the fingerprint will be constructed. <br>
		 *  It is constructed with the following pattern: <p>
		 *  
		 *  <i>unlocalized name + "@" + NBT identity hash code</i>
		 *  @param item ItemStack to create the fingerprint for
		 */
		private Fingerprint(ItemStack item)
		{
			String unlocalizedName = item.getItem().getUnlocalizedName();
			String nbtHashCode = String.valueOf(System.identityHashCode(item.getTagCompound()));
			sFingerprint = unlocalizedName + "@" + nbtHashCode;
		}
		
		/** 
		 *  Compare this fingerprint with a string expression of another. <br>
		 *  Primarily used to identify and extended properties belonging to certain items.
		 *  
		 * @param fingerprint String expression of the second fingerprint
		 * @return True if the second fingerprint is not <code>null</code> and is identical to this one  
		 */
		public boolean validate(String fingerprint)
		{
			return sFingerprint.equals(fingerprint);
		}
	}
	
	/**
	 *  Create and register new extended properties for the argument item. <br>
	 *  <i>New properties will not be created if the item already has registered properties.</i>
	 *  
	 *  @param item ItemStack to create and register new extended properties for
	 *  @return Newly created extended properties, or <code>null</code> if item already has registered properties  
	 */
	public static ExtendedItemProperties createExtendedPropertiesForItem(ItemStack item, World world)
	{
		ExtendedItemProperties createdProperties = null;
		if (item != null && item.hasTagCompound())
		{
			// A new NBT fingerprint is imprinted in the item NBT the first time
			// the item creates personal extended properties.
			
			final Fingerprint fingerprint = new Fingerprint(item);
			item.getTagCompound().setString("fingerprint", fingerprint.sFingerprint);
			
			createdProperties = new ExtendedItemProperties(item, world);
			ItemPropertiesMap.put(createdProperties, fingerprint);
		}
		else Logger.error("Failed to create extended item properties for ItemTorch.", item == null ? 
				new NullPointerException("The ItemStack was passed as null.") : new java.util.NoSuchElementException("The ItemStack has no TagCompound."));
		
		return createdProperties;
	}
	
	/** 
	 *  Our own custom garbage collector used for cleaning straggling NBTTagCompounds in <br>
	 *  <i>ItemPropertiesMap</i>. belonging to ItemStacks that have been removed from world. <p>
	 *  
	 *  The elements will be removed after they've not been updated for a certain amount of time.
	 * 
	 * @param world instance of the currently active world
	 */
	public static void callPropertiesGarbageCollector(World world)
	{
		if (ItemPropertiesMap.isEmpty())
			return;
		
		for (Iterator<Map.Entry<ExtendedItemProperties, Fingerprint>> i = ItemPropertiesMap.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<ExtendedItemProperties, Fingerprint> entry = i.next();
			if (world.getTotalWorldTime() - entry.getKey().getLong("lastUpdateTime") > 100)
				i.remove();
		}
	}
	
	/**
	 *  Try to find extended properties belonging to the argument item. <br>
	 *  Map entries will be compared using custom NBT <i>Fingerprints</i>.
	 *  
	 *  @param item ItemStack we're trying to find properties for
	 *  @return A reference to found properties, or a newly created properties if no properties were found
	 */
	public static ExtendedItemProperties findOrCreateExtendedPropertiesForItem(final ItemStack item, World world)
	{
		for (Map.Entry<ExtendedItemProperties, Fingerprint> entry : ItemPropertiesMap.entrySet())
		{
			if (entry.getValue().validate(item.getTagCompound().getString("fingerprint")))
				return entry.getKey();
		}
		
		return createExtendedPropertiesForItem(item, world);
	}
}
