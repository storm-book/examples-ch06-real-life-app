package storm.analytics;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class TopologyStarter {
	public final static String REDIS_HOST = "localhost";
	public final static int REDIS_PORT = 6379;
	public final static String WEBSERVER = "http://localhost:3000/news";
	public final static long DOWNLOAD_TIME = 100;
	public static boolean testing = false;

	public static void main(String[] args) {
        Logger.getRootLogger().removeAllAppenders();

		TopologyBuilder builder = new TopologyBuilder();
        
        builder.setSpout("read-feed", new UsersNavigationSpout(), 3);
        
        builder.setBolt("get-categ", new GetCategoryBolt(), 3)
        				.shuffleGrouping("read-feed");
        
        builder.setBolt("user-history", new UserHistoryBolt(), 5)
        				.fieldsGrouping("get-categ", new Fields("user"));
        
        builder.setBolt("product-categ-counter", new ProductCategoriesCounterBolt(), 5)
        				.fieldsGrouping("user-history", new Fields("product"));
        
        if(!testing)
        	builder.setBolt("news-notifier", new NewsNotifierBolt(), 5)
        				.shuffleGrouping("product-categ-counter");
        
        Config conf = new Config();
        conf.setDebug(true);

        conf.put("redis-host", REDIS_HOST);
        conf.put("redis-port", REDIS_PORT);
        conf.put("webserver", WEBSERVER);
        conf.put("download-time", DOWNLOAD_TIME);
        
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("analytics", conf, builder.createTopology());
	}
}