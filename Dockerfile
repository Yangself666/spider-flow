FROM maven:3.9.12-eclipse-temurin-8 AS build
WORKDIR /build

COPY pom.xml ./
COPY spider-flow-api/pom.xml spider-flow-api/pom.xml
COPY spider-flow-core/pom.xml spider-flow-core/pom.xml
COPY spider-flow-web/pom.xml spider-flow-web/pom.xml
COPY spider-flow-api/src/main/ spider-flow-api/src/main/
COPY spider-flow-core/src/main/ spider-flow-core/src/main/
COPY spider-flow-web/src/main/ spider-flow-web/src/main/
COPY docker/application.properties spider-flow-web/src/main/resources/application.properties

RUN mvn -B -ntp package

FROM eclipse-temurin:8-jre-jammy
WORKDIR /opt/spider-flow

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 spiderflow \
    && useradd --uid 10001 --gid 10001 --no-create-home --no-log-init spiderflow \
    && install -d -o 10001 -g 10001 /data/spider /data/spider/logs

COPY --from=build /build/spider-flow-web/target/spider-flow.jar ./spider-flow.jar
ENV TZ=Asia/Shanghai SERVER_PORT=8088 SPIDER_WORKSPACE=/data/spider \
    JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m"
USER 10001:10001
EXPOSE 8088

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS --max-time 3 "http://127.0.0.1:${SERVER_PORT}/index.html" > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/opt/spider-flow/spider-flow.jar"]
