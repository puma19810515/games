package com.games.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置类
 * 配置主库（Master）和从库（Slave）数据源，实现读写分离
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    /**
     * 主库数据源 - 处理所有写操作
     */
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        log.info("Initializing Master DataSource");
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
        log.info("Master DataSource initialized successfully");
        return dataSource;
    }

    /**
     * 从库数据源 - 处理所有读操作
     */
    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        log.info("Initializing Slave DataSource");
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
        log.info("Slave DataSource initialized successfully");
        return dataSource;
    }

    /**
     * 动态数据源 - 根据上下文自动路由到主库或从库
     * 标记为 @Primary，作为默认数据源
     */
    @Bean(name = "dynamicDataSource")
    @Primary
    public DataSource dynamicDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource) {

        log.info("Initializing Dynamic DataSource");

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER, masterDataSource);
        targetDataSources.put(DataSourceType.SLAVE, slaveDataSource);

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        // 默认数据源设置为主库
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);

        log.info("Dynamic DataSource initialized with MASTER as default");
        return dynamicDataSource;
    }
}
