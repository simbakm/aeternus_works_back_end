FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Copy the built jar (we will use multi-stage build locally or rely on CI to build jar)
COPY target/aeternus_back_end-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
