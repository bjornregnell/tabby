# tabby
* A simple-to-use zero-dependency [Scala 3](https://docs.scala-lang.org/scala3/new-in-scala3.html) library for managing tabular data such as `.csv` and `.tsv`. 
* The [code](https://github.com/bjornregnell/tabby/blob/main/src/main/scala/tabby/Grid.scala) is the doc.
* Use the latest [jar](https://github.com/bjornregnell/tabby/releases).
* Or let `sbt` download tabby: 
```
val tabbyVer = "0.2.2"
libraryDependencies += "tabby" % "tabby" % tabbyVer from 
  s"https://github.com/bjornregnell/tabby/releases/download/v$tabbyVer/tabby_3-$tabbyVer.jar"
```
