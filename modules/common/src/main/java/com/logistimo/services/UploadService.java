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

package com.logistimo.services;

import com.logistimo.entity.IDownloaded;
import com.logistimo.entity.IUploaded;

import java.util.List;


public interface UploadService extends Service {
  IUploaded getUploaded(String filename, String version, String locale)
      throws ServiceException, ObjectNotFoundException;

  IUploaded getLatestUpload(String filename, String locale) throws ServiceException;

  IUploaded getUploaded(String key) throws ServiceException, ObjectNotFoundException;

  IUploaded getUploadedByJobId(String mrJobId) throws
      ServiceException; // get an Uploaded object based on a MapReduce job Id (e.g. as used by bulk import of CSV files)

  void addNewUpload(List<IUploaded> uploads) throws ServiceException;

  void addNewUpload(IUploaded upload) throws ServiceException;

  void removeUploaded(String id) throws ServiceException;

  void updateUploaded(IUploaded u) throws ServiceException, ObjectNotFoundException;

  void storeDownloaded(IDownloaded downloaded) throws ServiceException;
}
