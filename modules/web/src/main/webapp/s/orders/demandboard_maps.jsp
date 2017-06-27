<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.constants.Constants" %>
<%@page import="com.logistimo.entities.entity.AccountsService"%>
<%@page import="com.logistimo.entities.service.AccountsServiceImpl"%>
<%@page import="com.logistimo.materials.service.MaterialCatalogService"%>
<%@page import="com.logistimo.materials.service.impl.MaterialCatalogServiceImpl"%>
<%@page import="com.logistimo.orders.service.OrderManagementService"%>
<%@page import="com.logistimo.orders.service.impl.OrderManagementServiceImpl"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.services.ServiceException" %>
<%@page import="com.logistimo.services.Services" %>
<%@page import="com.logistimo.tags.TagUtil"%>
<%@page import="com.logistimo.utils.LocalDateUtil" %>
<%@page import="org.lggi.samaanguru.entity.*" %>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date" %>
<%@page import="java.util.Locale" %>
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

<jsp:include page="/s/JSMessages.jsp" flush="true" />
<!--  Include Google Maps scripts -->
<script type="text/javascript" src="https://maps.google.com/maps/api/js?key=AIzaSyCWMVddkt2dvTfDrZikF77Mana4g8mZ7Vg&sensor=false"></script>
<script src="../../js/markerclusterer.js"></script> <!--  MarkerClusterer script -->
<script src="../../js/oms.min.js"></script> <!--  Overlapping Marker Spiderfier script -->
<script src="../../js/map.js"></script> <!--  map.js-->
<!-- Include Google visualization scripts -->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<!-- Autocomplete box -->
<link rel="stylesheet" href="/autocomplete/TextboxList.css" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="/autocomplete/TextboxList.Autocomplete.css" type="text/css" media="screen" charset="utf-8" />
<script src="/js/jquery-1.3.2.min.js" type="text/javascript" charset="utf-8"></script>
<script src="/autocomplete/GrowingInput.js" type="text/javascript" charset="utf-8"></script>
<script src="/autocomplete/TextboxList.js" type="text/javascript" charset="utf-8"></script>
<script src="/autocomplete/TextboxList.Autocomplete.js" type="text/javascript" charset="utf-8"></script>
<script src="/autocomplete/TextboxList.Autocomplete.Binary.js" type="text/javascript" charset="utf-8"></script>
<!-- JQuery UI css and scripts -->
<link type="text/css" href="/jqueryui/css/redmond/jquery-ui-1.8.22.custom.css" rel="stylesheet" />
<script type="text/javascript" src="/jqueryui/js/jquery-ui-1.8.22.custom.min.js"></script>
<style type="text/css">
	.sgForm input {border:0;}
