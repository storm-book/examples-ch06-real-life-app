package simplesearch;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import search.model.Item;
import storm.utils.AbstractAnswerBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;

public class AnswerQueryBolt extends AbstractAnswerBolt {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		super.prepare(stormConf, context, collector);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(Tuple input) {
		String origin= input.getString(0);
		String requestId= input.getString(1);
		List<Item> finalResult= (List<Item>)input.getValue(2);
		
		JSONArray list = new JSONArray();
		for (Item item : finalResult) {
			JSONObject obj= new JSONObject();
			obj.put("title", item.title);
			obj.put("id", item.id);
			obj.put("price", item.price);
			list.add(obj);
		}
		String json= JSONValue.toJSONString(list);
		sendBack(origin, requestId, json);
	}

	@Override
	protected int getDestinationPort() {
		return 8082;
	}
}
