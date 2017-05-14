/*
 * This js file contains all the functions specific to routing of managed entities
 * The routing is done using jquery ui's sortable table or maps
 */
// Global variables used for routing using google maps
var map; // Holds the map object
var polyLine; // The polyline object that shows the route plotted on the map
var g = google.maps; // Easy to type g instead of google.maps everywhere
var lastMarkerAddedOrRemoved; // Is the last square marker that was added or removed. This is used for the undo route operation
var lastOperation; // Whether last operation was adding or deleting a square marker.
var lastMarkerPosition; // Position at which the last square marker was removed. Note that this is not LatLng position but the index at which it was present in the routedEntitiesJsonArray
var allEntitiesJsonArray = []; // Array of json objects corresponding to all entities
var routedEntitiesJsonArray = []; // Array of json objects corresponding to routed entities
var emptyTag = '';
var actualRouteJsonArray = []; // Array of json objects containing actual route details such as coordinates, title etc.
var actualRouteGeoDataCollected = []; // Array containing the actual route geoData collectively (accumulated) 
var actualPolyLine; // The polyline object that shows the actual route
var showingActualRoute = {};
var alreadyClicked = false; // Flag that indicates if the showActualRoute button is clicked. This is used in order to prevent double clicks on the showActualRoute button that makes an ajax call.
var manualRouteMapCenter;
var manualRouteMapBounds;
var markerCluster; // MarkerClusterer
var oms; // OverlappingMarkerSpiderfier
var CLUSTER_MAXZOOM = 15;

/// For road route/distance using Google Directions Service
var MAX_WAYPOINTS = 8;
var roadmap = null; // road map for actual route
var directionsService; // directions service
var directionsDisplay; // directiosn renderer
var directionsResults = []; // an array of DirectionsResult, for each index

//Pagination size
var SIZE = 50;

// Returns the map object
function getMap() {
	return map;
}

//Get an ID value without spaces
function getId( tag ) {
	return tag.replace( / /g, '-' ); // replace spaces with -
}

// Function to show the routing on the map
function showRouteMap( mapdiv, tagName, type, geoCodesUrlBase, isRteEnabled, DEFAULT_ROUTE_INDEX ) {
	
	// Append type to geoCodesUrlBase
	geoCodesUrlBase += '&type=' + type;
	
	// Reset the variable values every time the tab is changed
	resetVariables();
	
	// Initialize the map every time
	initialize( mapdiv );
		
	// Init the polyline every time
	initPolyline();
  
	// Get the geocodes of the kiosks/entities and add them to map
	getGeocodesAndMap( mapdiv, tagName, geoCodesUrlBase, SIZE, null, 0, null, isRteEnabled, DEFAULT_ROUTE_INDEX );
}

function showActualRoute( mapdiv, tagName, type, geoCodesUrlBase ) {
	// If the flag alreadyClicked is true, return from the function.
	if ( alreadyClicked ) {
		return;
	}
	// If the flag alreadyClicked is false, then set it to true and continue processing.
	alreadyClicked = true;
		
	// Append type to geoCodesUrlBase
	geoCodesUrlBase += '&type=' + type;
	// Reset the actualRouteJsonArray every time 'Show actual route' button is clicked
	resetActualRouteVariables();
	
	// Init markerClusterer and spiderfier
	initMarkerClusterAndSpider();
	
	// Init the actualPolyLine
	initActualPolyline();
	
	// Before showing the actual route, store the manual route map center and bounds into global variables.
	storeManualRouteMapCenterAndBounds();
	
	// Get the geocodes of the kiosks/entities and add them to map
	getActualRouteGeocodesAndMap( mapdiv, tagName, geoCodesUrlBase, SIZE, null, 0, map.getBounds() );
	
	// If the jsonvariable showingActualRoute does not have an entry for tagName, only then add it and set it to true. (This case arises if the showActualROute button is clicked when an actual route is already been shown)
	if ( !showingActualRoute[tagName] ) {
		// Add a key with tagName to the json showingActualRoute and set it to true
		showingActualRoute[tagName] = true;
	}
	// Show the actual polyline and actual route coordinates
	// Iterate through actualRouteJsonArray
	for ( var i = 0; i < actualRouteJsonArray.length; i++ ) {
		actualRouteJsonArray[i].marker.setVisible( true );
	}
	
	// Set the actual route polyline to visible
	if ( actualPolyLine ) {
		actualPolyLine.setVisible( true );
	}
	
}

// Reset the actual route json array and the markers in the actual route and the polyline showing the actual route
function resetActualRouteVariables() {
	// Loop through the actualRouteJsonArray and remove transaction markers
	for ( var i = 0; i < actualRouteJsonArray.length; i++ ) {
		actualRouteJsonArray[i].marker.setMap( null ); // Remove transaction marker from the map
		if ( markerCluster )
			markerCluster.removeMarker( actualRouteJsonArray[i].marker );
		actualRouteJsonArray[i].marker = null; // Set the marker to null
		
	}
	actualRouteJsonArray = [];
	if ( actualPolyLine ) {
		actualPolyLine.setPath( [] ); // Set the path of the actualPolyline to empty
		actualPolyLine.setMap( null ); // Remove actualPolyLine from the map
		actualPolyLine = null; // Set the actualPolyLine to null
	}
	// Reset the actualRouteGeoDataCollected array to empty
	actualRouteGeoDataCollected = [];
	
	// Remove the markerCluster from the map
	if ( markerCluster ) 
		markerCluster.setMap( null );
	// If spider exists, clear all markers from it. TODO: Check how to remove spider from the map.
	if ( oms ) 
		oms.clearMarkers();

}

// This function initializes the marker cluster and spider
function initMarkerClusterAndSpider() {
	markerCluster = new MarkerClusterer( map ); // Create the markerCluster
	markerCluster.setMaxZoom( CLUSTER_MAXZOOM ); // Set the maximum zoom of the cluster to 15. This is required to integrate the cluster with spiderfier
	oms = new OverlappingMarkerSpiderfier( map, { markersWontMove: true, markersWontHide: true, keepSpiderfied: true }); // Create the spiderfier object
	
	// Add an event listener to the markerCluster
	  google.maps.event.addListener( markerCluster, 'click', function( clickedCluster ) {
		  // First clear the exisiting markers from oms
		  oms.clearMarkers();
		  if ( clickedCluster.getMarkers().length > 1  ) {
			  var mks = clickedCluster.getMarkers();
			  // Iterate through mks and add the markers to oms
			  var position;
			  var spiderfy = true; // Flag that indicates if there are markers under a cluster at the same position.
  	      for ( var i = 0; i < mks.length; i++ ) {
  	    	  position = mks[0].getPosition();
  	    	  oms.addMarker( mks[i] ); 
  	    	  if ( !position.equals( mks[i].getPosition() ) ) {
  	    		  spiderfy = false;
  	    	  }
  	  	  }
  	      // If there are multiple markers at the same location, call the click event on the marker so that it is spiderfied.
  	      if ( spiderfy == true ) {
	    	  console.log( 'There are ' + mks.length + ' entities at this location. Click to see all the orders/transactions at this location.' );
	    	  // alert( 'There are ' + mks.length + ' entities at this location. Click to see all the orders/transactions at this location.');
  	    	  // google.maps.event.trigger( mks[mks.length-1], 'click' ); // This should but it does not actually spiderfy the markers.
  	      }
		}
	  });	
}

// This function is called when 'Hide actual route button' is clicked
function hideActualRoute( mapdiv, tagName ) {
	// Reset the actual route variables
	resetActualRouteVariables( mapdiv );
	// Remove the tagName from the showingActualRoute json
	delete showingActualRoute[tagName];
	// Reset the map center and bounds to whatever value it was before showing actual route
	restoreManualRouteMapCenterAndBounds();
	
}

// Private function to reset map center and bounds
function restoreManualRouteMapCenterAndBounds() {
	// If manualRouteMapBounds is not null, then set the map bounds to that value. If it is null, then set the map center to manuRouteMapCenter
	if ( manualRouteMapBounds ) {
		map.fitBounds( manualRouteMapBounds );
	}
	if ( manualRouteMapCenter ) {
		map.setCenter( manualRouteMapCenter );
	}
	// map.setZoom( map.getZoom() + 1 ); // This was added because when the actual route was shown and hidden, the manual route zoom was getting reduced from 4 to 3. This line has fixed that problem.
}

