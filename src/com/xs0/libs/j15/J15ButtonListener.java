// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15;

/**
 * A listener for changes to the state of soft buttons on a given LCD screen.
 * 
 * @author Mitja Slenc
 */
public interface J15ButtonListener
{
	/**
	 * Called when a user presses or releases a soft button. The new button state
	 * is already provided in newButtonState, so no need to query it.
	 * 
	 * @param source the screen on which the event happened
	 * @param newButtonState the new button state
	 */
	public void onButtonChange(J15Screen source, int newButtonState);
}
