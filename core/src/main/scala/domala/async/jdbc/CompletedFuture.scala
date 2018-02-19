package domala.async.jdbc

import scala.concurrent.duration.Duration
import scala.concurrent.{CanAwait, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait CompletedFuture[+T] extends Future[T] {
  val tried: Try[T]
  def onComplete[U](f: Try[T] => U)(implicit executor: ExecutionContext): Unit = f(tried)

  def isCompleted: Boolean = true

  def value: Option[Try[T]] = Option(tried)

  def transform[S](f: Try[T] => Try[S])(implicit executor: ExecutionContext): Future[S] = CompletedFuture(f(tried))

  def transformWith[S](f: Try[T] => Future[S])(implicit executor: ExecutionContext): Future[S] = f(tried)

  def ready(atMost: Duration)(implicit permit: CanAwait): this.type = this
}

object CompletedFuture {

  def apply[T](thunk: => T): CompletedFuture[T] = apply(Try(thunk))

  def apply[T](Tried: Try[T]): CompletedFuture[T] = Tried match {
    case success: Success[T] => new SuccessFuture(success)
    case failure: Failure[T] => new FailureFuture(failure)
  }

  private class SuccessFuture[+T](val tried: Success[T]) extends CompletedFuture[T] {

    override def result(atMost: Duration)(implicit permit: CanAwait): T = tried.value

    override def foreach[U](f: T => U)(implicit executor: ExecutionContext): Unit = f(tried.value)

  }

  private class FailureFuture[+T](val tried: Failure[T]) extends CompletedFuture[T] {

    override def result(atMost: Duration)(implicit permit: CanAwait): T = throw tried.exception

    override def foreach[U](f: T => U)(implicit executor: ExecutionContext): Unit = throw tried.exception

  }

}