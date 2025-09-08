package com.wisemapping;

import com.wisemapping.config.AppConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.StrictHttpFirewall;

public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(AppConfig.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}
