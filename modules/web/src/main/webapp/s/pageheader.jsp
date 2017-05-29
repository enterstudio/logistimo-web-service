<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.services.Services"%>
<%@page import="com.logistimo.services.ServiceException"%>
<%@page import="com.logistimo.config.models.DomainConfig"%>
<%@page import="com.logistimo.logger.XLog"%>
<%@page import="com.logistimo.api.util.SessionMgr"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.api.security.SecurityMgr"%>
<%@ page import="com.logistimo.domains.entity.IDomain" %>
<%@ page import="com.logistimo.domains.service.DomainsService" %>
<%@ page import="com.logistimo.domains.service.impl.DomainsServiceImpl" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
	// Get the view (currently enabled for login page to prevent showing top links)
	String view = request.getParameter( "view" );
	String doNotShowTopLinksStr = request.getParameter( "notoplinks" );
    String mode = request.getParameter( "mode" ); // Added by Vani in order to hide 'Try new console' link in the pages that are shown when the 'Manage' link is clicked. This is accessed only by superusers.
    boolean isManageMode = "manage".equals( mode );

	boolean showTopLinks = true;
	if ( "login".equalsIgnoreCase( view ) )
		showTopLinks = false;
	if ( showTopLinks )
		showTopLinks = ( doNotShowTopLinksStr == null );
	// Get the user details
	SecureUserDetails userDetails = SecurityMgr.getUserDetails(request.getSession());
	// Get the domain Id and user role
	String role = "";
	String userId = null;
	Long domainId = null;
	DomainConfig dc = null;
	if ( userDetails != null ) {
		userId = userDetails.getUsername();
		role = userDetails.getRole();
		// Get the current domain Id
		domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
		if ( domainId != null )
			dc = DomainConfig.getInstance( domainId );
	} // NOTE: userDetails can be null in the intial login page, where user is yet to login

	
	String domainName = null;
	String pageHeader = null;
	// Get custom page header, if any
	if ( dc != null )
		pageHeader = dc.getPageHeader();
	// Get the domain name
	try {
		DomainsService as = Services.getService(DomainsServiceImpl.class);
		IDomain domain = null;
		if ( domainId != null )
			domain = as.getDomain( domainId );
		if ( domain != null )
			domainName = domain.getName();
	} catch ( ServiceException e ) {
		e.printStackTrace();
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
	<meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> <!--  This is placed to enable Google Maps in certain views -->
	<meta http-equiv="content-type" content="text/html; charset=utf-8">
        <title>Logistimo</title>
	<link href="/css/reset-fonts-grids.css" rel="stylesheet" type="text/css" />
	<link href="/css/tabview.css" rel="stylesheet" type="text/css" />
	<link href="/css/sg.css" rel="stylesheet" type="text/css" />
        <script src="/js/jquery-1.11.0.min.js" type="text/javascript" charset="utf-8"></script>
        <script type="text/javascript">
            function onClickTryNewConsole() {
                var url = '/s/createentity?type=kioskowner&action=setuipref&userid=<%=userId%>&uipref=true';
                $.ajax({
                    url: url,
                    dataType: 'text',
                    success: function( msg ) {
                        if ( msg && msg != '' ) {
                            var message = 'A system error occurred while setting the ui preference for the user. Please contact your system administrator. Message: ' + o.responseText;
                            alert( 'ERROR: ' + message );
                        }
                    }
                });
            }
        </script>
	</head>
	<body class="yui-skin-sam">
	<!--  Load the localization property file -->
	<div id="doc2">
		<% if ( showTopLinks ) { %>
		<fmt:bundle basename="Messages">
		<fmt:message key="manage" var="modeName" />
		<c:set var="modeUrl" value="/s/admin/domains.jsp" />
		<c:if test="${param['mode'] == 'manage'}">
			<fmt:message key="home" var="modeName" />
			<c:set var="modeUrl" value="/s/index.jsp" />
		</c:if>
		<div id="sgsignout">
            <% if ( !isManageMode ) { %>
			    <p><b><a href="/v2/index.html" onclick="onClickTryNewConsole();">Try the new console</a></b>&nbsp;|&nbsp;<b><%= userId %></b> <fmt:message key="in" /> <b><%= domainName %></b> &nbsp; | &nbsp;&nbsp;
            <%}%>
			<% if ( SecurityMgr.hasAccess(SecurityMgr.OP_MANAGE, role) ) { %>
				<a href="${modeUrl}">${modeName}</a> &nbsp; | &nbsp;
			<% } // end if %>
			<a href="/enc/authenticate?action=lo"><fmt:message key="signout" /></a></p>
		</div>
		</fmt:bundle>
		<% } %>
		<div id="hd" style="margin-top:20px;">
			<% if ( pageHeader != null && !pageHeader.isEmpty() ) { %>
            <div class="imagewrapper"><%= pageHeader %></div>
			<% } else { %>
			<div>
                <img class="imagewrapper" src="/images/acquia_marina_logo.png"/>
			</div>
			<% } // end if %>
		</div>
		<div id="bd">