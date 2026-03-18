FROM maven:3.9.9-eclipse-temurin-8 AS build
WORKDIR /workspace
COPY .. /workspace
RUN mvn -q -DskipTests -pl order-service -am package

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /workspace/order-service/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
