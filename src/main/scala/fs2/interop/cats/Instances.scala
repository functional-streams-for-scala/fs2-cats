package fs2.interop.cats

import fs2.util._
import _root_.cats.{ Eval, Functor => CatsFunctor, Monad => CatsMonad, MonadError }
import _root_.cats.arrow.NaturalTransformation

trait Instances extends Instances0 {
  implicit def effectToMonadError[F[_]](implicit F: Effect[F]): MonadError[F, Throwable] = new MonadError[F, Throwable] {
    def pure[A](a: A) = F.pure(a)
    override def pureEval[A](a: Eval[A]) = F.delay(a.value)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.bind(fa)(f)
    def raiseError[A](t: Throwable) = F.fail(t)
    def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]) = F.bind(F.attempt(fa))(e => e.fold(f, pure))
  }

  implicit def uf1ToNaturalTransformation[F[_], G[_]](implicit uf1: UF1[F, G]): NaturalTransformation[F, G] = new NaturalTransformation[F, G] {
    def apply[A](fa: F[A]) = uf1(fa)
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

