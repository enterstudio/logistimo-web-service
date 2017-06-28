<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.constants.Constants"%>
<%@page import="com.logistimo.domains.entity.IDomain" %>
<%@page import="com.logistimo.domains.service.DomainsService" %>
<%@page import="com.logistimo.domains.service.impl.DomainsServiceImpl" %>
<%@page import="com.logistimo.pagination.Navigator"%>
<%@page import="com.logistimo.pagination.PageParams" %>
<%@page import="com.logistimo.pagination.Results"%>
<%@page import="com.logistimo.security.SecureUserDetails" %>
<%@page import="com.logistimo.services.ServiceException" %>
<%@page import="com.logistimo.services.Services" %>
<%@page import="com.logistimo.users.service.UsersService" %>
<%@page import="com.logistimo.users.service.impl.UsersServiceImpl" %>
<%@ page import="java.util.List" %>
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
<jsp:param name="view" value="domains" />
	</jsp:include>
<script type="text/javascript" src="../../js/sg.js"></script>
<%
	// Get request parameters
	String offsetStr = request.getParameter( "o" );
	String sizeStr = request.getParameter( "s" );
	// Get the user ID
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();	
	Locale locale = sUser.getLocale();
	String role = sUser.getRole();
	// Get the current domain ID, if there is one
	Long curDomainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	// Determine if 'remove domains' button is to be shown
	String removeDisabled = ( "arun".equals( userId ) || "anup".equals( userId ) ? "" : "disabled" );

	// Get the desired subview
	String subview = request.getParameter( "subview" );
	if ( subview == null || subview.isEmpty() )
			subview = "dashboard";

	// Pagination
	int offset = 1;
	int size = PageParams.DEFAULT_SIZE;
	// Get pagination parameters
	if ( offsetStr != null && !offsetStr.isEmpty() )
		offset = Integer.parseInt( offsetStr );
	if ( sizeStr != null && !sizeStr.isEmpty() )
		size = Integer.parseInt( sizeStr );
	// Initialize the navigator to help generate appropriate navigation URLs and HTML code
	String url = "domains.jsp?subview=domains";
	Navigator navigator = new Navigator( request.getSession(), Constants.CURSOR_DOMAINS, offset, size, url, 0 );
	PageParams pageParams = new PageParams( navigator.getCursor( offset ), offset-1,size );
	List<IDomain> domains = null;
	int numResults = 0;
	String errMsg = null;
	DomainsService as = null;
	UsersService us = null;
	try {
		as = Services.getService(DomainsServiceImpl.class);
		us = Services.getService(UsersServiceImpl.class);
		Results results = as.getAllDomains( pageParams );
		// Update the navigator with new result params - esp. cursor and result size
		navigator.setResultParams( results );
		domains = results.getResults();
		if ( domains != null )
			numResults = domains.size();
	} catch ( Exception e ) {
		errMsg = e.getMessage();
	}
	// Get the navigation HTML
	String navigationHTML = navigator.getNavigationHTML( locale );
	boolean showNavigator = ( numResults > 0 || offset > 1 );
