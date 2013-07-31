package dao.squerylorm

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl._
import scala.reflect._

@BeanInfo
case class Phrase(
  val network_phrase_id: String = "", //fk phrase_id in Network's or client's DB
  val metrika_phrase_id: String = "",
  val phrase: String = "",
  val stats: Option[Long] = None) extends domain.Phrase with KeyedEntity[Long] {
  val id: Long = 0

  // Phrase -* BannerPhrase relation
  lazy val bannerPhrasesRel: OneToMany[BannerPhrase] = AppSchema.phraseBannerPhrases.left(this)

  /**
   * default put - save to db
   */
  def put(): Phrase = inTransaction { AppSchema.phrases insert this }

  /**
   * default update - update case in db
   */
  def updateName(): Int = inTransaction {
    update(AppSchema.phrases)(a =>
      where(a.network_phrase_id === this.network_phrase_id and
        a.phrase === "")
        set (a.phrase := this.phrase))
  }

  /**
   * default update - update case in db
   */
  def updateID(): Int = inTransaction {
    update(AppSchema.phrases)(a =>
      where(a.network_phrase_id === this.network_phrase_id and
        a.metrika_phrase_id === "" and
        this.metrika_phrase_id <> "")
        set (a.metrika_phrase_id := this.metrika_phrase_id))
  }

  /**
   * default update - update case in db
   */
  def createOrUpdateStat(i: Long): Int = inTransaction {
    update(AppSchema.phrases)(a =>
      where(a.phrase === this.phrase)
        set (a.stats := Some(i)))
  }

  /**
   * default update - update case in db
   */
  def tryToUpdate(): Unit = {
    if (updateName != 0) println("!Phrase - " + this.network_phrase_id + " Name is updated!")
    if (updateID != 0) println("!Phrase - " + this.network_phrase_id + " ID is updated!")
  }

}

object Phrase {

  /**
   * construct Phrase from domain.Phrase
   */
  def apply(p: domain.Phrase): Phrase =
    Phrase(
      network_phrase_id = p.network_phrase_id,
      metrika_phrase_id = p.metrika_phrase_id,
      phrase = p.phrase,
      stats = p.stats)

  /**
   * select by Campaing and domain.Phrase (basically network_phrase_id)
   * TODO: now it's simply wrong. it has to check BP-B-Campaing association
   */
  def select(p: domain.Phrase): Option[Phrase] = inTransaction {
    AppSchema.phrases.where(a =>
      a.network_phrase_id === p.network_phrase_id).headOption
  }

  /**
   * select by phrase
   * TODO: now it's simply wrong. it has to check BP-B-Campaing association
   */
  def select(phrase: String): Option[Phrase] = inTransaction {
    AppSchema.phrases.where(a =>
      a.phrase === phrase).headOption
  }

  /**
   * select by metrika_phrase_id
   * TODO: now it's simply wrong. it has to check BP-B-Campaing association
   */
  def select1(network_phrase_id: String): Option[Phrase] = inTransaction {
    AppSchema.phrases.where(a =>
      a.network_phrase_id === network_phrase_id).headOption
  }

  /**
   * select by metrika_phrase_id
   * TODO: now it's simply wrong. it has to check BP-B-Campaing association
   */
  def select2(metrika_phrase_id: String): Option[Phrase] = inTransaction {
    AppSchema.phrases.where(a =>
      a.metrika_phrase_id === metrika_phrase_id).headOption
  }

  /**
   * select Phrases for given network_campaign_id
   */
  def select3(user_name: String, net_name: String, network_campaign_id: String): List[Phrase] = inTransaction {
    from(AppSchema.users, AppSchema.networks, AppSchema.campaigns, AppSchema.bannerphrases, AppSchema.phrases)((u, n, c, b, p) =>
      where(
        c.network_campaign_id === network_campaign_id and
          u.name === user_name and n.name === net_name and
          c.user_id === u.id and c.network_id === n.id and
          b.campaign_id === c.id and
          b.phrase_id === p.id)
        select (p)).toList
  }

  def createOrUpdateStats(phrasesStats: Map[String, Int]): Boolean = {
    val bl = phrasesStats flatMap {
      case (ph, i) =>
        Phrase.select(ph) map { phrase =>
          if (phrase.createOrUpdateStat(i) == 0) {
            println("??? Stats is NOT updated for Phrase id=" + phrase.id)
            false
          } else true
        }
    }
    bl.find(_ == true).isDefined
  }
}

