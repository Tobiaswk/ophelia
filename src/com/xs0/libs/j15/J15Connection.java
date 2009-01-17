// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15;

import static com.xs0.libs.j15.raw.J15Native.ERROR_INVALID_PARAMETER;
import static com.xs0.libs.j15.raw.J15Native.ERROR_NO_MORE_ITEMS;
import static com.xs0.libs.j15.raw.J15Native.ERROR_SUCCESS;
import static com.xs0.libs.j15.raw.J15Native.lgLcdEnumerate;
import static com.xs0.libs.j15.raw.J15Native.lgLcdOpen;

import java.util.ArrayList;

import com.xs0.libs.j15.raw.lgLcdDeviceDesc;
import com.xs0.libs.j15.raw.lgLcdOnConfigureCB;
import com.xs0.libs.j15.raw.lgLcdOpenContext;

/**
 * This class represents a connection to the Logitech LCD system, where connections
 * roughly correspond to application from the users' point of view. You can only
 * connect once per screen per connection, so if you want to display more than one
 * screen, you'll also need more than one connection (with a different name, no less)
 * 
 * @author Mitja Slenc
 *
 */
public class J15Connection implements lgLcdOnConfigureCB
{
	boolean destroyed = false;
	int connection;
	final J15ConnManager manager;
	ArrayList<J15Screen> screens = new ArrayList<J15Screen>();
	ArrayList<J15ConfListener> listeners = new ArrayList<J15ConfListener>();

	J15Connection(J15ConnManager manager)
	{
		this.manager=manager;
		this.connection = Integer.MIN_VALUE;
	}

	
	/**
	 * This function is public only because I can't hide it. It should never be called directly, it's
	 * purpose is to serve as a low-level callback from the native side of things.
	 */
	public synchronized int callback(int connection, Object context)
	{
		int s = listeners.size();
		for (int a = 0; a < s; a++)
			listeners.get(a).doConfigure(this);

		return 0;
	}

	void setConnection(int connection)
	{
		this.connection = connection;
	}

	private void checkDestroyed()
	{
		if (destroyed)
			throw new IllegalStateException();
	}
	
	/**
	 * Adds a listener that listens for configuration requests for this connection (caused by the user
	 * clicking a button in the Logitech's LCD manager.
	 *
	 * @param listener the listener to add
	 */
	public synchronized void addConfListener(J15ConfListener listener)
	{
		checkDestroyed();
		
		if (listener == null)
			return;
		
		synchronized (this) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a listener from this connection's configuration listener list.
	 * 
	 * @param listener the listener to remove
	 */
	public synchronized void removeConfListener(J15ConfListener listener)
	{
		checkDestroyed();
		
		if (listener == null)
			return;
		synchronized (this) {
			int s = listeners.size();
			for (int a = 0; a < s; a++)
				if (listeners.get(a) == listener) {
					listeners.remove(a);
					return;
				}
		}
	}

	/**
	 * Returns the number of screens currently attached. Note that certain events may cause some of the
	 * screens to not be available by the time you attach to them (like unplugging a keyboard), and additional
	 * screens may become available later..
	 * 
	 * @return the number of attached screens
	 */
	public synchronized int getNumberOfScreens()
	{
		checkDestroyed();
		
		lgLcdDeviceDesc desc = new lgLcdDeviceDesc();
		int n = 0;
		while (lgLcdEnumerate(connection, n, desc) == ERROR_SUCCESS)
			n++;
		return n;
	}
	
	/**
	 * Retrieves the parameters of the given screen.
	 *  
	 * @param screenNumber number of screen (first is 0)
	 * @return the descriptor if everything went ok, null if the screen doesn't exist (ERROR_NO_MORE_ITEMS), and an exception on other errors 
	 * @throws J15Exception if an error occurs
	 */
	public synchronized lgLcdDeviceDesc getDescriptor(int screenNumber) throws J15Exception
	{
		checkDestroyed();
		
		lgLcdDeviceDesc desc = new lgLcdDeviceDesc();
		
		int err = lgLcdEnumerate(connection, screenNumber, desc);
		
		if (err==ERROR_SUCCESS)
			return desc;
		
		if (err!=ERROR_NO_MORE_ITEMS)
			J15.handleError(err);
		
		return null;
	}
	
	/**
	 * Retrieves the parameters of the given screen and stores them in the provided descriptor.
	 *  
	 * @param screenNumber number of screen (first is 0)
	 * @param desc output descriptor
	 * @return true if everything went ok, false if the screen doesn't exist (ERROR_NO_MORE_ITEMS), and an exception on other errors 
	 * @throws J15Exception if an error occurs
	 */
	public synchronized boolean getDescriptor(int screenNumber, lgLcdDeviceDesc desc) throws J15Exception
	{
		checkDestroyed();
		
		if (desc==null)
			J15.handleError(ERROR_INVALID_PARAMETER);
		
		int err = lgLcdEnumerate(connection, screenNumber, desc);
		
		if (err==ERROR_SUCCESS)
			return true;
		
		if (err!=ERROR_NO_MORE_ITEMS)
			J15.handleError(err);
		
		return false;
	}

	/**
	 * Attaches your application to a specific screen and returns the object through which
	 * you can update it.
	 * 
	 * @param screenNumber the consecutive screen number
	 * @return a screen object to manipulate the actual screen with
	 * @throws J15Exception if an error occurs
	 */
	public synchronized J15Screen attach(int screenNumber) throws J15Exception
	{
		checkDestroyed();
			
		lgLcdDeviceDesc desc = getDescriptor(screenNumber);
		if (desc==null)
			J15.handleError(ERROR_NO_MORE_ITEMS);
		
		J15Screen screen = new J15Screen(this, desc);
		
		lgLcdOpenContext ctx = new lgLcdOpenContext();
		ctx.connection = this.connection;
		ctx.index = screenNumber;
		ctx.softbuttonsChangedCallback = screen;
		
		J15.handleError(lgLcdOpen(ctx));
		screen.setDevice(ctx.device);
		
		screens.add(screen);
		
		return screen;
	}

	/**
	 * Call to clean up this connection (and all its screens).
	 */
	public synchronized void destroy()
    {
		if (destroyed)
			return;

		destroyInternal();

		manager.forgetMe(this);
    }
	
	synchronized void destroyInternal()
	{
		if (destroyed)
			return;
		
		for (J15Screen screen : screens) {
			screen.destroyInternal();
		}
		screens.clear();
		
		destroyed = true;
	}

	synchronized void forgetMe(J15Screen screen)
    {
		screens.remove(screen);
    }
}
