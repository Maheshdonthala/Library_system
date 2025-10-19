# Multi-stage Dockerfile: build with Maven, run with Eclipse Temurin JRE

# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
# Use the Maven image's installed mvn. Do not rely on the repo containing the Maven wrapper.
WORKDIR /workspace
# Copy only the POM and source tree. The builder image already has Maven installed.
COPY pom.xml ./
# copy sources
COPY src ./src
# package
RUN mvn -B -DskipTests package

# Run stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# Use the specific JAR file name for robustness
COPY --from=build /workspace/target/librarysystem-0.0.1-SNAPSHOT.jar app.jar
# Pass the MongoDB DNS resolver explicitly as a JVM argument in the exec form
# to avoid shell quoting/expansion issues that can prevent the driver from
# seeing the property in some container environments.
ENTRYPOINT ["java", "-Dcom.mongodb.dns.resolver=dnsjava", "-Xms256m", "-Xmx512m", "-jar", "/app/app.jar"]
