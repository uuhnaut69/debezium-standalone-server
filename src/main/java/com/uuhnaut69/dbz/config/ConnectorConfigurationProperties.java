package com.uuhnaut69.dbz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ConfigurationProperties(prefix = "debezium")
public class ConnectorConfigurationProperties {

  /**
   * Unique name for the connector. Attempting to register again with the same name will fail. (This
   * property is required by all Kafka Connect connectors.)
   */
  @NotEmpty private String engineName;

  /** The name of the Java class for the connector. Always use a value of the connector. */
  @NotEmpty private String connectorClass;

  /** IP address or hostname of the database server. */
  @NotEmpty private String databaseHostname;

  /** The name of the database from which to stream the changes */
  @NotEmpty private String databaseName;

  /** Integer port number of the database server. */
  @NotEmpty private Integer databasePort;

  /** Username to use when connecting to the database server. */
  @NotEmpty private String databaseUser;

  /** Password to use when connecting to the database server. */
  @NotEmpty private String databasePassword;

  /**
   * Logical name that identifies and provides a namespace for the database server that you want
   * Debezium to capture. The logical name should be unique across all other connectors, since it is
   * used as a prefix for all Kafka topic names emanating from this connector. Only alphanumeric
   * characters and underscores should be used.
   */
  @NotEmpty private String databaseServerName;

  /** Snapshot mode */
  private String snapshotMode;

  private String pluginName;

  private String slotName;

  private String schemaIncludeList;

  private String tableIncludeList;
}
