package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.common.FierySouls;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;

public class TileEntityTorch extends TileEntity implements IUpdatePlayerListBox
{
	protected static final short MAX_TORCH_FLAME_DURATION = 4500;   // What is the longest this torch will be able to be on fire?
	protected static final short HUMIDITY_THRESHOLD = 300;         // How wet must the torch be before it cannot burn anymore?
	
	protected static final byte MAIN_UPDATE_INTERVAL = 10;       // Number of ticks that need to elapse before we update. 
	protected static final float RAIN_STR_THRESHOLD = 0.85F;    // How strong must the rain be falling to extinguish the torch?
	protected static final byte SMOLDERING_RANDOM = 125;       // Random factor in determining how long is the torch going to smolder.
      
	private short combustionDuration = MAX_TORCH_FLAME_DURATION;
	private short humidityLevel = 0;
	
	protected byte updateTickCount = 0;
	protected long timeCreated = 0;
	protected long torchAge = 0;
	
	public TileEntityTorch() {};
	protected TileEntityTorch(long totalWorldTime) 
	{
		timeCreated = totalWorldTime;
	}
	public void postInit(short combustion, short humidity, long timeCreated, long worldTime)
	{
		this.humidityLevel = humidity;
	    this.combustionDuration = combustion;
	    this.torchAge = worldTime - timeCreated;
	}
	
	@Override
	// This updates every tileEntity tick on both client and server side
	public void update() {};
	
	/** Update the time duration of torch coal combustion with a new value.
	 *  @param value to increment the duration with (accepts negatives). 
	 */
	protected short updateCombustionDuration(final int value)
	{ 
		return combustionDuration += ((combustionDuration + value > 0) ? value : combustionDuration * -1);  // Keep the value unsigned;
	}
	protected short getCombustionDuration()
	{
		return combustionDuration;
	}
	
	/** Update torch humidity level with a new value.
	 *  @param value to increment the humidity level with (accepts negatives).
	 */
	protected short updateHumidityLevel(short value)
	{
	    return humidityLevel += value;
	}
	protected short getHumidityLevel()
	{
		return humidityLevel;
	}
	/** Check if this torch has been exposed to rain for a long period of time. */
    protected boolean isHighHumidity()
    {
    	return (humidityLevel > HUMIDITY_THRESHOLD);
    }
    
	@Override
    public void writeToNBT(NBTTagCompound par1)
    {
		super.writeToNBT(par1);
		par1.setLong("torchAge", torchAge);
		par1.setLong("timeCreated", timeCreated);
	    par1.setShort("humidityLevel", getHumidityLevel());
	    par1.setShort("combustionDuration", combustionDuration);
    }
    @Override
    public void readFromNBT(NBTTagCompound par1)
    {  
	    super.readFromNBT(par1);
	    torchAge = par1.getLong("torchAge");
	    timeCreated = par1.getLong("timeCreated");
        humidityLevel = par1.getShort("humidityLevel");
        combustionDuration = par1.getShort("combustionDuration");
        
        /** DEBUG LOG - Tracking issue #3 */
        if (this instanceof TileEntityTorchLit)
        	FierySouls.logger.info("Reading from NBT for TileEntityTorchLit, combustion: " + this.combustionDuration);
    }
}