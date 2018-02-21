package domala.async

import domala.message.Message
import org.seasar.doma.DomaException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/** A Database I/O Action that can be executed on a database. The AsyncAction type allows a
  * separation of execution logic and resource usage management logic from composition logic.
  * AsyncAction can be composed with methods such as `flatMap` and `map` and `andThen`.
  *
  * This trait is inspired by [[https://github.com/slick/slick/blob/3.2.1/slick/src/main/scala/slick/dbio/DBIOAction.scala Slick3]].
  */
sealed trait AsyncAction[+R] {
  val context: AsyncContext
  private[domala] def run: Future[R]

  /** Transform the result of a successful execution of this action. If this action fails, the
    * resulting action also fails. */
  def map[R2](f: R => R2): AsyncAction[R2] = flatMap(r => ResultAction(f(r))(context))

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

  def withFilter(f: R => Boolean): AsyncAction[R] = flatMap(v => if(f(v)) ResultAction(v)(context) else throw new NoSuchElementException("Action.withFilter failed"))

  def recover[R2 >: R](pf: PartialFunction[Throwable, AsyncAction[R2]]): AsyncAction[R2] = RecoverAction(this, pf)

}

object AsyncAction {
  def apply[R](thunk: => R)(implicit context: AsyncContext): AsyncAction[R] = {
    context.asyncStatus match {
      case AsyncStatus.Async =>
        new FutureAction[R](thunk)(context)
      case AsyncStatus.Transactional =>
        new LazyAction[R](thunk)(context)
      case _ =>
        throw new DomaException(Message.DOMALA6027)
    }
  }

  def unit(implicit context: AsyncContext) = new UnitAction(context)
}

class UnitAction(val context: AsyncContext) extends AsyncAction[Unit] {
  override private[domala] def run: Future[Unit] = Future.unit
}

private class FutureAction[+R] private[async] (thunk: => R)(val context: AsyncContext) extends AsyncAction[R] {
  private[this] val future: Future[R] = {
    Future {
      context.withAsyncStatus(AsyncStatus.Async) {
        context.atomicOperation(thunk)
      }
    }(context.executionContext)
  }
  private[domala] def run: Future[R] = future
}

class LazyAction[+R]  private[async] (thunk: => R)(val context: AsyncContext) extends AsyncAction[R] {
  private[domala] def run: Future[R] = {
    CompletedFuture(thunk)
  }
}

case class ResultAction[+R] private[async] (value: R)(val context: AsyncContext) extends AsyncAction[R] {
  private[domala] def run: Future[R] = CompletedFuture(value)
}

case class FlatMapAction[+R2, R] private[async] (base: AsyncAction[R], f: R => AsyncAction[R2]) extends AsyncAction[R2] {
  val context: AsyncContext = base.context
  implicit private[this] val executionContext: ExecutionContext = context.executionContext
  private[this] val asyncStatus = context.asyncStatus
  private[domala] def run: Future[R2] = base.run.flatMap {
    result => context.withAsyncStatus(asyncStatus)(f(result)).run.transform {
      case success: Success[R] => success
      case Failure(t) => throw t
    }
  }
}

case class AndThenAction[+R2, R] private[async] (base: AsyncAction[R], pf: PartialFunction[Try[R], AsyncAction[R2]]) extends AsyncAction[R] {
  val context: AsyncContext = base.context
  implicit private[this] val executionContext: ExecutionContext = context.executionContext
  private[this] val asyncStatus = context.asyncStatus
  private[domala] def run: Future[R] = {
    base.run.transformWith {
      result =>
        context.withAsyncStatus(asyncStatus)(pf(result)).run.transform {
          case Success(_) => result
          case Failure(t) => throw t
        }
    }
  }
}

case class RecoverAction[+R2 >: R, R] private[async] (base: AsyncAction[R], pf: PartialFunction[Throwable, AsyncAction[R2]]) extends AsyncAction[R2] {
  val context: AsyncContext = base.context
  implicit private[this] val executionContext: ExecutionContext = context.executionContext
  private[this] val asyncStatus = context.asyncStatus
  private[domala] def run: Future[R2] = {
    val future = base.run
    future.transformWith {
      case Success(_) => future
      case Failure(t) => context.withAsyncStatus(asyncStatus)(pf.apply(t)).run
    }
  }
}

