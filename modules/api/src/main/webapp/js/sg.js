
function getNumericEntityIDs() {
	var EntityIDs = "";
	var checkBoxes = document.getElementsByTagName("input");
	var checkBoxesLength = checkBoxes.length;
	var counter = 0;
	for (var i=0;i<checkBoxesLength;i++) {
		if (checkBoxes[i].checked) {
			var idVal = checkBoxes[i].getAttribute("id");
			if ( idVal != null && idVal != '' && !isNaN( idVal ) ) { 
				if (counter > 0) {
					EntityIDs += ",";
				}
				EntityIDs += idVal;
				counter++;
			}
		}
	}
	return EntityIDs;
}

function getAlphaEntityIDs( name ) {
	var EntityIDs = "";
	var checkBoxes = document.getElementsByName( name );
	var checkBoxesLength = checkBoxes.length;
	var counter = 0;
	for (var i=0;i<checkBoxesLength;i++) {
		if (checkBoxes[i].checked) {
			var idVal = checkBoxes[i].getAttribute("id");
			if ( idVal != null && idVal != '' ) { 
				if (counter > 0) {
					EntityIDs += ",";
				}
				EntityIDs += idVal;
				counter++;
			}
		}
	}
	return EntityIDs;
}

// Kiosk data simulation

function generateTransData( e ) {
	e.preventDefault();
	e.stopPropagation();
	
	window.location.href = "/s/admin/generatedata.jsp";
}

function generateOrders( e ) {
	e.preventDefault();
	e.stopPropagation();
	
	window.location.href = "/s/admin/generateorders.jsp";
}

// Disable the re-order level, depending on the inv. model
function disableReorderLevel( invModel, reorderLevelElement  ) {
	if ( invModel != '' && invModel != 'kn' ) {
		reorderLevelElement.disabled = true;
		reorderLevelElement.value = '0';
	} else {
		reorderLevelElement.disabled = false;
	}
}

// Check order quantities against vendor stock, if required
function validateOrderQuantities( vstockerrors ) {
	if ( vstockerrors == null )
		return true;
	var errs = vstockerrors.value.split(';');
	if ( errs == null || errs.length == 0 )
		return true;
	var msg = '';
	for ( var i = 0; i < errs.length; i++ ) {
		var errStr = trim( errs[i] );
		if ( errStr != '' ) {
			var err = errStr.split(',');
			msg += ' - ' + err[0] + ': ' + JSMessages.quantity + ' (' + err[1] + ') ' + JSMessages.cannotexceedstock + ' (' + err[2] + ')\n';
		}
	}
	if ( msg != '' ) {
		msg = JSMessages.quantity + ' ' + JSMessages.cannotexceedstock + '. ' + JSMessages.editquantities + ':\n\n' + msg;
		alert( msg );
		return false;
	}
	return true;
}

// Save the order item changes
function saveOrderItemChanges( divId, orderId, addNew ) {
	var textBoxes = document.getElementById( divId ).getElementsByTagName( 'input' );
	var materials = '';
	var errMsg = '';
	var counter = 0;
	var allItemsZero = true;
	// Enable the loading indicator
	var loader = null;
	if ( addNew )
		loader = document.getElementById( 'addItemsLoader' );
	else
		loader = document.getElementById( 'editItemsLoader' );
	if ( loader != null )
		loader.style.display = 'block';
	// Process quantities	
	for ( var i = 0; i < textBoxes.length; i++ ) {
		var materialId = textBoxes[ i ].name;
		if ( materialId == null ) // e.g. discount field will not have a name
			continue;
		if ( materialId != '' && !isNaN( materialId ) ) {
			var quantity = trim( textBoxes[ i ].value );
			if ( quantity == '' )
				continue;
			var isBatch = ( quantity == 'batch' );
			if ( allItemsZero )
				allItemsZero = ( quantity == 0 );
			if ( !isBatch && ( isNaN( quantity ) || quantity < 0 ) ) {
				if ( errMsg != '' )
					errMsg += ', ';
				errMsg += quantity;
			} else {
				// Get the material discount, if any
				var discountElement = document.getElementById( materialId + '_discount' );
				var discount = '';
				if ( discountElement != null )
					discount = trim( discountElement.value );
				if ( discount == '' )
					discount = '0';
				// Get the quantities by batches, if any
				var batchQuantity = 0;
				var batchData = '';
				if (isBatch ) {
					var batchIdsField = document.getElementById( 'batchnumbers_' + materialId );
					if ( batchIdsField ) {
						var batchIds = batchIdsField.value.split( ',' );
						for ( var j = 0; j < batchIds.length; j++ ) {
							var bid = batchIds[j];
							var q = document.getElementById( 'batchquantity_' + materialId + '_' + bid ).value;
							if ( q != '' )
								batchQuantity += parseFloat( q ); 
							var expiry = document.getElementById( 'batchexpiry_' + materialId + '_' + bid ).value;
							var manufacturer = '';
							var manufacturerField = document.getElementById( 'batchmanufacturer_' + materialId + '_' + bid );
							if ( manufacturerField )
								manufacturer = manufacturerField.value;
							var manufacturedOn = '';
							var manufacturedOnField = document.getElementById( 'batchmanufactured_' + materialId + '_' + bid );
							if ( manufacturedOnField )
								manufacturedOn = manufacturedOnField.value;
							batchData += ',' + escapeWith( bid, ',', 'XCOMMAX' ) + '|' + q + '|' + expiry + '|' + escapeWith( manufacturer, ',', 'XCOMMAX' ) + '|' + manufacturedOn;
						}
					}
				}
				// Get the material details CSV (each item details separated by ;)
				if ( counter > 0 )
					materials += ';';
				var finalQuantity = ( isBatch ? batchQuantity : quantity );
				if ( !addNew || finalQuantity != 0 ) {
					materials += materialId + ',' + finalQuantity + ',' + discount + batchData;
					counter++;
				}
			}
		}
	}
	if ( allItemsZero ) {
		alert( JSMessages.allitemszeromsg );
		return;
	}
	if ( errMsg != '' ) {
		alert( 'One or more entries (' + errMsg + ') are invalid. Please enter only postive integers.' );
		return false;
	}
	
	// Get the message, if any
	var message = "";
	if ( addNew )
		message = document.getElementById( 'message3' ).value
	else
		message = document.getElementById( 'message2' ).value;
	if ( message && message != '' ) {
		if ( message.length > 160 )
			message = message.substring( 0, 160 );
		message = encode( message ); // URL encode the message
	}
	// Get action
	var action = "changeitems";
	if ( addNew )
		action = "additems";
	// Update the item quantities and refresh order
	var url = '/s/orders/vieworder.jsp?oid=' + orderId + '&action=' + action + '&materials=' + materials + '&msg=' + message;
	getOrderData( url );
}

