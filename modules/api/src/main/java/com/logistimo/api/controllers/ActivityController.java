package com.logistimo.api.controllers;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.service.ActivityService;
import com.logistimo.activity.service.impl.ActivityServiceImpl;
import com.logistimo.activity.builders.ActivityBuilder;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.activity.models.ActivityModel;
import com.logistimo.api.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by kumargaurav on 07/10/16.
 */

@Controller
@RequestMapping("/activity")
public class ActivityController {

  private static final XLog xLogger = XLog.getLog(ActivityController.class);
  private ActivityBuilder builder = new ActivityBuilder();

  @RequestMapping(value = "/", method = RequestMethod.POST)
  public
  @ResponseBody
  ActivityModel createActivity(@RequestBody ActivityModel model,
                               HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    ActivityService activityService = null;
    ActivityModel retmodel = null;
    try {
      activityService = Services.getService(ActivityServiceImpl.class);
      IActivity activity = builder.buildActivity(model);
      activity = activityService.createActivity(activity);
      retmodel = builder.buildModel(activity);

    } catch (ServiceException se) {
      xLogger.warn("Error while creating activity {0}", model, se);
      throw new InvalidServiceException(backendMessages.getString("activity.create.error"));
    } catch (Exception e) {
      xLogger.warn("Error while creating activity {0}", model, e);
      throw new InvalidServiceException(backendMessages.getString("activity.create.error"));
    }
    return retmodel;
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getActivity(@RequestParam(required = false) String objectId,
                      @RequestParam(required = false) String objectType,
                      @RequestParam(required = false) String from,
                      @RequestParam(required = false) String to,
                      @RequestParam(required = false) String userId,
                      @RequestParam(required = false) String tag,
                      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
                      HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    try {
      PageParams pageParams = new PageParams(offset, size);
      ActivityService activityService = Services.getService(ActivityServiceImpl.class);
      Results
          results =
          activityService.getActivity(objectId, objectType, from == null ? null
                  : LocalDateUtil
                      .parseCustom(from, Constants.DATETIME_CSV_FORMAT, sUser.getTimezone()),
              to == null ? null : LocalDateUtil
                  .parseCustom(to, Constants.DATETIME_CSV_FORMAT, sUser.getTimezone()), userId, tag,
              pageParams);
      SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT);
      for (Object o : results.getResults()) {
        ActivityModel activity = (ActivityModel) o;
        if (activity.createDate != null) {
          Date cd = sdf.parse(activity.createDate);
          activity.createDate = LocalDateUtil.format(cd, sUser.getLocale(), sUser.getTimezone());
        }
      }
      return results;
    } catch (Exception e) {
      xLogger.warn("Error while getting activity {0}", e);
      throw new InvalidServiceException(e);
    }
  }
}
