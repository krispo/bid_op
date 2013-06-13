package domain.po

import org.joda.time._
import scala.reflect._

@BeanInfo
class PerformanceMetrika(
  val id: Long = 0,

  val counter_id: Long,
  val goal_id: Long,

  val visits: Int,
  val visits_all: Int,
  val goal_reaches: Int,

  val dateTime: DateTime) extends domain.PerformanceMetrika {

  var periodType: domain.PeriodType = (new dao.squerylorm.SquerylDao).getPeriodType(dateTime)
}