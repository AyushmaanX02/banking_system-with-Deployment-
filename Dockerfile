# Stage 1 — Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw package -DskipTests -q

# Stage 2 — Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Railway injects PORT at runtime; default to 8080 locally
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dserver.port=${PORT:-8080}", "app.jar"]
