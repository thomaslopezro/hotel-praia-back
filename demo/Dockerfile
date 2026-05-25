# Multi-stage build para Spring Boot
# Etapa 1: compilar el JAR con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: imagen ligera solo con el JAR + JRE 17
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Render inyecta $PORT - el app ya lo lee desde application-prod.properties
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
