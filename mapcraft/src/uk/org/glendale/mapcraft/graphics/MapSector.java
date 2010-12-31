package uk.org.glendale.mapcraft.graphics;

import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.map.NamedArea;
import uk.org.glendale.mapcraft.map.NamedPlace;
import uk.org.glendale.mapcraft.map.Rectangle;
import uk.org.glendale.mapcraft.map.Sector;
import uk.org.glendale.mapcraft.map.Terrain;
import uk.org.glendale.mapcraft.map.Tile;
import uk.org.glendale.mapcraft.server.database.MapTile;


/**
 * Handles the display of a map sector. Each sector is a set of
 * hex tiles 32x40 in size. A sector should be of a size that will
 * fit onto an A4 sheet of paper when printed.
 * 
 * @author Samuel Penn
 */
public class MapSector {
	private File		imageFolder = null;
	private Map			map = null;
	private boolean		bleeding = false;
	SimpleImage			image;
	private double		zoom = 1.00;
	private Scale		scale = Scale.STANDARD;
	private boolean		hideAsGrey = false;
	
	private Set<Integer>	allowedAreas = null;
	
	public enum Scale {
		STANDARD,   // Normal
		COMPACT,  // Individual tiles
		LARGE,   // 4 tiles
		SUBSECTOR,  // Sub-sector
		SECTOR, // Sector
	}
	
	public MapSector(Map map, File imageFolder) {
		this.map = map;
		this.imageFolder = imageFolder;
	}
	
	/**
	 * Set whether the map is drawn right to the edge of the drawable
	 * area. If false, edges of the map are 'ragged' - they will include
	 * whitespace around the hexagons. If true, the hexagons outside
	 * the selected draw area are also partially drawn, giving a straight
	 * edge to the map.
	 * 
	 * @param bleeding		True if map is drawn with straight edges.
	 */
	public void setBleeding(boolean bleeding) {
		this.bleeding = bleeding;
	}
	
	/**
	 * Set the scale of the map to be drawn. This controls the level of
	 * detail, and defaults to STANDARD.
	 * 
	 * @param scale		Scale to use when drawing map.
	 */
	public void setScale(Scale scale) {
		this.scale = scale;
	}
	
	/**
	 * Sets the scale of the map based on the current scale, and the size
	 * of the area to be drawn. If the area is too big, then the scale will
	 * be increased to keep the image size down.
	 * 
	 * @param dimension		Maximum size of the map, either width or height.
	 */
	private void setScale(int dimension) {
		
		// Note there are no break statements in the following.
		switch (scale) {
		case STANDARD:
			if (dimension > 40) {
				scale = Scale.COMPACT;
			}
		case COMPACT:
			if (dimension > 200) {
				scale = Scale.LARGE;
			}
		case LARGE:
			if (dimension > 800) {
				scale = Scale.SUBSECTOR;
			}
		case SUBSECTOR:
			if (dimension > 1600) {
				scale = Scale.SECTOR;
			}
		case SECTOR:
			break;
		}
	}
	
	public void setZoom(double zoom) {
		this.zoom = zoom;
	}
	
	public void save(File file) throws IOException {
		image.save(file);
	}
	
	private Image getIcon(Tile terrain) {
		File			file = new File(imageFolder.getAbsolutePath()+terrain.getPrefix()+terrain.getImage()+".png");
		if (!file.exists()) {
			System.out.println(file.getAbsolutePath());
		}
		SimpleImage		si = new SimpleImage(file);
		
		return si.getImage();
	}
	
	private Image getGreyIcon() {
		File			file = new File(imageFolder.getAbsolutePath()+"/effects/grey.png");
		if (!file.exists()) {
			System.out.println(file.getAbsolutePath());
		}
		SimpleImage		si = new SimpleImage(file);
		
		return si.getImage();		
	}
	
	public void drawMap(Sector sector) throws IOException {
		drawMap(sector.getOriginX(), sector.getOriginY(), sector.WIDTH, sector.HEIGHT);
	}

