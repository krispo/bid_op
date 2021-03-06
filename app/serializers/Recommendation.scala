package serializers

import org.joda.time._

import play.api.libs.json._

object Recommendation {
  /** Constructor from domain.Recommendation */
  def apply(c: domain.Campaign, rec: domain.Recommendation): JsValue = { //List[PhrasePriceInfo] = {
    val campID = filterDigits(c.network_campaign_id)

    val ppil = (rec.bannerPhraseBid map ((x: (domain.BannerPhrase, Double)) =>
      PhrasePriceInfo(
        PhraseID = filterDigits(x._1.phrase.get.network_phrase_id),
        BannerID = filterDigits(x._1.banner.get.network_banner_id),
        CampaignID = campID,
        Price = x._2))).toList

    implicit lazy val phrasePriceInfo = Json.writes[PhrasePriceInfo]
    Json.toJson[List[PhrasePriceInfo]](ppil)
  }

  def filterDigits(s: String): Int = s.filter(_.isDigit).toInt

}

case class PhrasePriceInfo(
  val PhraseID: Int,
  val BannerID: Int,
  val CampaignID: Int,
  val Price: Double) {}
