FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY book-service/pom.xml book-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY review-service/pom.xml review-service/pom.xml

RUN mvn dependency:go-offline -pl order-service -am -q

COPY order-service/src order-service/src

RUN mvn clean package -pl order-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/order-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
