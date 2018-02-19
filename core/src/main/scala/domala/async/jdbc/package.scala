package domala.async

import domala.jdbc.{BatchResult, Result}

package object jdbc {
  type AsyncResult[E] = AsyncAction[Result[E]]
  type AsyncBatchResult[E] = AsyncAction[BatchResult[E]]
}
