package com.logistimo.config.models;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;

/**
 * Created by charan on 22/06/17.
 */
public class OrderConfigTest {

  @Test
  public void testIsPurchaseApprovalEnabled() throws Exception {
    ApprovalsConfig config = new ApprovalsConfig();
    assertFalse(config.getOrderConfig().isPurchaseApprovalEnabled(new ArrayList<>(1)));
  }

  @Test
  public void testIsSaleApprovalEnabled() throws Exception {
    ApprovalsConfig config = new ApprovalsConfig();
    assertFalse(config.getOrderConfig().isSaleApprovalEnabled(new ArrayList<>(1)));
  }
}