package com.yooksi.fierysouls.client;

import com.yooksi.fierysouls.common.CommonProxy;
import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.ResourceLibrary;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerResourceRenderers()
	{
		FierySouls.logger.info("Preparing to register item and block renderers...");
        int objectsRegistered = 0;
		
		// Register all item and block RENDERERS from the resource library with Forge */	
		for (ResourceLibrary resource : ResourceLibrary.values())
		{
			RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
			
			if (renderItem == null) FierySouls.logger.info("wtf....");
			
			// IMPORTANT: Removing "tile." string for blocks and "item." string for items is crucial here.
			// If we don't do this Forge will search their textures under names different then those defined by our mod and listed in json files.
			// As a result your resource textures will not be loaded into the game.
			
			if (resource.isInstanceBlock())
			{
				ModelResourceLocation location = new ModelResourceLocation(FierySouls.MODID + ":" + (resource.getBlockInstance()).getUnlocalizedName().replaceFirst("tile.", ""), "inventory");
				renderItem.getItemModelMesher().register(Item.getItemFromBlock(resource.getBlockInstance()), 0, location);
				objectsRegistered += 1;
			}
			else if (resource.isInstanceItem())
			{
				ModelResourceLocation location = new ModelResourceLocation(FierySouls.MODID + ":" + (resource.getItemInstance()).getUnlocalizedName().replaceFirst("item.", ""), "inventory");
				renderItem.getItemModelMesher().register(resource.getItemInstance(), 0, location);
				objectsRegistered += 1;
			}
			else FierySouls.logger.info("Warrning: Couldn't find our resource or we're trying to register an object of unknown type."); 
		}
		int maxItems = ResourceLibrary.values().length;
		String report = (objectsRegistered == maxItems) ? "all object renderers succesfully registered!" : (int)(maxItems - objectsRegistered) + " were not registred, what happened?";
		FierySouls.logger.info("Finished registering object renderers, " + report);
	}
}