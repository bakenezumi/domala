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

  private val asyncStateHolder: DynamicVariable[AsyncState] = new DynamicVariable[AsyncState](AsyncState.Outside)

  def asyncState: AsyncState = asyncStateHolder.value

  def withAsyncStatus[R](status: AsyncState)(thunk: => R): R = asyncStateHolder.withValue(status)(thunk)

}

sealed trait AsyncState

object AsyncState {
  case object Outside extends AsyncState
  case object Async extends AsyncState
  case object Transactional extends AsyncState
}
