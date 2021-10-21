# tabby
* A simple-to-use zero-dependency [Scala 3](https://docs.scala-lang.org/scala3/new-in-scala3.html) library for managing tabular data such as `.csv` and `.tsv`. 
* The [code](https://github.com/bjornregnell/tabby/blob/main/src/main/scala/tabby/Grid.scala) is the doc (less than 300 lines of code).
* Use the latest [jar](https://github.com/bjornregnell/tabby/releases).
* Or let `sbt` download tabby: 
```
val tabbyVer = "0.2.3"
libraryDependencies += "tabby" % "tabby" % tabbyVer from 
  s"https://github.com/bjornregnell/tabby/releases/download/v$tabbyVer/tabby_3-$tabbyVer.jar"
```

# How to build

`sbt package`

# How to publish

`sbt package` and upload jar to releases of this repo


