package org.springframework.cloud.schema.apicurio.client.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.schema.apicurio.client.ApicurioSchemaRegistryClient;
import org.springframework.cloud.stream.schema.registry.client.config.SchemaRegistryClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@EnableConfigurationProperties({ SchemaRegistryClientProperties.class })
@ConditionalOnProperty(value = "spring.cloud.stream.schema.registry.type", havingValue = "apicurio")
public class SchemaRegistryClientApicurioAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RestClient restClient() {
		return RestClient.create();
	}

	@Bean
	@ConditionalOnMissingBean
	public ApicurioSchemaRegistryClient apicurioSchemaRegistryClient(RestClient restClient, SchemaRegistryClientProperties properties) {
		return new ApicurioSchemaRegistryClient(restClient, properties);
	}

}
