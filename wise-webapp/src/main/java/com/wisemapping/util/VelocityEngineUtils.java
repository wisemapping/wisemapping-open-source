package com.wisemapping.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class VelocityEngineUtils {
    private static final Log logger = LogFactory.getLog(VelocityEngineUtils.class);

    public static void mergeTemplate(
            VelocityEngine velocityEngine, String templateLocation,
            Map<String, Object> model, Writer writer
    ) throws VelocityException {
        try {
            VelocityContext velocityContext = new VelocityContext(model);
            velocityEngine.mergeTemplate(templateLocation, "UTF-8", velocityContext, writer);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Why does VelocityEngine throw a generic checked exception, after all?", ex);
            throw new VelocityException(ex.toString());
        }
    }

    public static String mergeTemplateIntoString(
            VelocityEngine velocityEngine, String templateLocation, Map<String, Object> model
    ) throws VelocityException {
        StringWriter result = new StringWriter();
        mergeTemplate(velocityEngine, templateLocation, model, result);
        return result.toString();
    }
}
