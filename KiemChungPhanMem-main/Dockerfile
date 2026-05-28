FROM maven:3.8.4-openjdk-17 AS build

COPY src /FoodStore/spring-mvc/src
COPY pom.xml /FoodStore/spring-mvc

WORKDIR /FoodStore/spring-mvc

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk

WORKDIR /FoodStore/spring-mvc

COPY --from=build /FoodStore/spring-mvc/target/*.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]