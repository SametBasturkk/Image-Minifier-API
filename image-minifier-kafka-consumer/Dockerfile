FROM openjdk:22-slim

COPY target/image-minifier-kafka-consumer-1.0-SNAPSHOT.jar image-minifier-kafka-consumer.jar
COPY minifiers/jpegoptim jpegoptim
COPY minifiers/pngquant pngquant

RUN chmod +x jpegoptim pngquant

EXPOSE 8080

CMD ["java", "-jar", "image-minifier-kafka-consumer.jar"]