// Private function to store the map center and bounds
function storeManualRouteMapCenterAndBounds() {
	// Set the global variables manualRouteMapCenter and manualRouteMapBounds
	manualRouteMapCenter = map.getCenter();
	manualRouteMapBounds = map.getBounds();
}

// Function to reset variable values
function resetVariables() {
	lastMarkerAddedOrRemoved = null;
	lastOperation = null; 
	lastMarkerPosition = null;
	// Iterate through allEntitiesJsonArray and set the markers to null
	for ( var i = 0; i< allEntitiesJsonArray.length; i++ ) {
		allEntitiesJsonArray[i].marker.setMap( null ); // Remove the marker from the map
		allEntitiesJsonArray[i].marker = null; // Set the marker to null
	}
	allEntitiesJsonArray = []; // Empty the allEntitiesJsonArray
	for ( var i = 0; i< routedEntitiesJsonArray.length; i++ ) {
		routedEntitiesJsonArray[i].marker.setMap( null ); // Remove the marker from the map
		routedEntitiesJsonArray[i].marker = null; // Set the marker to null
	}
	routedEntitiesJsonArray = []; // Empty the routedEntitiesJsonArray
	// Set the initial route to null, which means remove the polyLine from the map
	if ( polyLine ) {
		polyLine.setPath( [] );
		polyLine.setMap( null ); 
		polyLine = null;
	}
}

// Function to initialize the map
function initialize( mapdiv ) {
	 // Init. map
	  var myOptions = {
	    zoom: 5,
	    mapTypeId: g.MapTypeId.ROADMAP
	  }
	  
	  map = new g.Map( mapdiv, myOptions );
}

//Get geo-codes and map them
function getGeocodesAndMap( mapdiv, tagName, geoCodesUrlBase, size, cursor, mappedSoFar, latLngBoundsSoFar, isRteEnabled, DEFAULT_ROUTE_INDEX ) {
	// Form the URL
	var url = geoCodesUrlBase + '&s=' + size;
	if ( cursor != null )
		url += '&c=' + cursor;
	if( tagName != emptyTag )
		url += '&routetag=' + tagName;
	// Show the geo-data loader
	showGeoInfo( tagName, true, size, mappedSoFar );
	
	$.getJSON(
		 url, function(o) {
			 	var totalMapped = mappedSoFar;
			 	var latLngBounds = latLngBoundsSoFar;
			 	var geoData = o;
			 	console.log( geoData );
			 	
				if ( geoData.size > 0 ) { // map the points
					// Get the lat-lng bounds
					// latLngBounds = getUpdatedLatLngBounds( latLngBoundsSoFar, geoData.latlngbounds );
					latLngBounds = mygetUpdatedLatLngBounds( latLngBoundsSoFar, geoData.geocodes );
					// Map the data
					initKioskMarkers( geoData.geocodes, tagName, latLngBounds, isRteEnabled, DEFAULT_ROUTE_INDEX );
					totalMapped = mappedSoFar + geoData.size;
				}
						
				// Check if more data exists
				if ( geoData.hasMoreResults ) {
					getGeocodesAndMap(  mapdiv, tagName, geoCodesUrlBase, size, geoData.cursor, totalMapped, latLngBounds, isRteEnabled, DEFAULT_ROUTE_INDEX ); // recursive call...
				} else {
					if ( totalMapped == 0 ) {
						// mapdiv.innerHTML = JSMessages.nodataavailable; // This was commented because if there is no manual route and the user clicks Show actual route, the actual was not being shown. 
						showGeoInfo( tagName, false, size, totalMapped ); // false
						return;
					}
					
					showGeoInfo( tagName, false, size, totalMapped ); // false
					// By now, all the kiosks are mapped. Plot initial route if any
					if ( isRteEnabled )
						drawInitialRoute();
					// Store the manualRouteMap center and bounds so that the map can be centered around manual route when actual route is not being shown.
					storeManualRouteMapCenterAndBounds();
				}
		 }).error( function() { // TODO: Get the error message from the messages file instead of hard coding.
			 					alert( 'Error while fetching data from server' ); });
	}

//Get geo-codes and map them
function getActualRouteGeocodesAndMap( mapdiv, tagName, geoCodesUrlBase, size, cursor, mappedSoFar, latLngBoundsSoFar ) {
	// Form the URL
	var url = geoCodesUrlBase + '&s=' + size;
	if ( cursor != null )
		url += '&c=' + cursor;
	// Show the geo-data loader
	showActualRouteGeoInfo( tagName, true, size, mappedSoFar );
	$.getJSON(
		 url, function(o) {
			 	var totalMapped = mappedSoFar;
			 	var latLngBounds = latLngBoundsSoFar;
			 	var geoData = o;
			 	console.log( geoData );
				if ( geoData.size > 0 ) { // map the points
					// Get the updated lat-lng bounds 
					// latLngBounds = getUpdatedLatLngBounds( latLngBoundsSoFar, geoData.latlngbounds );
					latLngBounds = mygetUpdatedLatLngBounds( latLngBoundsSoFar, geoData.geocodes );
					// Iterate through geoData.geocodes and push it into the actualRouteGeoDataCollected array
					for ( var i = 0 ; i < geoData.geocodes.length; i++ ) {
						actualRouteGeoDataCollected.push( geoData.geocodes[i] );
					}
					// Update totalMapped
					totalMapped = mappedSoFar + geoData.size;
				}

				// Check if more data exists
				if ( geoData.hasMoreResults ) {
					getActualRouteGeocodesAndMap(  mapdiv, tagName, geoCodesUrlBase, size, geoData.cursor, totalMapped, latLngBounds ); // recursive call...
				} else {
					initActualRouteJsonArray( actualRouteGeoDataCollected, tagName, latLngBounds );
					// Get the road route id (with/without tag)
					var rrId = 'roadroute';
					if ( tagName )
						rrId = tagName + rrId;
					if ( totalMapped == 0 ) {
						///alert( totalMapped + ' ' + JSMessages.routing_actualroutegeoinfomsg );
						hideRoadRoute( rrId ); // hide any road route elements
						// Show the geo info.
						showActualRouteGeoInfo( tagName, false, size, totalMapped );
						alreadyClicked = false; // Set the flag alreadyClicked to false so that the button showActualRoute can be clicked again
						return;
					}
					showActualRouteGeoInfo( tagName, false, size, totalMapped ); // false
					plotActualRoute();
					// Fetch and plot road route
					if ( totalMapped > 1 ) {
						// plotRoadRoute( geoData, rrId );
						plotRoadRoute( actualRouteGeoDataCollected, rrId, latLngBounds );
					}
				}
				alreadyClicked = false; // Set the flag alreadyClicked to false so that the button showActualRoute can be clicked again
		 }).error( function() { // TODO: Get the error message from the messages file instead of hard coding.
				alert( 'Error while fetching data from server' );
				alreadyClicked = false; // Set the flag alreadyClicked to false so that the button showActualRoute can be clicked again
				});
	}

//Show geo info.
function showGeoInfo( tagName, showLoader, size, mappedSoFar ) {
	var tagId = getId( tagName );
	var div = document.getElementById( tagId + 'geoinfo');
	if ( showLoader ) {
		div.innerHTML = '<img src="/images/loader.gif" /> <b>' + mappedSoFar + '</b> ' + JSMessages.routing_manualroutegeoinfomsg + '. ' + JSMessages.fetching + ' ' + size + ' ' + JSMessages.more + '...';
	} else {
		div.innerHTML = '<b>' + mappedSoFar + '</b> ' + JSMessages.routing_manualroutegeoinfomsg;
	}
}

//Show actual route geo info.
function showActualRouteGeoInfo( tagName, showLoader, size, mappedSoFar ) {
	var tagId = getId( tagName );
	var div = document.getElementById( tagId + 'actualroutegeoinfo');
	if ( showLoader ) {
		div.innerHTML = '<img src="/images/loader.gif" /> <b><font color="red">' + mappedSoFar + '</b> ' + JSMessages.routing_actualroutegeoinfomsg + '. ' + JSMessages.fetching + ' ' + size + ' ' + JSMessages.more + ' ...' + '</font>';
	} else {
		div.innerHTML = '<b><font color="red">' + mappedSoFar + '</b> ' + JSMessages.routing_actualroutegeoinfomsg + '</font>';
	}
}