// Escape commas in values
function escapeWith( str, escStr, replaceStr ) {
	return str.replace( new RegExp( escStr, 'g' ), replaceStr );
}

// Refresh the order list view
function refreshOrderListView( data, view, table, rowIndex, colIndex, newValue ) {
	if ( data != null ) {
		data.setCell( rowIndex, colIndex, newValue );
		table.draw( view, { allowHtml: true } );
	}
}

// Get the data to display an order in a div
function getOrderData( url ) {
	getOrderData( url, null, null, null, null, null, null );
}
function getOrderData( url, newValue, rowIndex, colIndex, data, view, table ) {
	var handleSuccess = function(o){
		if( o && o != null ) {
			var text = o;
			document.getElementById( 'orderDetails' ).innerHTML = text;
			var error = text.match( "Error" ); // check if statement starts with Error - this is indicative of error
			if ( error == null && data ) // if no error, then change the status of table row and re-draw the main orders table
				refreshOrderListView( data, view, table, rowIndex, colIndex, newValue );
		}
	};
	
	var handleFailure = function(o) {
		alert(o.responseText);
	};	

	var callback = 	{
	  success: handleSuccess,
	  error: handleFailure,
	};
	connect( url, 'html', callback );
}

// Function that connects and gets data
//  - dataType can be xml, json, html or script; default is html
//  - callback is of format { success: function(data), error: function(xhrObject) }
// NOTE: jquery JS library should be included
function connect( url, dataType, callback ) {
	var type = 'html';
	if ( dataType != null )
		type = dataType;
	$.ajax( {
			 url: url,
			 dataType: type,
			 success: callback.success,
 	  		 error: callback.error
			} );
}

// Validate whether value of element is a number (int or float)
function validateNumber( element ) {
	if ( !isNaN( element.value ) ) {
	  if ( element.value < 0 ) {
		// alert( "'" + element.value + "' is less than 0. Please specify a non-zero integer." );
		  alert(JSMessages.enternumbergreaterthanzeromsg);
		return false;
	  } else {
		return true;
	  }
	}
	else {
		// alert( "'" + element.value + "' is not a valid number. Please specify an integer." );
		alert(JSMessages.entervalidnumbermsg);
		return false;
	}
}

// Validate that transaction/order quantities are correct
function validateTransEntry( textfield, allowZero ) {
	validateTransEntry( textfield, allowZero, null, null );
}
function validateTransEntry( textfield, allowZero, maxQuantity, origQuantity ) {
	if ( textfield.value != '' ) {
	  // Check if zero allowed (e.g. as in stock count or resetting an existing order item)
	  if ( allowZero && textfield.value == 0 )
	  	return;
	  // Is it a valid number?
	  if ( isNaN( textfield.value ) || textfield.value <= 0 ) { 
		alert( JSMessages.enternumbergreaterthanzeromsg + '.' );
		textfield.value = '';
		textfield.focus();
		return;
	  }
	  // Check against max. stock, if required
	  if ( maxQuantity != null ) {
	  	var quantity = textfield.value;
	  	if ( quantity > maxQuantity ) {
	  		alert( JSMessages.quantity + ' (' + quantity + ') ' + JSMessages.cannotexceedstock + ' (' + maxQuantity + ')' );
	  		if ( origQuantity != null )
	  			textfield.value = origQuantity;
	  		else
	  			textfield.value = '';
	  		textfield.focus();
	  		return;
	  	}
	  }
	}
}

