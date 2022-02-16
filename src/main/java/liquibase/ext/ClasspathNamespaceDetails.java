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
package liquibase.ext;

import java.net.MalformedURLException;
import java.net.URL;

import liquibase.parser.core.xml.StandardNamespaceDetails;

/**
 * Extends {@link StandardNamespaceDetails} class.
 */
public class ClasspathNamespaceDetails extends StandardNamespaceDetails {

    @Override
    public int getPriority() {
        return super.getPriority() + 1;
    }

    @Override
    public String getLocalPath(final String url) {
        String localPath = super.getLocalPath(url);

        if (localPath == null) {
            try {
                localPath = new URL(url).toString();
            } catch (MalformedURLException e) {
                // is not an URL
                localPath = url;
            }
        }

        return localPath;
    }

}
