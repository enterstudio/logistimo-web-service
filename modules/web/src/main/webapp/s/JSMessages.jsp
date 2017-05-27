<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%--
  ~ Copyright © 2017 Logistimo.
  ~
  ~ This file is part of Logistimo.
  ~
  ~ Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
  ~ low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
  ~
  ~ This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
  ~ Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
  ~ later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  ~ warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
  ~ for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
  ~ <http://www.gnu.org/licenses/>.
  ~
  ~ You can be released from the requirements of the license by purchasing a commercial license. To know more about
  ~ the commercial license, please contact us at opensource@logistimo.com
  --%>

<!-- Language mappings for JavaScript messages (include this before including javascript files in your JSP) -->
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!-- Get the required messages from the bundle -->
<fmt:bundle basename="Messages">
    <fmt:message key="accuracy" var="accuracy" />
    <fmt:message key="action" var="action" />
    <fmt:message key="add" var="add" />
    <fmt:message key="administrators" var="admins" />
    <fmt:message key="all" var="all" />
    <fmt:message key="amount" var="amount" />
    <fmt:message key="at" var="at" />
    <fmt:message key="batch" var="batch" />
    <fmt:message key="cancel" var="cancel" />
    <fmt:message key="change" var="change" />
    <fmt:message key="charactersallowed" var="charactersallowed" />
    <fmt:message key="close" var="close" />
	<fmt:message key="continue" var="_continue" />
    <fmt:message key="createdon" var="createdon" />
    <fmt:message key="creditlimit" var="creditlimit" />
    <fmt:message key="currency" var="currency" />
    <fmt:message key="customer" var="customer" />
    <fmt:message key="customers" var="customers" />
    <fmt:message key="daily" var="daily" />
    <fmt:message key="days" var="days" />
    <fmt:message key="demand" var="demand" />
    <fmt:message key="description" var="description" />
    <fmt:message key="deviceid" var="deviceid"/>
    <fmt:message key="devices" var="devices"/>
    <fmt:message key="devicevendor" var="devicevendor"/>
    <fmt:message key="disable" var="disable" />
    <fmt:message key="distance" var="distance" />
    <fmt:message key="domain" var="domain" />
	<fmt:message key="domains" var="domains" />
    <fmt:message key="enable" var="enable" />
    <fmt:message key="expectedfulfillmenttime" var="expectedfulfillmenttime" />
    <fmt:message key="expiry" var="expiry" />
    <fmt:message key="exportspreadsheet" var="exportspreadsheet" />
    <fmt:message key="fieldsrequiredfor" var="fieldsrequiredfor" />
    <fmt:message key="kiosks" var="kiosks" />
    <fmt:message key="material.name" var="materialname" />
    <fmt:message key="material.msrp" var="materialmsrp" />
    <fmt:message key="material.retailerprice" var="materialretailerprice" />
    <fmt:message key="meters" var="meters" />
    <fmt:message key="user.mobile" var="mobilephone" />
    <fmt:message key="user.confirmpassword" var="confirmpassword" />
    <fmt:message key="country" var="country" />
    <fmt:message key="district" var="district" />
    <fmt:message key="fetching" var="fetching" />
    <fmt:message key="for" var="_for" />
    <fmt:message key="geocodes" var="geocodes" />
    <fmt:message key="hide" var="hide" />
    <fmt:message key="in" var="_in" />
    <fmt:message key="incidents" var="incidents" />
    <fmt:message key="inventory" var="inventory" />
    <fmt:message key="inventory.reorderlevel" var="inventory_reorderlevel" />
    <fmt:message key="inventory.safetystock" var="inventory_safetystock" />
    <fmt:message key="isrequired" var="isrequired" />
    <fmt:message key="isinvalid" var="isinvalid" />
    <fmt:message key="items" var="items" />
    <fmt:message key="kiosk" var="kiosk" />
    <fmt:message key="kiosk.owner" var="kiosk_owner" />
    <fmt:message key="kiosk.name" var="kiosk_name" />
    <fmt:message key="language" var="language"/>
    <fmt:message key="lastlogin" var="lastlogin" />
    <fmt:message key="latitude" var="latitude" />
	<fmt:message key="linkeddomains.addchilddomains" var="linkeddomains_addchilddomains" />
	<fmt:message key="linkeddomains.alert.notaddself" var="linkeddomains_alert_notaddself" />
	<fmt:message key="linkeddomains.removechilddomains" var="linkeddomains_removechilddomains" />
	<fmt:message key="linkeddomains.removeconfirm" var="linkeddomains_removeconfirm" />
    <fmt:message key="login.password" var="password" />
    <fmt:message key="logins" var="logins" />
    <fmt:message key="longitude" var="longitude" />
    <fmt:message key="manufacturer" var="manufacturer" />
    <fmt:message key="manufactured" var="manufactured" />
    <fmt:message key="material" var="material" />
    <fmt:message key="materials" var="materials" />
    <fmt:message key="message" var="message" />
    <fmt:message key="more" var="more" />
    <fmt:message key="nobatchesavailable" var="nobatchesavailable" />
    <fmt:message key="nodataavailable" var="nodataavailable" />
    <fmt:message key="notemperaturedevicesavailable" var="notemperaturedevicesavailable"/>
    <fmt:message key="numberofitems" var="numberofitems" />
    <fmt:message key="on" var="on" />
    <fmt:message key="onlyoneitemcanbeselected" var="onlyoneitemcanbeselected" />
    <fmt:message key="order" var="order" />
    <fmt:message key="order.id" var="order_id" />
    <fmt:message key="orders" var="orders" />
    <fmt:message key="payable" var="payable" />
    <fmt:message key="payment" var="payment" />
    <fmt:message key="poolgroup" var="poolgroup" />
    <fmt:message key="places" var="places" />
    <fmt:message key="placesvisited" var="placesvisited" />
	<fmt:message key="filters.message" var="pleaseselectoneormore" />
    <fmt:message key="preferredtimezone" var="preferredtimezone" />
    <fmt:message key="price" var="price" />
    <fmt:message key="quantity.allocated" var="quantity_allocated" />
    <fmt:message key="quantity.islesserthanrequired" var="quantity_islesserthanrequired" />
    <fmt:message key="quantity.tobeallocated" var="quantity_to_be_allocated" />
    <fmt:message key="reason" var="reason" />
    <fmt:message key="recent" var="recent" />
	<fmt:message key="refresh" var="refresh" />
    <fmt:message key="remove" var="remove" />
    <fmt:message key="revenue" var="revenue" />
    <fmt:message key="routing.actualroutegeoinfomsg" var="routing_actualroutegeoinfomsg" />
    <fmt:message key="routing.hideactualroute" var="routing_hideactualroute" />
    <fmt:message key="routing.manualroutegeoinfomsg" var="routing_manualroutegeoinfomsg" />
    <fmt:message key="routing.showactualroute" var="routing_showactualroute" />
    <fmt:message key="save" var="save" />
    <fmt:message key="select" var="select" />
    <fmt:message key="selectitemmsg" var="selectitemmsg" />
    <fmt:message key="show" var="show" />
    <fmt:message key="state" var="state" />
    <fmt:message key="status" var="status" />
    <fmt:message key="stock" var="stock" />
    <fmt:message key="streetaddress" var="address" />
	<fmt:message key="systemerror" var="systemerror" />
    <fmt:message key="tagentity" var="tagentity" />
    <fmt:message key="tagmaterial" var="tagmaterial" />
    <fmt:message key="taluk" var="taluk" />
    <fmt:message key="temperature.device.config.comm.chnl" var="temperature_device_config_commchnl"/>
    <fmt:message key="temperature.device.config.comm.tmpUrl" var="temperature_device_config_tmpurl"/>
    <fmt:message key="temperature.device.config.comm.cfgUrl" var="temperature_device_config_cfgurl"/>
    <fmt:message key="temperature.device.config.comm.cfgUrl.prefix" var="temperature_device_config_cfgurl_prefix"/>
    <fmt:message key="temperature.device.config.comm.almUrl" var="temperature_device_config_almurl"/>
    <fmt:message key="temperature.device.config.comm.statsUrl" var="temperature_device_config_statsurl"/>
    <fmt:message key="temperature.device.config.comm.devRyUrl" var="temperature_device_config_devryurl"/>
    <fmt:message key="temperature.device.config.comm.smsGyPh" var="temperature_device_config_smsGyPh"/>
    <fmt:message key="temperature.device.config.comm.samplingInt" var="temperature_device_config_sint"/>
    <fmt:message key="temperature.device.config.comm.pushInt" var="temperature_device_config_pint"/>
    <fmt:message key="temperature.device.config.comm.usrPhones" var="temperature_device_config_usrphones"/>
    <fmt:message key="temperature.device.config.filter.excursion" var="temperature_device_config_filter_excursion"/>
    <fmt:message key="temperature.device.config.filter.alarms" var="temperature_device_config_filter_alarms"/>
    <fmt:message key="temperature.device.config.filter.nodata" var="temperature_device_config_filter_nodata"/>
    <fmt:message key="temperature" var="temperature"/>
    <fmt:message key="temperature.event.time" var="temperatureeventtime"/>
    <fmt:message key="temperature.event.creation.time" var="temperatureeventcreationtime"/>
    <fmt:message key="temperature.min" var="temperaturemin"/>
    <fmt:message key="temperature.max" var="temperaturemax"/>
    <fmt:message key="duration" var="duration"/>
    <fmt:message key="time" var="time" />
    <fmt:message key="timezone" var="timezone"/>
    <fmt:message key="transaction" var="transaction" />
    <fmt:message key="transaction.distance.msg" var="transaction_distancemsg" />
    <fmt:message key="transactions" var="transactions" />
    <fmt:message key="transporter" var="transporter" />
    <fmt:message key="transtype" var="transtype" />
    <fmt:message key="type" var="type" />
    <fmt:message key="typetogetsuggestions" var="typetogetsuggestions" />
    <fmt:message key="updatedon" var="updatedon" />
    <fmt:message key="user" var="user" />
    <fmt:message key="users" var="users" />
    <fmt:message key="user.age" var="userage" />
    <fmt:message key="user.email" var="useremail" />
    <fmt:message key="user.firstname" var="userfirstname" />
    <fmt:message key="user.gender" var="usergender" />
    <fmt:message key="user.id" var="userid" />
    <fmt:message key="user.ipaddress" var="useripaddress" />
    <fmt:message key="user.lastname" var="userlastname" />
    <fmt:message key="user.landline" var="userlandline" />
    <fmt:message key="user.mobile" var="usermobile" />
    <fmt:message key="user.mobilebrand" var="usermobilebrand" />
    <fmt:message key="user.mobilemodel" var="usermobilemodel" />
    <fmt:message key="user.mobileoperator" var="usermobileoperator" />
    <fmt:message key="valuesallowed" var="valuesallowed" />
    <fmt:message key="user.role" var="userrole" />
    <fmt:message key="variable.customercity" var="variable_customercity" />
    <fmt:message key="variable.materials" var="variable_materials" />
    <fmt:message key="variable.materialswithmetadata" var="variable_materialswithmetadata" />
    <fmt:message key="variable.statuschangetime" var="variable_statuschangetime" />
    <fmt:message key="variable.vendorcity" var="variable_vendorcity" />
    <fmt:message key="village" var="village" />
    <fmt:message key="vendor" var="vendor" />
    <fmt:message key="vendors" var="vendors" />
    <fmt:message key="was" var="was" />
    <fmt:message key="zipcode" var="zipcode" />
