package com.yooksi.fierysouls.common;

import net.minecraft.creativetab.CreativeTabs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = FierySouls.MODID, version = FierySouls.VERSION, name= FierySouls.NAME, acceptedMinecraftVersions = "[1.10,1.10.2]",
     guiFactory= FierySouls.GUIFACTORY)

public class FierySouls 
{
	public static final String MODID = "fierysouls";
	public static final String NAME = "Fiery Souls";
	public static final String VERSION = "1.4.5";
	
	public static final String GUIFACTORY = "com.yooksi.fierysouls.common.FSGuiFactory";

	// Use this thing to print in the console:
	public static final Logger logger = LogManager.getLogger(MODID);
	
	// This is where all our custom items should be listed in-game
	public static final CreativeTabs tabTorches = new CreativeTabs("FierySouls") 
	{
		@Override
	    @SideOnly(Side.CLIENT)
		public net.minecraft.item.Item getTabIconItem() 
		{ return net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.TORCH); }
	};
	
	/** The instance of our mod that Forge uses. */
	@Mod.Instance(MODID)
	public static FierySouls instance;

	@SidedProxy(clientSide = "com.yooksi.fierysouls.network.ClientProxy",   
			serverSide = "com.yooksi.fierysouls.network.ServerProxy")
	
	public static CommonProxy proxy;
	
	/** Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry. */
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) 
	{
		proxy.preInit(event);   
	}
	
	/** Do your mod setup. Build whatever data structures you care about. Register recipes. */
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) 
	{
		proxy.init(event);
	}
	
	/** Handle interaction with other mods, complete your setup based on this. */
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		// The majority of events use the MinecraftForge event bus:
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new EventHandler());
	}
}