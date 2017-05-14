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

package com.logistimo.events.models;


import java.util.Map;

/**
 * Created by charan on 07/03/17.
 */
public class EventData {
  public final CustomOptions customOptions;
  public final String objectId;
  public final Map<String, Object> params;
  public final int eventId;
  public final String objectType;
  public Long domainId;
  public Object eventObject;

  public EventData(Long domainId, int eventId, Map<String, Object> params, String objectType, String objectId,
                   CustomOptions customOptions, Object eventObject) {
    this.domainId = domainId;
    this.eventId = eventId;
    this.params = params;
    this.objectType = objectType;
    this.objectId = objectId;
    this.customOptions = customOptions;
    this.eventObject = eventObject;
  }
}
