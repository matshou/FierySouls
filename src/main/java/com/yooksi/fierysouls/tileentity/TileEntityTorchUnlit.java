package com.yooksi.fierysouls.tileentity;

import java.util.Random;

import com.yooksi.fierysouls.common.FierySouls;

import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;

public class TileEntityTorchUnlit extends TileEntity implements IUpdatePlayerListBox
{ 
	// TODO: Move this to a configuration file sooner or later:
	private static final int SMOLDERING_RANDOM = 125;
	
	private static int currentHumidityLevel;
	
	// This updates every tileEntity tick on both client and server side
	public void update()
	{
		if (getWorld().isRaining() && getWorld().canBlockSeeSky(pos))
		{
			this.currentHumidityLevel += 1;
			
		}
		if (this.shouldAddSmolderingEffect && getWorld().isRemote)
			this.setTorchSmoldering(true, getWorld().getWorldTime());
	}
	public boolean shouldAddSmolderingEffect = false;   // When this is true we need to start spawning smoke particles
	private long timeTorchStartedSmoldering = 0;       // Time time in the world when the torch started smoldering 
	private long torchSmolderingSDuration = 0;        // How long should the torch be producing smoke?
	
	// Tells us if our torch is emitting smoke particles due to recently being extinguished
	public final boolean isTorchSmoldering()
	{
		return (timeTorchStartedSmoldering != 0);
	}
	public void setTorchSmoldering(boolean smolderingState, long worldTime)
	{
		if (smolderingState == true)
		{
			// Make the smoldering duration somewhat random to add more realism
			Random rand = new Random();
			
			this.torchSmolderingSDuration = rand.nextInt(SMOLDERING_RANDOM) + 50;
			this.timeTorchStartedSmoldering = worldTime;
			this.shouldAddSmolderingEffect = false;
		}
		else this.timeTorchStartedSmoldering =  0;
	}
	
	// Check to see if we should stop the torch from smoldering
	public final boolean didSmolderingExpire(long worldTime)
	{
		return (worldTime - this.timeTorchStartedSmoldering > this.torchSmolderingSDuration);
	}
}
