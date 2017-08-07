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

package com.logistimo.orders.actions;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.OrderFilters;
import com.logistimo.pagination.QueryParams;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.service.UsersService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charan on 20/07/17.
 */
@Component
public class GetFilteredOrdersQueryAction {

  @Autowired
  private ITagDao tagDao;

  @Autowired
  private UsersService usersService;

  public QueryParams invoke(OrderFilters filters) {
    if (filters.getKioskId() == null && filters.getDomainId() == null) {
      throw new IllegalArgumentException(
          "No kiosk or domain specified. At least one of them must be specified");
    }

    StringBuilder sqlQuery = new StringBuilder("SELECT * FROM `ORDER` WHERE ");
    StringBuilder filterQuery = new StringBuilder();
    List<String> parameters = new ArrayList<>(1);
    if (filters.getKioskId() != null) {
      applyKioskFilter(filterQuery, parameters, filters);
    } else if (!filters.getKioskIds().isEmpty()) {
      applyKioskIdsFilter(filterQuery, parameters, filters);
    } else {
      if (StringUtils.isNotBlank(filters.getUserId()) && !isAdmin(filters.getUserId())) {
        applyUserAccessibleKiosks(filterQuery, parameters, filters);
      } else {
        applyDomainFilter(filterQuery, parameters, filters);
      }
    }

    applyLinkedKioskFilter(filterQuery, parameters, filters);

    applyStatusFilters(filterQuery, parameters, filters);

    applyTagFilters(filterQuery, parameters, filters);

    applyDateFilters(filterQuery, parameters, filters);

    applyOrderTypeFilter(filterQuery, filters);

    applyReferenceIdFilter(filterQuery, parameters, filters);

    applyApprovalStatusFilter(filterQuery, parameters, filters);

    sqlQuery.append(filterQuery);
    sqlQuery.append(" ORDER BY CON DESC");
    return new QueryParams(sqlQuery.toString(), parameters,
        QueryParams.QTYPE.SQL, IOrder.class, filterQuery.toString());

  }

  private void applyLinkedKioskFilter(StringBuilder filterQuery, List<String> parameters,
                                      OrderFilters filters) {
    if (filters.getLinkedKioskId() != null) {
      filterQuery.append(" AND SKID = ?");
      parameters.add(String.valueOf(filters.getLinkedKioskId()));
    }
  }

  private boolean isAdmin(String userId) {
    try {
      return SecurityUtil.compareRoles(usersService.getUserAccount(userId).getRole(),
          SecurityConstants.ROLE_DOMAINOWNER) >= 0;
    } catch (ObjectNotFoundException e) {
      throw new IllegalArgumentException("User is not found :" + userId);
    }
  }

  private void applyUserAccessibleKiosks(StringBuilder filterQuery, List<String> parameters,
                                         OrderFilters filters) {
    filterQuery.append(getKioskField(filters))
        .append(" IN (SELECT KIOSKID FROM USERTOKIOSK WHERE USERID = ?)").append(" AND ")
        .append(getVisibilityField(filters)).append(" = 1");
    parameters.add(filters.getUserId());
  }

  private void applyApprovalStatusFilter(StringBuilder filterQuery, List<String> parameters,
                                         OrderFilters filters) {
    if (StringUtils.isNotBlank(filters.getApprovalStatus())) {
      filterQuery.append(
          " AND `ID` IN (SELECT ORDER_ID FROM ORDER_APPROVAL_MAPPING WHERE LATEST = 1 "
              + "AND STATUS = ? ");
      parameters.add(filters.getApprovalStatus());
      if (filters.getOrderType() == IOrder.TRANSFER) {
        filterQuery.append("AND APPROVAL_TYPE = 0");
      } else if (filters.getKioskId() != null && StringUtils.isNotBlank(filters.getOtype())) {
        filterQuery.append("AND APPROVAL_TYPE = ?");
        if (IOrder.TYPE_SALE.equals(filters.getOtype())) {
          parameters.add(String.valueOf(IOrder.SALES_ORDER));
        } else {
          parameters.add(String.valueOf(IOrder.PURCHASE_ORDER));
        }
      } else {
        filterQuery.append("AND APPROVAL_TYPE <> 0");
      }
      filterQuery.append(")");
    }
  }

