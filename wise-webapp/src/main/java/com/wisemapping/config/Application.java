package com.wisemapping.config;

import com.wisemapping.config.common.CommonConfig;
import com.wisemapping.config.common.HibernateConfig;
import com.wisemapping.config.common.InterceptorsConfig;
import com.wisemapping.config.common.SecurityConfig;
import com.wisemapping.config.mvc.MvcAppConfig;
import com.wisemapping.config.mvc.MvcSecurityConfig;
import com.wisemapping.config.rest.ServletConfig;
import com.wisemapping.config.rest.RestAppConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .parent(HibernateConfig.class, ServletConfig.class, CommonConfig.class, SecurityConfig.class).web(WebApplicationType.NONE)
                .child(MvcAppConfig.class, MvcSecurityConfig.class, SecurityConfig.class, InterceptorsConfig.class).web(WebApplicationType.SERVLET)
                .sibling(RestAppConfig.class, ServletConfig.class, InterceptorsConfig.class).web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}
