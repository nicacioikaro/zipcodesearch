# ---- Stage 1: build ----
# Builds the jar inside the image so the host doesn't need Maven/JDK.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy only what's needed to resolve dependencies first (better layer caching:
# dependencies are re-downloaded only when pom.xml changes, not on every code change).
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline

# Now copy the source and build
COPY src/ src/
RUN ./mvnw -q -B clean package -DskipTests

# ---- Stage 2: runtime ----
# Smaller image with only the JRE; the build tools stay in stage 1 and are discarded.
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user (security best practice; never run app containers as root)
RUN useradd -r -u 1001 appuser
USER appuser

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Honor container memory limits and prefer the container-aware JVM defaults
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
