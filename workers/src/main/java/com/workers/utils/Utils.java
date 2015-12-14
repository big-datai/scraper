package com.workers.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.DateTimeZone;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import com.google.gson.JsonObject;
import com.utils.constants.UConstants;
import com.utils.constants.UStringUtils;
import com.utils.datatype.TimeoutRegexCharSequence;
import com.utils.messages.BigMessage;
import com.utils.queue.KafkaProducer;

public class Utils {
	static private final int RANGE = 250;
	static private final int BIG_RANGE = 1000;
	static private final int RANGE_TEXT = 75;
	public static final String CURRENCY_SYMBOLS = "\\p{Sc}";// \u0024\u060B";
	public static final String EURO = "\u20ac", POUND = "\u00a3", USD = "$";
	public final static String REGEX_PRICE_PATTERN = "(.*?)";
	public static final double PRICE_RANGE = 0.25;
	public static final List<String> SUPPORTED_LOCALES = Arrays.asList("en_US", "de_DE", "nl_NL");
	public static final Map<String, String> GGL_SRCH_URLS;
	static {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put("en_US", "https://www.google.com/shopping");
		aMap.put("de_DE", "https://www.google.de/shopping");
		aMap.put("nl_NL", "https://www.google.nl/shopping");

		GGL_SRCH_URLS = Collections.unmodifiableMap(aMap);
	}

	// Timeout in case pattern get stuck in find method
	public final static int PATTERN_TIMEOUT = 10000;
	public static final String FAILED_PATTERN = "FAILED GENERATIGN PATTERN IN BUILD PATTERNS";
	public static final String DID_NOT_FIND_PRICE = "WE COULD NOT FIND PRICE IN BUILD PATTERNS";
	public static final String NO_HTML = "ERROR COULD NOT GET HTML PAGE IN BUILD PATTERNS : ";

	public static BigMessage parseHtml2MS(BigMessage ms, HtmlPage page) throws TimeoutException, MalformedURLException, IOException {
		String pricePattern = "no", pricePatternText = "no", htmlString = "no", xml = "no";
		xml = page.asXml();// .replaceAll("[\\p{Blank}]{1,}",
							// " ").replaceAll("(?<=[\\d])(,)(?=[\\d])",
							// "");
		htmlString = page.asText();// .replaceAll("[\\p{Blank}]{1,}",
									// " ").replaceAll("(?<=[\\d])(,)(?=[\\d])",
									// "");
		// GETTING THE PRICE PATTERNS FROM XML AND TEXT
		boolean hasPriceXml = xml.contains(ms.getPrice());
		boolean hasPriceHtml = htmlString.contains(ms.getPrice());
		if (hasPriceXml) {
			pricePattern = getPatterns(ms.getPrice(), xml, page, RANGE, true, ms.currency);
		}
		if (hasPriceHtml) {
			pricePatternText = getPatterns(ms.getPrice(), htmlString, page, RANGE_TEXT, false, ms.currency);
		} else {
			UConstants.log.error("NOPRICE " + ms.getPrice() + "  URL " + ms.getUrl());
		}
		if (pricePattern == null || pricePattern.equals("") || pricePattern.equals("no")) {
			pricePattern = FAILED_PATTERN;
		}
		if (pricePatternText == null || pricePatternText.equals("") || pricePatternText.equals("no")) {
			pricePatternText = FAILED_PATTERN;
		}
		if (FAILED_PATTERN.contains(pricePatternText) || FAILED_PATTERN.contains(pricePattern) && ms.equals("")) {
			ms.setErrorMessage(FAILED_PATTERN);
		}
		Integer indexOfPattern = xml.indexOf(pricePattern) + RANGE;
		Integer indexOfPatternText = htmlString.indexOf(pricePatternText) + RANGE_TEXT;
		if (!hasPriceXml && !hasPriceHtml)
			ms.errorMessage = DID_NOT_FIND_PRICE;

		ms.indexOfPattern = indexOfPattern.toString();
		ms.indexOfPatternText = indexOfPatternText.toString();
		ms.sethtml(xml);
		ms.setPatternsText(pricePatternText);// .replace(ms.price,
												// REGEX_PRICE_PATTERN));
		ms.setPatternsHtml(pricePattern);// .replace(ms.price,
											// REGEX_PRICE_PATTERN));
		return ms;
	}

