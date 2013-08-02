package optimizer.pycharts

import scala.sys.process._
import play.api.libs.json._

class PyCharts {
  val dir = "/home/krispo/Documents/eclipse/bid_op/app/optimizer/pycharts/"//System.getProperty("user.dir") + "/app/optimizer/pycharts/"
  val wrap = dir + "wrap.py"
  val fnameLine = dir + "line.json"
  val fnameScatter = dir + "scatter.json"

  implicit val jspp = Json.writes[PP]

  def getLine(_x: List[Double], _ys: List[List[Double]], pp: PP): Unit = {

    val jsdata = Json.stringify {
      Json.obj(("x" -> _x), ("y" -> _ys)) ++ Json.toJson[PP](pp).as[JsObject]
    }
    writeToFile(fnameLine, jsdata)
    println("<<<")
    println(Seq("python", wrap, "line", fnameLine).!!)
    println(">>>")
  }

  def getScatter(f: Int, _x: List[Double], _y: List[Double], pp: PP) = {

    val jsdata = Json.stringify {
      Json.obj(("x" -> _x), ("y" -> _y)) ++ Json.toJson[PP](pp).as[JsObject]
    }
    writeToFile(fnameScatter, jsdata)
    println("<<<")
    println(Seq("python", wrap, "scatter", fnameScatter).!!)
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