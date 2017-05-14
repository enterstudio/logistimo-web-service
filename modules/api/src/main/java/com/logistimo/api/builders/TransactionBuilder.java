package com.logistimo.api.builders;

import org.apache.commons.lang.StringUtils;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;
import com.logistimo.api.models.TransactionModel;
import com.logistimo.api.util.CommonUtil;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionBuilder {
  private static final XLog xLogger = XLog.getLog(TransactionBuilder.class);

  public Results buildTransactions(Results trnResults, SecureUserDetails user, Long domainId)
      throws ServiceException {
    List transactions = trnResults.getResults();
    List<TransactionModel> finalTransactions = new ArrayList<>(
        transactions.size());
    EntitiesService as = Services.getService(
        EntitiesServiceImpl.class, user.getLocale());
    UsersService us = Services.getService(
        UsersServiceImpl.class, user.getLocale());
    MaterialCatalogService mc = Services.getService(
        MaterialCatalogServiceImpl.class, user.getLocale());
    DomainConfig dc = DomainConfig.getInstance(domainId);
    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    Map<Long, String> domainNames = new HashMap<>(1);
    int itemCount = trnResults.getOffset() + 1;
    for (Object trn : transactions) {
      TransactionModel model = buildTransaction((ITransaction) trn, as, us,
          mc, dc, user.getLocale(), user.getTimezone(), itemCount, ds, domainNames);
      if (model != null) {
        finalTransactions.add(model);
        itemCount++;
      }
    }
    Results finalResults = new Results(finalTransactions,
        trnResults.getCursor(), trnResults.getNumFound(),
        trnResults.getOffset());
    return finalResults;
  }

  public TransactionModel buildTransaction(ITransaction trn,
                                           EntitiesService accountsService,
                                           UsersService usersService,
                                           MaterialCatalogService materialCatalogService,
                                           DomainConfig domainConfig, Locale locale,
                                           String timezone, int itemCount, DomainsService ds,
                                           Map<Long, String> domainNames) {
    TransactionModel model = new TransactionModel();
    model.mid = trn.getMaterialId();
    model.sno = itemCount;
    IMaterial material = null;
    try {
      material = materialCatalogService.getMaterial(trn.getMaterialId());
    } catch (ServiceException e) {
      // Skip material if not found
      return null;
    }
    model.mnm = material.getName();
    model.mtgs = material.getTags();
    model.ty = trn.getType();
    model.type = TransactionUtil.getDisplayName(trn.getType(),
        domainConfig.getTransactionNaming(), locale);
    model.id = trn.getKeyString();
    model.lkId = trn.getLinkedKioskId();
    if (model.lkId != null) {
      try {
        IKiosk k1 = accountsService.getKiosk(model.lkId, false);
        model.lknm = k1.getName();
        model.lklt = k1.getLatitude();
        model.lkln = k1.getLongitude();
        model.lkadd = CommonUtil.getAddress(k1.getCity(),k1.getTaluk(), k1.getDistrict(), k1.getState());
      } catch (ServiceException e) {
        // Ignore .. Linked KioskId might have been deleted.
      }
    }
    model.eid = trn.getKioskId();
    if (model.lkId == null || (model.lklt == 0 && model.lkln == 0)) {
      try {
        IKiosk k = accountsService.getKiosk(model.eid, false);
        model.lklt = k.getLatitude();
        model.lkln = k.getLongitude();
        model.enMap = true;
      } catch (ServiceException e) {
        //Ignore
      }
    }
    try {
      IKiosk k = accountsService.getKiosk(model.eid, false);
      model.enm = (model.eid != null ? k.getName() : "");
      model.eadd = CommonUtil.getAddress(k.getCity(),k.getTaluk(), k.getDistrict(), k.getState());
    } catch (ServiceException e) {
      // Ignore .. KioskId might have been deleted.
      return null;
    }
    model.resn = trn.getReason();
    model.mst = trn.getMaterialStatus();
    model.cs = trn.getClosingStock();
    model.os = trn.getOpeningStock();
    if (trn.getBatchId() != null) {
      model.bexp =
          trn.getBatchExpiry() != null ? LocalDateUtil
              .formatCustom(trn.getBatchExpiry(), "dd/MM/yyyy", null) : "";
      model.csb = trn.getClosingStockByBatch();
      model.osb = trn.getOpeningStockByBatch();
      model.bid = trn.getBatchId();
      model.bmfdt =
          trn.getBatchManufacturedDate() != null ? LocalDateUtil
              .formatCustom(trn.getBatchManufacturedDate(),
                  "dd/MM/yyyy", null) : "";
      model.bmfnm = trn.getBatchManufacturer();
    }
    model.ln = trn.getLongitude();
    model.lt = trn.getLatitude();
    model.ac = trn.getGeoAccuracy();
    model.q = trn.getQuantity();
    model.ts = LocalDateUtil.format(trn.getTimestamp(), locale, timezone);
    model.uid = trn.getSourceUserId();
    model.sdid = trn.getDomainId();
    String domainName = domainNames.get(trn.getDomainId());
    if (domainName == null) {
      IDomain domain = null;
      try {
        domain = ds.getDomain(trn.getDomainId());
      } catch (Exception e) {
        xLogger.warn("Error while fetching Domain {0}", trn.getDomainId());
      }

      if (domain != null) {
        domainName = domain.getName();
      } else {
        domainName = Constants.EMPTY;
      }
      domainNames.put(trn.getDomainId(), domainName);
    }
    model.sdname = domainName;
    if (StringUtils.isNotBlank(model.uid)) {
      try {
        model.unm = usersService.getUserAccount(model.uid).getFullName();
      } catch (Exception ignored) {
        // ignore
      }
    }
    model.trkid = trn.getTrackingId();
    model.trkObTy = trn.getTrackingObjectType();
    model.trnId = trn.getTransactionId();
    model.trnSrc = trn.getSrc();
    if (trn.getAtd() != null) {
      model.atd = LocalDateUtil.formatCustom(trn.getAtd(), Constants.DATE_FORMAT, null);
    }
    return model;
  }


}
