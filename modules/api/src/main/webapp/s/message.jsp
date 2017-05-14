<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.logger.XLog"%>

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
	String message = (String) request.getAttribute( "message" );
	String view = (String) request.getAttribute( "view" );
	String subview = (String) request.getAttribute( "subview" );
	String mode = (String) request.getAttribute( "mode" );
	boolean nomenu = ( request.getAttribute( "nomenu" ) != null );
	// Form the menu page URL
	String menuPageUrl = "menu.jsp?";
	if ( "manage".equals( mode ) )
		menuPageUrl = "admin/menu_manage.jsp?";
	menuPageUrl += "view=" + view;
	if ( subview != null && !subview.isEmpty() )
		menuPageUrl += "&subview=" + subview;
	String pageHeaderUrl = "pageheader.jsp";
	if ( mode != null && !mode.isEmpty() )
		pageHeaderUrl += "?mode=manage";
%>
<jsp:include page="<%= pageHeaderUrl %>" />
<% if ( !nomenu ) { %>
<jsp:include page="<%= menuPageUrl %>" />
<% } %>
<div class="yui-g" style="border-bottom:1px solid #E6E6E6;">
	<div align="left">
		<br>
		<%= message %>
		<br>
	</div>
</div>
<jsp:include page="pagefooter.html" />