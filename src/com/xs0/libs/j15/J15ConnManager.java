// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15;

import static com.xs0.libs.j15.raw.J15Native.ERROR_SUCCESS;
import static com.xs0.libs.j15.raw.J15Native.lgLcdConnect;
import static com.xs0.libs.j15.raw.J15Native.lgLcdDeInit;
import static com.xs0.libs.j15.raw.J15Native.lgLcdInit;

import java.util.ArrayList;

import com.xs0.libs.j15.raw.lgLcdConnectContext;

/**
 * The main purpose of connection manager is to create new connections and set their state
 * appropriately.
 * 
 * @author Mitja Slenc
 */
public class J15ConnManager
{
	boolean libAvailable;
	boolean destroyed = false;
	ArrayList<J15Connection> conns = new ArrayList<J15Connection>();
	
	J15ConnManager()
	{
		libAvailable = lgLcdInit() == ERROR_SUCCESS;
	}
	
	synchronized void destroy()
	{
		if (destroyed)
			return;
						
		for (J15Connection conn : conns) {
			conn.destroyInternal();
		}

		if (libAvailable) {
			lgLcdDeInit();
		}
		
		destroyed=true;
	}
	
	/**
	 * Creates a new connection to Logitech LCD system. If you want to have more than one screen
	 * per keyboard, you'll also need more than one connection.
	 * 
	 * @param appFriendlyName the name of the application
	 * @param isPersistent is persistent?
	 * @param isAutostartable is autostartable?
	 * @return a new connection 
	 * @throws J15Exception if an error occurs
	 */
	public synchronized J15Connection connect(String appFriendlyName, boolean isPersistent, boolean isAutostartable) throws J15Exception
	{
		checkDestroyed();
		
		if (!libAvailable)
			throw new J15Unavailable("Native library could not be loaded");
		
		if (appFriendlyName==null || appFriendlyName.length()==0) {
			throw new IllegalArgumentException("Missing appFriendlyName");
		}
		
		J15Connection conn = new J15Connection(this);
		
		lgLcdConnectContext ctx = new lgLcdConnectContext();
		ctx.appFriendlyName = appFriendlyName;
		ctx.isPersistent = isPersistent;
		ctx.isAutostartable = isAutostartable;
		ctx.configureCallback = conn;
		
		J15.handleError(lgLcdConnect(ctx));
		conn.setConnection(ctx.connection);
		conns.add(conn);
		return conn;
	}

	synchronized void forgetMe(J15Connection connection)
    {
		conns.remove(connection);
    }
	
	private void checkDestroyed()
	{
		if (destroyed)
			throw new IllegalStateException();
	}
}
