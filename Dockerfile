# Stage 1 — Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY frontend ./frontend
RUN mvn clean package -DskipTests -q

# Stage 2 — Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Railway injects PORT at runtime; default to 8080 locally
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dserver.port=${PORT:-8080}", "app.jar"]
