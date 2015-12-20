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
import com.google.api.ads.adwords.axis.v201509.cm.Criterion
import com.google.api.ads.adwords.axis.v201509.cm.Money
import com.google.api.ads.adwords.axis.v201509.cm.BiddableAdGroupCriterion
import com.google.api.ads.adwords.axis.v201509.cm.BiddingStrategyConfiguration
import com.google.api.ads.adwords.axis.v201509.cm.Bids
import com.google.api.ads.adwords.axis.v201509.cm.CpcBid
import com.google.api.ads.adwords.axis.v201509.cm.AdGroupCriterionServiceInterface
import com.google.api.ads.adwords.axis.v201509.cm.AdGroupCriterionOperation
import com.google.api.ads.adwords.lib.selectorfields.v201509.cm.AdGroupCriterionField
import com.google.api.ads.adwords.axis.v201509.cm.Keyword
import com.google.api.ads.adwords.axis.v201509.cm.ReportDefinitionServiceInterface
import com.google.api.ads.adwords.axis.v201509.cm.ReportDefinitionReportType
import com.google.api.ads.adwords.lib.client.reporting.ReportingConfiguration
import com.google.api.ads.adwords.lib.utils.ReportDownloadResponseException
import com.google.api.ads.adwords.lib.utils.v201509.ReportDownloader
import com.google.api.ads.adwords.lib.jaxb.v201509.DownloadFormat
import com.google.api.ads.adwords.axis.v201509.cm.KeywordMatchType

object Utils {

  def main(args: Array[String]): Unit = {

    val session = getSession
    val adWordsServices: AdWordsServices = new AdWordsServices
    // Get the CampaignService.
    val campaignService = adWordsServices.get(session, classOf[CampaignServiceInterface]);
    val campaign = findCampaign("itdeviceonline")

    println(campaign.getName + " id : " + campaign.getId)
    showDataOnCampaign(campaign.getId.toString())
    val bidModifierService: AdGroupBidModifierServiceInterface = adWordsServices.get(session, classOf[AdGroupBidModifierServiceInterface])
    val builder = new SelectorBuilder()
    val selector: Selector = builder.fields("CampaignName", "CampaignId", "AdGroupId", "Id", "BidModifier", "KeywordText", "FirstPageCpc").contains("CampaignName", "itdeviceonline")
      //      .orderAscBy(CampaignField.Name)
      .offset(0)
      .limit(100)
      .build()
    selector.setPaging(new Paging(0, 10))

    //createAdGroup("mpn", 352064696, adWordsServices, session)
    val adGroupId = 23232312296L
    //23232312776L 23232312656L    23232312416L  23232312296L 23232312176L
    //Keyword with text 'retail competitive analysis', match type 'BROAD', criteria type 'KEYWORD', and ID 682811828 was found.
    //Keyword with text 'sample competitive analysis', match type 'BROAD', criteria type 'KEYWORD', and ID 625268409 was found.
    //Keyword with text 'website competitive analysis', match type 'BROAD', criteria type 'KEYWORD', and ID 263979756 was found.
    //val criterionId = 30001L
    //estimateTrafic(adGroupId, criterionId, "", adWordsServices, session)
    println("---     -  - - - - testing functions - -------- ---- ")
    //getKeywords(adWordsServices, session, adGroupId)
    val keywordId = 3478391383L
    //updateKeyword(adWordsServices, session, adGroupId, keywordId)
    //AverageCpc   AveragePosition  Conversions  CpcBid ValuePerConversion
    //reportFields(adWordsServices, session)
    //downloadReports(session, "report")
    //getCriteriaReport(session, "criteia")
    //estimates(adWordsServices, session)
    //estimateTop(adGroupId,"dynamic prices","http://deepricer.com")
    getKeywordReport(session, "keywords")
  }

