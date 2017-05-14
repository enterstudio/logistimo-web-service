// Google map
var map; // globally accessible
var infoWindow; // globally accessible, to show info. content on click of a marker
// Pagination size
var SIZE = 50;
var markers=[];
var markerCluster; // MarkerClusterer
var oms; // OverlappingMarkerSpiderfier
var CLUSTER_MAXZOOM = 15;
// Initialize map
function initialize( materialId, type, geoCodesUrlBase ) {
	 // Init. map
	  var myOptions = {
	    zoom: 5,
	    mapTypeId: google.maps.MapTypeId.ROADMAP
	  }
	  map = new google.maps.Map( document.getElementById( "imap" ), myOptions );
	  infoWindow = new google.maps.InfoWindow();
	  
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
    	    	  if ( type == 'stock' ) {
	    	    	  console.log( 'There are ' + mks.length + ' entities at this location. Click to see stock level in all the entities at this location.' );
	    	    	  alert( 'There are ' + mks.length + ' entities at this location. Click to see stock level in all the entities at this location.');
    	    	  }
    	    	  if ( type == 'demand' ) {
    	    		  console.log( 'There are ' + mks.length + ' entities at this location. Click to see demand in all the entities at this location.' );
    	   	    	  alert( 'There are ' + mks.length + ' entities at this location. Click to see demand in all the entities at this location.');
    	    	  }
    	    	  if ( type == 'kiosks' ) {
    	    		  console.log( 'There are ' + mks.length + ' entities at this location. Click to see the details of all the enities at this location.' );
    	    		  alert( 'There are ' + mks.length + ' entities at this location. Click to see the details of all the enities at this location.' );
    	    	  }
    	    	  google.maps.event.trigger( mks[mks.length-1], 'click' ); // This should but it does not actually spiderfy the markers.
    	      }
		}
	  });	
	  
	  // Get and update locations
	  getGeocodesAndMap( materialId, type, geoCodesUrlBase, SIZE, 0, 0, 0, null );
}

// Get geo-codes and map them
function getGeocodesAndMap( materialId, type, geoCodesUrlBase, size, offset, mappedSoFar, maxValueSoFar, latLngBoundsSoFar ) {
	// Form the URL
	var url = geoCodesUrlBase + '&s=' + size + '&o=' + offset;
	// Show the geo-data loader
	showGeoInfo( true, size, mappedSoFar );
	$.ajax( {
		 url: url,
		 dataType: 'json',
		 success: function(o) {
			 console.log( o );
			 	var maxValue = maxValueSoFar;
			 	var totalMapped = mappedSoFar;
			 	var latLngBounds = latLngBoundsSoFar;
			 	var geoData = o;
			 	
				if ( geoData.size > 0 ) { // map the points
					// Get the updated max. value and lat-lng bounds (TODO: do we need to re-plot all markers with the new maxValue so scaling happens correctly for material pins)
					maxValue = getUpdatedMaxValue( maxValueSoFar, geoData.maxvalue );
					// latLngBounds = getUpdatedLatLngBounds( latLngBoundsSoFar, geoData.latlngbounds );
					latLngBounds = mygetUpdatedLatLngBounds( latLngBoundsSoFar, geoData.geocodes );
					// Map the data
					initKioskMarkers( type, geoData.geocodes, maxValue, latLngBounds, materialId );
					totalMapped = mappedSoFar + geoData.size;
				}
				// Check if more data exists
				if ( geoData.hasMoreResults ) {
					offset = parseInt(offset, 10) + size;
					getGeocodesAndMap(materialId, type, geoCodesUrlBase, size, offset, totalMapped, maxValue, latLngBounds); // recursive call...
				} else {
					if ( totalMapped == 0 )
						document.getElementById('imap').innerHTML = JSMessages.nodataavailable;
					showGeoInfo( false, size, totalMapped );
				}
			 },
  		 error: function(o) {
	  		 	alert( o.responseText );
	  		 }
		} );
}

