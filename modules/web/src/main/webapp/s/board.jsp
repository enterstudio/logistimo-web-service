<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.config.models.BBoardConfig"%>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.constants.Constants"%>
<%@page import="com.logistimo.dao.JDOUtils" %>
<%@page import="com.logistimo.domains.entity.IDomain" %>
<%@page import="com.logistimo.domains.service.DomainsService" %>
<%@page import="com.logistimo.domains.service.impl.DomainsServiceImpl" %>
<%@page import="com.logistimo.entity.IALog" %>
<%@page import="com.logistimo.entity.IBBoard" %>
<%@page import="com.logistimo.events.handlers.BBHandler" %>
<%@page import="com.logistimo.logger.XLog"%>
<%@page import="com.logistimo.pagination.PageParams" %>
<%@page import="com.logistimo.security.SecureUserDetails" %>
<%@ page import="com.logistimo.services.Services" %>
<%@ page import="com.logistimo.services.utils.ConfigUtil" %>
<%@ page import="com.logistimo.utils.LocalDateUtil" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
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

<fmt:bundle basename="Messages">
<%
	Long domainId = null;
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String errMsg = null;
	try {
		domainId = Long.valueOf( request.getParameter( "domainid" ) );
	} catch ( Exception e ) {
		errMsg = e.getClass().getName() + ": " + e.getMessage();
	}
	if ( domainId == null )
		errMsg = "System error: could not identify domain.";
	BBoardConfig bbc = null;
	Locale locale = null;
	String timezone = null;
	String pageHeader = null;
	String domainName = null;
	if ( domainId != null ) {
		try{
			DomainsService ds = Services.getService( DomainsServiceImpl.class );
			IDomain d = ds.getDomain(domainId);
			if(d != null) {
				domainName = d.getName();
			}
		}catch (Exception e) {
			//Log
		}
		DomainConfig dc = DomainConfig.getInstance( domainId );
		pageHeader = dc.getPageHeader();
		bbc = dc.getBBoardConfig();
		locale = dc.getLocale();
		if ( locale == null )
			locale = new Locale( Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT );
		timezone = dc.getTimezone();
		if ( timezone == null )
			timezone = Constants.TIMEZONE_DEFAULT;
	}
	if ( bbc != null && !bbc.isEnabled() )
		errMsg = "Sorry! Bulletin Board is not enabled. Please contact your system administrator";
	else if ( bbc != null && ( bbc.getEnabled() == BBoardConfig.PRIVATE ) && sUser == null )
		errMsg = "Sorry! You are not authorized to access this bulletin board. Please try this to access this <a href=\"/s/board?domainid=" + domainId + "\">secured board</a>.";
	List<IBBoard> items = null;
	boolean hasItems = false;
	int size = 0;
	if ( errMsg == null ) {
		PageParams pageParams = new PageParams( null, bbc.getMaxItems() );
		items = BBHandler.getItems( domainId, LocalDateUtil.getOffsetDate( new Date(), -1 * bbc.getDataDuration(), Calendar.DATE ), pageParams ).getResults();
		hasItems = ( items != null && !items.isEmpty() );
		// Check validity of items, and remove invalid items if necessary
		if ( hasItems )
			BBHandler.removeInvalidItems( items );
		hasItems = ( items != null && !items.isEmpty() );
		if ( hasItems )
			size = items.size();
	}
	if ( errMsg == null && !hasItems )
		errMsg = "Nothing to show at this time.";
	boolean slide = ( hasItems && items.size() > 3 );
	// Create an access log for the bulletin board
    boolean isGAE = ConfigUtil.getBoolean("gae.deployment", true);
	XLog.logRequest( JDOUtils.createInstance(IALog.class).init( domainId, IALog.TYPE_BBOARD, isGAE ? request.getRemoteAddr() : request.getHeader("X-REAL-IP"), request.getHeader( "User-Agent" ) ) );
