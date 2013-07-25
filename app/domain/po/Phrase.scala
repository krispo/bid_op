package domain.po

import scala.reflect._

@BeanInfo
case class Phrase(
  val id: Long = 0,
  val network_phrase_id: String,
  val metrika_phrase_id: String = "",
  val phrase: String = "",
  val stats: Option[Long] = None) extends domain.Phrase {}