// Initialize kiosk markers
function initKioskMarkers( type, locations, maxValue, latLngBounds, materialId ) {
	// Get the max stock
	var	maxStock;
	var maxQuantity;
	var centerAtLatLng;
	if ( materialId != null && type == 'stock' ) {
		maxStock = maxValue;
	}
	if ( materialId != null && type == 'demand' ) {
		maxQuantity = maxValue;
	}
	// Plot the points on the map
	for ( var i = 0; i < locations.length; i++ ) {
		var kloc = locations[ i ];
		var latLng = new google.maps.LatLng( kloc.lat, kloc.lng ); /// kloc[ 2 ], kloc[ 3 ] );
		var marker;
		if ( !materialId )
			marker = getBasicMarker( latLng, map, kloc.title ); /// [ 1 ] );
		else {
			if ( type == 'stock' )
				marker = getMarkerWithStockData( latLng, map, kloc, maxStock );
			else if ( type == 'demand' )
				marker = getMarkerWithQuantityData( latLng, map, kloc, maxQuantity );
			else {
				console.log( 'ERROR Invalid type: ' + type );
				return;
			}
		}
		
		markers.push( marker ); // Add marker to the array of markers. This will be used while toggling between clustered and unclustered view.
		if ( !materialId ) // Show cluster view only if materialId is not present.
			markerCluster.addMarker( marker ); // Add the marker to the markerCluster
		else // Do not show clusters if materialId is present. Only show spider view if multiple markers are at the same location.
			oms.addMarker( marker ); 
		// Add an event listener to the marker
		if ( type == 'stock' )
			google.maps.event.addListener( marker, 'click', showInventory( marker, kloc, materialId ) );
		if ( type == 'demand' )
			google.maps.event.addListener( marker, 'click', showDemand( marker, kloc, materialId ) );
		if ( type == 'kiosks' )
			google.maps.event.addListener( marker, 'click', showKioskDetails( marker, kloc ) ); 
		// Get the first latLag for centering, only when bounds is not available
		if ( i == 0 )
			centerAtLatLng = latLng;
	}
	// Get the lat/lng bounds - i.e. the bounding box across all coordinates in this view
	if ( locations.length == 1 ) {
		map.setCenter( centerAtLatLng );
	} else {
		// var bounds = getLatLngBounds( latLngBounds, 0 );
		var bounds = latLngBounds;
		if ( bounds ) {
			// Fit bounds to the bounding box
			map.fitBounds( bounds );
		} else {
			map.setCenter( centerAtLatLng );
		}
	}
}	

// Get basic marker
function getBasicMarker( latLng, map, title ) {
	return new google.maps.Marker( {
		position: latLng,
		map: map,
		title: title
	} );
}

// Get a custom icon-based marker
function getImageMarker( latLng, map, title, imageUrl ) {
	var image = new google.maps.MarkerImage( imageUrl );
	
	return new google.maps.Marker( {
		position: latLng,
		map: map,
        icon: image,
		title: title
	} );
}

// Get a marker which can show stock information
function getMarkerWithStockData( latLng, map, location, maxStock ) {
	// Get the stock info.
	var stockOnHand = location.stock; 
	var safetyStock = location.safetystock;
	var reorderLevel = location.reorderlevel;
	var max = location.max;
	// Check flags needed
	var stockOut = ( stockOnHand == 0 );
	var unsafeStock = ( ( reorderLevel > 0 && stockOnHand <= reorderLevel ) || ( safetyStock > 0 && stockOnHand <= safetyStock )  );
	var overStock = ( max > 0 && stockOnHand >= max );
	// Get the pin colors
	var pinColor = 'FFFF66';
	if ( stockOut )
		pinColor = 'FF0000'; // earlier: 'FF0033';
	else if ( unsafeStock )
		pinColor = 'FFA500'; // earlier: 'FF9933';
	else if ( overStock )
		pinColor = 'FF00FF';
	// Get the scaling factor for the pin, and font-size
	var scalingFactor = 0.5;
	if ( maxStock != 0 )
		scalingFactor += ( stockOnHand / maxStock ); // 0.5 is nominal size, 1 is double, 0.25 is half and so on; min is 0.25
	if ( scalingFactor > 1.5 )
		scalingFactor = 1.5 + ( scalingFactor - 1.5 ) * 0.1; // if the scalingFactor is higher than 3 times normal size (normal size being 0.5), then make it incrementally more (i.e. 10% of the difference from max.) than the largest scaling factor that we want 

	// Get title
	var title = location.title; /// [ 1 ];
	// Get the approproiate image URL (based on Google charts pins)
	var pinImgUrl = 'https://chart.googleapis.com/chart?chst=d_map_spin&chld=' + scalingFactor + '|0|' + pinColor + '|12|b|' + stockOnHand; //  + '|'  // A|FF0000|000000;

	return getImageMarker( latLng, map, title, pinImgUrl );
}

