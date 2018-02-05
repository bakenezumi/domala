package domala.jdbc.interpolation

import domala.internal.macros.reflect.util.{MacroEntityDescGenerator, MacroTypeConverter, MacroUtil}
import domala.jdbc.`type`.Types
import domala.jdbc.builder.SelectBuilder
import domala.message.Message
import org.seasar.doma.{DomaException, MapKeyNamingType}

import scala.language.experimental.macros

/** The object used for executing a SELECT SQL statement and returning the results it produces.
  *
  * @param builder a builder for SELECT SQL already built
  */
class SelectStatement private (val builder: SelectBuilder) {

  def getSingle[T]: T = macro SelectStatementMacro.getSingle[T]

  def getOption[T]: Option[T] = macro SelectStatementMacro.getOption[T]

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

  def getSeq[T]: Seq[T] = macro SelectStatementMacro.getSeq[T]

  def getList[T]: List[T] = macro SelectStatementMacro.getList[T]

  def apply[TARGET, RESULT](mapper: Iterator[TARGET] => RESULT): RESULT = macro SelectStatementMacro.apply[TARGET, RESULT]

}

object SelectStatement {
  def apply(builder: SelectBuilder): SelectStatement = new SelectStatement(builder)
}

object SelectStatementMacro {
  import scala.reflect.macros.blackbox

  def getSingle[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[T] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val self = c.prefix

    c.Expr[T] {
      MacroTypeConverter.of(c).toType(tpe) match {
        case _: Types.Entity =>
          val entityDesc = MacroEntityDescGenerator.get(c)(tpe)
          q"$self.builder.getEntitySingleResult[$tpe]($entityDesc)"
        case t if t.isHolder || t.isBasic =>
          q"$self.builder.getScalarSingleResult[$tpe]"
        case Types.Map =>
          q"$self.getMapSingle.asInstanceOf[$tpe]"
        case _ => throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getSingle", MacroUtil.getPropertyErrorMessage(c)(tpe))
      }
    }
  }

  def getOption[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Option[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val self = c.prefix

    c.Expr[Option[T]] {
      MacroTypeConverter.of(c).toType(tpe) match {
        case _: Types.Entity =>
          val entityDesc = MacroEntityDescGenerator.get(c)(tpe)
          q"$self.builder.getOptionEntitySingleResult[$tpe]($entityDesc)"
        case t if t.isHolder || t.isBasic =>
          q"$self.builder.getOptionScalarSingleResult[$tpe]"
        case Types.Map =>
          q"$self.getOptionMapSingle.asInstanceOf[Option[$tpe]]"
        case _ => throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getOption", MacroUtil.getPropertyErrorMessage(c)(tpe))
      }
    }
  }

  def getSeq[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Seq[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val self = c.prefix
    c.Expr[Seq[T]] {
      MacroTypeConverter.of(c).toType(tpe) match {
        case _: Types.Entity =>
          val entityDesc = MacroEntityDescGenerator.get(c)(tpe)
          q"$self.builder.getEntityResultSeq[$tpe]($entityDesc)"
        case t if t.isHolder || t.isBasic =>
          q"$self.builder.getScalarResultSeq[$tpe]"
        case Types.Map =>
          q"$self.getMapSeq.asInstanceOf[Option[$tpe]]"
        case _ => throw new DomaException(Message.DOMALA4008, tpe, "SelectStatement", "getSeq", MacroUtil.getPropertyErrorMessage(c)(tpe))
      }
    }
  }

  def getList[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[List[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val self = c.prefix
    c.Expr[List[T]] {
      q"$self.getSeq[$tpe].toList"
    }
  }

  def apply[TARGET: c.WeakTypeTag, RESULT: c.WeakTypeTag](c: blackbox.Context)(mapper: c.Expr[Iterator[TARGET] => RESULT]): c.Expr[RESULT] = {
    import c.universe._
    val targetTpe = weakTypeOf[TARGET]
    val resultTpe = weakTypeOf[RESULT]
    val self = c.prefix
    c.Expr[RESULT] {
      MacroTypeConverter.of(c).toType(targetTpe) match {
        case _: Types.Entity =>
          val entityDesc = MacroEntityDescGenerator.get[blackbox.Context, TARGET](c)(targetTpe)
          q"$self.builder.iteratorEntity[$targetTpe, $resultTpe]($mapper)($entityDesc)"
        case t if t.isHolder || t.isBasic =>
          q"$self.builder.iteratorScalar[$resultTpe, $targetTpe]($mapper)"
        case Types.Map =>
          q"$self.builder.iteratorMap($mapper, org.seasar.doma.MapKeyNamingType.NONE)"
        case _ => throw new DomaException(Message.DOMALA4008, targetTpe, "SelectStatement", "apply", MacroUtil.getPropertyErrorMessage(c)(targetTpe))
      }
    }
  }

}
