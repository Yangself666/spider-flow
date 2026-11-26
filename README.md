<p align="center">
    <img src="https://www.spiderflow.org/images/logo.svg" width="600">
</p>
<p align="center">
    <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html"><img src="https://img.shields.io/badge/JDK-25-green.svg" /></a>
    <a target="_blank" href="https://www.spiderflow.org"><img src="https://img.shields.io/badge/Docs-latest-blue.svg"/></a>
    <a target="_blank" href="https://github.com/ssssssss-team/spider-flow/releases"><img src="https://img.shields.io/github/v/release/ssssssss-team/spider-flow?logo=github"></a>
    <a target="_blank" href='https://gitee.com/ssssssss-team/spider-flow'><img src="https://gitee.com/ssssssss-team/spider-flow/badge/star.svg?theme=white" /></a>
    <a target="_blank" href='https://github.com/ssssssss-team/spider-flow'><img src="https://img.shields.io/github/stars/ssssssss-team/spider-flow.svg?style=social"/></a>
    <a target="_blank" href="LICENSE"><img src="https://img.shields.io/:license-MIT-blue.svg"></a>
    <a target="_blank" href="https://shang.qq.com/wpa/qunwpa?idkey=10faa4cf9743e0aa379a72f2ad12a9e576c81462742143c8f3391b52e8c3ed8d"><img src="https://img.shields.io/badge/Join-QQGroup-blue"></a>
</p>

