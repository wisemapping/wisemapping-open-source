package com.wisemapping.config;

import com.wisemapping.config.mvc.MvcAppConfig;
import com.wisemapping.config.mvc.MvcSecurityConfig;
import com.wisemapping.config.rest.ServletConfig;
import com.wisemapping.config.rest.RestAppConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .parent(MethodSecurityConfig.class, HibernateConfig.class).web(WebApplicationType.NONE)
//                .child(MvcAppConfig.class, MvcSecurityConfig.class).web(WebApplicationType.SERVLET)
                .child(RestAppConfig.class, ServletConfig.class).web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}
