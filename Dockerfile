FROM bigtruedata/scala:2.12.4

ENV ENV_INFO=dev
WORKDIR /app

RUN wget -O- "https://github.com/sbt/sbt/releases/download/v1.1.0/sbt-1.1.0.tgz" \
    |  tar xzf - -C /usr/local --strip-components=1

RUN git clone https://github.com/Lykke-Waves/lykke-waves-common.git \
    && cd lykke-waves-common \
    && git checkout 0.0.1 \
    && sbt clean publishLocal

RUN git clone https://github.com/Lykke-Waves/lykke-waves-blockchain-api.git \
    && cd lykke-waves-blockchain-api \
    && git checkout 8b48f0a \
    && sbt clean assembly \
    && pwd \
    && cp target/scala-2.12/*.jar /app

EXPOSE 8080

CMD ["find . -name *.jar -exec java -jar {} \;"]