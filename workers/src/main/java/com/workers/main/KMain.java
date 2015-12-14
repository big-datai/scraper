package com.workers.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kafka.consumer.KafkaStream;

//import com.utils.aws.AWSUtils;
import com.utils.constants.UConstants;
import com.utils.queue.KafkaConsumer;
import com.workers.workers.BuildPatterns;
import com.workers.workers.GoogleAds;
import com.workers.workers.GoogleTitleId;
import com.workers.workers.PriceUpdater;
import com.workers.workers.PriceUpdaterAdvanced;

public class KMain {
	private static KMain ms_instance;
	private int threadsNumber = 20;
	private String workerName = "enrich";
	private String _broker = "localhost:2181", _topic = "gupdate";
	private String _ip = "localhost";

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	public void setThreadsNumber(int threadsNumber) {
		this.threadsNumber = threadsNumber;
	}

	private KMain() {
	}

	public static final KMain getInstance() {
		if (ms_instance == null) {
			synchronized (KMain.class) {
				if (ms_instance == null) {
					ms_instance = new KMain();
				}
			}
		}
		return ms_instance;
	}

	// #of threads workerName seeds ip
	// 10 update seeds localhost
	public static void main(String[] str) {
		try {
			new UConstants();
			KMain ss = KMain.getInstance();

			// first parameter # threads
			if (str.length > 0) {
				ss.setThreadsNumber(Integer.parseInt(str[0]));
			}
			// second parameter worker name
			if (str.length > 1) {
				ss.setWorkerName(str[1]);
			}
			if (str.length > 2) {
				ss.setTopic(str[2]);
				ss.setBroker(str[3]);
				ss._ip = str[3];
			}
			ss.execute();
		} catch (Exception e) {
			UConstants.log.error("Worker failed exception while reading from kafka " + e.getMessage());
		} finally {
		}
	}

	private void setBroker(String broker) {
		_broker = broker + ":2181";
	}

	private void setTopic(String topic) {
		_topic = topic;
	}

	public void execute() {

		if (!"localhost".contains(_ip))
			// _ip = AWSUtils.getPrivateIp(_ip);

			System.out.println("IP IS: " + _ip);
		setBroker(_ip);

		KafkaConsumer mess_consumer = new KafkaConsumer(_broker, "group2", _topic);
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(mess_consumer.topic, new Integer(threadsNumber));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = mess_consumer.consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(mess_consumer.topic);

		ExecutorService pool = Executors.newFixedThreadPool(threadsNumber);

		try {
			UConstants.log.info("number of available processors: " + Runtime.getRuntime().availableProcessors());
			for (final KafkaStream<byte[], byte[]> stream : streams) {
				pool.execute(getWorker(stream, _ip));
			}
		} catch (Exception e) {
			UConstants.log.error("exception while reading from kafka in the pool at KMain : " + e.getMessage());
		} finally {
			try {
				pool.awaitTermination(100, TimeUnit.HOURS);
				pool.shutdown();
			} catch (Exception e) {
				UConstants.log.error("pool was terminated as time is out: " + e.getMessage());
				mess_consumer.disconnect();
			}
		}
	}

	/**
	 * This method returns an instance of worker to run based on user input
	 * 
	 * @return
	 */
	public Runnable getWorker(KafkaStream<byte[], byte[]> stream, String ip) {

		switch (workerName) {
		case "html":
			return new PriceUpdater(stream, ip);
		case "updater":
			return new PriceUpdaterAdvanced(stream, ip);
		case "google":
			return new GoogleTitleId(stream, ip);
		case "ads":
			return new GoogleAds(stream, ip);
		case "patterns":
			return new BuildPatterns(stream, ip);
		default:
			return new GoogleAds(stream, ip);
		}
	}

}
