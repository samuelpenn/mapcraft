package uk.org.glendale.mapcraft.map;

/**
 * A named area can be applied to any tile on the map. Borders can be
 * automatically drawn between tiles which have differing areas. Any
 * one tile can at most belong to a single area. However, areas may
 * be parents of each other.
 * 
 * @author Samuel Penn
 */
public class NamedArea extends Tile {
	private int	parentId;

	public NamedArea(int id, String name, String title, int parentId) {
		super(id, name, title, null);
		this.parentId = parentId;
	}

	@Override
	public String getPrefix() {
		return null;
	}
	
	public int getParentId() {
		return parentId;
	}
}
