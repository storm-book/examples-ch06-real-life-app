package storm.analytics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class UserHistoryBolt extends BaseRichBolt{
	

	private static final long serialVersionUID = 1L;
	
	OutputCollector collector;
	String host;
	int port;
	Jedis jedis;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		this.host = (String)stormConf.get("redis-host");
		this.port = Integer.valueOf(stormConf.get("redis-port").toString());
		reconnect();
	}

	public void reconnect() {
		this.jedis = new Jedis(host, port);
	}

	HashMap<String, Set<String>> usersNavigatedItems = new HashMap<String, Set<String>>(); 
	
	
	
	@Override
	public void execute(Tuple input) {
		String user = input.getString(0);
		String prod1 = input.getString(1);
		String cat1 = input.getString(2);

		// Product key will have category information embedded.
		String prodKey = prod1+":"+cat1;
		
		Set<String> productsNavigated = getUserNavigationHistory(user);
		
		// If the user previously navigated this item -> ignore it
		if(!productsNavigated.contains(prodKey)) {
			
			// Otherwise update related items
			for (String other : productsNavigated) {
				String [] ot = other.split(":");
				String prod2 = ot[0];
				String cat2 = ot[1]; 
				collector.emit(new Values(prod1, cat2));
				collector.emit(new Values(prod2, cat1));
			}
			addProductToHistory(user, prodKey);
		}
	}

	private void addProductToHistory(String user, String product) {
		Set<String> userHistory = getUserNavigationHistory(user);
		userHistory.add(product);
		jedis.sadd(buildKey(user), product);
	}

	private Set<String> getUserNavigationHistory(String user) {
		Set<String> userHistory = usersNavigatedItems.get(user);
		if(userHistory == null) {
			userHistory = jedis.smembers(buildKey(user));
			if(userHistory == null) 
				userHistory = new HashSet<String>();
			usersNavigatedItems.put(user, userHistory);
		}
		return userHistory;
	}

	private String buildKey(String user) {
		return "history:"+user;
	}


	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("product", "categ"));
	}
}
