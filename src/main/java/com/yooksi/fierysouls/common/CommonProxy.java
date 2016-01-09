package com.yooksi.fierysouls.common;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy 
{
	public void preInit(FMLPreInitializationEvent event) 
	{
	    registerResources();
		handleRecipes(); 
	}
	
	/** Register all items and blocks from the resource library with Forge */
	private static void registerResources() 
	{
		FierySouls.logger.info("Preparing to register item and block instances...");
		int objectsRegistered = 0;
		
		for (ResourceLibrary resource : ResourceLibrary.values())
		{
			// IMPORTANT: Removing "tile." string for blocks and "item." string for items is crucial here.
			// If we don't do this they will be registered under names different then those defined by our mod and listed in json files.
			// As a result your resources will not be loaded into the game.
			
			if (resource.isInstanceBlock())
			{
				GameRegistry.registerBlock(resource.getBlockInstance(), resource.getBlockInstance().getUnlocalizedName().replaceFirst("tile.", ""));
				objectsRegistered += 1;
			}
			else if (resource.isInstanceItem())
			{
				GameRegistry.registerItem(resource.getItemInstance(), resource.getItemInstance().getUnlocalizedName().replaceFirst("item.", ""));
				objectsRegistered += 1;
			}
			else FierySouls.logger.info("Warrning: Couldn't find our resource or we're trying to register an object of unknown type."); 
		}
		int maxItems = ResourceLibrary.values().length;
		String report = (objectsRegistered == maxItems) ? "all object instances succesfully registered!" : (int)(maxItems - objectsRegistered) + " were not registered, what happened?";
		FierySouls.logger.info("Finished registering object instances. " + report);
	}
	
	/** Register new custom recipes and remove vanilla ones */
	private void handleRecipes() 
    {	
		// Remove vanilla recipes here
		int recipesRemovedCount = 0;
		recipesRemovedCount += removeRecipe(Item.getItemFromBlock(net.minecraft.init.Blocks.torch));
		FierySouls.logger.info("Removed " + recipesRemovedCount + " vanilla recipes.");
		
		// Initialize the recipe library and add recipes to the resource library
		FierySouls.logger.info("Recipe library loaded, " + RecipeLibrary.values().length + " custom recipes have been loaded.");
		
		// Add our custom recipes here
		for (ResourceLibrary resource : ResourceLibrary.values())
		{
			java.util.Iterator<RecipeLibrary> recipeList = resource.recipeList.iterator();
			while (recipeList.hasNext())
			{
				RecipeLibrary recipe = recipeList.next();
				if (recipe.isRecipeShapeless())
					GameRegistry.addShapelessRecipe(recipe.getProductItemStack(), recipe.getRecipePattern());
			    	
				else GameRegistry.addShapedRecipe(recipe.getProductItemStack(), recipe.getRecipePattern());
			}
		}
    }
	/** Removes vanilla recipes from CraftingManager recipe list.
	 *  @param toRemove Item corresponding to the output of the recipe we want to remove.
	 *  @return Number of recipes we removed from the recipe list.
	 */ 
	private static int removeRecipe(Item toRemove)
	{
		int recipesRemoved = 0;
		java.util.List<IRecipe> recipeList = net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
		
		// Iterate through the recipe list and find the recipes we're looking for.
		// Search using iterators instead of manual indexing to increase reliability.
		
	    java.util.Iterator<IRecipe> recipeEntry = recipeList.iterator();
	    while (recipeEntry.hasNext())
	    {
	    	net.minecraft.item.ItemStack outputItem = recipeEntry.next().getRecipeOutput();
			if (outputItem != null && outputItem.getItem() == toRemove)
			{
				recipeEntry.remove();
				recipesRemoved++;
			}
	    }   return recipesRemoved;
	}
	
	public void init(FMLInitializationEvent event) 
	{
		int tileEntitiesRegistered = 0;
		for (ResourceLibrary resource : ResourceLibrary.values())
		{
			// In order to register them we need to pass the entity class as an argument.
			// This info should be stored in the resource library. If nothing is found, don't registered.
			
			if (resource.getTileEntityClass() != null)
			{
				GameRegistry.registerTileEntity(resource.getTileEntityClass(), resource.name);
			    tileEntitiesRegistered += 1;
			}
		}
		FierySouls.logger.info("Finished registering TileEntities, " + tileEntitiesRegistered + " entities was registered.");	
		this.registerResourceRenderers();
	}	
	
	// This function is overriden on the client proxy side
	protected void registerResourceRenderers() {}
}
