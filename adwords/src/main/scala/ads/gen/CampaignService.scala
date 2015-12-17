package ads.gen

import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.utils.v201509.SelectorBuilder
import com.google.api.ads.adwords.axis.v201509.cm._
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.api.ads.common.lib.auth.OfflineCredentials.{ Api, Builder }
import com.google.api.client.auth.oauth2.Credential

/**
 * Created by admin on 12/8/15.
 */
object CampaignService {

  def getSession: AdWordsSession = {
    // Construct an OAuth2 credential.
    val oAuth2Credential: Credential = new Builder().forApi(Api.ADWORDS).fromFile.build.generateCredential

    // Construct an AdWordsSession.
    val session: AdWordsSession = new AdWordsSession.Builder().fromFile.withOAuth2Credential(oAuth2Credential).build
    session
  }

  

  def one() = {

    val adWordsServices: AdWordsServices = new AdWordsServices
    // Get the AdGroupBidModifierService.
    val bidModifierService: AdGroupBidModifierServiceInterface =
      adWordsServices.get(getSession, classOf[AdGroupBidModifierServiceInterface])

    // Set values for ad group, criterion IDs and bid modifier.
    val adGroupId = 22606201016L
    // Mobile criterion ID.
    val criterionId = 30001L
    val bidModifier = 1.1

    // Create criterion and bid modifier locally.
    val criterion: Criterion = new Platform()
    criterion.setId(criterionId)

    val modifier: AdGroupBidModifier = new AdGroupBidModifier()
    modifier.setAdGroupId(adGroupId)
    modifier.setCriterion(criterion)
    modifier.setBidModifier(bidModifier)

    // Create operations.
    val operation: AdGroupBidModifierOperation = new AdGroupBidModifierOperation()
    operation.setOperand(modifier)
    operation.setOperator(Operator.ADD)
    val operations: Array[AdGroupBidModifierOperation] =
      Array[AdGroupBidModifierOperation] { operation }

    // Execute mutate request.
    val result: AdGroupBidModifierReturnValue = bidModifierService.mutate(operations)

    result.getValue.foreach { modifier =>
      val value = Option(modifier.getBidModifier).getOrElse("unset")

      println(s"Campaign ID ${modifier.getCampaignId}, AdGroup ID ${modifier.getAdGroupId}, Criterion ID "
        + s"${modifier.getCriterion.getId} was updated with ad group level modifier: $value")
    }

  }
}
