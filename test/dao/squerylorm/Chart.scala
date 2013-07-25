package dao.squerylorm

import breeze.plot._
import breeze.linalg._
import breeze.numerics._

class Chart {

  /** PLOT ----------------------------*/
  def getLine(_x: List[Double], _ys: List[List[Double]], fTitle: String = "", fName: String = ""): Unit = {

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

  def getScatter(f: Figure, _x: List[Double], _y: List[Double], fTitle: String = ""): Figure = {

    val x = DenseVector.apply(_x.toArray)
    val y = DenseVector.apply(_y.toArray)

    val p = f.subplot(0)
    p += plot(x, y, '.', "b")

    p.xlabel = "Затраты, $"
    p.ylabel = "Клики"
    p.title_=(fTitle)
    f
  }

  def addPoints(f: Figure, _x: List[Double], _y: List[Double], style: Char = '.', color: String = "b"): Figure = {
    val x = DenseVector.apply(_x.toArray)
    val y = DenseVector.apply(_y.toArray)

    val p = f.subplot(0)
    p += plot(x, y, style, color)
    f
  }

  def create(fTitle: String): Figure = Figure(fTitle)

  def show(f: Figure, fName: String = "") = {
    f.saveas("charts/" +fName + ".png")
    f.visible_=(true)
  }
}