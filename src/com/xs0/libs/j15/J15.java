// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15;

import static com.xs0.libs.j15.raw.J15Native.ERROR_ACCESS_DENIED;
import static com.xs0.libs.j15.raw.J15Native.ERROR_ALREADY_EXISTS;
import static com.xs0.libs.j15.raw.J15Native.ERROR_ALREADY_INITIALIZED;
import static com.xs0.libs.j15.raw.J15Native.ERROR_DEVICE_NOT_CONNECTED;
import static com.xs0.libs.j15.raw.J15Native.ERROR_FILE_NOT_FOUND;
import static com.xs0.libs.j15.raw.J15Native.ERROR_INVALID_PARAMETER;
import static com.xs0.libs.j15.raw.J15Native.ERROR_LOCK_FAILED;
import static com.xs0.libs.j15.raw.J15Native.ERROR_NO_MORE_ITEMS;
import static com.xs0.libs.j15.raw.J15Native.ERROR_NO_SYSTEM_RESOURCES;
import static com.xs0.libs.j15.raw.J15Native.ERROR_OLD_WIN_VERSION;
import static com.xs0.libs.j15.raw.J15Native.ERROR_SERVICE_NOT_ACTIVE;
import static com.xs0.libs.j15.raw.J15Native.ERROR_SUCCESS;
import static com.xs0.libs.j15.raw.J15Native.RPC_S_SERVER_UNAVAILABLE;
import static com.xs0.libs.j15.raw.J15Native.RPC_X_WRONG_PIPE_VERSION;

/**
 * This is the class to start with. Call get() to obtain a connection manager and call
 * cleanUp() when you're done. The manager allows you to create connections, which in
 * turn allow you to attach to actual screens.
 * 
 * @author Mitja Slenc
 */
public class J15
{
	static J15ConnManager connManager;
	
	/**
	 * Returns a connection manager singleton instance. Note that if you call cleanUp a new connection
	 * manager will be created next time you call get(), which may be useful if you only want to show something
	 * some of the time.
	 * 
	 * @return a connection manager
	 */
	public static synchronized J15ConnManager get()
	{
		if (connManager==null || connManager.destroyed) {
			return connManager = new J15ConnManager();
		} else {
			return connManager;
		}
	}
	
	/**
	 * Cleans up all of J15 stuff - the connection manager, connections and screens. The result is the same
	 * as if all those were destroy()ed one by one (most notably, they become invalid and you can no longer
	 * use them).
	 */
	public static synchronized void cleanUp()
	{
		if (connManager!=null) {
			connManager.destroy();
			connManager = null;
		}
	}
	
	/**
	 * A utility method for checking the returned error codes and converting them to exceptions.
	 * 
	 * @param errcode the integer error code returned by a native function
	 * @throws J15Exception if errcode is not 0 (ERROR_SUCCESS), an exception with a suitable message
	 */
	
	static void handleError(int errcode) throws J15Exception
	{
		switch(errcode) {
		case ERROR_ACCESS_DENIED: throw new J15Exception("Access denied");
		case ERROR_ALREADY_EXISTS: throw new J15Exception("Already connected");
		case ERROR_ALREADY_INITIALIZED: throw new J15Exception("Already initialized");
		case ERROR_DEVICE_NOT_CONNECTED: throw new J15Exception("Device not connected");
		case ERROR_FILE_NOT_FOUND: throw new J15Exception("LCDMon not running");
		case ERROR_INVALID_PARAMETER: throw new J15Exception("Invalid parameter");
		case ERROR_LOCK_FAILED: throw new J15Exception("Lock failed");
		case ERROR_NO_MORE_ITEMS: throw new J15Exception("No more items");
		case ERROR_NO_SYSTEM_RESOURCES: throw new J15Exception("No system resources");
		case ERROR_OLD_WIN_VERSION: throw new J15Unavailable("Invalid Windows version");
		case ERROR_SERVICE_NOT_ACTIVE: throw new J15Exception("Service not active");
		case ERROR_SUCCESS: return;
		case RPC_S_SERVER_UNAVAILABLE: throw new J15Unavailable("Server unavailable");
		case RPC_X_WRONG_PIPE_VERSION: throw new J15Unavailable("Wrong service version");
		default:
			throw new J15Exception("Unknown error");
		}
	}
}
