FROM maven:3.9-eclipse-temurin-25 AS build
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

FROM eclipse-temurin:25-jre-noble
LABEL org.opencontainers.image.title="SpiderFlow" \
      org.opencontainers.image.version="0.6.0"
WORKDIR /opt/spider-flow

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data/spider/logs

COPY --from=build /build/spider-flow-web/target/spider-flow.jar ./spider-flow.jar
ENV TZ=Asia/Shanghai SERVER_PORT=8088 SPIDER_WORKSPACE=/data/spider \
    JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m"
EXPOSE 8088

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS --max-time 3 "http://127.0.0.1:${SERVER_PORT}/index.html" > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/opt/spider-flow/spider-flow.jar"]
