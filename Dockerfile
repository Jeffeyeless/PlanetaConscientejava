# Imagen base con Java 17
FROM eclipse-temurin:17-jdk-alpine

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Dar permisos a mvnw
RUN chmod +x mvnw

# Compilar el proyecto
RUN ./mvnw clean package -DskipTests

# Exponer el puerto de Render
EXPOSE 8080

# Ejecutar la aplicación
CMD ["java", "-jar", "target/*.jar"]
