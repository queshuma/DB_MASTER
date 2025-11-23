# 指定基础镜像
FROM registry.cn-hangzhou.aliyuncs.com/library/openjdk:17

# 设置工作目录
WORKDIR /web

# 复制你的jar包到镜像中的工作目录
COPY DB-Master-0.0.1-SNAPSHOT.jar /web/

# 暴露服务端口
EXPOSE 8000

# 设置启动命令
CMD ["java", "-jar", "DB-Master-0.0.1-SNAPSHOT.jar"]