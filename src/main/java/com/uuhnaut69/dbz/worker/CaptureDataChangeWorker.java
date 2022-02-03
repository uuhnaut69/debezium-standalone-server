package com.uuhnaut69.dbz.worker;

import static io.debezium.data.Envelope.FieldName.AFTER;
import static io.debezium.data.Envelope.FieldName.BEFORE;
import static io.debezium.data.Envelope.FieldName.OPERATION;
import static io.debezium.data.Envelope.FieldName.SOURCE;
import static java.util.stream.Collectors.toMap;

import com.uuhnaut69.dbz.exception.CDCException;
import com.uuhnaut69.dbz.stream.StreamService;
import io.debezium.config.Configuration;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaptureDataChangeWorker {

  private DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> engine;

  private final StreamService streamService;

  private final Configuration connectorConfiguration;

  @PostConstruct
  public void startCdcWorker() {
    log.info("Start cdc worker ...");
    this.engine =
        DebeziumEngine.create(Connect.class)
            .using(connectorConfiguration.asProperties())
            .notifying(
                sourceRecord -> {
                  try {
                    processEvent(sourceRecord);
                  } catch (Exception e) {
                    throw new CDCException(e.getMessage());
                  }
                })
            .build();
    var executor = Executors.newSingleThreadExecutor();
    executor.execute(engine);
  }

  @PreDestroy
  public void stopCdcWorker() {
    if (Objects.nonNull(engine)) {
      try {
        this.engine.close();
      } catch (IOException e) {
        log.error(e.getMessage());
        throw new CDCException(e.getMessage());
      } finally {
        log.info("Stop cdc worker ...");
      }
    }
  }

  private void processEvent(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
    var sourceRecordValue = (Struct) changeEvent.value().value();
    if (Objects.nonNull(sourceRecordValue)) {
      var op = Envelope.Operation.forCode((String) sourceRecordValue.get(OPERATION));
      if (!op.equals(Envelope.Operation.READ)) {
        var record = AFTER;

        if (op.equals(Envelope.Operation.DELETE)) {
          record = BEFORE;
        }
        var payload = getCDCEventAsMap(record, sourceRecordValue);

        var cdcEvent = new HashMap<String, Object>();
        cdcEvent.put("op", op);
        cdcEvent.put("payload", payload);

        var dbInfo = (Struct) sourceRecordValue.get(SOURCE);
        cdcEvent.put("db", dbInfo.getString("db"));
        cdcEvent.put("table", dbInfo.getString("table"));
        cdcEvent.put("ts_ms", dbInfo.getInt64("ts_ms"));
        streamService.publishEvent(cdcEvent);
      }
    }
  }

  private Map<String, Object> getCDCEventAsMap(String record, Struct payload) {
    var messagePayload = (Struct) payload.get(record);
    return messagePayload.schema().fields().stream()
        .map(Field::name)
        .filter(fieldName -> messagePayload.get(fieldName) != null)
        .map(fieldName -> Pair.of(fieldName, messagePayload.get(fieldName)))
        .collect(toMap(Pair::getFirst, Pair::getSecond));
  }
}
