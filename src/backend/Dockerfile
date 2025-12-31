# Backend Dockerfile
FROM maven:3.9.9-eclipse-temurin-22-alpine AS build
WORKDIR /workspace
COPY pom.xml mvnw* ./
COPY .mvn .mvn
COPY checkstyle.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:22-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
