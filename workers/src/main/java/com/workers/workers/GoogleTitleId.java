package com.workers.workers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import net.sourceforge.htmlunit.corejs.javascript.WrappedException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.google.common.base.Strings;
import com.utils.constants.UConstants;
import com.utils.constants.UStringUtils;
import com.utils.messages.BigMessage;
import com.utils.queue.KafkaProducer;
import com.workers.utils.Utils;

public class GoogleTitleId implements Runnable {

	// private KafkaConsumer m_messConsumer;// submit enriched data to the river
	private static KafkaProducer m_messPublisher;
	private static KafkaProducer m_messPublisherCompetitors;
	private static KafkaProducer m_logs;

	public static AtomicInteger inbox = new AtomicInteger();
	public static AtomicInteger outBox = new AtomicInteger();
	public static final String CAPTCHA = "systems have detected unusual traffic from your computer network",
			GOOGLE_CAPTCHA = "We got blocked from ggl";
	public static final int SLEEP = 17000;
	public static AtomicInteger m_withSrchResults = new AtomicInteger();
	public static AtomicInteger m_withCompResults = new AtomicInteger();
	public static AtomicInteger noGoogleResults = new AtomicInteger();

	public static AtomicInteger m_totalSrchResults = new AtomicInteger();
	public static AtomicInteger m_totalCompResults = new AtomicInteger();

	public static AtomicInteger m_outputSrchResults = new AtomicInteger();
	public static AtomicInteger m_outputCompResults = new AtomicInteger();

	public static AtomicInteger found = new AtomicInteger();
	public static AtomicInteger tb = new AtomicInteger();
	public static AtomicInteger tt = new AtomicInteger();
	public static AtomicInteger nosearchBrand = new AtomicInteger();
	public static AtomicInteger nosearchTitle = new AtomicInteger();
	public static AtomicInteger noprice = new AtomicInteger();
	public static AtomicInteger orphan = new AtomicInteger();
	// public static AtomicInteger orpahn = new AtomicInteger();
	public static String _ip = null;
	private KafkaStream<byte[], byte[]> m_stream;

	public GoogleTitleId(KafkaStream<byte[], byte[]> stream, String ip) {
		_ip = ip;
		m_stream = stream;
		m_messPublisher = new KafkaProducer("ggl", _ip);// "ec2-54-83-9-85.compute-1.amazonaws.com");//
														// _ip);
		m_messPublisherCompetitors = new KafkaProducer("preseeds", _ip);// "ec2-54-83-9-85.compute-1.amazonaws.com");//
																		// /_ip);
		m_logs = new KafkaProducer("logs", _ip);
	}

