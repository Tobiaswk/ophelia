// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

import java.io.File;

/**
 * This class is a holder for the native LCD functions. As all the methods are static, 
 * it can't be instantiated. Refer to the Logitech LCD SDK for detailed documentation
 * on what each function does, the functions are designed to mirror those as close as
 * possible.
 * <br/><br/>
 * The class checks whether the associated .dll could be loaded, and most functions
 * then return RPC_S_SERVER_UNAVAILABLE instead of throwing an exception, to avoid
 * the need to check for errors twice.
 * 
 * @author Mitja Slenc
 */
public final class J15Native
{
	private J15Native()
	{
		// prevent instantiation
	}
	
	private static native int _lgLcdInit();
	/**
	 * Must be called before any other functions will work. Initialized the
	 * native library and prepares it for use.
	 * <br/><br/>
	 * 
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdInit()
	{
		if (libLoaded) {
			return _lgLcdInit();
		} else {
			return RPC_S_SERVER_UNAVAILABLE;
		}
	}
	
	private static native int _lgLcdDeInit();
	/**
	 * Must be called at the end of using this library to ensure resources are
	 * properly cleaned up. Failure to do so has been known to cause LCDMon freezes,
	 * so definitely do call it.
	 * 
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdDeInit()
	{
		if (libLoaded) {
			return _lgLcdDeInit();
		} else {
			return ERROR_SUCCESS;
		}
	}
	
	private static char[] addZero(String s)
	{
		char[] out = new char[s.length()+1];
		s.getChars(0, s.length(), out, 0);
		out[out.length-1]=0;
		return out;
	}
	
	private static native int _lgLcdConnect(char[] appFriendlyName, boolean isPersistent, boolean isAutostartable, lgLcdOnConfigureCB configCallback, Object configContext, IntHolder connection);
	/**
	 * Connects to the Logitech LCD system and allows later attachment to actual screens. One connection
	 * is needed for each screen you want to display (so, connecting twice looks the same to the user
	 * as if two applications were connected). All parameters in ctx must be filled in upon calling this
	 * function, except connection. If no errors occur, ctx.connection is filled with the connection
	 * identifier you can use in later calls.
	 * 
	 * @param ctx connection parameters
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdConnect(lgLcdConnectContext ctx)
	{
		if (!libLoaded)
			return RPC_S_SERVER_UNAVAILABLE;

		if (ctx == null || ctx.appFriendlyName == null)
			return ERROR_INVALID_PARAMETER;

		IntHolder conn = new IntHolder(ctx.connection);
		int err = _lgLcdConnect(
				addZero(ctx.appFriendlyName), 
				ctx.isPersistent, 
				ctx.isAutostartable,
				ctx.configureCallback,
				ctx.configureContext, 
				conn);
		
		ctx.connection = conn.value;
		return err;
	}

	private static native int _lgLcdDisconnect(int connection);
	/**
	 * Disconnects a LCD connection. Should be called when a connection is no longer used to
	 * ensure proper clean-up of system resources.
	 * 
	 * @param connection connection identifier
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdDisconnect(int connection)
	{
		if (libLoaded) {
			return _lgLcdDisconnect(connection);
		} else {
			return RPC_S_SERVER_UNAVAILABLE;
		}
	}

	private static native int _lgLcdEnumerate(int connection, int index, lgLcdDeviceDesc description);
	/**
	 * Enumerates and describes available LCD screens. This function should be called with initial index
	 * set to 0, and then increased by one until ERROR_NO_MORE_ITEMS is returned; that will allow
	 * you to determine the number of screens as well as their properties. The fields of the description 
	 * parameter are filled in with properties of the screen upon success, and undefined in all other
	 * cases.
	 * 
	 * @param connection connection identifier
	 * @param index screen number
	 * @param description the holder for the description of the screen
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdEnumerate(int connection, int index, lgLcdDeviceDesc description)
	{
		if (!libLoaded)
			return RPC_S_SERVER_UNAVAILABLE;

		if (description==null)
			return ERROR_INVALID_PARAMETER;

		return _lgLcdEnumerate(connection, index, description);
	}

	private static native int _lgLcdOpen(int connection, int index, lgLcdOnSoftButtonsCB softbuttonsChangedCallback, Object softbuttonsChangedContext, IntHolder device);
	/**
	 * Opens a device/screen and returns an identifier (in ctx.device) that can be used for later screen
	 * updates and for reading soft keys. You can provide a callback that will be called whenever
	 * the user presses a soft button of the corresponding screen.
	 * 
	 * @param ctx screen opening parameters
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdOpen(lgLcdOpenContext ctx)
	{
		if (!libLoaded)
			return RPC_S_SERVER_UNAVAILABLE;

		if (ctx==null)
			return ERROR_INVALID_PARAMETER;
		
		IntHolder device = new IntHolder(ctx.device);

		int err = _lgLcdOpen(
			ctx.connection,
			ctx.index,
			ctx.softbuttonsChangedCallback,
			ctx.softbuttonsChangedContext,
			device);
		
		ctx.device = device.value;
		
		return err;
	}

	private static native int _lgLcdClose(int device);
	/**
	 * Closes a screen, making it no longer visible/available.
	 * 
	 * @param device the device identifier
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdClose(int device)
	{
		if (!libLoaded)
			return RPC_S_SERVER_UNAVAILABLE;
		
		return _lgLcdClose(device);
	}

	private static native int _lgLcdReadSoftButtons(int device, IntHolder buttons);
	/**
	 * Returns the state of buttons on the given screen as a bitmask of LGLCDBUTTON_BUTTON*.
	 * 
	 * @param device the device identifier
	 * @param buttons the holder of the result
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdReadSoftButtons(int device, IntHolder buttons)
	{
		if (!libLoaded)
			return RPC_S_SERVER_UNAVAILABLE;

		return _lgLcdReadSoftButtons(device, buttons);
	}

	private static native int _lgLcdUpdateBitmap(int device, int format, byte[] data, int priority);
	/**
	 * Updates a screen with a new image. The priority parameter specifies
	 * <ul>
	 * <li>The priority of the update (afaik, used for "smart" switching of active LCD applications)
	 * <li>Whether the request should complete synchronously or asynchrounously.
	 * </ul>
	 * @param device the device identifier
	 * @param bitmap the image to draw
	 * @param priority priority specifier
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdUpdateBitmap(int device, lgLcdBitmapHeader bitmap, int priority)
	{
		if (!libLoaded)
			return RPC_S_SERVER_UNAVAILABLE;
		
		if (bitmap == null || bitmap.Format!=LGLCD_BMP_FORMAT_160x43x1 || !(bitmap instanceof lgLcdBitmap160x43x1))
			return ERROR_INVALID_PARAMETER;

		lgLcdBitmap160x43x1 bbitmap = (lgLcdBitmap160x43x1) bitmap;

		return _lgLcdUpdateBitmap(device, LGLCD_BMP_FORMAT_160x43x1, bbitmap.pixels, priority);
	}

	private static native int _lgLcdSetAsLCDForegroundApp(int device, int foregroundYesNoFlag);
	
	/**
	 * Sets the application as the foreground on a given screen/device. If set to true, the application
	 * will be brought to foreground (until another application does the same). If set to false, the
	 * application regains normal status.
	 * 
	 * @param device the device identifier
	 * @param foregroundYesNoFlag either of LGLCD_LCD_FOREGROUND_*
	 * @return status code (ERROR_SUCCESS if everything OK)
	 */
	public static int lgLcdSetAsLCDForegroundApp(int device, int foregroundYesNoFlag)
	{
		if (!libLoaded)
			return RPC_S_SERVER_UNAVAILABLE;
		
		return _lgLcdSetAsLCDForegroundApp(device, foregroundYesNoFlag);
	}

