/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.builders;

import java.awt.Image;
import java.net.MalformedURLException;
import java.util.Hashtable;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import uk.org.glendale.graphics.SimpleImage;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class MapDrawer {
	private Tile[][] map = null;
	private int scale = 0;
	private int width, height;
	private int[][] fractalMap;
	private String fractalTint;
	private int[][] heightMap;

	MapDrawer(Tile[][] map, int scale) {
		this.map = map;
		this.scale = scale;

		this.height = map.length;
		this.width = map[0].length;
	}

	public void setFractalMap(int[][] fractalMap, String colourTint) {
		this.fractalMap = fractalMap;
		this.fractalTint = colourTint;
	}

	public void setHeightMap(int[][] heightMap) {
		this.heightMap = heightMap;
	}

	private Hashtable<String, Image> imageCache = new Hashtable<String, Image>();

	private Image getImage(String name, int scale) {
		Image img = imageCache.get(name);

		if (img != null) {
			// Have cached version of image, just return that.
			return img;
		} else if (name.startsWith("#")) {
			// Create image based on hex code.
			img = SimpleImage.createImage(scale, scale, name);
		} else {
			img = SimpleImage.createImage(scale, scale, "#010101");
		}
		imageCache.put(name, img);
		return img;
	}

	public SimpleImage getWorldMap() {
		SimpleImage image = new SimpleImage(width * scale, height * scale);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				String name = map[y][x].getRGB(heightMap[y][x]);
				Image i = getImage(name, scale);
				image.paint(i, x * scale, y * scale, scale, scale);
			}
		}

		if (fractalMap != null) {
			System.out.println("Fractal!");
			int min = 1000, max = 0;
			for (int x = 0; x < width * scale; x++) {
				for (int y = 0; y < height * scale; y++) {
					if (!map[y][x].isWater()) {
						int f = fractalMap[y][x];
						if (f < min)
							min = f;
						if (f > max)
							max = f;

						String h = Integer.toHexString((100 - f) / 2);

						if (h.length() < 2)
							h = "0" + h;
						String colour = fractalTint + h;

						image.rectangle(x, y, 1, 1, colour);
					}
				}
			}
			System.out.println("Range: " + min + " to " + max);
		}

		return image;
	}

	public SimpleImage getWorldGlobe(int scale) throws MalformedURLException {
		SimpleImage image = getWorldMap();

		// image = new SimpleImage(new File("/home/sam/appleseed.jpg"));

		Canvas3D canvas = new Canvas3D(
				SimpleUniverse.getPreferredConfiguration(), true);
		canvas.getScreen3D().setPhysicalScreenHeight(0.5);
		canvas.getScreen3D().setPhysicalScreenWidth(0.5);
		canvas.getScreen3D().setSize(500, 500);

		System.out.println("Width ["
				+ canvas.getScreen3D().getPhysicalScreenWidth() + "] Height ["
				+ canvas.getScreen3D().getPhysicalScreenHeight() + "]");

		SimpleUniverse universe = new SimpleUniverse(canvas);
		Appearance app = new Appearance();
		BranchGroup root = new BranchGroup();

		TextureLoader loader = new TextureLoader(image.getBufferedImage());

		BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0.0, 5), 5.0);
		Color3f lightColour = new Color3f(1.0f, 1.0f, 1.0f);
		Color3f ambientColour = new Color3f(0.5f, 0.5f, 0.5f);
		Vector3f lightDirection = new Vector3f(0.0f, 0f, -1f);
		DirectionalLight light1 = new DirectionalLight(lightColour,
				lightDirection);
		light1.setInfluencingBounds(bounds);
		root.addChild(light1);

		AmbientLight ambientLightNode = new AmbientLight(ambientColour);
		ambientLightNode.setInfluencingBounds(bounds);
		root.addChild(ambientLightNode);

		app.setTexture(loader.getTexture());
		app.setTextureAttributes(new TextureAttributes(
				TextureAttributes.MODULATE, new Transform3D(), new Color4f(),
				TextureAttributes.NICEST));

		// Material mat = new Material();
		// mat.setEmissiveColor(1.0f, 1.0f, 1.0f);
		// app.setMaterial(mat);

		Sphere sphere = new Sphere(0.8f, Sphere.GENERATE_TEXTURE_COORDS
				| Sphere.GENERATE_NORMALS, 100);
		sphere.setAppearance(app);
		root.addChild(sphere);

		root.compile();
		universe.getViewingPlatform().setNominalViewingTransform();
		universe.addBranchGraph(root);

		// Canvas3D canvas = universe.getCanvas();

		ImageComponent2D buffer = new ImageComponent2D(
				ImageComponent2D.FORMAT_RGB,
				new SimpleImage(500, 500).getBufferedImage());
		System.out.println("Width ["
				+ canvas.getScreen3D().getPhysicalScreenWidth() + "] Height ["
				+ canvas.getScreen3D().getPhysicalScreenHeight() + "]");
		canvas.setOffScreenBuffer(buffer);
		canvas.renderOffScreenBuffer();
		canvas.waitForOffScreenRendering();
		buffer = canvas.getOffScreenBuffer();
		if (buffer == null) {
			System.out.println("No off screen buffer");
		}
		Image img = buffer.getImage();
		universe.cleanup();

		// return image;
		return new SimpleImage(img);
	}

}
