package ads.utils

import ads.gen._
import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201506.cm.CampaignServiceInterface
import com.google.api.ads.adwords.axis.v201506.cm.AdGroupBidModifierServiceInterface
import com.google.api.ads.adwords.axis.v201506.cm.Selector
import com.google.api.ads.adwords.axis.v201506.cm.AdGroupBidModifierPage
import com.google.api.ads.adwords.axis.v201506.cm.Paging
import com.google.api.ads.adwords.axis.utils.v201506.SelectorBuilder
import com.google.api.ads.adwords.axis.v201506.cm.Money
import com.google.api.ads.adwords.axis.v201506.cm.CpcBid
import com.google.api.ads.adwords.axis.v201506.cm.BiddingStrategyConfiguration
import com.google.api.ads.adwords.axis.v201506.cm.Bids
import com.google.api.ads.adwords.axis.v201506.cm.AdGroupBidModifierOperation
import com.google.api.ads.adwords.axis.v201506.cm.Operator
import com.google.api.ads.adwords.axis.v201506.o.KeywordEstimateRequest
import java.util.ArrayList
import com.google.api.ads.adwords.axis.v201506.cm.Keyword
import com.google.api.ads.adwords.axis.v201506.cm.KeywordMatchType
import com.google.api.ads.adwords.axis.v201506.o.TrafficEstimatorResult
import com.google.api.ads.adwords.axis.v201506.o.TrafficEstimatorService
import com.google.api.ads.adwords.axis.v201506.cm.AdGroupStatus
import com.google.api.ads.adwords.axis.v201506.cm.AdGroup
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.api.ads.adwords.axis.v201506.cm.AdGroupServiceInterface
import com.google.api.ads.adwords.axis.v201506.cm.AdGroupOperation
import com.google.api.ads.adwords.axis.v201506.cm.AdGroupReturnValue

object Utils {

  def main(args: Array[String]): Unit = {

    println("hello there")
    val session = CampaignService.getSession

    val adWordsServices: AdWordsServices = new AdWordsServices
    // Get the CampaignService.
    val campaignService = adWordsServices.get(session, classOf[CampaignServiceInterface]);
    val campaign = CampaignService.findCampaign("itdeviceonline")

    println(campaign.getName + " id : " + campaign.getId)

    val bidModifierService: AdGroupBidModifierServiceInterface = adWordsServices.get(session, classOf[AdGroupBidModifierServiceInterface])
    val builder = new SelectorBuilder()
    val selector: Selector = builder.fields("CampaignName", "CampaignId", "AdGroupId", "Id", "BidModifier").contains("CampaignName", "itdeviceonline")
      //      .orderAscBy(CampaignField.Name)
      .offset(0)
      .limit(100)
      .build()
    selector.setPaging(new Paging(0, 10))
    
    createAdGroup("mpn", 352064696, adWordsServices, session)
    
    
  }
  def addKeyWordsToGroup()={
    
  }
  
  def createAdGroup(name: String, campaignId: Long, adWordsServices: AdWordsServices, session: AdWordsSession) = {
    val adGroupService = adWordsServices.get(session, classOf[AdGroupServiceInterface]);
    val adGroup = new AdGroup();
    adGroup.setName(name + System.currentTimeMillis());
    adGroup.setStatus(AdGroupStatus.ENABLED);
    adGroup.setCampaignId(campaignId);
    // Create operations.

    val operation = new AdGroupOperation();
    operation.setOperand(adGroup);
    operation.setOperator(Operator.ADD);
    val operations = Array[AdGroupOperation] { operation }
    // Add ad groups.
    val result = adGroupService.mutate(operations);
  }
}