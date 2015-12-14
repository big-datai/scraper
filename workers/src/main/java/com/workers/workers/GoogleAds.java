package com.workers.workers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.NumberUtils;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCitation;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.utils.constants.UConstants;
import com.utils.messages.BigMessage;
import com.utils.queue.KafkaProducer;
import com.workers.utils.Utils;

public class GoogleAds implements Runnable {

	// private KafkaConsumer m_messConsumer;// submit enriched data to the river
	private static KafkaProducer m_logs;

	public static AtomicInteger counter = new AtomicInteger();
	public static AtomicInteger outBox = new AtomicInteger();
	public static final String CAPTCHA = "systems have detected unusual traffic from your computer network",
			GOOGLE_CAPTCHA = "We got blocked from ggl";
	public static final int SLEEP = 10000;
	public static Cluster cluster;
	public static Session session;
	// public static AtomicInteger orpahn = new AtomicInteger();
	public static String _ip = null;
	private KafkaStream<byte[], byte[]> m_stream;
	public static String host = "107.20.157.48";// "deepricer.com";

	public GoogleAds(KafkaStream<byte[], byte[]> stream, String ip) {
		_ip = ip;
		m_stream = stream;
	}

	@Override
	public void run() {
		try {
			cluster = Cluster.builder().addContactPoint(host).build();
			session = cluster.connect("demo");
			final WebClient wc = new WebClient(BrowserVersion.CHROME);
			wc.getOptions().setThrowExceptionOnScriptError(false);
			wc.getOptions().setJavaScriptEnabled(true);
			wc.getOptions().setCssEnabled(true);

//			 final WebClient proxy = new WebClient(BrowserVersion.CHROME);
//			 proxy.getOptions().setThrowExceptionOnScriptError(false);
//			 proxy.getOptions().setJavaScriptEnabled(true);
//			 proxy.getOptions().setCssEnabled(true);
//			 HtmlPage shopping =
//			 proxy.getPage("https://proxy-us.hide.me/go.php?u=50gfghVrLr0P1jtKmGoOQcTGKj5IBD7V%2FGNftsgj&b=5");
//			 final HtmlForm form = shopping.getForms().get(0);
//			 HtmlSubmitInput button =
//			 form.getFirstByXPath("//*[@id=\"content\"]/form/input[2]");
//			 final HtmlTextInput textField = form.getInputByName("u");
//			 // Change the value of the text field
//			 textField.setValueAttribute("https://www.google.com");
//			 HtmlPage pageProxy = null;
//			 pageProxy = button.click();

			HtmlPage pageProxy = wc.getPage("https://google.com");
			while (true) {
				BigMessage ms = null;
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

					LinkedList<BigMessage> messages = new LinkedList<BigMessage>();

					ms = BigMessage.string2Message(delivery);
					if (ms.getMpn() != null && !ms.getMpn().isEmpty() && NumberUtils.isNumber(ms.mpn.replace("-", "")) && !ms.brand.equals("NONE")) {
						searchAds(pageProxy, ms, ms.mpn + " " + ms.brand, messages);
					} else {
						searchAds(pageProxy, ms, ms.mpn, messages);
					}
					// messages = getFirstSearch(ms, wc);
				} catch (Exception eee) {
					if (eee.getMessage().contains("FORCEDEXIT"))
						throw eee;
				}
			}
		} catch (Exception e) {
			UConstants.log.error("ERROR SYSTEM HAS STOPPED : ");
			e.printStackTrace();
			// KILL PROGRAM
			if (e.getMessage().contains("FORCEDEXIT")) {
				System.out.println("finally exiting");
				System.exit(-1);
				// cluster.close();
			}
			e.printStackTrace();
		} finally {
			System.out.println("finally exiting");
			// throw new RuntimeException("");
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean searchAds(HtmlPage shopping, BigMessage ms, String query, LinkedList<BigMessage> messages) {
		// System.out.println("SEARCH QUERY IS: " + query);
		try {
			Thread.sleep((long) (Math.random() * SLEEP));
		} catch (InterruptedException e) {
		}
		if (shopping.asText() != null && shopping.asXml().contains(CAPTCHA)) {
			systemExitOnBlocked(ms);
		}
		// System.out.println(shopping.asText());
		final HtmlForm form = shopping.getFormByName("f");
		final HtmlButton button = form.getButtonByName("btnG");
		final HtmlTextInput textField = form.getInputByName("q");
		// Change the value of the text field
		textField.setValueAttribute(query);
		System.out.println("++++" + query);
		HtmlPage page2 = null;
		try {
			page2 = button.click();
		} catch (com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException e) {
			systemExitOnBlocked(ms);
		} catch (Exception e) {
		}
		String ptop = "//div[@class='_Ak c'  and @id='tads']//li[@class=\"ads-ad\"]", pshrink = "//div[@class=\"_PD\"]", pshopping = "//div[@class=\"_Dw\"]", pside = "//div[@class='_Ak']//li[@class=\"ads-ad\"]", pshrink2 = "//div[@class=\"_Dad\"]", 
				pbottom = "//div[@id=\"bottomads\"]//div[@class='_Ak c']//li[@class=\"ads-ad\"]", pbottom2 = "//div[@class='_Ak c' and @id='tadsb']//li[@class=\"ads-ad\"]";
		List<HtmlListItem> adsTop = (List<HtmlListItem>) page2.getByXPath(ptop);
		List<HtmlListItem> adsSide = (List<HtmlListItem>) page2.getByXPath(pside);
		List<HtmlDivision> shoppings = (List<HtmlDivision>) page2.getByXPath(pshopping);
		List<HtmlDivision> shoppingShrink = (List<HtmlDivision>) page2.getByXPath(pshrink);
		List<HtmlDivision> shoppingShrink2 = (List<HtmlDivision>) page2.getByXPath(pshrink2);
		List<HtmlListItem> bottom = (List<HtmlListItem>) page2.getByXPath(pbottom);
		if (bottom.size() < 1) {
			bottom = (List<HtmlListItem>) page2.getByXPath(pbottom2);
		}

		if (shoppings.size() == 0 && shoppingShrink.size() == 0 && shoppingShrink2.size() == 0) {
			System.out.println();
		}
		for (int i = 0; i < adsTop.size(); i++) {
			messages.add(parseAd(adsTop.get(i), ms, "TOP", i, ptop));
		}
		for (int i = 0; i < shoppings.size(); i++) {
			messages.add(parseShopping(shoppings.get(i), ms, "SHOPPING", i, pshopping));
		}
		for (int i = 0; i < shoppingShrink.size(); i++) {
			messages.add(parseShoppingShort(shoppingShrink.get(i), ms, "SHOPPING_SHORT", i, pshrink));
		}
		for (int i = 0; i < shoppingShrink2.size(); i++) {
			messages.add(parseShoppingShort2(shoppingShrink.get(i), ms, "SHOPPING_SHORT", i, pshrink2));
		}
		for (int i = 0; i < adsSide.size(); i++) {
			messages.add(parseAd(adsSide.get(i), ms, "SIDE", i, pside));
		}
		for (int i = 0; i < bottom.size(); i++) {
			messages.add(parseAd(adsTop.get(i), ms, "BOTTOM", i, pbottom));
		}

		logicToCassandra(messages, "", adsTop.size(), adsSide.size(), shoppings.size(), shoppingShrink.size(), shoppingShrink2.size());
		return true;
	}

	public static BigMessage parseAd(HtmlListItem li, BigMessage inbox, String location, Integer i, String path) {
		BigMessage out = new BigMessage(inbox);
		out.domain = "";
		if (li == null)
			return out;
		try {
			out.domain = ((HtmlCitation) li.getByXPath(path + "//div[@class=\"ads-visurl\"]/cite").get(i)).asText();
			String fullAd = li.asText();
			int priceInd = fullAd.indexOf("$");
			out.details = location;
			out.rating = String.valueOf(i + 1);
			out.html = fullAd;
			if (priceInd > 0) {
				String cand = fullAd.substring(priceInd, Math.min(priceInd + 20, fullAd.length())).replaceAll("[\\$A-Za-z\\s]", "");
				String price = Utils.parcePrice(cand, inbox.locale);
				out.totalPrice = price;
			}
			// to skip next 2 chars
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	public static BigMessage parseShopping(HtmlDivision div, BigMessage inbox, String location, Integer i, String path) {

		BigMessage out = new BigMessage(inbox);
		try {
			out.domain = "";
			List<HtmlSpan> ss = (List<HtmlSpan>) div.getByXPath(path + "//span[@class=\"_kh\" and ./text()[contains(.,'$')]]");
			List<HtmlSpan> dd = (List<HtmlSpan>) div.getByXPath(path + "//span[@class=\"jackpot-merchant\"]//span[@class=\"rhsg4\"]");
			if (ss != null & ss.size() > 0) {
				out.totalPrice = Utils.parcePrice((ss.get(i)).asText().replace("$", "").replace(Utils.CURRENCY_SYMBOLS, ""), out.locale);
			}
			out.details = "SHOPPINGLISTS";
			out.rating = String.valueOf(i + 1);
			if (dd != null && dd.size() > 0) {
				out.gglName = (dd.get(i)).asText();
			}
			if (out.gglName != null && !out.gglName.isEmpty()) {
				out.domain = out.gglName;
			}
			// i=i+2;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	/**
	 * for top shopping box
	 * 
	 * @param div
	 * @param inbox
	 * @param location
	 * @param i
	 * @param path
	 * @return
	 */
	public static BigMessage parseShoppingShort(HtmlDivision div, BigMessage inbox, String location, int i, String path) {

		BigMessage out = new BigMessage(inbox);
		try {// shopping on top platinum
			out.domain = "";
			out.totalPrice = Utils.parcePrice(((com.gargoylesoftware.htmlunit.html.HtmlDivision) div.getByXPath(path + "//div[@class=\"_QD _pvi\"]")
					.get(i)).asText().replace("$", ""), out.locale);
			out.gglName = ((com.gargoylesoftware.htmlunit.html.HtmlDivision) div.getByXPath(path + "//div[@class=\"pla-unit-title\"]").get(i))
					.asText();
			out.details = "SHOOPINGBOXS";
			out.rating = String.valueOf(i + 1);
			List<?> domains = div.getByXPath(path + "//div[@class=\"_mC\"]/span[@class=\"a\"]");
			// on the top
			if (domains != null && domains.size() > 0 && domains.size() > i) {
				out.domain = ((com.gargoylesoftware.htmlunit.html.HtmlSpan) domains.get(i)).asText();
			} else {// side square position
				List<?> d = div.getByXPath(path + "//div[@class=\"_mC\"]/span[@class=\"rhsg4 a\"]");
				if (d.size() > 0 && d.size() > i) {
					out.domain = ((com.gargoylesoftware.htmlunit.html.HtmlSpan) d.get(i)).asText();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	public static BigMessage parseShoppingShort2(HtmlDivision div, BigMessage inbox, String location, int i, String path) {

		BigMessage out = new BigMessage(inbox);
		try {// shopping on top platinum
			out.domain = "";
			out.totalPrice = Utils.parcePrice(
					((com.gargoylesoftware.htmlunit.html.HtmlDivision) div.getByXPath(path + "//div[@class=\"_Bad\"]").get(i)).asText().replace("$",
							""), out.locale);
			out.gglName = ((com.gargoylesoftware.htmlunit.html.HtmlDivision) div.getByXPath(path + "//div[@class=\"_Aad\"]").get(i)).asText();
			out.rating = String.valueOf(i + 1);
			// on the top
			if (out.gglName != null) {
				out.domain = out.gglName;
			}
			out.details = "SHOOPINGBOXS";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	public static void logicToCassandra(LinkedList<BigMessage> list, String file, int top, int side, int shopping, int shrink, int shrink2) {
		BigMessage me = null;// new BigMessage();
		try {
			if (list.size() < 1) {
				System.out.println("LIST SIZE IS 0 !!!");
				return;
			}
			me = list.getFirst();
			int count = 7;
			if (shrink > 0) {
				count = top + shrink + 3;
			} else if (shopping > 0) {
				count = top + shopping;
			} else if (shrink2 > 0) {
				count = top + shrink2 + 4;
			}

			// is on the screen
			String isOnScreen = "NOSCREEN";
			int location = 0;
			for (int i = 0; i < list.size(); i++) {
		//TODO CHANGE FOR NEW DOMAIN		
				if (list.get(i).domain.toLowerCase().contains("itdevicesonline")
						|| list.get(i).domain.toLowerCase().contains("water-softeners-filters")) {
					location = i;
				}
			}
			if (location > 0 && location < count) {
				isOnScreen = "SCREEN";
			}
			if (count > 7) {
				count = 7;
			}
			String line = me.mpn + ", " + me.price + ", suggestion, " + me.rating + ", " + me.details + ", ";
			Map<String, String> map = new HashMap<String, String>();
			//TODO CHANGE FOR NEW DOMAIN
//			map.put("store_id", "IT_Devices_Online");
			map.put("store_id", me.upc);
			map.put("product_label", me.mpn);
			if ("SCREEN".equals(isOnScreen)) {
				map.put("rank", list.get(location).rating + "-" + list.get(location).details);
			} else if (location > 0) {
				map.put("rank", list.get(location).rating + " " + list.get(location).details);
			} else {
				map.put("rank", "non");
			}
			map.put("pso_price", "");
			map.put("inserted_date", "");
			map.put("new_price", "");
			map.put("pso_company", isOnScreen);
			map.put("suggestion", "");
			map.put("updated_date", "");

			for (int i = 0; i < 8; i++) {
				int c = i + 1;
				map.put("company" + String.valueOf(c), "non");
				map.put("company" + String.valueOf(c) + "_price", "non");
			}
			for (int i = 0; i < list.size() && i < 8 && i < count; i++) {
				BigMessage m = list.get(i);
				line += m.domain + ", " + m.totalPrice + ", ";
				int c = i + 1;
				map.put("company" + String.valueOf(c), i + 1 + "-" + m.rating + " - " + m.details + " - " + m.domain);
				map.put("company" + String.valueOf(c) + "_price", m.totalPrice);
			}
			Statement statement = QueryBuilder.insertInto("demo", "csv_products").values(map.keySet().toArray(new String[0]),
					map.values().toArray(new String[0]));
			System.out.println(counter.addAndGet(1) + " ++++" + line);
			session.execute(statement);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public static void main(String[] str) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		BigMessage ms = new BigMessage();
		ms.mpn = "WS-C2960-8TC-S";
		ms.brand = "";
		final WebClient proxy = new WebClient(BrowserVersion.CHROME);
		proxy.getOptions().setThrowExceptionOnScriptError(false);
		proxy.getOptions().setJavaScriptEnabled(true);
		proxy.getOptions().setCssEnabled(true);

		HtmlPage shopping = proxy.getPage("https://proxy-us.hide.me/go.php?u=MAu7gGgxAYHkYTBChPWdnLG7HaRFSnYR%2F1PeV5qx&b=5");
		final HtmlForm form = shopping.getForms().get(0);
		HtmlSubmitInput button = form.getFirstByXPath("//*[@id=\"content\"]/form/input[2]");// getByXPath("//*[@id=\"content\"]/form/input[2]");
		final HtmlTextInput textField = form.getInputByName("u");

		// Change the value of the text field
		textField.setValueAttribute("http://google.com");

		HtmlPage pageProxy = null;
		pageProxy = button.click();

		LinkedList<BigMessage> messages = new LinkedList<BigMessage>();
		boolean res = searchAds(pageProxy, ms, ms.brand + " " + ms.mpn, messages);

	}
}
