FROM anapsix/alpine-java:jdk8 as builder

RUN apk add --no-cache --update ca-certificates wget git && \
    update-ca-certificates

RUN wget -O - https://github.com/sbt/sbt/releases/download/v1.1.0/sbt-1.1.0.tgz \
    | gunzip \
    | tar -x -C /usr/local

ENV PATH="/usr/local/sbt/bin:${PATH}"

RUN git clone https://github.com/Lykke-Waves/lykke-waves-common.git \
    && cd lykke-waves-common \
    && git checkout 0.0.12 \
    && sbt clean publishLocal

RUN git clone https://github.com/Lykke-Waves/lykke-waves-blockchain-api.git \
    && cd lykke-waves-blockchain-api \
    && git checkout 0.0.21 \
    && sbt clean assembly

RUN mv `find /lykke-waves-blockchain-api/target/scala-2.12 -name *.jar` /lykke-waves-blockchain-api.jar && chmod -R 744 /lykke-waves-blockchain-api.jar

FROM openjdk:9-jre-slim
MAINTAINER Sergey Tolmachev <tolsi.ru@gmail.com>
WORKDIR /app
COPY --from=builder /lykke-waves-blockchain-api.jar /app

EXPOSE 8080

RUN useradd -s /bin/false lykke-waves-blockchain-api
USER lykke-waves-blockchain-api

COPY config-for-docker.json /app/config.json
ENV ENV_INFO=dev
ENV SettingsUrl=file:///app/config.json

CMD ["/usr/bin/java", "-jar", "/app/lykke-waves-blockchain-api.jar"]
