FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S eventos && adduser -S eventos -G eventos
COPY --from=build /app/target/eventos-0.0.1-SNAPSHOT.jar app.jar
USER eventos
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
