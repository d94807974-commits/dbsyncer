# Basic mirror
FROM crpi-hd9bjdgoqqg4ils8.cn-hangzhou.personal.cr.aliyuncs.com/xhtb-dbs/java:8

# Maintainer Information
LABEL authors="chuanyun"

# Define time zone parameters
ENV TZ=Asia/Shanghai
# Limit the number of glibc distribution areas and reduce the "JVM recycling normal but Docker RSS continues to grow" (extra/nature memory not returned to OS)
ENV MALLOC_ARENA_MAX=2

# Set Timezone
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Pass version number parameters
ARG REALTIME_ETL_VERSION
# Set the environment variable to ensure readability while running
ENV REALTIME_ETL_VERSION=${REALTIME_ETL_VERSION}

# Set Task Directory
WORKDIR /app

# Copy compression package
COPY target/realtime-etl-$REALTIME_ETL_VERSION-bin.zip .

# Depress, clean and rename directories
RUN unzip realtime-etl-$REALTIME_ETL_VERSION-bin.zip && \
    rm -rf realtime-etl-$REALTIME_ETL_VERSION-bin.zip && \
    mv realtime-etl-$REALTIME_ETL_VERSION realtime-etl && \
    # Enter the application directory for initialization
    cd realtime-etl && \
    # Add execution permissions
    chmod +x bin/startup-docker.sh && \
    # Creates the necessary directory (although VOLUME will mount, it is recommended to create for the integrity of privileges and structures)
    mkdir -p logs conf data plugins

# Exposure Port
EXPOSE 18686

# Mounted Volume (Note: If these directories are mounted by the host, the original files in the container will be hidden)
VOLUME [ "/app/realtime-etl/conf", "/app/realtime-etl/data", "/app/realtime-etl/logs", "/app/realtime-etl/plugins" ]

# Set the work directory to apply root directory
WORKDIR /app/realtime-etl

# Start Command
# Execute scripts in exec format to ensure that scripts are PID 1
ENTRYPOINT ["/bin/bash", "-c", "/app/realtime-etl/bin/startup-docker.sh"]