<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.logistimo.logger.XLog"%>
<%@page import="com.logistimo.api.security.SecurityMgr"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.constants.Constants"%>
<%@page import="com.logistimo.api.util.SessionMgr"%>
<%@page import="com.logistimo.tags.TagUtil"%>
<%@page import="com.logistimo.pagination.PageParams"%>
<%@page import="com.logistimo.pagination.Results"%>
<%@page import="com.logistimo.reports.generators.ReportData"%>
<%@page import="com.logistimo.services.Services"%>
<%@page import="com.logistimo.orders.service.OrderManagementService"%>
<%@page import="com.logistimo.services.ServiceException"%>
<%@page import="com.logistimo.orders.service.impl.OrderManagementServiceImpl"%>
<%@page import="com.logistimo.entities.entity.AccountsService"%>
<%@page import="com.logistimo.entities.service.AccountsServiceImpl"%>
<%@page import="com.logistimo.materials.service.MaterialCatalogService"%>
<%@page import="com.logistimo.materials.service.impl.MaterialCatalogServiceImpl"%>
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
	XLog xLogger = XLog.getLog("demandboardgrid");
	String errMsg = null;
	
	// Get the logged in user Id
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	// Get the domain ID from the session
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	//Get user locale & timezone
	Locale locale = sUser.getLocale();
	String timezone = sUser.getTimezone();
	
	///String durationStr = request.getParameter("duration");
	String kioskIdStr = request.getParameter("kioskid");
	String materialIdStr = request.getParameter("materialid");
	String kioskName = request.getParameter( "kioskname" );
	String materialName = request.getParameter( "materialname" );
	String offsetStr = request.getParameter( "o" );
	String sizeStr = request.getParameter( "s" );
	String subview = request.getParameter( "subview" );
	String tag = request.getParameter( "tag" );
	String tagType = request.getParameter( "tagtype" );
	
	// Get tag
	boolean hasTag = ( tag != null && !tag.isEmpty() );
	boolean hasMaterialTag = ( hasTag && TagUtil.TYPE_MATERIAL.equals( tagType ) );
	boolean hasKioskTag = ( hasTag && TagUtil.TYPE_ENTITY.equals( tagType ) );
	if ( hasTag )
		tag = URLDecoder.decode( tag, "UTF-8" );
	else
		tag = null;
	
	// Get the input parameters in non-string forms
	Long kioskId = null;
	Long materialId = null;
	
	if ( kioskIdStr != null && !kioskIdStr.isEmpty() )
		kioskId = Long.valueOf( kioskIdStr );
	if ( materialIdStr != null && !materialIdStr.isEmpty() )
		materialId = Long.valueOf( materialIdStr );
	
	// Ensure only one of kiosk or material is passed
	if ( kioskId != null && materialId != null )
		errMsg = "Both kiosk and material cannot be selected. Please select one of kiosk or material and retry.";
	
	// Get kiosk name
	if ( kioskId != null && ( kioskName == null || kioskName.isEmpty() ) ) {
		try {
			AccountsService as = Services.getService( AccountsServiceImpl.class );
			kioskName = as.getKiosk( kioskId, false ).getName();
		} catch ( Exception e ) {
			System.out.println( "Exception when trying to get kiosk with ID " + kioskId );
		}
	}
	// Get material name, if not present
	if ( materialId != null && ( materialName == null || materialName.isEmpty() ) ) {
		try {
			MaterialCatalogService mcs = Services.getService( MaterialCatalogServiceImpl.class );
			materialName = mcs.getMaterial( materialId ).getName();
		} catch ( Exception e ) {
			System.out.println( "Exception when trying to get kiosk with ID " + kioskId );
		}
	}
	
	//Offset for pagination
	int offset = 1;
	if ( offsetStr != null && !offsetStr.isEmpty() ) {
		try {
			offset = Integer.parseInt( offsetStr );
		} catch ( NumberFormatException e ) {
			// do nothing; offset remains as default
		}
	}
	// Size for pagination
	int size = PageParams.DEFAULT_SIZE;
	if ( sizeStr != null && !sizeStr.isEmpty() ) {
		try {
			size = Integer.parseInt( sizeStr );
		} catch ( NumberFormatException e ) {
			// do nothing; offset remains as default
		}
	}
	// Previous offset of pagination
	int prevOffset = offset - size;
	
	// Form the base URL
	String baseUrl = "orders.jsp?show=true" + ( kioskId != null ? "&kioskid=" + kioskId : "" ) +
				     ( materialId != null ? "&materialid=" + materialId : "" ) +
				     ( subview != null && !subview.isEmpty() ? "&subview=" + subview : "" );

	// Get the next offset
	int nextOffset = offset + size;
	// Get the maxRange for display purposes
	int maxRange = nextOffset - 1;
	// Flags to determine heading later
	boolean hasKioskName = ( kioskName != null && !kioskName.isEmpty() );
	boolean hasMaterialName = ( materialName != null && !materialName.isEmpty() );
	
	// Form the query string for /reports to get data
	// NOTE: Send a size that is 1 bigger than required to help determine if pagination is required 
	String queryString = "type=" + ReportsConstants.TYPE_DEMANDBOARD + /// "&startdate=" + startDateStr +
						 "&size=" + size + "&offset=" + offset + "&cursortype=" + Constants.CURSOR_DEMANDBOARD;
	if ( kioskId != null )
		queryString += "&kioskid=" + kioskId;
	else if ( materialId != null )
		queryString += "&materialid=" + materialId;
	else
		queryString += "&domainid=" + domainId;
	
	// Add tags, if present
	String baseUrlNoTags = baseUrl;
	if ( hasKioskTag ) {
		baseUrl += "&tagtype=" + tagType + "&tag=" + tag;
		queryString += "&ktag=" + tag;
	} else if ( hasMaterialTag ) {
		baseUrl += "&tagtype=" + tagType + "&tag=" + tag;
		queryString += "&mtag=" + tag;
	}
