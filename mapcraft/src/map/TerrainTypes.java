/**
 * TerrainTypes Class
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 *
 * Manages all the different types of terrain.
 * 
 */
 
package uk.co.demon.bifrost.rpg.xmlmap;

public class TerrainTypes {
    private String  dataPath;
    private String  imageDir;

    public
    TerrainTypes(String dataPath, String imageDir) {
        this.dataPath = dataPath;
        this.imageDir = imageDir;
    }
}
