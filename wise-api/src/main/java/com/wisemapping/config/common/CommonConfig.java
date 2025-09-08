package com.wisemapping.config.common;

import com.wisemapping.dao.LabelManagerImpl;
import com.wisemapping.model.Account;
import com.wisemapping.security.AuthenticationProvider;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapServiceImpl;
import com.wisemapping.util.VelocityEngineUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@ComponentScan(basePackageClasses = {AuthenticationProvider.class, MindmapServiceImpl.class, LabelManagerImpl.class, VelocityEngineUtils.class, com.wisemapping.service.SpamDetectionService.class, com.wisemapping.scheduler.SpamUserSuspensionScheduler.class, com.wisemapping.service.SpamDetectionBatchService.class, com.wisemapping.scheduler.SpamDetectionScheduler.class, com.wisemapping.service.spam.ContactInfoSpamStrategy.class})
@Import({JPAConfig.class, SecurityConfig.class})
@EnableAutoConfiguration
@EnableScheduling
@EnableAsync
public class CommonConfig {
    
    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver() {
            @Override
            public Locale resolveLocale(@NotNull HttpServletRequest request) {
                final Account user = Utils.getUser();
                Locale result;
                if (user != null && user.getLocale() != null) {
                    String locale = user.getLocale();
                    final String locales[] = locale.split("_");

                    Locale.Builder builder = new Locale.Builder().setLanguage(locales[0]);
                    if (locales.length > 1) {
                        builder.setVariant(locales[1]);
                    }
                    result = builder.build();
                } else {
                    result = super.resolveLocale(request);
                }
                return result;
            }
        };
    }
}

