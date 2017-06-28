<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.entities.entity.IKiosk" %>
<%@page import="com.logistimo.entities.service.EntitiesService" %>
<%@page import="com.logistimo.entities.service.EntitiesServiceImpl" %>
<%@page import="com.logistimo.materials.service.MaterialCatalogService" %>
<%@page import="com.logistimo.materials.service.impl.MaterialCatalogServiceImpl"%>
<%@page import="com.logistimo.orders.entity.IDemandItem" %>
<%@ page import="com.logistimo.orders.entity.IDemandItemBatch" %>
<%@ page import="com.logistimo.orders.entity.IOrder" %>
<%@ page import="com.logistimo.orders.service.IDemandService" %>
<%@ page import="com.logistimo.orders.service.OrderManagementService" %>
<%@ page import="com.logistimo.orders.service.impl.DemandService" %>
<%@ page import="com.logistimo.orders.service.impl.OrderManagementServiceImpl" %>
<%@ page import="com.logistimo.security.SecureUserDetails" %>
<%@ page import="com.logistimo.services.Services" %>
<%@ page import="com.logistimo.users.entity.IUserAccount" %>
<%@ page import="com.logistimo.users.service.UsersService" %>
<%@ page import="com.logistimo.users.service.impl.UsersServiceImpl" %>
<%@ page import="com.logistimo.utils.BigUtil" %>
<%@ page import="com.logistimo.utils.CommonUtils" %>
<%@ page import="com.logistimo.utils.LocalDateUtil" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
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

<%
	// Get the request params.
	String oid = request.getParameter( "oid" );
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	Locale locale = sUser.getLocale();
	String timezone = sUser.getTimezone();
	Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
	// Get the data
	String errMsg = null;
	IOrder o = null;
	IKiosk vendor = null;
	IKiosk customer = null;
	MaterialCatalogService mcs = null;
	IUserAccount vendorUser = null;
	DomainConfig dc = DomainConfig.getInstance(domainId);
	boolean dop = dc.isDisableOrdersPricing();
	String rid = null;
	try {
		// Get the services
		OrderManagementService oms = Services.getService( OrderManagementServiceImpl.class, locale );
		EntitiesService as = Services.getService( EntitiesServiceImpl.class, locale );
		UsersService us = Services.getService( UsersServiceImpl.class, locale );
		mcs = Services.getService( MaterialCatalogServiceImpl.class, locale );
		// Get the order
		Long oidL = Long.valueOf( oid );
		o = oms.getOrder( oidL );
		IDemandService ds = Services.getService(DemandService.class);
		o.setItems(ds.getDemandItems(oidL));
		rid = o.getReferenceID();
		// Get the ordering kiosk
		customer = as.getKiosk( o.getKioskId(), false );
		// Get the vendor kiosk
		Long vKioskId = o.getServicingKiosk();
		if ( vKioskId == null ) {
			// Get the logged in user's kiosk
			vendorUser = us.getUserAccount( userId ); // this is because vendor.getUser() will not yield any user (unfortunately!)
			vendor = as.getUserWithKiosks(vendorUser).getKiosks().get(0);
		} else {
			vendor = as.getKiosk( vKioskId );
			vendorUser = vendor.getUser();
		}
	} catch ( Exception e ) {
		errMsg = e.getMessage();
	}
	/*if ( errMsg == null && vendor == null )
		errMsg = "Unable to get vendor information";*/
	String currency = o.getCurrency();
%>
<jsp:include page="../pageheader.jsp" >
<jsp:param name="notoplinks" value="" />
	</jsp:include>
<fmt:bundle basename="JSMessages">
	<fmt:message key="date" var="date"/>
