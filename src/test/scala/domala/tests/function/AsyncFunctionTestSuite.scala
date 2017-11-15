package domala.tests.function

import domala._
import domala.jdbc.{BatchResult, Config, SelectOptions}
import domala.tests.{ID, Name}
import org.scalatest.{AsyncFunSuite, BeforeAndAfter}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AsyncFunctionTestSuite extends AsyncFunSuite with BeforeAndAfter {

  override def executionContext: ExecutionContext = global

  private def init(dao: FunctionDao)(implicit config: Config): BatchResult[Emp] = Required {
    dao.create()
    val employees = (1 to 100).map(i => Emp(ID(i), Name("hoge"),  Jpy(i), Some(ID(1)))).toList
    dao.insert(employees)
  }

  test("future transaction") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfigs.get(0)
    val dao: FunctionDao = FunctionDao.impl

    init(dao)

    Future(Required {
      val option = SelectOptions.get
      dao.selectAll(_.map(_.salary).sum)(option)
    }).map(sum => assert(sum == Jpy(5050)))
  }

  test("parallel future") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfigs.get(1)
    val dao: FunctionDao = FunctionDao.impl

    init(dao)

    def f(offset: Int, limit: Int) = Future(Required {
      val option = SelectOptions.get.offset(offset).limit(limit)
      dao.selectAll(_.map(_.salary).toList)(option)
    })

    import Numeric.Implicits._
    val f1 = f(0, 50)
    val f2 = f(50, 100)
    val ret = for {
      sum1 <- f1
      sum2 <- f2
    } yield sum1.sum + sum2.sum

    ret.map(sum => assert(sum == Jpy(5050)))
  }

  test("partitioning iterator") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfigs.get(2)
    val dao: FunctionDao = FunctionDao.impl

    init(dao)

    // one partition is one transaction
    def slice[A, T <: Traversable[A]](f: SelectOptions => T)(offset: Int, limit: Int, option: SelectOptions = SelectOptions.get): T =
      Required {
        f(option.clone.offset(offset).limit(limit))
      }

    def partition[A, T <: Traversable[A]](f: SelectOptions => T)(blockSize: Int, option: SelectOptions = SelectOptions.get): Iterator[T] = {
      def next(offset: Int): Iterator[T] = {
        val list = slice[A, T](f)(offset, blockSize, option)
          if (list.isEmpty)
            Iterator.empty
          else
            Iterator(list) ++ next(blockSize + offset) // Iterator's `++` operation is lazy evaluation
        }
      next(0)
    }
    val selectFunction = dao.selectAll(_.map(_.salary).toList) _
    val partitioningFunction = partition[Jpy, List[Jpy]](selectFunction)(33) // needs type parameter
    Future(partitioningFunction).map{ x =>
      assert(x.map{_.sum}.toList == Seq(Jpy(561), Jpy(1650), Jpy(2739), Jpy(100))) // (1 to 33).sum, (34 to 66).sum, (67 to 99).sum, 100,
    }
  }

  test("partitioning iterator in one transaction") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfigs.get(3)

    val createDao: FunctionDao = FunctionDao.impl

    init(createDao)

    // original connection use
    println("connection open!")
    import java.sql.DriverManager
    // noinspection SpellCheckingInspection
    val connection = DriverManager.getConnection("jdbc:h2:mem:asyncfnctest3;DB_CLOSE_DELAY=-1", "sa", "")
    connection.setAutoCommit(false)
    val dao: FunctionDao = FunctionDao.impl(connection)

    // no transaction
    def slice[A, T <: Traversable[A]](f: SelectOptions => T)(offset: Int, limit: Int, option: SelectOptions = SelectOptions.get): T =
      f(option.clone.offset(offset).limit(limit))

    def partition[A, T <: Traversable[A]](f: SelectOptions => T)(blockSize: Int, option: SelectOptions = SelectOptions.get): Iterator[T] = {
      def next(offset: Int): Iterator[T] = {
        val list = slice[A, T](f)(offset, blockSize, option)
        if (list.isEmpty)
          Iterator.empty
        else
          Iterator(list) ++ next(blockSize + offset) // Iterator's `++` operation is lazy evaluation
      }
      next(0)
    }
    val selectFunction = dao.selectAll(_.map(_.salary).toList) _
    val partitioningFunction = partition[Jpy, List[Jpy]](selectFunction)(33) // needs type parameter

    val assertion = Future(partitioningFunction).map{ x =>
      assert(x.map(_.sum).toList == Seq(Jpy(561), Jpy(1650), Jpy(2739), Jpy(100))) // (1 to 33).sum, (34 to 66).sum, (67 to 99).sum, 100,
    }

    assertion.onComplete(_ => {
      connection.close()
      println("connection close!")
    })
    assertion
  }

  test("parallel partitioning iterator") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfigs.get(4)

    val createDao: FunctionDao = FunctionDao.impl

    init(createDao)

    val dao: FunctionDao = FunctionDao.impl

    def slice[A, T <: Traversable[A]](f: SelectOptions => T)(offset: Int, limit: Int, option: SelectOptions = SelectOptions.get): T =
      f(option.clone.offset(offset).limit(limit))

    def parallelPartition[A, T <: Traversable[A]](f: SelectOptions => T)(
      blockSize: Int,
      concurrentSize: Int = 4,
      option: SelectOptions = SelectOptions.get
    ): Future[Seq[T]] = {
      def par: Future[Seq[T]] = {
        val futures: Seq[Future[Seq[T]]] = (0 until concurrentSize).map { i =>
          Future(Required(next(i * blockSize, Nil))) // Transaction block
        }
        Future.sequence(futures).map(_.flatten)
      }
      @tailrec
      def next(offset: Int, acc: List[T]): List[T] = {
        val list = slice[A, T](f)(offset, blockSize, option)
        if(list.isEmpty)
          acc
        else
          next(offset + blockSize * concurrentSize, list :: acc)
      }
      par
    }
    val selectFunction = dao.selectAllEager(_.map(_.salary).toList) _
    val partitioningFunction = parallelPartition[Jpy, List[Jpy]](selectFunction)(8) // needs type parameter

    partitioningFunction.map { x =>
      assert(x.map(_.sum).sum == Jpy(5050))
    }
  }

  test("buffered iterator") {
    implicit val config: jdbc.Config = AsyncFunctionTestConfigs.get(5)
    val dao: FunctionDao = FunctionDao.impl

    init(dao)

    // one buffer is one transaction
    def slice[A, T <: Traversable[A]](f: SelectOptions => T)(offset: Int, limit: Int, option: SelectOptions = SelectOptions.get): T = Required {
      f(option.clone.offset(offset).limit(limit))
    }

    def bufferedPartition[A, T <: Traversable[A]](f: SelectOptions => T)(
      blockSize: Int,
      bufferSize: Int = 4,
      option: SelectOptions = SelectOptions.get
    ): Iterator[T] = {
      def nextBuffer(offset: Int): Iterator[T] = {
        val buffer = slice[A, T](f)(offset, blockSize * bufferSize, option)
        if(buffer.isEmpty)
          Iterator.empty
        else
          buffer.toSeq.grouped(blockSize).asInstanceOf[Iterator[T]] ++ nextBuffer(offset + blockSize * bufferSize)
      }
      nextBuffer(0)
    }

    val selectFunction = dao.selectAllEager(_.map(_.salary).toList) _
    val partitioningFunction = bufferedPartition[Jpy, List[Jpy]](selectFunction)(8) // needs type parameter
    Future(Required(partitioningFunction)).map{ x =>
      assert(x.map{_.sum}.sum == Jpy(5050))
    }
  }

}
