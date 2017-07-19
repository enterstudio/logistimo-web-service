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

/**
 *
 */
package com.logistimo.mappers;


import com.logistimo.bulkuploads.BulkImportMapperContants;
import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.services.blobstore.BlobstoreRecordKey;
import com.logistimo.services.mapper.DatastoreMutationPool;
import com.logistimo.services.mapper.Entity;
import com.logistimo.services.mapper.GenericMapper;
import com.logistimo.services.mapper.Key;

import com.logistimo.entity.UploadedMsgLog;
import com.logistimo.logger.XLog;

import java.util.Date;

/**
 * Import a record from the CSV file in blobstore of a particular type
 *
 * @author Arun
 */
public class BulkImportMapper
    extends GenericMapper<BlobstoreRecordKey, byte[], NullWritable, NullWritable> {

  // Logger
  private static final XLog xLogger = XLog.getLog(BulkImportMapper.class);


  @Override
  public void map(BlobstoreRecordKey key, byte[] segment, Context context) {
    String sourceUserId = context.getConfiguration().get(BulkImportMapperContants.USERID);
    String type = context.getConfiguration().get(BulkImportMapperContants.TYPE);
    String domainIdStr = context.getConfiguration().get(BulkImportMapperContants.DOMAINID);
    xLogger.fine("Entered BulkImportManager.map");
    // Get the params.

    Long domainId = null;
    // Get the line
    try {
      if (domainIdStr != null && !domainIdStr.isEmpty()) {
        domainId = Long.valueOf(domainIdStr);
      }
      String line = new String(segment, "UTF-8");
      if (line.isEmpty() || line.startsWith("Operation") || (BulkUploadMgr.TYPE_ASSETS.equals(type)
          && (line.startsWith("Entity") || line.startsWith("Store"))
          && line.endsWith("Year of manufacture of monitoring asset"))) {
        // Empty or Header lines; ignore
        return;
      }
      // Tokenize
      BulkUploadMgr.EntityContainer
          ec =
          BulkUploadMgr.processEntity(type, line, domainId, sourceUserId);
      if (ec != null && ec.hasErrors()) {
        updateUploaded(key.getOffset(), line, ec, type, sourceUserId, domainId, context);
      }
    } catch (Exception e) {
      xLogger.warn("{0} during BulkImportMapper.map for user {1}, domain {2}, type {3}: {4}",
          e.getClass().getName(), sourceUserId, domainIdStr, type, e.getMessage());
    }
    xLogger.fine("Existing BulkImportManager.map");
  }

  // Update the uploaded object, in case of errors
  private void updateUploaded(long offset, String csvLine, BulkUploadMgr.EntityContainer ec, String type,
                              String sourceUserId, Long domainId, Context context) {
    xLogger.fine("Entered updateUploaded");
    // Get the uploaded object
    String uploadedKey = BulkUploadMgr.getUploadedKey(domainId, type, sourceUserId);
    try {
      // Form the text to be appended
      String
          lineMsg =
          BulkUploadMgr.getErrorMessageString(offset, csvLine, ec.operation, ec.getMessages(), ec.getMessagesCount());
      // Create an UploadedMsgLog
      Entity entity = new Entity(UploadedMsgLog.class.getSimpleName(), new Key(0l));
      if (lineMsg != null && !lineMsg.isEmpty()) {
        entity.setProperty("uploadedId", uploadedKey);
        entity.setProperty("msg", lineMsg);
        entity.setProperty("t", new Date());
        entity.setProperty("dId", domainId);
        DatastoreMutationPool mutationPool = this.getContext(context).getMutationPool();
        mutationPool.put(entity);
      }
    } catch (Exception e) {
      xLogger.warn("{0} when trying to update Uploaded object {1}: {2}", e.getClass().getName(),
          uploadedKey, e.getMessage(), e);
    }
    xLogger.fine("Exiting updateUploaded");
  }
}