//Get the updated lat-lng bounds
function getUpdatedLatLngBounds( latLngBoundsSoFar, newLatLngBounds ) {
	if ( latLngBoundsSoFar == null )
		return newLatLngBounds;
	var latLngBounds = [];
	// Lat. North
	if ( latLngBoundsSoFar[0] > newLatLngBounds[0] )
		latLngBounds.push( latLngBoundsSoFar[0] );
	else
		latLngBounds.push( newLatLngBounds[0] );
	// Lng. East
	if ( latLngBoundsSoFar[1] > newLatLngBounds[1] )
		latLngBounds.push( latLngBoundsSoFar[1] );
	else
		latLngBounds.push( newLatLngBounds[1] );
	// Lat. South
	if ( latLngBoundsSoFar[2] < newLatLngBounds[2] )
		latLngBounds.push( latLngBoundsSoFar[2] );
	else
		latLngBounds.push( newLatLngBounds[2] );
	// Lng. West
	if ( latLngBoundsSoFar[3] < newLatLngBounds[3] )
		latLngBounds.push( latLngBoundsSoFar[3] );
	else
		latLngBounds.push( newLatLngBounds[3] );
	return latLngBounds;
}

// Get lat-lng bounds of the given the north-east and southwest points; further, if scaling factor is specified, then scale the bounds accordingly
function getLatLngBounds( location, scalingFactor ) {
	if ( location.length < 4 )
		return null;
	var northEast = new g.LatLng( location[ 0 ] + scalingFactor * location[ 0 ],
										    location[ 1 ] - scalingFactor * location[ 1 ] );
	var southWest = new g.LatLng( location[ 2 ] + scalingFactor * location[ 2 ],
											location[ 3 ] - scalingFactor * location[ 3 ] );
	
	return new g.LatLngBounds( southWest, northEast );
}

//mygetUpdatedLatLngBounds()
function mygetUpdatedLatLngBounds( latLngBoundsSoFar, points ) {
	if ( points == null ) {
		console.log( 'Entering mygetUpdatedLatLngBounds. points: ' + points );
		return null;
	}
	if ( latLngBoundsSoFar == null ) {
		latLngBoundsSoFar = new google.maps.LatLngBounds();
	}
	for ( var i = 0; i < points.length; i++ ) {
		var point = points[i];
		var loc = new google.maps.LatLng( point.lat, point.lng );
		latLngBoundsSoFar.extend( loc );
	}
	return latLngBoundsSoFar;
}

//Initialize kiosk markers
function initKioskMarkers( locations, tagName, latLngBounds, isRteEnabled, DEFAULT_ROUTE_INDEX ) {
	var centerAtLatLng;

	// Plot the points on the map
	for ( var i = 0; i < locations.length; i++ ) {
		var kloc = locations[ i ];
		// If routetag matches tagName or if routetag is absent and tagName == EMPTY Tag( this happens for untagged entities ) display kiosk markers.
		if ( ( !kloc.routetag && tagName == emptyTag ) ||  kloc.routetag == tagName ) {
			var latLng = new g.LatLng( kloc.lat, kloc.lng );
			var marker;
			marker= getBasicMarker( latLng, map, kloc.title );
			var kid = kloc.kid;
			
			// Create a json to hold the entity data
			var entityJson = {};
			entityJson.kid = kid;
			entityJson.title = kloc.title;
			entityJson.marker = marker;
			entityJson.tag = kloc.routetag;
			// Add the entityDataJson to allEntitiesJsonArray 
			allEntitiesJsonArray.push( entityJson );
			
			// If the index of the kloc is < locations.length then, add this kid to routedKids
			if ( isRteEnabled ) {
				if ( kloc.routeindex != DEFAULT_ROUTE_INDEX ) {
					var routedEntityJson = {};
					routedEntityJson.kid = kid;
					routedEntityJson.title = kloc.title;
					routedEntityJson.marker = createMarker( marker.getPosition(), tagName );
					routedEntityJson.tag = kloc.routetag;
					routedEntitiesJsonArray.push( routedEntityJson );
				}
			}
			
	    	// Add an event listener to the marker
			g.event.addListener( marker, 'click', function(event) { markerLeftClick( event, tagName ); } );
	
			// Get the first latLag for centering, only when bounds is not available
			if ( i == 0 )
				centerAtLatLng = latLng;
		}
	}
	
	// Set the lat/lng bounds - i.e. the bounding box across all coordinates in this view
	// setCurrentMapCenterOrBounds( locations, latLngBounds, centerAtLatLng );
	setCurrentMapCenterAndBounds( latLngBounds );
}

function setCurrentMapCenterAndBounds( latLngBounds ) {
	if ( latLngBounds && latLngBounds != null ) {
		map.fitBounds( latLngBounds );
		map.setCenter( latLngBounds.getCenter() );
	}
}
//////////////////////////////////////////////////////////////////////////////
/*
// This function sets the center of the map if there is only location. If not, it sets the bounds for the map
function setCurrentMapCenterOrBounds( locations, latLngBounds, centerAtLatLng ) {
	if ( locations.length == 1 ) {
		map.setCenter( centerAtLatLng );
	} else {
		var bounds = latLngBounds; //getLatLngBounds( latLngBounds, 0 );
		if ( bounds ) {
			// Fit bounds to the bounding box
			map.fitBounds( bounds );
		} else {
			map.setCenter( centerAtLatLng );
		}
	}
}
*/
////////////////////////////////////////////////////////////////////////////////
//Get basic balloon marker
function getBasicMarker( latLng, map, title ) {
	return new g.Marker( {
		position: latLng,
		map: map,
		title: title
	} );
}

// This function draws the initial route is the route is enabled.
function drawInitialRoute() {
	// If route is already available, plot it.
	// Loop through routedEntitiesJsonArray and obtain marker positions to plot the polyline.
	// Also update the ranks of the routed entities baloon markers
	for ( var i = 0; i < routedEntitiesJsonArray.length; i++ ) {
		// Push that point into the polyline
	    var path = polyLine.getPath();
	    path.push( routedEntitiesJsonArray[i].marker.getPosition() );
	    for ( var j = 0; j < allEntitiesJsonArray.length; j++ ) {
	    	if ( routedEntitiesJsonArray[i].marker.getPosition() == allEntitiesJsonArray[j].marker.getPosition() ) {
	    		// Set the rank for the marker
	    	    // Make the marker baloon display the rank
	    	    var image = 'https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=' + (i+1) + '|FF0000|000000';
	    	    var markerImage = new google.maps.MarkerImage( image );
	    	    allEntitiesJsonArray[j].marker.setIcon( image );
	    	}
	    }
	}
}

// This function is called if the baloon marker is left clicked. It creates a square marker on top of the baloon marker
// and adds it to the array of square markers. If a square marker is already on the baloon marker, it means that the entity is already in the
// route. So return after giving a message to the user.
function markerLeftClick( event, tagName ) {
	// Only if actual route is not being shown for the tagName, then execute this function. Otherwise, exit.
	if ( !showingActualRoute[tagName] ) {
		if ( event.latLng ) {
			// If the latLng already exists in routedEntitiesJsonArray then alert the user that the entity is already in a route
			for ( var i = 0; i < routedEntitiesJsonArray.length; i++ ) {
				if ( routedEntitiesJsonArray[i].marker.getPosition() == event.latLng ) {
					var r = confirm( JSMessages.routingremovefromroutemsg + '?' );
					if ( r == true ) {
						// Remove the marker from the route. TODO: Rename this function to remove entity from route.
						sqmarkerLeftClick( event, tagName );
						return;
					} else {
					return;
					}
				}
			}
			// Create a square marker where the mouse was clicked and push it into sqmarkers array
		    var sqmarker = createMarker( event.latLng, tagName );
		    // Push that point into the polyline
		    var path = polyLine.getPath();
		    path.push( event.latLng );
		    
		    // Create an entityJson object and push it into the routedEntitiesJsonArray
		    var entityJson = {};
		    // Iterate through allEntitiesJsonArray. Get the element whose marker position matches event.latLng
		    for ( var i = 0; i < allEntitiesJsonArray.length; i++ ) {
		    	var jsonElem = allEntitiesJsonArray[i];
		    	if( jsonElem.marker.getPosition() == event.latLng ) {
		    		entityJson.kid = jsonElem.kid;
		    		entityJson.title = jsonElem.title;
		    		entityJson.marker = sqmarker;
		    		entityJson.tag = jsonElem.tag;
		    		// break the loop
		    		break;
		    	}
		    }
		    routedEntitiesJsonArray.push( entityJson );
		    
		    // Set the last operation to add
		    lastOperation = 'add';
		    // Set the lastMarkerAddedOrRemoved to sqmarker
		    lastMarkerAddedOrRemoved = sqmarker;
		    // Set the last position
		    lastMarkerPosition = routedEntitiesJsonArray.length;
		    
		    // It means the user has changed something on the map. Enable the save and undo buttons in the maproutingcontrol panel.
		   showMapRoutingControlPanel( tagName );
		    
		    // Make the marker baloon display the rank
		    var image = 'https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=' + routedEntitiesJsonArray.length + '|FF0000|000000';
		    var markerImage = new google.maps.MarkerImage( image );
		    		    
			 // The latLng already exists in allEntitiesJsonArray. Get the marker corresponding to this event and change it's image
			for ( var i = 0; i < allEntitiesJsonArray.length; i++ ) {
				if ( allEntitiesJsonArray[i].marker.getPosition() == event.latLng ) {
					allEntitiesJsonArray[i].marker.setIcon( markerImage );
					break;
				}
			}
		}
		event = null;
	}
};
   	
    
// This function initializes the polyline. The arrow thickness and color can be changed here.
var initPolyline = function() {
	var lineSymbol = {
			  path: g.SymbolPath.FORWARD_CLOSED_ARROW
			};
    var polyOptions = {
	icons: [{
	    icon: lineSymbol,
	    offset: '100%'
	  }],
    strokeColor: '#0000FF',
    strokeOpacity: 1,
    strokeWeight: 6
    };
    
    polyLine = new g.Polyline(polyOptions);
    
    polyLine.setMap(map);
    
};

