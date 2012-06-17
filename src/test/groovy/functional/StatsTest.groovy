package functional
import org.junit.Test;

class StatsTest extends AbstractAnalyticsTest {
	@Test
	public void testNoDuplication(){
		navigate("1", "0") // Players
		navigate("1", "1") // Players
		navigate("1", "2") // Players
		navigate("1", "3") // Cameras

		Thread.sleep(2000) // Give two seconds for the system to process the data.

		assertEquals 1, getProductCategoryStats("0", "Cameras")
		assertEquals 1, getProductCategoryStats("1", "Cameras")
		assertEquals 1, getProductCategoryStats("2", "Cameras")
		assertEquals 2, getProductCategoryStats("0", "Players")
		assertEquals 3, getProductCategoryStats("3", "Players")
	}
}
