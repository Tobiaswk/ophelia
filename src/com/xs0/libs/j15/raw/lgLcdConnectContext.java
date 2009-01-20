// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

/**
 * Specified parameters to be used when connecting to Logitech LCD system. See SDK documentation
 * for further details (but very quickly:<ul>
 * <li>Always fill in appFriendlyName
 * <li>You don't usually need configureContext, as you can store state in the callback itself
 * <li>connection is <i>returned</i> so no sense in setting it to something
 * </ul>
 * 
 * @author Mitja Slenc
 *
 */
public class lgLcdConnectContext
{
	public String appFriendlyName;
	public boolean isPersistent;
	public boolean isAutostartable;
	public lgLcdOnConfigureCB configureCallback;
	public Object configureContext;
	public int connection;
}