//This function initializes the polyline. The arrow thickness and color can be changed here.
var initActualPolyline = function() {
	var lineSymbol = {
			  path: g.SymbolPath.FORWARD_CLOSED_ARROW
			};
    var polyOptions = {
	icons: [{
	    icon: lineSymbol,
	    offset: '100%'
	  }],
    strokeColor: '#FF0000',
    strokeOpacity: 1,
    strokeWeight: 3,
    visible: true,
    zIndex: g.Marker.MAX_ZINDEX + 1
    };
    
    actualPolyLine = new g.Polyline(polyOptions);
    
    actualPolyLine.setMap(map);
};
 
function initActualRouteJsonArray( locations, tagName, latLngBounds ) {
	var centerAtLatLng;
	
	// Obtain the latLng from the locations json object and add it to the actualRouteJsonArray
	// Since transactions are returned in the descending order of time, parse the locations array backwards first and plot polyline points.
	var k = 1; // This is used to display the rank on the actual route
	for( var i = locations.length - 1 ; i >= 0; i-- ) {
		var tloc = locations[i]; // tloc holds transaction data
		// Create a json object containing the transaction data
		var transactionJson = {};
		transactionJson.tid = tloc.tid;
		transactionJson.title = tloc.title;
		var latLng = new g.LatLng( tloc.lat, tloc.lng );
		var marker = createTransactionMarker( latLng, transactionJson.title, k );
		var contentStr = '<html><div style="width:100 px"><b>' + tloc.title + '</b><br/>'+ tloc.lat + ', ' + tloc.lng;
		if ( tloc.accuracy )
			contentStr += ' (' + JSMessages.accuracy +': ' + tloc.accuracy + ' ' + JSMessages.meters + ')';
		contentStr += '</div></html>'; 
		// Create and open the info window by default.
		var infoWindow = new google.maps.InfoWindow({
		    content: contentStr
		});
		transactionJson.marker = marker;
		// Add the transaction.marker to the markerCluster
		markerCluster.addMarker( transactionJson.marker );
		// Add a left click event to the transaction marker just created.
		google.maps.event.addListener( transactionJson.marker, 'click', function( contentStr ) {
		    return function(){
		        infoWindow.setContent( contentStr );//set the content
		        infoWindow.open( map,this );
		    }
		}( contentStr ) );
		
		actualRouteJsonArray.push( transactionJson );
		k++; // Increment the rank on the actual route
		
		// Get the first latLag for centering, only when bounds is not available
		if ( i == locations.length - 1 )
			centerAtLatLng = latLng;
	}
	
	// Get the lat/lng bounds - i.e. the bounding box across all coordinates in this view
	// setCurrentMapCenterOrBounds( locations, latLngBounds, centerAtLatLng );
	setCurrentMapCenterAndBounds( latLngBounds );
}

function createTransactionMarker( point, title, markerindex ) {
	var pinIcon = new google.maps.MarkerImage(
		    'https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=' + markerindex + '|FFFF00|000000'
		);  
	
    var marker = new g.Marker({
    position: point,
    map: map,
    draggable: false,
    visible: true,
    icon: pinIcon,
    title: title, 
    zIndex: g.Marker.MAX_ZINDEX + 1 // Important fix: This makes the actual route marker always appear on top
    });
	return marker;
}

// This function plots the actual route. It iterates through the actualRouteJsonArray and pushes the points into the actualPolyLine
function plotActualRoute() {
	for ( var i = 0; i < actualRouteJsonArray.length; i++ ) {
		// Push that point into the polyline
	    var path = actualPolyLine.getPath();
	    path.push( actualRouteJsonArray[i].marker.getPosition() );
	}
}

// This function creates a square marker. It also sets the mouse over, mouse out  and mouse left click handlers for the square marker.
// The square marker that is created is returned to the caller function.
var createMarker = function( point, tagName ) {
	// Normal square marker image
    var imageNormal = new g.MarkerImage(
    "/images/square.png",
    new g.Size(10, 10),
    new g.Point(0, 0),
    new g.Point(5, 5),
    new g.Size( 10,10 )//11,11
    );
    
    var marker = new g.Marker({
    position: point,
    draggable: false,
    map: map,
    icon: imageNormal,
    });
    
    return marker;
    };

// Callback function that is executed when the left mouse button is clicked on the square marker
function sqmarkerLeftClick( event, tagName ) {
	// Only if actual route is not being shown for the tagName, then execute this function. Otherwise, exit.
	if ( !showingActualRoute[tagName] ) {
		// Get the latLng for the marker
		// var latLng = this.getPosition();
		var latLng = event.latLng;
		// Iterate through allEntitiesJsonArray and obtain the corresponding baloon marker and set it's icon to null
		for ( var i = 0; i < allEntitiesJsonArray.length; i++ ) {
			if ( allEntitiesJsonArray[i].marker.getPosition() == latLng ) {
				allEntitiesJsonArray[i].marker.setIcon( null );
			}
		}
	    // Remove the sqmarker from the routedEntitiesJsonArray
	    for ( var m = 0; m < routedEntitiesJsonArray.length; m++ ) {
	    	if ( routedEntitiesJsonArray[m].marker.getPosition() == latLng ) {
	    		// Set the last operation to remove
	    	    lastOperation = "delete";
	    	    
	    	    // Set the lastMarkerAddedOrRemoved to this
	    	    lastMarkerAddedOrRemoved = routedEntitiesJsonArray[m].marker;
	    	    
	    	    // Set the last position 
	    	    lastMarkerPosition = m;
	    	    
	    		routedEntitiesJsonArray[m].marker.setMap(null);
	    		routedEntitiesJsonArray.splice( m, 1 );
	    		// Remove the point from the polyline
	    	    polyLine.getPath().removeAt(m);
	    	    break;
	    	}
	    }
	    
	    // Update the ranks
	    // Iterate through the routedEntitiesJsonArray
	    for ( var i = 0; i < routedEntitiesJsonArray.length; i++ ) {
	    	for ( var j = 0; j < allEntitiesJsonArray.length; j++ ) {
	    		if ( allEntitiesJsonArray[j].marker.getPosition() == routedEntitiesJsonArray[i].marker.getPosition() ) {
	    			// Change the image of that marker
	    			// Make the marker baloon display the rank
	    			var k = i + 1;
	    		    var image = 'https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=' + k + '|FF0000|000000';
	    		    var markerImage = new google.maps.MarkerImage( image );
	    		    allEntitiesJsonArray[j].marker.setIcon( image );
	    		}
	    	}
	    }
		showMapRoutingControlPanel( tagName );
		m = null;
	}
}