// Validate that at least one transaction has an entry
function validateAllTransEntries() {
	var fields = document.getElementsByTagName('input');
	var transType = document.getElementById( 'transtype' ).value;
	var hasValues = false;
	var zeroBatchesMaterials = '';
	for ( var i = 0; i < fields.length; i++ ) {
		var item = fields.item( i );
		var fieldName = item.name; // typically materialId for quantity fields (and hence always a number)
		var quantity = item.value;
		if ( !isNaN( fieldName ) &&  item.value != '' && !isNaN( quantity ) ) {
			hasValues = true;
			// Check if batch form data exists or not
			var batchNumberField = document.getElementById( 'batchnumber_' + fieldName );
			var materialNameField = document.getElementById( 'materialname_' + fieldName );
			var batchId = ( batchNumberField ? batchNumberField.value : undefined );
			var materialName = ( materialNameField ? materialNameField.value : undefined );
			if ( transType == 'p' && batchId == '' ) { // batch data entry is option for physical counts; but if batch number field exists, but no batch number is given, then this will zero entire set of batches - so give warning; however if batch number exists, then other batch metadata are expected
				if ( parseFloat( quantity ) == 0 && batchNumberField && !batchId ) {
					zeroBatchesMaterials += ( zeroBatchesMaterials == '' ? '' : ', ' );
					zeroBatchesMaterials += materialName;
				}
				continue;
			}
			var errFields = '';
			// Check batch number
			if ( batchNumberField && batchNumberField.value == '' )
				errFields = JSMessages.batch + ' #';
			// Check batch expiry
			var batchExpiryField = document.getElementById( 'batchexpiry_' + fieldName );
			if ( batchExpiryField && batchExpiryField.value == '' ) {
				errFields += ( errFields == '' ? '' : ', ' );
				errFields += JSMessages.expiry;
			}
			// Check batch manufacturer
			var batchManufacturerField = document.getElementById( 'batchmanufacturer_' + fieldName );
			if ( batchManufacturerField && batchManufacturerField.value == '' ) {
				errFields += ( errFields == '' ? '' : ', ' );
				errFields += JSMessages.manufacturer;				
			}
			// Batch manufactured-on
			/** This is now optinal, so no checks
			var batchManufacturedField = document.getElementById( 'batchmanufactured_' + fieldName );
			if ( batchManufacturedField && batchManufacturedField.value == '' ) {
				errFields += ( errFields == '' ? '' : ', ' );
				errFields += JSMessages.manufactured;				
			}
			**/
			if ( errFields != '' ) {
				alert( JSMessages.fieldsrequiredfor + ' "' + materialName + '": ' + errFields );
				return false;
			}
		}
	}
	if ( zeroBatchesMaterials != '' ) {
		if ( !confirm( JSMessages.zerostocksettingmsg1 + ': ' + zeroBatchesMaterials + '.\n\n' + JSMessages.zerostocksettingmsg2  + '. ' + JSMessages._continue + '?' ) )
			return false;
	}
	if ( hasValues ) {
		return true;
	} else {
		alert( JSMessages.atleastoneentry + '.' );
		return false;
	}
}

// Go back to previous screen
function goBack(e) {
	e.preventDefault();
	e.stopPropagation();
	
	history.go(-1);
}

// Check all check boxes
function checkAll( masterCheckbox ) {
	// Get the master checkbox's check status
	var checkedStatus = masterCheckbox.checked;
	// Get all the check boxes in the document (assumes these are the only <input> types, or that others don't matter)
	var checkboxes = document.getElementsByTagName( "input" );
	// Set their status to the master checkbox status
	for ( var i = 0; i < checkboxes.length; i++ ) {
		checkboxes[ i ].checked = checkedStatus;
	}
}

// Show a map of a given location in a given div
function showMap( div, mapPointDetailsDiv, mapPointDetailsLoaderDiv, mapPointDetailsInfoDiv, mapPointDetailsMapDiv, lat, lng, geoAccuracy, zoom, markIt, destObj ) {
	var latLng = new google.maps.LatLng( lat, lng );
	var myOptions = {
		zoom: zoom,
		center: latLng,
		mapTypeId: google.maps.MapTypeId.ROADMAP,
		disableDefaultUI: true
	}
	var map = new google.maps.Map( div, myOptions );
	var geocoder = new google.maps.Geocoder();
	if ( markIt ) {
		// Get the address, given lat/lng
		geocoder.geocode( { 'latLng': latLng }, function( results, status ) {
			var address = '';
			if ( status == google.maps.GeocoderStatus.OK ) {
				if ( results[1] ) {
					// Get the address
					address = results[1].formatted_address;
				}
			} // end if ( status == ... )
			var title = address;
			if ( geoAccuracy != null && geoAccuracy != 0 )
				title += ' (' + JSMessages.accuracy +': ' + geoAccuracy + ' ' + JSMessages.meters +')';			
			// Show the marker (with or without address title, depending on geo-coding results)
			var marker = new google.maps.Marker( {
				    position: latLng,
				    map: map,
				    title: title
				} );
			// Add a click event to the marker to show an expanded map in a new window
			google.maps.event.addListener( marker, 'click', function() {
				// mapUrl = '/s/showmappoint.jsp?lat=' + lat + '&lng=' + lng + '&title=' + address;
				// Add destination params, if present
				/**** NOTE: showing distance using libraries API has been disabled since 16/1/2013 - this is given the "distance from what" has to be clarified; if using this, include libraries=geometry when loading Google Maps .js inventoryupdates.jsp and check API license terms for Geometry API 
				if ( destObj && destObj != null ) {
					var destLatLng = new google.maps.LatLng( destObj.lat, destObj.lng );
					var distance = google.maps.geometry.spherical.computeHeading( latLng, destLatLng );
					mapUrl += '&dlat=' + destObj.lat + '&dlng=' + destObj.lng + '&dtitle=' + destObj.title;
					if ( distance != 0 ) {
						if ( distance < 0 ) 
							distance = -1 * distance;
						mapUrl += '&distance=' + distance;
					}
				}
				****/
				// Open window
				// window.open(mapUrl,'Map','location=0,resizable=1,scrollbars=1,width=800,height=600');
				// Show the modal screen
				var mapPointDetailsDivId = mapPointDetailsDiv.id;
				$( '#' + mapPointDetailsDivId ).dialog( {
											modal: true,
											position: 'center',
											width: 720,
											height: 570,
											modal: true,
											title: title,
											resizable: false,
											open: function( event, ui ) {
												// The code below is a fix to the problem of the window scrollbar getting locked in Chrome. This problem seems to be fixed in jquery ui 1.10.0
												// Remove the line below when we move to jquery ui 1.10.0
												window.setTimeout(function () { 
													jQuery(document).unbind('mousedown.dialog-overlay').unbind('mouseup.dialog-overlay'); }, 100);
												// Show the order data on a map
												 showMapPointData( mapPointDetailsLoaderDiv, mapPointDetailsInfoDiv,  mapPointDetailsMapDiv, lat, lng, zoom, geoAccuracy, destObj );
											}
										   } );
				// Show the order data on a map
				// showMapPointData( lat, lng, zoom, geoAccuracy, destObj );
				
			} );
		} );
	} // end if ( markIt )
}

