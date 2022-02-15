// -*- mode: scala -*-

import $ivy.`com.lihaoyi::fansi:0.1.1`
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.Try

class MeterTester(
  val name: String,
  val baseUrl: String
)(implicit ec: ExecutionContext) {

  def doRequest(uuid: String): Future[Option[String]] = Future {
    Try {
      val res = requests.post(baseUrl, data = Map("browserId" -> uuid))
      if(res.statusCode != 200)
        None
      else {
        val js = ujson.read(res.text)
//        println(js)
        js.obj.get("testGroup").map(_.str)
      }
    }.toOption.flatten
  }
}

@main
def main(urlsFile: String = "urls.txt") = {

  implicit val ec = ExecutionContext.global

  val urls = scala.io.Source.fromFile(urlsFile).getLines()
    .filterNot(_.startsWith("#"))
    .map { s =>
      val f = s.split(",")
      (f(0), f(1))
    }.toList

  val uuids = Seq(
    "gia:FCAC2B2B-05AA-43C7-8584-827547887081" -> "Test",
    // "65548a6b-01d9-436c-b29f-120a4f02a23f"     -> "NotInTest",
    // "gia:EE2001BE-4A6B-45A7-824F-E4F4B33D9102" -> "TestGroup1"
  )

  val tstrs = urls.map { case (nm, url) => new MeterTester(nm, url) }

  for((uuid, exp) <- uuids; tstr <- tstrs) {
    val res = Await.result(tstr.doRequest(uuid), Duration.Inf)
    val passTxt = if(res.exists(_ == exp)) fansi.Color.Green("OK") else s"${fansi.Color.Red("FAIL")} ${res.getOrElse("?")} ≠ $exp"
    println(f"${tstr.name}%-10s ${uuid.take(20)}%-20s $passTxt")
    // assert(res.exists(_ == exp), s"$uuid did not match: $exp")
  }

  println("Done")

}
