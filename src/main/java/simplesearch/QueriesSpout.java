package simplesearch;

import java.util.Map;

import storm.utils.AbstractClientSpout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;

public class QueriesSpout extends AbstractClientSpout {
	private static final long serialVersionUID = 1L;
	
	String queriesPullHost;
	int maxPull;
	
	@Override
	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		this.queriesPullHost= (String) conf.get("queries-pull-host");
		try{
			this.maxPull= Integer.parseInt((String)conf.get("max-pull"));
		} catch(Exception ex){
			this.maxPull= 1;
		}

		super.open(conf, context, collector);
	}
	
	@Override
	protected String getPullHost() {
		return queriesPullHost;
	}

	@Override
	protected int getMaxPull() {
		return maxPull;
	}
}
