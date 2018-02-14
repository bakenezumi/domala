package domala.async

import domala.jdbc.{BatchResult, Result}

import scala.concurrent.Future

package object jdbc {
  type FutureResult[E] = Future[Result[E]]
  type FutureBatchResult[E] = Future[BatchResult[E]]
}
