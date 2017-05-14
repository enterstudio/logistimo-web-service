package com.logistimo.api.models.configuration;

import java.util.List;

/**
 * Created by naveensnair on 24/11/14.
 */
public class OrdersConfigModel {
  public String og; // ordergeneration
  public boolean agi; // auto goods issue on order shipped
  public boolean agr; // auto goods receipt on order fulfilled
  public boolean tm; // transporter mandatory
  public boolean tiss; // transporter details in status sms
  public boolean ao; // allow orders with no items
  public boolean aof; // allow mark order as fulfilled
  public String po; // payment options
  public String ps; // package sizes
  public String vid; // vendor Id
  public boolean eex; // enable export
  public String et; // export times
  public String an; // export user ids
  public String ip; // isPublic
  public String bn; // banner
  public String hd; // heading
  public String cp; // copyright
  public String url; // public url
  public boolean spb; // show stock levels on public board
  public String createdBy;// last update made by this user
  public String lastUpdated;// last updated time
  public String fn; // first name
  public boolean dop; //disable order pricing
  public boolean enOrdRsn; // enable recommended order reasons
  public String orsn; // recommended order reasons
  public boolean md; // Mark reason field as mandatory
  public List<String> usrTgs; // user tags for orders export schedule
  public boolean aoc; // Allow marking order as confirmed
  public boolean asc; //Allocate stock on confirmation
  public boolean tr; //Rename Transfer orders as Release orders
  public String orr; // Order Recommendation Reasons
  public boolean orrm; // Mark Order recommendation reasons mandatory
  public String eqr; // Editing quantity Reasons
  public boolean eqrm; // Mark Editing quantity Reasons mandatory
  public String psr; // Partial shipment Reasons
  public boolean psrm; // Mark Partial Shipment reasons mandatory
  public String pfr; // Partial fulfillment Reasons
  public boolean pfrm; // Mark partial fulfillment reasons mandatory
  public String cor; // Cancelling order Reasons
  public boolean corm; // Mark cancelling order reasons mandatory
  public boolean acs; // Allow creating shipments
}
