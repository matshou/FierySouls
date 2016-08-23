package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.SharedDefines;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.nbt.NBTTagCompound;

public final class TileEntityTorch extends TileEntity implements ITickable
{
	private short combustionTime;        // amount of ticks before the torch burns out.
	public long timeCreated;             // 'totalWorldTime' this entity was created.
	
	private byte updateTickCount = 0;    // used to keep track of passed updates per interval.
	private short humidityLevel = 0;     // amount of ticks the torch has been exposed to water.
	
	// This constructor is NEEDED during entity loading by FML:
	public TileEntityTorch() {};
	
	public TileEntityTorch(long totalWorldTime) 
	{
		combustionTime = SharedDefines.MAX_TORCH_FLAME_DURATION;
		timeCreated = totalWorldTime;
	}
	
	@Override
	public void update() {}
	
	/**
	 *  This method is intended to be used as a performance optimizer.<br>
	 *  Update only at set intervals to reduce performance hits.
	 */
	protected boolean isTorchReadyForUpdate()
	{
		boolean ready = updateTickCount++ == SharedDefines.MAIN_UPDATE_INTERVAL;
	    updateTickCount -= (ready) ? updateTickCount : 0;
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
	protected short updateTorchCombustionTime(int value) 
	{
		// Remember to keep the value unsigned;
		return combustionTime += ((combustionTime + value > 0) ? value : combustionTime * -1);
	}
	
	/** 
	 *  Returns the remaining combustion time <i>(expressed in ticks)</i> of this torch entity.
	 */
	protected short getTorchCombustionTime() 
	{
		return combustionTime;
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
	 * Returns the amount of time <i>(expressed in ticks)</i> this entity has been exposed to water. 
	 */
	protected short getTorchHumidityLevel()
	{
		return humidityLevel;
	}

    protected boolean isTorchInHighHumidity()
    {
    	return (humidityLevel >= SharedDefines.HUMIDITY_THRESHOLD);
    }
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
		nbt.setLong("timeCreated", timeCreated);
		nbt.setShort("humidityLevel", humidityLevel);
		nbt.setShort("combustionTime", combustionTime);
		
	    return super.writeToNBT(nbt);
    }
	
	@Override
    public void readFromNBT(NBTTagCompound nbt)
    {  
		super.readFromNBT(nbt);
		
	    timeCreated = nbt.getLong("timeCreated");
        humidityLevel = nbt.getShort("humidityLevel");
        combustionTime = nbt.getShort("combustionTime");
    }
}