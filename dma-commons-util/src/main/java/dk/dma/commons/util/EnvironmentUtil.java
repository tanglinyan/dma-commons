/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.commons.util;

import org.slf4j.Logger;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A set of utility functions to access OS environment variables.
 *
 * @author Thomas Borg Salling
 */
public final class EnvironmentUtil {

    private static final Logger LOG = getLogger(EnvironmentUtil.class);

    /**
     * Get the value of an OS environment variable. Issue a LOG error if the variable is blank or does not exist.
     * @param envVarName The name of the environment variable to lookup.
     * @return the value of the environment variable; or null if value not set.
     */
    public static String env(String envVarName) {
        String envVarValue = System.getenv(envVarName);
        if (isBlank(envVarValue)) {
            LOG.error("Environment variable \"" + envVarName + "\" not set.");
            envVarValue = null;
        }
        return envVarValue;
    }

}
