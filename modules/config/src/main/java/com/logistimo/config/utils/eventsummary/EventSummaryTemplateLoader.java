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

package com.logistimo.config.utils.eventsummary;

import com.google.gson.Gson;

import com.logistimo.config.models.EventSummaryConfigModel;
import com.logistimo.logger.XLog;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;


public class EventSummaryTemplateLoader {

  private EventSummaryTemplateLoader() {

  }

  private static final XLog xLogger = XLog.getLog(
      EventSummaryTemplateLoader.class);

  private static final String EVENT_SUMMARY_TEMPLATE_FILENAME = "eventSummaryTemplate.json";

  private static EventSummaryConfigModel eventSummaryConfigModel;

  /**
   * Load the template file with default contents
   *
   * @return json string
   */
  public static EventSummaryConfigModel getDefaultTemplate() {
    if (eventSummaryConfigModel==null) {
      //load the template json file
      try(InputStream inputStream=
              Thread.currentThread().getContextClassLoader()
                  .getResourceAsStream(EVENT_SUMMARY_TEMPLATE_FILENAME)) {
        eventSummaryConfigModel=new Gson().fromJson(IOUtils.toString(inputStream),EventSummaryConfigModel.class);
      } catch (IOException e) {
        xLogger.warn("Exception loading the template", e);
      }
    }
    return eventSummaryConfigModel;
  }

}
