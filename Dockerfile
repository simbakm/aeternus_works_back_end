FROM maven:3.8.8-eclipse-temurin-17 AS build

WORKDIR /workspace

# Copy pom and sources
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src src

# Build the application inside the image
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Copy JAR from the build stage
COPY --from=build /workspace/target/aeternus_back_end-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
