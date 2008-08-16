/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.8 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.worlds;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.*;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;

/**
 * Base class for the building of world maps.
 * 
 * The map itself consists of a rectangular grid of squares (normally twice as wide as high),
 * and each element is a single integer which stores both the height and terrain type.
 * 
 * The height is stored as mod 1000, the terrain as the thousands part.
 * 
 * Heights should be limited between 0 and about 100.
 * 
 * @author Samuel Penn
 *
 */
public class WorldBuilder {
	protected		int			width = 256+1;
	protected		int			height = 128+1;
	
	protected		int[][]		map = null;
	// Use the following to calculate how many squares there are of
	// each height. Can be used to find distribution stats quite
	// quickly.
	protected		int[]		heightBuckets = new int[100];
	
	protected 		Planet		planet = null;
	protected		int			hydrographics = 0;
	
	protected final	Terrain		sea = Terrain.create("Sea", 100, 100, 255, true);
	protected final	Terrain		land = Terrain.create("Land", 0, 0, 0, 1.5, 1.5, 1.5, false);
	
	protected		Properties	properties = new Properties();
	
	/**
	 * Properties define special geographical or ecological features about
	 * the world. If set, then they control how the world map is generated,
	 * and can be used when building a textual description of the world.
	 * 
	 * @param property		Property to check.
	 * @return				True if world has this property, false otherwise.
	 */
	public boolean hasProperty(String property) {
		if (properties.getProperty(property) != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * Set a property about this world.
	 * 
	 * @param property		Property to set.
	 */
	public void setProperty(String property) {
		properties.setProperty(property, "yes");
	}
	
	/**
	 * Get the temperature of the world. Zero is considered to be standard
	 * temperature, with -1 for cool, -2 cold, +1 warm, +2 hot etc.
	 * 
	 * @param temperature		Temperature to use.
	 */
	protected int getTemperature() {
		if (planet == null) return 0;

		switch (planet.getTemperature()) {
		case UltraHot: return +8;
		case ExtremelyHot:return +5;
		case VeryHot: return +3;
		case Hot: return +2;
		case Warm: return +1;
		case Standard: return 0;
		case Cool: return -1;
		case Cold: return -2;
		case VeryCold: return -3;
		case ExtremelyCold: return -5;
		case UltraCold: return -8;
		}
		
		return 0;
	}
	
	protected LifeType getLife() {
		if (planet == null) return LifeType.None;
		
		return planet.getLifeLevel();
	}

	
	public WorldBuilder() {
		map = new int[width][height];
		generateFractalHeightMap();
	}

	public WorldBuilder(int width, int height) {
		this.width = width;
		this.height = height;

		map = new int[width][height];
		generateFractalHeightMap();
	}
	
	/**
	 * Set the height of the given point on the map. The height is limited
	 * to be between 0 and 99. Values outside of this range will be capped.
	 * 
	 * @param x	X coordinate on the map.
	 * @param y	Y coordinate on the map.
	 * @param h	Height to set the point to.
	 */
	protected void setHeight(int x, int y, int h) {
		if (x < 0) x += width;
		if (x >= width) x -= width;
		
		if (y < 0) {
			y = 0;
			x = width - x;
		}
		if (y >= height) {
			y = height-1;
			x = width - x;
		}

		int		value = map[x][y];
		
		if (h > 99) h = 99;
		if (h < 0)  h = 0;
		
		map[x][y] = (value/1000) * 1000 + h;
	}

	protected void setTerrain(int x, int y, Terrain t) {
		if (y < 0) {
			y = 0;
			x = width - x;
		}
		if (y >= height) {
			y = height-1;
			x = width - x;
		}
		if (x < 0) x += width;
		if (x >= width) x -= width;
		
		int		value = map[x][y];
		map[x][y] = t.getIndex()*1000 + value%1000;
	}
	
	protected int getHeight(int x, int y) {
		if (x < 0) x += width;
		if (x >= width) x -= width;
		
		if (y < 0) {
			y = 0;
			x = width - x;
		}
		if (y >= height) {
			y = height-1;
			x = width - x;
		}
		return map[x][y] % 1000;
	}
	
	protected Terrain getTerrain(int x, int y) {
		if (x < 0) x += width;
		if (x >= width) x -= width;
		
		if (y < 0) {
			y = 0;
			x = width - x - 1;
		}
		if (y >= height) {
			y = height-1;
			x = width - x - 1;
		}
		return Terrain.getTerrain(map[x][y] / 1000);
	}





	/**
	 * Use a fractal algorithm to generate a random height map.
	 */
	protected void generateFractalHeightMap() {
		int[][]		h = new int[width][height];
		
		// Initiate everything to be '-1', which is used as a null value.
		// This is actually an illegal height, so we musn't copy it into
		// the real map at the end.
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				h[x][y] = -1;
			}
		}
		
		for (int i=0; i < 100; i++) heightBuckets[i] = 0;
		
		// Now start at the four corners. Actually, there are only two
		// distinct corners since the map wraps around East-West.
		h[0][0] = h[width-1][0] = Die.d20(2) + 30;
		h[0][height-1] = h[width-1][height-1] = Die.d20(2) + 30;
		
		// HACK:
		//h[0][0] = h[width-1][0] = 50;
		//h[0][height-1] = h[width-1][height-1] = 50;
		
		//System.out.println(h[0][0]);
		
		// Now run the fractal algorithm.
		int		step = width-1;
		while (step > 1) {
			for (int x = 0; x < width-1; x += step) {
				int		ystep = (step / 2); // Calculated here to make code cleaner later.
				
				for (int y = 0; y < height-1; y += ystep) {
					// Skip over tiles which have already been set.
					//if (h[x][y] != -1) continue;
					
					//System.out.println("("+x+","+y+") Step "+step);
					
					// SQUARE STEP
					// Generate a value for the middle of the square.
					int		px = x + (step/2);
					int		py = y + (step/4);
					
					//if (px >= width || px < 0) continue;
					//if (py >= height || py < 0) continue;
					
					int		total = 0;
					// Sum the four corners of the square.
					total = h[x][y];
					total += h[x+step][y];
					total += h[x][y+ystep]; 
					total += h[x+step][y+ystep];

					// Find the average.
					total /= 4;
					//System.out.println("Square: ("+px+","+py+"): "+total);

					// Add a random amount.
					int		random = 10;
					if (random < 0) random = 2;
					total += Die.die(random) - Die.die(random);
					if (total < 0) total = 0;
					if (total > 99) total = 99;
					if (h[px][py] == -1) {
						h[px][py] = total;
						//System.out.println(total);
					}
					
					// DIAMOND STEP
					// Generate values for the 4 vertices of the diamond.
					
					// Top vertex
					if (h[px][y] == -1) {
						total = h[px][py] + h[x][y] + h[x+step][y];
						h[px][y] = total/3 + Die.die(random) - Die.die(random);
					}
					// Left vertex
					if (h[x][py] == -1) {
						total = h[x][y] + h[px][py] + h[x][y+ystep];
						h[x][py] = total/3 + Die.die(random) - Die.die(random);
						if (x == 0) {
							h[width-1][py] = h[x][py];
						}
					}
					// Right vertex
					if (h[x+step][y] == -1) {
						total = h[x+step][y] + h[px][py] + h[x+step][y+ystep];
						h[x+step][py] = total/3 + Die.die(random) - Die.die(random);
					}
					// Bottom vertex
					if (h[px][y+ystep] == -1) {
						total = h[x][y+ystep] + h[px][py] + h[x+step][y+ystep];
						h[px][y+ystep] = total/3 + Die.die(random) - Die.die(random);
					}
				}  // y
			} // x
			step /= 2;
		}
		
		// Now set the real map.
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				setHeight(x, y, h[x][y]);
				setTerrain(x, y, land);
				heightBuckets[h[x][y]]++;
			}
		}		
		
	}
	
	protected void impacts(int number, int size, double force, Terrain terrain) {
		impacts(number, size, force, terrain, null);
	}
	
	/**
	 * Simulate asteroid bombardment by creating a number of random craters
	 * across the map of the world. Craters will have a specified depth, and
	 * should have a rim and random ejecta trails.
	 * 
	 * @param number		Number of craters to make.
	 * @param size			Average size of each crater.
	 * @param force			How deep is the hole? 0 = very deep, 1 = no impact.
	 * @param terrain		If non-null, set terrain to this.
	 */
	protected void impacts(int number, int size, double force, Terrain terrain, Terrain ejecta) {
		for (int i=0; i < number; i++) {
			int		 x = Die.rollZero(width);
			int		 y = (int)(Die.rollZero(height) * 0.9 + height * 0.05);
			int		 radius = Die.die(size, 2) / 2;
			
			for (int xx = x - radius; xx <= x + radius; xx++) {
				for (int yy = y - radius; yy <= y + radius; yy++) {
					int 		px = xx;
					int			py = yy;
					boolean		ridge = false;	
					
					if (py < 0 || py >= height) {
						continue;
					}

					int		d = (int)Math.sqrt( (x - px) * (x - px) + (y - py) * (y - py));
					// Give a fuzzy edge to the craters.)
					if (d == radius) {
						ridge = true;
					} else if (d > radius || Die.die(d,2) > radius) {
						continue;
					}
					
					// Wrap around west and east.
					if (px < 0) px += width;
					if (px >= width) px -= width;
					
					if (terrain != null) {
						setTerrain(px, py, terrain);
					}
					if (ridge && Die.d3()!=1) {
						setHeight(px, py, (int)(getHeight(px, py) / force));
						if (ejecta != null && Die.d3()!=1) {
							setTerrain(px, py, ejecta);
						}
					} else {
						setHeight(px, py, (int)(getHeight(px, py) * force));
					}
				}
			}
			
			if (ejecta != null && Die.die(radius) > 5 && Die.d2() == 1) {
				for (int e = 0; e < Math.pow(radius, 1.2); e++) {
					int		xx = x + Die.die(radius*10) - Die.die(radius*10);
					int		yy = y + Die.die(radius*10) - Die.die(radius*10);
					int		d = (int)(Math.sqrt((xx - x) * (xx - x) + (yy - y) * (yy - y)));
					
					for (int l=radius; l < d; l++) {
						int		px = x + ((xx - x) * l)/d;
						int		py = y + ((yy - y) * l)/d;
						
						if (px < 0) px += width;
						if (px >= width) px -= width;
						if (py < 0) break;
						if (py >= height) break;
						
						setHeight(px, py, (int)(getHeight(px, py) * force));
						setTerrain(px, py, ejecta);
						xx += Die.d2() - Die.d2();
						yy += Die.d2() - Die.d2();
					}
				}
			}
		}
	}
	
	protected void basicFlood(int hydrographics) {
		this.hydrographics = hydrographics;
		int[]	bucket = new int[100];
		
		// Perform a bucket sort. We need to know how many instances there
		// are of each possible height (0..99).
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				bucket[getHeight(x, y)]++;
			}
		}
		
		// Now flood fill the world.
		
		for (int h=99; h >= 0 && getHydrographics() > hydrographics; h--) {
			//System.out.println("Flooding world to a height of ["+h+"] at ["+getHydrographics()+"%]");
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					if (getHeight(x, y) > h && getTerrain(x, y) == sea) {
						setTerrain(x, y, land);
					} else {
						setTerrain(x, y, sea);
					}
				}
			}
		}
	}
	
	/**
	 * Flood the world with water. The percentage of the surface to be filled 
	 * is specified, and the number of tiles that need to be water to meet this
	 * percentage is calculated.
	 * 
	 * @param hydrographics		Percentage of surface that is water (0-100).
	 */
	protected void flood(int hydro) {
		hydrographics = hydro;
		
		System.out.println("flood:set hydro to ["+hydro+"%] from ["+getHydrographics()+"%]");

		int[]	bucket = new int[100];
		
		// Perform a bucket sort. We need to know how many instances there
		// are of each possible height (0..99).
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				bucket[getHeight(x, y)]++;
			}
		}
		
		// Now flood fill the world.
		
		for (int h=99; h >= 0 && getHydrographics() > hydro; h--) {
			//System.out.println("Flooding world to a height of ["+h+"] at ["+getHydrographics()+"%]");
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					if (getHeight(x, y) > h && getTerrain(x, y) == sea) {
						setTerrain(x, y, land);
					}
				}
			}
		}
		//System.out.println("Water "+maxWaterHeight+" height "+maxH);
		// Try and get rid of single tile islands etc.

		join(1); join(1);
		lineFill(3);

		/*
		// The previous join operations may have changed the hydrographics.
		// Try to fix it again.
		hydrographics = getHydrographics();
		int		diff = hydro - hydrographics;
		int		needed = (diff * width * height) / 100;
		
		while (needed > 0) {
			int		x = Die.rollZero(width);
			int		y = Die.rollZero(height);
			
			if (getWetness(x, y, false) > 20) {
				setHeight(x, y, 0);
				setTerrain(x, y, water);
				needed--;
			}
		}
		*/
	}
	
	protected void join(int size) {
		int[][]		h = new int[width][height];
		// Reset base map.
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				h[x][y] = getHeight(x, y);
				if (getTerrain(x, y) == sea) {
					h[x][y] = 0;
				}
			}
		}

		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				int		landCount = 0;
				int		total = 0;
				for (int xx=x-size; xx <= x+size; xx++) {
					for (int yy=y-size; yy <= y+size; yy++) {
						// Use xxx to represent wrapped around value.
						int			xxx = xx;
						if (xx < 0) xxx = xx+width;
						if (xx >= width) xxx = xx-width;
						
						if (yy > 0 && yy < height) {
							if (h[xxx][yy] > 0) {
								landCount++;
								total += h[xxx][yy];
							}
						}
					}
				}
				// Now, landCount is the number of tiles which are land.
				//int		average = total/landCount;
				if (landCount > 4 && getHeight(x, y) == 0) {
					int		average = total/landCount;
					setHeight(x, y, average);
					setTerrain(x, y, land);
				} else if (landCount < 4) {
					int		average = total/((size*2+1)*(size*2+1));
					setHeight(x, y, average);
					setTerrain(x, y, sea);
				}
			}
		}
	}
	
	protected void lineFill(int length) {
		for (int y=0; y < height; y++) {
			int		start = -1, count = 0;
			for (int x=0; x < width; x++) {
				if (getTerrain(x, y) == sea) {
					if (getTerrain(x, y-1) == sea || getTerrain(x, y+1) == sea) {
						count++;
					}
					if (start < 0) start = x;
				} else {
					if (start > -1 && count <= length) {
						for (; start <= x; start++) {
							// Change to land.
							setTerrain(start, y, land);
							// Set height to be closer to neighbouring land tile.
							setHeight(start, y, getHeight(x, y)+Die.d6()-Die.d6());
						}
					}
					start = -1;
					count = 0;
				}
			}
		}
	}
	
	/**
	 * Change heights so that they use the full range from 0 to 99. 0 is set to
	 * sea level, so any oceans are automatically given a height of 0.
	 */
	protected void normaliseHeights() {
		int		minHeight = 100;
		int		maxHeight = 0;
		
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				int h = getHeight(x, y);
				if (h < minHeight) minHeight = h;
				if (h > maxHeight) maxHeight = h;
			}
		}
		
		int		reduceBy = minHeight;
		double	scaleBy = 99.0 / (maxHeight - minHeight); 
	
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				if (getTerrain(x, y).isWater()) {
					setHeight(x, y, 0);
				} else {
					int h = getHeight(x, y);
					setHeight(x, y, (int)((h-reduceBy) * scaleBy));
				}
			}
		}
	}
	
	/**
	 * Get the latitude of the given row on the map. The latitude will be
	 * returned as a number between 0 and 90, and is always positive. It
	 * should be used to determine how far from the equator a point is.
	 * 
	 * @param y		Row on the map.
	 * @return		Absolute distance from equator in degrees, from 0 to 90.
	 */
	protected int getLatitude(int y) {
		return (int) Math.abs((-90 * ((y-(height/2.0)) / (height/2.0))));
	}
	
	/**
	 * Get a count of the number of water tiles at this longitude. This gives
	 * an indication of the amount of water in the area, for modifying the
	 * ecological profile.
	 * 
	 * @param longitude		X coordinate to check.
	 * @return
	 */
	protected int getLandCount(int longitude) {
		int		count = 0;
		
		for (int y=0; y < height; y++) {
			if (getTerrain(longitude, y) != sea) {
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * Get a count of the number of water tiles near to this tile. The
	 * value returned is between 0 and 100.
	 * 
	 * @param x				X coordinate to test.
	 * @param y				Y coordinate to test.
	 * @param ignoreSelf	If true, doesn't look at itself.
	 * 
	 * @return			Wetness, from 0 to 100.
	 */
	protected int getWetness(int x, int y, boolean ignoreSelf) {
		int		wetness = 0;
		
		// If this is a water tile, then totally wet.
		if (!ignoreSelf && getTerrain(x, y) == sea) {
			return 100;
		}
		
		for (int xx = x-2; xx <= x+2; xx++) {
			for (int yy = y-2; yy <= y+2; yy++) {
				if (yy < 0 || yy >= height) {
					continue;
				}
				// Wrap around the world.
				int		xxx = xx;
				if (xxx < 0) xxx += width;
				if (xxx >= width) xxx -= width;
				
				boolean		isWater = (getTerrain(xxx, yy).isWater());
				if (isWater) {
					if (xx == x-2 || xx == x+2 || yy == y-2 || yy == y+2) {
						// Two squares away.
						wetness += 3;
					} else {
						// Immediate neighbour.
						wetness += 5;
					}
				}
			}
		}
		
		return wetness;
	}
	
	protected void generateEcology() {
		int		latitude = 0;
		int		temperature = getTemperature();
		
		for (int y=0; y < height; y++) {
			latitude = getLatitude(y);
			for (int x=0; x < width; x++) {
				int			landCount = getLandCount(x);
				Terrain		terrain = null;
				
				if (Math.abs(latitude) + Math.sqrt(landCount) > (85 + Die.d6() - getHeight(x, y) + temperature*5)) {
					// Is it high enough latitude for snow and ice?
					terrain = land;
				} else if (getHeight(x, y) > 30 + temperature * 5) {
					terrain = land;
				} else if (getHeight(x, y) > 20) {
					terrain = land;
				} else 	if (getTerrain(x,y) == sea) {
					// Otherwise, if water then stay as water.
					terrain = sea;
				} else {
					// Now sort out the type of vegetation.
					int			fertility = hydrographics + getWetness(x, y, false)/2 - getHeight(x, y)/5 + landCount/5 -Math.abs(latitude)/5;
					fertility += temperature * 5;
					// Put some deserts in the tropics.
					int			tropical = 20 - Math.abs(Math.abs(latitude) - 25) - temperature*5;
					if (tropical > 0) fertility -= tropical*3;
					
					if (Math.abs(latitude) > 70) {
						fertility -= (Math.abs(latitude)-70);
					}
					
					switch (getLife()) {
					case None: case Metazoa: case Proteins: case Protozoa:
						fertility = 0;
						break;
					case ComplexOcean:
						fertility /= 5;
						if (fertility > 30) {
							fertility = 30;
						}
						break;
					case SimpleLand:
						fertility /= 2;
						break;
					default:
						// No change.
					}
					
					if (fertility < 20) {
						terrain = land;
						if (getHeight(x, y) > 70) {
							terrain = land;
						}
					} else if (fertility < 35) {
						terrain = land;
					} else if (fertility < 50) {
						terrain = land;
					} else {
						terrain = land;
					}
/*					
					System.out.println(fertility);
					terrain = Terrain.Grass;
					if (Math.abs(latitude) < 2 + fertility) {
						terrain = Terrain.Forest;
					} else if (Math.abs(latitude) < 40 + Die.d6() - (hydrographics/5)) {
						terrain = Terrain.Desert;
					}
 */
				}
				setTerrain(x, y, terrain);
			}
		}
	}

	private Image createImage(int width, int height, URL url) throws MalformedURLException {
        Image       image = null;
        
        //System.out.println(url);
        image = Toolkit.getDefaultToolkit().getImage(url);
        
        MediaTracker    tracker = new MediaTracker(new Container());
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            
        }
        image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        tracker.addImage(image, 1);
        try {
            tracker.waitForID(1);
        } catch (Exception e) {
            
        }
        
        return image;        
    }
	
	private static Hashtable<String,Image>		imageCache = new Hashtable<String,Image>();
	
	private Image getImage(String name, int scale) throws MalformedURLException {
		Image		img = imageCache.get(name);
		
		if (img != null) {
			// Have cached version of image, just return that.
			return img;
		} else if (name.startsWith("#")) {
			// Create image based on hex code.
			img = SimpleImage.createImage(scale, scale, name);
		} else {
			// Load image from file.
			String		baseImageDir = "file:/home/sam/src/traveller/webapp/images/terrain/";
			
			if (img == null) {
				img = createImage(scale, scale, new URL(baseImageDir+name+".png"));
			}
		}
		imageCache.put(name, img);
		return img;
	}
	
	public SimpleImage	getWorldMap(int scale) throws MalformedURLException {
		SimpleImage					image = new SimpleImage(width*scale, height*scale);
		
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				String		name = getTerrain(x, y).getImage(getHeight(x, y));
				Image		i = getImage(name, scale);
            	image.paint(i, x*scale, y*scale, scale, scale);
			}
		}

		return image;
	}
	

	
	/**
	 * Gets an image of the world as a 3D globe.
	 */
	public SimpleImage getWorldGlobe(int scale) throws MalformedURLException {
		SimpleImage					image = getWorldMap(scale);
		
		
		//image = new SimpleImage(new File("/home/sam/appleseed.jpg"));
		
		Canvas3D			canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration(), true);
		canvas.getScreen3D().setPhysicalScreenHeight(0.5);
		canvas.getScreen3D().setPhysicalScreenWidth(0.5);
		canvas.getScreen3D().setSize(500, 500);
		
		System.out.println("Width ["+canvas.getScreen3D().getPhysicalScreenWidth()+"] Height ["+canvas.getScreen3D().getPhysicalScreenHeight()+"]");

		SimpleUniverse		universe = new SimpleUniverse(canvas);
		Appearance			app = new Appearance();
		BranchGroup			root = new BranchGroup();
		
		TextureLoader		loader = new TextureLoader(image.getBufferedImage());

		BoundingSphere bounds = new BoundingSphere (new Point3d (0, 0.0, 5), 5.0);
		Color3f lightColour = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f lightDirection = new Vector3f(0.0f, 0.0f, -1f);
		DirectionalLight light1 = new DirectionalLight(lightColour, lightDirection);
		light1.setInfluencingBounds(bounds);
		root.addChild(light1);
		
	    AmbientLight ambientLightNode = new AmbientLight (lightColour);
	    ambientLightNode.setInfluencingBounds (bounds);
	    root.addChild (ambientLightNode);

		app.setTexture(loader.getTexture());
		app.setTextureAttributes(new TextureAttributes(TextureAttributes.MODULATE, new Transform3D(), new Color4f(), TextureAttributes.NICEST));

		//Material	mat = new Material();
		//mat.setEmissiveColor(1.0f, 1.0f, 1.0f);
		//app.setMaterial(mat);
		
		Sphere sphere = new Sphere(0.8f, Sphere.GENERATE_TEXTURE_COORDS | Sphere.GENERATE_NORMALS, 100);
		sphere.setAppearance(app);
		root.addChild(sphere);
		
		root.compile();
		universe.getViewingPlatform().setNominalViewingTransform();
		universe.addBranchGraph(root);
		
		//Canvas3D canvas = universe.getCanvas();

		ImageComponent2D buffer = new ImageComponent2D(ImageComponent2D.FORMAT_RGB, new SimpleImage(500,500).getBufferedImage());
		System.out.println("Width ["+canvas.getScreen3D().getPhysicalScreenWidth()+"] Height ["+canvas.getScreen3D().getPhysicalScreenHeight()+"]");
		canvas.setOffScreenBuffer(buffer);
		canvas.renderOffScreenBuffer();
		canvas.waitForOffScreenRendering();
		buffer = canvas.getOffScreenBuffer();
		if (buffer == null) {
			System.out.println("No off screen buffer");
		}
		Image img = buffer.getImage();
		universe.cleanup();

		//return image;
		return new SimpleImage(img);
	}

	
	public int getHydrographics2() {
		int		hydro = 0;
		double	waterArea = 0.0;
		double	surfaceArea = 0.0;
		
		for (int y=0; y < height; y++) {
			int		latitude = getLatitude(y);
			double 	area = 1.0;
			
			double	r = 10;
			double	radians = Math.toRadians(latitude);
			double	cos = Math.cos(radians);
			double	sin = Math.sin(radians);
			
			// Work out the radius. All we need at this point is a relative
			// value, so we don't need to convert to a circumference.
			area = Math.sqrt(((r*r*cos)*(r*r*cos) + (r*r*sin)*(r*r*sin))/((r*cos)*(r*cos) + (r*sin)*(r*sin)));
			
			surfaceArea += area * width;
			for (int x=0; x < width; x++) {
				if (getTerrain(x, y).isWater()) {
					waterArea += area;
				}
			}
		}
		
		hydro = (int)((100 * waterArea) / surfaceArea);
		
		return hydro;
	}

	
	/**
	 * Get the percentage of the planet's surface which is covered by water.
	 * Does a very dumb calculation to account for the roundness of the world,
	 * simply by weighting rows near the equator more than rows near the poles.
	 * 
	 * @return		Hydrographics percentage, from 0 to 100.
	 */
	public int getHydrographics() {
		int		hydro = 0;
		int		total = 0;
		
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				int		inc = y+1;				
				if (y > height/2) {
					inc = height-y;
				}
				total += inc;
				if (getTerrain(x, y) == sea) {
					hydro += inc;;
				}
			}
		}
		
		return (hydro*100)/(total);
	}
	
	public void generate() {
		flood(70);
		generateEcology();
	}
	
	public void setPlanet(Planet planet) {
		this.planet = planet;
	}
	
	/*
	public static WorldBuilder getBuilder(Planet planet, int width, int height) {
		WorldBuilder		wb = getBuilder(planet.getType(), width, height);
		wb.generate();
		wb.setPlanet(planet);
		
		return wb;
	}
	*/
	
	public static WorldBuilder getBuilder(Planet planet, int width, int height) {
		WorldBuilder		wb = null;
		PlanetType			type = planet.getType();
		
		System.out.println("Building ["+planet.getType()+"]");
		
		switch (type) {
		case Vulcanian:
		case Silicaceous:
		case Sideritic:
		case Basaltic:
		case Carbonaceous:
		case Enceladean:
		case Mimean:
		case Oortean:
			// Asteroids, small moons and comets.
			wb = new Asteroid(planet, width, height);
			break;
		case Selenian:
		case Hermian:
		case Ferrinian:
		case Hadean:
		case Vestian:
		case Cerean:
		case Kuiperian:
		case Hephaestian:
			// Typical airless moons and dwarf planets.
			wb = new Barren(planet, width, height);
			break;
		case EuJovian:
		case SubJovian:
		case CryoJovian:
		case SuperJovian:
		case MacroJovian:
		case EpiStellarJovian:
			// Any Jovian worlds should be created here.
			wb = new Jovian(planet, width, height);
			break;
		case LithicGelidian:
		case Europan:
		case EuTitanian:
		case MesoTitanian:
		case TitaniLacustric:
		case Iapetean:
		case JaniLithic:
		case Tritonic:
			wb = new IceWorld(planet, width, height);
			break;
		case Gaian:
		case EoGaian:
		case ArchaeoGaian:
		case GaianTundral:
		case MesoGaian:
		case PostGaian:
			wb = new Gaian(width, height);
			break;
		case Arean:
		case EoArean:
			break;
		default:
			throw new IllegalArgumentException("Unrecognised "+type);
		}
		wb.setPlanet(planet);
		
		return wb;
	}
	
	public static void makeAll() throws Exception {
		for (PlanetType type : PlanetType.values()) {
			System.out.println(type);
			try {
				Planet			planet = new Planet("Example", type, type.getRadius());
				WorldBuilder	wb = WorldBuilder.getBuilder(planet, 512+1, 256+1);
				if (wb != null) {
					wb.generate();
					wb.getWorldMap(2).save(new File("/home/sam/tmp/maps/types/"+type.toString()+".jpg"));
				}
			} catch (Throwable e) {
				continue;
			}
		}
		
	}

	public static void createPlanetType(PlanetType type) throws Exception {
		Planet			planet = new Planet("Example", type, type.getRadius());
		WorldBuilder	wb = WorldBuilder.getBuilder(planet, 513, 257);
		wb.generate();
		SimpleImage		image = wb.getWorldMap(2);
		image.save(new File("/home/sam/examplePlanet.jpg"));
	}
	
	public static void imageUniverse(int sectorId) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		Vector<StarSystem>	systems = factory.getStarSystemsBySector(sectorId);
		
		for (StarSystem system : systems) {
			for (Planet planet : factory.getPlanetsBySystem(system.getId())) {
				System.out.println(system.getName()+": "+planet.getName());
				WorldBuilder		wb = WorldBuilder.getBuilder(planet, 513, 257);
				wb.generate();
				SimpleImage		simple = wb.getWorldMap(2);
				factory.storePlanetMap(planet.getId(), simple.save());
				factory.storePlanetGlobe(planet.getId(), wb.getWorldGlobe(2).save());				
			}
		}
	}

	public static void imagePlanet(int planetId) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		Planet				planet = new Planet(factory, planetId);
		
		WorldBuilder		wb = WorldBuilder.getBuilder(planet, 513, 257);
		wb.generate();
		SimpleImage		simple = wb.getWorldMap(2);
		factory.storePlanetMap(planet.getId(), simple.save());
		factory.storePlanetGlobe(planet.getId(), wb.getWorldGlobe(2).save());				
	}
	
	public static void imagePlanet(ObjectFactory factory, Planet planet) throws Exception {
		WorldBuilder		wb = WorldBuilder.getBuilder(planet, 513, 257);
		wb.generate();
		SimpleImage		simple = wb.getWorldMap(2);
		factory.storePlanetMap(planet.getId(), simple.save());
		factory.storePlanetGlobe(planet.getId(), wb.getWorldGlobe(2).save());						
	}
	
	public static void exampleGlobes() throws Exception {
		for (PlanetType type : PlanetType.values()) {
			Planet		planet = new Planet("Example", type, type.getRadius());
			System.out.println(type);
			try {
				WorldBuilder	wb = WorldBuilder.getBuilder(planet, 512+1, 256+1);
				if (wb != null) {
					wb.generate();
					wb.getWorldGlobe(2).save(new File("/home/sam/tmp/maps/types/"+type.toString().toLowerCase()+".jpg"));
				}
			} catch (IllegalArgumentException e) {
				// Unsupported planet type.
			}
		}		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(GraphicsEnvironment.isHeadless());
		
		//createPlanetType(PlanetType.Hephaestian);
		ObjectFactory		factory = new ObjectFactory();
		Vector<Planet>		planets = factory.getPlanetsBySystem(10373);
		
		for (Planet planet : planets) {
			imagePlanet(factory, planet);
		}
		factory.close();
		//imageUniverse(38);
		//exampleGlobes();
		System.exit(0);
				
		//int	i = 1;
		for (Planet planet : planets) {
			if (planet.getId() == 68637) {
				System.out.println("Planet ["+planet.getName()+"]");
				//planet.setTemperature(Temperature.Standard);
				//planet.setAtmospherePressure(AtmospherePressure.Standard);
				//planet.setHydrographics(70);
				//planet.setLifeLevel(LifeType.Extensive);
				//planet.setTilt(10);
				for (SpectralType type : SpectralType.values()) {
					if (type.toString().endsWith("0")) {
						WorldBuilder wb = new Star(513, 257, type);
						wb.generate();
						wb.getWorldGlobe(2).save(new File("/home/sam/star_"+type.toString()+".jpg"));
					}
				}
				if (true) break;
				
				//planet.addFeature(PlanetFeature.ManyIslands);
				for (int i=0; i < 1; i++) {
					WorldBuilder	wb = WorldBuilder.getBuilder(planet, 512+1, 256+1);
					System.out.println("Builder ["+wb.getClass().getName()+"]");
					if (wb != null) {
						wb.generate();
						SimpleImage		simple = wb.getWorldMap(2);
						factory.storePlanetMap(planet.getId(), simple.save());
						factory.storePlanetGlobe(planet.getId(), wb.getWorldGlobe(2).save());
						//wb.getWorldGlobe(2).save(new File("/home/sam/"+planet.getId()+"-"+planet.getType()+".jpg"));
						/*
						SimpleImage	simple = wb.getWorldMap(1);
						Image		image = simple.getImage();
						System.out.println("Open");
						ViewCanvas	canvas = new ViewCanvas(image);
						System.out.println("Done");
						//wb.getWorldMap(2).save(new File("/home/sam/tmp/maps/types/"+planet.getSystemId()+"_"+(i)+".jpg"));
						 */
					}
				}
			}
		}
		
		/*
		for (int i=0; i < 10; i++) {
			new Random().setSeed(1);
			WorldBuilder		wb = new Hephaestian(512+1, 256+1);
			wb.setTemperature(0);
			wb.setLifeType(LifeType.SimpleLand);
			wb.generate();
			wb.getWorldMap(2).save(new File("/home/sam/tmp/maps/worldmap_"+i+".jpg"));
			System.out.println("Hydro: "+(70)+" "+wb.getHydrographics());
		}
		*/
	}
}
