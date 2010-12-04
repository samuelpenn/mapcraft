package uk.org.glendale.mapcraft.map;

/**
 * Defines a rectangular area on the map. Stores the X and Y offset, plus
 * the width and height. Also includes helper methods for finding the bounds
 * of the rectangle.
 * 
 * @author Samuel Penn
 *
 */
public class Rectangle {
	private int		x, y, w, h;
	
	public Rectangle(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	/**
	 * Gets the X offset to the left edge of the rectangle.
	 * 
	 * @return	X offset.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Gets the Y offset to the top edge of the rectangle.
	 * 
	 * @return	Y offset.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Gets the width of the rectangle.
	 * 
	 * @return	Rectangle width.
	 */
	public int getWidth() {
		return w;
	}
	
	/**
	 * Gets the height of the rectangle.
	 * 
	 * @return	Rectangle height.
	 */
	public int getHeight() {
		return h;
	}
	
	/**
	 * Gets the Y coordinate of the northern (top) edge of the rectangle. This
	 * is the same as the Y offset.
	 * 
	 * @return		Y coordinate of northern edge.
	 */
	public int getNorth() {
		return y;
	}
	
	/**
	 * Gets the Y coordinate of the southern (bottom) edge of the rectangle.
	 * This is the same as the Y offset + height.
	 * 
	 * @return		Y coordinate of southern edge.
	 */
	public int getSouth() {
		return y+h;
	}
	
	/**
	 * Gets the X coordinate of the western (left) edge of the rectangle.
	 * This is the same as the X offset.
	 * 
	 * @return		X coordinate of western edge.
	 */
	public int getWest() {
		return x;
	}
	
	/**
	 * Gets the X coordinate of the eastern (right) edge of the rectangle.
	 * This is the same as the X offset + width.
	 * 
	 * @return	X coordinate of eastern edge.
	 */
	public int getEast() {
		return x+w;
	}
}
