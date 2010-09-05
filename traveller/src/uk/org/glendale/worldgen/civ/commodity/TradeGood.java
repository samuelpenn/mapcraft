package uk.org.glendale.worldgen.civ.commodity;

public class TradeGood {
	private int				id;
	private Commodity		commodity;
	private long			quantity;
	private int				price;
	
	public TradeGood() {
	}
	
	public TradeGood(Commodity commodity, long quantity, int price) {
		this.commodity = commodity;
		this.quantity = quantity;
		this.price = price;
	}
	
	public int getId() {
		return id;
	}
	
	public Commodity getCommodity() {
		return commodity;
	}
	
	public long getQuantity() {
		return quantity;
	}
	
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	
	public void addQuantity(long amount) {
		this.quantity += amount;
	}
	
	public int getPrice() {
		return price;
	}
}
