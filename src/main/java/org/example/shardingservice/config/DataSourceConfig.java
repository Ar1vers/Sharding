package org.example.shardingservice.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties("app.datasource.shard1")
    public DataSourceProperties shard1Properties() {
        return new DataSourceProperties();
    }
    @Bean
    @ConfigurationProperties("app.datasource.shard2")
    public DataSourceProperties shard2Properties() {
        return new DataSourceProperties();
    }
    @Bean
    @ConfigurationProperties("app.datasource.shard3")
    public DataSourceProperties shard3Properties() {
        return new DataSourceProperties();
    }
    @Bean
    public DataSource shard1DataSource() {
        return shard1Properties().initializeDataSourceBuilder().build();
    }
    @Bean
    public DataSource shard2DataSource() {
        return shard2Properties().initializeDataSourceBuilder().build();
    }
    @Bean
    public DataSource shard3DataSource() {
        return shard3Properties().initializeDataSourceBuilder().build();
    }
    @Bean
    public DataSource routingDataSource() {
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("shard1", shard1DataSource());
        dataSources.put("shard2", shard2DataSource());
        dataSources.put("shard3", shard3DataSource());

        ShardRoutingDataSource routingDataSource = new ShardRoutingDataSource();
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(shard1DataSource());
        return routingDataSource;
    }
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(routingDataSource());
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(routingDataSource())
                .packages("org.example.shardingservice.model")
                .persistenceUnit("shardedPersistenceUnit")
                .build();
    }
    @Primary
    @Bean
    public DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }
}
