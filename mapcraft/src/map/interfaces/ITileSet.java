/*
 * Copyright (C) 2004 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.map.interfaces;

import net.sourceforge.mapcraft.map.*;
import net.sourceforge.mapcraft.map.elements.*;


/**
 * A TileSet defines a 2D set of tiles representing a map, as well as
 * any free floating objects on that map. The interface abstracts away
 * the storage method for the map - it could be in memory, from a file
 * or from a database.
 * <br/>
 * Each Tile on the map consists of a Terrain, a Feature and a Height.
 * Also associated with the TileSet are a list of Paths and Things, both
 * of which are free floating and not associated with any given tile.
 * Coordinates for all free floating objects are specified in hundreths
 * of a Tile width.
 * 
 * @author Samuel Penn
 *
 */
public interface ITileSet {

    /**
     * Set details of the parent of this TileSet. If this TileSet is
     * a sub-child, then we must record the X and Y offset into the
     * parent map, as well as the scale of the parent (in case the
     * parent is later rescaled).
     * 
     * @param scale     Scale of the parent, in whatever units are being used.
     * @param x         X-offset of this TileSet.
     * @param y         Y-Offset of this TileSet.
     */
    public void setParent(int scale, int x, int y);

    /**
     * Get the scale of this TileSet's parent, when the TileSet was created.
     * 
     * @return          Scale, in whatever units are being used.
     */
    public int getParentsScale();

    /**
     * Is this TileSet a child of another?
     * 
     * @return          True iff TileSet has a parent set.
     */
    public boolean isChild();

    /**
     * Y-Offset into the parent TileSet. Offset is measured from the top
     * of the parent map, and is offset to the top of this TileSet.
     * 
     * @return          Y-offset into parent map.
     */
    public int getParentsYOffset();
    
    /**
     * X-Offset into the parent TileSet. Offset is measured from the left
     * edge of the parent map, and is offset to the left edge of this TileSet.
     * 
     * @return          X-offset into parent map.
     */
    public int getParentsXOffset();
    
    
    /**
     * Crop this TileSet to the given size. The width and height must both
     * be positive - if not, a MapOutOfBoundsException is thrown.
     * 
     * @param x         X coordinate of top left corner.
     * @param y         Y coordinate of top left corner.
     * @param width     Width of the area to be cropped to.
     * @param height    Height of the area to be cropped to.
     */
    public void crop(int x, int y, int width, int height) 
                throws MapOutOfBoundsException;


    /**
     * Rescale the map. The scale of the tiles is changed, and, the number
     * of tiles is changed to reflect the new scale. If changing to a smaller
     * scale, then the number of tiles is increased.
     */
    public void rescale(int newScale)
                throws IllegalArgumentException;


 
    public void setTerrain(int x, int y, Terrain terrain)
                throws MapOutOfBoundsException;

    
    
    public void
    setHeight(int x, int y, short h) throws MapOutOfBoundsException;

    public String getName();
    public int getMapHeight();
    public int getMapWidth();
    public int getScale();

    /**
     * Set the scale for the TileSet. The scale change does
     * not perform any resizing of the TileSet.
     *
     * @param scale     Scale, in km, to set TileSet to.
     */
    public void setScale(int scale) throws IllegalArgumentException;


    /**
     * Get the terrain id for the particular tile.
     *
     * @param x     X coordinate of the tile.
     * @param y     Y coordinate of the tile.
     *
     * @return      Id of the terrain for this tile.
     */
    public Terrain getTerrain(int x, int y) throws MapOutOfBoundsException;

    /**
     * Get the height (in metres) for the particular tile.
     *
     * @param x     X coordinate of the tile.
     * @param y     Y coordinate of the tile.
     *
     * @return      Id of the terrain for this tile.
     */
    public int
    getHeight(int x, int y) throws MapOutOfBoundsException;

    public boolean
    isWritable(int x, int y) throws MapOutOfBoundsException;


    public int
    getArea(int x, int y) throws MapOutOfBoundsException;

    public void
    setArea(int x, int y, short area) throws MapOutOfBoundsException;

