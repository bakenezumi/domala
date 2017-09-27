package domala.internal.macros

import domala.internal.macros.decl.TypeDeclaration
import domala.message.Message
import org.seasar.doma.internal.expr.node.ExpressionNode
import org.seasar.doma.internal.expr.{ExpressionException, ExpressionParser}
import org.seasar.doma.internal.jdbc.sql.SimpleSqlNodeVisitor
import org.seasar.doma.jdbc.SqlNode
import org.seasar.doma.internal.jdbc.sql.node._

import scala.collection.immutable
import scala.meta._

class SqlValidator(
  trtName: Type.Name,
  defDecl: QueryDefDecl,
  expandable: Boolean,
  populatable: Boolean
) extends SimpleSqlNodeVisitor[Void, Void]{
  val SQL_MAX_LENGTH = 5000
  val expressionValidator = new ExpressionValidator
  val parameterSet: immutable.Seq[Term.Param] = defDecl.paramss.flatten.filter { p =>
    // query内で使用されないパラメータを除外
    p.decltpe.get match {
      case t"$_ => $_" => false
      case _ => true
    }
  }

  def validate(sqlNode: SqlNode): Unit = {
    sqlNode.accept(this, null)
    val validatedParameterNames = expressionValidator.validatedParameterNames
    parameterSet.map(_.name.syntax).foreach{ parameterName =>
      if (!validatedParameterNames.contains(parameterName)) {
        abort(org.seasar.doma.message.Message.DOMA4122.getMessage(trtName.syntax, defDecl.name.syntax, parameterName))
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
        abort(Message.DOMALA4153.getMessage(trtName.syntax, defDecl.name.syntax, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), variableName, typeDeclaration.getBinaryName))
      } else if (!isScalarIterable(typeDeclaration)) {
        val sql = getSql(location)
        abort(Message.DOMALA4161.getMessage(trtName.syntax, defDecl.name.syntax, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), variableName, typeDeclaration.getBinaryName))
      }
    }
    visitNode(node, p)
    null
  }

  protected def isScalar(typeDeclaration: TypeDeclaration): Boolean = {
    val typeMirror = typeDeclaration.getType
    // TODO
    //BasicCtType.newInstance(typeMirror, env) != null || DomainCtType.newInstance(typeMirror, env) != null
    true
  }

  protected def isScalarIterable(typeDeclaration: TypeDeclaration): Boolean = {
    val typeMirror = typeDeclaration.getType
//    val iterableCtType = IterableCtType.newInstance(typeMirror, env)
//    if (iterableCtType != null) return iterableCtType.getElementCtType.accept(new SimpleCtTypeVisitor[Boolean, Void, RuntimeException](false) {
//      @throws[RuntimeException]
//      override def visitBasicCtType(ctType: BasicCtType, p: Void) = true
//
//      @throws[RuntimeException]
//
//      override def visitDomainCtType(ctType: DomainCtType, p: Void) = true
//    }, null)
    true
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
      abort(Message.DOMALA4140.getMessage(trtName.syntax, defDecl.name.syntax, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), expression, typeDeclaration.getBinaryName))
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
      abort(Message.DOMALA4141.getMessage(trtName.syntax, defDecl.name.syntax, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), expression, typeDeclaration.getBinaryName))
    }
    visitNode(node, p)
    null
  }

  def visitForNode(node: Nothing, p: Void): Void = {
    // TODO:
    null
  }

  override def visitExpandNode(node: ExpandNode, p: Void): Void = {
    if (!expandable) {
      val location = node.getLocation
      val sql = getSql(location)
      abort(Message.DOMALA4257.getMessage(trtName.syntax, defDecl.name.syntax, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition)))
    }
    visitNode(node, p)
  }

  override def visitPopulateNode(node: PopulateNode, p: Void): Void = {
    if (!populatable) {
      val location = node.getLocation
      val sql = getSql(location)
      abort(Message.DOMALA4270.getMessage(trtName.syntax, defDecl.name.syntax, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition)))
    }
    parameterSet.map(_.name.syntax).foreach(name => expressionValidator.addValidatedParameterName(name))
    visitNode(node, p)
  }
  override protected def defaultAction(node: SqlNode, p: Void): Void = visitNode(node, p)

  protected def visitNode(node: SqlNode, p: Void): Void = {
    node.getChildren.forEach(child => child.accept(this, p))
    null
  }

  protected def validateExpressionVariable(location: SqlLocation, expression: String): TypeDeclaration = {
    val expressionNode = parseExpression(location, expression)
    expressionValidator.validate(expressionNode)
  }

  protected def parseExpression(location: SqlLocation, expression: String): ExpressionNode = try {
    val parser = new ExpressionParser(expression)
    parser.parse()
  } catch {
    case e: ExpressionException =>
      val sql = getSql(location)
      abort(org.seasar.doma.message.Message.DOMA4092.getMessage(trtName.syntax, defDecl.name.syntax, sql, Integer.valueOf(location.getLineNumber), Integer.valueOf(location.getPosition), e.getMessage))
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
