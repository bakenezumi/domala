package domala.internal

import scala.collection.immutable.Stream.Empty

object WrapStream {
  def of[T](javaStream: java.util.stream.Stream[T]): Stream[T] = {
    val it = javaStream.iterator()
    of(it)
  }

  def of[T](it: java.util.Iterator[T]): Stream[T] = {
    if(it.hasNext) {
      val next = it.next()
      next #:: of(it)
    } else {
      Empty
    }
  }

}
