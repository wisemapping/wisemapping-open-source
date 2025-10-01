/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.wisemapping.util;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class VelocityEngineWrapper {
    private final VelocityEngine velocityEngine;

    public VelocityEngineWrapper() {
        Properties properties = new Properties();
        properties.setProperty("resource.loaders", "class");
        properties.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        
        // Enable backward compatibility settings
        properties.setProperty("introspector.conversion_handler.class", "none");
        properties.setProperty("parser.space_gobbling", "bc");
        properties.setProperty("parser.allow_hyphen_in_identifiers", "true");
        properties.setProperty("velocimacro.enable_bc_mode", "true");

        this.velocityEngine = new VelocityEngine();
        velocityEngine.setProperties(properties);
        
        // Initialize the engine
        velocityEngine.init();
    }

    @NotNull
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }
}
