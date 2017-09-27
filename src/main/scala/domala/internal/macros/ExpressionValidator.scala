package domala.internal.macros

import domala.internal.macros.decl.TypeDeclaration
import org.seasar.doma.internal.expr.node._

import scala.collection.mutable

class ExpressionValidator extends ExpressionNodeVisitor[TypeDeclaration, Void] {
  val validatedParameterNames: mutable.Set[String] = mutable.Set[String]()

  def validate(node: ExpressionNode): TypeDeclaration = validateInternal(node)

  def validateInternal(node: ExpressionNode): TypeDeclaration = node.accept(this, null)

  def addValidatedParameterName(name: String): Unit = {
    validatedParameterNames.add(name)
  }

  override def visitGeOperatorNode(geOperatorNode: GeOperatorNode, p: Void): TypeDeclaration = ???

  override def visitDivideOperatorNode(divideOperatorNode: DivideOperatorNode, p: Void): TypeDeclaration = ???

  override def visitEmptyNode(emptyNode: EmptyNode, p: Void): TypeDeclaration = ???

  override def visitStaticMethodOperatorNode(staticMethodOperatorNode: StaticMethodOperatorNode, p: Void): TypeDeclaration = ???

  override def visitNeOperatorNode(neOperatorNode: NeOperatorNode, p: Void): TypeDeclaration = ???

  override def visitLeOperatorNode(leOperatorNode: LeOperatorNode, p: Void): TypeDeclaration = ???

  override def visitNotOperatorNode(notOperatorNode: NotOperatorNode, p: Void): TypeDeclaration = ???

  override def visitFunctionOperatorNode(functionOperatorNode: FunctionOperatorNode, p: Void): TypeDeclaration = ???

  override def visitLiteralNode(literalNode: LiteralNode, p: Void): TypeDeclaration = ???

  override def visitVariableNode(variableNode: VariableNode, p: Void): TypeDeclaration = ???

  override def visitFieldOperatorNode(fieldOperatorNode: FieldOperatorNode, p: Void): TypeDeclaration = ???

  override def visitAddOperatorNode(addOperatorNode: AddOperatorNode, p: Void): TypeDeclaration = ???

  override def visitStaticFieldOperatorNode(staticFieldOperatorNode: StaticFieldOperatorNode, p: Void): TypeDeclaration = ???

  override def visitGtOperatorNode(gtOperatorNode: GtOperatorNode, p: Void): TypeDeclaration = ???

  override def visitNewOperatorNode(newOperatorNode: NewOperatorNode, p: Void): TypeDeclaration = ???

  override def visitCommaOperatorNode(commaOperatorNode: CommaOperatorNode, p: Void): TypeDeclaration = ???

  override def visitEqOperatorNode(eqOperatorNode: EqOperatorNode, p: Void): TypeDeclaration = ???

  override def visitParensNode(parensNode: ParensNode, p: Void): TypeDeclaration = ???

  override def visitLtOperatorNode(ltOperatorNode: LtOperatorNode, p: Void): TypeDeclaration = ???

  override def visitSubtractOperatorNode(subtractOperatorNode: SubtractOperatorNode, p: Void): TypeDeclaration = ???

  override def visitModOperatorNode(modOperatorNode: ModOperatorNode, p: Void): TypeDeclaration = ???

  override def visitOrOperatorNode(orOperatorNode: OrOperatorNode, p: Void): TypeDeclaration = ???

  override def visitMultiplyOperatorNode(multiplyOperatorNode: MultiplyOperatorNode, p: Void): TypeDeclaration = ???

  override def visitAndOperatorNode(andOperatorNode: AndOperatorNode, p: Void): TypeDeclaration = ???

  override def visitMethodOperatorNode(methodOperatorNode: MethodOperatorNode, p: Void) = ???

}
