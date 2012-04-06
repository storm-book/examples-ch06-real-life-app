package search.model;

import java.io.Serializable;

public class Item implements Serializable {
	private static final long serialVersionUID = 1L;
	public long id;
	public String title;
	public double price;
	
	public Item() {
	}
	
	public Item(long id, String title, double price) {
		this.id= id;
		this.title= title;
		this.price= price;
	}

	@Override
	public boolean equals(Object obj) {
		Item other= (Item)obj;
		return other.id==id;
	}
	
	@Override
	public int hashCode() {
		return (int)(id%Integer.MAX_VALUE);
	}
	
	
	@Override
	public String toString() {
		return "id:"+id+ " title: "+title+ " price:"+price;
	}
}
