package com.workers.utils;

import java.util.ArrayList;

public class Filters {
	private ArrayList<String> m_urls = new ArrayList<String>();

	public Filters add(String url) {
		m_urls.add(url);
		return this;
	}

	public boolean filter(String url) {
		if (url == null)
			return false;

		for (String urlBase : m_urls) {
			if (url.contains(urlBase)) {
				return true;
			}
		}
		return false;
	}

	private static Filters instance;

	public static Filters instance() {
		if (instance == null) {
			instance = new Filters();
			instance.add("amazon.com").add("google.com/shopping").add("google.com/recaptcha").add("walmart.com").add("facebook.com")
					.add("pricefalls.com").add("www.ebay.com");
		}
		return instance;
	}

}
