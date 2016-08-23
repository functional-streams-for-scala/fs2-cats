FS2 Cats: Interoperability between FS2 and Cats
===============================================

[![Build Status](https://travis-ci.org/functional-streams-for-scala/fs2-cats.svg?branch=master)](http://travis-ci.org/functional-streams-for-scala/fs2-cats)
[![Gitter Chat](https://badges.gitter.im/functional-streams-for-scala/fs2.svg)](https://gitter.im/functional-streams-for-scala/fs2)

This library provides an interoperability layer between FS2 and Cats. At this time, the API of this library is two imports:

    import fs2.interop.cats._         // Provides conversions from FS2 to Cats (e.g., FS2 Monad to Cats Monad)
    import fs2.interop.cats.reverse._ // Provides conversions from Cats to FS2 (e.g., Cats Monad to FS2 Monad)

Note: importing both of these in to the same lexical scope may cause issues with ambiguous implicits.

### <a id="getit"></a> Where to get the latest version ###

The 0.1.0 release is coming soon and you can start using the milestone release now.

```scala
// available for Scala 2.11.8 + Cats 0.7.0
libraryDependencies += "co.fs2" %% "fs2-cats" % "0.1.0-RC2"
```

