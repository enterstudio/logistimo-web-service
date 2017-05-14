package com.logistimo.proto;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by charan on 01/11/16.
 */
public class UpdateOrderRequest {
  /**
   * String version
   */
  public String v;
  /**
   * String userId
   */
  public String uid;
  /**
   * Kiosk/Entity Id
   */
  public Long kid;
  /**
   * Order type - Sales or purchase
   * See IOrder.TYPE_SALE and IOrder.TYPE_PURCHASE
   */
  public String oty = "prc";
  /**
   * Comma seperated tags
   */
  public String tg;

  /**
   * List of materials
   */
  public List<MaterialRequest> mt;
  /**
   * target user Id
   */
  public String duid;
  /**
   * Message
   */
  public String ms;
  /**
   * Tracking Id
   */
  public Long tid;
  /**
   * Reference Id
   */
  public String rid;
  /**
   * Linked kiosk or vendor id
   */
  public Long lkid;

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
   * ost - initial status to which the order should be assigned, if any. Default is 'pn' (pending). Valid status
   * values include: pn (pending), cf (confirmed), cm (completed or shipped), cn (cancelled), fl (fulfilled).
   */
  public String ost;

  /**
   * Latitude
   */
  public Double lat;

  /**
   * Longitude
   */
  public Double lng;

  /**
   * GPS-accuracy in meters
   */
  public Double gacc;

  /**
   * GPS geo-code acquisition error, if any - 1 = position rejected by user, 2 = position unavailable, 3 = timed-out,
   * or any free-form error message
   */
  public String gerr;


  /**
   * Geo altitude
   */
  public Double galt;

  /**
   * Required by date
   */
  public Date rbd;

  /**
   * Expected time of arrival
   */
  public Date eta;

  /**
   * Transfer or Non transfer
   */
  public Integer trf;
}
