// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15;

/**
 * A listener for configuration requests.
 * 
 * @author Mitja Slenc
 */
public interface J15ConfListener
{
	/**
	 * Called when the user wants to configure this application/connection from the Logitech LCD
	 * manager application.
	 * 
	 * @param source The connection on which the event occurred.
	 */
	public void doConfigure(J15Connection source);
}
