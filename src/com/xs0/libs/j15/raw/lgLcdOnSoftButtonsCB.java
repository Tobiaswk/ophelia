// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

/**
 * A callback interface called when the user presses or depresses a soft button and your
 * application is currently displayed.
 * 
 * @author Mitja Slenc
 *
 */
public interface lgLcdOnSoftButtonsCB
{
	public int callback(int device, int dwButtons, Object pContext);
}
