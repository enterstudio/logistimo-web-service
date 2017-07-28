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

package com.logistimo.reports.plugins;

import java.util.Map;

/** Created by mohan on 25/03/17. */
public class Report {
  public Long did;
  public Long mid;
  public Long kid;
  public Long lkid;
  public Long uid;
  public Long mtag;
  public Long ktag;
  public Long utag;
  public String atype;
  public String mtype;
  public String dmodel;
  public String myear;
  public String country;
  public String state;
  public String district;
  public String city;
  public String taluk;
  public String vid;
  public String dvid;
  public String t;
  public Long akc;
  public Long boc;
  public Float brv;
  public Long cfoc;
  public Long cnoc;
  public Long dc;
  public Long dlt;
  public Long dltc;
  public Float dq;
  public Long dsc;
  public Float dsq;
  public Long emlc;
  public Long floc;
  public Long gmc;
  public Float gmd;
  public Long gmec;
  public Long ic;
  public Float iq;
  public Long kc;
  public Long lkc;
  public Long lmc;
  public Float lmd;
  public Long lmec;
  public Long lnc;
  public Long mac;
  public Long mawa;
  public Long mc;
  public Long miac;
  public Long mlwa;
  public Long mwa;
  public Long oc;
  public Long os;
  public Long pnoc;
  public Long poc;
  public Long pt;
  public Long ptc;
  public Long rc;
  public Float rq;
  public Float rrv;
  public Long rtc;
  public Float rtq;
  public Long sc;
  public Float sdf;
  public Long shoc;
  public Long smsc;
  public Long soc;
  public Float sod;
  public Long soec;
  public Long sosc;
  public Long lmsc;
  public Long gmsc;
  public Float sq;
  public Long tasec;
  public Long tc;
  public Long tec;
  public Long trc;
  public Float trq;
  public Float troiq;
  public Float trorq;
  public Long tuc;
  public Long uc;
  public Long wc;
  public Float wq;
  public Long so70;
  public Long so80;
  public Long so90;
  public Long so100;
  public Long lm70;
  public Long lm80;
  public Long lm90;
  public Long lm100;
  public Long gm70;
  public Long gm80;
  public Long gm90;
  public Long gm100;
  public Long sa70;
  public Long sa80;
  public Long sa90;
  public Long sa100;
  public Long sak70;
  public Long sak80;
  public Long sak90;
  public Long sak100;
  public Long sam70;
  public Long sam80;
  public Long sam90;
  public Long sam100;
  public Long lic;
  public Long soed;
  public Long lmed;
  public Long gmed;
  public Long sad;
  public Long iec;
  public Long rec;
  public Long scec;
  public Long trec;
  public Long wked;
  public Long wkec;
  public Long urecs;
  public Long wkecs;
  public Long brecs;
  public Long cdecs;
  public Long stbecs;
  public Long decs;
  public Long wec;
  public Long urec;
  public Long brec;
  public Long cdec;
  public Long stbec;
  public Long dec;
  public Long ured;
  public Long bred;
  public Long cded;
  public Long stbed;
  public Long ded;
  public Long drrt;
  public Long urtwc;
  public Long lac;
  public Long lasc;
  public Long hac;
  public Long hasc;
  public Long lwc;
  public Long lwsc;
  public Long hwc;
  public Long hwsc;
  public Long lec;
  public Long lesc;
  public Long hec;
  public Long hesc;
  public Long pof;
  public Long lad;
  public Long had;
  public Long lwd;
  public Long hwd;
  public Long led;
  public Long hed;
  public Long lec1;
  public Long lec2;
  public Long lec3;
  public Long hec1;
  public Long hec2;
  public Long hec3;
  public Long tec1;
  public Long tec2;
  public Long tec3;
  public Long pad;
  public Long lexd1;
  public Long lexd2;
  public Long lexd3;
  public Long hexd1;
  public Long hexd5;
  public Long hexd10;
  public Long cpty;
  public Long up70;
  public Long up80;
  public Long up90;
  public Long up100;
  //public Long dwn70;
  //public Long dwn80;
  //public Long dwn90;
  //public Long dwn100;
  public Map<String,Float> drsn;

  public Long getDomainId() {
    return did;
  }

  public Long getMaterialId() {
    return mid;
  }

