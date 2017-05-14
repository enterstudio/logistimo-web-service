package com.logistimo.dao;

import com.logistimo.domains.IMultiDomain;
import com.logistimo.services.ServiceException;

import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by Mohan Raja on 03/06/15.
 */
public interface IDaoUtil {

  // Remove the items with specified criteria from the board
  void removeBBItems(List<Long> bbItemIds);

  @SuppressWarnings({"unchecked", "rawtypes"})
  void updateTags(List<Long> domainIds, List<String> oldTags, List<String> newTags,
                  String type, Long entityId, PersistenceManager pm)
      throws ServiceException;

  Object createKeyFromString(String encodedString);

  List<String> getUploadedMessages(String uploadedKey);

  void deleteUploadedMessage(String uploadedKey);


  void setCursorExtensions(String cursorStr, Query query);

  String getCursor(List results);

  IMultiDomain getKioskDomains(Long kioskId) throws ServiceException;
}
