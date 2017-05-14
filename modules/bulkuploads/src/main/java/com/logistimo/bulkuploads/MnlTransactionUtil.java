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

package com.logistimo.bulkuploads;

import com.logistimo.AppFactory;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.orders.OrderResults;
import com.logistimo.orders.models.UpdatedOrder;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;

import com.logistimo.events.entity.IEvent;
import com.logistimo.entity.IUploaded;
import com.logistimo.entity.IUploadedMsgLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.UploadService;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.QueryUtil;
import com.logistimo.constants.SourceConstants;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 05/03/17.
 */
public class MnlTransactionUtil {

  private static final XLog xLogger = XLog.getLog(MnlTransactionUtil.class);

  private static ITransDao transDao = new TransDao();
  private static final int FULLYEAR_LENGTH = 4;


  public static Long saveOrderRowList(List<OrderRow> orderRowList, String type) {

    if (orderRowList == null || orderRowList.isEmpty()) {
      return null;
    }
    OrderRow orderRow = orderRowList.get(0);
    ITransaction trans = orderRow.orderTrans;
    Long domainId = trans.getDomainId();
    String userId = trans.getSourceUserId();
    Long kioskId = trans.getKioskId();
    Long linkedKioskId = trans.getLinkedKioskId();
    List<String> orderTags = orderRow.orderTags;
    List<ITransaction> transList = new ArrayList<>(orderRowList.size());
    for (OrderRow order : orderRowList) {
      transList.add(order.orderTrans);
    }

    BulkUploadMgr.EntityContainer ec = new BulkUploadMgr.EntityContainer();
    try {
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      IShipmentService shipmentService = Services.getService(ShipmentService.class);
      //Create order
      OrderResults
          or =
          oms.updateOrderTransactions(domainId, userId, ITransaction.TYPE_ORDER, transList, kioskId,
              null, null, true, linkedKioskId, null, null, null, null, null, null, BigDecimal.ZERO,
              null, null,
              false, orderTags, 1, false, null, null, null);
      IOrder order = or.getOrder();
      // Reset order quantities to fulfilled quantities
      transList.clear();
      boolean hasNonZeroQuantity = false;
      boolean modifyOrder = false;
      for (OrderRow o : orderRowList) {
        if (!modifyOrder && !BigUtil
            .equals(o.orderTrans.getQuantity(), o.mnlTransaction.getFulfilledQuantity())) {
          modifyOrder = true;
        }
        o.orderTrans.setQuantity(o.mnlTransaction.getFulfilledQuantity());
        transList.add(o.orderTrans);
        if (!hasNonZeroQuantity) {
          hasNonZeroQuantity = BigUtil.greaterThanZero(o.mnlTransaction.getFulfilledQuantity());
        }
      }
      if (hasNonZeroQuantity) {
        if (modifyOrder) {
          // Reset order quantities
          oms.modifyOrder(order, userId, transList, null, domainId, ITransaction.TYPE_ORDER, null,
              null, null,
              null, null, null, false, orderTags, null, null);
          UpdatedOrder
              updorder =
              oms.updateOrder(order, SourceConstants.UPLOAD,
                  false, true);
          order = updorder.order;
        }
        if (order.getServicingKiosk() != null) {
          // Mark order as shipped
          String shipmentId = oms.shipNow(order, null, null, null, null, userId, null);
          shipmentService.fulfillShipment(shipmentId, userId);
        }
      } else {
        //Cancel order.. no fulfilled quantities.
        order.setStatus(IOrder.CANCELLED);
        oms.updateOrderStatus(order.getOrderId(), IOrder.CANCELLED, userId, null, null,
            SourceConstants.UPLOAD);
      }
      return order.getOrderId();
    } catch (Exception e) {
      xLogger.severe("{0} when updating order for domain {1} user: {2} . Message: {3}",
          e.getClass().getName(),
          domainId, userId, e);
      ec.messages.add("System Error: " + e.getMessage());
    } finally {
      if (ec.hasErrors()) {
        // Send the rowNumber as jobId
        updateUploadedMsgLog(orderRow.offset, "Row " + orderRow.rowNumber + ": " + orderRow.line,
            ec, type,
            userId, domainId, String.valueOf(orderRow.rowNumber));
      }
    }
    return null;
  }

  private static void updateUploadedMsgLog(long offset, String csvLine, BulkUploadMgr.EntityContainer ec,
                                           String type, String sourceUserId, Long domainId,
                                           String jobIdStr) {
    // Get the uploaded object
    String uploadedKey = BulkUploadMgr.getUploadedKey(domainId, type, sourceUserId);

    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Form the text to be appended
    String
        lineMsg =
        BulkUploadMgr.getErrorMessageString(offset, csvLine, ec.operation, ec.getMessages());
    // Create an UploadedMsgLog
    //Key parentKey = KeyFactory.createKey( Uploaded.class.getSimpleName(), uploadedKey );
    IUploadedMsgLog entity = JDOUtils.createInstance(IUploadedMsgLog.class);

    try {
      if (lineMsg != null && !lineMsg.isEmpty()) {
        entity.setMessage(lineMsg);
        entity.setTimeStamp(new Date());
        entity.setDomainId(domainId);
        entity.setUploadedId(uploadedKey);
        pm.makePersistent(entity);
      }
    } catch (Exception e) {
      xLogger
          .warn("{0} when trying to update UploadedMsgLog object {1}: {2}", e.getClass().getName(),
              uploadedKey, e.getMessage());
    } finally {
      pm.close();
    }
  }

  // Parse the uploaded file that contains Transactions
  public static void parseUploadedTransactions(ResourceBundle backendMessages,
                                               ResourceBundle messages, Long domainId, Long kioskId,
                                               String userId, String blobKeyStr)
      throws IOException {
    // Read the csv line by line.
    // Ignore the first line i.e. the header
    // Parse the line
    // Create a Transaction object from every line
    // Continue to read all the lines.
    // In case of errors, accumulate all syntax errors and show them to the user. Do not store the transactions in the datastore.
    // If there are no syntax errors, save the accumulated transactions to the datastore. If there are any errors during update, accumulate and show them to the user.
    // Use InventoryManagementService and update the transactions
    // Using the blob key, get the blob
    BufferedReader bufReader = getReader(blobKeyStr);
    int rowNumber = 0;
    String line = bufReader.readLine();
    Date
        transTimestamp =
        new Date();// All transactions that are imported will have this timestamp 1 millisecond apart( so that ordering is maintained while displaying transactions in Logi web)
    long offset = 0;
    List<TransactionRow>
        transRowList =
        new ArrayList<>(); // List of TransactionRow objects that contain Transaction objects to update the Inventory.
    BulkUploadMgr.EntityContainer ec;
    boolean hasError = false;
    boolean hasProcessingError = false;
    boolean kidFound = false;
    while (line != null && !line.isEmpty()) {
      ec = new BulkUploadMgr.EntityContainer();
      int lineLength = byteCountUTF8(line);
      ITransaction trans;
      List<String> errors;
      // Skip the first line because it is the header
      if (rowNumber != 0) {
        xLogger.fine("Line being parsed: {0}", line);
        String kName = getKioskNameFromCSVString(domainId, line);
        Long kid = getKioskIdFromKioskName(domainId, kName);
        if (kid == null) {
          ec.messages.add(
              backendMessages.getString("kiosk") + " name is required but not given or is invalid "
                  + kName);
          hasError = true;
          // Send the rowNumber as jobId
          updateUploadedMsgLog(offset, "Row " + (rowNumber + 1) + ": " + line, ec,
              BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA, userId, domainId,
              String.valueOf(rowNumber));
        }
        if (kioskId == null || (kid != null && kioskId.equals(kid))) {
          kidFound = true;
          trans = JDOUtils.createInstance(ITransaction.class); // Create a new Transaction
          trans.setDomainId(domainId); // Set the domainId
          trans.setSrc(SourceConstants.UPLOAD);
          trans.setSourceUserId(userId); // Set the source user id
          trans.setTimestamp(transTimestamp); // Set the time stamp
          errors =
              populateTransactionFromCSVString(line, trans, backendMessages,
                  domainId); // Populate the transaction object from the csv line
          // If there are any errors during the parsing,  store them in EntityContainer. Set the hasError flag to true.
          // Also update the message log for this row.
          if (errors != null && !errors.isEmpty()) {
            ec.messages = errors;
            hasError = true;
            // Send the rowNumber as jobId
            updateUploadedMsgLog(offset, "Row " + (rowNumber + 1) + ": " + line, ec,
                BulkUploadMgr.TYPE_TRANSACTIONS, userId, domainId, String.valueOf(rowNumber));
          }

          // If there are no errors during parsing of the line, and if the transaction is not null, then
          // proceed to check if the opening stock mentioned in the csv line matches with the current stock in the inventory.
          // If they match then add the transaction to transRowList.
          // If they do not match, create a stock count transaction. Add it to the transRowList first
          // Then add the transaction to the transRowList.
          // If there are any errors encountered during this check, accumulate it in the entity container.
          if (!ec.hasErrors()) {
            List<ITransaction> transactions = new ArrayList<>();
            hasProcessingError =
                updateTransactionsList(trans, transactions, line, offset, (rowNumber + 1),
                    backendMessages) || hasProcessingError;
            if (!hasError && !hasProcessingError && !transactions.isEmpty()) {
              for (ITransaction t : transactions) {
                TransactionRow
                    transRow =
                    new TransactionRow(offset, (rowNumber + 1), line,
                        t); // Create a TransactionRow from transaction.
                transRowList.add(
                    transRow); // Add it to the list of TransactionRow objects to be saved later.
              }
            }
          }
        }
      }
      ++rowNumber;
      offset += lineLength + 1;
      line = bufReader.readLine();
    }
    if (!kidFound) {
      xLogger.warn(
          "The kioskId {0} for which data is being uploaded is not present in the uploaded csv file.",
          kioskId);
    }
    // Close the readers and streams that were opened
    bufReader.close();

    xLogger.info("Number of transactions accumulated: {0}", transRowList.size());
    if (!hasError && !hasProcessingError && !transRowList.isEmpty()) {
      if (fixTimestamp(transRowList) != -1) {
        createTransactionKeys(transRowList);
        if (saveTransactionRowList(transRowList, BulkUploadMgr.TYPE_TRANSACTIONS)) {
          xLogger.severe("Failed to save transactionRowList");
        }
      } else {
        xLogger.severe("Failed to save transactionRowList");
      }
    }

    // Finalize upload
    String
        uploadedKey =
        BulkUploadMgr.getUploadedKey(domainId, BulkUploadMgr.TYPE_TRANSACTIONS, userId);
    xLogger.fine("Got uploadedKey: {0}", uploadedKey);
    finalizeUpload(uploadedKey);
    xLogger.fine("Finalized upload");
  }