  //  def estimateTop(adGroupId:Long, keywordT:String,url:String){
  //    
  //    val keyword = new Keyword()
  //    keyword.setText(keywordT)
  //    keyword.setMatchType(KeywordMatchType.PHRASE)
  //   
  //    
  //    // Create biddable ad group criterion.
  //    val biddableAdGroupCriterion = new BiddableAdGroupCriterion()
  //    biddableAdGroupCriterion.setAdGroupId(adGroupId)
  //    biddableAdGroupCriterion.setCriterion(keyword)
  //    biddableAdGroupCriterion.setDestinationUrl(url)
  //    val bids = new ManualCPCAdGroupCriterionBids()
  //    bids.setMaxCpc(new Bid(new Money(null, 500000L)))
  //    biddableAdGroupCriterion.setBids(bids)
  //    
  //    // Create operation.
  //    val operation = new AdGroupCriterionOperation()
  //    operation.setOperand(biddableAdGroupCriterion)
  //    operation.setOperator(Operator.ADD)
  //    val operations = Array(operation)
  //    val result = adGroupCriterionService.mutate(operations)
  //  }
  def estimates(adWordsServices: AdWordsServices, session: AdWordsSession) {

    import com.google.api.ads.adwords.axis.factory.AdWordsServices
    import com.google.api.ads.adwords.axis.v201509.cm.Criterion
    import com.google.api.ads.adwords.axis.v201509.cm.Keyword
    import com.google.api.ads.adwords.axis.v201509.cm.KeywordMatchType
    import com.google.api.ads.adwords.axis.v201509.cm.Language
    import com.google.api.ads.adwords.axis.v201509.cm.Location
    import com.google.api.ads.adwords.axis.v201509.cm.Money
    import com.google.api.ads.adwords.axis.v201509.o.AdGroupEstimateRequest
    import com.google.api.ads.adwords.axis.v201509.o.CampaignEstimateRequest
    import com.google.api.ads.adwords.axis.v201509.o.KeywordEstimate
    import com.google.api.ads.adwords.axis.v201509.o.KeywordEstimateRequest
    import com.google.api.ads.adwords.axis.v201509.o.TrafficEstimatorResult
    import com.google.api.ads.adwords.axis.v201509.o.TrafficEstimatorSelector
    import com.google.api.ads.adwords.axis.v201509.o.TrafficEstimatorServiceInterface
    import com.google.api.ads.adwords.lib.client.AdWordsSession
    import com.google.api.ads.common.lib.auth.OfflineCredentials
    import com.google.api.ads.common.lib.auth.OfflineCredentials.Api
    import com.google.api.client.auth.oauth2.Credential
    import java.util.ArrayList
    import java.util.List
    import scala.collection.JavaConverters._
    val trafficEstimatorService = adWordsServices.get(session, classOf[TrafficEstimatorServiceInterface])
    val keywords = new ArrayList[Keyword]()
    val marsCruiseKeyword = new Keyword()
    marsCruiseKeyword.setText("pricing intelligence")
    marsCruiseKeyword.setMatchType(KeywordMatchType.PHRASE)
    keywords.add(marsCruiseKeyword)
    val cheapCruiseKeyword = new Keyword()
    cheapCruiseKeyword.setText("woocommerce dynamic pricing & discounts")
    cheapCruiseKeyword.setMatchType(KeywordMatchType.PHRASE)
    keywords.add(cheapCruiseKeyword)
    val keywordEstimateRequests = new ArrayList[KeywordEstimateRequest]()
    for (keyword <- keywords.asScala) {
      val keywordEstimateRequest = new KeywordEstimateRequest()
      keywordEstimateRequest.setKeyword(keyword)
      keywordEstimateRequests.add(keywordEstimateRequest)
    }
    val negativeKeywordEstimateRequest = new KeywordEstimateRequest()
    negativeKeywordEstimateRequest.setKeyword(new Keyword(null, null, null, "hiking tour", KeywordMatchType.BROAD))
    negativeKeywordEstimateRequest.setIsNegative(true)
    keywordEstimateRequests.add(negativeKeywordEstimateRequest)
    val adGroupEstimateRequests = new ArrayList[AdGroupEstimateRequest]()
    val adGroupEstimateRequest = new AdGroupEstimateRequest()
    adGroupEstimateRequest.setKeywordEstimateRequests(keywordEstimateRequests.toArray(Array()))
    adGroupEstimateRequest.setMaxCpc(new Money(null, 1000000L))
    adGroupEstimateRequests.add(adGroupEstimateRequest)
    val campaignEstimateRequests = new ArrayList[CampaignEstimateRequest]()
    val campaignEstimateRequest = new CampaignEstimateRequest()
    campaignEstimateRequest.setAdGroupEstimateRequests(adGroupEstimateRequests.toArray(Array()))
    val unitedStates = new Location()
    unitedStates.setId(2840L)
    val english = new Language()
    english.setId(1000L)
    campaignEstimateRequest.setCriteria(Array(unitedStates, english))
    campaignEstimateRequests.add(campaignEstimateRequest)
    val selector = new TrafficEstimatorSelector()
    selector.setCampaignEstimateRequests(campaignEstimateRequests.toArray(Array()))
    val result = trafficEstimatorService.get(selector)
    if (result != null && result.getCampaignEstimates != null) {
      val keywordEstimates = result.getCampaignEstimates()(0).getAdGroupEstimates()(0)
        .getKeywordEstimates

      for (i <- 0 until keywordEstimates.length) {
        val bc = new BiddableAdGroupCriterion().getFirstPageCpc
        println(bc.getAmount)
        val keyword = keywordEstimateRequests.get(i).getKeyword
        val keywordEstimate = keywordEstimates(i)
        if (true == keywordEstimateRequests.get(i).getIsNegative) {
          //continue
        }
        val meanAverageCpc = calculateMean(keywordEstimate.getMin.getAverageCpc, keywordEstimate.getMax.getAverageCpc)
        val meanAveragePosition = calculateMean(keywordEstimate.getMin.getAveragePosition, keywordEstimate.getMax.getAveragePosition)
        val meanClicks = calculateMean(keywordEstimate.getMin.getClicksPerDay, keywordEstimate.getMax.getClicksPerDay)
        val meanTotalCost = calculateMean(keywordEstimate.getMin.getTotalCost, keywordEstimate.getMax.getTotalCost)
        //val topPosition=keywordEstimate.getMax.get
        System.out.printf("Results for the keyword with text \'%s\' and match type \'%s\':%n", keyword.getText,
          keyword.getMatchType)
        System.out.printf("\tEstimated average CPC: %s%n", formatMean(meanAverageCpc))
        System.out.printf("\tEstimated ad position: %s%n", formatMean(meanAveragePosition))
        System.out.printf("\tEstimated daily clicks: %s%n", formatMean(meanClicks))
        System.out.printf("\tEstimated daily cost: %s%n%n", formatMean(meanTotalCost))
      }
    } else {
      println("No traffic estimates were returned.")
    }
  }

