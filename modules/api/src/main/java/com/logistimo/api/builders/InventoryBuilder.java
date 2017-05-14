package com.logistimo.api.builders;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.models.OptimizerConfig;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;
import com.logistimo.api.auth.Authoriser;
import com.logistimo.api.models.InventoryAbnStockModel;
import com.logistimo.api.models.InventoryBatchMaterialModel;
import com.logistimo.api.models.InventoryDomainModel;
import com.logistimo.api.models.InventoryMinMaxLogModel;
import com.logistimo.api.models.InventoryModel;
import com.logistimo.api.models.InvntryBatchModel;
import com.logistimo.api.util.CommonUtil;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.IInventoryMinMaxLog;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.IHandlingUnitService;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.HandlingUnitServiceImpl;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.OrderUtils;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;

public class InventoryBuilder {

  private static final XLog xLogger = XLog.getLog(InventoryBuilder.class);
  private IInvntryDao invDao = new InvntryDao();
  private ITagDao tagDao = new TagDao();

  public Results buildInventoryModelListAsResult(Results results, SecureUserDetails sUser,
                                                 Long domainId, Long entityId)
      throws ServiceException {
    List<InventoryModel> newInventory = getInventoryModelList(results, sUser, domainId, entityId);
    return new Results(newInventory, results.getCursor(), results.getNumFound(),
        results.getOffset());
  }

