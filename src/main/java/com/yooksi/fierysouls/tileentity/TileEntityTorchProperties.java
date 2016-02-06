package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.SharedDefines;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTorchProperties extends TileEntity
{
	private short combustionDuration;
	private long timeCreated;
	
	private byte updateTickCount = 0;
	private short humidityLevel = 0;
	private long torchAge = 0;
	
	protected TileEntityTorchProperties(final long worldTotalTime)
	{
		combustionDuration = SharedDefines.MAX_TORCH_FLAME_DURATION;
		timeCreated = worldTotalTime;
	}

	/**
	 *  This method is intended to be used as a performance optimizer.
	 *  @return True if this torch entity is ready to be updated.
	 */
	protected boolean isTorchReadyForUpdate()
	{
		/*
		 *  Update only at set intervals to reduce performance hits.
		 *  This is also a perfect encapsulated opportunity to update torch age.
		 */
		boolean ready = updateTickCount++ == SharedDefines.MAIN_UPDATE_INTERVAL;
	    updateTickCount -= (ready) ? updateTickCount : 0; torchAge++;
	    return ready;
	}
	
    /**
	 *  Update the time duration of torch coal combustion with a new value. <br>
	 *  It should update the value and return it for convenience sake.
	 *  
	 *  @param value to increment the duration with <i>(accepts negatives)</i>
	 *  @return Updated combustion duration value for practical purposes. <br>
	 *          <i>Note that the value is guaranteed to be kept unsigned for you</i>.
	 */
	protected short updateTorchCombustionDuration(int value) 
	{
		// Remember to keep the value unsigned;
		return combustionDuration += ((combustionDuration + value > 0) ? value : combustionDuration * -1);
	}

	/** 
	 *  @return The remaining combustion time <i>(expressed in ticks)</i> of this torch entity.
	 */
	protected short getTorchCombustionDuration() 
	{
		return combustionDuration;
	}
	
	/**
	 *  Update the persistent humidity of this torch entity with a new value. <br>
	 *  Increment when the torch is getting wet and decrement when it's drying off. <br>
	 *  In most conventional ways this method will be called when the torch is exposed to rain, <br>
	 *  or dipped in the body of water, either thrown or carried in.
	 *  
	 *  @param value to increment the humidity level with.
	 *  @return Updated torch entity humidity value for practical purposes. <br>
	 *          <i>Note that the value is guaranteed to be kept unsigned for you</i>.
	 */
	protected short updateTorchHumidityLevel(short value)
	{
		// Remember to keep the value unsigned;
		return humidityLevel += ((humidityLevel + value > 0) ? value : humidityLevel * -1);
	}
	
	/**
	 *  @return Representing how soaking wet this torch entity is. In the even of rain it
	 *          will represent the amount of time <i>(expressed in ticks)</i> this torch entity 
	 *          has been exposed to rain.
	 */
	protected short getTorchHumidityLevel()
	{
		return humidityLevel;
	}
	
	/** 
	 *  @return True if this torch has been exposed to rain for a long period of time.
	 */
    protected boolean isTorchInHighHumidity()
    {
    	return (humidityLevel >= SharedDefines.HUMIDITY_THRESHOLD);
    }
	
    /**
     *  Write custom data to the NBT passed as an argument.
     *  
     *  @param nbt NBTTagCompound to write the data to
     *  @return The freshly updated NBTTagCompound
     */
	protected NBTTagCompound saveToNBT(NBTTagCompound nbt)
    {
		nbt.setLong("torchAge", torchAge);
		nbt.setLong("timeCreated", timeCreated);
		nbt.setShort("humidityLevel", humidityLevel);
		nbt.setShort("combustionDuration", combustionDuration);
		
	    return nbt;
    }
	
	/**
     *  Update local variables with custom data from the argument.
     *  @param nbt NBTTagCompound to read the data from
     */
    protected void loadFromNBT(NBTTagCompound nbt)
    {  
	    torchAge = nbt.getLong("torchAge");
	    timeCreated = nbt.getLong("timeCreated");
        humidityLevel = nbt.getShort("humidityLevel");
        combustionDuration = nbt.getShort("combustionDuration");
    }
}
