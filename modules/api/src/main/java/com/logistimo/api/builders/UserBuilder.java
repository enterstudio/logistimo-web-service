package com.logistimo.api.builders;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.users.UserUtils;
import com.logistimo.users.entity.IUserAccount;

import org.apache.commons.lang.StringUtils;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.api.models.UserModel;
import com.logistimo.models.superdomains.DomainSuggestionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserBuilder {
  private static final XLog xLogger = XLog.getLog(UserBuilder.class);

  public List<UserModel> buildUserModels(List users, Locale locale, String timeZone,
                                         boolean isPartial, int offset) {
    List<UserModel> models = null;
    if (users != null) {
      models = new ArrayList<UserModel>(users.size());
      for (Object user : users) {
        UserModel model = buildSimpleUserModel((IUserAccount) user, locale, timeZone, isPartial);
        models.add(model);
      }
    }
    return models;
  }

  public UserModel buildSimpleUserModel(IUserAccount user, Locale locale, String timeZone,
                                        boolean isPartial) {
    UserModel model = new UserModel();
    String domainName;
    model.id = user.getUserId();
    model.phm = user.getMobilePhoneNumber();
    model.fnm = user.getFullName();
    model.atexp = user.getAuthenticationTokenExpiry();
    model.tgs = user.getTags();
    if (!isPartial) {
      model.ro = UserUtils.getRoleDisplay(user.getRole(), locale);
      model.phl = user.getLandPhoneNumber();
      model.ct = user.getCity();
      model.cnt = user.getCountry();
      model.st = user.getState();
      model.ll = user.getLastLogin();
      model.lr = user.getLastMobileAccessed();
      if (model.ll != null) {
        model.llStr = LocalDateUtil.format(user.getLastLogin(), locale, timeZone);
      }
      if (model.lr != null) {
        model.lrStr = LocalDateUtil.format(user.getLastMobileAccessed(), locale, timeZone);
      }
      model.en = user.isEnabled();
      model.lgSrc = user.getLoginSource();
    }
    model.sdid = user.getDomainId();
    IDomain domain;
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      domain = ds.getDomain(user.getDomainId());
      if (domain != null) {
        model.sdname = domain.getName();
      } else {
        model.sdname = Constants.EMPTY;
      }
    } catch (Exception e) {
      xLogger.fine("Unable to fetch the domain details" + e);
    }

    return model;
  }

  public List<IUserAccount> buildUserAccounts(List<UserModel> users, Locale locale, String timezone,
                                              boolean isPartial) {
    List<IUserAccount> accounts = null;
    if (users.size() != 0) {
      accounts = new ArrayList<IUserAccount>(users.size());
      for (Object user : users) {
        IUserAccount userAccount = buildUserAccount((UserModel) user, isPartial);
        accounts.add(userAccount);
      }
    }
    return accounts;
  }

  public IUserAccount buildUserAccount(UserModel model, boolean isPartial) {
    IUserAccount user = JDOUtils.createInstance(IUserAccount.class);
    user.setUserId(model.id);
    user.setFirstName(model.fnm);
    user.setLastName(model.lnm);
    if (!isPartial) {
      user.setRole(model.ro);
      user.setMobilePhoneNumber(model.phm);
      user.setLandPhoneNumber(model.phl);
      user.setCity(model.ct);
      user.setCountry(model.cnt);
      user.setState(model.st);
      user.setLastLogin(model.ll);
      user.setLastMobileAccessed(model.lr);
    }
    return user;
  }

  public UserModel buildUserModel(IUserAccount account, IUserAccount rb, IUserAccount lu,
                                  Locale locale, String timeZone, boolean Messsage) {
    UserModel model = buildUserModel(account, locale, timeZone, Messsage, null);
    if (rb != null) {
      model.regByn = rb.getFullName();
    }
    if (lu != null) {
      model.updaByn = lu.getFullName();
    }
    return model;
  }

  public UserModel buildUserModel(IUserAccount account, Locale locale, String timeZone,
                                  boolean isMessage, List<IKiosk> kiosks) {
    UserModel model = new UserModel();
    model.id = account.getUserId();
    model.fnm = account.getFullName();
    model.phm = account.getMobilePhoneNumber();
    model.em = account.getEmail();
    model.tgs = account.getTags();

    if (!isMessage) {
      //model.pw = account.getEncodedPassword();
      model.ro = account.getRole();
      model.cid = account.getCustomId();

      model.fnm = account.getFirstName();
      model.lnm = account.getLastName();
      model.gen = account.getGender();
      model.age = account.getAge();

      model.phl = account.getLandPhoneNumber();

      model.cnt = account.getCountry();
      model.st = account.getState();
      model.ds = account.getDistrict();
      model.tlk = account.getTaluk();
      model.ct = account.getCity();
      model.stn = account.getStreet();
      model.pin = account.getPinCode();

      model.lng = account.getLanguage();
      model.tz = account.getTimezone();
      model.ll = account.getLastLogin();
      model.lr = account.getLastMobileAccessed();
      if (model.ll != null) {
        model.llStr = LocalDateUtil.format(account.getLastLogin(), locale, timeZone);
      }
      if (model.lr != null) {
        model.lrStr = LocalDateUtil.format(account.getLastMobileAccessed(), locale, timeZone);
      }
      model.en = account.isEnabled();
      model.ms = account.getMemberSince();
      model.ms = account.getMemberSince();
      model.ua = account.getUserAgent();
      model.lgSrc = account.getLoginSource();

      model.updaBy = account.getUpdatedBy();
      if (account.getUpdatedOn() != null) {
        model.updaOn = LocalDateUtil.format(account.getUpdatedOn(), locale, timeZone);
      }
      model.regBy = account.getRegisteredBy();
      model.pua = account.getPreviousUserAgent();
      model.ip = account.getIPAddress();
      model.av = account.getAppVersion();
      model.br = account.getPhoneBrand();
      model.mo = account.getPhoneModelNumber();
      model.sId = account.getSimId();
      model.imei = account.getImei();
      model.sp = account.getPhoneServiceProvider();
      if (account.getPrimaryKiosk() != null) {
        model.pk = account.getPrimaryKiosk().toString();
      }
      if (kiosks != null) {
        model.entities = new EntityBuilder().buildUserEntities(kiosks);
      }
      model.isAdm = (SecurityConstants.ROLE_DOMAINOWNER.equals(account.getRole()) ? true : false);
      model.accDsm = buildAccDmnSuggestionModelList(account.getAccessibleDomainIds());
      model.per = account.getPermission();
      model.lgr = account.getLoginReconnect();
      model.atexp = account.getAuthenticationTokenExpiry();
    }
    model.sdid = account.getDomainId();
    IDomain domain;
    String domainName = Constants.EMPTY;
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      domain = ds.getDomain(account.getDomainId());
      if (domain != null) {
        domainName = domain.getName();
      } else {
        domainName = Constants.EMPTY;
      }
    } catch (Exception e) {
      xLogger.fine("Unable to fetch the domain details" + e);
    }
    model.sdname = domainName;
    return model;
  }

  public IUserAccount buildUserAccount(UserModel model, IUserAccount user) {
    user.setUserId(model.id);
    user.setEncodedPassword(model.pw);
    user.setRole(model.ro);
    user.setCustomId(model.cid);
    user.setTags(model.tgs);
    user.setPermission(model.per);
    user.setFirstName(model.fnm);
    user.setLastName(model.lnm);
    user.setGender(model.gen);
    user.setAge(model.age);
    user.setAgeType(IUserAccount.AGETYPE_YEARS);

    user.setMobilePhoneNumber(model.phm);
    user.setLandPhoneNumber(model.phl);
    user.setEmail(model.em);

    user.setCountry(model.cnt);
    user.setState(model.st);
    user.setDistrict(model.ds);
    user.setTaluk(model.tlk);
    user.setCity(model.ct);
    user.setStreet(model.stn);
    user.setPinCode(model.pin);

    user.setLanguage(model.lng);
    user.setTimezone(model.tz);
    user.setPhoneBrand(model.br);
    user.setPhoneModelNumber(model.mo);
    user.setSimId(model.sId);
    user.setImei(model.imei);
    user.setPhoneServiceProvider(model.sp);
    if (StringUtils.isNotEmpty(model.pk)) {
      user.setPrimaryKiosk(Long.parseLong(model.pk));
    } else {
      user.setPrimaryKiosk(null);
    }
    user.setLoginReconnect(model.lgr);
    user.setAuthenticationTokenExpiry(model.atexp);

    return user;
  }

  public IUserAccount buildUserAccount(UserModel model) {
    return buildUserAccount(model, JDOUtils.createInstance(IUserAccount.class));
  }

  public Results buildUsers(Results results, SecureUserDetails user, boolean isPartial) {
    List<UserModel> models = null;
    if (results != null && results.getSize() > 0) {
      models =
          buildUserModels(results.getResults(), user.getLocale(), user.getTimezone(), isPartial,
              results.getOffset());
    }
    return new Results(models, results.getCursor(), results.getNumFound(), results.getOffset());
  }

  public List<DomainSuggestionModel> buildAccDmnSuggestionModelList(List<Long> accDids) {
    if (accDids == null || accDids.isEmpty()) {
      return null;
    } else {
      try {
        List<DomainSuggestionModel>
            accDmnSuggestionModelList =
            new ArrayList<DomainSuggestionModel>();
        DomainsService ds = Services.getService(DomainsServiceImpl.class);

        for (Long accDid : accDids) {
          IDomain accDomain = ds.getDomain(accDid);
          DomainSuggestionModel dsm = new DomainSuggestionModel();
          dsm.id = accDomain.getId();
          dsm.text = accDomain.getName();
          dsm.hc = accDomain.getHasChild();
          accDmnSuggestionModelList.add(dsm);
        }
        return accDmnSuggestionModelList;
      } catch (ServiceException se) {
        xLogger.severe("{0} when building domain suggestion model for accIds {1}. Message: {2}",
            se.getClass().getName(), accDids, se.getMessage());
        throw new InvalidServiceException(
            "Unable to build domain suggestion models for accDids " + accDids.toString());
      } catch (ObjectNotFoundException onfe) {
        xLogger.severe("{0} when building domain suggestion model for accIds {1}. Message: {2}",
            onfe.getClass().getName(), accDids, onfe.getMessage());
        throw new InvalidServiceException(
            "Unable to build domain suggestion models for accDids " + accDids.toString());
      }
    }
  }
}
