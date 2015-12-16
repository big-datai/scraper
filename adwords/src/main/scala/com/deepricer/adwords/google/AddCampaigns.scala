package com.deepricer.adwords.google

import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201509.cm._
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api
import com.google.api.client.auth.oauth2.Credential
import org.joda.time.DateTime

/**
 * This example adds campaigns.
 * <p>
 * <p>Credentials and properties in {@code fromFile()} are pulled from the
 * "ads.properties" file. See README for more info.
 */
object AddCampaigns {
  @throws(classOf[Exception])
  def main(args: Array[String]) {
    val oAuth2Credential: Credential = new OfflineCredentials.Builder().forApi(Api.ADWORDS).fromFile.build.generateCredential
    val session: AdWordsSession = new AdWordsSession.Builder().fromFile.withOAuth2Credential(oAuth2Credential).build
    val adWordsServices: AdWordsServices = new AdWordsServices
    runExample(adWordsServices, session)
  }

  @throws(classOf[Exception])
  def runExample(adWordsServices: AdWordsServices, session: AdWordsSession) {
    val budgetService: BudgetServiceInterface = adWordsServices.get(session, classOf[BudgetServiceInterface])
    val sharedBudget: Budget = new Budget
    sharedBudget.setName("Interplanetary Cruise #" + System.currentTimeMillis)
    val budgetAmount: Money = new Money
    budgetAmount.setMicroAmount(50000000L)
    sharedBudget.setAmount(budgetAmount)
    sharedBudget.setDeliveryMethod(BudgetBudgetDeliveryMethod.STANDARD)
    sharedBudget.setPeriod(BudgetBudgetPeriod.DAILY)
    val budgetOperation: BudgetOperation = new BudgetOperation
    budgetOperation.setOperand(sharedBudget)
    budgetOperation.setOperator(Operator.ADD)
    val budgetId: Long = budgetService.mutate(Array[BudgetOperation](budgetOperation)).getValue(0).getBudgetId
    val campaignService: CampaignServiceInterface = adWordsServices.get(session, classOf[CampaignServiceInterface])
    val campaign: Campaign = new Campaign
    campaign.setName("Interplanetary Cruise #" + System.currentTimeMillis)
    campaign.setStatus(CampaignStatus.PAUSED)
    val biddingStrategyConfiguration: BiddingStrategyConfiguration = new BiddingStrategyConfiguration
    biddingStrategyConfiguration.setBiddingStrategyType(BiddingStrategyType.MANUAL_CPC)
    val cpcBiddingScheme: ManualCpcBiddingScheme = new ManualCpcBiddingScheme
    cpcBiddingScheme.setEnhancedCpcEnabled(false)
    biddingStrategyConfiguration.setBiddingScheme(cpcBiddingScheme)
    campaign.setBiddingStrategyConfiguration(biddingStrategyConfiguration)
    campaign.setStartDate(new DateTime().plusDays(1).toString("yyyyMMdd"))
    campaign.setEndDate(new DateTime().plusDays(30).toString("yyyyMMdd"))
    campaign.setAdServingOptimizationStatus(AdServingOptimizationStatus.ROTATE)
    campaign.setFrequencyCap(new FrequencyCap(5L, TimeUnit.DAY, Level.ADGROUP))
    val budget: Budget = new Budget
    budget.setBudgetId(budgetId)
    campaign.setBudget(budget)
    campaign.setAdvertisingChannelType(AdvertisingChannelType.SEARCH)
    val networkSetting: NetworkSetting = new NetworkSetting
    networkSetting.setTargetGoogleSearch(true)
    networkSetting.setTargetSearchNetwork(true)
    networkSetting.setTargetContentNetwork(false)
    networkSetting.setTargetPartnerSearchNetwork(false)
    campaign.setNetworkSetting(networkSetting)
    val geoTarget: GeoTargetTypeSetting = new GeoTargetTypeSetting
    geoTarget.setPositiveGeoTargetType(GeoTargetTypeSettingPositiveGeoTargetType.DONT_CARE)
    campaign.setSettings(Array[Setting](geoTarget))
    val campaign2: Campaign = new Campaign
    campaign2.setName("Interplanetary Cruise banner #" + System.currentTimeMillis)
    campaign2.setStatus(CampaignStatus.PAUSED)
    val biddingStrategyConfiguration2: BiddingStrategyConfiguration = new BiddingStrategyConfiguration
    biddingStrategyConfiguration2.setBiddingStrategyType(BiddingStrategyType.MANUAL_CPC)
    campaign2.setBiddingStrategyConfiguration(biddingStrategyConfiguration2)
    val budget2: Budget = new Budget
    budget2.setBudgetId(budgetId)
    campaign2.setBudget(budget2)
    campaign2.setAdvertisingChannelType(AdvertisingChannelType.DISPLAY)
    val operation: CampaignOperation = new CampaignOperation
    operation.setOperand(campaign)
    operation.setOperator(Operator.ADD)
    val operation2: CampaignOperation = new CampaignOperation
    operation2.setOperand(campaign2)
    operation2.setOperator(Operator.ADD)
    val operations: Array[CampaignOperation] = Array[CampaignOperation](operation, operation2)
    val result: CampaignReturnValue = campaignService.mutate(operations)
    for (campaignResult <- result.getValue) {
      System.out.printf("Campaign with name '%s' and ID %d was added.%n", campaignResult.getName, campaignResult.getId)
    }
  }
}