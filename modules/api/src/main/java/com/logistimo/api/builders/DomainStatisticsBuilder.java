package com.logistimo.api.builders;

import com.logistimo.reports.entity.slices.IDomainStats;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.api.models.DomainStatisticsModel;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;

import java.util.List;
import java.util.Locale;

/**
 * Created by mohansrinivas on 10/20/16.
 */
public class DomainStatisticsBuilder {
  private static final XLog xLogger = XLog.getLog(DomainStatisticsBuilder.class);
  private ITagDao tagDao = new TagDao();

  /**
   *
   * @param masterData
   * @return
   */

  public DomainStatisticsModel buildParentModel(IDomainStats masterData) {
    DomainStatisticsModel parentDataModel = new DomainStatisticsModel();
    try {
      Long domainId = masterData.getDomainId();
      if (domainId != 0) {
        DomainsService ds = Services.getService(DomainsServiceImpl.class);
        IDomain domain = ds.getDomain(domainId);
        parentDataModel.hc = domain.getHasChild();
        parentDataModel.did = domainId;
        parentDataModel.dnm = domain.getName();
        parentDataModel.akc = masterData.getActiveKioskCount();
        parentDataModel.lkc = masterData.getLiveKioskCount();
        parentDataModel.kc = masterData.getKioskCount();
        parentDataModel.uc = masterData.getUserCount();
        parentDataModel.mwa = masterData.getMonitoredWorkingAssets();
        parentDataModel.mawa = masterData.getMonitoredActiveWorkingAssets();
        parentDataModel.mac = masterData.getMonitoredAssetCounts();
        parentDataModel.mlwa = masterData.getMonitoredLiveWorkingAssets();
        parentDataModel.miac = masterData.getMonitoringAssetCounts();
      }
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("unable to get the get the details for domainId", e);
      throw new InvalidServiceException("unable to get the get the details for domainId");
    }
    return parentDataModel;
  }

  /**
   *
   * @param childrenData
   * @param domainStatisticsModel
   * @return
   */

  public DomainStatisticsModel buildChildModel(List<? extends IDomainStats> childrenData,
                                               DomainStatisticsModel domainStatisticsModel,
                                               Locale locale, String timezone) {
    try {
      for (IDomainStats childData : childrenData) {
        DomainStatisticsModel childDataModel = new DomainStatisticsModel();
        Long domainId = childData.getDomainId();
        // if domainID is null, we are assigning it to zero while fetching
        // this will check whether domainId is 0 or not
        if (domainId != 0) {
          DomainsService ds = Services.getService(DomainsServiceImpl.class);
          IDomain domain = ds.getDomain(domainId);
          childDataModel.hc = domain.getHasChild();
          childDataModel.dnm = domain.getName();
          childDataModel.did = childData.getDomainId();
          childDataModel.akc = childData.getActiveKioskCount();
          childDataModel.lkc = childData.getLiveKioskCount();
          childDataModel.kc = childData.getKioskCount();
          childDataModel.uc = childData.getUserCount();
          childDataModel.mwa = childData.getMonitoredWorkingAssets();
          childDataModel.mawa = childData.getMonitoredActiveWorkingAssets();
          childDataModel.mac = childData.getMonitoredAssetCounts();
          childDataModel.mlwa = childData.getMonitoredLiveWorkingAssets();
          childDataModel.miac = childData.getMonitoringAssetCounts();
          domainStatisticsModel.child.add(childDataModel);
        }
      }
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("unable to get the details for domainId", e);
      throw new InvalidServiceException("unable to get the get the details for domainId");
    }
    return domainStatisticsModel;
  }



}
