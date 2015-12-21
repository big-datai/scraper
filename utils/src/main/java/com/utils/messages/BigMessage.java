package com.utils.messages;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.utils.constants.UConstants;

public class BigMessage extends Message implements Serializable {

	/**
	 * @author dmitry
	 */

	private static final long serialVersionUID = -8626661955496600235L;
	private JsonObject jo = null;

	public BigMessage() {

	}

	public BigMessage(BigMessage ms) {
		mapping(ms);
		// this.jo = );jo;

	}

	public BigMessage(Message ms) {
		mapping(ms);
	}

	public void mapping(Message ms) {
		html = new String(ms.html);
		price = new String(ms.price);
		ggId = new String(ms.ggId);
		domain = new String(ms.domain);
		title = new String(ms.title);
		url = new String(ms.url);
		prodId = new String(ms.prodId);
		patternsHtml = new String(ms.patternsHtml);
		lastScrapedTime = new String(ms.lastScrapedTime);
		lastUpdatedTime = new String(ms.lastUpdatedTime);
		updatedPrice = new String(ms.updatedPrice);
		modelPrice = new String(ms.modelPrice);
		patternsText = new String(ms.patternsText);
		exception = new String(ms.exception);
		issue = new String(ms.issue);
		errorMessage = new String(ms.errorMessage);
		stackTrace = new String(ms.stackTrace);
		errorLocation = new String(ms.errorLocation);
		category = new String(ms.category);
		sku = new String(ms.sku);
		mpn = new String(ms.mpn);
		upc = new String(ms.upc);
		brand = new String(ms.brand);
		otherID = new String(ms.otherID);
		id = new String(ms.id);
		otherID = new String(ms.otherID);
		shipping = new String(ms.shipping);
		gTitle = new String(ms.gTitle);
		competitors = new String(ms.competitors);
		gNotParsed = new String(ms.gNotParsed);
		indexOfPattern = new String(ms.indexOfPattern);
		indexOfPatternText = new String(ms.indexOfPatternText);
		currency = new String(ms.currency);
		storeId = new String(ms.storeId);
		rating = new String(ms.rating);
		details = new String(ms.details);
		basePrice = new String(ms.basePrice);
		priceAddition = new String(ms.priceAddition);
		priceAdditionText = new String(ms.priceAdditionText);
		totalPrice = new String(ms.totalPrice);
		locale = new String(ms.locale);
		firstGglPrice = new String(ms.firstGglPrice);
		gglName = new String(ms.gglName);
		condition=new String(ms.condition);
		uBound=new String(ms.uBound);
		lBound=new String(ms.lBound);
	}

	public static BigMessage string2Message(byte[] message) {

		String ms = "";
		try {
			ms = new String(message, "UTF-8");
			final GsonBuilder gsonBuilder = new GsonBuilder();
			final Gson gson = gsonBuilder.create();
			// Parse JSON to Java
			final Message simple = gson.fromJson(ms, Message.class);
			return new BigMessage(simple);
		} catch (Exception e) {
			UConstants.log.error("ERROR PARSING MESSAGE " + e.getMessage()
					+ " the message is" + ms);
		}
		return null;
	}

	public static BigMessage string2Message(String message) {
		try {
			string2Message(message.getBytes());
		} catch (Exception e) {
			UConstants.log.error("could not load string messages from CSV "
					+ e.getMessage() + " the message is" + message);
		}
		return null;
	}

