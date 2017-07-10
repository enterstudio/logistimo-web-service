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

package com.logistimo.api.builders.mobile;

import com.logistimo.api.models.mobile.MobileInvDashboardDetailModel;
import com.logistimo.api.models.mobile.MobileInvDashboardDetails;
import com.logistimo.api.models.mobile.MobileInvDashboardModel;
import com.logistimo.events.entity.IEvent;
import com.logistimo.exception.SystemException;

import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MobileInvDashboardBuilder {

  public static final String STATE = "STATE";
  public static final String STATEID = "STATE_ID";
  public static final String STATE_LOWER = "state";
  public static final String DISTRICT = "DISTRICT";
  public static final String DISTRICT_LOWER = "district";
  public static final String DISTID = "DISTRICT_ID";
  public static final String KID = "KID";
  public static final String NAME = "NAME";
  public static final String MATERIAL = "MATERIAL";
  public static final String MID = "MID";
  public static final String GROUP_BY_LOC = "loc";
  public static final String GROUP_BY_MAT = "mat";
  public static final String EVENT_TYPE = "TY";
  static final String COUNT = "COUNT";

  private MobileInvDashboardBuilder() {
  }

  public static MobileInvDashboardModel buildInvDashboard(ResultSet invTyRes, ResultSet invAlRes,
                                                          ResultSet acstRes, ResultSet alstRes) {

    MobileInvDashboardModel model = new MobileInvDashboardModel();
    try {
      if (alstRes.next()) {
        model.tec = alstRes.getLong(COUNT);
      }
      if (acstRes.next()) {
        model.aec = acstRes.getLong(COUNT);
      }
      if (invAlRes.next()) {
        model.tc = invAlRes.getLong(COUNT);
      }
      while (invTyRes.next()) {
        if (invTyRes.getInt(EVENT_TYPE) == IEvent.STOCKOUT) {
          model.soc = invTyRes.getLong(COUNT);
        } else if (invTyRes.getInt(EVENT_TYPE) == IEvent.UNDERSTOCK) {
          model.lmnc = invTyRes.getLong(COUNT);
        } else if (invTyRes.getInt(EVENT_TYPE) == IEvent.OVERSTOCK) {
          model.gmc = invTyRes.getLong(COUNT);
        }
      }
      model.nc = model.tc - (model.soc + model.lmnc + model.gmc);
      return model;
    } catch (SQLException e) {
      throw new SystemException(e);
    }
  }


  public static MobileInvDashboardDetails buildInvDetailDashboard(ResultSet invTyRes,
                                                                  ResultSet invAlRes,
                                                                  ResultSet alstRes,
                                                                  String locty, String groubby) {
    //Here assumption is groupby will never be null, it will either be
    //"loc" or "mat".
    //prepare response
    MobileInvDashboardDetails details = new MobileInvDashboardDetails();
    List<MobileInvDashboardDetailModel> list;
    try {
      //process total inventory result set
      list = buildInvTotalResultSet(invAlRes, alstRes, locty, groubby);

      //process inventory by type
      List<MobileInvDashboardDetailModel> invtyList = buildInvByType(invTyRes, locty, groubby);

      //merging
      if (!invtyList.isEmpty()) {
        MobileInvDashboardDetailModel re;
        for (MobileInvDashboardDetailModel m : invtyList) {
          int i = list.indexOf(m);
          re = list.get(i);
          re.merge(m);
        }
      }

      if (!list.isEmpty()) {
        for (MobileInvDashboardDetailModel m : list) {
          m.nc = m.tc - (m.soc + m.gmc + m.lmnc);
        }
        details.items = list;
        details.total = list.size();
      }
    } catch (SQLException e) {
      throw new SystemException(e);
    }
    return details;
  }

  private static List<MobileInvDashboardDetailModel> buildInvByType(ResultSet invTyRes,
                                                                    String locty, String groubby)
      throws SQLException {
    List<MobileInvDashboardDetailModel> invntryList = new ArrayList<>(1);
    while (invTyRes.next()) {
      MobileInvDashboardDetailModel invTyModel = new MobileInvDashboardDetailModel();
      if (invTyRes.getInt(EVENT_TYPE) == IEvent.STOCKOUT) {
        invTyModel.soc = invTyRes.getLong(COUNT);
      } else if (invTyRes.getInt(EVENT_TYPE) == IEvent.UNDERSTOCK) {
        invTyModel.lmnc = invTyRes.getLong(COUNT);
      } else if (invTyRes.getInt(EVENT_TYPE) == IEvent.OVERSTOCK) {
        invTyModel.gmc = invTyRes.getLong(COUNT);
      }
      if (groubby.equals(GROUP_BY_LOC)) {
        if (StringUtils.isEmpty(locty)) {
          invTyModel.lcnm = invTyRes.getString(STATE);
          invTyModel.lcid = invTyRes.getString(STATEID);
        } else if (locty.equals(STATE_LOWER)) {
          invTyModel.lcnm = invTyRes.getString(DISTRICT);
          invTyModel.lcid = invTyRes.getString(DISTID);
        } else if (locty.equals(DISTRICT_LOWER)) {
          invTyModel.eId = invTyRes.getLong(KID);
          invTyModel.enm = invTyRes.getString(NAME);
        }
      } else if (groubby.equals(GROUP_BY_MAT)) {
        invTyModel.mnm = invTyRes.getString(MATERIAL);
        invTyModel.mId = invTyRes.getLong(MID);
      }
      invntryList.add(invTyModel);
    }
    return invntryList;
  }

  private static List<MobileInvDashboardDetailModel> buildInvTotalResultSet(ResultSet invAlRes,
                                                                            ResultSet alstRes,
                                                                            String locty,
                                                                            String groupBy)
      throws SQLException {

    List<MobileInvDashboardDetailModel> mInvDashboardList = new ArrayList<>(1);
    while (invAlRes.next()) {
      MobileInvDashboardDetailModel model = new MobileInvDashboardDetailModel();
      model.tc = invAlRes.getLong(COUNT);
      if (groupBy.equals(GROUP_BY_MAT)) {
        model.mId = invAlRes.getLong(MID);
        model.mnm = invAlRes.getString(MATERIAL);
      } else if (groupBy.equals(GROUP_BY_LOC)) {
        boolean store = alstRes.next();
        if (StringUtils.isEmpty(locty)) {
          model.lcnm = invAlRes.getString(STATE);
          model.lcid = invAlRes.getString(STATEID);
          if (store && (alstRes.getString(1).equals(model.lcnm))) {
            model.tsc = alstRes.getLong(3);
          }
        } else if (locty.equals(STATE_LOWER)) {
          model.lcnm = invAlRes.getString(DISTRICT);
          model.lcid = invAlRes.getString(DISTID);
          if (store && (alstRes.getString(1).equals(model.lcnm))) {
            model.tsc = alstRes.getLong(3);
          }
        } else if (locty.equals(DISTRICT_LOWER)) {
          model.eId = invAlRes.getLong(KID);
          model.enm = invAlRes.getString(NAME);
          if (store && (alstRes.getString(1).equals(model.enm))) {
            model.tsc = alstRes.getLong(3);
          }
        }
      }
      mInvDashboardList.add(model);
    }
    return mInvDashboardList;
  }

}
