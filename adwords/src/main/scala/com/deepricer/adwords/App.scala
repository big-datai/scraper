package com.deepricer.adwords

import java.lang.Boolean.getBoolean

import akka.actor.{Props, ActorSystem}
import com.deepricer.adwords.CampaignReadActor.FindAndAdd
import com.deepricer.adwords.google.AdGroupActor.AddAdGroup
import com.deepricer.adwords.google.{AdTextActor, AdGroupActor, CampaignActor}
import com.deepricer.adwords.config.{AdWordsConfig, ConfigCassandraCluster}
import com.deepricer.adwords.utils.CSVUtil
import com.typesafe.config.ConfigFactory

import scala.io.Source

trait Bootable {
  /**
   * Callback run on microkernel startup.
   * Create initial actors and messages here.
   */
  def startup(): Unit

  /**
   * Callback run on microkernel shutdown.
   * Shutdown actor systems here.
   */
  def shutdown(): Unit
}

object Main extends App with ConfigCassandraCluster with AdWordsConfig {
  import akka.actor.ActorDSL._
  implicit lazy val system = ActorSystem("ClusterSystem", ConfigFactory.load())
//  val findAndAdd = system.actorOf(Props(new CampaignActor(OAuth2Credential.session)))
//  val scan = system.actorOf(Props(new CampaignReadActor(cluster, findAndAdd)))
  val adText = system.actorOf(Props(new AdTextActor(OAuth2Credential.session)))
  val adGroup = system.actorOf(Props(new AdGroupActor(OAuth2Credential.session, adText)))

  // we don't want to bother with the ``ask`` pattern, so
  // we set up sender that only prints out the responses to
  // be implicitly available for ``tell`` to pick up.
  implicit val _ = actor(new Act {
    become {
      case x => println(">>> " + x)
    }
  })
  private val quiet = getBoolean("akka.kernel.quiet")

  private def log(s: String) = if (!quiet) println(s)

  def startup(ports: Seq[String], files: Array[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
        withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      // Create an actor that handles cluster domain events

//      val findAndAdd = system.actorOf(Props(new CampaignActor(OAuth2Credential.session)))
//      val scan = system.actorOf(Props(new CampaignReadActor(cluster, findAndAdd)))
//
//      scan ! FindAndAdd("www.bb.com")

      val adText = system.actorOf(Props(new AdTextActor(OAuth2Credential.session)))
      val adGroup = system.actorOf(Props(new AdGroupActor(OAuth2Credential.session, adText)))


      files.toList.map { file =>
        val bufferedSource = Source.fromFile(file)
        bufferedSource.getLines().next()
        for (line <- bufferedSource.getLines()) {
          val csvLine = CSVUtil.csvTo(line)
          adGroup ! AddAdGroup(csvLine)
        }
        bufferedSource.close()
      }
    }
  }

  override def main(args: Array[String]) = {

    log(banner)
    log("Starting Akka...")
    log("Running Akka " + ActorSystem.Version)
    startup(Seq("2551"), args)
//    val bufferedSource = Source.fromFile("/Users/admin/Downloads/firstClient.csv")
//    for (line <- bufferedSource.getLines()) {
//      val csvLine = CSVUtil.csvTo(line)
//    }
//    bufferedSource.close()

    //MPN	brand	bid	Suggestion	lbound	ubound	url	ad headline	Ad 1 line	Ad line 2	Ad display url	Ad final url	Campaign ID	Campaign budget
    log("Successfully started Akka")

    system.terminate()
  }


  private def banner = """
==============================================================================

                                                   ZZ:
                                                  ZZZZ
                                                 ZZZZZZ
                                                ZZZ' ZZZ
                                       ~7      7ZZ'   ZZZ
                                      :ZZZ:   IZZ'     ZZZ
                                     ,OZZZZ.~ZZ?        ZZZ
                                    ZZZZ' 'ZZZ$          ZZZ
                           .       $ZZZ   ~ZZ$            ZZZ
                         .=Z?.   .ZZZO   ~ZZ7              OZZ
                        .ZZZZ7..:ZZZ~   7ZZZ                ZZZ~
                      .$ZZZ$Z+.ZZZZ    ZZZ:                  ZZZ$
                   .,ZZZZ?'  =ZZO=   .OZZ                     'ZZZ
                 .$ZZZZ+   .ZZZZ    IZZZ                        ZZZ$
               .ZZZZZ'   .ZZZZ'   .ZZZ$                          ?ZZZ
            .ZZZZZZ'   .OZZZ?    ?ZZZ                             'ZZZ$
        .?ZZZZZZ'    .ZZZZ?    .ZZZ?                                'ZZZO
    .+ZZZZZZ?'    .7ZZZZ'    .ZZZZ                                    :ZZZZ
 .ZZZZZZ$'     .?ZZZZZ'   .~ZZZZ                                        'ZZZZ.


                      NNNNN              $NNNN+
                      NNNNN              $NNNN+
                      NNNNN              $NNNN+
                      NNNNN              $NNNN+
                      NNNNN              $NNNN+
    =NNNNNNNNND$      NNNNN     DDDDDD:  $NNNN+     DDDDDN     NDDNNNNNNNN,
   NNNNNNNNNNNNND     NNNNN    DNNNNN    $NNNN+   8NNNNN=    :NNNNNNNNNNNNNN
  NNNNN$    DNNNNN    NNNNN  $NNNNN~     $NNNN+  NNNNNN      NNNNN,   :NNNNN+
   ?DN~      NNNNN    NNNNN MNNNNN       $NNNN+:NNNNN7        $ND      =NNNNN
            DNNNNN    NNNNNDNNNN$        $NNNNDNNNNN                  :DNNNNN
     ZNDNNNNNNNNND    NNNNNNNNNND,       $NNNNNNNNNNN           DNDNNNNNNNNNN
   NNNNNNNDDINNNNN    NNNNNNNNNNND       $NNNNNNNNNNND       ONNNNNNND8+NNNNN
 :NNNND      NNNNN    NNNNNN  DNNNN,     $NNNNNO 7NNNND     NNNNNO     :NNNNN
 DNNNN       NNNNN    NNNNN    DNNNN     $NNNN+   8NNNNN    NNNNN      $NNNNN
 DNNNNO     NNNNNN    NNNNN     NNNNN    $NNNN+    NNNNN$   NNNND,    ,NNNNND
  NNNNNNDDNNNNNNNN    NNNNN     =NNNNN   $NNNN+     DNNNN?  DNNNNNNDNNNNNNNND
   NNNNNNNNN  NNNN$   NNNNN      8NNNND  $NNNN+      NNNNN=  ,DNNNNNNND NNNNN$

==============================================================================
                       """
}