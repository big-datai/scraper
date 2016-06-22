package com.workers.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.utils.messages.BigMessage;
import com.utils.queue.KafkaProducer;
import com.workers.utils.ReadCSV;
import com.workers.utils.Utils;

public class CSV2Kafka {

	// private KafkaConsumer m_messConsumer;// submit enriched data to the river
	private static KafkaProducer m_messPublisher;
	public static AtomicInteger m_counter = new AtomicInteger();
	public static AtomicInteger m_exceptionsCounter = new AtomicInteger();
	public static AtomicInteger m_life = new AtomicInteger();
	public static String _ip = "54.225.122.3";
	public static String path2File = "/home/ec2-user/deepricer.csv";
	public long time;
	public String price_prop1 = "";
	public static final int COL_NUM=9;
	public static String topic="gupdate";
	
	public CSV2Kafka(String ip, String t) {
		_ip = ip;
		m_messPublisher = new KafkaProducer(t, _ip);
	}

	public static LinkedList<BigMessage> loadCsv(String path2File) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		ReadCSV obj = new ReadCSV(path2File);
		return obj.loadFile();
	}

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		
		if (args.length > 1) {
			_ip = args[0];
			path2File = args[1];
		}
		if (args.length > 2) {
			_ip = args[0];
			path2File = args[1];
			topic=args[2];
		}
		System.out.println(topic);
		LinkedList<BigMessage> messages = loadCsv(path2File);
		new CSV2Kafka(_ip, topic);
		for (BigMessage m : messages) {
			Utils.postMessage(m, m_messPublisher);
		}
		System.out.println("FINISHED");
		return;
	}

}
