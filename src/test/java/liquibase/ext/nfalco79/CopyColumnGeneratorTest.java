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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

public class CopyColumnGeneratorTest {

    private static String clobType = "CLOB";
    private static String blobType = "BLOB";
    private static String varchar2Type = "VARCHAR2";
    public static String numberType = "NUMBER";
    public static String numericType = "NUMERIC(22,8)";
    public static String clobFunction = "TO_CLOB";
    public static String bigintType = "BIGINT";

    @Test
    public void test_varchar2_to_clob_on_oracle() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", varchar2Type, "mytoname", clobType);
        Database database = new OracleDatabase();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        assertTrue(sql[0].toSql().contains(clobFunction));
    }

    @Test
    public void test_copy_column_to_clob_on_postgres() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", "myfromtype", "mytoname", clobType);
        Database database = new PostgresDatabase();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        assertFalse(sql[0].toSql().contains(clobFunction));
    }

    @Test
    public void test_varchar2_to_blob_on_oracle() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", varchar2Type, "mytoname", blobType);
        Database database = new OracleDatabase();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        assertTrue(sql[0].toSql().contains("UTL_RAW.CAST_TO_RAW(myfromname)"));
    }

    @Test
    public void test_varchar2_to_blob_on_postgres() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", varchar2Type, "mytoname", blobType);
        Database database = new PostgresDatabase();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        assertTrue(sql[0].toSql().contains("CAST(myfromname AS BYTEA)"));
    }

    @Test
    public void test_varchar2_to_blob_on_h2() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", varchar2Type, "mytoname", blobType);
        Database database = new H2Database();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        assertFalse(sql[0].toSql().contains("UTL_RAW.CAST_TO_RAW(myfromname)"));
        assertFalse(sql[0].toSql().contains("CAST(myfromname AS BYTEA)"));
    }

    @Test
    public void test_blob_to_number_on_oracle() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", blobType, "mytoname", numberType);
        Database database = new OracleDatabase();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        assertTrue(sql[0].toSql().contains("UTL_RAW.CAST_TO_NUMBER(myfromname)"));
    }

    @Test
    public void test_varchar2_to_number_on_postgres() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", varchar2Type, "mytoname", bigintType);
        Database database = new PostgresDatabase();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        assertTrue(sql[0].toSql().contains("CAST(myfromname AS BIGINT)"));
    }

    @Test
    public void test_copy_column_string_to_number_on_postgres() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", varchar2Type, "mytoname", numericType);
        Database database = new PostgresDatabase();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        Assertions.assertThat(sql[0].toSql()).contains("SET mytoname = CAST(myfromname AS DOUBLE PRECISION");
    }

    @Test
    public void test_copy_column_string_to_number() {
        CopyColumnChange change = newCopyColumnChange("mytable", "myfromname", varchar2Type, "mytoname", numericType);
        Database database = new OracleDatabase();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        Sql[] sql = getSql(sqlStatements, database);
        Assertions.assertThat(sql[0].toSql()).contains("SET mytoname = CAST(myfromname AS " + numericType.toUpperCase() + ")");
    }

    private Sql[] getSql(SqlStatement[] statements, Database database) {
        CopyColumnGenerator generator = new CopyColumnGenerator();
        return generator.generateSql((CopyColumnStatement)statements[0], database, null);
    }

    private CopyColumnChange newCopyColumnChange(String tableName, String fromName, String fromType, String toName, String toType) {
        CopyColumnChange change = new CopyColumnChange();
        change.setTableName(tableName);
        change.setFromName(fromName);
        change.setFromType(fromType);
        change.setToName(toName);
        change.setToType(toType);
        return change;
    }

}
