package com.deepricer.adwords

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import com.datastax.driver.core.Cluster
import com.deepricer.adwords.CampaignReadActor.FindAndAdd
import com.deepricer.adwords.domain.Campaigns
import com.deepricer.adwords.google.CampaignActor.AddCampaign

import com.websudos.phantom.dsl._

/**
 * Created by admin on 12/14/15.
 */
object CampaignReadActor {
  case class FindAndAdd(storeId: String, maximum: Int = Int.MaxValue)
}

class CampaignReadActor(cluster: Cluster, campaignActor: ActorRef) extends Actor {
  import context.dispatcher
  import akka.pattern.pipe

  implicit val keySpace = KeySpace(Keyspaces.adwords)
  implicit val session = cluster.connect()

  def findAndAddCampaign(storeId: String) = {

    Campaigns.select.where(_.storeId eqs storeId).fetch map {
      _.foreach(campaign => campaignActor ! AddCampaign(campaign))
    }

  }

  override def receive: Receive = {
    case FindAndAdd(storeId, maximum) => findAndAddCampaign(storeId)
  }
}
