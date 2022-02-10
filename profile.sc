// -*- mode: scala -*-

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration

class MeterTester(val baseUrl: String)(implicit ec: ExecutionContext) {

  def doRequest(uuid: String) = Future {
    val res = requests.post(baseUrl, data = Map("browserId" -> uuid))
    if(res.statusCode != 200)
      None
    else
      ujson.read(res.text).obj.get("testGroup").map(_.str)
  }
}

@main
def main(url: Seq[String]) = {

  implicit val ec = ExecutionContext.global

  val uuids =
    Seq("65548a6b-01d9-436c-b29f-120a4f02a23f" -> "NotInTest")

  val tstrs = url.map(new MeterTester(_))

  for((uuid, exp) <- uuids; tstr <- tstrs) {
    val res = Await.result(tstr.doRequest(uuid), Duration.Inf)
    val passTxt = if(res.exists(_ == exp)) "pass" else "fail"
    println(s"${tstr.baseUrl} :: ${uuid} :: $passTxt")
    assert(res.exists(_ == exp), s"$uuid did not match: $exp")
  }

  println("Done")

}
