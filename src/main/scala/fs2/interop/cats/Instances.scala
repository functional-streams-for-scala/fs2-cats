package fs2.interop.cats

import fs2.util._
import fs2.async.immutable.Signal
import fs2.async.mutable.Queue
import _root_.cats.{ Eval, Functor => CatsFunctor, Monad => CatsMonad, MonadError }
import _root_.cats.arrow.NaturalTransformation
import _root_.cats.functor.Invariant

trait Instances extends Instances0 {
  implicit def effectToMonadError[F[_]](implicit F: Effect[F]): MonadError[F, Throwable] = new MonadError[F, Throwable] {
    def pure[A](a: A) = F.pure(a)
    override def pureEval[A](a: Eval[A]) = F.delay(a.value)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.bind(fa)(f)
    def raiseError[A](t: Throwable) = F.fail(t)
    def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]) = F.bind(F.attempt(fa))(e => e.fold(f, pure))
  }

  implicit def uf1ToNatrualTransformation[F[_], G[_]](implicit uf1: UF1[F, G]): NaturalTransformation[F, G] = new NaturalTransformation[F, G] {
    def apply[A](fa: F[A]) = uf1(fa)
  }

  implicit def catsInvariantQueue[F[_]](implicit F: Functor[F]): Invariant[Queue[F, ?]] =
    new Invariant[Queue[F, ?]] {
      def imap[A, B](fa: Queue[F, A])(f: A => B)(g: B => A): Queue[F, B] =
        new Queue[F, B] {
          def available: Signal[F, Int] = fa.available
          def dequeue1: F[B]            = F.map(fa.dequeue1)(f)
          def enqueue1(a: B): F[Unit]   = fa.enqueue1(g(a))
          def full: Signal[F, Boolean]  = fa.full
          def offer1(a: B): F[Boolean]  = fa.offer1(g(a))
          def size: Signal[F, Int]      = fa.size
          def upperBound: Option[Int]   = fa.upperBound
        }
    }
}

private[cats] trait Instances0 extends Instances1 {
  implicit def catchableToMonadError[F[_]](implicit F: Catchable[F]): MonadError[F, Throwable] = new MonadError[F, Throwable] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.bind(fa)(f)
    def raiseError[A](t: Throwable) = F.fail(t)
    def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]) = F.bind(F.attempt(fa))(e => e.fold(f, pure))
  }
}

private[cats] trait Instances1 extends Instances2 {
  implicit def monadToCats[F[_]](implicit F: Monad[F]): CatsMonad[F] = new CatsMonad[F] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.bind(fa)(f)
  }
}

private[cats] trait Instances2 {
  implicit def functorToCats[F[_]](implicit F: Functor[F]): CatsFunctor[F] = new CatsFunctor[F] {
    def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
  }
}
