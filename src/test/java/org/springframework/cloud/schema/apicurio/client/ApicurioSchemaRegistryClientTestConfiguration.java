package org.springframework.cloud.schema.apicurio.client;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.schema.registry.client.config.SchemaRegistryClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class ApicurioSchemaRegistryClientTestConfiguration {

    @Bean
    ApicurioSchemaRegistryClient client(RestClient restClient, SchemaRegistryClientProperties properties) {
        return new ApicurioSchemaRegistryClient(restClient, properties);
    }

}
