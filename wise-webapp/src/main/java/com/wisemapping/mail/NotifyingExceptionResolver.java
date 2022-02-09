package com.wisemapping.mail;

import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

public class NotifyingExceptionResolver extends SimpleMappingExceptionResolver {

    final private Logger logger = Logger.getLogger(NotifyingExceptionResolver.class);
    private Set<String> exclude = new HashSet<String>();
    private NotificationService notificationService;

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!exclude.contains(ex.getClass().getName())) {
            logger.error("An Exception has occurred in the application", ex);
            sendNotification(ex, request);
        }

        return super.doResolveException(request, response, handler, ex);
    }

    private void sendNotification(@NotNull Exception ex, @NotNull HttpServletRequest request) {
        final User user = Utils.getUser(false);
        notificationService.reportJavaException(ex, user, request);
    }

    public void setExclude(final Set<String> exclude) {
        this.exclude = exclude;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