	public static String getPatterns(String price, String html, HtmlPage page, int range, boolean withForm, String currency) {
		if (html == null || price == null) {
			return "";
		}
		/*
		 * // GO OVER FORMS Map<Integer, HtmlForm> candids = new
		 * TreeMap<Integer, HtmlForm>(Collections.reverseOrder()); if (withForm
		 * == true) { List<HtmlForm> forms = page.getForms(); for (HtmlForm form
		 * : forms) { String f_xml = form.asXml(); if (f_xml != null && f_xml !=
		 * "") { String low = f_xml.toLowerCase();// .contains("add to cart");
		 * Integer grade = new Integer(0); if (f_xml.contains(price)) { grade =
		 * grading(low, grade, price); candids.put(grade, form); } } } //
		 * candids; System.out.println("forms"); } // FIND BUTTON WITH XPATH //
		 * page.getByXPath(xpathExpr)
		 */
		String pattern = "";
		// FOR XML CONTENT (MORE TAGS)
		int l = html.length();
		int start = (int) (l * 0.2);
		int end = (int) (l * 0.8);
		String source = html.substring(start, end);
		pattern = subGradePattern(price, source, range, currency);
		return pattern;// .replace(price, "(.*?)");
	}

	public static String subGradePattern(String price, String source, int range, String currency) {
		String pricePatern = "";
		String patt = USD;
		if (EURO.contains(currency)) {
			patt = price + "\\p{Space}{0,10}" + EURO;
		} else if (POUND.contains(currency)) {
			patt = POUND + "\\p{Space}{0,10}" + price;
		} else if (USD.contains(currency)) {
			patt = "\\" + USD + "\\p{Space}{0,10}" + price;
		}
		// ALL STRING BECOMES IGNORED ALL BY REGES LIKE IT WAS TEXT
		String escapedString = java.util.regex.Pattern.quote(USD + price);
		Pattern pattern = Pattern.compile(patt);
		Matcher matcher = pattern.matcher(source);
		source.contains("$" + price);
		source.contains(price);
		int count = 0;
		int index_price = -1;
		Map<Integer, String> patterns = new TreeMap<Integer, String>(Collections.reverseOrder());
		while (matcher.find()) {
			index_price = matcher.start();
			// Beginning and end of price props
			int beginIndex = index_price - range > 0 ? index_price - range : 0;
			int endIndex = index_price + range < source.length() ? index_price + range : source.length();
			String low = source.substring(beginIndex, endIndex).toLowerCase();
			Integer grade = new Integer(0);

			grade = grading(low, grade, price);
			patterns.put(grade, source.substring(beginIndex, endIndex));
			count++;
			if (count > 3) {
				break;
			}
		}
		// DID NOT MATCH PATTERN LOOKING FOR EXACT MATCH
		if (patterns.size() == 0) {
			index_price = source.indexOf(USD + price);
			int beginIndex = index_price - range > 0 ? index_price - range : 0;
			int endIndex = index_price + range < source.length() ? index_price + range : source.length();
			pricePatern = source.substring(beginIndex, endIndex);
		} else {
			pricePatern = patterns.entrySet().iterator().next().getValue();
		}
		return pricePatern;
	}

	public static String subPatternBasic(String price, String source) {
		String price_paterns = "";
		Pattern pattern = Pattern.compile("[" + CURRENCY_SYMBOLS + "]+?" + "[\\p{Space}]*?" + price);
		Matcher matcher = pattern.matcher(source);
		// FIND ALL MATCHES
		int count = 0;
		int index_price = -1;
		while (matcher.find()) {
			count++;
			index_price = matcher.start();
			// Beginning and end of price props
			int beginIndex = index_price - RANGE > 0 ? index_price - RANGE : 0;
			int endIndex = index_price + RANGE < source.length() ? index_price + RANGE : source.length();
			price_paterns = price_paterns + source.substring(beginIndex, endIndex) + "|||";
			if (count > 10) {
				break;
			}
		}
		return price_paterns;
	}

	public static Integer grading(String low, Integer grade, String price) {
		grade = 0;
		if (low != null && low != "" && low.contains(price)) {
			if (low.contains("add to cart") || low.contains("addtocart") || low.contains("buy it now") || low.contains("buy")
					|| low.contains("paypal") || low.contains("checkout")) {
				grade = 3;
			}
			if (low.contains("cart")) {
				grade++;
			}
			if (low.contains("purchase")) {
				grade++;
			}
			if (low.contains("shipping")) {
				grade++;
			}
			if (low.contains("price")) {
				grade++;
			}
			if (low.contains("available")) {
				grade++;
			}
			if (low.contains("quantity")) {
				grade++;
			}
			if (low.contains("$")) {
				grade++;
			}
			if (low.contains(EURO)) {
				grade++;
			}
			if (low.contains(POUND)) {
				grade++;
			}
		}
		return grade;
	}

