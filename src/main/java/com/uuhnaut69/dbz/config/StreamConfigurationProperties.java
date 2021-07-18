package com.uuhnaut69.dbz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ConfigurationProperties(prefix = "stream")
public class StreamConfigurationProperties {

  /** Topic destination */
  @NotBlank private String topic;
}
