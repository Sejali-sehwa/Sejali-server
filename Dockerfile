FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8010

ENTRYPOINT ["java", "-jar", "app.jar"]
