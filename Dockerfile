# Stage 1: Build the application using Maven
FROM openjdk:17-jdk-slim as build

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: Create the final, smaller runtime image
FROM amazoncorretto:17-al2-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]