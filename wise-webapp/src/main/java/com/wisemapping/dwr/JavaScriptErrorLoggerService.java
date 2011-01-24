/*
*    Copyright [2011] [wisemapping]
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

// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2007-08-01 19:08:20 (-0300), by: imanzano. $Revision$
// ...........................................................................................................

package com.wisemapping.dwr;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import com.wisemapping.model.User;

import javax.servlet.http.HttpServletRequest;

public class JavaScriptErrorLoggerService
        extends BaseDwrService {

    final Log logger = LogFactory.getLog(JavaScriptErrorLoggerService.class);
    private static final int ERROR_MESSAGE = 3;
    private static final int FATAL_MESSAGE = 4;
    private static final String USER_AGENT = "User-Agent";

    JavaScriptErrorLoggerService() {
        LogFactory.getLog(JavaScriptErrorLoggerService.class);
    }
    //~ Methods ..............................................................................................

    public void logError(final int severity, final String logMsg)
            throws IOException {

        final User user = getUser();

        final WebContext ctx = WebContextFactory.get();
        final HttpServletRequest request = ctx.getHttpServletRequest();


        final String userAgent = request.getHeader(USER_AGENT);
        synchronized (logger) {
            // Log user info ...
            if (user != null) {
                log(severity, "UserId:" + user.getId() + ", UserEmail:" + user.getEmail());
            } else {
                log(severity, "Anonymous user");
            }

            // Log browser details ...
            log(severity, "Browser:" + userAgent);

            // Log error message ...
            log(severity, logMsg);
        }
    }

    void log(final int severity, final String msg) {
        // Log error message ...
        if (severity == ERROR_MESSAGE && logger.isErrorEnabled()) {
            logger.error(msg);
        } else if (severity == FATAL_MESSAGE && logger.isFatalEnabled()) {
            logger.fatal(msg);
        } else if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }

}
