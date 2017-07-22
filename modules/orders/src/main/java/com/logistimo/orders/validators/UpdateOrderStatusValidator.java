package com.logistimo.orders.validators;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.exception.ValidationException;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.MaterialUtils;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.MsgUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charan on 16/07/17.
 */
@Component
@Qualifier("orderStatusValidator")
public class UpdateOrderStatusValidator {

  @Autowired
  EntitiesService entitiesService;

  @Autowired
  MaterialCatalogService materialCatalogService;

  @Autowired
  InventoryManagementService inventoryManagementService;

  @Autowired
  IOrderApprovalsService orderApprovalsService;

  public void validateOrderStatusChange(IOrder order, String newStatus)
      throws ValidationException, ServiceException, ObjectNotFoundException {

    switch (newStatus) {
      case IOrder.CONFIRMED:
        checkTransferStatus(order);
        checkIfVisibleToVendor(order);
        checkIfMaterialsExistAtVendor(order);
        break;
      case IOrder.COMPLETED:
        checkTransferStatus(order);
        checkIfVisibleToVendor(order);
        checkShippingApproval(order);
        checkIfMaterialsExistAtVendor(order);
        break;
    }

  }

  public void checkTransferStatus(IOrder order) throws ValidationException {
    if (order.isTransfer() && !orderApprovalsService.isTransferApprovalComplete(order)) {
      throw new ValidationException("O008", new Object[0]);
    }
  }

  public void checkShippingApproval(IOrder order) throws ServiceException,
      ObjectNotFoundException, ValidationException {
    if (!orderApprovalsService.isShippingApprovalComplete(order)) {
      throw new ValidationException("O007", new Object[0]);
    }
  }

  public void checkIfVisibleToVendor(IOrder order) throws ValidationException {
    if (!order.isVisibleToVendor()) {
      throw new ValidationException("O006", new Object[0]);
    }
  }

  public void checkIfMaterialsExistAtVendor(IOrder order)
      throws ValidationException, ServiceException {
    List<IMaterial>
        materialsNotExistingInVendor =
        getMaterialsNotExistingInKiosk(order.getServicingKiosk(), order);
    if (!materialsNotExistingInVendor.isEmpty()) {
      IKiosk vnd = entitiesService.getKiosk(order.getServicingKiosk(), false);
      throw new ValidationException("I003", MsgUtil.bold(vnd.getName()),
          MaterialUtils.getMaterialNamesString(materialsNotExistingInVendor));
    }
  }

  public List<IMaterial> getMaterialsNotExistingInKiosk(Long kioskId, IOrder order)
      throws ServiceException {
    List<IMaterial> materialsNotExisting = new ArrayList<>(1);
    if(order.getItems() != null) {
      for (IDemandItem demandItem : order.getItems()) {
        if (BigUtil.greaterThanZero(demandItem.getQuantity())) {
          IInvntry inv = inventoryManagementService.getInventory(kioskId, demandItem.getMaterialId());
          if (inv == null) {
            IMaterial material = materialCatalogService.getMaterial(demandItem.getMaterialId());
            materialsNotExisting.add(material);
          }
        }
      }
    }
    return materialsNotExisting;
  }
}
