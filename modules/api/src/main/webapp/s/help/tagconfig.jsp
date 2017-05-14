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
	<h2 class="sgHeading"><fmt:message key="tagconfig"/> - <fmt:message key="help"/></h2>
	<br/>
	<fmt:message key="tagconfig.overview"/>
	<br/><br/>
	<b><fmt:message key="tagconfig.specifyingtags"/></b>
	<br/>
	<fmt:message key="tagconfig.specifyingtags.overview"/>
	<br/><br/>
	<b><fmt:message key="tagconfig.enforcingtags"/></b>
	<br/>
	<fmt:message key="tagconfig.enforcingtags.overview"/>
	<br/><br/>
	<div class="sgForm">
		<button name="close" onclick="window.close()"><fmt:message key="close"/></button>
	</div>
</div>
</fmt:bundle>
	