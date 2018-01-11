package domala.internal.macros.reflect

import java.util.OptionalDouble
import java.util.OptionalInt
import java.util.OptionalLong

import domala.internal.macros.reflect.decl.{MethodDeclaration, TypeDeclaration}
import domala.internal.reflect.util.ReflectionUtil
import domala.message.Message
import org.seasar.doma.expr.ExpressionFunctions
import org.seasar.doma.internal.expr.node._

import scala.collection.mutable
import scala.reflect.macros.blackbox

class ExpressionValidator[C <: blackbox.Context](c: C)(
  originalParamTypeMap: ReflectionHelper[C]#ParamMap) extends ExpressionNodeVisitor[TypeDeclaration[C], Void] {
  import c.universe._

  val validatedParameterNames: mutable.Set[String] = mutable.Set[String]()
  val parameterTypeMap: mutable.Map[String, ReflectionHelper[C]#ParamType] = mutable.Map[String,  ReflectionHelper[C]#ParamType](originalParamTypeMap.toSeq: _*)
  val unknownTypeDeclaration: TypeDeclaration[C] = TypeDeclaration.newUnknownTypeDeclaration[C](c)

  def removeParameterType(parameterName: String): ReflectionHelper[C]#ParamType = parameterTypeMap.remove(parameterName).orNull

  def putParameterType(parameterName: String, parameterType: ReflectionHelper[C]#ParamType): Unit = {
    parameterTypeMap.put(parameterName, parameterType)
  }

  def addValidatedParameterName(name: String): Unit = {
    validatedParameterNames.add(name)
  }

  def getValidatedParameterNames: mutable.Set[String] = validatedParameterNames

  def validate(node: ExpressionNode): TypeDeclaration[C] = validateInternal(node)

  def validateInternal(node: ExpressionNode): TypeDeclaration[C] = node.accept(this, null)

  override def visitEqOperatorNode(node: EqOperatorNode, p: Void): TypeDeclaration[C] = handleNullAvailableComparisonOperation(node, p)

  override def visitNeOperatorNode(node: NeOperatorNode, p: Void): TypeDeclaration[C] = handleNullAvailableComparisonOperation(node, p)

  override def visitGeOperatorNode(node: GeOperatorNode, p: Void): TypeDeclaration[C] = handleNullUnavailableComparisonOperation(node, p)

  override def visitGtOperatorNode(node: GtOperatorNode, p: Void): TypeDeclaration[C] = handleNullUnavailableComparisonOperation(node, p)

  override def visitLeOperatorNode(node: LeOperatorNode, p: Void): TypeDeclaration[C] = handleNullUnavailableComparisonOperation(node, p)

  override def visitLtOperatorNode(node: LtOperatorNode, p: Void): TypeDeclaration[C] = handleNullUnavailableComparisonOperation(node, p)

  protected def handleNullAvailableComparisonOperation(node: ComparisonOperatorNode, p: Void): TypeDeclaration[C] = {
    val left = node.getLeftNode.accept(this, p)
    val right = node.getRightNode.accept(this, p)
    if (left.isNullType || right.isNullType || left.isSameType(right)) TypeDeclaration.newBooleanTypeDeclaration[C](c)
    else {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4116 , location.getExpression, Integer.valueOf(location.getPosition), node.getExpression, node.getLeftNode, left.getBinaryName, node.getRightNode, right.getBinaryName)
    }
  }

  protected def handleNullUnavailableComparisonOperation(node: ComparisonOperatorNode, p: Void): TypeDeclaration[C] = {
    val left = node.getLeftNode.accept(this, p)
    val right = node.getRightNode.accept(this, p)
    if (left.isNullType || right.isNullType) {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4139, location.getExpression, Integer.valueOf(location.getPosition), node.getExpression)
    }
    if (left.isSameType(right))  TypeDeclaration.newBooleanTypeDeclaration[C](c)
    else {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4116, location.getExpression, Integer.valueOf(location.getPosition), node.getExpression, node.getLeftNode, left.getBinaryName, node.getRightNode, right.getBinaryName)
    }
  }

  override def visitAndOperatorNode(node: AndOperatorNode, p: Void): TypeDeclaration[C] = handleLogicalBinaryOperatorNode(node, p)

  override def visitOrOperatorNode(node: OrOperatorNode, p: Void): TypeDeclaration[C] = handleLogicalBinaryOperatorNode(node, p)

  protected def handleLogicalBinaryOperatorNode(node: LogicalBinaryOperatorNode, p: Void): TypeDeclaration[C] = {
    val left = node.getLeftNode.accept(this, p)
    val right = node.getRightNode.accept(this, p)
    if (!left.isBooleanType) {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4117, location.getExpression, Integer.valueOf(location.getPosition), node.getExpression, node.getLeftNode, left.getBinaryName)
    }
    if (!right.isBooleanType) {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4118, location.getExpression, Integer.valueOf(location.getPosition), node.getExpression, node.getRightNode, right.getBinaryName)
    }
    TypeDeclaration.newBooleanTypeDeclaration[C](c)
  }

  override def visitNotOperatorNode(node: NotOperatorNode, p: Void): TypeDeclaration[C] = {
    val result = node.getNode.accept(this, p)
    if (result.isBooleanType) TypeDeclaration.newBooleanTypeDeclaration[C](c)
    else {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4119, location.getExpression, Integer.valueOf(location.getPosition), node.getExpression, node.getNode, result.getBinaryName)
    }
  }

  override def visitAddOperatorNode(node: AddOperatorNode, p: Void): TypeDeclaration[C] = {
    val left = node.getLeftNode.accept(this, p)
    val right = node.getRightNode.accept(this, p)
    if (left.isTextType) {
      if (right.isTextType) left.emulateConcatOperation(right)
      else {
        val location = node.getLocation
        ReflectionUtil.abort(Message.DOMALA4126, location.getExpression, Integer.valueOf(location.getPosition), node.getExpression, node.getLeftNode, left.getBinaryName)
      }
    } else {
      handleArithmeticOperatorNode(node, left, right, p)
    }
  }

  override def visitSubtractOperatorNode(node: SubtractOperatorNode, p: Void): TypeDeclaration[C] = {
    val left = node.getLeftNode.accept(this, p)
    val right = node.getRightNode.accept(this, p)
    handleArithmeticOperatorNode(node, left, right, p)
  }

  override def visitMultiplyOperatorNode(node: MultiplyOperatorNode, p: Void): TypeDeclaration[C] = {
    val left = node.getLeftNode.accept(this, p)
    val right = node.getRightNode.accept(this, p)
    handleArithmeticOperatorNode(node, left, right, p)
  }

  override def visitDivideOperatorNode(node: DivideOperatorNode, p: Void): TypeDeclaration[C] = {
    val left = node.getLeftNode.accept(this, p)
    val right = node.getRightNode.accept(this, p)
    handleArithmeticOperatorNode(node, left, right, p)
  }

  override def visitModOperatorNode(node: ModOperatorNode, p: Void): TypeDeclaration[C] =  {
    val left = node.getLeftNode.accept(this, p)
    val right = node.getRightNode.accept(this, p)
    handleArithmeticOperatorNode(node, left, right, p)
  }

  protected def handleArithmeticOperatorNode(node: ArithmeticOperatorNode, left: TypeDeclaration[C], right: TypeDeclaration[C], p: Void): TypeDeclaration[C] = {
    if (!left.isNumberType) {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4120, location.getExpression, Integer.valueOf(location.getPosition), node.getExpression, node.getLeftNode, left.getBinaryName)
    }
    if (!right.isNumberType) {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4121, location.getExpression, Integer.valueOf(location.getPosition), node.getExpression, node.getRightNode, right.getBinaryName)
    }
    left.emulateArithmeticOperation(right)
  }

  override def visitLiteralNode(node: LiteralNode, p: Void): TypeDeclaration[C] = {
    val tpe =
      if (node.getValueClass eq classOf[Void]) typeOf[Null]
      else {
        val clazz = node.getValueClass
        if (clazz.isPrimitive)  {
          clazz.toString match {
            case "char" => typeOf[Char]
            case "byte" => typeOf[Byte]
            case "short" => typeOf[Short]
            case "int" => typeOf[Int]
            case "long" => typeOf[Long]
            case "float" => typeOf[Float]
            case "double" => typeOf[Double]
            case "boolean" => typeOf[Boolean]
            case "void" => typeOf[Null]
            case _ => c.abort(c.enclosingPosition, clazz.toString)
          }
        }
        else c.mirror.staticClass(clazz.getName).toType
      }
    TypeDeclaration.newTypeDeclaration[C](c)(tpe)
  }

  override def visitParensNode(node: ParensNode, p: Void): TypeDeclaration[C] = node.getNode.accept(this, p)

  // TODO:
  override def visitNewOperatorNode(node: NewOperatorNode, p: Void): TypeDeclaration[C] = ???

  override def visitCommaOperatorNode(node: CommaOperatorNode, p: Void): TypeDeclaration[C] = unknownTypeDeclaration

  override def visitEmptyNode(node: EmptyNode, p: Void): TypeDeclaration[C] = unknownTypeDeclaration

  override def visitMethodOperatorNode(node: MethodOperatorNode, p: Void): TypeDeclaration[C] = {
    val typeDeclaration = node.getTargetObjectNode.accept(this, p)
    val parameterTypeDeclarations = new ParameterCollector().collect(node.getParametersNode)
    val methodName = node.getMethodName
    val methodDeclarations: Seq[MethodDeclaration[C]] = typeDeclaration.getMethodDeclarations(methodName, parameterTypeDeclarations)
    if (methodDeclarations.isEmpty) {
      val location = node.getLocation
      val methodSignature = createMethodSignature(methodName, parameterTypeDeclarations)
      ReflectionUtil.abort(Message.DOMALA4071, location.getExpression, Integer.valueOf(location.getPosition), node.getTargetObjectNode.getExpression, typeDeclaration.getBinaryName, methodSignature)
    }
    if (methodDeclarations.size == 1) {
      val methodDeclaration = methodDeclarations.head
      val returnTypeDeclaration = methodDeclaration.getReturnTypeDeclaration
      if (returnTypeDeclaration != null) return convertIfOptional(returnTypeDeclaration)
    }
    val location = node.getLocation
    val methodSignature = createMethodSignature(methodName, parameterTypeDeclarations)
    ReflectionUtil.abort(Message.DOMALA4073, location.getExpression, Integer.valueOf(location.getPosition), node.getTargetObjectNode.getExpression, typeDeclaration.getBinaryName, methodSignature)
  }

  override def visitStaticMethodOperatorNode(node: StaticMethodOperatorNode, p: Void): TypeDeclaration[C] = {
    val location = node.getLocation
    ReflectionUtil.abort(Message.DOMALA6003, location.getExpression, Integer.valueOf(location.getPosition))

// Scala reflection: cannot invoke Java static methods and fields
// https://issues.scala-lang.org/browse/SI-6459

//    val className = node.getClassName
//    val typeElement = ElementUtil.getTypeElement(c)(node.getClassName).getOrElse{
//      val location = node.getLocation
//      c.abort(c.enclosingPosition, Message.DOMALA4145.getMessage(location.getExpression, Integer.valueOf(location.getPosition), className))
//    }
//    val typeDeclaration = TypeDeclaration.newTypeDeclaration(c)(typeElement)
//    val parameterTypeDeclarations = new ParameterCollector().collect(node.getParametersNode)
//    val methodName = node.getMethodName
//    val methodDeclarations = typeDeclaration.getStaticMethodDeclarations(methodName, parameterTypeDeclarations)
//    if (methodDeclarations.isEmpty) {
//      val location = node.getLocation
//      val methodSignature = createMethodSignature(methodName, parameterTypeDeclarations)
//      c.abort(c.enclosingPosition, Message.DOMALA4146.getMessage(location.getExpression, Integer.valueOf(location.getPosition), className, methodSignature))
//    }
//    if (methodDeclarations.size == 1) {
//      val methodDeclaration = methodDeclarations.head
//      val returnTypeDeclaration = methodDeclaration.getReturnTypeDeclaration
//      if (returnTypeDeclaration != null) return convertIfOptional(returnTypeDeclaration)
//    }
//    val location = node.getLocation
//    val methodSignature = createMethodSignature(methodName, parameterTypeDeclarations)
//    c.abort(c.enclosingPosition, Message.DOMALA4147.getMessage(location.getExpression, Integer.valueOf(location.getPosition), className, methodSignature))
  }

  override def visitFunctionOperatorNode(node: FunctionOperatorNode, p: Void): TypeDeclaration[C] = {
    val methodName = node.getMethodName
    val parameterTypeDeclarations = new ParameterCollector().collect(node.getParametersNode)
    if(node.getExpression.startsWith("@")) {
      val typeDeclaration = getExpressionFunctionsDeclaration(node)
      val methodDeclarations = typeDeclaration.getMethodDeclarations(methodName, parameterTypeDeclarations)
      if (methodDeclarations.isEmpty) {
        val location = node.getLocation
        val methodSignature = createMethodSignature(methodName, parameterTypeDeclarations)
        ReflectionUtil.abort(Message.DOMALA4072, location.getExpression, Integer.valueOf(location.getPosition), methodSignature)
      }
      if (methodDeclarations.size == 1) {
        val methodDeclaration = methodDeclarations.head
        val returnTypeDeclaration = methodDeclaration.getReturnTypeDeclaration
        if (returnTypeDeclaration != null) return returnTypeDeclaration
      }
    } else {
      parameterTypeMap.get(methodName).foreach { tpe =>
        val typeDeclaration = TypeDeclaration.newTypeDeclaration(c)(tpe)
        val methodDeclarations = typeDeclaration.getMethodDeclarations("apply", parameterTypeDeclarations)
        if (methodDeclarations.size == 1) {
          val methodDeclaration = methodDeclarations.head
          val returnTypeDeclaration = methodDeclaration.getReturnTypeDeclaration
          if (returnTypeDeclaration != null) {
            validatedParameterNames.add(methodName)
            return convertIfOptional(returnTypeDeclaration)
          }
        }
      }
    }
    val location = node.getLocation
    val methodSignature = createMethodSignature(methodName, parameterTypeDeclarations)
    ReflectionUtil.abort(Message.DOMALA4072, location.getExpression, Integer.valueOf(location.getPosition), methodSignature)
  }

  // TODO exprFunctions
  protected def getExpressionFunctionsDeclaration(node: FunctionOperatorNode): TypeDeclaration[C] = {
    TypeDeclaration.newTypeDeclaration(c)(typeOf[ExpressionFunctions])
  }

  protected def createMethodSignature(methodName: String, parameterTypeDeclarations: mutable.Buffer[TypeDeclaration[C]]): String = {
    val buf = new StringBuilder
    buf.append(methodName)
    buf.append("(")
    if (parameterTypeDeclarations.nonEmpty) {
      for (declaration <- parameterTypeDeclarations) {
        buf.append(declaration.tpe)
        buf.append(", ")
      }
      buf.setLength(buf.length - 2)
    }
    buf.append(")")
    buf.toString
  }

  override def visitFieldOperatorNode(node: FieldOperatorNode, p: Void): TypeDeclaration[C] = {
    val typeDeclaration = node.getTargetObjectNode.accept(this, p)
    val fieldName = node.getFieldName
    val fieldDeclaration = typeDeclaration.getFieldDeclaration(fieldName)
    if (fieldDeclaration != null) {
      val fieldTypeDeclaration = fieldDeclaration.getTypeDeclaration
      if (fieldTypeDeclaration != null) return convertIfOptional(fieldTypeDeclaration)
    }
    val methodDeclarations = typeDeclaration.getMethodDeclarations(fieldName, mutable.Buffer.empty)
    if (methodDeclarations.size == 1) {
      val methodDeclaration = methodDeclarations.head
      val returnTypeDeclaration = methodDeclaration.getReturnTypeDeclaration
      if (returnTypeDeclaration != null) return convertIfOptional(returnTypeDeclaration)
    }
    val location = node.getLocation
    ReflectionUtil.abort(Message.DOMALA4114, location.getExpression, Integer.valueOf(location.getPosition), node.getTargetObjectNode.getExpression, typeDeclaration.getBinaryName, fieldName)
  }

  override def visitStaticFieldOperatorNode(node: StaticFieldOperatorNode, p: Void): TypeDeclaration[C] = {
    val location = node.getLocation
    ReflectionUtil.abort(domala.message.Message.DOMALA6004, location.getExpression, Integer.valueOf(location.getPosition))

// Scala reflection: cannot invoke Java static methods and fields
// https://issues.scala-lang.org/browse/SI-6459

//    val className = node.getClassName
//    val typeElement = ElementUtil.getTypeElement(c)(node.getClassName).getOrElse{
//      val location = node.getLocation
//      c.abort(c.enclosingPosition, Message.DOMALA4145.getMessage(location.getExpression, Integer.valueOf(location.getPosition), className))
//    }
//    val typeDeclaration = TypeDeclaration.newTypeDeclaration(c)(typeElement)
//    val fieldName = node.getFieldName
//    val fieldDeclaration = typeDeclaration.getStaticFieldDeclaration(fieldName)
//    if (fieldDeclaration != null) {
//      val fieldTypeDeclaration = fieldDeclaration.getTypeDeclaration
//      if (fieldTypeDeclaration != null) return convertIfOptional(fieldTypeDeclaration)
//    }
//    val location = node.getLocation
//    c.abort(c.enclosingPosition, Message.DOMALA4145.getMessage(location.getExpression, Integer.valueOf(location.getPosition), className, fieldName))
  }

  override def visitVariableNode(node: VariableNode, p: Void): TypeDeclaration[C] = {
    val variableName = node.getExpression
    val tpe = parameterTypeMap.get(variableName)
    if (tpe.isEmpty) {
      val location = node.getLocation
      ReflectionUtil.abort(Message.DOMALA4067, variableName, Integer.valueOf(location.getPosition))
    }
    validatedParameterNames.add(variableName)
    // TODO: feed back
    //TypeDeclaration.newTypeDeclaration(c)(tpe.get)
    convertIfOptional(TypeDeclaration.newTypeDeclaration(c)(tpe.get))
  }

  protected def convertIfOptional(typeDeclaration: TypeDeclaration[C]): TypeDeclaration[C] = {
    import c.universe._
    if (typeDeclaration.tpe <:< typeOf[java.util.Optional[_]] || typeDeclaration.tpe <:< typeOf[Option[_]]) {
      val typeParameterDeclaration = typeDeclaration.getTypeParameterDeclarations.headOption.getOrElse(c.abort(c.enclosingPosition, typeDeclaration.toString))
      return TypeDeclaration.newTypeDeclaration[C](c)(typeParameterDeclaration.actualType)
    }
    else if (typeDeclaration.tpe <:< typeOf[OptionalInt]) return TypeDeclaration.newTypeDeclaration(c)(typeOf[Integer])
    else if (typeDeclaration.tpe <:< typeOf[OptionalLong]) return TypeDeclaration.newTypeDeclaration(c)(typeOf[Long])
    else if (typeDeclaration.tpe <:< typeOf[OptionalDouble]) return TypeDeclaration.newTypeDeclaration(c)(typeOf[Double])
    typeDeclaration
  }

  protected class ParameterCollector extends ExpressionNodeVisitor[Void, mutable.Buffer[TypeDeclaration[C]]] {

    def collect(node: ExpressionNode): mutable.Buffer[TypeDeclaration[C]] = {
      val results = mutable.Buffer[TypeDeclaration[C]]()
      node.accept(this, results)
      results
    }

    override def visitEqOperatorNode(node: EqOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitNeOperatorNode(node: NeOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitGeOperatorNode(node: GeOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitGtOperatorNode(node: GtOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitLeOperatorNode(node: LeOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitLtOperatorNode(node: LtOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitCommaOperatorNode(node: CommaOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      node.getNodes.forEach( expressionNode =>
        expressionNode.accept(this, p)
      )
      null
    }

    def visitLiteralNode(node: LiteralNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitVariableNode(node: VariableNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitOrOperatorNode(node: OrOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitAndOperatorNode(node: AndOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitNotOperatorNode(node: NotOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitAddOperatorNode(node: AddOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitSubtractOperatorNode(node: SubtractOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitMultiplyOperatorNode(node: MultiplyOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitDivideOperatorNode(node: DivideOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitModOperatorNode(node: ModOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitNewOperatorNode(node: NewOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitMethodOperatorNode(node: MethodOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitStaticMethodOperatorNode(node: StaticMethodOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitFunctionOperatorNode(node: FunctionOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitFieldOperatorNode(node: FieldOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    override def visitStaticFieldOperatorNode(node: StaticFieldOperatorNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      validate(node, p)
      null
    }

    def visitParensNode(node: ParensNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = {
      node.getNode.accept(this, p)
      null
    }

    def visitEmptyNode(node: EmptyNode, p: mutable.Buffer[TypeDeclaration[C]]): Void = null

    protected def validate(node: ExpressionNode, p: mutable.Buffer[TypeDeclaration[C]]): Unit = {
      val result = ExpressionValidator.this.validateInternal(node)
      p += result
    }

  }

}
