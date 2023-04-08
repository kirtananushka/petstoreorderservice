package com.chtrembl.petstore.order.service;

import com.chtrembl.petstore.order.client.OrderReservationServiceBusClient;
import com.chtrembl.petstore.order.model.ContainerEnvironment;
import com.chtrembl.petstore.order.model.Order;
import com.chtrembl.petstore.order.model.OrderReservationRequest;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OrderReservationServiceImpl implements OrderReservationService {
  private static final Logger logger = LoggerFactory.getLogger(OrderReservationServiceImpl.class);

  private final ContainerEnvironment containerEnvironment;
  private final OrderReservationServiceBusClient orderReservationServiceBusClient;
  private WebClient appWebClient;

  public OrderReservationServiceImpl(
    ContainerEnvironment containerEnvironment,
    OrderReservationServiceBusClient orderReservationServiceBusClient
  ) {
    this.containerEnvironment = containerEnvironment;
    this.orderReservationServiceBusClient = orderReservationServiceBusClient;
  }

  @PostConstruct
  public void initialize() {
    this.appWebClient = WebClient.builder()
      .baseUrl(this.containerEnvironment.getPetStoreAppURL())
      .build();
  }

  @Override
  public void reserveOrder(Order order) {
    String sessionId = retrieveSessionId().filter(id-> !id.isBlank()).orElse(order.getId());
    String orderJSON = "";
    try {
      orderJSON = new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false).writeValueAsString(order);
    } catch (JsonProcessingException e) {
      logger.warn("PetStoreOrderService: exception while order serialization: {}", e.getMessage());
    }

    OrderReservationRequest request = new OrderReservationRequest();
    request.setSessionId(sessionId);
    request.setOrderJSON(orderJSON);

//    orderReservationWebClient.reserveOrder(request);
    orderReservationServiceBusClient.reserveOrder(request);
  }


  private Optional<String> retrieveSessionId() {
    logger.info("PetStoreOrderService retrieving sessionId from PetStoreApp");
    String sessionId = "";
    try {
      sessionId = this.appWebClient.get()
        .uri("/api/sessionid")
        .accept(MediaType.TEXT_PLAIN)
        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
        .header("Cache-Control", "no-cache")
        .retrieve()
        .bodyToMono(String.class).block();
    } catch (Exception e) {
      logger.warn("PetStoreOrderService: exception while retrieving sessionId from PetStoreApp: {}", e.getMessage());
    }
    return Optional.ofNullable(sessionId);
  }
}
