package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.block.BlockTorchLit;
import com.yooksi.fierysouls.common.FierySouls;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;

import net.minecraft.network.Packet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TileEntityTorch extends TileEntityTorchProperties implements IUpdatePlayerListBox
{
	public TileEntityTorch() { this(0); };
	protected TileEntityTorch(long totalWorldTime) 
	{
		super(totalWorldTime);
	}
	
	@Override
	// This updates every tileEntity tick on both client and server side
	public void update() {};
	
    /** 
     *  Saves all important information to a custom NBT packet. <br>
     *  Used to pass internal data to external sources.
     *  @return NBT packet containing all up-to-date torch data. 
     */
    public NBTTagCompound saveDataToPacket()
    {
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
		super.writeToNBT(this.saveToNBT(par1));
    }
	
    @Override
    public void readFromNBT(NBTTagCompound par1)
    {  
	    super.readFromNBT(par1);
	    this.loadFromNBT(par1);
    }
}