package uk.co.demon.bifrost.rpg.xmlmap;


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
