package com.wisemapping.config.common;

import com.wisemapping.filter.RequestPropertiesInterceptor;
import com.wisemapping.filter.UserLocaleInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class InterceptorsConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(@NotNull final InterceptorRegistry registry) {
        registry.addInterceptor(new UserLocaleInterceptor());
        registry.addInterceptor(new RequestPropertiesInterceptor());
    }
}