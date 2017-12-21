package domala.tests

import domala._
import domala.jdbc.{BatchResult, Result}

@Dao
trait PersonSqlFileDao {

  @Select
  def selectById(id: Int): Option[Person]

  @Select("select count(1) from person")
  def selectCount: Int

  @Select
  def selectAll: Seq[Person]

  @Select
  def selectWithDepartmentEmbeddedById(id: Int): Option[PersonDepartmentEmbedded]

  @Select(strategy = SelectType.ITERATOR)
  def selectAllIterator(f: Iterator[Person] => Int): Int

  @Select
  def inSelect(ids: List[Int]): Seq[Person]

  @Select
  def literalSelect(id: Int): Option[Person]

  @Select
  def embeddedSelect(orderBy: String): Seq[Person]

  @Select
  def ifSelect(id: Option[Int]): Seq[Person]

  @Select
  def elseSelect(id: Option[Int], departmentId: Option[Int]): Seq[Person]

  @Select
  def forSelect(names: List[String]): Seq[Person]

  @Select
  def expandSelect: Seq[Person]

  @Select
  def expandAliasSelect: Seq[Person]

  @Insert(sqlFile = true)
  def insertSql(entity: Person, entity2: Person, version: Int): Result[Person]

  @Update(sqlFile = true)
  def updateSql(entity: Person, entity2: Person, version: Int): Result[Person]


  @Delete(sqlFile = true)
  def deleteSql(entity: Person, version: Int): Int

  @BatchInsert(sqlFile = true, batchSize = 100)
  def batchInsertSql(persons: Seq[Person]): BatchResult[Person]

}


