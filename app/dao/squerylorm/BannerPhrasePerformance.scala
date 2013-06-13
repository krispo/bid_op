package dao.squerylorm

import org.squeryl.{ Schema, KeyedEntity }
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl._
import org.joda.time._
import java.sql.Timestamp
import scala.reflect._
import common._

@BeanInfo
case class BannerPhrasePerformance(
  val bannerphrase_id: Long = 0, //fk
  val periodtype_id: Long = 0, //fk
  val cost_search: Double = 0,
  val cost_context: Double = 0,
  val impress_search: Int = 0,
  val impress_context: Int = 0,
  val clicks_search: Int = 0,
  val clicks_context: Int = 0,
  val visits: Int = 0,
  val denial: Double = 0d,
  val date: Timestamp = new Timestamp(0)) extends domain.Performance with KeyedEntity[Long] with History {
  val id: Long = 0

  def dateTime: DateTime = new DateTime(date)
  def periodType: domain.PeriodType = periodTypeRel.head

  // BannerPhrase -* BannerPhrasePerformance relation
  lazy val bannerPhraseRel: ManyToOne[BannerPhrase] = AppSchema.bannerPhrasePerformance.right(this)

  // PeriodType -* BannerPhrasePerformance relation
  lazy val periodTypeRel: ManyToOne[PeriodType] = AppSchema.periodTypeBannerPhrasePerformance.right(this)

  /**
   * put - save to db
   */
  def put(): BannerPhrasePerformance = inTransaction { AppSchema.bannerphraseperformance insert this }

}

object BannerPhrasePerformance {

  /**
   * get BannerPhrasePerformance from DB
   */
  def get_by_id(id: Long): BannerPhrasePerformance = inTransaction {
    AppSchema.bannerphraseperformance.where(a => a.id === id).single
  }

  def apply(t: (dao.squerylorm.BannerPhrase, domain.Performance), isXML: Boolean): BannerPhrasePerformance = {
    t match {
      case (bp, p) =>
        if (!isXML) {
          /**
           * for GetBannersStat method
           */
          //to retrieve detailed statistics on each bannerPhrase from DB, 
          //we need to set historyStartDate and historyEndDate for campaign 
          val c = Campaign.get_by_id(bp.campaign_id)
          c.historyStartDate = p.dateTime.minusMillis(p.dateTime.getMillisOfDay()) //00:00:00
          c.historyEndDate = c.historyStartDate.plusDays(1) //current day
          bp.campaign = Some(c) //each bannerPhrase we assign the campaign to then change historyStartDate and historyEndDate

          val perf = bp.performanceHistory //performance history of the current day

          BannerPhrasePerformance(
            bannerphrase_id = bp.id,
            periodtype_id = p.periodType.id,
            cost_search = p.cost_search - perf.map(_.cost_search).sum,
            cost_context = p.cost_context - perf.map(_.cost_context).sum,
            impress_search = p.impress_search - perf.map(_.impress_search).sum,
            impress_context = p.impress_context - perf.map(_.impress_context).sum,
            clicks_search = p.clicks_search - perf.map(_.clicks_search).sum,
            clicks_context = p.clicks_context - perf.map(_.clicks_context).sum,
            visits = p.visits - perf.map(_.visits).sum,
            denial = p.denial,
            date = p.dateTime)
        } else {
          /**
           * for XmlReport method
           */
          BannerPhrasePerformance(
            bannerphrase_id = bp.id,
            periodtype_id = p.periodType.id,
            cost_search = p.cost_search,
            cost_context = p.cost_context,
            impress_search = p.impress_search,
            impress_context = p.impress_context,
            clicks_search = p.clicks_search,
            clicks_context = p.clicks_context,
            visits = p.visits,
            denial = p.denial,
            date = p.dateTime)
        }
    }

  }

  /**
   * creates BannerPhrasePerformance records
   */
  /*
  def create(report: Map[domain.BannerPhrase, domain.Performance]): Unit = inTransaction{
    // toList
    val records = report.toList map (apply(_))
    AppSchema.bannerphraseperformance.insert(records)
  }
  */

}


