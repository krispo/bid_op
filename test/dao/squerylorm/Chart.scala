package dao.squerylorm

import breeze.plot._
import breeze.linalg._
import breeze.numerics._

class Chart {

  /** PLOT ----------------------------*/
  def getLinePlot(_x: List[Double], _ys: List[List[Double]], fTitle: String = "", fName: String = ""): Unit = {

    val x = DenseVector.apply(_x.toArray)
    val ys = _ys.map(_y => DenseVector.apply(_y.toArray))

    val f = Figure(fName)
    val p = f.subplot(0)
    for { y <- ys } { p += plot(x, y, '-') }

    p.xlabel = "x axis"
    p.ylabel = "y axis"
    p.title_=(fTitle)
    f.saveas("charts/" + fName + ".png")
    //f.visible_=(true)
  }
}