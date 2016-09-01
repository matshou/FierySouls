package com.yooksi.fierysouls.common;

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
}
