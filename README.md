# Apicurio client for Spring Cloud Schema Registry [![Twitter](https://img.shields.io/twitter/follow/piotr_minkowski.svg?style=social&logo=twitter&label=Follow%20Me)](https://twitter.com/piotr_minkowski)

![Maven Central Version](https://img.shields.io/maven-central/v/com.github.piomin/spring-cloud-schema-registry-client-apicurio)
[![CircleCI](https://circleci.com/gh/piomin/spring-cloud-schema-registry-client-apicurio.svg?style=svg)](https://circleci.com/gh/piomin/spring-cloud-schema-registry-client-apicurio)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=piomin_spring-cloud-schema-registry-client-apicurio)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=piomin_spring-cloud-schema-registry-client-apicurio&metric=bugs)](https://sonarcloud.io/dashboard?id=piomin_spring-cloud-schema-registry-client-apicurio)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=piomin_spring-cloud-schema-registry-client-apicurio&metric=coverage)](https://sonarcloud.io/dashboard?id=piomin_spring-cloud-schema-registry-client-apicurio)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=piomin_spring-cloud-schema-registry-client-apicurio&metric=ncloc)](https://sonarcloud.io/dashboard?id=piomin_spring-cloud-schema-registry-client-apicurio)

The aim of this project is to add support for [Apicurio Registry](https://www.apicur.io/registry/) in [Spring Cloud Schema Registry](https://spring.io/projects/spring-cloud-schema-registry). \
Currently Spring cloud Schema Registry supports Confluent Schema Registry and Spring Schema Registry Server (by default). You may read more about Spring Cloud Schema Registry in their [documentation](https://docs.spring.io/spring-cloud-schema-registry/docs/1.1.3-SNAPSHOT/reference/html/spring-cloud-schema-registry.html). \
Then you can the library with event-driven communication with Spring Cloud Stream and one of the message brokers supported by Spring Cloud Stream, e.g. Kafka or RabbitMQ.

## Usage
You just need to include the following dependency to your project.
```xml
<dependency>
    <groupId>com.github.piomin</groupId>
    <artifactId>spring-cloud-schema-registry-client-apicurio</artifactId>
    <version>2.0.0</version>
</dependency>
```
You can also use Jitpack to get the artifact from GitHub Releases directly. Before that add Jitpack to your build file to support loading artifacts directly from GitHub.
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

## Concept
You may find an introductory article to Spring Cloud Schema Registry and Spring Cloud Stream with Confluent Registry and Kafka in this article [Spring Cloud Stream with Schema Registry and Kafka](https://piotrminkowski.com/2021/07/22/spring-cloud-stream-with-schema-registry-and-kafka/).

A typical architecture of our solution is visible below. In general, in EDA (event-driven architecture) we may need a tools for messages schema validation and evolving. \
Spring Cloud Schema Register implements this approach. The concept is pretty simple. On the producer side client registers a new version of schema (by default in Apache Avro format) using REST API provided by the schema server. \
A consumer receives a message with subject name and schema version in the `ContentType` header. Then it retrieves a schema from schema registry server and deserializes a message. \

<img src="https://i2.wp.com/piotrminkowski.com/wp-content/uploads/2021/07/spring-cloud-stream-kafka-schema-registry.png?resize=696%2C441&ssl=1" title="Architecture"><br/>

Apicurio provides REST API for interaction with registry server. The current version of Apicurio REST API is 2.0.1 is described [here](https://www.apicur.io/registry/docs/apicurio-registry/2.0.1.Final/assets-attachments/registry-rest-api.htm).