  private void applyReferenceIdFilter(StringBuilder sqlQuery, List<String> parameters,
                                      OrderFilters filters) {
    if (StringUtils.isNotBlank(filters.getReferenceId())) {
      sqlQuery.append(" AND RID = ?");
      parameters.add(filters.getReferenceId());
    }
  }

  private void applyOrderTypeFilter(StringBuilder sqlQuery,
                                    OrderFilters filters) {
    if (filters.getOrderType() != null) {
      if (filters.getOrderType() == IOrder.TRANSFER) {
        sqlQuery.append(" AND OTY = 0");
      } else {
        sqlQuery.append(" AND OTY <> 0");
      }
    }
  }

  private void applyDateFilters(StringBuilder sqlQuery, List<String> parameters,
                                OrderFilters filters) {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    if (filters.getSince() != null) {
      sqlQuery.append(" AND CON >= ?");
      parameters.add(sdf.format(filters.getSince()));
    }
    if (filters.getUntil() != null) {
      sqlQuery.append(" AND CON < ?");
      parameters.add(sdf.format(filters.getSince()));
    }
  }

  private void applyTagFilters(StringBuilder sqlQuery, List<String> parameters,
                               OrderFilters filters) {
    if (StringUtils.isBlank(filters.getTag())) {
      return;
    }
    if (TagUtil.TYPE_ENTITY.equals(filters.getTagType())) {
      if (filters.getKioskId() != null) {
        throw new IllegalArgumentException("Store and store tags cannot be applied together");
      }
      sqlQuery.append(" AND EXISTS(SELECT 1 from KIOSK_TAGS where ID = ")
          .append(CharacterConstants.QUESTION)
          .append(" AND (KIOSKID = SKID or KIOSKID = KID)) ");
      parameters.add(String.valueOf(tagDao.getTagFilter(filters.getTag(), ITag.KIOSK_TAG)));
    } else if (TagUtil.TYPE_ORDER.equals(filters.getTagType())) {
      sqlQuery.append(" AND `ID` IN (SELECT `KEY` from ORDER_TAGS where ID = ")
          .append(CharacterConstants.QUESTION)
          .append(") ");
      parameters.add(String.valueOf(tagDao.getTagFilter(filters.getTag(), ITag.ORDER_TAG)));
    }
  }

  private void applyStatusFilters(StringBuilder sqlQuery, List<String> parameters,
                                  OrderFilters filters) {
    if (StringUtils.isNotBlank(filters.getStatus())) {
      sqlQuery.append(" AND ST = ?");
      parameters.add(filters.getStatus());
    }
  }

  private void applyDomainFilter(StringBuilder sqlQuery, List<String> parameters,
                                 OrderFilters filters) {
    sqlQuery.append("`ID` in (SELECT ID_OID FROM ORDER_DOMAINS WHERE DOMAIN_ID = ?)");
    parameters.add(String.valueOf(filters.getDomainId()));
  }

  private void applyKioskFilter(StringBuilder sqlQuery, List<String> parameters,
                                OrderFilters filters) {
    sqlQuery.append(getKioskField(filters)).append(" = ?").append(" AND ")
        .append(getVisibilityField(filters)).append(" = 1");
    parameters.add(String.valueOf(filters.getKioskId()));
  }

  private String getKioskField(OrderFilters filters) {
    return IOrder.TYPE_SALE.equals(filters.getOtype()) ? "SKID" : "KID";
  }

  private String getVisibilityField(OrderFilters filters) {
    return IOrder.TYPE_SALE.equals(filters.getOtype()) ? "VTV" : "VTC";
  }

  private void applyKioskIdsFilter(StringBuilder sqlQuery, List<String> parameters,
                                   OrderFilters filters) {
    sqlQuery.append(getKioskField(filters)).append(" IN (");
    for (Long id : filters.getKioskIds()) {
      sqlQuery.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
      parameters.add(String.valueOf(id));
    }
    sqlQuery.setLength(sqlQuery.length() - 1);
    sqlQuery.append(")").append(" AND ")
        .append(getVisibilityField(filters)).append(" = 1");
  }
}
