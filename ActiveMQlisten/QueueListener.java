import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.jms.*;

import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.SolrServerException;


import org.apache.activemq.ActiveMQConnectionFactory;

public class QueueListener {

	static String IV = "AAAAAAAAAAAAAAAA";
	static String encryptionKey = "0123456789abcdef";
	private String brokerUrl;
	private String username;
	private String password;
	static String decrypted;
	static int id = 1;

	public QueueListener(final String brokerUrl, String username, String password) {
		this.brokerUrl = brokerUrl;
		this.username = username;
		this.password = password;
	}

	public void startReceiving(final String queueName) throws Exception{
		Connection connection = null;
		Session session = null;

		try {
			// get the connection factory
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(this.username, this.password, this.brokerUrl);
			// create connection
			connection = connectionFactory.createConnection();
			// start
			connection.start();
			// create session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			// create queue (it will create if queue doesn't exist)
			Destination queue = session.createQueue(queueName);
			//create consumer
			MessageConsumer consumer = session.createConsumer(queue);
			
			// create listener
			MessageListener messageListener = new MessageListener() {
				@Override
				public void onMessage(Message message) {
					// only text type message
					if (message instanceof TextMessage) 
					{
						TextMessage txt = (TextMessage) message;
						
						try 
						{
							//pass the crypted message to a byte array
							byte[] cipher2 = txt.getText().getBytes();
							//decrypt message
							decrypted = decrypt(cipher2, encryptionKey);
							System.out.println("Message "+id+ ": " + decrypted);
							//index message to solr
							index_msg();
							id++;	
						} 
						catch (JMSException e) 
						{
							e.printStackTrace();
							System.exit(1);
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}	
					}
				}
			};
			consumer.setMessageListener(messageListener);
			
			System.in.read();
			consumer.close();
			session.close();
			connection.close();	
		} 
		catch (Exception e) 
		{
			System.out.println("Exception while sending message to the queue" + e);
			throw e;
		}
	}
	//method to decrypt msg inside cipher
	public static String decrypt(byte[] cipherText, String encryptionKey) throws Exception{
	    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
	    SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
	    cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
	    return new String(cipher.doFinal(cipherText),"UTF-8");
	}
	// method to index messages with solr
	public static void index_msg() throws IOException, SolrServerException {
		HttpSolrServer server = new HttpSolrServer("http://localhost:8983/solr/nita");
    	SolrInputDocument doc = new SolrInputDocument();
    	doc.addField("id", id);
    	doc.addField("message", decrypted);
    	server.add(doc);
	    server.commit(); 
	}
}