</style>
<%
	// Get request parameters, if any
	String materialIdStr = request.getParameter( "materialid" );
	// Get domain id, if any
	String domainIdStr = request.getParameter( "domainid" ); // required only for public demand maps
	// Check if this is public demand board
	String pdb = request.getParameter( "pdb" );
	boolean isPublicDemandBoard = ( pdb != null );
	// Tags, if any
	String tag = request.getParameter( "tag" ); // kiosk tag
	boolean hasTag = ( tag != null && !tag.isEmpty() );
	if ( hasTag )
		tag = URLDecoder.decode( tag, "UTF-8" );
	else
		tag = null;

	// Get the domain Id
	Long domainId = null;
	Locale locale = null;
	String role = null;
	String userId = null;
	if ( domainIdStr != null && !domainIdStr.isEmpty() ) {
		domainId = Long.valueOf( domainIdStr );
		DomainConfig dc = DomainConfig.getInstance( domainId );
		locale = dc.getLocale();
	} else {
		SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
		userId = sUser.getUsername();
		domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
		// Get locale
		locale = sUser.getLocale();
		// Get role
		role = sUser.getRole();
	}
	
	String mapData = "";
	Long materialId = null;
	IMaterial m = null;
	String strHeading = "";
	String startDate = null;
	boolean allKiosks = false;
	try {
		// Get the services
		AccountsService as = Services.getService( AccountsServiceImpl.class, locale );
		MaterialCatalogService mcs = Services.getService( MaterialCatalogServiceImpl.class, locale );
		OrderManagementService oms = Services.getService( OrderManagementServiceImpl.class, locale );
		// Get materialId, if selected
		if ( materialIdStr != null && !materialIdStr.isEmpty() )
			materialId = Long.valueOf( materialIdStr );
		// Update heading
		if ( materialId != null ) {
			m = mcs.getMaterial( materialId );
			strHeading += ": " + m.getName();
		}
		// Get the start date for demand board data
		SimpleDateFormat df = new SimpleDateFormat( Constants.DATE_FORMAT );
		startDate = df.format( LocalDateUtil.getOffsetDate( new Date(), -90 ) ); // 3 months back
	} catch ( ServiceException e ) {
		System.out.println( "ServiceException in inventoryviews_maps.jsp: " + e.getMessage() );
	} catch ( NumberFormatException e ) {
		System.out.println( "NumberFormatException in inventoryviews_maps.jsp: " + e.getMessage() );
	}
	// Form the demand map URL
	String demandMapUrl = "/s/orders/orders.jsp?maps=true&subview=demandboard";
	String listUrl = "/s/list.jsp?type=materials";
	String geoCodesUrlBase = "/s/getgeocodes.jsp?type=demand&domainid=" + domainId;
	if ( isPublicDemandBoard ) {
		demandMapUrl = "/pub/demand?id=" + domainIdStr;
		listUrl = "/pub/list?type=materials&pdbdomainid=" + domainIdStr;
		geoCodesUrlBase = "/pub/getgeocodes?type=demand&domainid=" + domainId;
	}
	if ( materialId != null )
		geoCodesUrlBase += "&materialid=" + materialId;
	if ( hasTag )
		geoCodesUrlBase += "&tag=" + tag;
%>
<fmt:bundle basename="Messages">
<fmt:message key="typetogetsuggestions" var="typetogetsuggestions" />
<fmt:message key="onlyoneitemcanbeselected" var="onlyoneitemcanbeselected" />
<!-- Google map and visualization script -->
<script type="text/javascript">
	//Load the Visualization API and the ready-made Google table visualization.
	google.load('visualization', '1', {packages: ['table']});

	// Show the latest demand associated with a given kiosk
	function showDemand( marker, location, materialId ) {
		<% if ( materialId == null ) { %>
		return function() {
			// If the markers are in the same position, then return
			// Iterate through mks and add the markers to oms
		  	var position;
			var spiderfy = true;
			var mks = oms.getMarkers();
			if ( mks.length == 0 ) {
				spiderfy = false;
			}
	   	    for ( var i = 0; i < mks.length; i++ ) {
	   	  		position = mks[0].getPosition();
	   	    	if ( !position.equals( mks[i].getPosition() ) ) {
	   	    		spiderfy = false;
	   	    	}
	   	  	}
   	      
   	      	if ( spiderfy == true ) {
   	    		return;
   	      	}
			var kioskId = location.kid; /// [ 0 ];
			var kioskName = location.title; /// [ 1 ];
			// Show the pop-up screen
			$('#imaptable').dialog(
									 {
										position: 'top',
										width: 600,
										height: 500,
										modal: true,
										title: kioskName
									   });
			// show the loading gif
			showLoader( 'imaptabledemand' );
			// Form the query URL for table visualization of inventory
			<%
			String detailsBaseUrl = null, pdbDomainId = null;
			if ( isPublicDemandBoard ) {
				detailsBaseUrl = "/pub/demanddetails";
				pdbDomainId = "&pdbdomainid=" + domainIdStr;
			} else
				detailsBaseUrl = "/s/reports"; 
			%>
			var url = '<%= detailsBaseUrl %>?type=<%= ReportsConstants.TYPE_DEMANDBOARD %>&kioskid=' + kioskId + '&startdate=<%= startDate %>'
					+ '&latest=true<%= pdbDomainId != null ? pdbDomainId : "" %>';
	      	var query = new google.visualization.Query( url );
	      	// Send the response to the inventory visualizer (table)
	      	query.send( handleDemandDsResponse );
		}
		<% } else { %>
		return function() {
			var kioskName = location.title; /// [ 1 ];
			var content = kioskName + '<br/><b>' + location.quantity + '</b> ' + JSMessages.items; /// [ 4 ]
			// Set the info. window content and open it
	      	infoWindow.setContent( content );
	        infoWindow.open( map, marker );
		}
		<% } %>
	}

	// Draw the inventory table, based on the query response
	function handleDemandDsResponse( response ) {
		if (response.isError()) {
			// Show error message
      		document.getElementById('imaptabledemand').innerHTML = response.getDetailedMessage();
      		return;
    	}
		// Get the data table and render the Table visualization
   	 	var data = response.getDataTable();
   	 	// Sort the table by material name
   	 	data.sort( [5] );
   	 	// Get a view with only material & stock values
   	 	var view = new google.visualization.DataView( data );
   		// Show only the required columns
   	 	view.setColumns( [ 5,6,10 ] );
   	 	var table = new google.visualization.Table( document.getElementById( 'imaptabledemand' ) );
   	 	table.draw( view, { allowHtml: true } );
   	 	// Show the table toggler
   	 	document.getElementById( 'imaptabledemand' ).style.display = 'block';
	}
	
	// Init. the material autocomplete drop-down, for material selection
	function initDropDowns() {
		<% if ( m == null ) { %>
		var placeholder = '${typetogetsuggestions}';
		<% } else { %>
		var placeholder = '${onlyoneitemcanbeselected}';	
		<% } %>
		// Init. material selector
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
											  			remote: { url: '<%= listUrl %>' }
												  	}
	  											}
									  } );
		  materialid.clear();
		<% if ( m != null ) { %>
		materialid.add( '<%= m.getName() %>', '<%= m.getMaterialId() %>', null );
		<% } %>
		// Event when something is selected
		materialid.addEvent( 'bitBoxAdd', function(b) {
										showLoader( 'materialloader' );
										location.href = '<%= demandMapUrl %>&materialid=' + b.value[0]; 
									});
	}
	
	// Load the map after page load
	window.onload = function() { 
		var type = 'demand';
		initialize( <%= materialId != null ? "'" + materialId + "'" : null %>, type, '<%=geoCodesUrlBase%>' );
		initDropDowns();
	}

	function onchangeCheckbox( doNotClusterCheckBox ) {
		toggleClustering( doNotClusterCheckBox );
	}
