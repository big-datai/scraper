package ads.utils

import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.utils.v201509.SelectorBuilder
import com.google.api.ads.adwords.axis.v201509.cm.AdGroup
import com.google.api.ads.adwords.axis.v201509.cm.AdGroupBidModifierPage
import com.google.api.ads.adwords.axis.v201509.cm.AdGroupBidModifierServiceInterface
import com.google.api.ads.adwords.axis.v201509.cm.AdGroupOperation
import com.google.api.ads.adwords.axis.v201509.cm.AdGroupServiceInterface
import com.google.api.ads.adwords.axis.v201509.cm.AdGroupStatus
import com.google.api.ads.adwords.axis.v201509.cm.Campaign
import com.google.api.ads.adwords.axis.v201509.cm.CampaignServiceInterface
import com.google.api.ads.adwords.axis.v201509.cm.DataServiceInterface
import com.google.api.ads.adwords.axis.v201509.cm.Operator
import com.google.api.ads.adwords.axis.v201509.cm.Paging
import com.google.api.ads.adwords.axis.v201509.cm.Selector
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.api.ads.adwords.lib.selectorfields.v201509.cm.DataField
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api
import com.google.api.ads.common.lib.auth.OfflineCredentials.Builder
import com.google.api.client.auth.oauth2.Credential
import com.google.api.ads.adwords.axis.v201509.o.TargetingIdeaSelector
import com.google.api.ads.adwords.axis.v201509.o.TargetingIdeaServiceInterface
import com.google.api.ads.adwords.axis.v201509.o.IdeaType
import com.google.api.ads.adwords.axis.v201509.o.AttributeType
import com.google.api.ads.adwords.axis.v201509.o.RequestType
import com.google.api.ads.adwords.axis.v201509.o.RelatedToQuerySearchParameter
import com.google.api.ads.adwords.axis.v201509.o.LanguageSearchParameter
import com.google.api.ads.adwords.axis.v201509.cm.Language
import com.google.api.ads.adwords.axis.v201509.o.StringAttribute
import com.google.api.ads.common.lib.utils.Maps
import com.google.api.ads.adwords.axis.v201509.o.IntegerSetAttribute
import com.google.api.client.repackaged.com.google.common.base.Joiner
import com.google.api.ads.adwords.axis.v201509.o.LongAttribute
import com.google.common.primitives.Ints

object Utils {

  def main(args: Array[String]): Unit = {

    println("hello there")
    val session = getSession

    val adWordsServices: AdWordsServices = new AdWordsServices
    // Get the CampaignService.
    val campaignService = adWordsServices.get(session, classOf[CampaignServiceInterface]);
    val campaign = findCampaign("Google Build")

    println(campaign.getName + " id : " + campaign.getId)
     showDataOnCampaign(campaign.getId.toString())
    val bidModifierService: AdGroupBidModifierServiceInterface = adWordsServices.get(session, classOf[AdGroupBidModifierServiceInterface])
    val builder = new SelectorBuilder()
    val selector: Selector = builder.fields("CampaignName", "CampaignId", "AdGroupId", "Id", "BidModifier").contains("CampaignName", "itdeviceonline")
      //      .orderAscBy(CampaignField.Name)
      .offset(0)
      .limit(100)
      .build()
    selector.setPaging(new Paging(0, 10))

    //createAdGroup("mpn", 352064696, adWordsServices, session)
    val adGroupId = 23232312656L
    val criterionId = 30001L
    estimateTrafic(adGroupId, criterionId, "", adWordsServices, session)

  }

  def setBid() = {

  }
  def getTopPageBid(){
    
  }
  
 
//  def getMoreKeywords(keyword:String, adWordsServices: AdWordsServices, session: AdWordsSession)={
//        val targetingIdeaService =
//        adWordsServices.get(session, classOf[TargetingIdeaServiceInterface]);
//
//    // Create selector.
//    val selector = new TargetingIdeaSelector();
//    selector.setRequestType(RequestType.IDEAS);
//    selector.setIdeaType(IdeaType.KEYWORD);
//    val att:Array[AttributeType] =Array(AttributeType.KEYWORD_TEXT, AttributeType.SEARCH_VOLUME, AttributeType.CATEGORY_PRODUCTS_AND_SERVICES)
//    selector.setRequestedAttributeTypes(att);
//
//    // Set selector paging (required for targeting idea service).
//    val paging = new Paging();
//    paging.setStartIndex(0);
//    paging.setNumberResults(10);
//    selector.setPaging(paging);
//
//    // Create related to query search parameter.
//    val relatedToQuerySearchParameter = new RelatedToQuerySearchParameter();
//    relatedToQuerySearchParameter.setQueries(Array[ String] {"mars cruise"});
//
//    // Language setting (optional).
//    // The ID can be found in the documentation:
//    //   https://developers.google.com/adwords/api/docs/appendix/languagecodes
//    // See the documentation for limits on the number of allowed language parameters:
//    //   https://developers.google.com/adwords/api/docs/reference/latest/TargetingIdeaService.LanguageSearchParameter
//    val languageParameter = new LanguageSearchParameter();
//    val english = new Language();
//    english.setId(1000L);
//    languageParameter.setLanguages(Array[ Language] {english});
//
//    selector.setSearchParameters(
//        Array( relatedToQuerySearchParameter, languageParameter));
//
//    // Get related keywords.
//    val page = targetingIdeaService.get(selector);
//
//    // Display related keywords.
//    if (page.getEntries() != null && page.getEntries().length > 0) {
//      for ( targetingIdea <- page.getEntries()) {
//        val data = Maps.toMap(targetingIdea.getData());
//        val keyword:StringAttribute = data.get(AttributeType.KEYWORD_TEXT);
//
//        val categories:IntegerSetAttribute =
//             data.get(AttributeType.CATEGORY_PRODUCTS_AND_SERVICES);
//        var categoriesString = "(none)";
//        if (categories != null && categories.getValue() != null) {
//          categoriesString = Joiner.on(", ").join(Ints.asList(categories.getValue()));
//        }
//        val averageMonthlySearches =
//            ((LongAttribute) data.get(AttributeType.SEARCH_VOLUME))
//                .getValue();
//        System.out.println("Keyword with text '" + keyword.getValue()
//            + "', and average monthly search volume '" + averageMonthlySearches
//            + "' was found with categories: " + categoriesString);
//      }
//    } else {
//      System.out.println("No related keywords were found.");
//    }
//  }
  
