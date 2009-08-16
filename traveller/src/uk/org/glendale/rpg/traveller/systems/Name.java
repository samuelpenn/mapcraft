package uk.org.glendale.rpg.traveller.systems;

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import uk.org.glendale.rpg.utils.Die;

/**
 * Random name generator.
 * @author Samuel Penn
 *
 */
public class Name {
	private StringBuffer		buffer = new StringBuffer();
	private String				style = null;
	
	private static final String	BUNDLE = "uk.org.glendale.rpg.traveller.systems.names";
	private static Properties	names = null;
	
	/**
	 * Automatically load the list of random phrases from the resource bundle.
	 */
    static {
        ResourceBundle      bundle = ResourceBundle.getBundle(BUNDLE);
        names = new Properties();

        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            names.setProperty(key, bundle.getString(key));
        }
    }
    
    public Name(String style) {
    	this.style = style;
    }
    
    /**
     * Get the phrase for the given key from the resource bundle. Some keys will
     * have a number of possible options (in the form key, key.1, key.2 etc). If
     * a key has several options, one will be selected randomly.
     * 
     * @param key		Key to use to find a phrase.
     * @return			The selected phrase, or null if none found.
     */
    private static String getRules(String key) {
    	String		text = null;
    	
    	text = names.getProperty(key);
    	if (text != null) {
    		int		i = 0;
    		while (names.getProperty(key+"."+(i+1)) != null) i++;
    		if (i > 0) {
    			int		choice = (int)(Math.random() * (i+1));
    			//System.out.println("Going for choice "+choice+" out of "+i);
    			if (choice != 0) text = names.getProperty(key+"."+choice);
    		}
    	}
    	//System.out.println("Got ["+key+"] ["+text+"]");
    	
    	return text;
    }

    private String get(String modifier, String key) {
    	String		list = null;
    	
    	if (modifier != null) list = getRules(key+"."+modifier);
    	if (list == null) list = getRules(key);
    	
    	//System.out.println("get["+modifier+","+key+"]: ["+list+"]");
    	
    	String[]	tokens = list.split(" +");
		String		rule = tokens[Die.rollZero(tokens.length)];
		String		word = "";

		for (int i=0; i < rule.length(); i++) {
			char	c = rule.charAt(i);
			if (Character.isUpperCase(c)) {
				word += get(modifier, style+"."+c);
			} else {
				word += c;
			}
		}
    	
    	return word;
    }

    public String getName() {
    	return getName(null);
    }
    
    public String getName(String modifier) {
    	String		format = null;
    	
    	if (modifier != null) format = getRules(style+"."+modifier+".format");
    	if (format == null) format = getRules(style+".format");
    	String[]	roots = format.split(" ");
    	String		name = "";
    	
    	for (String f : roots) {
    		String		n = get(modifier, f);
    		name += n.substring(0, 1).toUpperCase() + n.substring(1)+" ";
    	}
    	name = name.replaceAll(" '", "'");
    	name = name.replaceAll("_", " ");
    	
    	for (int i=0; i < name.length(); i++) {
    		if (name.charAt(i) == ' ' && i < name.length()-2) {
    			name = name.substring(0, i+1) + name.substring(i+1, i+2).toUpperCase() + name.substring(i+2); 
    		}
    	}
    	
    	return name.trim();
    }
    
    public static void main(String[] args) throws Exception {
    	Name	name = new Name("zhodani_planet");
    	
    	for (int i=0; i < 10; i++) {
    		System.out.println(name.getName(null));
    	}
    }
}
