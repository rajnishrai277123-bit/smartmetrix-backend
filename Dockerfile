FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests && cp target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]