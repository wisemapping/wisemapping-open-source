package com.wisemapping.listener;


import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.LockException;
import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import com.wisemapping.service.LockManager;
import com.wisemapping.service.MindmapService;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class UnlockOnExpireListener implements HttpSessionListener {
    private static final Logger logger = Logger.getLogger(UnlockOnExpireListener.class);

    @Override
    public void sessionCreated(@NotNull HttpSessionEvent event) {

    }

    @Override
    public void sessionDestroyed(@NotNull HttpSessionEvent event) {

        final ServletContext servletContext = event.getSession().getServletContext();
        final WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        final MindmapService mindmapService = (MindmapService) wc.getBean("mindmapService");
        final LockManager lockManager = mindmapService.getLockManager();

        final User user = Utils.getUser(false);
        if (user != null) {
            try {
                lockManager.unlockAll(user);
            } catch (LockException | AccessDeniedSecurityException e) {
                logger.error(e);
            }
        }
    }
}