	/**
	 * Draw a map over the given area at full scale.
	 * 
	 * @param orgX
	 * @param orgY
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	public void drawMap(int orgX, int orgY, int width, int height) throws IOException {
		image = new SimpleImage((int)(zoom*(width*49+25)), (int)(zoom * (height*54+54)), "#FFFFFF");

		// Always start on an even column.
		orgX -= orgX%2;
		
		if (orgX < 0) {
			orgX = 0;
		}
		if (orgY < 0) {
			orgY = 0;
		}
		
		// Draw the tiles for this map. Because 'lower' tiles can overlap 'higher'
		// tiles, we must draw top rows first. Also, must do each row in two passes,
		// once for the even (higher) columns and once for the odd (lower) columns.
		for (int y=orgY-(bleeding?1:0); y < orgY+height+(bleeding?1:0); y++) {
			// Even columns.
			for (int x=orgX-(bleeding?2:0); x < orgX+width+(bleeding?2:0); x+=2) {
				int		px = (int)(zoom * ((x-orgX)*49));
				int		py = (int)(zoom * ((y-orgY)*54+(x%2)*27));

				if (allowedAreas != null && !allowedAreas.contains(map.getArea(x, y))) {
					if (hideAsGrey && map.getInfo().getTerrain(map.getTerrain(x, y)).getWater() < 100) {
						image.paint(getGreyIcon(), px, py, (int)(zoom*65), (int)(zoom*65));
					}
					continue;
				}
				try {
					Image	i = getIcon(map.getInfo().getTerrain(map.getTerrain(x, y)));
					image.paint(i, px, py, (int)(zoom*65), (int)(zoom*65));
					
					if (map.getFeature(x, y) > 0) {
						i = getIcon(map.getInfo().getFeature(map.getFeature(x, y)));
						image.paint(i, px, py, (int)(zoom*65), (int)(zoom*65));
					}
				} catch (Throwable e) {
				}
			}
			// Odd columns (which can overlap higher, even, columns.
			for (int x=orgX+1-(bleeding?2:0); x < orgX+width+(bleeding?2:0); x+=2) {
				int		px = (int)(zoom * ((x-orgX)*49));
				int		py = (int)(zoom * ((y-orgY)*54+(x%2)*27));
				if (allowedAreas != null && !allowedAreas.contains(map.getArea(x, y))) {
					if (hideAsGrey && map.getInfo().getTerrain(map.getTerrain(x, y)).getWater() < 100) {
						image.paint(getGreyIcon(), px, py, (int)(zoom*65), (int)(zoom*65));
					}
					continue;
				}
				try {
					Image	i = getIcon(map.getInfo().getTerrain(map.getTerrain(x, y)));
					image.paint(i, px, py, (int)(zoom*65), (int)(zoom*65));

					if (map.getFeature(x, y) > 0) {
						i = getIcon(map.getInfo().getFeature(map.getFeature(x, y)));
						image.paint(i, px, py, (int)(zoom*65), (int)(zoom*65));
					}
				} catch (Throwable e) {
				}
			}
		}
		
		try {
			List<NamedPlace> places = map.getInfo().getNamedPlaces(orgX+1-(bleeding?2:0), orgY-(bleeding?1:0), orgX+width+(bleeding?2:0), orgY+height+(bleeding?1:0));
			
			for (NamedPlace place : places) {
				System.out.println(place.getName()+" ("+place.getX()+","+place.getY()+")");
				if (allowedAreas != null && !allowedAreas.contains(map.getArea(place.getX(), place.getY()))) {
					continue;
				}
				Image	i = getIcon(map.getInfo().getThing(place.getThingId()));
				int		px = (int)(zoom * ((place.getX()-orgX)*49));
				int		py = (int)(zoom * ((place.getY()-orgY)*54+(place.getX()%2)*27));
				image.paint(i, px, py, (int)(zoom*65), (int)(zoom*65));
				image.text(px-40, py+80, place.getName(), Font.PLAIN, 16, "#000000");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void drawMap(NamedArea area, int borderSize) throws IOException {
		Rectangle	bounds = map.getInfo().getNamedAreaBounds(area);
		
		allowedAreas = new HashSet<Integer>();
		allowedAreas.add(area.getId());
		for (NamedArea a : map.getInfo().getChildAreas(area)) {
			allowedAreas.add(a.getId());
		}
		int		maxDimension = Math.max(bounds.getWidth(), bounds.getHeight());
		setScale(maxDimension + borderSize*2);
		
		hideAsGrey = true;
		if (scale == Scale.STANDARD) {
			drawMap(bounds.getX()-borderSize, bounds.getY()-borderSize, 
					bounds.getWidth()+borderSize*2, bounds.getHeight()+borderSize*2);
		} else {
			drawOverviewMap(bounds.getX()-borderSize, bounds.getY()-borderSize,
							bounds.getWidth()+borderSize*2, bounds.getHeight()+borderSize*2);
		}
	}

	/**
	 * Draw an overview map which uses flat tiles to represent multiple hexes.
	 * 
	 * @param size
	 * @throws IOException
	 */
	public void drawOverviewMap(int size) throws IOException {
		drawOverviewMap(0, 0, map.getInfo().getWidth(), map.getInfo().getHeight());
	}
		
