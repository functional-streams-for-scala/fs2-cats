package fs2.interop.cats

import fs2.util._
import _root_.cats.{ Functor => CatsFunctor, Monad => CatsMonad, MonadError }
import _root_.cats.arrow.FunctionK

trait ReverseInstances extends ReverseInstances0 {

  implicit def monadErrorToCatchable[F[_]](implicit F: MonadError[F, Throwable]): Catchable[F] = new Catchable[F] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.flatMap(fa)(f)
    def fail[A](t: Throwable) = F.raiseError(t)
    def attempt[A](fa: F[A]) = F.handleErrorWith(F.map(fa)(a => Right(a): Either[Throwable, A]))(t => pure(Left(t)))
  }

  implicit def functionKToUf1[F[_], G[_]](fk: FunctionK[F, G]): UF1[F, G] = new UF1[F, G] {
    def apply[A](fa: F[A]) = fk(fa)
  }
}

private[cats] trait ReverseInstances0 extends ReverseInstances1 {

  implicit def catsToMonad[F[_]](implicit F: CatsMonad[F]): Monad[F] = new Monad[F] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.flatMap(fa)(f)
  }
}

private[cats] trait ReverseInstances1 {

  implicit def catsToFunctor[F[_]](implicit F: CatsFunctor[F]): Functor[F] = new Functor[F] {
    def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
  }
}

