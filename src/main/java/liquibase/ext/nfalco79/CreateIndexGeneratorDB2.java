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

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.DatabaseException;
import liquibase.ext.nfalco79.util.Version;
import liquibase.ext.nfalco79.util.VersionRange;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateIndexGenerator;
import liquibase.statement.core.CreateIndexStatement;

/**
 * Support index creation for DB2 version {@literal >=} 10.5 z/os excluded if column contains null values.
 *
 * @author Nikolas Falco
 */
public class CreateIndexGeneratorDB2 extends CreateIndexGenerator {

    private VersionRange supportedVersion;

    /**
     * Default constructor.
     */
    public CreateIndexGeneratorDB2() {
        supportedVersion = new VersionRange(true, Version.parseVersion("10.5"), null, false);
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateIndexStatement statement, Database database) {
        boolean supports = false;
        if (database instanceof DB2Database) {
            if (database.getConnection() == null) {
                supports = true;
            } else {
                try {
                    Version dbVersion = new Version(database.getDatabaseMajorVersion(), database.getDatabaseMinorVersion(), 0);
                    supports = supportedVersion.includes(dbVersion);
                } catch (DatabaseException e) { // NOSONAR
                    // let return false
                }
            }
        }
        return supports;
    }

    @Override
    public Sql[] generateSql(CreateIndexStatement statement, Database database, @SuppressWarnings("rawtypes") SqlGeneratorChain sqlGeneratorChain) {
        Sql[] generateSql = super.generateSql(statement, database, sqlGeneratorChain);
        if (generateSql.length == 0) {
            return generateSql;
        }
        String sql = generateSql[0].toSql() + " EXCLUDE NULL KEYS";
        return new Sql[] { new UnparsedSql(sql, getAffectedIndex(statement)) };
    }
}
