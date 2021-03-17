package com.uuhnaut69.dbz.worker.impl;

import com.uuhnaut69.dbz.exception.CDCException;
import com.uuhnaut69.dbz.stream.StreamService;
import com.uuhnaut69.dbz.worker.CaptureDataChangeWorker;
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.debezium.data.Envelope.FieldName.*;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaptureDataChangeWorkerImpl implements CaptureDataChangeWorker {

  private DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> engine;

  private final Configuration connectorConfiguration;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final StreamService streamService;

  @Override
  @PostConstruct
  public void startCdcWorker() {
    this.engine =
        DebeziumEngine.create(Connect.class)
            .using(connectorConfiguration.asProperties())
            .notifying(
                sourceRecord -> {
                  try {
                    handleEvent(sourceRecord);
                  } catch (Exception e) {
                    throw new CDCException(e.getMessage());
                  }
                })
            .build();
    this.executor.execute(engine);
  }

  @Override
  @PreDestroy
  public void stopCdcWorker() {
    if (Objects.nonNull(engine)) {
      try {
        this.engine.close();
      } catch (IOException e) {
        throw new CDCException(e.getMessage());
      }
    }
  }

  private void handleEvent(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
    var sourceRecordValue = (Struct) changeEvent.value().value();
    if (Objects.nonNull(sourceRecordValue)) {
      var op = Envelope.Operation.forCode((String) sourceRecordValue.get(OPERATION));
      if (op != Envelope.Operation.READ) {
        var record = AFTER;

        if (op == Envelope.Operation.DELETE) {
          record = BEFORE;
        }
        var payload = getCDCEventAsMap(record, sourceRecordValue);

        var cdcEvent = new HashMap<String, Object>();
        cdcEvent.put("op", op);
        cdcEvent.put("payload", payload);

        var dbInfo = (Struct) sourceRecordValue.get(SOURCE);
        cdcEvent.put("db", dbInfo.getString("db"));
        cdcEvent.put("table", dbInfo.getString("table"));
        streamService.publishEvent(cdcEvent);
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
