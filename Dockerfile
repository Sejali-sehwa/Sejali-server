# 1단계: Gradle 빌드
FROM gradle:8.4-jdk21 AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew build -x test --no-daemon

# 2단계: 실행용 이미지
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/build/libs/sejali-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8010
ENTRYPOINT ["java", "-jar", "app.jar"]
