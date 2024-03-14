FROM eclipse-temurin:21
RUN mkdir /opt/app
COPY . /opt/app

WORKDIR /opt/app
RUN ./mvnw verify
ENTRYPOINT ["./mvnw", "exec:java", "-Dexec.mainClass=com.example.ServerMain", "-Dh1certpath=selfsigned.p12", "-Dh2certpath=selfsigned.p12"]
