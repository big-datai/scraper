package com.workers.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.utils.constants.UConstants;
import com.utils.datatype.TimeoutRegexCharSequence;

/**
 * This class fetch all document from Elastic Search. For each document fields
 * (url,price_patterns,original_price) Using field 'url' scrap document and then
 * match it document with all fields 'price_patterns' Using matching, find the
 * updated price of the document and then decide the correct price and update
 * 'price_updated' field of ElasticSearch
 * 
 * @author dmitry
 * 
 */
public class UtilsOld {

	public final static String MATCH = "MATCH";
	public final static String WILDCARD = "WILDCARD";
	public final static String MATCHALL = "MATCHALL";
	public final static String MATCHNOT = "MATCHNOT";
	public final static String MISSING = "MISSING";
	public final static String IDS = "IDS";
	public final static String RANGE = "RANGE";
	public final static String REPLACE_CHAR = "(.*?)";
	public final static String ES_INDEX = "full_river2";
	public final static String ES_TYPE = "data";
	public final static String ES_PRICE_PATTERNS = "price_patterns";
	public final static String COULDNOTGET = "COULDNOTGET";
	public final static String NOHTML = "NOHTML";
	public final static String ES_RAW_TEXT = "raw_text";
	public final static String ES_SHIPPING = "shipping";
	public final static String ES_PRICE_UPDATED = "price_updated";
	public final static String ES_LAST_UPDATE_TIME = "last_updated_time";
	public final static String RANDOM = "RANDOM";

	// Timeout in case pattern get stuck in find method
	public final static int PATTERN_TIMEOUT = 10000;

	public final static String ES_PRICE_PROP = "price_prop1";
	public final static String REGEX_PRICE_PATTERN = "(.*?)";
	public static final String CURRENCY_SYMBOLS = "\\p{Sc}\u0024\u060B";
	public static final int TITLEPROP_RANGE = 200;
	public static final double PRICE_RANGE = 0.25;
	public static final int PRICE_PATTERN_COUNT = 3;




	public static String parseUrl(String url) throws Exception {
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
	}

	/**
	 * Getting most frequent price from the price list
	 * 
	 * @param array
	 * @return
	 */
	public static Map<Double, Integer> countElementOcurrences(ArrayList<Double> array) {

		Map<Double, Integer> countMap = new HashMap<Double, Integer>();

		for (Double element : array) {
			Integer count = countMap.get(element);
			count = (count == null) ? 1 : count + 1;
			countMap.put(element, count);
		}

		return countMap;
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
			price = mostOcurrencesElement(candid);

		}
		long time2 = System.currentTimeMillis();
		UConstants.log.info("Time taken in method find_Price:" + (time2 - time1));

