FROM maven:3.9-eclipse-temurin-11

WORKDIR /app
COPY pom.xml .
COPY testng.xml ./testng.xml
COPY testng-rerun.xml ./testng-rerun.xml
COPY src ./src
COPY config.properties ./config.properties

RUN mvn -q -DskipTests clean package

CMD ["mvn", "-q", "test"]
