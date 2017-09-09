package fs2.interop.cats

import fs2.util._
import _root_.cats.{ Functor => CatsFunctor, Monad => CatsMonad, MonadError }
import _root_.cats.arrow.FunctionK
import _root_.cats.data.Kleisli

trait Instances extends Instances0 {
  implicit def effectToMonadError[F[_]](implicit F: Effect[F]): MonadError[F, Throwable] = new MonadError[F, Throwable] {
    def pure[A](a: A) = F.pure(a)
    override def map[A, B](fa: F[A])(f: A => B) = F.map(fa)(f)
    def flatMap[A, B](fa: F[A])(f: A => F[B]) = F.flatMap(fa)(f)
    def tailRecM[A, B](a: A)(f: A => F[Either[A,B]]): F[B] = defaultTailRecM(a)(f)
    def raiseError[A](t: Throwable) = F.fail(t)
    def handleErrorWith[A](fa: F[A])(f: Throwable => F[A]) = F.flatMap(F.attempt(fa))(e => e.fold(f, pure))
  }

  implicit def uf1ToFunctionK[F[_], G[_]](uf1: UF1[F, G]): FunctionK[F, G] = new FunctionK[F, G] {
    def apply[A](fa: F[A]) = uf1(fa)
  }

  implicit def kleisliSuspendableInstance[F[_], E](implicit F: Suspendable[F]): Suspendable[Kleisli[F, E, ?]] = new Suspendable[Kleisli[F, E, ?]] {
    def pure[A](a: A): Kleisli[F, E, A] = Kleisli.pure[F, E, A](a)
    override def map[A, B](fa: Kleisli[F, E, A])(f: A => B): Kleisli[F, E, B] = fa.map(f)
    def flatMap[A, B](fa: Kleisli[F, E, A])(f: A => Kleisli[F, E, B]): Kleisli[F, E, B] = fa.flatMap(f)
    def suspend[A](fa: => Kleisli[F, E, A]): Kleisli[F, E, A] = Kleisli(e => F.suspend(fa.run(e)))
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

  implicit def kleisliCatchableInstance[F[_], E](implicit F: Catchable[F]): Catchable[Kleisli[F, E, ?]] = new Catchable[Kleisli[F, E, ?]] {
    def pure[A](a: A): Kleisli[F, E, A] = Kleisli.pure[F, E, A](a)
    override def map[A, B](fa: Kleisli[F, E, A])(f: A => B): Kleisli[F, E, B] = fa.map(f)
    def flatMap[A, B](fa: Kleisli[F, E, A])(f: A => Kleisli[F, E, B]): Kleisli[F, E, B] = fa.flatMap(f)
    def attempt[A](fa: Kleisli[F, E, A]): Kleisli[F, E, Attempt[A]] = Kleisli(e => F.attempt(fa.run(e)))
    def fail[A](t: Throwable): Kleisli[F, E, A] = Kleisli(e => F.fail(t))
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

  protected def defaultTailRecM[F[_], A, B](a: A)(f: A => F[Either[A,B]])
                                                 (implicit F: Monad[F]): F[B] =
    F.flatMap(f(a)) {
      case Left(a2) => defaultTailRecM(a2)(f)
      case Right(b) => F.pure(b)
    }
}

