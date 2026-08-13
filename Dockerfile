# Stage 1: Build the application
# Upgraded to Maven 3.9 and Java 21 for Spring Boot 4.1.0 compatibility
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the entire project source code into the container
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the lightweight runtime image
# Upgraded to Java 21 JRE (Java Runtime Environment)
FROM eclipse-temurin:21-jre-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the exact packaged JAR file based on your pom.xml artifactId and version
COPY --from=build /app/target/tms-backend-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port that the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]