function showMapPointData( mapPointDetailsLoaderDiv, mapPointDetailsInfoDiv, mapPointDetailsMapDiv, lat, lng, zoom, geoAccuracy, destObj ) {
	mapPointDetailsLoaderDiv.style.display = 'block'; 
	var latLngBounds = new google.maps.LatLngBounds(); 	
	var latLng = new google.maps.LatLng( lat, lng ); // Get a google LatLng object from the lat lng of the order.
	var myOptions = {
		zoom: zoom,
		center: latLng,
		mapTypeId: google.maps.MapTypeId.ROADMAP,
		disableDefaultUI: true
	}
	var map = new google.maps.Map( mapPointDetailsMapDiv, myOptions ); // Create a map object
	var oms = new OverlappingMarkerSpiderfier( map, { markersWontMove: true, markersWontHide: true, keepSpiderfied: true }); // Create the spiderfier object
	var geocoder = new google.maps.Geocoder(); 
	// Get the address, given lat/lng using google geocoder
	geocoder.geocode( {'latLng': latLng}, function(results, status) {
		var address = '';
		if ( status == google.maps.GeocoderStatus.OK ) {
			mapPointDetailsLoaderDiv.style.display = 'none'; // Hide the loader once reverse geocoding is complete.
			if ( results[1] ) {
				// Get the address
				address = results[1].formatted_address;
			}
		} // end if ( status == ... )
		var title = address;
		if ( geoAccuracy != null && geoAccuracy != 0 )
			title += ' (' + JSMessages.accuracy +': ' + geoAccuracy + ' ' + JSMessages.meters +')';	
		
		var contentStr = '<html><b>' + JSMessages.transaction + ' ' + JSMessages.at + ': '+ address + '</b><br/>'+ lat + ', ' + lng + '</html>'; 
		var mapPointMarker = plotMarker( map, oms, latLng, title, null );
		latLngBounds.extend( mapPointMarker.getPosition() );
		var mapPointInfoWindow = createInfoWindow( contentStr );
		// Open the info window
		mapPointInfoWindow.open( map, mapPointMarker );
				
		// Add a click event listener to the marker to display the infowindow.
		google.maps.event.addListener( mapPointMarker, 'click', function() { markerLeftClicked ( map, mapPointMarker, mapPointInfoWindow ); } );
		} );
	
		// If destObj is is undefined or null, silently return
		if ( !destObj || destObj == null ) {
			console.log( 'destObj was not specified' );
			return;
		}
		if ( destObj.lat == '0.0' && destObj.lng == '0.0' ) {
			console.log( 'Destination object does not have geocodes' );
			return;
		}
		// Create a google LatLng object from destObj lat and lng	
		var destLatLng = new google.maps.LatLng( destObj.lat, destObj.lng ); // Create a google LatLng object from the lat lng of the destObj
		// Now create a marker with destObj
		var title = destObj.title;
		// Show the marker (with or without address title, depending on geo-coding results). Change the marker color to green.
		// Attach an info window to the destination marker also.
		var icon = 'https://www.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png';
		var ltype = destObj.ltype;
		if ( ltype && ltype != null ) {
			if ( ltype == 'c' )
				ltype = JSMessages.customer;
			else if ( ltype == 'v' )
				ltype = JSMessages.vendor;
			
		} else {
			ltype = JSMessages.kiosk;
		}
			
		var contentStr = '<html><b>' + ltype + ' at: '+ title + '</b><br/>'+ destObj.lat + ', ' + destObj.lng + '</html>';
		var destObjMarker = plotMarker( map, oms, destLatLng, title, icon );
		latLngBounds.extend( destObjMarker.getPosition() );
		map.fitBounds( latLngBounds );
		var destObjInfoWindow = createInfoWindow( contentStr );
		// Open the destObj info window
		destObjInfoWindow.open( map, destObjMarker );
		// Add a click event listener to the marker to display the infowindow.
		google.maps.event.addListener( destObjMarker, 'click', function() { markerLeftClicked( map, destObjMarker, destObjInfoWindow ); } );
				
		// Calculate distance between the point at which the order was placed or transaction was made and the associated entity.
		var directionsDisplay;
		var directionsService = new google.maps.DirectionsService();
		directionsDisplay = new google.maps.DirectionsRenderer( { suppressMarkers: true } );
		directionsDisplay.setMap( map );
		var request = {
			      origin: latLng,
			      destination: destLatLng,
			      travelMode: google.maps.DirectionsTravelMode.DRIVING
			  };
			  directionsService.route( request, function( response, status ) {
			    if ( status == google.maps.DirectionsStatus.OK ) {
			      directionsDisplay.setDirections(response);
			      // Show the distance in the mapDetailsInfoDiv.
			      var distance = response.routes[0].legs[0].distance.text;
			      mapPointDetailsInfoDiv.innerHTML = '<font size="2"><b>' + JSMessages.transaction_distancemsg + ' ' + ltype + ': '+ distance + '</b></font>';
			      mapPointDetailsInfoDiv.style.display = 'block';
			    }
			  });
}

