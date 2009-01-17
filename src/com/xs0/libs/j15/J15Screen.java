// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

import com.xs0.libs.j15.raw.IntHolder;
import com.xs0.libs.j15.raw.J15Native;
import com.xs0.libs.j15.raw.lgLcdBitmap160x43x1;
import com.xs0.libs.j15.raw.lgLcdBitmapHeader;
import com.xs0.libs.j15.raw.lgLcdDeviceDesc;
import com.xs0.libs.j15.raw.lgLcdOnSoftButtonsCB;
import static com.xs0.libs.j15.raw.J15Native.*;

/**
 * Objects of this class represent actual screens (up to one per connection per keyboard). You can't create
 * them directly, but must use a J15Connection to do so.
 * 
 * @author Mitja Slenc
 *
 */
public class J15Screen implements lgLcdOnSoftButtonsCB
{
	private boolean destroyed = false;
	private int myDeviceNumber = J15Native.LGLCD_INVALID_DEVICE;
	private J15Connection conn = null;
	private lgLcdDeviceDesc desc = null;
	private lgLcdBitmapHeader myBitmap = null;
	private BufferedImage myImg = null;	
	ArrayList<J15ButtonListener> listeners = new ArrayList<J15ButtonListener>();

	/**
	 * Adds a soft button state listener to this screen. It will be called whenever a button
	 * is pressed or released.
	 * 
	 * @param listener the listener to add (if null, nothing happens)
	 */
	public void addButtonListener(J15ButtonListener listener)
	{
		if (listener==null)
			return;
		synchronized (this) {
			checkDestroyed();
			listeners.add(listener);
		}
	}
	
	private void checkDestroyed()
    {
		if (destroyed)
			throw new IllegalStateException("Destoroyed already");
    }

	public void removeButtonListener(J15ButtonListener listener)
	{
		if (listener==null)
			return;
		synchronized (this) {
			checkDestroyed();
			int s = listeners.size();
			for (int a=0; a<s; a++) {
				if (listeners.get(a)==listener) {
					listeners.remove(a);
					return;
				}
			}
		}
	}
	
	static final ColorSpace normalGray  = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	static final ColorSpace inverseGray = new InverseGrayColorSpace();
	
