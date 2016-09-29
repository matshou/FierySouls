package com.yooksi.fierysouls.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Use these extended properties when you want to store information that for some reason
 *  you are <br> unable to store in the regular ItemStack NBTTagCompounds.
 */
public class ExtendedItemProperties extends NBTTagCompound
{
	private static HashMap<ExtendedItemProperties, Fingerprint> ItemPropertiesMap = new HashMap();
	
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
	public static ExtendedItemProperties createExtendedPropertiesForItem(final ItemStack item)
	{
		ExtendedItemProperties createdProperties = null;
		if (item != null && item.hasTagCompound() && findExtendedPropertiesForItem(item) == null)
		{
			// A new NBT fingerprint is imprinted in the item NBT the first time
			// the item creates personal extended properties.
			
			final Fingerprint fingerprint = new Fingerprint(item);
			item.getTagCompound().setString("fingerprint", fingerprint.sFingerprint);
			
			createdProperties = new ExtendedItemProperties();
			createdProperties.merge(item.getTagCompound());
			ItemPropertiesMap.put(createdProperties, fingerprint);
		}
		
		return createdProperties;
	}
	
	/**
	 *  Remove extended properties from the global properties registry for the selected item. <br>
	 *  <i><b>Note</b> that the property itself will not be destroyed, only removed from the registry.
	 *  
	 *  @param item ItemStack to remove properties for
	 */
	public static void unregisterExtendedPropertyForItem(final ItemStack item)
	{		
		for (Iterator<Map.Entry<ExtendedItemProperties, Fingerprint>> i = ItemPropertiesMap.entrySet().iterator(); i.hasNext();)
		{
		    Map.Entry<ExtendedItemProperties, Fingerprint> entry = i.next();
		    if (entry.getValue().validate(item.getTagCompound().getString("fingerprint")))
		        i.remove();
		}
	}
	
	/**
	 *  Try to find extended properties belonging to the argument item. <br>
	 *  Map entries will be compared using custom NBT <i>Fingerprints</i>.
	 *  
	 *  @param item ItemStack we're trying to find properties for <i>(null-safe)</i>
	 *  @return A reference to found properties, or <code>null</code> if no properties were found
	 */
	public static ExtendedItemProperties findExtendedPropertiesForItem(final ItemStack item)
	{
		ExtendedItemProperties foundProperties = null;
		for (Map.Entry<ExtendedItemProperties, Fingerprint> entry : ItemPropertiesMap.entrySet())
		{
			if (entry.getValue().validate(item.getTagCompound().getString("fingerprint")))
				foundProperties = entry.getKey();
		}
		
		return foundProperties;
	}
}
