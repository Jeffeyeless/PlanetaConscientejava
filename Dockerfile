FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw -q -DskipTests clean package

RUN cp target/*.jar app.jar

RUN mkdir -p /app/uploads

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
