package com.tle.core.db.types

import io.doolse.simpledba.jdbc.SizedIso

case class String255(value: String) extends AnyVal
case class String20(value: String) extends AnyVal
case class String40(value: String) extends AnyVal

object String255 {
  implicit def stringIso: SizedIso[String255, String] = SizedIso(255, _.value, String255.apply)
  implicit def fromString(s: String): String255 = String255(s)
  implicit def fromStringOpt(s: String): Option[String255] = Option(s).map(String255.apply)
}
object String20 {
  implicit def string20Iso: SizedIso[String20, String] = SizedIso(20, _.value, String20.apply)
  implicit def fromString(s: String): String20 = String20(s)
}
object String40 {
  implicit def string20Iso: SizedIso[String40, String] = SizedIso(40, _.value, String40.apply)
  implicit def fromString(s: String): String40 = String40(s)
}