	@Override
	public void run() {
		try {
			final WebClient wc = new WebClient(BrowserVersion.CHROME);
			wc.getOptions().setThrowExceptionOnScriptError(false);
			while (true) {
				BigMessage ms = null;
				LinkedList<BigMessage> messages = null;
				LinkedList<BigMessage> messagesOfCompetitors = new LinkedList<BigMessage>();
				try {
					// Retrieving the message
					ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
					if (!it.hasNext())
						continue;

					byte[] delivery = it.next().message();
					if (delivery == null) {
						UConstants.log.error("ERROR THE DELIVERY IS NULL ");
						continue;
					}
					ms = BigMessage.string2Message(delivery);
					System.out.println(" RECIEVED MESSAGES : " + inbox.addAndGet(1) + " thread id : " + Thread.currentThread().getId());

					// get ids from google shopping
					if (ms.ggId != null && !ms.ggId.equals("") && ms.ggId.matches("\\d+")) {
						messages = getFirstSearch(ms, wc);
					} else {
						messages = new LinkedList<BigMessage>();
						messages.add(ms);
					}
					// System.out.println(" RETURN FROM GOOGLE SEARCH WITH RESULTS : "
					// + messages.size());

					if (messages.size() == 0) {
						noGoogleResults.addAndGet(1);
						continue;
					}

					BigMessage bestCandid = null;
					boolean priInd = true;
					for (BigMessage msg : messages) {
						String gglID = msg.ggId;
						Double gglPrice = Double.parseDouble(Utils.parcePrice(msg.firstGglPrice, ms.locale));
						Double pr = Double.parseDouble(Utils.parcePrice(msg.price, "en_US"));
						if (gglID != null && !gglID.equals("") && gglID.matches("\\d+")) {
							if ((pr > 0 && gglPrice > 0 && (pr / gglPrice) > 0.5 && (pr / gglPrice) < 2) || pr == 0.0 || gglPrice == 0.0 || pr < 10.0
									|| gglPrice < 10.0) {
								bestCandid = msg;
								messagesOfCompetitors = getCompetitors(bestCandid, wc);
								break;
							} else {
								noprice.getAndIncrement();
								System.out.println("+++ PROBLEM: PRICE DID NOT MATCH ORIGINAL: " + pr + " CANDIDATE PRICE : " + gglPrice
										+ " BRAND+MPN " + ms.brand + " " + ms.mpn);
							}
						}
					}
					if (!(bestCandid != null))
						UConstants.log.error("### NO COMPETITORS FOR SEARCH TITLE : " + ms.getTitle());
					// PUBLISHING STAGE ggl
					for (BigMessage m : messages) {
						try {
							Utils.postMessage(m, m_messPublisher);
							m_outputSrchResults.addAndGet(1);
						} catch (Exception eee) {
							UConstants.log.error("ERROR FAILED TO POST A MESSAGE WITH : " + eee.getMessage());
						}
					}
					boolean isOrphan = true;
					for (BigMessage m : messagesOfCompetitors) {
						try {
							if (m.url.contains(ms.domain))
								isOrphan = false;
							Utils.postMessage(m, m_messPublisherCompetitors);
							m_outputCompResults.addAndGet(1);
						} catch (Exception eee) {
							UConstants.log.error("ERROR FAILED TO POST A COMPETITOR MESSAGE WITH : " + eee.getMessage());
						}
					}
					if (isOrphan && messagesOfCompetitors.size() > 0) {
						ms.ggId = messagesOfCompetitors.getLast().ggId;
						ms.gglName = ms.upc; // temp use of upc as gglName
						Utils.postMessage(ms, m_messPublisherCompetitors);
						System.out.println("+++ PROBLEM:ORPHAN BRABD+MPN : " + ms.brand + " " + ms.mpn + "          ");
						orphan.getAndIncrement();
					}
					System.out.println("TOTAL MESSAGES RECIEVED : " + inbox.get() + " TOTAL FOUND " + found.get() + " TOTAL PRICE TOO HIGH : "
							+ noprice.get() + " orphans " + orphan.get() + " COULD NOT FIND BRANDS COUNT : " + nosearchBrand.get()
							+ " COUND NOT FIND TITLES " + nosearchTitle.get() + " of total : " + tt.get());
				} catch (Exception eee) {
					System.out.println("FAILED WITH EXCEPTION " + eee.getMessage());
					if (eee.getMessage().contains("FORCEDEXIT"))
						throw eee;
				}
			}
		} catch (Exception e) {
			UConstants.log.error("ERROR SYSTEM HAS STOPPED : " + e.getMessage());
			// KILL PROGRAM
			if (e.getMessage().contains("FORCEDEXIT")) {
				System.out.println("finally exiting");
				m_messPublisher.disconnect();
				m_messPublisherCompetitors.disconnect();
				m_logs.disconnect();
				System.exit(-1);
			}
			e.printStackTrace();
		} finally {
			System.out.println("finally exiting");
			m_messPublisher.disconnect();
			m_messPublisherCompetitors.disconnect();
			m_logs.disconnect();
			// throw new RuntimeException("");
		}
	}

	public static LinkedList<BigMessage> getFirstSearch(BigMessage ms, WebClient wc) throws FailingHttpStatusCodeException, MalformedURLException,
			IOException {
		LinkedList<BigMessage> messages = new LinkedList<BigMessage>();
		LinkedList<String> queries = new LinkedList<String>();

		if (ms.locale.equals("") || !Utils.SUPPORTED_LOCALES.contains(ms.locale)) {
			ms.locale = "en_US";
		}
		// if (ms.upc != null && ms.upc.trim().length() > 5) {
		// queries.add(ms.upc);
		// }
		// if (ms.brand != null && ms.brand.length() > 2) {
		// queries.add("\"" + ms.brand + "\"" + " " + "\"" + ms.mpn + "\"");
		// }
		// if (ms.title != null && !ms.title.isEmpty()) {
		// queries.add(ms.title);
		// }

		try {
			HtmlPage shopping = wc.getPage(Utils.GGL_SRCH_URLS.get(ms.locale));
			// for (String query : queries) {
			// SEARCH GOOGLE FOR A SPECIFIC QUERY
			String mpn = ms.mpn;
			if (ms.domain.contains("kwaliteitparket") && !mpn.isEmpty() && !mpn.equals("0")) {
				mpn = "0" + mpn;
			}
			boolean res = false;
			if (!mpn.isEmpty()) {
				res = search(shopping, ms, (ms.brand + " " + mpn).trim(), messages);
			}
			// boolean res = search(shopping, ms, "\"" + ms.brand + "\"" + " " +
			// "\"" + ms.mpn + "\"", messages);

			if (!res) {
				System.out.println("+++ PROBLEM: SEARCHED " + ms.brand + " " + ms.mpn + " DID NOT FIND RESULTS");
				nosearchBrand.getAndIncrement();
				// tt.incrementAndGet();
				if (!search(shopping, ms, ms.title, messages)) {
					nosearchTitle.getAndIncrement();
					// // break;
				}
			}
			// }
		} catch (ScriptException | WrappedException we) {
			UConstants.log.error("ERROR script exceptions");
		} catch (IOException ioe) {
			UConstants.log.error("ERROR IO : " + ioe.getMessage() + " -- " + ioe.getStackTrace());
		}
		return messages;
	}

