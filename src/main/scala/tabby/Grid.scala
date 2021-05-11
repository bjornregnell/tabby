package tabby

object Grid:
  type Row = Vector[String]
  type Col = Vector[String]
  type Matrix = Vector[Row]
  type RowMap = Map[String, String]
  
  val defaultDelim = '\t'
  val defaultEnc = "UTF-8"
  val defaultHeadingShowSep = '-'
  val defaultColShowSep = '|'
  
  extension (text: String)
    def toFile(file: String, enc: String = defaultEnc): Unit = 
      val path = java.nio.file.Paths.get(file)
      val _ = java.nio.file.Files.write(path, text.getBytes(enc))
    
  def fromLines(lines: Vector[String], delim: Char = defaultDelim) =
    val headings = lines(0).split(delim).toVector.map(_.trim.toLowerCase)
    val data = lines.drop(1).map(_.split(delim).toVector
      .take(headings.length).padTo(headings.length,""))
    Grid(headings, data)

  object Lines:  
    def fromFile(file: String, enc: String = defaultEnc): Vector[String] =
      val source = scala.io.Source.fromFile(file, enc)
      try source.getLines.toVector finally source.close()

  def fromFile(
    file: String, 
    delim: Char = defaultDelim, 
    enc: String = defaultEnc
  ): Grid = fromLines(Lines.fromFile(file, enc), delim)
  
