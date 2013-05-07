package dao.squerylorm

import org.joda.time.DateTime
import scala.concurrent._
import scala.concurrent.{ Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Charts {

  //Budget evolution in time
  def getBudget(oc: Option[Campaign]): List[(Long, Double)] =
    oc map {
      c => c.budgetHistory.map(bh => (bh.date.getMillis(), bh.budget)).tail
    } getOrElse (Nil)

  //Campaign CTR evolution in time with cumulative clicks and shows
  def get_c_CTR(oc: Option[Campaign]): List[(Long, Double, Double, Double)] = {
    oc map { c =>
      val cp = c.performanceHistory
      val cClicksContext = cp.map(_.clicks_context).scan(0)(_ + _).tail
      val cClicksSearch = cp.map(_.clicks_search).scan(0)(_ + _).tail
      val cShowsContext = cp.map(_.impress_context).scan(0)(_ + _).tail
      val cShowsSearch = cp.map(_.impress_search).scan(0)(_ + _).tail

      val p = cp.map(_.date).zipWithIndex

      p map {
        case (dt, i) =>
          (dt.getMillis(), //DateTime
            ctr(cClicksSearch(i), cShowsSearch(i)), //CTR search
            ctr(cClicksContext(i), cShowsContext(i)), //CTR context
            ctr(cClicksSearch(i) + cClicksContext(i), cShowsSearch(i) + cShowsContext(i)) //CTR SUM
            )
      }
    } getOrElse (Nil) // List((new DateTime().getMillis(), 1.2, 3.4, 5.6))
  }

  //BannerPhrase CTR evolution in time with cumulative clicks and shows
  def get_bp_CTR(oc: Option[Campaign], bpID: Long): List[(Long, Double, Double, Double)] = {
    oc map { c =>
      val obp = BannerPhrase.selectWithRegion(c, bpID, "0")

      obp map { bp => //find bp with identity phrase and banner but region!="0"or""

        val bpp = bp.performanceHistory
        val cClicksContext = bpp.map(_.clicks_context).scan(0)(_ + _).tail
        val cClicksSearch = bpp.map(_.clicks_search).scan(0)(_ + _).tail
        val cShowsContext = bpp.map(_.impress_context).scan(0)(_ + _).tail
        val cShowsSearch = bpp.map(_.impress_search).scan(0)(_ + _).tail

        val p = bpp.map(_.date).zipWithIndex

        p map {
          case (dt, i) =>
            (dt.getMillis(), //DateTime
              ctr(cClicksSearch(i), cShowsSearch(i)), //CTR search
              ctr(cClicksContext(i), cShowsContext(i)), //CTR context
              ctr(cClicksSearch(i) + cClicksContext(i), cShowsSearch(i) + cShowsContext(i)) //CTR SUM
              )
        }
      } getOrElse (Nil)
    } getOrElse (Nil) // Nil
  }

  //ActualBids and NetAdvisedBids evolution in time
  def get_bp_PP(oc: Option[Campaign], bpID: Long): List[(Long, Double, Double, Double, Double, Double, Double)] = { //time,min,max,pmin,pmax,bid,actualprice
    oc map { c =>
      val obp = BannerPhrase.selectWithoutRegions(c, bpID, List("", "0")).headOption //bpID!="0" and bp!=""    !!!!!!!OK

      obp map { bp =>
        val ab = bp.actualBidHistory
        val nab = bp.netAdvisedBidsHistory

        val pp = ab.map(_.date).zipWithIndex
        pp map {
          case (dt, i) =>
            (dt.getMillis(), //DateTime
              nab(i).a, //min
              nab(i).b, //max
              nab(i).c, //pmin
              nab(i).d, //pmax              
              ab(i).bid, //bid
              nab(i).e) //actual price
        }
      } getOrElse (Nil)
    } getOrElse (Nil)
  }

  //Effectiveness of all bp for the campaign 
  def get_c_bpEffectiveness(oc: Option[Campaign]): List[(String, Double, Double, Double)] = {
    oc map { c => //network_region_id="" - only for bp with XMLreport
      c.bannerPhrases.filter(_.region.map(_.network_region_id == "").getOrElse(false)) map { bp =>
        val obp = BannerPhrase.select(c, bp.id)
        obp map { bp =>
          val perf_clicks = bp.performanceHistory.map(p => p.clicks_search + p.clicks_context).sum
          val perf_cost = bp.performanceHistory.map(p => p.cost_search + p.cost_context).sum
          (bp.id.toString + bp.phrase.map(ph => " - ID=" + ph.network_phrase_id + " - " + ph.phrase).getOrElse(" -1"),
            ctr(perf_clicks, perf_cost),
            perf_clicks.toDouble,
            perf_cost)
        } getOrElse ("-1", 0d, 0d, 0d)
      } sortWith (_._2 > _._2)
    } getOrElse (Nil)
  }

  // (Effectiveness,CTR) of all bp for the campaign 
  def get_c_bpEffectivenessCTR(oc: Option[Campaign]): List[(String, Double, Double)] = {
    oc map { c => //network_region_id="" - only for bp with XMLreport
      c.bannerPhrases.filter(_.region.map(_.network_region_id == "").getOrElse(false)) map { bp =>
        val obp = BannerPhrase.select(c, bp.id)
        obp map { bp =>
          val perf_clicks = bp.performanceHistory.map(p => p.clicks_search + p.clicks_context).sum
          val perf_cost = bp.performanceHistory.map(p => p.cost_search + p.cost_context).sum
          val perf_impress = bp.performanceHistory.map(p => p.impress_search + p.impress_search).sum

          (bp.id.toString + bp.phrase.map(ph => " - ID=" + ph.network_phrase_id + " - " + ph.phrase).getOrElse(" -1"),
            ctr(perf_clicks, perf_cost),
            ctr(perf_clicks, perf_impress))
        } getOrElse ("-1", 0d, 0d)
      } sortWith (_._3 > _._3)
    } getOrElse (Nil)
  }

  //(Effectiveness,CTR) of all b for the campaign 
  def get_c_bEffectivenessCTR(oc: Option[Campaign]): List[(String, Double, Double)] = {
    oc map { c =>
      val byB = c.bannerPhrases
        .map(_.banner.map(_.network_banner_id).getOrElse("-1"))
        .distinct
        .map { bID =>
          val byBP = BannerPhrase.select(c, bID, "") //network_region_id="" - only for bp with XMLreport
            .map { bp =>
              (bp.performanceHistory.map(p => p.clicks_search + p.clicks_context).sum.toDouble,
                bp.performanceHistory.map(p => p.impress_search + p.impress_context).sum.toDouble,
                bp.performanceHistory.map(p => p.cost_search + p.cost_context).sum)
            }
          (bID, byBP.map(_._1).sum, byBP.map(_._2).sum, byBP.map(_._3).sum)
        }
      byB.map {
        case (bID, clicks, shows, cost) =>
          (bID, ctr(clicks, cost), ctr(clicks, shows))
      }
        .sortWith(_._2 > _._2)
    } getOrElse (Nil)
  }

  //CTR function
  def ctr(cl: Double, sh: Double): Double = {
    if (sh != 0)
      cl / sh
    else
      0
  }
}