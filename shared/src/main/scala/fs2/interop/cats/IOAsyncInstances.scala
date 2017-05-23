package fs2
package interop.cats

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import fs2.internal.{Actor, LinkedMap}
import fs2.util.{Async, Attempt, Effect, Free}

import _root_.cats.effect.IO

import scala.concurrent.ExecutionContext

// mostly cribbed from fs2-scalaz:TaskAsyncInstances.scala
trait IOAsyncInstances {
  import IOAsyncInstances._

  protected class EffectIO extends Effect[IO] {
    def pure[A](a: A) = IO.pure(a)
    def flatMap[A,B](a: IO[A])(f: A => IO[B]): IO[B] = a flatMap f
    override def delay[A](a: => A) = IO(a)
    def suspend[A](fa: => IO[A]) = IO.suspend(fa)
    def fail[A](err: Throwable) = IO.raiseError(err)
    def attempt[A](t: IO[A]) = t.attempt
    def unsafeRunAsync[A](t: IO[A])(cb: Attempt[A] => Unit): Unit = t.unsafeRunAsync(cb)
    override def toString = "Effect[IO]"
  }

  implicit def asyncInstance(implicit ec: ExecutionContext): Async[IO] = new EffectIO with Async[IO] {
    def ref[A]: IO[Async.Ref[IO, A]] = CatsIO.ref[A](ec)
    override def toString = "Async[IO]"
  }

  /*
   * Implementation is taken from `fs2` library, with only minor changes. See:
   *
   * https://github.com/functional-streams-for-scala/fs2/blob/v0.9.0-M2/core/src/main/scala/fs2/util/IO.scala
   *
   * Copyright (c) 2013 Paul Chiusano, and respective contributors
   *
   * and is licensed MIT, see LICENSE file at:
   *
   * https://github.com/functional-streams-for-scala/fs2/blob/series/0.9/LICENSE
   */
  private[fs2] object CatsIO {
    private type Callback[A] = Either[Throwable, A] => Unit

    private trait MsgId
    private trait Msg[A]
    private object Msg {
      case class Read[A](cb: Callback[(A, Long)], id: MsgId) extends Msg[A]
      case class Nevermind[A](id: MsgId, cb: Callback[Boolean]) extends Msg[A]
      case class Set[A](r: Either[Throwable, A]) extends Msg[A]
      case class TrySet[A](id: Long, r: Either[Throwable, A],
        cb: Callback[Boolean]) extends Msg[A]
    }

    def ref[A](implicit ec: ExecutionContext): IO[Ref[A]] = IO {
      implicit val S = Strategy.fromExecutionContext(ec)

      var result: Either[Throwable, A] = null
      // any waiting calls to `access` before first `set`
      var waiting: LinkedMap[MsgId, Callback[(A, Long)]] = LinkedMap.empty
      // id which increases with each `set` or successful `modify`
      var nonce: Long = 0

      lazy val actor: Actor[Msg[A]] = Actor.actor[Msg[A]] {
        case Msg.Read(cb, idf) =>
          if (result eq null) waiting = waiting.updated(idf, cb)
          else { val r = result; val id = nonce; ec { cb(r.right.map((_,id))) }; () }

        case Msg.Set(r) =>
          nonce += 1L
          if (result eq null) {
            val id = nonce
            waiting.values.foreach(cb => ec { cb(r.right.map((_,id))) })
            waiting = LinkedMap.empty
          }
          result = r

        case Msg.TrySet(id, r, cb) =>
          if (id == nonce) {
            nonce += 1L; val id2 = nonce
            waiting.values.foreach(cb => ec { cb(r.right.map((_,id2))) })
            waiting = LinkedMap.empty
            result = r
            cb(Right(true))
          }
          else cb(Right(false))

        case Msg.Nevermind(id, cb) =>
          val interrupted = waiting.get(id).isDefined
          waiting = waiting - id
          val _ = ec { cb (Right(interrupted)) }
      }

      new Ref(actor)
    }

    class Ref[A] private[fs2](actor: Actor[Msg[A]])(implicit ec: ExecutionContext, protected val F: Async[IO]) extends Async.Ref[IO,A] {

      def access: IO[(A, Either[Throwable,A] => IO[Boolean])] =
        IO(new MsgId {}).flatMap { mid =>
          getStamped(mid).map { case (a, id) =>
            val set = (a: Either[Throwable,A]) =>
              IO.async[Boolean] { cb => actor ! Msg.TrySet(id, a, cb) }
            (a, set)
          }
        }

      /**
       * Return a `IO` that submits `t` to this ref for evaluation.
       * When it completes it overwrites any previously `put` value.
       */
      def set(t: IO[A]): IO[Unit] =
        IO { ec { t.unsafeRunAsync { r => actor ! Msg.Set(r) } }; () }
      def setFree(t: Free[IO,A]): IO[Unit] =
        set(t.run(F))
      def runSet(e: Either[Throwable,A]): Unit =
        actor ! Msg.Set(e)

      private def getStamped(msg: MsgId): IO[(A,Long)] =
        IO.async[(A,Long)] { cb => actor ! Msg.Read(cb, msg) }

      /** Return the most recently completed `set`, or block until a `set` value is available. */
      override def get: IO[A] = IO(new MsgId {}).flatMap { mid => getStamped(mid).map(_._1) }

      /** Like `get`, but returns a `IO[Unit]` that can be used cancel the subscription. */
      def cancellableGet: IO[(IO[A], IO[Unit])] = IO {
        val id = new MsgId {}
        val get = getStamped(id).map(_._1)
        val cancel = IO.async[Unit] {
          cb => actor ! Msg.Nevermind(id, r => cb(r.right.map(_ => ())))
        }
        (get, cancel)
      }

      /**
       * Runs `t1` and `t2` simultaneously, but only the winner gets to
       * `set` to this `ref`. The loser continues running but its reference
       * to this ref is severed, allowing this ref to be garbage collected
       * if it is no longer referenced by anyone other than the loser.
       */
      def setRace(t1: IO[A], t2: IO[A]): IO[Unit] = IO {
        val ref = new AtomicReference(actor)
        val won = new AtomicBoolean(false)
        val win = (res: Either[Throwable, A]) => {
          // important for GC: we don't reference this ref
          // or the actor directly, and the winner destroys any
          // references behind it!
          if (won.compareAndSet(false, true)) {
            val actor = ref.get
            ref.set(null)
            actor ! Msg.Set(res)
          }
        }
        t1.shift.unsafeRunAsync(win)
        t2.shift.unsafeRunAsync(win)
      }
    }

  }
}

private[fs2] object IOAsyncInstances {
  private implicit final class ECSyntax(val ec: ExecutionContext) extends AnyVal {
    def apply[A](thunk: => A): Unit =
      ec.execute(new Runnable { def run() = { thunk; () } })
  }
}
