package domala.jdbc.interpolation

import domala.internal.interpolation.util.TypeUtil
import domala.jdbc.builder.SelectBuilder
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
    if (TypeUtil.isEntity(tpe)) {
      builder.getEntitySingleResult[T](cTag.runtimeClass.asInstanceOf[Class[T]])
    } else if (TypeUtil.isHolder(tpe) || TypeUtil.isBasic(tpe)) {
      builder.getScalarSingleResult[T](cTag.runtimeClass.asInstanceOf[Class[T]])
    } else if (TypeUtil.isMap(tpe)) {
      getMapSingle().asInstanceOf[T]
    } else {
      throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getSingle")
    }
  }

  def getOption[T](implicit cTag: ClassTag[T], tTag: TypeTag[T]): Option[T] = {
    val tpe = typeOf[T]
    if (TypeUtil.isEntity(tpe)) {
      builder.getOptionEntitySingleResult[T](cTag.runtimeClass.asInstanceOf[Class[T]])
    } else if (TypeUtil.isHolder(tpe) || TypeUtil.isBasic(tpe)) {
      builder.getOptionScalarSingleResult[T](cTag.runtimeClass.asInstanceOf[Class[T]])
    } else if (TypeUtil.isMap(tpe)) {
      getOptionMapSingle().asInstanceOf[Option[T]]
    } else {
      throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getOption")
    }
  }

  def getMapSingle(mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE): Map[String, AnyRef] = {
    builder.getMapSingleResult()
  }

  def getOptionMapSingle(mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE): Option[Map[String, AnyRef]] = {
    builder.getOptionMapSingleResult()
  }

  def getMapSeq(mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE): Seq[Map[String, AnyRef]] = {
    builder.getMapResultSeq()
  }

  def getMapList(mapKeyNamingType: MapKeyNamingType = MapKeyNamingType.NONE): List[Map[String, AnyRef]] = {
    builder.getMapResultSeq().toList
  }

  def getSeq[T](implicit cTag: ClassTag[T], tTag: TypeTag[T]): Seq[T] = {
    val tpe = typeOf[T]
    if (TypeUtil.isEntity(tpe)) {
      builder.getEntityResultSeq[T](cTag.runtimeClass.asInstanceOf[Class[T]])
    } else if (TypeUtil.isHolder(tpe) || TypeUtil.isBasic(tpe)) {
      builder.getScalarResultSeq[T](cTag.runtimeClass.asInstanceOf[Class[T]])
    } else if (TypeUtil.isMap(tpe)) {
      getMapSeq().asInstanceOf[Seq[T]]
    } else {
      throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getSeq")
    }
  }

  def getList[T](implicit cTag: ClassTag[T], tTag: TypeTag[T]): List[T] = {
    getSeq[T](cTag, tTag).toList
  }

  def apply[TARGET, RESULT](mapper: Iterator[TARGET] => RESULT)(implicit cTag: ClassTag[TARGET], tTag: TypeTag[TARGET]): RESULT = {
    val tpe = typeOf[TARGET]
    if (TypeUtil.isEntity(tpe)) {
      builder.iteratorEntity[TARGET, RESULT](cTag.runtimeClass.asInstanceOf[Class[TARGET]], mapper)
    } else if (TypeUtil.isHolder(tpe) || TypeUtil.isBasic(tpe)) {
      builder.iteratorScalar[RESULT, TARGET](cTag.runtimeClass.asInstanceOf[Class[TARGET]], mapper)
    } else if (TypeUtil.isMap(tpe)) {
      builder.iteratorMap[RESULT](mapper.asInstanceOf[Iterator[Map[String, AnyRef]] => RESULT])
    } else {
      throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "apply")
    }
  }

}

object SelectStatement {
  def apply(builder: SelectBuilder): SelectStatement = new SelectStatement(builder)
}