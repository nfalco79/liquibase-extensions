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

import java.text.MessageFormat;

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
import liquibase.ext.nfalco79.util.Version;
import liquibase.ext.nfalco79.util.VersionRange;
import liquibase.precondition.AbstractPrecondition;

/**
 * This precondition is capable to filter database version to match the given a
 * range.
 *
 * @author Nikolas Falco
 */
public class DatabaseVersionPrecondition extends AbstractPrecondition {
    private boolean minIncluded;
    private boolean maxIncluded;
    private String minVersion;
    private String maxVersion;

    @Override
    public String getName() {
        return "dbVersion";
    }

    @Override
    public void check(Database database,
                      DatabaseChangeLog changeLog,
                      ChangeSet changeSet,
                      ChangeExecListener changeExecListener) throws PreconditionFailedException, PreconditionErrorException {
        check(database, changeLog, changeSet);
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        try {
            Version dbVersion = getDatabaseVersion(database);
            VersionRange expectedRange = new VersionRange(minIncluded, Version.parseVersion(minVersion), //
                maxVersion != null ? Version.parseVersion(maxVersion) : null, maxIncluded);

            if (!expectedRange.includes(dbVersion)) {
                String msg = "Expected DB version in {0} but found " + database.getDatabaseProductName() + " version ''{1}''";
                throw new PreconditionFailedException(MessageFormat.format(msg, expectedRange, dbVersion), changeLog, this);
            }
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }

    }

    protected Version getDatabaseVersion(Database database) throws DatabaseException {
        // in case of oracle get full version using SELECT value FROM v$parameter WHERE name = 'compatible';
        return new Version(database.getDatabaseMajorVersion(), database.getDatabaseMinorVersion(), 0);
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public boolean isMaxIncluded() {
        return maxIncluded;
    }

    public void setMaxIncluded(boolean maxIncluded) {
        this.maxIncluded = maxIncluded;
    }

    public boolean isMinIncluded() {
        return minIncluded;
    }

    public void setMinIncluded(boolean minIncluded) {
        this.minIncluded = minIncluded;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if ((getMinVersion() == null) && (getMaxVersion() == null)) {
            validationErrors.addError("Either minVersion or maxVersion must be set");
        }
        return validationErrors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return Namespaces.CHANGELOG_NAMESPACE;
    }

}