case class Grid(headings: Grid.Row, data: Grid.Matrix):
  import Grid._
  
  assert(headings == headings.distinct, s"headings contains duplicates: $headings")
  assert(data.map(_.size).forall(_ == headings.size), s"all rows must match headings size")

  lazy val dim = (data.size, headings.size)
  lazy val (nRows, nCols) = dim

  lazy val indexOf: Map[String, Int] = headings.zipWithIndex.toMap
  private def mkRowMap(row: Int): RowMap = headings.map(h => (h, apply(row)(h))).toMap
  lazy val rowMap: Vector[RowMap] = data.indices.toVector.map(mkRowMap)

  def apply(row: Int)(colName: String): String = data(row)(indexOf(colName))
  def apply(colName: String): Col = data.map(row => row(indexOf(colName)))
  def apply(row: Int, col: Int): String = data(row)(col)

  def get(row: Int)(colName: String): Option[String] = data.lift(row).flatMap(_.lift(indexOf(colName)))
  def get(colName: String): Option[Vector[String]] = scala.util.Try(apply(colName)).toOption
  def get(row: Int, col: Int): Option[String] = data.lift(row).flatMap(_.lift(col))

  def updated(row: Int, colName: String, newValue: String): Grid =
    copy(data = data.updated(row, data(row).updated(indexOf(colName), newValue)))

  def isDistinct(colNames: String*): Boolean =
    colNames.forall{ c => val values = apply(c); values.distinct == values }

  def toNestedMap(keyColName: String)(valueColNames: String*): Map[String, RowMap] =
    data.indices.map { r =>
      val key = apply(r)(keyColName)
      val mapOfValues: RowMap = {
        val values = valueColNames.map(c => apply(r)(c))
        valueColNames.zip(values).toMap
      }
      key -> mapOfValues
    }.toMap

  def filter(colName: String)(p: String => Boolean): Grid =
    copy(data = data.filter(row => p(row(indexOf(colName)))))

  def lookUp(colName: String)(rowValueInCol: String): Grid = 
    filter(colName)(_ == rowValueInCol)

  def filterRow(p: RowMap => Boolean): Grid =
      copy(data = (for (i <- data.indices if p(rowMap(i))) yield data(i)).toVector)

  def sorted(colName: String = headings(0)): Grid =
    copy(data = data.sortBy(row => row.lift(indexOf(colName)).getOrElse("")))

  def sortBy[T: Ordering](f: Row => T): Grid = copy(data = data.sortBy(f))

  def mapCol(colName: String)(f: RowMap => String): Grid =
    Grid(headings, data.indices.map(r => data(r).updated(indexOf(colName), f(rowMap(r)))).toVector)

  def sumIntCol(colName: String): Int = 
    apply(colName).map(_.toIntOption.getOrElse(0)).sum
  
  def sumDoubleCol(colName: String): Double = 
    apply(colName).map(_.toDoubleOption.getOrElse(0.0)).sum

  def addCol(colName: String)(f: RowMap => String): Grid =
    Grid(headings :+ colName, data.indices.map(i => data(i) :+ f(rowMap(i))).toVector)

  def addCol(colName: String, col: Col): Grid =
    Grid(headings :+ colName, data.indices.map(i => data(i) :+ col(i)).toVector)

  def keep(colNames: String*): Grid = {
    val heads = colNames.toVector
    Grid(heads, data.indices.map(i => heads.map(c => apply(i)(c))).toVector)
  }

  def skip(colNames: String*): Grid = {
    val heads = headings diff colNames
    Grid(heads, data.indices.map(i => heads.map(c => apply(i)(c))).toVector)
  }

  def join(colNameInThis: String, colNameInThat: String)(that: Grid): Grid = {
    val extraHeadings = (that.headings diff Seq(colNameInThat)) diff headings
    val thatMap = that.toNestedMap(colNameInThat)(extraHeadings:_*)
    var result = this
    extraHeadings.foreach { colName =>
      result = result.addCol(colName) { rm =>
        val key = rm(colNameInThis)
        thatMap(key)(colName)
      }
    }
    result
  }

  def appendIntersecting(that: Grid): Grid = 
    val common = headings intersect that.headings
    Grid(headings = common, data = keep(common:_*).data ++ that.keep(common:_*).data)
 
  def find(p: RowMap => Boolean): Option[RowMap] = data.indices.map(rowMap).find(p)

  def rename(colNamePairs: (String,String)*): Grid = 
    val renameMap = colNamePairs.toMap
    copy(headings = headings.map(h => renameMap.getOrElse(h, h)))

  def values: Map[String, Col] = headings.map(h => (h, apply(h).distinct)).toMap

  def replaceBy(colName: String)(f: String => String): Grid = 
    val i = indexOf(colName)
    copy(data=data.map(r => r.updated(i, f(r(i)))))

  def trim: Grid = copy(data = data.map(r => r.map(_.trim)))

  lazy val maxLengths: Vector[Int] = headings.map(h => (apply(h).map(_.size) :+ h.size).max)

  lazy val maxLengthOf: Map[String, Int] = (headings zip maxLengths).toMap

  lazy val showHeadings: String =
    headings.map(h => h + (" " * (maxLengthOf(h) - h.size))).mkString("|")

  lazy val hline: String = (defaultHeadingShowSep.toString * showHeadings.size) + "\n"

  def padToMax(row: Int, pad: String = " ")(colName: String): String =
    apply(row)(colName) + (pad * (maxLengthOf(colName) - apply(row)(colName).size))

  lazy val showData: String =
    data.indices.map(r => headings.map(h =>
      padToMax(r)(h)).mkString(defaultColShowSep.toString)).toVector.mkString("\n")

  lazy val show: String =
    s"$hline$showHeadings\n$hline$showData"//\n$hline  dim = (nRows, nCols) = ($nRows, $nCols)"
    
  /** Pretty-printing suitable for REPL **/
  def pp: Unit = println(show)

  /** Trimmed pretty-printing suitable for REPL **/
  def pp(maxWidth: Int): Unit =
    println(show.split('\n').map(_.take(maxWidth)).mkString("\n"))

  def toText(delim: Char = defaultDelim): String =
    headings.mkString("",delim.toString,"\n") +
      data.map(_.mkString("", delim.toString, "\n")).mkString

  def toFile(file: String, delim: Char = defaultDelim, enc: String = defaultEnc): Unit = 
    toText(delim).toFile(file, enc)




