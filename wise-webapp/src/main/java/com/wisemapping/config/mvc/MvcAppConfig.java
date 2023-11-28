package com.wisemapping.config.mvc;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;


@SpringBootApplication
@EnableWebMvc
@ImportResource(value = {"classpath:spring/wisemapping-servlet.xml"})
@ComponentScan("com.wisemapping.webmvc")
public class MvcAppConfig implements WebMvcConfigurer {
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry
//                .addResourceHandler("/**")
//                .addResourceLocations("classpath:/public/");
//    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        resolver.setViewClass(JstlView.class);
        return resolver;
    }

    @Bean
    HandlerExceptionResolver errorHandler() {
        final SimpleMappingExceptionResolver result = new SimpleMappingExceptionResolver();

        //mapping status code with view response.
        result.addStatusCode("reactInclude", 403);

        //setting default error view
        result.setDefaultErrorView("reactInclude");
        result.setDefaultStatusCode(500);
        return result;
    }
}