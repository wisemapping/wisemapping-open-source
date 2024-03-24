package com.wisemapping;

import com.wisemapping.config.common.CommonConfig;
import com.wisemapping.config.rest.RestAppConfig;
import com.wisemapping.config.rest.WebConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.StrictHttpFirewall;

public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .parent(CommonConfig.class).web(WebApplicationType.NONE)
                .child(RestAppConfig.class, WebConfig.class).web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}