%>
<!DOCTYPE html>
<html>
<head>
	<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
	<meta http-equiv="content-type" content="text/html; charset=utf-8">
	<meta http-equiv="refresh" content="<%= bbc != null ? bbc.getRefreshDuration() : "60" %>">
	<title><fmt:message key="bulletinboard"/></title>
	<script type="text/javascript" src="/js/jquery-1.3.2.min.js"></script>
	<script type="text/javascript" src="/js/jquery.easing.1.3.js"></script>
	<script type="text/javascript" src="/js/jquery.CarouSlide.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../v2/css/3rdparty/glyphicons-1.9.2.1.css">

	<!-- JQuery UI css and scripts -->
	<script type="text/javascript" src="/jqueryui/js/jquery-ui-1.8.22.custom.min.js"></script>

	<script type="text/javascript">
		$(document).ready(function(){
			$(".board").CarouSlide({
				animTime: 800,
				animType: 'slide',
				animInfinity:true,
				showSlideNav:false,
				autoAnim:<%= slide %>,
//				transAnimCallback: location.reload(true)
				//easingStyle: 'easeOutCirc',
			});
		});

		function startTime() {

			var today = new Date();
			var h = today.getHours();
			var m = today.getMinutes();
			var s = today.getSeconds();
			var n = today.toDateString();
			var dt = today.getDate();
			var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
			var mon = monthNames[today.getMonth()];
			var days= ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];
			var day = days[today.getDay()];


			m = checkTime(m);
			s = checkTime(s);

			document.getElementById('txt').innerHTML = day + ",  " + dt + " " + mon + " " + h + ":" + m;
			setTimeout(startTime, 500);
		}
		function checkTime(i) {
			if (i < 10) {i = "0" + i};  // add zero in front of numbers < 10
			return i;
		}


	</script>
	<link href='https://fonts.googleapis.com/css?family=Sanchez:400' rel='stylesheet' type='text/css' />
	<link rel="stylesheet" type="text/css" href="/css/board.css" />
</head>
<body onload="startTime()">
	<div class="banner">
		<% if ( pageHeader != null && !pageHeader.isEmpty() ) { %>
			<div class="imagewrapper"><%= pageHeader %></div>
		<% } else { %>
		<div style="background-color:white">
            <img class="imagewrapper" src="/images/acquia_marina_logo.png"/>
		</div>
		<% } %>
		<% if ( size > 0 ) { %>
		<div id="txt" class="nav"></div>
		<% } %>
	</div>
	<% if ( errMsg != null ) { %>
		<br/>&nbsp;<font style="color:white;font-weight:bold;font-size:16px;"><%= errMsg %></font>
	<% } else { %>
	<div class="domainName"> <%= domainName %></div>
	<div class="board">
	  <ul class="slider-holder">
	  	<%
		Iterator<IBBoard> it = items.iterator();
	  	int i = 0;
		while ( it.hasNext() ) {
			IBBoard item = it.next();
			++i;
			String objectType = item.getType();
			Integer eventId = item.getEventId();

			String clazz = "";
			if ( objectType != null )
				clazz = objectType.replaceAll( "\\.", "_" );
			if ( eventId != null ) {
				if ( !clazz.isEmpty() )
					clazz += " ";
				clazz += "_" + String.valueOf( eventId ); // event Id is used as the class name
			}
		%>
		  <li id="s<%= i %>">
			<ul id="notes">
			    <li class="<%= clazz %>">
					<span class="number"><%= i %></span>
					<div class="date"><%= LocalDateUtil.format( item.getTimestamp(), locale, timezone ) %></div>
					<%--<span style="color: #008800" class="glyphicons glyphicons-thumbs-up"></span>--%>
					<p><%= item.getMessage() %></p>
			    </li>
			</ul>
		</li>
	<% } // end while %>
	</ul>
	<ul class="slider-nav">
	<%
		for ( int j = 1; j <= size; j++ )  { %>
			<li><a href="#s<%= j %>"><%= j %></a></li>
	 <% } // end for 
	%>
	</ul>
		<div class="items"><b><%= size %></b> <fmt:message key="items"/></div>

	</div>
	<% } // end if ( errMsg == null ) %>

</body>
</html>
</fmt:bundle>