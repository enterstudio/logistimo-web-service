package com.logistimo.api.builders;

import com.logistimo.config.models.BBoardConfig;
import com.logistimo.api.models.configuration.BulletinBoardConfigModel;

/**
 * Created by naveensnair on 18/12/14.
 */
public class BulletinBoardBuilder {

  public BBoardConfig buildBBoardConfig(BulletinBoardConfigModel model) {
    BBoardConfig config = null;
    int enabled = BBoardConfig.DISABLED;
    int data_duration = BBoardConfig.DATA_DURATION_DEFAULT;
    int refresh_duration = BBoardConfig.REFRESH_DURATION_DEFAULT;
    int scroll_interval = BBoardConfig.SCROLL_INTERVAL_DEFAULT;
    int max_items = BBoardConfig.MAX_ITEMS;
    boolean pauseOnHover = false;
    boolean showNav = false;
    if (model != null) {
      if (model.ecl != null) {
        enabled = Integer.parseInt(model.ecl);
      }
      if (model.dd != null) {
        data_duration = Integer.parseInt(model.dd);
      }
      if (model.rd != null) {
        refresh_duration = Integer.parseInt(model.rd);
      }
      if (model.si != null) {
        scroll_interval = Integer.parseInt(model.si);
      }
      if (model.iob != null) {
        if (Integer.parseInt(model.iob) > BBoardConfig.MAX_ITEMS) {
          max_items = BBoardConfig.MAX_ITEMS;
        } else {
          max_items = Integer.parseInt(model.iob);
        }
      }
      if (model.sn) {
        showNav = model.sn;
      }
      config =
          new BBoardConfig(enabled, data_duration, refresh_duration, scroll_interval, pauseOnHover,
              showNav, max_items);
    }

    return config;
  }

  public BulletinBoardConfigModel buildModel(BBoardConfig config) {
    BulletinBoardConfigModel model = new BulletinBoardConfigModel();
    if (config != null) {
      model.ecl = Integer.toString(config.getEnabled());
      model.dd = Integer.toString(config.getDataDuration());
      model.rd = Integer.toString(config.getRefreshDuration());
      model.si = Integer.toString(config.getScrollInterval());
      model.iob = Integer.toString(config.getMaxItems());
      model.sn = config.showNav();
    }

    return model;
  }
}
