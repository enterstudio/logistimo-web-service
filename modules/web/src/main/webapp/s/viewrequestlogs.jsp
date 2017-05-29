<%@page import="java.net.URLDecoder"%>
<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Date"%>
<%@page import="com.logistimo.entity.IALog"%>
<%@page import="com.logistimo.constants.Constants"%>
<%@page import="com.logistimo.api.security.SecurityMgr"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.api.util.SessionMgr"%>
<%@page import="com.logistimo.utils.LocalDateUtil"%>
<%@page import="com.logistimo.logger.XLog"%>
<%@page import="com.logistimo.pagination.PageParams"%>
<%@page import="com.logistimo.pagination.Navigator"%>
<%@page import="com.logistimo.pagination.Results"%>
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

<jsp:include page="pageheader.jsp" >


<jsp:param name="notoplinks" value="" />
	</jsp:include>
<jsp:include page="JSMessages.jsp" flush="true" />
<script type="text/javascript" src="../../js/sg.js"></script>
<%
	// Get request parameters
	String type = request.getParameter( "type" );
	String durationStr = request.getParameter( "duration" );
	// Get pagination parameters
	String offsetStr = request.getParameter( "o" );
	String sizeStr = request.getParameter( "s" );
	int offset = 1;
	int size = PageParams.DEFAULT_SIZE;
	if ( offsetStr != null && !offsetStr.isEmpty() )
		offset = Integer.parseInt( offsetStr );
	if ( sizeStr != null && !sizeStr.isEmpty() )
		size = Integer.parseInt( sizeStr );
	// Get start date
	Date start = null;
	if ( durationStr != null && !durationStr.isEmpty() ) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.add( Calendar.DATE, -1 * Integer.parseInt( durationStr ) );
		LocalDateUtil.resetTimeFields( cal );
		start = cal.getTime();
	}
	// Get the domain Id
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	// Get locale
	Locale locale = sUser.getLocale();
	String timezone = sUser.getTimezone();
	// Pagination
	// Get base URL
	String baseUrl = "viewrequestlogs.jsp?type=" + type;
	if ( durationStr != null && !durationStr.isEmpty() )
		baseUrl += "&duration=" + durationStr;
	// Initialize the navigator to help generate appropriate navigation URLs and HTML code
	Navigator navigator = new Navigator( request.getSession(), Constants.CURSOR_ALOGS, offset, size, baseUrl, 0 );
	PageParams pageParams = new PageParams( navigator.getCursor( offset ), offset-1, size );
	// Get all materials in domain 
	Results results = XLog.getRequestLogs( domainId, IALog.TYPE_BBOARD, start, pageParams );
	int numLogs = 0;
	List<IALog> logs = null;
	if ( results != null ) {
		logs = results.getResults();
		navigator.setResultParams( results );
	}
	if ( logs != null )
		numLogs = logs.size();
	// Get navigation HTML
	String navigationHTML = navigator.getNavigationHTML( locale );
	boolean showNavigator = ( numLogs > 0 || offset > 1 );
%>
<form name="logsForm" id="logsForm" class="sgForm">
<fmt:bundle basename="Messages">
<div id="doc3">
	<h2 class="sgHeading">
	<fmt:message key="bulletinboard.accesslog"/>
	</h2>
	<br/>
	<% if ( numLogs == 0 ) { %>
		<p>No items found.</p>
		<br/>
	<% } else { %>
	<div style="margin-bottom:10px;">
		<ul id="nmenu">
		  <li class="selected">Logs</li>
		</ul>
	</div>
	<% if ( showNavigator ) { %>
	<table id="kioskstable" width="100%">
	  <tr>
	  	<td style="text-align:right;">
	  		<%= navigationHTML %>
	  	</td>
	  </tr>
	</table>
	<% } %>
	<table id="kioskstable" width="100%">
	<tr>
		<th><fmt:message key="serialnum" /></th>
		<th><fmt:message key="date" /></th>
		<th><fmt:message key="user.ipaddress" /></th>
		<th><fmt:message key="useragent"/></th>
	</tr>
	<%
	Iterator<IALog> it = logs.iterator();
	int slNo = offset - 1;
	while ( it.hasNext() ) {
		IALog log = it.next();
		++slNo;
		%>
		<tr><td><%= slNo %></td>
		<td><%= LocalDateUtil.format( log.getTimestamp(), locale, timezone ) %></td>
		<td><%= log.getIPAddress() %></td>
		<td><%= log.getUserAgent() %></td>
		<%
	}
	%>
	</table>
	<% if ( showNavigator ) { %>
	<table id="kioskstable" width="100%">
	  <tr>
	  	<td style="text-align:right;">
	  		<%= navigationHTML %>
	  	</td>
	  </tr>
	</table>
	<%} %>
	<br/>
	<% } // end if ( numLogs == 0 ) %>
</div>
</fmt:bundle>
</form>
<jsp:include page="pagefooter.html" />