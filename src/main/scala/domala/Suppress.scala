package domala

import domala.message.Message

/** Used to suppress warning messages. */
class Suppress(messages: Seq[Message]) extends scala.annotation.StaticAnnotation
