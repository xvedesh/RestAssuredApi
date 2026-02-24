FROM maven:3.9-eclipse-temurin-11

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY config.properties ./config.properties

RUN mvn -q -DskipTests clean package

ENV CUCUMBER_TAGS="@ClientData"

CMD mvn -q test -Dcucumber.filter.tags=${CUCUMBER_TAGS}
