package uk.org.glendale.worldgen.server;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="sector")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Foo {
	public String name;
	
	public Foo() {
		this.name = "Unset";
	}
	
	public Foo(String name) {
		this.name = name;
	}
	
	@XmlElement
	public String getName() {
		return name;
	}
	
	@XmlAttribute
	public int getX() {
		return 4;
	}
	
	@XmlAttribute
	public int getY() {
		return 2;
	}
}
