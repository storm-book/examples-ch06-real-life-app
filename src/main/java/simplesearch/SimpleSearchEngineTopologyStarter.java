package simplesearch;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;

public class SimpleSearchEngineTopologyStarter {
	public static StormTopology createTopology() {
		TopologyBuilder builder= new TopologyBuilder();

		builder.setSpout("queries-spout", new QueriesSpout(), 1);
		builder.setBolt("queries-processor", new SearchBolt(), 1).allGrouping("queries-spout");
		builder.setBolt("answer-query", new AnswerQueryBolt(), 2).allGrouping("queries-processor");
		return builder.createTopology();
	}

	public static Config createConf(String queriesPullHost, int maxPull) {
		// Custom configuration
		Config conf= new Config();
		conf.put("queries-pull-host", queriesPullHost);
		conf.put("max-pull", maxPull);
		// Disable ackers mechanismo for this topology which doesn't need to be safe.
		conf.put(Config.TOPOLOGY_ACKERS, 0);
		return conf;
	}
	
	public static void main(String[] args) {
		if(args.length < 3) {
			System.err.println("Incorrect parameters. Use: <name> <queries-pull-host> <max-pulling>");
			System.exit(-1);
		}
		
		System.out.println("Topology Name  ["+args[0]+"]");
        try {
        	Config conf= createConf(args[1], Integer.valueOf(args[2]));
			LocalCluster cluster= new LocalCluster();
			cluster.submitTopology(args[0], conf, createTopology());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
