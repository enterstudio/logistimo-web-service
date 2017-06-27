<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.SecurityUtil" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.entities.entity.AccountsService"%>
<%@page import="com.logistimo.entities.service.AccountsServiceImpl" %>
<%@page import="com.logistimo.materials.service.MaterialCatalogService"%>
<%@page import="com.logistimo.materials.service.impl.MaterialCatalogServiceImpl"%>
<%@page import="com.logistimo.pagination.PageParams" %>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.services.Services" %>
<%@page import="org.lggi.samaanguru.entity.*" %>
<%@page import="java.util.List"%>
<%@page import="java.util.Locale"%>
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

<jsp:include page="../JSMessages.jsp" flush="true" />
<%
	AccountsService as;
	MaterialCatalogService mc;
	// Get the request parameters
	///String durationStr = request.getParameter("duration");
	String kioskIdStr = request.getParameter("kioskid");
	String materialIdStr = request.getParameter("materialid");
	String offsetStr = request.getParameter( "o" );
	String showDB = request.getParameter( "show" );
	String subview = request.getParameter( "subview" );

	///if ( durationStr == null || durationStr.isEmpty() )
	///	durationStr = Constants.FILTER_DAYS_DEFAULT;
	// Get the domain Id
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	String role = sUser.getRole();
	// Get locale
	Locale locale = sUser.getLocale();
	
	as = Services.getService(AccountsServiceImpl.class,locale);
	mc = Services.getService(MaterialCatalogServiceImpl.class,locale);
	
	// Get list of kiosks to present in the select box
	IUserAccount user = as.getUserAccount( userId );
	List<IKiosk> kiosks = as.getKiosks( user, domainId, null, new PageParams( null, 2 ) ).getResults(); // get at least 2 kiosk, if present, to check for singularity
	// Default kiosk, if only one
	if ( kiosks != null && kiosks.size() == 1 )
		kioskIdStr = kiosks.get(0).getKioskId().toString();
	
	boolean hasKioskId = ( kioskIdStr != null && !kioskIdStr.isEmpty() );
	IKiosk k = null;
	if ( hasKioskId )
		k = as.getKiosk( Long.valueOf( kioskIdStr ), false );
	// Flag to show the demand board
	boolean show = ( "true".equals( showDB ) || hasKioskId );
	boolean isOwner = ( SecurityUtil.compareRoles(role, IUserAccount.ROLE_DOMAINOWNER) >= 0 );
	if ( isOwner )
		show = true;
	// Show materials drop-down only if user is a domain owner or above
	boolean showMaterials = ( SecurityUtil.compareRoles(user.getRole(), IUserAccount.ROLE_DOMAINOWNER) >= 0 );
	IMaterial m = null;
	if ( materialIdStr != null && !materialIdStr.isEmpty() )
		m = mc.getMaterial( Long.valueOf( materialIdStr ) );
		
	String queryString = ""; /// "duration=" + durationStr;
	if ( kioskIdStr != null && !kioskIdStr.isEmpty() ) {
		queryString += "kioskid=" + kioskIdStr;
	}
	if ( materialIdStr != null && !materialIdStr.isEmpty() ) {
		if ( !queryString.isEmpty() )
			queryString += "&";
		queryString += "materialid=" + materialIdStr;
	}
	if ( offsetStr != null && !offsetStr.isEmpty() ) {
		if ( !queryString.isEmpty() )
			queryString += "&";
		queryString += "o=" + offsetStr;
	}
	
	String demandBoardGridUrl = "demandboardgrid.jsp?" + queryString;
	// Check if it is order mode or demand mode (e.g. show 'Add new demand' only in demand mode)
	DomainConfig dc = DomainConfig.getInstance( domainId );
	boolean autoOrderGen = dc.autoOrderGeneration();
%>
<!-- Autocomplete box -->
<link rel="stylesheet" href="../../autocomplete/TextboxList.css" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="../../autocomplete/TextboxList.Autocomplete.css" type="text/css" media="screen" charset="utf-8" />
<script src="../../js/jquery-1.3.2.min.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/GrowingInput.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.Autocomplete.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.Autocomplete.Binary.js" type="text/javascript" charset="utf-8"></script>
<style type="text/css">
	.sgForm input {border:0;}