	public static final int LGLCD_INVALID_CONNECTION = -1;
	public static final int LGLCD_INVALID_DEVICE = -1;

	/** Bit mask for first soft button */
	public static final int LGLCDBUTTON_BUTTON0 = 0x00000001;
	/** Bit mask for second soft button */
	public static final int LGLCDBUTTON_BUTTON1 = 0x00000002;
	/** Bit mask for third soft button */
	public static final int LGLCDBUTTON_BUTTON2 = 0x00000004;
	/** Bit mask for fourth soft button */
	public static final int LGLCDBUTTON_BUTTON3 = 0x00000008;

	/** The only current image format specifier - 160x43 pixels, 1 bit each */
	public static final int LGLCD_BMP_FORMAT_160x43x1 = 0x00000001;
	/** Screen width */
	public static final int LGLCD_BMP_WIDTH = 160;
	/** Screen height */
	public static final int LGLCD_BMP_HEIGHT = 43;

	/** Lowest priority */
	public static final int LGLCD_PRIORITY_IDLE_NO_SHOW = 0;
	/** Second lowest priority */
	public static final int LGLCD_PRIORITY_BACKGROUND = 64;
	/** Second highest priority */
	public static final int LGLCD_PRIORITY_NORMAL = 128;
	/** Highest priority */
	public static final int LGLCD_PRIORITY_ALERT = 255;

