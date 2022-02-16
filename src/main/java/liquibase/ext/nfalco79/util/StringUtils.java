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
package liquibase.ext.nfalco79.util;

/**
 * Utility class to check and manipulate String.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Remove params from type.
     *
     * @param type
     *            the type with params
     * @return type without params
     */
    public static String removeParam(final String type) {
        if (type != null) {
            int startIndex = type.indexOf('(');
            int endIndex = type.indexOf(')');

            String dt = type;
            if (startIndex != -1 && endIndex != -1) {
                dt = type.substring(0, startIndex) + type.substring(endIndex + 1);
            }
            return dt.trim();
        } else {
            return null;
        }
    }

}
