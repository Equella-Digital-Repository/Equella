/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.hibernate.dialect;

import org.apache.log4j.Logger;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitAnyDiscriminatorColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitAnyKeyColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitCollectionTableNameSource;
import org.hibernate.boot.model.naming.ImplicitDiscriminatorColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitEntityNameSource;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitIdentifierColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitIndexColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitIndexNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinTableNameSource;
import org.hibernate.boot.model.naming.ImplicitMapKeyColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.ImplicitPrimaryKeyJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitTenantIdColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;

public class OeqImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {
  private static final Logger LOGGER = Logger.getLogger(OeqImplicitNamingStrategy.class);

  @Override
  public Identifier determinePrimaryTableName(ImplicitEntityNameSource source) {
    LOGGER.trace(
        "determinePrimaryTableName - "
            + source.getEntityNaming().getEntityName()
            + ", "
            + source.getEntityNaming().getClassName()
            + ", "
            + source.getEntityNaming().getJpaEntityName());
    return super.determinePrimaryTableName(source);
  }

  @Override
  public Identifier determineJoinTableName(ImplicitJoinTableNameSource source) {
    Identifier resp;
    // TODO [SpringHib5] Consider if this should be handled ... differently.
    if (source.getOwningPhysicalTableName().equals("Item")
        && source.getNonOwningPhysicalTableName().equals("HistoryEvent")) {
      resp = Identifier.toIdentifier("item_history");
    } else if (source.getOwningPhysicalTableName().equals("Item")
        && source.getNonOwningPhysicalTableName().equals("ReferencedURL")) {
      resp = Identifier.toIdentifier("item_referenced_urls");
    } else {
      resp = super.determineJoinTableName(source);
    }
    final String respText = (resp == null) ? "NULL" : resp.getText();
    LOGGER.trace(
        "determineJoinTableName - "
            + source.getOwningEntityNaming().getEntityName()
            + ", "
            + source.getNonOwningEntityNaming().getEntityName()
            + "Result="
            + respText);
    return resp;
  }

  @Override
  public Identifier determineCollectionTableName(ImplicitCollectionTableNameSource source) {
    LOGGER.trace(
        "determineCollectionTableName - "
            + source.getOwningPhysicalTableName().getText()
            + " - "
            + source.getOwningEntityNaming().getEntityName());
    return super.determineCollectionTableName(source);
  }

  @Override
  public Identifier determineDiscriminatorColumnName(ImplicitDiscriminatorColumnNameSource source) {
    LOGGER.trace("determineDiscriminatorColumnName - " + source.getEntityNaming().getEntityName());
    return super.determineDiscriminatorColumnName(source);
  }

  @Override
  public Identifier determineTenantIdColumnName(ImplicitTenantIdColumnNameSource source) {
    LOGGER.trace("determineTenantIdColumnName - " + source.getEntityNaming().getEntityName());
    return super.determineTenantIdColumnName(source);
  }

  @Override
  public Identifier determineIdentifierColumnName(ImplicitIdentifierColumnNameSource source) {
    LOGGER.trace(
        "determineIdentifierColumnName - "
            + source.getEntityNaming().getEntityName()
            + " - "
            + source.getIdentifierAttributePath().getFullPath());
    return super.determineIdentifierColumnName(source);
  }

  @Override
  public Identifier determineBasicColumnName(ImplicitBasicColumnNameSource source) {
    Identifier resp;
    // TODO [SpringHib5] Consider if this should be handled ... differently.
    if (source.getAttributePath().getFullPath().equals("impTransforms.collection&&element.filename")
        || source
            .getAttributePath()
            .getFullPath()
            .equals("expTransforms.collection&&element.filename")) {
      resp = Identifier.toIdentifier("fil");
    } else if (source
            .getAttributePath()
            .getFullPath()
            .equals("impTransforms.collection&&element.type")
        || source
            .getAttributePath()
            .getFullPath()
            .equals("expTransforms.collection&&element.type")) {
      resp = Identifier.toIdentifier("typ");
    } else if (source
        .getAttributePath()
        .getFullPath()
        .equals("citations.collection&&element.transformation")) {
      resp = Identifier.toIdentifier("transfo");
    } else {
      resp = super.determineBasicColumnName(source);
    }

    final String respText = (resp == null) ? "NULL" : resp.getText();
    LOGGER.trace(
        "determineBasicColumnName - "
            + source.getAttributePath().getFullPath()
            + " - result: ["
            + respText
            + "]");
    return resp;
  }

  @Override
  public Identifier determineJoinColumnName(ImplicitJoinColumnNameSource source) {
    LOGGER.trace(
        "determineJoinColumnName - "
            + source.getReferencedTableName().getText()
            + " - "
            + source.getReferencedColumnName().getText());
    return super.determineJoinColumnName(source);
  }

  @Override
  public Identifier determinePrimaryKeyJoinColumnName(
      ImplicitPrimaryKeyJoinColumnNameSource source) {
    LOGGER.trace(
        "determinePrimaryKeyJoinColumnName - "
            + source.getReferencedTableName().getText()
            + " - "
            + source.getReferencedPrimaryKeyColumnName().getText());
    return super.determinePrimaryKeyJoinColumnName(source);
  }

  @Override
  public Identifier determineAnyDiscriminatorColumnName(
      ImplicitAnyDiscriminatorColumnNameSource source) {
    LOGGER.trace(
        "determineAnyDiscriminatorColumnName - " + source.getAttributePath().getFullPath());
    return super.determineAnyDiscriminatorColumnName(source);
  }

  @Override
  public Identifier determineAnyKeyColumnName(ImplicitAnyKeyColumnNameSource source) {
    LOGGER.trace("determineAnyKeyColumnName - " + source.getAttributePath().getFullPath());
    return super.determineAnyKeyColumnName(source);
  }

  @Override
  public Identifier determineMapKeyColumnName(ImplicitMapKeyColumnNameSource source) {
    LOGGER.trace("determineMapKeyColumnName - " + source.getPluralAttributePath().getFullPath());
    return super.determineMapKeyColumnName(source);
  }

  @Override
  public Identifier determineListIndexColumnName(ImplicitIndexColumnNameSource source) {
    LOGGER.trace("determineListIndexColumnName - " + source.getPluralAttributePath().getFullPath());
    return super.determineListIndexColumnName(source);
  }

  @Override
  public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
    Identifier resp = super.determineForeignKeyName(source);
    final String respText = (resp == null) ? "NULL" : resp.getText();
    LOGGER.trace(
        "determineForeignKeyName - "
            + source.getReferencedTableName()
            + ", "
            + source.getReferencedTableName().getText()
            + ", Result="
            + respText);
    return resp;
  }

  @Override
  public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
    LOGGER.trace(
        "determineUniqueKeyName - " + source.getTableName() + " - " + source.getColumnNames());
    return super.determineUniqueKeyName(source);
  }

  @Override
  public Identifier determineIndexName(ImplicitIndexNameSource source) {
    LOGGER.trace("determineIndexName - " + source.getTableName() + " - " + source.getColumnNames());
    return super.determineIndexName(source);
  }
}
