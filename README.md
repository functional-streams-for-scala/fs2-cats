FS2 Cats: Interoperability between FS2 and Cats
===============================================

**As of FS2 1.0, this project is no longer necessary, as cats support is built in to FS2.**

[![Build Status](https://travis-ci.org/functional-streams-for-scala/fs2-cats.svg?branch=master)](http://travis-ci.org/functional-streams-for-scala/fs2-cats)
[![Gitter Chat](https://badges.gitter.im/functional-streams-for-scala/fs2.svg)](https://gitter.im/functional-streams-for-scala/fs2)

This library provides an interoperability layer between FS2 0.9 and Cats. At this time, the API of this library is two imports:

    import fs2.interop.cats._         // Provides conversions from FS2 to Cats (e.g., FS2 Monad to Cats Monad)
    import fs2.interop.cats.reverse._ // Provides conversions from Cats to FS2 (e.g., Cats Monad to FS2 Monad)

Note: importing both of these in to the same lexical scope may cause issues with ambiguous implicits.

Important: FS2 0.10+ has a direct dependency on Cats and Cats Effect so this library is NOT needed when using 0.10+.

### <a id="getit"></a> Where to get the latest version ###

```scala
// Available for Scala 2.11.11 / 2.12.4 + Cats 1.0.0-RC1 + Cats Effect 0.5 + FS2 0.9
libraryDependencies += "co.fs2" %% "fs2-cats" % "0.5.0"

// Available for Scala 2.11.11 / 2.12.3 + Cats 1.0.0-MF + Cats Effect 0.4 + FS2 0.9
libraryDependencies += "co.fs2" %% "fs2-cats" % "0.4.0"

// Available for Scala 2.11.8 / 2.12.1 + Cats 0.9.0 + FS2 0.9
libraryDependencies += "co.fs2" %% "fs2-cats" % "0.3.0"

// Available for Scala 2.11.8 / 2.12.0 + Cats 0.8.1 + FS2 0.9
libraryDependencies += "co.fs2" %% "fs2-cats" % "0.2.0"
```

