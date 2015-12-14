package com.utils.queue;

import java.util.Properties;

import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaConsumer {
	public final ConsumerConnector consumer;
	public final String topic;

	// creates consumer connector
	public KafkaConsumer(String a_zookeeper, String a_groupId, String a_topic) {
		consumer = kafka.consumer.Consumer
				.createJavaConsumerConnector(createConsumerConfig(a_zookeeper,
						a_groupId));
		this.topic = a_topic;
	}

	private static ConsumerConfig createConsumerConfig(String a_zookeeper,
			String a_groupId) {
		Properties props = new Properties();
		props.put("zookeeper.connect", a_zookeeper);
		props.put("group.id", a_groupId);
		props.put("zookeeper.session.timeout.ms", "100000");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.commit.enable", "true");
		props.put("auto.offset.reset", "smallest");
		props.put("queued.max.message.chunks", "1");
		props.put("replica.fetch.max.bytes", "500000");
		// props.put("message.max.bytes", "490000");
		props.put("rebalance.max.retries", "1000");
		props.put("rebalance.backoff.ms", "15000");
		
		return new ConsumerConfig(props);
	}

	public void disconnect() {
		consumer.shutdown();
	}

}
