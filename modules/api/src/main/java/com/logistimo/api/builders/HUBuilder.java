package com.logistimo.api.builders;

import com.logistimo.dao.JDOUtils;

import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.models.HUModel;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IHandlingUnitContent;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HUBuilder {

  private HUContentBuilder cBuilder = new HUContentBuilder();

  public IHandlingUnit buildHandlingUnit(HUModel model) {
    IHandlingUnit hu = JDOUtils.createInstance(IHandlingUnit.class);
    hu.setName(model.name);
    hu.setDescription(model.description);
    hu.setId(model.id);
    if (model.contents != null) {
      hu.setContents(cBuilder.buildHUContentSet(model.contents));
    }
    return hu;
  }

  public HUModel buildHUModel(IHandlingUnit hu, IUserAccount cb, IUserAccount ub,
                              SecureUserDetails sUser) {
    HUModel model = buildHUModel(hu, sUser);
    if (cb != null) {
      model.cbName = cb.getFullName();
    }
    if (ub != null) {
      model.ubName = ub.getFullName();
    }
    return model;
  }

  public HUModel buildHUModel(IHandlingUnit hu, SecureUserDetails sUser) {
    HUModel model = new HUModel();
    model.id = hu.getId();
    model.name = hu.getName();
    model.description = hu.getDescription();
    model.timeStamp =
        LocalDateUtil.format(hu.getTimeStamp(), sUser.getLocale(), sUser.getTimezone());
    model.ub = hu.getUpdatedBy();
    model.cb = hu.getCreatedBy();
    model.lastUpdated =
        LocalDateUtil.format(hu.getLastUpdated(), sUser.getLocale(), sUser.getTimezone());
    model.sdId = hu.getDomainId();
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      model.sdname = ds.getDomain(model.sdId).getName();
      UsersService as = Services.getService(UsersServiceImpl.class);
      IUserAccount ua = as.getUserAccount(model.ub);
      model.ubName = ua.getFullName();
      ua = as.getUserAccount(model.cb);
      model.cbName = ua.getFullName();
    } catch (Exception ignored) {
      // ignore
    }
    model.contents = cBuilder.buildHUContentModelList((Set<IHandlingUnitContent>) hu.getContents());
    return model;
  }

  public Results buildHandlingUnitModelList(Results results, SecureUserDetails sUser)
      throws ServiceException {
    List huList = results.getResults();
    List<HUModel> newInventory = new ArrayList<>(huList.size());
    for (Object material : huList) {
      HUModel item = buildHUModel((IHandlingUnit) material, sUser);
      if (item != null) {
        newInventory.add(item);
      }
    }
    return new Results(newInventory, results.getCursor(), results.getNumFound(),
        results.getOffset());
  }
}
