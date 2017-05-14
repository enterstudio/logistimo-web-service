package com.logistimo.api.builders;

import com.logistimo.accounting.service.impl.AccountingServiceImpl;
import com.logistimo.api.auth.Authoriser;
import com.logistimo.api.models.DemandItemBatchModel;
import com.logistimo.api.models.DemandModel;
import com.logistimo.api.models.OrderModel;
import com.logistimo.api.models.OrderResponseModel;
import com.logistimo.api.util.CommonUtil;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.IHandlingUnitService;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.HandlingUnitServiceImpl;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.models.shipments.ShipmentItemModel;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.UpdatedOrder;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.Services;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.entity.IShipmentItem;
import com.logistimo.shipments.entity.IShipmentItemBatch;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.CommonUtils;
import com.logistimo.utils.LocalDateUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class OrderBuilder {

  private static final XLog xLogger = XLog.getLog(OrderBuilder.class);

  public Results buildOrders(Results results, SecureUserDetails user, Long domainId) {
    List orders = results.getResults();
    List<OrderModel> newOrders = new ArrayList<>();
    int sno = results.getOffset() + 1;
    Map<Long, String> domainNames = new HashMap<>(1);
    for (Object obj : orders) {
      IOrder o = (IOrder) obj;
      // Add row
      OrderModel model = build(o, user, domainId, domainNames);
      if (model != null) {
        model.sno = sno++;
        newOrders.add(model);
      }
    }
    Results finalResults = new Results(newOrders, results.getCursor(),
        results.getNumFound(), results.getOffset());
    return finalResults;
  }

  public OrderModel build(IOrder o, SecureUserDetails user, Long domainId,
                          Map<Long, String> domainNames) {
    OrderModel model = new OrderModel();
    Long kioskId = o.getKioskId();
    Locale locale = user.getLocale();
    String timezone = user.getTimezone();
    IKiosk k = null;
    IKiosk vendor = null;
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
      k = as.getKiosk(kioskId, false);
    } catch (Exception e) {
      xLogger.warn("{0} when getting kiosk data for order {1}: {2}",
          e.getClass().getName(), o.getOrderId(), e.getMessage());
      return null;
    }
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      EntitiesService es = Services.getService(EntitiesServiceImpl.class, locale);
      String domainName = domainNames.get(o.getDomainId());

      model.id = o.getOrderId();
      model.size = o.getNumberOfItems();
      model.cur = o.getCurrency();
      model.price = o.getFormattedPrice();
      model.status = OrderUtils.getStatusDisplay(o.getStatus(), locale);
      model.st = o.getStatus();
      model.enm = k.getName();
      model.eadd = CommonUtil.getAddress(k.getCity(),k.getTaluk(), k.getDistrict(), k.getState());
      model.cdt = LocalDateUtil.format(o.getCreatedOn(), locale, timezone);
      model.ubid = o.getUpdatedBy();
      if (o.getUpdatedBy() != null) {
        try {
          model.uby = as.getUserAccount(o.getUpdatedBy()).getFullName();
          model.udt = LocalDateUtil.format(o.getUpdatedOn(), locale, timezone);
          model.orderUpdatedAt =
              LocalDateUtil.formatCustom(o.getUpdatedOn(), Constants.DATETIME_FORMAT, null);
        } catch (Exception e) {
          // ignore
        }
      }
      model.tax = o.getTax();
      model.uid = o.getUserId();

      if (model.uid != null) {
        try {
          IUserAccount orderedBy = as.getUserAccount(model.uid);
          model.unm = orderedBy.getFullName();
        } catch (Exception e) {
          xLogger.warn("{0} when getting details for user who created the order {1}: ",
              e.getClass().getName(), o.getOrderId(), e);
        }
      }
      model.eid = o.getKioskId();
      model.tgs = o.getTags(TagUtil.TYPE_ORDER);
      model.sdid = o.getDomainId();
      model.lt = o.getLatitude();
      model.ln = o.getLongitude();
      Long vendorId = o.getServicingKiosk();

      if (vendorId != null) {
        try {
          vendor = es.getKiosk(vendorId);
          model.vid = vendorId.toString();
          model.vnm = vendor.getName();
          model.vadd =
              CommonUtil.getAddress(vendor.getCity(),vendor.getTaluk(), vendor.getDistrict(), vendor.getState());
          model.hva =
              Authoriser
                  .authoriseEntity(vendorId, user.getRole(), locale, user.getUsername(), domainId);
        } catch (Exception e) {
          xLogger.warn("{0} when getting vendor data for order {1}: {2}",
              e.getClass().getName(), o.getOrderId(), e.getMessage());
          if (o.getStatus().equals(IOrder.FULFILLED)) {
            model.vid = "-1";
          }
        }
      }
      Integer vPermission = k.getVendorPerm();
      if (vPermission < 2 && model.vid != null) {
        vPermission =
            Authoriser
                .authoriseEntityPerm(Long.valueOf(model.vid), user.getRole(), user.getLocale(),
                    user.getUsername(), user.getDomainId());
      }
      model.atv = vPermission > 1;
      model.atvv = vPermission > 0;

      if (vendor != null) {
        Integer cPermission = vendor.getCustomerPerm();
        if (cPermission < 2 && model.eid != null) {
          cPermission =
              Authoriser.authoriseEntityPerm(model.eid, user.getRole(), user.getLocale(),
                  user.getUsername(), user.getDomainId());
        }
        model.atc = cPermission > 1;
        model.atvc = cPermission > 0;
      }
      if (domainName == null) {
        IDomain domain = null;
        try {
          domain = ds.getDomain(o.getDomainId());
        } catch (Exception e) {
          xLogger.warn("Error while fetching Domain {0}", o.getDomainId());
        }
        if (domain != null) {
          domainName = domain.getName();
        } else {
          domainName = Constants.EMPTY;
        }
        domainNames.put(o.getDomainId(), domainName);
      }
      model.sdname = domainName;
      SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
      if (o.getExpectedArrivalDate() != null) {
        model.efd = sdf.format(o.getExpectedArrivalDate());
        model.efdLabel =
            LocalDateUtil
                .format(o.getExpectedArrivalDate(), user.getLocale(), user.getTimezone(), true);
      }
      if (o.getDueDate() != null) {
        model.edd = sdf.format(o.getDueDate());
        model.eddLabel =
            LocalDateUtil.format(o.getDueDate(), user.getLocale(), user.getTimezone(), true);
      }
    } catch (Exception e) {
      xLogger.warn("{0} when trying to get kiosk {1} and create a new row for order {2}: {3}",
          e.getClass().getName(), kioskId, o.getOrderId(), e);
    }
    model.oty = o.getOrderType();
    model.crsn = o.getCancelledDiscrepancyReason();
    return model;
  }

  public OrderModel buildFull(IOrder order, SecureUserDetails user,
                              Long domainId) throws Exception {
    Map<Long, String> domainNames = new HashMap<>(1);
    OrderModel model = build(order, user, domainId, domainNames);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, user.getLocale());
    InventoryManagementService ims = Services.getService(InventoryManagementServiceImpl.class, user.getLocale());
    IKiosk k = null;
    IKiosk vendorKiosk = null;
    Locale locale = user.getLocale();
    String timezone = user.getTimezone();
    MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class, user.getLocale());

    boolean showStocks = IOrder.PENDING.equals(order.getStatus()) || IOrder.CONFIRMED.equals(order.getStatus())
            || IOrder.BACKORDERED.equals(order.getStatus());
    // to the logged in user
    boolean showVendorStock = dc.autoGI() && order.getServicingKiosk() != null;

    if (order.getServicingKiosk() != null) {
      try {
        vendorKiosk = as.getKiosk(order.getServicingKiosk(), false);
      } catch (Exception e) {
        xLogger.warn("Error when trying to get kiosk {0} and create a new row for order {1}",
            order.getKioskId(), order.getOrderId(), e);
      }
    }

    // Accounting
    // Check if accounting is enabled
    boolean accountingEnabled = dc.isAccountingEnabled();

    // Get the credit limit and amount paid for the customer
    String creditLimitErr = null;
    BigDecimal availableCredit = BigDecimal.ZERO;
    // Get the credit limit and check against receivables
    if (accountingEnabled) {
      try {
        Long customerId = order.getKioskId();
        // Get the credit limit
        if (customerId != null && model.vid != null) {
          availableCredit = Services.getService(AccountingServiceImpl.class,locale)
              .getCreditData(customerId,
                  order.getServicingKiosk(), dc).availabeCredit;
        }
      } catch (Exception e) {
        creditLimitErr = e.getMessage();
      }
    }
    model.avc = availableCredit;
    model.avcerr = creditLimitErr;

    model.lt = order.getLatitude();
    model.ln = order.getLongitude();
    model.ac = order.getGeoAccuracy();

    if (order.getKioskId() != null) {
      try {
        k = as.getKiosk(order.getKioskId(), false);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when trying to get kiosk {1} while fetching order details for order {2}: {3}",
            e.getClass().getName(), order.getKioskId(), order.getOrderId(), e);
      }
    }

    if (k != null) {
      model.elt = k.getLatitude();
      model.eln = k.getLongitude();
    }

    model.tp = order.getTotalPrice();
    model.pst = order.getPriceStatement();

    if (accountingEnabled) {
      model.pd = order.getPaid();
      model.po = order.getPaymentOption();

    }

    UsersService accountsService = Services.getService(UsersServiceImpl.class);
    if (model.uid != null) {
      try {
        IUserAccount orderedBy = accountsService.getUserAccount(model.uid);
        model.unm = orderedBy.getFullName();
      } catch (Exception e) {
        // ignore
      }
    }
    if (order.getOrderType() != null && order.getOrderType() == 0) {
      model.alc = false;
      if (SecurityConstants.ROLE_SUPERUSER.equals(user.getRole()) || user.getUsername()
          .equals(order.getUserId())) {
        model.alc = true;
      } else if ((SecurityConstants.ROLE_DOMAINOWNER.equals(user.getRole()))) {
        if (user.getDomainId().equals(order.getDomainId())) {
          model.alc = true;
        } else {
          Set<Long> rs = DomainsUtil.getDomainParents(order.getDomainId(), true);
          if (rs != null) {
            model.alc = rs.contains(user.getDomainId());
          }
        }
      }
    } else {
      model.alc = true;
    }
    model.tgs = order.getTags(TagUtil.TYPE_ORDER);
    model.rid = order.getReferenceID();
    model.pt = LocalDateUtil.getFormattedMillisInHoursDays(order.getProcessingTime(), locale, true);
    model.dlt =
        LocalDateUtil.getFormattedMillisInHoursDays(order.getDeliveryLeadTime(), locale, true);

    IShipmentService ss = Services.getService(ShipmentService.class);
    List<IShipment> shipments = ss.getShipmentsByOrderId(order.getOrderId());
    Map<Long, Map<String, BigDecimal>> quantityByBatches = null;
    Map<Long, Map<String, DemandBatchMeta>> fQuantityByBatches = null;
    Map<Long, List<ShipmentItemModel>> fReasons = null;
    if (!showStocks) {
      quantityByBatches = new HashMap<>();
      for (IShipment shipment : shipments) {
        boolean isFulfilled = ShipmentStatus.FULFILLED.equals(shipment.getStatus());
        if (isFulfilled && fQuantityByBatches == null) {
          fQuantityByBatches = new HashMap<>();
        }
        if (isFulfilled && fReasons == null) {
          fReasons = new HashMap<>();
        }
        if (ShipmentStatus.SHIPPED.equals(shipment.getStatus()) || isFulfilled) {
          ss.includeShipmentItems(shipment);
          for (IShipmentItem iShipmentItem : shipment.getShipmentItems()) {
            if (iShipmentItem.getShipmentItemBatch() != null
                && iShipmentItem.getShipmentItemBatch().size() > 0) {
              for (IShipmentItemBatch iShipmentItemBatch : iShipmentItem.getShipmentItemBatch()) {
                if (!quantityByBatches.containsKey(iShipmentItem.getMaterialId())) {
                  quantityByBatches
                      .put(iShipmentItem.getMaterialId(), new HashMap<String, BigDecimal>());
                }
                Map<String, BigDecimal>
                    batches =
                    quantityByBatches.get(iShipmentItem.getMaterialId());
                if (batches.containsKey(iShipmentItemBatch.getBatchId())) {
                  batches.put(iShipmentItemBatch.getBatchId(),
                      batches.get(iShipmentItemBatch.getBatchId())
                          .add(iShipmentItemBatch.getQuantity()));
                } else {
                  batches.put(iShipmentItemBatch.getBatchId(), iShipmentItemBatch.getQuantity());
                }
                if (isFulfilled) {
                  if (!fQuantityByBatches.containsKey(iShipmentItem.getMaterialId())) {
                    fQuantityByBatches
                        .put(iShipmentItem.getMaterialId(), new HashMap<String, DemandBatchMeta>());
                  }
                  Map<String, DemandBatchMeta>
                      fBatches =
                      fQuantityByBatches.get(iShipmentItem.getMaterialId());
                  if (fBatches.containsKey(iShipmentItemBatch.getBatchId())) {
                    fBatches.get(iShipmentItemBatch.getBatchId()).quantity =
                        fBatches.get(iShipmentItemBatch.getBatchId()).quantity
                            .add(iShipmentItemBatch.getFulfilledQuantity());
                    fBatches.get(iShipmentItemBatch.getBatchId()).bd
                        .add(getShipmentItemBatchBD(shipment.getShipmentId(), iShipmentItemBatch));
                  } else {
                    DemandBatchMeta dMeta = new DemandBatchMeta(iShipmentItemBatch.getFulfilledQuantity());
                    dMeta.bd.add(getShipmentItemBatchBD(shipment.getShipmentId(), iShipmentItemBatch));
                    fBatches.put(iShipmentItemBatch.getBatchId(), dMeta);
                  }
                }
              }
            } else if (isFulfilled) {
              if (!fReasons.containsKey(iShipmentItem.getMaterialId())) {
                fReasons.put(iShipmentItem.getMaterialId(), new ArrayList<ShipmentItemModel>());
              }
              ShipmentItemModel m = new ShipmentItemModel();
              m.sid = shipment.getShipmentId();
              m.q = iShipmentItem.getQuantity();
              m.fq = iShipmentItem.getFulfilledQuantity();
              m.frsn = iShipmentItem.getFulfilledDiscrepancyReason();
              fReasons.get(iShipmentItem.getMaterialId()).add(m);
            }
          }
        }
      }
    }
    IDemandService dms = Services.getService(DemandService.class);
    List<IDemandItem> items = dms.getDemandItems(order.getOrderId());
    if (items != null) {
      Set<DemandModel> modelItems = new TreeSet<>();
      for (IDemandItem item : items) {
        Long mid = item.getMaterialId();
        IMaterial m = null;
        try {
          m = mcs.getMaterial(item.getMaterialId());
        } catch (Exception e) {
          xLogger.warn("WARNING: " + e.getClass().getName() + " when getting material "
              + item.getMaterialId() + ": " + e.getMessage());
          continue;
        }
        DemandModel itemModel = new DemandModel();
        itemModel.nm = m.getName();
        itemModel.id = mid;
        itemModel.q = item.getQuantity();
        itemModel.p = item.getFormattedPrice();
        itemModel.t = item.getTax();
        itemModel.d = item.getDiscount();
        itemModel.a = CommonUtils.getFormattedPrice(item.computeTotalPrice(false));
        itemModel.isBn = m.isBinaryValued();
        itemModel.isBa = (vendorKiosk == null || vendorKiosk.isBatchMgmtEnabled()) && m.isBatchEnabled();
        itemModel.oq = item.getOriginalQuantity();
        itemModel.tx = item.getTax();
        itemModel.rsn = item.getReason();
        itemModel.sdrsn = item.getShippedDiscrepancyReason();
        itemModel.sq = item.getShippedQuantity();
        itemModel.yts = itemModel.q.subtract(itemModel.sq);
        itemModel.isq = item.getInShipmentQuantity();
        itemModel.ytcs = itemModel.q.subtract(itemModel.isq);
        //itemModel.mst = item.getMaterialStatus();
        itemModel.rq = item.getRecommendedOrderQuantity();
        itemModel.fq = item.getFulfilledQuantity();
        itemModel.oastk = BigDecimal.ZERO;
        itemModel.astk = BigDecimal.ZERO;
        itemModel.tm = m.isTemperatureSensitive();
        if (showStocks) {
          List<IInvAllocation> allocationList = ims.getAllocationsByTagMaterial(mid,
              IInvAllocation.Type.ORDER + CharacterConstants.COLON + order.getOrderId());
          for (IInvAllocation ia : allocationList) {
            if (IInvAllocation.Type.ORDER.toString().equals(ia.getType())) {
              if (itemModel.isBa) {
                if (BigUtil.equalsZero(ia.getQuantity())) {
                  continue;
                }
                DemandItemBatchModel batchModel = new DemandItemBatchModel();
                batchModel.q = ia.getQuantity();
                batchModel.id = ia.getBatchId();
                IInvntryBatch
                    b =
                    ims.getInventoryBatch(order.getServicingKiosk(), item.getMaterialId(),
                        batchModel.id, null);
                if (b == null) {
                  b = ims.getInventoryBatch(order.getKioskId(), item.getMaterialId(),
                      batchModel.id, null);
                }
                if (b == null) {
                  xLogger.warn("Error while getting inventory batch for kiosk {0}, material {1}, "
                          + "batch id {2}, order id: {3}", order.getServicingKiosk(),
                      item.getMaterialId(), batchModel.id, order.getOrderId());
                  continue;
                }
                batchModel.e = b.getBatchExpiry() != null ? LocalDateUtil
                        .formatCustom(b.getBatchExpiry(), "dd/MM/yyyy", null) : "";
                batchModel.m = b.getBatchManufacturer();
                batchModel.mdt = b.getBatchManufacturedDate() != null ? LocalDateUtil
                        .formatCustom(b.getBatchManufacturedDate(), "dd/MM/yyyy", null) : "";
                itemModel.astk = itemModel.astk.add(batchModel.q);
                if (itemModel.bts == null) {
                  itemModel.bts = new HashSet<>();
                }
                batchModel.mst = ia.getMaterialStatus();
                itemModel.bts.add(batchModel);
              } else {
                itemModel.astk = ia.getQuantity();
                itemModel.mst = ia.getMaterialStatus();
              }
            }
            itemModel.oastk = itemModel.oastk.add(ia.getQuantity());
          }
        } else {
          if (itemModel.isBa) {
            Map<String, BigDecimal> batchMap = quantityByBatches.get(item.getMaterialId());
            Map<String, DemandBatchMeta> fBatchMap = null;
            if (fQuantityByBatches != null) {
              fBatchMap = fQuantityByBatches.get(item.getMaterialId());
            }
            if (batchMap != null) {
              for (String batchId : batchMap.keySet()) {
                DemandItemBatchModel batchModel = new DemandItemBatchModel();
                batchModel.q = batchMap.get(batchId);
                if (fBatchMap != null && fBatchMap.get(batchId) != null) {
                  batchModel.fq = fBatchMap.get(batchId).quantity;
                  batchModel.bd = fBatchMap.get(batchId).bd;
                }
                batchModel.id = batchId;
                IInvntryBatch b = ims.getInventoryBatch(order.getServicingKiosk(), item.getMaterialId(),
                        batchModel.id, null);
                if (b == null) {
                  b = ims.getInventoryBatch(order.getKioskId(), item.getMaterialId(), batchModel.id,
                      null);
                }
                if (b == null) {
                  xLogger.warn("Error while getting inventory batch for kiosk {0}, material {1}, "
                          + "batch id {2}, order id: {3}", order.getServicingKiosk(),
                      item.getMaterialId(), batchModel.id, order.getOrderId());
                  continue;
                }
                batchModel.e = b.getBatchExpiry() != null ? LocalDateUtil
                        .formatCustom(b.getBatchExpiry(), "dd/MM/yyyy", null) : "";
                batchModel.m = b.getBatchManufacturer();
                batchModel.mdt = b.getBatchManufacturedDate() != null ? LocalDateUtil
                        .formatCustom(b.getBatchManufacturedDate(), "dd/MM/yyyy", null) : "";
                itemModel.astk = itemModel.astk.add(batchModel.q);
                if (itemModel.bts == null) {
                  itemModel.bts = new HashSet<>();
                }
                itemModel.bts.add(batchModel);
              }
            }
          } else {
            if (fReasons != null) {
              itemModel.bd = fReasons.get(item.getMaterialId());
            }
          }
        }
        if (showVendorStock && showStocks) {
          try {
            IInvntry inv = ims.getInventory(order.getServicingKiosk(), mid);
            if (inv != null) {
              itemModel.vs = inv.getStock();
              itemModel.vsavibper = ims.getStockAvailabilityPeriod(inv, dc);
              itemModel.atpstk = inv.getAvailableStock(); //todo: Check Available to promise stock is right??????
              itemModel.itstk = inv.getInTransitStock();
              itemModel.vmax = inv.getMaxStock();
              itemModel.vmin = inv.getReorderLevel();
              IInvntryEvntLog lastEventLog = new InvntryDao().getInvntryEvntLog(inv);
              if (lastEventLog != null) {
                itemModel.vevent = inv.getStockEvent();
              }
            }
          } catch (Exception e) {
            // Its ok for vendor to not have inventory;
          }
        }
        if (showStocks) {
          try {
            IInvntry inv = ims.getInventory(order.getKioskId(), mid);
            if (inv != null) {
              itemModel.stk = inv.getStock();
              itemModel.max = inv.getMaxStock();
              itemModel.min = inv.getReorderLevel();
              IInvntryEvntLog lastEventLog = new InvntryDao().getInvntryEvntLog(inv);
              if (lastEventLog != null) {
                itemModel.event = inv.getStockEvent();
              }
              itemModel.im = inv.getInventoryModel();
              itemModel.eoq = inv.getEconomicOrderQuantity();
              itemModel.csavibper = ims.getStockAvailabilityPeriod(inv, dc);
            }
          } catch (Exception ignored) {
            // ignore
          }
        }
        try {
          IHandlingUnitService hus = Services.getService(HandlingUnitServiceImpl.class);
          Map<String, String> hu = hus.getHandlingUnitDataByMaterialId(mid);
          if (hu != null) {
            itemModel.huQty = new BigDecimal(hu.get(IHandlingUnit.QUANTITY));
            itemModel.huName = hu.get(IHandlingUnit.NAME);
          }
        } catch (Exception e) {
          xLogger.warn("Error while fetching Handling Unit {0}", mid, e);
        }
        modelItems.add(itemModel);

      }

      model.its = modelItems;
    }
    return model;
  }

  private ShipmentItemBatchModel getShipmentItemBatchBD(String shipmentID,
                                                        IShipmentItemBatch iShipmentItemBatch) {
    ShipmentItemBatchModel bd = new ShipmentItemBatchModel();
    bd.fq = iShipmentItemBatch.getFulfilledQuantity();
    bd.frsn = iShipmentItemBatch.getFulfilledDiscrepancyReason();
    bd.sid = shipmentID;
    bd.q = iShipmentItemBatch.getQuantity();
    return bd;
  }

  public OrderResponseModel buildOrderResponseModel(UpdatedOrder updOrder,
                                                    boolean includeOrder, SecureUserDetails sUser,
                                                    Long domainId, boolean isFullOrder)
      throws Exception {
    OrderModel order = null;
    if (includeOrder) {
      if (isFullOrder) {
        order = buildFull(updOrder.order, sUser, domainId);
      } else {
        order = build(updOrder.order, sUser, domainId, new HashMap<Long, String>());
      }
    }
    return new OrderResponseModel(order, updOrder.message, updOrder.inventoryError, null);
  }

  public List<ITransaction> buildTransactionsForNewItems(IOrder order,
                                                         List<DemandModel> demandItemModels) {
    Long domainId = order.getDomainId();
    Long kioskId = order.getKioskId();
    String userId = order.getUserId();
    Date now = new Date();
    List<ITransaction> transactions = new ArrayList<ITransaction>();
    for (DemandModel demandModel : demandItemModels) {
      IDemandItem item = order.getItem(demandModel.id);
      if (item == null) {
        ITransaction trans = JDOUtils.createInstance(ITransaction.class);
        trans.setDomainId(domainId);
        trans.setKioskId(kioskId);
        trans.setMaterialId(demandModel.id);
        trans.setQuantity(demandModel.q);
        trans.setType(ITransaction.TYPE_REORDER);
        trans.setTrackingId(String.valueOf(order.getOrderId()));
        trans.setSourceUserId(userId);
        trans.setTimestamp(now);
        transactions.add(trans);
      }

    }
    return transactions;
  }


  public IOrder buildOrderMaterials(IOrder order, List<DemandModel> items) {
    for (DemandModel model : items) {
      IDemandItem item = order.getItem(model.id);
      if (item != null) {
        if (BigUtil.notEquals(item.getQuantity(), model.q)) {
          item.setQuantity(model.q);
        }
        if (BigUtil.equals(model.q, model.oq)) {
          item.setOriginalQuantity(model.q);
          item.setShippedDiscrepancyReason(null);
        }
        if (BigUtil.notEquals(item.getDiscount(), model.d)) {
          item.setDiscount(model.d);
        }
        item.setShippedDiscrepancyReason(model.sdrsn);
        item.setReason(model.rsn);
      }
    }
    return order;
  }

  private class DemandBatchMeta {
    public BigDecimal quantity;
    List<ShipmentItemBatchModel> bd = new ArrayList<>();

    DemandBatchMeta(BigDecimal quantity) {
      this.quantity = quantity;
    }
  }
}
