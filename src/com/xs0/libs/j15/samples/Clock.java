// Copyright (C) 2006 Mitja Slenc
// See license.txt for licensing details

package com.xs0.libs.j15.samples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DateFormat;
import java.util.Date;

import com.xs0.libs.j15.J15;
import com.xs0.libs.j15.J15ButtonListener;
import com.xs0.libs.j15.J15Connection;
import com.xs0.libs.j15.J15Exception;
import com.xs0.libs.j15.J15Screen;
import com.xs0.libs.j15.raw.lgLcdDeviceDesc;

/**
 * A simple (textual) clock J15 sample. Exits when a soft button is pressed.
 *  
 * @author Mitja Slenc
 */
public class Clock
{
	static boolean stop;

	static void drawClock(J15Screen screen)
	{
		int w = screen.getWidth();
		int h = screen.getHeight();
		
		Graphics2D g = screen.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, w-1, h-1);
		
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		DateFormat tf = DateFormat.getTimeInstance(DateFormat.FULL);
		
		String time = tf.format(new Date());
		String date = df.format(new Date());
		
		g.drawString(time, 3, 13);
		g.drawString(date, 3, 26);
	}
	
	public static void main(String[] args) throws J15Exception
	{
		J15Connection conn = J15.get().connect("Java clock", false, false);
		
		lgLcdDeviceDesc desc = new lgLcdDeviceDesc();
		
		int num = 0;
		while (conn.getDescriptor(num, desc))
			num++;
		
		J15Screen[] screens = new J15Screen[num];
		for (int a=0; a<num; a++) {
			screens[a] = conn.attach(a);
			drawClock(screens[a]);
			screens[a].updateImage();
			screens[a].addButtonListener(new J15ButtonListener() {
				public void onButtonChange(J15Screen source, int newButtonState)
				{
					stop = true;
				}
			});
			screens[a].setAsForegroundApp(true);
		}
		
		
		while (!stop) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				break;
			}
			for (int a=0; a<num; a++) {
				drawClock(screens[a]);
				screens[a].updateImage();
			}
		}
		
		J15.cleanUp();
	}
}
