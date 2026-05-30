# ---- Build stage: full JDK + Maven wrapper, produces the jar ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Pre-download dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Now bring in the source and build
COPY src ./src
RUN ./mvnw -B -q package -DskipTests

# ---- Runtime stage: slim JRE only ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]