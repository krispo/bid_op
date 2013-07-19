package dao.squerylorm

import org.joda.time.DateTime
import scala.concurrent._
import scala.concurrent.{ Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{ Duration, TimeUnit }

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

  //Campaign CTR evolution in time with cumulative clicks and shows
  def get_c_CTR_Loc(oc: Option[Campaign]): List[(Long, Double, Double, Double)] = {
    oc map { c =>
      val n = 48
      val cp = c.performanceHistory
      val cClicksContext = cp.map(_.clicks_context).scan(0)(_ + _).tail
      val cClicksSearch = cp.map(_.clicks_search).scan(0)(_ + _).tail
      val cShowsContext = cp.map(_.impress_context).scan(0)(_ + _).tail
      val cShowsSearch = cp.map(_.impress_search).scan(0)(_ + _).tail

      val p = cp.map(_.date).zipWithIndex

      p map {
        case (dt, i) =>
          if (i > n)
            (dt.getMillis(), //DateTime
              ctr(cClicksSearch(i) - cClicksSearch(i - n), cShowsSearch(i) - cShowsSearch(i - n)), //CTR search
              ctr(cClicksContext(i) - cClicksContext(i - n), cShowsContext(i) - cShowsContext(i - n)), //CTR context
              ctr(cClicksSearch(i) + cClicksContext(i) - (cClicksSearch(i - n) + cClicksContext(i - n)),
                cShowsSearch(i) + cShowsContext(i) - (cShowsSearch(i - n) + cShowsContext(i - n))) //CTR SUM
                )
          else
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
      val obp = BannerPhrase.select(c, bpID)

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
      val obp = BannerPhrase.select(c, bpID)

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
      c.bannerPhrases map { bp =>
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
      c.bannerPhrases map { bp =>
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
          val byBP = BannerPhrase.select(c, bID) //network_region_id="" - only for bp with XMLreport
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

  /* Scatter plot clicks/impress to price */
  def get_bp_Scatter(oc: Option[Campaign], bpID: Long): List[(Double, Int, Int)] = {
    oc map { c =>
      syncDateTime(c, bpID).map {
        case (dt, na, p) =>
          (na.e, p.clicks_search + p.clicks_context, p.impress_search + p.impress_context)
      }
    } getOrElse (Nil) // Nil
  }

  /* Line plot clicks vs impress vs price */
  def get_bp_LinePlot(oc: Option[Campaign], bpID: Long): List[(Long, Int, Int, Double)] = {
    oc map { c =>
      syncDateTime(c, bpID).map {
        case (dt, na, p) =>
          (dt.getMillis(),
            p.clicks_search + p.clicks_context,
            p.impress_search + p.impress_context,
            na.e)
      }
    } getOrElse (Nil) // Nil
  }

  /* DailyTraffic */
  def get_bp_Traffic(oc: Option[Campaign], bpID: Long): List[(Double, Double, Double, Double)] = {
    oc map { c =>
      val data = syncDateTime(c, bpID).map {
        case (dt, na, p) =>
          (dt.getHourOfDay(), dt.getMinuteOfHour(),
            p.clicks_search + p.clicks_context,
            p.impress_search + p.impress_context,
            na.e)
      }

      val res = for {
        h <- data.map(_._1).distinct;
        m <- data.map(_._2).distinct
      } yield {
        val f = data.filter(d => d._1 == h & d._2 == m)
        (h.toDouble + m.toDouble / 60, //h.toString + ":" + m.toString,
          ctr(f.map(_._3).sum, f.length),
          ctr(f.map(_._4).sum, f.length),
          ctr(f.map(_._5).sum, f.length))
      }
      res.sortWith(_._1 < _._1)

    } getOrElse (Nil) // Nil
  }

  /* Scatter plot clicks/impress to price */
  def get_c_Scatter(oc: Option[Campaign]): List[(Double, Int, Int)] = {
    oc map { c =>
      val data = c.bannerPhrases
        .flatMap { bp =>
          syncDateTime(c, bp.id)
        }.map {
          case (dt, na, p) =>
            (na.e, p.clicks_search + p.clicks_context, p.impress_search + p.impress_context)
        }
      data
    } getOrElse (Nil) // Nil
  }

  /* DailyTraffic */
  def get_c_Traffic(oc: Option[Campaign]): List[(Double, Double, Double, Double)] = {
    oc map { c =>
      val data = c.bannerPhrases
        .flatMap { bp =>
          syncDateTime(c, bp.id)
        }.map {
          case (dt, na, p) =>
            (dt.getHourOfDay(), dt.getMinuteOfHour(),
              p.clicks_search + p.clicks_context,
              p.impress_search + p.impress_context,
              na.e)
        }
      val res = for {
        h <- data.map(_._1).distinct;
        m <- data.map(_._2).distinct
      } yield {
        val f = data.filter(d => d._1 == h & d._2 == m)
        (h.toDouble + m.toDouble / 60, //h.toString + ":" + m.toString,
          ctr(f.map(_._3).sum, f.length),
          ctr(f.map(_._4).sum, f.length),
          ctr(f.map(_._5).sum, f.length))
      }
      res.sortWith(_._1 < _._1)

    } getOrElse (Nil) // Nil
  }

  /* ClusterPrice */
  def get_c_ClusterPrice(oc: Option[Campaign]): List[(Double, Double, Double, Int, Int)] = {
    oc map { c =>
      val bpList = c.bannerPhrases map { bp =>
        val info = syncDateTime(c, bp.id).map {
          case (dt, na, p) =>
            (p.clicks_search + p.clicks_context,
              p.impress_search + p.impress_context,
              na.e)
        }
        println("bp - " + bp + " - " + info.length)
        val minI = info.map(_._3).min
        val maxI = info.map(_._3).max
        val nClicks = info.map(_._1).sum
        val nShows = info.map(_._2).sum
        val sInfo = info.map(i => (ctr(i._1, nClicks), ctr(i._2, nShows), ctr(i._3 - minI, (maxI - minI)))) //Standartization

        bp -> sInfo
      }

      val f =
        for { i <- 1 to 9 } yield {
          val q = bpList.map {
            case (bp, l) =>
              //println("qwewer" + l.map(_._3))
              val f1 = l.filter(_._3 <= ctr(i, 10))
              val f1N = f1.length
              val f2 = l.filter(_._3 > ctr(i, 10))
              val f2N = f2.length
              (ctr(f1.map(_._1).sum, f1N),
                ctr(f2.map(_._1).sum, f2N),
                ctr(f1.map(_._2).sum, f1N),
                ctr(f2.map(_._2).sum, f2N),
                f1N,
                f2N)
          }
          (ctr(i, 10),
            ctr(q.map(_._1).sum, q.map(_._2).sum),
            ctr(ctr(q.map(_._1).sum, q.map(_._3).sum), ctr(q.map(_._2).sum, q.map(_._4).sum)),
            q.map(_._5).sum,
            q.map(_._6).sum)
        }
      println("!!!!!!!!!!!" + f)
      f.toList
    } getOrElse (Nil)
  }

  //CTR function
  def ctr(cl: Double, sh: Double): Double = {
    if (sh != 0)
      cl / sh
    else
      0
  }

  def syncDateTime(c: Campaign, bpID: Long, oStart: Option[DateTime] = None, oEnd: Option[DateTime] = None): List[(DateTime, NetAdvisedBidHistory, BannerPhrasePerformance)] = {
    import dao.squerylorm.{ NetAdvisedBidHistory, BannerPhrasePerformance }

    BannerPhrase.select(c, bpID) map { bp =>
      val nMinutes = 15
      val dtS = oStart.getOrElse(c.historyStartDate)
      //val dtE = oEnd.getOrElse(c.historyEndDate)
      val start = dtS
        .minusMillis(dtS.getMillisOfDay())
        .plusMinutes(nMinutes * (dtS.getMinuteOfDay() / nMinutes + 1))

      def f(dt: DateTime, naL: List[NetAdvisedBidHistory], pL: List[BannerPhrasePerformance]): List[(DateTime, NetAdvisedBidHistory, BannerPhrasePerformance)] = {
        if (naL.isEmpty | pL.isEmpty) //| dt.isAfter(dtE))
          Nil
        else {
          //println(naL.length + " - " + pL.length + " : " + naL.head.dateTime + " - " + pL.head.dateTime)
          val ah = naL.head
          val ph = pL.head

          val ahA = ah.dateTime.isAfter(dt.minusMinutes(3)) | ah.dateTime.isEqual(dt.minusMinutes(3))
          val ahB = ah.dateTime.isBefore(dt.plusMinutes(nMinutes - 3))
          val phA = ph.dateTime.isAfter(dt.minusMinutes(3)) | ph.dateTime.isEqual(dt.minusMinutes(3))
          val phB = ph.dateTime.isBefore(dt.plusMinutes(nMinutes - 3))

          (ahA, ahB, phA, phB) match {
            case (true, true, true, true) =>
              (dt, ah, ph) :: f(dt.plusMinutes(nMinutes), naL.tail, pL.tail)

            case (true, false, true, true) => (dt, NetAdvisedBidHistory(ph.bannerphrase_id, dt), ph) :: f(dt.plusMinutes(nMinutes), naL, pL.tail)
            case (true, true, true, false) => (dt, ah, BannerPhrasePerformance(ph.bannerphrase_id, date = dt)) :: f(dt.plusMinutes(nMinutes), naL.tail, pL)
            case (true, false, true, false) => (dt, NetAdvisedBidHistory(ph.bannerphrase_id, dt), BannerPhrasePerformance(bannerphrase_id = ph.bannerphrase_id, date = dt)) :: f(dt.plusMinutes(nMinutes), naL, pL)
            case (false, _, true, _) => f(dt, naL.tail, pL)
            case (true, _, false, _) => f(dt, naL, pL.tail)
            case (false, _, false, _) => f(dt, naL.tail, pL.tail)
          }
        }
      }
      f(start, bp.netAdvisedBidsHistory, bp.performanceHistory)

    } getOrElse { Nil }
  }

  /*
  def syncDateTime(c: Campaign, bpID: Long): List[(DateTime, NetAdvisedBidHistory, BannerPhrasePerformance)] = {

    BannerPhrase.select(c, bpID) map { bp =>
      val nMinutes = 15
      val start = c.historyStartDate
        .minusMillis(c.historyStartDate.getMillisOfDay())
        .plusMinutes(nMinutes * (c.historyStartDate.getMinuteOfDay() / nMinutes + 1))

      def f(dt: DateTime, naL: List[NetAdvisedBidHistory], pL: List[BannerPhrasePerformance], isHoleBefore: Boolean): List[(DateTime, NetAdvisedBidHistory, BannerPhrasePerformance)] = {
        if (naL.isEmpty | pL.isEmpty)
          Nil
        else {
          //println(naL.length + " - " + pL.length + " : " + naL.head.dateTime + " - " + pL.head.dateTime)
          val ah = naL.head
          val ph = pL.head

          val ahA = ah.dateTime.isAfter(dt) | ah.dateTime.isEqual(dt)
          val ahB = ah.dateTime.isBefore(dt.plusMinutes(nMinutes))
          val phA = ph.dateTime.isAfter(dt) | ph.dateTime.isEqual(dt)
          val phB = ph.dateTime.isBefore(dt.plusMinutes(nMinutes))

          (ahA, ahB, phA, phB) match {
            case (true, true, true, true) =>
              if (!isHoleBefore)
                (dt, ah, ph) :: f(dt.plusMinutes(nMinutes), naL.tail, pL.tail, false)
              else
                f(dt.plusMinutes(nMinutes), naL.tail, pL.tail, false)

            case (true, false, true, true) => f(dt.plusMinutes(nMinutes), naL, pL.tail, true)
            case (true, true, true, false) => f(dt.plusMinutes(nMinutes), naL.tail, pL, true)
            case (true, false, true, false) => f(dt.plusMinutes(nMinutes), naL, pL, true)
            case (false, _, true, _) => f(dt, naL.tail, pL, true)
            case (true, _, false, _) => f(dt, naL, pL.tail, true)
            case (false, _, false, _) => f(dt, naL.tail, pL.tail, true)
          }
        }
      }
      f(start, bp.netAdvisedBidsHistory, bp.performanceHistory, false)

    } getOrElse { Nil }
  }
  */
  /*
  def f(dt: DateTime, naL: List[NetAdvisedBidHistory], pL: List[BannerPhrasePerformance]): List[(DateTime, NetAdvisedBidHistory, BannerPhrasePerformance)] = {
        if (naL.isEmpty | pL.isEmpty)
          Nil
        else {
          //println(naL.length + " - " + pL.length + " : " + naL.head.dateTime + " - " + pL.head.dateTime)
          val ah = naL.head
          val ph = pL.head

          val ahA = ah.dateTime.isAfter(dt) | ah.dateTime.isEqual(dt)
          val ahB = ah.dateTime.isBefore(dt.plusMinutes(nMinutes))
          val phA = ph.dateTime.isAfter(dt) | ph.dateTime.isEqual(dt)
          val phB = ph.dateTime.isBefore(dt.plusMinutes(nMinutes))

          (ahA, ahB, phA, phB) match {
            case (true, true, true, true) =>
              (dt, ah, ph) :: f(dt.plusMinutes(nMinutes), naL.tail, pL.tail)

            case (true, false, true, true) => (dt, NetAdvisedBidHistory(ph.bannerphrase_id, dt), ph) :: f(dt.plusMinutes(nMinutes), naL, pL.tail)
            case (true, true, true, false) => (dt, ah, BannerPhrasePerformance(ph.bannerphrase_id, date = dt)) :: f(dt.plusMinutes(nMinutes), naL.tail, pL)
            case (true, false, true, false) => (dt, NetAdvisedBidHistory(ph.bannerphrase_id, dt), BannerPhrasePerformance(bannerphrase_id = ph.bannerphrase_id, date = dt)) :: f(dt.plusMinutes(nMinutes), naL, pL)
            case (false, _, true, _) => f(dt, naL.tail, pL)
            case (true, _, false, _) => f(dt, naL, pL.tail)
            case (false, _, false, _) => f(dt, naL.tail, pL.tail)
          }
        }
      }*/
}