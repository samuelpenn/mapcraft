/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/01/01 11:04:14 $
 */
package uk.org.glendale.rpg.traveller.map;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import sun.font.Font2D;
import sun.font.FontManager;
import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.traveller.Log;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.Star;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.traveller.systems.codes.*;

/**
 * Create a bitmap image of a subsector map. Designed for display on a webpage,
 * to allow a scrollable view. Also designed to have varying levels of detail.
 * The maps are supposed to stitch together, so edges can appear truncated since
 * these hexes will be half drawn on the neighbouring map. 
 * 
 * @author Samuel Penn
 *
 */
public class SubSectorImage {
	private int				sectorId = 0;
	private int				ssx = 0;
	private int				ssy = 0;
	
	private SimpleImage		image = null;
	private int				scale = 32;
	private int				verbosity = 0;

	// Some simple constants.
	static final double COS30 = Math.sqrt(3.0)/2.0;
	static final double COS60 = 0.5;
	static final double SIN60 = Math.sqrt(3.0)/2.0;
	static final double SIN30 = 0.5;
	static final double ROOT_TWO = Math.sqrt(2.0);
	
	enum HexSides { Top, TopRight, BottomRight, Bottom, BottomLeft, TopLeft };
	
	static String SYMBOL_BASE = "file:/home/sam/src/traveller/webapp/images/symbols/";
	
	private int		leftMargin = 0;
	private int		topMargin = 0;
	
	public static void setSymbolBase(String base) {
		System.out.println("setSymbolBase: ["+base+"]");
		SYMBOL_BASE = base;
	}
	
	public enum SubSector {
		A(0, 0), B(1, 0), C(2, 0), D(3, 0),
		E(0, 1), F(1, 1), G(2, 1), H(3, 1),
		I(0, 2), J(1, 2), K(2, 2), L(3, 2),
		M(0, 3), N(1, 3), O(2, 3), P(3, 3);
		
		private int		x = 0;
		private int		y = 0;
		
		private SubSector(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX() { return x; }
		public int getY() { return y; }
		
	}
	
	
	/**
	 * Create a new SubSectorImage for the given sector and sub sector
	 * coordinates. Coordinates are from top-left corner of the sector.
	 * 
	 * @param SectorId		Id of the sector to draw subsector map for.
	 * @param x			X coordinate of subsector (0-3).
	 * @param y			Y coordinate of subsector (0-3).
	 */
	public SubSectorImage(int sectorId, int x, int y) {
		this.sectorId = sectorId;
		this.ssx = x;
		this.ssy = y;
	}
	
	public SubSectorImage(int sectorId, SubSector subSector) {
		this.sectorId = sectorId;
		this.ssx = subSector.getX();
		this.ssy = subSector.getY();
	}
	
	/**
	 * Set the scale of the maps to be generated. This is the width
	 * of each hex. 64 gives a good sized hex, 48 is medium and
	 * 32 is considered small.
	 * 
	 * @param scale	Size of the map.
	 */
	public void setScale(int scale) {
		this.scale = scale;
	}
	
	public int getScale() {
		return scale;
	}

	/**
	 * Get actual coordinate of the hexagon specified by x and y index.
	 * 
	 * @param x		X index of hexagon.
	 * @param y		Y index of hexagon.
	 * @return			X coordinate of top left of hexagon.
	 */
	public double getX(int x, int y) {
		return leftMargin + ((x-1)*(scale * 1.5));
	}

	/**
	 * Get actual coordinate of the hexagon specified by x and y index.
	 * 
	 * @param x		X index of hexagon.
	 * @param y		Y index of hexagon.
	 * @return			Y coordinate of top left of hexagon.
	 */
	public double getY(int x, int y) {
		return (topMargin + (SIN60*2*scale) +  (Math.abs(x-1)%2)*(scale*SIN60) + (y-1)*(SIN60*2*scale));
	}
	
