<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.entities.service.AccountsServiceImpl"%>
<%@page import="com.logistimo.services.Services"%>
<%@page import="com.logistimo.services.ServiceException"%>
<%@page import="com.logistimo.config.models.DomainConfig"%>
<%@page import="com.logistimo.config.models.OptimizerConfig"%>
<%@page import="com.logistimo.inventory.service.InventoryManagementService"%>
<%@page import="com.logistimo.inventory.service.impl.InventoryManagementServiceImpl"%>
<%@page import="com.logistimo.entities.entity.AccountsService"%>
<%@page import="com.logistimo.materials.service.MaterialCatalogService"%>
<%@page import="com.logistimo.materials.service.impl.MaterialCatalogServiceImpl"%>
<%@page import="com.logistimo.api.security.SecurityMgr"%>
<%@page import="com.logistimo.pagination.PageParams"%>
<%@page import="com.logistimo.inventory.TransactionUtil"%>
<%@page import="com.logistimo.tags.TagUtil"%>
<%@page import="com.logistimo.utils.NumberUtil"%>
<%@page import="com.logistimo.utils.Counter"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.api.util.SessionMgr"%>
<%@page import="com.logistimo.pagination.Results"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Iterator"%>
<%@ page import="org.lggi.samaanguru.entity.*" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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

<script type="text/javascript" src="../../js/sg.js"></script>
<jsp:include page="../JSMessages.jsp" flush="true" />
<%
	String kioskIdStr = request.getParameter("kioskid");
	String subview = request.getParameter( "subview" );	
	String tag = request.getParameter( "tag" );
	boolean hasTag = ( tag != null && !tag.isEmpty() );
	// Decode tag, if needed
	if ( hasTag )
		tag = java.net.URLDecoder.decode( tag, "UTF-8" );
	else
		tag = null; // just to nullify it, in case of empty string
		
	MaterialCatalogService mcs = null;
	InventoryManagementService ims = null;
	AccountsService as = null;
	IKiosk kiosk = null;
	List<IInvntry> inList = null;
	boolean allowVendorSelection = false;
	String errMsg = null;
	int numVendors = 0;
	
	// Get the domain Id
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	// Get locale
	Locale locale = sUser.getLocale();
	
	// Get the kiosk ID
	Long kioskId = null;
	try {
		ims = Services.getService( InventoryManagementServiceImpl.class, locale );
		if ( kioskIdStr != null && !kioskIdStr.isEmpty() )
			kioskId = Long.valueOf( kioskIdStr );
	} catch ( NumberFormatException e ) {
		errMsg = "Kiosk ID " + kioskIdStr + " should be a number [" + e.getMessage() + "]";
	} catch ( ServiceException e ) {
		errMsg = "Error: " + e.getMessage();
	}
	
	// Get the transaction naming convention
	DomainConfig dc = DomainConfig.getInstance( domainId ); // SessionMgr.getDomainConfig( request.getSession(), domainId );
	String transNaming = dc.getTransactionNaming();
	// Get the optimization config
	OptimizerConfig oc = dc.getOptimizerConfig();
	
	// Get base URL
	String baseUrl = "orders.jsp?action=create&subview=orders&kioskid=" + kioskId;
	
	// Get the kiosk object and its inventory
	try {
		// Get kiosk
		as = Services.getService( AccountsServiceImpl.class, locale );
		kiosk = as.getKiosk( kioskId, false );
		// Get linked kiosks info., if any
		numVendors = Counter.getKioskLinkCounter( domainId, kioskId, IKioskLink.TYPE_VENDOR ).getCount();
		allowVendorSelection = ( numVendors > 0 || dc.getVendorId() != null ); // has vendors or has default vendor configured
		// Get inventory for this kiosk
		Results results = null;
		if ( hasTag )
			results = ims.getInventoryByKiosk( kioskId, tag, null );
		else
			results = ims.getInventoryByKiosk( kioskId, null );
		inList = results.getResults();
		// Get the catalog service for later use - to display material names
		mcs = Services.getService( MaterialCatalogServiceImpl.class, locale );
		if ( inList == null || inList.size() == 0 ) {
			errMsg = "No materials available.";
		}
	} catch ( ServiceException e ) {
		errMsg = "Error: " + e.getMessage();
	}
	// Form this URL, for any redirects
	String thisUrl = "orders.jsp?action=create&subview=" + subview + "&kioskid=" + kioskIdStr;
	// Check if consumption rate is to be shown
	boolean isConsumptionRate = ( oc.getCompute() == OptimizerConfig.COMPUTE_CONSUMPTION ); 
	boolean showConsumptionRateDaily = oc.hasConsumptionRate( OptimizerConfig.FREQ_DAILY );
	boolean showConsumptionRateWeekly = oc.hasConsumptionRate( OptimizerConfig.FREQ_WEEKLY );
	boolean showConsumptionRateMonthly = oc.hasConsumptionRate( OptimizerConfig.FREQ_MONTHLY );
	boolean isDemandForecast = ( oc.getCompute() == OptimizerConfig.COMPUTE_FORECASTEDDEMAND );
	boolean isEOQ = ( oc.getCompute() == OptimizerConfig.COMPUTE_EOQ && kiosk.isOptimizationOn() );
