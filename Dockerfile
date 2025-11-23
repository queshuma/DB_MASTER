# 指定基础镜像
FROM openjdk:17

# 替换 APT 源为国内源
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list && \
    sed -i 's/security.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list

# 如果需要安装其他软件包，先更新源
RUN apt-get update && apt-get install -y curl

# 设置工作目录
WORKDIR /web

# 复制你的jar包到镜像中的工作目录
COPY DB-Master-0.0.1-SNAPSHOT.jar /web/

# 暴露服务端口
EXPOSE 8000

# 设置启动命令
CMD ["java", "-jar", "DB-Master-0.0.1-SNAPSHOT.jar"]