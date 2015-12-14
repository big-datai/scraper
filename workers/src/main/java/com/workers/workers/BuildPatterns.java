package com.workers.workers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.Strings;
import com.utils.constants.UConstants;
import com.utils.messages.BigMessage;
import com.utils.queue.KafkaProducer;
import com.workers.utils.Utils;

public class BuildPatterns implements Runnable {

	private static KafkaProducer m_messPublisher;
	private static KafkaProducer m_logs;
	public static AtomicInteger inbox = new AtomicInteger();
	public static AtomicInteger htmls = new AtomicInteger();
	public static AtomicInteger handeled = new AtomicInteger();
	public static AtomicInteger failed = new AtomicInteger();
	public static String _ip = null;

	private KafkaStream<byte[], byte[]> m_stream;

	public BuildPatterns(KafkaStream<byte[], byte[]> stream, String ip) {
		_ip = ip;
		m_stream = stream;
		m_messPublisher = new KafkaProducer("ghtmls", _ip);
		m_logs = new KafkaProducer("logs", _ip);
		System.out.println("producer ip : " + _ip);
	}

	@Override
	public void run() {
		try {
			Thread.sleep((long) Math.random() * 1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
				
				System.out.println("TOTAL EMOUNT OF RECIEVED MESSAGES : " + inbox.getAndAdd(1));
				/* && Filters.instance().filter(ms.getUrl())) */
				
				if (ms == null || Strings.isNullOrEmpty(ms.ggId) || Strings.isNullOrEmpty(ms.url)) {
					UConstants.log.error("ERROR : empty message of ggId is null " + ms.toJson());
					continue;
				}
				java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
				java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter").setLevel(Level.OFF);
				WebClient wc = new WebClient(BrowserVersion.CHROME);
				wc.getOptions().setCssEnabled(false);
				wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
				wc.getOptions().setThrowExceptionOnScriptError(false);
				wc.getOptions().setJavaScriptEnabled(true);
				wc.getOptions().setUseInsecureSSL(true);
				wc.setCssErrorHandler(new SilentCssErrorHandler());
				wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
				wc.getOptions().setThrowExceptionOnScriptError(false);
				wc.getOptions().setRedirectEnabled(true);
				wc.getOptions().setAppletEnabled(false);
				wc.getOptions().setPopupBlockerEnabled(true);
				wc.getOptions().setPrintContentOnFailingStatusCode(false);
				wc.getOptions().setTimeout(15000);

				HtmlPage page = Utils.getHtml(ms, wc, m_logs);

				UConstants.log.error("GOT HTMLS FOR :" + htmls.getAndIncrement() + " OUT OF TOTAL " + inbox.get());
				// If we got a html
				try {
					ms = Utils.parseHtml2MS(ms, page);
					try {
						if (ms.errorMessage.equals("")) {
							UConstants.log.error("SUCCESS COUNTER : " + handeled.addAndGet(1) + "vs TOTAL " + inbox.get());
							Utils.postMessage(ms, m_messPublisher);
						} else {
							UConstants.log.error(ms.errorMessage + failed.addAndGet(1) + "vs TOTAL " + inbox.get());
							Utils.postMessage(ms, m_logs);
						}
					} catch (Exception ee) {
						UConstants.log.error("ERROR FAILD TO SEND MESSAGE TO THE QUEUE : " + failed.addAndGet(1) + "vs TOTAL " + inbox.get() + " "
								+ ee.getMessage());
					}
				} catch (Exception ec) {
					UConstants.log
							.error("ERROR FAILD CREATING PATTERN FROM HTML" + ms.errorMessage + failed.addAndGet(1) + "vs TOTAL " + inbox.get());
				}
				// UConstants.log.info("ERROR FAILD BRINGING HTMLS : " +
				// ms.errorMessage + failed.addAndGet(1) + "vs TOTAL " +
				// inbox.get());
			} catch (Exception e) {
				UConstants.log.error("ERROR: [ BUILDPATTERN THREAD HAS STOPPED WORKING ]" + " in class: " + e.getMessage());
				m_messPublisher.disconnect();
				m_logs.disconnect();
			}

		}

	}
}
