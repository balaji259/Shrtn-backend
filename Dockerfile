# Use Eclipse Temurin JDK 17 (compatible with Spring Boot 3+)
FROM eclipse-temurin:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy everything from the repo into the image
COPY . .

# Grant permissions if needed
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Run the application (replace with your final JAR name)
CMD ["java", "-jar", "target/shrtn-backend.jar"]
