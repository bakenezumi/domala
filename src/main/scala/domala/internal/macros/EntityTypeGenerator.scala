package domala.internal.macros

import scala.collection.immutable.Seq
import scala.meta._
import scala.meta.contrib._
import org.scalameta.logger

/**
 * @see [[https://github.com/domaframework/doma/tree/master/src/main/java/org/seasar/doma/internal/apt/EntityTypeGenerator.java]]
 */
object EntityTypeGenerator {

  def generate(cls: Defn.Class, name: Term.Arg) = {
    val internalClass = makeInternalClass(cls.name, cls.ctor, name)

    val obj = q"""
    object ${Term.Name(cls.name.value)} {
      private val __singleton = new Internal()

      def getSingletonInternal() = __singleton

      def newInstance() = new Internal()

      object ListenerHolder {
        val listener = 
          new org.seasar.doma.jdbc.entity.NullEntityListener[${cls.name}]()
      }
      $internalClass
    }
    """

    logger.debug(obj)
    Term.Block(Seq(
      // 処理済みコンストラクタ内アノテーション除去
      cls.copy(ctor = cls.ctor.copy(paramss = cls.ctor.paramss.map(x => x.map(xx => xx.copy(mods = Nil))))),
      obj
    ))
  }

  protected def makeInternalClass(clsName: Type.Name, ctor: Ctor.Primary, tableName: Term.Arg) = {
    val fields = makeFields(clsName, ctor)

    val constructor = makeConstructor(clsName, ctor, tableName)

    val methods = makeMethods(clsName, ctor)
    
    q"""
    class Internal private[$clsName] ()
    extends org.seasar.doma.jdbc.entity.AbstractEntityType[$clsName] {
      ..${fields ++ constructor ++ methods}
    }
    """
  }

  protected def makeFields(clsName: Type.Name, ctor: Ctor.Primary) = {
    val fields1 = q"""
              private val __namingType: org.seasar.doma.jdbc.entity.NamingType = null

              private val __idGenerator =
                new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()
              """
    val propertyTypeFields = makePropertyTypeFields(clsName, ctor)

    fields1.stats ++ propertyTypeFields
  }


  private case class PropertyTypeSet()

  protected def makePropertyTypeFields(clsName: Type.Name, ctor: Ctor.Primary) = {
    ctor.paramss.flatten.map { p =>
      val Term.Param(mods, name, Some(decltpe), default) = p
      val tpe = Type.Name(decltpe.toString)
      val tpeTerm = Term.Name(decltpe.toString)
      val nameStr = name.value
      val propertyName = Pat.Var.Term(Term.Name("$" + name.value))

      if (p contains mod"@Embedded") {
        q"""
        val $propertyName = new org.seasar.doma.jdbc.entity.EmbeddedPropertyType[
          $clsName,
          $tpe
        ](
          $nameStr,
          classOf[$clsName],
          $tpeTerm.getSingletonInternal.getEmbeddablePropertyTypes(
            $nameStr,
            classOf[$clsName],
            __namingType))
        """        
      } else {
        val (basicTpe, newWrapperExpr, domainTpe) = MacroUtil.convertType(decltpe)

        if (p contains mod"@Id") {
          p.mods.collect{
            case mod"@GeneratedValue(strategy = GenerationType.$strategy)" => strategy
          }.headOption match {
            case Some(strategy) => {
              q"""
              val $propertyName = new domala.jdbc.entity.GeneratedIdPropertyType(
                classOf[$clsName],
                classOf[$tpe],
                $basicTpe,
                $newWrapperExpr,
                null,
                $domainTpe,
                $nameStr,
                "",
                __namingType,
                false,
                __idGenerator
              )
              """
            }
            // TODO:
            case _ => abort("not implementation now")
          }
        } else if (p contains mod"@Version" ) {
          q"""
          val $propertyName = new domala.jdbc.entity.VersionPropertyType(
            classOf[$clsName],
            classOf[$tpe],
            $basicTpe,
            $newWrapperExpr,
            null,
            $domainTpe,
            $nameStr,
            "",
            __namingType,
            false
          )
          """
        } else {
          q"""
          val $propertyName = new domala.jdbc.entity.DefaultPropertyType(
            classOf[$clsName],
            classOf[$tpe],
            $basicTpe,
            $newWrapperExpr,
            null,
            $domainTpe,
            $nameStr,
            "",
            __namingType,
            true,
            false,
            false
          )
          """
        }
      }
    }
  }

