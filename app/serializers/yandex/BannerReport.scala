package serializers.yandex

import org.joda.time._

import play.api.libs.json._

import common._

import domain._

/** Yandex GetBanners (Live) API call output data structure */
object BannerReport {

  /** creates Map (see return) of domain.Types */
  def getDomainReport(data: List[BannerInfo]): Map[BannerPhrase, (ActualBidHistoryElem, NetAdvisedBids)] = {
    val res = for {
      bInfo <- data
      phInfo <- bInfo.Phrases
    } yield {
      (
        serializers.BannerPhrase(
          banner = Some(domain.po.Banner(network_banner_id = phInfo.BannerID.toString, geo = bInfo.Geo)),
          phrase = Some(domain.po.Phrase(network_phrase_id = phInfo.PhraseID.toString, phrase = phInfo.Phrase))),
          (new ActualBidHistoryElem { val dateTime = new DateTime; val elem = phInfo.Price },
            po.NetAdvisedBids(
              a = phInfo.Min,
              b = phInfo.Max,
              c = phInfo.PremiumMin,
              d = phInfo.PremiumMax,
              e = phInfo.CurrentOnSearch.getOrElse(0.0),
              f = 0,
              dateTime = new DateTime)))
    }
    res toMap
  }
  def getDomainReportJS(data: List[JsValue]): Map[BannerPhrase, (ActualBidHistoryElem, NetAdvisedBids)] = {
    val res = for {
      bInfo <- data
      phInfo <- (bInfo \ "Phrases").as[List[JsValue]]
    } yield {
      println("<<<" + phInfo + ">>>")
      (
        serializers.BannerPhrase(
          banner = Some(domain.po.Banner(
            network_banner_id = (phInfo \ "BannerID").as[Int].toString,
            geo = (bInfo \ "Geo").as[String])),
          phrase = Some(domain.po.Phrase(
            network_phrase_id = (phInfo \ "PhraseID").as[Int].toString,
            phrase = (phInfo \ "Phrase").as[String]))),
          (new ActualBidHistoryElem {
            val dateTime = new DateTime;
            val elem = (phInfo \ "Price").as[Double]
          },
            po.NetAdvisedBids(
              a = (phInfo \ "Min").as[Double],
              b = (phInfo \ "Max").as[Double],
              c = (phInfo \ "PremiumMin").as[Double],
              d = (phInfo \ "PremiumMax").as[Double],
              e = (phInfo \ "CurrentOnSearch").asOpt[Double].getOrElse(0.0),
              f = 0,
              dateTime = new DateTime)))
    }
    res toMap
  }

}

/*object BannerReport extends Function1[List[BannerInfo], BannerReport] {
  def _apply(jsValue: JsValue): BannerReport = {
    Json.fromJson[BannerReport](jsValue)(json_api.Reads.bannerReport).get
    /*import common.Reads.bannerReport
    jsValue.validate[BannerReport].map { br => br }.
      recoverTotal(er => { println(er); BannerReport(data = List()) })*/
  }
}*/

case class BannerInfo(
  val BannerID: Long,
  val Text: String,
  /**String containing a comma-separated list of IDs for the ad display regions. An ID of 0 or an empty string indicate displays in all regions.*/
  val Geo: String,
  val Phrases: List[BannerPhraseInfo]) {
}

case class BannerPhraseInfo(
  val BannerID: Long,
  val PhraseID: Long,
  val CampaignID: Long,
  val Phrase: String,
  val Min: Double,
  val Max: Double,
  val PremiumMin: Double,
  val PremiumMax: Double,
  val ContextPrice: Option[Double] = Some(0.0), //CPC on sites in the Yandex Advertising Network
  val AutoBroker: String, // Yes / No
  val Price: Double, // Maximum CPC on Yandex search set for the phrase.
  val CurrentOnSearch: Option[Double] = Some(0.0) //The current CPC set by Autobroker
  ) {}

