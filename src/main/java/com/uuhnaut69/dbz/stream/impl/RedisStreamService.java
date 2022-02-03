package com.uuhnaut69.dbz.stream.impl;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.uuhnaut69.dbz.config.StreamConfigurationProperties;
import com.uuhnaut69.dbz.stream.StreamService;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.client.codec.StringCodec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(StreamConfigurationProperties.class)
public class RedisStreamService implements StreamService {

  private final RedissonClient redissonClient;

  private final StreamConfigurationProperties properties;

  @Override
  public void publishEvent(Map<String, Object> cdcEvent) {
    var cdcEventJsonString = new Gson().toJson(cdcEvent);
    var sha256Hash = Hashing.sha256().hashString(cdcEventJsonString, StandardCharsets.UTF_8).toString();
    var bucket = redissonClient.getBucket(sha256Hash);

    // Deduplicate event, keep event in redis 24 hours
    if (bucket.trySet(cdcEvent, 24, TimeUnit.HOURS)) {
      log.debug("Publish event {} to {} topic", cdcEvent, properties.getTopic());
      RStream<String, Object> stream =
          redissonClient.getStream(properties.getTopic(), new StringCodec());
      stream.add(StreamAddArgs.entries(cdcEvent));
    }
  }
}
