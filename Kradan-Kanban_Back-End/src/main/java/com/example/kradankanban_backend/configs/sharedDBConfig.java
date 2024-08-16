package com.example.kradankanban_backend.configs;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "sharedEntityManagerFactory",
        transactionManagerRef = "sharedTransactionManager",
        basePackages = { "com.example.kradankanban_backend.shared" }
)
public class sharedDBConfig {
    @Bean(name = "sharedDataSource")
    @ConfigurationProperties(prefix = "shared.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sharedEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    sharedEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("sharedDataSource") DataSource dataSource
    ) {
        return
                builder
                        .dataSource(dataSource)
                        .packages("com.example.kradankanban_backend.shared")
                        .persistenceUnit("shared")
                        .build();
    }
    @Bean(name = "sharedTransactionManager")
    public PlatformTransactionManager sharedTransactionManager(
            @Qualifier("sharedEntityManagerFactory") EntityManagerFactory
                    sharedEntityManagerFactory
    ) {
        return new JpaTransactionManager(sharedEntityManagerFactory);
    }
}
