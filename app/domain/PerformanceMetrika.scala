package domain

import org.joda.time._

trait PerformanceMetrika {

  def id: Long

  def counter_id: Long
  def goal_id: Long

  def visits: Int
  def visits_all: Int
  def goal_reaches: Int

  def periodType: PeriodType
  def dateTime: DateTime // DateTime of Performance snap-shot

}