package serializers

import play.api.libs.json._
import dao.squerylorm._

object PhrasesStats {

  def getStats(js: JsValue): Map[String, Int] = {
    val phStatList = (js \ "data").as[List[JsValue]]
    val res = phStatList flatMap { phStat =>
      val searchedWith = (phStat \ "SearchedWith").as[List[JsValue]]
      searchedWith
        .filter(_ \ "Phrase" == phStat \ "Phrase")
        .headOption
        .map(sw => (sw \ "Phrase").as[String] -> (sw \ "Shows").as[Int])
    }
    println("<<<" + res + ">>>")
    res.toMap
  }
}