package uk.org.glendale.mapcraft.graphics;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.map.NamedPlace;
import uk.org.glendale.mapcraft.map.Sector;
import uk.org.glendale.mapcraft.map.Terrain;
import uk.org.glendale.mapcraft.map.Tile;


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
	private double		scale = 1.00;
	
	public enum Scale {
		LARGE,   // Normal
		MEDIUM,  // Individual tiles
		SMALL,   // 4 tiles
		XSMALL,  // Sub-sector
		XXSMALL, // Sector
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
	
	public void setScale(double scale) {
		this.scale = scale;
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
		image = new SimpleImage((int)(scale*(width*49+25)), (int)(scale * (height*54+54)), "#FFFFFF");

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
				try {
					int		px = (int)(scale * ((x-orgX)*49));
					int		py = (int)(scale * ((y-orgY)*54+(x%2)*27));
					Image	i = getIcon(map.getInfo().getTerrain(map.getTerrain(x, y)));
					image.paint(i, px, py, (int)(scale*65), (int)(scale*65));
					
					if (map.getFeature(x, y) > 0) {
						i = getIcon(map.getInfo().getFeature(map.getFeature(x, y)));
						image.paint(i, px, py, (int)(scale*65), (int)(scale*65));
					}
				} catch (Throwable e) {
				}
			}
			// Odd columns (which can overlap higher, even, columns.
			for (int x=orgX+1-(bleeding?2:0); x < orgX+width+(bleeding?2:0); x+=2) {
				try {
					Image	i = getIcon(map.getInfo().getTerrain(map.getTerrain(x, y)));
					int		px = (int)(scale * ((x-orgX)*49));
					int		py = (int)(scale * ((y-orgY)*54+(x%2)*27));
					image.paint(i, px, py, (int)(scale*65), (int)(scale*65));

					if (map.getFeature(x, y) > 0) {
						i = getIcon(map.getInfo().getFeature(map.getFeature(x, y)));
						image.paint(i, px, py, (int)(scale*65), (int)(scale*65));
					}
				} catch (Throwable e) {
				}
			}
		}
		
		try {
			List<NamedPlace> places = map.getInfo().getNamedPlaces(orgX+1-(bleeding?2:0), orgY-(bleeding?1:0), orgX+width+(bleeding?2:0), orgY+height+(bleeding?1:0));
			
			for (NamedPlace place : places) {
				System.out.println(place.getName()+" ("+place.getX()+","+place.getY()+")");
				Image	i = getIcon(map.getInfo().getThing(place.getThingId()));
				System.out.println(place.getX()-orgX);
				System.out.println(place.getY()-orgY);
				int		px = (int)(scale * ((place.getX()-orgX)*49));
				int		py = (int)(scale * ((place.getY()-orgY)*54+(place.getX()%2)*27));
				image.paint(i, px, py, (int)(scale*65), (int)(scale*65));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Draw an overview map which uses flat tiles to represent multiple hexes.
	 * 
	 * @param size
	 * @throws IOException
	 */
	public void drawOverviewMap(int size) throws IOException {
		drawOverviewMap(0, 0, map.getInfo().getWidth(), map.getInfo().getHeight(), size, Scale.XXSMALL);
	}
	
	public void drawOverviewMap(int orgX, int orgY, int width, int height, int size, Scale scale) throws IOException {
		int		sectorWidth = size * 4;
		int		sectorHeight = size * 5;
		
		
		
		image = new SimpleImage(sectorWidth * width / Sector.WIDTH, 
								sectorHeight * height / Sector.HEIGHT, "#FFFFFF");

		for (int y=orgY; y < orgY+height; y+= Sector.HEIGHT) {
			if (y%10 == 0) System.out.println(y);
			for (int x=orgX; x < orgX+width; x+=Sector.WIDTH) {
				try {
					int		px = ((x-orgX) * sectorWidth) / Sector.WIDTH;
					int		py = ((y-orgY) * sectorHeight) / Sector.HEIGHT;
					Terrain	t = map.getInfo().getTerrain(map.getTerrain(x, y));
					Image	i = image.createImage(sectorWidth, sectorHeight, t.getColour());
					image.paint(i, px, py, sectorWidth, sectorHeight);					
				} catch (Throwable e) {
				}
			}
		}		
	}

	
	public static void main(String[] args) throws Exception {
		//makeHex(64);
		
		//MapSector		map = new MapSector(new File("/home/sam/src/mapcraft/mapcraft/WebContent/webapp/images/map/style/paper"));
		//map.drawSector(32, 40);
	}
}
