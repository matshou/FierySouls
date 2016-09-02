package com.yooksi.fierysouls.common;

import com.yooksi.fierysouls.block.*;
import com.yooksi.fierysouls.item.*;
import com.yooksi.fierysouls.tileentity.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class CommonProxy 
{
	/** Called by {@link FierySouls#preInit } in the preInit phase of mod loading. */
	public void preInit(FMLPreInitializationEvent event) 
	{
		FSConfiguration.preInit();
		
	    registerResources();
	    registerTileEntities();
	    
	    CustomSoundEvents.registerSounds();
	}
	
	/** 
	 *  Register all items and blocks from the resource library with Forge. <br>
	 *  This is called when the mod is passing the pre-initialization phase.
	 */
	private void registerResources() 
	{
		FierySouls.logger.info("Preparing to register item and block instances...");
		
		registerBlock(new BlockTorchLit(), "torch_lit", new ItemTorch(BlockTorchLit.localInstance));
		registerBlock(new BlockTorchUnlit(), "torch_unlit", new ItemTorch(BlockTorchUnlit.localInstance));
		
		registerItem(new ItemMatchbox(), "matchbox");
		registerItem(new ItemGlowstoneCrystal(), "glowstone_crystal");
		
		FierySouls.logger.info("Finished registering object instances. ");
	}
	
	/** Register all custom tile entities with Forge. */
	private void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityTorch.class, "fierysouls:tile_entity_torch");
		GameRegistry.registerTileEntity(TileEntityTorchLit.class, "fierysouls:tile_entity_torch_lit");
		GameRegistry.registerTileEntity(TileEntityTorchUnlit.class, "fierysouls:tile_entity_torch_unlit");
	}
	
	private static <T extends net.minecraft.block.Block> void registerBlock(T block, String name, ItemBlock itemBlock) 
	{	
		block.setUnlocalizedName(name);
		block.setRegistryName(name);
		
		GameRegistry.register(block);
		GameRegistry.register(itemBlock.setRegistryName(name));  // Forge says to register ItemBlock as item.
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
		
		GameRegistry.addShapedRecipe(new ItemStack(ResourceLibrary.TORCH_UNLIT), new Object[]                      // Unlit Torch standard recipe.
				{ "y", "x", 'x', Items.STICK, 'y', Items.COAL });   
		
		GameRegistry.addShapedRecipe(new ItemStack(ResourceLibrary.TORCH_UNLIT), new Object[]                      // Unlit Torch crafted with CHARCOAL.
				{ "y", "x", 'x', Items.STICK, 'y', new ItemStack(Items.COAL, 1, 1) });  
		
		GameRegistry.addShapedRecipe(new ItemStack(Blocks.TORCH), new Object[]                                     // Vanilla torch (permanently lit).
				{ "y", "x", 'x', Items.STICK, 'y', Items.GLOWSTONE_DUST });                                
		
        GameRegistry.addShapelessRecipe(new ItemStack(ResourceLibrary.MATCHBOX), new Object[]                      // Matchbox recipe with ALL planks.
        		{ Items.GUNPOWDER, new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE), Items.PAPER });
        
        GameRegistry.addShapedRecipe(new ItemStack(ResourceLibrary.GLOWSTONE_CRYSTAL), new Object[]                // Glowstone Crystal standard recipe.
        		{ ".x.", "xyx", ".x.", 'x', Items.GLOWSTONE_DUST, 'y', Items.COAL });
        
        GameRegistry.addShapedRecipe(new ItemStack(ResourceLibrary.GLOWSTONE_CRYSTAL), new Object[]                // Glowstone Crystal crafted with CHARCOAL.
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
