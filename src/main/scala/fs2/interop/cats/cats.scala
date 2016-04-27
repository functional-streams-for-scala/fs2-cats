package fs2
package interop

import _root_.cats.Applicative
import _root_.cats.Monoid
import _root_.cats.Semigroup

import _root_.cats.std.map.mapMonoid

import fs2.util.Free

package object cats extends Instances {

  object reverse extends ReverseInstances

  implicit class StreamCatsOps[F[_], A](val self: Stream[F, A]) extends AnyVal {

    def foldMap[B](f: A => B)(implicit M: Monoid[B]): Stream[F, B] =
      self.fold(M.empty)((b, a) => M.combine(b, f(a)))

    def foldMonoid(implicit M: Monoid[A]): Stream[F, A] =
      self.fold(M.empty)(M.combine(_, _))

    def foldSemigroup(implicit S: Semigroup[A]): Stream[F, A] =
      self.reduce(S.combine(_, _))

    def runFoldMap[B](f: A => B)(implicit M: Monoid[B]): Free[F, B] =
      self.runFold(M.empty)((b, a) => M.combine(b, f(a)))

    def runGroupByFoldMap[K, B: Monoid](f: A => K)(g: A => B): Free[F, Map[K, B]] =
      runFoldMap(a => Map(f(a) -> g(a)))

    def runGroupByFoldMonoid[K](f: A => K)(implicit M: Monoid[A]): Free[F, Map[K, A]] =
      runFoldMap(a => Map(f(a) -> a))

    def runGroupBy[X[_], K](f: A => K)(implicit A: Applicative[X], M: Monoid[X[A]]): Free[F, Map[K, X[A]]] =
      runGroupByFoldMap(f)(A.pure)

  }

}

