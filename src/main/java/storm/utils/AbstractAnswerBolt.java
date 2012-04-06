package storm.utils;

import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.log4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;

public abstract class AbstractAnswerBolt implements IRichBolt {
	private static final long serialVersionUID = 1L;
	protected transient Logger log;
	transient HttpClient client;

	protected void sendBack(String origin, String id, String content){
		String to= "http://"+origin+":"+getDestinationPort()+"/?id="+id;
		log.debug("Answering to:"+to);
		HttpPost post= new HttpPost(to);
		try {
			StringEntity entity= new StringEntity(content);
			post.setEntity(entity);
			HttpResponse response= client.execute(post);
			InputStream is= response.getEntity().getContent();
			is.close();			
		} catch (Exception e) {
			log.error("Answering to:["+to+"] with ["+content+"]", e);
		}
	}

	protected abstract int getDestinationPort();


	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context,
			OutputCollector collector) {
		log = Logger.getLogger(this.getClass());
		client= new DefaultHttpClient(new SingleClientConnManager());
	}

	@Override
	public void cleanup() {
		// Default is NO action for an answer bolt in this method
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// Default is NO action for an answer bolt in this method
	}
}