	public static boolean search(HtmlPage shopping, BigMessage ms, String query, LinkedList<BigMessage> messages) {
		// System.out.println("SEARCH QUERY IS: " + query);
		try {
			Thread.sleep((long) (Math.random() * SLEEP));
		} catch (InterruptedException e) {
		}
		if (shopping.asText() != null && shopping.asXml().contains(CAPTCHA)) {
			systemExitOnBlocked(ms);
		}
		final HtmlForm form = shopping.getFormByName("gbqf");
		final HtmlButton button = form.getButtonByName("btnG");
		final HtmlTextInput textField = form.getInputByName("q");
		// Change the value of the text field
		textField.setValueAttribute(query);

		HtmlPage page2 = null;
		try {
			page2 = button.click();
		} catch (com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException e) {
			systemExitOnBlocked(ms);
		} catch (Exception e) {
		}

		List<?> res = page2.getByXPath("//a[@class=\"pstl\"]");
		// System.out.println(page2.asText());
		if (res.size() > 0) {
			found.incrementAndGet();
		}

		boolean hasGgl = false;
		for (int i = 0; i < res.size(); i++) {
			String rawLink = res.get(i).toString();
			if (rawLink.contains("href=\"/url?url=/shopping/product/") || rawLink.contains("href=\"/shopping/product/")) {
				hasGgl = true;
			}
		}// FIRST SEARCH DID NOT BRING COMPETITORS but had results
		if (!hasGgl && res.size() > 0) {
			return true;
		}
		if (!hasGgl) {
			return false;
		}
		List<?> titles = page2.getByXPath("//a[@class=\"pstl\"]/text()");
		List<?> competitores = page2.getByXPath("//div[@class=\"_tyb shop__secondary\"]/text()");
		// //*[@id="rso"]/div/div/div[1]/div[1]/div[2]/div[1]/div/span/b
		String[] prices = page2.getByXPath("//div[@class=\"_tyb shop__secondary\"]/span/b/text()").toString()
				.replaceAll("[\\[\\]" + Utils.CURRENCY_SYMBOLS + "]", "").split(", ");
		if (page2.asText().contains(CAPTCHA)) {
			systemExitOnBlocked(ms);
		}
		if (res.size() != titles.size() && res.size() != competitores.size() && prices != null && prices.length != res.size()) {
			UConstants.log.error("ERROR BAD PARSING SIZES ARE NOT EQUAL");
		}

		for (int i = 0; i < res.size(); i++) {
			String rawLink = res.get(i).toString();
			int start = rawLink.indexOf("product") + 8;
			String num = rawLink.substring(start).replaceAll("[^\\d]", "%").replace("%%", "%");
			int end = num.indexOf("%") + start;
			String id = res.get(i).toString().substring(start, end);
			String title = titles.get(i).toString();
			String stores = competitores.get(i).toString();
			// System.out.println(id + " titles:   " + title + stores);
			BigMessage m = new BigMessage(ms);
			m.gTitle = title;
			m.competitors = stores;
			m.ggId = id;
			m.gNotParsed = rawLink;
			if (prices != null)
				m.firstGglPrice = prices[i];
			messages.add(m);
			// System.out.println(m.toJson().toString());
		}
		return true;
	}

	public static void systemExitOnBlocked(BigMessage ms) {
		UConstants.log.error("ERROR CAPTCHA WE GOT BLOCKED ! ! !");
		try {
			ms.errorMessage = GOOGLE_CAPTCHA;
			Utils.postMessage(ms, m_logs);
		} catch (Exception eee) {
			UConstants.log.error("ERROR FAILED TO POST A MESSAGE WITH : " + eee.getMessage());
		}
		throw new RuntimeException("FORCEDEXIT");
	}

