package domala.internal.expr

import java.util._

import org.seasar.doma.expr.ExpressionFunctions
import org.seasar.doma.internal.expr.{EvaluationResult, ExpressionException, NullExpressionFunctions, Value}
import org.seasar.doma.message.Message
import org.seasar.doma.internal.expr.node.{ExpressionNode, FieldOperatorNode}
import org.seasar.doma.jdbc.ClassHelper
import org.seasar.doma.internal.util.{AssertionUtil, GenericsUtil}
import java.lang.reflect.{GenericDeclaration, ParameterizedType, TypeVariable}

class ExpressionEvaluator(variableValues: java.util.Map[String, Value] =
                            Collections.emptyMap[String, Value],
                          expressionFunctions: ExpressionFunctions =
                            new NullExpressionFunctions(),
                          classHelper: ClassHelper = new ClassHelper() {})
    extends org.seasar.doma.internal.expr.ExpressionEvaluator(
      variableValues,
      expressionFunctions,
      classHelper) {

  override def visitFieldOperatorNode(node: FieldOperatorNode,
                                      p: Void): EvaluationResult = {
    val targetResult = node.getTargetObjectNode.accept(this, p)
    val target = targetResult.getValue
    val location = node.getLocation
    val field = findField(node.getFieldName, target.getClass)
    if (field == null) {
      val method = findMethod(node.getFieldName,
                              expressionFunctions,
                              target.getClass,
                              new Array[Class[_]](0))
      if (method == null) {
        throw new ExpressionException(Message.DOMA3018,
                                      location.getExpression,
                                      Integer.valueOf(location.getPosition),
                                      target.getClass.getName,
                                      node.getFieldName)
      }
      invokeMethod(node.getLocation,
                   method,
                   target,
                   target.getClass,
                   new Array[Class[_]](0),
                   new Array[Object](0))
    } else {
      getFieldValue(location, field, target)
    }
  }

  override protected def createEvaluationResult(target: Object, value: Object, valueClass: Class[_], genericType: java.lang.reflect.Type): EvaluationResult = {
    value match {
      case optional: Optional[_] =>
        genericType match {
          case parameterizedType: ParameterizedType =>
            val typeArguments = parameterizedType.getActualTypeArguments
            if (typeArguments.nonEmpty && typeArguments(0).isInstanceOf[Class[_]]) {
              val elementValue = if(optional.isPresent) optional.get() else null
              return new EvaluationResult(elementValue, typeArguments(0).asInstanceOf[Class[_]])
            }
          case _ => ()
        }
      case optional: OptionalInt =>
        val nullable = if (optional.isPresent) optional.getAsInt else null
        return new EvaluationResult(nullable, classOf[Integer])
      case optional: OptionalLong =>
        val nullable = if (optional.isPresent) java.lang.Long.valueOf(optional.getAsLong) else null
        return new EvaluationResult(nullable, classOf[Long])
      case optional: OptionalDouble =>
        val nullable = if (optional.isPresent) java.lang.Double.valueOf(optional.getAsDouble) else null
        return new EvaluationResult(nullable, classOf[Double])
      case option: Option[_] =>
        genericType match {
          case parameterizedType: ParameterizedType =>
            val typeArguments = parameterizedType.getActualTypeArguments
            if (typeArguments.nonEmpty && typeArguments(0).isInstanceOf[Class[_]]) {
              val elementValue = option.getOrElse(null)
              return new EvaluationResult(elementValue, typeArguments(0).asInstanceOf[Class[_]])
            }
          case _ => ()
        }
      case _ =>
        if (target != null && genericType.isInstanceOf[TypeVariable[_ <: GenericDeclaration]]) {
          val typeArgument = GenericsUtil.inferTypeArgument(target.getClass, genericType.asInstanceOf[TypeVariable[_ <: GenericDeclaration]])
          if (typeArgument != null) return new EvaluationResult(value, typeArgument)
        }
    }
    if(value == null)
      new EvaluationResult(value, valueClass)
    else
      new EvaluationResult(value, value.getClass)
  }

}
