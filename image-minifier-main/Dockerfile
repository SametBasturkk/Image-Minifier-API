FROM openjdk:22-slim

COPY target/image-minifier-main-1.0-SNAPSHOT.jar image-minifier-main.jar

EXPOSE 8080
EXPOSE 5005

CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "image-minifier-main.jar"]