//Get a marker which can show stock information
function getMarkerWithQuantityData( latLng, map, location, maxQuantity ) {
	// Get the stock info.
	var quantity = location.quantity; /// [ 4 ];
	// Get the pin colors
	var pinColor = 'FFFF66';
	// Get the scaling factor for the pin, and font-size
	var scalingFactor = 0.5;
	if ( maxQuantity != 0 )
		scalingFactor += ( quantity / maxQuantity ); // 0.5 is nominal size, 1 is double, 0.25 is half and so on; min is 0.25
	if ( scalingFactor > 1.5 )
		scalingFactor = 1.5 + ( scalingFactor - 1.5 ) * 0.1; // if the scalingFactor is higher than 3 times normal size (normal size being 0.5), then make it incrementally more (i.e. 10% of the difference from max.) than the largest scaling factor that we want 

	// Get title
	var title = location.title; /// [ 1 ];
	// Get the approproiate image URL (based on Google charts pins)
	var pinImgUrl = 'https://chart.googleapis.com/chart?chst=d_map_spin&chld=' + scalingFactor + '|0|' + pinColor + '|12|b|' + quantity; //  + '|'  // A|FF0000|000000;

	return getImageMarker( latLng, map, title, pinImgUrl );
}

// Get the updated maxValue
function getUpdatedMaxValue( maxValueSoFar, newMaxValue ) {
	if ( maxValueSoFar > newMaxValue )
		return maxValueSoFar;
	else	
		return newMaxValue;
}

// Get the updated lat-lng bounds
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
	var northEast = new google.maps.LatLng( location[ 0 ] + scalingFactor * location[ 0 ],
										    location[ 1 ] - scalingFactor * location[ 1 ] );
	var southWest = new google.maps.LatLng( location[ 2 ] + scalingFactor * location[ 2 ],
											location[ 3 ] - scalingFactor * location[ 3 ] );
	
	return new google.maps.LatLngBounds( southWest, northEast );
}

// Show the loading indicator
function showLoader( elemId ) {
	var div = document.getElementById( elemId );
	if ( div )
		div.innerHTML = '<img src="/images/loader.gif"/>';
}

// Show geo info.
function showGeoInfo( showLoader, size, mappedSoFar ) {
	var div = document.getElementById('geoinfo');
	if ( showLoader ) {
		div.innerHTML = '<img src="/images/loader.gif" /> <b>' + mappedSoFar + '</b> locations on map. Fetching ' + size + ' more...';
	} else {
		div.innerHTML = '<b>' + mappedSoFar + '</b> locations on map';
	}
}

//mygetUpdatedLatLngBounds()
function mygetUpdatedLatLngBounds( latLngBoundsSoFar, points ) {
	if ( points == null ) {
		console.log( 'points is null' );
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

// Called when the map is to be shown with clustering of markers
function clusterMap () {
	// Clear markers in the spider if any
	oms.clearMarkers();
	markerCluster.setOptions( {map: map} );
	
}
// Called when the map is to be shown without clustering of markers
function unclusterMap() {
	markerCluster.setOptions( {map: null});
	// Iterate through the markers on the map.
	oms.clearMarkers();
	for ( var i = 0; i < markers.length; i++ ) {
		// Add all the markers to the oms
		oms.addMarker( markers[i] );
	}
}
// Called when the toggle clustering button is clicked. 
function toggleClustering( doNotClusterCheckBox ) {
	if ( doNotClusterCheckBox.value && doNotClusterCheckBox.value == 'uncluster' ) {
		// Now make it unclustered
		unclusterMap();
		doNotClusterCheckBox.value = 'cluster';
	} else if ( doNotClusterCheckBox.value && doNotClusterCheckBox.value == 'cluster' ){
		clusterMap();
		doNotClusterCheckBox.value = 'uncluster';
	}
}






