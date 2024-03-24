/*
 *    Copyright [2022] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.wisemapping.util;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class VelocityEngineWrapper {
    private final VelocityEngine velocityEngine;

    public VelocityEngineWrapper() {
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.setProperty("resource.loader", "class");
        extendedProperties.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        this.velocityEngine = new VelocityEngine();
        velocityEngine.setExtendedProperties(extendedProperties);

        // Configure velocity to use log4j.
        velocityEngine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                "org.apache.velocity.runtime.log.SimpleLog4JLogSystem" );
        velocityEngine.setProperty("runtime.log.logsystem.log4j.category", "org.apache.velocity");

    }

    @NotNull
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }
}
