# Apicurio client for Spring Cloud Schema Registry
The aim of this project is to add support for [Apicurio Registry](https://www.apicur.io/registry/) in [Spring Cloud Schema Registry](https://spring.io/projects/spring-cloud-schema-registry). \
Currently Spring cloud Schema Registry supports Confluent Schema Registry and Spring Schema Registry Server (by default). You may read more about Spring Cloud Schema Registry in their [documentation](https://docs.spring.io/spring-cloud-schema-registry/docs/1.1.3-SNAPSHOT/reference/html/spring-cloud-schema-registry.html). \
Then you can the library with event-driven communication with Spring Cloud Stream and one of the message brokers supported by Spring Cloud Stream, e.g. Kafka or RabbitMQ.

## Usage
You just need to include the following dependency to your project.
```xml
<dependency>
    <groupId>com.github.piomin</groupId>
    <artifactId>spring-cloud-schema-registry-client-apicurio</artifactId>
    <version>1.0</version>
</dependency>
```
Before that add Jitpack to your build file to support loading artifacts directly from GitHub.
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## Configuration
The default address of schema registry server is http://localhost:8081. You can override it using the following configuration property.
```properties
spring.cloud.schemaRegistryClient.endpoint=http://example-apicurioregistry-service:8080/
```