# 1. Download Java 21 image
FROM eclipse-temurin:21-jdk

# 2. Set working directory
WORKDIR /app

# 3. Copy project files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# 4. Set permissions for Maven wrapper (Linux/MacOS)
RUN chmod +x mvnw

# 5. Download Maven dependencies
RUN ./mvnw dependency:go-offline

# 6. Copy the source code and build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# 7. Copy the generated JAR from the build process
COPY target/CentralBank-0.0.1-SNAPSHOT.jar app.jar

# 8. Launch the application
CMD ["java", "-jar", "app.jar"]