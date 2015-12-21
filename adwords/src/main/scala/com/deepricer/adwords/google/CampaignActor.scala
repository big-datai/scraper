package com.deepricer.adwords.google

import akka.actor.{ActorLogging, Actor}
import com.deepricer.adwords.domain.CampaignCassandra
import com.deepricer.adwords.google.CampaignActor.{SetCampaign, AddCampaign}
import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201509.cm._
import com.google.api.ads.adwords.lib.client.AdWordsSession

/**
 * Created by admin on 12/14/15.
 *
 */

object CampaignActor {
  case class AddCampaign(campaignCassandra: CampaignCassandra)
  case class SetCampaign(campaignCassandra: CampaignCassandra)
}

class CampaignActor(session: AdWordsSession) extends Actor with ActorLogging {

  def addCampaign(campaignCassandra: CampaignCassandra, operator: Operator) = {
    val adWordsServices: AdWordsServices = new AdWordsServices

    // Get the BudgetService.
    val budgetService: BudgetServiceInterface = adWordsServices.get(session, classOf[BudgetServiceInterface])
    val sharedBudget: Budget = new Budget

    sharedBudget.setName(s"Budget for ${campaignCassandra.name}")

    val budgetOperation: BudgetOperation = new BudgetOperation
    budgetOperation.setOperator(operator)

    campaignCassandra.budgetAmount map { amount =>
      val budgetAmount: Money = new Money
      budgetAmount.setMicroAmount(amount.longValue())
      sharedBudget.setAmount(budgetAmount)
      sharedBudget.setDeliveryMethod(campaignCassandra.budgetDeliveryMethod.map(BudgetBudgetDeliveryMethod.fromString).getOrElse(BudgetBudgetDeliveryMethod.STANDARD))
      sharedBudget.setPeriod(BudgetBudgetPeriod.DAILY)

      budgetOperation.setOperand(sharedBudget)
    }

    // Add the budget
    val budgetId: Long = budgetService.mutate(Array[BudgetOperation](budgetOperation)).getValue(0).getBudgetId

    // Get the CampaignService.
    val campaignService: CampaignServiceInterface = adWordsServices.get(session, classOf[CampaignServiceInterface])

    // Create campaign.
    val campaign: Campaign = new Campaign
    campaign.setName(campaignCassandra.name)
    campaign.setStatus(campaignCassandra.status.map(CampaignStatus.fromString).getOrElse(CampaignStatus.PAUSED))
    val biddingStrategyConfiguration: BiddingStrategyConfiguration = new BiddingStrategyConfiguration
    biddingStrategyConfiguration.setBiddingStrategyType(campaignCassandra.strategyType.map(BiddingStrategyType.fromString).getOrElse(BiddingStrategyType.MANUAL_CPC))

    // You can optionally provide a bidding scheme in place of the type.
    val cpcBiddingScheme: ManualCpcBiddingScheme = new ManualCpcBiddingScheme
    cpcBiddingScheme.setEnhancedCpcEnabled(false)
    biddingStrategyConfiguration.setBiddingScheme(cpcBiddingScheme)

    campaign.setBiddingStrategyConfiguration(biddingStrategyConfiguration)

    // Only the budgetId should be sent, all other fields will be ignored by CampaignService.
    val budget: Budget = new Budget
    budget.setBudgetId(budgetId)
    campaign.setBudget(budget)

    val operation: CampaignOperation = new CampaignOperation
    operation.setOperand(campaign)
    operation.setOperator(operator)

    val result: CampaignReturnValue = campaignService.mutate(Array[CampaignOperation](operation))
//    campaign.setAdvertisingChannelType(AdvertisingChannelType.SEARCH)
  }

  override def receive: Receive = {
    case AddCampaign(campaignCassandra: CampaignCassandra) => addCampaign(campaignCassandra, Operator.ADD)
    case SetCampaign(campaignCassandra: CampaignCassandra) => addCampaign(campaignCassandra, Operator.SET)
  }
}
