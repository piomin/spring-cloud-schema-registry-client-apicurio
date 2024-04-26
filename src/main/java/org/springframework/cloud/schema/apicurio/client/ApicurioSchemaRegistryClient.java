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

package org.springframework.cloud.schema.apicurio.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.schema.apicurio.client.domain.Registration;
import org.springframework.cloud.stream.schema.registry.SchemaNotFoundException;
import org.springframework.cloud.stream.schema.registry.SchemaReference;
import org.springframework.cloud.stream.schema.registry.SchemaRegistrationResponse;
import org.springframework.cloud.stream.schema.registry.client.SchemaRegistryClient;
import org.springframework.cloud.stream.schema.registry.client.config.SchemaRegistryClientProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class ApicurioSchemaRegistryClient implements SchemaRegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(ApicurioSchemaRegistryClient.class);

    @Value("${spring.application.name:default}")
    private String applicationName;
    private RestTemplate restTemplate;
    private SchemaRegistryClientProperties properties;

    public ApicurioSchemaRegistryClient(RestTemplate restTemplate, SchemaRegistryClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public SchemaRegistrationResponse register(String subject, String format, String schema) {
        Registration registration = null;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; artifactType=" + format.toUpperCase());
        headers.add("X-Registry-ArtifactId", subject);
        try {
            registration = getArtifactMeta(subject);
        } catch (SchemaNotFoundException e) {
            LOG.debug("Schema not found: subject={}", subject);
        }
        if (registration == null) {
            try {
                LOG.debug("Adding new schema: subject={}", subject);
                HttpEntity<String> request = new HttpEntity<>(schema, headers);
                ResponseEntity<Registration> response = restTemplate.exchange(
                        properties.getEndpoint() + "/apis/registry/v2/groups/{groupId}/artifacts",
                        HttpMethod.POST, request, Registration.class, applicationName);
                registration = response.getBody();
                LOG.info("New artifact registered: globalId={}, groupId={}, artifactId={}", registration.getGlobalId(), applicationName, subject);
            } catch (HttpStatusCodeException httpException) {
                LOG.error("Registration failed", httpException);
                throw new RuntimeException(String.format("Failed to register subject %s, server replied with status %d",
                        subject, httpException.getStatusCode().value()), httpException);
            }
        } else {
            String existingSchema = fetch(registration.getContentId());
            if (!existingSchema.equals(schema)) {
                HttpEntity<String> request = new HttpEntity<>(schema);
                ResponseEntity<Registration> response = restTemplate.exchange(
                        properties.getEndpoint() + "/apis/registry/v2/groups/{groupId}/artifacts/{artifactId}",
                        HttpMethod.PUT, request, Registration.class, applicationName, subject);
                registration = response.getBody();
                LOG.info("New artifact version uploaded: globalId={}, groupId={}, artifactId={}, version={}",
                        registration.getGlobalId(), applicationName, subject, registration.getVersion());
            }
        }

        SchemaRegistrationResponse schemaRegistrationResponse = new SchemaRegistrationResponse();
        schemaRegistrationResponse.setId(registration.getGlobalId());
        schemaRegistrationResponse.setSchemaReference(new SchemaReference(subject, Integer.parseInt(registration.getVersion()), format));
        return schemaRegistrationResponse;
    }

    @Override
    public String fetch(SchemaReference schemaReference) {
        LOG.debug("Fetching schema by subject: subject={}", schemaReference.getSubject());
        Registration registration = getArtifactMeta(schemaReference.getSubject());
        Integer id = registration.getContentId();
        LOG.info("Registration object found: contentId={}", id);
        return restTemplate.getForObject(properties.getEndpoint() + "/apis/registry/v2/ids/contentIds/{id}",
                String.class, id);
    }

    @Override
    public String fetch(int id) {
        LOG.debug("Fetching schema by id: id={}", id);
        String schema = restTemplate.getForObject(properties.getEndpoint() + "/apis/registry/v2/ids/contentIds/{contentId}",
                String.class, id);
        if (schema == null)
            throw new SchemaNotFoundException(String.format("No schema found for id=%d", id));
        else
            return schema;
    }

    private Registration getArtifactMeta(String subject) {
        try {
            Registration registration = restTemplate.getForObject(properties.getEndpoint() + "/apis/registry/v2/groups/{groupId}/artifacts/{artifactId}/meta",
                    Registration.class, applicationName, subject);
            LOG.info("Registration object found: globalId={}, contentId={}",
                    registration.getGlobalId(), registration.getContentId());
            return registration;
        } catch (HttpClientErrorException e) {
            LOG.debug("Schema not found: groupId={}, artifactId={}", applicationName, subject);
            throw new SchemaNotFoundException(String.format("No schema found for groupId=%s, subject=%s", applicationName, subject));
        }
    }
}
