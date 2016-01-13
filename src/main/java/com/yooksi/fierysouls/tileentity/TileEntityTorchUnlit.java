package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.block.BlockTorchUnlit;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTorchUnlit extends TileEntityTorch
{ 
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
		if (updateTickCount++ < MAIN_UPDATE_INTERVAL)
			return; else updateTickCount = 0;
		
		if (!getWorld().isRemote)
		{
			// When it's raining and the torch is directly exposed to rain it will start collecting humidity.
			// Update humidity only on SERVER, we don't really need to do this on client.
			
			if (getWorld().isRaining() && !isHighHumidity() && getWorld().canBlockSeeSky(pos))
				updateHumidityLevel(MAIN_UPDATE_INTERVAL);
		}
		else if (didSmolderingExpire())	
			setTorchSmoldering(false);
	}
	
	/** Set the torch on fire by updating 'blockstate' at world coordinates. This method serves 
	 *  as a proxy for the duplicate method in BlockTorchUnlit, checking humidity and combustion duration 
	 *  as well as handling data inheritance. Always call this function first!
	 */
    public void lightTorch()
    {
    	if (!getWorld().isRemote && !isHighHumidity() && getCombustionDuration() > 0 && BlockTorchUnlit.lightTorch(getWorld(), pos))
    	{
    		TileEntity entityTorch = getWorld().getTileEntity(pos);
    		if (entityTorch != null && entityTorch instanceof TileEntityTorchLit)
    		{
    			TileEntityTorchLit torchLit = (TileEntityTorchLit)entityTorch;
    			torchLit.postInit(getCombustionDuration(), getHumidityLevel(), timeCreated, getWorld().getTotalWorldTime());
    		}
    	}
    }
	// Tells us if our torch is emitting smoke particles due to recently being extinguished
    public final boolean isTorchSmoldering()
	{
		return (timeTorchStartedSmoldering != 0);
	}
	/** Activate or deactivate smoke particles spawning above the torch.
     * When torch smoldering has been activated, the particles will be created in it's block class.
     */
	@SideOnly(Side.CLIENT)
	public void setTorchSmoldering(boolean smolderingState)
	{
		if (smolderingState == true)
		{
			// Make the smoldering duration somewhat random to add more realism
			java.util.Random rand = new java.util.Random();
			
			torchSmolderingDuration = rand.nextInt(SMOLDERING_RANDOM) + 50;
			timeTorchStartedSmoldering = getWorld().getTotalWorldTime(); 
		}
		else timeTorchStartedSmoldering =  0;
	}
	@SideOnly(Side.CLIENT)
	private final boolean didSmolderingExpire()
	{
		return (getWorld().getTotalWorldTime() - timeTorchStartedSmoldering > torchSmolderingDuration);
	}
}