	/**
	 * Angles:
	 *  x = x * cos(a) - y * sin(a)
	 *  y = x * sin(a) + y * cos(a)
	 */
	private void plotHexagon(double ox, double oy, EnumSet<HexSides> flags) {
		double 		x = ox, y = oy;
		double		topLeft_x, top_y, topRight_x, right_x, middle_y, bottom_y, left_x;
		double		size = scale;
		
		// Work out basic positions.
		topLeft_x = x;
		top_y = y;
		topRight_x = x + size;
		right_x = topRight_x + (size * COS60 - 0 * SIN60);
		middle_y = y - (size * SIN60 + 0 * COS60);
		bottom_y = y - 2 * (size * SIN60 + 0 * COS60);
		left_x = x - (size * COS60 - 0 * SIN60);
		
		String		normal = "#000000";
		String		emphasis = "#FF0000";
		float		width = (float)(scale / 64.0);
		float		emWidth = (float)(scale / 16.0);;

		// Now draw the hexagon. The hexagon is actually upside down, so the
		// Top/Bottom flags aren't quite what you'd expect them to be.
		image.line(topLeft_x, top_y, topRight_x, top_y, (flags.contains(HexSides.Bottom)?emphasis:normal), (flags.contains(HexSides.Bottom)?emWidth:width));
		image.line(topRight_x, top_y, right_x, middle_y, (flags.contains(HexSides.BottomRight)?emphasis:normal), flags.contains(HexSides.BottomRight)?emWidth:width);
		image.line(right_x, middle_y, topRight_x, bottom_y, (flags.contains(HexSides.TopRight)?emphasis:normal), flags.contains(HexSides.TopRight)?emWidth:width);
		image.line(topRight_x, bottom_y, topLeft_x, bottom_y, (flags.contains(HexSides.Top)?emphasis:normal), flags.contains(HexSides.Top)?emWidth:width);
		image.line(topLeft_x, bottom_y, left_x, middle_y, (flags.contains(HexSides.TopLeft)?emphasis:normal), flags.contains(HexSides.TopLeft)?emWidth:width);
		image.line(left_x, middle_y, topLeft_x, top_y, (flags.contains(HexSides.BottomLeft)?emphasis:normal), flags.contains(HexSides.BottomLeft)?emWidth:width);		
	}
	
	private void plotText(double x, double y, String text, int style, int size, String colour) {
		image.text((int)x, (int)y, text, style, size, colour);
	}
	
	private void plotText(double x, double y, int hx, int hy) {
		String		text = "";
		
		//if (hx == 8) return;
		
		if (hx < 1) {
			text += 32 + hx;
			return;
		} else if (hx < 10) {
			text += "0" + hx;
		} else {
			text += hx;
		}

		if (hy < 1) {
			text += 40 + hy;
			return;
		} else if (hy < 10) {
			text += "0" + hy;
		} else {
			text += hy;
		}
		
		plotText(x + scale*0.1, y - scale*1.4, text, 0, (int)(scale * 0.3), "#000000");
	}
	
