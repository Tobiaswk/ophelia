// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

/**
 * This class holds the raw bytes of the only currently supported image format.
 * 
 * @author Mitja Slenc
 */
public final class lgLcdBitmap160x43x1 extends lgLcdBitmapHeader
{
	/**
	 * Creates a new bitmap object.
	 */
	public lgLcdBitmap160x43x1()
	{
		super(J15Native.LGLCD_BMP_FORMAT_160x43x1);
	}
	
	/**
	 * Holds the image data (currently, <128 means white and >=128 means black)
	 */
	public final byte[] pixels = new byte[160*43];
}
