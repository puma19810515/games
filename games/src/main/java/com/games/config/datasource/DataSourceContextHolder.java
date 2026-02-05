package com.games.config.datasource;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据源上下文持有者
 * 使用 ThreadLocal 存储当前线程使用的数据源类型
 */
@Slf4j
public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置数据源类型
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        if (dataSourceType == null) {
            log.warn("DataSourceType is null, using default MASTER");
            CONTEXT_HOLDER.set(DataSourceType.MASTER);
        } else {
            log.debug("Switch to {} datasource", dataSourceType);
            CONTEXT_HOLDER.set(dataSourceType);
        }
    }

    /**
     * 获取数据源类型
     */
    public static DataSourceType getDataSourceType() {
        DataSourceType type = CONTEXT_HOLDER.get();
        if (type == null) {
            log.debug("No datasource type set, using default MASTER");
            return DataSourceType.MASTER;
        }
        return type;
    }

    /**
     * 清除数据源类型
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
        log.debug("Cleared datasource type from context");
    }
}
