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

package com.logistimo.dashboards.service;

import com.logistimo.dashboards.entity.IDashboard;
import com.logistimo.dashboards.entity.IWidget;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Mohan Raja
 */
public interface IDashboardService extends Service {
  void createDashboard(IDashboard ds) throws ServiceException;

  void createWidget(IWidget wid) throws ServiceException;

  List<IDashboard> getDashBoards(Long domainId);

  List<IWidget> getWidgets(Long domainId);

  IDashboard getDashBoard(Long dbId) throws ServiceException;

  IWidget getWidget(Long wId) throws ServiceException;

  String deleteDashboard(Long id) throws ServiceException;

  String deleteWidget(Long id) throws ServiceException;

  String setDefault(Long oid, Long id) throws ServiceException;

  String updateDashboard(Long id, String ty, String val) throws ServiceException;

  String updateWidget(Long id, String ty, String val) throws ServiceException;

  void updateWidgetConfig(IWidget widget) throws ServiceException;

  ResultSet getMainDashboardResults(Long domainId, Map<String, String> filters, String type);

  ResultSet getMainDashboardResults(Long domainId, Map<String, String> filters, String type,
                                    boolean isCountOnly, String groupby);

  Integer getInvTotalCount(Map<String, String> filters) throws SQLException;

}
