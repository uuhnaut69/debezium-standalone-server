package com.uuhnaut69.dbz.runner;

import com.uuhnaut69.dbz.worker.CaptureDataChangeWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCdcWorker implements ApplicationRunner {

  private final CaptureDataChangeWorker worker;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("Start data capture change worker...");
    worker.startCdcWorker();
  }
}
