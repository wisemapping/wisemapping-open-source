package com.wisemapping.config;

import com.wisemapping.security.MapAccessPermissionEvaluation;
import com.wisemapping.security.ReadSecurityAdvise;
import com.wisemapping.security.UpdateSecurityAdvise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
@ImportResource(value = {"classpath:spring/wisemapping-service.xml"})
@ComponentScan({"com.wisemapping.security", "com.wisemapping.service", "com.wisemapping.dao", "com.wisemapping.util", "com.wisemapping.model"})
public class MethodSecurityConfig  {

    @Autowired
    private ReadSecurityAdvise readAdvice;

    @Autowired
    private UpdateSecurityAdvise updateAdvice;

    @Bean
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();

        final MapAccessPermissionEvaluation permissionEvaluator = new MapAccessPermissionEvaluation(readAdvice, updateAdvice);
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
}
