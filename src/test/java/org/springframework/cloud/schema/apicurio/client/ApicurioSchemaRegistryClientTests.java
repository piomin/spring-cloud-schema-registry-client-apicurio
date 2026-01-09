package org.springframework.cloud.schema.apicurio.client;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.schema.apicurio.client.config.SchemaRegistryClientApicurioAutoConfiguration;
import org.springframework.cloud.stream.schema.registry.SchemaReference;
import org.springframework.cloud.stream.schema.registry.SchemaRegistrationResponse;
import org.springframework.cloud.stream.schema.registry.client.config.SchemaRegistryClientProperties;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = {
        ApicurioSchemaRegistryClientTestConfiguration.class,
        SchemaRegistryClientApicurioAutoConfiguration.class,
        SchemaRegistryClientProperties.class
})
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApicurioSchemaRegistryClientTests {

    String schema = """
        {
          "type":"record",
          "name":"CallmeEvent",
          "namespace":"pl.piomin.samples.eventdriven.producer.message.avro",
          "fields": [
            {
              "name":"id",
              "type":"int"
            },{
              "name":"message",
              "type":"string"
            },{
              "name":"eventType",
              "type": "string"
            }
          ]
        }
        """;

    @Container
    static final GenericContainer apicurio = new GenericContainer("apicurio/apicurio-registry:3.1.6")
            .withExposedPorts(8080);


    private static int id;
    private static SchemaReference schemaReference;

    @Autowired
    ApicurioSchemaRegistryClient client;
    @Autowired
    SchemaRegistryClientProperties properties;

    @Test
    @Order(1)
    void register() {
        properties.setEndpoint("http://" + apicurio.getHost() + ":" + apicurio.getFirstMappedPort());
        SchemaRegistrationResponse response = client.register("callme-event", "avro", schema);
        Assertions.assertNotNull(response);
        Assertions.assertNotEquals(0, response.getId());
        id = response.getId();
        schemaReference = response.getSchemaReference();
    }

    @Test
    @Order(2)
    void fetchById() {
        properties.setEndpoint("http://" + apicurio.getHost() + ":" + apicurio.getFirstMappedPort());
        String schemaRet = client.fetch(id);
        Assertions.assertNotNull(schemaRet);
        Assertions.assertEquals(schema, schemaRet);
    }

    @Test
    @Order(3)
    void fetchReference() {
        properties.setEndpoint("http://" + apicurio.getHost() + ":" + apicurio.getFirstMappedPort());
        String schemaRet = client.fetch(schemaReference);
        Assertions.assertNotNull(schemaRet);
        Assertions.assertEquals(schema, schemaRet);
    }
}