/* This function shows enabled the save and undo buttons in the maproutingcontrolpanel */
function showMapRoutingControlPanel( tagName ) {
	var tagId = getId( tagName );
	// Route is being modified. Show the routing control panel. So, enable the save route and undo route buttons
    $( '#' + tagId + 'maproutingstatus' ).html( emptyTag );
    $( '#' + tagId + 'saveroute' ).removeAttr('disabled' );
    $( '#' + tagId + 'undoroute' ).removeAttr('disabled' );
}

// This function is called when the user clicks the Save Route button.
function saveManagedEntitiesRoute( userID, tagName ) {
	var tagId = getId( tagName );
	showLoader( tagId + 'maproutingstatus' );
	var routedKids = []; // This array holds the kiosk ids of the routed entities
		
	// Obtain the list of routed kids
	// Loop through routedEntitiesJsonArray
	for ( var i = 0; i < routedEntitiesJsonArray.length; i++ ) {
		routedKids.push( routedEntitiesJsonArray[i].kid );
	}
	
	// Get the csv of the routed Kids
	var routecsv = getRouteCsvFromMap( routedKids );
	
	var noroutecsv = '';
	// If the saveRoute button is clicked on any of the tabs, then execute this part of the code
	// tagName is passed as '' if saveRoute button is clicked under the master table of managed entities
	if ( tagName != emptyTag ) {
		// Get a csv of the unsorted kiosk ids
		var unsortedIds = $( "#" + "table tbody" ).sortable( 'toArray' );
		
		var i = 0;
		// Get a csv of the unsorted kiosk ids
		for ( i = 0; i < unsortedIds.length; i++ ) {
			if ( noroutecsv.length > 0 )
				noroutecsv += ',';
			noroutecsv += unsortedIds[i];
		}
	}
	
	// Make an AJAX call to save the route
	// Submit the form here using AJAX
	var url = '/s/createentity';
	var postData = {}; // JSON object containing the servlet post parameters
	postData.action = 'saveordering';
	postData.type = 'me';
	postData.userid = userID;
	postData.routequerystring = 'tag=' + tagName + '&routecsv=' + routecsv + '&noroutecsv=' + noroutecsv;

	$.ajax( { 
			type: 'POST',
			url: url,
			dataType: 'json',
			data: postData,
			success: function(o) {
								$( '#' + tagId + 'maproutingstatus' ).html( '<div style="color: black;float: left">' + JSMessages.routingsuccessfullysavedmsg + '.' + '</div><br/>' );
								$( '#' + tagId + 'saveroute').attr('disabled', true ); // Disable the Save route button
								$( '#' + tagId + 'undoroute').attr( 'disabled', true ); // Disable the Cancel route button
								},
			error: function(o) {
								alert( 'Error while saving route. ' + o.responseText );
								$( '#' + tagId + 'maproutingstatus' ).html( '<div style="color: red;float: left">Error. Message: ' + o.responseText + '</div><br/>' ); // Dislay an error message to the user
								}			
		});
}

// Get the csv of the kids of all the routed entities. Not all entities may be routed.
// So, get the routed Kids csv. And append the csvs of the kids of the unrouted entities to it.
function getRouteCsvFromMap( routedKids ) {
	var routecsv = '';
	// Get a csv of rhe routedKids and ( unroutedKids if any )
	if ( routedKids ) {
		for ( var i = 0; i < routedKids.length; i++ ) {
			routecsv += routedKids[i];
			if ( i < ( routedKids.length - 1 ) )
				routecsv += ',';
		}
	}
	return routecsv;
}

// Utility function to get the difference between two arrays
function difference( array1, array2 ) {
	var result=[];
	for ( var i = 0 ; i < array1.length; i++ ) {
		if ( array2.indexOf( array1[i] ) == -1 ) {
			result.push( array1[i] )
		}
	}
	return result;
}

// This function is called when the user clicks the Undo Route button. It cancels the last operation.
// Suppose the last operation was add, this function removes the last added square marker and removes that
// point from the polyline.
// Suppose the last operation was delete, this function puts back the square marker and adds that point to the polyline.
// In both cases the rank of the entity is also correspondingly changed.
function undoRoute( tagName ) {
	var tagId = getId( tagName );
	// Get the last operation
	if ( lastOperation ) {
		if ( lastOperation == 'add' ) {
			// Change the marker to look like one without rank
			for ( var i = 0; i < allEntitiesJsonArray.length; i++ ) {
				if ( allEntitiesJsonArray[i].marker.getPosition() == lastMarkerAddedOrRemoved.getPosition() ) {
					// Change the icon of the corresponding marker to default.
					allEntitiesJsonArray[i].marker.setIcon( null );
				}
			}
			// Remove the marker that was added to the routedEntitiesJsonArray.
			for ( var i = 0; i < routedEntitiesJsonArray.length; i ++ ) {
				if ( routedEntitiesJsonArray[i].marker == lastMarkerAddedOrRemoved ) {
					lastMarkerAddedOrRemoved.setMap( null );
					routedEntitiesJsonArray.splice ( i, 1 );
					// Remove this point from the polyLine
					polyLine.getPath().removeAt( i );
				    break;
				} 
			}
		}
		if ( lastOperation == 'delete' ) {
			// If it is delete then add the sqmarker at the lastMarkerPosition into the sqmarkers array and into the polyline.
			// Create a sqmarker using lastMarkerAddedOrRemoved coordinates
			var newMarker = createMarker( lastMarkerAddedOrRemoved.getPosition(), tagName );
			// Obtain the kiosk details for this routed entity from allEntitiesJsonArray
			for ( var i = 0; i < allEntitiesJsonArray.length; i++ ) {
				if ( allEntitiesJsonArray[i].marker.getPosition() == newMarker.getPosition() ) {
					var entityJson = {};
					entityJson.kid = allEntitiesJsonArray[i].kid;
					entityJson.title = allEntitiesJsonArray[i].title;
					entityJson.marker = newMarker;
					entityJson.tag = allEntitiesJsonArray[i].tag;
					// Add the entityJson to routedEntitiesJsonArray
					routedEntitiesJsonArray.splice( lastMarkerPosition, 0, entityJson );
				}
			}
			// Insert the lastLng into the polyLine at the lastMarkerPosition
			polyLine.getPath().insertAt( lastMarkerPosition, newMarker.getPosition() );
			// Update the ranks for the markers
			// Iterate through sqmarkers
		    for ( var i = 0; i < routedEntitiesJsonArray.length; i++ ) {
		    	for ( var j = 0; j < allEntitiesJsonArray.length; j++ ) {
		    		if ( allEntitiesJsonArray[j].marker.getPosition() == routedEntitiesJsonArray[i].marker.getPosition() ) {
		    			// Change the image of that marker
		    			// Make the marker baloon display the rank
		       		    var image = 'https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld=' + ( i+1 ) + '|FF0000|000000';
		    		    var markerImage = new google.maps.MarkerImage( image );
		    		    allEntitiesJsonArray[j].marker.setIcon( image );
		    		}
		    	}
		    }
		}
		//Disable the undo route button
	   	$( '#' + tagId + 'undoroute' ).attr( 'disabled', true );
		// If there are no sqmarkers in the routedEntitiesJsonArray and if there are no points in the polyline, then disable the save button also. Otherwise, leave it as it is.
	   	if ( routedEntitiesJsonArray.length == 0 && polyLine.length == 0 )
	   		$( '#' + tagId + 'saveroute' ).attr('disabled', true );
	}
}

// This function is called when the Reset Route button is clicked. Upon confirmation from the user this method
// resets the route (which means it removes any polyline that was drawn). 
function resetManagedEntitiesRoute( userID ) {
	// Call the createentity servlet to reset ordering
	// Ask for user confirmation before calling servlet
	var r = confirm( 'All the routing that you have done so far will be lost. Are you sure you want to continue?' );
	if ( r == false ) {
		$( '#maproutingstatus' ).html( emptyTag );
		return;
	}
	
	// Show a loading indicator
	showLoader( 'maproutingstatus' );
	// Pass the map parameter to the CreateEntityServlet so that the user is redirected to the map view.
	location.href = '/s/createentity?action=resetordering&type=me&userid=' + userID+'&map';
}

// This method resets all the global variables. It is not being used at the moment, and can be safely removed.
function clearRoute() {
	// Reset the map, markers, sqmarkers, polyline and call initialize again
	allEntitiesJsonArray = [];
	routedEntitiesJsonArray = [];
	polyline = null;
	map = null;
}

