package com.workers.workers;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.utils.constants.UConstants;
import com.utils.messages.BigMessage;
import com.utils.queue.KafkaProducer;
import com.workers.utils.Filters;
import com.workers.utils.Utils;
import com.workers.utils.UtilsOld;

public class PriceUpdater implements Runnable {

	//private KafkaConsumer m_messConsumer;// submit enriched data to the river
	private static KafkaProducer m_messPublisher;
	private static KafkaProducer m_logs;
	public static AtomicInteger m_counter = new AtomicInteger();
	public static AtomicInteger m_exceptionsCounter = new AtomicInteger();
	public static AtomicInteger m_life = new AtomicInteger();
	public static String _ip = null;
	final private WebClient m_webClient = new WebClient();
	public long time;
	private boolean istesting = false;
	public String price_prop1 = "";
	private KafkaStream<byte[], byte[]> m_stream;

	public PriceUpdater(KafkaStream<byte[], byte[]> stream, String ip) {
		_ip = ip;
		m_webClient.getOptions().setCssEnabled(false);
		m_webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		m_webClient.getOptions().setThrowExceptionOnScriptError(false);
		m_webClient.getOptions().setJavaScriptEnabled(true);
		m_webClient.getOptions().setUseInsecureSSL(true);
		int timeout = 30000;
		m_stream = stream;
		// Add timeout for ETA
		m_webClient.getOptions().setTimeout(timeout);
		// zooKeeper, groupId, topic
		// connect to publish data for river
		m_messPublisher = new KafkaProducer("htmls", _ip);
		m_logs = new KafkaProducer("logs", _ip);
		System.out.println("producer ip : "+ _ip);
	}

	public Double getPriceText(String price_patterns, String asText, String url, Double original_price, String url2) {
		ArrayList<Double> priceList = new ArrayList<Double>();
		String[] tokens = Utils.getPriceToken(price_patterns);
		for (String price_pattern : tokens) {
			if (price_pattern != null && price_pattern.trim().length() > 0 && price_pattern.contains(UtilsOld.REGEX_PRICE_PATTERN)) {
				double price_val = 0.0;
				price_val = UtilsOld.pricePatternMatchAlgo(price_pattern.toUpperCase(),
						UtilsOld.fetchHtmlWithAlphabetNumber(asText).toUpperCase(), url, original_price);
				if (price_val > 0.0) {
					priceList.add(price_val);
				}
			}
		}
		return UtilsOld.findPrice(priceList, original_price, UtilsOld.PRICE_RANGE);
	}

	public double getPrice(String price_patterns, String htmlPage, String url, double original_price, String es_id) throws RuntimeException {

		ArrayList<Double> priceList = new ArrayList<Double>();
		String[] tokens = Utils.getPriceToken(price_patterns);
		for (String price_pattern : tokens) {
			if (price_pattern != null && price_pattern.trim().length() > 0 && price_pattern.contains(UtilsOld.REGEX_PRICE_PATTERN)) {
				double price_val = 0.0;
				price_val = UtilsOld.pricePatternMatchAlgo(price_pattern, UtilsOld.fetchHtmlWithAlphabetNumber(htmlPage), url,
						original_price);
				if (price_val > 0.0) {
					priceList.add(price_val);
				}
			}
		}
		return UtilsOld.findPrice(priceList, original_price, UtilsOld.PRICE_RANGE);

	}

	@Override
	public void run() {
		try {
			while (true) {
				BigMessage ms = null;
				try {
					// Retrieving the message
					ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
					if (!it.hasNext())
						continue;

					byte[] delivery = it.next().message();
					if (delivery == null) {
						UConstants.log.info("the delivery object is null in " + PriceUpdater.class.getName());
						continue;
					}
					ms = BigMessage.string2Message(delivery);
					if (ms == null || Filters.instance().filter(ms.getUrl())) {
						UConstants.log.error("ERROR : message did not have url was null or was an escape ");
						continue;
					}

					HtmlPage page = Utils.getHtml(ms, m_webClient, m_logs);

					Double original_price = Double.parseDouble(ms.getPrice());

					Double price_new = 0.0;
					boolean pattern_exception = false;
					try {
						price_new = getPrice(ms.getPatternsHtml(), page.asXml(), ms.getUrl(), original_price, ms.getUrl());
						ms.setUpdatedPrice(price_new.toString());
						// could not get the price try again with text of from
						// //html
						// could not get the price try again with text of from
						// html
						if (price_new == 0.0) {
							// price_new = getPrice(ms.getPrice_prop_anal(),
							// page.asText(), ms.getUrl(), original_price,
							// ms.getUrl());
						}
						if (price_new == 0.0) {
							// int dist = 10;
							// price_new = getLevenPrice(price_patterns,
							// page.asText(), original_price, dist);
						}
						if (price_new == 0.0) {
							// ms.setPrice_updated("0.0");
						}
					} catch (RuntimeException exception) {
						m_exceptionsCounter.addAndGet(1);
						pattern_exception = true;
						UConstants.log.error(" Pattern got stuck for document Id: " + ms.getUrl() + " Found runtime exception");
					}
					if (price_new > 0.0) {
						ms.setUpdatedPrice(price_new.toString());
						ms.setShipping("0");
					} else if (pattern_exception) {
						ms.setShipping("3");
						ms.setUpdatedPrice(price_new.toString());
					} else {
						ms.setShipping("1");
						ms.setUpdatedPrice(price_new.toString());
					}

				} catch (Exception e) {
					m_exceptionsCounter.addAndGet(1);
					UConstants.log.info(" all exceptions : " + e.getMessage());
					Utils.postMessage(ms, m_logs);
					if (ms != null && !e.getMessage().equals("skip exception")) {
						ms.setUpdatedPrice("0.0");
						ms.setShipping("2");
						ms.sethtml(e.getMessage());
					}

				} finally {
					try {
						Utils.postMessage(ms, m_messPublisher);
						UConstants.log.error(" totatl success: " + m_counter.addAndGet(1) + " vs failes " + m_exceptionsCounter.get()
								+ " updated price: " + ms.getUpdatedPrice());
						
						// TODO acknowlage
					} catch (Exception ee) {
						UConstants.log.error("ack exception channel or connection problem : " + ee.getMessage());
						if (istesting) {
							break;
						}
						//m_messPublisher.disconnect();
					}
				}
			}
		} catch (Exception e) {
			UConstants.log.error("Worker has stoped working as result of a bad error" + " in class: " + e.getMessage());
		} finally {
			//m_messConsumer.disconnect();
			m_messPublisher.disconnect();
			m_logs.disconnect();

		}

	}
	
}
