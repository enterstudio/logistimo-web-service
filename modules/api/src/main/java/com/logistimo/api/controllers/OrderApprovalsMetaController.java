package com.logistimo.api.controllers;

import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.services.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

/**
 * Created by charan on 15/07/17.
 */
@Controller
@RequestMapping("/order-approvals-meta")
public class OrderApprovalsMetaController {

  @Autowired
  private IOrderApprovalsService orderApprovalsService;

  @RequestMapping(value = "/requesters", method = RequestMethod.GET)
  public
  @ResponseBody
  Collection<String> findRequesters(@RequestParam String q) {

    return orderApprovalsService.findRequesters(q, SecurityUtils.getCurrentDomainId());
  }

  @RequestMapping(value = "/approvers", method = RequestMethod.GET)
  public
  @ResponseBody
  Collection<String> findApprovers(@RequestParam String q) throws ServiceException {
    return orderApprovalsService.findApprovers(q, SecurityUtils.getCurrentDomainId());
  }

}