</fmt:bundle>
<fmt:bundle basename="JSMessages">
    <fmt:message key="abnormalstockevent" var="abnormalstockevent" />
    <fmt:message key="abnormalstockduration" var="abnormalstockduration" />
    <fmt:message key="addremoveallentities" var="addremoveallentities" />
	<fmt:message key="addmatallentities" var="addmatallentities" />
    <fmt:message key="allitemszeromsg" var="allitemszeromsg" />
    <fmt:message key="annotation" var="annotation" />
    <fmt:message key="areyousure" var="areyousure" />
    <fmt:message key="atleastoneentry" var="atleastoneentry" />
    <fmt:message key="averageoverentireperiod" var="averageoverentireperiod" />
    <fmt:message key="averageresponsetime" var="averageresponsetime" />
    <fmt:message key="batch.expirynotinfuture" var="batch_expirynotinfuture" />
    <fmt:message key="batch.manufactureddatenotnewer" var="batch_manufactureddatenotnewer" />
    <fmt:message key="batch.notallocatedfully" var="batch_notallocatedfully" />
    <fmt:message key="batch.orderallocatedpartially" var="batch_orderallocatedpartially" />
    <fmt:message key="batch.ordernotallocatedfully1" var="batch_ordernotallocatedfully1" />
    <fmt:message key="batch.ordernotallocatedfully2" var="batch_ordernotallocatedfully2" />
    <fmt:message key="batch.selectexpirydate" var="batch_selectexpirydate" />
    <fmt:message key="board.refreshdurationmsg" var="board_refreshdurationmsg"/>
    <fmt:message key="board.scrollintervalmsg" var="board_scrollintervalmsg"/>
    <fmt:message key="bulkupload.selectcsvfile" var="bulkupload_selectcsvfile" />
    <fmt:message key="bulkupload.containcsvdata" var="bulkupload_containcsvdata" />
    <fmt:message key="bulkupload.selectentity" var="bulkupload_selectentity" />
    <fmt:message key="cannotexceedstock" var="cannotexceedstock" />
    <fmt:message key="confirmpasswordmsg" var="confirmpasswordmsg" />
    <fmt:message key="confirmundotransactionmsg" var="confirmundotransactionmsg" />
    <fmt:message key="count" var="count" />
    <fmt:message key="customreports.confirmremovetemplatemsg" var="customreports_confirmremovetemplatemsg" />
    <fmt:message key="customreports.enteradminlistmsg" var="customreports_enteradminlistmsg" />
    <fmt:message key="customreports.enteradminorsuperuserlistmsg" var="customreports_enteradminorsuperuserlistmsg" />
    <fmt:message key="customreports.enterdatadurationmsg" var="customreports_enterdatadurationmsg" />
    <fmt:message key="customreports.entermanageroradminlistmsg" var="customreports_entermanageroradminlistmsg" />
    <fmt:message key="customreports.entermanageroradminorsuperuserlistmsg" var="customreports_entermanageroradminorsuperuserlistmsg" />
    <fmt:message key="customreports.enterreportgenerationschedulemsg" var="customreports_enterreportgenerationschedulemsg" />
    <fmt:message key="customreports.entersheetnamemsg" var="customreports_entersheetnamemsg" />
    <fmt:message key="customreports.entertemplatenamemsg" var="customreports_entertemplatenamemsg" />
    <fmt:message key="customreports.entertimefordailyreportgenerationmsg" var="customreports_entertimefordailyreportgenerationmsg" />
    <fmt:message key="customreports.entertimeformonthlyreportgenerationmsg" var="customreports_entertimeformonthlyreportgenerationmsg" />
    <fmt:message key="customreports.entertimeforweeklyreportgenerationmsg" var="customreports_entertimeforweeklyreportgenerationmsg" />
    <fmt:message key="customreports.invaliddatadurationforhistoricalinventorysnapshotmsg" var="customreports_invaliddatadurationforhistoricalinventorysnapshotmsg" />
    <fmt:message key="customreports.invalidfilenamemsg" var="customreports_invalidfilenamemsg" />
    <fmt:message key="customreports.invalidsheetnamemsg" var="customreports_invalidsheetnamemsg" />
    <fmt:message key="customreports.invalidtemplatenamemsg" var="customreports_invalidtemplatenamemsg" />
    <fmt:message key="customreports.maxdatadurationmsg" var="customreports_maxdatadurationmsg" />
    <fmt:message key="customreports.maxmonthlydatadurationmsg" var="customreports_maxmonthlydatadurationmsg" />
    <fmt:message key="customreports.selectdayforweeklyreportgenerationmsg" var="customreports_selectdayforweeklyreportgenerationmsg" />
    <fmt:message key="customreports.selecttemplatetouploadmsg" var="customreports_selecttemplatetouploadmsg" />
    <fmt:message key="customreports.sheetnameformatmsg" var="customreports_sheetnameformatmsg" />
    <fmt:message key="customreports.templatealreadyexisitsmsg" var="customreports_templatealreadyexistsmsg" />
    <fmt:message key="customreports.templatenameformatmsg" var="customreports_templatenameformatmsg" />
    <fmt:message key="dailytrendsinmonth" var="dailytrendsinmonth" />
    <fmt:message key="date" var="date" />
    <fmt:message key="demandfrom" var="demandfrom" />
    <fmt:message key="editquantities" var="editquantities" />
    <fmt:message key="enteralphanumeric" var="enteralphanumeric" />
    <fmt:message key="entermandatoryfields" var="entermandatoryfields" />
    <fmt:message key="enternumbergreaterthanzeromsg" var="enternumbergreaterthanzeromsg" />
    <fmt:message key="entervalidname" var="entervalidname" />
    <fmt:message key="entervalidnumbermsg" var="entervalidnumbermsg" />
    <fmt:message key="export_confirmall1" var="export_confirmall1" />
    <fmt:message key="export_confirmall2" var="export_confirmall2" />
    <fmt:message key="export_confirmall3" var="export_confirmall3" />
    <fmt:message key="exportedorders" var="exportedorders" />
    <fmt:message key="fetchingroutemap" var="fetchingroutemap" />
    <fmt:message key="fieldisresetmsg" var="fieldisresetmsg" />
    <fmt:message key="first" var="first" />
    <fmt:message key="followformatmsg" var="followformatmsg" />
    <fmt:message key="geocodes.confirmmessage" var="geocodes_confirmmessage" />
    <fmt:message key="geocodes.select" var="geocodes_select" />
    <fmt:message key="geocodes.selectonemarker" var="geocodes_selectonemarker" />
    <fmt:message key="geocodes.enter" var="geocodes_enter" />
    <fmt:message key="geocodes.failuremessage" var="geocodes_failuremessage" />
    <fmt:message key="geocodes.specifylocationmsg" var="geocodes_specifylocationmsg" />
    <fmt:message key="isrequired" var="isrequired" />
    <fmt:message key="isinvalid" var="isinvalid" />
    <fmt:message key="itemdetails" var="itemdetails" />
    <fmt:message key="lettersnumbershyphenallowed" var="lettersnumbershyphenallowed" />
    <fmt:message key="materialkioskcannotbeselectedmsg" var="materialkioskcannotbeselectedmsg" />
    <fmt:message key="maxstock" var="maxstock" />
    <fmt:message key="minstock" var="minstock" />
    <fmt:message key="mobiledownloadmessage" var="mobiledownloadmessage" />
    <fmt:message key="morefilters" var="morefilters" />
    <fmt:message key="next" var="next" />
    <fmt:message key="nopayment" var="nopayment" />
    <fmt:message key="notifications.nomessagetemplate" var="notifications_nomessagetemplate" />
    <fmt:message key="notifications.usersnotselected" var="notifications_usersnotselected" />
    <fmt:message key="notselectedusers" var="notselectedusers" />
    <fmt:message key="numbersnotallowed" var="numbersnotallowed" />
    <fmt:message key="of" var="of" />
    <fmt:message key="onlynumbersallowed" var="onlynumbersallowed" />
    <fmt:message key="ordercannotbe" var="ordercannotbe" />
    <fmt:message key="ordercostexceedscredit" var="ordercostexceedscredit" />
    <fmt:message key="orderdetails" var="orderdetails" />
    <fmt:message key="orderingerrormsg" var="orderingerrormsg" />
    <fmt:message key="orderingloadmoremsg" var="orderingloadmoremsg" />
    <fmt:message key="orderingresetmsg" var="orderingresetmsg" />
    <fmt:message key="orderingsuccessfullysavedmsg" var="orderingsuccessfullysavedmsg" />
    <fmt:message key="ownedby" var="ownedby" />
    <fmt:message key="phoneformatmsg" var="phoneformatmsg" />
    <fmt:message key="passwordvalidationmsg" var="passwordvalidationmsg" />
    <fmt:message key="paymenthigher" var="paymenthigher" />
    <fmt:message key="pleaseselect" var="pleaseselect" />
    <fmt:message key="prev" var="prev" />
    <fmt:message key="quantity" var="quantity" />
    <fmt:message key="removekioskconfirmmsg" var="removekioskconfirmmsg" />
    <fmt:message key="removematerialconfirmmsg" var="removematerialconfirmmsg" />
    <fmt:message key="removepoolgroupconfirmmsg" var="removepoolgroupconfirmmsg" />
    <fmt:message key="removerelationconfirmmsg" var="removerelationconfirmmsg" />
    <fmt:message key="removeuserconfirmmsg" var="removeuserconfirmmsg" />
    <fmt:message key="reportsdatemsg" var="reportsdatemsg" />
    <fmt:message key="reportsfiltermsg" var="reportsfiltermsg" />
    <fmt:message key="reportsmaxfiltersmsg" var="reportsmaxfiltersmsg" />
    <fmt:message key="reportsmaxitemsmsg" var="reportsmaxitemsmsg" />
    <fmt:message key="reportsremovefiltermsg" var="reportsremovefiltermsg" />
    <fmt:message key="reportsselectmsg" var="reportsselectmsg" />
    <fmt:message key="routingremovefromroutemsg" var="routingremovefromroutemsg" />
    <fmt:message key="routingsuccessfullysavedmsg" var="routingsuccessfullysavedmsg" />
    <fmt:message key="selectkioskmsg" var="selectkioskmsg" />
    <fmt:message key="selectitemmsg" var="selectitemmsg" />
    <fmt:message key="selectitemtoaddmsg" var="selectitemtoaddmsg" />
    <fmt:message key="selectitemtoeditmsg" var="selectitemtoeditmsg" />
    <fmt:message key="selectitemtoexportmsg" var="selectitemtoexportmsg" />
    <fmt:message key="selectitemtoremovemsg" var="selectitemtoremovemsg" />
    <fmt:message key="selectoperationmsg" var="selectoperationmsg" />
    <fmt:message key="selectotypemsg" var="selectotypemsg" />
    <fmt:message key="selectmaterial" var="selectmaterial" />
    <fmt:message key="selectrelationtypemsg" var="selectrelationtypemsg" />
    <fmt:message key="selectreporttypemsg" var="selectreporttypemsg" />
    <fmt:message key="selectstatusmsg" var="selectstatusmsg" />
    <fmt:message key="selecttransactionmsg" var="selecttransactionmsg" />
    <fmt:message key="selectusermsg" var="selectusermsg" />
    <fmt:message key="selectusertoremovemsg" var="selectusertoremovemsg" />
    <fmt:message key="selectvendormsg" var="selectvendormsg" />
    <fmt:message key="specifyvaliduseridmsg" var="specifyvaliduseridmsg" />
    <fmt:message key="stockboard.horizontalscrollintervalmsg" var="stockboard_horizontalscrollintervalmsg" />
    <fmt:message key="stockboard.maxitemsonboardmsg" var="stockboard_maxitemsonboardmsg" />
    <fmt:message key="temperature.entermaxtempofdevicemsg" var="temperature_entermaxtempofdevicemsg" />
    <fmt:message key="temperature.entermintempofdevicemsg" var="temperature_entermintempofdevicemsg" />
    <fmt:message key="temperature.enterserialnumbermsg" var="temperature_enterserialnumbermsg" />
    <fmt:message key="temperature.errorwhilesavingtemperaturedevicesmsg" var="temperature_errorwhilesavingtemperaturedevicesmsg" />
    <fmt:message key="temperature.invalidmaxtemperaturemsg" var="temperature_invalidmaxtemperaturemsg" />
    <fmt:message key="temperature.invalidmintemperaturemsg" var="temperature_invalidmintemperaturemsg" />
    <fmt:message key="temperature.notaddedorremoveddevicemsg" var="temperature_notaddedorremoveddevicemsg" />
    <fmt:message key="temperature.notselecteditemstoremove" var="temperature_notselecteditemstoremove" />
    <fmt:message key="temperature.removedbutnotsavedmsg" var="temperature_removedbutnotsavedmsg" />
    <fmt:message key="temperature.removedevicesconfirmmsg" var="temperature_removedevicesconfirmmsg" />
    <fmt:message key="temperature.removeserialnumbermsg" var="temperature_removeserialnumbermsg" />
    <fmt:message key="temperature.serialnumberalreadyexistsmsg" var="temperature_serialnumberalreadyexistsmsg" />
    <fmt:message key="temperature.temperaturedevicesconfirmclosemsg" var="temperature_temperaturedevicesconfirmclosemsg"/>
    <fmt:message key="temperaturedevicesfor" var="temperaturedevicesfor"/>
    <fmt:message key="total" var="total" />
    <fmt:message key="transportermandatory" var="transportermandatory" />
    <fmt:message key="useragevalidationmsg" var="useragevalidationmsg" />
    <fmt:message key="useridvalidationmsg" var="useridvalidationmsg" />
    <fmt:message key="zerostocksettingmsg1" var="zerostocksettingmsg1" />
    <fmt:message key="zerostocksettingmsg2" var="zerostocksettingmsg2" />
