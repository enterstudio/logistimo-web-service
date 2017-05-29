<%@page contentType="text/html; charset=UTF-8" language="java"%>
<%@page import="com.logistimo.api.security.SecurityMgr"%>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.services.Services"%>
<%@page import="com.logistimo.entities.entity.IKiosk" %>
<%@page import="com.logistimo.config.models.KioskConfig" %>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.config.models.StockboardConfig" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Iterator" %>
<%@ page import="com.logistimo.entities.service.EntitiesService" %>
<%@ page import="com.logistimo.entities.service.EntitiesServiceImpl" %>
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
	final int MAX_SIZE = 50;
	// Read the kiosk id parameter and get the inventory for that kiosk using rest api
	Long kioskId = null;
	String kioskName = null; // Will be displayed the stock board
	String kioskLocation = null; // Will be displayed on the stock board
	String errMsg = null; // Will be displayed in case of error.
	int size = MAX_SIZE;
	int offset = 0;
	StockboardConfig sbConfig = null;
	Long domainId = null;
	String pageHeader = null;
	int items = 0;
	// Read the request parameters here.
	String kioskIdStr = request.getParameter( "kioskid" );
	String sizeStr = request.getParameter( "s" );
	String offsetStr = request.getParameter("o");
	
	// Check if there is a logged-in user
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = null;
	if ( sUser != null )
		userId = sUser.getUsername();
	
	if ( kioskIdStr == null ) {
		errMsg = "System error: could not identify kiosk.";
	}
	if ( sizeStr != null && !sizeStr.isEmpty() )
		size = Integer.valueOf( sizeStr );

	if ( offsetStr != null && !offsetStr.isEmpty() )
		offset = Integer.valueOf( offsetStr );

	if ( kioskIdStr != null && !kioskIdStr.isEmpty() ) {
		kioskId = Long.valueOf( kioskIdStr );
		try {
			// Get the kiosk details.
			EntitiesService as = Services.getService( EntitiesServiceImpl.class );
			IKiosk k = as.getKiosk( kioskId );
			if ( k != null ) {
				// Get the domain to which the kiosk belongs
				domainId = k.getDomainId();
				if ( domainId != null ) {
					DomainConfig dc = DomainConfig.getInstance( domainId );
					// Get the page header if any for the domain
					pageHeader = dc.getPageHeader();
				}
				kioskName = k.getName();
				kioskLocation = k.getCity();
				// Get the Kiosk Configuration
				KioskConfig kc = KioskConfig.getInstance( kioskId );
				if ( kc != null && kc.getStockboardConfig() != null && kc.getStockboardConfig().isEnabled() )
					sbConfig = kc.getStockboardConfig();
				else
					errMsg = "This view is not available for '" + kioskName + "'";
			}
		} catch ( Exception e ) {
			errMsg = e.getMessage();
		}
	} 
	boolean hasNoErrors = ( errMsg == null );
