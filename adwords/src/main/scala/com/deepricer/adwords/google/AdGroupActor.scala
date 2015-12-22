package com.deepricer.adwords.google

import akka.actor.{ ActorRef, ActorLogging, Actor }
import com.deepricer.adwords.domain.{ CSVLine, AdGroupCassandra }
import com.deepricer.adwords.google.AdGroupActor.AddAdGroup
import com.deepricer.adwords.google.AdTextActor.AddAdText
import com.deepricer.adwords.google.AdKeywordActor._
import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201509.cm._
import com.google.api.ads.adwords.lib.client.AdWordsSession
import akka.actor.Props
import com.deepricer.adwords.Main
import java.net.URLEncoder
import com.google.api.client.util.Charsets

/**
 * Created by admin on 12/15/15.
 */

object AdGroupActor {
  case class AddAdGroup(csvLine: CSVLine)
  //  case class SetAdGroup(adGroupCassandra: AdGroupCassandra)
}

class AdGroupActor(session: AdWordsSession, adTextActor: ActorRef, key: ActorRef) extends Actor with ActorLogging {

  def addGroup(csvLine: CSVLine, operator: Operator) = {
    val adWordsServices: AdWordsServices = new AdWordsServices
    val adGroupService: AdGroupServiceInterface =
      adWordsServices.get(session, classOf[AdGroupServiceInterface])

    // Create ad group.
    val adGroup: AdGroup = new AdGroup
    adGroup.setName(csvLine.mpn)
    adGroup.setStatus(AdGroupStatus.ENABLED)
    adGroup.setCampaignId(csvLine.campaignId.toLong)
    val operation: AdGroupOperation = new AdGroupOperation
    operation.setOperand(adGroup)
    operation.setOperator(operator)

    // Add ad groups.
    val result: AdGroupReturnValue = adGroupService.mutate(Array[AdGroupOperation](operation))
    var i: Long = 1L
    result.getValue foreach { group =>
      adTextActor ! AddAdText(csvLine, group.getId)
      key ! AddAdKeyword(csvLine, group.getId)
    }

  }

  override def receive: Receive = {
    case AddAdGroup(csvLine: CSVLine) => addGroup(csvLine, Operator.ADD)
    //    case SetAdGroup(adGroupCassandra: AdGroupCassandra) => addCampaign(adGroupCassandra, Operator.SET))))))
  }
}