  private def formatMean(mean: java.lang.Double): String = {
    if (mean == null) {
      return null
    }
    String.format("%.2f", mean)
  }

  private def calculateMean(minMoney: Money, maxMoney: Money): java.lang.Double = {
    if (minMoney == null || maxMoney == null) {
      return null
    }
    calculateMean(minMoney.getMicroAmount, maxMoney.getMicroAmount)
  }

  private def calculateMean(min: Number, max: Number): java.lang.Double = {
    if (min == null || max == null) {
      return null
    }
    (min.doubleValue() + max.doubleValue()) / 2
  }

  def getCriteriaReport(session: AdWordsSession, reportFile: String) {
    val query = "SELECT CampaignId, AdGroupId, Id, Criteria, CriteriaType, " +
      "Impressions, Clicks, Cost FROM CRITERIA_PERFORMANCE_REPORT " +
      "WHERE Status IN [ENABLED, PAUSED] " +
      "DURING YESTERDAY"
    val reportingConfiguration = new ReportingConfiguration.Builder().skipReportHeader(false)
      .skipColumnHeader(false)
      .skipReportSummary(false)
      .includeZeroImpressions(true)
      .build()
    session.setReportingConfiguration(reportingConfiguration)
    try {
      val response = new ReportDownloader(session).downloadReport(query, DownloadFormat.CSV)
      response.saveToFile(reportFile)
      System.out.printf("Report successfully downloaded to: %s%n", reportFile)
    } catch {
      case e: ReportDownloadResponseException => System.out.printf("Report was not downloaded due to: %s%n",
        e)
    }
  }
  def getKeywordReport(session: AdWordsSession, reportFile: String) {
    val query = "SELECT  AdGroupName,  AveragePosition, Id, KeywordName, QualityScore, Clicks, TopOfPageCpc FROM KEYWORDS_PERFORMANCE_REPORT "

    val reportingConfiguration = new ReportingConfiguration.Builder().skipReportHeader(false)
      .skipColumnHeader(false)
      .skipReportSummary(false)
      .includeZeroImpressions(true)
      .build()
    session.setReportingConfiguration(reportingConfiguration)
    try {
      val response = new ReportDownloader(session).downloadReport(query, DownloadFormat.CSV)
      response.saveToFile(reportFile)
      System.out.printf("Report successfully downloaded to: %s%n", reportFile)
    } catch {
      case e: ReportDownloadResponseException => System.out.printf("Report was not downloaded due to: %s%n",
        e)
    }
  }
  def downloadReports(session: AdWordsSession, reportFile: String) {
    import com.google.api.ads.adwords.lib.client.AdWordsSession
    import com.google.api.ads.adwords.lib.client.reporting.ReportingConfiguration
    import com.google.api.ads.adwords.lib.jaxb.v201509.DownloadFormat
    import com.google.api.ads.adwords.lib.jaxb.v201509.ReportDefinition
    import com.google.api.ads.adwords.lib.jaxb.v201509.ReportDefinitionDateRangeType
    import com.google.api.ads.adwords.lib.jaxb.v201509.ReportDefinitionReportType
    import com.google.api.ads.adwords.lib.jaxb.v201509.Selector
    import com.google.api.ads.adwords.lib.utils.ReportDownloadResponse
    import com.google.api.ads.adwords.lib.utils.ReportDownloadResponseException
    import com.google.api.ads.adwords.lib.utils.v201509.ReportDownloader
    import com.google.api.ads.common.lib.auth.OfflineCredentials
    import com.google.api.ads.common.lib.auth.OfflineCredentials.Api
    import com.google.api.client.auth.oauth2.Credential
    import com.google.common.collect.Lists
    import java.io.File
    //remove if not needed
    import scala.collection.JavaConversions._
    val selector = new Selector()
    selector.getFields.addAll(Lists.newArrayList("CampaignId", "AdGroupId", "Id", "CriteriaType", "Criteria",
      "FinalUrls", "Impressions", "Clicks", "Cost"))
    val reportDefinition = new ReportDefinition()
    reportDefinition.setReportName("Criteria performance report #" + System.currentTimeMillis())
    reportDefinition.setDateRangeType(ReportDefinitionDateRangeType.YESTERDAY)
    reportDefinition.setReportType(ReportDefinitionReportType.CRITERIA_PERFORMANCE_REPORT)
    reportDefinition.setDownloadFormat(DownloadFormat.CSV)
    val reportingConfiguration = new ReportingConfiguration.Builder().skipReportHeader(false)
      .skipColumnHeader(false)
      .skipReportSummary(false)
      .includeZeroImpressions(false)
      .build()
    session.setReportingConfiguration(reportingConfiguration)
    reportDefinition.setSelector(selector)
    try {
      val response = new ReportDownloader(session).downloadReport(reportDefinition)
      response.saveToFile(reportFile)
      System.out.printf("Report successfully downloaded to: %s%n", reportFile)
    } catch {
      case e: ReportDownloadResponseException => System.out.printf("Report was not downloaded due to: %s%n",
        e)
    }
  }

