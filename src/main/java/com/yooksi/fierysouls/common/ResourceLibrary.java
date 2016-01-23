package com.yooksi.fierysouls.common;

import com.yooksi.fierysouls.entity.item.EntityItemTorch;
import com.yooksi.fierysouls.tileentity.*;
import com.yooksi.fierysouls.block.*;
import com.yooksi.fierysouls.item.*;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.item.EntityItem;
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
	TORCH_LIT(new BlockTorchLit(), "torch_lit", TileEntityTorchLit.class, ItemTorch.class, EntityItemTorch.class),
	TORCH_UNLIT(new BlockTorchUnlit(), "torch_unlit", TileEntityTorchUnlit.class, ItemTorch.class, EntityItemTorch.class),
	GLOWSTONE_CRYSTAL(new ItemGlowstoneCrystal(), "glowstone_crystal", null),
	MATCHBOX(new ItemMatchbox(), "matchbox", null);
	
	private final Class tileEntityClass;
	private final Class entityItemClass;
	private final Class itemBlockClass;
	
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
	private ResourceLibrary(Object resource, String resourceName, Class<? extends TileEntity>tileEntity, Class<? extends ItemBlock>itemBlock, Class<? extends EntityItem>entityItem, java.util.List recipes)
	{
		tileEntityClass = tileEntity;
		entityItemClass = entityItem;
		itemBlockClass = itemBlock;
		
		recipeList = recipes;
		instance = resource;
		name = resourceName;		
	}
	/** This constructor is used by resources that initialize BLOCKS. 
	 */
	private ResourceLibrary(Object resourceInstance, String resourceName, Class tileEntity, Class itemBlockClass, Class entityItem)
	{	
		this(resourceInstance, resourceName, tileEntity, itemBlockClass, entityItem, new java.util.ArrayList());
		getBlock().setUnlocalizedName(resourceName).setCreativeTab(FierySouls.tabTorches);
	}
	/** This constructor is used by resources that initialize ITEMS. 
	 */
	private ResourceLibrary(Object resourceInstance, String resourceName, Class entityItem)
	{
		this(resourceInstance, resourceName, null, null, entityItem, new java.util.ArrayList());
		getItem().setUnlocalizedName(resourceName).setCreativeTab(FierySouls.tabTorches);
	}
	
	// I've tried adding this to the constructor but the problem is that the recipe library uses resource library references
	// before we're properly initialized it. Have to call it from there after we initialize first.
	public final void addRecipeToLibrary(RecipeLibrary recipeToAdd)
	{
		this.recipeList.add(recipeToAdd);
	}
	
	/** 
	 * This will return the TileEntity class associated with this resource.
	 * @return <i>if return value is null, this resource does not spawn a TileEntity
	 */
	public Class getTileEntityClass()
	{
		return tileEntityClass;
	}
	/** 
	 * This will return the ItemBlock class associated with this resource.
	 * @return <i>if return value is null, this resource does not drop an ItemBlock type item
	 */
	public Class getItemBlockClass()
	{
		return itemBlockClass;
	}
	/** 
	 * This will return the EntityItem class associated with this resource.
	 * @return <i>if return value is null, this resource does not have a custom EntityItem class
	 */
	public Class getEntityItemClass()
	{
		return entityItemClass;
	}
	
	/** Find out if this object is a block */
	public boolean isResourceBlock()
	{
		return this.instance instanceof Block;
	}
	/** Find out if this object is an item */
	public boolean isResourceItem()
	{
		return this.instance instanceof Item;
	}
	
    public final Block getBlock()
    {	
    	return (isResourceBlock()) ? (Block)instance : Block.getBlockFromItem((Item)instance); 
    }
    public Item getItem()
    {
    	// In some cases we might want to call this function to get an item instance of a block.
    	// For example this would be needed when adding recipes.
    	
    	return (isResourceItem()) ? (Item)instance : Item.getItemFromBlock((Block)instance);
    }
}