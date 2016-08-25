package com.yooksi.fierysouls.network;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

import com.yooksi.fierysouls.common.CommonProxy;
import com.yooksi.fierysouls.common.ResourceLibrary;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		
		// Used to set up an event handler for the GUI so that the altered values are saved back to disk.
		com.yooksi.fierysouls.common.FSConfiguration.clientPreInit();
		
		// This step is necessary in order to make your block render properly when it is an item (i.e. in the inventory or in your hand or thrown on the ground).
	    // It must be done on client only, and must be done after the block has been created in Common.preinit().
		
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ResourceLibrary.TORCH_LIT), 0, new ModelResourceLocation(ResourceLibrary.TORCH_LIT.getRegistryName().toString()));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ResourceLibrary.TORCH_UNLIT), 0, new ModelResourceLocation(ResourceLibrary.TORCH_UNLIT.getRegistryName().toString()));
		
		ModelLoader.setCustomModelResourceLocation(ResourceLibrary.MATCHBOX, 0, new ModelResourceLocation(ResourceLibrary.MATCHBOX.getRegistryName().toString()));
		ModelLoader.setCustomModelResourceLocation(ResourceLibrary.GLOWSTONE_CRYSTAL, 0, new ModelResourceLocation(ResourceLibrary.GLOWSTONE_CRYSTAL.getRegistryName().toString()));
	}
}