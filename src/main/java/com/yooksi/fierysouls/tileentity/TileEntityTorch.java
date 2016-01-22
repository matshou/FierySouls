package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.common.FierySouls;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;

public class TileEntityTorch extends TileEntity implements IUpdatePlayerListBox
{
	private short combustionDuration;
	private short humidityLevel = 0;
	
	protected byte updateTickCount = 0;
	protected long torchAge = 0;
	protected long timeCreated;
	
	public TileEntityTorch() { this(0); };
	protected TileEntityTorch(long totalWorldTime) 
	{
		timeCreated = totalWorldTime;
		combustionDuration = SharedDefines.MAX_TORCH_FLAME_DURATION;
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
    	return (humidityLevel > SharedDefines.HUMIDITY_THRESHOLD);
    }
    
    /** This update is for the moment only being done just before the entity is destroyed.
     *  @param worldTime current total time in the world. 
     *  @see {@link #saveDataToPacket()}
     */ 
    protected void updateTorchAge(long worldTime)
    {
    	torchAge = worldTime - timeCreated;
    }
    
    /** Saves all important information to a custom NBT packet. <br>
     *  Used to pass internal data to external sources.
     *  @return NBT packet containing all up-to-date torch data. 
     */
    public NBTTagCompound saveDataToPacket()
    {
    	updateTorchAge(getWorld().getTotalWorldTime());
    	NBTTagCompound dataPacket = getTileData();

    	this.writeToNBT(dataPacket);
        return dataPacket;
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
        {
        	String position = " (x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ() + ")";
        	FierySouls.logger.info("Reading from NBT for TileEntityTorchLit, combustion: " + this.combustionDuration + position);
        }
    }
}