	/**
	 * This method gets html content from url
	 * 
	 * @param ms
	 * @param webClient
	 * @return
	 * @throws IOException
	 * @throws FailingHttpStatusCodeException
	 * @throws InterruptedException
	 */
	public static HtmlPage getHtml(BigMessage ms, WebClient webClient, KafkaProducer m_logs) {
		HtmlPage page = null;
		try {
			page = webClient.getPage(UStringUtils.normilizeUrl(ms.getUrl()));
			webClient.waitForBackgroundJavaScript(2000);
		} catch (Exception e) {
		}
		if (page == null || page.asText() == null || page.asXml() == null) {
			UConstants.log.error(NO_HTML);
			ms.setErrorMessage(NO_HTML);
			try {
				Utils.postMessage(ms, m_logs);
			} catch (Exception ee) {
				UConstants.log.error("ERROR FAILD TO SEND MESSAGE TO LOGS QUEUE : " + ee.getMessage());
			}
		}
		return page;
	}

	public static HtmlPage getHtml(BigMessage ms, WebClient webClient) throws FailingHttpStatusCodeException, IOException {

		HtmlPage page = webClient.getPage(UStringUtils.normilizeUrl(ms.getUrl()));
		if (page == null || page.asText() == null || page.asXml() == null) {
			UConstants.log.error("COULD NOT GET DATA FROM THE PAGE WITH URL+++++: " + ms.getUrl());
		}
		return page;
	}

