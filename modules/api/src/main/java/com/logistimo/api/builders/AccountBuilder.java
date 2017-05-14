package com.logistimo.api.builders;

import com.logistimo.accounting.entity.IAccount;
import com.logistimo.dao.JDOUtils;

import org.apache.commons.lang.StringUtils;
import com.logistimo.config.models.AccountingConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.CommonUtils;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.models.AccountModel;
import com.logistimo.api.util.CommonUtil;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by mohan raja on 03/12/14
 */
public class AccountBuilder {
  // Get the accounting years
  public static int[] getAccountingYears() {
    // Get currenty year
    int curYear = LocalDateUtil.getCurrentYear();
    int numYears = curYear - IAccount.START_YEAR;
    int[] years = null;
    if (numYears == 0) {
      years = new int[1];
      years[0] = curYear;
    } else {
      years = new int[numYears];
      for (int i = 0; i < numYears; i++) {
        years[i] = curYear;
        curYear--;
      }
    }
    return years;
  }

  public AccountModel buildAccountConfigModel() {
    AccountModel model = new AccountModel();
    model.years = getAccountingYears();
    model.curyear = LocalDateUtil.getCurrentYear();
    return model;
  }

  public List<AccountModel> buildAccountModelList(List<IAccount> accounts, String type,
                                                  Locale locale, Long kioskId, Long domainId)
      throws ServiceException {
    List<AccountModel> models = new ArrayList<AccountModel>(accounts.size());
    int sno = 1;
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    for (IAccount account : accounts) {
      AccountModel model = buildAccountModel(account, type, locale, as, kioskId, dc);
      model.sno = sno++;
      models.add(model);
    }
    return models;
  }

  public AccountModel buildAccountModel(IAccount account, String type, Locale locale,
                                        EntitiesService as, Long kioskId, DomainConfig dc)
      throws ServiceException {
    AccountModel model = new AccountModel();
    String linkId = null;
    if (IAccount.RECEIVABLE.equals(type)) {
      model.name = account.getCustomerName();
      if (!kioskId.equals(account.getCustomerId())) {
        linkId =
            JDOUtils.createKioskLinkId(kioskId, IKioskLink.TYPE_CUSTOMER, account.getCustomerId());
      }
    } else {
      model.name = account.getVendorName();
      if (!kioskId.equals(account.getVendorId())) {
        linkId =
            JDOUtils.createKioskLinkId(account.getVendorId(), IKioskLink.TYPE_CUSTOMER, kioskId);
      }
    }
    if (as != null) {
      IKiosk k = as.getKiosk(kioskId, false);
      model.cur = k.getCurrency();
      model.add = CommonUtil.getAddress(k.getCity(),k.getTaluk(), k.getDistrict(), k.getState());
      if (StringUtils.isBlank(model.cur)) {
        model.cur = dc.getCurrency();
      }
    }
    model.npay = CommonUtils.getFormattedPrice(account.getPayable());
    IKioskLink kl = null;
    try {
      if (linkId != null) {
        if (as == null) {
          as = Services.getService(EntitiesServiceImpl.class, locale);
        }
        kl = as.getKioskLink(linkId);
      }
    } catch (Exception e) {
      System.out.println("accounts.jsp: " + e.getClass().getName() + ": " + e.getMessage());
    }
    BigDecimal creditLimit = BigDecimal.ZERO;
    if (kl != null) {
      creditLimit = kl.getCreditLimit();
    }
    if (BigUtil.equalsZero(creditLimit)) {
      AccountingConfig ac = dc.getAccountingConfig();
      BigDecimal defaultCreditLimit = BigDecimal.ZERO;
      if (ac != null) {
        defaultCreditLimit = ac.getCreditLimit();
      }
      creditLimit = defaultCreditLimit;
    }
    model.bal = CommonUtils.getFormattedPrice(creditLimit.subtract(account.getPayable()));
    return model;
  }

}
