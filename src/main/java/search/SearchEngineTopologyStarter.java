package search;

import search.model.Item;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class SearchEngineTopologyStarter {
	public static StormTopology createTopology() {
		TopologyBuilder builder= new TopologyBuilder();

		/**
		 * Search flow:
		 */
		builder.setSpout("queries-spout", new QueriesSpout(), 1);
		builder.setBolt("search", new SearchBolt(), 10).allGrouping("queries-spout").allGrouping("read-item-data");
		builder.setBolt("merge", new MergeBolt(), 3).fieldsGrouping("search", new Fields("origin", "requestId"));
		builder.setBolt("answer-query", new AnswerQueryBolt(), 2).fieldsGrouping("merge", new Fields("origin"));
		
		/**
		 * Indexing flow:
		 */
		builder.setSpout("items-news-feed-spout", new ItemsNewsFeedSpout(), 1);
		builder.setBolt("read-item-data", new ReadItemDataBolt(), 2).shuffleGrouping("items-news-feed-spout");
		builder.setBolt("answer-items-feed", new AnswerItemsFeedBolt()).fieldsGrouping("read-item-data", new Fields("origin"));

		return builder.createTopology();
	}

	public static Config createConf(String queriesPullHost, String feedPullHost, String itemsApiHost, int maxPull) {
		// Custom configuration
		Config conf= new Config();
		conf.put("queries-pull-host", queriesPullHost);
		conf.put("feed-pull-host", feedPullHost);
		conf.put("items-api-host", itemsApiHost);
		conf.put("max-pull", "100");
		conf.registerSerialization(Item.class);
		// Disable ackers mechanismo for this topology which doesn't need to be safe.
		conf.put(Config.TOPOLOGY_ACKERS, 0);
		return conf;
	}
	
	public static void main(String[] args) {
		if(args.length < 5) {
			System.err.println("Incorrect parameters. Use: <name> <queries-pull-host> <feed-pull-host> <items-api-host> <max-pulling>");
			System.exit(-1);
		}
		
		System.out.println("Topology Name  ["+args[0]+"]");
        try {
        	Config conf= createConf(args[1], args[2], args[3], Integer.valueOf(args[4]));
    		conf.setNumWorkers(20);
			StormSubmitter.submitTopology(args[0], conf, createTopology());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
