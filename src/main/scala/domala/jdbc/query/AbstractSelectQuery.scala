package domala.jdbc.query
import java.util
import java.util.function.Function

import domala.internal.expr.ExpressionEvaluator
import org.seasar.doma.internal.jdbc.sql.node.ExpandNode
import org.seasar.doma.jdbc.PreparedSql

import scala.collection.Iterable
import scala.collection.JavaConverters._

abstract class AbstractSelectQuery extends org.seasar.doma.jdbc.query.AbstractSelectQuery{

  protected def buildSqlDomala(sqlBuilder: (ExpressionEvaluator, Function[ExpandNode, util.List[String]]) => PreparedSql): Unit = {
    val evaluator: ExpressionEvaluator = new ExpressionEvaluator(this.parameters, this.config.getDialect.getExpressionFunctions, this.config.getClassHelper)
    this.sql = sqlBuilder.apply(evaluator, this.expandColumns _)
  }

  override def addParameter(name: String, tpe: Class[_], value: Any): Unit = {
    value match {
      case x: Iterable[_] =>
        val converted = x.asJava
        super.addParameter(name, converted.getClass, converted)
      case _ => super.addParameter(name, tpe, value)
    }
  }
}
