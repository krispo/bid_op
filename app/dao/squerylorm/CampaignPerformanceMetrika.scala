package dao.squerylorm

import org.squeryl.{ Schema, KeyedEntity }
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl._
import org.joda.time._
import java.sql.Timestamp
import scala.reflect._
import common._

@BeanInfo
case class CampaignPerformanceMetrika(
  val campaign_id: Long = 0, //fk
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

  // PeriodType -* CampaignPerformance relation
  lazy val periodTypeRel: ManyToOne[PeriodType] = AppSchema.periodTypeCampaignPerformanceMetrika.right(this)

  /**
   * put - save to db
   */
  def put(): CampaignPerformanceMetrika = inTransaction { AppSchema.campaignperformancemetrika insert this }
}

object CampaignPerformanceMetrika {

  /**
   * get CampaignPerformance from DB
   */
  def get_by_id(id: Long): CampaignPerformanceMetrika = inTransaction {
    AppSchema.campaignperformancemetrika.where(a => a.id === id).single
  }

  def apply(c: domain.Campaign, pml: List[domain.PerformanceMetrika]): List[CampaignPerformanceMetrika] = {
    //p.dateTime must be between 00:00:00 and 23:59:59
    //current day performance List
    val camp = Campaign.get_by_id(c.id)
    camp.historyStartDate = pml.head.dateTime.minusMillis(pml.head.dateTime.getMillisOfDay()) //00:00:00
    camp.historyEndDate = camp.historyStartDate.plusDays(1) //current day

    val perfHistory = camp.performanceMetrikaHistory //only today performance

    pml.map { pm =>
      val perf = perfHistory.filter(ph => ph.counter_id == pm.counter_id & ph.goal_id == pm.goal_id)
      CampaignPerformanceMetrika(
        campaign_id = c.id,
        periodtype_id = pm.periodType.id,

        counter_id = pm.counter_id,
        goal_id = pm.goal_id,

        visits = pm.visits - perf.map(_.visits).sum,
        visits_all = pm.visits_all - perf.map(_.visits_all).sum,
        goal_reaches = pm.goal_reaches - perf.map(_.goal_reaches).sum,

        date = pm.dateTime)
    }

  }
}
