package domala.internal.macros.reflect

import domala.message.Message
import org.seasar.doma.internal.jdbc.sql.node._

import scala.reflect.macros.blackbox

class BatchSqlValidator[C <: blackbox.Context](c: C)(
  daoTpe: C#Type,
  defName: String,
  expandable: Boolean,
  populatable: Boolean,
  paramTypeMap: ReflectionHelper[C]#ParamMap,
  suppress: Seq[String]
) extends SqlValidator(c)(
  daoTpe,
  defName,
  expandable,
  populatable,
  paramTypeMap
) {

  override def visitEmbeddedVariableNode(node: EmbeddedVariableNode, p: Void): Void = {
    if (!isSuppressed(Message.DOMALA4181)) {
      c.warning(c.enclosingPosition, Message.DOMALA4181.getMessage(daoTpe, defName))
    }
    super.visitEmbeddedVariableNode(node, p)
  }

  override def visitIfNode(node: IfNode, p: Void): Void = {
    if (!isSuppressed(Message.DOMALA4182)) {
      c.warning(daoTpe.typeSymbol.pos.asInstanceOf, Message.DOMALA4182.getMessage(daoTpe, defName))
    }
    super.visitIfNode(node, p)
  }

  override def visitForNode(node: ForNode, p: Void): Void = {
    if (!isSuppressed(Message.DOMALA4183)) {
      c.warning(daoTpe.typeSymbol.pos.asInstanceOf, Message.DOMALA4183.getMessage(daoTpe, defName))
    }
    super.visitForNode(node, p)
  }

  protected def isSuppressed(message: Message): Boolean = {
    suppress.contains(message.toString)
  }
}