// Function to plot a Marker. If oms is specified, then adds the marker to the oms also.
function plotMarker( map, oms, latLng, title, icon ) {
	// Show the marker (with or without address title, depending on geo-coding results)
	var marker = new google.maps.Marker( {
		    position: latLng,
		    map: map,
		    title: title,
		    icon: icon
		} );
	if ( oms && oms != null )
		oms.addMarker( marker ); // Add the marker to the oms
	return marker;
	
}

// Function to create an InfoWindow object
function createInfoWindow( contentStr ) {
	// Create and open the info window by default.
	var infoWindow = new google.maps.InfoWindow({
	    content: contentStr
	});
	return infoWindow;
}

// Event handler when marker is left clicked.
function markerLeftClicked( map, marker, infoWindow ) {
	infoWindow.open( map, marker );
}

// Show the map inline with less opacity
function showMapInline( mapIconDiv, mapDiv, mapPointDetailsDiv, mapPointDetailsLoaderDiv, mapPointDetailsInfoDiv, mapPointDetailsMapDiv, lat, lng, geoAccuracy, destObj ) {
	mapIconDiv.style.display = 'none';
	mapDiv.style.display = 'block';
	showMap( mapDiv, mapPointDetailsDiv, mapPointDetailsLoaderDiv, mapPointDetailsInfoDiv, mapPointDetailsMapDiv, lat, lng, geoAccuracy, 12, true, destObj );
}

function getGeocodes() {	
	// Hide the latitude and longitude already displayed.
	var geocodesDisplayDiv = document.getElementById( "geocodesdisplay" );
	geocodesDisplayDiv.style.display = 'none';
	
	// Hide the div that is used to manually enter latitude and longitude.
	var manualgeocodesDiv = document.getElementById("manualgeocodes");
	manualgeocodesDiv.style.display = 'none';
	
	// Get the geo-codes using the google geocoding api. If the geo-cdoes are obtained successfuly, then display them in a div elements.
	// Otherwise, display a div that has two input elements for the user to enter the geo-codes.
	var city = document.getElementById("city");
	var state = document.getElementById("states");
	var country = document.getElementById("countries");
	
	var geocodesErrorMessage = document.getElementById("geocodeserrormessage");

	// If country or state or village/city name is null, then display a message to the user. 
	if( country.value == '' | state.value == '' | city.value == '' ){
		geocodesErrorMessage.innerHTML = "<b>" + JSMessages.geocodes_specifylocationmsg + ".";
		geocodesErrorMessage.style.display = 'block';
		return;
	}

	var addressStr = city.value + ',' + state.options[state.selectedIndex].text + ',' + country.options[country.selectedIndex].text;
	var geocoder = new google.maps.Geocoder();
	if(geocoder == null){
		// Geocoder object could not be created. So, display a message.
		geocodesErrorMessage.innerHTML = "<b>" + JSMessages.geocodes_failuremessage + ".</b>"
		geocodesErrorMessage.style.display = 'block';
		
		// Enable the user to enter geocodes manually.
		var geocodesManual = document.getElementById("manualgeocodes");
		geocodesManual.style.display = 'block';
		
		// Hide the geo-codes that are already displayed.
		var geocodesDisplay = document.getElementById("geocodesdisplay");
		geocodesDisplay.style.display = 'none';
	}
	
	
	geocoder.geocode({address: addressStr}, geocodeResult );
	
	// callback funtion
	function geocodeResult( results, status )
	{
		if( status == google.maps.GeocoderStatus.OK ) {
			var geocodeserrormessage = document.getElementById("geocodeserrormessage");
			geocodeserrormessage.style.display = 'none';
			
			var geocodesDiv = document.getElementById("geocodes");
			var resultsLength = results.length;
			var cityFound = false;
			if( resultsLength > 0 ){
				for( var j = 0; j< resultsLength; j++ ) {
					var formattedAddress = results[j].formatted_address;
					if( formattedAddress.match( city.value ) ) {
						cityFound = true;
						break;
					}
				}
				
				var HTMLStr = '<b>' + JSMessages.geocodes_select + '.</b>';
				HTMLStr = HTMLStr + '<table id="kioskstable" style="width:100%"><tr><th style="width:2%;"></th><th style="width:75%">' + JSMessages.address + '</th><th style="width:23%">' + JSMessages.geocodes + '</th></tr>';
			}
				
			for( var i = 0; i < resultsLength; i++ ) {
				var formattedAddress = results[i].formatted_address;
				var latitude = results[i].geometry.location.lat();
				var longitude = results[i].geometry.location.lng();
								
				HTMLStr = HTMLStr + '<tr><td style="width:2%;"><input type="radio" name="geocodesradio" id="' + i + '"' + 'onclick="storeLatLong(this,' + latitude + ',' + longitude + ' )"' + '></input></td>';
				HTMLStr = HTMLStr + '<td style="width:70%">' + formattedAddress + '</td>' + '<td style="width:28%"' + '>' + latitude + ', ' + longitude + '</td>' + '</tr>';
								
			}
			
			HTMLStr = HTMLStr +'</table>';
			HTMLStr = HTMLStr + "<a href = 'javascript:cancel()'>" + JSMessages.cancel + "</a>" + "&nbsp;&nbsp;&nbsp;" + "<a href = 'javascript:manual()'>" + JSMessages.geocodes_enter + "</a>"; 

			geocodesDiv.innerHTML = HTMLStr;
			geocodesDiv.style.display = 'block';

		} else {
			// Reset the latitude and longitude values if any.
			var latitude = document.getElementById("latitude");
			var longitude = document.getElementById("longitude");
			latitude.value = "";
			longitude.value = "";
			
			var geocodesErrorMessage = document.getElementById( "geocodeserrormessage");
			geocodesErrorMessage.innerHTML = "<b>" + JSMessages.geocodes_failuremessage + ".</b>"
			geocodesErrorMessage.style.display ='block';
			
			var geocodesManual = document.getElementById("manualgeocodes");
			geocodesManual.style.display = 'block';
			var geocodesDiv = document.getElementById("geocodes");
			geocodesDiv.style.display = 'none';
			var geocodesDisplay = document.getElementById("geocodesdisplay");
			geocodesDisplay.style.display = 'none';
		}
	}
}

