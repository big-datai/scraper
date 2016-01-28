package com.workers.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import com.utils.constants.UConstants;
import com.utils.constants.UStringUtils;
import com.utils.messages.BigMessage;
import com.workers.main.CSV2Kafka;

public class ReadCSV {
	String csvFile;

	LinkedList<BigMessage> messages = new LinkedList<BigMessage>();

	public ReadCSV(String path) {
		csvFile = path;
	}

	public LinkedList<BigMessage> loadFile() {
		BufferedReader br = null;
		String line = "";
		// regex: split on the comma only if that comma has zero, or an even
		// number of quotes ahead of it.
		String cvsSplitBy = ",";//",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

		try {
			int cnt = 0;
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] msg = line.split(cvsSplitBy, -1);
				cnt++;
				// Mapping:
				/*
				 * domain = msg[0]; id = msg[1]; category = msg[2]; title =
				 * msg[3]; url = msg[4]; price = msg[5]; brand = msg[6]; mpn =
				 * msg[7]; upc = msg[8]; sku = msg[9]; otherId = msg[10];
				 */

				if (msg == null || msg.length != CSV2Kafka.COL_NUM && msg.length != CSV2Kafka.COL_NUM+1 && msg.length!= CSV2Kafka.COL_NUM+2 && msg.length!= CSV2Kafka.COL_NUM+3) {
					System.out.println(msg[0]);
					continue;
				}
				try {
					BigMessage m = new BigMessage();
					// m.setDomain(msg[0]);
					// //ID,Category,Title,URL,Price,Brand,MPN,UPC,SKU
					// System.out.println(msg[0]);
					m.storeId = msg[0];
					// System.out.println(msg[1]);
					m.setCat(msg[1]);
					// System.out.println(msg[2]);
					m.setTitle(msg[2]);
					// System.out.println(msg[3]);
					m.setUrl(msg[3]);
					// System.out.println(msg[4]);
					m.setPrice(msg[4]);
					// System.out.println(msg[5]);
					m.setBrand(msg[5]);
					// System.out.println(msg[6]);
					m.setMpn(msg[6]);
					// System.out.println(msg[7]);
					m.setUpc(msg[7]);
					// System.out.println(msg[8]);
					m.setSku(msg[8]);
					// System.out.println(msg[9]);
					// m.setOtherID(msg[10]);
					// System.out.println(msg[10]);
					if (msg.length == CSV2Kafka.COL_NUM+1){
						m.locale=msg[9];
					}else if (msg.length == CSV2Kafka.COL_NUM+2){
						m.lBound=msg[9];
						m.uBound=msg[10];
					}else if (msg.length == CSV2Kafka.COL_NUM+3){
						m.locale=msg[9];
						m.lBound=msg[10];
						m.uBound=msg[11];
					}else if (msg.length == CSV2Kafka.COL_NUM+4){
						m.locale=msg[9];
						m.lBound=msg[10];
						m.uBound=msg[11];
						m.ggId=msg[12];
					}
					
					
					if (cnt > 1){
						m.domain=UStringUtils.getDomainName(msg[3]);
					}
					if (cnt > 1) {
						messages.add(m);
					}
				} catch (Exception e) {
					UConstants.log.error("ERROR script exceptions" + e.getMessage());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return messages;
	}
}