	J15Screen(J15Connection conn, lgLcdDeviceDesc desc) throws J15Exception
	{
		this.conn=conn;
		if (desc.Width!=160 || desc.Height!=43 || (desc.Bpp!=1 && desc.Bpp!=8))
			throw new J15Exception("Unsupported bitmap format");
		
		this.desc=desc;

		lgLcdBitmap160x43x1 myBitmap = new lgLcdBitmap160x43x1();
		this.myBitmap = myBitmap;
		
		ColorSpace colorSpace = inverseGray;//desc.Bpp==8 ? normalGray : inverseGray; // for 8 it's just speculation :) 
        int bits[] = { 8 };
        int offsets[] = { 0 };
        ColorModel colorModel=new ComponentColorModel(colorSpace, bits, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        DataBuffer buffer=new DataBufferByte(myBitmap.pixels, myBitmap.pixels.length);
        WritableRaster raster=Raster.createInterleavedRaster(buffer, desc.Width, desc.Height, desc.Width, 1, offsets, null);
        myImg = new BufferedImage(colorModel, raster, false, null);
	}
	
	/**
	 * This function is public only because I can't hide it. It should never be called directly, it's
	 * purpose is to serve as a low-level callback from the native side of things.
	 */
	public synchronized int callback(int device, int dwButtons, Object pContext)
	{
		int s = listeners.size();
		for (int a=0; a<s; a++) {
			try {
				listeners.get(a).onButtonChange(this, dwButtons);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	void setDevice(int device)
	{
		this.myDeviceNumber = device;
	}
	
	/**
	 * Returns the state of soft buttons on this screen.
	 * 
	 * @return the state of soft buttons
	 * @throws J15Exception if an error occurs
	 */
	public int getButtonState() throws J15Exception
	{
		IntHolder tmp = new IntHolder(0);
		J15.handleError(lgLcdReadSoftButtons(myDeviceNumber, tmp));
		return tmp.value;
	}
	
	/**
	 * Returns the state of soft buttons on this screen and stores it in the supplied IntHolder. If you
	 * query the state often, this version should be more efficient, as the IntHolder can be reused between
	 * multiple invocations.
	 * 
	 * @param out the holder in which the result is stored
	 * @throws J15Exception if an error occurs
	 */
	public void getButtonState(IntHolder out) throws J15Exception
	{
		if (out==null)
			throw new IllegalArgumentException();
		
		J15.handleError(lgLcdReadSoftButtons(myDeviceNumber, out));
	}
	
	/**
	 * Returns the width in pixels of this screen (with G15 it's always 160)
	 * 
	 * @return the width in pixels of this screen
	 */
	public int getWidth()
	{
		return desc.Width;
	}
	
	/**
	 * Returns the height in pixels of this screen (with G15 it's always 43)
	 * 
	 * @return the height in pixels of this screen
	 */
	public int getHeight()
	{
		return desc.Height;
	}
	
	/**
	 * Returns the number of bits per pixel on this screen (with G15 it's always 1)
	 * 
	 * @return the number of bits per pixel on this screen
	 */
	public int getBpp()
	{
		return desc.Bpp;
	}
	
	/**
	 * Returns the number of soft buttons on this screen (with G15 it's always 4)
	 * 
	 * @return the number of soft buttons on this screen
	 */
	public int getNumSoftButtons()
	{
		return desc.NumSoftButtons;
	}
	
	/**
	 * Returns the image of this screen. Note that you have to call updateImage when 
	 * finished drawing, if you want (a modified) image to actually show on the 
	 * physical screen. 
	 * 
	 * @return the image of this screen
	 */
	public BufferedImage getImage()
	{
		return myImg;
	}
	
	/**
	 * Returns the graphics with which you can draw to the screen's image. Note that you have
	 * to call updateImage when finished drawing, if you want the new image to actually show
	 * on the physical screen.
	 * 
	 * @return graphics to draw with on this screen
	 */
	public Graphics2D getGraphics()
	{
		return myImg.createGraphics();
	}
	
	/**
	 * Updates the image with the default priority (UPD_ASYNC_NORMAL)
	 * @throws J15Exception if an error occurs
	 */
	public void updateImage() throws J15Exception
	{
		updateImage(UPD_ASYNC_NORMAL);
	}
	
	/** Asynchronous update, lowest priority */
	public static final int UPD_ASYNC_IDLE   = J15Native.LGLCD_ASYNC_UPDATE(J15Native.LGLCD_PRIORITY_IDLE_NO_SHOW);
	/** Asynchronous update, second lowest priority */
	public static final int UPD_ASYNC_BACK   = J15Native.LGLCD_ASYNC_UPDATE(J15Native.LGLCD_PRIORITY_BACKGROUND);
	/** Asynchronous update, second highest priority */
	public static final int UPD_ASYNC_NORMAL = J15Native.LGLCD_ASYNC_UPDATE(J15Native.LGLCD_PRIORITY_NORMAL);
	/** Asynchronous update, highest priority */
	public static final int UPD_ASYNC_ALERT  = J15Native.LGLCD_ASYNC_UPDATE(J15Native.LGLCD_PRIORITY_ALERT);
	
	/** Synchronous update, lowest priority */
	public static final int UPD_SYNC_IDLE   = J15Native.LGLCD_SYNC_UPDATE(J15Native.LGLCD_PRIORITY_IDLE_NO_SHOW);
	/** Synchronous update, second lowest priority */
	public static final int UPD_SYNC_BACK   = J15Native.LGLCD_SYNC_UPDATE(J15Native.LGLCD_PRIORITY_BACKGROUND);
	/** Synchronous update, second highest priority */
	public static final int UPD_SYNC_NORMAL = J15Native.LGLCD_SYNC_UPDATE(J15Native.LGLCD_PRIORITY_NORMAL);
	/** Synchronous update, highest priority */
	public static final int UPD_SYNC_ALERT  = J15Native.LGLCD_SYNC_UPDATE(J15Native.LGLCD_PRIORITY_ALERT);
	
	/** Synchronous update within current frame, lowest priority */
	public static final int UPD_FRAME_IDLE   = J15Native.LGLCD_SYNC_COMPLETE_WITHIN_FRAME(J15Native.LGLCD_PRIORITY_IDLE_NO_SHOW);
	/** Synchronous update within current frame, second lowest priority */
	public static final int UPD_FRAME_BACK   = J15Native.LGLCD_SYNC_COMPLETE_WITHIN_FRAME(J15Native.LGLCD_PRIORITY_BACKGROUND);
	/** Synchronous update within current frame, second highest priority */
	public static final int UPD_FRAME_NORMAL = J15Native.LGLCD_SYNC_COMPLETE_WITHIN_FRAME(J15Native.LGLCD_PRIORITY_NORMAL);
	/** Synchronous update within current frame, highest priority */
	public static final int UPD_FRAME_ALERT  = J15Native.LGLCD_SYNC_COMPLETE_WITHIN_FRAME(J15Native.LGLCD_PRIORITY_ALERT);

	/**
	 * Updates this screen with a new image. The priority parameter determines whether the function should<ul>
	 * <li>return immediately (UPD_ASYNC_*),
	 * <li>wait for the update to happen (UPD_SYNC_*), or 
	 * <li>wait for the update to happen, but fail if that doesn't happen within the current frame (UPD_FRAME_*)
	 * </ul>
	 * You can get to the image with getImage or getGraphics. Modifying it does not update the screen, until
	 * this function is called.
	 * 
	 * @param priority one of the UPD_ constants
	 * @throws J15Exception if an error occurs
	 */
	public void updateImage(int priority) throws J15Exception
	{
		J15.handleError(lgLcdUpdateBitmap(myDeviceNumber, myBitmap, priority));
	}
	
	/**
	 * Requests for this screen to be moved to the foreground (if true) or to be treated
	 * normally again (if false). 
	 * 
	 * @param toForeground as described above
	 * @throws J15Exception if an error occurs
	 */
	public void setAsForegroundApp(boolean toForeground) throws J15Exception
	{
		J15.handleError(lgLcdSetAsLCDForegroundApp(myDeviceNumber, toForeground?LGLCD_LCD_FOREGROUND_APP_YES:LGLCD_LCD_FOREGROUND_APP_NO));
	}

	// note: one could override finalize(), but it would
	// never happen in a useful context anyway - screens
	// are referenced by connections, and those are referenced
	// by the conn manager, and the only way to have anything
	// happen is actually manually destroying them anyway
	//
	// furthermore, the native side holds references to listeners
	// anyway, so it's not even possible to have any kind
	// of automatic disposal
	
	/**
	 * Hides this screen and releases any native resources associated with it. 
	 */
	public synchronized void destroy()
    {
		if (destroyed)
			return;
		
		destroyInternal();
		conn.forgetMe(this);
    }
	
	synchronized void destroyInternal()
	{
		if (destroyed)
			return;
		
		listeners = null;
		conn = null;
		desc = null;
		myBitmap = null;
		myImg = null;
		lgLcdClose(myDeviceNumber);
		myDeviceNumber = J15Native.LGLCD_INVALID_DEVICE;
		
		destroyed = true;
	}
}
