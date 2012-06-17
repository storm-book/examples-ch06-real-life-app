package storm.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import redis.clients.jedis.Jedis;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class ProductCategoriesCounterBolt extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	private Jedis jedis;
	
	// ITEM:CATEGORY -> COUNT
	HashMap<String, Integer> counter = new HashMap<String, Integer>();
	
	// ITEM:CATEGORY -> COUNT
	HashMap<String, Integer> pendingToSave = new HashMap<String, Integer>(); 
	
	Timer timer;
	OutputCollector collector;
	String host;
	int port;
	long downloadTime;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.host = (String)stormConf.get("redis-host");
		this.port = Integer.valueOf(stormConf.get("redis-port").toString());
		this.downloadTime = Long.valueOf(stormConf.get("download-time").toString());
		startDownloaderThread();
		this.collector = collector;
		reconnect();
	}
	
	
	public void reconnect() {
		this.jedis = new Jedis(host, port);
	}
	
	public int getProductCategoryCount(String categ, String product) {
		Integer count = counter.get(buildLocalKey(categ, product));
		if(count == null) {
			String sCount = jedis.hget(buildRedisKey(product), categ);
			if(sCount == null || "nil".equals(sCount)) {
				count = 0;
			} else {
				count = Integer.valueOf(sCount);
			}
		}
		return count;
	}


	private String buildRedisKey(String product) {
		return "prodcnt:"+product;
	}
	
	private String buildLocalKey(String categ, String product) {
		return product+":"+categ;
	}
	
	private void storeProductCategoryCount(String categ, String product, int count) {
		String key = buildLocalKey(categ, product);
		counter.put(key , count);
		synchronized (pendingToSave) {
			pendingToSave.put(key, count);	
		}
	}
	
	private int count(String product, String categ) {
		int count = getProductCategoryCount(categ, product);
		count ++;
		storeProductCategoryCount(categ, product, count);
		return count;
	}

	// Start a thread in charge of downloading metrics to redis.
	private void startDownloaderThread() {
		TimerTask t = new TimerTask() {
			@Override
			public void run() {
				HashMap<String, Integer> pendings;
				synchronized (pendingToSave) {
					pendings = pendingToSave;
					pendingToSave = new HashMap<String, Integer>();
				}
				
				for (String key : pendings.keySet()) {
					String[] keys = key.split(":");
					String product = keys[0];
					String categ = keys[1];
					Integer count = pendings.get(key);
					jedis.hset(buildRedisKey(product), categ, count.toString());
				}
			}
		};
		timer = new Timer("Item categories downloader");
		timer.scheduleAtFixedRate(t, downloadTime, downloadTime);
	}

	@Override
	public void execute(Tuple input) {
		String product = input.getString(0);
		String categ = input.getString(1);
		int total = count(product, categ);
		collector.emit(new Values(product, categ, total));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("product", "categ", "visits"));
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		timer.cancel();
	}
}
