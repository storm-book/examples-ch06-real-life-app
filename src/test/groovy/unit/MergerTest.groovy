package unit

import org.junit.Test;
import search.MergeBolt.Merger
import search.model.Item
import java.util.ArrayList
import org.junit.Assert

public class MergerTest extends Assert  {

	@Test
	public void mergerMaintainsMaxSize() {
		def a = new ArrayList<Item>()
		def b = new ArrayList<Item>()
		def c = new ArrayList<Item>()
		long id = 0

		for(int i=0; i<10; i++) {
			a.add(new Item(id, "a", i))
			id++;
			b.add(new Item(id, "b", i+0.25))
			id++;
			c.add(new Item(id, "c", i+0.5))
			id++;
		}
		
		Merger m= new Merger("localhost", "44", 5);
		
		m.merge(a);
		m.merge(b);
		m.merge(c);
		
		assertEquals(5, m.getResults().size())
	}
	
	@Test
	public void mergerCorrectSort() {
		def a= new ArrayList<Item>();
		def b= new ArrayList<Item>();
		def c= new ArrayList<Item>();
		long id=0;

		for(int i=0; i<10;i++){
			a.add(new Item(id, "a", i));
			id++;
			b.add(new Item(id, "b", i+0.25));
			id++;
			c.add(new Item(id, "c", i+0.5));
			id++;
		}
		
		Merger m= new Merger("localhost", "44", 5);
		
		m.merge(a);
		m.merge(b);
		m.merge(c);

		assertEquals(9.5, m.getResults().get(0).price, 0.01)
		assertEquals(9.25, m.getResults().get(1).price, 0.01)
		assertEquals(9, m.getResults().get(2).price, 0.01)
		assertEquals(8.5, m.getResults().get(3).price, 0.01)
		assertEquals(8.25, m.getResults().get(4).price, 0.01)
	}
}
