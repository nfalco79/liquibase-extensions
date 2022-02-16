/*
 * Copyright 2022 Falco Nikolas
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package liquibase.ext.nfalco79.precondition;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.ext.nfalco79.Namespaces;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

/**
 * This precondition verify if a given column is nullable or not.
 *
 * @author Nikolas Falco
 */
public class NonNullConstraintExistsPrecondition extends AbstractPrecondition {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;

    @Override
    public String getName() {
        return "nonNullConstraintExists";
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (getTableName() == null && getColumnName() == null) {
            validationErrors.addError("tableName and columnName are required");
        }
        return validationErrors;
    }

    @Override
    public void check(Database database,
                      DatabaseChangeLog changeLog,
                      ChangeSet changeSet,
                      ChangeExecListener changeExecListener) throws PreconditionFailedException, PreconditionErrorException {
        check(database, changeLog, changeSet);
    }

    /*
     * Method compatible with liquibase 3.5.5
     */
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        String dbCatalogName = database.correctObjectName(getCatalogName(), Catalog.class);
        String dbSchemaName = database.correctObjectName(getSchemaName(), Schema.class);
        String dbTableName = database.correctObjectName(getTableName(), Table.class);
        String dbColName = database.correctObjectName(getColumnName(), Column.class);

        Column example = new Column(dbColName);
        Schema schema = new Schema(dbCatalogName, dbSchemaName);
        example.setRelation(new Table().setName(dbTableName).setSchema(schema));

        try {
            Column snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            if (snapshot == null) {
                throw new PreconditionFailedException("Table or column " + example.toString() + " not found", changeLog, this);
            } else if (Boolean.TRUE.equals(snapshot.isNullable())) {
                throw new PreconditionFailedException("Column " + example.toString() + " is nullable", changeLog, this);
            }
        } catch (DatabaseException | InvalidExampleException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return Namespaces.CHANGELOG_NAMESPACE;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = StringUtil.trimToNull(tableName);
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = StringUtil.trimToNull(columnName);
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = StringUtil.trimToNull(catalogName);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtil.trimToNull(schemaName);
    }

}