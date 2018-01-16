package domala.internal.expr

import java.util._

import org.seasar.doma.expr.ExpressionFunctions
import org.seasar.doma.internal.expr.{EvaluationResult, ExpressionException, NullExpressionFunctions, Value}
import org.seasar.doma.message.Message
import org.seasar.doma.internal.expr.node._
import org.seasar.doma.jdbc.ClassHelper
import org.seasar.doma.internal.util.{ClassUtil, GenericsUtil, MethodUtil}
import java.lang.reflect.{GenericDeclaration, ParameterizedType, TypeVariable}

// createEvaluationResultにてOptionのunwrapを行うために拡張
class ExpressionEvaluator(variableValues: java.util.Map[String, Value] =
                            Collections.emptyMap[String, Value],
                          expressionFunctions: ExpressionFunctions =
                            new NullExpressionFunctions(),
                          classHelper: ClassHelper = new ClassHelper() {})
    extends org.seasar.doma.internal.expr.ExpressionEvaluator(
      variableValues,
      expressionFunctions,
      classHelper) {

  def this(expressionFunctions: ExpressionFunctions, classHelper: ClassHelper) = {
    this(Collections.emptyMap[String, Value], expressionFunctions, classHelper)
  }

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
            if (typeArguments.nonEmpty) {
              typeArguments(0) match {
                case valueClass: Class[_] =>
                  val elementValue: Any = option.orNull
                  if(elementValue != null && !elementValue.getClass.isAssignableFrom(valueClass))
                    return new EvaluationResult(elementValue, elementValue.getClass)
                  else
                    return new EvaluationResult(elementValue, valueClass)
                case parameterizedType: ParameterizedType if parameterizedType.getRawType.isInstanceOf[Class[_]] =>
                  val  elementValue: Any = option.orNull
                  return new EvaluationResult(elementValue, parameterizedType.getRawType.asInstanceOf[Class[_]])
                case _ => ()
              }
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

  override def visitFunctionOperatorNode(node: FunctionOperatorNode, p: Void): EvaluationResult = {
    val collector = new ParameterCollector()
    val collection = collector.collect(node.getParametersNode)
    val location = node.getLocation
    if(node.getExpression.startsWith("@")) {
      val targetClass = expressionFunctions.getClass
      val method = findMethod(node.getMethodName, expressionFunctions, targetClass, collection.getParamTypes)
      if (method == null) {
        val signature = MethodUtil.createSignature(node.getMethodName, collection.getParamTypes)
        throw new ExpressionException(Message.DOMA3028, location.getExpression, Integer.valueOf(location.getPosition), signature)
      }
      invokeMethod(node.getLocation, method, expressionFunctions, targetClass, collection.getParamTypes, collection.getParams)
    } else { // Domala add
      val value = variableValues.get(node.getMethodName)
      if (value != null) {
        val method = findMethod("apply", value.getValue, value.getType, collection.getParamTypes)
        if (method != null)
          return invokeMethod(location, method, value.getValue, value.getType, collection.getParamTypes, collection.getParams)
      }
      val signature = MethodUtil.createSignature(node.getMethodName, collection.getParamTypes)
      throw new ExpressionException(Message.DOMA3028, location.getExpression, Integer.valueOf(location.getPosition), signature)
    }
  }

  override protected def calculateHierarchyDifference(paramType: Class[_], argType: Class[_], initDifference: Int): Int = {
    var difference = initDifference
    if (paramType == classOf[Any] && argType.isInterface) return Integer.MAX_VALUE
    var tpe = argType
    while ( {tpe != null}) {
      if (paramType == tpe || paramType == ClassUtil.toBoxedPrimitiveTypeIfPossible(tpe)) return difference
      difference += 1
      if (paramType.isInterface) for (interfaceClass <- tpe.getInterfaces) {
        val result = calculateHierarchyDifference(paramType, interfaceClass, difference)
        if (result != -1) return result
      }
      tpe = tpe.getSuperclass
    }
    -1
  }

  protected class ParameterCollector extends ExpressionNodeVisitor[Void, java.util.List[EvaluationResult]] {
    def collect(node: ExpressionNode): org.seasar.doma.internal.expr.ExpressionEvaluator.ParameterCollection = {
      val evaluationResults = new java.util.ArrayList[EvaluationResult]
      node.accept(this, evaluationResults)
      new org.seasar.doma.internal.expr.ExpressionEvaluator.ParameterCollection(evaluationResults)
    }

    override def visitEqOperatorNode(node: EqOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitNeOperatorNode(node: NeOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitGeOperatorNode(node: GeOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitGtOperatorNode(node: GtOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitLeOperatorNode(node: LeOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitLtOperatorNode(node: LtOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitCommaOperatorNode(node: CommaOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      node.getNodes.forEach(expressionNode => expressionNode.accept(this, p))
      null
    }

    override def visitLiteralNode(node: LiteralNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitVariableNode(node: VariableNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitOrOperatorNode(node: OrOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitAndOperatorNode(node: AndOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitNotOperatorNode(node: NotOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitAddOperatorNode(node: AddOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitSubtractOperatorNode(node: SubtractOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitMultiplyOperatorNode(node: MultiplyOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitDivideOperatorNode(node: DivideOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitModOperatorNode(node: ModOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitNewOperatorNode(node: NewOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitMethodOperatorNode(node: MethodOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitStaticMethodOperatorNode(node: StaticMethodOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitFunctionOperatorNode(node: FunctionOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitFieldOperatorNode(node: FieldOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitStaticFieldOperatorNode(node: StaticFieldOperatorNode, p: java.util.List[EvaluationResult]): Void = {
      evaluate(node, p)
      null
    }

    override def visitParensNode(node: ParensNode, p: java.util.List[EvaluationResult]): Void = {
      node.getNode.accept(this, p)
      null
    }

    override def visitEmptyNode(node: EmptyNode, p: java.util.List[EvaluationResult]): Void = null

    protected def evaluate(node: ExpressionNode, p: java.util.List[EvaluationResult]): Unit = {
      val evaluationResult = evaluateInternal(node)
      p.add(evaluationResult)
    }
  }
}
