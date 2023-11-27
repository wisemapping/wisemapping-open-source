package com.wisemapping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {
//    @Value("${database.hibernate.dialect}")
//    private String dbDialect;
//
//    @Value("${database.driver}")
//    private String dbDriver;
//
//    @Value("${database.url}")
//    private String dbUrl;
//
//    @Value("${database.username}")
//    private String dbUsername;
//    @Value("${database.password}")
//    private String dbPassword;
//
//    @Value("${database.validation.enabled:true}")
//    private boolean dbSetOnBorrow;
//
//    @Value("${database.validation.query:SELECT 1}")
//    private String dbValQuery;

//    @Bean
//    public LocalSessionFactoryBean sessionFactory() {
//        final LocalSessionFactoryBean result = new LocalSessionFactoryBean();
//        result.setPackagesToScan("com.wisemapping.model");
//        result.setDataSource(dataSource());
//        result.setHibernateProperties(hibernateProperties());
//
//        return result;
//    }
//
//
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
