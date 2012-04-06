package search;

import java.util.Map;

import storm.utils.AbstractAnswerBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;

public class AnswerItemsFeedBolt extends AbstractAnswerBolt {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		super.prepare(stormConf, context, collector);
	}

	@Override
	public void execute(Tuple input) {
		String origin= input.getString(0);
		String requestId= input.getString(1);
		sendBack(origin, requestId, "OK");
	}

	@Override
	protected int getDestinationPort() {
		return 9092;
	}
}
