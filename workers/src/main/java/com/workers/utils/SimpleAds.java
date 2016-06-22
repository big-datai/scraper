/*package com.workers.utils;

import java.rmi.RemoteException;

import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.axis.v201506.cm.AdvertisingChannelType;
import com.google.api.ads.adwords.axis.v201506.cm.ApiException;
import com.google.api.ads.adwords.axis.v201506.cm.BiddingStrategyConfiguration;
import com.google.api.ads.adwords.axis.v201506.cm.BiddingStrategyType;
import com.google.api.ads.adwords.axis.v201506.cm.Budget;
import com.google.api.ads.adwords.axis.v201506.cm.BudgetBudgetDeliveryMethod;
import com.google.api.ads.adwords.axis.v201506.cm.BudgetBudgetPeriod;
import com.google.api.ads.adwords.axis.v201506.cm.BudgetOperation;
import com.google.api.ads.adwords.axis.v201506.cm.BudgetServiceInterface;
import com.google.api.ads.adwords.axis.v201506.cm.Campaign;
import com.google.api.ads.adwords.axis.v201506.cm.CampaignOperation;
import com.google.api.ads.adwords.axis.v201506.cm.CampaignPage;
import com.google.api.ads.adwords.axis.v201506.cm.CampaignReturnValue;
import com.google.api.ads.adwords.axis.v201506.cm.CampaignServiceInterface;
import com.google.api.ads.adwords.axis.v201506.cm.CampaignStatus;
import com.google.api.ads.adwords.axis.v201506.cm.Money;
import com.google.api.ads.adwords.axis.v201506.cm.Operator;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.auth.oauth2.Credential;

public class SimpleAds {

	public static void main(String[] str) throws OAuthException, ValidationException, ConfigurationLoadException, ApiException, RemoteException {

		
		// Construct an OAuth2 credential.
		Credential oAuth2Credential = new OfflineCredentials.Builder()
		    .forApi(Api.ADWORDS)
		    .fromFile()
		    .build()
		    .generateCredential();
		
		
		// Construct an AdWordsSession.
		AdWordsSession session = new AdWordsSession.Builder()
		    .fromFile()
		    .withOAuth2Credential(oAuth2Credential)
		    .build();
		
		// Construct a AdWords service factory, which can only be used once per thread,
		// but should be reused as much as possible.
		AdWordsServices adWordsServices = new AdWordsServices();
		
		// Get the CampaignService.
		CampaignServiceInterface campaignService =
		     adWordsServices.get(session, CampaignServiceInterface.class);
		
		// Get the BudgetService.
		BudgetServiceInterface budgetService =
		    adWordsServices.get(session, BudgetServiceInterface.class);

		// Create a budget, which can be shared by multiple campaigns.
		Budget sharedBudget = new Budget();
		sharedBudget.setName("Interplanetary Cruise #" + System.currentTimeMillis());
		Money budgetAmount = new Money();
		budgetAmount.setMicroAmount(50000000L);
		sharedBudget.setAmount(budgetAmount);
		sharedBudget.setDeliveryMethod(BudgetBudgetDeliveryMethod.STANDARD);
		sharedBudget.setPeriod(BudgetBudgetPeriod.DAILY);

		BudgetOperation budgetOperation = new BudgetOperation();
		budgetOperation.setOperand(sharedBudget);
		budgetOperation.setOperator(Operator.ADD);

		// Execute the new budget operation and save the assigned budget ID.
		Long budgetId =
		    budgetService.mutate(new BudgetOperation[] {budgetOperation}).getValue(0).getBudgetId();

		
		Campaign campaign = new Campaign();
		campaign.setName("Interplanetary Cruise #" + System.currentTimeMillis());
		campaign.setStatus(CampaignStatus.PAUSED);
		campaign.setAdvertisingChannelType(AdvertisingChannelType.SEARCH);

		// Set the bidding strategy configuration.
		BiddingStrategyConfiguration biddingStrategyConfiguration =
		    new BiddingStrategyConfiguration();
		biddingStrategyConfiguration.setBiddingStrategyType(BiddingStrategyType.MANUAL_CPC);
		campaign.setBiddingStrategyConfiguration(biddingStrategyConfiguration);

		// Only the budgetId should be sent.
		Budget budget = new Budget();
		budget.setBudgetId(budgetId);
		campaign.setBudget(budget);
		
		CampaignOperation operation = new CampaignOperation();
		operation.setOperand(campaign);
		operation.setOperator(Operator.ADD);
		CampaignOperation[] operations = new CampaignOperation[] {operation};
		
		
		CampaignReturnValue result = campaignService.mutate(operations);
		
//		CampaignPage p = campaignService.query("SELECT Id ");
		
		System.out.println();
		if (result != null && result.getValue() != null) {
			  for (Campaign campaignResult : result.getValue()) {
			    System.out.println("Campaign with name \""
			        + campaignResult.getName() + "\" and id \""
			        + campaignResult.getId() + "\" was added.");
			  }
			} else {
			  System.out.println("No campaigns were added.");
			}
		
		

	}
}
*/