# Realtime ETL

Realtime ETL is an open-source **real-time data processing / ETL** service. It covers sync scenarios for MySQL, Oracle, SQL Server, PostgreSQL, Elasticsearch (ES), Kafka, File, SQL, and more. You can upload plugins to customize transform logic, and monitor full/incremental stats with application performance alerts.

## Features

* Flexible drivers — combine relational and non-relational sources/targets with arbitrary table mappings
* Real-time monitoring — full or incremental sync status, results, sync logs, and system logs
* Plugin SDK — customize transform and sync logic

## Build

```bash
mvn -DskipTests package
```

Artifacts are under each module's `target/` directory. The runnable distribution is produced by the `realtime-etl-web` module.

## Run

```bash
java -Dloader.path=lib -jar realtime-etl-web-*.jar
# or thin-classpath style:
# java -cp "conf:lib/*" org.realtimeetl.web.Application
```

Default UI: `http://localhost:18686` (admin / admin).

## Configuration

Application properties use the `realtime-etl.*` / `info.app.*` prefixes. Storage table names use the `realtime_etl_*` prefix.

## License

Apache License 2.0. See [LICENSE](./LICENSE).

Chinese overview: [README.zh-CN.md](./README.zh-CN.md).
