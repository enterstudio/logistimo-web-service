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

package com.logistimo.entities.actions;

import com.logistimo.entities.entity.IApprovers;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.exception.SystemException;
import com.logistimo.services.impl.PMF;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;

/**
 * Created by naveensnair on 31/07/17.
 */
@Component
public class UpdateApproversAction {

  private EntitiesService entitiesService;

  @Autowired
  public UpdateApproversAction(EntitiesService entitiesService){
    this.entitiesService = entitiesService;
  }

  public void invoke(Long kioskId, List<IApprovers> newApprovers, String userName) {
    if (kioskId == null || newApprovers == null) {
      throw new IllegalArgumentException("Invalid parameters for adding Approvers");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IApprovers> approvers = entitiesService.getApprovers(kioskId, pm);
    List<IApprovers> deleteApproversList = getDeleteApproversList(approvers, newApprovers);
    List<IApprovers> persistApproversList = getPersistApproversList(approvers, newApprovers);
    try {
      deleteApprovers(deleteApproversList, pm);
      persistApprovers(persistApproversList, pm, userName);
      generateEvent();
    } catch (JDOException e) {
      throw new SystemException(e, "Could not persist approver for entity " + kioskId);
    } finally {
      pm.close();
    }
  }

  private void generateEvent() {
    //TODO : generate entity modified event
  }

  /**
   * Returns the list of approvers to be deleted
   * @param existingApprovers
   * @param newApprovers
   * @return
   */
  private List<IApprovers> getDeleteApproversList(List<IApprovers> existingApprovers,
                                                  List<IApprovers> newApprovers) {
    return existingApprovers.stream()
        .filter(approver -> !newApprovers.stream().anyMatch(
            newApprover -> approver.getUserId().equals(newApprover.getUserId()) && approver
                .getType().equals(newApprover.getType())
                && approver.getOrderType().equals(newApprover.getOrderType())))
        .collect(Collectors.toList());
  }

  /**
   * Delete the approvers from db which are not in the model.
   * @param approvers
   * @param pm
   */
  public void deleteApprovers(List<IApprovers> approvers, PersistenceManager pm) {
    if(!approvers.isEmpty()) {
      pm.deletePersistentAll(approvers);
    }
  }

  /**
   * Persist the approvers which are new and update the one which already exists
   * @param approvers
   * @param pm
   * @param userName
   */
  public void persistApprovers(List<IApprovers> approvers, PersistenceManager pm, String userName) {
    if(!approvers.isEmpty()) {
      pm.makePersistentAll(approvers.stream()
          .map(apr -> {
            apr.setUpdatedBy(userName);
            apr.setCreatedOn(new Date());
            return apr;
          })
          .collect(Collectors.toList()));
    }
  }

  /**
   * Get the list of approvers to be persisted in db
   * @param existingApprovers
   * @param newApprovers
   * @return
   */
  private List<IApprovers> getPersistApproversList(List<IApprovers> existingApprovers,
                                                   List<IApprovers> newApprovers) {
    return newApprovers.stream()
        .filter(approver -> !existingApprovers.stream().anyMatch(
            existingApprover -> approver.getUserId().equals(existingApprover.getUserId()) && approver
                .getType().equals(existingApprover.getType())
                && approver.getOrderType().equals(existingApprover.getOrderType())))
        .collect(Collectors.toList());
  }


}
