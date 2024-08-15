FROM openjdk:22-slim


COPY target/image-minifier-main-1.0-SNAPSHOT.jar image-minifier-main.jar

EXPOSE 8080

CMD ["java", "-jar", "image-minifier-main.jar"]