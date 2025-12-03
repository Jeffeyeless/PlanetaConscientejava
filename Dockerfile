# Imagen base con Java 17
FROM eclipse-temurin:17-jdk-alpine

# Directorio de trabajo
WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Permisos para mvnw
RUN chmod +x mvnw

# Compilar el proyecto
RUN ./mvnw clean package -DskipTests

# Buscar el jar real y renombrarlo a app.jar
RUN cp target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Ejecutar aplicación
CMD ["java", "-jar", "app.jar"]
