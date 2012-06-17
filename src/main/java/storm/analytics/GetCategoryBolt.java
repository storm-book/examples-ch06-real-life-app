package storm.analytics;

import java.util.Map;

import storm.analytics.utilities.Product;
import storm.analytics.utilities.ProductsReader;
import storm.analytics.utilities.NavigationEntry;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class GetCategoryBolt extends BaseBasicBolt {
	private static final long serialVersionUID = 1L;
	private ProductsReader reader;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		String host = stormConf.get("redis-host").toString();
		int port = Integer.valueOf(stormConf.get("redis-port").toString());

		this.reader = new ProductsReader(host, port); 
		super.prepare(stormConf, context);
	}
	
	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		NavigationEntry entry = (NavigationEntry)input.getValue(1);
		if("PRODUCT".equals(entry.getPageType())){
			try {
				String product = (String)entry.getOtherData().get("product");

				// Call the items API to get item information
				Product itm = reader.readItem(product);
				if(itm ==null)
					return ;

				String categ = itm.getCategory();

				collector.emit(new Values(entry.getUserId(), product, categ));

			} catch (Exception ex) {
				System.err.println("Error processing PRODUCT tuple"+ ex);
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("user","product", "categ"));
	}
}
