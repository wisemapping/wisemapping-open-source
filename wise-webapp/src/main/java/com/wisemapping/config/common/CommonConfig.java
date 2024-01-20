package com.wisemapping.config.common;

import com.wisemapping.config.rest.ServletConfig;
import com.wisemapping.dao.LabelManagerImpl;
import com.wisemapping.model.Mindmap;
import com.wisemapping.security.AuthenticationProvider;
import com.wisemapping.service.MindmapServiceImpl;
import com.wisemapping.util.VelocityEngineUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(value = {"classpath:spring/wisemapping-mail.xml"})
@ComponentScan(basePackageClasses = {HibernateConfig.class, SecurityConfig.class, AuthenticationProvider.class, MindmapServiceImpl.class, LabelManagerImpl.class, VelocityEngineUtils.class})
public class CommonConfig {
}
