package com.deepricer.adwords.google

import akka.actor.{ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.deepricer.adwords.domain.{CSVLine, AdTextCassandra}
import com.deepricer.adwords.google.AdTextActor.AddAdText
import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201509.cm._
import com.google.api.ads.adwords.lib.client.AdWordsSession

/**
 * Created by admin on 12/16/15.
 */

object AdTextActor {
  case class AddAdText(csvLine: CSVLine, groupId: Long)
//  case class SetAdText(adTextCassandra: AdTextCassandra)
}

class AdTextActor(session: AdWordsSession) extends Actor with ActorLogging {

  def adText(csvLine: CSVLine, groupId: Long, operator: Operator) = {
    val adWordsServices: AdWordsServices = new AdWordsServices
    val adGroupAdService: AdGroupAdServiceInterface =
      adWordsServices.get(session, classOf[AdGroupAdServiceInterface])

    // Create text ads.
    val textAd = new TextAd
    textAd.setHeadline(csvLine.adHeadline)
    textAd.setDescription1("")
    textAd.setDescription2("")
    textAd.setDisplayUrl(csvLine.adDisplayUrl)
    textAd.setFinalUrls(Array[String] {csvLine.adFinalUrl})

    // Create ad group ad.
    val textAdGroupAd = new AdGroupAd
    textAdGroupAd.setAdGroupId(groupId)
    textAdGroupAd.setAd(textAd)

    // You can optionally provide these field(s).
    textAdGroupAd.setStatus(AdGroupAdStatus.PAUSED)

    val textAdGroupAdOperation = new AdGroupAdOperation
    textAdGroupAdOperation.setOperand(textAdGroupAd)
    textAdGroupAdOperation.setOperator(operator)

    // Add ads.
    val result = adGroupAdService.mutate(Array[AdGroupAdOperation](textAdGroupAdOperation))
  }

  override def receive: Receive = {
    case AddAdText(csvLine: CSVLine, groupId: Long) => adText(csvLine, groupId, Operator.ADD)
//    case SetAdText(adTextCassandra: AdTextCassandra) => adText(adTextCassandra, Operator.SET)
  }
}