</script>
<div id="doc3" style="clear:both">
	<h2 class="sgHeading"><fmt:message key="demandreview"/><%= strHeading %>
	<div style="float:right"><a href="<%= demandMapUrl %>"><img src="/images/map.png" title="Map" valign="middle" width="20" height="20" alt="Map"/></a></div>
	</h2>
</div>
<%= TagUtil.getTagHTML( domainId, TagUtil.TYPE_ENTITY, tag, locale, demandMapUrl + ( materialId != null ? "&materialid=" + materialId : "" ) ) %>
<div id="mapcontainer">
	<div id="imap"></div>
</div>
<% if ( materialId != null ) { %>
<div id="maplegend">
	<img src="/images/yellow-pin.png" alt="yellow pin" title="Latest demand"/> <fmt:message key="demand"/>
</div>
<% } %>
<div class="sgForm" style="float:right">
	<fieldset>
		<legend><fmt:message key="inventory.viewby" /> <fmt:message key="material"/></legend>
		<input type="text" id="materialid" />
	</fieldset>
	<div id="materialloader" style="float:right"></div>
</div>
<div id="geoinfo" class="sgForm" style="float:left;background-color:#FAFAFA;margin-top:3px"></div>
<% if ( materialId == null ) { %>
<div class="sgForm" style="float:left;margin-left:150px"><input id="donotclustercheckbox" type="checkbox" value="uncluster" style="vertical-align:middle" onchange="onchangeCheckbox(this)"/>&nbsp;<label for="donotclustercheckbox"><fmt:message key="map.donotclusterpoints"/></label></div>
<% } %>
<!-- Div to show inventory details in a model popup -->
<div id="imaptable" style="text-align:left;font-family:Arial">
	<div id="imaptabledemand"></div>
</div>
</div>
</fmt:bundle>