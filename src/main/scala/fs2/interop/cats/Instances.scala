package fs2.interop.cats

import fs2.util._
import _root_.cats.{ Functor => CatsFunctor, Monad => CatsMonad, MonadError }
import _root_.cats.arrow.FunctionK

trait Instances extends Instances0 {
  implicit def effectToMonadError[F[_]](implicit F: Effect[F]): MonadError[F, Throwable] = new MonadError[F, Throwable] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.flatMap(fa)(f)
    def tailRecM[A, B](a: A)(f: A => F[Either[A,B]]): F[B] = defaultTailRecM(a)(f)
    def raiseError[A](t: Throwable) = F.fail(t)
    def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]) = F.flatMap(F.attempt(fa))(e => e.fold(f, pure))
  }

  implicit def uf1ToFunctionK[F[_], G[_]](implicit uf1: UF1[F, G]): FunctionK[F, G] = new FunctionK[F, G] {
    def apply[A](fa: F[A]) = uf1(fa)
  }
}

private[cats] trait Instances0 extends Instances1 {
  implicit def catchableToMonadError[F[_]](implicit F: Catchable[F]): MonadError[F, Throwable] = new MonadError[F, Throwable] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.flatMap(fa)(f)
    def tailRecM[A, B](a: A)(f: A => F[Either[A,B]]): F[B] = defaultTailRecM(a)(f)
    def raiseError[A](t: Throwable) = F.fail(t)
    def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]) = F.flatMap(F.attempt(fa))(e => e.fold(f, pure))
  }
}

private[cats] trait Instances1 extends Instances2 {
  implicit def monadToCats[F[_]](implicit F: Monad[F]): CatsMonad[F] = new CatsMonad[F] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.flatMap(fa)(f)
    def tailRecM[A, B](a: A)(f: A => F[Either[A,B]]): F[B] = defaultTailRecM(a)(f)
  }
}

private[cats] trait Instances2 {

  implicit def functorToCats[F[_]](implicit F: Functor[F]): CatsFunctor[F] = new CatsFunctor[F] {
    def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
  }
}

