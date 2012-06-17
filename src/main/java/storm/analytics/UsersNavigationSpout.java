package storm.analytics;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import org.json.simple.JSONValue;

import redis.clients.jedis.Jedis;
import storm.analytics.utilities.NavigationEntry;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;


public class UsersNavigationSpout extends BaseRichSpout {
	private static final long serialVersionUID = 1L;
	Jedis jedis;
	String host; 
	int port;
	SpoutOutputCollector collector;
			
	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map stormConf, TopologyContext context,
			SpoutOutputCollector collector) {
		host = stormConf.get("redis-host").toString();
		port = Integer.valueOf(stormConf.get("redis-port").toString());
		this.collector = collector;
		reconnect();
	}
	
	private void reconnect() {
		jedis = new Jedis(host, port);
	}

	@Override
	public void nextTuple() {
		String content = jedis.rpop("navigation");
		if(content==null || "nil".equals(content)) {
			try { Thread.sleep(300); } catch (InterruptedException e) {}
		} else {
	        JSONObject obj=(JSONObject)JSONValue.parse(content);
	        String user = obj.get("user").toString();
	        String product = obj.get("product").toString();
	        String type = obj.get("type").toString();
	        HashMap<String, String> map = new HashMap<String, String>();
	        map.put("product", product);
	        NavigationEntry entry = new NavigationEntry(user, type, map);
	        collector.emit(new Values(user, entry));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("user", "otherdata"));
	}
	
}
