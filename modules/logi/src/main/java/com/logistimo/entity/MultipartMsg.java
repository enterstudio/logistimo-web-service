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

/**
 *
 */
package com.logistimo.entity;

import com.logistimo.entity.IMultipartMsg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * Represents a multi-part message (e.g. multiple SMS messages that make up a single protocol message)
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class MultipartMsg implements IMultipartMsg {

  private static final String SEP = "|||";

  @PrimaryKey
  @Persistent
  private String id; // typically the session ID for a given message session
  @Persistent
  private String ms = null;
  @Persistent
  private Date t;
  @Persistent
  private String ccode; // country code, to determine gateway

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public int size() {
    if (ms == null) {
      return 0;
    }
    StringTokenizer st = new StringTokenizer(ms, SEP);
    return st.countTokens();
  }

  @Override
  public List<String> getMessages() {
    if (ms == null) {
      return null;
    }
    String msgs = ms;
    StringTokenizer st = new StringTokenizer(msgs, SEP);
    List<String> list = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      list.add(st.nextToken());
    }
    return list;
  }

  @Override
  public void addMessage(String m) {
    if (ms == null) {
      ms = m;
      return;
    }
    String msgs = ms;
    msgs += SEP + m;
    ms = msgs;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date t) {
    this.t = t;
  }

  @Override
  public String getCountry() {
    return ccode;
  }

  @Override
  public void setCountry(String ccode) {
    this.ccode = ccode;
  }
}
