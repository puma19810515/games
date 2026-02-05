#!/bin/bash

echo "==================================="
echo "检查服务依赖..."
echo "==================================="

# 检查 MySQL
echo -n "检查 MySQL (localhost:3306)... "
nc -z localhost 3306 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 运行中"
else
    echo "❌ 未运行"
    echo "   启动命令: docker-compose up -d mysql"
fi

# 检查 Redis
echo -n "检查 Redis (localhost:6379)... "
nc -z localhost 6379 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 运行中"
else
    echo "❌ 未运行"
    echo "   启动命令: docker-compose up -d redis"
fi

# 检查 RocketMQ
echo -n "检查 RocketMQ (localhost:9876)... "
nc -z localhost 9876 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 运行中"
else
    echo "❌ 未运行 (可选服务)"
    echo "   启动命令: docker-compose up -d rocketmq-namesrv rocketmq-broker"
fi

echo ""
echo "==================================="
echo "读写分离配置状态"
echo "==================================="
SLAVE_PORT=$(grep -A 15 "slave:" src/main/resources/application.yml | grep "jdbc-url" | grep -o "localhost:[0-9]*" | cut -d: -f2)

if [ "$SLAVE_PORT" == "3306" ]; then
    echo "⚠️  读写分离: 已禁用 (主从都使用 localhost:3306)"
    echo "   主库和从库都指向同一个数据库"
    echo "   这是开发环境的推荐配置"
elif [ "$SLAVE_PORT" == "3307" ]; then
    echo "✅ 读写分离: 已启用"
    echo "   主库: localhost:3306"
    echo "   从库: localhost:3307"
    echo -n "   从库状态: "
    nc -z localhost 3307 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ 运行中"
    else
        echo "❌ 未运行"
        echo "   请先配置 MySQL 主从复制，或修改配置文件暂时禁用读写分离"
    fi
fi

echo ""
echo "==================================="
echo "快速启动命令"
echo "==================================="
echo "启动所有依赖: docker-compose up -d"
echo "启动应用: ./mvnw spring-boot:run"
echo ""
