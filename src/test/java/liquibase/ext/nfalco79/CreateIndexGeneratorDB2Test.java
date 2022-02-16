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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import liquibase.change.AddColumnConfig;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DB2Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.core.Column;

public class CreateIndexGeneratorDB2Test {

    @Test
    public void test_priority() throws Exception {
        CreateIndexGeneratorDB2 generator = new CreateIndexGeneratorDB2();
        Assertions.assertThat(generator.getPriority()).isGreaterThan(SqlGenerator.PRIORITY_DEFAULT);
    }

    @Test
    public void test_supports() throws Exception {
        CreateIndexStatement statement = mock(CreateIndexStatement.class);

        CreateIndexGeneratorDB2 generator = new CreateIndexGeneratorDB2();

        DB2Database database = mock(DB2Database.class);
        when(database.getDatabaseMajorVersion()).thenReturn(10);
        when(database.getDatabaseMinorVersion()).thenReturn(1);
        when(database.getConnection()).thenReturn(mock(DatabaseConnection.class));
        Assertions.assertThat(generator.supports(statement, database)).isFalse();

        when(database.getDatabaseMinorVersion()).thenReturn(5);
        Assertions.assertThat(generator.supports(statement, database)).isTrue();

        // return true for offline database
        database = new DB2Database();
        Assertions.assertThat(generator.supports(statement, database)).isTrue();
    }

    @Test
    public void test_generated_sql() throws Exception {
        CreateIndexStatement statement = new CreateIndexStatement("indexName", null, null, "tableName", true, null, //
                new AddColumnConfig(new Column("col1")), new AddColumnConfig(new Column("col2")));

        CreateIndexGeneratorDB2 generator = new CreateIndexGeneratorDB2();
        Sql[] sql = generator.generateSql(statement, new DB2Database(), mock(SqlGeneratorChain.class));
        Assertions.assertThat(sql).isNotEmpty();
        Assertions.assertThat(sql[0].toSql()).endsWith(" EXCLUDE NULL KEYS");
    }

}