package domala.async.jdbc

import domala.jdbc.Config

import scala.concurrent.ExecutionContext
import scala.util.DynamicVariable

/** A runtime configuration for asynchronously DAOs.
  *
  * The implementation must be thread safe.
  *
  */
trait AsyncConfig extends Config with AsyncOperation

trait AsyncOperation {

  val executionContext: ExecutionContext

  private[async] val atomicityHolder: DynamicVariable[Boolean] = new DynamicVariable[Boolean](false)

  def atomicity: Boolean = atomicityHolder.value

  def transaction[R](thunk: => R): R

}
