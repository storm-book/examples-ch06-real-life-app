package functional

import org.junit.Test

public class SearchTest extends AbstractSearchIntegrationTest {

	@Test
	public void newsFeedTest() {
		// Verify Empty
		def result = search("new")
		assertEquals(result.size(), 0)
  
		// Publish items
		addItem(1, "new dvd player", 100)
		addItem(2, "new digital camera", 80)
		addItem(3, "new laptop computer", 70)
		notifyItemChange(1)
		notifyItemChange(2)
		notifyItemChange(3)

		result = search("drive")
		assertEquals(0, result.size())

		// Verify a query with results
		result = search("new")
		assertEquals(3, result.size())

		// Delete & modify items
		removeItem(1)
		addItem(1, "new dvd player just sold", 100)
		removeItem(2)
		removeItem(3)
		notifyItemChange(1)
		notifyItemChange(2)
		notifyItemChange(3)

		result = search("new")
		assertEquals(1, result.size())

		result = search("sold")
		assertEquals(1, result.size())

		result = search("new-dvd-player-just-sold")
		assertEquals(1, result.size())
	}

	@Test
	public void searchMultiple() {
		addItem(1, "new dvd player", 100)
		addItem(2, "new digital camera", 80)
		addItem(3, "new laptop computer", 70)
		addItem(4, "new mobile cell phone with long life battery", 100)
		addItem(5, "car battery", 80)
		addItem(6, "hair implant", 70)
		addItem(7, "freezer", 100)
		addItem(8, "kitchen", 80)
		addItem(9, "remote control", 80)
		addItem(10, "air conditioner", 80)

		notifyItemChange(1)
		notifyItemChange(2)
		notifyItemChange(3)
		notifyItemChange(4)
		notifyItemChange(5)
		notifyItemChange(6)
		notifyItemChange(7)
		notifyItemChange(8)
		notifyItemChange(9)
		notifyItemChange(10)

		def result = search("new")

		assertEquals(4, result.size())
		assertEquals(100, result[0].price, 0.1)

		result = search("new-mobile")
		assertEquals(1, result.size())
		assertEquals(100, result[0].price, 0.1)

		result = search("battery")
		assertEquals(2, result.size())
		assertEquals(100, result[0].price, 0.1)
		assertEquals(80, result[1].price, 0.1)
	}

}
