package uk.co.demon.bifrost.rpg.mapcraft.xml;

/**
* Exception class, raised when an error occurs during
* processing of an XML document. Used as a generic
* exception, to make things easier to catch.
*/
public class XMLException extends Exception {
    public
    XMLException() {
        super();
    }

    public
    XMLException(String msg) {
        super(msg);
    }
}