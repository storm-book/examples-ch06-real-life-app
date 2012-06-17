package storm.analytics.utilities;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;
    public String title;
    public double price;
    public String category;

    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Product() {
    }

    public Product(long id, String title, double price, String category) {
        this.id= id;
        this.title= title;
        this.price= price;
        this.category = category;
    }

    @Override
    public boolean equals(Object obj) {
        Product other= (Product)obj;
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
