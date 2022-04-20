# tabby
* A simple-to-use zero-dependency [Scala 3](https://docs.scala-lang.org/scala3/new-in-scala3.html) library for managing tabular data such as `.csv` and `.tsv`. 
* The [code](https://github.com/bjornregnell/tabby/blob/main/src/main/scala/tabby/Grid.scala) is the doc (less than 300 lines of code).

# How to use tabby

Use tabby with [scala-cli](https://scala-cli.virtuslab.org/install) by adding this magic comment in the beginning of your scala file (note the `>` after `//`):
```
//> using lib "tabby:tabby:0.2.3,url=https://github.com/bjornregnell/tabby/releases/download/v0.2.3/tabby_3-0.2.3.jar"
//> using scala "3"

import tabby.*

@main def run = println(Grid("a", "b", "c")(1, 2, 3))
```

Or use the [`sbt` build tool](https://www.scala-sbt.org/download.html) with this in your `build.sbt`: 
```
val tabbyVer = "0.2.3"
libraryDependencies += "tabby" % "tabby" % tabbyVer from 
  s"https://github.com/bjornregnell/tabby/releases/download/v$tabbyVer/tabby_3-$tabbyVer.jar"
```

* The Scala version in your `build.sbt` should be `scalaVersion := "3.0.2"` or higher

* The sbt version in your in your `project/build.properties` should be `sbt.version=1.5.5` or higher  

Or download the [jar](https://github.com/bjornregnell/tabby/releases) and put it on your classpath manually.



# How to build

`sbt package`

# How to publish

`sbt package` and upload jar to releases of this repo


