package com.workers.utils;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.utils.constants.UConstants;

public class ESQuery {
	private Client esClient;
	private Settings settings;
	final static String HOST_NAME = UConstants.HOST_NAME;
	final static String CLUSTER_NAME = UConstants.CLUSTER_NAME;
	final static String ALL_QUERY="domain";
	private String scrollId;
	
	public String getScrollId() {
		return scrollId;
	}


	@SuppressWarnings("resource")
	public ESQuery(String requestType) throws Exception {
		String clusterName = CLUSTER_NAME;
		String hostname = HOST_NAME;

		settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", clusterName)
				// .put("transport.type","org.elasticsearch.transport.netty.NettyTransport")
				// .put("transport.netty.worker_count","2")
				.put("client.transport.ignore_cluster_name", false).put("client.transport.nodes_sampler_interval", "30s")
				.put("client.transport.ping_timeout", "30s").build();

		esClient = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(hostname, 9300));
		
		UConstants.log.info("Connected to ES " + clusterName + " " + hostname);

		if((ALL_QUERY).equalsIgnoreCase(requestType)){
			scrollId=this.fetchAllQuery();
		}else{
			scrollId=this.fetchUpdatePriceQuery();	
		}	

	}
	

	public Client getEsClient() {
		return esClient;
	}


	public void setEsClient(Client esClient) {
		this.esClient = esClient;
	}


	public String fetchUpdatePriceQuery() throws Exception{
		
		List<String> queryTypeList = new ArrayList<String>();
		List<String> queryfieldList = new ArrayList<String>();
		List<Object> queryObjectList = new ArrayList<Object>();

		// Add for MatchNot RAW_TEXT COULDNOTGET
		queryTypeList.add(UtilsOld.MATCHNOT);
		queryfieldList.add(UtilsOld.ES_RAW_TEXT);
		queryObjectList.add(UtilsOld.COULDNOTGET);
		
		// Add for MatchNot RAW_TEXT NOHTML
		queryTypeList.add(UtilsOld.MATCHNOT);
		queryfieldList.add(UtilsOld.ES_RAW_TEXT);
		queryObjectList.add(UtilsOld.NOHTML);

		// Add for Shipping=0
		queryTypeList.add(UtilsOld.MATCH);
		queryfieldList.add(UtilsOld.ES_SHIPPING);
		queryObjectList.add("0");

		// last updated time bigger the 12 hours
		queryTypeList.add(UtilsOld.RANGE);
		queryfieldList.add(UtilsOld.ES_LAST_UPDATE_TIME);
		queryObjectList.add("now-6h");

		// result must be random
		queryTypeList.add(UtilsOld.RANDOM);
		queryfieldList.add(UtilsOld.RANDOM);
		queryObjectList.add(UtilsOld.RANDOM);

		String scrollId = ESUpdatePrices.fetchQueryScrollable(esClient, UtilsOld.ES_INDEX, UtilsOld.ES_TYPE,
				queryTypeList, queryfieldList, queryObjectList, 0);

      return scrollId;		
	}
	
	public String fetchAllQuery() throws Exception{
		
		List<String> queryTypeList=new ArrayList<String>();
		queryTypeList.add(UtilsOld.MATCHALL);
		
		String scrollId = ESUpdatePrices.fetchQueryScrollable(esClient,
				UtilsOld.ES_INDEX, UtilsOld.ES_TYPE,
				queryTypeList,
				null,
				null,0);
		
		return scrollId;
	}
	
}
