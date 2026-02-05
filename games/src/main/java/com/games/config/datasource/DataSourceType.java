package com.games.config.datasource;

/**
 * 数据源类型枚举
 */
public enum DataSourceType {
    /**
     * 主库 - 处理所有写操作（INSERT, UPDATE, DELETE）
     */
    MASTER,

    /**
     * 从库 - 处理所有读操作（SELECT）
     */
    SLAVE
}
