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

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.ext.nfalco79.util.StringUtils;
import liquibase.statement.SqlStatement;

@DatabaseChange(name = "resizeDataType", description = "Resize data type", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class ResizeDataTypeChange extends ModifyDataTypeChange {

    private String oldDataType;

    @Override
    public String getConfirmationMessage() {
        return getTableName() + "." + getColumnName() + " datatype was resized";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (database instanceof OracleDatabase) {
            String type = StringUtils.removeParam(getNewDataType());
            if ("BLOB".equalsIgnoreCase(type) || "CLOB".equalsIgnoreCase(type) || "NCLOB".equalsIgnoreCase(type)) {
                return new SqlStatement[0];
            }
        }
        return super.generateStatements(database);
    }

    @DatabaseChangeProperty()
    public String getOldDataType() {
        return oldDataType;
    }

    public void setOldDataType(String oldDataType) {
        this.oldDataType = oldDataType;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);

        String newType = StringUtils.removeParam(getNewDataType().trim().toUpperCase());
        if (!oldDataType.trim().equalsIgnoreCase(newType)) {
            errors.addError("oldDataType must match newDataType");
        }

        return errors;
    }
}