  public List<InventoryModel> getInventoryModelList(Results results, SecureUserDetails sUser,
                                                    Long domainId, Long entityId)
      throws ServiceException {
    if (results != null) {
      List inventory = results.getResults();
      List<InventoryModel> newInventory = new ArrayList<InventoryModel>(inventory.size());
      Map<Long, String> domainNames = new HashMap<>(1);
      int itemCount = results.getOffset() + 1;
      DomainConfig domainConfig = DomainConfig.getInstance(domainId);
      EntitiesService
          accountsService =
          Services.getService(EntitiesServiceImpl.class, sUser.getLocale());
      UsersService
          usersService =
          Services.getService(UsersServiceImpl.class, sUser.getLocale());
      MaterialCatalogService
          mCatalogService =
          Services.getService(MaterialCatalogServiceImpl.class, sUser.getLocale());
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, sUser.getLocale());
      IKiosk ki = null;
      if (entityId != null) {
        ki = accountsService.getKiosk(entityId, false);
      }
      for (Object invntry : inventory) {
        InventoryModel item = buildInventoryModel((IInvntry) invntry,
            domainConfig, accountsService,mCatalogService,usersService, ims, ki, sUser, itemCount, domainNames);
        if (item != null) {
          newInventory.add(item);
          itemCount++;
        }
      }
      return newInventory;
    }
    return null;
  }

  public InventoryModel buildInventoryModel(IInvntry invntry,
                                            DomainConfig domainConfig,
                                            EntitiesService accountsService,
                                            MaterialCatalogService mCatalogService,
                                            UsersService usersService,
                                            InventoryManagementService ims, IKiosk kiosk,
                                            SecureUserDetails sUser, int itemCount,
                                            Map<Long, String> domainNames) {
    InventoryModel model = new InventoryModel();
    model.sno = itemCount;
    model.invId = invntry.getKey();
    model.mId = invntry.getMaterialId();
    model.sdid = invntry.getDomainId();
    model.kId = invntry.getKioskId();
    try {
      model.updtBy = invntry.getUpdatedBy();
      model.fn = usersService.getUserAccount(model.updtBy).getFullName();
    } catch (Exception ignored) {
      // ignore
    }
    IMaterial material;
    try {
      material = mCatalogService.getMaterial(model.mId);
      DomainsService ds = Services.getService(DomainsServiceImpl.class);

      String domainName = domainNames.get(invntry.getDomainId());
      if (domainName == null) {
        IDomain domain = null;
        try {
          domain = ds.getDomain(invntry.getDomainId());

        } catch (Exception e) {
          xLogger.warn("Error while fetching Domain {0}", material.getDomainId());
        }
        if (domain != null) {
          domainName = domain.getName();
        } else {
          domainName = Constants.EMPTY;
        }
        domainNames.put(invntry.getDomainId(), domainName);
      }
      model.sdname = domainName;
    } catch (Exception e) {
      // CONTINUE: could not find item material
      return null;
    }
    if (kiosk == null) {
      try {
        kiosk = accountsService.getKiosk(model.kId);
      } catch (ServiceException e) {
        xLogger.warn("Kiosk associated with material in inventory not found", e);
        return null;
      }
    }
    model.enm = kiosk != null ? kiosk.getName() : invntry.getKioskName();
    if (kiosk != null) {
      model.lt = kiosk.getLatitude();
      model.ln = kiosk.getLongitude();
      model.add = CommonUtil.getAddress(kiosk.getCity(), kiosk.getTaluk(), kiosk.getDistrict(), kiosk.getState());
    }
    model.mnm = material.getName();
    model.b = material.getType();
    model.t = LocalDateUtil.format(invntry.getTimestamp(), sUser.getLocale(), sUser.getTimezone());
    String empty = "";
    IInvntryEvntLog lastEventLog = invDao.getInvntryEvntLog(invntry);

    if (lastEventLog != null) {
      model.eventType = lastEventLog.getType();
      if (invntry.getStockEvent() != -1) {
        model.period = new Date().getTime() - lastEventLog.getStartDate().getTime();
      }
      model.event = invntry.getStockEvent();
    }
    model.stk = invntry.getStock();
    model.ldtdmd = invntry.getLeadTimeDemand();
    model.ldt = invntry.getLeadTime();
    model.max = invntry.getMaxStock();
    model.ordp = invntry.getOrderPeriodicity();
    model.reord = invntry.getNormalizedSafetyStock();
    model.rvpdmd = invntry.getRevPeriodDemand();
    model.sfstk = invntry.getSafetyStock();
    model.stdv = invntry.getStdevRevPeriodDemand();
    model.tgs = invntry.getTags(TagUtil.TYPE_MATERIAL);

    model.rp =
        BigUtil.equalsZero(invntry.getRetailerPrice()) ? material.getRetailerPrice()
            : invntry.getRetailerPrice();

    model.tx =
        BigUtil.notEqualsZero(invntry.getTax()) ? invntry.getTax()
            : kiosk != null ? kiosk.getTax() : BigDecimal.ZERO;

    model.enOL = OrderUtils.isReorderAllowed(invntry.getInventoryModel());
    if (material.isTemperatureSensitive()) {
      model.tmin = material.getTemperatureMin();
      model.tmax = material.getTemperatureMax();
    }

    InventoryConfig ic = domainConfig.getInventoryConfig();
    if (ic != null) {
      if (InventoryConfig.CR_MANUAL == ic.getConsumptionRate()) {
        model.crd =
            ims.getDailyConsumptionRate(invntry, ic.getConsumptionRate(), ic.getManualCRFreq());
        model.crw = model.crd.multiply(Constants.WEEKLY_COMPUTATION);
        model.crm = model.crd.multiply(Constants.MONTHLY_COMPUTATION);
        model.crMnl = invntry.getConsumptionRateManual();
      } else if (InventoryConfig.CR_AUTOMATIC == ic.getConsumptionRate()) {
        model.crd = invntry.getConsumptionRateDaily();
        model.crw = invntry.getConsumptionRateWeekly();
        model.crm = invntry.getConsumptionRateMonthly();
      }
      if (Constants.FREQ_DAILY.equals(ic.getDisplayCRFreq())) {
        model.sap = ims.getStockAvailabilityPeriod(model.crd, invntry.getStock());
      } else if (Constants.FREQ_WEEKLY.equals(ic.getDisplayCRFreq())) {
        model.sap = ims.getStockAvailabilityPeriod(model.crw, invntry.getStock());
      } else if (Constants.FREQ_MONTHLY.equals(ic.getDisplayCRFreq())) {
        model.sap = ims.getStockAvailabilityPeriod(model.crm, invntry.getStock());
      }
    }
    if (kiosk != null && kiosk.isOptimizationOn()) {
      model.im = invntry.getInventoryModel();
      model.sl = invntry.getServiceLevel();
    }

    if (kiosk != null) {
      model.be = kiosk.isBatchMgmtEnabled() && material.isBatchEnabled();
    }
    model.ts = material.isTemperatureSensitive();

    model.eoq = invntry.getEconomicOrderQuantity();
    model.omsg = invntry.getOptMessage();
    model.pst =
        invntry.getPSTimestamp() != null ? LocalDateUtil
            .format(invntry.getPSTimestamp(), sUser.getLocale(), sUser.getTimezone()) : empty;
    model.dqt =
        invntry.getDQTimestamp() != null ? LocalDateUtil
            .format(invntry.getDQTimestamp(), sUser.getLocale(), sUser.getTimezone()) : empty;

    // Parameter update times, if any
    model.reordT =
        (invntry.getReorderLevelUpdatedTime() == null ? null : LocalDateUtil
            .format(invntry.getReorderLevelUpdatedTime(), sUser.getLocale(), sUser.getTimezone()));
    model.maxT =
        (invntry.getMaxUpdatedTime() == null ? null : LocalDateUtil
            .format(invntry.getMaxUpdatedTime(), sUser.getLocale(), sUser.getTimezone()));
    model.crMnlT =
        (invntry.getMnlConsumptionRateUpdatedTime() == null ? null : LocalDateUtil
            .format(invntry.getMnlConsumptionRateUpdatedTime(), sUser.getLocale(),
                sUser.getTimezone()));
    model.rpT =
        (invntry.getRetailerPriceUpdatedTime() == null ? null : LocalDateUtil
            .format(invntry.getRetailerPriceUpdatedTime(), sUser.getLocale(), sUser.getTimezone()));
    model.pdos = invntry.getPredictedDaysOfStock();

    try {
      IHandlingUnitService hus = Services.getService(HandlingUnitServiceImpl.class);
      Map<String, String> hu = hus.getHandlingUnitDataByMaterialId(invntry.getMaterialId());
      if (hu != null) {
        model.huQty = new BigDecimal(hu.get(IHandlingUnit.QUANTITY));
        model.huName = hu.get(IHandlingUnit.NAME);
      }
    } catch (Exception e) {
      xLogger.warn("Error while fetching Handling Unit {0}", material.getMaterialId(), e);
    }
    model.minDur = invntry.getMinDuration();
    model.maxDur = invntry.getMaxDuration();
    model.atpstk = invntry.getAvailableStock();
    model.tstk = invntry.getInTransitStock();
    model.astk = invntry.getAllocatedStock();
    return model;
  }

  public IInvntry buildInvntry(Long domainId, String kioskName, Long kioskId, InventoryModel invm,
                               IInvntry in, String user, boolean isDurationOfStock,
                               boolean isManual, String minMaxDur, InventoryManagementService ims) {
    in.setDomainId(domainId);
    in.setKioskId(kioskId);
    in.setKioskName(kioskName);
    in.setMaterialId(invm.mId);
    in.setMaterialName(invm.mnm);
    in.setBinaryValued(invm.b);
    in.setKioskName(invm.enm);
    if (isDurationOfStock) {
      in.setMinDuration(invm.minDur);
      in.setMaxDuration(invm.maxDur);
    } else {
      in.setMinDuration(null);
      in.setMaxDuration(null);
      in.setReorderLevel(invm.reord);
      in.setMaxStock(invm.max);
    }
    in.setUpdatedBy(user);
    if (invm.crMnl != null) {
      in.setConsumptionRateManual(invm.crMnl);
    } else {
      in.setConsumptionRateManual(BigDecimal.ZERO);
    }
    if (isManual && isDurationOfStock) {
      BigDecimal cr = ims.getDailyConsumptionRate(in);
      BigDecimal mul = BigDecimal.ONE;
      if (Constants.FREQ_WEEKLY.equals(minMaxDur)) {
        mul = Constants.WEEKLY_COMPUTATION;
      } else if (Constants.FREQ_MONTHLY.equals(minMaxDur)) {
        mul = Constants.MONTHLY_COMPUTATION;
      }
      in.setReorderLevel(invm.minDur.multiply(mul).multiply(cr));
      in.setMaxStock(invm.maxDur.multiply(mul).multiply(cr));
    }
    in.setRetailerPrice(invm.rp);
    in.setTax(invm.tx);
    //in.setKey(Invntry.createKey(kioskId, invm.mId));
    if ("yes".equals(invm.b)) {
      in.setStock(BigDecimal.ONE);
    } else {
      in.setStock(invm.stk);
    }
    in.setTmin(invm.tmin);
    in.setTmax(invm.tmax);
    in.setInventoryModel(invm.im);
    in.setServiceLevel(invm.sl);
    return in;
  }

  public List<InventoryAbnStockModel> buildAbnormalStockModelList(List<IInvntryEvntLog> evntLogs,
                                                                  Locale locale, String timezone) {
    List<InventoryAbnStockModel> modelList = new ArrayList<InventoryAbnStockModel>(evntLogs.size());
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Map<Long, String> domainNames = new HashMap<>(1);
    try {
      for (IInvntryEvntLog evntLog : evntLogs) {
        InventoryAbnStockModel
            model =
            buildAbnormalStockModel(evntLog, pm, locale, timezone, domainNames);
        if (model != null) {
          modelList.add(model);
        }
      }
    } finally {
      pm.close();
    }
    return modelList;
  }

  public InventoryAbnStockModel buildAbnormalStockModel(IInvntryEvntLog evntLog,
                                                        PersistenceManager pm, Locale locale,
                                                        String timezone,
                                                        Map<Long, String> domainNames) {
    InventoryAbnStockModel model = new InventoryAbnStockModel();
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
      MaterialCatalogServiceImpl
          materialCatalogService =
          Services.getService(MaterialCatalogServiceImpl.class, locale);
      IKiosk k = null;
      IMaterial m = null;
      try {
        k = as.getKiosk(evntLog.getKioskId());
      } catch (Exception e) {
        xLogger.warn("Unable to fetch the kiosk details for " + evntLog.getKioskId());
        return null;
      }
      try {
        m = materialCatalogService.getMaterial(evntLog.getMaterialId());
      } catch (Exception e) {
        xLogger.warn("Unable to fetch the material details for " + evntLog.getMaterialId());
        return null;
      }
      model.enm = k.getName();
      model.add = CommonUtil.getAddress(k.getCity(), k.getTaluk(), k.getDistrict(), k.getState());
      model.mnm = m.getName();
      IInvntry invntry = invDao.getInvntry(evntLog);
      model.st = invntry.getStock();
      model.min = invntry.getNormalizedSafetyStock();
      model.max = invntry.getMaxStock();
      Date eDt = evntLog.getEndDate() == null ? new Date() : evntLog.getEndDate();
      model.du = eDt.getTime() - evntLog.getStartDate().getTime();
      model.stDt = evntLog.getStartDate();
      model.edDt = eDt;
      model.sdid = invntry.getDomainId();
      String domainName = domainNames.get(invntry.getDomainId());
      if (domainName == null) {
        IDomain domain = null;
        try {
          domain = ds.getDomain(invntry.getDomainId());
        } catch (Exception e) {
          xLogger.fine("Unable to fetch the domain details for domain " + invntry.getDomainId());
        }
        if (domain != null) {
          domainName = domain.getName();
        } else {
          domainName = Constants.EMPTY;
        }
        domainNames.put(invntry.getDomainId(), domainName);
      }
      model.sdname = domainName;
      if (model.stDt != null) {
        model.stDtstr = LocalDateUtil.format(evntLog.getStartDate(), locale, timezone);
      }
      model.edDtstr =
          evntLog.getEndDate() != null ? LocalDateUtil.format(eDt, locale, timezone) : "Now";
      model.kid = evntLog.getKioskId();
      model.mid = evntLog.getMaterialId();
      model.type = evntLog.getType();
      model.minDur = invntry.getMinDuration();
      model.maxDur = invntry.getMaxDuration();
    } catch (Exception e) {
      xLogger.warn("Failed to stock event to abnormal stock list: {0}", e);
      model = null;
    }
    return model;
  }

  public String trimReasons(String reasonsCSV) {
    String csv = reasonsCSV;
    if (csv != null) {
      csv = csv.trim();
      if (csv.isEmpty()) {
        csv = null;
      } else {
        // Compact spaces between reasons
        csv = StringUtil.getCSV(StringUtil.trim(StringUtil.getArray(csv)));
      }
    }
    if (csv == null) {
      csv = "";
    }
    return csv;
  }

  public InventoryDomainModel buildInventoryDomainModel(HttpServletRequest request, String userId,
                                                        Locale locale, IKiosk kiosk) {
    boolean optimizationOn = kiosk != null ? kiosk.isOptimizationOn() : true;

    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    OptimizerConfig oc = dc.getOptimizerConfig();
    boolean isDemandForecast = (oc.getCompute() == OptimizerConfig.COMPUTE_FORECASTEDDEMAND);
    boolean isEOQ = (oc.getCompute() == OptimizerConfig.COMPUTE_EOQ);
    optimizationOn = (optimizationOn && isEOQ);
    boolean allowDisplayConsumptionRates = (ic != null && ic.displayCR());
    String
        crUnits =
        (allowDisplayConsumptionRates ? InventoryConfig
            .getFrequencyDisplay(ic.getDisplayCRFreq(), false, locale) : null);

    InventoryDomainModel model = new InventoryDomainModel();
    model.cr = allowDisplayConsumptionRates;
    model.cu = crUnits;
    model.ieoq = isEOQ;
    model.idf = isDemandForecast;
    model.ioon = optimizationOn;
    model.cur = kiosk != null ? kiosk.getCurrency() : null;
    return model;
  }

  public List<InventoryBatchMaterialModel> buildInventoryBatchMaterialModels(int offset,
                                                                             Locale locale,
                                                                             String timezone,
                                                                             EntitiesService as,
                                                                             MaterialCatalogService mc,
                                                                             List<IInvntryBatch> inventory,
                                                                             List<IKiosk> myKiosks) {
    List<InventoryBatchMaterialModel> models = new ArrayList<InventoryBatchMaterialModel>(0);
    if (inventory != null && inventory.size() > 0) {
      models = new ArrayList<InventoryBatchMaterialModel>(inventory.size());
      Map<Long, String> domainNames = new HashMap<>(1);
      int slno = offset;
      for (IInvntryBatch invBatch : inventory) {
        Long kioskID = invBatch.getKioskId();
        String domainName = domainNames.get(invBatch.getDomainId());
        IKiosk k;
        IMaterial thisMaterial;
        try {
          k = as.getKiosk(kioskID, false);
          DomainsService ds = Services.getService(DomainsServiceImpl.class);
          if (domainName == null) {
            IDomain domain = null;
            try {
              domain = ds.getDomain(invBatch.getDomainId());
            } catch (Exception e) {
              xLogger.warn("Error while fetching Domain {0}", invBatch.getDomainId());
            }
            if (domain != null) {
              domainName = domain.getName();
            } else {
              domainName = Constants.EMPTY;
            }
            domainNames.put(invBatch.getDomainId(), domainName);
          }

          if (myKiosks != null && !myKiosks.contains(k)) {
            continue;
          }
          thisMaterial = mc.getMaterial(invBatch.getMaterialId());
        } catch (Exception e) {
          continue;
        }
        Date timestamp = invBatch.getTimestamp();
        BigDecimal stock = invBatch.getQuantity();
        InventoryBatchMaterialModel mod = new InventoryBatchMaterialModel();
        mod.slno = slno + 1;
        mod.mat = thisMaterial.getName();
        mod.mId = thisMaterial.getMaterialId();
        mod.ent = k.getName();
        mod.eid = k.getKioskId();
        mod.add = CommonUtil.getAddress(k.getCity(), k.getTaluk(), k.getDistrict(), k.getState());
        mod.bat = invBatch.getBatchId();
        mod.exp =
            invBatch.getBatchExpiry() != null ? LocalDateUtil
                .format(invBatch.getBatchExpiry(), locale, timezone, true) : "";
        mod.manr = invBatch.getBatchManufacturer() != null ? invBatch.getBatchManufacturer() : "";
        mod.mand =
            invBatch.getBatchManufacturedDate() != null ? LocalDateUtil
                .format(invBatch.getBatchManufacturedDate(), locale, timezone, true) : "";
        mod.cst = BigUtil.getFormattedValue(stock);
        mod.lup = LocalDateUtil.format(timestamp, locale, timezone);
        mod.sdid = invBatch.getDomainId();
        mod.sdname = domainName;
        models.add(mod);
        slno++;
      }
    }
    return models;
  }

  public List<InvntryBatchModel> buildInvntryBatchModel(Results results, boolean allBatches,
                                                        SecureUserDetails sUser, Long allocOrderId)
      throws ServiceException {
    if (results.getResults() != null) {
      List<InvntryBatchModel> modelList = new ArrayList<>();
      List<IInvntryBatch> iInvntryBatches = results.getResults();
      Integer hasPerm = 0;
      Long prevKId = null;
      List<IInvAllocation> allocations;
      Map<String, BigDecimal> orderAllocations = null;
      if (allocOrderId != null) {
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class);
        allocations =
            ims.getAllocationsByTypeId(null, null, IInvAllocation.Type.ORDER,
                String.valueOf(allocOrderId));
        if (allocations != null && allocations.size() > 0) {
          orderAllocations = new HashMap<>();
          for (IInvAllocation allocation : allocations) {
            if (allocation.getBatchId() == null) {
              continue;
            }
            orderAllocations.put(allocation.getBatchId(), allocation.getQuantity());
          }
        }
      }
      for (IInvntryBatch batch : iInvntryBatches) {
        if (BigUtil.greaterThanZero(batch.getQuantity()) && (allBatches || !batch.isExpired())) {
          InvntryBatchModel model = new InvntryBatchModel();
          model.dId = batch.getDomainIds();
          model.sdId = batch.getDomainId();
          model.kId = batch.getKioskId();
          model.mId = batch.getMaterialId();
          model.q = batch.getQuantity();
          model.bid = batch.getBatchId();
          model.bexp = batch.getBatchExpiry();
          model.bmfnm = batch.getBatchManufacturer();
          model.bmfdt = batch.getBatchManufacturedDate();
          model.t = batch.getTimestamp();
          model.mtgs = batch.getTags(TagUtil.TYPE_MATERIAL);
          model.ktgs = batch.getTags(TagUtil.TYPE_ENTITY);
          model.vld = batch.getVld();
          model.isExp = batch.isExpired();
          model.astk = batch.getAllocatedStock();
          model.atpstk = batch.getAvailableStock();
          if (model.kId.equals(prevKId)) {
            model.perm = hasPerm;
          } else if (sUser != null) {
            try {
              model.perm =
                  Authoriser
                      .authoriseEntityPerm(batch.getKioskId(), sUser.getRole(), sUser.getLocale(),
                          sUser.getUsername(), sUser.getDomainId());
            } catch (ServiceException e) {
              model.perm = 0;
            }
            hasPerm = model.perm;
          }
          if (orderAllocations != null && orderAllocations.containsKey(model.bid)) {
            model.oastk = orderAllocations.get(model.bid);
          }
          modelList.add(model);
          prevKId = model.kId;
        }
      }
      return modelList;
    }
    return null;
  }

  public List<InventoryMinMaxLogModel> buildInventoryMinMaxLogModel(List<IInventoryMinMaxLog> logs,
                                                                    SecureUserDetails sUser,
                                                                    ResourceBundle backendMessages) {
    List<InventoryMinMaxLogModel> models = null;
    if (logs != null) {
      models = new ArrayList<>(logs.size());
      for (IInventoryMinMaxLog invLog : logs) {
        InventoryMinMaxLogModel invModel = new InventoryMinMaxLogModel();
        invModel.invkey = invLog.getInventoryId();
        invModel.cr = invLog.getConsumptionRate();
        invModel.kid = invLog.getKioskId();
        invModel.mid = invLog.getMaterialId();
        invModel.min = invLog.getMin();
        invModel.max = invLog.getMax();
        invModel.minDur = invLog.getMinDuration();
        invModel.maxDur = invLog.getMaxDuration();
        invModel.t =
            LocalDateUtil.format(invLog.getCreatedTime(), sUser.getLocale(), sUser.getTimezone());
        invModel.freq =
            IInventoryMinMaxLog.Frequency.getDisplayFrequency(invLog.getMinMaxFrequency());
        if (invLog.getSource() != null && invLog.getSource() == 0) {
          invModel.source = "u";
          try {
            UsersService accountsService = Services.getService(UsersServiceImpl.class);
            IUserAccount account = accountsService.getUserAccount(invLog.getUser());
            if (account != null) {
              invModel.uid = account.getUserId();
              invModel.username = account.getFullName();
            }
          } catch (Exception e) {
            xLogger.warn("Error in fetching user details for {0}", invLog.getUser(), e);
          }
        } else {
          invModel.source = "s";
        }
        models.add(invModel);
      }
    }
    return models;
  }
}
