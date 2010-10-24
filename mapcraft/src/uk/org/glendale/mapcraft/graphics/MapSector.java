package uk.org.glendale.mapcraft.graphics;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.map.Sector;
import uk.org.glendale.mapcraft.map.Terrain;


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
	
	public void save(File file) throws IOException {
		image.save(file);
	}
	
	private Image getIcon(Terrain terrain) {
		File			file = new File(imageFolder.getAbsolutePath()+"/"+terrain.getImage()+".png");
		SimpleImage		si = new SimpleImage(file);
		
		return si.getImage();
	}
	
	public void drawMap(Sector sector) throws IOException {
		drawMap(sector.getOriginX(), sector.getOriginY(), sector.WIDTH, sector.HEIGHT);
	}

	public void drawMap(int orgX, int orgY, int width, int height) throws IOException {
		image = new SimpleImage(width*49+25, height*54+54, "#FFFFFF");

		// Always start on an even column.
		orgX -= orgX%2;
		
		if (orgX < 0) {
			orgX = 0;
		}
		if (orgY < 0) {
			orgY = 0;
		}
		System.out.println(bleeding);
		
		for (int y=orgY-(bleeding?1:0); y < orgY+height+(bleeding?1:0); y++) {
			for (int x=orgX-(bleeding?2:0); x < orgX+width+(bleeding?2:0); x+=2) {
				try {
					Image	i = getIcon(map.getInfo().getTerrain(map.getTerrain(x, y)));
					image.paint(i, (x-orgX)*49, (y-orgY)*54+(x%2)*27, 65, 65);
				} catch (Throwable e) {
				}
			}
			for (int x=orgX+1-(bleeding?2:0); x < orgX+width+(bleeding?2:0); x+=2) {
				try {
					Image	i = getIcon(map.getInfo().getTerrain(map.getTerrain(x, y)));
					image.paint(i, (x-orgX)*49, (y-orgY)*54+(x%2)*27, 65, 65);
				} catch (Throwable e) {
				}
			}
		}
	}

	public void drawSector(Map map) throws IOException {
		SimpleImage		image = new SimpleImage(Sector.WIDTH*49+25, Sector.HEIGHT*54+54, "#FFFFFF");
		
		String[]		icons = { "", "sea", "grass", "hills", "woods" };

		for (int y=0; y < Sector.HEIGHT; y++) {
			for (int x=0; x < Sector.WIDTH; x+=2) {
				Image	i = getIcon(map.getInfo().getTerrain(map.getTerrain(x, y)));
				try {
					image.paint(i, x*49, y*54+(x%2)*27, 65, 65);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (int x=1; x < Sector.WIDTH; x+=2) {
				Image	i = getIcon(map.getInfo().getTerrain(map.getTerrain(x, y)));
				try {
					image.paint(i, x*49, y*54+(x%2)*27, 65, 65);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		image.save(new File("/home/sam/hexmap.jpg"));		
	}
	
	public static void main(String[] args) throws Exception {
		//makeHex(64);
		
		//MapSector		map = new MapSector(new File("/home/sam/src/mapcraft/mapcraft/WebContent/webapp/images/map/style/paper"));
		//map.drawSector(32, 40);
	}
}
