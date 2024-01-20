package com.wisemapping.config;

import com.wisemapping.config.common.CommonConfig;
import com.wisemapping.config.common.HibernateConfig;
import com.wisemapping.config.mvc.MvcAppConfig;
import com.wisemapping.config.rest.InterceptorsConfig;
import com.wisemapping.config.common.SecurityConfig;
import com.wisemapping.config.rest.ServletConfig;
import com.wisemapping.config.rest.RestAppConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.StrictHttpFirewall;

public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .parent(CommonConfig.class).web(WebApplicationType.NONE)
                .child(MvcAppConfig.class).web(WebApplicationType.SERVLET)
                .sibling(RestAppConfig.class).web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}
