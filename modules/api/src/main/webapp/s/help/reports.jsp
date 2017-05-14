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
	<h2 class="sgHeading"><fmt:message key="reports"/> - <fmt:message key="help"/></h2>
	<br/>
	<fmt:message key="reports.overview"/>
	<br/><br/>
	<b><fmt:message key="reports.viewing.title"/></b>
	<br/>
	<fmt:message key="reports.viewing.overview"/>
	<br/><br/>
	<b><fmt:message key="note"/></b>: <fmt:message key="reports.viewing.note"/>
	<br/><br/>
	<b><fmt:message key="filters"/></b>
	<br/>
	<fmt:message key="reports.filters.overview"/>
	<br/><br/>
	<b><fmt:message key="reports.trends"/></b>
	<br/>
	<fmt:message key="reports.trends.overview"/>
	<br/><br/>
	<b><fmt:message key="reports.exporting"/></b>
	<br/>
	<fmt:message key="reports.exporting.overview" />
	<br/><br/>
	<b><fmt:message key="reports.specific"/></b>
	<br/>
	<ul>
	<li>
	<i><fmt:message key="reports.consumptiontrends"/></i>: <fmt:message key="reports.consumptiontrends.overview"/>
	</li> 
	<li>
	<i><fmt:message key="reports.transactioncounts"/></i>: <fmt:message key="reports.transactioncounts.overview" />
	</li>
	</ul>
	<br/><br/>
	<div class="sgForm">
		<button name="close" onclick="window.close()"><fmt:message key="close"/></button>
	</div>
</div>
</fmt:bundle>
	