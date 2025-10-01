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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class VelocityEngineUtils {
    final private static Logger logger = LogManager.getLogger();

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
