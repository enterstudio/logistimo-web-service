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

package com.logistimo.entities.dao;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IPoolGroup;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.utils.QueryUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 17/02/15.
 */
public class EntityDao implements IEntityDao {

  private static final XLog xLogger = XLog.getLog(EntityDao.class);

  ITagDao tagDao = new TagDao();

  public Results getAllKiosks(Long domainId, String tag, PageParams pageParams) {
    return getKiosks(domainId, tag, pageParams, false);
  }

  public Results getAllDomainKiosks(Long domainId, String tag, PageParams pageParams) {
    return getKiosks(domainId, tag, pageParams, true);
  }

  private Results getKiosks(Long domainId, String tag, PageParams pageParams, boolean isDomain) {
    xLogger.fine("Entering getKiosks");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IKiosk> kiosks = new ArrayList<IKiosk>();
    String cursor = null;
    try {
      String filter;
      if (isDomain) {
        filter = "sdId == domainIdParam";
      } else {
        filter = "dId.contains(domainIdParam)";
      }
      String declaration = "Long domainIdParam";
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("domainIdParam", domainId);
      if (tag != null && !tag.isEmpty()) {
        if (tag.contains(CharacterConstants.COMMA)) {
          List<String> tags = StringUtil.getList(tag, true);
          List<ITag> tagIdList = tagDao.getTagsByNames(tags, ITag.KIOSK_TAG);
          int i = 0;
          filter += " && ( ";
          for (ITag localTag : tagIdList) {
            String tgsParam = "tgsParam" + (++i);
            if (i != 1) {
              filter += " || ";
            }
            filter += " tgs.contains(" + tgsParam + ")";
            declaration += ", Long " + tgsParam;
            params.put(tgsParam, localTag.getId());
          }
          filter += " ) ";
        } else {
          filter += " && tgs.contains(tgsParam)";
          declaration += ", Long tgsParam";
          params.put("tgsParam", tagDao.getTagFilter(tag, ITag.KIOSK_TAG));
        }
      }
      Query query = pm.newQuery(Kiosk.class);
      query.setFilter(filter);
      query.declareParameters(declaration);
      query.setOrdering("nName asc");
      if (pageParams != null) {
        QueryUtil.setPageParams(query, pageParams);
      }
      try {
        kiosks = (List<IKiosk>) query.executeWithMap(params);
        kiosks
            .size(); // TODO This is to prevent datanucleus exception - "Object manager is closed"; this retrieves all objects before object manager is closed
        // Get the cursor, if any
        cursor = QueryUtil.getCursor(kiosks);
        kiosks = (List<IKiosk>) pm.detachCopyAll(kiosks);
      } finally {
        query.closeAll();
      }
    } catch (Exception e) {
      xLogger.warn("Exception: {0}", e.getMessage());
    } finally {
      // Close PM
      pm.close();
    }

    xLogger.fine("Exiting getKiosks");
    return new Results(kiosks, cursor, -1, (pageParams == null ? 0 : pageParams.getOffset()));
  }

  public String getKeyString(IKiosk kiosk) {
    return String.valueOf(kiosk.getKioskId());
  }


  public String getKeyString(IPoolGroup group) {
    return String.valueOf(group.getGroupId());
  }
}
