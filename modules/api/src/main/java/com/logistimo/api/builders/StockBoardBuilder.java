package com.logistimo.api.builders;

import org.apache.commons.lang.StringUtils;
import com.logistimo.config.models.KioskConfig;
import com.logistimo.config.models.StockboardConfig;
import com.logistimo.config.entity.IConfig;
import com.logistimo.api.models.StockBoardModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naveensnair on 15/04/15.
 */
public class StockBoardBuilder {

  public String getKey(Long kioskId) {
    if (kioskId != null) {
      return IConfig.CONFIG_KIOSK_PREFIX + kioskId.toString();
    }
    return null;
  }

  public StockboardConfig buildStockBoardConfig(StockboardConfig sb, StockBoardModel model) {
    if (sb != null && model != null) {
      if (model.esb > 0) {
        sb.setEnabled(model.esb);
      } else {
        sb.setEnabled(0);
      }
      sb.setEnabled(model.esb);
      if (model.itv > 10) {
        sb.setMaxItems(StockboardConfig.MAX_ITEMS);
      } else {
        sb.setMaxItems(model.itv);
      }
      if (model.rd > 0) {
        sb.setRefreshDuration(model.rd);
      } else {
        sb.setRefreshDuration(sb.REFRESH_DURATION_DEFAULT);
      }
      if (model.sci > 0) {
        sb.setScrollInterval(model.sci);
      } else {
        sb.setScrollInterval(sb.SCROLL_INTERVAL_DEFAULT);
      }
      if (model.hsi > 0) {
        sb.setHorScrollInterval(model.hsi);
      } else {
        sb.setHorScrollInterval(sb.HOR_SCROLL_INTERVAL_DEFAULT);
      }
      if (StringUtils.isNotEmpty(model.msg)) {
        List<String> hmsg = new ArrayList<String>();
        String[] msgarr = model.msg.split("\n");
        for (String s : msgarr) {
          hmsg.add(s);
        }
        if (hmsg.size() > 0) {
          sb.setHorScrlMsgsList(hmsg);
        }
      }
    }

    return sb;
  }

  public StockBoardModel buildStockBoardModel(KioskConfig kc, StockBoardModel model) {
    try {
      if (kc != null) {
        StockboardConfig sbc = kc.getStockboardConfig();
        if (sbc != null) {
          model.esb = sbc.getEnabled();
          if (sbc.getMaxItems() >= 0) {
            model.itv = sbc.getMaxItems();
          } else {
            model.itv = StockboardConfig.MAX_ITEMS;
          }
          if (sbc.getRefreshDuration() >= 0) {
            model.rd = sbc.getRefreshDuration();
          } else {
            model.rd = StockboardConfig.REFRESH_DURATION_DEFAULT;
          }
          if (sbc.getScrollInterval() >= 0) {
            model.sci = sbc.getScrollInterval();
          } else {
            model.sci = StockboardConfig.SCROLL_INTERVAL_DEFAULT;
          }
          if (sbc.getHorScrollInterval() >= 0) {
            model.hsi = sbc.getHorScrollInterval();
          } else {
            model.hsi = sbc.getHorScrollInterval();
          }
          if (sbc.getHorScrlMsgsList() != null && sbc.getHorScrlMsgsList().size() > 0) {
            List<String> horScrlMsgList = sbc.getHorScrlMsgsList();
            if (horScrlMsgList.size() > 0) {
              model.msg = StringUtils.join(horScrlMsgList, "\n");
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return model;

  }

}