	/** Means the application should no longer be foreground (default) */
	public static final int LGLCD_LCD_FOREGROUND_APP_NO = 0;
	/** Means the application should be made foreground */
	public static final int LGLCD_LCD_FOREGROUND_APP_YES = 1;

	/** No error / operation successfully completed */
	public static final int ERROR_SUCCESS = 0;
	
	/** Error code */
	public static final int ERROR_FILE_NOT_FOUND = 2;
	/** Error code */
	public static final int ERROR_ACCESS_DENIED = 5;
	/** Error code */
	public static final int ERROR_INVALID_PARAMETER = 87;
	/** Error code */
	public static final int ERROR_LOCK_FAILED = 167;
	/** Error code */
	public static final int ERROR_ALREADY_EXISTS = 183;
	/** Error code */
	public static final int ERROR_NO_MORE_ITEMS = 259;
	/** Error code */
	public static final int ERROR_SERVICE_NOT_ACTIVE = 1062;
	/** Error code */
	public static final int ERROR_OLD_WIN_VERSION = 1150;
	/** Error code */
	public static final int ERROR_DEVICE_NOT_CONNECTED = 1167;
	/** Error code */
	public static final int ERROR_ALREADY_INITIALIZED = 1247;
	/** Error code */
	public static final int ERROR_NO_SYSTEM_RESOURCES = 1450;

	/** Error code */
	public static final int RPC_S_SERVER_UNAVAILABLE = 1722;
	/** Error code */
	public static final int RPC_X_WRONG_PIPE_VERSION = 1832;

	/** Modifies a base update priority to make it update-synchronized */
	public static final int LGLCD_SYNC_UPDATE(int priority)
	{
		return 0x80000000 | (priority);
	}
	
	/** Modifies a base update priority to make it update-synchronized and fail-if-timeout */
	public static final int LGLCD_SYNC_COMPLETE_WITHIN_FRAME(int priority)
	{
		return 0xC0000000 | (priority);
	}
	
	/** Modifies a base update priority to make it asynchronized */
	public static final int LGLCD_ASYNC_UPDATE(int priority)
	{
		return priority;
	}
	
	private static boolean libLoaded;
	static {
		try {
			System.load("C:\\Users\\Tobias W. Kjeldsen\\Documents\\NetBeansProjects\\Ophelia\\src\\J15_Win32.dll");
			libLoaded = true;
		} catch (Throwable e) {
			libLoaded = false;
            System.out.println(e.getMessage());
		}
	}
}
