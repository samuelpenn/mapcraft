package uk.co.demon.bifrost.rpg.mapcraft.map;

/**
 * Represents a single tile on a map. A tile is a square
 * area of fixed size, describing the most common terrain,
 * average height and other information.
 * <br/>
 * The tile itself does not know how 'big' it is - this
 * information is stored as part of the TileSet, since all
 * tiles in a TileSet must be of the same size.
 * <br/>
 * Tiles also lack knowledge of what terrain means - again
 * mappings between terrain values and 'real-world' meanings
 * is performed by the Map itself.
 *
 * @see TileSet
 * @see Map
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
public class Tile {
    private short   terrain;
    private short   height;
    private boolean writable;
    private boolean river;
    private byte    riverMask;

    private Site    site;

    /**
     * Construct an empty tile, of 0m height, and the default
     * terrain type (normally ocean).
     */
    public Tile() {
        terrain = (short)1;
        height = 0;
        writable = true;
        river = false;
        riverMask = 0;
        site = null;
    }

    /**
     * Construct a tile from another Tile.
     *
     * @param tile  The Tile to be copied.
     */
    public Tile(Tile tile) {
        this.terrain = tile.terrain;
        this.height = tile.height;
        this.writable = tile.writable;
        this.river = tile.river;
        this.riverMask = tile.riverMask;
        this.site = tile.site;
    }

    /**
     * Construct a fully described tile.
     *
     * @param terrain   Number representing the terrain type.
     * @param height    Height of the tile, in metres.
     * @param writable  If false, tile is 'off-map'.
     */
    public
    Tile(short terrain, short height, boolean writable) {
        this.terrain = terrain;
        this.height = height;
        this.writable = writable;
    }

    /**
     * Set the terrain for this tile to be the defined type.
     * The Tile does not know what the terrain represents, it
     * is just an identifier.
     */
    public void
    setTerrain(short t) {
        this.terrain = t;
    }

    public short
    getTerrain() {
        return terrain;
    }

    public void
    setHeight(short h) {
        this.height = h;
    }

    public short
    getHeight() {
        return height;
    }
    
    public short
    getHills() {
        return 0;
    }

    public boolean
    isWritable() {
        return writable;
    }

    public boolean
    isRiver() {
        return river;
    }

    public short
    getRiverMask() {
        return (short)riverMask;
    }

    public void
    setRiverMask(short mask) {
        this.river = true;
        this.riverMask = (byte)mask;
    }

    public void
    setRiver(boolean river) {
        this.river = river;
        if (river == false) {
            riverMask = (byte)0;
        }
    }

    public void
    setSite(Site site) {
        this.site = site;
    }

    public Site
    getSite() {
        return site;
    }
}
