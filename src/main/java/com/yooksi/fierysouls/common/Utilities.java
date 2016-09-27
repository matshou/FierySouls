package com.yooksi.fierysouls.common;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;

public class Utilities 
{
	/** Modify a number with a percentage. If the value is a positive number the chance <br>
	 *  will be decreased, and if it's negative, the chance will decrease.
	 *  
	 *  @param chance the number to modify.
	 *  @param value percentage to add to subtract from the number. 
	 */ 
	public static int modifyChance(double chance, double value)
	{
		int modifier = (int) Math.round((chance / (double) 100) * (value < 0 ? value * -1 : value));
		return (int) chance + ((value < 0) ? modifier : modifier * -1);
	}
	
    /**
     *  Checks if the player is in the process of hitting a block. <br>
     *  <i>If the game is played in multiplayer, this should be validated only on client.</i>
     *  
     *  @return True if the player is holding mouse left-click and is mousing over a block.
     */
    public static boolean isPlayerBreakingBlock()
    {
    	final Minecraft minecraft = Minecraft.getMinecraft();
    	final RayTraceResult mouseOver = minecraft.objectMouseOver;
    	
    	boolean isAttackKeyDown = minecraft.gameSettings.keyBindAttack.isKeyDown();
    	boolean isMouseOverBlock = mouseOver != null && mouseOver.typeOfHit == RayTraceResult.Type.BLOCK;
    	
    	return isAttackKeyDown && isMouseOverBlock;
    }
}