  public Long getKioskId() {
    return kid;
  }

  public Long getLinkedKioskId() {
    return lkid;
  }

  public Long getUserId() {
    return uid;
  }

  public Long getMaterialTag() {
    return mtag;
  }

  public Long getKioskTag() {
    return ktag;
  }

  public Long getUserTag() {
    return utag;
  }

  public String getAssetType() {
    return atype;
  }

  public String getCountry() {
    return country;
  }

  public String getState() {
    return state;
  }

  public String getDistrict() {
    return district;
  }

  public String getCity() {
    return city;
  }

  public String getTaluk() {
    return taluk;
  }

  public String getTime() {
    return t;
  }

  public String setTime(String time) {
    return this.t = time;
  }

  public Long getActiveKioskCount() {
    return akc == null ? 0 :akc;
  }

  public Long getBackOrderCount() {
    return boc == null? 0 : boc;
  }

  public Float getBookedRevenue() {
    return brv == null ? 0 : brv;
  }

  public Long getConfirmedOrderCount() {
    return cfoc == null ? 0 : cfoc;
  }

  public Long getCancelledOrderCount() {
    return cnoc == null ? 0 : cnoc;
  }

  public Long getDemandCounts() {
    return dc == null ? 0 : dc;
  }

  public Long getDeliveryLeadTime() {
    return dlt == null ? 0 : dlt;
  }

  public Long getDeliveryLeadCount() {
    return dltc == null ? 0 : dltc;
  }

  public Float getDemandQuantity() {
    return dq == null ? 0 : dq;
  }

  public Long getDemandShippedCount() {
    return dsc == null ? 0 : dsc;
  }

  public Float getDemandShippedQuantity() {
    return dsq == null ? 0 : dsq;
  }

  public Long getEMailCount() {
    return emlc == null ? 0 : emlc;
  }

  public Long getFulfilledOrderCount() {
    return floc == null ? 0 : floc;
  }

  public Long getGreaterThanMaxCount() {
    return gmc == null ? 0 : gmc;
  }

  public Float getGreaterThanMaxDuration() {
    return gmd == null ? 0 : gmd;
  }

  public Long getStockOutInventoryGreaterThan70() {
    return so70 == null ? 0 : so70;
  }

  public Long getStockOutInventoryGreaterThan80() {
    return so80 == null ? 0 : so80;
  }

  public Long getStockOutInventoryGreaterThan90() {
    return so90 == null ? 0 : so90;
  }

  public Long getStockOutInventory100() {
    return so100 == null ? 0 : so100;
  }

  public Long getMinInventoryGreaterThan70() {
    return lm70 == null ? 0 : lm70;
  }

  public Long getMinInventoryGreaterThan80() {
    return lm80 == null ? 0 : lm80;
  }

  public Long getMinInventoryGreaterThan90() {
    return lm90 == null ? 0 : lm90;
  }

  public Long getMinInventory100() {
    return lm100 == null ? 0 : lm100;
  }

  public Long getMaxInventoryGreaterThan70() {
    return gm70 == null ? 0 : gm70;
  }

  public Long getMaxInventoryGreaterThan80() {
    return gm80 == null ? 0: gm80;
  }

  public Long getMaxInventoryGreaterThan90() {
    return gm90 == null ? 0:gm90;
  }

  public Long getMaxInventory100() {
    return gm100 == null ? 0 : gm100;
  }

  public Long getGreaterThanMaxEventCount() {
    return gmec == null ? 0 : gmec;
  }
  public Long getGreaterThanMaxStateCount() {
    return gmsc == null ? 0 : gmsc;
  }

  public Long getIssueCount() {
    return ic == null ? 0 : ic;
  }

  public Float getIssueQuantity() {
    return iq;
  }

  public Long getKioskCount() {
    return kc == null ? 0 : kc;
  }

  public Long getLiveKioskCount() {
    return lkc == null ? 0 : lkc;
  }

  public Long getLessThanMinCount() {
    return lmc == null ? 0 : lmc;
  }

  public Float getLessThanMinDuration() {
    return lmd == null ? 0 : lmd;
  }

  public Long getLessThanMinEventCount() {
    return lmec == null ? 0 : lmec;
  }
  public Long getLessThanMinStateCount() {
    return lmsc == null ? 0 : lmsc;
  }

