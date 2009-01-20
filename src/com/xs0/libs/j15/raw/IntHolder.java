// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

/**
 * This class serves a single purpose - to hold a reference to an integer.
 * 
 * @author Mitja Slenc
 */
public class IntHolder
{
	/** The current value */
	public int value;

	/**
	 * Creates a new IntHolder with the specified initial value.
	 * 
	 * @param startValue the initial value
	 */
	public IntHolder(int startValue)
	{
		value = startValue;
	}
}
