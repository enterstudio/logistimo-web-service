package com.logistimo.api.models.superdomains;

/**
 * Created by naveensnair on 09/09/15.
 */
public class DomainPermissionModel {
  public Long did;
  public Boolean uv; // userView
  public Boolean ua; // userAdd
  public Boolean ue; // userEdit
  public Boolean ur; // userRemove
  public Boolean ev; // entityView
  public Boolean ea; // entityAdd
  public Boolean ee; // entityEdit
  public Boolean er; // entityRemove
  public Boolean egv; // entityGroupView
  public Boolean ega; // entityGroupAdd
  public Boolean ege; // entityGroupEdit
  public Boolean egr; // entityGroupRemove
  public Boolean erv; // entityGroupView
  public Boolean era; // entityGroupAdd
  public Boolean ere; // entityGroupEdit
  public Boolean err; // entityGroupRemove
  public Boolean iv; // inventoryView
  public Boolean ia; // inventoryAdd
  public Boolean ie; // inventoryEdit
  public Boolean ir; // inventoryRemove
  public Boolean mv; // materialView
  public Boolean ma; // materialAdd
  public Boolean me; // materialEdit
  public Boolean mr; // materialRemove
  public Boolean cm; // copyMaterials
  public Boolean cc; // copyConfiguration
  public Boolean cv; // viewConfiguration
  public Boolean ce; // editConfiguration
  /**
   * Asset edit
   */
  public Boolean ae;
  /**
   * Asset remove
   */
  public Boolean ar;
  /**
   * Asset add
   */
  public Boolean aa;
  /**
   * Asset view
   */

  public Boolean av;

  /**
   * View only permission
   */

  public Boolean vp;
}
