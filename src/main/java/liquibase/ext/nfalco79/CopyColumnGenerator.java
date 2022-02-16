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

import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.ext.nfalco79.util.StringUtils;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;

public class CopyColumnGenerator extends AbstractSqlGenerator<CopyColumnStatement> {

    private static final String CLOB_TYPE = "CLOB";
    private static final String BLOB_TYPE = "BLOB";
    private static final List<String> CHAR_TYPES =  Arrays.asList("VARCHAR", "NVARCHAR", "VARCHAR2", "NVARCHAR2", "CHAR");
    private static final List<String> NUMERIC_TYPES =  Arrays.asList("BIGINT", "NUMERIC", "INTEGER", "NVARCHAR2", "DECIMAL");

    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(CopyColumnStatement statement, Database database) {
        return true;
    }

    @Override
    public ValidationErrors validate(CopyColumnStatement statement, Database database, @SuppressWarnings("rawtypes") SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("fromName", statement.getFromName());
        validationErrors.checkRequiredField("fromType", statement.getFromType());
        validationErrors.checkRequiredField("toName", statement.getToName());
        validationErrors.checkRequiredField("toType", statement.getToType());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CopyColumnStatement statement, Database database, @SuppressWarnings("rawtypes") SqlGeneratorChain sqlGeneratorChain) { // NOSONAR
        String tableNameEscaped = database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
        String fromNameEscaped = database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getFromName());
        String toNameEscaped = database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getToName());

        String sql = "UPDATE " + tableNameEscaped + " SET " + toNameEscaped + " = ";

        String srcField = fromNameEscaped;

        String fromType = statement.getFromType().toUpperCase();
        String toType = statement.getToType().toUpperCase();

        if (!fromType.equals(toType)) {
            if (CLOB_TYPE.equalsIgnoreCase(toType)) {
                if (database instanceof OracleDatabase) {
                    srcField = "TO_CLOB(" + fromNameEscaped + ")";
                }
            } else if (BLOB_TYPE.equalsIgnoreCase(toType)) {
                if (database instanceof OracleDatabase) {
                    String fun = OracleRawUtil.getToBlobFunction(fromType);
                    if (fun != null) {
                        srcField = "UTL_RAW." + fun + "(" + fromNameEscaped + ")";
                    }
                } else if (database instanceof PostgresDatabase) {
                    srcField = "CAST(" + fromNameEscaped + " AS BYTEA)";
                }
            } else if (BLOB_TYPE.equalsIgnoreCase(fromType)) {
                if (database instanceof OracleDatabase) {
                    String fun = OracleRawUtil.getBlobToFunction(toType);
                    if (fun != null) {
                        srcField = "UTL_RAW." + fun + "(" + fromNameEscaped + ")";
                    }
                }
            } else if (CHAR_TYPES.contains(StringUtils.removeParam(fromType))
                    && NUMERIC_TYPES.contains(StringUtils.removeParam(toType))) {
                if (database instanceof PostgresDatabase) {
                    toType = StringUtils.removeParam(toType);
                    if ("NUMERIC".equals(toType)) {
                        toType = "DOUBLE PRECISION";
                    }
                }
                srcField = "CAST(" + fromNameEscaped + " AS " + toType + ")";
            }
        }

        sql += srcField;

        return new Sql[] { new UnparsedSql(sql) };
    }

    private static class OracleRawUtil {
        public static String getToBlobFunction(String type) {
            if ("BINARY_DOUBLE".equals(type)) {
                return "CAST_FROM_BINARY_DOUBLE";
            } else if ("BINARY_FLOAT".equals(type)) {
                return "CAST_FROM_BINARY_FLOAT";
            } else if ("BINARY_INTEGER".equals(type)) {
                return "CAST_FROM_BINARY_INTEGER";
            } else if ("NUMBER".equals(type)) {
                return "CAST_FROM_NUMBER";
            } else if ("VARCHAR2".equals(type)) {
                return "CAST_TO_RAW";
            }
            return null;
        }
        public static String getBlobToFunction(String type) {
            if ("BINARY_DOUBLE".equals(type)) {
                return "CAST_TO_BINARY_DOUBLE";
            } else if ("BINARY_FLOAT".equals(type)) {
                return "CAST_TO_BINARY_FLOAT";
            } else if ("BINARY_INTEGER".equals(type)) {
                return "CAST_TO_BINARY_INTEGER";
            } else if ("NUMBER".equals(type)) {
                return "CAST_TO_NUMBER";
            } else if ("NVARCHAR2".equals(type)) {
                return "CAST_TO_NVARCHAR2";
            } else if ("VARCHAR2".equals(type)) {
                return "CAST_TO_VARCHAR2";
            }
            return null;
        }
    }
}