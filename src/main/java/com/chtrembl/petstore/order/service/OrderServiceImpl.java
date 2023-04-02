package com.chtrembl.petstore.order.service;

import com.azure.cosmos.implementation.NotFoundException;
import com.chtrembl.petstore.order.model.Order;
import com.chtrembl.petstore.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

  static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

  public OrderServiceImpl(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  private final OrderRepository orderRepository;

  @Override
  public void save(Order order) {
    try {
      log.info("Saving order={} to database", order);
      orderRepository.save(order);
    } catch (Exception e) {
      log.error("Error while saving order={}", order, e);
    }
  }

  @Override
  public Order findById(String id) {
    try {
      return orderRepository.findById(id);
    } catch (NotFoundException e) {
      log.warn("Order with id {} not found in database. Creating new order...", id);
      return new Order();
    }
  }
}
