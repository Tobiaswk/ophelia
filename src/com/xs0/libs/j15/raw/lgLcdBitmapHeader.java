// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.raw;

/**
 * The base class for image classes. Currently serves no particular purpose, it's just here
 * to mimic the C structures.
 *  
 * @author Mitja Slenc
 */
public abstract class lgLcdBitmapHeader
{
	/** The image format constant */
	public final int Format;
	
	/**
	 * Initializes the "header" with the specified format.
	 * @param Format the format ID number (e.g. LGLCD_BMP_FORMAT_160x43x1)
	 */
	protected lgLcdBitmapHeader(int Format)
	{
		this.Format = Format;
	}
}
