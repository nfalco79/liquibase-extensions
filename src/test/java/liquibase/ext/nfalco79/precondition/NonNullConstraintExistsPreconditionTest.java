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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class NonNullConstraintExistsPreconditionTest {
    private static class MockDatabaseSnapshot extends EmptyDatabaseSnapshot {

        public MockDatabaseSnapshot(Database database) throws DatabaseException, InvalidExampleException {
            super(database);
        }

        @Override
        public <T extends DatabaseObject> T include(T example) throws DatabaseException, InvalidExampleException {
            return super.include(example);
        }
    }

    private Database database;
    private MockDatabaseSnapshot mockDatabaseSnapshot;

    @Before
    public void setup() throws Exception {
        OfflineConnection connection = mock(OfflineConnection.class);

        database = new H2Database();
        database.setConnection(connection);
        mockDatabaseSnapshot = new MockDatabaseSnapshot(database);

        when(connection.getSnapshot(any())).then(new Answer<DatabaseSnapshot>() {
            @Override
            public DatabaseSnapshot answer(InvocationOnMock invocation) throws Throwable {
                return mockDatabaseSnapshot;
            }
        });
    }

    private void populateDBCatalog(DatabaseObject object) throws Exception {
        mockDatabaseSnapshot.include(object);
    }

    private Column createColumn(String tableName, String columnName) {
        Column realColumn = new Column("column1");
        realColumn.setRelation(new Table().setName("table1"));
        realColumn.setAttribute("liquibase-complete", true);
        return realColumn;
    }

    @Test
    public void test_check_column_is_nullable() throws Exception {
        String tableName = "table1";
        String columnName = "column1";

        populateDBCatalog(createColumn(tableName, columnName).setNullable(false));

        NonNullConstraintExistsPrecondition precondition = new NonNullConstraintExistsPrecondition();
        precondition.setTableName(tableName);
        precondition.setColumnName(columnName);
        precondition.check(database, mock(DatabaseChangeLog.class), mock(ChangeSet.class));
    }

    @Test(expected = PreconditionFailedException.class)
    public void test_fails_when_column_not_nullable() throws Exception {
        String tableName = "table1";
        String columnName = "column1";

        populateDBCatalog(createColumn(tableName, columnName).setNullable(true));

        NonNullConstraintExistsPrecondition precondition = new NonNullConstraintExistsPrecondition();
        precondition.setTableName(tableName);
        precondition.setColumnName(columnName);
        precondition.check(database, mock(DatabaseChangeLog.class), mock(ChangeSet.class));
    }

    @Test(expected = PreconditionFailedException.class)
    public void test_fails_if_column_not_exists() throws Exception {
        String tableName = "table1";
        String columnName = "column1";

        NonNullConstraintExistsPrecondition precondition = new NonNullConstraintExistsPrecondition();
        precondition.setTableName(tableName);
        precondition.setColumnName(columnName);
        precondition.check(database, mock(DatabaseChangeLog.class), mock(ChangeSet.class));
    }

    @Test
    public void test_validation() throws Exception {
        NonNullConstraintExistsPrecondition precondition = new NonNullConstraintExistsPrecondition();
        ValidationErrors errors = precondition.validate(mock(Database.class));
        assertThat(errors).isNotNull();
        assertThat(errors.hasErrors()).isTrue();
    }
}
