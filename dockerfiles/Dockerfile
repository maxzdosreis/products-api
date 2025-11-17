FROM eclipse-temurin:21-jre-alpine
COPY target/*.jar app.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "/app.jar"]