  //GetKeywordBidSimulations  
  def estimateTrafic(adGroupId: Long, criterionId: Long, keyword: String, adWordsServices: AdWordsServices, session: AdWordsSession) = {
    // Get the DataService.
    val dataService =
      adWordsServices.get(session, classOf[DataServiceInterface]);

    // Create selector.
    val selector = new SelectorBuilder()
      .fields(
        DataField.AdGroupId,
        DataField.CriterionId,
        DataField.StartDate,
        DataField.EndDate,
        DataField.Bid,
        DataField.LocalClicks,
        DataField.LocalCost,
        DataField.LocalImpressions)
      .equals(DataField.AdGroupId, adGroupId.toString())
      .equals(DataField.CriterionId, criterionId.toString())
      .build();
    val page = dataService.getCriterionBidLandscape(selector);

    // Display bid landscapes.
    if (page.getEntries() != null) {
      for (criterionBidLandscape <- page.getEntries()) {
        System.out.println("Criterion bid landscape with ad group id \""
          + criterionBidLandscape.getAdGroupId() + "\", criterion id \""
          + criterionBidLandscape.getCriterionId() + "\", start date \""
          + criterionBidLandscape.getStartDate() + "\", end date \""
          + criterionBidLandscape.getEndDate() + "\", with landscape points: ");

        for (
          bidLanscapePoint <- criterionBidLandscape
            .getLandscapePoints()
        ) {
          System.out.println("\t{bid: " + bidLanscapePoint.getBid().getMicroAmount() + " clicks: "
            + bidLanscapePoint.getClicks() + " cost: "
            + bidLanscapePoint.getCost().getMicroAmount() + " impressions: "
            + bidLanscapePoint.getImpressions() + "}");
        }
        System.out.println(" was found.");
      }
    } else {
      System.out.println("No criterion bid landscapes were found.");
    }
  }
  def showDataOnCampaign(id: String) {
    val session: AdWordsSession = getSession

    // Construct a AdWords service factory, which can only be used once per thread,
    // but should be reused as much as possible.
    val adWordsServices: AdWordsServices = new AdWordsServices

    // Get the CampaignService.
    val campaignService: CampaignServiceInterface = adWordsServices.get(session, classOf[CampaignServiceInterface])

    // Get the AdGroupBidModifierService.
    val bidModifierService: AdGroupBidModifierServiceInterface =
      adWordsServices.get(session, classOf[AdGroupBidModifierServiceInterface])

    // Create selector.
    val builder = new SelectorBuilder()
    val selector: Selector = builder.fields("CampaignName", "CampaignId", "AdGroupId", "Id", "BidModifier")
      //      .orderAscBy(CampaignField.Name)
      .offset(0)
      .limit(100)
      .equals("CampaignId", id)
      .build()
    //selector.setFields("CampaignId", "AdGroupId", "Id", "BidModifier")
    selector.setPaging(new Paging(0, 30))

    // Make a 'get' request.
    val page: AdGroupBidModifierPage = bidModifierService.get(selector)
    //
    //    // Display bid modifiers.
    if (page.getEntries != null) {
      for (modifier <- page.getEntries) {
        val value = Option(modifier.getBidModifier).getOrElse("unset")
        //if("352064696".equals(modifier.getCampaignId))
        println(s"Campaign ID ${modifier.getCampaignId}, AdGroup ID ${modifier.getAdGroupId}, Criterion ID ${modifier.getCriterion.getId}"
          + s" has ad group level modifier: ${value.toString}")
      }
    } else {
      println("No bid modifiers were found.")
    }
  }
  def findCampaign(name: String) = {

    val session: AdWordsSession = getSession
    // Construct a AdWords service factory, which can only be used once per thread,
    // but should be reused as much as possible.
    val adWordsServices: AdWordsServices = new AdWordsServices
    // Get the CampaignService.
    val campaignService: CampaignServiceInterface = adWordsServices.get(session, classOf[CampaignServiceInterface])
    // Get the AdGroupBidModifierService.
    val bidModifierService: AdGroupBidModifierServiceInterface =
      adWordsServices.get(session, classOf[AdGroupBidModifierServiceInterface])
    // Create selector.
    val builder = new SelectorBuilder()
    val selector2: Selector = builder.fields("CampaignName", "CampaignId")
      //      .orderAscBy(CampaignField.Name)
      .offset(0)
      .limit(100)
      .build()
    val page2 = campaignService.get(selector2)
    // Display campaigns.
    var res: Campaign = null
    if (page2.getEntries() != null) {
      for (campaign <- page2.getEntries) {
        if (name.equals(campaign.getName)) {
          res = campaign

        }
      }
    }

    res
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
  def getSession: AdWordsSession = {
    // Construct an OAuth2 credential.
    val oAuth2Credential: Credential = new Builder().forApi(Api.ADWORDS).fromFile.build.generateCredential

    // Construct an AdWordsSession.
    val session: AdWordsSession = new AdWordsSession.Builder().fromFile.withOAuth2Credential(oAuth2Credential).build
    session
  }
}