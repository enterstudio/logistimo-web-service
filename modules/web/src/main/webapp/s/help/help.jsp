<%@ page contentType="text/html; charset=UTF-8" language="java" %>
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
	String type = request.getParameter( "type" );
	String url = "/s/help/" + type + ".jsp";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
	<meta name="viewport" content="initial-scale=1.0, user-scalable=no" /> <!--  This is placed to enable Google Maps in certain views -->
	<meta http-equiv="content-type" content="text/html; charset=utf-8">
	<title>Logistimo</title>
	<link href="../../css/reset-fonts-grids.css" rel="stylesheet" type="text/css" />
	<link href="../../css/tabview.css" rel="stylesheet" type="text/css" />
	<link href="../../css/sg.css" rel="stylesheet" type="text/css" />
	</head>
	<body class="yui-skin-sam">
	<div id="doc">
		<div id="hd">
			<div>
				<img src="../../images/logo.png"/>
			</div>
		</div>
		<jsp:include page="<%= url %>" flush="true" />
	</div>
	
	</body>
</html>
	