package fs2.interop.cats

import fs2.util._
import _root_.cats.{ Functor => CatsFunctor, Monad => CatsMonad, MonadError }
import _root_.cats.arrow.NaturalTransformation

trait Instances extends Instances0 {

  implicit def catchableToMonadError[F[_]](implicit F: Catchable[F]): MonadError[F, Throwable] = new MonadError[F, Throwable] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.bind(fa)(f)
    def raiseError[A](t: Throwable) = F.fail(t)
    def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]) = F.bind(F.attempt(fa))(e => e.fold(f, pure))
  }

  implicit def monadErrorToCatchable[F[_]](implicit F: MonadError[F, Throwable]): Catchable[F] = new Catchable[F] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def bind[A, B](fa: F[A])(f: A => F[B]) = F.flatMap(fa)(f)
    def fail[A](t: Throwable) = F.raiseError(t)
    def attempt[A](fa: F[A]) = F.handleErrorWith(F.map(fa)(a => Right(a): Either[Throwable, A]))(t => pure(Left(t)))
  }

  implicit def uf1ToNatrualTransformation[F[_], G[_]](implicit uf1: UF1[F, G]): NaturalTransformation[F, G] = new NaturalTransformation[F, G] {
    def apply[A](fa: F[A]) = uf1(fa)
  }

  implicit def naturalTransformationToUf1[F[_], G[_]](implicit nt: NaturalTransformation[F, G]): UF1[F, G] = new UF1[F, G] {
    def apply[A](fa: F[A]) = nt(fa)
  }
}

private[cats] trait Instances0 extends Instances1 {

  implicit def monadToCats[F[_]](implicit F: Monad[F]): CatsMonad[F] = new CatsMonad[F] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.bind(fa)(f)
  }

  implicit def catsToMonad[F[_]](implicit F: CatsMonad[F]): Monad[F] = new Monad[F] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def bind[A, B](fa: F[A])(f: A => F[B]) = F.flatMap(fa)(f)
  }
}

private[cats] trait Instances1 {

  implicit def functorToCats[F[_]](implicit F: Functor[F]): CatsFunctor[F] = new CatsFunctor[F] {
    def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
  }

  implicit def catsToFunctor[F[_]](implicit F: CatsFunctor[F]): Functor[F] = new Functor[F] {
    def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
  }
}
