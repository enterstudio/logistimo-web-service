<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.config.entity.IConfig" %>
<%@page import="com.logistimo.config.service.ConfigurationMgmtService" %>
<%@ page import="com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl" %>
<%@ page import="com.logistimo.security.SecureUserDetails" %>
<%@ page import="com.logistimo.services.ObjectNotFoundException" %>
<%@ page import="com.logistimo.services.ServiceException" %>
<%@ page import="com.logistimo.services.Services" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Locale" %>


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

<jsp:include page="../pageheader.jsp" >
<jsp:param name="mode" value="manage" />
	</jsp:include>
<jsp:include page="menu_manage.jsp" >
<jsp:param name="view" value="system_configuration" />
	</jsp:include>
<script type="text/javascript" src="../../js/sg.js"></script>
<%
	// Get the type of coniguration
	String type = request.getParameter( "configtype" );

	// Get the configuration details, if type is specified
	ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
	IConfig c = null;
	String strDetails = "";
	String strError = null;
	boolean typeSelected = false;
	boolean hasKey = false;
	if ( type != null && !type.isEmpty() ) {
		typeSelected = true;
		try {
			c = cms.getConfiguration( type );
			hasKey = true;
			if ( c.getConfig() != null ) {
				strDetails = c.getConfig();
			}
		} catch ( ObjectNotFoundException e ) {
			// implies hasKey is false, which it is anyway - so do nothing
		} catch ( ServiceException e ) {
			strError = e.getMessage(); 
		}
	}
	
	// Get the current user's locale (esp. for date display)
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String curUserId = sUser.getUsername();
	Locale locale = sUser.getLocale();
	DateFormat df = DateFormat.getDateInstance( DateFormat.SHORT, locale );
	String strLastUpdated = null;
	if ( c != null && c.getLastUpdated() != null )
		strLastUpdated = df.format( c.getLastUpdated() );
%>
<div class="yui-g" style="border-bottom:1px solid #E6E6E6;">
<p><br></br><b>Select a configuration type and edit.</b><br></br>
<b>NOTE</b>: Please edit this ONLY if you are familiar with it. Please ensure that existing configuration is not lost.</p>
</div>
<div id="doc3">
	<form name="addmaterial" method="post" action="/s/createentity" class="sgForm">
	<table id="addresource" width="100%">
	<tr>
		<th class="mandatory">Configuration type</th>
		<td>
			<select name="configtype" onchange="location='system_configuration.jsp?configtype=' + this.options[this.selectedIndex].value;">
				<option value="" <%= ( type == null || type.isEmpty() )  ? "selected" : "" %>>-- Select type --</option>
				<option value="<%= IConfig.CURRENCIES %>" <%= IConfig.CURRENCIES.equals( type ) ? "selected" : "" %>><%= IConfig.CURRENCIES %></option>
				<option value="<%= IConfig.LANGUAGES %>" <%= IConfig.LANGUAGES.equals( type ) ? "selected" : "" %>><%= IConfig.LANGUAGES %></option>
				<option value="<%= IConfig.LANGUAGES_MOBILE %>" <%= IConfig.LANGUAGES_MOBILE.equals( type ) ? "selected" : "" %>><%= IConfig.LANGUAGES_MOBILE %></option>
				<option value="<%= IConfig.LOCATIONS %>" <%= IConfig.LOCATIONS.equals( type ) ? "selected" : "" %>><%= IConfig.LOCATIONS %></option>
				<option value="<%= IConfig.OPTIMIZATION %>" <%= IConfig.OPTIMIZATION.equals( type ) ? "selected" : "" %>><%= IConfig.OPTIMIZATION %></option>
				<option value="<%= IConfig.REPORTS %>" <%= IConfig.REPORTS.equals( type ) ? "selected" : "" %>><%= IConfig.REPORTS %></option>
				<option value="<%= IConfig.SMSCONFIG %>" <%= IConfig.SMSCONFIG.equals( type ) ? "selected" : "" %>><%= IConfig.SMSCONFIG %></option>
				<!-- <option value="<%= IConfig.PAYMENTPROVIDERSCONFIG %>" <%= IConfig.PAYMENTPROVIDERSCONFIG.equals( type ) ? "selected" : "" %>><%= IConfig.PAYMENTPROVIDERSCONFIG %></option> -->
				<option value="<%= IConfig.ASSETSYSTEMCONFIG %>" <%= IConfig.ASSETSYSTEMCONFIG.equals( type ) ? "selected" : "" %>><%= IConfig.ASSETSYSTEMCONFIG %></option>
                <option value="<%= IConfig.MAPLOCATIONCONFIG %>" <%= IConfig.MAPLOCATIONCONFIG.equals( type ) ? "selected" : "" %>><%= IConfig.MAPLOCATIONCONFIG %></option>
				<option value="<%= IConfig.GENERALCONFIG %>" <%= IConfig.GENERALCONFIG.equals( type ) ? "selected" : "" %>><%= IConfig.GENERALCONFIG %></option>
				<option value="<%= IConfig.DASHBOARDCONFIG %>" <%= IConfig.DASHBOARDCONFIG.equals( type ) ? "selected" : "" %>><%= IConfig.DASHBOARDCONFIG %></option>
			</select>
		</td>
	</tr>
	<% if ( typeSelected ) {  %>
	<tr>
		<th class="mandatory">Details</th>
		<td><% if ( strError == null ) { // no error, so show the text area with details, if any
				if ( c != null ) { %>
				   <p><i>Last edited <%= strLastUpdated != null ? "on <b>" + strLastUpdated + "</b>" : "" %> by <b><%= c.getUserId() %></b></i></p>
				<% } %>
				<textarea name="details" rows="20" cols="100"><%= strDetails %></textarea>
			<% } else { %>
				<%= strError %>
			<% } %>
		</td>
	</tr>
	<% } %>
	</table>
	<br></br>
	<input type="hidden" name="action" value="<%= hasKey ? "modify" : "create" %>" />
	<input type="hidden" name="type" value="system_configuration" />
	<input type="hidden" name="userid" value="<%= curUserId %>" />
	<% if ( typeSelected ) { %>
	<input type="submit" class="submit" value="Save" />
	<% } %>
	</form>
</div>