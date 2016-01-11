package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.common.FierySouls;
import com.yooksi.fierysouls.block.BlockTorchUnlit;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTorchUnlit extends TileEntityTorch
{ 
	private boolean addSmolderingEffect;        // When this is true we need to start spawning smoke particles.
	private long timeTorchStartedSmoldering;    // Time  in the world when the torch started smoldering.
	private int torchSmolderingDuration;        // How long should the torch be producing smoke?

	public TileEntityTorchUnlit() {}
	public TileEntityTorchUnlit(long totalWorldTime)
	{
		super(totalWorldTime);
		this.addSmolderingEffect = false;
		this.timeTorchStartedSmoldering = 0;
		this.torchSmolderingDuration = 0;
	}
	@Override
	public void update()
	{	
		// Update only at set intervals to reduce performance hits.
		if (this.updateTickCount++ < this.MAIN_UPDATE_INTERVAL)
			return; else this.updateTickCount = 0;
		
		if (!getWorld().isRemote)
		{
			// When it's raining and the torch is directly exposed to rain it will start collecting humidity.
			// Update humidity only on SERVER, we don't really need to do this on client.
			
			if (getWorld().isRaining() && !this.isHighHumidity() && getWorld().canBlockSeeSky(pos))
				this.updateHumidityLevel(MAIN_UPDATE_INTERVAL);
		}
		else if (this.addSmolderingEffect == true)
			this.setTorchSmoldering(true, getWorld().getWorldTime());
	}
	
	/** Set the torch on fire by updating 'blockstate' at world coordinates. This method serves 
	 *  as a proxy for the duplicate method in BlockTorchUnlit, checking humidity and combustion duration 
	 *  as well as handling data inheritance. Always call this function first!
	 */
    public void lightTorch()
    {
    	if (!getWorld().isRemote && !this.isHighHumidity() && this.getCombustionDuration() > 0)
    	{
    		if (!BlockTorchUnlit.lightTorch(getWorld(), pos))
    			return;
    			
    		TileEntity entityTorch = getWorld().getTileEntity(pos);
    		if (entityTorch != null && entityTorch instanceof TileEntityTorchLit)
    		{
    			TileEntityTorchLit torchLit = (TileEntityTorchLit)entityTorch;
    			torchLit.torchAge = getWorld().getTotalWorldTime() - this.timeCreated;
    			torchLit.updateHumidityLevel(this.getHumidityLevel());
    		    torchLit.setCombustionDuration(this.getCombustionDuration());
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
	public void setTorchSmoldering(boolean smolderingState, long worldTime)
	{
		if (smolderingState == true)
		{
			FierySouls.logger.info("Setting torch smoldering!");
			// Make the smoldering duration somewhat random to add more realism
			java.util.Random rand = new java.util.Random();
			
			this.torchSmolderingDuration = rand.nextInt(SMOLDERING_RANDOM) + 50;
			this.timeTorchStartedSmoldering = worldTime;
			this.addSmolderingEffect = false;
		}
		else this.timeTorchStartedSmoldering =  0;
	}
	// Check to see if we should stop the torch from smoldering
	public final boolean didSmolderingExpire(long worldTime)
	{
		return (worldTime - this.timeTorchStartedSmoldering > this.torchSmolderingDuration);
	}
	/** Let the torch know that it should start smoldering on the next tick update */
	public void scheduleSmolderingEffect()
	{
		this.addSmolderingEffect = true;
	}
}