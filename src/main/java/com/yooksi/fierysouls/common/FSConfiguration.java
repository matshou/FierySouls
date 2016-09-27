package com.yooksi.fierysouls.common;

import static net.minecraftforge.common.config.Property.Type.INTEGER;

import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.tileentity.TileEntityTorchLit;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;	

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
		  
	    java.util.ArrayList<Property> configProperties = new java.util.ArrayList<Property>();
	    
		/* 
		 * The following code is used to define the properties in the configuration file,
	     * their name, type, default values and comment. These affect what is displayed on the GUI.
	     * If the file already exists, the property values will already have been read from the file, otherwise they
	     * will be assigned the default value. 
	     */
	    
		final int MAX_TORCH_LIGHT_LEVEL = 13;                  // Index - #0

        String comment = "Defines the starting and maximum light value our custom torch will have.";
        Property configProperty = getFixedIntProperty(TORCH_CATEGORY, "maximum_torch_light_level", MAX_TORCH_LIGHT_LEVEL, comment, 0, 15);
        configProperties.add(configProperty);
        
        if (!loadConfigFromFile && readFieldsFromConfig && configProperty.getInt() != BlockTorchLit.MAX_TORCH_LIGHT_LEVEL)
        	ResourceLibrary.TORCH_LIT.updateBlockLightLevel(configProperty.getInt());
        
        else BlockTorchLit.MAX_TORCH_LIGHT_LEVEL = configProperty.getInt();
		
		final int MAX_TORCH_FLAME_DURATION_DEFAULT = 3500;     // Index - #1
		
		comment = "The amount of ticks this torch will burn before extinguishing itself.";
		configProperty = getFixedIntProperty(TORCH_CATEGORY, "max_torch_flame_duration", MAX_TORCH_FLAME_DURATION_DEFAULT, comment, 0, 24000);
        configProperties.add(configProperty);
		
        SharedDefines.MAX_TORCH_FLAME_DURATION = configProperty.getInt();
        
        final int HUMIDITY_THRESHOLD_DEFAULT = 150;           // Index - #2
        
        comment = "Maximum amount of time (in ticks) this torch is allowed to be exposed to rain before extinguishing.";
        configProperty = getFixedIntProperty(TORCH_CATEGORY, "humidity_threshold", HUMIDITY_THRESHOLD_DEFAULT, comment, 0, 24000);
        configProperties.add(configProperty);
        
        SharedDefines.HUMIDITY_THRESHOLD = configProperty.getInt();
        
        final int CATCH_FIRE_CHANCE_MULTIPLIER = 5;           // Index - #3
        
        comment = "This multiplier affects the chances of blocks being set on fire by torches. The higher the number the LOWER the chances.";
        configProperty = getFixedIntProperty(TORCH_CATEGORY, "catch_fire_chance_multiplier", CATCH_FIRE_CHANCE_MULTIPLIER, comment, 1, 100);
        configProperties.add(configProperty);
        
        TileEntityTorchLit.CATCH_FIRE_CHANCE_BASE *= configProperty.getInt();
        
        final boolean IS_OXYGEN_UPDATE_ENABLED_DEFAULT = true;  // Index - #4
        
        comment = "Should the torch burn out faster when enclosed in a small space without oxygen?";
        configProperty = config.get(TORCH_CATEGORY, "oxygen_update_enabled", IS_OXYGEN_UPDATE_ENABLED_DEFAULT, comment);
        configProperties.add(configProperty);
        
        TileEntityTorchLit.isOxygenUpdateEnabled = configProperty.getBoolean();
        
		/* 
		 * Write the class's variables back into the config properties and save to disk.
		 * This is done even for a 'loadFromFile == true', because some of the properties 
		 * may have been assigned default values if the file was empty or corrupt.
		 */
		
		configProperties.get(0).set(BlockTorchLit.MAX_TORCH_LIGHT_LEVEL);
		configProperties.get(1).set(SharedDefines.MAX_TORCH_FLAME_DURATION);
		configProperties.get(2).set(SharedDefines.HUMIDITY_THRESHOLD);
		//configProperties.get(3).set(TileEntityTorchLit.CATCH_FIRE_CHANCE_BASE);
		configProperties.get(4).set(TileEntityTorchLit.isOxygenUpdateEnabled);
		
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
	    	if (FierySouls.MODID.equals(event.getModID()) && event.getConfigID().equals(TORCH_CATEGORY) && !event.isRequiresMcRestart())
	    	{    			
	    		syncConfig(false, true);                     // save the GUI-altered values to disk.
	        }
	    }
	}
}
