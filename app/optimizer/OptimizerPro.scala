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

  lazy val bp2ctr = c.bannerPhrases.map { bp =>
    val ctrs = (0 to 4).toList map { pos =>
      ctr(bp, pos)
    }
    bp -> ctrs
  } toMap

  lazy val bp2imp = c.bannerPhrases.map { bp =>
    bp -> calc_imp(bp.performanceHistory)
  } toMap

  def clicos(bp: BannerPhrase, price: Double, nab: domain.NetAdvisedBids): (Double, Double) = {
    val pos = getPos(price, nab)

    val _ctr = bp2ctr(bp)(pos)

    val clicks = _ctr * bp2imp(bp)
    val cost = clicks * price

    (clicks, cost)
  }
}