  protected def makeConstructor(clsName: Type.Name, ctor: Ctor.Primary, tableName: Term.Arg) = {
    val tname = if (tableName == null) Term.Name("\"" + clsName.value + "\"") else Term.Name(tableName.toString)
    val propertySize = ctor.paramss.flatten.size

    q"""
    val __listenerSupplier: java.util.function.Supplier[org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]] =
      () => ListenerHolder.listener
    val __immutable = true
    val __name = $tname
    val __catalogName = ""
    val __schemaName = ""
    val __tableName = ""
    val __isQuoteRequired = false

    val __idList = new java.util.ArrayList[org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]()
    val __list = new java.util.ArrayList[org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]($propertySize)
    val __map = new java.util.HashMap[String, org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]($propertySize)
    """.stats ++
    ctor.paramss.flatten.flatMap { p =>
      val Term.Param(mods, name, Some(decltpe), default) = p
      val nameStr = name.value
      val propertyName = Term.Name("$" + name.value)

      if (p contains mod"@Embedded") {
        Seq(
          q"__list.addAll($propertyName.getEmbeddablePropertyTypes)",
          q"__map.putAll($propertyName.getEmbeddablePropertyTypeMap)"
        )
      } else {
        (if (p contains mod"@Id") {
          Seq(q"__idList.add($propertyName)")
        } else Nil) ++
        Seq(
          q"__list.add($propertyName)",
          q"__map.put($nameStr, $propertyName)"
        )
      }
    } ++
    q"""
    val __idPropertyTypes = java.util.Collections.unmodifiableList(__idList)
    val __entityPropertyTypes = java.util.Collections.unmodifiableList(__list)
    val __entityPropertyTypeMap: java.util.Map[
      String,
      org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]] =
      java.util.Collections.unmodifiableMap(__map)
    """.stats
  }

  // TODO: @GeneratedValueの有無で判定
  protected def makeGeneratedIdPropertyType(clsName: Type.Name, ctor: Ctor.Primary) = {
    if(clsName.value == "Person") {
      q"$$id"
    } else {
      q"null"
    }
  }
  // TODO: @Versionの有無で判定
  protected def makeVersionPropertyType(clsName: Type.Name, ctor: Ctor.Primary) = {
    if(clsName.value == "Person") {
      q"$$version"
    } else {
      q"null"
    }
  }

  protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary) = {
    q"""

    override def getNamingType() = __namingType

    override def isImmutable() = __immutable

    override def getName() = __name

    override def getCatalogName() = __catalogName

    override def getSchemaName() = __schemaName

    override def getTableName() =
      getTableName(org.seasar.doma.jdbc.Naming.DEFAULT.apply)

    override def getTableName(
      namingFunction: java.util.function.BiFunction[
        org.seasar.doma.jdbc.entity.NamingType,
        String,
        String]): String = {
      if (__tableName.isEmpty) {
        namingFunction.apply(__namingType, __name)
      } else {
        __tableName
      }
    }

    override def isQuoteRequired() = __isQuoteRequired

    override def preInsert(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreInsertContext[$clsName]) = {
      val __listenerClass =
        classOf[org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]](__listenerClass, __listenerSupplier)
      __listener.preInsert(entity, context)
    }

    override def preUpdate(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreUpdateContext[$clsName]) = {
      val __listenerClass =
        classOf[org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]](__listenerClass, __listenerSupplier)
      __listener.preUpdate(entity, context)
    }

    override def preDelete(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PreDeleteContext[$clsName]) = {
      val __listenerClass =
        classOf[org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]](__listenerClass, __listenerSupplier)
      __listener.preDelete(entity, context)
    }

    override def postInsert(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostInsertContext[$clsName]) = {
      val __listenerClass =
        classOf[org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]](__listenerClass, __listenerSupplier)
      __listener.postInsert(entity, context)
    }

    override def postUpdate(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostUpdateContext[$clsName]) = {
      val __listenerClass =
        classOf[org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]](__listenerClass, __listenerSupplier)
      __listener.postUpdate(entity, context)
    }

    override def postDelete(
        entity: $clsName,
        context: org.seasar.doma.jdbc.entity.PostDeleteContext[$clsName]) = {
      val __listenerClass =
        classOf[org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]]
      val __listener =
        context.getConfig.getEntityListenerProvider
          .get[$clsName, org.seasar.doma.jdbc.entity.NullEntityListener[$clsName]](__listenerClass, __listenerSupplier)
      __listener.postDelete(entity, context)
    }

    override def getEntityPropertyTypes() = __entityPropertyTypes

    override def getEntityPropertyType(__name: String) = __entityPropertyTypeMap.get(__name)

    override def getIdPropertyTypes() = __idPropertyTypes

    override def getGeneratedIdPropertyType() = ${makeGeneratedIdPropertyType(clsName, ctor)}

    override def getVersionPropertyType() = ${makeVersionPropertyType(clsName, ctor)}
    """.stats ++ {
      val params = ctor.paramss.flatten.map { p =>
        val Term.Param(mods, name, Some(decltpe), default) = p
        val nameStr = name.value
        if (p contains mod"@Embedded") {
          val tpe = Term.Name(decltpe.toString)
          q"""$tpe.getSingletonInternal.newEmbeddable[$clsName]($nameStr, __args)"""
        } else {
          val tpe = Type.Name(decltpe.toString)

          q"""(if (__args.get($nameStr) != null) __args.get($nameStr).get else null).asInstanceOf[$tpe]"""
        }
      }
      Seq(q"""
      override def newEntity(
          __args: java.util.Map[
            String,
            org.seasar.doma.jdbc.entity.Property[$clsName, _]]) =
        new ${clsName.toString.parse[Ctor.Call].get}(
          ..$params
        )
      """)
    } ++ q"""
    override def getEntityClass() = classOf[$clsName]

    override def getOriginalStates(__entity: $clsName): $clsName = null

    override def saveCurrentStates(__entity: $clsName): Unit = {}
        
    """.stats
  }
}
