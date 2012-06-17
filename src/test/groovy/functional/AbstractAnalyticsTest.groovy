package functional

import redis.clients.jedis.Jedis
import storm.analytics.*
import backtype.storm.LocalCluster
import org.junit.Before
import org.junit.After
import org.junit.Assert


public abstract class AbstractAnalyticsTest extends Assert {
	def jedis

	static topologyStarted = false
	static sync= new Object()

	private void reconnect() {
		jedis = new Jedis(TopologyStarter.REDIS_HOST, TopologyStarter.REDIS_PORT)
	}

	@Before
	public void startTopology(){
		synchronized(sync){
			reconnect()
			if(!topologyStarted){
				jedis.flushAll()
				populateProducts()
				TopologyStarter.testing = true
				TopologyStarter.main(null)
				topologyStarted = true
				sleep 1000
			}
		}
	}

	public void populateProducts() {
	    def testProducts = [
	        [id: 0, title:"Dvd player with surround sound system", category:"Players", price: 100],
	        [id: 1, title:"Full HD Bluray and DVD player", category:"Players", price:130],
	        [id: 2, title:"Media player with USB 2.0 input", category:"Players", price:70],

	        [id: 3, title:"Full HD Camera", category:"Cameras", price:200],
	        [id: 4, title:"Waterproof HD Camera", category:"Cameras", price:300],
	        [id: 5, title:"ShockProof and Waterproof HD Camera", category:"Cameras", price:400],
	        [id: 6, title:"Reflex Camera", category:"Cameras", price:500],

	        [id: 7, title:"DualCore Android Smartphon with 64Gb SD card", category:"Phones", price:200],
	        [id: 8, title:"Regular Movile Phone", category:"Phones", price:20],
	        [id: 9, title:"Satellite phone", category:"Cameras", price:500],

	        [id: 10, title:"64Gb SD Card", category:"Memory", price:35],
	        [id: 11, title:"32Gb SD Card", category:"Memory", price:27],
	        [id: 12, title:"16Gb SD Card", category:"Memory", price:5],

	        [id: 13, title:"Pink smartphone cover", category:"Covers", price:20],
	        [id: 14, title:"Black smartphone cover", category:"Covers", price:20],
	        [id: 15, title:"Kids smartphone cover", category:"Covers", price:30],

	        [id: 16, title:"55 Inches LED TV", category:"TVs", price:800],
	        [id: 17, title:"50 Inches LED TV", category:"TVs", price:700],
	        [id: 18, title:"42 Inches LED TV", category:"TVs", price:600],
	        [id: 19, title:"32 Inches LED TV", category:"TVs", price:400],

	        [id: 20, title:"TV Wall mount bracket 32-42 Inches", category:"Mounts", price:50],
	        [id: 21, title:"TV Wall mount bracket 50-55 Inches", category:"Mounts", price:80]
    	]

		testProducts.each() { product ->
			def val = "{ \"title\": \"${product.title}\" , \"category\": \"${product.category}\", \"price\": ${product.price}, \"id\": ${product.id} }"
			println val
			jedis.set(product.id.toString(), val.toString())
		} 
	}
	

	public int getProductCategoryStats(String product, String categ) {
		String count = jedis.hget("prodcnt:${product}", categ)
		if(count == null || "nil".equals(count))
			return 0
		return Integer.valueOf(count)
	}

	public void navigate(user, product) {
		String nav= "{\"user\": \"${user}\", \"product\": \"${product}\", \"type\": \"PRODUCT\"}".toString()
		println "Pushing navigation: ${nav}"
		jedis.lpush('navigation', nav)
	}
}