  public Long getLoginCount() {
    return lnc == null ? 0:lnc;
  }

  public Long getMonitoredAssetCount() {
    return mac == null ? 0 : mac;
  }

  public Long getMonitoredActiveWorkingAsset() {
    return mawa == null ? 0 : mawa;
  }

  public Long getMaterialCount() {
    return mc == null ? 0 : mc;
  }

  public Long getMonitoringAssetCount() {
    return miac == null ? 0 : miac;
  }

  public Long getMonitoredLiveWorkingAsset() {
    return mlwa == null ? 0 : mlwa;
  }

  public Long getMonitoredWorkingAsset() {
    return mwa == null ? 0 : mwa;
  }

  public Long getOrderCount() {
    return oc == null ? 0 : oc;
  }

  public Long getOrderSize() {
    return os == null ? 0 : os;
  }

  public Long getPendingOrderCount() {
    return pnoc == null ? 0 : pnoc;
  }

  /*public Long getPaidOrderCount() {
    return poc == null ? 0 : poc;
  }*/

  public Long getProcessingTime() {
    return pt == null ? 0 : pt;
  }

  public Long getProcessingTimeCount() {
    return ptc == null ? 0 : ptc;
  }

  public Long getReceiptCount() {
    return rc == null ? 0 : rc;
  }

  public Float getReceiptQuantity() {
    return rq == null ? 0 : rq;
  }

  public Float getRealizableRevenue() {
    return rrv == null ? 0 : rrv;
  }

  public Long getReturnCount() {
    return rtc == null ? 0 : rtc;
  }

  public Float getReturnQuantity() {
    return rtq == null ? 0 : rtq;
  }

  public Long getStockCountCount() {
    return sc == null ? 0 : sc;
  }

  public Float getStockDifferenceQuantity() {
    return sdf == null ? 0 : sdf;
  }

  public Long getShippedOrderCount() {
    return shoc == null ? 0 : shoc;
  }

  public Long getSmsCount() {
    return smsc == null ? 0 : smsc;
  }

  public Long getStockOutCount() {
    return soc == null ? 0 : soc;
  }

  public Float getStockOutDuration() {
    return sod == null ? 0 : sod;
  }

  public Long getStockOutEventDuration() {
    return soed == null ? 0 : soed;
  }
  public Long getLessThanMinEventDuration() {
    return lmed == null ? 0 : lmed;
  }
  public Long getGreaterThanMaxEventDuration() {
    return gmed == null ? 0 : gmed;
  }

  public Long getStockOutEventCount() {
    return soec == null ? 0 : soec;
  }

  public Long getStockOutStateCount() {
    return sosc == null ? 0 : sosc;
  }

  public Float getClosingStock() {
    return sq == null ? 0 : sq;
  }

  public Long getTotalAbnormalStockEventCount() {
    return tasec == null ? 0 : tasec;
  }

  public Long getTransactionCount() {
    return tc == null ? 0 : tc;
  }

  public Long getTransactionEntityCount() {
    return tec == null ? 0 : tec;
  }

  public Long getIssueEntityCount() {
    return iec == null ? 0 : iec;
  }

  public Long getReceiptEntityCount() {
    return rec == null ? 0 : rec;
  }

  public Long getStockCountEntityCount() {
    return scec == null ? 0 : scec;
  }

  public Long getStockAvailabilityDuration() {
    return sad == null ? 0 : sad;
  }

  public Long getStockAvailabilityGreaterThan70() {
    return sa70 == null ? 0 : sa70;
  }

  public Long getStockAvailabilityGreaterThan80() {
    return sa80 == null ? 0 : sa80;
  }

  public Long getStockAvailabilityGreaterThan90() {
    return sa90 == null ? 0 : sa90;
  }

  public Long getStockAvailability100() {
    return sa100 == null ? 0 : sa100;
  }

  public Long getLiveInventoryCount() {
    return lic == null ? 0 : lic;
  }

  public Long getDiscardEntityCount() {
    return wec == null ? 0 : wec;
  }

  public Long getTransferEntityCount() {
    return trec == null ? 0 : trec;
  }

  public Long getTransferCount() {
    return trc == null ? 0 : trc;
  }

  public Float getTransfersQuantity() {
    return trq == null ? 0 : trq;
  }

