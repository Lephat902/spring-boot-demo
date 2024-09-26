# Use a Maven image for development
FROM maven:3.9.9-eclipse-temurin-21-alpine AS dev

WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Run the application with devtools
CMD ["mvn", "spring-boot:run"]