FROM openjdk:22-slim

COPY target/kafka-consumer-1.0-SNAPSHOT.jar kafka-consumer.jar
COPY ./minifiers/jpegoptim jpegoptim
COPY ./minifiers/pngquant pngquant

EXPOSE 8080

CMD ["java", "-jar", "kafka-consumer.jar"]