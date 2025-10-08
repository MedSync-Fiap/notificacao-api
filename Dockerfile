FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:resolve

COPY src ./src

RUN mvn clean package 

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/cadastro-agendamento-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]