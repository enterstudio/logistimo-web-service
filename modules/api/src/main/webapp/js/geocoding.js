// Global variables
var map; // The map
var bounds; // Bounds for the map
var markersArray = []; // Array of markers( used when multiple geocodes are returned for by Google Geocoding service )
var marker; // The marker that is currently set for the entity. This marker is draggable so that the user can place it anywhere on the map and adjust it's position 
var geocoder; // Google geocoder object
var mapCenter; // Center of the map. If the country is configured in the domain config, then this set to that country's geocodes. Othewise, it's set to the world's center.
var action; // Global variable that holds the action parameter value. Set to 'create' for add entity, 'modify' for edit entity
var zoom; // Initial zoom level
var hasCountry = false;
var countryName, countryValue;

function initialize( actionType, countryName, countryValue ) {
	console.log( 'actionType: ' + actionType + ', countryName: ' + countryName + ', countryValue: ' + countryValue );
	// Get the country name 
	// If country name is configured in the domain, and set mapCenter and bounds global variables to that value
	// If country name is not configured, then set mapCenter 0.0 and bounds to cover the world map.
	// DO NOT set the map center and bounds yet.
	
	// If action is modify, check if entity has geocodes.
	// If it does, then center the map around that lat lng and set zoom to 8
	// If it does not have geocodes, then center the map around mapCenter and bounds.
	// If the action is create, center the map around the mapCenter and fit the bounds.
	
	geocoder = new google.maps.Geocoder();
	action = actionType;
	countryName = countryName;
	countryValue = countryValue;
	
	bounds = new google.maps.LatLngBounds();
	mapCenter = new google.maps.LatLng( 0, 0 );
	zoom = 1;
	// Create a mapOptions object and initialize center to LatLng (0,0)
	var myOptions = {
	        zoom: 1, 
	        center: mapCenter,
	        mapTypeId: google.maps.MapTypeId.ROADMAP,
	        navigationControlOptions: {
	            style: google.maps.NavigationControlStyle.SMALL
	        }
	    };
	// Create the map object with the map options
	map = new google.maps.Map( document.getElementById( 'map_canvas' ), myOptions ); // Create the map
	// If countryValue is not '' ( which means a country is selected in the select box of addkiosk or editkiosk form ), geocode the country using google geocoder
	if ( countryValue ) {
		console.log( 'showing ' + countryName + ' map' );
		hasCountry = true;
		geocoder.geocode( { 'address': countryName }, geocodeCountrySuccess );
	} else {
		console.log( 'Showing world map' );
	}
}

// Success Callback function that gets executed if geocoding country is successful.
function geocodeCountrySuccess( results , status ) {
	// If geocoder returned status OK
	if ( status == google.maps.GeocoderStatus.OK ) { 
		// Get the geocodes of the country specified by countryName and set mapCenter to it. Set the map bounds to the value returned by google geocoder.
		for ( var i = 0; i < results.length; i++ ) {
			mapCenter = results[0].geometry.location; // Store the location returned by geocoder into mapCenter
			bounds = results[0].geometry.bounds; // Store this bounds into the global variable. It will be used later if the user wants to start geocoding from the beginning.
			break;
		}
		// If action is modify, check if the entity has geocodes.
		if ( action == 'modify' ) {
			// Show the latitude and longitude
			var lat = document.editkiosk.latitude.value;
			var lng = document.editkiosk.longitude.value;
			var hasGeocodes = ( lat && lat != '0.0' ) &&  ( lng && lng != '0.0' );
			// If the entity has geocodes, get it, form the address string using village/city name, state name and countryname.
			if ( hasGeocodes ) {
				var latLng = new google.maps.LatLng( lat, lng );
				var address = '';
				
				if ( document.editkiosk.city.value )
					address += document.editkiosk.city.value + ', ';
				address += document.editkiosk.states.value;
				///address += countryName ;
				// Plot the marker and make it draggable so that the user can move it and adjust the location.
				plotMarker( address, latLng, true );
				
				// Set the center and zoom around this marker
				map.setCenter( latLng );
				map.setZoom( 8 );
				
				// Make the geocodesdisplay div visible and display the marker's geocodes in the div
				displayGeocodes( marker.getPosition() );
				saveGeocodes(); // Store the geocodes into the hidden form parameters ( in addkiosk and editkiosk forms )
			} else {
				// If there are no geocodes for the entity, the map should be centered around the default center. If country is present, then bounds should be set
				// around that country.
				map.setCenter( mapCenter );
				if ( hasCountry )
					map.fitBounds( bounds );
			}
		}
		// If action is create, set map center to mapCenter and uif country is present, then set bounds to be around that country.
		if ( action == 'create' ) {
			map.setCenter ( mapCenter );
			if ( hasCountry )
				map.fitBounds( bounds );
		}
		
	} else {
		alert( JSMessages.geocodes_failuremessage + ': ' + status );
	}
}

