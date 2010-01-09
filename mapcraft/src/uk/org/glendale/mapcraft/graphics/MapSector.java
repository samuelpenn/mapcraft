package uk.org.glendale.mapcraft.graphics;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.mapcraft.map.Map;
import uk.org.glendale.mapcraft.map.Sector;
import uk.org.glendale.rpg.utils.Die;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Handles the display of a map sector. Each sector is a set of
 * hex tiles 32x40 in size. A sector should be of a size that will
 * fit onto an A4 sheet of paper when printed.
 * 
 * @author Samuel Penn
 */
public class MapSector {
	private File		imageFolder = null;
	
	
    private static BufferedImage getBufferedImage(Image image, int crop) {
        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(96, 96, type);
        }
    
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
    
        // Paint the image onto the buffered image
        g.drawImage(image, 0-crop, 0-crop, null);
        g.dispose();
    
        return bimage;
    }

    private static void saveAsJPEG(BufferedImage image, String filename) throws IOException {
        File          file = new File(filename);
        OutputStream  out = new BufferedOutputStream(new FileOutputStream(file));
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        encoder.encode(image);
    }
	
	private static void makeHex(int width) throws Exception {
        int  height = (int)((Math.sqrt(3)/2)*width);
        HexFilter	filter = new HexFilter(width);
        
        
        Toolkit		toolkit = Toolkit.getDefaultToolkit();
        
        String		filename = "/home/sam/src/mapcraft/mapcraft/application/images/hexagonal/standard/96x96/terrain/ocean.png";
        
        File		file = new File(filename);
    	if (!file.exists()) {
    		System.out.println("File not found");
    		return;
    	}
        SimpleImage	simple = new SimpleImage(file);
        ImageProducer  producer = new FilteredImageSource(simple.getImage().getSource(), filter);
        
        Image	image = toolkit.createImage(producer);

		MediaTracker tracker = new MediaTracker(new Container());
		tracker.addImage(image, 2);
		tracker.waitForID(2);
        saveAsJPEG(getBufferedImage(image, 0), "blank_hex.jpg");
        //saveAsJPEG(getBufferedImage(simple.getImage(), 0), "blank_hex.jpg");
        
        //iconSet.add(id, icon);
        //prepareImage(icon);

	}
	
	public MapSector(File imageFolder) {
		this.imageFolder = imageFolder;
	}
	
	private Image getIcon(String name) {
		File			file = new File(imageFolder.getAbsolutePath()+"/"+name+".png");
		SimpleImage		si = new SimpleImage(file);
		
		return si.getImage();
	}
	
	public void drawSector(int width, int height) throws IOException {
		SimpleImage		image = new SimpleImage(width*49+25, height*54+54, "#FFFFFF");
		
		String[]		icons = { "sea", "grass", "woods", "hills" };
		String[]		testMap = { "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
				                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" };
		
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x+=2) {
				Image	i = getIcon(icons[Die.d4()-1]);
				try {
					image.paint(i, x*49, y*54+(x%2)*27, 65, 65);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (int x=1; x < width; x+=2) {
				Image	i = getIcon(icons[Die.d4()-1]);
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
	
	public void drawSector(Map map) throws IOException {
		SimpleImage		image = new SimpleImage(Sector.WIDTH*49+25, Sector.HEIGHT*54+54, "#FFFFFF");
		
		String[]		icons = { "", "sea", "grass", "woods", "hills" };

		for (int y=0; y < Sector.HEIGHT; y++) {
			for (int x=0; x < Sector.WIDTH; x+=2) {
				Image	i = getIcon(icons[map.getTerrain(x, y)]);
				try {
					image.paint(i, x*49, y*54+(x%2)*27, 65, 65);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (int x=1; x < Sector.WIDTH; x+=2) {
				Image	i = getIcon(icons[map.getTerrain(x, y)]);
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
		
		MapSector		map = new MapSector(new File("/home/sam/src/mapcraft/mapcraft/WebContent/webapp/images/map/style/paper"));
		map.drawSector(32, 40);
	}
}
