package com.deepricer.adwords.utils

import com.deepricer.adwords.domain.CSVLine

/**
 * Created by admin on 12/20/15.
 */
object CSVUtil {

  def csvTo(line: String) = {
    val Array(mpn, brand, bid, suggestion, lbound, ubound, url, adHeadline, ad1Line, adLine2, adDisplayUrl, adFinalUrl, campaignId, campaignBudget) = line.split(",").map(_.trim)

    CSVLine(mpn, brand, bid, suggestion, lbound, ubound, url, adHeadline, ad1Line, adLine2, adDisplayUrl, adFinalUrl, campaignId, campaignBudget)
  }
}
