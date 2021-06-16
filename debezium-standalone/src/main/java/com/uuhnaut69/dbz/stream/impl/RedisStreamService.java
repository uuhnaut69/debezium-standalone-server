package com.uuhnaut69.dbz.stream.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.uuhnaut69.dbz.stream.StreamService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStreamService implements StreamService {

  @Value(value = "${stream.endpoint}")
  private String endpoint;

  private final ObjectMapper objectMapper;

  private final RedissonClient redissonClient;

  @Override
  @SneakyThrows
  public void publishEvent(Map<String, Object> cdcEvent) {
    var cdcEventJsonString = objectMapper.writeValueAsString(cdcEvent);
    var sha256Hash =
        Hashing.sha256().hashString(cdcEventJsonString, StandardCharsets.UTF_8).toString();
    var bucket = redissonClient.getBucket(sha256Hash);

    if (bucket.trySet(cdcEvent)) {
      log.debug("Publish event {} to {} topic", cdcEvent, endpoint);
      RStream<String, Object> stream = redissonClient.getStream(endpoint, new StringCodec());
      stream.add(StreamAddArgs.entry(UUID.randomUUID().toString(), cdcEvent));
    }
  }
}