		return price;
	}

	/**
	 * Function to measure similarities between price pattern and a candidate
	 * how much to similar strings are different
	 * 
	 * @param s0
	 * @param s1
	 * @return
	 */
	public int LevenshteinDistance(String s0, String s1) {
		int len0 = s0.length() + 1;
		int len1 = s1.length() + 1;

		// the array of distances
		int[] cost = new int[len0];
		int[] newcost = new int[len0];

		// initial cost of skipping prefix in String s0
		for (int i = 0; i < len0; i++)
			cost[i] = i;

		// dynamicaly computing the array of distances

		// transformation cost for each letter in s1
		for (int j = 1; j < len1; j++) {
			// initial cost of skipping prefix in String s1
			newcost[0] = j;

			// transformation cost for each letter in s0
			for (int i = 1; i < len0; i++) {
				// matching current letters in both strings
				int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

				// computing cost for each transformation
				int cost_replace = cost[i - 1] + match;
				int cost_insert = cost[i] + 1;
				int cost_delete = newcost[i - 1] + 1;

				// keep minimum cost
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
			}

			// swap cost/newcost arrays
			int[] swap = cost;
			cost = newcost;
			newcost = swap;
		}

		// the distance is the cost for transforming all letters in both strings
		return cost[len0 - 1];
	}

	/**
	 * This function prepares the patter for pricing matching with special
	 * characters adding skip mark "\"
	 * 
	 * @param price_pattern
	 * @param htmlPage
	 * @param url
	 * @return
	 */
	public static double matchPatternWithSpecialCharParse(String price_pattern, String htmlPage, String url, double orignal_price) {
		/**
		 * In regex $,/n/t/r,(),[],$ are special character they need to be
		 * handled
		 * 
		 * Replace character \t\n\r with "" Replace (),[],.,*,?,$ with escape
		 * character in front of it.
		 * 
		 * Note: We need to revert back the price regex group (.*?) as need to
		 * match it and find price for it
		 */
		String price_match = price_pattern.replaceAll("[\t\n\r,]", "").replaceAll("[\\p{Blank}]{1,}", " ").replaceAll("\\(", "\\\\(")
				.replaceAll("\\)", "\\\\)").replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]").replaceAll("\\$", "\\\\\\$")
				.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\*").replaceAll("\\?", "\\\\?").replaceAll("\\+", "\\\\+")
				.replace("\\(\\.\\*\\?\\)", "(.*?)");

		Pattern p = Pattern.compile(price_match);
		htmlPage = htmlPage.replaceAll("[\\p{Blank}]{1,}", " ").replaceAll("(?<=[\\d])(,)(?=[\\d])", "").replaceAll("[\t\n\r,]", "");

		Matcher match = p.matcher(htmlPage);
		double price_val = 0.0;
		if (match.find()) {
			price_val = getPrice(match, price_match, url, orignal_price);
		}

		return price_val;

	}

	/**
	 * Only taking into account know character in price_pattern and htmlpage
	 * 
	 * @param price_pattern
	 * @param htmlPage
	 * @param url
	 * @return
	 */
	public static double matchPatternWithStrings(String price_pattern, String htmlPage, String url, double orignal_price) {

		long time1 = System.currentTimeMillis();

		Pattern p = Pattern.compile(price_pattern);
		// Matcher match = p.matcher(htmlPage);

		CharSequence charSequence = new TimeoutRegexCharSequence(htmlPage, UtilsOld.PATTERN_TIMEOUT, htmlPage, p.pattern());
		Matcher match = p.matcher(charSequence);

		double price_val = 0.0;
		if (match.find()) {
			price_val = getPrice(match, price_pattern, url, orignal_price);
		}

		long time2 = System.currentTimeMillis();
		UConstants.log.info("Time taken in method getPrice:" + (time2 - time1));

		return price_val;

	}

	/**
	 * Only taking into account letters and numbers in price_pattern and
	 * htmlpage
	 * 
	 * @param price_pattern
	 * @param htmlPage
	 * @param url
	 * @return
	 */
	public static double matchPatternWithAlphabetNumber(String price_pattern, String htmlPage, String url, double orignal_price) {
		long time1 = System.currentTimeMillis();
		// TODO change it
		String price_match = price_pattern.replaceAll("[^a-zA-Z_0-9\\(\\.\\*\\?\\)" + CURRENCY_SYMBOLS + "]", "").replaceAll("\\(", "\\\\(")
				.replaceAll("\\)", "\\\\)").replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\*").replaceAll("\\?", "\\\\?")
				.replace("\\(\\.\\*\\?\\)", "(.*?)").replaceAll("\\$", "\\\\\\$");

		Pattern p = Pattern.compile(price_match);
		// Matcher match = p.matcher(htmlPage);

		CharSequence charSequence = new TimeoutRegexCharSequence(htmlPage, UtilsOld.PATTERN_TIMEOUT, htmlPage, p.pattern());
		Matcher match = p.matcher(charSequence);

		double price_val = 0.0;

		if (match.find()) {
			// int index=match.start();
			price_val = getPrice(match, price_match, url, orignal_price);
		}

		long time2 = System.currentTimeMillis();
		UConstants.log.info("Time taken in method getPrice:" + (time2 - time1));

		return price_val;

	}

	/**
	 * This method should replace all characters with latters beside "*.?$"
	 * 
	 * @param htmlPage
	 * @return
	 */
	// TODO VALIDATE THAT ALL CHARACHTERS DOTS ETC ARE REPLACED
	public static String fetchHtmlWithAlphabetNumber(String htmlPage) {
		if (htmlPage != null && htmlPage.length() > 0) {
			return htmlPage.replaceAll("[\\p{Blank}]{1,}", " ").replaceAll("[^a-zA-Z_0-9\\(\\.)" + CURRENCY_SYMBOLS + "]", "");
			// return htmlPage.replaceAll("[\\p{Blank}]{1,}",
			// " ").replaceAll("(?<=[\\d])(,)(?=[\\d])", "")
			// .replaceAll("[^a-zA-Z_0-9\\(\\.\\*\\?\\)" + CURRENCY_SYMBOLS +
			// "]", "");
		}
		return htmlPage;
	}

	/**
	 * 
	 * @param pattern_matter
	 * @param price_match
	 * @param url
	 * @return
	 */
	public static double getPrice(Matcher pattern_matter, String price_match, String url, double orignal_price) {

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
	 * Written the pattern match Algo
	 * 
	 * @param price_pattern
	 * @param htmlPage
	 * @param url
	 * @param original_price
	 * @return
	 */

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

		price_value = matchPatternWithAlphabetNumber(price_pattern, htmlPage, url, original_price);

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
}
