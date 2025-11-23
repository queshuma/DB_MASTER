FROM openjdk:17

RUN echo "deb https://mirrors.aliyun.com/debian/ bookworm main contrib non-free" > /etc/apt/sources.list && \
    echo "deb https://mirrors.aliyun.com/debian/ bookworm-updates main contrib non-free" >> /etc/apt/sources.list && \
    echo "deb https://mirrors.aliyun.com/debian-security bookworm-security main contrib non-free" >> /etc/apt/sources.list

RUN apt-get update && apt-get install -y curl

WORKDIR /web
COPY DB-Master-0.0.1-SNAPSHOT.jar /web/
EXPOSE 8000
CMD ["java", "-jar", "DB-Master-0.0.1-SNAPSHOT.jar"]
