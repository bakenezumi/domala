package domala.async.jdbc

import domala.jdbc.Config

import scala.concurrent.{ExecutionContext, Future}

/** A runtime configuration for asynchronously DAOs.
  *
  * The implementation must be thread safe.
  *
  */
trait AsyncConfig extends Config with AsyncOperation

trait AsyncOperation {

  val executionContext: ExecutionContext

  private[domala] def future[T](block: => T): Future[T]

}