  public Float getTransferOrderIssueQuantity() {
    return troiq == null ? 0 : troiq;
  }

  public Float getTransferOrderReceiptQuantity() {
    return trorq == null ? 0 : trorq;
  }

  public Long getTransactionUserCount() {
    return tuc == null ? 0 : tuc;
  }

  public Long getUserCount() {
    return uc == null ? 0 : uc;
  }

  public Long getDiscardCount() {
    return wc == null ? 0 : wc;
  }

  public Float getDiscardQuantity() {
    return wq == null ? 0 : wq;
  }

  public Float getOpeningStock() {
    return Math.max(getClosingStock()
        + getIssueQuantity()
        + getDiscardQuantity()
        - getReceiptQuantity()
        - getStockDifferenceQuantity(),0);
  }

  public Long getAssetStatusWorking() {
    return wkecs == null ? 0 : wkecs;
  }

  public Long getAssetStatusUnderRepair() {
    return urecs == null ? 0 : urecs;
  }

  public Long getAssetStatusBeyondRepair() {
    return brecs == null ? 0 : brecs;
  }

  public Long getAssetStatusCondemned() {
    return cdecs == null ? 0 : cdecs;
  }

  public Long getAssetStatusStandby() {
    return stbecs == null ? 0 : stbecs;
  }

  public Long getAssetStatusDefrost() {
    return decs == null ? 0 : decs;
  }

  public Long getAssetStatusEventWorking() {
    return wkec == null ? 0 : wkec;
  }

  public Long getAssetStatusEventUnderRepair() {
    return urec == null ? 0 : urec;
  }

  public Long getAssetStatusEventBeyondRepair() {
    return brec == null ? 0 : brec;
  }

  public Long getAssetStatusEventCondemned() {
    return cdec == null ? 0 : cdec;
  }

  public Long getAssetStatusEventStandby() {
    return stbec == null ? 0 : stbec;
  }

  public Long getAssetStatusEventDefrost() {
    return dec == null ? 0 : dec;
  }

  public Long getAssetStatusWorkingDuration() {
    return wked == null ? 0 : wked;
  }

  public Long getAssetStatusUnderRepairDuration() {
    return ured == null ? 0 : ured;
  }

  public Long getAssetStatusBeyondRepairDuration() {
    return bred == null ? 0 : bred;
  }

  public Long getAssetStatusCondemnedDuration() {
    return cded == null ? 0 : cded;
  }

  public Long getAssetStatusStandbyDuration() {
    return stbed == null ? 0 : stbed;
  }

  public Long getAssetStatusDefrostDuration() {
    return ded == null ? 0 : ded;
  }

  public Long getPowerAvailabilityDuration() {
    return pad == null ? 0 : pad;
  }

  public Long getPowerOutageCount() {
    return pof == null ? 0 : pof;
  }

  public Long getRepairResponseTime() {
    return drrt == null ? 0 : drrt;
  }

  public Long getRepairToWorkingCount() {
    return urtwc == null ? 0 : urtwc;
  }

  public Long getLowAlarmCount() {
    return lac == null ? 0 : lac;
  }

  public Long getHighAlarmCount() {
    return hac == null ? 0 : hac;
  }

  public Long getLowExcursionCount() {
    return lec == null ? 0 : lec;
  }

  public Long getHighExcursionCount() {
    return hec == null ? 0 : hec;
  }

  public Long getLowWarningCount() {
    return lwc == null ? 0 : lwc;
  }

  public Long getHighWarningCount() {
    return hwc == null ? 0 : hwc;
  }

  public Long getLowAlarmDuration() {
    return lad == null ? 0 : lad;
  }

  public Long getHighAlarmDuration() {
    return had == null ? 0 : had;
  }

  public Long getLowExcursionDuration() {
    return led == null ? 0 : led;
  }

  public Long getHighWarningDuration() {
    return hwd == null ? 0 : hwd;
  }

  public Long getLowWarningDuration() {
    return lwd == null ? 0 : lwd;
  }

  public Long getHighExcursionDuration() {
    return hed == null ? 0 : hed;
  }

  public Long getAssetsWithMoreThan1LowExcursion() {
    return lec1 == null ? 0 : lec1;
  }

  public Long getAssetsWithMoreThan2LowExcursion() {
    return lec2 == null ? 0 : lec2;
  }

