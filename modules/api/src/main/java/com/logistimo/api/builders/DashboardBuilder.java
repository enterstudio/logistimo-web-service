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

package com.logistimo.api.builders;

import com.logistimo.api.models.DashboardChartModel;
import com.logistimo.api.models.DashboardModel;
import com.logistimo.api.models.EventDistribModel;
import com.logistimo.api.models.InvDashboardModel;
import com.logistimo.api.models.MainDashboardModel;
import com.logistimo.api.models.SessionDashboardModel;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.dashboards.entity.IDashboard;
import com.logistimo.events.entity.IEvent;
import com.logistimo.exception.SystemException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Services;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public class DashboardBuilder {

  public static final String MATERIAL_BD = "MAT_BD";

  public IDashboard buildDashboard(DashboardModel model, Long domainId, String userName) {
    IDashboard db = JDOUtils.createInstance(IDashboard.class);
    db.setdId(domainId);
    db.setCreatedOn(new Date());
    db.setCreatedBy(userName);
    db.setName(model.nm);
    db.setDesc(model.desc);
    db.setDef(model.def);
    return db;
  }

  public List<DashboardModel> buildDashboardModelList(Collection<IDashboard> dbList) {
    List<DashboardModel> models = new ArrayList<>(dbList.size());
    for (IDashboard iDashboard : dbList) {
      models.add(buildDashboardModel(iDashboard, false));
    }
    return models;
  }

  public DashboardModel buildDashboardModel(IDashboard db, boolean deep) {
    DashboardModel model = new DashboardModel();
    model.dbId = db.getDbId();
    model.nm = db.getName();
    model.def = db.isDef();
    model.isConfd = StringUtils.isNotEmpty(db.getConf());
    if (deep) {
      try {
        UsersService as = Services.getService(UsersServiceImpl.class);
        model.cByNm = as.getUserAccount(db.getCreatedBy()).getFullName();
        model.cBy = db.getCreatedBy();
        model.cOn = db.getCreatedOn();
        model.desc = db.getDesc();
      } catch (SystemException | ObjectNotFoundException ignored) {
      }
    }
    return model;
  }

  public MainDashboardModel getMainDashBoardData(ResultSet eventRes, ResultSet invRes,
                                                 ResultSet actRes, ResultSet entRes,
                                                 ResultSet tempRes, ResultSet predRes,
                                                 String filterCol)
      throws SQLException {
    MainDashboardModel model = new MainDashboardModel();
    model.inv = constructData(eventRes, filterCol, "i");
    model.invDomain = addDetailData(invRes, model.inv, filterCol, "n");
    model.ent = constructData(actRes, filterCol, "e");
    model.entDomain = addDetailData(entRes, model.ent, filterCol, "i");
    model.pred = addPredictiveData(predRes);
    model.temp = constructData(tempRes, filterCol, "t");
    model.tempDomain = addDetailData(null, model.temp, filterCol, null, true);
    return model;
  }

  private DashboardChartModel addPredictiveData(ResultSet res) throws SQLException {
    if (res == null) {
      return null;
    }
    res.next();
    DashboardChartModel model = new DashboardChartModel();
    model.num = res.getLong("COUNT");
    model.den = res.getLong("ALLCOUNT");
    if (model.den > 0) {
      model.per = (double) model.num / model.den * 100;
    }
    return model;
  }

  public MainDashboardModel getInvDashBoardData(ResultSet eventRes, ResultSet invRes,
                                                ResultSet tempRes, String filterCol, Locale locale,
                                                String timezone) throws SQLException {
    MainDashboardModel model = new MainDashboardModel();
    model.invByMat =
        constructMaterialwiseData(eventRes, invRes, tempRes, filterCol, locale, timezone);
    return model;
  }

  public MainDashboardModel getPredictiveDashBoardData(ResultSet eventRes, ResultSet invRes,
                                                       String filterCol) throws SQLException {
    MainDashboardModel model = new MainDashboardModel();
    model.inv = constructData(eventRes, filterCol, "i");
    model.invDomain = addDetailData(invRes, model.inv, filterCol, "n");
    return model;
  }

  private Map<String, Long> addDetailData(ResultSet res,
                                          Map<String, Map<String, DashboardChartModel>> data,
                                          String filterCol, String addColumn) throws SQLException {
    return addDetailData(res, data, filterCol, addColumn, false);
  }

  private Map<String, Long> addDetailData(ResultSet res,
                                          Map<String, Map<String, DashboardChartModel>> data,
                                          String filterCol, String addColumn, boolean isSameData)
      throws SQLException {
    Map<String, Long> overAllData = new HashMap<>();
    if(data == null || data.size() == 0) {
      return overAllData;
    }
    Map<String, Long> subDataCounts = new HashMap<>();
    Map<String, Long> allMatCounts = new HashMap<>();
    Map<String, Long> subDataID = new HashMap<>();
    Map<String, Long> matIDs = new HashMap<>();
    long allTotal = 0;
    if (!isSameData) {
      ResultSetMetaData metaData = res.getMetaData();
      List<String> columns = new ArrayList<>(metaData.getColumnCount());
      for(int i= 0; i< metaData.getColumnCount(); i++) {
        columns.add(i,metaData.getColumnName(i + 1));
      }
      while (res.next()) {
        long count = res.getLong("COUNT");
        final String filterColValue = res.getString(filterCol);
        if ("NAME".equals(filterCol)) {
          subDataID.put(filterColValue, res.getLong("KID"));
        }
        if (subDataCounts.containsKey(filterColValue)) {
          subDataCounts.put(filterColValue, subDataCounts.get(filterColValue) + count);
        } else {
          subDataCounts.put(filterColValue, count);
        }
        if ("n".equals(addColumn)) {
          final String matValue = res.getString("MATERIAL");
          final Long mid;
          if (!allMatCounts.containsKey(matValue)) {
            allMatCounts.put(matValue, count);
          } else {
            allMatCounts.put(matValue, allMatCounts.get(matValue) + count);
          }
          if (columns.contains("MID") && !matIDs.containsKey(matValue)) {
            mid = res.getLong("MID");
            matIDs.put(matValue, mid);
          }
        }
        allTotal += count;
      }
    } else {
      for (Map<String, DashboardChartModel> val : data.values()) {
        for (String s : val.keySet()) {
          if (subDataCounts.get(s) == null) {
            subDataCounts.put(s, val.get(s).value);
          } else {
            subDataCounts.put(s, subDataCounts.get(s) + val.get(s).value);
          }
          if ("NAME".equals(filterCol)) {
            subDataID.put(s, val.get(s).kid);
          }
          allTotal += val.get(s).value;
        }
      }
    }
    Map<String, Long> allTypeSubDataCounts = new HashMap<>();
    long allTypeTotal = 0;
    Map<String, Long> allMatTypeCounts = new HashMap<>();
    for (String type : data.keySet()) {
      Map<String, DashboardChartModel> subData = data.get(type);
      long typeTotal = 0;
      for (String k : subDataCounts.keySet()) {
        if (!data.get(type).containsKey(k)) {
          if ("NAME".equals(filterCol)) {
            data.get(type).put(k, new DashboardChartModel(0L, subDataID.get(k)));
          } else {
            data.get(type).put(k, new DashboardChartModel(0L));
          }
        }
        if (k != null) { //ignore when key is null; either state or district is not defined
          DashboardChartModel model = subData.get(k);
          model.per = Math.round((model.value * 100 / (double) subDataCounts.get(k)) * 10) / 10.0;
          model.den = subDataCounts.get(k);
          typeTotal += model.value;
          Long sdCnt = allTypeSubDataCounts.get(k);
          allTypeSubDataCounts.put(k, (sdCnt == null ? 0L : sdCnt) + model.value);
        }
      }
      if ("n".equals(addColumn)) {
        final Map<String, DashboardChartModel> mat_bd = data.get(type).get(MATERIAL_BD).materials;
        for (String mat : allMatCounts.keySet()) {
          if (!mat_bd.containsKey(mat)) {
            mat_bd.put(mat, new DashboardChartModel(0L));
          }
          if (allMatTypeCounts.containsKey(mat)) {
            allMatTypeCounts.put(mat, allMatTypeCounts.get(mat) + mat_bd.get(mat).value);
          } else {
            allMatTypeCounts.put(mat, mat_bd.get(mat).value);
          }
          mat_bd.get(mat).den = allMatCounts.get(mat);
        }
      }
      overAllData.put(type, typeTotal);
      allTypeTotal += typeTotal;
    }

    //Add additional data for all sub data (state/district) and overall domain data
    Map<String, DashboardChartModel> additionalData = new HashMap<>();
    for (String k : subDataCounts.keySet()) {
      Long sdCnt = allTypeSubDataCounts.get(k);
      DashboardChartModel
          model =
          new DashboardChartModel(subDataCounts.get(k) - (sdCnt == null ? 0L : sdCnt),
              subDataID.get(k));
      model.per = Math.round((model.value * 100 / (double) subDataCounts.get(k)) * 10) / 10.0;
      model.den = subDataCounts.get(k);
      additionalData.put(k, model);
    }
    if ("n".equals(addColumn)) {
      final DashboardChartModel materialBreakDown = new DashboardChartModel();
      materialBreakDown.materials = new HashMap<>();
      for (String matName : allMatCounts.keySet()) {
        final long count = allMatCounts.get(matName) -
            (allMatTypeCounts.get(matName) == null ? 0 : allMatTypeCounts.get(matName));
        DashboardChartModel m = new DashboardChartModel();
        m.value = count;
        m.den = allMatCounts.get(matName);
        m.mid = matIDs.get(matName);
        materialBreakDown.materials.put(matName, m);
      }
      additionalData.put(MATERIAL_BD, materialBreakDown);
    }
    if (addColumn != null) {
      data.put(addColumn, additionalData);
      overAllData.put(addColumn, allTotal - allTypeTotal);
    }
    return overAllData;
  }

  /**
   * Return Map of State/District with values as Map Material Id,
   *
   * @param res       -
   * @param invRes    -
   * @param filterCol @return
   */
  private Map<String, InvDashboardModel> constructMaterialwiseData(ResultSet res, ResultSet invRes,
                                                                   ResultSet tempRes,
                                                                   String filterCol, Locale locale,
                                                                   String timezone)
      throws SQLException {
    if (res == null || invRes == null) {
      return null;
    }
    Map<String, Map<Long, Long>> stateCounts = new HashMap<>();
    Map<String, InvDashboardModel> dimModel = new HashMap<>(1);
    boolean isEntity = "ENTITY".equals(filterCol);
    while (invRes.next()) {
      String dim = invRes.getString(filterCol);
      if (StringUtils.isBlank(dim)) {
        dim = "No district";
      }
      InvDashboardModel model = dimModel.get(dim);
      if (model == null) {
        model = new InvDashboardModel();
        model.dim = dim;
        if (isEntity) {
          model.id = invRes.getLong("KID");
        }
      }
      long count = invRes.getLong("COUNT");

      Map<Long, Long> matCounts = stateCounts.get(dim);
      if (matCounts == null) {
        matCounts = new HashMap<>(1);
        matCounts.put(-1l, 0l);
        stateCounts.put(dim, matCounts);
      }
      long mId = invRes.getLong("MID");
      matCounts.put(mId, count);
      matCounts.put(-1l, matCounts.get(-1l) + count);
      EventDistribModel typeDistrib = model.eve.get(mId);
      if (typeDistrib == null) {
        typeDistrib = new EventDistribModel();
        model.eve.put(mId, typeDistrib);
      }
      typeDistrib.c = count;
      typeDistrib.n = count;
      typeDistrib.np = 100d;
      if (isEntity) {
        typeDistrib.q = new Float(invRes.getFloat("STK")).longValue();
        typeDistrib.min = new Float(invRes.getFloat("REORD")).longValue();
        typeDistrib.max = new Float(invRes.getFloat("MAX")).longValue();
        typeDistrib.t = LocalDateUtil.format(invRes.getDate("T"), locale, timezone);
      } else {
        typeDistrib.q = new Float(invRes.getFloat("TOTALQ")).longValue();
      }
      dimModel.put(model.dim, model);
    }
    while (res.next()) {
      String dim = res.getString(filterCol);
      if (StringUtils.isBlank(dim)) {
        dim = "No district";
      }
      InvDashboardModel model = dimModel.get(dim);
      if (model == null) {
        model = new InvDashboardModel();
        model.dim = dim;
        if (isEntity) {
          model.id = invRes.getLong("KID");
        }
      }
      Map<Long, Long> matCounts = stateCounts.get(model.dim);
      int type = res.getInt("TY");
      Long mId = res.getLong("MID");
      EventDistribModel typeDistrib = model.eve.get(mId);
      if (typeDistrib == null) {
        typeDistrib = new EventDistribModel();
        model.eve.put(mId, typeDistrib);
      }
      Long count = res.getLong("COUNT");
      typeDistrib.c = matCounts.get(mId) != null ? matCounts.get(mId) : 0L;
      double per = Math.round((count * 100 / (double) typeDistrib.c * 10) / 10.0);
      switch (type) {
        case IEvent.STOCKOUT:
          typeDistrib.oosp = per;
          typeDistrib.oos = count;
          break;
        case IEvent.UNDERSTOCK:
          typeDistrib.usp = per;
          typeDistrib.us = count;
          break;
        case IEvent.OVERSTOCK:
          typeDistrib.osp = per;
          typeDistrib.os = count;
      }
      typeDistrib.n = typeDistrib.n - count;
      typeDistrib.np = Math.round((typeDistrib.n * 100 / (double) typeDistrib.c * 10) / 10.0);
      dimModel.put(model.dim, model);
    }
    if (tempRes != null) {
      while (tempRes.next()) {
        String dim = tempRes.getString(isEntity ? "NAME" : filterCol);
        InvDashboardModel model = dimModel.get(dim);
        if (model != null) {
          model.assets.put(tempRes.getString("STAT"), tempRes.getInt("COUNT"));
        }
      }
    }
    return dimModel;
  }

  private Map<String, Map<String, DashboardChartModel>> constructData(ResultSet res,
                                                                      String filterCol,
                                                                      String resType)
      throws SQLException {
    Map<String, Map<String, DashboardChartModel>> inv = new HashMap<>();
    if (res == null) {
      return inv;
    }
    Collection<String> keys = new HashSet<>();
    ResultSetMetaData metaData = res.getMetaData();
    List<String> columns = new ArrayList<>(metaData.getColumnCount());
    for(int i= 0; i< metaData.getColumnCount(); i++) {
      columns.add(i,metaData.getColumnName(i + 1));
    }
    while (res.next()) {
      String
          type =
          "i".equals(resType) ? res.getString("TY")
              : ("t".equals(resType) ? res.getString("STAT") : "a");
      if (!keys.contains(type)) {
        keys.add(type);
        inv.put(type, new HashMap<String, DashboardChartModel>());
      }
      final Map<String, DashboardChartModel> invByType = inv.get(type);
      final String filterColValue = res.getString(filterCol);
      final long count = res.getLong("COUNT");
      if (invByType.containsKey(filterColValue)) {
        invByType.get(filterColValue).value += count;
      } else {
        if ("NAME".equals(filterCol)) {
          invByType.put(filterColValue, new DashboardChartModel(count, res.getLong("KID")));
        } else {
          invByType.put(filterColValue, new DashboardChartModel(count));
        }
      }
      if ("i".equals(resType)) {
        DashboardChartModel materialBreakDown = invByType.get(MATERIAL_BD);
        final String matValue = res.getString("MATERIAL");
        if (materialBreakDown == null) {
          materialBreakDown = new DashboardChartModel();
          materialBreakDown.materials = new HashMap<>();
          invByType.put(MATERIAL_BD, materialBreakDown);
        }
        if (materialBreakDown.materials.containsKey(matValue)) {
          materialBreakDown.materials.get(matValue).value += count;
        } else {
          DashboardChartModel matChartModel = new DashboardChartModel(count);
          if(columns.contains("MID")) {
            matChartModel.mid = res.getLong("MID");
        }
          materialBreakDown.materials.put(matValue, matChartModel);
        }
      }
    }
    return inv;
  }

  public SessionDashboardModel getSessionData(ResultSet allSessionRes, ResultSet sessionRes,
                                              String colFilter, Date atd)
      throws SQLException, ParseException {
    SessionDashboardModel model = new SessionDashboardModel();
    model.data = new HashMap<>();
    SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_CUSTOMREPORT);
    Calendar calendar = Calendar.getInstance();
    while (allSessionRes.next()) {
      Map<String, List<DashboardChartModel>> dd = new LinkedHashMap<>(7);
      calendar.setTime(atd);
      calendar.add(Calendar.DAY_OF_MONTH, -6);
      long den = allSessionRes.getLong("COUNT");
      for (int i = 6; i >= 0; i--) { // 1 week data add placeholders to fill
        List<DashboardChartModel> def = new ArrayList<>(3);
        def.add(new DashboardChartModel(0L, 0D, 0L, den));
        def.add(new DashboardChartModel(0L, 0D, 0L, den));
        def.add(new DashboardChartModel(0L, 0D, 0L, den));
        dd.put(df.format(calendar.getTime()), def);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
      }
      model.data.put(allSessionRes.getString(colFilter), dd);
    }
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
    SimpleDateFormat fdf = new SimpleDateFormat(Constants.DATE_FORMAT_CUSTOMREPORT);

    while (sessionRes.next()) {
      String dim = sessionRes.getString(colFilter);
      String dt = fdf.format(sdf.parse(sessionRes.getString("ATD")));
      List<DashboardChartModel> data = model.data.get(dim).get(dt);
      DashboardChartModel d = data.get(sessionRes.getInt("DF") - 1);
      d.value = sessionRes.getLong("TCNT");
      d.num = sessionRes.getLong("CNT");
      if (d.den > 0) {
        d.per = (double) d.num / d.den * 100;
      }
    }
    model.domain = addDetailSessionData(model.data);
    return model;
  }

  private Map<String, List<DashboardChartModel>> addDetailSessionData(
      Map<String, Map<String, List<DashboardChartModel>>> data) {
    Map<String, List<DashboardChartModel>> domData = new LinkedHashMap<>(data.size());
    for (Map<String, List<DashboardChartModel>> value : data.values()) {
      for (String date : value.keySet()) {
        List<DashboardChartModel> v = value.get(date);
        if (domData.get(date) == null) {
          List<DashboardChartModel> def = new ArrayList<>(3);
          for (DashboardChartModel model : v) {
            def.add(new DashboardChartModel(model.value, 0D, model.num, model.den));
          }
          domData.put(date, def);
        } else {
          List<DashboardChartModel> def = domData.get(date);
          for (int i = 0; i < def.size(); i++) {
            def.get(i).value += v.get(i).value;
            def.get(i).num += v.get(i).num;
            def.get(i).den += v.get(i).den;
          }
        }
      }
    }
    for (List<DashboardChartModel> d : domData.values()) {
      for (DashboardChartModel model : d) {
        if (model.den > 0) {
          model.per = (double) model.num / model.den * 100;
        }
      }
    }
    return domData;
  }

  public MainDashboardModel getEntityInvTempDashboard(ResultSet enInvData, ResultSet enTempData, Integer totalInvCount) throws SQLException {
    MainDashboardModel evModel = new MainDashboardModel();
    evModel.invDomain = getEntityInvDashboardModel(enInvData, totalInvCount);
    evModel.tempDomain = getEntityTempDashboardModel(enTempData);
    return evModel;
  }
  private Map<String,Long> getEntityInvDashboardModel(ResultSet enInvData, Integer totalInvCount)
      throws SQLException {
    if(enInvData != null) {
      HashMap<String,Long> invData = new HashMap<>();
      Long total = 0l;
      while (enInvData.next()) {
        Long count = enInvData.getLong("COUNT");
        String type = enInvData.getString("TY");
        invData.put(type, count);
        total = total + count;
      }
      total = Long.valueOf(totalInvCount) - total;
      invData.put("n", total);
      return invData;
    }
    return null;
  }

  private Map<String,Long> getEntityTempDashboardModel(ResultSet enTempData) throws SQLException {
    if(enTempData != null) {
      Map<String,Long> tempData = new HashMap<>();
      while (enTempData.next()) {
        Long count = enTempData.getLong("COUNT");
        String stat = enTempData.getString("STAT");
        tempData.put(stat, count);
      }
      return tempData;
    }
    return null;
  }
}
