package com.deepricer.adwords.factory

import com.datastax.driver.core.{Cluster, Session}
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

object ClusterFactory {
  lazy val session: Future[Session] = {
    val clusterBuilder = Cluster.builder()
    clusterBuilder.addContactPoint("127.0.0.1")
    clusterBuilder.withPort(9042)
    clusterBuilder.withCredentials("admin", "password")

    val cluster = clusterBuilder.build()
    Future { cluster.connect("keyspace") }
  }
}
