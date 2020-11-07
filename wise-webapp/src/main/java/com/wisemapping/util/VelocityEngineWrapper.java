package com.wisemapping.util;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.NotNull;

public class VelocityEngineWrapper {
    private VelocityEngine velocityEngine;

    public VelocityEngineWrapper() {
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.setProperty("resource.loader", "class");
        extendedProperties.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        this.velocityEngine = new VelocityEngine();
        velocityEngine.setExtendedProperties(extendedProperties);
    }

    @NotNull
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }
}
