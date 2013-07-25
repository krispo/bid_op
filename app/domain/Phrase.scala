package domain

trait Phrase {
  def id: Long
  def network_phrase_id: String
  def metrika_phrase_id: String
  def phrase: String
  def stats: Option[Long]
}

