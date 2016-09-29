package com.yooksi.fierysouls.common;

import com.yooksi.fierysouls.block.BlockTorch;
import com.yooksi.fierysouls.entity.item.EntityItemTorch;
import com.yooksi.fierysouls.item.ExtendedItemProperties;
import com.yooksi.fierysouls.item.ItemTorch;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class EventHandler 
{
	/** Used to optimize execution of code in {@link #onWorldTick}. */ 
	private static int serverTickCounter = 0;
	
	/**
	 * Event that is fired whenever a player tosses (Q) an item or drag-n-drops a
	 * stack of items outside the inventory GUI screens. Canceling the event will
	 * stop the items from entering the world, but will not prevent them being
	 * removed from the inventory - and thus removed from the system. <p>
	 * 
	 * <i>The reason we're intercepting this event to handle item specific actions is
	 * because this is the only way to catch both player toss and drag-n-drop events.</i>
	 */
	@SubscribeEvent
	public void itemEvent(net.minecraftforge.event.entity.item.ItemTossEvent event)
	{
		net.minecraft.item.ItemStack droppedStack = event.getEntityItem().getEntityItem();
		boolean isItemCustomTorch = Block.getBlockFromItem(droppedStack.getItem()) instanceof BlockTorch;
		
		if (!event.getEntity().worldObj.isRemote && droppedStack.stackSize > 0 && isItemCustomTorch)
		{
			if (!droppedStack.hasTagCompound())
				ItemTorch.createCustomItemNBT(droppedStack, event.getEntity().worldObj);
			
			// We're going to create a custom EntityItem and do all the work needed here.
			// The entity will then spawn in the world as being tossed in front of the player.
			
            EntityItem entityItemTorch = EntityItemTorch.createTossedEntityItem(droppedStack, event.getPlayer());
            event.getEntity().worldObj.spawnEntityInWorld(entityItemTorch);
            
            // We created and added the EntityItem to the world here, override default event action. 
            event.setCanceled(true);    
		}
	}
	
	/** 
	 * This event will be called around three to four times per tick.<br>
	 * <i>Note: eventy.type will always be WORLD and event.side will always be SERVER.</i>
	 */
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) 
	{
		if (event.side == Side.SERVER && event.phase == Phase.END)
		{	
			if (serverTickCounter > 160)   //  40 calls per second here
			{
				ExtendedItemProperties.callPropertiesGarbageCollector(event.world);
	    	    serverTickCounter = 0;
			}
	        else serverTickCounter++;
		}
	}
}