    public short
    getFeature(int x, int y) throws MapOutOfBoundsException;

    /**
     * Set the tile at the specified location to have the specified feature.
     * Features are just a special type of Terrain (the image of which often
     * has a mask associated with it).
     * 
     * @param x
     * @param y
     * @param feature
     * @throws MapOutOfBoundsException
     */
    public void
    setFeature(int x, int y, Terrain feature) throws MapOutOfBoundsException;

    /**
     * Crop the tiles to the given area. The map is searched for all tiles
     * which match the area, and a rectangle is formed which encloses all
     * these tiles. The rectangle will then be grown in each direction by
     * the size of the margin, if it is positive.
     *
     * The margin will not take the cropped area out beyond the edges of
     * the original map.
     *
     * @param area      Area id to be cropped to.
     * @param margin    Number of tiles to add as a margin.
     */
    public void
    cropToArea(short area, int margin) throws MapOutOfBoundsException;

    /**
     * Crop the tiles to the highlighted region. If nothing is highlighted,
     * then no cropping is performed.
     */
    public void
    cropToHighlighted() throws MapOutOfBoundsException;

    /**
     * Crop the tiles to a region around a named Thing.
     * 
     * @param name          Name of thing to crop to.
     * @param radius        Number of tiles radius to display.
     * 
     * @throws MapException
     */
    public void
    cropToThing(String name, short radius) throws MapException;

    /**
     * Crop the tiles to a bounding box which fully includes the named
     * path. If the path goes beyond the existing map edge, then the
     * path will not be fully bounded by the cropped area.
     * 
     * @param name
     * @param margin
     * @throws MapException
     */
    public void
    cropToPath(String name, short margin) throws MapException;

    /**
     * Replace all the rivers with the new set of rivers.
     */
    public void setPaths(Path[] paths); 

    /**
     * Replace all the Things in this TileSet with the Things given
     * in the provided array.
     * @param things
     */
    public void setThings(Thing[] things);

    
    public Path[] getPaths();
    public Thing[] getThings();

    /**
     * Get a list of the names of all the things in this TileSet.
     * The list is returned as an array of Strings. It is not guaranteed
     * that all things will have unique names.
     *
     * @return      Array of strings holding all things in the map.
     *              null if there are no things defined.
     */
    public String[] getThingNames();
    
    public Thing getThing(String name);
    
    /**
     * Get the names of roads and rivers as an array. The type should be
     * either 0 for all types, Path.ROAD for roads or Path.RIVER for rivers.
     * If no paths of the given type are found, then null is returned.
     */
    public String[] getPathNames(short type);
    
    public Path getPath(String name);

    /**
     * Create and add a new river or road to the map. Return the new
     * Path. The name of the Path must be unique to the TileSet.
     */
    public Path addPath(String name, short type, short style, int x, int y)
                throws MapException;


    public void removeThing(Thing thing);

    public void addThing(Thing thing);

    public short getFeatureRotation(int x, int y)
                 throws MapOutOfBoundsException;

    public void setFeatureRotation(int x, int y, short rotation)
                throws MapOutOfBoundsException;

    public short getTerrainRotation(int x, int y) 
                 throws MapOutOfBoundsException;

    public void setTerrainRotation(int x, int y, short rotation) 
                throws MapOutOfBoundsException;

    /**
     * Check to see if the given tile is highlighted.
     */
    public boolean isHighlighted(int x, int y)
                   throws MapOutOfBoundsException;

    public void setHighlighted(int x, int y, boolean hl) 
                throws MapOutOfBoundsException;


    /**
     * Find all occurrences of a given area on a map, and change them to be
     * the new area. Often used when an area is deleted, and it is to be set
     * to the parent area (or null, if it has no parent). The caller is
     * expected to decide whether to set a deleted area to its parent or to
     * null.
     * 
     * @param oldArea       Area to be found and changed.
     * @param newArea       Area to set to, or null to delete.
     * 
     * @return              Count of number of tiles changed.
     */
    public int changeArea(Area oldArea, Area newArea);

}
