# Etapa 1: Construcción (Build)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargar dependencias (esto ayuda a que los builds sean más rápidos)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copiar el código fuente y compilar
COPY src src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecución (Runtime)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]