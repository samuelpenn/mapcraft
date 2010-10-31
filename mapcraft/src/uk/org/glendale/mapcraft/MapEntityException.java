package uk.org.glendale.mapcraft;

public class MapEntityException extends Exception {
	private static final long serialVersionUID = 1L;

	public MapEntityException(String type, String name, String message) {
		super("Cannot create "+type+" with name ["+name+"]: "+message);
	}
}
