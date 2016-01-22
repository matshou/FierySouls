package com.yooksi.fierysouls.common;

import com.yooksi.fierysouls.block.*;
import com.yooksi.fierysouls.item.*;
import com.yooksi.fierysouls.tileentity.*;

import net.minecraft.item.ItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/** 
 * List of all resources (items, blocks and entities) contained in this distribution. 
 * To create a new resource just create a new enumerator here and initialize the resource inside it.
 * Be sure to invoke the right constructor depending on the resource type (block or item).
 * Your resource instance will be registered with Forge in "CommonProxy" and it's renderers in "ClientProxy".
 * If you want to register tile entities just include the class or enter null if none exists.
 * 
 * Optionally if you want to add a recipe for your item visit the "RecipeLibrary".
 */
public enum ResourceLibrary
{
	TORCH_LIT(new BlockTorchLit(), "torch_lit", TileEntityTorchLit.class, ItemTorch.class),
	TORCH_UNLIT(new BlockTorchUnlit(), "torch_unlit", TileEntityTorchUnlit.class, ItemTorch.class),
	GLOWSTONE_CRYSTAL(new ItemGlowstoneCrystal(), "glowstone_crystal"),
	MATCHBOX(new ItemMatchbox(), "matchbox");
	
	public final Class tileEntityClass;
	public final Class itemBlockClass;
	private final Object instance;
	public final String name;
	
	// It's much more convenient if we write the recipes here, otherwise we have to run loop searches
	public java.util.List<RecipeLibrary> recipeList;
	
	/** The main constructor for this class, invoked by blocks and items.
	 *  @param resourceInstance Newly created instance of our resource; there is only one per resource.
	 *  @param resourceName The name will be used to register the resource with Forge.
	 *  @param entityClass The class of a TileEntity of this resource. Used to register said TileEntity with Forge.
	 *  @param itemBlockClass The item type to register with it, used by blocks that have custom item classes.
	 */
	private ResourceLibrary(Object resource, String resourceName, Class tileEntity, Class itemBlock, java.util.List recipes)
	{
		tileEntityClass = tileEntity;
		itemBlockClass = itemBlock;
		recipeList = recipes;
		instance = resource;
		name = resourceName;		
	}
	/** This constructor is used by resources that initialize BLOCKS. 
	 */
	private ResourceLibrary(Object resourceInstance, String resourceName, Class tileEntity, Class itemBlockClass)
	{	
		this(resourceInstance, resourceName, tileEntity, itemBlockClass, new java.util.ArrayList());
		getBlockInstance().setUnlocalizedName(resourceName).setCreativeTab(FierySouls.tabTorches);
	}
	/** This constructor is used by resources that initialize ITEMS. 
	 */
	private ResourceLibrary(Object resourceInstance, String resourceName)
	{
		this(resourceInstance, resourceName, null, null, new java.util.ArrayList());
		getItemInstance().setUnlocalizedName(resourceName).setCreativeTab(FierySouls.tabTorches);
	}
	
	// I've tried adding this to the constructor but the problem is that the recipe library uses resource library references
	// before we're properly initialized it. Have to call it from there after we initialize first.
	public final void addRecipeToLibrary(RecipeLibrary recipeToAdd)
	{
		this.recipeList.add(recipeToAdd);
	}
	
	/** Find out if this object is a block */
	public boolean isInstanceBlock()
	{
		return this.instance instanceof Block;
	}
	/** Find out if this object is an item */
	public boolean isInstanceItem()
	{
		return this.instance instanceof Item;
	}
	
    public final Block getBlockInstance()
    {	
    	return (isInstanceBlock()) ? (Block)instance : Block.getBlockFromItem((Item)instance); 
    }
    public Item getItemInstance()
    {
    	// In some cases we might want to call this function to get an item instance of a block.
    	// For example this would be needed when adding recipes.
    	
    	return (isInstanceItem()) ? (Item)instance : Item.getItemFromBlock((Block)instance);
    }
}