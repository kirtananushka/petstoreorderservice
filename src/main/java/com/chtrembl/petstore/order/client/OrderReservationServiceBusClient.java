package com.chtrembl.petstore.order.client;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.chtrembl.petstore.order.model.OrderReservationRequest;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderReservationServiceBusClient {

  private static final Logger logger = LoggerFactory.getLogger(OrderReservationServiceBusClient.class);

  private final ServiceBusSenderClient serviceBusSenderClient;

  public OrderReservationServiceBusClient(ServiceBusSenderClient serviceBusSenderClient) {
    this.serviceBusSenderClient = serviceBusSenderClient;
  }

  public void reserveOrder(OrderReservationRequest request) {

    String payload = request.getOrderJSON();
    ServiceBusMessage message = new ServiceBusMessage(payload);

    logger.info("PetStoreOrderService: reserving order using service bus client; order={}", request.getOrderJSON());

    try {
      serviceBusSenderClient.sendMessage(message);
      logger.info("PetStoreOrderService: order reserved using service bus client; order={}", request.getOrderJSON());
    } catch (Exception e) {
      logger.error("PetStoreOrderService: error while reserving order using service bus client; order={} ", request.getOrderJSON(), e);
    }
  }

  @PreDestroy
  public void cleanup() throws InterruptedException {
    serviceBusSenderClient.close();
  }
}
