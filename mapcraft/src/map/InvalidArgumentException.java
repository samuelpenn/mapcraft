package uk.co.demon.bifrost.rpg.mapcraft.map;


public class InvalidArgumentException extends MapException {
    public
    InvalidArgumentException() {
        super("Method arguments are invalid");
    }

    public
    InvalidArgumentException(String msg) {
        super(msg);
    }
}
