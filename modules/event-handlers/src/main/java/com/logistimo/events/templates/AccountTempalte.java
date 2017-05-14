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

package com.logistimo.events.templates;

import com.logistimo.accounting.entity.IAccount;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.entities.entity.KioskLink;
import com.logistimo.services.impl.PMF;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 10/03/17.
 */
public class AccountTempalte implements ITemplate {
  private final IAccount account;

  public AccountTempalte(IAccount account) {
    this.account = account;
  }

  @Override
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updationTime) {
    HashMap<String, String> varMap = new HashMap<String, String>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IKiosk k = null;
      // Custom info.
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMER)) {
        try {
          k = pm.getObjectById(Kiosk.class, account.getCustomerId());
          varMap.put(EventsConfig.VAR_CUSTOMER, k.getName());
        } catch (Exception e) {
          // ignore
        }
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMERCITY)) {
        try {
          if (k == null) {
            k = pm.getObjectById(Kiosk.class, account.getCustomerId());
          }
          varMap.put(EventsConfig.VAR_CUSTOMERCITY, k.getCity());
        } catch (Exception e) {
          // ignore
        }
      }
      // Vendor info.
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_VENDOR)) {
        try {
          k = pm.getObjectById(Kiosk.class, account.getVendorId());
          varMap.put(EventsConfig.VAR_VENDOR, k.getName());
        } catch (Exception e) {
          // ignore
        }
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_VENDORCITY)) {
        try {
          if (k == null) {
            k = pm.getObjectById(Kiosk.class, account.getVendorId());
          }
          varMap.put(EventsConfig.VAR_VENDORCITY, k.getCity());
        } catch (Exception e) {
          // ignore
        }
      }
      // Account info. - payable
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_PAYABLE)) {
        varMap.put(EventsConfig.VAR_PAYABLE, String.valueOf(account.getPayable()));
      }
      // Credit limit
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CREDITLIMIT)) {
        try {
          IKioskLink
              kl =
              pm.getObjectById(KioskLink.class,
                  JDOUtils.createKioskLinkId(account.getVendorId(),
                      IKioskLink.TYPE_CUSTOMER, account.getCustomerId()));
          varMap.put(EventsConfig.VAR_CREDITLIMIT, String.valueOf(kl.getCreditLimit()));
        } catch (Exception e) {
          // ignore
        }
      }
    } finally {
      // Close PM
      pm.close();
    }
    return varMap;
  }
}
