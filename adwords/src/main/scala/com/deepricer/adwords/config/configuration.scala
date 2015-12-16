package com.deepricer.adwords.config

import akka.actor.ActorSystem
import com.datastax.driver.core.{ProtocolOptions, Cluster}
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api
import com.google.api.client.auth.oauth2.Credential

/**
 * Created by admin on 12/14/15.
 */
trait CassandraCluster {
  def cluster: Cluster
}

trait ConfigCassandraCluster extends CassandraCluster {
  def system: ActorSystem

  private def config = system.settings.config

  import scala.collection.JavaConversions._
  private val cassandraConfig = config.getConfig("akka-cassandra.main.db.cassandra")
  private val port = cassandraConfig.getInt("port")
  private val hosts = cassandraConfig.getStringList("hosts").toList

  lazy val cluster: Cluster =
    Cluster.builder()
      .addContactPoints(hosts: _*)
      .withCompression(ProtocolOptions.Compression.SNAPPY)
      .withPort(port)
      .build()
}

trait AdWordsConfig {
  private[adwords] object OAuth2Credential {
    val oAuth2Credential: Credential = new OfflineCredentials.Builder().forApi(Api.ADWORDS).fromFile.build.generateCredential
    lazy val session: AdWordsSession = new AdWordsSession.Builder().fromFile.withOAuth2Credential(oAuth2Credential).build
  }
}