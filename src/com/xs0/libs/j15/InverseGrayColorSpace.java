// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15;

import java.awt.color.ColorSpace;

/**
 * A hack to implement an inverse gray ColorSpace. It is needed to 
 * match what is displayed on screens to Java's colors (1.0f or 255 
 * normally white, but produces black on the LCD).
 * 
 * @author Mitja Slenc
 *
 */
public final class InverseGrayColorSpace extends ColorSpace
{
	static final ColorSpace backend = ColorSpace.getInstance(CS_GRAY);
	
	public InverseGrayColorSpace()
    {
		super(ColorSpace.CS_GRAY, 1);
    }

	@Override
	public float[] toCIEXYZ(float[] colorvalue)
	{
		int l = colorvalue.length;
		float[] tmp = new float[l];
		for (int a=0; a<l; a++)
			tmp[a] = 1.0f - colorvalue[a];
		
		return backend.toCIEXYZ(tmp);
	}

	@Override
    public float[] toRGB(float[] colorvalue)
    {
		int l = colorvalue.length;
		float[] tmp = new float[l];
		for (int a=0; a<l; a++)
			tmp[a] = 1.0f - colorvalue[a];
		
		return backend.toRGB(tmp);
    }			

	@Override
    public float[] fromCIEXYZ(float[] colorvalue)
    {
		float[] tmp = backend.fromCIEXYZ(colorvalue);
		int l = tmp.length;
		for (int a=0; a<l; a++)
			tmp[a] = 1.0f - tmp[a];
		return tmp;
    }

	@Override
    public float[] fromRGB(float[] rgbvalue)
    {
		float[] tmp = backend.fromRGB(rgbvalue);
		int l = tmp.length;
		for (int a=0; a<l; a++)
			tmp[a] = 1.0f - tmp[a];
		return tmp;
    }
}
