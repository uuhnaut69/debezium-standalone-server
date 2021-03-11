package com.uuhnaut69.dbz.worker.impl;

import com.uuhnaut69.dbz.worker.DataCaptureChangeWorker;
import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataCaptureChangeWorkerImpl implements DataCaptureChangeWorker {

  private DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> engine;

  private final Configuration connectorConfiguration;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @Override
  public void startCdcWorker() {
    this.engine =
        DebeziumEngine.create(Connect.class)
            .using(connectorConfiguration.asProperties())
            .notifying(
                sourceRecord -> {
                  try {
                    log.info("{}", sourceRecord);
                  } catch (Exception e) {
                    log.error(e.getMessage());
                  }
                })
            .build();
    this.executor.execute(engine);
  }

  @Override
  public void stopCdcWorker() {
    if (Objects.nonNull(engine)) {
      try {
        this.engine.close();
      } catch (IOException e) {
        log.error("{}", e.getMessage());
      }
    }
  }
}
