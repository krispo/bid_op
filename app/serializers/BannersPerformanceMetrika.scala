package serializers

import org.joda.time._

object BannersPerformanceMetrika {

  // Report date format yyyy-MM-dd
  val fmt_date = format.ISODateTimeFormat.date()

  /**
   * map bp->List[pm] --- List[pm] for all goals
   */
  def createBannerPhrasePerformanceMetrikaReport(pml: List[PerformanceMetrika], c: dao.squerylorm.Campaign, cur_date: DateTime): Map[domain.BannerPhrase, List[domain.PerformanceMetrika]] = {
    // check dates are present and valid
    // TODO: in case the period (endDate - startDate) > 1 day make an appropriate adjustments - i.e.
    // several Performances

    val endDate: DateTime = cur_date

    // process rows
    val report = for (pm <- pml) yield {

      val bannerID = pm.bannerID.getOrElse(0)
      val phrase_id = pm.phrase_id.getOrElse(0l) //phrase ID in Metrika
      val phraseID = dao.squerylorm.Phrase //phrase ID in Direct network
        .select2(metrika_phrase_id = phrase_id.toString)
        .map(_.network_phrase_id)
        .getOrElse("")

      (serializers.BannerPhrase(
        Some(new domain.po.Banner(network_banner_id = bannerID.toString)),
        Some(new domain.po.Phrase(network_phrase_id = phraseID.toString))),
        pm.statWithGoals.map { s =>
          new domain.po.PerformanceMetrika(
            counter_id = s.counter_id,
            goal_id = s.goal_id,

            visits = s.visits,
            visits_all = s.visits_all,
            goal_reaches = s.goal_reaches,

            dateTime = cur_date)
        })
    }

    report.toMap
  }

}