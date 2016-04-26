package fs2
package interop

import _root_.cats.Monoid

package object cats extends Instances {

  object reverse extends ReverseInstances

  implicit class StreamCatsOps[F[_], A](val self: Stream[F, A]) extends AnyVal {

    def foldMap[B](f: A => B)(implicit M: Monoid[B]): Stream[F, B] =
      self.fold(M.empty)((b,a) => M.combine(b, f(a)))

    def foldMonoid(implicit M: Monoid[A]): Stream[F, A] =
      self.fold(M.empty)(M.combine(_, _))

  }

}