</fmt:bundle>
<fmt:bundle basename="Messages">
<% if ( errMsg != null ) { %>
 <fmt:message key="error"/>: <%= errMsg %>
<% } else { %>
<div id="doc3" style="width:90%">
	<div id="printbutton" style="text-align:right;margin-bottom:5px;"><a href="#" onclick="document.getElementById('printbutton').style.display='none'; window.print(); return false;"><img src="../../images/print.png" width="24px" height="24px" alt="print" title="print"/></a></div>
	<h2 class="sgHeading" style="text-align:center;"><fmt:message key="invoice"/>
	</h2>
	<table id="order" width="100%" style="font-size:12px;margin-top:5px;">
		<tr>
			<td style="text-align:center;">
                <% if(vendor != null){%>
				<p style="font-size:14px;font-weight:bold;"><%= vendor.getName() %></p>
				<%= vendor.getFormattedAddress() %>
				<br/>
				<% if ( vendorUser != null ) { %>
				<fmt:message key="phone"/>: <%= vendorUser.getMobilePhoneNumber() %>
				<% } %>
				<% if ( vendor.getTaxId() != null && !vendor.getTaxId().isEmpty() ) { %>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<fmt:message key="tax.id"/>: <%= vendor.getTaxId() %>
				<% } }%>
			</td>
		</tr>
		<tr>
			<td>
				<table style="width:100%;text-align:left;">
					<tr>
						<td style="width:50%">
							To:<br/><p style="font-size:14px;font-weight:bold;"><%= customer.getName() %></p>
							<%= customer.getFormattedAddress() %>
						</td>
						<td style="width:50%;">
						 	<table style="width:100%;text-align:left;">
						 		<tr><td style="font-weight:bold;"><fmt:message key="invoice.number"/></td><td><%= oid %></td></tr>
								<% if (StringUtils.isNotBlank(rid)) { %>
								<tr><td style="font-weight:bold;"><fmt:message key="referenceid" /></td><td><%= rid %></td></tr>
								<% } %>
						 		<tr><td style="font-weight:bold;">${date}</td><td><%= LocalDateUtil
										.format(new Date(), locale, timezone) %></td></tr>
						 		<% if ( customer.getTaxId() != null && !customer.getTaxId().isEmpty() ) { %>
						 		<tr><td style="font-weight:bold;"><fmt:message key="tax.id"/></td><td><%= customer.getTaxId() %></td></tr>
						 		<% } %>
						 	</table>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	<table id="order" width="100%" style="font-size:12px;margin-top:5px;">
		<tr>
			<th style="width:10px"><fmt:message key="serialnum"/></th>
			<th><fmt:message key="item"/></th>
			<th><fmt:message key="quantity"/></th>
			<%if(!dop) {%>
			<th><fmt:message key="price"/> <%= currency == null || currency.isEmpty() ? "" : "(" + currency + ")" %></th>
			<th><fmt:message key="amount"/> <%= currency == null || currency.isEmpty() ? "" : "(" + currency + ")" %></th>
			<% } %>
		</tr>
		<%
		Iterator<IDemandItem> it = (Iterator<IDemandItem>) o.getItems().iterator();
		int slnum = 1;
		while ( it.hasNext() ) {
			IDemandItem item = it.next();
			if (BigUtil.equalsZero(item.getQuantity()) )
				continue;
			BigDecimal discount = item.getDiscount();
            BigDecimal itemTax = item.getTax();
			List<Map<String,Object>> batches = item.getBatchesAsMap();
			%>
			<tr>
			<td style="width:10px"><%= slnum++ %></td>
			<td><%= mcs.getMaterial( item.getMaterialId() ).getName() %></td>
			<td>
				<%= BigUtil.round2( item.getQuantity() ) %>
				<% if ( batches != null && !batches.isEmpty() ) {
					%>
					<%
					Iterator<Map<String,Object>> itBatches = batches.iterator();
					while ( itBatches.hasNext() ) {
						Map<String,Object> batch = itBatches.next();
						%>
						<br/><font style="font-size:7pt"><fmt:message key="batch"/>: <%= batch.get( IDemandItemBatch.BATCH_ID ) %>, <fmt:message key="expiry" />: <%= LocalDateUtil.format( (Date) batch.get( IDemandItemBatch.EXPIRY ), locale, null, true ) %>, <fmt:message key="quantity" />: <%= batch.get( IDemandItemBatch.QUANTITY ) %></font>
						<%
					}
					%>
				<% } %>
			</td>
			<%if(!dop) {%>
			<td><%= item.getFormattedPrice() %>
			<% if ( discount != null && BigUtil.notEqualsZero(discount) ) { %>
		 	</br>[<font style="font-size:10px;"><%= String.format( "%.2f", discount ) %>% <fmt:message key="discount"/></font>]
		 	<% } %>
		 	<% if ( itemTax != null && BigUtil.notEqualsZero(itemTax)) { %>
		 	<br/>[<font style="font-size:10px;color:blue;"><%= BigUtil.getFormattedValue(itemTax) %>% <fmt:message key="tax"/></font>]
		 	<% } %>
			</td>
			<td><%= CommonUtils.getFormattedPrice(item.computeTotalPrice(false)) %></td>
			<% } %>
			</tr>
			<%
		}
		%>
	</table>
	<% if(!dop){ %>
	<table id="order" style="width:100%;text-align:left;font-size:12px;margin-top:5px;">
		<tr>
			<td style="width:60%;"></td>
			<td style="width:40%;">
				<table style="width:100%;text-align:left;">
					<tr><td style="font-weight:bold;"><fmt:message key="subtotal"/></td><td><%= CommonUtils.getFormattedPrice( o.computeTotalPrice( false ) ) %></td></tr>
					<tr><td style="font-weight:bold;"><fmt:message key="tax"/></td><td><%= BigUtil.getFormattedValue(o.getTax()) %>%</td></tr>
					<tr style="font-weight:bold;"><td><fmt:message key="total"/> <%= currency == null || currency.isEmpty() ? "" : "(" + currency + ")" %></td><td><%= o.getFormattedPrice() %></td></tr>
				</table>
			</td>
		</tr>
	</table>
	<% } %>
	<table id="order" style="width:100%;text-align:left;font-size:12px;margin-top:5px;">
		<td style="width:60%;"></td>
		<td style="width:40%;"><fmt:message key="for"/> <b><%= vendor!=null?vendor.getName():"" %></b>
			<br/><br/><br/><br/><br/>
			<fmt:message key="authorizedsignatory"/>
		</td>
	</table>
</div>
<% } %>
</fmt:bundle>
</div>
</body>
</html>