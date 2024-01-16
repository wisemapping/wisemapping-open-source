package com.wisemapping.config.common;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(value = {"classpath:spring/wisemapping-mail.xml"})
@ComponentScan({"com.wisemapping.security", "com.wisemapping.service", "com.wisemapping.dao", "com.wisemapping.util", "com.wisemapping.model"})
public class CommonConfig {
}
