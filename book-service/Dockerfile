FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY book-service/pom.xml book-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY review-service/pom.xml review-service/pom.xml

RUN mvn dependency:go-offline -pl book-service -am -q

COPY book-service/src book-service/src

RUN mvn clean package -pl book-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app

COPY --from=builder /app/book-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
