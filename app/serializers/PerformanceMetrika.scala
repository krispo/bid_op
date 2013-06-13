package serializers

import org.joda.time.DateTime

case class PerformanceMetrika(
  val counter_id: Long = 0,
  val campaignID: Option[Long] = None,
  val bannerID: Option[Long] = None,
  val phrase_id: Option[Long] = None,

  val statWithoutGoals: WithoutGoal = WithoutGoal(),
  val statWithGoals: List[WithGoal] = Nil) {

  @transient
  def createPerformanceMetrikaWithGoals(dt: DateTime, pt: domain.PeriodType): List[domain.PerformanceMetrika] = {
    statWithGoals.map { wg =>
      new domain.po.PerformanceMetrika(
        counter_id = wg.counter_id,
        goal_id = wg.goal_id,

        visits = wg.visits,
        visits_all = wg.visits_all,
        goal_reaches = wg.goal_reaches,

        dateTime = dt)
    }
  }
}

case class WithoutGoal(
  val visits: Int = 0,
  val denial: Double = 0d //percentage
  )

case class WithGoal(
  val counter_id: Long = 0,
  val goal_id: Long = 0,

  val visits: Int = 0,
  val visits_all: Int = 0,
  val goal_reaches: Int = 0)