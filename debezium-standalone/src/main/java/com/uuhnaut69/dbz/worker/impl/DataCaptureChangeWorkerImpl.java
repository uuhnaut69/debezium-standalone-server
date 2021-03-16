package com.uuhnaut69.dbz.worker.impl;

import com.uuhnaut69.dbz.worker.DataCaptureChangeWorker;
import io.debezium.config.Configuration;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.debezium.data.Envelope.FieldName.*;
import static java.util.stream.Collectors.toMap;

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
                    handleEvent(sourceRecord);
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

  private void handleEvent(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
    var sourceRecordValue = (Struct) changeEvent.value().value();

    if (Objects.nonNull(sourceRecordValue)) {
      var op = Envelope.Operation.forCode((String) sourceRecordValue.get(OPERATION));

      if (op != Envelope.Operation.READ) {
        var record = AFTER; // For Update & Insert operations.
        if (op == Envelope.Operation.DELETE) {
          record = BEFORE; // For Delete operations.
        }
        var payload = this.getCDCEventAsMap(record, sourceRecordValue);
        log.info("Operation {} with payload {}", op, payload);
      }
    }
  }

  private Map<String, Object> getCDCEventAsMap(String record, Struct payload) {
    Struct messagePayload = (Struct) payload.get(record);
    return messagePayload.schema().fields().stream()
        .map(Field::name)
        .filter(fieldName -> messagePayload.get(fieldName) != null)
        .map(fieldName -> Pair.of(fieldName, messagePayload.get(fieldName)))
        .collect(toMap(Pair::getKey, Pair::getValue));
  }
}
