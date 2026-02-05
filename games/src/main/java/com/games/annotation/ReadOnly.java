package com.games.annotation;

import java.lang.annotation.*;

/**
 * 只读操作注解
 * 用于标记只读方法，这些方法将被路由到从库（Slave）数据源执行
 *
 * 使用场景：
 * 1. 查询方法（SELECT）
 * 2. 报表生成
 * 3. 统计分析
 *
 * 注意：
 * 1. 标记了 @Transactional 且 readOnly=false 的方法会被路由到主库
 * 2. 标记了 @ReadOnly 的方法会被路由到从库
 * 3. 如果同时存在事务注解和只读注解，优先使用主库
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnly {
}
