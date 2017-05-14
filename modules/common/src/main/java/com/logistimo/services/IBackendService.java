package com.logistimo.services;

import java.util.List;

/**
 * Created by charan on 16/03/15.
 */
public interface IBackendService {

  String getBackendAddress(String exportBackend);

  String getBackupURL(String backupUrl, List<String> doNotBackup);
}
