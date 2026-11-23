FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress -Dmaven.test.skip=true package

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/fitombo-*.jar /app/fitombo.jar
USER 10001:10001
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/fitombo.jar"]
