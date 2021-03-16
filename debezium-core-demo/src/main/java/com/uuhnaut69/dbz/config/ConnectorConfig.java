package com.uuhnaut69.dbz.config;

import io.debezium.connector.mysql.MySqlConnector;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import io.debezium.util.Strings;
import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static io.debezium.embedded.EmbeddedEngine.*;

@Configuration
@EnableConfigurationProperties(ConnectorProperties.class)
public class ConnectorConfig {

  @Bean
  public io.debezium.config.Configuration connectorConfiguration(
      ConnectorProperties connectorProperties) {
    Properties props = new Properties();
    props.setProperty(ENGINE_NAME.toString(), connectorProperties.getEngineName());
    props.setProperty(CONNECTOR_CLASS.toString(), connectorProperties.getConnectorClass());
    props.setProperty("database.server.name", connectorProperties.getDatabaseServerName());
    props.setProperty("database.hostname", connectorProperties.getDatabaseHostname());
    props.setProperty("database.dbname", connectorProperties.getDatabaseName());
    props.setProperty("database.port", connectorProperties.getDatabasePort().toString());
    props.setProperty("database.user", connectorProperties.getDatabaseUser());
    props.setProperty("database.password", connectorProperties.getDatabasePassword());
    props.setProperty(OFFSET_STORAGE.toString(), FileOffsetBackingStore.class.getName());
    props.setProperty(
        OFFSET_STORAGE_FILE_FILENAME.toString(), connectorProperties.getOffsetStorageFile());

    if (!Strings.isNullOrEmpty(connectorProperties.getSnapshotMode())) {
      props.setProperty("snapshot.mode", connectorProperties.getSnapshotMode());
    }

    if (!Strings.isNullOrEmpty(connectorProperties.getPluginName())) {
      props.setProperty("plugin.name", connectorProperties.getPluginName());
    }

    if (!Strings.isNullOrEmpty(connectorProperties.getSlotName())) {
      props.setProperty("slot.name", connectorProperties.getSlotName());
    }

    if (!Strings.isNullOrEmpty(connectorProperties.getSchemaIncludeList())) {
      props.setProperty("schema.include.list", connectorProperties.getSchemaIncludeList());
    }

    if (!Strings.isNullOrEmpty(connectorProperties.getTableIncludeList())) {
      props.setProperty("table.include.list", connectorProperties.getTableIncludeList());
    }

    if (connectorProperties.getConnectorClass().equals(MySqlConnector.class.getName())) {
      props.setProperty(
          RelationalDatabaseConnectorConfig.INCLUDE_SCHEMA_CHANGES.toString(), "false");
      props.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
      props.setProperty(
          "database.history.file.filename", connectorProperties.getDatabaseHistoryStorageFile());
    }
    return io.debezium.config.Configuration.from(props);
  }
}