// Loads various buttons on the page
function loadEventListenersForButtons() {
	
	// Back button
	var goBackButton = document.getElementById("goback");
	if (goBackButton != null) {
		goBackButton.addEventListener("click",goBack, false);
	}
		
	// Event listeners for kiosk data simulation
	
	// Generate transaction data for a given kiosk and its materials
	var generateTransDataButton = document.getElementById( "generatetransdata" );
	if ( generateTransDataButton != null ) {
		generateTransDataButton.addEventListener( "click", generateTransData, false );
	}
	
	// Generate orders for a given kiosk and its materials
	var generateOrdersButton = document.getElementById( "generateorders" );
	if ( generateOrdersButton != null ) {
		generateOrdersButton.addEventListener( "click", generateOrders, false );
	}
}

// URI encode UTF-8 strings (for, say, GET/POST purposes)
function encode( msg ) {
	var encoded = msg;
	if (encodeURIComponent) {
    	encoded = encodeURIComponent(msg);
	} else {
    	encoded = escape(msg);
	}
	return encoded;
}

// Function to initialize a calendar selector
function initCalendar( id, dateFormat, defaultDate, minDate, maxDate, isMonthly ) {
	var options = {};
	if ( dateFormat )
		options.dateFormat = dateFormat;
	if ( defaultDate )
		options.defaultDate = defaultDate;
	if ( minDate )
		options.minDate = minDate;
	if ( maxDate )
		options.maxDate = maxDate;
	if ( isMonthly ) {
		options.dateFormat = dateFormat;
	    options.changeMonth = true;
	    options.changeYear =  true;
	    options.showButtonPanel =  true;
	    options.onClose = function(dateText, inst) {
	            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
	            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
	            $(this).val($.datepicker.formatDate(dateFormat, new Date(year, month, 1)));
	        };
	}
	var datePicker = $( '#' + id ).datepicker( options );
	if ( defaultDate )
	    datePicker.datepicker( "setDate", defaultDate );
	// Hide the date view on focus, if monthly selection only
	if ( isMonthly ) {
	    $( '#' + id ).focus( function () {
	        $(".ui-datepicker-calendar").hide();
	        $("#ui-datepicker-div").position({
	            my: "center top",
	            at: "center bottom",
	            of: $(this)
        	} );
    	} );
    }
    return datePicker;
}

// Update media items on a div
function updateMedia( photoPanelDiv, mediaItems, size ) {
	if ( !mediaItems || !mediaItems.items || mediaItems.items.length == 0 )
		return;
	// Add images to the photo panel
	var html = '';
	for ( var i = 0; i < mediaItems.items.length; i++ ) {
		var media = mediaItems.items[i];
		var url = media.servingUrl;
		if ( i != 0 )
			html += '<br/>';
		html += '<a href="' + url + '" target="_new"><img style="padding:5px;border:1px solid #000000;margin-bottom:3px;" src="' + url + '=s' + size + '" /></a>';
	}
	photoPanelDiv.innerHTML = html;
}

// Toggle selection of batch-enabled-on-mobile
function toggleBatchEnabledOnMobile( checked ) {
	var elem = document.getElementById( 'isbatchenabledonmobile' );
	if ( elem ) {
		elem.checked = checked;
		elem.disabled = !checked;
	}
}

