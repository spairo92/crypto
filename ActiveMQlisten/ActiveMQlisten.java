import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;

public class ActiveMQlisten {

	public static void main(String[] args)throws IOException, SolrServerException{
		QueueListener sender = new QueueListener("tcp://localhost:61616", "admin", "admin");
		try {
			sender.startReceiving("spyro's_queue");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
