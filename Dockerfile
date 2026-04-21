FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /home

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /home/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]