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

package com.logistimo.bulkuploads.headers;

import com.logistimo.services.Resources;
import com.logistimo.constants.CharacterConstants;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class AssetsHeader implements IHeader {

  @Override
  public String getUploadableCSVHeader(Locale locale, String type) {
    ResourceBundle bundle = Resources.get().getBundle("BackendMessages", locale);
    String format = bundle.getString("kiosk") + CharacterConstants.COMMA +
        bundle.getString("custom.entity") + CharacterConstants.COMMA +
        bundle.getString("city") + CharacterConstants.COMMA +
        bundle.getString("district") + CharacterConstants.COMMA +
        bundle.getString("state") + CharacterConstants.COMMA +
        bundle.getString("asset.type") + CharacterConstants.COMMA +
        bundle.getString("bulk.asset.id") + CharacterConstants.COMMA +
        bundle.getString("manufacturer") + CharacterConstants.COMMA +
        bundle.getString("model") + CharacterConstants.COMMA +
        bundle.getString("monitored.asset.manufacture.year") + CharacterConstants.COMMA +
        bundle.getString("sensor.device.id") + CharacterConstants.COMMA +
        bundle.getString("sim1") + CharacterConstants.COMMA +
        bundle.getString("sim1.id") + CharacterConstants.COMMA +
        bundle.getString("sim1.ntw.provider") + CharacterConstants.COMMA +
        bundle.getString("sim2") + CharacterConstants.COMMA +
        bundle.getString("sim2.id") + CharacterConstants.COMMA +
        bundle.getString("sim2.ntw.provider") + CharacterConstants.COMMA +
        bundle.getString("imei") + CharacterConstants.COMMA +
        bundle.getString("manufacturer.name") + CharacterConstants.COMMA +
        bundle.getString("asset.model") + CharacterConstants.COMMA +
        bundle.getString("monitoring.asset.manufacture.year");
    return format;
  }
}
