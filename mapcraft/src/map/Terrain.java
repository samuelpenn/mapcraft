package uk.co.demon.bifrost.rpg.xmlmap;

/**
 * Describes a single type of terrain.
 *
 * @author  Samuel Penn (sam@bifrost.demon.co.uk)
 * @version $Revision$
 */
public class Terrain {
    private short   id;
    private String  name;
    private String  description;
    private String  imagePath;

    public
    Terrain(short id, String name, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
    }

    public short getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
}