%>
<fmt:bundle basename="Messages">
<!--  Scripts for Google Data Visualization -->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
    // Load the Visualization API and the ready-made Google table visualization.
    google.load('visualization', '1', {packages: ['table,piechart,annotatedtimeline']});
    
    // Set a callback to run when the API is loaded - this will run the summary count table/chart visualization.
  	google.setOnLoadCallback( drawDemandBoardTable);

	// Send the queries to the data sources and draw visualization
    function drawDemandBoardTable() {
        // Show the loading gif
        document.getElementById( 'dataTable' ).innerHTML = '<img src="../../images/loader.gif"/>';
    	// Form the query with the reports URL
      	var query = new google.visualization.Query( '/s/reports?<%= queryString %>' );
      	// Send the data to the Summary Counts Table visualizer	
    	query.send( handleDemandBoardResponse );   
    }
    
    // Handle the data source query response for summary count Tabular report
    function handleDemandBoardResponse( response ) {
    	if (response.isError()) {
        	// Hide the navigation panels
        	document.getElementById( 'dataNavigatorTop' ).style.display = '<%= hasTag ? "block" : "none" %>';
        	// Stop the loading indicator and show error
        	document.getElementById( 'dataTable' ).innerHTML = response.getDetailedMessage();
      		return;
    	}
		// Get the data table and render the Table report
   	 	var data = response.getDataTable();
   		// Create a view on this data (with the first few columns hidden)
   	 	var view = new google.visualization.DataView( data );
   	 	// Hide the last row - Totals row - of the data table to render in the Pie Chart
   	 	view.hideColumns( [ 0, 1, 2, 3, 4, 11, 12, 13, 14, 15, 16 ] );
   	 	// Hide the last row, which may contain the cursor
   	 	var lastrow = data.getNumberOfRows() - 1; // NOTE: this is also the number of valid data records (it is 1 more than required - to determine pagination)
   	 	view.hideRows( [ lastrow ] );
   	 	var hasMoreResults = false;
	 	////if ( lastrow > <%= size %> ) { 
	 	if ( lastrow >= <%= size %> ) { 
	   	 	hasMoreResults = true;
	   	 	////view.hideRows( [ lastrow - 1 ] ); // NOTE: Hide the last valid "data" row: this being the extra record queried to determine if pagination is required
	 	}
   		// Update the table page navigator
   	 	updateNavigator( data.getNumberOfRows() - 1, data.getFormattedValue( lastrow, 0 ), hasMoreResults ); // NOTE: numeberOfRows() - 1: -1 on number of rows to account for the last row with a cursor cell
   	 	// Init. table
   	 	var table = new google.visualization.Table( document.getElementById( 'dataTable' ) );
   		// Add a listner to notify when the data is ready - this can help stop a loading screen, for instance
 	    // or show the navigation bar
 	    // NOTE: THIS SHOULD BE ADDED BEFORE THE TABLE IS DRAWN
   	 	google.visualization.events.addListener( table, 'ready',
   		    function(event) {
   		      // Show the table navigator (which is initially hidden)
     		  document.getElementById( 'dataNavigatorTop' ).style.display = 'block';
   		    });
		// Draw the table
   	 	table.draw( view, { allowHtml: true } );
    }

	// Update the page range indication and navigation details
	function updateNavigator( numResults, nextCursor, hasMoreResults ) {
		// Update range of values displayed
		var maxRange = <%= maxRange %>;
		if ( numResults < <%= size %> )
			maxRange = <%= offset %> + numResults - 1;
		// Get the next/prev. offsets
		var nextOffset = <%= offset + size %>;
		var prevOffset = <%= offset - size %>;
		var htmlString = '<b><%=offset%>-' + maxRange + '</b>';
		// Update next URL
		if ( hasMoreResults ) { // nextCursor != '' && numResults > <%= size %> // ) {
			var nextUrl = '<%= baseUrl %>&o=' + nextOffset;
			htmlString += '&nbsp;&nbsp;&nbsp;<a href="' + nextUrl + '">' + JSMessages.next + '&gt;</a>';
		}
		// Show prev. and first URLs, if previous offset is non-negative
		if ( prevOffset > 0 ) {
			var prevUrl = '<%= baseUrl %>&o=' + prevOffset;
			var firstUrl = '<%= baseUrl %>';
			htmlString = '<a href="' + firstUrl + '">&lt;&lt;' + JSMessages.first + '</a>&nbsp;&nbsp;&nbsp;<a href="' + prevUrl + '">&lt;' + JSMessages.prev + '</a>&nbsp;&nbsp;&nbsp;'
						 + htmlString;
		}
		document.getElementById( 'navigator' ).innerHTML = htmlString;
	}