%>
<!-- Autocomplete box -->
<link rel="stylesheet" href="../../autocomplete/TextboxList.css" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="../../autocomplete/TextboxList.Autocomplete.css" type="text/css" media="screen" charset="utf-8" />
<script src="../../js/jquery-1.3.2.min.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/GrowingInput.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.Autocomplete.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.Autocomplete.Binary.js" type="text/javascript" charset="utf-8"></script>
<style type="text/css">
	.dropdownselector input {border:0;}
</style>
<script type="text/javascript">
function validateVendorAndAllTransEntries() {
	// Check if a vendor is selected
	var vendor = document.getElementById('vendorkioskid');
	if ( vendor == null || vendor.options[ vendor.selectedIndex ].value == '' ) {
		alert( JSMessages.selectvendormsg );
		return false;
	}
	// Check if one can place orders without items
	<% if ( dc.allowEmptyOrders() ) { %>
		return true;
	<% } else { %>
	return validateAllTransEntries();
	<% } %>
}

function showInvMessage( kioskId, materialId ) {
	window.open ('../inventory/inventory_message.jsp?kioskid=' + kioskId + '&materialid=' + materialId,'inventory','location=1,resizable=1,scrollbars=1,width=800,height=300');
}

//Initialize vendor selection drop-down
function initVendorDropDown() {
	<% if ( allowVendorSelection ) { %>
	var placeholder = JSMessages.typetogetsuggestions;
	var vendorDropDown = new $.TextboxList('#vendorkioskid', { unique: true,
						    				max: 1,
						    				bitsOptions: {
												 box: { deleteButton: false }
											 },
										    plugins: { 	autocomplete: 
														{ 	method:'binary',
												  			onlyFromValues: true,
												  			placeholder: placeholder,
												  			maxResults: 50,
												  			minLength: 1,
												  			queryRemote: true,
												  			remote: { url: '/s/list.jsp?type=kiosklinks&linktype=<%= IKioskLink.TYPE_VENDOR %>&kioskid=<%= kioskId %>' }
													  	}
		  											}
										  } );
	 vendorDropDown.clear();
	 <% } %>
	 // Add default vendor, if needed
	 <% if ( numVendors == 1 || ( numVendors == 0 && dc.getVendorId() != null ) ) { 
			 IKiosk vendor = null;
			 Long vendorId = null;
			 if ( numVendors == 1 ) {
				 List<IKioskLink> links = as.getKioskLinks( kioskId, IKioskLink.TYPE_VENDOR, null, new PageParams( null, 1 ) ).getResults();
				 if ( links != null && !links.isEmpty() )
					 vendorId = links.get(0).getLinkedKioskId();
			 } else {
				 vendorId = dc.getVendorId();
			 }
			 if ( vendorId != null ) {
				 try { 
					 vendor = as.getKiosk( vendorId, false );
				 } catch ( Exception e ) {
					 e.printStackTrace();
				 }
			 }
			 if ( vendor != null ) { %>
		 		if ( vendorDropDown )
		 			vendorDropDown.add( '<%= vendor.getName() %>', '<%= vendor.getKioskId() %>', null );
		  <% }
	    } %>
}

