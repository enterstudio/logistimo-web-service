package com.logistimo.proto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by vani on 03/11/16.
 */
public class MobileOrderModel {
  /**
   * String version
   */
  public String v;
  /**
   * String status
   */
  public String st;
  /**
   * String tracking id/order id
   */
  public Long tid;
  /**
   * String order reference id
   */
  public String rid;

  /**
   * String order status
   */
  public String ost;
  /**
   * Number of items
   */
  public Integer q;
  /**
   * Created by user id
   */
  public String cbid;
  /**
   * Created by user full name
   */
  public String cbn;
  /**
   * Order creation time
   */
  public String t;
  /**
   * Kiosk id
   */
  public Long kid;
  /**
   * Kiosk name
   * Kiosk name
   */
  public String knm;
  /**
   * Kiosk city
   */
  public String kcty;
  /**
   * Vendor id
   */
  public Long vid;
  /**
   * Vendor name
   */
  public String vnm;
  /**
   * Vendor city
   */
  public String vcty;
  /**
   * Comma seperated tags
   */
  public String tg;
  /**
   * Order updated time
   */
  public String ut;
  /**
   * Total price of order
   */
  public BigDecimal tp;
  /**
   * Currency
   */
  public String cu;
  /**
   * Message
   */
  public String ms;
  /**
   * Required by date
   */
  public String rbd;

  /**
   * Expected time of arrival
   */
  public String eta;
  /**
   * Reason for cancellation
   */
  public String rsnco;
  /**
   * efts -  Expected fulfillment time-ranges, is a comma-separated list of local time-ranges of the format
   * <fromTime1>-<toTime1>,..., where <toTime1> is optional, and each time is in the format of user's locale
   * (either <dd/MM/yy hh:mm am/pm> or <MM/dd/yy hh:mm am/pm>, according to locale) and local timezone.  'toTime'
   * is optional. (NOTE: year can be in either yy or yyyy format)
   */
  public String efts;

  /**
   * cft - Confirmed time range in the format <fromTime>-<toTime>, with <toTime> being optional, and all times
   * being in the format of user's locale (either <dd/MM/yy hh:mm am/pm> or <MM/dd/yy hh:mm am/pm>) and
   * local timezone. (NOTE: year can be in either yy or yyyy format)
   */
  public String cft;
  /**
   * pymt - amount paid on this order so far
   */
  public BigDecimal pymt;

  /**
   * popt - payment option, which is any free-form string.
   */
  public String popt;

  /**
   * pksz - package size, which is any free-form string.
   */
  public String pksz;
  /**
   * Custom user id
   */
  public String cuid;
  /**
   * Custom kiosk id
   */
  public String ckid;

  /**
   * Custom vendor id
   */
  public String cvid;
  /**
   * Credit limit
   */
  public BigDecimal crl;
  /**
   * Payable
   */
  public BigDecimal pybl;

  /**
   * List of demand items
   */
  public List<MobileDemandItemModel> mt;
  /**
   * List of Shipments
   */
  public List<MobileShipmentModel> shps;
  /**
   * List of comments/conversations
   */
  public MobileConversationModel cmnts;
  /**
   * Order shipped time
   */
  public String osht;
  /**
   * Order updated by user id
   */
  public String ubid;
  /**
   * Order updated by user name
   */
  public String ubn;
}
