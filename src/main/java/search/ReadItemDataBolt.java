package search;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import search.model.Item;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class ReadItemDataBolt implements IRichBolt {
	private static final long serialVersionUID = 1L;
	Logger log; 
	OutputCollector collector;
	@SuppressWarnings("rawtypes")
	Map stormConf;
	TopologyContext context;
	String itemsApiHost;
	HttpClient httpclient;
	HttpGet httpget;
	
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, 
						TopologyContext context,
						OutputCollector collector) {
		log = Logger.getLogger(this.getClass());
		this.stormConf = stormConf;
		this.context = context;
		this.collector = collector;
		this.itemsApiHost = (String)stormConf.get("items-api-host");
		reconnect();
	}

	private void reconnect() {
		httpclient = new DefaultHttpClient(new SingleClientConnManager()); 
	}
	
	public Item readItem(int id) throws Exception{
		HttpResponse response;
		BufferedReader reader= null;
		String url= "http://"+itemsApiHost+"/"+id+".json";
		log.debug("Reading item data:["+url+"]");
		httpget = new HttpGet(url);
		try {
			response = httpclient.execute(httpget);

			if(response.getStatusLine().getStatusCode()==200) {
				HttpEntity entity = response.getEntity();
				entity.getContent();
				reader= new BufferedReader(new InputStreamReader(entity.getContent()));
				Object obj=JSONValue.parse(reader);
				JSONObject item=(JSONObject)obj;
				Item i= new Item((Long)item.get("id"), (String)item.get("title"), (Long)item.get("price"));
				return i;
			} else if (response.getStatusLine().getStatusCode() == 404) {
				response.getEntity().getContent().close();
				return null;
			} else
				throw new Exception(response.getStatusLine().getStatusCode()+" is not a valid HTTP code for this response");
		} catch (Exception e) {
			log.error("Error reading item "+id, e);
			reconnect();
			throw new Exception("Error reading item ["+id+"]", e);
		} finally {
			if(reader!=null){
				try {
					reader.close();
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
	}
	
	@Override
	public void execute(Tuple input) {
		String origin = input.getString(0);
		String requestId = input.getString(1);
		int itemId = Integer.valueOf(input.getString(2));

		Item i;
		try {
			i = readItem(itemId);
			log.debug("Item readed "+ itemId+" ["+i+"]");
			if(i==null) {
				collector.emit(new Values(origin, requestId, itemId, null));
			} else {
				collector.emit(new Values(origin, requestId, itemId, i));
			}
		} catch (Exception e) {
			log.error("Error ["+origin+"] ["+requestId+"] ["+itemId+"]", e);
		}
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("origin", "requestId", "itemId", "data"));
	}
}
