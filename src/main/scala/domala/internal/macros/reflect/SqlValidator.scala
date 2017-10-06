package domala.internal.macros.reflect

import domala.internal.macros.reflect.decl.TypeDeclaration
import domala.internal.macros.reflect.util.TypeUtil
import domala.message.Message
import org.seasar.doma.internal.expr.node.ExpressionNode
import org.seasar.doma.internal.expr.{ExpressionException, ExpressionParser}
import org.seasar.doma.internal.jdbc.sql.SimpleSqlNodeVisitor
import org.seasar.doma.internal.jdbc.sql.node._
import org.seasar.doma.jdbc.SqlNode

import scala.reflect.macros.blackbox

class SqlValidator[C <: blackbox.Context](val c: C)(
  trtName: String,
  defName: String,
  expandable: Boolean,
  populatable: Boolean,
  paramTypeMap: ReflectionHelper[C]#ParamMap
) extends SimpleSqlNodeVisitor[Void, Void]{
  val SQL_MAX_LENGTH = 5000
  val expressionValidator = new ExpressionValidator[C](c)(trtName, defName, paramTypeMap)

  def validate(sqlNode: SqlNode): Unit = {
    sqlNode.accept(this, null)
    val validatedParameterNames = expressionValidator.validatedParameterNames
    paramTypeMap.keys.foreach{ parameterName =>
      if (!validatedParameterNames.contains(parameterName)) {
        c.abort(c.enclosingPosition, Message.DOMALA4122.getMessage(trtName, defName, parameterName))
      }
    }
  }

  override def visitBindVariableNode(node: BindVariableNode, p: Void): Void = visitValueNode(node, p)

  override def visitLiteralVariableNode(node: LiteralVariableNode, p: Void): Void = visitValueNode(node, p)

  protected def visitValueNode(node: ValueNode, p: Void): Void = {
    val location = node.getLocation
    val variableName = node.getVariableName
    val typeDeclaration = validateExpressionVariable(location, variableName)
    if (node.getWordNode != null) {
      if (!isScalar(typeDeclaration)) {
        val sql = getSql(location)
        c.abort(c.enclosingPosition, Message.DOMALA4153.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), variableName, typeDeclaration.getBinaryName))
      }
    } else {
      if (!isScalarIterable(typeDeclaration)) {
        val sql = getSql(location)
        c.abort(c.enclosingPosition, Message.DOMALA4161.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), variableName, typeDeclaration.getBinaryName))
      }
    }
    visitNode(node, p)
    null
  }

  protected def isScalar(typeDeclaration: TypeDeclaration[C]): Boolean = {
    val tpe: C#Type = typeDeclaration.tpe
    TypeUtil.isBasic(c)(tpe) || TypeUtil.isDomain(c)(tpe)
  }

  protected def isScalarIterable(typeDeclaration: TypeDeclaration[C]): Boolean = {
    val tpe: C#Type = typeDeclaration.tpe
    if(TypeUtil.isIterable(c)(tpe)) {
      TypeUtil.isBasic(c)(tpe.typeArgs.asInstanceOf[List[C#Type]].head) || TypeUtil.isDomain(c)(tpe.typeArgs.asInstanceOf[List[C#Type]].head)
    } else {
      false
    }
  }

  override def visitEmbeddedVariableNode(node: EmbeddedVariableNode, p: Void): Void = {
    val location = node.getLocation
    val variableName = node.getVariableName
    validateExpressionVariable(location, variableName)
    visitNode(node, p)
    null
  }

  override def visitIfNode(node: IfNode, p: Void): Void = {
    val location = node.getLocation
    val expression = node.getExpression
    val typeDeclaration = validateExpressionVariable(location, expression)
    if (!typeDeclaration.isBooleanType) {
      val sql = getSql(location)
      c.abort(c.enclosingPosition, Message.DOMALA4140.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), expression, typeDeclaration.getBinaryName))
    }
    visitNode(node, p)
    null
  }

  override def visitElseifNode(node: ElseifNode, p: Void): Void = {
    val location = node.getLocation
    val expression = node.getExpression
    val typeDeclaration = validateExpressionVariable(location, expression)
    if (!typeDeclaration.isBooleanType) {
      val sql = getSql(location)
      c.abort(c.enclosingPosition, Message.DOMALA4141.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), expression, typeDeclaration.getBinaryName))
    }
    visitNode(node, p)
    null
  }

  override def visitForNode(node: ForNode, p: Void): Void = {
    import c.universe._
    val location = node.getLocation
    val identifier = node.getIdentifier
    val expression = node.getExpression
    val typeDeclaration = validateExpressionVariable(location, expression)
    val tpe: C#Type = typeDeclaration.tpe
    if (!(tpe <:< typeOf[Iterable[_]])) {
      val sql = getSql(location)
      c.abort(c.enclosingPosition, Message.DOMALA4149.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), expression, typeDeclaration.getBinaryName))
    }
    val typeArgs = tpe.typeArgs
    if (typeArgs.isEmpty) {
      val sql = getSql(location)
      c.abort(c.enclosingPosition, Message.DOMALA4150.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), expression, typeDeclaration.getBinaryName))
    }
    val originalIdentifierType: C#Type = expressionValidator.removeParameterType(identifier)
    expressionValidator.putParameterType(identifier, typeArgs.asInstanceOf[List[C#Type]].head)
    val hasNextVariable = identifier + ForBlockNode.HAS_NEXT_SUFFIX
    val originalHasNextType: C#Type = expressionValidator.removeParameterType(hasNextVariable)
    expressionValidator.putParameterType(hasNextVariable, typeOf[Boolean])
    val indexVariable = identifier + ForBlockNode.INDEX_SUFFIX
    val originalIndexType: C#Type = expressionValidator.removeParameterType(indexVariable)
    expressionValidator.putParameterType(indexVariable, typeOf[Int])
    visitNode(node, p)
    if (originalIdentifierType == null) expressionValidator.removeParameterType(identifier)
    else expressionValidator.putParameterType(identifier, originalIdentifierType)
    if (originalHasNextType == null) expressionValidator.removeParameterType(hasNextVariable)
    else expressionValidator.putParameterType(hasNextVariable, originalHasNextType)
    if (originalIndexType == null) expressionValidator.removeParameterType(indexVariable)
    else expressionValidator.putParameterType(indexVariable, originalIndexType)
    null
  }

  override def visitExpandNode(node: ExpandNode, p: Void): Void = {
    if (!expandable) {
      val location = node.getLocation
      val sql = getSql(location)
      c.abort(c.enclosingPosition, Message.DOMALA4257.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition)))
    }
    visitNode(node, p)
  }

  override def visitPopulateNode(node: PopulateNode, p: Void): Void = {
    if (!populatable) {
      val location = node.getLocation
      val sql = getSql(location)
      c.abort(c.enclosingPosition, Message.DOMALA4270.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition)))
    }
    paramTypeMap.keys.foreach(name => expressionValidator.addValidatedParameterName(name))
    visitNode(node, p)
  }
  override protected def defaultAction(node: SqlNode, p: Void): Void = visitNode(node, p)

  protected def visitNode(node: SqlNode, p: Void): Void = {
    node.getChildren.forEach(child => child.accept(this, p))
    null
  }

  protected def validateExpressionVariable(location: SqlLocation, expression: String): TypeDeclaration[C] = {
    val expressionNode = parseExpression(location, expression)
    expressionValidator.validate(expressionNode)
  }

  protected def parseExpression(location: SqlLocation, expression: String): ExpressionNode = try {
    val parser = new ExpressionParser(expression)
    parser.parse()
  } catch {
    case e: ExpressionException =>
      val sql = getSql(location)
      c.abort(c.enclosingPosition, Message.DOMALA4092.getMessage(trtName, defName, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), e.getMessage))
  }

  protected def getSql(location: SqlLocation): String = {
    var sql = location.getSql
    if (sql != null && sql.length > SQL_MAX_LENGTH) {
      sql = sql.substring(0, SQL_MAX_LENGTH)
      sql += org.seasar.doma.message.Message.DOMA4185.getSimpleMessage(Integer.valueOf(SQL_MAX_LENGTH))
    }
    sql
  }
}
