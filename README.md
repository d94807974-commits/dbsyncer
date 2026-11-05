# Realtime ETL

> **Derivative notice.** This project is derived from [86dbs/dbsyncer](https://github.com/86dbs/dbsyncer) (Apache License 2.0).
> Upstream copyright and authors are retained in source file headers and `LICENSE` / `NOTICE`.
> This fork renames packages and branding to **Realtime ETL** (`org.realtimeetl`, Maven `realtime-etl-*`) and removes third-party company branding from the UI.

## Introduction

![logo](https://gitee.com/ghi/realtime-etl/raw/master/realtime-etl-web/src/main/resources/static/img/logo.png)

[Realtime ETL](https://github.com/d94807974-commits/dbsyncer) is an open-source **real-time data processing / ETL** service. It is a derivative fork of [DBSyncer](https://github.com/86dbs/dbsyncer) (Apache-2.0). It covers sync scenarios for MySQL, Oracle, SQL Server, PostgreSQL, Elasticsearch (ES), Kafka, File, SQL, and more. You can upload plugins to customize transform logic, and monitor full/incremental stats with application performance alerts.

> Features

* Flexible drivers — combine relational and non-relational sources/targets with arbitrary table mappings
* Real-time monitoring — full or incremental sync status, results, sync logs, and system logs
* Plugin SDK — customize transform and sync logic

> Upstream repositories

* [Gitee](https://gitee.com/ghi/realtime-etl)
* [GitHub](https://github.com/86dbs/dbsyncer)

[![star](https://gitee.com/ghi/realtime-etl/badge/star.svg?theme=dark)](https://gitee.com/ghi/realtime-etl/stargazers)
[![fork](https://gitee.com/ghi/realtime-etl/badge/fork.svg?theme=dark)](https://gitee.com/ghi/realtime-etl/members)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://gitee.com/ghi/realtime-etl/blob/master/LICENSE)

> Website

https://dbsyncer.cn

> This fork

* English UI / messages / logs branch: `i18n-english`
* Chinese README: [README.zh-CN.md](./README.zh-CN.md)
* Upstream: https://github.com/86dbs/dbsyncer

## Application scenarios

| Connector       | Source | Target | Supported versions                          |
|-----------------|--------|--------|---------------------------------------------|
| ClickHouse      | ✔      | ✔      | 22.x+                                       |
| Dameng          | ✔      | ✔      | DM8                                         |
| Doris           | ✔      | ✔      | 1.2.x+                                      |
| DuckDB          | ✔      | ✔      | 1.5.x                                       |
| Elasticsearch   | ✔      | ✔      | 6.0.0–8.15.3                                |
| File            | ✔      | ✔      | *.txt, *.unl                                |
| H2              | ✔      | ✔      | 2.2.x                                       |
| Http            | ✔      | ✔      | HTTP/1.1                                    |
| Kafka           | ✔      | ✔      | 2.10-0.9.0.0+                               |
| Kingbase        | ✔      | ✔      | V8                                          |
| MongoDB         | ✔      | ✔      | 4.2+                                        |
| MySQL           | ✔      | ✔      | 5.7.19+                                     |
| OceanBase       | ✔      | ✔      | 3.x+                                        |
| OceanBaseOracle | ✔      | ✔      | 3.x+                                        |
| Oracle          | ✔      | ✔      | 10g–19c                                     |
| PostgreSQL      | ✔      | ✔      | 9.5.25+                                     |
| Redis           | ✔      | ✔      | Source (incremental CDC) 5.x–7.x; target 2.6+ |
| RocketMQ        | ✔      | ✔      | 4.9.x+                                      |
| Sqlite          | ✔      | ✔      | 2+                                          |
| SqlServer       | ✔      | ✔      | 2008+                                       |
| StarRocks       | ✔      | ✔      | 2.5+                                        |
| TiDB            | ✔      | ✔      | 5.x+                                        |
| SQL             | ✔      |        | Relational databases listed above           |

## Preview

![Dashboard](https://foruda.gitee.com/images/1769953561917318680/775e6928_376718.png "Dashboard")

![Monitor](https://foruda.gitee.com/images/1769953915949399025/fc16d35b_376718.png "Monitor")

## User manual (Wiki)

https://gitee.com/ghi/realtime-etl/wikis

## Install

- realtime-etl-x.x.x.zip — [Community install guide](https://gitee.com/ghi/realtime-etl/wikis)
- realtime-etl-enterprise-x.x.x.zip — [Professional install guide](https://gitee.com/ghi/realtime-etl/wikis)

#### Option 1 — Download package

1. Install [JDK 1.8+](https://www.oracle.com/java/technologies/jdk8-downloads.html)
2. Download [realtime-etl-x.x.x.zip](https://gitee.com/ghi/realtime-etl/releases) (or build from source)
3. Unzip; on Windows run `bin/startup.bat`, on Linux run `bin/startup.sh`
4. Open http://127.0.0.1:18686
5. Default account: `admin` / `admin`

#### Option 2 — Docker

```shell
# Community
docker pull images.dbsyncer.cn/xhtb-dbs/realtime-etl:latest
docker pull images.dbsyncer.cn/xhtb-dbs/realtime-etl:2.1.5
# Professional
docker pull images.dbsyncer.cn/xhtb-dbs/realtime-etl-enterprise:latest
docker pull images.dbsyncer.cn/xhtb-dbs/realtime-etl-enterprise:2.1.5
```

```shell
docker run -d \
  --name=realtime-etl \
  --restart=unless-stopped \
  -p 18686:18686 \
  -e TZ="Asia/Shanghai" \
  -m 5g \
  --memory-swap=5g \
  -v /opt/realtime-etl/data:/app/realtime-etl/data \
  -v /opt/realtime-etl/logs:/app/realtime-etl/logs \
  -v /opt/realtime-etl/plugins:/app/realtime-etl/plugins \
  --log-driver json-file \
  --log-opt max-size=100m \
  --log-opt max-file=7 \
  images.dbsyncer.cn/xhtb-dbs/realtime-etl:latest
```

## Build from source

Requires JDK and Maven:

```bash
git clone https://github.com/d94807974-commits/dbsyncer.git
cd realtime-etl
git checkout i18n-english
chmod u+x build.sh
./build.sh
```

Or:

```bash
mvn -DskipTests package
```

## Performance tests

See upstream wiki: https://gitee.com/ghi/realtime-etl/wikis

#### Full sync

| OS      | Hardware                                      | Rows      | Duration   |
|---------|-----------------------------------------------|-----------|------------|
| Mac     | Apple M3 Pro 12-core, 18GB RAM                | 100M      | 31m 50s    |
| Linux   | Intel Xeon E5-2696 v3B 8-core, 48GB RAM       | 100M      | 37m 52s    |
| Windows | AMD Ryzen 7 5800x 8-core, 12GB RAM            | 100M      | 57m 43s    |

#### Incremental sync

| OS      | Hardware                                      | Heap | TPS     | Peak     |
|---------|-----------------------------------------------|------|---------|----------|
| Mac     | Apple M3 Pro 12-core, 18GB RAM                | 4GB  | 8112/s  | 11000/s  |
| Linux   | Intel Xeon E5-2696 v3B 8-core, 48GB RAM       | 4GB  | 8000/s  | 10000/s  |
| Windows | AMD Ryzen 7 5800x 8-core, 12GB RAM            | 4GB  | 7553/s  | 9000/s   |

## FAQ

* MySQL connection issues: default driver is 8.0.21; for MySQL 5.x replace with [mysql-connector-java-5.1.40.jar](https://gitee.com/ghi/realtime-etl/attach_files)
* SQL Server SSL/TLS issues: see upstream issues
* Garbled sync data / remote debug: see upstream wiki FAQ

## Credits

* Upstream project and authors: [86dbs/dbsyncer](https://github.com/86dbs/dbsyncer) (Apache-2.0)
* Original Gitee project: https://gitee.com/ghi/realtime-etl
* This English localization is a community fork contribution and is not an official upstream release

## License

Apache License 2.0 — see [LICENSE](./LICENSE)