	public static LinkedList<BigMessage> getCompetitors(BigMessage ms, WebClient wc) throws FailingHttpStatusCodeException, MalformedURLException,
			IOException {

		try {
			Thread.sleep((long) (Math.random() * SLEEP));
		} catch (InterruptedException e) {
		}
		LinkedList<BigMessage> messages = new LinkedList<BigMessage>();

		String gglID = "";
		if (!Strings.isNullOrEmpty(ms.ggId) && !"0".equals(ms.ggId)) {
			gglID = ms.ggId;
		} else {
			UConstants.log.error("BAD INPUT MESSAGE/ MISSING ggId ");
			return messages;
		}
		if (ms.locale.isEmpty() || !Utils.SUPPORTED_LOCALES.contains(ms.locale)) {
			ms.locale = "en_US"; // default
		}

		HtmlPage competitors = wc.getPage(Utils.GGL_SRCH_URLS.get(ms.locale) + "/product/" + gglID);
		if (competitors.asText().contains(CAPTCHA)) {
			systemExitOnBlocked(ms);
		}

		HtmlTable table = (HtmlTable) competitors.getFirstByXPath("//*[@id=\"os-sellers-table\"]");
		int numRows = 0;
		if (!(table != null) || table.getRowCount() < 2) {
			UConstants.log.error("THERE IS NO COMPETITORS");
			return messages;
		} else {
			numRows = table.getRowCount();
		}
		for (int i = 2; i <= numRows; i++) { // starting from row 2
			try {
				BigMessage msgTemp = new BigMessage(ms);
				String href = ((HtmlAnchor) competitors.getByXPath("//*[@id=\"os-sellers-table\"]/tbody/tr[" + i + "]/td[1]/span[1]/a").get(0))
						.getAttribute("href");
				msgTemp.url = Utils.getCleanURL(href);
				if (msgTemp.url.isEmpty()) {
					msgTemp.url = href;
				}
				// //*[@id="os-sellers-table"]/tbody/tr[2]/td[1]/span/a
				// //*[@id="os-sellers-table"]/tbody/tr[2]/td[1]/span/a
				msgTemp.gglName = competitors.getByXPath("//*[@id=\"os-sellers-table\"]/tbody/tr[" + i + "]/td[1]/span[1]/a/text()").get(0)
						.toString().replace(" ", "_");
				msgTemp.domain = UStringUtils.getDomainName(ms.url);
				String basePrice = ((HtmlSpan) competitors.getByXPath("//*[@id=\"os-sellers-table\"]/tbody/tr[" + i + "]/td[4]/span").get(0))
						.asText();
				msgTemp.price = basePrice.replaceAll(Utils.CURRENCY_SYMBOLS, "").trim();
				Pattern pattern = Pattern.compile(Utils.CURRENCY_SYMBOLS);
				Matcher matcher = pattern.matcher(basePrice);
				if (matcher.find()) {
					msgTemp.currency = matcher.group(0);
				} else {
					msgTemp.currency = "Not Found Symbol";
				}
				String basePriceCleaned = basePrice.replaceAll(Utils.CURRENCY_SYMBOLS, "").trim();

				msgTemp.basePrice = Utils.parcePrice(basePriceCleaned, msgTemp.locale);
				if (msgTemp.basePrice.isEmpty())
					continue;

				String priceAdditionText = ((HtmlDivision) competitors.getByXPath("//*[@id=\"os-sellers-table\"]/tbody/tr[" + i + "]/td[4]/div").get(
						0)).asText();
				msgTemp.priceAdditionText = priceAdditionText;

				if (priceAdditionText.length() > 5) { // then parse number
					String numericAddition = priceAdditionText.replaceAll("[^\\d.,]", ""); // remove
					msgTemp.priceAddition = Utils.parcePrice(numericAddition, msgTemp.locale);
				}

				String totalPrice = ((HtmlTableDataCell) competitors.getByXPath("//*[@id=\"os-sellers-table\"]/tbody/tr[" + i + "]/td[5]").get(0))
						.asText();
				if (totalPrice.length() > 5) { // then parse number
					String totalPriceCleaned = totalPrice.replaceAll(Utils.CURRENCY_SYMBOLS, ""); // remove
					msgTemp.totalPrice = Utils.parcePrice(totalPriceCleaned, msgTemp.locale);
				}
				// TODO add store here to filter republished and competitors
				// Refurbished Used
				// if (ms.domain.contains("discountcomputercenter") ||
				// "discountcomputercenter".contains(ms.domain)
				if (!(msgTemp.details.contains("Refurbished") || msgTemp.details.contains("Used"))) {
					messages.add(msgTemp);
				}
			} catch (java.lang.IndexOutOfBoundsException e) {
			}
		}

		return messages;
	}
}
