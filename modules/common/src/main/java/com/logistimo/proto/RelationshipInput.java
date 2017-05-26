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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author vani
 */
public class RelationshipInput extends InputMessageBean implements JsonBean {
  private String kioskId = null; // Kiosk id of the kiosk for which the relationship is being added.
  private String linkType = null;
  private Vector linkedKiosks = null;
  private Vector linkedKioskIdsRm = null;

  public RelationshipInput() {

  }

  public RelationshipInput(String kid, String lnkType, Vector lnkKsks, Vector lnkKskIdsRm) {
    this.kioskId = kid;
    this.linkType = lnkType;
    this.linkedKiosks = lnkKsks;
    this.linkedKioskIdsRm = lnkKskIdsRm;
  }

  public String getKioskId() {
    return kioskId;
  }

  public void setKioskId(String kid) {
    this.kioskId = kid;
  }

  public String getLinkType() {
    return linkType;
  }

  public void setLinkType(String lnkType) {
    this.linkType = lnkType;
  }

  public Vector getLinkedKiosks() {
    return linkedKiosks;
  }

  public void setLinkedKiosks(Vector lnkKsks) {
    this.linkedKiosks = lnkKsks;
  }

  public Vector getLinkedKioskIdsRm() {
    return linkedKioskIdsRm;
  }

  public void setLinkedKioskIdsRm(Vector lnkKsksRm) {
    this.linkedKioskIdsRm = lnkKsksRm;
  }

  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject jsonObject = new JSONObject(jsonString);
      // Set the kiosk id
      this.kioskId = (String) jsonObject.getString(JsonTagsZ.KIOSK_ID);
      // Set the link type
      this.linkType = (String) jsonObject.getString(JsonTagsZ.LINK_TYPE);
      // Set the Vector of Linked Kiosks
      loadLinkedKiosksData(jsonObject);
      // Set the Vector LinkedKiosksIdsRm
      loadLinkedKioskIdsRmData(jsonObject);

    } catch (JSONException je) {
      throw new ProtocolException(je.getMessage());
    }
  }

  public String toJSONString() throws ProtocolException {
    try {
      JSONObject json = new JSONObject();
      // Add version
      json.put(JsonTagsZ.VERSION, this.version);
      // Add kiosk id
      json.put(JsonTagsZ.KIOSK_ID, this.kioskId);
      // Add linkedKiosksData
      addLinkedKiosksData(json);

      if (this.linkedKioskIdsRm != null || !this.linkedKioskIdsRm.isEmpty()) {
        // Add linkedKioskIdsRm
        addLinkedKioskIdsRmData(json);
      }
      return json.toString();
    } catch (JSONException je) {
      throw new ProtocolException(je.getMessage());
    }

  }

  private void loadLinkedKiosksData(JSONObject jsonObject) throws JSONException {
    this.linkedKiosks = new Vector();
    JSONArray array = (JSONArray) jsonObject.get(JsonTagsZ.LINKED_KIOSKS);
    for (int i = 0; i < array.length(); i++) {
      // Get the linked kiosk
      JSONObject lki = (JSONObject) array.get(i);
      Hashtable linkedKiosk = new Hashtable();
      linkedKiosk.put(JsonTagsZ.LINKED_KIOSK_ID, (String) lki.get(JsonTagsZ.LINKED_KIOSK_ID));
      // Now put optional attributes into the linked kiosk hashtable
      // Description
      try {
        linkedKiosk.put(JsonTagsZ.DESCRIPTION, (String) lki.get(JsonTagsZ.DESCRIPTION));
      } catch (JSONException e) {
        // ignore
      }

      // Credit Limit
      try {
        linkedKiosk.put(JsonTagsZ.CREDIT_LIMIT, (String) lki.get(JsonTagsZ.CREDIT_LIMIT));
      } catch (JSONException e) {
        // ignore
      }

      // Add the liunkedKiosk to the Vector of linkedKiosk Hashtables
      this.linkedKiosks.addElement(linkedKiosk);
    }
  }

  private void loadLinkedKioskIdsRmData(JSONObject jsonObject) {
    this.linkedKioskIdsRm = new Vector();
    try {
      JSONArray array = (JSONArray) jsonObject.get(JsonTagsZ.LINKED_KIOSK_IDS_TOBEREMOVED);
      for (int i = 0; i < array.length(); i++) {
        this.linkedKioskIdsRm.addElement(array.get(i));
      }
    } catch (JSONException e) {
      // ignore
    }
  }

  private void addLinkedKiosksData(JSONObject json) throws JSONException {
    if (this.linkedKiosks == null || this.linkedKiosks.isEmpty()) {
      throw new JSONException("No Kiosk Ids specified");
    }
    Enumeration linkedKiosks = this.linkedKiosks.elements();
    while (linkedKiosks.hasMoreElements()) {
      Hashtable linkedKiosk = (Hashtable) linkedKiosks.nextElement();
      // Form the linkedKiosk JSON
      JSONObject lk = new JSONObject();
      lk.put(JsonTagsZ.LINKED_KIOSK_ID, linkedKiosk.get(JsonTagsZ.LINKED_KIOSK_ID));
      // Description is optional. Add it if present
      String description = (String) linkedKiosk.get(JsonTagsZ.DESCRIPTION);
      if (description != null && !description.equals("")) {
        lk.put(JsonTagsZ.DESCRIPTION, description);
      }

      // Credit limit is optional. Add it if present
      String creditLimit = (String) linkedKiosk.get(JsonTagsZ.CREDIT_LIMIT);
      if (creditLimit != null && !creditLimit.equals("")) {
        lk.put(JsonTagsZ.CREDIT_LIMIT, creditLimit);
      }

    }

  }

  private void addLinkedKioskIdsRmData(JSONObject json) throws JSONException {
    JSONArray array = new JSONArray();
    Enumeration linkedKioskIdsRmEnum = this.linkedKioskIdsRm.elements();
    while (linkedKioskIdsRmEnum.hasMoreElements()) {
      String lkIdRm = (String) linkedKioskIdsRmEnum.nextElement();
      if (lkIdRm != null && !lkIdRm.equals("")) {
        array.put(lkIdRm);
      }
    }
    // Add the array to json
    json.put(JsonTagsZ.LINKED_KIOSK_IDS_TOBEREMOVED, array);
  }

  public Vector toMessageString() throws ProtocolException {
    throw new ProtocolException("Not supported yet.");
  }

  public void fromMessageString(Vector messages) throws ProtocolException {
    throw new ProtocolException("Not supported yet.");
  }
}
