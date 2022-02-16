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
package liquibase.ext.nfalco79;

import liquibase.statement.AbstractSqlStatement;

public class CopyColumnStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String fromName;
    private String fromType;
    private String toName;
    private String toType;


    public CopyColumnStatement(String catalogName, String schemaName, String tableName, String fromName, String fromType, String toName, String toType) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.fromName = fromName;
        this.fromType = fromType;
        this.toName = toName;
        this.toType = toType;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFromType() {
        return fromType;
    }

    public String getToName() {
        return toName;
    }

    public String getToType() {
        return toType;
    }
}