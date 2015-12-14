package com.workers.workers;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.utils.constants.UConstants;
import com.utils.messages.BigMessage;
import com.utils.queue.KafkaProducer;
import com.workers.utils.Filters;
import com.workers.utils.Utils;

public class PriceUpdaterAdvanced implements Runnable {

	// private KafkaConsumer m_messConsumer;// submit enriched data to the river
	private static KafkaProducer m_messPublisher;
	private static KafkaProducer m_logs;
	public static AtomicInteger m_counter = new AtomicInteger();
	public static String _ip = null;
	private boolean istesting = false;
	private KafkaStream<byte[], byte[]> m_stream;

	public PriceUpdaterAdvanced(KafkaStream<byte[], byte[]> stream, String ip) {
		_ip = ip;
		m_stream=stream;
		m_messPublisher = new KafkaProducer("updated", _ip);
		m_logs = new KafkaProducer("logs", _ip);
		System.out.println("producer ip : " + _ip);
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
						UConstants.log.info("the delivery object is null in " + PriceUpdaterAdvanced.class.getName());
						continue;
					}
					ms = BigMessage.string2Message(delivery);
					if (ms == null || Filters.instance().filter(ms.getUrl())) {
						UConstants.log.error("ERROR : message did not have url was null or was an escape ");
						continue;
					}
					WebClient wc = new WebClient(BrowserVersion.CHROME);
					wc.getOptions().setCssEnabled(true);
					wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
					wc.getOptions().setThrowExceptionOnScriptError(false);
					wc.getOptions().setJavaScriptEnabled(true);
					wc.getOptions().setUseInsecureSSL(true);
					int timeout = 10000;
					wc.getOptions().setTimeout(timeout);

					HtmlPage page = Utils.getHtml(ms, wc, m_logs);
					
					NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
					double price = 2.50000000000003;
					System.out.println(currencyFormatter.format(price));
					Double originalPrice = Double.parseDouble(ms.getPrice());

					Double pricePattern = 0.0;
					Double pricePatternText = 0.0;
					try {
						pricePattern = Utils.getPrice(ms.getPatternsHtml(), page.asXml(), ms.getUrl(), originalPrice,
								Integer.parseInt(ms.indexOfPattern));

						pricePatternText = Utils.getPrice(ms.getPatternsText(), page.asText(), ms.getUrl(), originalPrice,
								Integer.parseInt(ms.indexOfPatternText));

						if(pricePattern!=pricePatternText){
							UConstants.log.error("HUSTON WE GO A PROBLEM");
						}
						
						ms.setUpdatedPrice(pricePattern.toString());
					} catch (RuntimeException exception) {
						UConstants.log.error(" Pattern got stuck for document Id: " + ms.getUrl() + " Found runtime exception");
					}

				} catch (Exception e) {
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
						UConstants.log.error(" totatl success: " + m_counter.addAndGet(1) + " vs failes "
								+ " updated price: " + ms.getUpdatedPrice());

						// TODO acknowlage
					} catch (Exception ee) {
						UConstants.log.error("ack exception channel or connection problem : " + ee.getMessage());
						if (istesting) {
							break;
						}
						// m_messPublisher.disconnect();
					}
				}
			}
		} catch (Exception e) {
			UConstants.log.error("Worker has stoped working as result of a bad error" + " in class: " + e.getMessage());
			m_messPublisher.disconnect();
			m_logs.disconnect();
		}

	}

}
