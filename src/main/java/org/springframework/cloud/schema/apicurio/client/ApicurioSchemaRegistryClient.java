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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

public class ApicurioSchemaRegistryClient implements SchemaRegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(ApicurioSchemaRegistryClient.class);

    @Value("${spring.application.name:default}")
    private String applicationName;
    private RestClient restClient;
    private SchemaRegistryClientProperties properties;

    public ApicurioSchemaRegistryClient(RestClient restClient, SchemaRegistryClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public SchemaRegistrationResponse register(String subject, String format, String schema) {
        Registration registration = null;
        try {
            registration = getArtifactMeta(subject);
        } catch (SchemaNotFoundException e) {
            LOG.debug("Schema not found: subject={}", subject);
        }
        if (registration == null) {
            try {
                LOG.debug("Adding new schema: subject={}", subject);
                registration = restClient.post()
                        .uri(properties.getEndpoint() + "/apis/registry/v2/groups/{groupId}/artifacts", applicationName)
                        .header("Content-Type", "application/json; artifactType=" + format.toUpperCase())
                        .header("X-Registry-ArtifactId", subject)
                        .body(schema)
                        .retrieve()
                        .body(Registration.class);
                LOG.info("New artifact registered: globalId={}, groupId={}, artifactId={}", registration.getGlobalId(), applicationName, subject);
            } catch (HttpStatusCodeException httpException) {
                LOG.error("Registration failed", httpException);
                throw new RuntimeException(String.format("Failed to register subject %s, server replied with status %d",
                        subject, httpException.getStatusCode().value()), httpException);
            }
        } else {
            String existingSchema = fetch(registration.getContentId());
            if (!existingSchema.equals(schema)) {
                registration = restClient.put()
                        .uri(properties.getEndpoint() + "/apis/registry/v2/groups/{groupId}/artifacts/{artifactId}", applicationName, subject)
                        .body(schema)
                        .retrieve()
                        .body(Registration.class);
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
        return restClient.get()
                .uri(properties.getEndpoint() + "/apis/registry/v2/ids/contentIds/{id}", id)
                .retrieve()
                .body(String.class);
    }

    @Override
    public String fetch(int id) {
        LOG.debug("Fetching schema by id: id={}", id);
        String schema = restClient.get()
                .uri(properties.getEndpoint() + "/apis/registry/v2/ids/contentIds/{contentId}", id)
                .retrieve()
                .body(String.class);
        if (schema == null)
            throw new SchemaNotFoundException(String.format("No schema found for id=%d", id));
        else
            return schema;
    }

    private Registration getArtifactMeta(String subject) {
        try {
            Registration registration = restClient.get()
                    .uri(properties.getEndpoint() + "/apis/registry/v2/groups/{groupId}/artifacts/{artifactId}/meta", applicationName, subject)
                    .retrieve()
                    .body(Registration.class);
            LOG.info("Registration object found: globalId={}, contentId={}",
                    registration.getGlobalId(), registration.getContentId());
            return registration;
        } catch (HttpClientErrorException e) {
            LOG.debug("Schema not found: groupId={}, artifactId={}", applicationName, subject);
            throw new SchemaNotFoundException(String.format("No schema found for groupId=%s, subject=%s", applicationName, subject));
        }
    }
}
