package com.chtrembl.petstore.order.api.testcontroller;

import com.chtrembl.petstore.order.model.Order;
import com.chtrembl.petstore.order.model.Product;
import com.chtrembl.petstore.order.service.OrderReservationService;
import com.chtrembl.petstore.order.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${openapi.swaggerPetstore.base-path:/petstoreorderservice/v2}")
public class TestController {

  private final OrderService orderService;
  private final OrderReservationService orderReservationService;

  @PostMapping("/reservedummyorder")
  public Order reserveDummyOrder() {
    Order order = new Order();
    order.setId("TEST_ID");
    order.setEmail("test@mail.com");
    order.setStatus(Order.StatusEnum.APPROVED);
    order.setComplete(Boolean.FALSE);

    Product product = new Product();
    product.setId(1L);
    product.setQuantity(42);
    order.setProducts(List.of(product));

    orderService.save(order);
    orderReservationService.reserveOrder(order);

    return orderService.findById(order.getId());
  }
}
