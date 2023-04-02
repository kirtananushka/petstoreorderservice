package com.chtrembl.petstore.order.repository;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.chtrembl.petstore.order.model.Order;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

  private final CosmosContainer container;

  public OrderRepositoryImpl(CosmosContainer container) {
    this.container = container;
  }

  @Override
  public void save(Order order) {
    container.upsertItem(order, new PartitionKey(order.getId()), new CosmosItemRequestOptions());
  }

  @Override
  public Order findById(String id) {
      return container.readItem(id, new PartitionKey(id), Order.class).getItem();
  }
}
