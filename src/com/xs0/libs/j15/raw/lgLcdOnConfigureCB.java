// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

/**
 * A configure callback called when the user tries to configure your application
 * through LCD manager software.
 * 
 * @author Mitja Slenc
 */
public interface lgLcdOnConfigureCB
{
	public int callback(int connection, Object context);
}
