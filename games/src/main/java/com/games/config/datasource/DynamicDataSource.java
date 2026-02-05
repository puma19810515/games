package com.games.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源路由器
 * 根据当前线程的数据源类型，动态切换到对应的数据源
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 决定使用哪个数据源
     * 在每次数据库操作前，Spring会调用此方法来确定使用哪个数据源
     */
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DataSourceContextHolder.getDataSourceType();
        log.debug("Current datasource type: {}", type);
        return type;
    }
}
