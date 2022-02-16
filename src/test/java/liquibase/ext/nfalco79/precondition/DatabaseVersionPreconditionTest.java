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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionFailedException;
import liquibase.ext.nfalco79.util.Version;

public class DatabaseVersionPreconditionTest {

    private Database getDatabase(String version) throws DatabaseException {
        Version v = Version.parseVersion(version);

        Database db = mock(Database.class);
        when(db.getDatabaseMajorVersion()).thenReturn(v.getMajor());
        when(db.getDatabaseMinorVersion()).thenReturn(v.getMinor());
        when(db.getDatabaseProductName()).thenReturn("Mock DB");
        when(db.getDatabaseProductVersion()).thenReturn(version);

        return db;
    }

    @Test
    public void test_maxVersion() throws Exception {
        DatabaseVersionPrecondition precondition = new DatabaseVersionPrecondition();
        precondition.setMaxVersion("12");
        precondition.setMaxIncluded(true);

        precondition.check(getDatabase("11.2.0.4.0"), mock(DatabaseChangeLog.class), mock(ChangeSet.class));
    }

    @Test
    public void test_greaterThan() throws Exception {
        DatabaseVersionPrecondition precondition = new DatabaseVersionPrecondition();
        precondition.setMinVersion("10.1");

        precondition.check(getDatabase("11.2.0.4.0"), mock(DatabaseChangeLog.class), mock(ChangeSet.class));
    }

    @Test(expected = PreconditionFailedException.class)
    public void test_lessThan() throws Exception {
        DatabaseVersionPrecondition precondition = new DatabaseVersionPrecondition();
        precondition.setMaxVersion("11.2");
        precondition.setMaxIncluded(true);

        precondition.check(getDatabase("12.1.0.1"), mock(DatabaseChangeLog.class), mock(ChangeSet.class));
    }

    @Test
    public void test_equalOrlessThan() throws Exception {
        DatabaseVersionPrecondition precondition = new DatabaseVersionPrecondition();
        precondition.setMinVersion("11");
        precondition.setMinIncluded(true);
        precondition.setMaxVersion("12");

        precondition.check(getDatabase("11.2.0.4.0"), mock(DatabaseChangeLog.class), mock(ChangeSet.class));
    }

}
