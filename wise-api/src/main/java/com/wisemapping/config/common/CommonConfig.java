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
@Import({JPAConfig.class, SecurityConfig.class})
@EnableAutoConfiguration
public class CommonConfig {
}
