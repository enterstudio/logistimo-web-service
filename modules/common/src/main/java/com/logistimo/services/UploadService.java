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