  def reportFields(adWordsServices: AdWordsServices, session: AdWordsSession) {
    val reportDefinitionService = adWordsServices.get(session, classOf[ReportDefinitionServiceInterface])
    val reportDefinitionFields = reportDefinitionService.getReportFields(ReportDefinitionReportType.KEYWORDS_PERFORMANCE_REPORT)
    println("Available fields for report:")
    for (reportDefinitionField <- reportDefinitionFields) {
      System.out.print("\t" + reportDefinitionField.getFieldName + "(" + reportDefinitionField.getFieldType +
        ") := [")
      if (reportDefinitionField.getEnumValues != null) {
        for (enumValue <- reportDefinitionField.getEnumValues) {
          System.out.print(enumValue + ", ")
        }
      }
      println("]")
    }
  }
  def getKeywords(adWordsServices: AdWordsServices, session: AdWordsSession, adGroupId: java.lang.Long) {
    val PAGE_SIZE = 100
    val adGroupCriterionService = adWordsServices.get(session, classOf[AdGroupCriterionServiceInterface])
    var offset = 0
    var morePages = true
    val builder = new SelectorBuilder()

    var selector = builder.fields(AdGroupCriterionField.Id, AdGroupCriterionField.CriteriaType, AdGroupCriterionField.KeywordMatchType,
      AdGroupCriterionField.KeywordText)
      .orderAscBy(AdGroupCriterionField.KeywordText)
      .offset(offset)
      .limit(PAGE_SIZE)
      .in(AdGroupCriterionField.AdGroupId, adGroupId.toString)
      .in(AdGroupCriterionField.CriteriaType, "KEYWORD")
      .build()

    while (morePages) {
      val page = adGroupCriterionService.get(selector)
      if (page.getEntries != null && page.getEntries.length > 0) {
        for (adGroupCriterionResult <- page.getEntries) {
          val keyword = adGroupCriterionResult.getCriterion.asInstanceOf[Keyword]
          System.out.printf("Keyword with text '%s', match type '%s', criteria type '%s', and ID %d was found.%n",
            keyword.getText, keyword.getMatchType, keyword.getType, keyword.getId)
        }
      } else {
        println("No ad group criteria were found.")
      }
      offset += PAGE_SIZE
      selector = builder.increaseOffsetBy(PAGE_SIZE).build()
      morePages = offset < page.getTotalNumEntries
    }

  }

