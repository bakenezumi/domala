package domala.jdbc

import java.sql.Connection
import javax.sql.DataSource

import domala.internal.macros.reflect.DaoProviderMacro

import scala.reflect.ClassTag
import scala.language.experimental.macros

/** A provider of Dao instance. */
object DaoProvider {
  def get[T](implicit config: Config, classTag: ClassTag[T]): T = macro DaoProviderMacro.get[T]
  def get[T](config: Config)(implicit classTag: ClassTag[T]): T = macro DaoProviderMacro.getByConfig[T]
  def get[T](connection: Connection)(implicit config: Config, classTag: ClassTag[T]): T = macro DaoProviderMacro.getByConnection[T]
  def get[T](dataSource: DataSource)(implicit config: Config, classTag: ClassTag[T]): T = macro DaoProviderMacro.getByDataSource[T]
}