function saveManagedEntitiesOrdering( userID, tagName ) {
	var tagId = getId( tagName );
	// Show a loading indicator
	showLoader( tagId + 'tableorderingstatus' );
	var sortedIds = $( "#" + tagId + "table tbody" ).sortable( 'toArray' );
	var routecsv = '';
	var i = 0;
	// Get a csv of the sorted kiosk ids
	for ( i = 0; i < sortedIds.length; i++ ) {
		if ( routecsv.length > 0 )
			routecsv += ',';
		routecsv += sortedIds[i];
	}
	
	var noroutecsv = '';
	// If the saveOrdering button is clicked on any of the tabs, then execute this part of the code
	// tagName is passed as '' if saveOrdering button is clicked under the master table of managed entities
	if ( tagName != emptyTag ) {
		// Get a csv of the unsorted kiosk ids
		var unsortedIds = $( "#" + "table tbody" ).sortable( 'toArray' );
		
		var i = 0;
		// Get a csv of the unsorted kiosk ids
		for ( i = 0; i < unsortedIds.length; i++ ) {
			if ( noroutecsv.length > 0 )
				noroutecsv += ',';
			noroutecsv += unsortedIds[i];
		}
	}
	// Submit the form here using AJAX
	var url = '/s/createentity';
	var postData = {}; // JSON object containing the servlet post parameters
	postData.action = 'saveordering';
	postData.type = 'me';
	postData.userid = userID;
	postData.routequerystring = 'tag=' + tagName + '&routecsv=' + routecsv + '&noroutecsv=' + noroutecsv;
	
	$.ajax( { 
			type: 'POST',
			url: url,
			dataType: 'json',
			data: postData,
			success: function(o) {
								$( '#' + tagId + 'tableorderingstatus' ).html( '<div style="color: black;float: left">' + JSMessages.orderingsuccessfullysavedmsg + '</div><br/>' );
								// Disable the Save ordering button
						    	$( '#' + tagId + 'saveordering').attr('disabled', true );
						    	// Disable the Cancel ordering button
						    	$( '#' + tagId + 'undoordering').attr('disabled', true );
								},
			error: function(o) {
								alert( JSMessages.orderingerrormsg + o.responseText );
								$( '#' + tagId + 'tableorderingstatus').html( '<div style="color: red;float: left">' + JSMessages.orderingerrormsg + '. Message: ' + o.responseText + '</div><br/>' ); // Dislay an error message to the user
								}			
		});
	
	// Only if tags are configured
	if ( tagName != emptyTag ) {
		// After saving the entities ordering, the tab name should be changed to show the new count of entities under every tag. 
		var selectedTab = $( '#tagstab' ).tabs( 'option', 'selected' );
		$("#tagstab ul li a").eq( selectedTab ).text( tagName + ' [' + sortedIds.length + ']' );
		// Update the number of entities under the selected tag and the untagged entities
		var tmeNumKiosksDiv = document.getElementById( tagId + 'numkiosks' );
		tmeNumKiosksDiv.innerHTML = '<b>' + sortedIds.length + '</b> ' + JSMessages.kiosks;
		var utmeNumKiosksDiv = document.getElementById( 'untaggedmanagedentitiesnumkiosks' );
		utmeNumKiosksDiv.innerHTML = '<b>'+ unsortedIds.length + '</b> ' + JSMessages.kiosks;
	}
}

//Function to undo the ordering in the table view
function undoOrdering( origKids, tagName ) {
	var tagId = getId( tagName );
	$( '#' + tagId + 'table tbody' ).sortable( 'cancel' );
	// Disable the Undo Ordering button
	$( '#' + tagId + 'undoordering').attr('disabled', true );
	// Check if the items in the sortable are in the same order as it was when it was initialized. If yes, then disable the save ordering button.
	// Otherwise, leave it as it is.
	var sortedKids = $( '#' + tagId + 'table tbody' ).sortable( 'toArray' );
	
	if ( arraysIdentical( origKids, sortedKids ) ) {
		// Disable the Save ordering button
	   	$( '#' + tagId + 'saveordering').attr( 'disabled', true );
	}
	return false;
}

//Function to reset ordering in the table view. This calls the servlet and gets the kiosks (managed entities) in alphabetical order
function resetManagedEntitiesOrdering( userID, tagName ) {
	var tagId = getId( tagName );
	// Call the createentity servlet to reset ordering
	// Ask for user confirmation before calling servlet
	var r = confirm( JSMessages.orderingresetmsg + '?');
	if ( r == false ) {
		$( '#' + tagId + 'tableorderingstatus').html( emptyTag );
		return;
	}
	// Show a loading indicator
	showLoader( tagId + 'tableorderingstatus' );
	location.href = '/s/createentity?action=resetordering&type=me&userid=' + userID ;
}

//Show the loading indicator while ordering managed entities in the table view
function showLoader( elemId ) {
	var div = document.getElementById( elemId );
	if ( div )
		div.innerHTML = '<img src="/images/loader.gif"/>';
}

function arraysIdentical( a, b ) {
	    var i = a.length;
	    if ( i != b.length ) return false;
	    while ( i-- ) {
	        if (a[i] !== b[i]) return false;
	    }
	    return true;
}

function saveRelatedEntitiesOrdering( kioskID, linkType, tagName, baseUrl ) {
	var tagId = getId( tagName );
	// Show a loading indicator
	showLoader( tagId + 'tableorderingstatus' );
	
	var sortedIds = $( "#" + tagId + "table tbody" ).sortable( 'toArray' );
	var routecsv = '';
	var i = 0;
	// Get a csv of the sorted kiosk ids
	for ( i = 0; i < sortedIds.length; i++ ) {
		if ( routecsv.length > 0 )
			routecsv += ',';
		routecsv += sortedIds[i];
	}
	
	var noroutecsv = '';
	// If the saveOrdering button is clicked on any of the tabs, then execute this part of the code
	// tagName is passed as '' if saveOrdering button is clicked under the master table of linked kiosks
	if ( tagName != emptyTag ) {
		// Get a csv of the unsorted kiosk ids
		var unsortedIds = $( "#" + "table tbody" ).sortable( 'toArray' );
		
		var i = 0;
		// Get a csv of the unsorted kiosk ids
		for ( i = 0; i < unsortedIds.length; i++ ) {
			if ( noroutecsv.length > 0 )
				noroutecsv += ',';
			noroutecsv += unsortedIds[i];
		}
	}
	
	// Submit the form here using AJAX
	var url = '/s/createentity';
	var postData = {}; // JSON object containing the servlet post parameters
	postData.action = 'saveordering';
	postData.type = 're';
	postData.linktype= linkType;
	postData.kioskid = kioskID;
	postData.routequerystring = 'tag=' + tagName + '&routecsv=' + routecsv + '&noroutecsv=' + noroutecsv; 
	
	$.ajax( { 
		type: 'POST',
		url: url,
		dataType: 'json',
		data: postData,
		success: function(o) {
							$( '#' + tagId + 'tableorderingstatus' ).html( '<div style="color: black;float: left">' + JSMessages.orderingsuccessfullysavedmsg + '</div><br/>' );
							// Disable the Save ordering button
					    	$( '#' + tagId + 'saveordering').attr('disabled', true );
					    	// Disable the Cancel ordering button
					    	$( '#' + tagId + 'undoordering').attr('disabled', true );
					    	// Reload this page, so that the pagination sequence can be done on the new query (ordered by route, instead of kioks names)
							// setInterval( function() { location.href = baseUrl }, 2000 );
							},
		error: function(o) {
							alert( JSMessages.orderingerrormsg + o.responseText );
							$( '#' + tagId + 'tableorderingstatus').html( '<div style="color: red;float: left">' + JSMessages.orderingerrormsg + '. Message: ' + o.responseText + '</div><br/>' ); // Dislay an error message to the user
							}			
	});
	
	// Only if tags are configured
	if ( tagName != emptyTag ) {
		// After saving the entities ordering, the tab name should be changed to show the new count of entities under every tag. 
		var selectedTab = $( '#tagstab' ).tabs( 'option', 'selected' );
		$("#tagstab ul li a").eq( selectedTab ).text( tagName + ' [' + sortedIds.length + ']' );
		// Update the number of entities under the selected tag and the untagged entities
		var treNumKiosksDiv = document.getElementById( tagId + 'numkiosks' );
		treNumKiosksDiv.innerHTML = '<b>' + sortedIds.length + '</b> ' + JSMessages.kiosks;
		var utreNumKiosksDiv = document.getElementById( 'untaggedrelatedentitiesnumkiosks' );
		utreNumKiosksDiv.innerHTML = '<b>'+ unsortedIds.length + '</b> ' + JSMessages.kiosks;
	}
}

