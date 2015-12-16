package com.deepricer.adwords.google

import akka.actor.Actor
import akka.actor.Actor.Receive
import com.deepricer.adwords.domain.AdTextCassandra
import com.deepricer.adwords.google.AdTextActor.{SetAdText, AddAdText}
import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201509.cm._
import com.google.api.ads.adwords.lib.client.AdWordsSession

/**
 * Created by admin on 12/16/15.
 */

object AdTextActor {
  case class AddAdText(adTextCassandra: AdTextCassandra)
  case class SetAdText(adTextCassandra: AdTextCassandra)
}

class AdTextActor(session: AdWordsSession) extends Actor {

  def adText(adTextCassandra: AdTextCassandra, operator: Operator) = {
    val adWordsServices: AdWordsServices = new AdWordsServices
    val adGroupAdService: AdGroupAdServiceInterface =
      adWordsServices.get(session, classOf[AdGroupAdServiceInterface])

    // Create text ads.
    val textAd = new TextAd
    textAd.setHeadline(adTextCassandra.headline)
    textAd.setDescription1(adTextCassandra.desc1.getOrElse(""))
    textAd.setDescription2(adTextCassandra.desc2.getOrElse(""))
    textAd.setDisplayUrl(adTextCassandra.displayUrl)
    textAd.setFinalUrls(Array[String] {adTextCassandra.finalUrl})

    // Create ad group ad.
    val textAdGroupAd = new AdGroupAd
    textAdGroupAd.setAdGroupId(adTextCassandra.groupId)
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
    case AddAdText(adTextCassandra: AdTextCassandra) => adText(adTextCassandra, Operator.ADD)
    case SetAdText(adTextCassandra: AdTextCassandra) => adText(adTextCassandra, Operator.SET)
  }
}
