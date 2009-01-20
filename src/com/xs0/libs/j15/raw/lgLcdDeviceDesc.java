// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

/**
 * A class that holds description of a screen. The information includes width, height, number of
 * bits per pixel and number of soft buttons available.
 * 
 * @author Mitja Slenc
 */
public final class lgLcdDeviceDesc implements Cloneable
{
	public int Width;
	public int Height;
	public int Bpp;
	public int NumSoftButtons;
	
	@Override
	public lgLcdDeviceDesc clone()
	{
		try {
			return (lgLcdDeviceDesc) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
}
