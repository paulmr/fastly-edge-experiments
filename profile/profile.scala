import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration.Inf
import scala.util.Try
import scala.io.Source.fromFile
import java.time.{ Duration, Instant }
import me.tongfei.progressbar.ProgressBar
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException

case class TestResult(duration: Duration, success: Boolean)
case class AggregatedResults(tag: String, totalClock: Duration, res: Seq[TestResult]) {
  val avgDuration = Duration.ofNanos(res.map(_.duration.toNanos).sum / res.length)
  val numSuccess = res.filter(_.success).length
}

class MeterTester(
  val name: String,
  val baseUrl: String,
  numThreads: Int = 1
) {

  val threadPool = Executors.newFixedThreadPool(numThreads)
  implicit val ec = ExecutionContext.fromExecutorService(threadPool)

  def doRequest(uuid: String): Future[(Option[String], Duration)] = Future {
    val startTime = System.nanoTime()
    val res = Try {
      val res = requests.post(baseUrl, data = Map("browserId" -> uuid))
      if(res.statusCode != 200) None else {
        val js = ujson.read(res.text)
        js.obj.get("testGroup").map(_.str)
      }
    }.toOption.flatten
    val t = Duration.ofNanos(System.nanoTime() - startTime)
    (res, t)
  }

  def doTest(uuid: String, exp: String): Future[TestResult] = {
    doRequest(uuid).map { case (res, t) =>
      val pass = res.exists(_ == exp)
      TestResult(t, pass)
    }
  }

  def doTests(uuids: Seq[(String, String)]) = {
//    val pb = new ProgressBar(s"$name-complete", uuids.length)
    val startTime = System.nanoTime()
    val f = Future.traverse(uuids) { case (uuid, exp) =>
      val f = doTest(uuid, exp)
  //    f.foreach(_ => pb.step())
      f
    }.map { r =>
      val t = Duration.ofNanos(System.nanoTime() - startTime)
      AggregatedResults(name, t, r)
    }
    // f.andThen { case _ =>
    //   pb.close()
    // }
    f
  }

  def finish(): Unit = {
    threadPool.shutdownNow()
  }
}

object Example {

// def withPb[T](name: String, len: Int)(cb: (() => Unit) => Future[T])(implicit ec: ExecutionContext): Future[T] = {
//   val pb = new ProgressBar(name, len)
//   val updateCb: () => Unit = () => pb.step()
//   cb(updateCb).andThen { case _ => pb.close() }
// }

  import mainargs._

  @main
  def run(
    urlsFile: String = "urls.txt",
    testDataFile: String = "browserIds.csv",
    threads: Int = 10
  ) = {
    import scala.io.Source.fromFile
    import java.util.concurrent.Executors

    // val numThreads = System.getProperty("scala.concurrent.context.numThreads")
    // println(s"numThreads: $numThreads")

    implicit val ec = ExecutionContext.global

    val urls = fromFile(urlsFile).getLines()
      .filterNot(_.startsWith("#"))
      .map { s =>
        val f = s.split(",")
        (f(0), f(1))
      }.toList

    val uuids = fromFile(testDataFile).getLines()
      .map { id =>
        // for each id, introduce another random one that doesn't exist
        Seq(
          id -> "Test",
          java.util.UUID.randomUUID().toString() -> "NotInTest"
        )
      }.flatten
      .toList

    val testUuids = uuids ++: uuids ++: uuids

    //  .take(2)

    // val uuids = Seq(
    //   "gia:FCAC2B2B-05AA-43C7-8584-827547887081" -> "Test",
    //   // "65548a6b-01d9-436c-b29f-120a4f02a23f"     -> "NotInTest",
    //   // "gia:EE2001BE-4A6B-45A7-824F-E4F4B33D9102" -> "TestGroup1"
    // )

    val tstrs = urls.map { case (nm, url) => new MeterTester(nm, url, threads) }

    try {
      val res = Await.result(
        Future.traverse(tstrs) { tstr =>
          tstr.doTests(testUuids)
        },
        scala.concurrent.duration.Duration(10, "minute")
      )

      for(r <- res.sortBy(_.avgDuration)) {
        println(f"${r.tag}%-10s | ${r.avgDuration.toMillis()}%5d ms | ${r.totalClock.toSeconds()}%5d s")
      }
    } catch {
      case _: TimeoutException => println("timed out")
    } finally {
      tstrs.foreach(_.finish())
    }

    // for(tstr <- tstrs) {
    //   val res = tstr.doTest(uuid, exp), Inf)
    //   val res = Await.result(
    //   println(res)
    // }

    // println("Done")

  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)

}
