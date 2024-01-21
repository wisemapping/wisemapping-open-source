package com.wisemapping.config.common;

import com.wisemapping.dao.LabelManagerImpl;
import com.wisemapping.security.AuthenticationProvider;
import com.wisemapping.service.MindmapServiceImpl;
import com.wisemapping.util.VelocityEngineUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@ComponentScan(basePackageClasses = {AuthenticationProvider.class, MindmapServiceImpl.class, LabelManagerImpl.class, VelocityEngineUtils.class})
@Import({HibernateConfig.class, SecurityConfig.class})
@EnableAutoConfiguration
@ImportResource(value = {"classpath:spring/wisemapping-mail.xml"})
public class CommonConfig {
}
