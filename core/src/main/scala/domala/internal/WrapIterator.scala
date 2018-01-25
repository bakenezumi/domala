package domala.internal

private class WrapIterator[T](it: java.util.Iterator[T]) extends Iterator[T]{
  override def hasNext: Boolean = it.hasNext
  override def next(): T = it.next()
}

object WrapIterator {
  def of[T](javaStream: java.util.stream.Stream[T]): Iterator[T] = {
    val it = javaStream.iterator()
    new WrapIterator(it)
  }
}
