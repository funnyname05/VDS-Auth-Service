# ── Etapa 1: compilar ────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Descarga dependencias primero (caching de capas)
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ── Etapa 2: imagen final liviana ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Carpeta para el archivo H2
RUN mkdir -p /app/data
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
