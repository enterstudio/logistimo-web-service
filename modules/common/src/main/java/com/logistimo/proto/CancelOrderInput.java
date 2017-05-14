/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

/**
 * @author Arun
 */
public class CancelOrderInput extends GetOrderInput {

  public CancelOrderInput() {
    cmd = RestConstantsZ.ACTION_CANCELORDER;
  }

  public CancelOrderInput(String version, String userId, String password, String orderId) {
    super(version, userId, password, orderId);
    cmd = RestConstantsZ.ACTION_CANCELORDER;
  }
}
