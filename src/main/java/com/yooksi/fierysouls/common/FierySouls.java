package com.yooksi.fierysouls.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = FierySouls.MODID, version = FierySouls.VERSION, name= FierySouls.NAME, acceptedMinecraftVersions = "[1.8.0]")
public class FierySouls 
{
	public static final String MODID = "fierysouls";
	public static final String NAME = "Fiery Souls";
	public static final String VERSION = "1.2.8";

	// Use this thingy to print in the console:
	public static final Logger logger = LogManager.getLogger(MODID);
	
	// This is where all our custom items should be listed in-game
	public static final CreativeTabs tabTorches = new CreativeTabs("FierySouls") 
	{
		@Override
	    @SideOnly(Side.CLIENT)
		public net.minecraft.item.Item getTabIconItem() 
		{ return net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.torch); }
	};
	
	@Mod.Instance(MODID)
	public static FierySouls instance;

	// Register client and server proxies 
	@SidedProxy(clientSide = "com.yooksi.fierysouls.client.ClientProxy", serverSide = "com.yooksi.fierysouls.common.CommonProxy")
	public static CommonProxy proxy;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		// Create new item registry and register items
		proxy.preInit(event);   
	}
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) 
	{
		FMLCommonHandler.instance().bus().register(instance);
		proxy.init(event);
	}
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		// The majority of events use the MinecraftForge event bus:
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new EventHandler());
	}
}