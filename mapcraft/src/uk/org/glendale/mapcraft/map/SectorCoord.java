package uk.org.glendale.mapcraft.map;

/**
 * Key used to identify a sector. The coordinates given are the global
 * map coordinates, which should be a multiple of 32 (X) or 40 (Y). If
 * they aren't, then they're automatically truncated down to the nearest.
 * 
 * @author Samuel Penn
 */
public class SectorCoord {
	private	int	x;
	private int	y;
	SectorCoord(int x, int y) {
		this.x = x - x%32;
		this.y = y - y%40;
	}
	
	int	getX() {
		return x;
	}
	
	int getY() {
		return y;
	}
	
	@Override
	public int hashCode() {
		return x*20000 + y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SectorCoord) {
			if (x == ((SectorCoord)o).x && y == ((SectorCoord)o).y) {
				return true;
			}
		}
		return false;
	}
	
	public String toString() {
		return "["+x+":"+y+"]";
	}
}
