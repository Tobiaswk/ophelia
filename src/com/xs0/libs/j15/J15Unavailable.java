// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15;

/**
 * A subclass of J15Exception used to tell clients that it's (probably) permanently impossible
 * to connect to a screen, the possible causes being:
 * <ul>
 * <li>Failure to load the native .dll from Java
 * <li>Invalid Windows version
 * <li>Missing (part of?) Logitech LCD software
 * <li>Invalid version of Logitech LCD software
 * </ul> 
 *
 * @author Mitja Slenc
 */
public class J15Unavailable extends J15Exception
{
	public J15Unavailable(String message)
	{
		super(message);
	}
}
