package com.logistimo.approvals.client.command;

import com.logistimo.approvals.builders.RestResponsePageBuilder;
import com.logistimo.utils.GsonUtils;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by charan on 24/06/17.
 */
public class GetFilteredApprovalsCommandTest {

  @Test
  public void testExecute() {
    String approvals = GsonUtils.toJson(new RestResponsePageBuilder().build());
    assertNotNull(approvals);
  }

}