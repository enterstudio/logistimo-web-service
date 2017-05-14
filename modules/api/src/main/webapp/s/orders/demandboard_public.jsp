<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.config.models.DomainConfig"%>
<%@page import="com.logistimo.config.models.DemandBoardConfig"%>
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
	String errMsg = null;
	String bannerDiv = null;
	String headingDiv = null;
	String copyrightDiv = null;
	// Get domain Id, if any
	String domainIdStr = request.getParameter( "id" );
	// Get the show-stock parameter
	String view = request.getParameter( "view" );
	boolean stockView = ( "stock".equals( view ) );
	boolean showStock = true; // permissions
	// Get material Id, if any
	String mIdStr = request.getParameter( "materialid" );
	boolean noPermissions = false;
	
	// First check if this demand board is public or not
	Long domainId = null;
	String localeStr = "en";
	if ( domainIdStr != null && !domainIdStr.isEmpty() ) {
		try {
			domainId = Long.valueOf( domainIdStr );
			// Get domain config
			DomainConfig dc = DomainConfig.getInstance( domainId );
			if ( !dc.isDemandBoardPublic() ) {
				noPermissions = true;
			} else {
				DemandBoardConfig dbc = dc.getDemandBoardConfig();
				bannerDiv = dbc.getBanner();
				headingDiv = dbc.getHeading();
				copyrightDiv = dbc.getCopyright();
				showStock = ( !dc.isCapabilityDisabled( DomainConfig.CAPABILITY_INVENTORY ) && dbc.showStock() );
			}
			// Get the domain's locale
			localeStr = dc.getLocale().toString();
		} catch ( NumberFormatException e ) {
			errMsg = "Invalid ID format: " + domainIdStr;
		}
	}
	// Get the demand board map URL
	String mapUrl = "/s/orders/demandboard_maps.jsp?";
	if ( stockView )
		mapUrl = "/s/inventory/inventoryviews_maps.jsp?";
	mapUrl += "pdb";
	if ( domainIdStr != null && !domainIdStr.isEmpty() )
		mapUrl += "&domainid=" + domainIdStr;
	if ( mIdStr != null && !mIdStr.isEmpty() )
		mapUrl += "&materialid=" + mIdStr;
	// Check if custom banner exists
	boolean hasBanner = ( bannerDiv != null && !bannerDiv.isEmpty() );
	// Get the URL for Demand and Stock
	String queryString = ( domainId != null ? "id=" + domainId : "" );
	/*
	if ( mIdStr != null && !mIdStr.isEmpty() ) {
		if ( !queryString.isEmpty() )
			queryString += "&";
		queryString += "materialid=" + mIdStr;
	}
	*/
	String demandUrl = "/pub/demand?" + queryString;
	String stockUrl = demandUrl + "&view=stock";
%>
<!-- Set the user's locale -->
<fmt:setLocale value="<%= localeStr %>" scope="session"/>
<fmt:bundle basename="Messages">
<html>
	<head>
	<meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> <!--  This is placed to enable Google Maps in certain views -->
	<meta http-equiv="content-type" content="text/html; charset=utf-8">
	<title><fmt:message key="config.demandboardinfotitle"/></title>
	<link href="/css/reset-fonts-grids.css" rel="stylesheet" type="text/css" />
	<link href="/css/tabview.css" rel="stylesheet" type="text/css" />
	<link href="/css/sg.css" rel="stylesheet" type="text/css" />
	</head>
	<body class="yui-skin-sam">
	<div id="doc">
		<div id="hd">
			<% if ( !hasBanner ) { %>
			<div style="padding:10px;">
                <!-- @if !isCustomDeployment -->
                <img src="/images/logo.png"/>
                <!-- @endif -->
                <!-- @if isCustomDeployment -->
                <!-- comments:uncomment-block html -->
                <!--
                <img class="imagewrapper" src="<!-- @echo custom.logo -->"/>
                -->
                <!-- endcomments -->
                <!-- @endif -->
			</div>
			<% } else { %>
			<%= bannerDiv %>
			<% } %>
		</div>
		<% if ( noPermissions ) { %>
		<br/><b><fmt:message key="demandboard.nopermissions"/></b>
		<% } else if ( errMsg != null ) { %>
		<b><br/><%= errMsg %></b>
		<% } else { %>
		<div id="bd">
			<% if ( headingDiv == null || headingDiv.isEmpty() ) { %>
			<div id="doc3">
				<div style="font-weight:bold;font-size:14px;padding:6px;margin-left:-10px;margin-right:-10px;border-top:1px solid #E6E6E6;border-bottom:1px solid #E6E6E6;margin-top:2px;"><fmt:message key="demandboard.welcome" />!
					<br/>
					<p style="font-size:12px;font-weight:normal;"><fmt:message key="demandboard.subtitle"/>!</p>
				</div>
			</div>
			<% } else { %>
			<%= headingDiv %>
			<% } %>
			<!-- Togglers between demand and stock views -->
			<% if ( showStock ) { %>
			<div style="text-align:right;font-size:10px;">
				<% if ( stockView ) { %>
					<a href="<%= demandUrl %>"><fmt:message key="demand"/></a> &nbsp;&nbsp;|&nbsp;&nbsp; <b><fmt:message key="stock"/></b>
				<% } else { %>
					<b><fmt:message key="demand"/></b> &nbsp;&nbsp;|&nbsp;&nbsp; <a href="<%= stockUrl %>"><fmt:message key="stock"/></a>
				<% } %>	
			</div>
			<% } %>
			<!--  Include the map here -->
			<jsp:include page="<%= mapUrl %>" flush="true" /> 
			<% if ( copyrightDiv == null || copyrightDiv.isEmpty() ) { %>
			<div id="ft" class="sgfooter">
				&copy; 2013 Logistimo
			</div>
			<% } else { %>
				<%= copyrightDiv %>
			<% } %>
			<% if ( hasBanner ) { // show powered-by SG logo %>
				<div style="float:right;">
					<img src="/images/poweredby.gif" align="right"/>
				</div>
			<% } %>
		</div>
		<% } // if ( errMsg != null ) %>
	</div>
	</body>
</html>
</fmt:bundle>