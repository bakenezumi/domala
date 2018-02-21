package domala.async

import scala.concurrent.ExecutionContext
import scala.util.DynamicVariable

/** A context of asynchronous processing */
trait AsyncContext {
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

  private val asyncStatusHolder: DynamicVariable[AsyncStatus] = new DynamicVariable[AsyncStatus](AsyncStatus.Outside)

  def asyncStatus: AsyncStatus = asyncStatusHolder.value

  def withAsyncStatus[R](status: AsyncStatus)(thunk: => R): R = asyncStatusHolder.withValue(status)(thunk)

}

sealed trait AsyncStatus

object AsyncStatus {
  case object Outside extends AsyncStatus
  case object Async extends AsyncStatus
  case object Transactional extends AsyncStatus
}