// This function is called when the Get Geocodes button is clicked
function onClickGetGeocodes() {
	// Show loading indicator
	showGeoInfo( true );
	// Get the value of the city, state and countries fields from the form.
	var city = document.getElementById( 'city' );
	var state = document.getElementById( 'states' );
	var country = document.getElementById( 'countries' );
		
	var geocodingErrorMessage = document.getElementById( 'geocodingerrormsg' );
	var selectMarkerMessage = document.getElementById( 'selectmarkermsg' );
	var geocodesDisplay = document.getElementById( 'geocodesdisplay' );
	
	// Hide the select marker message.
	selectMarkerMessage.style.display = 'none';
	resetMarker();
	removeAllMarkersFromMap();
	geocodesDisplay.style.display = 'none'; // Hide the previously displayed geocodes
	// If country or state or village/city name is null, then display a message to the user. 
	if( country.value == '' | state.value == '' | city.value == '' ){
		geocodingErrorMessage.innerHTML = '<font color="red">' + JSMessages.geocodes_specifylocationmsg + '.' + '</font>';
		geocodingErrorMessage.style.display = 'block';
		showGeoInfo( false );
		return false;
	}
	// Form the address string that can be passed to google geocoder.
	var addressStr = city.value + ',' + state.value + ',' + country.value;
	// Call the getGeocodes method
	getGeocodes( addressStr );
	showGeoInfo( false );
	return false;
}

// Function to get the geocodes using Google Geocoder. It takes an input address string
function getGeocodes( addressStr ) {
	geocoder.geocode( { 'address': addressStr }, function( results , status ) {
	var geocodingErrorMessage = document.getElementById( 'geocodingerrormsg' );
    var selectMarkerMesssage = document.getElementById( 'selectmarkermsg' );
    
    if ( status == google.maps.GeocoderStatus.OK ) {
    	// Hide any previous error message
    	geocodingErrorMessage.innerHTML = '';
    	geocodingErrorMessage.style.display = 'none';
    	var draggable = false;
    	
    	if ( results.length > 1 ) {
    		if ( action == 'create' ) {
    			document.addkiosk.multiplegeocodes.value = true;
    		}
    		if ( action == 'modify' ) {
    			document.editkiosk.multiplegeocodes.value = true;
    		}
    	} 
    		
    	// If there is only one result, then create a marker for it and make it draggable. Disable the left click event on that marker.
    	if ( results.length == 1 ) {
    		draggable = true;
    		
    	} 
    	// Iterate through results and create markers for each result. Set the title for each marker to the formatted address returned by Google geocoding service
    	for ( var i = 0; i < results.length; i++ ) {
    		// Display the select marker message
    		selectMarkerMesssage.style.display = 'block';
    		// Get the formatted address as returned by Google Geocoding service
    		var formattedAddress = results[i].formatted_address;
    		// Plot a marker for each result. And make them non draggable.
    		plotMarker( formattedAddress, results[i].geometry.location, draggable );
    		// If the marker is draggable ( there will be only one draggable marker at any time on the map ), display the geocodes of that marker
    		if ( draggable ) {
    			displayGeocodes( marker.getPosition() ); // Display the geocodes below the map
    			saveGeocodes(); // Save it into the hidden form fields of the addkiosk form
    		}
    	}
    } else {
    	// Geocoding not successful. Display error message and reason for failure to obtain geocodes and return false.
    	geocodingErrorMessage.innerHTML = '<b>' + status + '.';
    	geocodingErrorMessage.style.display = 'block';
		return false;
    }
    
    // If there was only result, then set center and zoom
    if ( results.length == 1 ) {
    	map.setCenter( marker.getPosition() );
    	map.setZoom( 8 );
    } else if ( results.length > 1 ) {
    	// Iterate through markers array. 
    	var newBounds = new google.maps.LatLngBounds(); // Create a newBounds object. Because, if bounds object may already initialized with country's bounds value as returned by google geocoder. 
    	for ( var i = 0; i < markersArray.length; i++ ) {
    		// Extend map bounds to fit the marker location
            newBounds.extend( markersArray[i].getPosition() );
            // Set the bounds for the map.
            map.fitBounds( newBounds );
    	}
    }
    	
 	// Get the manualgeocodes div
    var manualgeocodes = document.getElementById( 'manualgeocoding' );
    manualgeocodes.style.display = 'block';
	});
	
}

