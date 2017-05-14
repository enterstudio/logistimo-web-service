<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.services.Services"%>
<%@page import="com.logistimo.services.ServiceException"%>
<%@page import="com.logistimo.entities.entity.IKiosk"%>
<%@page import="com.logistimo.constants.Constants"%>
<%@page import="com.logistimo.api.security.SecurityMgr"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.api.util.SessionMgr"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.logistimo.pagination.Results"%>
<%@ page import="com.logistimo.users.service.UsersService" %>
<%@ page import="com.logistimo.users.service.impl.UsersServiceImpl" %>
<%@ page import="com.logistimo.auth.SecurityConstants" %>
<%@ page import="com.logistimo.users.entity.IUserAccount" %>

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

<div class="yui-g" style="border-bottom:1px solid #E6E6E6;">
<h2 class="addresourceheader">Switch Domain</h2>
</div>
<%
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	UsersService as = Services.getService(UsersServiceImpl.class);
	
	// Get the domain Id
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
%>
<div id="doc3">
	<form name="adddomain" method="post" action="/s/createentity" class="sgForm">
	<table id="addresource" width="100%">
	<tr><th class="mandatory">Domain Name</th><td><input type="text" name="name"/></td></tr>
	<tr>
		<th class="mandatory">Domain Owner</th>
		<td>
			<select name="ownerid">
			<%
				Results results = as.getUsers(domainId, SecurityConstants.ROLE_SUPERUSER,true,null,null); // no pagination required, given few super users
				List<IUserAccount> userAccounts = results.getResults();
				for (IUserAccount u : userAccounts) {
					String userID = u.getUserId();
					String userName = u.getFirstName()+" "+u.getLastName();
					out.println("<option value=\""+userID+"\">"+userName+"</option>");
				}
			%>
			</select>
		</td>
	</tr>
	<tr><th>Description</th><td><input type="text" name="description"/></td></tr>
	<tr><td colspan="2">&nbsp;</td></tr>
	</table>
	<input type="hidden" name="action" value="create" />
	<input type="hidden" name="type" value="domain" />
	<input type="hidden" name="userid" value="<%= userId %>" />
	<input type="submit" class="submit" value="Save" />
	</form>
</div>