	public JsonObject toJson() {
		if (jo == null) {
			jo = new JsonObject();
		} else {
			return jo;
		}
		// jo.addProperty("index", "_river");
		jo.addProperty("url", url);
		jo.addProperty("title", title);
		jo.addProperty("patternsHtml", patternsHtml);
		jo.addProperty("price", price);
		jo.addProperty("html", html);
		jo.addProperty("patternsText", patternsText);
		jo.addProperty("ggId", ggId);
		jo.addProperty("prodId", prodId);
		jo.addProperty("domain", domain);

		if (lastScrapedTime != null)
			jo.addProperty("lastScrapedTime", lastScrapedTime);
		if (lastUpdatedTime != null)
			jo.addProperty("lastUpdatedTime", lastUpdatedTime);
		if (updatedPrice != null)
			jo.addProperty("updatedPrice", updatedPrice);
		if (modelPrice != null)
			jo.addProperty("modelPrice", modelPrice);

		jo.addProperty("exception", exception);
		jo.addProperty("issue", issue);
		jo.addProperty("errorMessage", errorMessage);
		jo.addProperty("stackTrace", stackTrace);
		jo.addProperty("errorLocation", errorLocation);

		jo.addProperty("category", category);
		jo.addProperty("sku", sku);
		jo.addProperty("mpn", mpn);
		jo.addProperty("upc", upc);
		jo.addProperty("brand", brand);
		jo.addProperty("otherID", otherID);
		jo.addProperty("shipping", shipping);
		jo.addProperty("gTitle", gTitle);
		jo.addProperty("competitors", competitors);
		jo.addProperty("gNotParsed", gNotParsed);
		jo.addProperty("indexOfPattern", indexOfPattern);
		jo.addProperty("indexOfPatternText", indexOfPatternText);
		jo.addProperty("currency", currency);
		jo.addProperty("storeId", storeId);

		jo.addProperty("rating", rating);
		jo.addProperty("details", details);
		jo.addProperty("basePrice", basePrice);
		jo.addProperty("priceAddition", priceAddition);
		jo.addProperty("priceAdditionText", priceAdditionText);
		jo.addProperty("totalPrice", totalPrice);
		jo.addProperty("locale", locale);
		jo.addProperty("firstGglPrice", firstGglPrice);
		jo.addProperty("gglName", gglName);
		jo.addProperty("condition", condition);
		jo.addProperty("uBound", uBound);
		jo.addProperty("lBound", lBound);
		return jo;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPatternsText() {
		return patternsText;
	}

	public void setPatternsText(String patternsText) {
		this.patternsText = patternsText;
	}

	public String getPatternsHtml() {
		return patternsHtml;
	}

	public void setPatternsHtml(String title_prop) {
		this.patternsHtml = title_prop;
	}

	public String getHtml() {
		return html;
	}

	public void sethtml(String html) {
		this.html = html;
	}

	public String getShipping() {
		return shipping;
	}

	public void setShipping(String shipping) {
		this.shipping = shipping;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String raw) {
		this.domain = raw;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUpdatedPrice() {
		return updatedPrice;
	}

	public void setUpdatedPrice(String updatedPrice) {
		this.updatedPrice = updatedPrice;
	}

	public String getModelPrice() {
		return modelPrice;
	}

	public void setModelPrice(String modelPrice) {
		this.modelPrice = modelPrice;
	}

	public void setCat(String category) {
		this.category = category;
	}

	public String getCat() {
		return category;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getBrand() {
		return brand;
	}

	public void setMpn(String mpn) {
		this.mpn = mpn;
	}

	public String getMpn() {
		return mpn;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	public String getUpc() {
		return upc;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getSku() {
		return upc;
	}

	public void setOtherID(String otherID) {
		this.otherID = otherID;
	}

	public String getOtherID() {
		return otherID;
	}

	public String getupdatedPrice() {
		return updatedPrice;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getErrorLocation() {
		return errorLocation;
	}

	public void setErrorLocation(String errorLocation) {
		this.errorLocation = errorLocation;
	}

	public String getLastScrapedTime() {
		return lastScrapedTime;
	}

	public void setLastScrapedTime(String lastScrapedTime) {
		this.lastScrapedTime = lastScrapedTime;
	}

	public String getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	public void setLastUpdatedTime(String lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	public String getProdId() {
		return prodId;
	}

	public void setProdId(String prodId) {
		this.prodId = prodId;
	}

}
