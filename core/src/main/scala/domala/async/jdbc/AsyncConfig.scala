package domala.async.jdbc

import domala.jdbc.Config

import scala.concurrent.ExecutionContext
import scala.util.DynamicVariable

/** A runtime configuration for asynchronously DAOs.
  *
  * The implementation must be thread safe.
  *
  */
trait AsyncConfig extends Config {

  /** ExecutionContext used when accessing DB */
  val executionContext: ExecutionContext

  /** A operation performed for each Future
    *
    * For transaction control, logging etc
    *
    * When [[domala.async.Async#apply]] is used, a Future is created for each Async Action,
    * and when [[domala.async.Async#transactionally]] is used, it is grouped into one Future
    *
    **/
  def atomicOperation[R](thunk: => R): R

  private[async] val transactionallyHolder: DynamicVariable[Boolean] = new DynamicVariable[Boolean](false)

  def isTransactionally: Boolean = transactionallyHolder.value

}
