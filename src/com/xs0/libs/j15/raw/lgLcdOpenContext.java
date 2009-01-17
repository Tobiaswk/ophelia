// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

/**
 * The set of information needed to establish a connection.
 * 
 * @author Mitja Slenc
 *
 */
public class lgLcdOpenContext
{
	public int connection;
	public int index;
	public lgLcdOnSoftButtonsCB softbuttonsChangedCallback;
	public Object softbuttonsChangedContext;
	public int device;
}
