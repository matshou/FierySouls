package com.yooksi.fierysouls.common;

import com.yooksi.fierysouls.block.BlockTorch;
import com.yooksi.fierysouls.entity.item.EntityItemTorch;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler 
{
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
		net.minecraft.item.ItemStack droppedStack = event.entityItem.getEntityItem();	
		if (droppedStack.stackSize > 0 && Block.getBlockFromItem(droppedStack.getItem()) instanceof BlockTorch)
		{
			if (!droppedStack.hasTagCompound())
				com.yooksi.fierysouls.item.ItemTorch.createCustomItemNBT(droppedStack, event.entity.worldObj.getTotalWorldTime());
			
			// We're going to create a custom EntityItem and do all the work needed here.
			// The entity will then spawn in the world as being tossed in front of the player.
			
            EntityItem entityItemTorch = EntityItemTorch.createTossedEntityItem(droppedStack, event.player);
            event.player.joinEntityItemWithWorld(entityItemTorch);
            
            // We created and added the EntityItem to the world here, override default event action. 
            event.setCanceled(true);    
		}
	 }
}