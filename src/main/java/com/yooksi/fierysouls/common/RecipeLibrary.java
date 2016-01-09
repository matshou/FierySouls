package com.yooksi.fierysouls.common;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** This is where we store all our custom item recipes 
 *  Recipe library is initialized and used in "CommonProxy" to register new recipes with Forge
 */
public enum RecipeLibrary
{
	MATCHBOX_RECIPE(new ItemStack(ResourceLibrary.MATCHBOX.getItemInstance(), 1), new Object[] { Items.gunpowder, Items.stick, Items.paper  }, true, ResourceLibrary.MATCHBOX),
	GLOWSTONE_CRYSTAL_RECIPE(new ItemStack(ResourceLibrary.GLOWSTONE_CRYSTAL.getItemInstance(), 1), new Object[] { "x", "xyx", "x", 'x', Items.glowstone_dust, 'y', Items.coal }, false, ResourceLibrary.GLOWSTONE_CRYSTAL),
	TORCH_UNLIT_RECIPE_1(new ItemStack(ResourceLibrary.TORCH_UNLIT.getItemInstance(), 1), new Object[] { "x", "y", 'x', Items.coal, 'y', Items.stick }, false, ResourceLibrary.TORCH_UNLIT),
	TORCH_UNLIT_RECIPE_2(new ItemStack(ResourceLibrary.TORCH_UNLIT.getItemInstance(), 1), new Object[] { "x", "y", 'x', new ItemStack(Items.coal, 1, 1), 'y', Items.stick }, false, ResourceLibrary.TORCH_UNLIT);  // Charcoal version
	
	private final ItemStack product;       // Item that the recipe produces when crafted
	private final Object[] chart;         // Recipe pattern that will be used to register the recipe
	private final boolean isShapeless;
	
	/** This constructor will be called when any Enum constants are first called or referenced in code.	 
	 * 
	 * @param recipeProduct The item that's crafted when using this recipe.
	 * @param recipeChart The recipe pattern is used as a crafting parameter for shaped recipes. 
	 * @param isShapelessRecipe Determines registration method and usage of the chart.
	 * @param parentResource Resource enumerator of the product; used to add recipes to the resource library.
	 */
	private RecipeLibrary(ItemStack recipeProduct, Object[] recipeChart, boolean isShapelessRecipe, ResourceLibrary parentResource)
	{
		this.product = recipeProduct;
		this.chart = recipeChart;
		this.isShapeless = isShapelessRecipe;
		parentResource.addRecipeToLibrary(this);       // Write this recipe in the resource library for easier access
	}
	public Object[] getRecipePattern()
	{
		return this.chart;
	}
	public ItemStack getProductItemStack()
	{
		return this.product;
	}
    public boolean isRecipeShapeless()
    {
    	return this.isShapeless;
    }
}
