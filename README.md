FS2 Cats: Interoperability between FS2 and Cats
===============================================

[![Build Status](https://travis-ci.org/functional-streams-for-scala/fs2.svg?branch=master)](http://travis-ci.org/functional-streams-for-scala/fs2-cats)
[![Gitter Chat](https://badges.gitter.im/functional-streams-for-scala/fs2.svg)](https://gitter.im/functional-streams-for-scala/fs2)

This library provides an interoperability layer between FS2 and Cats. At this time, the API of this library is a single import:

    import fs2.interop.cats._

This provides implicit conversions between the type classes in `fs2.util` and Cats.
