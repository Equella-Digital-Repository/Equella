/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.db

import java.util

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.tables.{AttachmentViewCount, AuditLogEntry, ItemViewCount, Setting}
import com.tle.core.db.types.{DbUUID, InstId, JsonColumn}
import com.tle.core.hibernate.factory.guice.HibernateFactoryModule
import fs2.Stream
import io.doolse.simpledba._
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._
import shapeless._

import scala.collection.JavaConverters._

trait DBSchema extends StdColumns {

  implicit def config: JDBCConfig.Aux[C]

  implicit def dbUuidCol: C[DbUUID]

  def schemaSQL : JDBCSchemaSQL = config.schemaSQL

  def indexEach(cols: TableColumns, name: NamedColumn => String): Seq[String] =
    cols.columns.map { cb =>
      schemaSQL.createIndex(TableColumns(cols.name, Seq(cb)), name(cb))
    }


  def jsonColumnMod(ct: ColumnType): ColumnType = ct

  implicit def jsonColumns[A <: JsonColumn](implicit c: Iso[A, Option[String]], col: C[Option[String]]): C[A] =
    wrap[Option[String], A](col, _.isoMap[A](c), jsonColumnMod)

  def autoIdCol: C[Long]

  val auditLog = TableMapper[AuditLogEntry].table("audit_log_entry").edit('id, autoIdCol).key('id)

  def insertAuditLog: (Long => AuditLogEntry) => Stream[JDBCIO, AuditLogEntry]

  val userAndInst = Cols('user_id, 'institution_id)

  val auditLogQueries = AuditLogQueries(insertAuditLog,
    auditLog.delete.where(userAndInst, BinOp.EQ).build,
    auditLog.query.where(userAndInst, BinOp.EQ).build,
    auditLog.delete.where('institution_id, BinOp.EQ).build,
    auditLog.delete.where('timestamp, BinOp.LT).build,
    auditLog.select.count.where('institution_id, BinOp.EQ).buildAs[InstId, Int],
    auditLog.query.where('institution_id, BinOp.EQ).build
  )

  val auditLogTable = auditLog.definition

  val auditLogIndexColumns : TableColumns = auditLog.subset(Cols('institution_id, 'timestamp, 'event_category, 'event_type,
    'user_id) ++ Cols('session_id, 'data1, 'data2, 'data3))

  val auditLogNewColumns = auditLog.subset(Cols('meta))

  val itemViewId = Cols('inst, 'item_uuid, 'item_version)
  val itemViewCount = TableMapper[ItemViewCount].table("viewcount_item").keys(itemViewId)
  val attachmentViewCount = TableMapper[AttachmentViewCount].table("viewcount_attachment")
    .keys(itemViewId ++ Cols('attachment))

  val viewCountTables = Seq(itemViewCount.definition, attachmentViewCount.definition)

  val countByCol = JDBCQueries.queryRawSQL("select sum(\"count\") from viewcount_item vci " +
    "inner join item i on vci.item_uuid = i.uuid and vci.item_version = i.version " +
    "inner join base_entity be on be.id = i.item_definition_id where be.id = ?",
    config.record[Long :: HNil], config.record[Option[Int] :: HNil])

  val attachmentViewCountByCol = JDBCQueries.queryRawSQL("select sum(\"count\") from viewcount_attachment vca " +
    "inner join attachment a on vca.attachment = a.uuid " +
    "inner join item i on a.item_id = i.id " +
    "inner join base_entity be on be.id = i.item_definition_id where be.id = ?",
    config.record[Long :: HNil], config.record[Option[Int] :: HNil])

  val viewCountQueries = {
    val del1 = itemViewCount.delete.where(itemViewId, BinOp.EQ).build[(InstId, DbUUID, Int)]
    val del2 = attachmentViewCount.delete.where(itemViewId, BinOp.EQ).build[(InstId, DbUUID, Int)]
    ViewCountQueries(itemViewCount.writes,
      attachmentViewCount.writes,
      itemViewCount.byPK,
      itemViewCount.query.where(Cols('inst), BinOp.EQ).build,
      attachmentViewCount.byPK,
      attachmentViewCount.query.where(Cols('inst, 'item_uuid, 'item_version), BinOp.EQ).build,
      countByCol.as[Long => Stream[JDBCIO, Option[Int]]].andThen(_.map(_.getOrElse(0))),
      attachmentViewCountByCol.as[Long => Stream[JDBCIO, Option[Int]]].andThen(_.map(_.getOrElse(0))),
      id => del1(id) ++ del2(id)
    )
  }

  def creationSQL: util.Collection[String] = {
    Seq(schemaSQL.createTable(auditLogTable)) ++
      indexEach(auditLogIndexColumns, "audit_" + _.name) ++
      viewCountTables.map(schemaSQL.createTable)
  }.asJava

  val settingsRel = TableMapper[Setting].table("configuration_property").keys(Cols('institution_id, 'property))

  val settingsQueries = SettingsQueries(settingsRel.writes, settingsRel.byPK,
    settingsRel.query.where(Cols('institution_id), BinOp.EQ).where(Cols('property), BinOp.LIKE).build)


}

object DBSchema
{
  lazy private val schemaForDBType: DBSchema with DBQueries with DBSchemaMigration = {
    val p = new HibernateFactoryModule
    p.getProperty("hibernate.connection.driver_class") match {
      case "org.postgresql.Driver" => PostgresSchema
      case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerSchema
      case "oracle.jdbc.driver.OracleDriver" => OracleSchema
    }
  }

  def schema : DBSchema = schemaForDBType

  def schemaMigration : DBSchemaMigration = schemaForDBType

  def queries : DBQueries = schemaForDBType
}
