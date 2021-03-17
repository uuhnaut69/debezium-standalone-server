package com.uuhnaut69.dbz.stream;

import java.util.Map;

public interface StreamService {

  void publishEvent(Map<String, Object> cdcEvent);
}
