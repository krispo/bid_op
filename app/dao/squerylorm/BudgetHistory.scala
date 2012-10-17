package dao.squerylorm

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl._
import java.util.Date
import org.joda.time._
import scala.reflect._
import common._



@BeanInfo
case class BudgetHistory(
  val campaign_id: Long = 0, //fk
  val date: Date = new Date,
  val budget: Double = 0
)extends KeyedEntity[Long]
{
  val id: Long = 0

  /**
  * default put - save to db
  **/
  def put(): BudgetHistory = inTransaction { AppSchema.budgethistory insert this }

  /** creates domain.TSValue
  **/
  def domainTSValue(): domain.TSValue[Double] = domain.TSValue(
      date = new DateTime(date),
      elem = budget
    )


}

