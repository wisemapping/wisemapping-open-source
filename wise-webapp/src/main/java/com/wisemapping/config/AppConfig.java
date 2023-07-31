package com.wisemapping.config;

import com.wisemapping.exceptions.AccessDeniedSecurityException;
import com.wisemapping.exceptions.MapNotPublicSecurityException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.Properties;

@EnableWebMvc
@Configuration
public class AppConfig {

    @Bean
    HandlerExceptionResolver errorHandler() {
        final SimpleMappingExceptionResolver result =
                new SimpleMappingExceptionResolver();

        //exception to view name mapping
        final Properties p = new Properties();
        p.setProperty(MapNotPublicSecurityException.class.getName(), "reactInclude");
        p.setProperty(AccessDeniedSecurityException.class.getName(), "reactInclude");
        result.setExceptionMappings(p);

        //mapping status code with view response.
        result.addStatusCode("reactInclude", 403);

        //setting default error view
        result.setDefaultErrorView("errorTemplate");
        result.setDefaultStatusCode(500);

        return result;
    }

    @Bean
    public ViewResolver viewResolver(){
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        return resolver;
    }
}
