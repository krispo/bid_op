package optimizer.pycharts

import scala.sys.process._
import play.api.libs.json._

class PyCharts {
  val dir = "/home/krispo/Documents/eclipse/bid_op/app/optimizer/pycharts/" //System.getProperty("user.dir") + "/app/optimizer/pycharts/"
  val wrap = dir + "wrap.py"
  val fnameLine = dir + "line.json"
  val fnameScatter = dir + "scatter.json"
  val fnameLineScatter = dir + "linescatter.json"

  implicit val jspp = Json.writes[PP]

  def getLine(_xl: List[List[Double]], _yl: List[List[Double]], pp: PP): Unit = {

    val jsl = Json.obj(("x" -> _xl), ("y" -> _yl))
    val jsdata = Json.stringify {
      Json.obj(("line" -> jsl)) ++ Json.toJson[PP](pp).as[JsObject]
    }
    writeToFile(fnameLine, jsdata)
    println("<<<")
    println(Seq("python", wrap, "line", fnameLine).!!)
    println(">>>")
  }

  def getScatter(f: Int, _xs: List[Double], _ys: List[Double], pp: PP) = {

    val jss = Json.obj(("x" -> _xs), ("y" -> _ys))
    val jsdata = Json.stringify {
      Json.obj(("scatter" -> jss)) ++ Json.toJson[PP](pp).as[JsObject]
    }
    writeToFile(fnameScatter, jsdata)
    println("<<<")
    println(Seq("python", wrap, "scatter", fnameScatter).!!)
    println(">>>")
  }

  def getLineScatter(_xl: List[List[Double]], _yl: List[List[Double]], _xs: List[Double], _ys: List[Double], pp: PP) = {

    val jsl = Json.obj(("x" -> _xl), ("y" -> _yl))
    val jss = Json.obj(("x" -> _xs), ("y" -> _ys))
    val jsdata = Json.stringify {
      Json.obj(("line" -> jsl), ("scatter" -> jss)) ++ Json.toJson[PP](pp).as[JsObject]
    }
    writeToFile(fnameLineScatter, jsdata)
    println("<<<")
    println(Seq("python", wrap, "linescatter", fnameLineScatter).!!)
    println(">>>")
  }

  def writeToFile(fname: String, str: String) {
    import java.io.File
    val pw = new java.io.PrintWriter(new File(fname))
    try {
      pw.write(str)
    } finally {
      pw.close()
    }
  }
}

/*
 * Plot Parameters
 */
case class PP(
  var title: Option[String] = None,
  var xlabel: Option[String] = None,
  var ylabel: Option[String] = None,
  var fname: Option[String] = None)