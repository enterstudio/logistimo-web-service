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

<fmt:bundle basename="HelpMessages">
<div id="doc3">
	<h2 class="sgHeading"><fmt:message key="user.roles"/> - <fmt:message key="help"/></h2>
	<br/>
	<fmt:message key="user.roles.overview"/>:
	<br/><br/>
	<ul>
		<li><b><fmt:message key="user.roles.kioskowner"/></b>: <fmt:message key="user.roles.kioskowner.overview"/></li>
		<li><b><fmt:message key="user.roles.servicemanager"/></b>: <fmt:message key="user.roles.servicemanager.overview"/></li>
		<li><b><fmt:message key="user.roles.domainowner"/></b>: <fmt:message key="user.roles.domainowner.overview"/></li>
	</ul>
	<br/>
	<div class="sgForm">
		<button name="close" onclick="window.close()"><fmt:message key="close"/></button>
	</div>
</div>
</fmt:bundle>
	