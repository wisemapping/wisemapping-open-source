package com.wisemapping.config;

import com.wisemapping.config.mvc.MvcAppConfig;
import com.wisemapping.config.mvc.MvcSecurityConfig;
import com.wisemapping.config.mvc.ServletConfig;
import com.wisemapping.config.rest.RestAppConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@SpringBootApplication
@ImportResource(value = {"classpath:spring/wisemapping-service.xml"})
@ComponentScan({"com.wisemapping.security", "com.wisemapping.service", "com.wisemapping.dao", "com.wisemapping.util", "com.wisemapping.model"})
public class Application {

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .parent(Application.class, MethodSecurityConfig.class, HibernateConfig.class).web(WebApplicationType.NONE)
                .child(MvcAppConfig.class, MvcSecurityConfig.class, ServletConfig.class).web(WebApplicationType.SERVLET)
                .sibling(RestAppConfig.class).web(WebApplicationType.SERVLET)
                .run(args);

//        new SpringApplicationBuilder(Application.class, MethodSecurityConfig.class,MvcAppConfig.class, MvcSecurityConfig.class, HibernateConfig.class, ServletConfig.class).web(WebApplicationType.SERVLET).run(args);

    }

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}