// Show a list of valid batches (if allocation quantity is specified, then show a form with allocations up to what is specified)
function showBatches( divId, userId, kioskId, materialId, materialName, showForm, allocationQuantity, isOrder, cacheJson, isEEFORule ) {
	var div = $( '#' + divId );
	// Set loading indicator
	div.html( '<img src="/images/loader.gif" />' );
	// Load the dialog
	$( '#' + divId ).dialog( {
		modal: true,
		position: {
		    my: 'left',
		    at: 'right',
		  },
		width: 600,
		title: 'Batches of ' + materialName
	   });
	if ( !cacheJson )
		$batches = {};
	var json = $batches[ materialId ];
	if ( json ) {
		renderBatchForm( div, json, materialId, materialName, showForm, allocationQuantity, isOrder, isEEFORule );
	} else {
		// Get the batches, if any
		var url = '/api/i?a=gibtchs&uid=' + userId + '&kid=' + kioskId + '&mid=' + materialId + '&s=100';
		$.ajax( {
			 url: url,
			 dataType: 'json',
			 success: function(o) { renderBatchForm( div, o, materialId, materialName, showForm, allocationQuantity, isOrder, isEEFORule ); },
	  		 error: function(o) { div.html( 'Unable to retrieve batch information (' + o.responseText + ')' ); }
			} );
	}
}

// Render batch information
function renderBatchForm( div, json, materialId, materialName, showForm, allocationQuantity, isOrder, isEEFORule ) {
	var closeButtonHtml = '<button type="button" onclick="$(\'#' + div.attr( 'id' ) + '\').dialog(\'close\');">Close</button>';
	if ( json.st && json.st == '1' ) { // error
		div.html( '<div class="sgForm" style="text-align:left">' + json.ms + '<br/><br/>' + closeButtonHtml + '</div>' );
		return;
	}
	if ( !json.btchs || json.btchs.length == 0 ) {
		div.html( '<div class="sgForm" style="text-align:left">' + JSMessages.nobatchesavailable + '.<br/><br/>' + closeButtonHtml + '</div>' );
		return;
	}
	// Globally store the JSON
	$batches[ materialId ] = json;
	var remaining = allocationQuantity;
	var html = '<table id="kioskstable">';
	html += '<tr><th>#</th><th>' + JSMessages.batch + '</th><th>' + JSMessages.expiry + ' (dd/MM/yyyy)</th><th>' + JSMessages.manufacturer + '</th><th>' + JSMessages.manufactured + ' (dd/MM/yyyy)</th><th>' + JSMessages.stock + '</th>';
	if ( allocationQuantity || showForm )
		html += '<th>' + JSMessages.quantity + '</th>';
	html += '</tr>';
	for ( var i = 0; i < json.btchs.length; i++ ) {
		var b = json.btchs[i];
		html += '<tr>';
		html += '<td>' + ( i + 1 ) + '</td>';
		html += '<td>' + b.bid + '</td>';
		var expiry = ( b.bexp ? b.bexp : '' );
		html += '<td>' + expiry + '</td>';
		html += '<td>' + ( b.bmfnm ? b.bmfnm : '' ) + '</td>';
		var manuDate = ( b.bmfdt ? b.bmfdt : '' );
		html += '<td>' + manuDate + '</td>';
		html += '<td>' + b.q + '</td>';
		if ( allocationQuantity || showForm ) {
			var allocate = 0;
			if ( b.q <= remaining ) {
				allocate = b.q;
				remaining -= b.q;
			} else {
				allocate = remaining;
				remaining = 0;
			}
			var value = ( b.nquantity ? b.nquantity : ( allocate > 0 ? allocate : '' ) );
			html += '<td>';
			var onBlur = 'validateTransEntryByType(this,' + b.q + ',false)';
			if ( isOrder )
				onBlur = 'validateTransEntry(this,true)';
			html += '<input id="f_batchquantity_' + materialId + '_' + getASCIIStr( b.bid ) + '" type="text" size="5" maxlength="20" value="' + value + '" onblur="' + onBlur + '"  />';
			html += '</td>';
		}
		html += '</tr>';
	}
	html += '</table>';
	var finalHtml = '<div class="sgForm" style="text-align:left">';
	if ( allocationQuantity ) {
		finalHtml += JSMessages.quantity_to_be_allocated + ': <b>' + allocationQuantity + '</b><br/>';
		finalHtml += JSMessages.quantity_allocated + ': <font style="font-weight:bold;color:' + ( remaining > 0 ? 'red' : 'green' ) + '">' + ( allocationQuantity - remaining ) + '</font></br></br>';
	}
	finalHtml += html;
	// Add the save/cancel buttons
	finalHtml += '<br/><br/>';
	if ( showForm )
		finalHtml += '<button type="button" onclick="saveBatchFormData(\'' + div.attr('id') + '\',\'' + materialId + '\',' + allocationQuantity + ',' + isOrder + ',' + isEEFORule + ')">Save</button> &nbsp;';
	finalHtml += closeButtonHtml;
	// Add the JSON to a hidden field for use by the Save processor
	finalHtml += '<input type="hidden" id="json_' + materialId + '" value="'
	finalHtml += '</div>'
	div.html( finalHtml );
}

