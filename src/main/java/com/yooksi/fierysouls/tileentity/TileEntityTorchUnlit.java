package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.common.SharedDefines;
import com.yooksi.fierysouls.block.BlockTorchUnlit;

import net.minecraft.network.Packet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import net.minecraft.tileentity.TileEntity;

public final class TileEntityTorchUnlit extends TileEntityTorch
{ 
	private static final float RAIN_STR_THRESHOLD = 0.85F;   // How strong must the rain be falling to extinguish the torch?
	private static final byte SMOLDERING_RANDOM = 125;      // Random factor in determining how long is the torch going to smolder.
	
	private int torchSmolderingDuration = 0;        // How long should the torch be producing smoke?
	private long timeTorchStartedSmoldering = 0;    // Time  in the world when the torch started smoldering.

	public TileEntityTorchUnlit() {}
	public TileEntityTorchUnlit(long totalWorldTime)
	{
		super(totalWorldTime);
	}
	
	@Override
	public void update()
	{	
		// Update only at set intervals to reduce performance hits.
		if (updateTickCount++ < SharedDefines.MAIN_UPDATE_INTERVAL)
			return; else updateTickCount = 0;
		
		if (!getWorld().isRemote)
		{
			// When it's raining and the torch is directly exposed to rain it will start collecting humidity.
			// Update humidity only on SERVER, we don't really need to do this on client.
			
			if (getWorld().isRaining() && !isHighHumidity() && getWorld().canBlockSeeSky(pos))
				updateHumidityLevel(SharedDefines.MAIN_UPDATE_INTERVAL);
		}
		else if (isTorchSmoldering() && didSmolderingExpire())				
			setTorchSmoldering(false, getWorld().getTotalWorldTime());
	}
	
	/** 
	 *  Set the torch on fire by updating 'blockstate' at world coordinates. <p>
	 *  
	 *  <i>This method serves  as a proxy for the duplicate method in BlockTorchUnlit, <br>
	 *  checking humidity and combustion duration as well as handling data inheritance. <p>
	 *  <b>Always call this method first!</b></i>
	 */
    public void lightTorch()
    {
    	if (!getWorld().isRemote && !isHighHumidity() && getCombustionDuration() > 0 && BlockTorchUnlit.lightTorch(getWorld(), pos))
    	{
    		TileEntity entityTorch = getWorld().getTileEntity(pos);
    		if (entityTorch != null && entityTorch instanceof TileEntityTorchLit)
    		{
    			TileEntityTorchLit torchLit = (TileEntityTorchLit)entityTorch;
                torchLit.readFromNBT(saveDataToPacket());
    		}
    	}
    }
    
	/** 
	 *  Activate or deactivate smoke particles spawning above the torch. <br>
     *  When torch smoldering has been activated, the particles will be created in it's block class.
     *  
     *  @param smolderingState True to activate, false to deactivate smoldering effect
     *  @param totalWorldTime Total time in this world, passed instead of World for security reasons
     *  
     *  @see {@link BlockTorchUnlit#randomDisplayTick}
     */
	public void setTorchSmoldering(boolean smolderingState, long totalWorldTime)
	{
		if (smolderingState == true)
		{
			// Make the smoldering duration somewhat random to add more realism
			java.util.Random rand = new java.util.Random();
			
			torchSmolderingDuration = rand.nextInt(SMOLDERING_RANDOM) + 50;
			timeTorchStartedSmoldering = totalWorldTime; 
		}
		else timeTorchStartedSmoldering =  0;
	}
	
	@SideOnly(Side.CLIENT)
	private final boolean didSmolderingExpire()
	{
		return (getWorld().getTotalWorldTime() - timeTorchStartedSmoldering > torchSmolderingDuration);
	}
	
	/** 
	 * Tells us if our torch is emitting smoke particles after recently being extinguished.
	 */
	@SideOnly(Side.CLIENT)
    public boolean isTorchSmoldering()
	{
		return (timeTorchStartedSmoldering != 0);
	}
	
	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		
		nbtTag.setInteger("torchSmolderingDuration", torchSmolderingDuration);
		nbtTag.setLong("timeTorchStartedSmoldering", timeTorchStartedSmoldering);
		
		return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound par1)
    {
		super.readFromNBT(par1);
		
		torchSmolderingDuration = par1.getInteger("torchSmolderingDuration");
		timeTorchStartedSmoldering = par1.getLong("timeTorchStartedSmoldering");
    }
}