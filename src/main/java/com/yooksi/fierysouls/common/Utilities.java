package com.yooksi.fierysouls.common;

public final class Utilities 
{
	/**
	 *  Returns the number of digits found in the argument by first converting it to a string <br>
	 *  and then returning the number of "Unicode code units" in the string. <p>
	 *  
	 *  <i>Note that the decimal mark will be converted to a string as well, account for that.</i> 
	 */
	public static int getNumberOfDigits(double number)
	{
		return (int)String.valueOf(number).length() - 1;
	}

	/**
	 *  Truncate a certain number of decimals from an argument.
	 *  
	 *  @param number Decimal number to truncate
	 *  @param decimals Number of decimals to truncate from <i>number</i>
	 *  @return Truncated decimal number
	 */
	public static double truncateDecimals(double number, int decimals)
	{
		double factor = Math.pow(10, decimals);
		return Math.round((number) * factor) / factor;
	}
	
    /**  
     *  Converts an integer to a series of zeros in a string. <br>
     *  The number of zero digits directly reflects the length of that integer. <p>
     *  
     *  <b>Example:</b> <i>the number 3247 would be converted to "0000" (4 digits)</i>.
     */
	public static String convertToZeroDigits(int integer)
	{
		return Integer.toString((int)Math.pow(10, getNumberOfDigits(integer))).substring(1);
	}
	
	  /**
	   * Search a String to find the first index of any
	   * character not in the given set of characters.<p>
	   *
	   * A <code>null</code> String will return <code>-1</code>.<br>
	   * A <code>null</code> search string will return <code>-1</code>.
	   *
	   * @param str  the String to check, may be null
	   * @param searchChars  the chars to search for, may be null
	   * @return the index of any of the chars, -1 if no match or null input
	   */
	  public static int indexOfAnyBut(String str, String searchChars) 
	  {
	      if (isEmpty(str.toCharArray()) || isEmpty(searchChars.toCharArray()))
	          return -1;
	      
	      for (int i = 0; i < str.length(); i++) 
	    	  if (searchChars.indexOf(str.charAt(i)) < 0)
	              return i;
	      
	      return -1;
	  }
	  
	  /**
	   * Checks if an array of Objects is empty or <code>null</code>.
	   *
	   * @param array  the array to test
	   * @return <code>true</code> if the array is empty or <code>null</code>
	   */
	  private static boolean isEmpty(char[] array) 
	  {
	      return (array == null || array.length == 0);
	  }
	  
	  /**
	   * Emulate a random roll of a dice, and check if the roll beats the challenge.
	   * 
	   * @param challenge Value to roll against
	   * @param sides Number of dice sides <i>(if 0, roll will be 0)</i>
	   * @param rand Instance of java Random
	   * @return True if the natural roll was successful against the challenge
	   */
	  public static boolean rollDiceAgainst(int challenge, int sides, java.util.Random rand)
	  {
		  final int naturalRoll = (sides > 0) ? rand.nextInt(sides) + 1 : 0;
		  return (naturalRoll > 0 && naturalRoll <= challenge);
	  }
}