	public static HtmlPage getHtmlJS(BigMessage ms, WebClient webClient) throws FailingHttpStatusCodeException, IOException {

		HtmlPage page = webClient.getPage(UStringUtils.normilizeUrl(ms.getUrl()));

		JavaScriptJobManager manager = page.getEnclosingWindow().getJobManager();

		while (manager.getJobCount() > 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// System.out.println(page.asXml());
		if (page == null || page.asText() == null || page.asXml() == null) {
			UConstants.log.error("COULD NOT GET DATA FROM THE PAGE WITH URL+++++: " + ms.getUrl());
		}
		return page;
	}

	public static void postMessage(BigMessage ms, KafkaProducer mess_publisher) {
		ms.setLastUpdatedTime(new DateTime(DateTimeZone.UTC).toString());
		mess_publisher.send(ms.toJson().toString());
	}

	public static String parseUrl(String url) throws Exception {
		try {
			if (url.startsWith("http:/")) {
				if (!url.contains("http://")) {
					url = url.replaceAll("http:/", "http://");
				}
			} else {
				url = "http://" + url;
			}

			URI uri = new URI(url);
			String domain = uri.getHost();
			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (Exception e) {
			return "no";
		}
	}

	public static String[] getPriceToken(String price_patterns) {
		String[] tokens = price_patterns.split("\\|\\|\\|");

		// Take only few pattern out of all the patterns
		if (tokens.length > UtilsOld.PRICE_PATTERN_COUNT) {
			String[] newToken = new String[UtilsOld.PRICE_PATTERN_COUNT];
			for (int i = 0; i <= (UtilsOld.PRICE_PATTERN_COUNT - 1); i++) {
				newToken[i] = tokens[i];
			}
			tokens = newToken;
		}

		return tokens;
	}

	public static double getPrice(String pricePattern, String htmlPage, String url, double originalPrice, int index) throws RuntimeException {
		double priceVal = 0.0;
		if (pricePattern != null && pricePattern.trim().length() > 0 && pricePattern.contains(REGEX_PRICE_PATTERN)) {
			int beginIndex = Math.max(0, index - BIG_RANGE);
			int endIndex = Math.min(htmlPage.length(), index + BIG_RANGE);
			String source = htmlPage.substring(beginIndex, endIndex);

			// pattern matching
			priceVal = match(pricePattern, source, url, originalPrice);
		}
		return priceVal;
	}

	public static double match(String pricePattern, String htmlPage, String url, double orignalPrice) {
		long time1 = System.currentTimeMillis();

		String price_match = pricePattern. /*
											 * . replaceAll(
											 * "[^a-zA-Z_0-9\\(\\.\\*\\?\\)" +
											 * CURRENCY_SYMBOLS + "]", "")
											 */
		replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\*").replaceAll("\\?", "\\\\?")
				.replace("\\(\\.\\*\\?\\)", "(.*?)").replaceAll("\\$", "\\\\\\$");

		Pattern p = Pattern.compile(price_match);

		CharSequence charSequence = new TimeoutRegexCharSequence(htmlPage, PATTERN_TIMEOUT, htmlPage, p.pattern());
		Matcher match = p.matcher(charSequence);

		double price_val = 0.0;

		if (match.find()) {
			price_val = findPriceInOneResult(match, price_match, url, orignalPrice);
			System.out.println(htmlPage.substring(match.start()));
		}
		long time2 = System.currentTimeMillis();
		UConstants.log.info("Time taken in method getPrice:" + (time2 - time1));

		return price_val;

	}

	/**
	 * Written the pattern match Algo
	 * 
	 * @param price_pattern
	 * @param htmlPage
	 * @param url
	 * @param original_price
	 * @return
	 */

	public static double pricePatternMatchAlgo(String price_pattern, String htmlPage, String url, double original_price) {

		long time1 = System.currentTimeMillis();
		double price_value = 0.0;

		int patternInx = price_pattern.indexOf(REGEX_PRICE_PATTERN);

		if (patternInx <= -1) {
			return price_value;
		}

		price_value = match(price_pattern, htmlPage, url, original_price);

		if (price_value <= 0.0) {

			int startVal = patternInx / 2;

			int endVal = price_pattern.length();

			if (price_pattern.length() > patternInx) {
				endVal = patternInx + (price_pattern.length() - patternInx) / 2;
			}

			// If length too less then forget the right portion
			if (price_pattern.length() <= 20) {
				endVal = startVal + 5;
			}

			String newpatt = price_pattern.substring(startVal, endVal);

			if (newpatt.length() <= 15) {
				return price_value;
			}

			price_value = pricePatternMatchAlgo(newpatt, htmlPage, url, original_price);

		}

		long time2 = System.currentTimeMillis();
		UConstants.log.info("Time taken in method price_pattern_match_Algo:" + (time2 - time1));

		return price_value;
	}

	/**
	 * 
	 * @param pattern_matter
	 * @param price_match
	 * @param url
	 * @return
	 */
	public static double findPriceInOneResult(Matcher pattern_matter, String price_match, String url, double orignal_price) {

		double price_val = 0.0;
		long time1 = System.currentTimeMillis();
		try {
			// Find the regex group for the price match
			String price_finder = pattern_matter.group(1);
			price_val = Double.parseDouble(price_finder);

		} catch (NumberFormatException formatException) {

			// In case price is not found in
			// grouping then we need to fetch price
			// based on length of character before
			// and after the matching regex
			try {

				int beginingIndex = price_match.indexOf(REGEX_PRICE_PATTERN);
				int endIndex = (pattern_matter.group()).indexOf(price_match.substring(beginingIndex + (REGEX_PRICE_PATTERN).length()));
				String price_pattern = (pattern_matter.group()).substring(beginingIndex - REGEX_PRICE_PATTERN.length(), endIndex);

				price_val = Double.parseDouble(price_pattern);

			} catch (Exception formatEx) {

				// Fetch All digits present in the String
				ArrayList<Double> price_List = new ArrayList<Double>();
				String digit_price_pattern = "([\\d]+[\\.]?[\\d+]*)";

				Pattern p = Pattern.compile(digit_price_pattern);
				Matcher m = p.matcher(pattern_matter.group());

				while (m.find()) {
					try {
						String html_pattern = m.group(1);

						price_List.add(Double.parseDouble(html_pattern));
					} catch (Exception formatEx1) {
						UConstants.log.error("Price not found location 1" + url + "Error Message:" + formatEx.getStackTrace());

						if (price_List.size() <= 0) {
							return price_val;
						} else {
							price_val = findPrice(price_List, orignal_price, PRICE_RANGE);
							return price_val;
						}
					}
				}

				price_val = findPrice(price_List, orignal_price, PRICE_RANGE);
			}
		} catch (Exception e) {

			// EXCEPTION.incrementAndGet();
			UConstants.log.error("Price not found location 2" + url + "Error Message:" + e.getStackTrace());
			return price_val;

		} finally {
			long time2 = System.currentTimeMillis();
			UConstants.log.info("Time taken in method getPrice:" + (time2 - time1));
		}

		return price_val;
	}

	/**
	 * Just fetching price between the range
	 * 
	 * @param priceList
	 * @return
	 */
	public static Double findPrice(List<Double> priceList, double product_price, double range) {
		long time1 = System.currentTimeMillis();
		Double price = 0.0;
		if (priceList != null && priceList.size() > 0) {
			double expectPriceIncreased = product_price * range + product_price;
			double expectPriceDecrease = product_price - product_price * range;
			ArrayList<Double> candid = new ArrayList<Double>();

			for (Double priceVal : priceList) {
				// filter prices by range
				if (expectPriceDecrease <= priceVal && priceVal <= expectPriceIncreased) {
					candid.add(priceVal);
				}
			}
			// TODO IF NEEDED IMPROVE PRICE LOGIC HERE, THE LOGIC TO CHOOSE THE
			// BEST PRICE
			price = UtilsOld.mostOcurrencesElement(candid);

		}
		long time2 = System.currentTimeMillis();
		UConstants.log.info("Time taken in method find_Price:" + (time2 - time1));

		return price;
	}

	public static Double mostOcurrencesElement(ArrayList<Double> array) {
		Map<Double, Integer> countMap = countElementOcurrences(array);

		int maxCount = 0;
		Double element = null;
		for (Double e : countMap.keySet()) {
			if (countMap.get(e) > maxCount) {
				element = e;
				maxCount = countMap.get(e);
			}
		}

		return element;

	}

	public static Map<Double, Integer> countElementOcurrences(ArrayList<Double> array) {

		Map<Double, Integer> countMap = new HashMap<Double, Integer>();

		for (Double element : array) {
			Integer count = countMap.get(element);
			count = (count == null) ? 1 : count + 1;
			countMap.put(element, count);
		}

		return countMap;
	}

	public static String parcePrice(String inputStr, String locale) {
		try {
			if (locale.contains("_"))
				locale = locale.substring(0,locale.indexOf("_"));
			NumberFormat format = NumberFormat.getInstance(new Locale(locale));
			Number number = format.parse(inputStr);
			return String.valueOf(number.doubleValue());
		} catch (Exception e) {
		}
		return "";
	}



	static public String removeQuotes(String str) {
		if (str != null && !str.isEmpty() && str.length() > 2 && str.startsWith("\""))
			str = str.substring(1, str.length() - 1);
		return str;
	}

	static public String getCleanURL(String str) {
		// CLEANS GGL ENCODED LINK
		if (str.contains("http")) {
			str = str.substring(str.lastIndexOf("http"));
			try {
				while (true) {
					str = java.net.URLDecoder.decode(str, "UTF-8");
					if (str == java.net.URLDecoder.decode(str, "UTF-8"))
						break;
				}

			} catch (UnsupportedEncodingException e) {
				// couldn't decode probably bad encoding
				return "";
			}
			if (str.contains("html")) {
				str = str.substring(0, str.indexOf("html") + 4);
				return str;
			}

			int cutIndex = str.indexOf("&nm_mc");
			if (cutIndex == -1 || (cutIndex > str.indexOf("?sgd=") && str.indexOf("?sgd=") != -1))
				cutIndex = str.indexOf("?sgd=");
			if (cutIndex == -1 || (cutIndex > str.indexOf("?lpid=") && str.indexOf("?lpid=") != -1))
				cutIndex = str.indexOf("?lpid=");
			if (cutIndex == -1 || (cutIndex > str.indexOf("?utm") && str.indexOf("?utm") != -1))
				cutIndex = str.indexOf("?utm");
			if (cutIndex == -1 || (cutIndex > str.indexOf("&utm") && str.indexOf("&utm") != -1))
				cutIndex = str.indexOf("&utm");
			if (cutIndex == -1 || (cutIndex > str.indexOf("&sa=") && str.indexOf("&sa=") != -1))
				cutIndex = str.indexOf("&sa=");
			if (cutIndex != -1)
				str = str.substring(0, cutIndex);
			return str;
		} else {
			return "";
		}
	}

	public static void insertCassandra(BigMessage m, Session session,String table){
		Statement statement = QueryBuilder.insertInto("demo", table)
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
}
