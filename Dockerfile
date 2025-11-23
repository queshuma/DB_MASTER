# 指定基础镜像
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:17.0.2-slim-linuxarm64

# 设置工作目录
WORKDIR /web

# 复制你的jar包到镜像中的工作目录
#COPY DB-Master-0.0.1-SNAPSHOT.jar /web/
COPY target/DB-Master-0.0.1-SNAPSHOT.jar /web/

# 暴露服务端口
EXPOSE 8000

# 设置启动命令
CMD ["java", "-jar", "DB-Master-0.0.1-SNAPSHOT.jar"]