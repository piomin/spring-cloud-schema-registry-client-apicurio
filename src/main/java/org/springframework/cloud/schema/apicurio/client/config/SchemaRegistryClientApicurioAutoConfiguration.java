/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
//@ConditionalOnClass(RestClient.class)
//@EnableConfigurationProperties({ SchemaRegistryClientProperties.class })
//@ConditionalOnProperty(value = "spring.cloud.stream.schema.registry.type", havingValue = "apicurio")
public class SchemaRegistryClientApicurioAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RestClient restClient() {
		return RestClient.create();
	}

//	@Bean
//	@ConditionalOnMissingBean
//	public ApicurioSchemaRegistryClient apicurioSchemaRegistryClient(RestClient restClient, SchemaRegistryClientProperties properties) {
//		return new ApicurioSchemaRegistryClient(restClient, properties);
//	}

}