%>
<!DOCTYPE html>
<html>
<head>
	<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
	<meta http-equiv="content-type" content="text/html; charset=utf-8">
	<meta http-equiv="refresh" content="<%= sbConfig != null ? sbConfig.getRefreshDuration() : "60" %>">
    <title>Stock Board</title>
	<link rel="stylesheet" type="text/css" href="/css/stockboard.css"/>
	<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
	<script type="text/javascript" src="/js/jquery.totemticker.js"></script><!--  Vertical ticker -->
	<script type="text/javascript" src="/js/newsTicker.js"></script><!--  Horizontal message board -->
    <style type="text/css">
        .imagewrapper, .imagewrapper img {
            max-height: 60px;
        }
    </style>
	<script type="text/javascript">
		// Global variable that holds the total number of materials that are displayed on the stock board. 
		var numOfItems = 0;
		// Called in the document on ready function
		function init( size, offset ) {
			var showHorTicker = <%= ( sbConfig != null && sbConfig.getHorScrlMsgsList() != null ) %> ? true : false;
			if (  showHorTicker ) {
				initHorTicker();
			}
			initVerticalTicker();
			getInventoryAndShowOnStockboard( size, offset, <%= userId != null ? "'" + userId + "'" : "undefined" %> );
		}
		
		// Initialize the horizontal message board
		function initHorTicker() {
			$('#newsList').newsTicker({ interval: '<%= sbConfig != null ? sbConfig.getHorScrollInterval() : StockboardConfig.HOR_SCROLL_INTERVAL_DEFAULT %>' });
		};
		
		// Initialize the vertical totem ticker
		function initVerticalTicker() {
			$('#vertical-ticker').totemticker({
				row_height	:	'100px',
				mousestop	:	true,
				speed		:   800,
				interval	:	<%= sbConfig != null ? sbConfig.getScrollInterval() : StockboardConfig.SCROLL_INTERVAL_DEFAULT %>,
				max_items	: 	<%= sbConfig != null ? sbConfig.getMaxItems() : StockboardConfig.MAX_ITEMS %>,
			});
		};
		
		// Function to call the rest api to get inventory and populate the stock board.
		function getInventoryAndShowOnStockboard( size, offset, userId ) {
			// Make an AJAX call to the Rest API. TODO: Get inventory without uid and p parameters.
			var url = '/api/i';
			var reqParams = {};
			reqParams.a = 'gi'; 
			reqParams.kid = '<%= kioskId %>';
			reqParams.s=size;
			reqParams.o=offset;
			if ( userId )
				reqParams.uid = userId;
			$.ajax({
				url: url,
				data: reqParams,
				type: 'GET',
				dataType: 'json',
				success: function(o) {
					if ( o ) {
						if ( o.st == 0 ) {
							processResponse( o );
							if ( o.mt.length == size ) {
								offset = parseInt(offset,10)+size;
								// call rest api recursively if the number of materials returned == size 
								getInventoryAndShowOnStockboard( size, offset, userId );
							} 
						} else {
							showErrorMessage( true, o.ms );
							hideVerticalTicker();
							hideHorTicker();
						}
					}
				},
				error: function( o ) {
					alert( 'Sorry there was a problem while calling the rest api' );
					showErrorMessage( true, 'Sorry there was an error' );
				},
				complete: function( o ) {
				}
			});
		}
		
		// Populate the global variable materialsjson with data returned by the rest api.
		function processResponse( o ) {
			// Get the materials from o
			var mt = o.mt;
			if ( mt ) {
				populateTotemTicker( mt );
			} else {
				console.log( 'ERROR: Materials is null or undefined' );
				showErrorMessage( true, 'ERROR: Materials is null or undefined' );
			}
		} 
			
		// Populate the totem ticker plugin with values from the json returned by the rest api
		function populateTotemTicker( mt ) {
			if ( mt ) {
				if ( mt.length == 0 ) { 
					// If no materials were returned and it's the first iteration, display error message and hide the ticker controls.
					if ( numOfItems == 0 ) {
						showErrorMessage( true, 'No materials to display' );
						hideVerticalTicker();
						hideHorTicker();
					}
				} else { 
					for ( var i = 0; i < mt.length; i++ ) {
						var materialId = mt[i].mid;
						var materialName = mt[i].n;
						var stockOnHand = mt[i].q;
						
						var liHtml = '<li><div class="liname">' +  materialName;
						// liHtml += '<span class="liinfo">' + 'some very very very very long utf-8 string' + '</span>';
						liHtml += '</div><div class="livalue">' + stockOnHand +'</div></li>';
						$( '#vertical-ticker' ).append( liHtml );
					}
					numOfItems += mt.length; // Update the global variable.
					// Update the number of items in the div.
					var numOfItemsDiv = document.getElementById( "numofitems" );
					if ( numOfItemsDiv ) {
						numOfItemsDiv.style.display='block';
						numOfItemsDiv.innerHTML = numOfItems;
					}
				}
			}
		}
		
		// Function that shows or hides an error message	
		function showErrorMessage( show, message ) {
			var div = document.getElementById( 'errormsgdiv' );
			if ( div ) {
				if ( show ) {
					div.style.display = 'block';
					if ( message )
						div.innerHTML = '<br/>&nbsp;<font style="color:red;font-weight:bold;font-size:16px;">' + message + '. Please contact your System Administrator.' + '</font>';
				} else {
					div.style.display = 'none';
				}
			}
		}
		
		// function that hides horizontal ticker
		function hideHorTicker() {
			var horTicker = document.getElementById( "newsData" );
			if ( horTicker ) {
				horTicker.style.display = 'none';
				$( '#newsData' ).remove();
				$( '#newsList' ).remove();
			}
		}
		
		// function that hides vertical ticker
		function hideVerticalTicker() {
			var verticalTicker = document.getElementById( "wrapper" );
			if ( verticalTicker )
				verticalTicker.style.display = 'none';
		}
		
		<% if ( hasNoErrors ) { %>
		$(document).ready( function() {
							init( <%= size %>, '<%= offset %>' ); // Initialize with the parameters that are passed to the jsp.
						   });
		<% } %>
	</script>
</head>
<body>
	<div class="banner">
		<% if ( pageHeader != null && !pageHeader.isEmpty() ) { %>
        <div class="imagewrapper"><%= pageHeader %></div>
		<% } else { %>
		<table width="100%">
			<tr>
				<td style="width:20%">
                    <img class="imagewrapper" src="/images/acquia_marina_logo.png"/>
                </td>
				<% if ( hasNoErrors ) { %>
				<td style="width:80%;text-align:left;font-size:16pt;color:brown"><b><%= kioskName %>, <%= kioskLocation %></b></td>
				<% } %>
				<td style="text-align:right"><div id="numofitems" class="nav"></div></td>
			</tr>
		</table>
		<% } %>
	</div>
	<div id="errormsgdiv">
	<% if ( !hasNoErrors ) { %>
		<br/>&nbsp;<font style="color:red;font-weight:bold;font-size:16px;"><%= errMsg %>. Please contact your System Administrator.</font>
	<% } %>
	</div>
	<% if ( hasNoErrors ) { %>
	<div id="wrapper">
		<ul id="vertical-ticker">
		</ul>	 
	</div>
	<% if ( sbConfig != null && sbConfig.getHorScrlMsgsList() != null ) { %>
		<div id="newsData" class="newsCss">
			 <ul id="newsList">
			 <% if (sbConfig != null ) {
				 List<String> horScrlMsgsList = sbConfig.getHorScrlMsgsList();
				 if ( horScrlMsgsList != null && !horScrlMsgsList.isEmpty() ) {
					 Iterator<String> horScrlMsgsListIter = horScrlMsgsList.iterator();
					 while ( horScrlMsgsListIter.hasNext() ) {
			 			%>
						 <li><%= horScrlMsgsListIter.next() %></li>
						<%
					 } // end - while
				 } // end if horScrlMsgsList != null & horScrlMsgsList is not empty
			 } // end if sbConfig is not null
			 %>
			 </ul>
		</div>
		<%  } // End if sbConfig != null && sbConfig.getHorScrlMsgsList != null %> 	
	<% } %>
</body>
</html>
