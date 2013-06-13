package dao.squerylorm

import org.squeryl.{ Schema, KeyedEntity }
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl._
import org.joda.time._
import java.sql.Timestamp
import scala.reflect._
import common._

@BeanInfo
case class BannerPhrasePerformanceMetrika(
  val bannerphrase_id: Long = 0, //fk
  val periodtype_id: Long = 0, //fk

  val counter_id: Long = 0,
  val goal_id: Long = 0,

  val visits: Int = 0, //goal visits
  val visits_all: Int = 0, //all visits
  val goal_reaches: Int = 0,

  val date: Timestamp = new Timestamp(0)) extends domain.PerformanceMetrika with KeyedEntity[Long] with History {

  val id: Long = 0

  def dateTime = date
  // TODO: optimize... it should be no direct access to DB
  def periodType: domain.PeriodType = inTransaction { periodTypeRel.head }

  // BannerPhrase -* BannerPhrasePerformanceMetrika relation
  lazy val bannerPhraseRel: ManyToOne[BannerPhrase] = AppSchema.bannerPhrasePerformanceMetrika.right(this)

  // PeriodType -* CampaignPerformance relation
  lazy val periodTypeRel: ManyToOne[PeriodType] = AppSchema.periodTypeBannerPhrasePerformanceMetrika.right(this)

  /**
   * put - save to db
   */
  def put(): BannerPhrasePerformanceMetrika = inTransaction { AppSchema.bannerphraseperformancemetrika insert this }

}

object BannerPhrasePerformanceMetrika {

  /**
   * get BannerPhrasePerformance from DB
   */
  def get_by_id(id: Long): BannerPhrasePerformanceMetrika = inTransaction {
    AppSchema.bannerphraseperformancemetrika.where(a => a.id === id).single
  }

  def apply(t: (dao.squerylorm.BannerPhrase, List[domain.PerformanceMetrika])): List[BannerPhrasePerformanceMetrika] = {
    t match {
      case (bp, pl) =>
        /**
         * for GetBannersStat (+ metrika) method
         */
        //to retrieve detailed statistics on each bannerPhrase from DB, 
        //we need to set historyStartDate and historyEndDate for campaign 
        val c = Campaign.get_by_id(bp.campaign_id)
        c.historyStartDate = pl.head.dateTime.minusMillis(pl.head.dateTime.getMillisOfDay()) //00:00:00
        c.historyEndDate = c.historyStartDate.plusDays(1) //current day
        bp.campaign = Some(c) //each bannerPhrase we assign the campaign to then change historyStartDate and historyEndDate

        val perfHistory = bp.performanceMetrikaHistory //performance history of the current day

        pl.map { p =>
          val perf = perfHistory.filter(ph => ph.counter_id == p.counter_id & ph.goal_id == p.goal_id)
          BannerPhrasePerformanceMetrika(
            bannerphrase_id = bp.id,
            periodtype_id = p.periodType.id,

            counter_id = p.counter_id,
            goal_id = p.goal_id,

            visits = p.visits - perf.map(_.visits).sum,
            visits_all = p.visits_all - perf.map(_.visits_all).sum,
            goal_reaches = p.goal_reaches - perf.map(_.goal_reaches).sum,

            date = p.dateTime)
        }
    }
  }
}