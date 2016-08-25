package com.yooksi.fierysouls.common;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

public class FSGuiFactory implements IModGuiFactory
{
	// This class is accessed when Forge needs a GUI made relating to our mod (e.g. config GUI)
		
	@Override
	public void initialize(net.minecraft.client.Minecraft minecraftInstance) 
	{
		// needed to implement IModGuiFactory but doesn't really do anything.
	}
		
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() 
	{
		return FSConfigGui.class; // tells Forge which class represents our main GUI screen.
	}
		
	// The following two functions are needed for implementation only, 
	// the config GUI does not require them.
	
	@Override
	public java.util.Set<RuntimeOptionCategoryElement> runtimeGuiCategories() 
	{
		return null;
	}
		
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) 
	{
		return null;
	}

	/** This class inherits from GuiConfig, a specialized GuiScreen designed 
	 *  to display your configuration categories.
	 */
	public static class FSConfigGui extends GuiConfig 
		{
			public FSConfigGui(GuiScreen parentScreen) 
			{
				//I18n function basically "translates" or localizes the given key using the appropriate .lang file
				super(parentScreen, getConfigElements(), FierySouls.MODID,
	            false, false, I18n.format("gui.fs_configuration.mainTitle"));
			}
			
			/** Compiles a list of config elements */
			private static java.util.List<IConfigElement> getConfigElements() 
			{
				java.util.List<IConfigElement> list = new java.util.ArrayList<IConfigElement>();
				//Add the two buttons that will go to each config category edit screen
				list.add(new DummyCategoryElement("mainCfg", "gui.fs_configuration.ctgy.torches", (Class<? extends IConfigEntry>) CategoryEntryTorches.class));
				return list;
			}
		    
			public static class CategoryEntryTorches extends CategoryEntry
			{
				public CategoryEntryTorches(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
				{
					super(owningScreen, owningEntryList, prop);
				}
				
				@Override
				protected GuiScreen buildChildScreen() 
				{
					// Find all the config options that are going to be displayed in the GUI.
					
	                Configuration configuration = FSConfiguration.config;
	                ConfigElement cat_general = new ConfigElement(configuration.getCategory(FSConfiguration.TORCH_CATEGORY));
	                java.util.List<IConfigElement> propertiesOnThisScreen = cat_general.getChildElements();
	                String windowTitle = configuration.toString();

	                // For some reason we're getting duplicate config elements in our categories,
	                // so check for that and remove the duplicates from the category.
	                
	               final java.util.Set<String> set1 = new java.util.HashSet<String>();
	               
	               java.util.List<IConfigElement> propertiesOnThisScreen2 = new java.util.ArrayList<IConfigElement>(propertiesOnThisScreen.size());
	               propertiesOnThisScreen2.addAll(propertiesOnThisScreen);
	               
	               for (int i = 0; propertiesOnThisScreen2.size() > i; i++)
	               {
	            	   IConfigElement element = propertiesOnThisScreen2.get(i);
	            	   String name = element.getName();
	            	   if (!set1.add(name))
	            		   propertiesOnThisScreen.remove(element);  
	               }
	               
	               boolean worldRestart = this.configElement.requiresWorldRestart() ? true : this.owningScreen.allRequireWorldRestart;
	               boolean mcRestart = this.configElement.requiresMcRestart() ? true : this.owningScreen.allRequireMcRestart;
	        
	               return new GuiConfig(this.owningScreen, propertiesOnThisScreen, this.owningScreen.modID,               
	            		   FSConfiguration.TORCH_CATEGORY, worldRestart, mcRestart, windowTitle);
				}
			}
		}
}