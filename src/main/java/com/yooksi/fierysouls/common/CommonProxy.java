package com.yooksi.fierysouls.common;

import com.yooksi.fierysouls.block.*;
import com.yooksi.fierysouls.item.*;

import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy 
{
	/** Called by {@link FierySouls#preInit } in the preInit phase of mod loading. */
	public void preInit(FMLPreInitializationEvent event) 
	{
	    registerResources();
	}
	
	/** 
	 *  Register all items and blocks from the resource library with Forge. <br>
	 *  This is called when the mod is passing the pre-initialization phase.
	 */
	private static void registerResources() 
	{
		FierySouls.logger.info("Preparing to register item and block instances...");
		
		registerBlock(new BlockTorchLit(), "torch_lit");
		registerBlock(new BlockTorchUnlit(), "torch_unlit");
		
		registerItem(new ItemMatchbox(), "matchbox");
		registerItem(new ItemGlowstoneCrystal(), "glowstone_crystal");
		
		FierySouls.logger.info("Finished registering object instances. ");
	}
	
	private static <T extends net.minecraft.block.Block> void registerBlock(T block, String name) 
	{	
		block.setUnlocalizedName(name);
		block.setRegistryName(name);
		
		GameRegistry.register(block);
		GameRegistry.register(new net.minecraft.item.ItemBlock(block).setRegistryName(name));
	}
	
	private static <T extends net.minecraft.item.Item> void registerItem(T item, String name) 
	{
		item.setUnlocalizedName(name);
		item.setRegistryName(name);
		GameRegistry.register(item);
	}
	
	/** 
	 *  Register new custom recipes and remove vanilla ones. <br>
	 *  This is called when the mod is passing the pre-initialization phase.
	 */
	private void handleRecipes() 
    {	
		// Remove vanilla recipes here
		int recipesRemovedCount = 0;
		recipesRemovedCount += removeRecipe(Item.getItemFromBlock(net.minecraft.init.Blocks.TORCH));
		FierySouls.logger.info("Removed " + recipesRemovedCount + " vanilla recipes.");
		
		GameRegistry.addShapedRecipe(new ItemStack(ResourceLibrary.TORCH_UNLIT), new Object[]               // Unlit Torch standard recipe.
				{ "y", "x", 'x', Items.STICK, 'y', Items.COAL });   
		
		GameRegistry.addShapedRecipe(new ItemStack(ResourceLibrary.TORCH_UNLIT), new Object[]               // Unlit Torch crafted with CHARCOAL.
				{ "y", "x", 'x', Items.STICK, 'y', new ItemStack(Items.COAL, 1, 1) });  
		
		GameRegistry.addShapedRecipe(new ItemStack(Blocks.TORCH), new Object[]                              // Vanilla torch (permanently lit).
				{ "y", "x", 'x', Items.STICK, 'y', Items.GLOWSTONE_DUST });                                
		
        GameRegistry.addShapelessRecipe(new ItemStack(ResourceLibrary.MATCHBOX), new Object[]               // Matchbox standard recipe.
        		{ Items.GUNPOWDER, Items.STICK, Items.PAPER });
        
        GameRegistry.addShapedRecipe(new ItemStack(ResourceLibrary.GLOWSTONE_CRYSTAL), new Object[]         // Glowstone Crystal standard recipe.
        		{ ".x.", "xyx", ".x.", 'x', Items.GLOWSTONE_DUST, 'y', Items.COAL });
        
        GameRegistry.addShapedRecipe(new ItemStack(ResourceLibrary.GLOWSTONE_CRYSTAL), new Object[]         // Glowstone Crystal crafted with CHARCOAL.
        		{ ".x.", "xyx", ".x.", 'x', Items.GLOWSTONE_DUST, 'y', new ItemStack(Items.COAL, 1, 1) });
    }
	
	/** 
	 *  Removes vanilla recipes from the CraftingManager recipe list.
	 *  
	 *  @param toRemove Item corresponding to the output of the recipe we want to remove.
	 *  @return Number of recipes we removed from the recipe list.
	 *  
	 *  @throws java.lang.NullPointerException <br>
	 *          if either the crafting manager or the recipe list are not found.
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
	
	/** Called by {@link FierySouls#init } in the init phase of mod loading. */
	public void init(FMLInitializationEvent event) 
	{
		handleRecipes();
	}	
}
