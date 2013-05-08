package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import domain._
import dao.squerylorm._
import play.api.libs.json._

import play.api.libs.{ Comet }
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller with Secured {

  def index = Action {
    Ok(views.html.index())
  }

  /**
   * A String Enumerator producing a formatted Time message every 100 millis.
   * A callback enumerator is pure an can be applied on several Iteratee.
   */
  lazy val clock: Enumerator[String] = {
    import java.util._
    import java.text._

    val dateFormat = new SimpleDateFormat("HH mm ss")

    Enumerator.generateM {
      Promise.timeout(Some(dateFormat.format(new Date)), 100 milliseconds)
    }
  }

  def liveClock = Action {
    Ok.stream(clock &> Comet(callback = "parent.clockChanged"))
  }

  /**
   * Working with CHARTS
   *
   * lazy val charts: Enumerator[String] = {
   * Enumerator.generateM {
   * Promise.timeout(Some("Comet is alive!"), 2 seconds)
   * }
   * }
   * import scala.concurrent.Future
   * def liveCharts = Action {
   * Ok.stream(charts &> Comet(callback = "console.log"))
   * }
   */

  /**
   * clear database function
   * is used with GET request from web client
   */
  def clearDB(user_name: String) = IsAuth(
    user_name,
    (dao, user) => implicit request => if (dao.clearDB) Ok else BadRequest)

  /**
   * sample
   */
  def sample(id: String, c: String) = Action {
    Ok("you tried headers: " + id + " camp: " + c).withHeaders(
      CONTENT_TYPE -> "text/html",
      CACHE_CONTROL -> "max-age=3600",
      ETAG -> "xx")
  }

  /**
   * Charts
   */
  import scala.concurrent.Future

  /* */
  def charts = Action { implicit request =>
    common.Helpers.cform.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.charts(formWithErrors)),
      form => {
        val f =
          if (form._1.map(_ == "--- Choose a User ---").getOrElse(true))
            common.Helpers.cform
          else if (form._2.map(_ == "--- Choose a Campaign ---").getOrElse(true))
            common.Helpers.cform.fill(form._1, None, None)
          else if (form._3.map(_ == "--- Choose a BannerPhrase ---").getOrElse(true))
            common.Helpers.cform.fill(form._1, form._2, None)
          else
            common.Helpers.cform.fill(form)

        Ok(views.html.charts(f))
      })
  }

  def drawCharts(ctype: String, u: String, n: String, cID: String, bpID: String) = Action {
    Async {
      Future {
        val dao = new SquerylDao()
        dao.getCampaign(u, n, cID) match {
          case None => NotFound("CAMPAIGN is NOT FOUND...")
          case Some(c) => {
            //we will retrieve all data from db for the campaign c
            c.historyStartDate = c.startDate
            c.historyEndDate = org.joda.time.DateTime.now()

            val jsval = ctype match {

              case "c_CTR" => { /* Campaign Performance, CTR */
                val res = Charts.get_c_CTR(Some(c))
                Json.toJson(res map { e =>
                  Json.arr(
                    JsNumber(e._1),
                    JsNumber(e._2),
                    JsNumber(e._3),
                    JsNumber(e._4))
                })
              }

              case "c_bpEffectiveness" => { /* BannerPhrases Effectiveness for the campaign */
                val res = Charts.get_c_bpEffectiveness(Some(c))
                Json.toJson(res map { e =>
                  Json.arr(
                    JsString(e._1),
                    JsNumber(e._2),
                    JsNumber(e._3),
                    JsNumber(e._4))
                })
              }

              case "c_bpEffectivenessCTR" => { /* BannerPhrases Effectiveness vs CTR for the campaign */
                val res = Charts.get_c_bpEffectivenessCTR(Some(c))
                Json.toJson(res map { e =>
                  Json.arr(
                    JsString(e._1),
                    JsNumber(e._2),
                    JsNumber(e._3))
                })
              }

              case "c_bEffectivenessCTR" => { /* Banners Effectiveness vs CTR for the campaign */
                val res = Charts.get_c_bEffectivenessCTR(Some(c))
                Json.toJson(res map { e =>
                  Json.arr(
                    JsString(e._1),
                    JsNumber(e._2),
                    JsNumber(e._3))
                })
              }

              case "bp_PP" => { /* Position Prices */
                val res = Charts.get_bp_PP(Some(c), bpID.toInt)
                Json.toJson(res map { e =>
                  Json.arr(
                    JsNumber(e._1),
                    JsNumber(e._2),
                    JsNumber(e._3),
                    JsNumber(e._4),
                    JsNumber(e._5),
                    JsNumber(e._6),
                    JsNumber(e._7))
                })
              }

              case "bp_CTR" => { /* BannerPhrases CTR */
                val res = Charts.get_bp_CTR(Some(c), bpID.toInt)
                Json.toJson(res map { e =>
                  Json.arr(
                    JsNumber(e._1),
                    JsNumber(e._2),
                    JsNumber(e._3),
                    JsNumber(e._4))
                })
              }

              case "bp_Scatter" => { /* ScatterPlot Clicks/Shows to Price */
                val res = Charts.get_bp_Scatter(Some(c), bpID.toInt)
                Json.toJson(res map { e =>
                  Json.arr(
                    JsNumber(e._1),
                    JsNumber(e._2),
                    JsNumber(e._3))
                })
              }

              case "bp_LinePlot" => { /* LinePlot Clicks vs Shows vs Price */
                val res = Charts.get_bp_LinePlot(Some(c), bpID.toInt)
                Json.toJson(res map { e =>
                  Json.arr(
                    JsNumber(e._1),
                    JsNumber(e._2),
                    JsNumber(e._3),
                    JsNumber(e._4))
                })
              }

            }

            Ok(jsval) as JSON
          }
        }
      }
    }
  }
}


