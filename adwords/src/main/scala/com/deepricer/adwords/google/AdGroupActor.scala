package com.deepricer.adwords.google

import akka.actor.{ActorRef, ActorLogging, Actor}
import com.deepricer.adwords.domain.{CSVLine, AdGroupCassandra}
import com.deepricer.adwords.google.AdGroupActor.AddAdGroup
import com.deepricer.adwords.google.AdTextActor.AddAdText
import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201509.cm._
import com.google.api.ads.adwords.lib.client.AdWordsSession

/**
 * Created by admin on 12/15/15.
 */

object AdGroupActor {
  case class AddAdGroup(csvLine: CSVLine)
//  case class SetAdGroup(adGroupCassandra: AdGroupCassandra)
}

class AdGroupActor(session: AdWordsSession, adTextActor: ActorRef) extends Actor with ActorLogging {
  def addCampaign(csvLine: CSVLine, operator: Operator) = {
    val adWordsServices: AdWordsServices = new AdWordsServices
    val adGroupService:AdGroupServiceInterface =
      adWordsServices.get(session, classOf[AdGroupServiceInterface])

    // Create ad group.
    val adGroup:AdGroup = new AdGroup
    adGroup.setName(csvLine.mpn)
    adGroup.setStatus(AdGroupStatus.ENABLED)
    adGroup.setCampaignId(csvLine.campaignId.toLong)

    val operation: AdGroupOperation = new AdGroupOperation
    operation.setOperand(adGroup)
    operation.setOperator(operator)

    // Add ad groups.
    val result: AdGroupReturnValue = adGroupService.mutate(Array[AdGroupOperation](operation))

    var i: Long = 1L
    result.getValue foreach {  group =>
      adTextActor ! AddAdText(csvLine, group.getId)
//      createAdGroup(new AdWordsServices, session, group.getId, group.getName)
    }

  }

  def createAdGroup(adWordsServices: AdWordsServices, session: AdWordsSession, adGroupId: Long, mpn:String) {
    val adGroupAdService = adWordsServices.get(session, classOf[AdGroupAdServiceInterface])
    val textAd1 = new TextAd()
    textAd1.setHeadline("Luxury "+mpn)
    textAd1.setDescription1("Visit the Red Planet in style.")
    textAd1.setDescription2("Low-gravity fun for everyone!")
    textAd1.setDisplayUrl("www.example.com")
    textAd1.setFinalUrls(Array("http://www.example.com/1"))

    val textAdGroupAd1 = new AdGroupAd()
    textAdGroupAd1.setAdGroupId(adGroupId)
    textAdGroupAd1.setAd(textAd1)
    textAdGroupAd1.setStatus(AdGroupAdStatus.PAUSED)

    val textAdGroupAdOperation1 = new AdGroupAdOperation()
    textAdGroupAdOperation1.setOperand(textAdGroupAd1)
    textAdGroupAdOperation1.setOperator(Operator.ADD)

    val operations = Array(textAdGroupAdOperation1)
    val result = adGroupAdService.mutate(operations)
    for (adGroupAdResult <- result.getValue) {
      println("Ad with id  \"" + adGroupAdResult.getAd.getId + "\"" +
        " and type \"" +
        adGroupAdResult.getAd.getAdType +
        "\" was added.")
    }
  }


  override def receive: Receive = {
    case AddAdGroup(csvLine: CSVLine) => addCampaign(csvLine, Operator.ADD)
//    case SetAdGroup(adGroupCassandra: AdGroupCassandra) => addCampaign(adGroupCassandra, Operator.SET)
  }
}