// Function called when a marker is left clicked. It asks the user to cofirm the geocodes of the entity. If the user clicks yes, then it fixes that marker on the map and makes it draggable
// so that it can be dragged to any point on the map. It also removes all the other markers from the map.
function markerLeftClicked( event ) {
	if ( markersArray.length == 1 )
		return;
    var answer = confirm( JSMessages.geocodes_confirmmessage + '?' );
    if ( answer ) {
    	// Iterate through the markers array
    	for ( var i = 0; i < markersArray.length; i++ ) {
    		marker = markersArray[i];
    		if ( marker.getPosition() == event.latLng ) {
    			marker.setDraggable( true );
    			// Set the geocodesdisplay div to the geocodes of this marker
    			var geocodesDisplay = document.getElementById( 'geocodesdisplay' );
    			geocodesDisplay.style.display = 'block';
    			displayGeocodes( marker.getPosition() );
    			// Now add a drag event listener to the marker
    			google.maps.event.addListener(marker, 'drag', function( e ) {
	            	markerDragged( e );
	            });
    			// Add a drag end event listener to the marker
    			google.maps.event.addListener( marker, 'dragend', function( e ) {
    				markerDragEnd( e );
    			});
    			saveGeocodes();
    			continue;
    		} else {
    			// Remove the marker from the map
    			marker.setMap( null );
    			// Remove the marker from the markersArray
    			markersArray.splice( i, 1 );
    			i--;
    		}
   	 	}
	}
}

// This function is called when the user is trying to geo code the entity by manualy dragging the marker on the map. 
// It updates the geocodesdisplay div to show the latitude and longitude of the location at which the entity is dropped.
function markerDragged( e ) {
	displayGeocodes( e.latLng );
}

// This function saves the marker's latitude and longitude into the form variables 
// so that they can get saved to the entity when the Save button is clicked in the addKiosk form
function markerDragEnd( e ) {
	saveGeocodes();
	var title = marker.getTitle();
	// Get the first '.' in the title. Send it as address to the getMarkerTitle function
	var index = title.indexOf( '. ' );
	var address = title.substring( 0, index );
	marker.setTitle( getMarkerTitle( address, e.latLng ) );
}

// This function saves the marker's latitude and longitude into the form variables
// so that they can get saved to the entity when the Save button is clicked in the addKiosk form.
function saveGeocodes() {
	if ( action == 'create' ) {
		document.addkiosk.latitude.value = marker.getPosition().lat();
		document.addkiosk.longitude.value = marker.getPosition().lng();
		document.addkiosk.multiplegeocodes.value = false;
	}
	if ( action == 'modify' ) {
		document.editkiosk.latitude.value = marker.getPosition().lat();
		document.editkiosk.longitude.value = marker.getPosition().lng();
		document.editkiosk.multiplegeocodes.value = false;
	}
	return false;
}

function manuallyGeocode() {
	// Reset the marker variable in the map to null
	// resetMarker();
	// Remove all the markers from the map
	// removeAllMarkersFromMap();
	showMarkers( false );
	// Hide the geocodesdisplay div
	var geocodesDisplay = document.getElementById( 'geocodesdisplay' );
	geocodesDisplay.style.display = 'none';
	// Hide the manual geocoding message
	var manualGeocodingMsg = document.getElementById( 'manualgeocodingmsg' );
	manualGeocodingMsg.style.display = 'none';
	var manualGeocodesInput = document.getElementById( 'manualgeocodesinput' );
	manualGeocodesInput.style.display = 'block';
	return false;
}

function saveManualGeocodes() {
	var lat, lng;
	// Get the lat and lng entered by the user
	lat = document.getElementById( "lat" ).value.trim();
	lng = document.getElementById( "lng" ).value.trim();
	
	// Validate the latitude and the longitude entered by the user.
	var latitudeFlag = validateGeo( document.getElementById( "lat" ), JSMessages.latitude, 0, 10, 'latitudeStatus', false );
	var longitudeFlag = validateGeo( document.getElementById( "lng"), JSMessages.longitude, 0, 10, 'longitudeStatus', false );
	
	if ( latitudeFlag && longitudeFlag ) {
		resetMarker();
		removeAllMarkersFromMap();
		
		// Plot the latitude and longitude as a marker on the map.
		var latLng = new google.maps.LatLng( lat, lng );
		var address = '';
		
		if ( action == 'create' ) {
			if ( document.addkiosk.city.value )
				address += document.addkiosk.city.value + ', ';
			address += document.addkiosk.states.value + ', ';
			address += document.addkiosk.countries.value;
		}
		
		if ( action == 'modify' ) {
			if ( document.editkiosk.city.value )
				address += document.editkiosk.city.value + ', ';
			address += document.editkiosk.states.value;
			///address += countryName;
		}
		
		plotMarker( address, latLng, true ); // Make the marker draggable
		// Set the center and zoom because there is only one marker
		map.setCenter( latLng );
		map.setZoom( 8 );
		// Make the geocodesdisplay div visible and display the marker's geocodes in the div
		displayGeocodes( latLng ); // Changed from marker.getPosition() to latLng
		saveGeocodes();
		// Close the div containing lat and lng field text field inputs
		// Hide the manual geocoding message
		var manualGeocodingMsg = document.getElementById( 'manualgeocodingmsg' );
		manualGeocodingMsg.style.display = 'block';
		var manualGeocodesInput = document.getElementById( 'manualgeocodesinput' );
		manualGeocodesInput.style.display = 'none';
	}  
	return false; // Return false in either case so that the addkiosk form does not get closed.
}

