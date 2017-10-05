package domala.internal.expr

import org.seasar.doma.expr.ExpressionFunctions
import org.seasar.doma.internal.expr.{
  EvaluationResult,
  ExpressionException,
  Value
}
import org.seasar.doma.message.Message
import org.seasar.doma.internal.expr.node.FieldOperatorNode
import org.seasar.doma.jdbc.ClassHelper

class ExpressionEvaluator(
  variableValues: java.util.Map[String, Value],
  expressionFunctions: ExpressionFunctions,
  classHelper: ClassHelper)
  extends org.seasar.doma.internal.expr.ExpressionEvaluator(
    variableValues,
    expressionFunctions,
    classHelper) {

  override def visitFieldOperatorNode(
    node: FieldOperatorNode,
    p: Void): EvaluationResult = {
    val targetResult = node.getTargetObjectNode.accept(this, p)
    val target = targetResult.getValue
    val location = node.getLocation
    val field = findField(node.getFieldName, target.getClass)
    if (field == null) {
      val method = findMethod(node.getFieldName, expressionFunctions, target.getClass, new Array[Class[_]](0))
      if (method == null) {
        throw new ExpressionException(Message.DOMA3018, location.getExpression, Integer.valueOf(location.getPosition), target.getClass.getName, node.getFieldName)
      }
      invokeMethod(node.getLocation, method, target, target.getClass, new Array[Class[_]](0), new Array[Object](0))
    } else {
      getFieldValue(location, field, target)
    }
  }
}
