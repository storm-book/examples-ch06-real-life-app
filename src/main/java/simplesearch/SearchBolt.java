package simplesearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import search.model.Item;
import search.model.ItemsContainer;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class SearchBolt implements IRichBolt {
	private static final long serialVersionUID = 1L;
	Logger log;
	OutputCollector collector;
	@SuppressWarnings("rawtypes")
	Map stormConf;
	TopologyContext context;
	ItemsContainer myItems;
	
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, 
						TopologyContext context,
						OutputCollector collector) {
		log = Logger.getLogger(this.getClass());
		this.stormConf= stormConf;
		this.context= context;
		this.collector= collector;
		myItems = new ItemsContainer(10000); 
		populateItems();
	}

	private void populateItems() {
		myItems.add(new Item(0, "old dvd player", Math.random()*Math.random()*100));
		myItems.add(new Item(1, "new cell phone", Math.random()*100));
		myItems.add(new Item(2, "old pirates coin", Math.random()*100));
		myItems.add(new Item(3, "fashion sun glasses", Math.random()*100));
		myItems.add(new Item(4, "electric guitar", Math.random()*100));
		myItems.add(new Item(5, "expresso coffe machine", Math.random()*100));
		myItems.add(new Item(6, "cheap cell phone case", Math.random()*100));
		myItems.add(new Item(7, "fast laptop computer", Math.random()*100));
		myItems.add(new Item(8, "small laptop computer", Math.random()*100));
		myItems.add(new Item(9, "universal remote control", Math.random()*100));
		myItems.add(new Item(10, "universal serial bus video card", Math.random()*100));
		myItems.add(new Item(11, "vintage vcr player", Math.random()*100));
		myItems.add(new Item(12, "new bluray player", Math.random()*100));
		myItems.add(new Item(13, "almost new games console", Math.random()*100));
		myItems.add(new Item(14, "portable air conditioner", Math.random()*100));
		myItems.add(new Item(15, "portable television set", Math.random()*100));
		myItems.add(new Item(16, "big led full hd television set", Math.random()*100));
		myItems.add(new Item(17, "powerful microwave oven", Math.random()*100));
		myItems.add(new Item(18, "brand new car", Math.random()*100));
		myItems.add(new Item(19, "brand new suv", Math.random()*100));
		myItems.add(new Item(20, "brand new watch", Math.random()*100));
		myItems.add(new Item(21, "used car", Math.random()*100));
		myItems.add(new Item(22, "used suv", Math.random()*100));
		myItems.add(new Item(23, "new home theater audio system", Math.random()*100));
		myItems.add(new Item(24, "new high speed gamer computer", Math.random()*100));
		myItems.add(new Item(25, "new small computer charger", Math.random()*100));
		myItems.add(new Item(26, "car cell phone adapter", Math.random()*100));
	}

	@Override
	public void execute(Tuple input) {
		String origin= input.getString(0);
		String requestId= input.getString(1);
		String query= input.getString(2);
		List<Item> results= executeQuery(query, 5);
		log.debug("Searching ["+ query +"]:"+results.size()+" results found");
		collector.emit(new Values(origin, requestId, results));
	}

	private List<Item> executeQuery(String query, int quantity) {
		List<Item> items= new ArrayList<Item>(myItems.getItemsContainingWords(query));
		Collections.sort(items, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				double diff= o1.price-o2.price;
				if(diff>0)
					return -1;
				else
					return 1;
			}
		});
		
		if(items.size()>quantity)
			items = items.subList(0, quantity-1);
		return items;
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("origin", "requestId", "results"));
	}
}
