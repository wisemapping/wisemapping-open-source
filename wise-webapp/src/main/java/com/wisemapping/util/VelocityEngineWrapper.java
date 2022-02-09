package com.wisemapping.util;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jetbrains.annotations.NotNull;

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