//Function to reset ordering of related entities in the table view. This calls the servlet and gets the kiosks (managed entities) in alphabetical order
function resetRelatedEntitiesOrdering( kioskID, linkType, tagName ) {
	var tagId = getId( tagName );
	// Call the createentity servlet to reset ordering
	// Ask for user confirmation before calling servlet
	var r = confirm( JSMessages.orderingresetmsg + '?');
	if ( r == false ) {
		$( '#' + tagId + 'tableorderingstatus').html( emptyTag );
		return;
	}
	// Show a loading indicator
	showLoader( tagId + 'tableorderingstatus' );
	location.href = '/s/createentity?action=resetordering&type=re&kioskid=' + kioskID + '&linktype=' + linkType;
}

// Function to save the related entities routing on a map
function saveRelatedEntitiesRoute( kioskID, linkType, tagName ) {
	var tagId = getId( tagName );
	showLoader( tagId + 'maproutingstatus' );
	var routedKids = []; // This array holds the kiosk ids of the routed entities
		
	// Obtain the list of routed kids
	// Loop through routedEntitiesJsonArray
	for ( var i = 0; i < routedEntitiesJsonArray.length; i++ ) {
		routedKids.push( routedEntitiesJsonArray[i].kid );
	}
	
	// Get the csv of the routed Kids
	var routecsv = getRouteCsvFromMap( routedKids );
		
	var noroutecsv = '';
	// If the saveRoute button is clicked on any of the tabs, then execute this part of the code
	// tagName is passed as '' if saveRoute button is clicked under the master table of managed entities
	if ( tagName != emptyTag ) {
		// Get a csv of the unsorted kiosk ids
		var unsortedIds = $( "#" + "table tbody" ).sortable( 'toArray' );
		
		var i = 0;
		// Get a csv of the unsorted kiosk ids
		for ( i = 0; i < unsortedIds.length; i++ ) {
			if ( noroutecsv.length > 0 )
				noroutecsv += ',';
			noroutecsv += unsortedIds[i];
		}
	}
	
	// Make an AJAX call to save the route
	// Submit the form here using AJAX
	var url = '/s/createentity';
	var postData = {}; // JSON object containing the servlet post parameters
	postData.action = 'saveordering';
	postData.type = 're';
	postData.linktype= linkType;
	postData.kioskid = kioskID;
	postData.routequerystring = 'tag=' + tagName + '&routecsv=' + routecsv + '&noroutecsv=' + noroutecsv;

	$.ajax( { 
			type: 'POST',
			url: url,
			dataType: 'json',
			data: postData,
			success: function(o) {
								$( '#' + tagId + 'maproutingstatus' ).html( '<div style="color: black;float: left">' + JSMessages.routingsuccessfullysavedmsg + '.' + '</div><br/>' );
								$( '#' + tagId + 'saveroute').attr('disabled', true ); // Disable the Save route button
								$( '#' + tagId + 'undoroute').attr( 'disabled', true ); // Disable the Cancel route button
								},
			error: function(o) {
								alert( 'Error while saving route. ' + o.responseText );
								$( '#' + tagId + 'maproutingstatus' ).html( '<div style="color: red;float: left">Error. Message: ' + o.responseText + '</div><br/>' ); // Dislay an error message to the user
								}			
		});
}

//This function is called when the Reset Route button is clicked on the related entities page ( kiosklinks.jsp ). Upon confirmation from the user this method
//resets the route (which means it removes any polyline that was drawn). 
function resetRelatedEntitiesRoute( kioskID, linkType ) {
	// Call the createentity servlet to reset ordering
	// Ask for user confirmation before calling servlet
	var r = confirm( 'All the routing that you have done so far will be lost. Are you sure you want to continue?' );
	if ( r == false ) {
		$( '#maproutingstatus' ).html( '' );
		return;
	}
	
	// Show a loading indicator
	showLoader( 'maproutingstatus' );
	// Pass the map parameter to the CreateEntityServlet so that the user is redirected to the map view.
	location.href = '/s/createentity?action=resetordering&type=re&kioskid=' + kioskID + '&linktype=' + linkType + '&map';
}

//This function is called when the Show Actual Route/Hide Actual Route hyperlink is clicked in the actual route filter panel.
//If the filter panel is hidden is shows it and if is shown it hides it. The text on the hyperlink is changed accordingly.
//In addition, if the Hide Actual Route hypelink is clicked, this function hides the actual route that is already shown on the map.
function toggleActualRouteFilterPanel( tagName ) {
	var tagId = getId( tagName );
	 if ( $( '#' + tagId + 'actualRouteFilterPanel' ).is( ':hidden' ) ) {
		$( '#' + tagId + 'actualRouteFilterPanel' ).show();
		// Change the link text to Hide Actual Route
		$( '#' + tagId + 'showActualRouteLink' ).text( JSMessages.routing_hideactualroute );
	 }
	 else {
		$( '#' + tagId + 'actualRouteFilterPanel' ).hide();
		// Get the mapdiv
		var mapdiv = document.getElementById( tagId + 'routemap' );
		// Hide the actual route that is already shown in the map
		hideActualRoute( mapdiv, tagName );
		// Change the link text to Hide Actual Route
		$( '#' + tagId + 'showActualRouteLink' ).text(  JSMessages.routing_showactualroute  );
		// Hide the actualroutegeoinfo
		$( '#' + tagId + 'actualroutegeoinfo' ).html( '' );
		// Hide the road route distance info, and map panel
		$( '#' + tagId + 'roadrouteinfo' ).html( '' );
		$( '#' + tagId + 'roadroutepanel' ).hide();
	 }
}

//Get formatted date as required by Logistimo report generator (irrespective of the US or non-US date formats)
//Format: dd/mm/yyyy
function getFormattedDate( dateId ) {
	var d = $( '#' + dateId ).datepicker( "getDate" );
	var date = d.getDate() + '';
	if ( date.length == 1 )
		date = '0' + date;
	var month = ( d.getMonth() + 1 ) + '';
	if ( month.length == 1 )
		month = '0' + month;
	return date + '/' + month + '/' + d.getFullYear();
}


//////////////////////// Functions for plotting road route using Google Directions service ///////////////////////////////////

function plotRoadRoute( geoData, mapId, latLngBounds ) {
    if ( geoData.length < 2 ) {
    	document.getElementById( mapId + 'title' ).innerHTML = JSMessages.nodataavailable;
    	return;
    }
    updateRoadRouteInfoPanel( mapId, true, undefined );
 	// Init. directions service and display
    directionsService = new google.maps.DirectionsService();
    directionsDisplay = new google.maps.DirectionsRenderer();
    // Init. map
    var mapOptions = {
            	zoom: 5,
            	mapTypeId: google.maps.MapTypeId.ROADMAP
            };
    roadmap = new google.maps.Map( document.getElementById( mapId ), mapOptions );
   /* if ( geoData.latlngbounds && geoData.latlngbounds.length > 0 )
    	roadmap.fitBounds( getLatLngBounds( geoData.latlngbounds ) );
    */
    if ( latLngBounds )
    	roadmap.fitBounds( latLngBounds );
    // Update directions display with map
    directionsDisplay.setMap( roadmap );
    // Get route request for directions service and plot
    // NOTE: given a limit on number of waypoints (8 for free and 23 for premier maps customers), we need to map routes 8 way-points at a time;
    // 		 also, we iterate over geoData.geocodes in the reverse order, given it is ordered in descending order of time of order/trans. 
    var startIndex = geoData.length - 1;
    var endIndex = 0;
    if ( geoData.length > ( MAX_WAYPOINTS + 2 ) )
        endIndex = startIndex - MAX_WAYPOINTS - 1;
    // Reset directions results
    directionsResults = [];
    // Get results recursively
    plotRoadRouteWithWaypoints( mapId, geoData, startIndex, endIndex, 0, 0 );
}

