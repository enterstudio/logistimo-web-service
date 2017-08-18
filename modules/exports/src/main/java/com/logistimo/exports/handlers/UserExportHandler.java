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

package com.logistimo.exports.handlers;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class UserExportHandler implements IExportHandler {

  IUserAccount user;

  public UserExportHandler(IUserAccount user){
    this.user = user;
  }

  // Get the CSV of this user
  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    StringBuilder csv = new StringBuilder();
    String pkName = null, pkCustomId = null, cbUserName = null, cbUserCustomId = null,
        ubUserName =
            null, ubUserCustomId = null;

    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      if (user.getUpdatedBy() != null) {
        try {
          IUserAccount ubUser = as.getUserAccount(user.getUpdatedBy());
          ubUserName = ubUser.getFullName();
          ubUserCustomId = ubUser.getCustomId();
        } catch (ObjectNotFoundException e) {
          ubUserName = Constants.UNKNOWN;
        }
      }
      if (user.getRegisteredBy() != null) {
        try {
          IUserAccount cbUser = as.getUserAccount(user.getRegisteredBy());
          cbUserName = cbUser.getFullName();
          cbUserCustomId = cbUser.getCustomId();
        } catch (ObjectNotFoundException e) {
          cbUserName = Constants.UNKNOWN;
        }
      }
      if (user.getPrimaryKiosk() != null) {
        EntitiesService es = Services.getService(EntitiesServiceImpl.class);
        IKiosk pk = es.getKiosk(user.getPrimaryKiosk());
        pkName = pk.getName();
        pkCustomId = pk.getCustomId();
      }
    } catch (ServiceException ignored) {
      pkName = Constants.UNKNOWN;
    }
    List<String> tgs = user.getTags();
    csv.append(user.getUserId()).append(CharacterConstants.COMMA)
        .append(user.getCustomId() != null ? StringEscapeUtils.escapeCsv(user.getCustomId()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getRole()).append(CharacterConstants.COMMA)
        .append(StringEscapeUtils.escapeCsv(user.getFirstName())).append(CharacterConstants.COMMA)
        .append(user.getLastName() != null ? StringEscapeUtils.escapeCsv(user.getLastName()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.isEnabled() ? Constants.YES : Constants.NO).append(CharacterConstants.COMMA)
        .append(user.getMobilePhoneNumber()).append(CharacterConstants.COMMA)
        .append(user.getLandPhoneNumber() != null ? user.getLandPhoneNumber() : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getEmail() != null ? user.getEmail() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(user.getCountry() != null ? StringEscapeUtils.escapeCsv(user.getCountry()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getState() != null ? StringEscapeUtils.escapeCsv(user.getState()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getDistrict() != null ? StringEscapeUtils.escapeCsv(user.getDistrict()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getTaluk() != null ? StringEscapeUtils.escapeCsv(user.getTaluk()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getCity() != null ? StringEscapeUtils.escapeCsv(user.getCity()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getStreet() != null ? StringEscapeUtils.escapeCsv(user.getStreet()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getPinCode() != null ? user.getPinCode() : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getLanguage() != null ? StringEscapeUtils.escapeCsv(user.getLanguage()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(StringEscapeUtils.escapeCsv(user.getTimezone())).append(CharacterConstants.COMMA)
        .append(user.getGender() != null ? user.getGender() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(user.getAge()).append(CharacterConstants.COMMA)
        .append(
            tgs != null && !tgs.isEmpty() ? StringUtil.getCSV(tgs, CharacterConstants.SEMICOLON)
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            user.getPhoneBrand() != null ? StringEscapeUtils.escapeCsv(user.getPhoneBrand()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getPhoneModelNumber() != null ? StringEscapeUtils
            .escapeCsv(user.getPhoneModelNumber())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(user.getImei() != null ? StringEscapeUtils.escapeCsv(user.getImei()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getPhoneServiceProvider() != null ? StringEscapeUtils.escapeCsv(user.getPhoneServiceProvider())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(user.getSimId() != null ? StringEscapeUtils.escapeCsv(user.getSimId()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getPrimaryKiosk() != null ? user.getPrimaryKiosk() : CharacterConstants.EMPTY)
        .append(
            CharacterConstants.COMMA)
        .append(
            pkCustomId != null ? StringEscapeUtils.escapeCsv(pkCustomId) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(pkName != null ? StringEscapeUtils.escapeCsv(pkName) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getAppVersion() != null ? StringEscapeUtils.escapeCsv(user.getAppVersion()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getLastLogin() != null ? LocalDateUtil
            .formatCustom(user.getLastLogin(), Constants.DATETIME_CSV_FORMAT, timezone)
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            user.getLastMobileAccessed() != null ? LocalDateUtil.formatCustom(
                user.getLastMobileAccessed(), Constants.DATETIME_CSV_FORMAT, timezone)
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(user.getRegisteredBy() != null ? user.getRegisteredBy() : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(cbUserCustomId != null ? StringEscapeUtils.escapeCsv(cbUserCustomId)
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            cbUserName != null ? StringEscapeUtils.escapeCsv(cbUserName) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(LocalDateUtil.formatCustom(user.getMemberSince(), Constants.DATETIME_CSV_FORMAT, timezone))
        .append(CharacterConstants.COMMA)
        .append(user.getUpdatedBy() != null ? user.getUpdatedBy() : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(ubUserCustomId != null ? StringEscapeUtils.escapeCsv(ubUserCustomId)
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            ubUserName != null ? StringEscapeUtils.escapeCsv(ubUserName) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(user.getUpdatedOn() != null ? LocalDateUtil.formatCustom(user.getUpdatedOn(), Constants.DATETIME_CSV_FORMAT, timezone)
            : CharacterConstants.EMPTY);

    return csv.toString();
  }

  // Header for CSV of a user (either import or export)
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    ResourceBundle bundle = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsBundle = Resources.get().getBundle("JSMessages", locale);
    StringBuilder header = new StringBuilder();
    header.append(bundle.getString("user.id")).append(CharacterConstants.COMMA)
        .append(bundle.getString("customid.user")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.role")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.O_BRACKET).append(bundle.getString("role.domainowner"))
        .append(CharacterConstants.SPACE).append(CharacterConstants.EQUALS)
        .append(CharacterConstants.SPACE)
        .append(SecurityConstants.ROLE_DOMAINOWNER).append(CharacterConstants.SPACE)
        .append(CharacterConstants.F_SLASH).append(CharacterConstants.SPACE)
        .append(bundle.getString("role.servicemana"
            + "ger")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.EQUALS)
        .append(CharacterConstants.SPACE).append(SecurityConstants.ROLE_SERVICEMANAGER)
        .append(CharacterConstants.SPACE).append(CharacterConstants.F_SLASH)
        .append(CharacterConstants.SPACE).append(bundle.getString("role.kioskowner"))
        .append(CharacterConstants.SPACE)
        .append(CharacterConstants.EQUALS).append(CharacterConstants.SPACE)
        .append(SecurityConstants.ROLE_KIOSKOWNER).append(CharacterConstants.C_BRACKET)
        .append(CharacterConstants.COMMA)
        .append(bundle.getString("user.firstname")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.lastname")).append(CharacterConstants.COMMA)
        .append(bundle.getString("enabled.upper")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.mobile")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.landline")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.email")).append(CharacterConstants.COMMA)
        .append(bundle.getString("country")).append(CharacterConstants.COMMA)
        .append(bundle.getString("state")).append(CharacterConstants.COMMA)
        .append(bundle.getString("district")).append(CharacterConstants.COMMA)
        .append(bundle.getString("taluk")).append(CharacterConstants.COMMA)
        .append(bundle.getString("village")).append(CharacterConstants.COMMA)
        .append(bundle.getString("streetaddress")).append(CharacterConstants.COMMA)
        .append(bundle.getString("zipcode")).append(CharacterConstants.COMMA)
        .append(bundle.getString("language")).append(CharacterConstants.COMMA)
        .append(bundle.getString("preferredtimezone")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.gender")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.age")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.O_BRACKET).append(jsBundle.getString("years.lower"))
        .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
        .append(bundle.getString("tags")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.mobilebrand")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.mobilemodel")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.imei")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.mobileoperator")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.simId")).append(CharacterConstants.COMMA)
        .append(bundle.getString("primaryentity")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
        .append(jsBundle.getString("customid.primaryentity")).append(CharacterConstants.COMMA)
        .append(bundle.getString("primaryentity")).append(CharacterConstants.COMMA)
        .append(bundle.getString("user.mobileappversion")).append(CharacterConstants.COMMA)
        .append(bundle.getString("lastlogin")).append(CharacterConstants.COMMA)
        .append(bundle.getString("lastmobileaccess")).append(CharacterConstants.COMMA)
        .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
        .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("customid.lower")).append(CharacterConstants.COMMA)
        .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("fullname.lower")).append(CharacterConstants.COMMA)
        .append(bundle.getString("createdon")).append(CharacterConstants.COMMA)
        .append(bundle.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
        .append(bundle.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("customid.lower")).append(CharacterConstants.COMMA)
        .append(bundle.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("fullname.lower")).append(CharacterConstants.COMMA)
        .append(bundle.getString("updatedon"));
    return header.toString();
  }


}
