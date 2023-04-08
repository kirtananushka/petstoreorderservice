package com.chtrembl.petstore.order.client;

import com.chtrembl.petstore.order.model.ContainerEnvironment;
import com.chtrembl.petstore.order.model.OrderReservationRequest;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OrderReservationWebClient {

  private static final Logger logger = LoggerFactory.getLogger(OrderReservationWebClient.class);

  private final ContainerEnvironment containerEnvironment;

  private WebClient orderReservationFnWebClient;

  public OrderReservationWebClient(ContainerEnvironment containerEnvironment) {
    this.containerEnvironment = containerEnvironment;
  }

  @PostConstruct
  public void initialize() {
    this.orderReservationFnWebClient = WebClient.builder()
      .baseUrl(this.containerEnvironment.getPetStoreOrderReservationFnURL())
      .build();
  }

  public void reserveOrder(OrderReservationRequest request) {
    try {
      this.orderReservationFnWebClient.post()
        .uri("")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Cache-Control", "no-cache")
        .bodyValue(request)
        .retrieve();
//        .bodyToMono(String.class)
//        .block()
      logger.info("PetStoreOrderService: order reserved using web client; order={}", request.getOrderJSON());
    } catch (Exception e) {
      logger.error("PetStoreOrderService: error while reserving order using web client; order={} ", request.getOrderJSON(), e);
    }
  }
}
