package uk.co.demon.bifrost.rpg.mapcraft.map;


public class MapOutOfBoundsException extends MapException {
    public
    MapOutOfBoundsException() {
        super("Map out of bounds");
    }
    
    public
    MapOutOfBoundsException(String msg) {
        super(msg);
    }
}
