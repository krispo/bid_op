package optimizer

import domain._
import scala.concurrent.duration.Duration
import dao.squerylorm.NetAdvisedBidHistory

class OptimizerPro {
  def cost_ij(bids: List[Double], price: List[Double]): Double = ???

  def clicks_ij(bp: BannerPhrase, traffic: Double, impress: Int, ctr: Double): Double = {
    traffic * impress * ctr
  }

  def efficientFrontier(l: List[(Double, Double)]): List[(Double, Double)] = {

    val ls = l.sortWith(_._1 < _._1)
    val lhead = ls.head
    val ltail = ls.tail

    val (l1, l2) = ltail.partition(e => e._1 <= lhead._1 + ltail.head._1)
    //l1.filter(p)

    Nil
  }

}

class CTRclass(c: Campaign) {
  val minutes = 5

  def calc_ctr(p: List[domain.Performance]) = {
    p match {
      case Nil => 0d
      case _ =>
        val clicksContext = p.map(_.clicks_context).sum //.scan(0)(_ + _).tail
        val clicksSearch = p.map(_.clicks_search).sum //.scan(0)(_ + _).tail
        val showsContext = p.map(_.impress_context).sum //.scan(0)(_ + _).tail
        val showsSearch = p.map(_.impress_search).sum //.scan(0)(_ + _).tail
        ratio(clicksSearch + clicksContext, showsSearch + showsContext)
    }
  }

  def calc_imp(p: List[domain.Performance]) = {
    p match {
      case Nil => 0d
      case _ =>
        val showsContext = p.map(_.impress_context).sum //.scan(0)(_ + _).tail
        val showsSearch = p.map(_.impress_search).sum //.scan(0)(_ + _).tail
        ratio(showsSearch + showsContext, p.filter(a => a.impress_context != 0 | a.impress_search != 0).length)
    }
  }

  def ratio(cl: Double, sh: Double): Double = {
    if (sh != 0)
      cl / sh
    else
      0
  }

  def ctr(bp: BannerPhrase, pos: Int) = {
    val bpp = pos match {
      case 4 => //[PremiumMax,...)
        for {
          ana <- bp.netAdvisedBidsHistory;
          perf <- bp.performanceHistory
        } yield {
          if (ana.e >= ana.d & ana.dateTime.minusMinutes(minutes).isBefore(perf.dateTime) & ana.dateTime.plusMinutes(minutes).isAfter(perf.dateTime)) {
            Some(perf)
          } else {
            None
          }
        }

      case 3 => //[PremiumMin,PremiumMax)
        for {
          ana <- bp.netAdvisedBidsHistory;
          perf <- bp.performanceHistory
        } yield {
          if (ana.e < ana.d & ana.e >= ana.c & ana.dateTime.minusMinutes(5).isBefore(perf.dateTime) & ana.dateTime.plusMinutes(5).isAfter(perf.dateTime)) {
            Some(perf)
          } else {
            None
          }
        }

      case 2 => //[Max,PremiumMin)
        for {
          ana <- bp.netAdvisedBidsHistory;
          perf <- bp.performanceHistory
        } yield {
          if (ana.e < ana.c & ana.e >= ana.b & ana.dateTime.minusMinutes(5).isBefore(perf.dateTime) & ana.dateTime.plusMinutes(5).isAfter(perf.dateTime)) {
            Some(perf)
          } else {
            None
          }
        }

      case 1 => //[Min,min(Max,PremiumMin,PremiumMax))
        for {
          ana <- bp.netAdvisedBidsHistory;
          perf <- bp.performanceHistory
        } yield {
          if (ana.e < scala.math.min(ana.c, ana.b) & ana.e >= ana.a & ana.dateTime.minusMinutes(5).isBefore(perf.dateTime) & ana.dateTime.plusMinutes(5).isAfter(perf.dateTime)) {
            Some(perf)
          } else {
            None
          }
        }

      case 0 => //[...,Min)
        for {
          ana <- bp.netAdvisedBidsHistory;
          perf <- bp.performanceHistory
        } yield {
          if (ana.e <= ana.a & ana.dateTime.minusMinutes(5).isBefore(perf.dateTime) & ana.dateTime.plusMinutes(5).isAfter(perf.dateTime)) {
            Some(perf)
          } else { None }
        }
    }
    calc_ctr(bpp.flatten)
  }

