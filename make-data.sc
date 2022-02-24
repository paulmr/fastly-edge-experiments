// mode: -*- scala -*-

import ujson._

// takes input as a list of browserIds creates a json file that can be
// passed to fastly command line to add to a dictionary

def makeDataJson(input: Seq[String]) = {
  var items = input.map { id =>
    ujson.Obj(
      "op" -> "upsert",
      "item_key" -> id,
      "item_value" -> "test-group-1"
    )
  }

  ujson.Obj("items" -> items).toString
}

def makeLocalDict(input: Seq[String]) =
  ujson.Obj.from(
    input.map(id => id -> "Test")
  ).toString

def makeFile(fname: String, data: String): Unit = {
  val f = new java.io.PrintWriter(fname)
  try {
    f.print(data)
  } finally {
    f.close()
  }
  println(s"Created file: $fname")
}

@main
def main(): Unit = {
  var input = scala.io.Source.fromFile("browserIds.csv")
    .getLines
    .take(999)
    .toSeq
  makeFile("data.json",       makeDataJson(input))
  makeFile("local_dict.json", makeLocalDict(input))
}
