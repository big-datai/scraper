package com.deepricer.adwords

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import com.datastax.driver.core.Cluster
import com.deepricer.adwords.CampaignReadActor.FindAndAdd
import com.deepricer.adwords.domain.CampaignCassandra
import com.deepricer.adwords.google.CampaignActor.AddCampaign

import com.websudos.phantom.dsl._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by admin on 12/14/15.
 */
object CampaignReadActor {
  case class FindAndAdd(storeId: String, maximum: Int = Int.MaxValue)
}

class CampaignReadActor(cluster: Cluster, campaignActor: ActorRef) extends Actor {

//  implicit val session = cluster.connect(Keyspaces.adwords)

  def findAndAddCampaign(storeId: String) = {

//    println(session.execute("select * from campaigns").all().iterator().next().toString)

//    Campaigns.select.where(_.storeId eqs storeId).fetch

    CampaignCassandra.c(storeId) map (cas => cas.foreach(ca => println(ca)))

    CampaignCassandra.c(storeId) map {
      _.foreach(campaign => campaignActor ! AddCampaign(campaign))
    }

  }

  override def receive: Receive = {
    case FindAndAdd(storeId, maximum) => findAndAddCampaign(storeId)
  }
}