	/**
	 * Draw a subsector map onto an image buffer. The basic hexagons are drawn,
	 * together with all the star systems and related data.
	 * 
	 * @throws ObjectNotFoundException
	 */
	private void drawBaseMap() throws ObjectNotFoundException {
		int		hexWidth = (int)(scale + 1.0 * scale * COS60);
		int		hexHeight = (int)(2.0 * scale * SIN60);
		//image = new SimpleImage(hexWidth * 8 + (int)(scale * COS60), hexHeight * 10, "FFFFFF");
		image = new SimpleImage(hexWidth * 8, hexHeight * 10 + (int)(scale * SIN30 * 0.25), "FFFFFF");
		
		Log.info("Draw map for ["+sectorId+"]");
		
		leftMargin = (int)(scale * 0.5);
		topMargin = 0;
		
		int		baseX = ssx*8;
		int		baseY = ssy*10;
		
		ObjectFactory		factory = new ObjectFactory();
		StarSystem			system = null;
		
		// In case we need to display surrounding sectors, work out where we
		// are in the galaxy.
		Sector				sector = new Sector(factory, sectorId);
		int					sectorX = sector.getX();
		int					sectorY = sector.getY();
		
		for (int x = -1; x < 9; x++) {
			for (int y = -1; y < 12; y++) {
				int			id = sectorId;
				int			sx = baseX + x;
				int			sy = baseY + y;
				EnumSet<HexSides>	flags = EnumSet.noneOf(HexSides.class);
				
				if (sx == 1) {
					flags.add(HexSides.TopLeft);
					flags.add(HexSides.BottomLeft);
				}
				if (sy == 1) {
					flags.add(HexSides.Top);
					if (sx%2 != 0) {
						flags.add(HexSides.TopLeft);
						flags.add(HexSides.TopRight);
					}
				}
				if (sx == 32) {
					flags.add(HexSides.TopRight);
					flags.add(HexSides.BottomRight);
				}
				if (sy > 39) {
					flags.add(HexSides.Bottom);
					if (sx%2 == 0) {
						flags.add(HexSides.BottomLeft);
						flags.add(HexSides.BottomRight);
					}
				}
//				if (flags.contains(HexSides.TopRight)) Log.info("Got TopRight at "+sx+","+sy);

				plotHexagon(getX(x, y), getY(x, y), flags);
				plotText(getX(x, y), getY(x, y), baseX+x, baseY+y);		

				try {
					if (sx < 1 && sy < 1) {
						// Top left
						sector  = new Sector(factory, sectorX - 1, sectorY - 1);
						id = sector.getId();
						sx += 32;
						sy += 40;
					} else if (sx < 1) {
						// Left
						sector  = new Sector(factory, sectorX - 1, sectorY);
						id = sector.getId();
						sx += 32;
					} else if (sy < 1) {
						// Top
						sector  = new Sector(factory, sectorX, sectorY - 1);
						id = sector.getId();
						sy += 40;
					}
				} catch (ObjectNotFoundException e) {
					// There isn't a neighbouring sector, so skip.
					continue;
				}

				system = factory.getStarSystem(id, sx, sy);
				if (system != null) {
					//System.out.println((baseX+x)+","+(baseY+y)+": "+system.getName()+" ("+x+","+y+")");
					int		cx = (int)(getX(x, y) + scale*0.5); 		// X coordinate of centre.
					int		cy = (int)(getY(x, y) - (scale * SIN60));	// Y coordinate of centre.
					
					Vector<Star>		stars = system.getStars();
					Star				star = null;
					switch (stars.size()) {
					case 1:
						star = stars.elementAt(0);
						image.circle(cx, cy, (int)(star.getSize() * scale * 0.25), star.getColour());
			            break;
			        case 2:
						star = stars.elementAt(0);
						image.circle(cx - (int)(scale * 0.15), cy, (int)(star.getSize() * scale * 0.25), star.getColour());
						star = (Star)(stars.elementAt(1));
						image.circle(cx + (int)(scale * 0.15), cy, (int)(star.getSize() * scale * 0.25), star.getColour());
			            break;
			        case 3:
						star = stars.elementAt(0);
						image.circle(cx, cy + (int)(scale * 0.10), (int)(star.getSize() * scale * 0.25), star.getColour());
						star = (Star)(stars.elementAt(1));
						image.circle(cx + (int)(scale * 0.15), cy - (int)(scale * 0.10), (int)(star.getSize() * scale * 0.25), star.getColour());
						star = (Star)(stars.elementAt(2));
						image.circle(cx - (int)(scale * 0.15), cy - (int)(scale * 0.10), (int)(star.getSize() * scale * 0.25), star.getColour());
						break;
					}

					String	colour = "#000000";
					if (system.getZone() == StarSystem.Zone.Red) {
						colour = "#FF0000";
					} else if (system.getZone() == StarSystem.Zone.Amber) {
						colour = "#FF8000";
					}
					try {
						int				iconSize = (scale / 4);
						Planet			mainWorld = system.getMainWorld();
						
						if (mainWorld != null && mainWorld.getPopulation() > 0) {
							StarportType	starport = mainWorld.getStarport();
							int				tl = mainWorld.getTechLevel();
							if (starport != StarportType.X) {
								double	fontSize = scale * 0.2;
								plotText(getX(x, y) + (scale * 0.30), getY(x, y) - scale*1.1, starport.toString()+"/"+tl, Font.PLAIN, (int)fontSize, "#000000");
							} else {
								double	fontSize = scale * 0.2;
								plotText(getX(x, y) + (scale * 0.30), getY(x, y) - scale*1.1, "-/"+tl, Font.PLAIN, (int)fontSize, "#000000");							
							}
							if (system.hasLife(LifeType.Extensive)) {
								image.paint(new URL(SYMBOL_BASE+"life_extensive.png"), (int)(cx + scale * 0.4), (int)(cy - scale * 0.4), iconSize, iconSize);
							} else if (system.hasLife(LifeType.ComplexLand)) {
								image.paint(new URL(SYMBOL_BASE+"life_land.png"), (int)(cx + scale * 0.4), (int)(cy - scale * 0.4), iconSize, iconSize);
							} else if (system.hasLife(LifeType.ComplexOcean)) {
								image.paint(new URL(SYMBOL_BASE+"life_water.png"), (int)(cx + scale * 0.4), (int)(cy - scale * 0.4), iconSize, iconSize);
							}
							
							String	gov = mainWorld.getGovernment().getAbbreviation()+"/"+mainWorld.getLawLevel()+"/"+mainWorld.getShortPopulation();
							plotText(getX(x, y) + (scale * 0.10), getY(x, y) - scale*0.5, gov, Font.PLAIN, (int)(scale*0.2), "#000000");
						}
						if (system.hasWater(10)) {
							image.paint(new URL(SYMBOL_BASE+"planet_water.png"), (int)(cx + scale * 0.4), (int)(cy - scale * 0.6), iconSize, iconSize);
						}
						
						// Trade codes.
						TradeCode[]		codes = new TradeCode[] { TradeCode.In, TradeCode.Ni, TradeCode.Ag, TradeCode.Na,
																  TradeCode.Ri, TradeCode.Po, TradeCode.Hi, TradeCode.Lo, 
								                                  TradeCode.Cx, TradeCode.Cp };
						int		tradeX = (int)(cx - scale * 0.65);
						int		tradeY = (int)(cy - scale * 0.6);
						
						for (int tc = 0; tc < codes.length; tc++) {
							if (system.hasTradeCode(codes[tc])) {
								String		img = "trade_"+codes[tc].toString().toLowerCase()+".png";
								image.paint(new URL(SYMBOL_BASE+img), tradeX, tradeY, iconSize, iconSize);
								tradeY += iconSize + 1;
							}
						}
					} catch (MalformedURLException e) {
						Log.error(e);
					}
					
					double	fontSize = scale * 0.2;
					int		len = 0;
					
					try {
						len = image.getTextWidth(system.getName(), Font.BOLD, (int)fontSize);
					} catch (Throwable e) {
						Log.error(e);
					}
					
					plotText(getX(x, y) + scale * 0.5 - (len/2), getY(x, y) - scale * 0.2, 
							 system.getName(), Font.BOLD, (int)fontSize, colour);
				}
			}
		}
		
		factory.close();
	}
	
	public SimpleImage getImage() throws ObjectNotFoundException {
		drawBaseMap();
		return image;
	}
		
	public static void main(String[] args) throws Exception {
		Log.init(3);
		Log.info("Hello");
		//int		id = 1; // Full Thrust
		int		id = 89; // Traveller;
		for (SubSector ss : SubSector.values()) {
			SubSectorImage		sub = new SubSectorImage(id, ss);
			sub.setScale(128);
			sub.getImage().save(new File("/home/sam/tmp/subsector/ss_"+ss.name()+".jpg"));
			break;
		}
		
	}

}
