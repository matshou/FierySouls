package com.yooksi.fierysouls.common;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Loader;

import static net.minecraftforge.common.config.Property.Type.INTEGER;

import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.tileentity.TileEntityTorch;
import com.yooksi.fierysouls.tileentity.TileEntityTorchLit;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;	

public class FSConfiguration 
{
	// Define the configuration object
	protected static Configuration config = null;
	
	public static final String TORCH_CATEGORY = "torch_category";
	
	public static void preInit() 
	{
		/* specify the location from where the config file will be read, or created 
		 * if it is not present and initialize configuration object with config file values. 
		 */
		java.io.File configFile = new java.io.File(Loader.instance().getConfigDir(), "FierySouls.cfg");
		config = new Configuration(configFile);
		
	    syncFromFile();
	}
	
	public static void clientPreInit() 
	{
	    /*
	     * Register the save config handler to the Forge event bus,
	     * creates an instance of the static class ConfigEventHandler and has it listen
	     * on the core Forge event bus (see Notes and ConfigEventHandler for more information) 
	     */
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ConfigEventHandler());
	}
	
	/** Load the configuration values from the configuration file. */ 
	public static void syncFromFile()
	{
		syncConfig(true, true);
	}
	
	/** Save the configuration variables (fields) to disk. */
	public static void syncFromFields()
	{
		syncConfig(false, false);
	}
	 
	private static void syncConfig(boolean loadConfigFromFile, boolean readFieldsFromConfig)
	{
		if (loadConfigFromFile) 
			config.load();       // load raw values from config file
		
		/* By defining a property order we can control the order of the properties in the 
		 * config file and GUI. This is defined on a per config-category basis. */
		
	    java.util.List<String> propOrderTorch = new java.util.ArrayList<String>();
	    config.setCategoryPropertyOrder(TORCH_CATEGORY, propOrderTorch);
	    
	    java.util.ArrayList<Property> configProperties = new java.util.ArrayList<Property>();
	    
		/* 
		/* The following code is used to define the properties in the configuration file,
	     * their name, type, default values and comment. These affect what is displayed on the GUI.
	     * If the file already exists, the property values will already have been read from the file, otherwise they
	     * will be assigned the default value. 
	     */
	    
		final int MAX_TORCH_LIGHT_LEVEL = 13;                  // Index - #0

        String comment = "Defines the starting and maximum light value our custom torch will have.";
        Property configProperty = getFixedIntProperty(TORCH_CATEGORY, "maximum_torch_light_level", MAX_TORCH_LIGHT_LEVEL, comment, 0, 15);
        
        propOrderTorch.add(configProperty.getName());
        configProperties.add(configProperty);
        
        if (!loadConfigFromFile && readFieldsFromConfig && configProperty.getInt() != BlockTorchLit.MAX_TORCH_LIGHT_LEVEL)
        	ResourceLibrary.TORCH_LIT.updateBlockLightLevel(configProperty.getInt());
		
		final int MAX_TORCH_FLAME_DURATION_DEFAULT = 3500;     // Index - #1
		
		comment = "The amount of ticks this torch will burn before extinguishing itself.";
		configProperty = getFixedIntProperty(TORCH_CATEGORY, "max_torch_flame_duration", MAX_TORCH_FLAME_DURATION_DEFAULT, comment, 0, 24000);
        
		propOrderTorch.add(configProperty.getName());
        configProperties.add(configProperty);
		
        TileEntityTorchLit.MAX_TORCH_FLAME_DURATION = configProperty.getInt();
        
        final int HUMIDITY_THRESHOLD_DEFAULT = 300;           // Index - #2
        
        comment = "Maximum amount of time (in ticks) this torch is allowed to be exposed to rain before extinguishing.";
        configProperty = getFixedIntProperty(TORCH_CATEGORY, "humidity_threshold", HUMIDITY_THRESHOLD_DEFAULT, comment, 0, 24000);
        
        propOrderTorch.add(configProperty.getName());
        configProperties.add(configProperty);
        
        TileEntityTorch.HUMIDITY_THRESHOLD = configProperty.getInt();
        
		/* 
		 * Write the class's variables back into the config properties and save to disk.
		 * This is done even for a 'loadFromFile == true', because some of the properties 
		 * may have been assigned default values if the file was empty or corrupt.
		 */
		
		configProperties.get(0).set(BlockTorchLit.MAX_TORCH_LIGHT_LEVEL);
		configProperties.get(1).set(TileEntityTorchLit.MAX_TORCH_FLAME_DURATION);
		configProperties.get(2).set(TileEntityTorch.HUMIDITY_THRESHOLD);
		
		if (config.hasChanged())
			config.save();
	}
	
	/** 
	 * This method is intended to replace the main Property integer get method.<br>
	 * For some reason the method in question does not account for the fact that the property value<br>
	 * will be a decimal number instead of a clean integer and thus will not be parsed correctly.<p>
	 * 
     * @param category the config category
     * @param key the Property key value
     * @param defaultValue the default value
     * @param comment a String comment
     * @param minValue minimum boundary
     * @param maxValue maximum boundary
     * @return a FIXED integer Property object with the defined comment, minimum and maximum bounds
	 * */
	private static Property getFixedIntProperty(String category, String key, int defaultValue, String comment, int minValue, int maxValue)
	{
		Property prop = config.get(category, key, Integer.toString(defaultValue), comment, INTEGER);
		prop.setValue((int) Float.parseFloat(prop.getString()));
		
		prop.setMinValue(minValue);
        prop.setMaxValue(maxValue);

        if (!prop.isIntValue())
        	prop.setValue(defaultValue);
        
        return prop;
	}
	
	public static class ConfigEventHandler 
	{
	    /*
	     * This class, when instantiated as an object, will listen on the Forge
	     * event bus for an OnConfigChangedEvent
	     */
	    @SubscribeEvent(priority = EventPriority.NORMAL)
	    public void onEvent(ConfigChangedEvent.OnConfigChangedEvent event) 
	    {
	    	if (FierySouls.MODID.equals(event.getModID()) && event.getConfigID().equals(TORCH_CATEGORY))
	    	{
	    		// Don't update config if the world requires a restart.
	    		boolean result = event.isWorldRunning() && !config.getCategory(TORCH_CATEGORY).requiresWorldRestart();
	    		
	    		if (result && !event.isRequiresMcRestart())
	    			syncConfig(false, true);                     // save the GUI-altered values to disk.
	        }
	    }
	}
}
