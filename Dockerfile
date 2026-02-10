# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY Makefile .
COPY Makefile.local .

RUN apt-get update \
 && apt-get install -y make \
 && rm -rf /var/lib/apt/lists/*

RUN make deps
RUN make build

# RUN chmod +x postgres-initdb.s

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

#ENTRYPOINT ["java","-Dspring.devtools.restart.enabled=true","-Dspring.devtools.livereload.enabled=true","-Dspring.devtools.remote.secret=mysecret","-jar","/app/app.jar"]
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]