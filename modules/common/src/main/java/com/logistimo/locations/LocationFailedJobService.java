package com.logistimo.locations;

import com.logistimo.entity.ILocationFailedJob;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.util.List;
import java.util.Locale;

/**
 * Created by yuvaraj on 24/03/17.
 */
public interface LocationFailedJobService extends Service {
  List<ILocationFailedJob> getLocationFailedJobs(Locale locale, String timezone)
      throws ServiceException, ObjectNotFoundException;
  void updateLocations(List<ILocationFailedJob> locationFailedJobs) throws ServiceException;
}
