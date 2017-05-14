package com.logistimo.api.builders;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.entity.IMessageLog;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.models.UserMessageModel;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;

import java.util.Locale;

/**
 * Created by mohan raja.
 */
public class UserMessageBuilder {

  final static int MAX_MESSAGE_SIZE = 160;

  public UserMessageModel buildUserMessageModel(IMessageLog messageLog, UsersService as,
                                                Locale locale, String userId, int offset,
                                                String timezone)
      throws ServiceException, ObjectNotFoundException, MessageHandlingException {
    IUserAccount u = as.getUserAccount(userId);
    MessageService smsService = MessageService.getInstance(MessageService.SMS, u.getCountry());
    MessageService emailService = MessageService.getInstance(MessageService.EMAIL, u.getCountry());

    IUserAccount sender = null;
    IUserAccount user = null;

    try {
      sender = as.getUserAccount(messageLog.getSenderId());
    } catch (Exception ignored) {
      // ignore
    }

    try {
      user = as.getUserAccount(messageLog.getUserId());
    } catch (Exception ignored) {
      // ignore
    }

    MessageService ms;
    if (MessageService.SMS.equals(messageLog.getType())) {
      ms = smsService;
    } else {
      ms = emailService;
    }

    String msg = messageLog.getMessage();
    if (msg != null && msg.length() > MAX_MESSAGE_SIZE) {
      msg = msg.substring(0, MAX_MESSAGE_SIZE) + "...";
    }

    UserMessageModel model = new UserMessageModel();
    model.sno = offset;
    if (null != user) {
      model.sto = user.getFullName() + " (" + user.getMobilePhoneNumber() + ")";
    }
    model.tm = LocalDateUtil.format(messageLog.getTimestamp(), locale, timezone);
    model.sta = ms.getStatusMessage(messageLog.getStatus(), locale);
    if (sender != null) {
      model.sby = sender.getFullName();
    }
    model.text = msg;
    if (messageLog.getEventType() != null && !messageLog.getEventType().isEmpty()) {
      model.evtp = messageLog.getEventType();
    }
    return model;
  }

/*    public List<UserMessageModel> buildUserMessageModels(List<MessageLog> messageLogs, AccountsService as, Locale locale, String userId, int offset, String timezone)
            throws ServiceException, ObjectNotFoundException, MessageHandlingException {
        List<UserMessageModel> models = new ArrayList<UserMessageModel>(messageLogs.size());
        for (MessageLog messageLog : messageLogs) {
            models.add(buildUserMessageModel(messageLog,as,locale,userId,offset,timezone));
        }
        return models;
    }*/
}
