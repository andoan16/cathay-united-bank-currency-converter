FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

FROM tomcat:10.1-jre17-temurin-focal
WORKDIR /usr/local/tomcat/webapps/

RUN rm -rf ROOT

COPY --from=builder /app/target/*.war ROOT.war

# Expose Tomcat's default port
EXPOSE 8080