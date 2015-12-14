package com.workers.workers;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.utils.constants.UConstants;
import com.utils.messages.BigMessage;
import com.workers.utils.ESQuery;
import com.workers.utils.Utils;

public class ES2Cassandra implements Runnable {

	public static TreeMap<String, ArrayList<String>> map = new TreeMap<String, ArrayList<String>>();

	private ESQuery query;

	public ES2Cassandra(ESQuery query) {

		this.query = query;
	}

	@Override
	public void run() {
		try {
			Cluster cluster;
			Session session;
			cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
			session = cluster.connect("ks");
			String scrollId = query.getScrollId();
			while (true) {
				SearchResponse scrollResp = query.getEsClient().prepareSearchScroll(scrollId).setScroll(new TimeValue(1, TimeUnit.MINUTES))
						.execute().actionGet();

				// Break condition: No hits are returned
				if (scrollResp.getHits().getHits().length == 0) {
					break;
				}

				// Connect to the cluster and keyspaces "demo"
				for (SearchHit hit : scrollResp.getHits()) {
					if (hit != null && hit.getSourceAsString() != null) {
						BigMessage message = BigMessage.string2Message(hit.getSourceAsString().getBytes());
						message.setId(hit.getId());
						message.setDomain(Utils.parseUrl(message.getUrl()));

						String cMessage = null;
						try {
							// to copy all data to cassandra from elasticsearch
							cMessage = message.toJson().toString().replace("'", "").replaceAll("\":\"", "':'").replaceAll("\",\"", "','")
									.replace("\"}", "'}").replace("{\"", "{'");
							session.execute("INSERT INTO data_points (id, properties) VALUES ('" + message.getProdId() + "__"
									+ message.getUrl() + "', " + cMessage + " )");
							// System.out.println(message.toJson().toString());
						} catch (Exception e) {
							System.out.println("error " + e.getMessage() + "  " + cMessage);
						}

					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			UConstants.log.error("Error Message:" + e.getStackTrace() + " :" + e.getMessage());
		} finally {
			query.getEsClient().close();
		}
	}



}