  /**
   * Persists all events in the inventory event row.
   *
   * @param inventoryEventRowList - List of inventory event rows to be persisted
   * @param type                  - Bulk upload type ( Refer Types in BulkUploadMgr )
   * @return true if save is succesful.
   */
  public static boolean saveInventoryEventRowList(List<InventoryEventRow> inventoryEventRowList,
                                                     String type) {
    boolean hasSaveError = false;
    for (InventoryEventRow inventoryEventRow : inventoryEventRowList) {
      BulkUploadMgr.EntityContainer ec = new BulkUploadMgr.EntityContainer();
      IInvntryEvntLog invEvntLog = inventoryEventRow.invEventLog;
      try {
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class);
        ims.adjustInventoryEvents(inventoryEventRow.invEventLog);
      } catch (Exception e) {
        xLogger.warn("{0} when updating inventory events.  domainId: {1}, kioskId: {2}," +
                " materialId: {3}, sourceUserId: {5} Message: {6}", e.getClass().getName(),
            invEvntLog.getDomainId(), invEvntLog.getKioskId(), invEvntLog.getMaterialId(),
            inventoryEventRow.userId, e.getMessage(), e);
        ec.messages.add("System Error: " + e.getMessage());
      } finally {
        if (ec.hasErrors()) {
          // Send the rowNumber as jobId
          updateUploadedMsgLog(inventoryEventRow.offset,
              "Row " + inventoryEventRow.rowNumber + ": " +
                  inventoryEventRow.line, ec, type, inventoryEventRow.userId,
              inventoryEventRow.invEventLog.getDomainId(),
              String.valueOf(inventoryEventRow.rowNumber));
          hasSaveError = true;
        }
      }
      if (hasSaveError) {
        break;
      }
    }
    return hasSaveError;
  }

  private static boolean saveTransactionRowList(List<TransactionRow> transRowList, String type) {
    xLogger.fine("Entering saveTransactionRowList. transRowList.size: {0}", transRowList.size());
    boolean hasSaveError = false;
    for (TransactionRow aTransRowList : transRowList) {
      BulkUploadMgr.EntityContainer ec = new BulkUploadMgr.EntityContainer();
      TransactionRow transRow = aTransRowList;
      ITransaction transaction = transRow.transaction;
      Long domainId = transaction.getDomainId();
      String userId = transaction.getSourceUserId();
      try {
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class);
        ITransaction transactionInError = ims.updateInventoryTransaction(domainId, transaction);
        if (transactionInError != null) {
          xLogger.warn(
              "Failed to create the Transaction. type: {0}, domainId: {1}, kioskId: {2}, materialId: {3}, sourceUserId: {4}",
              transactionInError.getType(), transactionInError.getDomainId(),
              transactionInError.getKioskId(), transactionInError.getMaterialId(),
              transactionInError.getSourceUserId());
          ec.messages.add(transactionInError.getMessage());
        }
      } catch (Exception e) {
        xLogger.warn(
            "{0} when updating inventory transaction.  type: {1}, domainId: {2}, kioskId: {3}, materialId: {4}, sourceUserId: {5} Message: {6}",
            e.getClass().getName(), transaction.getType(), transaction.getDomainId(),
            transaction.getKioskId(), transaction.getMaterialId(), transaction.getSourceUserId(),
            e.getMessage());
        ec.messages.add("System Error: " + e.getMessage());
      } finally {
        if (ec.hasErrors()) {
          // Send the rowNumber as jobId
          updateUploadedMsgLog(transRow.offset, "Row " + transRow.rowNumber + ": " + transRow.line,
              ec, type,
              userId, domainId, String.valueOf(transRow.rowNumber));
          hasSaveError = true;
        }
      }
      if (hasSaveError) {
        break;
      }
    }
    return hasSaveError;
  }



  private static void saveMnlTransactionRowList(List<MnlTransactionRow> mnlTransRowList) {
    xLogger.fine("Entering saveMnlTransactionRowList");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Iterate through the list of MnlTransactionRow objects. Get the MnlTransction objects and persist them.
      Iterator<MnlTransactionRow> mnlTransRowIter = mnlTransRowList.iterator();
      List<IMnlTransaction> mnlTransList = new ArrayList<>();
      while (mnlTransRowIter.hasNext()) {
        IMnlTransaction mnlTrans = mnlTransRowIter.next().mnlTransaction;
        mnlTransList.add(mnlTrans);
      }
      pm.makePersistentAll(mnlTransList);
    } finally {
      pm.close();
    }
  }

  // Method to set the transactions 1 millisecond apart so that they are displayed in the correct order in Logiweb
  private static long fixTimestamp(List<TransactionRow> transRowList) {
    xLogger.fine("Entering fixTimestamp");
    if (transRowList != null && !transRowList.isEmpty()) {
      TransactionRow
          transRow =
          transRowList
              .get(0); // Get the first transaction in the list and get it's time in milliseconds.
      ITransaction trans = transRow.transaction;
      Date timestamp = trans.getTimestamp();
      long timestampInMillis = timestamp.getTime();

      // Increment the time stamp for the remaining transactions in a way that they are spaced 1 millisecond apart.
      for (int i = 1; i < transRowList.size(); i++) {
        transRow = transRowList.get(i);
        trans = transRow.transaction;
        trans.setTimestamp(new Date(++timestampInMillis));
      }
      return timestampInMillis;
    }
    xLogger.severe("Error while fixing timestamps");
    return -1; // Should not come here. If this place is reached, then there is some error. Return -1.
  }

  private static void createTransactionKeys(List<TransactionRow> transRowList) {
    xLogger.fine("Entering createTransactionKeys");
    if (transRowList != null && !transRowList.isEmpty()) {
      for (TransactionRow transRow : transRowList) {
        ITransaction transaction = transRow.transaction;
        transDao.setKey(transaction);
      }
    }
    xLogger.fine("Exiting createTransactionKeys");

  }

  public static IKiosk getKioskFromKioskName(Long domainId, String kioskName) {
    if (domainId != null && kioskName != null && !kioskName.isEmpty()) {
      try {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        IKiosk kiosk = as.getKioskByName(domainId, kioskName);
        xLogger.fine("kiosk: {0}", kiosk);
        return kiosk;
      } catch (Exception e) {
        xLogger.warn("{0} when trying to get Kiosk Id for kiosk {1} in domain {2}",
            e.getClass().getName(), kioskName, domainId, e.getMessage());
      }
    }
    return null;
  }

  public static Long getKioskIdFromKioskName(Long domainId, String kioskName) {
    IKiosk kiosk = getKioskFromKioskName(domainId, kioskName);
    return kiosk != null ? kiosk.getKioskId() : null;
  }

  private static boolean isDateStrValid(String dateStr) {
    if (dateStr == null || dateStr.isEmpty()) {
      return false;
    }
    int index = dateStr.lastIndexOf('/');
    if (index == -1) {
      return false;
    }
    if (dateStr.substring(index + 1).length() != FULLYEAR_LENGTH) {
      return false;
    }
    return true;
  }

  private static void finalizeUpload(String key) {
    try {
      // Get the Uploaded object
      UploadService svc = Services.getService(UploadServiceImpl.class);
      IUploaded u = svc.getUploaded(key);
      if (u != null) {
        u.setJobStatus(IUploaded.STATUS_DONE);
        svc.updateUploaded(u);
      } else {
        xLogger.severe("Unable to find Uploaded object corresponding to key {0}", key);
      }
    } catch (Exception e) {
      xLogger.severe("{0} when handling bulk-import of transactions for key {1}: {2}",
          e.getClass().getName(), key, e.getMessage());
    }
  }

  private static BufferedReader getReader(String blobKeyStr) throws IOException {
    BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
    return new BufferedReader(new InputStreamReader(blobstoreService.getInputStream(blobKeyStr)));
  }

  private static int byteCountUTF8(String input) {
    xLogger.fine("Entering byteCountUTF8");
    int ret = 0;
    if (input != null && !input.isEmpty()) {
      for (int i = 0; i < input.length(); ++i) {
        int cc = Character.codePointAt(input, i);
        if (cc <= 0x7F) {
          ret++;
        } else if (cc <= 0x7FF) {
          ret += 2;
        } else if (cc <= 0xFFFF) {
          ret += 3;
        } else if (cc <= 0x10FFFF) {
          ret += 4;
          i++;
        }
      }
    }
    xLogger.fine("Entering byteCountUTF8");
    return ret;
  }

  private static BigDecimal getOpeningStockFromCSVString(String csvLine) {
    xLogger.fine("Entering getOpeningStockFromCSVString: {0}", csvLine);
    if (csvLine != null && !csvLine.isEmpty()) {
      String[] tokens = StringUtil.getCSVTokens(csvLine);
      if (tokens != null && tokens.length != 0) {
        try {
          String openingStockStr = tokens[2];
          if (openingStockStr != null && !openingStockStr.isEmpty()) {
            BigDecimal openingStock = new BigDecimal(openingStockStr);
            return BigUtil.getZeroIfNull(openingStock);
          }
        } catch (Exception e) {
          xLogger.warn("{0} when getting opening stock from csv string. Message: {0}",
              e.getClass().getClass().getName(), e.getMessage());
          return BigDecimal.ZERO;
        }
      }
    }
    xLogger.fine("Exiting getOpeningStockFromCSVString: {1}", csvLine);
    return BigDecimal.ZERO;
  }

  private static String getKioskNameFromCSVString(Long domainId, String csvLine) {
    if (csvLine != null && !csvLine.isEmpty()) {
      String[] tokens = StringUtil.getCSVTokens(csvLine);
      if (tokens != null && tokens.length > 1) {
        String kioskName = tokens[0];
        return kioskName;
      }
    }
    return null;
  }

  public static String getMaterialNameFromMaterialId(Long materialId) {
    if (materialId != null) {
      try {
        MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
        IMaterial material = mcs.getMaterial(materialId);
        if (material != null) {
          return material.getName();
        }
      } catch (Exception e) {
        xLogger.warn("{0} when trying to get Material name for material {1}. Message: {2}",
            e.getClass().getName(), materialId, e.getMessage());
      }
    }
    return null;
  }

  public static String getKioskNameFromKioskId(Long kioskId) {
    if (kioskId != null) {
      try {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        IKiosk kiosk = as.getKiosk(kioskId, false);
        xLogger.fine("kiosk: {0}", kiosk);
        if (kiosk != null) {
          return kiosk.getName();
        }
      } catch (Exception e) {
        xLogger.warn("{0} when trying to get Kiosk name for kiosk {1}. Message: {2}",
            e.getClass().getName(), kioskId, e.getMessage());
      }
    }
    return null;
  }

  public static Long getMaterialIdFromMaterialName(Long domainId, String materialName) {
    if (domainId != null && materialName != null && !materialName.isEmpty()) {
      try {
        MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
        IMaterial material = mcs.getMaterialByName(domainId, materialName);
        if (material != null) {
          return material.getMaterialId();
        }
      } catch (Exception e) {
        xLogger
            .warn("{0} when trying to get Material Id for Material {1} in domain {2}. Message: {3}",
                e.getClass().getName(), materialName, domainId, e.getMessage());
      }
    }
    return null;
  }

  private static boolean updateTransactionsList(ITransaction trans, List<ITransaction> transactions,
                                                String line, long offset, int rowNumber,
                                                ResourceBundle backendMessages) {
    xLogger.fine("Entering updateTransactionsList");
    boolean hasError = false;
    ITransaction scTrans = null;
    BulkUploadMgr.EntityContainer ec = null;
    BigDecimal openingStock = getOpeningStockFromCSVString(line);
    ec = new BulkUploadMgr.EntityContainer();
    // Get the inventory for the Kiosk and Material to obtain current stock in inventory
    BigDecimal
        currentStock =
          getCurrentStock(trans.getDomainId(), trans.getKioskId(), trans.getMaterialId(),
              trans.getSourceUserId(), BulkUploadMgr.TYPE_TRANSACTIONS, line, offset, rowNumber,
              ec);
    if (BigUtil.equals(currentStock, -1)) {
      ec.messages.add(
          "Material " + getMaterialNameFromMaterialId(trans.getMaterialId())
              + " not found in entity " + getKioskNameFromKioskId(trans.getKioskId()));
      hasError = true;
      updateUploadedMsgLog(offset, "Row " + rowNumber + ": " + line, ec,
          BulkUploadMgr.TYPE_TRANSACTIONS, trans.getSourceUserId(), trans.getDomainId(),
          String.valueOf(rowNumber));
    }
    // If there are no errors, then proceed to check if openingStock matches currentStock. If they do not match, create a stock count transaction and add it to the transaction list
    if (!hasError) {
      if (BigUtil.notEquals(currentStock, openingStock)) {
        scTrans = createStockCountTransaction(trans, openingStock);
      }
      // If opening stock < quantity and transaction type == "issue", flag an error saying opening stock >= issue
      if (ITransaction.TYPE_ISSUE.equals(trans.getType()) && BigUtil
          .lesserThan(openingStock, trans.getQuantity())
          || ITransaction.TYPE_WASTAGE.equals(trans.getType()) && BigUtil
          .lesserThan(openingStock, trans.getQuantity())
          || ITransaction.TYPE_TRANSFER.equals(trans.getType()) && BigUtil
          .lesserThan(openingStock, trans.getQuantity())) {
        ec.messages
            .add("Quantity " + trans.getQuantity() + " exceeds opening stock " + openingStock);
        hasError = true;
        // Send the rowNumber as jobId
        updateUploadedMsgLog(offset, "Row " + rowNumber + ": " + line, ec,
            BulkUploadMgr.TYPE_TRANSACTIONS, trans.getSourceUserId(), trans.getDomainId(),
            String.valueOf(rowNumber));
      }

      // If there is no error, proceed
      if (!hasError) {
        if (ITransaction.TYPE_ISSUE.equals(trans.getType()) || ITransaction.TYPE_RECEIPT
            .equals(trans.getType())) {
          boolean
              hasRelationship =
              hasRelationship(trans.getDomainId(), trans.getKioskId(), trans.getLinkedKioskId(),
                  (ITransaction.TYPE_ISSUE.equals(trans.getType()) ? IKioskLink.TYPE_CUSTOMER
                      : IKioskLink.TYPE_VENDOR), trans.getSourceUserId(),
                  BulkUploadMgr.TYPE_TRANSACTIONS, line, offset, rowNumber, ec);
          if (trans.getLinkedKioskId() != null && !hasRelationship) {
            ec.messages.add(
                "Relationship (" + backendMessages.getString("bck.customer.lower") + " or "
                    + backendMessages.getString("bck.vendor.lower") + ") does not exist");
            hasError = true;
            updateUploadedMsgLog(offset, "Row " + rowNumber + ": " + line, ec,
                BulkUploadMgr.TYPE_TRANSACTIONS, trans.getSourceUserId(), trans.getDomainId(),
                String.valueOf(rowNumber));
          }
        }
        if (!hasError) {
          // If stock count transaction was created and is not null, add it to the transactions list.
          if (scTrans != null) {
            transactions.add(scTrans);
          }
          // Then, add the transaction to the transactions list.
          // Do not set the key here. Set the key for all the accumulated transactions after fixing the timestamps in a separate method createTransactionKeys
          // trans.setKey( Transaction.createKey( trans.getKioskId(), trans.getMaterialId(), trans.getType(), trans.getTimestamp(), trans.getBatchId() ) );
          // Add the transaction to the transactions list
          transactions.add(trans);
        }
      }
    }
    xLogger.fine("Exiting updateTransactionsList, hasError: {0}, transactions: {1}", hasError,
        transactions.toString());
    return hasError;
  }

  private static boolean hasRelationship(Long domainId, Long kioskId, Long linkedKioskId,
                                         String linkType, String userId, String type, String line,
                                         long offset, int rowNumber, BulkUploadMgr.EntityContainer ec) {
    boolean hasKioskLink = false;
    if (kioskId == null || linkedKioskId == null || linkType == null || domainId == null
        || userId == null) {
      return hasKioskLink;
    }

    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      hasKioskLink = as.hasKioskLink(kioskId, linkType, linkedKioskId);
    } catch (Exception e) {
      xLogger.warn(
          "{0} while getting kiosk link for kioskId: {1}, linkedKioskId: {2} and linkType: {3}. Message: {4}",
          e.getClass().getName(), kioskId, linkedKioskId, linkType, e.getMessage());
      ec.messages.add("Error while getting relationship (Either customer or vendor) for entity");
      updateUploadedMsgLog(offset, "Row " + rowNumber + ": " + line, ec, type, userId, domainId,
          String.valueOf(rowNumber));
      hasKioskLink = false;
    }
    return hasKioskLink;
  }

  private static boolean isMaterialInEntity(Long domainId, Long kioskId, Long materialId,
                                            String userId, String type, String line, long offset,
                                            int rowNumber, BulkUploadMgr.EntityContainer ec) {
    if (kioskId == null || materialId == null || domainId == null || userId == null) {
      return false;
    }

    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      IInvntry inventory = ims.getInventory(kioskId, materialId);

      if (inventory != null) {
        return true;
      }
    } catch (Exception e) {
      xLogger.warn("{0} while getting inventory for kioskId: {1} and materialId: {2}. Message: {3}",
          e.getClass().getName(), kioskId, materialId, e.getMessage());
      ec.messages
          .add("Error while getting inventory of related entity (Either customer or vendor)");
      updateUploadedMsgLog(offset, "Row " + rowNumber + ": " + line, ec, type, userId, domainId,
          String.valueOf(rowNumber));
      return false;
    }
    return false;
  }

  // Private method that gets the current stock of a material in a kiosk.
  // Returns -1 if there was any exception while getting the inventory.
  private static BigDecimal getCurrentStock(Long domainId, Long kioskId, Long materialId,
                                            String userId, String type, String line, long offset,
                                            int rowNumber, BulkUploadMgr.EntityContainer ec) {
    InventoryManagementService ims;
    IInvntry inventory;
    BigDecimal currentStock = new BigDecimal(-1);
    if (kioskId == null || materialId == null || domainId == null || userId == null) {
      return new BigDecimal(-1);
    }

    try {
      ims = Services.getService(InventoryManagementServiceImpl.class);
      inventory = ims.getInventory(kioskId, materialId);
      if (inventory != null) {
        currentStock = inventory.getStock();
      }
    } catch (Exception e) {
      xLogger.warn("{0} while getting inventory for kioskId: {1} and materialId: {2}. Message: {3}",
          e.getClass().getName(), kioskId, materialId, e.getMessage());
      ec.messages.add("Error while validating opening stock against current stock.");
      updateUploadedMsgLog(offset, "Row " + rowNumber + ": " + line, ec, type, userId, domainId,
          String.valueOf(rowNumber));
      return new BigDecimal(-1);
    }
    return currentStock;
  }

  // Creates a transaction with type Transaction.PHYSICALCOUNT from the transaction and sets the quantity to the opening stock passed.
  private static ITransaction createStockCountTransaction(ITransaction trans,
                                                          BigDecimal openingStock) {
    // Create a Transaction scTrans and set it's members
    ITransaction scTrans = JDOUtils.createInstance(ITransaction.class);
    scTrans.setDomainId(trans.getDomainId()); // Mandatory for a transaction
    scTrans.setSourceUserId(trans.getSourceUserId()); // Mandatory for a transaction
    scTrans
        .setQuantity(openingStock); // Set the stock count transaction's quantity to opening stock
    scTrans.setKioskId(trans.getKioskId());
    scTrans.setMaterialId(trans.getMaterialId());
    scTrans.setType(ITransaction.TYPE_PHYSICALCOUNT);
    scTrans.setReason(trans.getReason());
    scTrans.setMaterialStatus(trans.getMaterialStatus());
    scTrans.setLinkedKioskId(trans.getLinkedKioskId());
    scTrans.setBatchId(trans.getBatchId());
    scTrans.setBatchExpiry(trans.getBatchExpiry());
    scTrans.setBatchManufacturer(trans.getBatchManufacturer());
    scTrans.setBatchManufacturedDate(trans.getBatchManufacturedDate());
    scTrans.setLatitude(trans.getLatitude());
    scTrans.setLongitude(trans.getLongitude());
    scTrans.setGeoAccuracy(trans.getGeoAccuracy());
    scTrans.setTimestamp(trans.getTimestamp());
    scTrans.setSrc(trans.getSrc());
    // scTrans.setAtd(trans.getAtd()); // for stock counts done by the system, ATD should be appearing.. so removing it
    // Do not set the Key here. Set the keys for all the accumulated transactions after fixing the timestamps.
    // scTrans.setKey( Transaction.createKey( scTrans.getKioskId(), scTrans.getMaterialId(), Transaction.TYPE_PHYSICALCOUNT, scTrans.getTimestamp(), scTrans.getBatchId() ) );
    return scTrans;
  }

  private static ITransaction createTransactionFromMnlTransaction(IMnlTransaction mnlTrans,
                                                                  String type) {
    ITransaction trans = JDOUtils.createInstance(ITransaction.class);
    trans.setDomainId(mnlTrans.getDomainId()); // Mandatory for a transaction
    trans.addDomainIds(mnlTrans.getDomainIds());
    trans.setSourceUserId(mnlTrans.getUserId()); // Mandatory for a transaction
    trans.setKioskId(mnlTrans.getKioskId());
    trans.setMaterialId(mnlTrans.getMaterialId());
    trans.setSrc(SourceConstants.UPLOAD);
    trans.setType(type);
    trans.setTimestamp(mnlTrans.getTimestamp());
    trans.setAtd(mnlTrans.getReportingPeriod());
    if (ITransaction.TYPE_ISSUE.equals(type)) {
      trans.setQuantity(mnlTrans.getIssueQuantity());
    } else if (ITransaction.TYPE_RECEIPT.equals(type)) {
      trans.setQuantity(mnlTrans.getReceiptQuantity());
    } else if (ITransaction.TYPE_WASTAGE.equals(type)) {
      trans.setQuantity(mnlTrans.getDiscardQuantity());
    }
    transDao.setKey(trans);
    return trans;
  }

  // If either issue quantity or receipt quantity or discard quantity is present and > 0 in the MnlTransaction object, then it is a Transaction
  // Otherwise it's not a transaction.
  private static boolean isTransaction(IMnlTransaction mnlTrans) {
    boolean isTransaction = false;
    if (mnlTrans != null) {
      if (BigUtil.greaterThanEqualsZero(mnlTrans.getOpeningStock())) {
        isTransaction = true;
      }
    }
    return isTransaction;
  }

  // If computed order quantity is > 0 in the MnlTransaction object then, it is an Order.
  private static boolean isOrder(IMnlTransaction mnlTrans) {
    boolean isOrder = false;
    if (mnlTrans != null) {
      if (BigUtil.greaterThanZero(mnlTrans.getOrderedQuantity()) ||
          BigUtil.greaterThanZero(mnlTrans.getFulfilledQuantity())) {
        isOrder = true;
      }
    }
    return isOrder;
  }

  // Process the manual transaction as a Transaction and return a boolean indicating if there was any error during processing.
  // This method checks populates a list of Transaction objects based on whether issues and/or receipts and/or discards are specified.
  // Additionally, if the opening stock does not match the current stock in the system, a Stock count transaction is added as the first
  // item to the list of Transaction objects. In case opening stock + receipts < issues + discards, an error is flagged and false is returned.
  private static boolean processAsTransaction(IMnlTransaction mnlTrans,
                                              List<ITransaction> transactions, String line,
                                              long offset, int rowNumber,
                                              ResourceBundle backendMessages) {
    xLogger.fine("Entering processAsTransaction");
    boolean hasError = false;
    ITransaction issueTrans = null;
    ITransaction receiptTrans = null;
    ITransaction discardTrans = null;
    // The order in which the transactions are added to the list is important.
    // First stock count, if opening stock does not match with current stock.
    BulkUploadMgr.EntityContainer ec;
    String userId = mnlTrans.getUserId();
    Long domainId = mnlTrans.getDomainId();
    Long kioskId = mnlTrans.getKioskId();
    Long materialId = mnlTrans.getMaterialId();

    ITransaction scTrans;
    // Check openingStock
    ec = new BulkUploadMgr.EntityContainer();
    // Check if openingStock + receipts >= issues + discards. Otherwise, flag an error
    boolean isValid = isOpeningStockAndReceiptsTotalValid(mnlTrans);
    BigDecimal openingStock = mnlTrans.getOpeningStock();
    if (!isValid) {
      ec.messages.add(
          "The sum of the number of issues and discards cannot be greater than the sum of opening stock and receipts.");
      // Send the rowNumber as jobId
      updateUploadedMsgLog(offset, "Row " + rowNumber + ": " + line, ec,
          BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA, userId, domainId,
          String.valueOf(rowNumber));
      hasError = true;
    }
    if (!hasError) {
      // Get the inventory for the Kiosk and Material to obtain current stock in inventory
      BigDecimal
          currentStock =
          getCurrentStock(domainId, kioskId, materialId, userId,
              BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA, line, offset, rowNumber, ec);
      if (BigUtil.equals(currentStock, -1)) {
        ec.messages.add("Material " + getMaterialNameFromMaterialId(materialId)
            + " not found in " + backendMessages.getString("kiosk.lowercase") + " "
            + getKioskNameFromKioskId(kioskId));
        // Send the rowNumber as jobId
        updateUploadedMsgLog(offset, "Row " + rowNumber + ": " + line, ec,
            BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA, userId, domainId,
            String.valueOf(rowNumber));
        hasError = true;
      }
      // If there are no errors, then proceed to check if openingStock matches currentStock. If the two do not match, create a stock count transaction and add it to the transaction list
      if (!hasError) {
        if (BigUtil.notEquals(currentStock, openingStock)) {
          scTrans =
              createTransactionFromMnlTransaction(mnlTrans, ITransaction.TYPE_PHYSICALCOUNT);
          if (scTrans != null) {
            scTrans.setQuantity(openingStock);
            transactions.add(
                scTrans); // Insert the stock count transaction at the first position of the list.
          }
        }

        // Add the other transactions if any
        if (BigUtil.greaterThanZero(mnlTrans.getReceiptQuantity())) {
          receiptTrans =
              createTransactionFromMnlTransaction(mnlTrans, ITransaction.TYPE_RECEIPT);
        }
        if (BigUtil.greaterThanZero(mnlTrans.getIssueQuantity())) {
          issueTrans =
              createTransactionFromMnlTransaction(mnlTrans, ITransaction.TYPE_ISSUE);
        }
        if (BigUtil.greaterThanZero(mnlTrans.getDiscardQuantity())) {
          discardTrans =
              createTransactionFromMnlTransaction(mnlTrans, ITransaction.TYPE_WASTAGE);
        }

        if (receiptTrans != null) {
          transactions.add(receiptTrans);
        }
        if (issueTrans != null) {
          transactions.add(issueTrans);
        }
        if (discardTrans != null) {
          transactions.add(discardTrans);
        }
      }
    }

    xLogger.fine("Exiting processAsTransaction. hasError: {0}, transactions: {1}", hasError,
        transactions.toString());
    return hasError;
  }

  /**
   * Constructs and returns the InvEventLog object for the given manual transaction. Invntry Event is created with
   * start date as reporting period date and end date as reporting period date + no. of stock out days.
   *
   * @param mnlTrans  - Manual upload transaction object.
   * @param line      - Uploaded line
   * @param offset    - Offset in row
   * @param rowNumber - line number
   * @return return InvntoryEventRow object with log.
   */
  public static InventoryEventRow getInvEventRow(IMnlTransaction mnlTrans, String line,
                                                    long offset,
                                                    int rowNumber) {
    if (mnlTrans.getReportingPeriod() != null) {
      IInvntryEvntLog invntryEvntLog = JDOUtils.createInstance(IInvntryEvntLog.class);
      invntryEvntLog.setKioskId(mnlTrans.getKioskId());
      invntryEvntLog.setDomainId(mnlTrans.getDomainId());
      invntryEvntLog.setMaterialId(mnlTrans.getMaterialId());
      invntryEvntLog.setStartDate(mnlTrans.getReportingPeriod());
      invntryEvntLog.setType(IEvent.STOCKOUT);
      Calendar endDateCal = Calendar.getInstance();
      endDateCal.setTime(mnlTrans.getReportingPeriod());
      endDateCal.add(Calendar.DATE, (int) mnlTrans.getStockoutDuration());
      if (endDateCal.getTimeInMillis() > System.currentTimeMillis()) {
        endDateCal.setTimeInMillis(System.currentTimeMillis());
        endDateCal = LocalDateUtil.resetTimeFields(endDateCal);
      }
      invntryEvntLog.setEndDate(endDateCal.getTime());
      return new InventoryEventRow(offset, rowNumber, line, invntryEvntLog, mnlTrans.getUserId());
    }
    return null;
  }

  /**
   * Private method that returns an OrderRow object from an MnlTransaction object
   */
  public static OrderRow getOrderRow(IMnlTransaction mnlTrans, String line, long offset,
                                        int rowNumber) {
    ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
    transaction.setDomainId(mnlTrans.getDomainId());
    transaction.setSourceUserId(mnlTrans.getUserId());
    transaction.setTimestamp(mnlTrans.getTimestamp());
    transaction.setKioskId(mnlTrans.getKioskId());
    transaction.setMaterialId(mnlTrans.getMaterialId());
    transaction.setType(ITransaction.TYPE_ORDER);
    if (BigUtil.greaterThanZero(mnlTrans.getOrderedQuantity())) {
      transaction.setQuantity(mnlTrans.getOrderedQuantity());
    } else {
      transaction.setQuantity(mnlTrans.getFulfilledQuantity());
    }
    List<String> orderTags = mnlTrans.getTags();
    transaction.setLinkedKioskId(mnlTrans.getVendorId());
    transDao.setKey(transaction);

    return new OrderRow(offset, rowNumber, line, transaction, orderTags, mnlTrans);
  }

  private static boolean isOpeningStockAndReceiptsTotalValid(IMnlTransaction mnlTrans) {
    boolean isValid = false;
    if (mnlTrans != null) {
      if (BigUtil.greaterThanEquals(mnlTrans.getOpeningStock().add(mnlTrans.getReceiptQuantity()),
          mnlTrans.getIssueQuantity().add(mnlTrans.getDiscardQuantity()))) {
        isValid = true;
      }
    }
    return isValid;
  }

  // Method that creates a Transaction object from a CSV line
  public static List<String> populateTransactionFromCSVString(String csvLine, ITransaction trans,
                                                              ResourceBundle backendMessages,
                                                              Long domainId) {
    xLogger.fine("Entering populateTransactionFromCSVString");
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    String type = null;
    List<String> errors = new ArrayList<>();
    if (csvLine == null || csvLine.isEmpty()) {
      errors.add("Invalid or null row ");
      return errors;
    }
    boolean done = false;
    // Get a tokens from the csv
    String[] tokens = StringUtil.getCSVTokens(csvLine);
    if (tokens == null || tokens.length == 0) {
      errors.add("No fields specified");
      done = true;
    }
    int i = 0;
    int size = 0;
    if (!done) {
      size = tokens.length;
      xLogger.fine("numberOfTokens in CSV: {0}", size);
    }
    // Kiosk Name as it appears in Logi web - Mandatory
    Long kioskId = null;
    if (!done) {
      // Kiosk Name - mandatory
      String kioskName = tokens[i].trim();
      if (kioskName.isEmpty()) {
        errors.add(backendMessages.getString("kiosk") + " name is required but not given");
      } else {
        // Get the Kiosk Id from Kiosk Name
        kioskId = getKioskIdFromKioskName(trans.getDomainId(), kioskName);
        if (kioskId == null) {
          errors.add(backendMessages.getString("kiosk") + " name not found " + kioskName);
        } else {
          trans.setKioskId(kioskId);
        }
      }
    }
    if (++i == size) {
      errors.add("No fields specified after kiosk name");
      done = true;
    }

    // Material Name as it appears in Logi Web - mandatory
    Long materialId = null;
    if (!done) {
      String materialName = tokens[i].trim();
      if (materialName.isEmpty()) {
        errors.add("Material name is required but not given");
      } else {
        // Material Id from Material Name
        materialId =
            getMaterialIdFromMaterialName(trans.getDomainId(), materialName);
        if (materialId == null) {
          errors.add("Material name not found " + materialName);
        } else {
          trans.setMaterialId(materialId);
        }
      }
    }

    if (++i == size) {
      errors.add("No fields specified after material name");
      done = true;
    }

    // Check whether material is batch enabled
    boolean isBatchMaterial = false;
    try {
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      EntitiesService acs = Services.getService(EntitiesServiceImpl.class);
      if (materialId != null && kioskId != null) {
        IMaterial material = mcs.getMaterial(materialId);
        IKiosk kiosk = acs.getKiosk(kioskId);
        isBatchMaterial = kiosk.isBatchMgmtEnabled() && material.isBatchEnabled();
      }
    } catch (ServiceException e) {
      isBatchMaterial = false;
    }

    // Opening Stock - mandatory
    // If opening stock does not match with the Inventory's quantity, signal and error to the user and exit.
    if (!done) {
      String openingStockStr = tokens[i].trim();
      BigDecimal openingStock = null;
      if (openingStockStr.isEmpty()) {
        errors.add("Opening stock is required but not given");
      } else {
        try {
          openingStock = new BigDecimal(openingStockStr);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when parsing openingStock in row: {1}", e.getClass().getName(),
              e.getMessage());
        }
        if (BigUtil.isInvalidQ(openingStock)) {
          errors.add("Invalid opening stock " + openingStockStr);
        }
      }
    }

    if (++i == size) {
      errors.add("No fields specified after opening stock");
      done = true;
    }
    // Type of transaction - mandatory
    if (!done) {
      type = tokens[i].trim();
      if (type.isEmpty()) {
        errors.add("Type is required but is not given");
      } else {
        if (!(ITransaction.TYPE_ISSUE.equals(type) || ITransaction.TYPE_PHYSICALCOUNT.equals(type)
            || ITransaction.TYPE_RECEIPT.equals(type) || ITransaction.TYPE_TRANSFER.equals(type)
            || ITransaction.TYPE_WASTAGE.equals(type))) {
          errors.add("Invalid type " + type);
        } else {
          trans.setType(type);
        }
      }
    }

    if (++i == size) {
      errors.add("No fields specified after type");
      done = true;
    }
    // Quantity - mandatory
    if (!done) {
      String quantityStr = tokens[i].trim();
      BigDecimal quantity = null;
      if (quantityStr.isEmpty()) {
        errors.add("Quantity required but not given");
      } else if (quantityStr.indexOf('.') != -1) {
        errors.add("Invalid quantity " + quantityStr + ". Quantity should be a whole number");
      } else {
        try {
          quantity = new BigDecimal(quantityStr);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when parsing quantity in row: {1}", e.getClass().getName(),
              e.getMessage());
        }
        if (BigUtil.isInvalidQ(quantity)) {
          errors.add("Invalid quantity " + quantityStr);
        } else {
          trans.setQuantity(quantity);
          trans.setSrc(SourceConstants.UPLOAD);
        }
      }
    }

    // Reason
    if (++i == size) {
      done = true;
    }
    if (!done) {
      String reasonStr = tokens[i].trim();
      if (!reasonStr.isEmpty()) {
        trans.setReason(reasonStr);
      }
    }
    // status
    if (++i == size) {
      done = true;
    }
    if (!done) {
      String statusStr = tokens[i].trim();
      if (!statusStr.isEmpty()) {
        trans.setMaterialStatus(statusStr);
      }
    }

    // Related entity
    if (!done && ++i < size) {
      String relatedEntityName = tokens[i].trim();
      if (ITransaction.TYPE_TRANSFER.equals(type) && StringUtils.isEmpty(relatedEntityName)) {
        errors.add(backendMessages.getString("error.nodestinationfortransfer"));
      }
      if (!relatedEntityName.isEmpty()) {
        // Get the Kiosk Id from Kiosk Name
        Long
            linkedKioskId =
              getKioskIdFromKioskName(trans.getDomainId(), relatedEntityName);
        if (linkedKioskId == null) {
          errors.add(
              "Related " + backendMessages.getString("kiosk.lowercase") + " (" + backendMessages
                  .getString("bck.customer.upper") + " or " + backendMessages
                  .getString("bck.vendor.upper") + " ) not found " + relatedEntityName);
        } else {
          trans.setLinkedKioskId(linkedKioskId);
        }
      }
    }

    // Batch
    String batchId;
    if (!done && ++i < size) {
      batchId = tokens[i].trim();
      if (isBatchMaterial && !batchId.isEmpty()) {
        trans.setBatchId(batchId.toUpperCase());
      } else if (isBatchMaterial) {
        errors.add("BatchID is required");
      }
    }

    // Batch Expiry
    if (!done && ++i < size) {
      String batchExpiryStr = tokens[i].trim();
      if (isBatchMaterial && !batchExpiryStr.isEmpty()) {
        // Check if the batchExpiryStr has 4 digits for year
        if (!isDateStrValid(batchExpiryStr)) {
          errors.add("Invalid batch expiry date " + batchExpiryStr + ", Expected format: "
              + Constants.DATE_FORMAT);
        } else {
          Date batchExpiryDate;
          // Parse the batchExpiry string
          try {
            batchExpiryDate =
                LocalDateUtil.parseCustom(batchExpiryStr, Constants.DATE_FORMAT, null);
            xLogger.info("batchExpiryDate: {0}", batchExpiryDate.getTime());
            Date today = new Date();
            if (batchExpiryDate.compareTo(today) <= 0) {
              errors.add("Invalid batch expiry date " + batchExpiryStr
                  + ". Expiry date should be greater than today");
            }
            trans.setBatchExpiry(batchExpiryDate);
          } catch (Exception e) {
            xLogger.warn("{0} when parsing batch expiry date. Message: {1}", e.getClass().getName(),
                e.getMessage());
            errors.add("Invalid batch expiry date " + batchExpiryStr);
          }
        }
      } else if (isBatchMaterial) {
        errors.add("Batch expiry date required.");
      }
    }

    // Manufacturer
    if (!done && ++i < size) {
      String manufacturer = tokens[i].trim();
      if (isBatchMaterial && !manufacturer.isEmpty()) {
        trans.setBatchManufacturer(manufacturer);
      } else if (isBatchMaterial) {
        errors.add("Manufacturer name is required.");
      }
    }

    // Manufactured date
    if (!done && ++i < size) {
      String manufacturedDateStr = tokens[i].trim();
      if (isBatchMaterial && !manufacturedDateStr.isEmpty()) {
        // Check if the manufacturedDateStr has 4 digits for year
        if (!isDateStrValid(manufacturedDateStr)) {
          errors.add(
              "Invalid batch manufactured date " + manufacturedDateStr + ", Expected format: "
                  + Constants.DATE_FORMAT);
        } else {
          Date manufacturedDate;
          // Parse the manufactureddate string
          try {
            manufacturedDate =
                LocalDateUtil.parseCustom(manufacturedDateStr, Constants.DATE_FORMAT, null);
            Date today = new Date();
            if (manufacturedDate.compareTo(today) > 0) {
              errors.add("Invalid manufactured date " + manufacturedDateStr
                  + ". Manufactured date cannot be greater than today");
            }
            xLogger.info("manufacturedDate: {0}", manufacturedDate.getTime());
            trans.setBatchManufacturedDate(manufacturedDate);
          } catch (Exception e) {
            xLogger.warn("{0} when parsing batch manufactured date. Message: {1}",
                e.getClass().getName(), e.getMessage());
            errors.add("Invalid batch manufactured date " + manufacturedDateStr);
          }
        }
      }
    }

    // Latitude
    if (!done && ++i < size) {
      String latStr = tokens[i].trim();
      Double lat;
      if (!latStr.isEmpty()) {
        try {
          lat = Double.valueOf(latStr);
          trans.setLatitude(lat);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when trying to parse latitude. Message: {1}", e.getClass().getName(),
              e.getMessage());
          errors.add("Invalid latitude " + latStr);
        }
      }
    }

    // Longitude
    if (!done && ++i < size) {
      String lngStr = tokens[i].trim();
      Double lng;
      if (!lngStr.isEmpty()) {
        try {
          lng = Double.valueOf(lngStr);
          trans.setLongitude(lng);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when trying to parse lng. Message: {1}", e.getClass().getName(),
              e.getMessage());
          errors.add("Invalid longitide " + lngStr);
        }
      }
    }

    // Geo accuracy
    if (!done && ++i < size) {
      String geoAccuracyStr = tokens[i].trim();
      Double gacc;
      if (!geoAccuracyStr.isEmpty()) {
        try {
          gacc = Double.valueOf(geoAccuracyStr);
          trans.setGeoAccuracy(gacc);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when trying to parse gacc. Message: {1}", e.getClass().getName(),
              e.getMessage());
          errors.add("Invalid GPS accuracy " + geoAccuracyStr);
        }
      }
    }

    //actual transaction date
    if (!done && ++i < size) {
      String actualTransDateStr = tokens[i].trim();
      if (!actualTransDateStr.isEmpty()) {
        if (!isDateStrValid(actualTransDateStr)) {
          errors.add("Invalid transaction   date " + actualTransDateStr + ", Expected format: "
              + Constants.DATE_FORMAT);
        } else {
          Date actualTransDate;
          // Parse the actualTransDateStr string
          try {
            actualTransDate =
                LocalDateUtil.parseCustom(actualTransDateStr, Constants.DATE_FORMAT, null);
            //To check whether given date is not greater than system time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(sdf.format(new Date()));
            Date
                actualTransDateSrv =
                LocalDateUtil.parseCustom(actualTransDateStr, Constants.DATE_FORMAT, "GMT");
            Date actuaTransDateTimeOff = sdf.parse(sdf.format(actualTransDateSrv));
            if (actuaTransDateTimeOff.compareTo(date) > 0) {
              xLogger.warn("Actual date of transaction cannot be after than today");
              errors.add("Actual date of transaction cannot be after than today");
            } else {
              xLogger.info("actualTransDateStr: {0}", actualTransDate.getTime());
              trans.setAtd(actualTransDate);
            }
          } catch (Exception e) {
            xLogger.warn("{0} when parsing transaction date date. Message: {1}",
                e.getClass().getName(), e.getMessage());
            errors.add("Invalid actual date of transaction" + actualTransDateStr);
          }
        }
      }
    } else {
      boolean transMandate = ic.getActualTransConfigByType(type) != null
          && !ic.getActualTransConfigByType(type).getTy().equals("0") && !ic
          .getActualTransConfigByType(type).getTy().equals("1");
      if (transMandate && type != null && !type.isEmpty()) {
        xLogger.warn(
            "Actual date is mandatory for the transaction type {0} based on the configuration",
            type);
        errors.add("Actual date of transaction is required but not given");
      }
    }
    xLogger.fine("Exiting populateTransactionFromCSVString");
    return errors;
  }

  /**
   * Parse the uploaded file that contains Transactions along with Inventory metadata.
   */
  public static void parseUploadedManualTransactions(ResourceBundle backendMessages,
                                                     ResourceBundle messages,
                                                     Long domainId, Long kioskId, String userId,
                                                     String blobKeyStr)
      throws IOException {
    xLogger.fine("Entering parseUploadedManualTransactions, kioskId: {0}", kioskId);
    // Read the csv line by line.
    // Ignore the first line i.e. the header
    // Parse the line
    // Create a MnlTransaction object from every line
    // Continue to read all the lines.
    // In case of errors, accumulate all syntax errors and show them to the user. Do not store the MnlTransactions in the datastore.
    // In case there are no errors, store the manual transactions in the data store.
    BufferedReader bufReader = getReader(blobKeyStr);
    int rowNumber = 0;
    String line = bufReader.readLine();
    Date transTimestamp = new Date();// All transactions that are imported will have this timestamp.

    long offset = 0;
    List<MnlTransactionRow> mnlTransRowList = new ArrayList<>();
    List<TransactionRow> transRowList = new ArrayList<>();
    List<InventoryEventRow> inventoryEventRowList = new ArrayList<>();

    BulkUploadMgr.EntityContainer ec = null;
    boolean hasError = false;
    boolean hasTransactionProcessingError = false;
    Map<Long, List<OrderRow>> kidOrderRowListMap = new HashMap<>();

    DomainConfig dc = DomainConfig.getInstance(domainId);

    while (line != null && !line.isEmpty()) {
      ec = new BulkUploadMgr.EntityContainer();
      List<String> errors;
      IMnlTransaction mnlTrans = null;
      int lineLength = byteCountUTF8(line);
      // Skip the first line because it is the header
      if (rowNumber != 0) {
        xLogger.fine("Line being parsed: {0}, hasError: {1}", line, hasError);
        if (dc.autoGI()) {
          ec.messages
              .add("Domain is configured to auto post transactions for Order, Manual transaction" +
                  " upload will not work in such configuration. Please contact System administrator");
          hasError = true;
          // Send the rowNumber as jobId
          updateUploadedMsgLog(offset, "Row " + (rowNumber + 1) + ": " + line, ec,
              BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA, userId, domainId,
              String.valueOf(rowNumber));
        }
        String kName = getKioskNameFromCSVString(domainId, line);
        IKiosk kiosk = getKioskFromKioskName(domainId, kName);
        // If an invalid or null kiosk name was present in the csv line, alert the user with an error message.
        if (!hasError) {
          if (kiosk == null) {
            ec.messages.add(backendMessages.getString("kiosk")
                + " name is required but is not given or is invalid");
            hasError = true;
            // Send the rowNumber as jobId
            updateUploadedMsgLog(offset, "Row " + (rowNumber + 1) + ": " + line, ec,
                BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA, userId, domainId,
                String.valueOf(rowNumber));
          } else {

            mnlTrans = JDOUtils.createInstance(IMnlTransaction.class);
            mnlTrans.setDomainId(kiosk.getDomainId());
            mnlTrans.addDomainIds(kiosk.getDomainIds());
            mnlTrans.setUserId(userId);
            mnlTrans.setTimestamp(transTimestamp);
            errors = fromCSVString(mnlTrans, line, backendMessages);
            xLogger.fine("Populated mnlTrans from csv line: {0}, errors: {1}, hasError: {2}", line,
                errors,
                hasError);
            // If there are any errors, then populate the Entity Container and set the hasError flag.
            // Also update the message log for this row.
            if (errors != null && !errors.isEmpty()) {
              ec.messages = errors;
              hasError = true;
              // Send the rowNumber as jobId
              updateUploadedMsgLog(offset, "Row " + (rowNumber + 1) + ": " + line, ec,
                  BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA, userId, domainId,
                  String.valueOf(rowNumber));
            }
            xLogger.fine("ec.messages: {0}, hasError: {1}", ec.messages, hasError);
            // Only if there are no errors, accumulate the transactions
            if (!ec.hasErrors() && !hasError) {
              // if ( !hasError && mnlTrans != null ) {
              // Check if the manual transaction is of type Transaction or Inventory Update or Order
              // If it's a Transaction, process it as a transaction
              boolean isTransaction = isTransaction(mnlTrans);
              boolean isOrder = isOrder(mnlTrans);
              xLogger.fine("isTransaction: {0}, isOrder: {1}", isTransaction, isOrder);
              // If it's a transaction, populate the transRowList
              if (isTransaction) {
                List<ITransaction> transactions = new ArrayList<>();
                hasTransactionProcessingError =
                    processAsTransaction(mnlTrans, transactions, line, offset,
                        rowNumber + 1, backendMessages) || hasTransactionProcessingError;
                if (!hasTransactionProcessingError && !transactions.isEmpty()) {
                  for (ITransaction transaction : transactions) {
                    TransactionRow transRow = new TransactionRow(offset, rowNumber + 1, line,
                        transaction);
                    transRowList.add(transRow);
                  }
                }
              }

              // If it's an order, then populate the orderRowList
              if (isOrder) {
                // Check if the kioskId is present in the map.
                // Create a list of OrderRow objects for a kioskId and add it to the map.
                // If the kioskId is not present in the map, create a OrderRow for this material and add
                // it to the list
                // If the kioskId is present in the map, then get the list of OrderRow objects for the
                // kioskId, create a new OrderRow for this material and add it to the list
                Long kskId = mnlTrans.getKioskId();
                List<OrderRow> orderRowList;
                OrderRow
                    orderRow =
                    getOrderRow(mnlTrans, line, offset, rowNumber + 1);
                if (kidOrderRowListMap.containsKey(kskId)) {
                  orderRowList = kidOrderRowListMap.get(mnlTrans.getKioskId());
                } else {
                  orderRowList = new ArrayList<>();
                  kidOrderRowListMap.put(kskId, orderRowList);
                }
                orderRowList.add(orderRow);
              }

              if (mnlTrans.getStockoutDuration() > 0) {
                //create inventory events
                inventoryEventRowList.add(getInvEventRow(mnlTrans, line, offset, rowNumber + 1));
              }

              MnlTransactionRow
                  mnlTransRow =
                  new MnlTransactionRow(offset, rowNumber, line, mnlTrans);
              mnlTransRowList.add(mnlTransRow);
            }
          }
        }
      }
      ++rowNumber;
      offset += lineLength + 1;
      line = bufReader.readLine();
      xLogger.fine("offset: {0}", offset);
    }

    bufReader.close();

    // If there are no syntax errors, save the accumulated transactions to the datastore.
    // If there are any errors during update, accumulate and show them to the user.
    // Use InventoryManagementService and update the transactions
    // Using the blob key, get the blob
    xLogger.info("Number of manual transactions accumulated: {0}, Number of transactions: {1}",
        mnlTransRowList.size(), transRowList.size());
    // Save the transactionRowList
    long timestampInMillis = 0;
    boolean hasSaveError = false;

    if (!hasError && !hasTransactionProcessingError && !inventoryEventRowList.isEmpty()) {
      hasSaveError = saveInventoryEventRowList(inventoryEventRowList,
          BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA);

    }

    if (!hasError && !hasTransactionProcessingError && !transRowList.isEmpty() && !hasSaveError) {
      timestampInMillis = fixTimestamp(transRowList);
      if (timestampInMillis != -1) {
        hasSaveError = saveTransactionRowList(transRowList,
            BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA);
      }
    }

    // Save orders if any
    if (!hasError && !hasTransactionProcessingError && !hasSaveError) {
      // Now iterate through the kidOrderRowMap and get the order rows for every kiosk. Save the orderRowList for every kiosk.
      if (!kidOrderRowListMap.isEmpty()) {
        Set<Long> kidSet = kidOrderRowListMap.keySet();
        // Iterate through the kidSet
        for (Long kid : kidSet) {
          List<OrderRow> orl = kidOrderRowListMap.get(kid);
          xLogger.info("Number of order rows for kid: {0}", orl.size());
          Long orderId = saveOrderRowList(orl,
              BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA);
          if (orderId == null) {
            hasSaveError = true;
            break;
          }
        }
      }
    }

    // Save manual transactions if any
    if (!hasError && !hasTransactionProcessingError && !mnlTransRowList.isEmpty()
        && !hasSaveError) {
      saveMnlTransactionRowList(mnlTransRowList);
    }
    // Finalize upload
    String
        uploadedKey =
        BulkUploadMgr
            .getUploadedKey(domainId, BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA,
                userId);
    xLogger.fine("Got uploadedKey: {0}", uploadedKey);
    finalizeUpload(uploadedKey);
    xLogger.fine("Finalized upload");
  }

  @SuppressWarnings("unchecked")
  public static Results getManuallyUploadedTransactions(Long domainId, Long kioskId, Date sinceDate,
                                                        Date untilDate, PageParams pageParams)
      throws ServiceException {
    //Now execute the query and return the results
    Results results = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Form the query
    Query query = pm.newQuery(JDOUtils.getImplClass(IMnlTransaction.class));
    String filter = "";
    String params = "";
    HashMap<String, Object> paramsMap = new HashMap<>();
    if (kioskId != null) { // Add the kiosk filter, if needed
      filter += "kId == kioskIdParam";
      params += "Long kioskIdParam";
      paramsMap.put("kioskIdParam", kioskId);
    } else if (domainId != null) {
      filter += "dId.contains(domainIdParam)";
      params += "Long domainIdParam";
      paramsMap.put("domainIdParam", domainId);
    }
    // From Date
    if (sinceDate != null) {
      if (!filter.isEmpty()) {
        filter += " && ";
        params += ", ";
      }
      filter += "rp > sinceDateParam";
      params += "Date sinceDateParam";
      query.declareImports("import java.util.Date");
      paramsMap.put("sinceDateParam", LocalDateUtil.getOffsetDate(sinceDate, -1,
          Calendar.MILLISECOND)); // we reduce this date by 1 millisec, so that we can avoid a >= query on time (more efficient to do a single comparator in GAE)
    }
    if (untilDate != null) {
      if (!filter.isEmpty()) {
        filter += " && ";
        params += ", ";
      }
      filter += "rp < untilDateParam";
      params += "Date untilDateParam";
      query.declareImports("import java.util.Date");
      paramsMap.put("untilDateParam", untilDate);
    }
    // Update query
    query.setFilter(filter);
    query.declareParameters(params);
    query.setOrdering("t desc");
    // Add pagination parameters, if needed
    if (pageParams != null) {
      QueryUtil.setPageParams(query, pageParams);
    }
    // Execute query
    try {
      xLogger.fine("Query: {0}, paramsMap: {1}", query.toString(), paramsMap.toString());
      List<IMnlTransaction> iList = (List<IMnlTransaction>) query.executeWithMap(paramsMap);
      iList
          .size(); // this is to force retrieval before closing PM; TODO change this later to be more efficient
      // Get the cursor of the next element in the result set (for future iteration, efficiently)
      String cursorStr = QueryUtil.getCursor(iList);
      iList = (List<IMnlTransaction>) pm.detachCopyAll(iList);
      // Create the result set
      results = new Results(iList, cursorStr);
    } catch (Exception e) {
      xLogger.warn("Exception: {0}", e.getMessage());
      throw new ServiceException(e.getMessage());
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return results;
  }

  public static class OrderRow {
    private long offset;
    private int rowNumber;
    private String line;
    private ITransaction orderTrans;
    private List<String> orderTags = null;
    private IMnlTransaction mnlTransaction;

    protected OrderRow(long offset, int rowNumber, String line, ITransaction orderTrans,
                       List<String> orderTags,
                       IMnlTransaction mnlTransaction) {
      this.offset = offset;
      this.rowNumber = rowNumber;
      this.line = line;
      this.orderTrans = orderTrans;
      this.orderTags = orderTags;
      this.mnlTransaction = mnlTransaction;
    }
  }

  public static class MnlTransactionRow {
    private long offset;
    private int rowNumber;
    private String line;
    private IMnlTransaction mnlTransaction;

    private MnlTransactionRow(long offset, int rowNumber, String line,
                              IMnlTransaction mnlTransaction) {
      this.offset = offset;
      this.rowNumber = rowNumber;
      this.line = line;
      this.mnlTransaction = mnlTransaction;
    }
  }

  public static class TransactionRow {
    private long offset;
    private int rowNumber;
    private String line;
    private ITransaction transaction;

    private TransactionRow(long offset, int rowNumber, String line, ITransaction transaction) {
      this.offset = offset;
      this.rowNumber = rowNumber;
      this.line = line;
      this.transaction = transaction;
    }
  }

  public static class InventoryEventRow {
    private long offset;
    private int rowNumber;
    private String line;
    private IInvntryEvntLog invEventLog;
    private String userId;

    protected InventoryEventRow(long offset, int rowNumber, String line,
                                IInvntryEvntLog invEventLog, String userId) {
      this.offset = offset;
      this.rowNumber = rowNumber;
      this.line = line;
      this.invEventLog = invEventLog;
      this.userId = userId;
    }
  }


  public static List<String> fromCSVString(IMnlTransaction mnlTransaction, String csvLine,
                                           ResourceBundle backendMessages) {
    xLogger.fine("Entering fromCSVString");
    List<String> errors = new ArrayList<String>();
    if (csvLine == null || csvLine.isEmpty()) {
      errors.add("Invalid or null row ");
      return errors;
    }

    boolean done = false;
    // Get a tokens from the csv
    String[] tokens = StringUtil.getCSVTokens(csvLine);
    if (tokens == null || tokens.length == 0) {
      errors.add("No fields specified");
      done = true;
    }
    int i = 0;
    int size = 0;
    if (!done) {
      size = tokens.length;
      xLogger.fine("numberOfTokens in CSV: {0}", size);
    }

    // Kiosk Name as it appears in Logi web - Mandatory
    Long kioskId = null;
    if (!done) {
      // Kiosk Name - mandatory
      String kioskName = tokens[i].trim();
      if (kioskName.isEmpty()) {
        if (backendMessages != null) {
          errors.add(backendMessages.getString("kiosk") + " name is required but not given");
        } else {
          errors.add("Entity name is required but not given");
        }

      } else {
        // Get the Kiosk Id from Kiosk Name
        kioskId = MnlTransactionUtil.getKioskIdFromKioskName(mnlTransaction.getDomainId(), kioskName);
        if (kioskId == null) {
          if (backendMessages != null) {
            errors.add(backendMessages.getString("kiosk") + " is not found " + kioskName);
          } else {
            errors.add("Entity is not found " + kioskName);
          }

        } else {
          mnlTransaction.setKioskId(kioskId);
        }
      }
    }
    if (++i == size) {
      errors.add("No fields specified after kiosk name");
      done = true;
    }

    // Material Name as it appears in Logi Web - mandatory
    Long materialId = null;
    if (!done) {
      String materialName = tokens[i].trim();
      if (materialName.isEmpty()) {
        errors.add("Material name is required but not given");
      } else {
        // Material Id from Material Name
        materialId = MnlTransactionUtil.getMaterialIdFromMaterialName(mnlTransaction.getDomainId(), materialName);
        if (materialId == null) {
          errors.add("Material name is not found " + materialName);
        } else {
          mnlTransaction.setMaterialId(materialId);
        }
      }
    }

    if (++i == size) {
      errors.add("No fields specified after material name");
      done = true;
    }

    // Opening Stock - mandatory
    if (!done) {
      String openingStockStr = tokens[i].trim();
      BigDecimal openingStock = null;
      if (openingStockStr.isEmpty()) {
        errors.add("Opening stock is not specified");
      } else {
        try {
          openingStock = new BigDecimal(openingStockStr);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when parsing opening stock in row: {1}", e.getClass().getName(),
              e.getMessage());
        }
        if (BigUtil.isInvalidQ(openingStock)) {
          errors.add("Invalid opening stock " + openingStockStr);
        } else {
          mnlTransaction.setOpeningStock(openingStock);
        }
      }
    }

    // Reporting period - will be in Constants.DATE_FORMAT_CSV format
    if (++i < size) {
      String reportingPeriodStr = tokens[i].trim();
      Date reportingPeriod = null;
      if (!reportingPeriodStr.isEmpty()) {
        Calendar cal = GregorianCalendar.getInstance();
        try {
          reportingPeriod =
              LocalDateUtil.parseCustom(reportingPeriodStr, Constants.DATE_FORMAT_CSV, null);
          cal.setTime(reportingPeriod);
          LocalDateUtil.resetTimeFields(cal);
          mnlTransaction.setReportingPeriod(cal.getTime());
        } catch (ParseException pe) {
          xLogger.warn("{0} when parsing reporting period. Message: {1}", pe.getClass().getName(),
              pe.getMessage());
          errors.add("Invalid date format for reporting period " + reportingPeriodStr);
        }
      }
    }

    // Receipts
    if (++i < size) {
      String receiptsStr = tokens[i].trim();
      BigDecimal receipts = null;
      if (!receiptsStr.isEmpty()) {
        try {
          receipts = new BigDecimal(receiptsStr);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when parsing receipts in row: {1}", e.getClass().getName(),
              e.getMessage());
        }
        if (BigUtil.isInvalidQ(receipts)) {
          errors.add("Invalid receipts quantity " + receiptsStr);
        } else {
          mnlTransaction.setReceiptQuantity(receipts);
        }
      }
    }

    // Issues
    if (++i < size) {
      String issuesStr = tokens[i].trim();
      BigDecimal issues = null;
      if (!issuesStr.isEmpty()) {
        try {
          issues = new BigDecimal(issuesStr);
        } catch (NumberFormatException e) {
          xLogger
              .warn("{0} when parsing issues in row: {1}", e.getClass().getName(), e.getMessage());
        }
        if (BigUtil.isInvalidQ(issues)) {
          errors.add("Invalid issue quantity " + issuesStr);
        } else {
          mnlTransaction.setIssueQuantity(issues);
        }
      }
    }

    // Discards
    if (++i < size) {
      String discardsStr = tokens[i].trim();
      BigDecimal discards = null;
      if (!discardsStr.isEmpty()) {
        try {
          discards = new BigDecimal(discardsStr);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when parsing discards in row: {1}", e.getClass().getName(),
              e.getMessage());
        }
        if (BigUtil.isInvalidQ(discards)) {
          errors.add("Invalid discards quantity " + discardsStr);
        } else {
          mnlTransaction.setDiscardQuantity(discards);
        }
      }
    }

    // Stock out duration
    if (++i < size) {
      String stockoutDurStr = tokens[i].trim();
      Integer stockoutDur = null;
      if (!stockoutDurStr.isEmpty()) {
        try {
          stockoutDur = Integer.valueOf(stockoutDurStr);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when parsing stockout duration in row: {1}", e.getClass().getName(),
              e.getMessage());
        }
        if (stockoutDur == null || stockoutDur < 0) {
          errors.add("Invalid stock out duration " + stockoutDurStr);
        } else {
          mnlTransaction.setStockoutDuration(NumberUtil.getIntegerValue(stockoutDur));
        }
      }
    }
    // Manual Consumption Rate
    if (++i < size) {
      String manConsRateStr = tokens[i].trim();
      BigDecimal manConsRate = null;
      if (!manConsRateStr.isEmpty()) {
        try {
          manConsRate = new BigDecimal(manConsRateStr);
        } catch (NumberFormatException e) {
          xLogger
              .warn("{0} when parsing manual consumption rate in row: {1}", e.getClass().getName(),
                  e.getMessage());
        }
        if (BigUtil.isInvalidQ(manConsRate)) {
          errors.add("Invalid manual consumption rate " + manConsRateStr);
        } else {
          mnlTransaction.setManualConsumptionRate(manConsRate);
        }
      }
    }

    // Computed Consumption rate
    if (++i < size) {
      String compConsRateStr = tokens[i].trim();
      BigDecimal compConsRate = null;
      if (!compConsRateStr.isEmpty()) {
        try {
          compConsRate = new BigDecimal(compConsRateStr);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when parsing computed consumption rate in row: {1}",
              e.getClass().getName(), e.getMessage());
        }
        if (BigUtil.isInvalidQ(compConsRate)) {
          errors.add("Invalid computed consumption rate " + compConsRateStr);
        } else {
          mnlTransaction.setComputedConsumptionRate(compConsRate);
        }
      }
    }

    // Manual Order Quantity
    if (++i < size) {
      String manOrderQtyStr = tokens[i].trim();
      BigDecimal manOrderQty = null;
      if (!manOrderQtyStr.isEmpty()) {
        try {
          manOrderQty = new BigDecimal(manOrderQtyStr);
        } catch (NumberFormatException e) {
          xLogger.warn("{0} when parsing manual order quantity in row: {1}", e.getClass().getName(),
              e.getMessage());
        }
        if (BigUtil.isInvalidQ(manOrderQty)) {
          errors.add("Invalid manual order quantity " + manOrderQtyStr);
        } else {
          mnlTransaction.setOrderedQuantity(manOrderQty);
        }
      }
    }

    // Computed Order Quantity
    if (++i < size) {
      String compOrderQtyStr = tokens[i].trim();
      BigDecimal compOrderQty = null;
      if (!compOrderQtyStr.isEmpty()) {
        try {
          compOrderQty = new BigDecimal(compOrderQtyStr);
        } catch (NumberFormatException e) {
          xLogger
              .warn("{0} when parsing computed order quantity in row: {1}", e.getClass().getName(),
                  e.getMessage());
        }
        if (BigUtil.isInvalidQ(compOrderQty)) {
          errors.add("Invalid computed order quantity " + compOrderQtyStr);
        } else {
          mnlTransaction.setFulfilledQuantity(compOrderQty);
        }
      }
    }

    // Tags
    if (++i < size) {
      List<String> tagList = new ArrayList<String>();
      String tagsStr = tokens[i].trim();
      if (tagsStr != null && !tagsStr.isEmpty()) {
        String[] tags = tagsStr.split(";");
        for (int j = 0; j < tags.length; j++) {
          tagList.add(tags[j]);
        }
        mnlTransaction.setTgs(new TagDao().getTagsByNames(tagList, ITag.ORDER_TAG));
      }
    }

    // Related entity
    if (++i < size) {
      String relatedEntityName = tokens[i].trim();
      if (relatedEntityName != null && !relatedEntityName.isEmpty()) {
        // Get the Kiosk Id from Kiosk Name
        Long
            linkedKioskId =
            MnlTransactionUtil.getKioskIdFromKioskName(mnlTransaction.getDomainId(), relatedEntityName);
        if (linkedKioskId == null) {
          errors.add("Invalid " + backendMessages.getString("bck.vendor.lower") + " name "
              + relatedEntityName);
        } else {
          mnlTransaction.setVendorId(linkedKioskId);
        }
      }
    }
    return errors;
  }
}
