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
      val fields = generateFields(cls.name, cls.ctor, entitySetting)
      val constructor = generateConstructor(cls.name, cls.ctor, entitySetting, tableSetting)
      val methods = generateMethods(cls.name, cls.ctor, entitySetting)

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

    protected def generateFields(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting): Seq[Stat] = {
      val propertySize = ctor.paramss.flatten.size
      val fields1 =
        q"""
        private val __namingType: org.seasar.doma.jdbc.entity.NamingType = ${entitySetting.naming}
        private val __idGenerator =
          new org.seasar.doma.jdbc.id.BuiltinIdentityIdGenerator()
        val __idList = new java.util.ArrayList[org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]()
        val __list = new java.util.ArrayList[org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]($propertySize)
        val __map = new java.util.HashMap[String, org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]]($propertySize)
        """
      val propertyTypeFields = generatePropertyTypeFields(clsName, ctor)

      fields1.stats ++ propertyTypeFields
    }

    protected def generatePropertyTypeFields(clsName: Type.Name, ctor: Ctor.Primary): Seq[Defn.Val] = {
      ctor.paramss.flatten.map { p =>
        val columnSetting = ColumnSetting.read(p.mods)
        val Term.Param(mods, name, Some(decltpe), default) = p
        val tpe = Type.Name(decltpe.toString)
        val tpeTerm = Term.Name(decltpe.toString)
        val propertyName = Pat.Var.Term(Term.Name("$" + name.syntax))

        val (basicTpe, newWrapperExpr) = TypeHelper.convertToEntityDomaType(decltpe) match {
          case DomaType.Basic(_, convertedType, wrapperSupplier) => (convertedType, wrapperSupplier)
          case DomaType.Option(DomaType.Basic(_, convertedType, wrapperSupplier), _) => (convertedType, wrapperSupplier)
          case DomaType.EntityOrHolderOrEmbeddable(unknownTpe) => (unknownTpe, q"null")
          case DomaType.Option(DomaType.EntityOrHolderOrEmbeddable(unknownTpe), _) => (unknownTpe,  q"null")
          case _ => abort(domala.message.Message.DOMALA4096.getMessage(decltpe.syntax, clsName.syntax, name.syntax))
        }

        val isId = mods.exists {
          case mod"@Id" => true
          case mod"@domala.Id" => true
          case _ => false
        }
//        if(isId) {
//          mods.exists {
//            case mod"@GeneratedValue(strategy = GenerationType.IDENTITY)" => true
//            case mod"@GeneratedValue(strategy = GenerationType.SEQUENCE)" => abort("not implementation now") // TODO:
//            case _ => abort("not implementation now") // TODO:
//          }
//        }

        val isVersion = mods.exists {
          case mod"@Version" => true
          case mod"@domala.Version" => true
          case _ => false
        }

        q"""
        val $propertyName = domala.internal.macros.EntityReflectionMacros.generatePropertyType(
          classOf[$tpe],
          classOf[$clsName],
          ${name.syntax},
          __namingType,
          ${if(isId) q"true" else q"false"},
          __idGenerator,
          ${if(isVersion) q"true" else q"false"},
          classOf[$basicTpe],
          $newWrapperExpr,
          ${columnSetting.name},
          ${columnSetting.insertable},
          ${columnSetting.updatable},
          ${columnSetting.quote},
          __list,
          __map,
          __idList
        )
        """
      }
    }

    protected def generateConstructor(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting, tableSetting: TableSetting): Seq[Stat] = {
      q"""
      val __listenerSupplier: java.util.function.Supplier[${entitySetting.listener}] =
        () => ListenerHolder.listener
      val __immutable = true
      val __name = ${Term.Name("\"" + clsName.syntax + "\"")}
      val __catalogName = ${Term.Name(tableSetting.catalog.syntax)}
      val __schemaName = ${Term.Name(tableSetting.schema.syntax)}
      val __tableName = ${Term.Name(tableSetting.name.syntax)}
      val __isQuoteRequired = ${tableSetting.quote}
      val __idPropertyTypes = java.util.Collections.unmodifiableList(__idList)
      val __entityPropertyTypes = java.util.Collections.unmodifiableList(__list)
      val __entityPropertyTypeMap: java.util.Map[
        String,
        org.seasar.doma.jdbc.entity.EntityPropertyType[$clsName, _]] =
        java.util.Collections.unmodifiableMap(__map)
      """.stats
    }

    protected def generateGeneratedIdPropertyType(clsName: Type.Name, ctor: Ctor.Primary): Term = {
      ctor.paramss.flatten.collect {
        case param if param.mods.exists(mod => mod.syntax.startsWith("@GeneratedValue")) =>
          ("$" + param.name.syntax + s".asInstanceOf[org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[$clsName, $clsName, _ <: Number, _]]").parse[Term].get
      }.headOption.getOrElse(q"null")
    }

    protected def generateVersionPropertyType(clsName: Type.Name, ctor: Ctor.Primary): Term = {
      ctor.paramss.flatten.collect {
        case param if param.mods.exists(mod => mod.syntax.startsWith("@Version") || mod.syntax.startsWith("@domala.Version")) =>
          ("$" + param.name.syntax  + s".asInstanceOf[org.seasar.doma.jdbc.entity.VersionPropertyType[$clsName, $clsName, _ <: Number, _]]").parse[Term].get
      }.headOption.getOrElse(q"null")
    }

    protected def generateMethods(clsName: Type.Name, ctor: Ctor.Primary, entitySetting: EntitySetting): Seq[Stat] = {
      q"""

      override def getNamingType() = __namingType

      override def isImmutable() = __immutable

      override def getName() = __name

      override def getCatalogName() = __catalogName

      override def getSchemaName() = __schemaName

      override def getTableName() =
        getTableName(org.seasar.doma.jdbc.Naming.DEFAULT.apply _)

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

      override def getGeneratedIdPropertyType(): org.seasar.doma.jdbc.entity.GeneratedIdPropertyType[_ >: $clsName, $clsName, _, _] = ${generateGeneratedIdPropertyType(clsName, ctor)}

      override def getVersionPropertyType(): org.seasar.doma.jdbc.entity.VersionPropertyType[_ >: $clsName, $clsName, _, _] = ${generateVersionPropertyType(clsName, ctor)}
      """.stats ++ {
          val params = ctor.paramss.flatten.map { p =>
            q"domala.internal.macros.EntityReflectionMacros.readProperty(classOf[$clsName], __args, ${p.name.syntax}, classOf[${Type.Name(p.decltpe.get.toString)}])"
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
