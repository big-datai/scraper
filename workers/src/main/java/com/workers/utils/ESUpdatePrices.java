package com.workers.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.random.RandomScoreFunctionBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.utils.constants.UConstants;

public class ESUpdatePrices {

	/**
	 * This method update the field in Elastic Search document
	 * 
	 * @param client
	 * @param index
	 * @param type
	 * @param insertorUpdate
	 * @param fieldName
	 * @param fieldObject
	 * @param id
	 */

	public static void updateDocument(Client client, String index, String type, String insertorUpdate, String fieldName,
			Object fieldObject, String id) {

		Map<String, Object> updateObject = new HashMap<String, Object>();
		updateObject.put(fieldName, fieldObject);

		client.prepareUpdate(index, type, id).setScript("ctx._source." + fieldName + "=" + fieldName, null).setScriptParams(updateObject)
				.execute().actionGet();
	}

	/**
	 * This method fetch query based on query specified
	 * 
	 * @param client
	 * @param index
	 * @param type
	 * @param queryType
	 * @param queryfieldName
	 * @param queryfieldObject
	 * @return
	 * @throws Exception
	 */
	public static SearchResponse fetchQuery(Client client, String index, String type, String queryType, String queryfieldName,
			Object queryfieldObject) throws Exception {
		long time1 = System.currentTimeMillis();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		QueryBuilder queryBuilder = null;

		if ((UtilsOld.WILDCARD).equals(queryType)) {
			queryBuilder = QueryBuilders.wildcardQuery(queryfieldName, (String) queryfieldObject);
		} else if ((UtilsOld.MATCHALL).equals(queryType)) {
			queryBuilder = QueryBuilders.matchAllQuery();
		} else if (UtilsOld.IDS.equals(queryType)) {
			queryBuilder = QueryBuilders.idsQuery().ids((String) queryfieldObject);
		} else {
			queryBuilder = QueryBuilders.matchQuery(queryfieldName, queryfieldObject);
		}

		searchSourceBuilder.query(queryBuilder);

		try {
			return client.prepareSearch(index).setTypes(type).setQuery(queryBuilder).execute().actionGet();
		} catch (Exception e) {
			UConstants.log.error(" all exceptions : " + e.getMessage());
			throw e;
		} finally {
			long time2 = System.currentTimeMillis();
			UConstants.log.info("Time taken to excute query" + queryType + ":" + queryfieldName + ":" + queryfieldObject + (time2 - time1));
		}
	}

	/**
	 * 
	 * @param client
	 * @param index
	 * @param type
	 * @param queryType
	 * @param queryfieldName
	 * @param queryfieldObject
	 * @return
	 * @throws Exception
	 */
	public static String fetchQueryScrollable(Client client, String index, String type, List<String> queryTypeList,
			List<String> queryfieldList, List<Object> queryObjectList, int beginingFrom) throws Exception {

		long time1 = System.currentTimeMillis();
		boolean filterflag = false;
		BoolQueryBuilder builder = QueryBuilders.boolQuery();
		BoolFilterBuilder boolFilterBuilder = FilterBuilders.boolFilter();

		for (int count = 0; count < queryTypeList.size(); count++) {
			QueryBuilder queryBuilder = null;
			String queryType = queryTypeList.get(count);
			String queryfieldName = null;
			Object queryfieldObject = null;
			if (queryfieldList != null && queryfieldList.size() >= count) {
				queryfieldName = queryfieldList.get(count);
			}

			if (queryObjectList != null && queryObjectList.size() >= count) {
				queryfieldObject = queryObjectList.get(count);
			}

			if ((UtilsOld.WILDCARD).equals(queryType)) {
				queryBuilder = QueryBuilders.wildcardQuery(queryfieldName, (String) queryfieldObject);
				builder.must(queryBuilder);
			} else if ((UtilsOld.MATCHALL).equals(queryType)) {
				queryBuilder = QueryBuilders.matchAllQuery();
				builder.must(queryBuilder);
			} else if ((UtilsOld.MATCHNOT).equals(queryType)) {
				queryBuilder = QueryBuilders.matchQuery(queryfieldName, queryfieldObject);
				builder.mustNot(queryBuilder);
			} else if ((UtilsOld.MATCH).equals(queryType)) {
				queryBuilder = QueryBuilders.matchQuery(queryfieldName, queryfieldObject);
				builder.must(queryBuilder);
			} else if ((UtilsOld.MISSING).equals(queryType)) {
				filterflag = true;
				boolFilterBuilder.must(FilterBuilders.missingFilter(queryfieldName).existence(true).nullValue(true));
			} else if ((UtilsOld.RANGE).equals(queryType)) {
				filterflag = true;
				boolFilterBuilder.must(FilterBuilders.rangeFilter(queryfieldName).from("2014-01-01").to(queryfieldObject));
			}else if ((UtilsOld.RANDOM).equals(queryType)) {
				filterflag = true;
				queryBuilder = QueryBuilders.functionScoreQuery().add(new RandomScoreFunctionBuilder());
				builder.must(queryBuilder);
			}
			
		}
		// QueryBuilders.filteredQuery(queryBuilder, filterBuilder)
		// boolFilterBuilder.must(FilterBuilders.queryFilter(builder));

		try {

			SearchResponse scrollResp = null;
			if (filterflag) {

				scrollResp = client.prepareSearch(index).setTypes(type).setQuery(builder).setPostFilter(boolFilterBuilder)
						.setSearchType(SearchType.SCAN).setScroll(new TimeValue(15, TimeUnit.MINUTES)).setSize(200).setFrom(beginingFrom)
						.setVersion(true).execute().actionGet();
			} else {
				scrollResp = client.prepareSearch(index).setTypes(type).setQuery(builder).setSearchType(SearchType.SCAN)
						.setScroll(new TimeValue(15, TimeUnit.MINUTES)).setSize(200).setFrom(beginingFrom).setVersion(true).execute()
						.actionGet();
			}

			return scrollResp.getScrollId();
		} catch (Exception e) {
			UConstants.log.error(" all exceptions : " + e.getMessage());
			throw e;
		} finally {
			long time2 = System.currentTimeMillis();
			UConstants.log.info("Time taken to scrollable excute query:" + (time2 - time1));
		}
	}

}
