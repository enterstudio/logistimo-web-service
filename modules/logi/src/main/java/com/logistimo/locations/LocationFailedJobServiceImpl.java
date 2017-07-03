/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.locations;

import com.google.gson.Gson;

import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.entity.ILocationFailedJob;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by yuvaraj on 24/03/17.
 */
public class LocationFailedJobServiceImpl extends ServiceImpl implements LocationFailedJobService {
  @Override
  public List<ILocationFailedJob> getLocationFailedJobs(Locale locale, String timezone)
      throws ServiceException, ObjectNotFoundException {
    String endDate = LocalDateUtil.format(new Date(), locale, timezone);
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_CSV_FORMAT);
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(new Date());
    calendar.add(Calendar.DAY_OF_MONTH, -Constants.LOCATION_FAILED_JOB_LIMIT);
    calendar.add(Calendar.SECOND, 1);
    String
        query =
        "SELECT * FROM LOCATIONFAILEDJOB WHERE PROCESSFLAG = 0 AND CREATEDATE BETWEEN '"
            .concat(sdf.format(calendar.getTime()))
            .concat("' AND '").concat(endDate).concat("'");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery("javax.jdo.query.SQL", query);
    q.setClass(JDOUtils.getImplClass(ILocationFailedJob.class));
    return  (List<ILocationFailedJob>) q.execute();
  }

  public void updateLocations(List<ILocationFailedJob> locationFailedJobs) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Map<String,Object> objectMap = new HashMap<>();
    String payLoad;
    for (ILocationFailedJob locationFailedJob : locationFailedJobs) {
      payLoad = locationFailedJob.getPayLoad();
      Gson gson = new Gson();
      Map map = gson.fromJson(payLoad, Map.class);
      if (locationFailedJob.getType().equals("kiosk")) {
        int id = (int) Double.parseDouble(String.valueOf(map.get("kioskId")));
        IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, id, pm);
        objectMap.put("kioskId", kiosk.getKioskId());
        objectMap.put("userName", kiosk.getRegisteredBy());
        objectMap = LocationServiceUtil.getInstance().getLocationIds(kiosk, objectMap);
        EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);
        entitiesService.updateKioskLocationIds(kiosk, objectMap, pm);
      } else if(locationFailedJob.getType().equals("user")) {
        String userId = String.valueOf(map.get("userId"));
        IUserAccount userAccount = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
        objectMap.put("userId",userAccount.getUserId());
        objectMap.put("userName",userAccount.getRegisteredBy());
        objectMap = LocationServiceUtil.getInstance().getLocationIds(userAccount, objectMap);
        UsersService usersService = Services.getService(UsersServiceImpl.class);
        usersService.updateUserLocationIds(userAccount, objectMap, pm);
      }
    }
  }
}
