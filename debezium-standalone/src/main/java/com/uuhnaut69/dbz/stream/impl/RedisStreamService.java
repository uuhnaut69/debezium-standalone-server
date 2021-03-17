package com.uuhnaut69.dbz.stream.impl;

import com.uuhnaut69.dbz.stream.StreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStreamService implements StreamService {

  @Value(value = "${stream.endpoint}")
  private String endpoint;

  private final RedissonClient redissonClient;

  @Override
  public void publishEvent(Map<String, Object> cdcEvent) {
    log.info("Publish event {} to {} topic", cdcEvent, endpoint);
    RStream<String, Object> stream = redissonClient.getStream(endpoint);
    stream.add(UUID.randomUUID().toString(), cdcEvent);
  }
}
