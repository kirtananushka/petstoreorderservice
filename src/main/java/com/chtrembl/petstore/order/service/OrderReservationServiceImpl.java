package com.chtrembl.petstore.order.service;

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

  private WebClient appWebClient = null;
  private WebClient orderReservationFnWebClient = null;

  public OrderReservationServiceImpl(ContainerEnvironment containerEnvironment) {
    this.containerEnvironment = containerEnvironment;
  }

  @PostConstruct
  public void initialize() {
    this.appWebClient = WebClient.builder()
      .baseUrl(this.containerEnvironment.getPetStoreAppURL())
      .build();
    this.orderReservationFnWebClient = WebClient.builder()
      .baseUrl(this.containerEnvironment.getPetStoreOrderReservationFnURL())
      .build();
  }

  @Override
  public void reserveOrder(Order order) {
    String sessionId = retrieveSessionId().orElse(order.getId());
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

    try {
      this.orderReservationFnWebClient.post()
        .uri("")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Cache-Control", "no-cache")
        .bodyValue(request)
        .retrieve();
//        .bodyToMono(String.class)
//        .block()
      logger.info("PetStoreOrderService: order={} reserved", request.getOrderJSON());
    } catch (Exception e) {
      logger.error("PetStoreOrderService: error while reserving order={}", request.getOrderJSON(), e);
    }
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
