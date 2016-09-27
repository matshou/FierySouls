package com.yooksi.fierysouls.common;

public class SharedDefines 
{
	/** Maximum amount of time (in ticks) the torch is allowed to be exposed to rain before extinguishing. */ 
	public static int HUMIDITY_THRESHOLD;
	
	/** The amount of ticks this torch will burn before extinguishing itself. */
	public static int MAX_TORCH_FLAME_DURATION;
	
	static public enum TorchUpdateTypes 
	{
		/* Note: if your update type is being called in the code somewhere after the MAIN_UPDATE,
		 *       declare your interval with taking into account the main update interval.    
		 */
		
		MAIN_UPDATE(0, 10),                // Used to validate entry into the whole torch update() block.
		OXYGEN_UPDATE(1, 8);               // Used to schedule an 'checkIsTorchEnclosed()' update call.
		
		public final int index;
		public final int interval;
		
		TorchUpdateTypes(int dex, int time) 
		{ 
			index = dex;
			interval = time;
		}
	}
}