</fmt:bundle>
<script language="javascript" type="text/javascript">
    // Get the JSON object
    JSMessages = {
        abnormalstockevent: "${abnormalstockevent}",
        abnormalstockduration: "${abnormalstockduration}",
        accuracy: "${accuracy}",
        add: "${add}",
        addremoveallentities: "${addremoveallentities}",
	 			addmatallentities: "${addmatallentities}",
        address: "${address}",
        admins: "${admins}",
        all: "${all}",
        allitemszeromsg: "${allitemszeromsg}",
        amount: "${amount}",
        annotation: "${annotation}",
        areyousure: "${areyousure}",
        at: "${at}",
        atleastoneentry: "${atleastoneentry}",
        averageoverentireperiod: "${averageoverentireperiod}",
        averageresponsetime: "${averageresponsetime}",
        batch: "${batch}",
        batch_expirynotinfuture: "${batch_expirynotinfuture}",
        batch_manufactureddatenotnewer: "${batch_manufactureddatenotnewer}",
        batch_orderallocatedpartially: "${batch_orderallocatedpartially}",
        batch_notallocatedfully: "${batch_notallocatedfully}",
        batch_ordernotallocatedfully1: "${batch_ordernotallocatedfully1}",
        batch_ordernotallocatedfully2: "${batch_ordernotallocatedfully2}",
        batch_selectexpirydate: "${batch_selectexpirydate}",
        board_refreshdurationmsg: "${board_refreshdurationmsg}",
        board_scrollintervalmsg: "${board_scrollintervalmsg}",
        bulkupload_selectcsvfile: "${bulkupload_selectcsvfile}",
        bulkupload_containcsvdata: "${bulkupload_containcsvdata}",
        bulkupload_selectentity: "${bulkupload_selectentity}",
        cancel: "${cancel}",
        cannotexceedstock: "${cannotexceedstock}",
        change: "${change}",
        charactersallowed: "${charactersallowed}",
        close: "${close}",
        confirmpassword: "${confirmpassword}",
        confirmpasswordmsg: "${confirmpasswordmsg}",
        confirmundotransactionmsg: "${confirmundotransactionmsg}",
        _continue: "${_continue}",
        count: "${count}",
        country: "${country}",
        createdon: "${createdon}",
        creditlimit: "${creditlimit}",
        currency: "${currency}",
        customer: "${customer}",
        customers: "${customers}",
        customreports_confirmremovetemplatemsg: "${customreports_confirmremovetemplatemsg}",
        customreports_enteradminlistmsg: "${customreports_enteradminlistmsg}",
        customreports_enteradminorsuperuserlistmsg: "${customreports_enteradminorsuperuserlistmsg}",
        customreports_enterdatadurationmsg: "${customreports_enterdatadurationmsg}",
        customreports_entermanageroradminlistmsg: "${customreports_entermanageroradminlistmsg}",
        customreports_entermanageroradminorsuperuserlistmsg: "${customreports_entermanageroradminorsuperuserlistmsg}",
        customreports_entersheetnamemsg: "${customreports_entersheetnamemsg}",
        customreports_enterreportgenerationschedulemsg: "${customreports_enterreportgenerationschedulemsg}",
        customreports_entertemplatenamemsg: "${customreports_entertemplatenamemsg}",
        customreports_entertimefordailyreportgenerationmsg: "${customreports_entertimefordailyreportgenerationmsg}",
        customreports_entertimeformonthlyreportgenerationmsg: "${customreports_entertimeformonthlyreportgenerationmsg}",
        customreports_entertimeforweeklyreportgenerationmsg: "${customreports_entertimeforweeklyreportgenerationmsg}",
        customreports_invaliddatadurationforhistoricalinventorysnapshotmsg: "${customreports_invaliddatadurationforhistoricalinventorysnapshotmsg}",
        customreports_invalidfilenamemsg: "${customreports_invalidfilenamemsg}",
        customreports_invalidsheetnamemsg: "${customreports_invalidsheetnamemsg}",
        customreports_invalidtemplatenamemsg: "${customreports_invalidtemplatenamemsg}",
        customreports_maxdatadurationmsg: "${customreports_maxdatadurationmsg}",
        customreports_maxmonthlydatadurationmsg: "${customreports_maxmonthlydatadurationmsg}",
        customreports_selectdayforweeklyreportgenerationmsg: "${customreports_selectdayforweeklyreportgenerationmsg}",
        customreports_selecttemplatetouploadmsg: "${customreports_selecttemplatetouploadmsg}",
        customreports_sheetnameformatmsg: "${customreports_sheetnameformatmsg}",
        customreports_templatealreadyexistsmsg: "${customreports_templatealreadyexistsmsg}",
        customreports_templatenameformatmsg: "${customreports_templatenameformatmsg}",
        daily: "${daily}",
        dailytrendsinmonth: "${dailytrendsinmonth}",
        date: "${date}",
        days: "${days}",
        demand: "${demand}",
        demandfrom: "${demandfrom}",
        description: "${description}",
        deviceid: "${deviceid}",
        devicevendor: "${devicevendor}",
        disable: "${disable}",
        distance: "${distance}",
        district: "${district}",
        domain: "${domain}",
        domains: "${domains}",
        duration: "${duration}",
        editquantities: "${editquantities}",
        enable: "${enable}",
        enteralphanumeric: "${enteralphanumeric}",
        entermandatoryfields: "${entermandatoryfields}",
        enternumbergreaterthanzeromsg: "${enternumbergreaterthanzeromsg}",
        entervalidname: "${entervalidname}",
        entervalidnumbermsg: "${entervalidnumbermsg}",
        expectedfulfillmenttime: "${expectedfulfillmenttime}",
        expiry: "${expiry}",
        export_confirmall1: "${export_confirmall1}",
        export_confirmall2: "${export_confirmall2}",
        export_confirmall3: "${export_confirmall3}",
        exportedorders: "${exportedorders}",
        exportspreadsheet: "${exportspreadsheet}",
        fetching: "${fetching}",
        fetchingroutemap: "${fetchingroutemap}",
        fieldisresetmsg: "${fieldisresetmsg}",
        fieldsrequiredfor: "${fieldsrequiredfor}",
        first: "${first}",
        _for: "${_for}",
        followformatmsg: "${followformatmsg}",
        geocodes: "${geocodes}",
        geocodes_confirmmessage: "${geocodes_confirmmessage}",
        geocodes_enter: "${geocodes_enter}",
        geocodes_failuremessage: "${geocodes_failuremessage}",
        geocodes_select: "${geocodes_select}",
        geocodes_selectonemarker: "${geocodes_selectonemarker}",
        geocodes_specifylocationmsg: "${geocodes_specifylocationmsg}",
        hide: "${hide}",
        isinvalid: "${isinvalid}",
        isrequired: "${isrequired}",
        _in: "${_in}",
        incidents: "${incidents}",
        inventory: "${inventory}",
        inventory_reorderlevel: "${inventory_reorderlevel}",
        inventory_safetystock: "${inventory_safetystock}",
        itemdetails: "${itemdetails}",
        items: "${items}",
        kiosk: "${kiosk}",
        kiosks: "${kiosks}",
        kiosk_owner: "${kiosk_owner}",
        kiosk_name: "${kiosk_name}",
        language: "${language}",
        lettersnumbershyphenallowed: "${lettersnumbershyphenallowed}",
        lastlogin: "${lastlogin}",
        latitude: "${latitude}",
        linkeddomains_addchilddomains: "${linkeddomains_addchilddomains}",
        linkeddomains_alert_notaddself: "${linkeddomains_alert_notaddself}",
        linkeddomains_removechilddomains: "${linkeddomains_removechilddomains}",
        linkeddomains_removeconfirm: "${linkeddomains_removeconfirm}",
        logins: "${logins}",
        longitude: "${longitude}",
        manufacturer: "${manufacturer}",
        manufactured: "${manufactured}",
        material: "${material}",
        materials: "${materials}",
        materialkioskcannotbeselectedmsg: "${materialkioskcannotbeselectedmsg}",
        materialname: "${materialname}",
        materialmsrp: "${materialmsrp}",
        materialretailerprice: "${materialretailerprice}",
        maxstock: "${maxstock}",
        meters: "${meters}",
        message: "${message}",
        minstock: "${minstock}",
        mobiledownloadmessage: "${mobiledownloadmessage}",
        mobilephone: "${mobilephone}",
        more: "${more}",
        morefilters: "${morefilters}",
        next: "${next}",
        nobatchesavailable: "${nobatchesavailable}",
        nodataavailable: "${nodataavailable}",
        nopayment: "${nopayment}",
        notemperaturedevicesavailable: "${notemperaturedevicesavailable}",
        notifications_nomessagetemplate: "${notifications_nomessagetemplate}",
        notifications_usersnotselected: "${notifications_usersnotselected}",
        notselectedusers: "${notselectedusers}",
        numberofitems: "${numberofitems}",
        numbersnotallowed: "${numbersnotallowed}",
        of: "${of}",
        on: "${on}",
        onlynumbersallowed: "${onlynumbersallowed}",
        onlyoneitemcanbeselected: "${onlyoneitemcanbeselected}",
        order: "${order}",
        order_id: "${order_id}",
        orders: "${orders}",
        ordercannotbe: "${ordercannotbe}",
        ordercostexceedscredit: "${ordercostexceedscredit}",
        orderdetails: "${orderdetails}",
        orderingerrormsg: "${orderingerrormsg}",
        orderingloadmoremsg: "${orderingloadmoremsg}",
        orderingresetmsg: "${orderingresetmsg}",
        orderingsuccessfullysavedmsg: "${orderingsuccessfullysavedmsg}",
        ownedby: "${ownedby}",
        password: "${password}",
        passwordvalidationmsg: "${passwordvalidationmsg}",
        payable: "${payable}",
        payment: "${payment}",
        paymenthigher: "${paymenthigher}",
        phoneformatmsg: "${phoneformatmsg}",
        poolgroup: "${poolgroup}",
        preferredtimezone: "${preferredtimezone}",
        places: "${places}",
        placesvisited: "${placesvisited}",
        pleaseselect: "${pleaseselect}",
        pleaseselectoneormore: "${pleaseselectoneormore}",
        prev: "${prev}",
        price: "${price}",
        quantity: "${quantity}",
        quantity_allocated: "${quantity_allocated}",
        quantity_islesserthanrequired: "${quantity_islesserthanrequired}",
        quantity_to_be_allocated: "${quantity_to_be_allocated}",
        reason: "${reason}",
        recent: "${recent}",
        refresh: "${refresh}",
        remove: "${remove}",
        removekioskconfirmmsg: "${removekioskconfirmmsg}",
        removematerialconfirmmsg: "${removematerialconfirmmsg}",
        removepoolgroupconfirmmsg: "${removepoolgroupconfirmmsg}",
        removerelationconfirmmsg: "${removerelationconfirmmsg}",
        removeuserconfirmmsg: "${removeuserconfirmmsg}",
        reportsdatemsg: "${reportsdatemsg}",
        reportsfiltermsg: "${reportsfiltermsg}",
        reportsmaxfiltersmsg: "${reportsmaxfiltersmsg}",
        reportsmaxitemsmsg: "${reportsmaxitemsmsg}",
        reportsremovefiltermsg: "${reportsremovefiltermsg}",
        reportsselectmsg: "${reportsselectmsg}",
        revenue: "${revenue}",
        routingremovefromroutemsg: "${routingremovefromroutemsg}",
        routingsuccessfullysavedmsg: "${routingsuccessfullysavedmsg}",
        routing_actualroutegeoinfomsg: "${routing_actualroutegeoinfomsg}",
        routing_hideactualroute: "${routing_hideactualroute}",
        routing_manualroutegeoinfomsg: "${routing_manualroutegeoinfomsg}",
        routing_showactualroute: "${routing_showactualroute}",
        save: "${save}",
        selectkioskmsg: "${selectkioskmsg}",
        selectitemmsg: "${selectitemmsg}",
        selectitemtoaddmsg: "${selectitemtoaddmsg}",
        selectitemtoeditmsg: "${selectitemtoeditmsg}",
        selectitemtoexportmsg: "${selectitemtoexportmsg}",
        selectitemtoremovemsg: "${selectitemtoremovemsg}",
        selectoperationmsg: "${selectoperationmsg}",
        selectotypemsg: "${selectotypemsg}",
        selectmaterial: "${selectmaterial}",
        selectrelationtypemsg: "${selectrelationtypemsg}",
        selectreporttypemsg: "${selectreporttypemsg}",
        selectstatusmsg: "${selectstatusmsg}",
        selecttransactionmsg: "${selecttransactionmsg}",
        selectusermsg: "${selectusermsg}",
        selectusertoremovemsg: "${selectusertoremovemsg}",
        selectvendormsg: "${selectvendormsg}",
        select_state: "-- ${select} ${state} --",
        select_district: "-- ${select} ${district} --",
        select_taluk: "-- ${select} ${taluk} --",
        select_village: "-- ${select} ${village} --",
        show: "${show}",
        specifyvaliduseridmsg: "${specifyvaliduseridmsg}",
        state: "${state}",
        status: "${status}",
        stock: "${stock}",
        stockboard_horizontalscrollintervalmsg: "${stockboard_horizontalscrollintervalmsg}",
        stockboard_maxitemsonboardmsg: "${stockboard_maxitemsonboardmsg}",
        systemerror: "${systemerror}",
        tagentity: "${tagentity}",
        tagmaterial: "${tagmaterial}",
        taluk: "${taluk}",
        temperature: "${temperature}",
        temperature_entermaxtempofdevicemsg: "${temperature_entermaxtempofdevicemsg}",
        temperature_entermintempofdevicemsg: "${temperature_entermintempofdevicemsg}",
        temperature_enterserialnumbermsg: "${temperature_enterserialnumbermsg}",
        temperature_errorwhilesavingtemperaturedevicesmsg: "${temperature_errorwhilesavingtemperaturedevicesmsg}",
        temperature_invalidmaxtemperaturemsg: "${temperature_invalidmaxtemperaturemsg}",
        temperature_invalidmintemperaturemsg: "${temperature_invalidmintemperaturemsg}",
        temperature_notaddedorremoveddevicemsg: "${temperature_notaddedorremoveddevicemsg}",
        temperature_notselecteditemstoremove: "${temperature_notselecteditemstoremove}",
        temperature_removedbutnotsavedmsg: "${temperature_removedbutnotsavedmsg}",
        temperature_removedevicesconfirmmsg: "${temperature_removedevicesconfirmmsg}",
        temperature_removeserialnumbermsg: "${temperature_removeserialnumbermsg}",
        temperature_serialnumberalreadyexistsmsg: "${temperature_serialnumberalreadyexistsmsg}",
        temperature_temperaturedevicesconfirmclosemsg: "${temperature_temperaturedevicesconfirmclosemsg}",
        temperaturedevicesfor: "${temperaturedevicesfor}",
        temperature_device_config_commchnl: "${temperature_device_config_commchnl}",
        temperature_device_config_tmpurl: "${temperature_device_config_tmpurl}",
        temperature_device_config_cfgurl: "${temperature_device_config_cfgurl}",
        temperature_device_config_cfgurl_prefix: "${temperature_device_config_cfgurl_prefix}",
        temperature_device_config_almurl: "${temperature_device_config_almurl}",
        temperature_device_config_statsurl: "${temperature_device_config_statsurl}",
        temperature_device_config_devryurl: "${temperature_device_config_devryurl}",
        temperature_device_config_smsGyPh: "${temperature_device_config_smsGyPh}",
        temperature_device_config_sint: "${temperature_device_config_sint}",
        temperature_device_config_pint: "${temperature_device_config_pint}",
        temperature_device_config_usrphones: "${temperature_device_config_usrphones}",
     temperature_device_config_filter_excursion: "${temperature_device_config_filter_excursion}",
     temperature_device_config_filter_alarms: "${temperature_device_config_filter_alarms}",
     temperature_device_config_filter_nodata: "${temperature_device_config_filter_nodata}",
        temperatureeventtime: "${temperatureeventtime}",
        temperatureeventcreationtime: "${temperatureeventcreationtime}",
        temperaturemin: "${temperaturemin}",
        temperaturemax: "${temperaturemax}",
        time: "${time}",
        timezone: "${timezone}",
        total: "${total}",
        transaction: "${transaction}",
        transaction_distancemsg: "${transaction_distancemsg}",
        transactions: "${transactions}",
        transporter: "${transporter}",
        transportermandatory: "${transportermandatory}",
        transtype: "${transtype}",
        type: "${type}",
        typetogetsuggestions: "${typetogetsuggestions}",
        updatedon: "${updatedon}",
        user: "${user}",
        users: "${users}",
        userage: "${userage}",
        useragevalidationmsg: "${useragevalidationmsg}",
        useremail: "${useremail}",
        userfirstname: "${userfirstname}",
        usergender: "${usergender}",
        userid: "${userid}",
        useridvalidationmsg: "${useridvalidationmsg}",
        useripaddress: "${useripaddress}",
        userlandline: "${userlandline}",
        userlastname: "${userlastname}",
        usermobile: "${usermobile}",
        usermobilebrand: "${usermobilebrand}",
        usermobilemodel: "${usermobilemodel}",
        usermobileoperator: "${usermobileoperator}",
        userrole: "${userrole}",
        valuesallowed: "${valuesallowed}",
        variable_customercity: "${variable_customercity}",
        variable_materials: "${variable_materials}",
        variable_materialswithmetadata: "${variable_materialswithmetadata}",
        variable_statuschangetime: "${variable_statuschangetime}",
        variable_vendorcity: "${variable_vendorcity}",
        vendor: "${vendor}",
        vendors: "${vendors}",
        village: "${village}",
        was: "${was}",
        zerostocksettingmsg1: "${zerostocksettingmsg1}",
        zerostocksettingmsg2: "${zerostocksettingmsg2}",
        zipcode: "${zipcode}"
    };
</script>
