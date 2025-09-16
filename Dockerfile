# Build stage

FROM alpine/java:21-jdk AS builder

WORKDIR /app

COPY . .
RUN ./gradlew clean build -x test


# Run stage

FROM alpine/java:21-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 50051
EXPOSE 8080

ENTRYPOINT [ "java", "-Duser.timezon e=Asia/Seoul", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar" ]