</script>
<div id="doc3">
	<h2 class="sgHeading"><fmt:message key="demand"/>
	<% if ( hasKioskName ) { %>
	<fmt:message key="for"/> <a href="../setup/setup.jsp?subview=kiosks&form=kioskdetails&id=<%= kioskId %>" target="_new"><%= kioskName %></a>
	<% } else if ( hasMaterialName ) { %>
	<fmt:message key="for"/> <a href="../setup/setup.jsp?subview=materials&form=materialdetails&id=<%= materialId %>" target="_new"><%= materialName %></a>
	<% } %>
	</h2>
</div>
<!-- <div class="yui-skin-sam">  -->
<div id="dataContainer" style="width:100%;margin-right:1%;float:left;margin-top:1%;margin-bottom:10px;">
	<div id="dataNavigatorTop" style="width:100%;display:none;">
		<table id="kioskstable" width="100%">
			<tr><td align="right"><div id="navigator"></div></td>
			</tr>
		</table>
		<% if ( kioskId == null ) { %>
			<%= TagUtil.getTagHTML( domainId, TagUtil.TYPE_ENTITY, tag, locale, baseUrlNoTags + "&tagtype=" + TagUtil.TYPE_ENTITY ) %>
		<% } %>
		<% if ( materialId == null ) { %>
			<%= TagUtil.getTagHTML( domainId, TagUtil.TYPE_MATERIAL, tag, locale, baseUrlNoTags + "&tagtype=" + TagUtil.TYPE_MATERIAL ) %>
		<% } %>
	</div>
	<div id="dataTable" style="width:100%;height:300px;overflow:auto;"></div>
</div>
</div>
</fmt:bundle>