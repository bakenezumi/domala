package domala

import domala.internal.macros.EntityTypeGenerator
import org.seasar.doma.jdbc.entity.EntityListener
import org.seasar.doma.jdbc.entity.{NamingType, NullEntityListener}

import scala.meta._

class Entity(listener: Class[_ <: EntityListener[_ <: Any]] = classOf[NullEntityListener[_]], naming: NamingType = NamingType.NONE) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"new $_(..$params)" = this
    defn match {
      case cls: Defn.Class => EntityTypeGenerator.generate(cls, params)
      case _ => abort("@Entity most annotate a class")
    }
  }
}

package internal { package macros {

  import scala.meta.contrib._
  import org.scalameta.logger
  import scala.collection.immutable.Seq

  case class EntitySetting(
    listener: Type,
    naming: Term with Pat
  )

  /**
    * @see [[https://github.com/domaframework/doma/tree/master/src/main/java/org/seasar/doma/internal/apt/EntityTypeGenerator.java]]
    */
  object EntityTypeGenerator {
    def generate(cls: Defn.Class, args: Seq[Term.Arg]): Term.Block = {
      val entitySetting = EntitySetting(
        args.collectFirst { case arg"listener = classOf[$x]" => x }.getOrElse(t"org.seasar.doma.jdbc.entity.NullEntityListener[${cls.name}]"),
        args.collectFirst { case arg"naming = $x" => Term.Name(x.syntax) }.getOrElse(q"null")
      )
      val tableSetting = TableSetting.read(cls.mods)
      val fields = makeFields(cls.name, cls.ctor, entitySetting)
      val constructor = makeConstructor(cls.name, cls.ctor, entitySetting, tableSetting)
      val methods = makeMethods(cls.name, cls.ctor, entitySetting)

      val obj =
      q"""
      object ${Term.Name(cls.name.syntax)} extends org.seasar.doma.jdbc.entity.AbstractEntityType[${cls.name}] {
        object ListenerHolder {
          val listener =
            new ${entitySetting.listener.syntax.parse[Ctor.Call].get}()
        }
        val instance = this
        ..${fields ++ constructor ++ methods}
      }
      """

      logger.debug(obj)
      Term.Block(Seq(
        // 処理済みアノテーション除去
        cls.copy(
          mods = cls.mods.collect { case m@mod"case" => m },
          ctor = cls.ctor.copy(paramss = cls.ctor.paramss.map(x => x.map(xx => xx.copy(mods = Nil))))
        ),
        obj
      ))
    }

    protected def makeFields(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting): Seq[Stat] = {
      val fields1 =
        q"""
        private val __namingType: org.seasar.doma.jdbc.entity.NamingType = ${entitySetting.naming}
        private val __idGenerator =
          new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()
        """
      val propertyTypeFields = makePropertyTypeFields(clsName, ctor)

      fields1.stats ++ propertyTypeFields
    }


    private case class PropertyTypeSet()

    protected def makePropertyTypeFields(clsName: Type.Name, ctor: Ctor.Primary): Seq[Defn.Val] = {
      ctor.paramss.flatten.map { p =>
        val columnSetting = ColumnSetting.read(p.mods)
        val Term.Param(mods, name, Some(decltpe), default) = p
        val tpe = Type.Name(decltpe.toString)
        val tpeTerm = Term.Name(decltpe.toString)
        val propertyName = Pat.Var.Term(Term.Name("$" + name.syntax))

        if (p contains mod"@Embedded") {
          q"""
          val $propertyName = new org.seasar.doma.jdbc.entity.EmbeddedPropertyType[
            $clsName,
            $tpe
          ](
            ${name.syntax},
            classOf[$clsName],
            $tpeTerm.getEmbeddablePropertyTypes(
              ${name.syntax},
              classOf[$clsName],
              __namingType))
          """
        } else {
          val (basicTpe, newWrapperExpr, domainTpe) = TypeHelper.generateEntityTypeParts(decltpe)

          if (p contains mod"@Id") {
            p.mods.collect {
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
                  ${name.syntax},
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
          } else if (p contains mod"@Version") {
            q"""
            val $propertyName = new domala.jdbc.entity.VersionPropertyType(
              classOf[$clsName],
              classOf[$tpe],
              $basicTpe,
              $newWrapperExpr,
              null,
              $domainTpe,
              ${name.syntax},
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
              ${name.syntax},
              ${columnSetting.name},
              __namingType,
              ${columnSetting.insertable},
              ${columnSetting.updatable},
              ${columnSetting.quote}
            )
            """
          }
        }
      }
    }

    protected def makeConstructor(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting, tableSetting: TableSetting): Seq[Stat] = {
      val propertySize = ctor.paramss.flatten.size

      q"""
      val __listenerSupplier: java.util.function.Supplier[${entitySetting.listener}] =
        () => ListenerHolder.listener
      val __immutable = true
      val __name = ${Term.Name("\"" + clsName.syntax + "\"")}
      val __catalogName = ${Term.Name(tableSetting.catalog.syntax)}
      val __schemaName = ${Term.Name(tableSetting.schema.syntax)}
      val __tableName = ${Term.Name(tableSetting.name.syntax)}
      val __isQuoteRequired = ${tableSetting.quote}
      val __idList = new java.util.ArrayList[org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]()
      val __list = new java.util.ArrayList[org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]($propertySize)
      val __map = new java.util.HashMap[String, org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]($propertySize)
      """.stats ++
          ctor.paramss.flatten.flatMap { p =>
            val Term.Param(mods, name, Some(decltpe), default) = p
            val propertyName = Term.Name("$" + name.syntax)

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
                  q"__map.put(${name.syntax}, $propertyName)"
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

    protected def makeGeneratedIdPropertyType(clsName: Type.Name, ctor: Ctor.Primary): Term = {
      ctor.paramss.flatten.collect {
        case param if param.mods.exists(mod => mod.syntax.startsWith("@GeneratedValue")) =>
          ("$" + param.name.syntax).parse[Term].get
      }.headOption.getOrElse(q"null")
    }

    protected def makeVersionPropertyType(clsName: Type.Name, ctor: Ctor.Primary): Term = {
      ctor.paramss.flatten.collect {
        case param if param.mods.exists(mod => mod.syntax.startsWith("@Version")) =>
          ("$" + param.name.syntax).parse[Term].get
      }.headOption.getOrElse(q"null")
    }

    protected def makeMethods(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting): Seq[Stat] = {
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
          classOf[${entitySetting.listener}]
        val __listener =
          context.getConfig.getEntityListenerProvider
            .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
        __listener.preInsert(entity, context)
      }

      override def preUpdate(
          entity: $clsName,
          context: org.seasar.doma.jdbc.entity.PreUpdateContext[$clsName]) = {
        val __listenerClass =
          classOf[${entitySetting.listener}]
        val __listener =
          context.getConfig.getEntityListenerProvider
            .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
        __listener.preUpdate(entity, context)
      }

      override def preDelete(
          entity: $clsName,
          context: org.seasar.doma.jdbc.entity.PreDeleteContext[$clsName]) = {
        val __listenerClass =
          classOf[${entitySetting.listener}]
        val __listener =
          context.getConfig.getEntityListenerProvider
            .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
        __listener.preDelete(entity, context)
      }

      override def postInsert(
          entity: $clsName,
          context: org.seasar.doma.jdbc.entity.PostInsertContext[$clsName]) = {
        val __listenerClass =
          classOf[${entitySetting.listener}]
        val __listener =
          context.getConfig.getEntityListenerProvider
            .get[$clsName,${entitySetting.listener}](__listenerClass, __listenerSupplier)
        __listener.postInsert(entity, context)
      }

      override def postUpdate(
          entity: $clsName,
          context: org.seasar.doma.jdbc.entity.PostUpdateContext[$clsName]) = {
        val __listenerClass =
          classOf[${entitySetting.listener}]
        val __listener =
          context.getConfig.getEntityListenerProvider
            .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
        __listener.postUpdate(entity, context)
      }

      override def postDelete(
          entity: $clsName,
          context: org.seasar.doma.jdbc.entity.PostDeleteContext[$clsName]) = {
        val __listenerClass =
          classOf[${entitySetting.listener}]
        val __listener =
          context.getConfig.getEntityListenerProvider
            .get[$clsName, ${entitySetting.listener}](__listenerClass, __listenerSupplier)
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
            if (p contains mod"@Embedded") {
              val tpe = Term.Name(decltpe.toString)
              q"""$tpe.newEmbeddable[$clsName](${name.syntax}, __args)"""
            } else {
              val tpe = Type.Name(decltpe.toString)

              q"""(if (__args.get(${name.syntax}) != null) __args.get(${name.syntax}).get else null).asInstanceOf[$tpe]"""
            }
          }
          Seq(
            q"""
            override def newEntity(
                __args: java.util.Map[
                  String,
                  org.seasar.doma.jdbc.entity.Property[$clsName, _]]) =
              new ${clsName.toString.parse[Ctor.Call].get}(
                ..$params
              )
            """)
      } ++
      q"""
      override def getEntityClass() = classOf[$clsName]

      override def getOriginalStates(__entity: $clsName): $clsName = null

      override def saveCurrentStates(__entity: $clsName): Unit = {}

      """.stats
    }
  }
}}
