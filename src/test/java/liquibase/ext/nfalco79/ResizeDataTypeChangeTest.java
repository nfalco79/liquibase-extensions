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

import org.assertj.core.api.Assertions;
import org.junit.Test;

import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;

public class ResizeDataTypeChangeTest {

    @Test
    public void verify_that_olddatatype_and_newdatatype_must_be_the_same() {
        ResizeDataTypeChange change = new ResizeDataTypeChange();
        change.setOldDataType("boolean");
        change.setNewDataType("clob");
        ValidationErrors errors = change.validate(new OracleDatabase());
        Assertions.assertThat(errors.getErrorMessages()).contains("oldDataType must match newDataType");
    }

    @Test
    public void verify_that_resize_clob_on_oracle_is_not_performed() {
        ResizeDataTypeChange change = new ResizeDataTypeChange();
        change.setOldDataType("clob");
        change.setNewDataType("clob");
        Assertions.assertThat(change.generateStatements(new OracleDatabase())).isEmpty();
    }

    @Test
    public void verify_that_resize_blob_on_oracle_is_not_performed() {
        ResizeDataTypeChange change = new ResizeDataTypeChange();
        change.setOldDataType("BLOB");
        change.setNewDataType("BLOB(1048576)");
        Assertions.assertThat(change.generateStatements(new OracleDatabase())).isEmpty();
    }
}