function updateRoadRouteInfoPanel( mapId, showLoader, distance ) {
    var infoDiv = document.getElementById( mapId + 'info' );
    if ( !infoDiv )
    	return;
	if ( showLoader ) {
		// Show loading indicator
	    infoDiv.innerHTML = '<img src="/images/loader.gif" valign="top"/> Fetching road route...';
	} else {
		infoDiv.innerHTML = '<font style="color:red"><b>' + distance + ' Kms.</b> travelled</font>';
	}
}

// Hide road route elements
function hideRoadRoute( mapId ) {
	var infoDiv = document.getElementById( mapId + 'info' );
	if ( infoDiv )
		infoDiv.innerHTML = '';
	var panelDiv = document.getElementById( mapId + 'panel' );
	if ( panelDiv )
		panelDiv.style.display = 'none';
}

function plotRoadRouteWithWaypoints( mapId, geoData, startIndex, endIndex, distance, legs ) {
	// Get the directions service
    var directionsRequest = getDirectionsRequest( geoData, startIndex, endIndex );
    if ( !directionsRequest ) {
    	hideRoadRoute( mapId );
    	alert( 'Unable to get complete road route on the map. Choose a different time range and try again.' );
    	return;
    }
    directionsService.route( directionsRequest, function( response, status ) {
	    	if ( status == google.maps.DirectionsStatus.OK ) {
		    	// Update the directions results array
		    	directionsResults.push( response );
		    	// Get route
		    	var route = response.routes[0];
		    	// Get the distance of this route
		    	for ( var i = 0; i < route.legs.length; i++ )
			    	distance += route.legs[i].distance.value;
		    	legs += route.legs.length;
		    	// Update start/end indices
			    if ( endIndex == 0 ) { // update route map; end recursion
			    	var distanceKms = ( distance / 1000 );
			    	// Show distance text
			    	document.getElementById( mapId + 'title' ).innerHTML = JSMessages.distance + ': <b>' + distanceKms + ' Kms</b>, ' + JSMessages.placesvisited + ': <b>' + ( legs + 1 ) + '</b>';
			    	// Show the route on map, if present
			        if ( directionsResults.length > 0 ) {
				    	// Update the route map
				    	updateRoadRouteMap( mapId, 0, legs + 1 );
			        }
			        // Update road route info. panel, if it exists
			        updateRoadRouteInfoPanel( mapId, false, distanceKms );
			        var roadmapPanel = document.getElementById( mapId + 'panel' );
			        if ( roadmapPanel ) {
			        	roadmapPanel.style.display = 'block';
			        	google.maps.event.trigger( roadmap, 'resize' ); // refresh it
			        }
		            return; // ends recursion
			    } else {
				    startIndex = endIndex;
				    endIndex = ( startIndex - MAX_WAYPOINTS - 1 );
				    if ( endIndex < 0 )
					    endIndex = 0;
			    }
			    // Recursively plot
			    plotRoadRouteWithWaypoints( mapId, geoData, startIndex, endIndex, distance, legs );
		    } else {
		    	// Get the error message and update the status area
		    	var msg = 'Unable to get route.';
		    	if ( status == google.maps.DirectionsStatus.NOT_FOUND )
		    		msg += ' At least one of the locations could not be geocoded.';
		    	else if ( status == google.maps.DirectionsStatus.ZERO_RESULTS )
		    		msg += ' No road route could be found between the specified points.';
		    	else if ( status == google.maps.DirectionsStatus.OVER_QUERY_LIMIT )
		    		msg += ' Requests to Google Maps have exceeded the limit within the allowed time. Please contact administrator.';
		    	else if ( status == google.maps.DirectionsStatus.REQUEST_DENIED )
		    		msg += ' Access to Google Directions service is denied. Please contact administrator.';
		    	else if ( status == google.maps.DirectionsStatus.UNKNOWN_ERROR )
		    		msg += ' A server error may have occurred. Please try again.';
		    	// Update status on screen
		    	document.getElementById( mapId + 'title' ).innerHTML = msg;
			}
	    }
    );
}

// Get the directions request JSON from the given geoData
function getDirectionsRequest( geoData, startIndex, endIndex ) {
    var geoCodes = geoData; //.geocodes;
    if ( !geoCodes || geoCodes.length == 0 )
    	return undefined;
    var start = new google.maps.LatLng( geoCodes[startIndex].lat, geoCodes[startIndex].lng );
    var end = new google.maps.LatLng( geoCodes[endIndex].lat, geoCodes[endIndex].lng );
    var waypts = [];
    for ( var i = startIndex - 1; i > endIndex; i-- ) {
        waypts.push( { location: new google.maps.LatLng( geoCodes[i].lat, geoCodes[i].lng ), stopover: true } );
    }
    var request = {
            	origin: start,
            	destination: end,
            	waypoints: waypts,
            	travelMode: google.maps.TravelMode.DRIVING
            };
    return request;
}

// Get the lat-lng bounds for the map
function getLatLngBounds( latLngBoundsArray ) {
	if ( latLngBoundsArray.length < 4 )
		return null;
	var northEast = new google.maps.LatLng( latLngBoundsArray[ 0 ], latLngBoundsArray[ 1 ] );
	var southWest = new google.maps.LatLng( latLngBoundsArray[ 2 ], latLngBoundsArray[ 3 ] );
	return new google.maps.LatLngBounds( southWest, northEast );
}

// Update the route map navigator
function updateRoadRouteMap( mapId, directionsResultsIndex, totalPlacesVisited ) {
	if ( directionsResultsIndex >= directionsResults.length ) {
		alert( 'Could not find route map for ' + directionsResultsIndex + '. Please reload page and try again.' );
		return;
	}
	// Get the route to be plotted
	var directionsResult = directionsResults[ directionsResultsIndex ];
	// Update the map
	directionsDisplay.setDirections( directionsResult );
	// Get the prev. and next navigation texts
	var prev = null, next = null;
	if ( directionsResultsIndex > 0 )
		prev = '<a onclick="javascript:updateRoadRouteMap(\'' + mapId + '\', ' + ( directionsResultsIndex - 1 ) + ',' + totalPlacesVisited + ')">&lt;' + JSMessages.prev + '</a>';
	if ( directionsResultsIndex < ( directionsResults.length - 1 ) )
		next = '<a onclick="javascript:updateRoadRouteMap(\'' + mapId + '\', ' + ( directionsResultsIndex + 1 ) + ',' + totalPlacesVisited + ')">' + JSMessages.next + '&gt;</a>';
	// Update navigation text
	var navtext = '';
	if ( prev != null )
		navtext += prev + ' &nbsp;';
	// Get the range of plages plotted
	var rangeStart = ( directionsResultsIndex * ( MAX_WAYPOINTS + 1 ) ) + 1; /// earlier: ( directionsResultsIndex * ( MAX_WAYPOINTS + 2 ) ) + 1;
	var rangeEnd = rangeStart + directionsResult.routes[0].legs.length;
	navtext += '<b>' + rangeStart + '-' + rangeEnd + '</b> ' + JSMessages.of + ' <b>' + totalPlacesVisited + '</b> ' + JSMessages.places;
	if ( next != null )
		navtext += ' &nbsp;' + next;
	document.getElementById( mapId + 'navigator' ).innerHTML = navtext;
}

function saveOrdering( type, userID, tagName, kioskID, linkType, baseUrl ) {
	if ( type == 'managedentities' )
		saveManagedEntitiesOrdering( userID, tagName );
	if ( type == 'relatedentities' )
		saveRelatedEntitiesOrdering( kioskID, linkType, tagName, baseUrl );
}

function resetOrdering( type, userID, tagName, kioskID, linkType ) {
	if ( type == 'managedentities' )
		resetManagedEntitiesOrdering( userID, tagName );
	if ( type == 'relatedentities' )
		resetRelatedEntitiesOrdering( kioskID, linkType, tagName );
}

function saveRoute( type, userID, tagName, kioskID, linkType, baseUrl ) {
	if ( type == 'managedentities' )
		saveManagedEntitiesRoute( userID, tagName );
	if ( type == 'relatedentities' )
		saveRelatedEntitiesRoute( kioskID, linkType, tagName, baseUrl );
}

function resetRoute( type, userID, tagName, kioskID, linkType ) {
	if ( type == 'managedentities' )
		resetManagedEntitiesRoute( userID );
	if ( type == 'relatedentities' )
		resetRelatedEntitiesRoute( kioskID, linkType );
}