	public void drawOverviewMap(int orgX, int orgY, int width, int height) throws IOException {
		int		pixelWidth = 4;
		int		pixelHeight = 5;
		int		xStep = 1;
		int		yStep = 1;
		int		minImportance = 3;
		
		switch (scale) {
		case STANDARD:
			minImportance = 1;
			break;
		case COMPACT:
			minImportance = 2;
			pixelWidth = 4;
			pixelHeight = 5;
			break;
		case LARGE:
			minImportance = 3;
			pixelWidth = 10;
			pixelHeight = 11;
			xStep = yStep = 4;
			break;
		case SUBSECTOR:
			minImportance = 4;
			pixelWidth = 4;
			pixelHeight = 5;
			xStep = 8;
			yStep = 10;
			break;
		case SECTOR:
			minImportance = 5;
			pixelWidth = 4;
			pixelHeight = 5;
			xStep = 32;
			yStep = 40;
			break;
		}
		
		
		image = new SimpleImage(pixelWidth * width / xStep, 
								pixelHeight * height / yStep, "#FFFFFF");
		
		Hashtable<String,Image> colourTiles = new Hashtable<String,Image>();
		colourTiles.put("#E0E0E0", SimpleImage.createImage(pixelWidth, pixelHeight, "#E0E0E0"));

		for (int y=orgY; y < orgY+height; y+= yStep) {
			if (y%10 == 0) System.out.println(y);
			for (int x=orgX; x < orgX+width; x+=xStep) {
				try {
					int		px = ((x-orgX) * pixelWidth) / xStep;
					int		py = ((y-orgY) * pixelHeight) / yStep;
					
					MapTile	tile = map.getData().getTile(x, y);
					
					if (allowedAreas != null && !allowedAreas.contains(tile.getAreaId())) {
						if (map.getInfo().getTerrain(tile.getTerrainId()).getWater() < 100) {
							if (hideAsGrey) {
								image.paint(colourTiles.get("#E0E0E0"), px, py, pixelWidth, pixelHeight);
							}
							continue;
						}
					}
					Terrain	t = map.getInfo().getTerrain(tile.getTerrainId());
					Image	i = colourTiles.get(t.getColour());
					if (i == null) {
						i = SimpleImage.createImage(pixelWidth, pixelHeight, t.getColour());
						colourTiles.put(t.getColour(), i);
					}
					image.paint(i, px, py, pixelWidth, pixelHeight);					
				} catch (Throwable e) {
				}
			}
		}

		try {
			List<NamedPlace> places = map.getInfo().getNamedPlaces(orgX, orgY, orgX+width, orgY+height);
			
			for (NamedPlace place : places) {
				if (place.getImportance() < minImportance) {
					continue;
				}
				int		px = ((place.getX()-orgX) * pixelWidth) / xStep;
				int		py = ((place.getY()-orgY) * pixelHeight) / yStep;
				
				System.out.println(place.getName()+" ("+place.getX()+","+place.getY()+")");
				if (allowedAreas != null && !allowedAreas.contains(map.getArea(place.getX(), place.getY()))) {
					continue;
				}
				Image	i = getIcon(map.getInfo().getThing(place.getThingId()));
				image.circle(px, py, 5, "#000000");
				image.text(px, py, place.getTitle(), Font.PLAIN, 12, "#000000");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) throws Exception {
		//makeHex(64);
		
		//MapSector		map = new MapSector(new File("/home/sam/src/mapcraft/mapcraft/WebContent/webapp/images/map/style/paper"));
		//map.drawSector(32, 40);
	}
}
