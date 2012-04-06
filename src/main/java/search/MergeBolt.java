package search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import search.model.Item;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class MergeBolt implements IRichBolt {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("rawtypes")
	Map stormConf;
	TopologyContext context;
	int totalShards;
	OutputCollector collector;
	HashMap<String, Merger> inCourse= new HashMap<String, Merger>();
	
	public static class Merger {
		List<Item> items;
		int size;
		int totalMerged;
		long start;
		String origin;
		String requestId;

		public List<Item> getResults(){
			return items;
		}

		public long getAge(){
			return System.currentTimeMillis()-start;
		}

		public String getId(){
			return getId(origin, requestId);
		}

		public static String getId(String origin, String requestId){
			return origin+"-"+requestId;
		}

		public Merger(String origin, String requestId, int size) {
			this.size = size;
			items = new ArrayList<Item>(size);
			totalMerged = 0;
			start= System.currentTimeMillis();
			this.origin = origin;
			this.requestId = requestId;
		}

		public int getTotalMerged(){
			return totalMerged;
		}

		public void merge(List<Item> newItems) {
			totalMerged++;
			ArrayList<Item> chgList= new ArrayList<Item>(newItems.size()+items.size());
			chgList.addAll(newItems);
			chgList.addAll(items);
			Collections.sort(chgList, new Comparator<Item>() {
				@Override
				public int compare(Item i1, Item i2) {
					if(i1.price > i2.price)
						return -1;
					else
						return 1;
				}
			});
			if(chgList.size() < size)
				items = chgList;
			else
				items = chgList.subList(0, size);
		}

		@Override
		public String toString() {
			return items.toString();
		}

		public String getRequestId() {
			return requestId;
		}

		public Object getOrigin() {
			return origin;
		}
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.stormConf= stormConf;
		this.context= context;
		this.collector = collector;
		totalShards = context.getRawTopology().get_bolts().get("search").get_common().get_parallelism_hint();
		TimerTask t= new TimerTask() {
			@Override
			public void run() {
				ArrayList<Merger> mergers;
				
				synchronized (inCourse) {
					mergers= new ArrayList<MergeBolt.Merger>(inCourse.values());
				}
				
				for (Merger merger : mergers) {
					if(merger.getAge()>1000)
						finish(merger);
				}
			}
		};
		Timer timer= new Timer();
		timer.scheduleAtFixedRate(t, 1000, 1000);
	}

	@Override
	public void execute(Tuple input) {
		String origin= input.getString(0);
		String requestId= input.getString(1);

		@SuppressWarnings("unchecked")
		List<Item> shardResults = (List<Item>)input.getValue(3);

		String id = Merger.getId(origin, requestId);
		Merger merger= null;
		
		synchronized (inCourse) {
			merger= inCourse.get(id);	
		}
		
		
		if(merger==null){
			merger= new Merger(origin, requestId, 5);
			synchronized (inCourse) {
				inCourse.put(merger.getId(), merger);	
			}
		}

		merger.merge(shardResults);

		if(merger.getTotalMerged()>=totalShards)
			finish(merger);
	}
	
	protected void finish(Merger merger){
		collector.emit(new Values(merger.getOrigin(), merger.getRequestId(), merger.getResults()));
		synchronized (inCourse) {
			inCourse.remove(merger.getId());	
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("origin", "requestId", "results"));
	}
}
