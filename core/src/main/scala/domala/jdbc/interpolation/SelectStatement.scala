package domala.jdbc.interpolation

import domala.internal.reflect.util.RuntimeTypeConverter
import domala.jdbc.builder.SelectBuilder
import domala.jdbc.`type`.Types
import domala.message.Message
import org.seasar.doma.{DomaException, MapKeyNamingType}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** The object used for executing a SELECT SQL statement and returning the results it produces.
  *
  * @param builder a builder for SELECT SQL already built
  */
class SelectStatement(builder: SelectBuilder) {
  def getSingle[T](implicit cTag: ClassTag[T], tTag: TypeTag[T]): T = {
    val tpe = typeOf[T]
    RuntimeTypeConverter.toType(tpe) match {
      case _: Types.Entity =>
        builder.getEntitySingleResult[T]
      case t if t.isHolder || t.isBasic =>
        builder.getScalarSingleResult[T]
      case Types.Map =>
        getMapSingle.asInstanceOf[T]
      case _ => throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getSingle")
    }
  }

  def getOption[T](implicit cTag: ClassTag[T], tTag: TypeTag[T]): Option[T] = {
    val tpe = typeOf[T]
    RuntimeTypeConverter.toType(tpe) match {
      case _: Types.Entity =>
        builder.getOptionEntitySingleResult[T]
      case t if t.isHolder || t.isBasic =>
        builder.getOptionScalarSingleResult[T]
      case Types.Map =>
        getOptionMapSingle.asInstanceOf[Option[T]]
      case _ => throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getOption")
    }
  }

  def getMapSingle(mapKeyNamingType: MapKeyNamingType): Map[String, AnyRef] = {
    builder.getMapSingleResult(mapKeyNamingType)
  }

  def getMapSingle: Map[String, AnyRef] = {
    builder.getMapSingleResult(MapKeyNamingType.NONE)
  }

  def getOptionMapSingle(mapKeyNamingType: MapKeyNamingType): Option[Map[String, AnyRef]] = {
    builder.getOptionMapSingleResult(mapKeyNamingType)
  }

  def getOptionMapSingle: Option[Map[String, AnyRef]] = {
    builder.getOptionMapSingleResult(MapKeyNamingType.NONE)
  }

  def getMapSeq(mapKeyNamingType: MapKeyNamingType): Seq[Map[String, AnyRef]] = {
    builder.getMapResultSeq(mapKeyNamingType)
  }

  def getMapSeq: Seq[Map[String, AnyRef]] = {
    builder.getMapResultSeq(MapKeyNamingType.NONE)
  }

  def getMapList(mapKeyNamingType: MapKeyNamingType): List[Map[String, AnyRef]] = {
    builder.getMapResultSeq(mapKeyNamingType).toList
  }

  def getMapList: List[Map[String, AnyRef]] = {
    builder.getMapResultSeq(MapKeyNamingType.NONE).toList
  }

  def getSeq[T](implicit cTag: ClassTag[T], tTag: TypeTag[T]): Seq[T] = {
    val tpe = typeOf[T]
    RuntimeTypeConverter.toType(tpe) match {
      case _: Types.Entity =>
        builder.getEntityResultSeq[T]
      case t if t.isHolder || t.isBasic =>
        builder.getScalarResultSeq[T]
      case Types.Map =>
        getMapSeq.asInstanceOf[Seq[T]]
      case _ => throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getSeq")
    }
  }

  def getList[T](implicit cTag: ClassTag[T], tTag: TypeTag[T]): List[T] = {
    getSeq[T].toList
  }

  def apply[TARGET, RESULT](mapper: Iterator[TARGET] => RESULT)(implicit cTag: ClassTag[TARGET], tTag: TypeTag[TARGET]): RESULT = {
    val tpe = typeOf[TARGET]
    RuntimeTypeConverter.toType(tpe) match {
      case _: Types.Entity =>
        builder.iteratorEntity[TARGET, RESULT](mapper)
      case t if t.isHolder || t.isBasic =>
        builder.iteratorScalar[RESULT, TARGET](mapper)
      case _ => throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "apply")
    }
  }

}

object SelectStatement {
  def apply(builder: SelectBuilder): SelectStatement = new SelectStatement(builder)
}