  def updateKeyword(adWordsServices: AdWordsServices, session: AdWordsSession, adGroupId: java.lang.Long, keywordId: java.lang.Long, bidAmount : Long) {
    val adGroupCriterionService = adWordsServices.get(session, classOf[AdGroupCriterionServiceInterface])
    //selector.setFields(new String[] {"Id", "AdGroupId", "MatchType", "KeywordText", "FirstPageCpc"});
    val criterion = new Criterion()
    criterion.setId(keywordId)
    var biddableAdGroupCriterion = new BiddableAdGroupCriterion()
    biddableAdGroupCriterion.setAdGroupId(adGroupId)
    biddableAdGroupCriterion.setCriterion(criterion)
    //biddableAdGroupCriterion.setDestinationUrl("http://www.deepricer.com")

    val biddingStrategyConfiguration = new BiddingStrategyConfiguration()
    val bid = new CpcBid()
    bid.setBid(new Money(null, bidAmount))
    biddingStrategyConfiguration.setBids(Array(bid))
    // biddingStrategyConfiguration.setBiddingStrategyName("")
    biddableAdGroupCriterion.setBiddingStrategyConfiguration(biddingStrategyConfiguration)

    val operation = new AdGroupCriterionOperation()
    operation.setOperand(biddableAdGroupCriterion)
    operation.setOperator(Operator.SET)
    val operations = Array(operation)
    val result = adGroupCriterionService.mutate(operations)
    // adGroupCriterionService.query(query)
    //    val builder = new SelectorBuilder()
    //    val selector: Selector = builder.fields("Id", "AdGroupId", "MatchType", "KeywordText", "FirstPageCpc")
    //      //      .orderAscBy(CampaignField.Name)
    //      .offset(0)
    //      .limit(100)
    //      .build()
    //      val res=adGroupCriterionService.get(selector)
    //         
    //    for (adGroupCriterionResult <- res.getEntries ) {
    //     // adGroupCriterionResult.get
    //    }

    for (adGroupCriterionResult <- result.getValue if adGroupCriterionResult.isInstanceOf[BiddableAdGroupCriterion]) {
      biddableAdGroupCriterion = adGroupCriterionResult.asInstanceOf[BiddableAdGroupCriterion]
      println("Ad group criterion with ad group id \"" + biddableAdGroupCriterion.getAdGroupId +
        "\", criterion id \"" +
        biddableAdGroupCriterion.getCriterion +
        "\", type \"" +
        biddableAdGroupCriterion.getCriterion.getCriterionType +
        "\", and bid \"" +
        biddableAdGroupCriterion.getBiddingStrategyConfiguration()
        .getBids()(0).asInstanceOf[CpcBid]
        .getBid
        .getMicroAmount +
        "\", first page bid price \"" +
        biddableAdGroupCriterion.getBiddingStrategyConfiguration() +
        "\" was updated.")
    }
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