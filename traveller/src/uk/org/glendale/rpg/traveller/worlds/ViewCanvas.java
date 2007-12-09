/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.worlds;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.*;

import uk.org.glendale.graphics.SimpleImage;

import jgl.*;

public class ViewCanvas extends GLCanvas implements ImageObserver {
	private static final long serialVersionUID = 1L;
	private Image		map = null;
	
	public ViewCanvas() {
		
	}
	
	public ViewCanvas(Image map) {
		this.map = map;
		//init();
		
		
		int			width = map.getWidth(this);
		int			height = map.getHeight(this);
		
		//int width = 516;
		//int height = 200;
		byte[][][]  texture = new byte[width][height][3];
		int[]		pixels = new int[width*height];

		
		PixelGrabber		pg = new PixelGrabber(map, 0, 0, width, height, pixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			// Can this happen?
		}
		
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				
				int pixel = pixels[y * width + x];
		        int alpha = (pixel >> 24) & 0xff;
		        int red   = (pixel >> 16) & 0xff;
		        int green = (pixel >>  8) & 0xff;
		        int blue  = (pixel      ) & 0xff;

		        texture[x][y][0] = (byte)red;
				texture[x][y][1] = (byte)green;
				texture[x][y][2] = (byte)blue;
				
		        //texture[x][y][0] = (byte)50;
				//texture[x][y][1] = (byte)150;
				//texture[x][y][2] = (byte)0;
			}
		}
		
    	myUT.glutInitWindowSize (700, 700);
    	myUT.glutInitWindowPosition (0, 0);
    	myUT.glutCreateWindow (this);
    	myinit (width, height, texture);
    	myUT.glutReshapeFunc ("myReshape");
    	myUT.glutDisplayFunc ("display");
    	myUT.glutKeyboardFunc ("keyboard");
    	myUT.glutMainLoop ();


		Frame mainFrame = new Frame ();
    	mainFrame.setSize (700, 700);
    	mainFrame.add (this);
    	mainFrame.setVisible (true);
    	
	}
	

	public void displaySphere() {
		final double PI = 3.14159265359;
		final double TWOPI = 6.28318530718;
		final double DE2RA = 0.01745329252;
		final double RA2DE = 57.2957795129;
		final double FLATTENING = 1.0/298.26;
		final double PIOVER2 = 1.570796326795;
		
		myGL.glBegin(GL.GL_TRIANGLES);
		
		double	start_lat = -90;
		double	start_lon = 0;
		double	R = 1.0;
		
		int		numLats = 200, numLongs = 200;
		double	lat_inc = 180.0 / numLats;
		double  lon_inc = 360.0 / numLongs;
		
		for (int col=0; col < numLongs; col++) {
			double phi1 = (start_lon + col * lon_inc) * DE2RA;
			double phi2 = (start_lon + (col + 1) * lon_inc) * DE2RA;
			
			for (int row = 0; row < numLats; row++) {
				double theta1 = (start_lat + row * lat_inc) * DE2RA;
				double theta2 = (start_lat + (row+1) * lat_inc) * DE2RA;
				
				double[] u = new double[3];
				double[] v = new double[3];
				double[] w = new double[3];
				
				u[0] = R * Math.cos(phi1) * Math.cos(theta1);
				u[1] = R * Math.sin(theta1);
				u[2] = R * Math.sin(phi1) * Math.cos(theta1);
				
				v[0] = R * Math.cos(phi1) * Math.cos(theta2);
				v[1] = R * Math.sin(theta2);
				v[2] = R * Math.sin(phi1) * Math.cos(theta2);
				
				v[0] = R * Math.cos(phi2) * Math.cos(theta2);
				v[1] = R * Math.sin(theta2);
				v[2] = R * Math.sin(phi2) * Math.cos(theta2);
				
			}
		}
	}

	/**
	 * Display it, I think.
	 */
    public void display () {
    	myGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    	myGL.glBegin (GL.GL_QUADS);
    	myGL.glTexCoord2f (0.0f, 0.0f);
    	myGL.glVertex3f (-2.0f, -1.0f, 0.0f);
    	myGL.glTexCoord2f (0.0f, 1.0f);
    	myGL.glVertex3f (-2.0f, 1.0f, 0.0f);
    	myGL.glTexCoord2f (1.0f, 1.0f);
    	myGL.glVertex3f (0.0f, 1.0f, 0.0f);
    	myGL.glTexCoord2f (1.0f, 0.0f);
    	myGL.glVertex3f (0.0f, -1.0f, 0.0f);
/*
    	myGL.glTexCoord2f (0.0f, 0.0f);
    	myGL.glVertex3f (1.0f, -1.0f, 0.0f);
    	myGL.glTexCoord2f (0.0f, 1.0f);
    	myGL.glVertex3f (1.0f, 1.0f, 0.0f);
    	myGL.glTexCoord2f (1.0f, 1.0f);
    	myGL.glVertex3f (2.41421f, 1.0f, -1.41421f);
    	myGL.glTexCoord2f (1.0f, 0.0f);
    	myGL.glVertex3f (2.41421f, -1.0f, -1.41421f);
    	*/
    	myGL.glEnd ();
    	
	
    	myGL.glFlush ();
    }
    
    public void myReshape (int w, int h) {
        myGL.glViewport (0, 0, w, h);
        myGL.glMatrixMode (GL.GL_PROJECTION);
        myGL.glLoadIdentity ();
        myGLU.gluPerspective (60.0, 1.0 * (double)w/(double)h, 1.0, 30.0);
        myGL.glMatrixMode (GL.GL_MODELVIEW);
        myGL.glLoadIdentity ();
        myGL.glTranslatef (0.0f, 0.0f, -3.6f);
    }

    public void keyboard (char key, int x, int y) {
    	switch (key) {
  	    case 32:
    		System.exit(0);
   	    default:
   		break;
    	}
    }

    private void myinit (int width, int height, byte[][][] texture) {
    	myGL.glClearColor (0.4f, 0.0f, 0.0f, 0.0f);
    	myGL.glEnable (GL.GL_DEPTH_TEST);
    	myGL.glDepthFunc (GL.GL_LESS);

    	myGL.glPixelStorei (GL.GL_UNPACK_ALIGNMENT, 1);
    	myGL.glTexImage2D (GL.GL_TEXTURE_2D, 0, 3, width,
    	    height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, texture);
    	myGL.glTexParameterf (GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    	myGL.glTexParameterf (GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
    	myGL.glTexParameterf (GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    	myGL.glTexParameterf (GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    	myGL.glTexEnvf (GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
    	myGL.glEnable (GL.GL_TEXTURE_2D);
    	myGL.glShadeModel (GL.GL_FLAT);
    }


    static public void main (String args[]) throws IOException {
    	ViewCanvas mainCanvas = new ViewCanvas (null);
    }

}
