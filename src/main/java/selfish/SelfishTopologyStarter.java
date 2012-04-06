package selfish;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;

public class SelfishTopologyStarter {
	public static StormTopology createTopology() {
		TopologyBuilder builder= new TopologyBuilder();
		builder.setSpout("questions-spout", new ClientSpout(), 1);
		builder.setBolt("answers-bolt", new AnswerBolt(), 1).allGrouping("questions-spout");
		return builder.createTopology();
	}
	
	
	public static void main(String[] args) {
    	Logger.getRootLogger().setLevel(Level.ERROR);
    	Logger.getLogger("org.apache.http.wire").setLevel(Level.ERROR);
    	Logger.getLogger("selfish").setLevel(Level.DEBUG);
    	
		LocalCluster cluster = new LocalCluster();
		StormTopology topology = createTopology();
		cluster.submitTopology("Test-Topology", new Config(), topology);
	}
}
