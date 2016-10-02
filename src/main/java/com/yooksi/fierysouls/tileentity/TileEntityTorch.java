package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.Logger;
import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.common.SharedDefines.TorchUpdateType;

import jline.internal.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityTorch extends TileEntity implements ITickable
{
	private int combustionTime;            // amount of ticks before the torch burns out.
	public long timeCreated;               // 'totalWorldTime' this entity was created.
	
	private int[] updateTickCounts;        // used to keep track of passed updates per interval.  
	private int humidityLevel = 0;         // amount of ticks the torch has been exposed to water.
	
	// This constructor is NEEDED during entity loading by FML:
	public TileEntityTorch() { this(0); };
	
	@Override
	public void update() 
	{
		// This is here just to meet the implementation criteria.
	}
	
	public TileEntityTorch(long totalWorldTime) 
	{
		updateTickCounts = new int[TorchUpdateType.values().length];

		combustionTime = SharedDefines.MAX_TORCH_FLAME_DURATION;
		timeCreated = totalWorldTime;
	}
	
	/**
	 *  This method is intended to be used as a performance optimizer.<br>
	 *  Update only at set intervals to reduce performance hits.
	 */
	protected boolean isTorchReadyForUpdate(TorchUpdateType type)
	{	
		if (updateTickCounts[type.index]++ == type.getInterval())
		{
			updateTickCounts[type.index] = 0;
			return true;
		}
		else return false;
	}
	
    /**
	 *  Update the time duration of torch coal combustion with a new value. <br>
	 *  It should update the value and return it for convenience sake.
	 *  
	 *  @param value to increment the duration with <i>(accepts negatives)</i>
	 *  @return Updated combustion duration value for practical purposes. <br>
	 *          <i>Note that the value is guaranteed to be kept unsigned for you</i>.
	 */
	protected int updateTorchCombustionTime(double value) 
	{
		// Remember to keep the value unsigned;
		return combustionTime += ((combustionTime + value > 0) ? value : combustionTime * -1);
	}
	
	/** 
	 *  Returns the remaining combustion time <i>(expressed in ticks)</i> of this torch entity.
	 */
	protected int getTorchCombustionTime() 
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
	 */
	protected int updateTorchHumidityLevel(int value)
	{
		return humidityLevel += value;
	}
	
	/** 
	 * Returns the amount of time <i>(expressed in ticks)</i> this entity has been exposed to water. 
	 */
	protected int getTorchHumidityLevel()
	{
		return humidityLevel;
	}

    protected boolean isTorchInHighHumidity()
    {
    	return (humidityLevel >= SharedDefines.TORCH_HUMIDITY_THRESHOLD);
    }
	
    /** 
     * Find a torch tile entity instance in World.
     *   
     * @param world World instance where to search for the entity.
     * @param pos BlockPos where the entity is suppose to be in World.
     * @param throwException should the method throw an exception if we didn't find the entity.
     * @throws NullPointerException if <code>throwException</code> is true and no TorchTileEntity was found.
     */
    public static TileEntity findTorchTileEntity(@Nullable World world, BlockPos pos, boolean throwException)
    {
    	TileEntity torchTE = world != null ? world.getTileEntity(pos) : null;
    	TileEntity result = torchTE != null && torchTE instanceof TileEntityTorch ? torchTE : null; 
    	
    	if (throwException && result == null)
    	{
    		Logger.fatal("TileEntityTorch was not found in World, it returned a null value.");
    		throw new NullPointerException();
    	}
    	return result;
    }
    
    // Creates and returns an updated NBTTagCompound for this TileEntity. 
    // This method is by default called by 'getUpdatePacket()'.
    @Override
   	public NBTTagCompound getUpdateTag()    /** SERVER-side */
   	{
    	return this.writeToNBT(super.getUpdateTag());
   	}
    
    // This method is called whenever a new TileEntity is created (not loaded in world).
    // It will get an updated compound from server and send it to client.
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()   /** SERVER-side */
    {
    	return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
    }
    
    // Called on CLIENT after a getUpdatePacket() call.
    // Here we can receive a packet crafted by server on a sync call.
    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt)
    {
		readFromNBT(pkt.getNbtCompound());
	}
    
    /** 
     *  Saves all important information to a custom NBT packet. <br>
     *  @return NBT packet containing all up-to-date torch data. 
     */
    public NBTTagCompound saveDataToPacket()
    {	
    	return this.writeToNBT(new NBTTagCompound());
    }
    
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {	
		if (this instanceof TileEntityTorchUnlit)
		{
			TileEntityTorchUnlit unlitTorch = (TileEntityTorchUnlit)this;
			nbt.setInteger("smolderingDuration", unlitTorch.smolderingDuration);
			nbt.setLong("startedSmoldering", unlitTorch.startedSmoldering);
		}
		
		nbt.setLong("timeCreated", timeCreated);
		nbt.setInteger("humidityLevel", humidityLevel);
		nbt.setInteger("combustionTime", combustionTime);
		
	    return super.writeToNBT(nbt);
    }
	
	@Override
    public void readFromNBT(NBTTagCompound nbt)
    {  
		super.readFromNBT(nbt);
		boolean result = nbt.hasKey("smolderingDuration") && nbt.hasKey("startedSmoldering");
		
		if (this instanceof TileEntityTorchUnlit && result == true)
		{
			TileEntityTorchUnlit unlitTorch = (TileEntityTorchUnlit)this;
			unlitTorch.smolderingDuration = nbt.getInteger("smolderingDuration");
			unlitTorch.startedSmoldering = nbt.getLong("startedSmoldering");
		}
		
	    timeCreated = nbt.getLong("timeCreated");
        humidityLevel = nbt.getInteger("humidityLevel");
        combustionTime = nbt.getInteger("combustionTime");
    }
}