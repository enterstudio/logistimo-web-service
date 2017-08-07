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

package com.logistimo.inventory.models;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.pagination.PageParams;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InventoryFilters {
  private Long kioskId;
  private Long materialId;
  private List<String> kioskTags;
  private List<String> excludedKioskTags;
  private List<String> materialTags;
  private List<Long> kioskIds;
  private PageParams pageParams = new PageParams(0, 50);
  private Long domainId;
  private String materialNameStartsWith;
  private int matType = IInvntry.ALL;
  private boolean onlyNonZeroStk;
  private LocationSuggestionModel location;
  private String pdos;
  private Set<Integer> eventTypes = new LinkedHashSet<>(1);
  private Long sourceDomainId;
  private boolean noInTransitStock;

  public Long getKioskId() {
    return kioskId;
  }

  public Long getMaterialId() {
    return materialId;
  }

  public List<String> getKioskTags() {
    return kioskTags;
  }

  public List<String> getExcludedKioskTags() {
    return excludedKioskTags;
  }

  public List<String> getMaterialTags() {
    return materialTags;
  }

  public List<Long> getKioskIds() {
    return kioskIds;
  }

  public PageParams getPageParams() {
    return pageParams;
  }

  public Long getDomainId() {
    return domainId;
  }

  public String getMaterialNameStartsWith() {
    return materialNameStartsWith;
  }

  public int getMatType() {
    return matType;
  }

  public boolean isOnlyNonZeroStk() {
    return onlyNonZeroStk;
  }

  public LocationSuggestionModel getLocation() {
    return location;
  }

  public String getPdos() {
    return pdos;
  }

  public Collection<Integer> getEventTypes() {
    return eventTypes;
  }

  public InventoryFilters withKioskId(Long kioskId) {
    this.kioskId = kioskId;
    return this;
  }

  public InventoryFilters withMaterialId(Long materialId) {
    this.materialId = materialId;
    return this;
  }

  public InventoryFilters withKioskTags(String kioskTags) {
    if (StringUtils.isNotBlank(kioskTags)) {
      this.kioskTags = Arrays.asList(kioskTags.split(CharacterConstants.COMMA));
    }
    return this;
  }

  public InventoryFilters withExcludedKioskTags(String excludedKioskTag) {
    if (StringUtils.isNotBlank(excludedKioskTag)) {
      this.excludedKioskTags = Arrays.asList(excludedKioskTag.split(CharacterConstants.COMMA));
    }
    return this;
  }

  public InventoryFilters withMaterialTags(String materialTag) {
    if (StringUtils.isNotBlank(materialTag)) {
      this.materialTags = Arrays.asList(materialTag.split(CharacterConstants.COMMA));
    }
    return this;
  }

  public InventoryFilters withKioskIds(List<Long> kioskIds) {
    this.kioskIds = kioskIds;
    return this;
  }

  public InventoryFilters withPageParams(PageParams pageParams) {
    this.pageParams = pageParams;
    return this;
  }

  public InventoryFilters withDomainId(Long domainId) {
    this.domainId = domainId;
    return this;
  }

  public InventoryFilters withMaterialNameStartsWith(String materialNameStartsWith) {
    this.materialNameStartsWith = materialNameStartsWith;
    return this;
  }

  public InventoryFilters withMatType(int matType) {
    this.matType = matType;
    return this;
  }

  public InventoryFilters withOnlyNonZeroStk(boolean onlyNonZeroStk) {
    this.onlyNonZeroStk = onlyNonZeroStk;
    return this;
  }

  public InventoryFilters withLocation(
      LocationSuggestionModel location) {
    this.location = location;
    return this;
  }

  public InventoryFilters withPdos(String pdos) {
    this.pdos = pdos;
    return this;
  }

  public InventoryFilters withEventType(Integer eventType) {
    this.eventTypes.add(eventType);
    return this;
  }

  public InventoryFilters withSourceDomainId(Long domainId) {
    this.sourceDomainId = domainId;
    return this;
  }

  public Long getSourceDomainId() {
    return sourceDomainId;
  }

  public InventoryFilters withMaterialTags(List<String> materialTags) {
    this.materialTags = materialTags;
    return this;
  }

  public InventoryFilters withKioskTags(List<String> kioskTags) {
    this.kioskTags = kioskTags;
    return this;
  }

  public InventoryFilters withNoIntransitStock() {
    this.noInTransitStock = true;
    return this;
  }

  public boolean isNoInTransitStock() {
    return noInTransitStock;
  }
}
