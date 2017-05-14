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
