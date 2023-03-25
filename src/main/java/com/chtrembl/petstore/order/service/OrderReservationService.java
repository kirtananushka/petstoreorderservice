package com.chtrembl.petstore.order.service;

import com.chtrembl.petstore.order.model.Order;

public interface OrderReservationService {

	void reserveOrder(Order order);
}
