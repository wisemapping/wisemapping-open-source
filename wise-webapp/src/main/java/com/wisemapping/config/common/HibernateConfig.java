package com.wisemapping.config.common;

import com.wisemapping.dao.MindmapManagerImpl;
import com.wisemapping.model.User;
import com.wisemapping.service.MindmapServiceImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@EnableJpaRepositories(basePackageClasses={MindmapServiceImpl.class, MindmapManagerImpl.class})
@EntityScan(basePackageClasses= User.class)
public class HibernateConfig {

//    @Bean
//    public HibernateTransactionManager hibernateTransactionManager() {
//        final HibernateTransactionManager result = new HibernateTransactionManager();
//        result.setNestedTransactionAllowed(true);
//        // @Todo: Am I creatting two instances ???
//        result.setSessionFactory(sessionFactory().getObject());
//        return result;
//    }
//
//    private Properties hibernateProperties() {
//        final Properties result = new Properties();
//        result.setProperty("hibernate.dialect", dbDialect);
//        result.setProperty("hibernate.default_batch_fetch_size", "200");
//        result.setProperty("hibernate.nestedTransactionAllowed", "true");
//        result.setProperty("hibernate.auto_quote_keyword", "true");
//
//        return result;
//    }
//
//    @Bean
//    public DataSource dataSource() {
//        final BasicDataSource result = new BasicDataSource();
//        result.setDriverClassName(dbDriver);
//        result.setUrl(dbUrl);
//        result.setUsername(dbUsername);
//        result.setPassword(dbPassword);
//        result.setTestOnBorrow(dbSetOnBorrow);
//
//        result.setDefaultQueryTimeout(15);
//        result.setMaxTotal(100);
//        result.setMaxIdle(30);
//        result.setInitialSize(5);
//        result.setMaxWaitMillis(10000);
//        result.setValidationQuery(dbValQuery);
//
//        return result;
//    }
}
