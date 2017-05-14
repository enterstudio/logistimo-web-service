
/*
//This function sets the country options from the json object.
// The input to this function is the json object containing the country codes and country names.
function setCountryOptions( countryCodesJsonObject , selectedCountryCode )
{
	var countryNamesArray = []; // An array holding the country names so that they can be sorted
	var countryMap = new Map(); // Map holding key value pairs of country name (key) and country code(value)
	
	for(var key in countryCodesJsonObject)
	{
		countryNamesArray.push(countryCodesJsonObject[key]); // Push the country name corresponding to the code into an array, so that it can be sorted before displaying.
		countryMap.put(countryCodesJsonObject[key],key); // Populate the countryMap object
	}

	countryNamesArray.sort(); // Sort the country names
	// Get a handle to the country list options element
	var countryListElement = document.getElementById( "countries" ).options;
	
	// Iterate on the sorted countryNamesArray object. Retrieve the country code from the countryCodesJsonObject 
	for(var i = 0; i<countryNamesArray.length; i++)
	{
		var opt = document.createElement("option");

		// Add an Option object to Drop Down/List Box
		countryListElement.add(opt);

		// Assign text and value to Option object
		opt.text = countryNamesArray[i]; // text that is displayed on the form
		opt.value = countryMap.get(countryNamesArray[i]); // Value that is sent to the server
		if( selectedCountryCode != null && selectedCountryCode != "")
		{
			if( opt.value == selectedCountryCode )
			{
				// set the countries select box to display the country name corresponding to the selected country code
				countryListElement.selectedIndex = i + 1;
				///opt.defaultSelected = true;
			}
		}
	}
}
*/


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NEW CODE BEGINS HERE
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
var countries,states,districts, taluks; // Global variables.
var locationJsonDataObjectGlobal;

// Create dropdowns for countries, states, districts and taluks. District and Taluk dropdowns are editable (meaning, the user can enter text into it)
function createLocationDropdowns() {
	// Create a TextboxList object for countries
	countries = new $.TextboxList( '#countries', { unique: true,
												 max: 1,
												 bitsOptions: {
													 box: { deleteButton: false }
												 },
												 plugins: { autocomplete: {
															method: 'binary',
															onlyFromValues: true,
															placeholder: JSMessages.typetogetsuggestions,
															maxResults: 50,
															minLength: 1
														}
													}
	}); 
	// Create a TextboxList object for states
	states = new $.TextboxList('#states', { unique: true,
										   	max: 1,
										   	bitsOptions: {
												 box: { deleteButton: false }
										   	},
										    plugins: { autocomplete: {
											   		   method: 'binary',
											   		   onlyFromValues: true,
											   		   placeholder: JSMessages.typetogetsuggestions,
											   		   maxResults: 50,
											   		   minLength: 1
										   			}
										    }
	});
	
	// Create a TextboxList object for districts. Note: Districts dropdown should allow new entries. So, do not set the onlyFromValues option for districts.
	districts = new $.TextboxList('#districts', { unique: true,
												  max: 1,
												  bitsOptions: {
													 		box: { deleteButton: false }
												  },
												  plugins: { autocomplete: {
													 		method: 'binary',
													 		placeholder: JSMessages.typetogetsuggestions,
													 		maxResults: 50,
													 		minLength: 1
												 		}
												  }
	});
	// Create a TextboxList object for taluks. Note: Taluks dropdown should allow new entries. So, do not set the onlyFromValues option for taluks.
	taluks = new $.TextboxList('#taluks', { unique: true,
										   max: 1,
										   bitsOptions: {
												 box: { deleteButton: false }
										   },
										   plugins: { autocomplete: {
													  method: 'binary',
													  placeholder: JSMessages.typetogetsuggestions,
													  maxResults: 50,
													  minLength: 1
										   			}
										   }
	});
}

