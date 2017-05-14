package com.logistimo.models.shipments;

import java.util.List;

/**
 * Created by Mohan Raja on 05/10/16
 */
public class ShipmentMaterialsModel {
  public List<ShipmentItemModel> items;
  public String msg;
  /**
   * Shipment Id
   */
  public String sId;
  public String userId;
  public Long kid;
  /**
   * Actual fulfilment date, only in case of fulfilling shipment
   */
  public String afd;

  public boolean isFulfil;
  public boolean isOrderFulfil;
  /**
   * Order update time
   */
  public String orderUpdatedAt;
}
