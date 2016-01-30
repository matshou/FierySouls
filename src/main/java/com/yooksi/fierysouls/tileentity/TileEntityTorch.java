package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.common.FierySouls;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TileEntityTorch extends TileEntity implements IUpdatePlayerListBox
{
	private short combustionDuration;
	private short humidityLevel = 0;
	
	protected byte updateTickCount = 0;
	protected long torchAge = 0;            // Currently unused
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
	
	/** 
	 *  Update the time duration of torch coal combustion with a new value.
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
	
	/** 
	 *  Update torch humidity level with a new value.
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
    	return (humidityLevel >= SharedDefines.HUMIDITY_THRESHOLD);
    }
    
    /** 
     *  This update is for the moment only being done just before the entity is destroyed.
     *  
     *  @param worldTime current <b>total</b> time in the world. 
     *  @see {@link #saveDataToPacket()}
     */ 
    protected void updateTorchAge(long worldTime)
    {
    	if (worldTime > 0 && timeCreated > worldTime)   // Make sure age value doesn't get corrupt
    		torchAge = worldTime - timeCreated;
    }
    
    // ====================================== NETWORK UTILITIES ==============================================
    
    /** 
     *  Saves all important information to a custom NBT packet. <br>
     *  Used to pass internal data to external sources.
     *  @return NBT packet containing all up-to-date torch data. 
     */
    public NBTTagCompound saveDataToPacket()
    {
    	updateTorchAge(getWorld().getTotalWorldTime());
    	NBTTagCompound dataPacket = new NBTTagCompound();

    	this.writeToNBT(dataPacket);
        return dataPacket;
    }
    
	/**
	 *  This will make the server call <b><i>'getDescriptionPacket'</b></i> for a full data sync, <br>
	 *  the client will receive the packet in the method <b><i>'onDataPacket'</b></i>.
	 *  
	 *  @see #onDataPacket
	 */
	protected void markForUpdate()
	{
		getWorld().markBlockForUpdate(pos);
		this.markDirty();
	}
	
	/** 
	 * Gathers data into a packet that is to be sent to the client. <br>
	 * Place custom packet data you want to send to client here. <p>
	 * 
	 * <i>Called on server only.</i>
	 */
	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);

		return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
	}
	
	/** 
	 * Extracts data from a packet that was sent from the server.
	 * Minecraft automatically sends a 'description packet' for the tile entity when it is first 
	 * loaded on the client, and you can force it to resend one afterwards. <p>
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(net.minecraft.network.NetworkManager net, S35PacketUpdateTileEntity packet) 
	{
		this.readFromNBT(packet.getNbtCompound());
	}
    
	@Override
    public void writeToNBT(NBTTagCompound par1)
    {
		super.writeToNBT(par1);
		par1.setLong("torchAge", torchAge);
		par1.setLong("timeCreated", timeCreated);
	    par1.setShort("humidityLevel", humidityLevel);
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
    }
}