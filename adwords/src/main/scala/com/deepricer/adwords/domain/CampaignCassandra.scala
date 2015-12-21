package com.deepricer.adwords.domain

import com.datastax.driver.core.Row
import com.deepricer.adwords.config.AdwordsConnector
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._

/**
 * Created by admin on 12/17/15.
 */
case class CampaignCassandra(
                              storeId: String,
                              name: String,
                              id: Long,
                              url: String,
                              status: Option[String],
                              budgetId: Option[Long],
                              strategyType: Option[String],
                              budgetAmount: Option[BigDecimal],
                              budgetPeriod: Option[String],
                              budgetDeliveryMethod: Option[String]
                              )

sealed class Campaigns extends CassandraTable[Campaigns, CampaignCassandra] {

  object storeId extends StringColumn(this) with PrimaryKey[String] with ClusteringOrder[String] {
    override def name: String = "store_id"
  }

  object name extends StringColumn(this) with PartitionKey[String]

  object id extends LongColumn(this) with PartitionKey[Long]

  object url extends StringColumn(this)

  object status extends OptionalStringColumn(this)

  object budgetId extends OptionalLongColumn(this) {
    override def name: String = "budget_id"
  }

  object strategyType extends OptionalStringColumn(this) {
    override def name: String = "strategy_type"
  }

  object budgetAmount extends OptionalBigDecimalColumn(this) {
    override def name: String = "budget_amount"
  }

  object budgetPeriod extends OptionalStringColumn(this) {
    override def name: String = "budget_period"
  }

  object budgetDeliveryMethod extends OptionalStringColumn(this) {
    override def name: String = "budget_delivery_method"
  }

  override def fromRow(row: Row): CampaignCassandra = {
    CampaignCassandra(
      storeId(row),
      name(row),
      id(row),
      url(row),
      status(row),
      budgetId(row),
      strategyType(row),
      budgetAmount(row),
      budgetPeriod(row),
      budgetDeliveryMethod(row)
    )
  }
}

object CampaignCassandra extends Campaigns with AdwordsConnector {
  def c(storeId: String) = {
    select.where(_.storeId eqs storeId).fetch
  }
}