%>
<% if ( errMsg != null ) { %>
	<%= errMsg %>
<% } else { %>
<script type="text/javascript">

var domainNameMap = {
		<% for ( IDomain d : domains ) { %>
		"<%= d.getId() %>" : "<%= d.getName() %>", 
		<% } %>
};

function switchDomain() {
	// Get the user Id
	var userId = document.getElementById( "userid" );

	// Get the domain Id to switch to
	var checkBoxes = document.getElementsByTagName("input");
	var domainIdStr = getNumericEntityIDs();
	if ( domainIdStr == '' ) {
		alert( "Please select a domain to switch to." );
		return;
	}
	var domainIdsArr = domainIdStr.split( "," );	
	if ( domainIdsArr.length > 1 ) {
		alert( "Please select only one domain to switch." );
		return;
	}
	window.location.href="/s/createentity?action=switch&type=domain&view=domains&userid=" + userId.value + "&domainid=" + domainIdStr;
}

function createDomain() {
	window.location.href="adddomain.jsp";
}

function removeDomains() {
	// Get the domain Ids to remove
	var dID = getNumericEntityIDs();
	
	if ( dID == '' ) {
		alert( "Please select at least one item to remove." );
		return;
	}

	if ( dID.split(',').length >  1 ) {
		alert( "Please select only one domain at a time to delete" );
		return;
	}

	var name = domainNameMap[ dID ];
	
	// Ask for confirmation from user
	if ( !confirm( 'The domain "' + name + '" and ALL transactions associated with it will be deleted. Are you certain you wish to delete domain "' + name + '"?' ) ) {
		return;
	}
	
	// Get the userId of user who initiated this
	var userId = document.getElementById( "userid" );

	window.location.href="/s/createentity?action=remove&type=domain&userid=" + userId.value + "&domainids=" + dID;
}
</script>
<div id="sgSubMenu">
	<p>
	 <a class="<%= "dashboard".equalsIgnoreCase( subview ) ? "selected" : "" %>" href="domains.jsp?subview=dashboard">Dashboard</a>
	 <a class="<%= "domains".equalsIgnoreCase( subview ) ? "selected" : "" %>" href="domains.jsp?subview=domains">Domains</a>
	</p>
</div>
<% if ( subview.equals( "dashboard" ) ) { %>
	<jsp:include page="dashboard.jsp?subview=dashboard" />
<% } %>
<% if ( subview.equals( "domains" ) ) { %>
<div class="yui-g">
	<div class="yui-u first">
		<form id="domains" class="sgForm" action="/s/createentity">
				<button type="button" onclick="switchDomain()">Switch to domain</button>
				<button type="button" onclick="createDomain()">Create domain</button>
				<button type="button" onclick="removeDomains()" <%= removeDisabled %>>Remove domain(s)</button>
				<!--  <button type="button" id="goback">Go Back</button>  -->
				<input type="hidden" id="userid" name="userid" value="<%= userId %>">
		</form>
	</div>
	<div class="yui-u">
	</div>
</div>
<div id="doc3">
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
		<th><input type="checkbox"></th>
		<th>Domain</th>
		<th>Description</th>
		<th>Owner</th>
		<th>Is active?</th>
		<th>Created On</th>
	</tr>
	<%
	// Print Materials
	if ( numResults > 0 ) {
		int styleCounter = 0;
		for (IDomain d : domains) {
			String tdclass="";
			if (styleCounter %2 ==0) {
				tdclass="oddrow";
			}
			String active = "yes";
			if (!d.isActive()) {
				active = "no";
			}
			// Get the owner name
			String ownerId = d.getOwnerId();
			String ownerName = "";
			if ( Constants.ADMINID_DEFAULT.equalsIgnoreCase( ownerId ) )
				ownerName = Constants.ADMINNAME_DEFAULT;
			else {
				try {
					ownerName = us.getUserAccount( ownerId ).getFullName();
				} catch ( ServiceException e ) {
					ownerName = "[Unknown]";
				}
			}
			
			// If it is the current domain, then disable the checkbox
			String disabled = "";
			boolean isCurrentDomain = false;
			if ( d.getId().equals( curDomainId ) ) {
				isCurrentDomain = true;
				disabled = " disabled ";
			}
			out.println("<tr>" +
			"<td class=\"" + tdclass+"\"><input class=\"eachkiosk\" type=\"checkbox\" id=\"" + d.getId() + "\"" + disabled + "/></td>" + 
			"<td class=\"" + tdclass + "\">" + d.getName() + ( isCurrentDomain ? " <b>[current]</b>" : "" ) + "</td>" + 
			"<td class=\"" + tdclass + "\">" + ( d.getDescription() == null ? "" : d.getDescription() ) + "</td>" +
			"<td class=\"" + tdclass + "\">" + ownerName + "</td>" + 
			"<td class=\"" + tdclass + "\">" + active + "</td>" + 
			"<td class=\"" + tdclass + "\">" + d.getCreatedOn() + "</td>" + 
		"</tr>");
			styleCounter++;
		}
	}
	else {
		out.println("<tr><td colspan=\"6\">No domains configured.</td></tr>");
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
	<% } %>
</div>
<% } // end if ( errMsg != null ) %>
<jsp:include page="../pagefooter.html" /><%} // end if ( subview=="domains" ) %>