// This function initializes the dropdowns
function initLocationDropdowns( locationJsonDataObject, selectedCountryCode, selectedState, selectedDistrict, selectedTaluk ) {
	var locationJsonObject = locationJsonDataObject.data;
	// Init countries first
    initCountriesDropdown( locationJsonObject );
    // If a country is already selected (either in configuration or in case of edit), display that country name in the input text box 
	// and initialize the states dropdown to display states for the selected country.
	if ( selectedCountryCode && selectedCountryCode != null && selectedCountryCode != '' ) {
		countries.add( getCountryNameFromCode( locationJsonDataObject, selectedCountryCode ), selectedCountryCode, null );
		initStatesDropdown( locationJsonObject, selectedCountryCode );
	}
	// If a state is selected (in case of edit), display that state name in the input text box 
	// and initialize the districts dropdown to display districts for the selected state.
	var statesJsonObject;
	if ( selectedState && selectedState != null && selectedState != '' ) {
		states.add( selectedState, selectedState, null );
		for ( key in locationJsonObject[selectedCountryCode] ) {
			if ( locationJsonObject[selectedCountryCode].hasOwnProperty( key ) && key == 'states' ) {
				statesJsonObject = locationJsonObject[selectedCountryCode].states; // The states Json array object
				initDistrictsDropdown( statesJsonObject, selectedState );
			}
		}
	}
	// If a district is selected (in case of edit), display that district name in the input text box
	// and initialize the taluks dropdown to display taluks for the selected district.
	if ( selectedDistrict && selectedDistrict != null && selectedDistrict != '' ) {
		districts.add( selectedDistrict, selectedDistrict, null );
		for ( key in statesJsonObject[selectedState] ) {
			if ( statesJsonObject[selectedState].hasOwnProperty( key ) && key == 'districts' ) {
				var districtsJsonObject = statesJsonObject[selectedState].districts;
				initTaluksDropdown( districtsJsonObject, selectedDistrict );
			}
		}
		
	}
	// If a taluk is selected (in case of edit), display that taluk name in the input text box
	if( selectedTaluk && selectedTaluk != null && selectedTaluk != '' ) {
		taluks.add( selectedTaluk, selectedTaluk, null );
	}
}

function initCountriesDropdown( locationJsonObject ) {
	var countryNamesArray = []; // An array holding the country names so that they can be sorted
	var countryMap = new Map(); // Map holding key value pairs of country name (key) and country code(value). For eg: India - IN, United States of America - US
	for( var key in locationJsonObject ) {
		countryNamesArray.push( locationJsonObject[key].name ); // Push the country name corresponding to the code into an array, so that it can be sorted before displaying.
		countryMap.put( locationJsonObject[key].name,key ); // Populate the countryMap object
	}
	countryNamesArray.sort(); // Sort the country names before setting it to the TextBoxList widget
	var valuesArray=[]; // This is used to populate the TextboxList widget.
	// Iterate on the countryNamesArray. Get the corresponding country code for each country name.
	for ( var i = 0; i < countryNamesArray.length; i++ ) {
		var valuesArrayElement=[];
		var text = countryNamesArray[i]; // Country Name will be the text that is displayed in the list box
		var value = countryMap.get( text ); // Country Code will the value that will be submitted
		valuesArrayElement.push( value ); // value
		valuesArrayElement.push( text ); // plain text
		valuesArrayElement.push( null ); // bit html
		valuesArrayElement.push( null ); // suggestion item html
		valuesArray.push( valuesArrayElement );
	}
	countries.plugins['autocomplete'].setValues( valuesArray );
	// Validate the entry on blur of the countries dropdown.
	countries.addEvent( 'blur', function() {
			validateTextField( document.getElementById( 'countries' ), JSMessages.country, 1, 500, 'countryStatus', true );
	});
	// If a country is selected, then initialize the states dropdown list accordingly.
	countries.addEvent( 'bitBoxAdd', function() {
		// Change the countries placeholder to different text
		initStatesDropdown( locationJsonObject, document.getElementById( 'countries' ).value );
	});
	// If a country selection is removed, reset the states, districts, taluks dropdowns.
	countries.addEvent( 'bitBoxRemove', function() {
		resetDropdown( states );
		resetDropdown( districts );
		resetDropdown( taluks );
	});
	countries.addEvent( 'bitRemove', function() { 
		validateTextField( document.getElementById( 'countries' ), JSMessages.country, 1, 500, 'countryStatus', true );
	});
}