  public Long getAssetsWithMoreThan3LowExcursion() {
    return lec3 == null ? 0 : lec3;
  }

  public Long getAssetsWithMoreThan1HighExcursion() {
    return hec1 == null ? 0 : hec1;
  }

  public Long getAssetsWithMoreThan2HighExcursion() {
    return hec2 == null ? 0 : hec2;
  }

  public Long getAssetsWithMoreThan3HighExcursion() {
    return hec3 == null ? 0 : hec3;
  }

  public Long getAssetsWithMoreThan1HourLowExposure() {
    return lexd1 == null ? 0 : lexd1;
  }

  public Long getAssetsWithMoreThan2HourLowExposure() {
    return lexd2 == null ? 0 : lexd2;
  }

  public Long getAssetsWithMoreThan3HourLowExposure() {
    return lexd3 == null ? 0 : lexd3;
  }

  public Long getAssetsWithMoreThan1HourHighExposure() {
    return hexd1 == null ? 0 : hexd1;
  }

  public Long getAssetsWithMoreThan5HourHighExposure() {
    return hexd5 == null ? 0 : hexd5;
  }

  public Long getAssetsWithMoreThan10HourHighExposure() {
    return hexd10 == null ? 0 : hexd10;
  }

  public Long getTotalSpaceAcrossWorkingAssets() {
    return cpty == null ? 0 : cpty;
  }

  public Long getAssetsWithUpTimeGreaterThan70() {
    return up70 == null ? 0 : up70;
  }

  public Long getAssetsWithUpTimeGreaterThan80() {
    return up80 == null ? 0 : up80;
  }

  public Long getAssetsWithUpTimeGreaterThan90() {
    return up90 == null ? 0 : up90;
  }

  public Long getAssetsWithUpTime100() {
    return up100 == null ? 0 : up100;
  }

  /*public Long getAssetsWithDownTimeGreaterThan70() {
    return dwn70 == null ? 0 : dwn70;
  }

  public Long getAssetsWithDownTimeGreaterThan80() {
    return dwn80 == null ? 0 : dwn80;
  }

  public Long getAssetsWithDownTimeGreaterThan90() {
    return dwn90 == null ? 0 : dwn90;
  }

  public Long getAssetsWithDownTime100() {
    return dwn100 == null ? 0 : dwn100;
  }*/

  public Map<String,Float> getDiscardReasons() {
    return drsn;
  }

  public Long getAssetsWithMoreThan1Excursion() {
    return tec1 == null ? 0 : tec1;
  }

  public Long getAssetsWithMoreThan2Excursion() {
    return tec2 == null ? 0 :  tec2;
  }

  public Long getAssetsWithMoreThan3Excursion() {
    return tec3 == null ? 0 : tec3;
  }

  public Long getLowExcursionStateCount() {
    return lesc == null ? 0 : lesc;
  }

  public Long getHighExcursionStateCount() {
    return hesc == null ? 0 : hesc;
  }

  public Long getLowWarningStateCount() {
    return lwsc == null ? 0 : lwsc;
  }

  public Long getHighWarningStateCount() {
    return hwsc == null ? 0 : hwsc;
  }

  public Long getLowAlarmStateCount() {
    return lasc == null ? 0 : lasc;
  }

  public Long getHighAlarmStateCount() {
    return hasc == null ? 0 : hasc;
  }

  public Long getKioskAvailability100() {
    return sak100 == null ? 0 : sak100;
  }

  public Long getMatAvailability100() {
    return sam100 == null ? 0 : sam100;
  }

  public Long getkioskAvailabilityGreaterThan90() {
    return sak90 == null ? 0 : sak90;
  }

  public Long getMatAvailabilityGreaterThan90() {
    return sam90 == null ? 0 : sam90;
  }

  public Long getKioskAvailabilityGreaterThan80() {
    return sak80 == null ? 0 : sak80;
  }

  public Long getMatAvailabilityGreaterThan80() {
    return sam80 == null ? 0 : sam80;
  }

  public Long getKioskAvailabilityGreaterThan70() {
    return sak70 == null ? 0 : sak70;
  }

  public Long getMatAvailabilityGreaterThan70() {
    return sam70 == null ? 0 : sam70;
  }
}
