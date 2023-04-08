package io.swagger;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.chtrembl.petstore.order.client.OrderReservationServiceBusClient;
import com.chtrembl.petstore.order.client.OrderReservationWebClient;
import com.chtrembl.petstore.order.model.ContainerEnvironment;
import com.chtrembl.petstore.order.service.OrderReservationService;
import com.chtrembl.petstore.order.service.OrderReservationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableCaching
@EnableSwagger2
@ComponentScan(basePackages = {
  "io.swagger",
  "com.chtrembl.petstore.order.api",
  "com.chtrembl.petstore.order.service",
  "com.chtrembl.petstore.order.repository",
  "io.swagger.configuration"
})
public class Swagger2SpringBoot implements CommandLineRunner {
  static final Logger log = LoggerFactory.getLogger(Swagger2SpringBoot.class);

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public CosmosClient cosmosClient(
    @Value("${petstore.cosmosdb.account.host}") String accountHost,
    @Value("${petstore.cosmosdb.account.key}") String accountKey) {
    return new CosmosClientBuilder()
      .endpoint(accountHost)
      .key(accountKey)
      .consistencyLevel(ConsistencyLevel.EVENTUAL)
      .buildClient();
  }

  @Bean
  public CosmosDatabase cosmosDatabase(
    CosmosClient cosmosClient, @Value("${petstore.cosmosdb.database.name}") String databaseName) {
    CosmosDatabaseResponse databaseResponse = cosmosClient.createDatabaseIfNotExists(databaseName);
    return cosmosClient.getDatabase(databaseResponse.getProperties().getId());
  }

  @Bean
  public ServiceBusSenderClient serviceBusSenderClient(
    @Value("${azure.service-bus.connection-string}") String connectionString,
    @Value("${azure.service-bus.queue-name}") String queueName
  ) {
    return new ServiceBusClientBuilder()
      .connectionString(connectionString)
      .sender()
      .queueName(queueName)
      .buildClient();
  }

  @Bean
  public CosmosContainer cosmosContainer(
    CosmosDatabase cosmosDatabase, @Value("${petstore.cosmosdb.container.name}") String containerName) {
    CosmosContainerProperties containerProperties =
      new CosmosContainerProperties(containerName, "/id");
    CosmosContainerResponse containerResponse = cosmosDatabase.createContainerIfNotExists(containerProperties);
    return cosmosDatabase.getContainer(containerResponse.getProperties().getId());
  }

  @Bean
  public ContainerEnvironment containerEnvironment() {
    return new ContainerEnvironment();
  }

  @Bean
  public OrderReservationWebClient orderReservationWebClient(ContainerEnvironment containerEnvironment) {
    return new OrderReservationWebClient(containerEnvironment);
  }

  @Bean
  public OrderReservationServiceBusClient orderReservationServiceBusClient(
    ServiceBusSenderClient serviceBusSenderClient) {
    return new OrderReservationServiceBusClient(serviceBusSenderClient);
  }

  @Bean
  public OrderReservationService orderReservationService(
    ContainerEnvironment containerEnvironment,
    OrderReservationServiceBusClient orderReservationServiceBusClient) {
    return new OrderReservationServiceImpl(
      containerEnvironment,
      orderReservationServiceBusClient
    );
  }

  @Override
  public void run(String... arg0) throws Exception {
    if (arg0.length > 0 && arg0[0].equals("exitcode")) {
      throw new ExitException();
    }
  }

  public static void main(String[] args) throws Exception {
    new SpringApplication(Swagger2SpringBoot.class).run(args);
  }

  class ExitException extends RuntimeException implements ExitCodeGenerator {
    private static final long serialVersionUID = 1L;

    @Override
    public int getExitCode() {
      return 10;
    }

  }
}
