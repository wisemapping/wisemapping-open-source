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
package com.wisemapping.listener;


import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.LockException;
import com.wisemapping.model.Account;
import com.wisemapping.security.Utils;
import com.wisemapping.service.LockManager;
import com.wisemapping.service.MindmapService;
import org.jetbrains.annotations.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

public class UnlockOnExpireListener implements HttpSessionListener {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void sessionCreated(@NotNull HttpSessionEvent event) {

    }

    @Override
    public void sessionDestroyed(@NotNull HttpSessionEvent event) {

        final ServletContext servletContext = event.getSession().getServletContext();
        final WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        final MindmapService mindmapService = (MindmapService) wc.getBean("mindmapService");

        final LockManager lockManager = mindmapService.getLockManager();
        final Account user = Utils.getUser(false);
        if (user != null) {
            synchronized (mindmapService.getLockManager()) {
                try {
                    lockManager.unlockAll(user);
                } catch (LockException | AccessDeniedSecurityException e) {
                    logger.error(e);
                }
            }
        }
    }
}