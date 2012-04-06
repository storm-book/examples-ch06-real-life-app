package search;

import java.util.Map;

import storm.utils.AbstractClientSpout;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;

public class ItemsNewsFeedSpout extends AbstractClientSpout {
	private static final long serialVersionUID = 1L;
	String feedPullHost;
	int maxPull;
	
	/**
	 * Open a thread for each processed server.
	 */
	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
			SpoutOutputCollector collector) {

		this.feedPullHost= (String) conf.get("feed-pull-host");
		try{
			this.maxPull= Integer.parseInt((String)conf.get("max-pull"));
		} catch(Exception ex){
			this.maxPull= 1;
		}
		super.open(conf, context, collector);
	}
	
	@Override
	protected String getPullHost() {
		return feedPullHost;
	}

	@Override
	protected int getMaxPull() {
		return maxPull;
	}
}
