package com.uuhnaut69.dbz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Validated
@ConfigurationProperties(prefix = "debezium")
public class ConnectorProperties {

  @NotNull private String engineName;

  @NotNull private String connectorClass;

  @NotNull private String databaseHostname;

  @NotNull private String databaseName;

  @NotNull private String databasePort;

  @NotNull private String databaseUser;

  @NotNull private String databasePassword;

  @NotNull private String databaseServerName;

  @NotNull private String offsetStorageFile;

  @NotNull private String snapshotMode;

  private String pluginName;

  private String slotName;

  private String schemaIncludeList;

  private String tableIncludeList;
}
