#!/bin/sh
(
    set -eu

    # 仅向新实例导入表结构，数据库由官方 MySQL 镜像初始化
    schema=$(sed -n '/^CREATE TABLE /,/;[[:space:]]*$/p' /spiderflow-schema.sql)
    test -n "$schema"
    printf '%s\n' "$schema" |
        MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --protocol=socket --user=root --database="$MYSQL_DATABASE"
)
