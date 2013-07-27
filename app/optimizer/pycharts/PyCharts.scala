package optimizer.pycharts

import scala.sys.process._
import play.api.libs.json._

class PyCharts {

  def getLine(_x: List[Double], _ys: List[List[Double]], fTitle: String = "", fName: String = ""): Unit = {

    val l1 = List(2, 3, 1, 4, 8, 3)
    val l2 = List(4, 1, 5, 2, 1, 8)
    val js1 = Json.stringify {
      Json.obj {
        "x" -> l1
      }
    }
    val js2 = Json.stringify {
      Json.obj {
        "x" -> l2
      }
    }
    println("Hi!")

    println(Seq("sh", "-c", "ulimit -n").!!)
    println(Seq("pwd").!!)
    println(Seq("python", "src/main/scala/pyplot.py", "plot", js1).!!)
    println(Seq("python", "src/main/scala/pyplot.py", "plot", js2).!!)

  }

  def getScatter(f: Int, _x: List[Double], _y: List[Double], fTitle: String = "") = {

    val dir = System.getProperty("user.dir") + "/app/optimizer/pycharts/"
    val pyplot = dir + "wrap.py"
    val fname = dir + "jsdata.json"

    val jsdata = Json.stringify {
      Json.obj(("x" -> _x), ("y" -> _y))
    }
    writeToFile(fname, jsdata)
    println("<<<")
    println(pyplot)
    println(Seq("python", pyplot, "scatter", fname).!!)
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