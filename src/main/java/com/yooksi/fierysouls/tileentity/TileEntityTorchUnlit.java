package com.yooksi.fierysouls.tileentity;

import com.yooksi.fierysouls.block.BlockTorch;
import com.yooksi.fierysouls.block.BlockTorchUnlit;
import com.yooksi.fierysouls.common.ResourceLibrary;
import com.yooksi.fierysouls.common.SharedDefines;

import jline.internal.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class TileEntityTorchUnlit extends TileEntityTorch
{
	private static final byte SMOLDERING_RANDOM = 125;  // Random factor in determining how long is the torch going to smolder.
	
	protected int smolderingDuration = 0;      // How long should the torch be producing smoke?
	protected long startedSmoldering = 0;      // Time in the world when the torch started smoldering.
	
	// This constructor is NEEDED during entity loading by FML:
	public TileEntityTorchUnlit() {}
	
	public TileEntityTorchUnlit(long totalWorldTime) 
	{
		super(totalWorldTime);
	}
	
	@Override
	public void update() 
	{
		if (!isTorchReadyForUpdate(SharedDefines.TorchUpdateTypes.MAIN_UPDATE))
			return;
		
		if (!getWorld().isRemote)
		{
			// When it's raining and the torch is directly exposed to rain it will start collecting humidity.

			if (getWorld().isRaining() && !isTorchInHighHumidity() && getWorld().canBlockSeeSky(pos))
				updateTorchHumidityLevel(SharedDefines.TorchUpdateTypes.MAIN_UPDATE.interval);
		}
		
		if (isTorchSmoldering() && didSmolderingExpire())				
			setTorchSmoldering(false, getWorld().getTotalWorldTime());
	}
	
	/** 
	 *  Activate or deactivate smoke particles spawning above the torch. <br>
     *  When torch smoldering has been activated, the particles will be created in it's block class.
     *  
     *  @param smolderingState true to activate, false to deactivate smoldering effect.
     *  @param totalWorldTime time mark when torch started smoldering.
     *  
     *  @see {@link BlockTorchUnlit#randomDisplayTick}
     */
	public void setTorchSmoldering(boolean smolderingState, long totalWorldTime)
	{
		if (smolderingState == true)
		{
			// Make the smoldering duration somewhat random to add more realism
			java.util.Random rand = new java.util.Random();
			
			smolderingDuration = rand.nextInt(SMOLDERING_RANDOM) + 50;
			startedSmoldering = totalWorldTime;
		}
		else startedSmoldering =  0;
	}
	
	private final boolean didSmolderingExpire()
	{
		return (getWorld().getTotalWorldTime() - startedSmoldering > smolderingDuration);
	}
	
	/** 
	 * Tells us if our torch is emitting smoke particles after recently being extinguished.
	 */
	public boolean isTorchSmoldering()
	{
		return (startedSmoldering != 0);

	}
	
	/** 
	 *  Set the torch on fire and handle tile entity data.<p>
	 *  <i> Here we will check for humidity and combustion duration as well as handle data inheritance.</i>
	 *  
	 *  @return true if we successfully set the torch on fire.
	 */
    public final boolean lightTorch()
    {
    	boolean result = !isTorchInHighHumidity() && getTorchCombustionTime() > 0;
    	if (!getWorld().isRemote && result == true && lightTorch(getWorld(), pos))
    	{	
    		TileEntityTorchLit torchLit = TileEntityTorchLit.findLitTorchTileEntity(getWorld(), pos);
            torchLit.readFromNBT(saveDataToPacket());
            return true;
    	}
    	else return false;
    }
    
    /** Set the torch on fire by updating 'blockstate' at world coordinates. <p>
     *  <i>This method only handles adding the block and tileEntity in the world.</i> 
	 * 
	 * @param world the instance of the world the torch is located in.
	 * @param pos coordinates of the torch in the world.
	 * @return true if we successfully lit the torch on fire.
	 */
    private static boolean lightTorch(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos)
    {
    	// Find out the direction the torch is facing
        EnumFacing facing = (EnumFacing)world.getBlockState(pos).getValue(BlockTorch.FACING);
    			
    	// If the torch is not facing up but is placed on the side of a block we have to take into
    	// account facing sides, otherwise the torch will detach from the wall and turn into an item.
    			
    	if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) 
    		return world.setBlockState(pos, ResourceLibrary.TORCH_LIT.getBlockState().getBaseState().withProperty(BlockTorch.FACING, facing)); 
    		
    	else return world.setBlockState(pos, ResourceLibrary.TORCH_LIT.getDefaultState());
    }
    
    /** Helper method for finding a torch tile entity instance from World. */
    public static TileEntityTorchUnlit findUnlitTorchTileEntity(@Nullable World world, net.minecraft.util.math.BlockPos pos)
    {
    	return (TileEntityTorchUnlit)TileEntityTorch.findTorchTileEntity(world, pos);
    }
}