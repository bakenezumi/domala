package domala.async

import domala.async.jdbc.{AsyncAction, AsyncConfig}

import scala.concurrent.Future

object Async {
  /** Execute DB access asynchronously and returns the future of result.
    *
    * All AsyncAction is executed in independent transaction.
    *
    * Use [[domala.async.Async#transactionally transactionally]] if want to maintain atomicity.
    *
    * {{{
    * implicit val config: AsyncConfig = ...
    * val person: Person = ...
    * val future: Future[(Result[Person], List[Person])] =
    *   Async {
    *     for {
    *       inserted <- AsyncEntityManager.insert(person)
    *       selected <- select"select * from person".async[Person, List[Person]](_.toList)
    *     } yield {
    *      (inserted, selected)
    *     }
    *   }
    * }}}
    *
    * @tparam RESULT the result type
    * @param thunk the code for constructing AsyncAction
    * @param config the runtime configuration
    * @return the future of result
    */
  def apply[RESULT](thunk: => AsyncAction[RESULT])(implicit config: AsyncConfig): Future[RESULT] =
    thunk.run

  /** Execute DB access asynchronously and returns the future of result.
    *
    * Individual parts of a composite AsyncAction executed serially
    * on a single database session.
    *
    * {{{
    * implicit val config: AsyncConfig = ...
    * val person: Person = ...
    * val future: Future[(Result[Person], List[Person])] =
    *   Async.transactionally {
    *     for {
    *       inserted <- AsyncEntityManager.insert(person)
    *       selected <- select"select * from person".async[Person, List[Person]](_.toList)
    *     } yield {
    *      (inserted, selected)
    *     }
    *   }
    * }}}
    *
    * @tparam RESULT the result type
    * @param thunk the code for constructing AsyncAction
    * @param config the runtime configuration
    * @return the future of result
    */
  def transactionally[RESULT](thunk: => AsyncAction[RESULT])(implicit config: AsyncConfig): Future[RESULT] =
    Future(
      config.atomicityHolder.withValue(true) {
        config.transaction {
          thunk.run
        }
      })(config.executionContext).flatten

}
