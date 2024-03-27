# Use OpenJDK 11 as the base image
FROM openjdk:11-jdk-slim

# Install Maven
RUN apt-get update && \
    apt-get install -y maven

# Set the working directory in the container
WORKDIR /app

# Copy the project files into the working directory
COPY . .

# Build the project without running tests
RUN mvn clean install -DskipTests

# Command to run tests
CMD ["mvn", "test", "-Dcucumber.filter.tags=@ClientData"]