function saveBatchFormData( dialogDivId, materialId, allocationQuantity, isOrder, isEEFORule ) {
	var html = '<table>';
	html += '<tr><td style="background-color:lightgray">' + JSMessages.batch + '</td><td style="background-color:lightgray">' + JSMessages.expiry + '</td><td style="background-color:lightgray">' + JSMessages.quantity + '</td></tr>';
	var json = $batches[ materialId ];
	if ( !json || !json.btchs ) {
		console.log( 'No json batch data found for material: ' + materialId );
		return;
	}
	var hasQuantities = false;
	var bids = '';
	var allocatedQuantity = 0;
	for ( var i = 0; i < json.btchs.length; i++ ) {
		var b = json.btchs[i];
		var qField = $( '#f_batchquantity_' + materialId + '_' + getASCIIStr( b.bid ) );
		var quantity;
		if ( qField )
			quantity = qField.val();
		if ( quantity != '' ) {
			var enteredQuantity = parseFloat( quantity );
			allocatedQuantity += enteredQuantity;
			// Check if fefo (first-expiry-first-out rule was followed), i.e. previous batch was fully allocated
			if ( isEEFORule && i > 0 ) {
				var prevBatch = json.btchs[i-1];
				if ( !prevBatch.nquantity || ( prevBatch.nquantity < prevBatch.q ) ) {
					if ( !confirm( JSMessages.batch_notallocatedfully ) )
						return;
				}
			}
			hasQuantities = true;
			html += '<tr><td>' + b.bid + '</td><td>' + ( b.bexp ? b.bexp : '' ) + '</td><td>' + quantity;
			var batchQuantityName = 'batchquantity_' + materialId + '_' + b.bid;
			html += '<input type="hidden" name="' + batchQuantityName + '" id="' + batchQuantityName + '" value="' + quantity + '" />'; // quantity
			if ( b.bexp ) {
				var batchExpiryName = 'batchexpiry_' + materialId + '_' + b.bid;
				html += '<input type="hidden" name="' + batchExpiryName + '" id="' + batchExpiryName + '" value="' + b.bexp + '" />'; // expiry
			}
			if ( b.bmfnm && b.bmfnm != '' ) {
				var batchManufacturerName = 'batchmanufacturer_' + materialId + '_' + b.bid;
				html += '<input type="hidden" name="' + batchManufacturerName + '" id="' + batchManufacturerName + '" value="' + b.bmfnm + '" />'; // manufacturer
			}
			if ( b.bmfdt && b.bmfdt != '' ) {
				var batchManufacturedOnName = 'batchmanufactured_' + materialId + '_' + b.bid;
				html += '<input type="hidden" name="' + batchManufacturedOnName + '" id="' + batchManufacturedOnName + '" value="' + b.bmfdt + '" />'; // manufactured on
			}
			html += '</td></tr>';
			b.nquantity = quantity;
			// Accumulate bids
			if ( bids != '' )
				bids += ',';
			bids += b.bid;
		}
	}
	html += '</table>';
	var batchNumbersName = 'batchnumbers_' + materialId;
	html += '<input type="hidden" name="' + batchNumbersName + '" id="' + batchNumbersName + '" value="' + bids + '" />';
	if ( !hasQuantities ) {
		html = '';
	} else if ( allocatedQuantity < allocationQuantity ) { // check if allocated quantity is what is required
		var msg = JSMessages.quantity_allocated + ' (' + allocatedQuantity + ') ' + JSMessages.quantity_islesserthanrequired + ' (' + allocationQuantity + '). ' + JSMessages._continue + '?'
		if ( !confirm( msg ) )
			return;
	}
	// Update with selected quantities
	$( '#batchselectedpanel_' + materialId ).html( html );
	// Close the dialog
	$( '#batchselectpanel_' + materialId ).dialog( 'close' );
	// Remove this select panel, if order form, given the quirks of using this along with editdatatable, where two sets of input fields with same id may be there in dom; removing here solves this problem
	if ( isOrder )
		$( '#batchselectpanel_' + materialId ).remove();
	
	// Update quantity display
	var quantityDisplayField = $( '#quantitydisplay_' + materialId );
	if ( quantityDisplayField )
		quantityDisplayField.html( allocatedQuantity );
}

// Replace chars. in a string with their ascii equivalent
function getASCIIStr( str ) {
	var newStr = '';
	for ( var i = 0; i < str.length; i++ )
		newStr += str.charCodeAt( i );
	return newStr;
}

// Temperature data view functions
// Render temp. data
function showTemperatures( divId, materialId, materialName, kioskId, kioskName ) {
	if ( !divId || !materialId || !kioskId )
		return;
	var url = '/s/temperature/tempdevicesview.jsp?kioskid=' + kioskId + '&materialid=' + materialId;
	var title = materialName + ' at ' + kioskName; 
	// Show the modal screen
	$('#' + divId ).dialog( {
								modal: true,
								position: {
												my: 'left bottom',
												at: 'center'
										  },
								width: 500,
								height: 280,
								modal: true,
								title: title,
								close: function() {
							        $(this).html(''); // clear previous data; required if multiple dialogs on the same page with different divs.
							    }
					});
	// Get the temper
	$.ajax( {
				url: url,
				dataType: 'html',
				success: function( data ) {
					$('#' + divId).html( data );
				},
				errors: function( o ) {
					$('#' + divId).html( o.responseText );					
				}
			} );
} 


window.addEventListener("load",loadEventListenersForButtons,false);