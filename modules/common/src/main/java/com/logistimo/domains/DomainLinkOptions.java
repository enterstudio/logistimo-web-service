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

package com.logistimo.domains;

import com.google.gson.annotations.Expose;

public class DomainLinkOptions {

  @Expose
  private boolean
      inheritMaterials =
      true;
  // inherit the materials of the parent domain to the linked domain
  @Expose
  private boolean copyConfig = true; // Copy configuration of the parent domain to the linked domain
  @Expose
  private boolean manageMaterials = true; // Allow management of materials in child
  @Expose
  private boolean manageConfiguration = true; // Allow management of configuration in child
  @Expose
  private boolean manageMasterData = true; // Allow management of master data in child
  @Expose
  private boolean manageUsers = true; // Allow management of users in child
  @Expose
  private boolean manageEntities = true; // Allow management of entities in child
  @Expose
  private boolean manageEntityGroups = true; // Allow management of entity groups in child

  public DomainLinkOptions() {
  }

  public DomainLinkOptions(boolean inheritMaterials, boolean copyConfig, boolean manageMaterials,
                           boolean manageConfiguration, boolean manageMasterData,
                           boolean manageUsers, boolean manageEntities,
                           boolean manageEntityGroups) {
    this.inheritMaterials = inheritMaterials;
    this.copyConfig = copyConfig;
    this.manageMaterials = manageMaterials;
    this.manageConfiguration = manageConfiguration;
    this.manageMasterData = manageMasterData;
    this.manageUsers = manageUsers;
    this.manageEntities = manageEntities;
    this.manageEntityGroups = manageEntityGroups;
  }

  public boolean inheritMaterials() {
    return inheritMaterials;
  }

  public boolean copyConfig() {
    return copyConfig;
  }

  public boolean manageMaterials() {
    return manageMaterials;
  }

  public boolean manageConfiguration() {
    return manageConfiguration;
  }

  public boolean manageMasterData() {
    return manageMasterData;
  }

  public boolean manageUsers() {
    return manageUsers;
  }

  public boolean manageEntities() {
    return manageEntities;
  }

  public boolean manageEntityGroups() {
    return manageEntityGroups;
  }

  public boolean doesSomething() {
    return inheritMaterials || copyConfig;
  }
}
