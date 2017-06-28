package com.logistimo.orders.builders;

import com.logistimo.entities.builders.EntityBuilder;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.OrderModel;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charan on 26/06/17.
 */
@Component
public class OrderBuilder {

  @Autowired
  OrderManagementService orderManagementService;

  @Autowired
  EntityBuilder entityBuilder;

  public OrderModel buildMeta(Long orderId) throws ServiceException, ObjectNotFoundException {
    IOrder order = orderManagementService.getOrder(orderId);
    OrderModel model = new OrderModel();
    model.setOrderId(orderId);
    model.setCustomer(entityBuilder.buildMeta(order.getKioskId()));
    if (order.getServicingKiosk() != null) {
      model.setVendor(entityBuilder.buildMeta(order.getServicingKiosk()));
    }
    model.setNumItems(order.getNumberOfItems());
    model.setCreatedAt(order.getCreatedOn());
    return model;
  }

  public OrderModel build(Long orderId) throws ServiceException, ObjectNotFoundException {
    //TODO Include demand items;
    return buildMeta(orderId);
  }
}
