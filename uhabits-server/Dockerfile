FROM openjdk:8-jre-alpine
RUN mkdir /app
COPY uhabits-server.jar /app/uhabits-server.jar
ENV LOOP_REPO_PATH /data/
WORKDIR /app
CMD ["java", \
     "-server", \
     "-XX:MaxGCPauseMillis=100", \
     "-XX:+UseStringDeduplication", \
     "-jar", \
     "uhabits-server.jar"]