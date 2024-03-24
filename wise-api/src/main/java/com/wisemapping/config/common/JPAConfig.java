package com.wisemapping.config.common;

import com.wisemapping.dao.MindmapManagerImpl;
import com.wisemapping.model.Account;
import com.wisemapping.service.MindmapServiceImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@EnableJpaRepositories(basePackageClasses={MindmapServiceImpl.class, MindmapManagerImpl.class})
@EntityScan(basePackageClasses= Account.class)
public class JPAConfig {

}
