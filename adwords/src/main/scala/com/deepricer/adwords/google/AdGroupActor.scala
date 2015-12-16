package com.deepricer.adwords.google

import akka.actor.Actor
import com.deepricer.adwords.domain.AdGroupCassandra
import com.deepricer.adwords.google.AdGroupActor.{SetAdGroup, AddAdGroup}
import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201509.cm._
import com.google.api.ads.adwords.lib.client.AdWordsSession

/**
 * Created by admin on 12/15/15.
 */

object AdGroupActor {
  case class AddAdGroup(adGroupCassandra: AdGroupCassandra)
  case class SetAdGroup(adGroupCassandra: AdGroupCassandra)
}

class AdGroupActor(session: AdWordsSession) extends Actor {
  def addCampaign(adGroupCassandra: AdGroupCassandra, operator: Operator) = {
    val adWordsServices: AdWordsServices = new AdWordsServices
    val adGroupService:AdGroupServiceInterface =
      adWordsServices.get(session, classOf[AdGroupServiceInterface])

    // Create ad group.
    val adGroup:AdGroup = new AdGroup
    adGroup.setName(adGroupCassandra.name)
    adGroup.setStatus(AdGroupStatus.ENABLED)
    adGroup.setCampaignId(adGroupCassandra.campaignId)

    val operation: AdGroupOperation = new AdGroupOperation
    operation.setOperand(adGroup)
    operation.setOperator(operator)

    // Add ad groups.
    val result: AdGroupReturnValue = adGroupService.mutate(Array[AdGroupOperation](operation))

  }

  override def receive: Receive = {
    case AddAdGroup(adGroupCassandra: AdGroupCassandra) => addCampaign(adGroupCassandra, Operator.ADD)
    case SetAdGroup(adGroupCassandra: AdGroupCassandra) => addCampaign(adGroupCassandra, Operator.SET)
  }
}