  def getPos(price: Double, nab: domain.NetAdvisedBids): Int = {
    /*
     * positions: 4 - PremiumMax, 3 - PremiumMin, 2 - Max, 1 - Min, 0 - others
     */

    price match {
      //[PremiumMax,...)
      case p if (p >= nab.d) => 4

      //[PremiumMin,PremiumMax)
      case p if (p < nab.d & p >= nab.c) => 3

      //[Max,PremiumMin)
      case p if (p < nab.c & p >= nab.b) => 2

      //[Min,min(Max,PremiumMin,PremiumMax))
      case p if (p < scala.math.min(nab.b, nab.c) & p >= nab.a) => 1

      //[...,Min)
      case p if (p <= nab.a) => 0
    }
  }

  lazy val bp2ctr: Map[BannerPhrase, List[Double]] = c.bannerPhrases.map { bp =>
    val ctrs = (0 to 4).toList map { pos =>
      //ctr(bp, pos)
      (pos * pos * 2 + 1).toDouble / 100
    }
    bp -> ctrs
  } toMap

  lazy val bp2imp = c.bannerPhrases.map { bp =>
    //bp -> calc_imp(bp.performanceHistory)
    bp -> bp.phrase.map(_.stats.getOrElse(0l)).getOrElse(0l)
  } toMap

  def clicosByPrice(bp: BannerPhrase, price: Double, oNAB: Option[domain.NetAdvisedBids] = None): (Double, Double) = {
    val nab = oNAB.getOrElse(bp.netAdvisedBidsHistory.last)

    val pos = getPos(price, nab)

    val _ctr = bp2ctr(bp)(pos)

    val clicks = _ctr * bp2imp(bp) / (30 * 96)
    val cost = clicks * price

    (clicks, cost)
  }

  def clicosByPos(bp: BannerPhrase, pos: Int, oNAB: Option[domain.NetAdvisedBids] = None): (Double, Double) = {
    val nab = oNAB.getOrElse(bp.netAdvisedBidsHistory.last)

    val price = pos match {
      case 0 => nab.a / 2
      case 1 => nab.a
      case 2 => scala.math.min(nab.b, nab.c)
      case 3 => nab.c
      case 4 => nab.d
    }

    val _ctr = bp2ctr(bp)(pos)

    val clicks = _ctr * bp2imp(bp) / (30 * 96)
    val cost = clicks * price

    (clicks, cost)
  }

  /**
   * Optimization
   */
  import scala.math._
  def bp2tg = {
    for {
      (bp, ctrl) <- bp2ctr
    } yield {
      val nab = bp.netAdvisedBidsHistory.last
      val d_cli01 = clicosByPos(bp, 1)._1 - clicosByPos(bp, 0)._1
      val d_cos01 = clicosByPos(bp, 1)._2 - clicosByPos(bp, 0)._2

      val d_cli12 = clicosByPos(bp, 2)._1 - clicosByPos(bp, 1)._1
      val d_cos12 = clicosByPos(bp, 2)._2 - clicosByPos(bp, 1)._2

      val d_cli23 = clicosByPos(bp, 3)._1 - clicosByPos(bp, 2)._1
      val d_cos23 = clicosByPos(bp, 3)._2 - clicosByPos(bp, 2)._2

      val d_cli34 = clicosByPos(bp, 4)._1 - clicosByPos(bp, 3)._1
      val d_cos34 = clicosByPos(bp, 4)._2 - clicosByPos(bp, 3)._2

      val p01 = d_cli01 / d_cos01 //(ctrl(1) - ctrl(0)) / (ctrl(1) * nab.a - ctrl(0) * nab.a / 2)
      val p12 = d_cli12 / d_cos12 //(ctrl(2) - ctrl(1)) / (ctrl(2) * nab.b - ctrl(1) * nab.a)
      val p23 = d_cli23 / d_cos23 //(ctrl(3) - ctrl(2)) / (ctrl(3) * nab.c - ctrl(2) * nab.b)
      val p34 = d_cli34 / d_cos34 //(ctrl(4) - ctrl(3)) / (ctrl(4) * nab.d - ctrl(3) * nab.c)
      /*val p01 = (ctrl(1) - ctrl(0)) / (ctrl(1) * nab.a - ctrl(0) * nab.a / 2)
      val p12 = (ctrl(2) - ctrl(1)) / (ctrl(2) * nab.b - ctrl(1) * nab.a)
      val p23 = (ctrl(3) - ctrl(2)) / (ctrl(3) * nab.c - ctrl(2) * nab.b)
      val p34 = (ctrl(4) - ctrl(3)) / (ctrl(4) * nab.d - ctrl(3) * nab.c)*/

      val tgl = List(
        (p01, (d_cli01, d_cos01)),
        (p12, (d_cli12, d_cos12)),
        (p23, (d_cli23, d_cos23)),
        (p34, (d_cli34, d_cos34)))
      /*val tgl = List(
        (p01, (ctrl(1) - ctrl(0), ctrl(1) * nab.a - ctrl(0) * nab.a / 2)),
        (p12, (ctrl(2) - ctrl(1), ctrl(2) * nab.b - ctrl(1) * nab.a)),
        (p23, (ctrl(3) - ctrl(2), ctrl(3) * nab.c - ctrl(2) * nab.b)),
        (p34, (ctrl(4) - ctrl(3), ctrl(4) * nab.d - ctrl(3) * nab.c)))*/

      bp -> tgl
    }
  }

