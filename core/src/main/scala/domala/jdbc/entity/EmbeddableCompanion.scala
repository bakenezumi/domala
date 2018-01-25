package domala.jdbc.entity

trait EmbeddableCompanion[EMBEDDABLE] {
  val embeddableDesc: EmbeddableDesc[EMBEDDABLE]
}