</style>
<fmt:bundle basename="Messages">
<fmt:message key="add" var="add"/>
<fmt:message key="demand" var="demand"/>
<fmt:message key="get" var="get"/>
<fmt:message key="new" var="new"/>
<fmt:message key="lastdays" var="lastdays"/>
<fmt:message key="typetogetsuggestions" var="typetogetsuggestions" />
<fmt:message key="onlyoneitemcanbeselected" var="onlyoneitemcanbeselected" />
<script type="text/javascript">
function addOrders() {
	// Get the kioskId and name
	var kioskIdE = document.getElementById( "kioskid" );
	if ( kioskIdE.value == '' ) {
		alert( JSMessages.selectkioskmsg );
		return;
	}
	location.href = "orders.jsp?show=false&action=create<%= subview != null ? "&subview=" + subview : "" %>&kioskid=" + kioskIdE.value;
}

function initDropDowns() {
	<% if ( k == null ) { %>
	var placeholder = '${typetogetsuggestions}';
	<% } else { %>
	var placeholder = '${onlyoneitemcanbeselected}';	
	<% } %>
	// Init. kiosk selector
	var kioskid = new $.TextboxList('#kioskid', { unique: true,
								    max: 1,
								    bitsOptions: {
										 box: { deleteButton: false }
									 },
								    plugins: { 	autocomplete: 
												{ 	method:'binary',
										  			onlyFromValues: true,
										  			placeholder: placeholder,
										  			maxResults: 50,
										  			minLength: 1,
										  			queryRemote: true,
										  			remote: { url: '/s/list.jsp?type=kiosks' }
											  	}
  											}
								  } );
	  kioskid.clear();
	<% if ( k != null ) { %>
	kioskid.add( '<%= k.getName() %>', '<%= k.getKioskId() %>', null );
	<% } %>
	// Event when something is selected
	kioskid.addEvent( 'bitBoxAdd', function(b) {
									location.href = 'orders.jsp?subview=demandboard&kioskid=' + b.value[0];
									} );

	// Init. material selector
	<% if ( showMaterials ) { %>
	<% if ( m == null ) { %>
	var placeholder = '${typetogetsuggestions}';
	<% } else { %>
	var placeholder = '${onlyoneitemcanbeselected}';	
	<% } %>
	var materialid = new $.TextboxList('#materialid', { unique: true,
								    max: 1,
								    bitsOptions: {
										 box: { deleteButton: false }
									 },
								    plugins: { 	autocomplete: 
												{ 	method:'binary',
										  			onlyFromValues: true,
										  			placeholder: placeholder,
										  			maxResults: 50,
										  			minLength: 1,
										  			queryRemote: true,
										  			remote: { url: '/s/list.jsp?type=materials' }
											  	}
  											}
								  } );
	  materialid.clear();
	<% if ( m != null ) { %>
	materialid.add( '<%= m.getName() %>', '<%= m.getMaterialId() %>', null );
	<% } %>
	// Event when something is selected
	materialid.addEvent( 'bitBoxAdd', function(b) {
									location.href = 'orders.jsp?subview=demandboard&materialid=' + b.value[0];
									} );
	<% } // end if ( showMaterials ) %>
}

window.onload = function() {
	initDropDowns();
}
</script>
<div class="yui-g" style="border-bottom:1px solid #E6E6E6;">
	<form id="sgFilterByKiosk" name="getDemandForm" class="sgForm" action="orders.jsp">
	<table width="100%">
	<tr>
	<td>
			<fieldset>			
				<legend><fmt:message key="filterby"/> <fmt:message key="kiosk" />:</legend>
				<input type="text" name="kioskid" id="kioskid" />
				<% if ( !autoOrderGen ) { %>
				<button type="button" onclick="addOrders()">${add} ${new} ${demand}</button>
				<% } %>
				<input type="hidden" id="show" name="show" value="true"/>
				<input type="hidden" name="subview" value="<%= subview %>" />
			</fieldset>
	</td>
	<td>
		<% if ( showMaterials ) { %>
			<fieldset>			
			<legend><fmt:message key="filterby"/> <fmt:message key="material" />:</legend>
				<input type="text" name="materialid" id="materialid" />
			</fieldset>
		<% } %>
	</td>
	<td>
		<a href="orders.jsp?maps=true<%= subview != null ? "&subview=" + subview : "" %>"><img src="../../images/map.png" title="Map" valign="middle" align="right" width="30" height="30" alt="Map"/></a>
	</td>
	</tr>
	</table>
	</form>
</div>
</fmt:bundle>
<% if ( show ) { %>
<jsp:include page="<%= demandBoardGridUrl %>" flush="true" />
<% } %>