window.onload = function() {
	initVendorDropDown();
}
</script>
<fmt:bundle basename="Messages">
<fmt:message key="submit" var ="submit"/>
<fmt:message key="reset" var ="reset"/>

<div id="doc3">
	<h2 class="sgHeading"><%= kiosk.getName() %> &gt <fmt:message key="add"/> <fmt:message key="order" /></h2>
</div>
<% if ( kiosk == null || errMsg != null ) { %>
<%= errMsg %>
<% } else { %>
<div id="doc3">
 <form id="sgFilter" name="orderform" class="sgForm" action="/s/createentity" method="post">
	<table id="addresource" width="100%">
	<tr>
		<th class="mandatory"><fmt:message key="vendor"/></th>
		<td class="dropdownselector">
		<% if ( allowVendorSelection ) { // has vendors or has a default vendor configured %>
			<input type="text" id="vendorkioskid" name="vendorkioskid" />
			(<a href="../setup/setup.jsp?subview=kiosks&form=kiosklinks&kioskid=<%= kioskId %>&linktype=v" target="_new"><fmt:message key="view" /> <fmt:message key="vendors" /></a>)
		<% } %>
		<% if ( numVendors == 0 ) { %>
			<fmt:message key="relationship.novendors" />. 
			<a href="../setup/setup.jsp?subview=kiosks&form=kiosklinks&kioskid=<%= kioskId %>"><fmt:message key="relationship.specifyvendors" /></a>.
		<% } %>
		</td>
	</tr>
	<% if ( dc == null || dc.autoOrderGeneration() ) { %>
	<tr>
		<th><fmt:message key="status"/></th>
		<td>
		<select name="status" id="status">
			<option value="">-- <fmt:message key="select"/> <fmt:message key="order"/> <fmt:message key="status"/> --</option>
			<option value="<%= IOrder.CONFIRMED %>"><fmt:message key="order.confirmed"/></option>
		</select>
		<br/>
		<i>(<fmt:message key="order.initialstatusmsg"/>)</i>
		</td>
	</tr>
	<tr>
		<th><fmt:message key="message"/></th>
		<td><input type="text" name="message" maxlength=200 /></td>
	</tr>
	<% } %>
	</table>
	<br/>
	<%= TagUtil.getTagHTML( domainId, TagUtil.TYPE_MATERIAL, tag, locale, baseUrl ) %>
	<table id="kioskstable" width="100%">
	<tr>
		<th style="width:2%"><fmt:message key="serialnum"/></th>
		<th><fmt:message key="material"/></th>
		<th><fmt:message key="price"/> <%= kiosk.getCurrency() != null && !kiosk.getCurrency().isEmpty() ? " (" + kiosk.getCurrency() + ")" : "" %></th>
		<th><fmt:message key="material.stockonhand"/></th>
		<% if ( isConsumptionRate ) { %>
		<th><fmt:message key="config.consumptionrates"/></th>
		<% } else if ( isDemandForecast || isEOQ ) { %>
		<th><fmt:message key="demandforecast"/></th>
		<% } %>
		<% if ( isEOQ ) { %>
		<th><fmt:message key="order.optimalorderquantity"/></th>
		<% } %>
		<th><fmt:message key="quantity"/></th>
	</tr>
	<%
		// Show material list
		if ( inList != null && inList.size() > 0) {
			int styleCounter = 0;
			int slNo = 1;
			for ( IInvntry inv : inList ) {
				IMaterial m = mcs.getMaterial( inv.getMaterialId() );
				String tdclass="";
				if (styleCounter %2 ==0) {
					tdclass="oddrow";
				}
				float retailerPrice = m.getRetailerPrice();
				if ( inv.getRetailerPrice() != 0 )
					retailerPrice = inv.getRetailerPrice();
				%>
				<tr>
				 <td class="<%= tdclass %>"><%= slNo %></td>
				 <td class="<%= tdclass %>"><%= m.getName() %></td>
				 <td class="<%= tdclass %>"><%= retailerPrice %></td>
				 <td class="<%= tdclass %>"><%= NumberUtil.round2( inv.getStock() ) %></td>
				 <% if ( isConsumptionRate ) { %>
				<td class="<%= tdclass %>">
				 <table>
				  <tr>
					 <% if ( showConsumptionRateDaily ) { %>
				  	<td style="width:33%;border:0";>
					  <%= NumberUtil.round2( inv.getConsumptionRateDaily() ) %> / <fmt:message key="day"/>
					</td>
					 <% } %>
					 <% if ( showConsumptionRateWeekly ) { %>
					<td style="width:33%;border:0";>
					  <%= NumberUtil.round2( inv.getConsumptionRateWeekly() ) %> / <fmt:message key="week"/>
					</td>
					 <% } %>
					 <% if ( showConsumptionRateMonthly ) { %>
					<td style="width:33%;border:0";>
					  <%= NumberUtil.round2( inv.getConsumptionRateMonthly() ) %> / <fmt:message key="month"/>
					</td>
					 <% } %>
					</tr>
				  </table>
				  <sup><a href="#" title="details" onclick="showInvMessage('<%= inv.getKioskId() %>','<%= inv.getMaterialId() %>')">></a></sup>
				 </td>
				<% } else if ( isDemandForecast || isEOQ ) { %>
				 <td class="<%= tdclass %>"><%= NumberUtil.round2( inv.getRevPeriodDemand() ) %> (<fmt:message key="over"/> <%= inv.getOrderPeriodicity() %> <fmt:message key="days"/>) <sup><a href="#" title="details" onclick="showInvMessage('<%= inv.getKioskId() %>','<%= inv.getMaterialId() %>')">></a></sup></td> 
				<% } %>
				 <% if ( isEOQ ) { %>
				 <td class="<%= tdclass %>"><%= NumberUtil.round2( inv.getEconomicOrderQuantity() ) %> <sup><a href="#" title="details" onclick="showInvMessage('<%= inv.getKioskId() %>','<%= inv.getMaterialId() %>')">></a></sup></td>
				 <% } %>
				 <td class="<%= tdclass %>"><input type="text" name="<%= m.getMaterialId() %>" size="6" onblur="validateTransEntry( this, false<%= m.isBinaryValued() ? ", 1, 1" : "" %> )"/>
				 <% if ( m.isBinaryValued() ) { %>
				 <br/><i>(<fmt:message key="isbinaryvalued.indicator"/>)</i>
				 <% } %>
				 </td>
				</tr>
				<%
				styleCounter++;
				slNo++;
			}
		} else { %>
			<tr><td colspan="3"><fmt:message key="materials.none" /></td></tr>
	 <% } %>
	</table>
	<br/>
	<input type="submit" name="Submit" value="${submit}" onclick="return validateVendorAndAllTransEntries();"/>
	<input type="reset" name="Reset" value="${reset}" />
	<input type="hidden" name="subview" value="<%= subview %>" /> 
	<input type="hidden" name="kioskid" value="<%= kioskIdStr %>" />
	<input type="hidden" name="action" value="create" />
	<input type="hidden" name="type" value="order" />
 </form>
</div>
<% } // end if %>
</fmt:bundle>