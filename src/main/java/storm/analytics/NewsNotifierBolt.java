package storm.analytics;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

public class NewsNotifierBolt extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	
	String webserver;
	HttpClient client;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.webserver = (String)stormConf.get("webserver");
		reconnect();
	}
	
	private void reconnect() {
		client = new DefaultHttpClient();
	}

	@Override
	public void execute(Tuple input) {
		String product = input.getString(0);
		String categ = input.getString(1);
		int visits = input.getInteger(2);

		String content = "{ \"product\": \""+product+"\", \"categ\":\""+categ+"\", \"visits\":"+visits+" }";

		HttpPost post = new HttpPost(webserver);
		try {
			post.setEntity(new StringEntity(content));
			HttpResponse response = client.execute(post);
			org.apache.http.util.EntityUtils.consume(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			reconnect();
		} 
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// This bolt does not emmit any tuple
	}
}
