package com.yooksi.fierysouls.client;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.CommonProxy;
import com.yooksi.fierysouls.common.ResourceLibrary;

import net.minecraft.client.resources.model.ModelResourceLocation;
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
			net.minecraft.client.renderer.entity.RenderItem renderItem =
					net.minecraft.client.Minecraft.getMinecraft().getRenderItem();

			// IMPORTANT: Removing "tile." string for blocks and "item." string for items is crucial here.
			// If we don't do this Forge will search their textures under names different then those defined by our mod and listed in json files.
			// As a result your resource textures will not be loaded into the game.
			
			if (resource.isResourceBlock())
			{
				ModelResourceLocation location = new ModelResourceLocation(FierySouls.MODID + ":" + (resource.getBlock()).getUnlocalizedName().replaceFirst("tile.", ""), "inventory");
				renderItem.getItemModelMesher().register(net.minecraft.item.Item.getItemFromBlock(resource.getBlock()), 0, location);
				objectsRegistered += 1;
			}
			else if (resource.isResourceItem())
			{
				ModelResourceLocation location = new ModelResourceLocation(FierySouls.MODID + ":" + (resource.getItem()).getUnlocalizedName().replaceFirst("item.", ""), "inventory");
				renderItem.getItemModelMesher().register(resource.getItem(), 0, location);
				objectsRegistered += 1;
			}
			else FierySouls.logger.info("Warrning: Couldn't find our resource or we're trying to register an object of unknown type."); 
		}
		int maxItems = ResourceLibrary.values().length;
		String report = (objectsRegistered == maxItems) ? "all object renderers succesfully registered!" : (int)(maxItems - objectsRegistered) + " were not registred, what happened?";
		FierySouls.logger.info("Finished registering object renderers, " + report);
	}
}