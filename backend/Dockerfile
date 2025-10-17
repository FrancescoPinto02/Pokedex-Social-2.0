# ---------------------------
# STAGE 1: Build dell'app con Maven
# ---------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Imposta la working directory
WORKDIR /app

# Copia i file di progetto e scarica le dipendenze
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia il codice sorgente e builda l'app
COPY src ./src
RUN mvn clean package -DskipTests -Dcheckstyle.skip=true

# ---------------------------
# STAGE 2: Immagine finale runtime
# ---------------------------
FROM eclipse-temurin:17-jdk

# Imposta la working directory
WORKDIR /app

# Copia il jar generato dallo stage precedente
COPY --from=build /app/target/*.jar app.jar

# Espone la porta del backend
EXPOSE 8080

# Variabili d'ambiente per DB
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/pokedex \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=postgres

# Comando di avvio
ENTRYPOINT ["java", "-jar", "app.jar"]
