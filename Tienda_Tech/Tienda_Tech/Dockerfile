# syntax=docker/dockerfile:1

# ----- Build stage -----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copia solo lo necesario para cachear dependencias
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copia el código y compila
COPY src ./src
RUN mvn -DskipTests clean package

# ----- Runtime stage -----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia el jar (usa wildcard)
COPY --from=build /workspace/target/*-SNAPSHOT.jar app.jar

# Render inyecta $PORT; seteamos por si se ejecuta localmente
ENV JAVA_OPTS=""
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
