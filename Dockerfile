FROM openjdk:8-jdk

RUN apt-get update && \
    apt-get install -y --fix-missing ffmpeg

# Создай рабочую директорию
WORKDIR /app

# Копируем .jar в /app
COPY target/onvif-camera-project-1.0-SNAPSHOT.jar app.jar

# Запуск из /app
ENTRYPOINT ["java", "-jar", "app.jar"]
