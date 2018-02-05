package domala.internal.macros.reflect
import domala.jdbc.{BatchResult, Config, Result}

import scala.reflect.macros.blackbox
object EntityManagerMacros {

  private def templateSingle[ENTITY: c.WeakTypeTag](c: blackbox.Context)(
    tpe: c.Type,
    entity: c.Expr[ENTITY],
    config: c.Expr[Config],
    methodName: String
  ): c.Expr[Result[ENTITY]] = {
    import c.universe._
    val method = TermName(methodName + "Method")
    val methodNameLiteral = Literal(Constant(methodName+ "[" + tpe.typeSymbol.fullName + "]"))
    val capitalizeMethodName = methodName.capitalize

    c.Expr[Result[ENTITY]] {
      q"""{
        val __desc = domala.jdbc.EntityDescProvider.get[$tpe]
        val __method = domala.jdbc.EntityManagerMethods.$method
        $config.getJdbcLogger().logDaoMethodEntering("domala.jdbc.EntityManager", $methodNameLiteral, $entity)
        try {
          if ($entity == null) {
            throw new org.seasar.doma.DomaNullPointerException("entity")
          }
          val __query = $config.getQueryImplementors.${TermName("createAuto" + capitalizeMethodName + "Query")}(__method, __desc)
          __query.setMethod(__method)
          __query.setConfig($config)
          __query.setEntity($entity)
          __query.setCallerClassName("domala.jdbc.EntityManager")
          __query.setCallerMethodName($methodNameLiteral)
          __query.setQueryTimeout(-1)
          __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
          __query.prepare()
          val __command = $config.getCommandImplementors.${TermName("create" + capitalizeMethodName + "Command")}(__method, __query)
          val __count = __command.execute()
          __query.complete()
          val __result = domala.jdbc.Result[$tpe](__count, __query.getEntity)
          $config.getJdbcLogger().logDaoMethodExiting("domala.jdbc.EntityManager", $methodNameLiteral, __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            $config.getJdbcLogger().logDaoMethodThrowing("domala.jdbc.EntityManager", $methodNameLiteral, __e)
            throw __e
        }
      }"""
    }
  }


  def insert[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[Config]): c.Expr[Result[ENTITY]] = {
    import c.universe._
    val tpe = weakTypeOf[ENTITY]
    templateSingle(c)(tpe, entity, config, "insert")
  }
  def update[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[Config]): c.Expr[Result[ENTITY]] = {
    import c.universe._
    val tpe = weakTypeOf[ENTITY]
    templateSingle(c)(tpe, entity, config, "update")
  }
  def delete[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entity: c.Expr[ENTITY])(config: c.Expr[Config]): c.Expr[Result[ENTITY]] = {
    import c.universe._
    val tpe = weakTypeOf[ENTITY]
    templateSingle(c)(tpe, entity, config, "delete")
  }

  private def templateBatch[ENTITY: c.WeakTypeTag](c: blackbox.Context)(
    tpe: c.Type,
    entities: c.Expr[Iterable[ENTITY]],
    config: c.Expr[Config],
    methodName: String
  ): c.Expr[BatchResult[ENTITY]] = {
    import c.universe._
    val method = TermName(methodName + "Method")
    val methodNameLiteral = Literal(Constant(methodName + "[" + tpe.typeSymbol.fullName + "]"))
    val capitalizeMethodName = methodName.capitalize

    c.Expr[BatchResult[ENTITY]] {
      q"""{
        import scala.collection.JavaConverters._
        val __desc = domala.jdbc.EntityDescProvider.get[$tpe]
        val __method = domala.jdbc.EntityManagerMethods.$method
        $config.getJdbcLogger().logDaoMethodEntering("domala.jdbc.EntityManager", $methodNameLiteral, $entities)
        try {
          if ($entities == null) {
            throw new org.seasar.doma.DomaNullPointerException("entity")
          }
          val __query = $config.getQueryImplementors.${TermName("createAuto" + capitalizeMethodName + "Query")}(__method, __desc)
          __query.setMethod(__method)
          __query.setConfig($config)
          __query.setEntities($entities.asJava)
          __query.setCallerClassName("domala.jdbc.EntityManager")
          __query.setCallerMethodName($methodNameLiteral)
          __query.setBatchSize(-1)
          __query.setQueryTimeout(-1)
          __query.setSqlLogType(org.seasar.doma.jdbc.SqlLogType.FORMATTED)
          __query.prepare()
          val __command = $config.getCommandImplementors.${TermName("create" + capitalizeMethodName + "Command")}(__method, __query)
          val __count = __command.execute()
          __query.complete()
          val __result = domala.jdbc.BatchResult[$tpe](__count, __query.getEntities.asScala)
          $config.getJdbcLogger().logDaoMethodExiting("domala.jdbc.EntityManager", $methodNameLiteral, __result)
          __result
        } catch {
          case __e: java.lang.RuntimeException =>
            $config.getJdbcLogger().logDaoMethodThrowing("domala.jdbc.EntityManager", $methodNameLiteral, __e)
            throw __e
        }
      }"""
    }
  }

  def batchInsert[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[Config]): c.Expr[BatchResult[ENTITY]] = {
    import c.universe._
    val tpe = weakTypeOf[ENTITY]
    templateBatch(c)(tpe, entities, config, "batchInsert")
  }
  def batchUpdate[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[Config]): c.Expr[BatchResult[ENTITY]] = {
    import c.universe._
    val tpe = weakTypeOf[ENTITY]
    templateBatch(c)(tpe, entities, config, "batchUpdate")
  }
  def batchDelete[ENTITY: c.WeakTypeTag](c: blackbox.Context)(entities: c.Expr[Iterable[ENTITY]])(config: c.Expr[Config]): c.Expr[BatchResult[ENTITY]] = {
    import c.universe._
    val tpe = weakTypeOf[ENTITY]
    templateBatch(c)(tpe, entities, config, "batchDelete")
  }

}
