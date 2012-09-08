package com.wisemapping.rest;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@EnableWebMvc
@Configuration
public class RestMvcConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(@NotNull final List<HttpMessageConverter<?>> converters) {
        converters.add(converter());
        super.configureMessageConverters(converters);
    }

    @Bean
    MappingJacksonHttpMessageConverter converter() {
        return  new DebugMappingJacksonHttpMessageConverter();
    }
}