package uk.org.glendale.worldgen.astro.sector;

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
	
	public static SubSector getSubSector(int x, int y) {
		x = (x-1) / 8;
		y = (y-1) / 10;
		
		for (SubSector ss : values()) {
			if (ss.x == x && ss.y == y) {
				return ss;
			}
		}
		return null;
	}
}
