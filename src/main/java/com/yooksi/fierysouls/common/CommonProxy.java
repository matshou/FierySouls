package com.yooksi.fierysouls.common;

import java.util.List;

import com.yooksi.fierysouls.tileentity.TileEntityTorchUnlit;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
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
		removeRecipe(Item.getItemFromBlock(Blocks.torch));
		
		// Initialize the recipe library and add recipes to the resource library
		FierySouls.logger.info("Recipe library loaded, " + RecipeLibrary.values().length + " custom recipes have been loaded.");
		
		// Add our custom recipes here
		for (ResourceLibrary resource : ResourceLibrary.values())
		{
			RecipeLibrary recipe = resource.resourceIsMadeFrom();
			if (recipe != null)
			{
				if (recipe.isRecipeShapeless())
					GameRegistry.addShapelessRecipe(recipe.getProductItemStack(), recipe.getRecipePattern());
			    	
				else GameRegistry.addShapedRecipe(recipe.getProductItemStack(), recipe.getRecipePattern());
			}
		}
    }
	
	/** Removes recipes crafting a certain item from Forge recipe list */ 
	private int removeRecipe(Item item)
	{
		int recipesRemoved = 0;
		List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
		for (int i = 0; i < recipeList.size(); i++) 
		{
			ItemStack output = recipeList.get(i).getRecipeOutput();
			if (output != null && output.getItem() == item) 
			{	
				recipeList.remove(i);
				recipesRemoved++;
			}
		}
		// Let the calling function know how much recipes we removed
		return recipesRemoved;
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
