package uk.co.demon.bifrost.rpg.mapcraft.map;


public class Site {
    private short       type;
    private String      name;
    private String      description;
    
    public
    Site(short type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }
    
    public short
    getType() {
        return type;
    }
    
    public String
    getName() {
        return name;
    }
    
    public String
    getDescription() {
        return description;
    }
    
    public void
    setType(short type) { this.type = type; }
    
    public void
    setName(String name) { this.name = name; }
    
    public void
    setDescription(String description) { this.description = description; }
}