function initStatesDropdown( locationJsonObject, selectedCountryCode ) {
	// Check if the key 'states' is present under country (specified by selectedCountryCode)
	for ( key in locationJsonObject[selectedCountryCode] ) {
		if ( locationJsonObject[selectedCountryCode].hasOwnProperty( key ) && key == 'states' ) {
			var statesJsonObject = locationJsonObject[selectedCountryCode].states; // The states Json array object
			var stateNamesArray = [];
			for( var key in statesJsonObject ) {
				stateNamesArray.push( key ); // Push the country name corresponding to the code into an array, so that it can be sorted before displaying.
			}
			// Sort the state names before setting it to the TextboxList widget
			stateNamesArray.sort();
			var valuesArray = [];
			// Iterate through stateNamesArray. Create valuesArrayElement frm each state name. Add it to valuesArray.
			for ( var j = 0; j < stateNamesArray.length; j++ ) {
				var valuesArrayElement = [];
				var text = stateNamesArray[j];
				valuesArrayElement.push( text ); // value
				valuesArrayElement .push( text ); // text
				valuesArrayElement .push( null ); // bit html
				valuesArrayElement .push( null ); // suggestion html
				valuesArray.push( valuesArrayElement );
			}
			// Initialize the state autocomplete text box options here
			states.plugins['autocomplete'].setValues( valuesArray );
			// Validate the entry on blur of widget
			states.addEvent( 'blur', function() {
					validateTextField( document.getElementById( 'states' ), JSMessages.state, 1, 500, 'stateStatus', true );
				});
			// Event when something is selected
			states.addEvent( 'bitBoxAdd', function() { 
				initDistrictsDropdown( statesJsonObject, document.getElementById( 'states' ).value );
			});
			// If a state selecttion is removed, reset the district and taluk options.
			states.addEvent( 'bitBoxRemove', function() {
				resetDropdown( districts );
				resetDropdown( taluks );
			});
			states.addEvent( 'bitRemove', function() { 
				validateTextField( document.getElementById( 'states' ), JSMessages.state, 1, 500, 'stateStatus', true );
			});
		}
	}
}

function initDistrictsDropdown( statesJsonObject, selectedState ) {
	// Check if the key 'districts' is present under the state (specified by selectedState)
	for ( key in statesJsonObject[selectedState] ) {
		if ( statesJsonObject[selectedState].hasOwnProperty( key ) && key == 'districts' ) {
			var districtsJsonObject = statesJsonObject[selectedState].districts;
			var districtNamesArray = [];
			for( var key in districtsJsonObject ) {
				districtNamesArray.push( key ); // Push the district names into an array, so that it can be sorted before displaying.
			}
			// Sort the district names before setting it to the TextboxList widget
			districtNamesArray.sort();
			var valuesArray = [];
			// Iterate through districtNamesArray. Create valuesArrayElement frm each district name. Add it to valuesArray.
			for ( var j = 0; j < districtNamesArray.length; j++ ) {
				var valuesArrayElement = [];
				var text = districtNamesArray[j];
				valuesArrayElement.push( text ); // value
				valuesArrayElement .push( text ); // text
				valuesArrayElement .push( null ); // bit html
				valuesArrayElement .push( null ); // suggestion html
				valuesArray.push( valuesArrayElement );
			}
			// Initialize the state autocomplete text box options here
			districts.plugins['autocomplete'].setValues( valuesArray );
			// Validate the entry on blur of widget
			districts.addEvent( 'blur', function() {
					validateTextField( document.getElementById( 'districts' ), JSMessages.district, 1, 500, 'districtStatus', false );
				});
			// Event when something is selected
			districts.addEvent( 'bitBoxAdd', function() { 
				initTaluksDropdown( districtsJsonObject, document.getElementById( 'districts' ).value );
			});
			districts.addEvent( 'bitBoxRemove', function() {
				resetDropdown( taluks );
			});
			districts.addEvent( 'bitRemove', function() {
				validateTextField( document.getElementById( 'districts' ), JSMessages.district, 1, 500, 'districtStatus', false );
			});
		}
	}
}

function initTaluksDropdown( districtsJsonObject, selectedDistrict ) {
	// Check if the key 'taluks' is present under the district (specified by selectedDistrict)
	for ( key in districtsJsonObject[selectedDistrict] ) {
		if ( districtsJsonObject[selectedDistrict].hasOwnProperty( key ) ) {
			var taluksJsonObject = districtsJsonObject[selectedDistrict].taluks;
			var talukNamesArray = [];
			for ( var i = 0; i < taluksJsonObject.length; i++ ) {
				talukNamesArray.push( taluksJsonObject[i] );
			}
			// Sort the taluk names before setting it to the TextboxList widget
			talukNamesArray.sort();
			
			var valuesArray = [];
			// Iterate through talukNamesArray. Create valuesArrayElement from each taluk name. Add it to valuesArray.
			for ( var j = 0; j < talukNamesArray.length; j++ ) {
				var valuesArrayElement = [];
				var text = talukNamesArray[j];
				valuesArrayElement.push( text ); // value
				valuesArrayElement .push( text ); // text
				valuesArrayElement .push( null ); // bit html
				valuesArrayElement .push( null ); // suggestion html
				valuesArray.push( valuesArrayElement );
			}
			
			// Initialize the state autocomplete text box options here
			taluks.plugins['autocomplete'].setValues( valuesArray );
		}
	}
	
	// Validate the entry on blur of widget
	taluks.addEvent( 'blur', function() {
			validateTextField( document.getElementById( 'taluks' ), JSMessages.taluk, 1, 500, 'talukStatus', false );
		});
	
}

