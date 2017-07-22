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

package com.logistimo.tags.dao;


import com.logistimo.logger.XLog;
import com.logistimo.services.impl.PMF;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 17/02/15.
 */
@Repository
public class TagDao implements ITagDao {

  private static final XLog xLogger = XLog.getLog(TagDao.class);

  @Override
  public ITag getTagByName(String name, int type) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(Tag.class);
    query.setFilter("name == nameParam && type == typeParam");
    query.declareParameters("String nameParam,Integer typeParam");
    ITag result = null;
    try {
      List<ITag> results = (List<ITag>) query.execute(name, type);
      if (results != null && results.size() > 0) {
        result = results.get(0);
        result = pm.detachCopy(result);
      } else {
        ITag tag = new Tag(type, name);
        tag = pm.makePersistent(tag);
        return pm.detachCopy(tag);
      }
    } catch (Exception e) {
      xLogger.warn("Error while fetching tag with name {0} and type {1}", name, type, e);
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return result;
  }

  @Override
  public List<ITag> getTagsByNames(List<String> tagNames, int type) {
    if (tagNames == null) {
      return null;
    }
    List<ITag> tags = new ArrayList<ITag>(tagNames.size());
    for (String tagName : tagNames) {
      tags.add(getTagByName(tagName, type));
    }
    return tags;
  }

  @Override
  public Object getTagFilter(String tag, int type) {
    return getTagByName(tag, type).getId();
  }

  @Override
  public ITag getTagById(long id, int type) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(Tag.class);
    query.setFilter("id == idParam && type == typeParam");
    query.declareParameters("Long idParam,Integer typeParam");
    ITag result = null;
    try {
      List<ITag> results = (List<ITag>) query.execute(id, type);
      if (results != null && results.size() > 0) {
        result = results.get(0);
        result = pm.detachCopy(result);
      } else {
        ITag tag = new Tag(type, id);
        tag = pm.makePersistent(tag);
        return pm.detachCopy(tag);
      }
    } catch (Exception e) {
      xLogger.warn("Error while fetching tag with name {0} and type {1}", id, type, e);
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return result;
  }
}
