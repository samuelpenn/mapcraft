package uk.org.glendale.worldgen.astro.planet;

import javax.persistence.*;

@Embeddable
public class MapImage {
	public enum Projection {
		Mercator, Globe;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(name="type")
	private Projection	type;
	
	@Column(name="image") @Lob
	private byte[]	imageData;
	
	public MapImage() {
	}
	
	public void setType(Projection type) {
		this.type = type;
	}
	
	public Projection getType() {
		return type;
	}
	
	
	public void setData(byte[] imageData) {
		this.imageData = imageData;
	}
	
	public byte[] getData() {
		return imageData;
	}
}
