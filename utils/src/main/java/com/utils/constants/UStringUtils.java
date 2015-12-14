package com.utils.constants;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

public class UStringUtils {

	/**
	 * This method removes " e.g "10" -> 10
	 * 
	 * @param str
	 * @return
	 */
	public static String clearFromQuotes(String str) {

		if (str == null || str.equals("")) {
			return str;
		} else {
			if (str.trim().contains("\"")) {
				if (str.trim().startsWith("\"")) {
					str = str.substring(1);
				}
				if (str.trim().endsWith("\"")) {
					str = str.substring(0, str.length() - 1);
				}
			}
		}

		return str;
	}

	public static String getDomainName(String url) throws URIException {
		try {
			URI uri = new URI(url, false, "UTF-8");
			String domain = uri.getHost();
			if (domain == null) {
				throw new URIException(url);
			}
			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (URIException | NullPointerException e) {
			throw new URIException(url);
		}
	}

	public static String removeSpecialCharachters(String str) {
		if (str != null && str != "") {
			// String result = str.replaceAll("[^\\w\\s]", " ");
			// result = result.replaceAll("[ \\-\\+\\.\\^:,]+", " ");
			String result = str.replaceAll("[^a-zA-Z0-9\\$\\.\\,]", " ");
			String finalres = result.replaceAll("  ", " ");
			return finalres;
		} else {
			return "";
		}
	}
	public static String normilizeUrl(String url) {

		if (url != null) {
			url = url.replaceAll("www.", "");
			url = url.replaceAll("https", "http");
		}
		return url;
	}
}
