package com.workers.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;


import com.datastax.driver.core.Cluster;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.utils.messages.BigMessage;

import com.workers.utils.ReadCSV2Cassandra;
import com.utils.aws.AWSUtils;

public class CSV2CMS {

	// private KafkaConsumer m_messConsumer;// submit enriched data to the river

	
	public static String _ip = "localhost";
	public static String path2File = "/home/ec2-user/deepricer.csv";
	public long time;
	public String price_prop1 = "";
	public static final int COL_NUM=9;
	public static Cluster cluster;
	public static Session session;
	public static String _innerCassandraHost="localhost";
	
	
	

	public static LinkedList<BigMessage> loadCsv(String path2File) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		ReadCSV2Cassandra obj = new ReadCSV2Cassandra(path2File);
		return obj.loadFile();
	}

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		
		if (args.length > 1) {
			_ip = args[0];
			path2File = args[1];
			_innerCassandraHost = AWSUtils.getPrivateIp(_ip);
			   
		}
		LinkedList<BigMessage> messages = loadCsv(path2File);
		// Connect to the cluster and keyspace "demo"
		cluster = Cluster.builder().addContactPoint(_innerCassandraHost).build();
		session = cluster.connect("demo");
		for (BigMessage m : messages) {
			Statement statement = QueryBuilder.insertInto("demo", "cms_simulator")
			        .value("store_id",m.getDomain().toString())
			        .value("store_prod_id", m.storeId.toString())
			        .value("prod_brand", m.getBrand().toString())
			        .value("prod_mpn", m.getMpn().toString())
			        .value("prod_other_id", m.getOtherID().toString())
			        .value("prod_sku", m.getSku().toString())
			        .value("prod_upc", m.getUpc().toString())
			        .value("store_prod_category", m.getCat().toString())
			        .value("store_prod_price", Double.parseDouble(m.getPrice()))
			        .value("store_prod_title", m.getTitle().toString())
			        .value("store_prod_url", m.getUrl().toString());
			session.execute(statement);
								}
		System.out.println("FINISHED");
	}

}

                                                                 
