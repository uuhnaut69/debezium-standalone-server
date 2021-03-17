package com.uuhnaut69.dbz.rest;

import com.uuhnaut69.dbz.worker.CaptureDataChangeWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorkResource {

  private final CaptureDataChangeWorker worker;

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public void startCdcWorker() {
    worker.startCdcWorker();
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.OK)
  public void stopCdcWorker() {
    worker.stopCdcWorker();
  }
}
