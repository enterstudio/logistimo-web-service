<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityConstants" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.config.models.DomainConfig"%>
<%@ page import="com.logistimo.security.SecureUserDetails" %>
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
	/*
		menuitems - list of tabs on the page
		Default view is home. Just pass the 'view' parameter to set the menu to that view.
	*/
	// Get the tab view parameters
	String selectedItem = request.getParameter("view");

	// Get the request parameter 'view' and cycle through the array
	if ("".equalsIgnoreCase(selectedItem)) {
		selectedItem = "home";
	}
	// Check capabilties that may be disabled
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String role = null;
	DomainConfig dc = null;
	if ( sUser != null ) {
		String userId = sUser.getUsername();
		role = sUser.getRole();
		Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
		dc = DomainConfig.getInstance( domainId );
	}

	boolean showOnlyTemperatures = (dc != null && dc.isTemperatureMonitoringEnabled());
	// Is AutoGI/GR enabled for orders
	boolean autoInventoryPosting = dc!=null && ( dc.autoGI() || dc.autoGR() );
	boolean showInventory = ((!showOnlyTemperatures) && (autoInventoryPosting || !(dc!=null && dc.isCapabilityDisabled(DomainConfig.CAPABILITY_INVENTORY))));
	boolean showOrders = (!showOnlyTemperatures && dc != null && !dc.isCapabilityDisabled(DomainConfig.CAPABILITY_ORDERS));
	boolean isEntityManager = SecurityConstants.ROLE_SERVICEMANAGER.equals( role );
	boolean isEntityOperator = SecurityConstants.ROLE_KIOSKOWNER.equals( role );
	boolean showConfiguration = ( !isEntityManager && !isEntityOperator );
	boolean showAccounts = (!showOnlyTemperatures && dc != null && dc.isAccountingEnabled());
	boolean showReports = ((!showOnlyTemperatures) && !(isEntityManager || isEntityOperator));
	boolean showPayments = ((!showOnlyTemperatures) && dc != null && dc.isPaymentsEnabled());
	boolean showTemperatures = (dc != null && (showOnlyTemperatures || dc.isTemperatureMonitoringWithLogisticsEnabled()));
%>
<fmt:bundle basename="Messages">
<div id="sgmenu" class="yui-navset">
	 <ul class="yui-nav">
	 <% if ( "kioskmaterials".equalsIgnoreCase( selectedItem ) ) { %>
	 	<li class="selected"><em><fmt:message key="kioskmaterials" /></em></li>
	 <% } else { %>
            <% if (!showOnlyTemperatures) { %>
            <li class="<%= "home".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a
					href="/s/index.jsp"><em><fmt:message key="home"/></em></a></li>
            <% } %>
	 	<% if ( showOrders ) { %>
	 	<li class="<%= "orders".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a href="/s/orders/orders.jsp"><em><fmt:message key="orders" /></em></a></li>
	 	<% } %>
	 	<% if ( showInventory ) { %>
	 	<li class="<%= "inventory".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a href="/s/inventory/inventory.jsp"><em><fmt:message key="inventory" /></em></a></li>
	 	<% } %>
	 	<% if ( showPayments ) { %>
	 	<li class="<%= "payments".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a href="/s/payments/payments.jsp"><em><fmt:message key="payments" /></em></a></li>
	 	<% } %>
	 	<% if ( showAccounts ) { %>
	 	<li class="<%= "accounts".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a href="/s/accounts/accounts.jsp"><em><fmt:message key="accounts" /></em></a></li>
	 	<% } %>
	 	<% if ( showReports ) { %>
	 	<li class="<%= "reports".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a href="/s/reports/reports.jsp"><em><fmt:message key="reports" /></em></a></li>
	 	<% } %>
			<% if (showTemperatures) { %>
			<li class="<%= "temperature".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a
					href="/s/temperature/temperature.jsp"><em><fmt:message key="temperature"/></em></a></li>
			<% } %>
	 	<% if ( showConfiguration ) { %>
	 	<li class="<%= "configuration".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a href="/s/config/configuration.jsp"><em><fmt:message key="configuration" /></em></a></li>
	 	<% } %>
	 	<li class="<%= "setup".equalsIgnoreCase( selectedItem ) ? "selected" : "" %>"><a href="/s/setup/setup.jsp"><em><fmt:message key="setup" /></em></a></li>
	 <% } // end if %>				 	
		</ul>
	</div>
</fmt:bundle>