package domala.async.jdbc

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/** A Database I/O Action that can be executed on a database. The AsyncAction type allows a
  * separation of execution logic and resource usage management logic from composition logic.
  * AsyncAction can be composed with methods such as `flatMap` and `map` and `andThen`.
  *
  * This trait is inspired by [[https://github.com/slick/slick/blob/3.2.1/slick/src/main/scala/slick/dbio/DBIOAction.scala Slick3]].
  */
sealed trait AsyncAction[+R] {
  val config: AsyncConfig
  private[domala] def run: Future[R]

  /** Transform the result of a successful execution of this action. If this action fails, the
    * resulting action also fails. */
  def map[R2](f: R => R2): AsyncAction[R2] = flatMap(r => ResultAction(f(r))(config))

  /** Use the result produced by the successful execution of this action to compute and then
    * run the next action in sequence. The resulting action fails if either this action, the
    * computation, or the computed action fails. */
  def flatMap[R2](f: R => AsyncAction[R2]): AsyncAction[R2] = FlatMapAction(this, f)

  /** Run the side-effecting action to the result of this action, and returns a new action with
    * the result of this action. If either of the two actions fails, the resulting action also
    * fails. */
  def andThen[R2](pf: PartialFunction[Try[R], AsyncAction[R2]]): AsyncAction[R] = AndThenAction(this, pf)

  /** Filter the result of this action with the given predicate. If the predicate matches, the
    * original result is returned, otherwise the resulting action fails with a
    * NoSuchElementException. */
  def filter(p: R => Boolean): AsyncAction[R] =
    withFilter(p)

  def withFilter(f: R => Boolean): AsyncAction[R] = flatMap(v => if(f(v)) ResultAction(v)(config) else throw new NoSuchElementException("Action.withFilter failed"))

  def recover[R2 >: R](pf: PartialFunction[Throwable, AsyncAction[R2]]): AsyncAction[R2] = RecoverAction(this, pf)

}

object AsyncAction {
  def apply[R](thunk: => R)(implicit config: AsyncConfig): AsyncAction[R] = {
    if (config.isTransactionally)
      new LazyAction[R](thunk)(config)
    else
      new FutureAction[R](thunk)(config)
  }

  def unit(implicit config: AsyncConfig) = new UnitAction(config)
}

class UnitAction(val config: AsyncConfig) extends AsyncAction[Unit] {
  override private[domala] def run: Future[Unit] = Future.unit
}

private class FutureAction[+R] private[jdbc] (thunk: => R)(val config: AsyncConfig) extends AsyncAction[R] {
  private[this] val future: Future[R] = {
    Future {
      config.atomicOperation(thunk)
    }(config.executionContext)
  }
  private[domala] def run: Future[R] = future
}

class LazyAction[+R]  private[jdbc] (thunk: => R)(val config: AsyncConfig) extends AsyncAction[R] {
  private[domala] def run: Future[R] = {
    CompletedFuture(thunk)
  }
}

case class ResultAction[+R] private[jdbc] (value: R)(val config: AsyncConfig) extends AsyncAction[R] {
  private[domala] def run: Future[R] = CompletedFuture(value)
}

case class FlatMapAction[+R2, R] private[jdbc] (base: AsyncAction[R], f: R => AsyncAction[R2]) extends AsyncAction[R2] {
  val config: AsyncConfig = base.config
  implicit val executionContext: ExecutionContext = config.executionContext
  private[domala] def run: Future[R2] = base.run.flatMap {
    result => f(result).run.transform {
      case success: Success[R] => success
      case Failure(t) => throw t
    }
  }
}

case class AndThenAction[+R2, R] private[jdbc] (base: AsyncAction[R], pf: PartialFunction[Try[R], AsyncAction[R2]]) extends AsyncAction[R] {
  val config: AsyncConfig = base.config
  implicit val executionContext: ExecutionContext = config.executionContext
  private[domala] def run: Future[R] = {
    base.run.transformWith {
      result =>
        pf(result).run.transform {
          case Success(_) => result
          case Failure(t) => throw t
        }
    }
  }
}

case class RecoverAction[+R2 >: R, R] private[jdbc] (base: AsyncAction[R], pf: PartialFunction[Throwable, AsyncAction[R2]]) extends AsyncAction[R2] {
  val config: AsyncConfig = base.config
  implicit val executionContext: ExecutionContext = config.executionContext
  private[domala] def run: Future[R2] = {
    val future = base.run
    future.transformWith {
      case Success(_) => future
      case Failure(t) => pf.apply(t).run
    }
  }
}

