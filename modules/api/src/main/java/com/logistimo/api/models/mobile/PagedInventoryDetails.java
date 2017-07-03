package com.logistimo.api.models.mobile;

import com.logistimo.api.models.InventoryDetail;

import java.util.List;

/**
 * Created by yuvaraj on 03/05/17.
 */
public class PagedInventoryDetails extends BasePagedResponseModel {

  public List<InventoryDetail> items;

}
