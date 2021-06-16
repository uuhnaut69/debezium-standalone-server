# Debezium Standalone Server

![Maven Central](https://img.shields.io/maven-central/v/org.springframework.boot/spring-boot-starter-parent?color=green&label=spring-boot&logo=spring-boot&logoColor=green&style=for-the-badge)
![Maven Central](https://img.shields.io/maven-central/v/io.debezium/debezium-api?color=green&label=debezium&style=for-the-badge)

Implement Debezium Standalone Server without Apache Kafka, using Redis as infrastructure (Redis Stream) to push CDC
event.

In this example includes:

- Standalone worker

- Prebuild docker-compose to demo multi-worker (Using atomic SetNX of Redis to deduplicate cdc event).

## Prerequisites

- `Java 11+`
- `Docker`
- `Docker-compose`
- `Redis Insight (Redis GUI)`

## Run multi-worker demo

This multi-worker demo using Postgres.

Start demo

```shell
docker-compose -f demo.yml up -d
```

Connect to Postgres and create some data

```sql
create table customers
(
    id   serial primary key not null,
    name varchar(255)       not null
);
```

Create 2 customers
```sql
insert into customers (name)
values ('First customer'),
       ('Second customer');
```

Go to Redisinsight browser tab -> Connect to Redis -> Redis Stream to check.
