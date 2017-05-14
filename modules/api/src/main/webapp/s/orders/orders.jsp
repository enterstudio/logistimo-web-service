<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.ArrayList" %>
<%@page import="com.logistimo.api.security.SecurityMgr"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.api.util.SessionMgr"%>
<%@page import="com.logistimo.config.models.DomainConfig"%>
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

<jsp:include page="../pageheader.jsp" />
<jsp:include page="../menu.jsp" >

<jsp:param name="view" value="orders" />
	</jsp:include>

<%
	// Get domain Id
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	// Check if auto-order generation is enabled
	DomainConfig dc = DomainConfig.getInstance( domainId );
	boolean autoOrderGen = dc.autoOrderGeneration();
	// Get the desired subview
	String subview = request.getParameter("subview");
	if ( subview == null || subview.isEmpty() ) {
		if ( autoOrderGen )
			subview = "orders";
		else
			subview = "demandboard";
	}
	String maps = request.getParameter("maps");

	// Get the request parameters
	String durationStr = request.getParameter("duration");
	String kioskIdStr = request.getParameter("kioskid");
	String materialIdStr = request.getParameter("materialid");
	String offsetStr = request.getParameter( "o" );
	String showDB = request.getParameter( "show" );
	String action = request.getParameter( "action" );
	
	// Form the demand board URL
	String dbUrl = "demandboard.jsp?duration=" + ( durationStr != null ? durationStr : "" ) +
				   "&kioskid=" + ( kioskIdStr != null ? kioskIdStr : "" ) +
				   "&materialid=" + ( materialIdStr != null ? materialIdStr : "" ) +
				   "&o=" + ( offsetStr != null ? offsetStr : "" ) +
				   "&show=" + ( showDB == null ? "" : showDB ) +
				   "&subview=" + subview;
	// Form the add-orders (add-demand) URL
	String addOrdersUrl = "adddemand.jsp?kioskid=" + ( kioskIdStr != null ? kioskIdStr : "" ) + "&subview=" + subview;
	// View orders URL with subview
	String viewOrdersUrl = "vieworders.jsp?subview=" + subview;
%>
<fmt:bundle basename="Messages">
<div id="sgSubMenu">
	<p>
	 <%if ( autoOrderGen ) {%>
	 <a class="<%= "orders".equalsIgnoreCase( subview ) ? "selected" : "" %>" href="orders.jsp?subview=orders"><fmt:message key="orders"/></a>
	 <%}%>
	 <a class="<%= "demandboard".equalsIgnoreCase( subview ) ? "selected" : "" %>" href="orders.jsp?subview=demandboard"><fmt:message key="demand"/></a>
	</p>
</div>

</fmt:bundle>
<% if ( subview.equals("demandboard") ) { %>
    <% if ( "create".equals( action ) || "modify".equals( action ) ) { %>
    <jsp:include page="<%= addOrdersUrl %>" flush="true" />
    <% } else { 
    	if ( maps == null || maps.isEmpty() ) { %>
		<jsp:include page="<%= dbUrl %>" flush="true" />
		<% } else {  %>
		<jsp:include page="demandboard_maps.jsp" flush="true" />
		<% } %>
	<% } %>
<% } else if ( subview.equals( "orders" ) ) { %>
	<% if ( "create".equals( action ) || "modify".equals( action ) ) { %>
    <jsp:include page="<%= addOrdersUrl %>" flush="true" />
    <% } else { %>
	<jsp:include page="<%= viewOrdersUrl %>" flush="true" />
	<% } %>
<% } %>
<jsp:include page="../pagefooter.html" />