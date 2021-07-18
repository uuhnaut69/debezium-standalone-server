package com.uuhnaut69.dbz.config;

import io.debezium.connector.mysql.MySqlConnector;
import io.debezium.relational.history.MemoryDatabaseHistory;
import io.debezium.util.Strings;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static io.debezium.embedded.EmbeddedEngine.*;

@Configuration
@EnableConfigurationProperties(ConnectorConfigurationProperties.class)
public class ConnectorConfig {

  private static final String DATABASE_SERVERNAME = "database.server.name";
  private static final String DATABASE_HOSTNAME = "database.hostname";
  private static final String DATABASE_NAME = "database.dbname";
  private static final String DATABASE_PORT = "database.port";
  private static final String DATABASE_USER = "database.user";
  private static final String DATABASE_PASSWORD = "database.password";
  private static final String SNAPSHOT_MODE = "snapshot.mode";
  private static final String PLUGIN_NAME = "plugin.name";
  private static final String SLOT_NAME = "slot.name";
  private static final String SCHEMA_INCLUDE_LIST = "schema.include.list";
  private static final String TABLE_INCLUDE_LIST = "table.include.list";
  private static final String DATABASE_HISTORY = "database.history";

  @Bean
  public io.debezium.config.Configuration connectorConfiguration(
      ConnectorConfigurationProperties connectorConfigurationProperties) {
    Properties props = new Properties();
    props.setProperty(ENGINE_NAME.toString(), connectorConfigurationProperties.getEngineName());
    props.setProperty(CONNECTOR_CLASS.toString(), connectorConfigurationProperties.getConnectorClass());
    props.setProperty(DATABASE_SERVERNAME, connectorConfigurationProperties.getDatabaseServerName());
    props.setProperty(DATABASE_HOSTNAME, connectorConfigurationProperties.getDatabaseHostname());
    props.setProperty(DATABASE_NAME, connectorConfigurationProperties.getDatabaseName());
    props.setProperty(DATABASE_PORT, connectorConfigurationProperties.getDatabasePort().toString());
    props.setProperty(DATABASE_USER, connectorConfigurationProperties.getDatabaseUser());
    props.setProperty(DATABASE_PASSWORD, connectorConfigurationProperties.getDatabasePassword());
    props.setProperty(OFFSET_STORAGE.toString(), MemoryOffsetBackingStore.class.getName());

    if (!Strings.isNullOrEmpty(connectorConfigurationProperties.getSnapshotMode())) {
      props.setProperty(SNAPSHOT_MODE, connectorConfigurationProperties.getSnapshotMode());
    }

    if (!Strings.isNullOrEmpty(connectorConfigurationProperties.getPluginName())) {
      props.setProperty(PLUGIN_NAME, connectorConfigurationProperties.getPluginName());
    }

    if (!Strings.isNullOrEmpty(connectorConfigurationProperties.getSlotName())) {
      props.setProperty(SLOT_NAME, connectorConfigurationProperties.getSlotName());
    }

    if (!Strings.isNullOrEmpty(connectorConfigurationProperties.getSchemaIncludeList())) {
      props.setProperty(SCHEMA_INCLUDE_LIST, connectorConfigurationProperties.getSchemaIncludeList());
    }

    if (!Strings.isNullOrEmpty(connectorConfigurationProperties.getTableIncludeList())) {
      props.setProperty(TABLE_INCLUDE_LIST, connectorConfigurationProperties.getTableIncludeList());
    }

    if (connectorConfigurationProperties.getConnectorClass().equals(MySqlConnector.class.getName())) {
      props.setProperty(DATABASE_HISTORY, MemoryDatabaseHistory.class.getName());
    }
    return io.debezium.config.Configuration.from(props);
  }
}
