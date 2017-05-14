package com.logistimo.api.servlets.mobile.builders;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Services;

import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.proto.MobileConversationModel;
import com.logistimo.proto.MobileMessageModel;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;
import com.logistimo.conversations.models.MessageModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by vani on 04/11/16.
 */
public class MobileConversationBuilder {
  public static final String CONVERSATION_OBJECT_TYPE_ORDER = "ORDER";
  public static final String CONVERSATION_OBJECT_TYPE_SHIPMENT = "SHIPMENT";
  private static final XLog xLogger = XLog.getLog(MobileOrderBuilder.class);

  public MobileConversationModel build(String objectType, String objectId, Locale locale,
                                       String timezone) {
    MobileConversationModel mcm = new MobileConversationModel();
    try {
      ConversationService cs = Services.getService(ConversationServiceImpl.class);
      PageParams pp = new PageParams(0, 10);
      Results res = cs.getMessages(null, objectType, objectId, pp);
      Results allMsgs = cs.getMessagesCount(null, objectType, objectId, pp);
      int count = 0;
      if (allMsgs != null) {
        List<MessageModel> fullList = res.getResults();
        if (fullList != null && !fullList.isEmpty()) {
          count = fullList.size();
        }
      }
      List<MessageModel> mmList;
      List<MobileMessageModel> mmmList = null;
      if (res != null) {
        mmList = res.getResults();
        if (mmList != null && !mmList.isEmpty()) {
          mmmList = new ArrayList<>(1);
          for (MessageModel mm : mmList) {
            MobileMessageModel mmm = buildMobileMessageModel(mm, locale, timezone);
            if (mmm != null) {
              mmmList.add(mmm);
            }
          }
        }
      }
      if (mmmList != null && !mmmList.isEmpty()) {
        mcm.msgs = mmmList;
      }
      mcm.cnt = count;
    } catch (Exception e) {
      xLogger.warn("Exception while getting conversations for object type: {0}, object id: {1}",
          objectType, objectId, e);
    }
    return mcm;
  }

  MobileMessageModel buildMobileMessageModel(MessageModel mm, Locale locale, String timezone) {
    if (mm == null) {
      return null;
    }
    MobileMessageModel mmm = new MobileMessageModel();
    mmm.msg = mm.message;
    mmm.uid = mm.userId;
    mmm.n = mm.userName;
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT);
    try {
      if (mm.updateDate != null) {
        Date ud = sdf.parse(mm.updateDate);
        mmm.t = LocalDateUtil.format(ud, locale, timezone);
      } else {
        Date cd = sdf.parse(mm.createDate);
        mmm.t = LocalDateUtil.format(cd, locale, timezone);
      }
    } catch (Exception e) {
      xLogger.warn("Exception while building mobile message model", e);
    }
    return mmm;
  }

}
