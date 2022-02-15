import ujson._

// takes input as a list of browserIds creates a json file that can be
// passed to fastly command line to add to a dictionary

var input = scala.io.Source.fromFile("browserIds.csv").getLines.take(999)

var items = ujson.Arr.from(
  input.map { id =>
    ujson.Obj(
      "op" -> "upsert",
      "item_key" -> id,
      "item_value" -> "test-group-1"
    )
  }
)

val res = ujson.Obj("items" -> items)

println(res.toString)
