package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.FierySouls;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;

public class TileEntityTorch extends TileEntity implements IUpdatePlayerListBox
{
	protected static final byte HUMIDITY_AMOUNT_PER_TICK = 1; 
	protected static final float RAIN_STR_THRESHOLD = 0.85F;    // How strong must the rain be falling to extinguish the torch?
	protected static final short HUMIDITY_THRESHOLD = 300;      // After wet must the torch be before it cannot burn anymore?
	protected static final byte SMOLDERING_RANDOM = 125;        // Random factor in determining how long is the torch going to smolder.

	private short humidityLevel;
	protected long timeCreated;    // Time in the world this tile entity was created
	protected long torchAge;
	
	public TileEntityTorch() { this(0); };
	protected TileEntityTorch(long totalWorldTime) 
	{
		this.humidityLevel = 0;
		this.timeCreated = totalWorldTime;
		this.torchAge = 0;
	}
	@Override
	public void update() {};  // This updates every tileEntity tick on both client and server side
	
	/** Update torch humidity level with a new value.
	 *  @param humidityValue Value to increment the humidity level with (accepts negatives).
	 *  @return new and updated humidity level data. 
	 */
	protected short updateHumidityLevel(short value)
	{
		this.humidityLevel += value;
	    return this.humidityLevel;
	}
	protected short getHumidityLevel()
	{
		return this.humidityLevel;
	}
	/** Check if this torch has been exposed to rain for a long period of time. */
    protected boolean isHighHumidity()
    {
    	return (this.humidityLevel > this.HUMIDITY_THRESHOLD);
    }
    
	@Override
    public void writeToNBT(NBTTagCompound par1)
    {
		super.writeToNBT(par1);
	    par1.setLong("timeCreated", this.timeCreated);
	    par1.setShort("humidityLevel", this.getHumidityLevel());
        par1.setLong("torchAge", this.torchAge);
    }
    @Override
    public void readFromNBT(NBTTagCompound par1)
    {  
	    super.readFromNBT(par1);
        this.timeCreated = par1.getLong("timeCreated");
        this.updateHumidityLevel(par1.getShort("humidityLevel"));
        this.torchAge = par1.getLong("torchAge");
    }
}