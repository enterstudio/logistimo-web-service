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

package com.logistimo.domains.entity;

/**
 * Created by naveensnair on 08/09/15.
 */
public interface IDomainPermission {

  Long getdId();

  void setdId(Long dId);

  Boolean isUsersView();

  void setUsersView(Boolean iuv);

  Boolean isUsersAdd();

  void setUsersAdd(Boolean iua);

  Boolean isUsersEdit();

  void setUsersEdit(Boolean iue);

  Boolean isUsersRemove();

  void setUsersRemove(Boolean iur);

  Boolean isEntityView();

  void setEntityView(Boolean iev);

  Boolean isEntityAdd();

  void setEntityAdd(Boolean iea);

  Boolean isEntityEdit();

  void setEntityEdit(Boolean iee);

  Boolean isEntityRemove();

  void setEntityRemove(Boolean ier);

  Boolean isEntityGroupView();

  void setEntityGroupView(Boolean iegv);

  Boolean isEntityGroupAdd();

  void setEntityGroupAdd(Boolean iega);

  Boolean isEntityGroupEdit();

  void setEntityGroupEdit(Boolean iege);

  Boolean isEntityGroupRemove();

  void setEntityGroupRemove(Boolean iegr);

  Boolean isEntityRelationshipView();

  void setEntityRelationshipView(Boolean erv);

  Boolean isEntityRelationshipAdd();

  void setEntityRelationshipAdd(Boolean era);

  Boolean isEntityRelationshipEdit();

  void setEntityRelationshipEdit(Boolean ere);

  Boolean isEntityRelationshipRemove();

  void setEntityRelationshipRemove(Boolean err);

  Boolean isInventoryView();

  void setInventoryView(Boolean iiv);

  Boolean isInventoryAdd();

  void setInventoryAdd(Boolean iia);

  Boolean isInventoryEdit();

  void setInventoryEdit(Boolean iie);

  Boolean isInventoryRemove();

  void setInventoryRemove(Boolean iir);

  Boolean isMaterialView();

  void setMaterialView(Boolean imv);

  Boolean isMaterialAdd();

  void setMaterialAdd(Boolean ima);

  Boolean isMaterialEdit();

  void setMaterialEdit(Boolean ime);

  Boolean isMaterialRemove();

  void setMaterialRemove(Boolean imr);

  Boolean isCopyConfiguration();

  void setCopyConfiguration(Boolean cc);

  Boolean isCopyMaterials();

  void setCopyMaterials(Boolean cm);

  Boolean isConfigurationView();

  void setConfigurationView(Boolean cv);

  Boolean isConfigurationEdit();

  void setConfigurationEdit(Boolean ce);

  Boolean isAssetEdit();

  void setAssetEdit(Boolean ae);

  Boolean isAssetRemove();

  void setAssetRemove(Boolean ar);

  Boolean isAssetAdd();

  void setAssetAdd(Boolean aa);

  Boolean isAssetView();

  void setAssetView(Boolean av);
}
