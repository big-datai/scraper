package com.deepricer

import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api
import com.google.api.client.auth.oauth2.Credential

/**
 * Created by admin on 12/14/15.
 */
package object adwords {

  private[adwords] object Keyspaces {
    val adwords = "adwords"
  }

  private[adwords] object ColumnFamilies {
    val campaigns = "campaigns"
    val adGroups = "adgroups"
    val ads = "ads"
  }

}