function cancelManualGeocodes() {
	// Display only the message for manual geocoding
	var manualGeocodingMsg = document.getElementById( 'manualgeocodingmsg' );
	manualGeocodingMsg.style.display = 'block';
	var manualGeocodesInput = document.getElementById( 'manualgeocodesinput' );
	manualGeocodesInput.style.display = 'none';
	// Show the markers that were hidden
	showMarkers( true );
	if ( markersArray.length == 1 ) {
		// Show the geocodesDisplay div
		var geocodesDisplay = document.getElementById( 'geocodesdisplay' );
		geocodesDisplay.style.display = 'block';
	}
	return false;
}

// Function to reset the global variable marker
function resetMarker() {
	marker = null;
}
// Function to remove all the markers from te map and set the markersArray to empty
function removeAllMarkersFromMap() {
	// Iterate through the markersArray and remove each marker from the map.
	for ( var i = 0; i < markersArray.length; i++ ) {
		markersArray[i].setMap( null );
	}
	markersArray = [];
}

function showMarkers( show ) {
	for ( var i = 0; i < markersArray.length; i++ ) {
		markersArray[i].setVisible( show );
	}
	if ( marker )
		marker.setVisible( show );
}
// Function to plot a Google Marker on Google map given a Google LatLng object
function plotMarker( address, location, draggable ) {
	marker = new google.maps.Marker({
        map: map,
        title: getMarkerTitle( address, location ),
        position: location
    });
	 // Add the marker to the markersArray
	markersArray.push( marker );
		
	// If isDraggable flag is true, make this marker draggable. And add a drag event listener to the marker.
	if ( draggable ) {
		marker.setDraggable( true );
		// Now add a drag event listener to the marker
		google.maps.event.addListener(marker, 'drag', function( e ) {
        	markerDragged( e );
        });
		// Add a drag end event listener to the marker
		google.maps.event.addListener( marker, 'dragend', function( e ) {
			markerDragEnd( e );
		});
		
	} else {
		// Add an onclick event handler to the marker 
    	google.maps.event.addListener( marker, 'click', function( e ) {
        	markerLeftClicked( e );
        });
	}
}

// Function to display geocodes corresponding to the marker position
function displayGeocodes( latLng ) {
	var geocodesDisplay = document.getElementById( 'geocodesdisplay' );
	geocodesDisplay.innerHTML = '<b>' + JSMessages.latitude + ' = ' + latLng.lat() + ', ' + JSMessages.longitude + ' = ' + latLng.lng() + '</b>&nbsp;&nbsp;- <a href="javascript:removeGeocodes()">' + JSMessages.remove + '</a>';
	geocodesDisplay.style.display = 'block';
}

function getMarkerTitle( address, latLng ) {
	var title = '';
	if ( address ) {
		title += address;
	}
	title += ' (' + latLng.lat() + ', ' + latLng.lng() + ')';
	return title;
}

function removeGeocodes()
{
	if ( action == 'create' ) {
		document.addkiosk.latitude.value = '';
		document.addkiosk.longitude.value = '';
	}
	if ( action == 'modify' ) {
		document.editkiosk.latitude.value = '';
		document.editkiosk.longitude.value = '';
	}
	var geocodesDisplayDiv = document.getElementById( 'geocodesdisplay' );
	geocodesDisplayDiv.style.display = 'none';
	// Remove the marker from the map
	// Clear the markersArray.
	resetMarker();
	removeAllMarkersFromMap();
	// If country is not set in the configuration, center the map around the world and set zoom to 1
	if ( !hasCountry ) {
		bounds = new google.maps.LatLngBounds();
		mapCenter = new google.maps.LatLng( 0, 0 );
		zoom = 1;
	}
	map.setCenter( mapCenter );
	map.setZoom( zoom );
	// If a country is configured then set the map to fit the bounds of the country.
	if ( hasCountry ) {
		map.fitBounds( bounds );
	}
}

//Show geo info.
function showGeoInfo( showLoader ) {
	var div = document.getElementById( 'geocodinginfo' );
	if ( showLoader ) {
		div.innerHTML = '<img src="/images/loader.gif" /> <b>';
	} else {
		div.innerHTML = '';
	}
}

