package com.logistimo.api.controllers;

import com.logistimo.pagination.Navigator;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.logger.XLog;

import com.logistimo.accounting.entity.IAccount;
import com.logistimo.accounting.service.IAccountingService;
import com.logistimo.accounting.service.impl.AccountingServiceImpl;
import com.logistimo.api.builders.AccountBuilder;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.api.models.AccountModel;
import com.logistimo.api.auth.Authoriser;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mohan raja on 03/12/14
 */
@Controller
@RequestMapping("/accounts")
public class AccountsController {
  private static final XLog xLogger = XLog.getLog(AccountsController.class);
  AccountBuilder builder = new AccountBuilder();

  @RequestMapping("/")
  public
  @ResponseBody
  Results getAccounts(
      @RequestParam String kioskId, @RequestParam String type, @RequestParam String yr,
      @RequestParam String sb,
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = null;
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    Navigator
        navigator =
        new Navigator(request.getSession(), "AccountsController.getAccounts", offset, size,
            "act" + type, 0);
    PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
    backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      IAccountingService
          oms =
          Services.getService(AccountingServiceImpl.class, locale);
      Results results;
      if (Authoriser.authoriseEntity(request, Long.valueOf(kioskId))) {
        if (IAccount.RECEIVABLE.equals(type)) {
          results =
              oms.getAccounts(Long.valueOf(kioskId), null, Integer.valueOf(yr), sb, pageParams);
        } else {
          results =
              oms.getAccounts(null, Long.valueOf(kioskId), Integer.valueOf(yr), sb, pageParams);
        }
      } else {
        throw new UnauthorizedException(backendMessages.getString("permission.denied"));
      }
      navigator.setResultParams(results);
      results.setNumFound(-1);
      List<AccountModel>
          models =
          builder.buildAccountModelList(results.getResults(), type, locale, Long.parseLong(kioskId),
              domainId);
      return new Results(models, results.getCursor(), results.getNumFound(), results.getOffset());
    } catch (ServiceException e) {
      xLogger.severe("Error in getting accounts details");
      throw new InvalidServiceException(backendMessages.getString("account.error"));
    }
  }

  @RequestMapping("/config")
  public
  @ResponseBody
  AccountModel getAccountsConfig() {
    return builder.buildAccountConfigModel();
  }
}