// Reset the dropdown. Set the autocomplete valuewArray to empty array and clear the value displayed in the input text field
function resetDropdown( dropdown ) {
	// Reset the dropdown's autocomplete values array to []
	dropdown.plugins['autocomplete'].setValues( [] );
	// Set the input text box value state to ""
	dropdown.clear();
}

// Given the countryCode get the country name from the locationJsonDataObject.
function getCountryNameFromCode( locationJsonDataObject, countryCode ) {
	var locationJsonObject = locationJsonDataObject.data;
	for( var key in locationJsonObject ) {
		if ( key == countryCode ) {
			return locationJsonObject[key].name; // return the country name corresponding to the country code
		}
	}
}

//This function sets the country options from the locations json object. This is used in config_general.jsp
function setCountryOptions( locationJsonDataObject, selectedCountryCode ) {
    locationJsonDataObjectGlobal = locationJsonDataObject;
	var locationJsonObject = locationJsonDataObject.data;
	var countryNamesArray = []; // An array holding the country names so that they can be sorted
	var countryMap = new Map(); // Map holding key value pairs of country name (key) and country code(value). For eg: India - IN, United States of America - US
	for( var key in locationJsonObject ) {
		countryNamesArray.push( locationJsonObject[key].name ); // Push the country name corresponding to the code into an array, so that it can be sorted before displaying.
		countryMap.put( locationJsonObject[key].name,key ); // Populate the countryMap object
	}
	countryNamesArray.sort();
	// Get a handle to the country list options element
	var countryListElement = document.getElementById( 'countries' ).options;
	
	// Iterate on the sorted countryNamesArray object. Retrieve the country code from the countryCodesJsonObject 
	for(var i = 0; i<countryNamesArray.length; i++) {
		var opt = document.createElement( 'option' );
		// Add an Option object to Drop Down/List Box
		countryListElement.add(opt);
		// Assign text and value to Option object
		opt.text = countryNamesArray[i]; // text that is displayed on the form
		opt.value = countryMap.get( countryNamesArray[i] ); // Value that is sent to the server
		if( selectedCountryCode != null && selectedCountryCode != "") {
			if( opt.value == selectedCountryCode ) {
				// set the countries select box to display the country name corresponding to the selected country code
				countryListElement.selectedIndex = i + 1;
			}
		}
	}
}

function setStateOptions( selectedCountryCode, selectedStateCode ) {
    var locationJsonObject = locationJsonDataObjectGlobal.data;
    var stateNamesArray = []; // An array holding the country names so that they can be sorted
    for( var key in locationJsonObject ) {
        if(selectedCountryCode == key){
            for(var stateKey in locationJsonObject[key].states){
                stateNamesArray.push( stateKey );
            }
        } // Push the state name corresponding to the code into an array, so that it can be sorted before displaying.
    }
    stateNamesArray.sort();
    // Get a handle to the country list options element
    var stateListElement = document.getElementById( 'states' ).options;


    for(var i=stateListElement.length-1;i>=0;i--) {
        if(i != 0)
            stateListElement.remove(i);
    }

    // Iterate on the sorted countryNamesArray object. Retrieve the country code from the countryCodesJsonObject
    for(var i = 0; i<stateNamesArray.length; i++) {
        var opt = document.createElement( 'option' );
        // Add an Option object to Drop Down/List Box
        stateListElement.add(opt);
        // Assign text and value to Option object
        opt.text = stateNamesArray[i]; // text that is displayed on the form
        opt.value = stateNamesArray[i]; // Value that is sent to the server
        if( selectedCountryCode != null && selectedCountryCode != "" && selectedStateCode != null && selectedStateCode != "") {
            if( opt.value == selectedStateCode ) {
                // set the countries select box to display the country name corresponding to the selected country code
                stateListElement.selectedIndex = i + 1;
            }
        }
    }
}

