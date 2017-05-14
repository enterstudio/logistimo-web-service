<%@page contentType="text/html; charset=UTF-8" language="java" %>
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
	// Get source paramters
	String lat = request.getParameter( "lat" );
	String lng = request.getParameter( "lng" );
	String title = request.getParameter( "title" );
	// Get desitination parameters, if any
	String dlat = request.getParameter( "dlat" );
	String dlng = request.getParameter( "dlng" );
	String dtitle = request.getParameter( "dtitle" );
	// Get distance, if specified
	String distance = request.getParameter( "distance" );
	if ( distance != null && !distance.isEmpty() )
		distance = String.valueOf( Math.round( Double.parseDouble( distance ) ) );
	// Get the command
	String cmd = request.getParameter( "cmd" );
	boolean isDistance = ( "distance".equals( cmd ) );
	// Check if destination exists
	boolean hasDestination = ( dlat != null && !dlat.isEmpty() && dlng != null && !dlng.isEmpty() );
	// Get the tab URLs
	String urlRegular = "showmappoint.jsp?lat=" + lat + "&lng=" + lng;
	if ( title != null && !title.isEmpty() )
		urlRegular += "&title=" + title;
	if ( hasDestination ) {
		urlRegular += "&dlat=" + dlat + "&dlng=" + dlng;
		if ( dtitle != null && !dtitle.isEmpty() )
			urlRegular += "&dtitle=" + dtitle;
		if ( distance != null && !distance.isEmpty() )
			urlRegular += "&distance=" + distance;
	}
	String urlDistance = urlRegular + "&cmd=distance";
	// Map URL
	String mapUrlRegular = "https://maps.google.com/maps?output=embed&q=" + lat + "," + lng;
	if ( title != null && !title.isEmpty() )
		mapUrlRegular += "+(" + title + ")";
	String mapUrlDistance = "https://maps.google.com/maps?output=embed&iwloc=B&saddr=" + lat + "," + lng;
	if ( title != null && !title.isEmpty() )
		mapUrlDistance += "+(" + title + ")";
	mapUrlDistance += "&daddr=" + dlat + "," + dlng;
	if ( dtitle != null && !dtitle.isEmpty() )
		mapUrlDistance += "+(" + dtitle + ")";
	// NOTE: Temporarily disabling showing the destination, given its logic has to be worked out (otherwise erroneous) - 16/1/13
	hasDestination = false;
%>
<html>
	<head>
		<meta name="viewport" content="initial-scale=1.0, user-scalable=yes" /> <!--  This is placed to enable Google Maps in certain views -->
		<meta http-equiv="content-type" content="text/html; charset=utf-8">
		<link href="/css/reset-fonts-grids.css" rel="stylesheet" type="text/css" />
		<link href="/css/tabview.css" rel="stylesheet" type="text/css" />
		<link href="/css/sg.css" rel="stylesheet" type="text/css" />
	</head>
	<body>
	<fmt:bundle basename="Messages">
		<div id="doc3" style="margin-bottom:10px;margin-top:10px;">
		<ul id="nmenu">
		  <li class="<%= !isDistance ? "selected" : "" %>"><a href="<%= urlRegular %>"><%= title == null || title.isEmpty() ? "Unknown" : title %></a></li>
		  <% if ( hasDestination ) { %>
		  <li class="<%= isDistance ? "selected" : "" %>"><a href="<%= urlDistance %>"><%= distance != null ? distance + " km" : "" %> <fmt:message key="from"/> <%= dtitle != null ? dtitle : "" %></a></li>
		  <% } %>
		</ul>
		<% if ( isDistance ) { %>
			<div id="kioskstable" style="margin-top:5px">
			<b><fmt:message key="order"/> @ A</b> - <%= title %> &nbsp;&nbsp;&nbsp;&nbsp; <b><fmt:message key="customer"/> @ B</b> - <%= dtitle %> &nbsp; (<%= distance %> km)
			</div>
		<% } %>
		</div>
		<iframe id="mappanel" width="98%" height="92%" src="<%= !isDistance ? mapUrlRegular : mapUrlDistance %>">
		Your browser does not support iFrames.
		</iframe>
	</fmt:bundle>
	</body>
</html>