[介绍](#介绍) | [特性](#特性) | [插件](#插件) | <a target="_blank" href="http://demo.spiderflow.org">DEMO站点</a> | <a target="_blank" href="https://www.spiderflow.org">文档</a> | <a target="_blank" href="https://www.spiderflow.org/changelog.html">更新日志</a> | [截图](#项目部分截图) | [其它开源](#其它开源项目) | [免责声明](#免责声明)

## 介绍
平台以流程图的方式定义爬虫,是一个高度灵活可配置的爬虫平台

## 特性
- [x] 支持Xpath/JsonPath/css选择器/正则提取/混搭提取
- [x] 支持JSON/XML/二进制格式
- [x] 支持多数据源、SQL select/selectInt/selectOne/insert/update/delete
- [x] 支持爬取JS动态渲染(或ajax)的页面
- [x] 支持代理
- [x] 支持自动保存至数据库/文件
- [x] 常用字符串、日期、文件、加解密等函数
- [x] 支持插件扩展(自定义执行器，自定义方法）
- [x] 任务监控,任务日志
- [x] 支持HTTP接口
- [x] 支持Cookie自动管理
- [x] 支持自定义函数

## 插件
- [x] [Selenium插件](https://gitee.com/ssssssss-team/spider-flow-selenium)
- [x] [Redis插件](https://gitee.com/ssssssss-team/spider-flow-redis)
- [x] [OSS插件](https://gitee.com/ssssssss-team/spider-flow-oss)
- [x] [Mongodb插件](https://gitee.com/ssssssss-team/spider-flow-mongodb)
- [x] [IP代理池插件](https://gitee.com/ssssssss-team/spider-flow-proxypool)
- [x] [OCR识别插件](https://gitee.com/ssssssss-team/spider-flow-ocr)
- [x] [电子邮箱插件](https://gitee.com/ssssssss-team/spider-flow-mailbox)

## 项目部分截图
### 爬虫列表
![爬虫列表](https://images.gitee.com/uploads/images/2020/0412/104521_e1eb3fbb_297689.png "list.png")
### 爬虫测试
![爬虫测试](https://images.gitee.com/uploads/images/2020/0412/104659_b06dfbf0_297689.gif "test.gif")
### Debug
![Debug](https://images.gitee.com/uploads/images/2020/0412/104741_f9e1190e_297689.png "debug.png")
### 日志
![日志](https://images.gitee.com/uploads/images/2020/0412/104800_a757f569_297689.png "logo.png")

## 其它开源项目
- [spider-flow-vue，spider-flow的前端](https://gitee.com/ssssssss-team/spider-flow-vue)
- [magic-api，一个以XML为基础自动映射为HTTP接口的框架](https://gitee.com/ssssssss-team/magic-api)
- [magic-api-spring-boot-starter](https://gitee.com/ssssssss-team/magic-api-spring-boot-starter)


## Docker 部署

### 构建镜像

在项目根目录执行以下任一命令。Dockerfile 使用 Maven 和 Temurin 25 构建应用，运行镜像使用 Temurin 25 JRE。本地执行 Maven 构建时，请使用 JDK 25。

使用 Docker 直接构建。

```bash
docker build -t spider-flow:0.6.0 .
```

也可以用 Compose 构建同一个镜像。

```bash
docker compose build spider-flow
```

构建完成后可用 `docker image ls spider-flow` 查看镜像。

### 启动服务

直接编辑 `compose.yaml`。使用内置 MySQL 时，把 `MYSQL_PASSWORD` 和 `SPRING_DATASOURCE_PASSWORD` 改成同一个业务账号密码，再为 `MYSQL_ROOT_PASSWORD` 设置管理密码。

```bash
mkdir -p ./data/spider ./data/mysql
docker compose up -d --build
docker compose ps
```

Java 进程使用容器启动用户运行，默认继承基础镜像的用户。可通过 Compose 的 `user` 指定 UID/GID，该用户需要能写入挂载的工作目录。接入外部 MySQL 时只需准备 `./data/spider`。

默认访问 `http://127.0.0.1:8088`。应用端口默认仅绑定宿主机回环地址，线上可由反向代理转发。MySQL 数据和 SpiderFlow 工作目录分别保存在 Compose 文件旁的 `./data/mysql` 和 `./data/spider`。内置 MySQL 首次启动时会创建空表。已有命名卷中的数据需要在切换前迁入对应目录。

日志集中保存在 `./data/spider/logs` 下。

- 应用日志为 `spider-flow.log`
- 新任务日志为 `<流程ID>/<任务ID>.log`
- 请求失败记录为 `<流程ID>/access_error.log`

已有任务日志仍可在页面查看和下载。

### 升级已有部署

在原部署目录更新 Dockerfile、Compose 配置和项目源码，沿用已部署的数据库连接、监听端口和绑定挂载。用以下命令重建并更新 SpiderFlow 应用容器。

```bash
docker compose build spider-flow
docker compose up -d --no-deps spider-flow
docker compose ps spider-flow
```

| 环境变量 | 用途 |
| --- | --- |
| `ports` | 应用在宿主机的监听地址和端口 |
| `MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD` | Compose 内置 MySQL 的首次初始化参数 |
| `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD` | 覆盖应用的数据库连接 |
| `SPIDER_JOB_ENABLE` | 设为 `true` 后运行已启用且配置了 Cron 的流程，默认 `false` |
| `SPRING_MAIL_HOST`、`SPRING_MAIL_PORT`、`SPRING_MAIL_USERNAME`、`SPRING_MAIL_PASSWORD` | 邮件服务器和账号 |
| `SMTP_AUTH`、`SMTP_SSL_ENABLE`、`SMTP_STARTTLS_ENABLE` | SMTP 认证和加密模式。常见配置为 465 端口启用 SSL，或 587 端口启用 STARTTLS |
| `TZ`、`JAVA_TOOL_OPTIONS` | 容器时区和 JVM 参数 |
| `LOGGING_LEVEL_ROOT` | 应用日志级别，默认 `INFO`。设为 `DEBUG` 可查看脚本的 `console.debug()` |

使用已有 MySQL 时，直接修改 `spider-flow.environment` 下的三项 `SPRING_DATASOURCE_*`，确认目标库已有 SpiderFlow 表结构，再单独启动应用。

```bash
docker compose up -d --build --no-deps spider-flow
```

定时任务、线程数、时区、JVM 参数和 SMTP 属于可选项。按需在 `compose.yaml` 取消对应行的注释并修改示例值，再重新执行启动命令应用配置。

## 免责声明
请勿将`spider-flow`应用到任何可能会违反法律规定和道德约束的工作中，请友善使用`spider-flow`，遵守蜘蛛协议，不要将`spider-flow`用于任何非法用途。如您选择使用`spider-flow`即代表您遵守此协议，作者不承担任何由于您违反此协议带来任何的法律风险和损失，一切后果由您承担。
