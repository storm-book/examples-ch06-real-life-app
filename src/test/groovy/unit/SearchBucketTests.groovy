package unit

import org.junit.Test
import search.model.Item
import search.model.ItemsContainer
import org.junit.Assert

public class SearchBucketTest extends Assert {
	@Test
	public void searchContainer() {
		def myContainer= new ItemsContainer(20)
		
		Item a= new Item(0, "nice dvd player with usb and card reader", 100)
		Item b= new Item(1, "new laptop computer with dvd and usb and manual", 100)
		Item c= new Item(2, "elegant cell phone with usb charger manual dvd included", 100)
		Item d= new Item(3, "new balck microwave includes cooking book and operation manual", 100)
		
		myContainer.add a
		myContainer.add b
		myContainer.add c
		myContainer.add d
		
		def result= myContainer.getItemsContainingWords("nice-dvd")
		assert result.contains(a);
		assert !result.contains(b);
		assert !result.contains(c);
		assert !result.contains(d);
		
		result= myContainer.getItemsContainingWords("new-manual");
		assert result.contains(b);
		assert result.contains(d);
		assert !result.contains(a);
		assert !result.contains(c);

		result= myContainer.getItemsContainingWords("nice-dvd-player-with-usb-and-card-reader");
		assert result.contains(a);
		assert !result.contains(b);
		assert !result.contains(c);
		assert !result.contains(d);
	}
}