  lazy val clicosMAX = {
    val res = c.bannerPhrases.map { bp =>
      clicosByPos(bp, 4)
    }
    val cli = res.map(_._1).sum
    val cos = res.map(_._2).sum
    (cli, cos)
  }

  def perfectEF = {
    val tgl = bp2tg.map(_._2).toList.flatten
    val _clicos = tgl.sortWith(_._1 < _._1) //.map(_._2)
    val clicos = _clicos.map(_._2)
    println("@#$%^" + clicosMAX)
    println(clicos.length)
    println(bp2ctr.map(_._2))
    println(_clicos.map(_._1))

    //println(clicos)
    val clicks = clicos.map(_._1).scan(0d)(_ + _) //.tail //.map(clicosMAX._1 - _)
    val cost = clicos.map(_._2).scan(0d)(_ + _) //.tail //.map(clicosMAX._2 - _)

    clicks.map(clicks.last - _) zip cost.map(cost.last - _)
    //clicks.map(clicosMAX._1 - _) zip cost.map(clicosMAX._2 - _)
  }

  //def greeddown(bp2ind:Map[BannerPhrase,Int]):Map[BannerPhrase,]
  def deltaUP(bp2ind: Map[BannerPhrase, Int]): Map[BannerPhrase, Double] = {
    bp2tg.map {
      case (bp, l) => bp -> l(bp2ind.get(bp).get)
    }
    c.bannerPhrases.map(_ -> 1.0).toMap
  }

  def greedDOWN = {
    def deltaDOWN(bp2ind: scala.collection.mutable.Map[BannerPhrase, Int]): List[(Double, Double)] = {

      val tgl = bp2tg.map {
        case (bp, l) =>
          val ind = bp2ind.get(bp).get
          bp -> (l(if (ind < 0) 0 else ind) -> ind)
      }.toList
      tgl.filter(tg => tg._2._2 >= 0) match {
        case Nil => Nil
        case l =>
          println(l.length)
          val m = l.minBy(_._2._1._1)
          bp2ind(m._1) = m._2._2 - 1
          m._2._1._2 :: deltaDOWN(bp2ind)
      }
    }
    val clicos = deltaDOWN(scala.collection.mutable.Map(c.bannerPhrases.map(bp => bp -> 3).toSeq: _*))
    val clicks = clicos.map(_._1).scan(0d)(_ + _) //.tail //.map(clicosMAX._1 - _)
    val cost = clicos.map(_._2).scan(0d)(_ + _) //.tail //.map(clicosMAX._2 - _)
    println("!!!", clicks.last, cost.last)
    clicks.map(clicks.last - _) zip cost.map(cost.last - _)
    //clicks.map(clicosMAX._1 - _) zip cost.map(clicosMAX._2 - _)
    //clicks zip cost

  }

  def greedUP = {
    def deltaUP(bp2ind: scala.collection.mutable.Map[BannerPhrase, Int]): List[(Double, Double)] = {

      val tgl = bp2tg.map {
        case (bp, l) =>
          val ind = bp2ind.get(bp).get
          bp -> (l(if (ind > 3) 3 else ind) -> ind)
      }.toList
      tgl.filter(tg => tg._2._2 <= 3) match {
        case Nil => Nil
        case l =>
          println(l.length)
          val m = l.maxBy(_._2._1._1)
          bp2ind(m._1) = m._2._2 + 1
          m._2._1._2 :: deltaUP(bp2ind)
      }
    }
    val clicos = deltaUP(scala.collection.mutable.Map(c.bannerPhrases.map(bp => bp -> 0).toSeq: _*))
    val clicks = clicos.map(_._1).scan(0d)(_ + _) //.tail //.map(clicosMAX._1 - _)
    val cost = clicos.map(_._2).scan(0d)(_ + _) //.tail //.map(clicosMAX._2 - _)
    //println("!!!", clicks.last, cost.last)
    //clicks.map(clicks.last - _) zip cost.map(cost.last - _)
    clicks zip cost

  }
}
