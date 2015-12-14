package com.utils.queue;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class KafkaProducer {
	Properties props = new Properties();
	ProducerConfig config;
	Producer<String, String> producer;
	String topic;

	public KafkaProducer(String topic, String ip) {
		System.out.println("starting producer with ip : "+ip);
		props.put("metadata.broker.list", ip + ":9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		// props.put("partitioner.class", "example.producer.SimplePartitioner");
		props.put("request.required.acks", "1");
		props.put("producer.type", "async");
		props.put("compression.codec", "gzip");
		props.put("batch.num.messages", "1");
		this.topic = topic;
		config = new ProducerConfig(props);
		
	}

	public void send(String msg) {
		producer= new Producer<String, String>(config);
		KeyedMessage<String, String> data = new KeyedMessage<String, String>(
				this.topic, msg);
		producer.send(data);
		
		producer.close();
	}

	public void disconnect() {
		producer.close();
	}

	public static void main(String[] str) {
		KafkaProducer prod = new KafkaProducer("test", null);
		prod.send("the best message for test");
	}
}
