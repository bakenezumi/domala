package domala.internal.macros.reflect

import domala.message.Message
import org.seasar.doma.internal.jdbc.sql.node._

import scala.reflect.macros.blackbox

class BatchSqlValidator[C <: blackbox.Context](c: C)(
  trtName: String,
  defName: String,
  expandable: Boolean,
  populatable: Boolean,
  paramTypeMap: ReflectionHelper[C]#ParamMap,
  suppress: Seq[String]
) extends SqlValidator(c)(
  trtName,
  defName,
  expandable,
  populatable,
  paramTypeMap
)
  {

  override def visitEmbeddedVariableNode(node: EmbeddedVariableNode, p: Void): Void = {
    if (!isSuppressed(Message.DOMALA4181)) {
      c.warning(c.enclosingPosition, Message.DOMALA4181.getMessage(trtName, defName))
    }
    super.visitEmbeddedVariableNode(node, p)
  }

  override def visitIfNode(node: IfNode, p: Void): Void = {
    if (!isSuppressed(Message.DOMALA4182)) {
      c.warning(c.enclosingPosition, Message.DOMALA4182.getMessage(trtName, defName))
    }
    super.visitIfNode(node, p)
  }

  override def visitForNode(node: ForNode, p: Void): Void = {
    if (!isSuppressed(Message.DOMALA4183)) {
      c.warning(c.enclosingPosition, Message.DOMALA4183.getMessage(trtName, defName))
    }
    super.visitForNode(node, p)
  }

  protected def isSuppressed(message: Message): Boolean = {
    suppress.contains(message.toString)
  }
}
