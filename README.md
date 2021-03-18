# Debezium Standalone Server

Implement Debezium Standalone Server without Apache Kafka, using Redis as infrastructure (Redis Stream) to push CDC
event.

In this example includes:

- Standalone Con

- Prebuild docker-compose to demo multi-worker (Using atomic setnx of Redis to deduplicate cdc event).

## Prerequisites

- Java 11 +
- Docker
- Docker-compose
- Redisinsight (Redis GUI)

## Run multi-worker demo

This multi-worker demo using Postgres.

Create two file offset storage and place their directory into demo.yml

```dockerfile
#File offset storage of worker1
./offset/demo.dat:/offset/demo.dat 
#File offset storage of worker2
/offset/demo2.dat:/offset/demo2.dat
```

Start demo

```shell
docker-compose -f demo.yml up -d
```

Connect to Postgres and create some data

```postgresql
create table if not exists public.customers
(
    id        serial primary key,
    full_name TEXT           NOT NULL,
    balance   NUMERIC(19, 2) NOT NULL
);
```

```postgresql
insert into public.customers(full_name, balance) values ('Mark', 1000);
```

Go to Redisinsight browser tab -> Connect to Redis -> Redis Stream to check.
