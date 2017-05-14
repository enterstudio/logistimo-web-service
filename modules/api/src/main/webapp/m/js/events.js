/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

// Events to bind to
$("#setupentity").live("pageshow", function(e) {
    var state = $('#state').val();
    setupDistrictAndTalukAutocomplete(state);
    $('#entitysavebutton').prop('disabled', true).addClass('ui-disabled');
});


$('#entityname').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#newentityname').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#city').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#pincode').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#street').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#taluk').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#routetag').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#usegeocodespanel').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#state').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#district').live('input', function(e) {
	$('#taluk').val('');
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#userfirstname').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#userlastname').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#mobilephone').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#userstate').live('input', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});
$('#changegeo').live('click', function(e) {
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});

// Check if refresh of transactions has to be made from server
$('#transactions').live('pageshow', function(e) {
	if ( !isTransactionsRefreshed() ) {
		refreshTransactions();
	}
});

// Check if orders have to be refreshed on loading (works only for first load during a login; clicking between purchase/sales will not activate this, given this page is already shown)
$('#orders').live('pageshow', function(e) {
	var otype = ( $osearch.oty ? $osearch.oty : 'prc' );
	if (!$transferOrder) {
		if (!isOrdersRefreshed(otype)) {
			loadOrders(otype, 0, null);
		}
	}
	else {
		if (!isTransfersRefreshed(otype)) {
			loadOrders(otype, 0, null);
		}
	}
});




// Handle the material show event
$( '#materials').live( 'pageshow', function(e) {
	// Signal partial errors, if any
	if ( !$partialErrorsShown ) {
		showPartialErrors(null, null);
	}
	document.title = $.mobile.activePage.attr("data-title");
});

$( '#relatedentities').live( 'pageshow', function(e) {
	document.title = $.mobile.activePage.attr("data-title");
});

$( '#entities').live( 'pageshow', function(e) {
	document.title = $.mobile.activePage.attr("data-title");
});

$( '#myentityrelationships').live( 'pageshow', function(e) {
	document.title = $.mobile.activePage.attr("data-title");
});

$( '#materialinfo').live( 'pageshow', function(e) {
	document.title = $.mobile.activePage.attr("data-title");
});

$( '#entityinfo').live( 'pageshow', function(e) {
	document.title = $.mobile.activePage.attr("data-title");
});

$( '#orderinfo').live( 'pageshow', function(e) {
	document.title = $.mobile.activePage.attr("data-title");
	$.mobile.hashListeningEnabled = false;  // workaround for multiple order tags
});

$( '#orderinfo').live( 'pagehide', function(e) {
	$.mobile.hashListeningEnabled = true;  // workaround for multiple order tags
});

$( '#review').live( 'pageshow', function(e) {
	$.mobile.hashListeningEnabled = false;  // workaround for multiple order tags
});

$( '#review').live( 'pagehide', function(e) {
	$.mobile.hashListeningEnabled = true;  // workaround for multiple order tags
});

$( '#ordertypeselection').live( 'pageshow', function(e) {
	document.title = $.mobile.activePage.attr("data-title");
});

$('#state').live('change', function(e) {
	$('#district').val('');
	$('#taluk').val('');
    var state = $('#state').val();
	setupDistrictAndTalukAutocomplete(state);
	 $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});

$('#userstate').live('change', function(e) {
   $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});

$('#usegeocodes').live('change', function(e) {
    $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled'); 
});

$('#routetag').live('change', function(e) {
	$('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
});

$('#quantity').live('input', function(e) {
	var stockAfterOperationDiv = $( '#stockAfterOperation' );
	var quantityErrorMessageDiv = $('#quantityerrormessage');
	var operationQuantity = 0;

	if ($('#quantity').val() != ''){
		operationQuantity = parseFloat($('#quantity').val());
		 $('#datasavebutton').prop('disabled', false).removeClass('ui-disabled');
		 if ($('#quantity').val().length > $maxNumberLength) {
             //remove extra text which is more then maxlength
             $('#quantity').val($('#quantity').val().slice(0, $maxNumberLength));
			 operationQuantity = parseFloat($('#quantity').val());
         }
	}
	//else
	//	 $('#datasavebutton').prop('disabled', true).addClass('ui-disabled');
	if ($currentOperation != 'no' && operationQuantity > 0){
		var quantity = parseFloat($existingQuantity); 
		var postOperationQuantity = getPostOperationQuantity(quantity, operationQuantity);
		if (postOperationQuantity != null){
			var stockMessage = $messagetext.newquantityafteroperation + postOperationQuantity;
			stockAfterOperationDiv.attr( 'style', 'display:block' );
			stockAfterOperationDiv.html( '<font style="color:black;font-size:8pt;">' + stockMessage + '</font>' );
		} else {
			stockAfterOperationDiv.attr( 'style', 'display:none' );
		}
	} else {
		stockAfterOperationDiv.attr( 'style', 'display:none' );
	}
	quantityErrorMessageDiv.attr( 'style', 'display:none' );
	//recommended order quantity reasons
	if ((isOperationOrder($currentOperation)) ) {
		var eoq = $('#eoq').val();
		if (eoq != null && eoq != undefined) {
			eoq = parseFloat(eoq);
			if (eoq >= 0 && operationQuantity != eoq)
				$('#materialinfo').find('div[id^="irreasonsdiv"]').attr('style', 'display:block');
			else
				$('#materialinfo').find('div[id^="irreasonsdiv"]').attr('style', 'display:none');

		}
	}
});

$('#quantity').live('keypress', function(key) {
	if((key.which < 48 || key.which > 57)&&(key.which !=46 || $(this).val().indexOf('.') != -1) )
		return false;
});

$('input[id^="q_"]').live('input', function(e) {
	var qtyId = this.id;
	var id = this.id.substring(this.id.indexOf('_'),this.id.length);
	var operationQuantity = 0;
	if ($('#'+qtyId).val() != '') {
		operationQuantity = parseFloat($('#' + qtyId).val());
		if (this.getAttribute('value') != operationQuantity )
			$('#orderinfo').find('#eoreasonsdiv' + id).attr('style', 'display:block');
		else
			$('#orderinfo').find('#eoreasonsdiv' + id).attr('style', 'display:none');
	}
	if ($newOrderType == 'prc') {
		var eoq = $('#eoq'+id).val();
		if (eoq != null && eoq != undefined) {
			eoq = parseFloat(eoq);
			if (eoq >= 0 && operationQuantity != eoq)
				$('#orderinfo').find('#irreasonsdiv'+id).attr('style', 'display:block');
			else
				$('#orderinfo').find('#irreasonsdiv'+id).attr('style', 'display:none');
		}
	}
	
});



$('input[id^="rq_"]').live('input', function(e) {
	var qtyId = this.id;
	var id = this.id.substring(this.id.indexOf('_'),this.id.length);
	var operationQuantity = 0;
	if ($('#'+qtyId).val() != '') {
		operationQuantity = parseFloat($('#' + qtyId).val());
		if (this.getAttribute('value') != operationQuantity )
			$('#orderfulfilment').find('#flreasonsdiv' + id).attr('style', 'display:block');
		else
			$('#orderfulfilment').find('#flreasonsdiv' + id).attr('style', 'display:none');
	}
});

$('#payment').live('input', function(e) {
      if ($('#payment').val().length > $maxNumberLength) {
         //remove extra text which is more then maxlength
         $('#payment').val($('#payment').val().slice(0, $maxNumberLength));
     }
});

$('#payment').live('keypress', function(key) {
    if((key.which < 48 || key.which > 57)&&(key.which !=46 || $(this).val().indexOf('.') != -1) )
	return false;
});

$('#orderquantity').live('input', function(e) {
	 if ($('#orderquantity').val().length > $maxNumberLength) {
        //remove extra text which is more then maxlength
        $('#orderquantity').val($('#orderquantity').val().slice(0, $maxNumberLength));
    }
});

$('#feedbackmessage').live('input', function(e) {
	$('#fbsendbutton').prop('disabled', false).removeClass('ui-disabled');
});

$('.inputfieldchange').live('change', function(e) {
	//var materialinfopage = $( '#materialinfo' );
	$( $.mobile.activePage[0] ).find( '#fieldchange' ).val('1');
});


$('.checkbuttonclick').live('click', function(e) {
	//var materialinfopage = $('#materialinfo');
	 var activePage = $.mobile.activePage[0];
	var changed = $(activePage).find('#fieldchange').val();
	var saveButton;
	if (activePage.id == 'materialinfo')
		saveButton = '#datasavebutton';
	else if (activePage.id == 'orderinfo')
		saveButton = '#orderdatasavebutton';
	else if (activePage.id == 'batchinfo')
		saveButton = '#batchdatasavebutton';
	var saveButtonDisabled;
    if (saveButton)
		saveButtonDisabled = $(saveButton).prop('disabled');
	if (changed == '1' && !saveButtonDisabled) {
		e.preventDefault();
		e.stopPropagation();

		var backUrl;
		var backButton = $(activePage).find( '#' + activePage.id + 'back' );
		if ( backButton && backButton.css('display') != 'none' ) {
			 backUrl = backButton.attr( 'href' );
		}
		var saveButtonFunction = function() {$(activePage).find( saveButton).click();};
		/*if (isDevice())
			showCordovaPopupDialog($buttontext.save + '?', $messagetext.changesmadepleasesave,saveButtonFunction,function(){cancelChanges();});
		else
			showPopupDialog($buttontext.save + '?', $messagetext.changesmadepleasesave,,null,"popuponcancel");*/

		showPopupDialog( $buttontext.save + '?', $messagetext.changesmadepleasesave,saveButtonFunction,function(){cancelChanges();},null, "popuponcancel", false);

	}
});


function saveChanges() {
	//$.mobile.sdCurrentDialog.close();
	//$('#datasavebutton' ).click();
	var saveButton = $( $.mobile.activePage[0] ).find( '#datasavebutton' );
	if ( saveButton && saveButton.css('display') != 'none' ) {
		saveButton.click();
		//var clickFunction = saveButton.attr( 'onclick' );
		//if ( clickFunction ) {
		//	clickFunction();
	//	}
	}
}

function cancelChanges() {
	//$.mobile.sdCurrentDialog.close();
	var backButton = $( $.mobile.activePage[0] ).find( '#' + $.mobile.activePage[0].id + 'back' );
	if ( backButton && backButton.css('display') != 'none' ) {
		var backUrl = backButton.attr( 'href' );
		if ( backUrl ) {
			$.mobile.changePage( backUrl );
		}
	}
}

$('img.galleryImageItem').live('taphold', function(e){
	e.stopPropagation();
	var id = this.id;
	var domainKey = $(this).attr('domainKey');
	var type;
	if (id && canEditEntity(type) && !isOperator($role)) {
		$(this).simpledialog2({
	            mode:"button",
	            headerText:$labeltext.imageoptions,
	            headerClose:true,
	            forceInput:true,
	            buttons: {
	            	'Delete': {
	            		click: function(){
	            			deleteMedia(id, domainKey);
	            		},
	            		icon: "delete"
	            	}
	            }
		});
	}
});

$(document).ready(function() {
	var localeLanguage = "en";
	localeLanguage = getUserLanguage();
	if (!localeLanguage || localeLanguage == '')
		localeLanguage = "en";
	loadLanguage(localeLanguage, function () {
		initializeLoginPage(localeLanguage);
		if ( isDevice()) {
			checkAppVersion();
	    }
	});
  	// Enable switching to a new server, should a 409 be returned from the server
  	$.ajaxSetup({
      statusCode: {
          409: function(o) {
              if (o && o.responseText) {
                  var newUrl = o.responseText;
                  if ( !newUrl || newUrl == '' ) {
                      console.log( 'No redirection URL sent' );
                      return;
                  }
                  // Check if URL has the https prefix
                  if ( newUrl.indexOf( 'https' ) == -1 && newUrl.indexOf( 'http' ) == -1 )
                    newUrl = 'https://' + newUrl;
                  $host = newUrl;
                  // Clear the transaction log
                  clearLocalTransactionLog();
                  if ( isDevice() ) { // simply show a loading message and perform new server login
                      hideLoader();
                      showLoader( $messagetext.switchservermobile );
                      storeHostDomain( $host );
                      // Login again
                      login( true );
                  } else {
                      var redirectUrl = newUrl + '/m/index.html';
                      showDialog( $labeltext.reconnect, $messagetext.switchserverweb, redirectUrl );
                  }
            }
          }
      }
  });

});

// Wait for Cordova to connect with the device
document.addEventListener("deviceready",onDeviceReady,false);

function onDeviceReady() {
    // Init. the camera state variables (to determine if camera or gallery-upload is to be used)
    cameraStateInit(); // defined in media.js
	if ( isDevice() ) {
		// Load the file system and required sync/log/sync-config files
		window.requestFileSystem( window.PERSISTENT, 5*1024*1024 /*5MB*/, initFiles, fileErrorHandler );
		// Enable location service, if required
		enableLocationService();
	}
}


// Handle the phone's back button clicks - exit when this is clicked on login page
document.addEventListener( 'backbutton', function(e) {
    if ( $.mobile.activePage[0].id == 'login' ) {
        e.preventDefault();
        exit();
    } else { 
      if (!$pictureClicked)	 { 
	    	// Get the back URL on the page, if any
	    	var backButton = $( $.mobile.activePage[0] ).find( '#' + $.mobile.activePage[0].id + 'back' );
	    	if ( backButton && backButton.css('display') != 'none' ) {
	    		var backUrl = backButton.attr( 'href' );
	    		if ( backUrl ) {
	    			$.mobile.changePage( backUrl );
	    			return;
	    		}
	    	}
	    	var logoutButton = $( $.mobile.activePage[0] ).find( '#' + $.mobile.activePage[0].id + 'logout' );
	        if (logoutButton && logoutButton.css('display') != 'none' ) {
				var hasPendingData = ( getNotificationCount() > 0 );
				var msg;
				if ( isLoginAsReconnect() )
					msg = ( hasPendingData ? $messagetext.somedataenterednotsentlogout_loginasreconnect : $messagetext.doyouwanttologout );
				else
					msg = ( hasPendingData ? $messagetext.somedataenterednotsentlogout : $messagetext.doyouwanttologout );
				var okButtonClickCallback = function() { logout(); };
	    	    showConfirm( $buttontext.logout,msg,null,okButtonClickCallback,getCurrentUrl() );
	    	    return;
	    	}
    	}
    	// If above did not succeed, then use the navigator's back history
    	navigator.app.backHistory();
    	$pictureClicked=false;
    }
}, true );


// Cordova application paused/stopped
document.addEventListener( 'pause', function(e) {
	flushLog( null );
}, false );

// Cordova application resumed
document.addEventListener( 'resume', function(e) {
//	$networkListenerOn = false; // reset the network listener (this is in case, the listening was turned off by the Java module after it recd. network and completed the task)
	try {
		// Reload sync. data and reconcile, in case syncing has happened when the app. was closed
		showLoader($messagetext.refreshingofflinedata + '...');
		initSyncData(function () {
			try {
				if ( reconcileSyncedData($entity ? $entity.kid : null)) { // there was some reconciliation of responses
					// Change to the landing page, post login - so that reconcilied views appear fresh (TODO: try to refresh the current page after reconciliation)
					if ($hasMultipleEntities)
						$.mobile.changePage('#entities');
					else
						$.mobile.changePage('#menu');
				}
			} catch (e) {
				///console.log('event:app-resume: ERROR: ' + e.message + ', ' + e.stack);
				logR( 'e', 'app.resume', e.message, e.stack );
			} finally {
				hideLoader();
			}
		});
		// Set network listener to off (this is because, there is a chance that the offline sync. that was completed which shut off network listener; and now again there is no network; hence, for safety, we are turning this flag off on each time the app. resumes)
		$networkListenerOn = false;
	} catch ( e ) {
		///console.log( 'resume: ERROR: ' + e.message + ', ' + e.stack );
		logR( 'e', 'resume', e.message, e.stack );
	} finally {
		hideLoader();
	}
}, false );


// Bind to the pageinit event of loginmain to check if a past login existed
$('#login').live( 'pageinit', function( e ) {
	// Check if a past login existed; if so, set the last user ID in login form
	var lastUserId = getLastUser();
	var login = $( '#login' );
	if ( lastUserId != null )
		login.find('#userid').val( lastUserId );
	if ( isLoginAsReconnect() ) {
		login.find('#refreshtext').hide();
		login.find('#refreshbutton').hide();
	} else { // show these buttons, just in case they were disabled before
		login.find('#refreshtext').show();
		login.find('#refreshbutton').show();
	}
	//get host domain name
	var hostDomain =  getHostDomain();
	if (hostDomain)
		$host = hostDomain;
} );



$('#login').live( 'pageshow', function( e ) {
	//update support phone and email from config
    updateSupportInformation('#login');
	// Ensure refresh button is hidden/shown, depending on the login-as-reconnect configuration
	var login = $( '#login' );
	if ( isLoginAsReconnect() ) {
		login.find('#refreshtext').hide();
		login.find('#refreshbutton').hide();
	} else { // show these buttons, just in case they were disabled before
		login.find('#refreshtext').show();
		login.find('#refreshbutton').show();
	}
} );


// On initing the menu page (once only event), log the fact that one is in the menu screen - this represents the start of the app.
$( '#menu').live( 'pageinit', function(e) {
	// Log the start of the app. with a particular kiosk
	logR( 'i', 'menu.pageinit', ( $entity ? $entity.kid : '' ), null );
} );

$('#menu').live( 'pageshow', function( e ) {
	// Update support phone and email from config
  	updateSupportInformation('#menu');
	$("#menu").find( '#needhelptext').text($labeltext.needhelptext);
	// Check if an inventory refresh is required in the background
	if ( !$reconnect && !isInventoryRefreshed() ) {
		refreshInventory( false ); // do not change to materials page
	}
} );


$('#materialinfo').live( 'pagebeforeshow', function( e ) {
	var picker;
	picker = $('#actualTransactionDate').mobipick({
			dateFormat:  $datePickerFormat,
			locale: getUserLanguage()
		});

});

$('#review').live( 'pagebeforeshow', function( e ) {
	var picker;
	picker = $('#rbd').mobipick({
		dateFormat: $datePickerFormat,
		locale: getUserLanguage()
	});

});


$('#orderinfo').live( 'pagebeforeshow', function( e ) {
	var picker;
	picker = $('#rbd').mobipick({
		dateFormat: $datePickerFormat,
		locale: getUserLanguage()
	});

});


$('#orderinfo').live( 'pageaftershow', function( e ) {
	var picker;
	picker = $('#rbd').mobipick({
		dateFormat: $datePickerFormat,
		locale: getUserLanguage()
	});

});

$('#batchinfo').live( 'pagebeforeshow', function( e ) {
	$('#batchExpiryDate').mobipick({
		dateFormat: "dd/MM/yyyy",
		locale: getUserLanguage()
	});
	$('#manufacturedDate').mobipick({
		dateFormat: "dd/MM/yyyy",
		locale: getUserLanguage()
	});

	$('#batchExpiryDate').mobipick().on( 'tap', function() {
		removeFieldHighlight($( '#batchExpiryDate' ) );
		$('#batchExpiryDate_error').hide();
	});
});



$('#orderfulfilment').live( 'pagebeforeshow', function( e ) {
	var picker;
		picker = $( ".mobidatepicker" ).mobipick({
			dateFormat: $datePickerFormat,
			locale: getUserLanguage()
		});
});



$('#orderstatuschange').live( 'pagebeforeshow', function( e ) {
	var picker;
	picker = $( ".mobidatepicker" ).mobipick({
		dateFormat: $datePickerFormat,
		locale: getUserLanguage()
	});
});





$('#languagename').live('change', function(e) {
	     var localeLanguage = $('#languagename').val();
		 // Log the language choice
		 var lastUserId = getLastUser();
		 logR( 'i', 'language.change', 'uid:' + lastUserId + ',lang:' + localeLanguage, null );
	      loadLanguage(localeLanguage,function(){
		  //set login page text
	          initializeLoginPage(localeLanguage);
	      });
 });

$('#orders').live( 'pageinit', function( e ) {

	$('#orders').on('click', '.ui-radio', function( e ) {
		e.stopImmediatePropagation();
	});
});
////// pagebeforechange
$( document ).bind( 'pagebeforechange', function( e, data ) {
	if ( typeof data.toPage == 'string' ) {
		var u = $.mobile.path.parseUrl( data.toPage );
		///console.log( 'string URI; u.href = ' + u.href + ', u.filename = ' + u.filename + ', u.hash = ' + u.hash );
		// Process the page transition
		if ( u.hash.search( /^#entities/ ) != -1 ) { //////////////// Entities page ////////////////// NOTE: included mainly to support back navigation and passing of page offset
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the page params, if any
			var params = getURLParams( u.hash ); // looking for page offset, primarily
			// Get the list of entity ids
			var entityIds = getUserData( $uid ).ki;
			// Render entities
			renderEntities( '#entities', entityIds, params, data.options );
		} else if ( u.hash.search( /^#menu/ ) != -1 ) { //////////////// Menu page //////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get URL params, if any
			var params = getURLParams( u.hash );
			// Render menu
			renderMenu( '#menu', params, getPrevPageId( data.options ), data.options );
		}  else if ( u.hash.search( /^#materials/ ) != -1 ) { //////////////// Materials page //////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the command from the URL
			var params = getURLParams( u.hash );
			// IMPORTANT: set the current operation, if passed; else leave it alone
			if ( params.op )
				$currentOperation = params.op;
			if ( params.kid )
		 	{
		    	 $entity = getEntity($uid,params.kid);
		    	 initMaterialData( $entity.mt, $entity.cu );
		    	 updateRelatedEntityIndices();
		  	}
			if ( params.lkid ) {
				// Get the linked entity object
				var linkedEntity = {};
				linkedEntity.kid = params.lkid;
				linkedEntity.type = getRelationshipTypeForOperation( $currentOperation );
				if ($currentOperation == 'ei' || $currentOperation == 'er' || ($currentOperation == 'no' && $newOrderType == 'prc')) {
					if ( !getSelectedLinkedEntity($currentOperation,$entity))
						togglelinkedEntityForMaterials($currentOperation,linkedEntity);
				}
				 // Store the linked kiosk Id as a local modification for the given entity and operation
				 storeLocalModifications( $currentOperation, $entity, $uid, null, null, linkedEntity );
			}
			// Load the materials (either locally or over network)
			renderMaterials( '#materials', params, data.options );
		} else if ( u.hash.search( /^#materialinfo/ ) != -1 ) { //////////////////// Material info. /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the params.
			var params = getURLParams( u.hash );
			// Get form related data (including saved entries and configuration, say, reasons)
			renderMaterialInfo( '#materialinfo', params, data.options );
		} else if ( u.hash.search( /^#batchinfo/ ) != -1 ) { //////////////////// Batch info. /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the params.
			var params = getURLParams( u.hash ); // encoding/decoding the / in batch ID/expiry is necessary
			// Render batch information form
			renderBatchInfo( '#batchinfo', params, data.options );
		} else if ( u.hash.search( /^#orderallocate/ ) != -1 ) { //////////////////// Batch info. /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the params.
			var params = getURLParams(u.hash); // encoding/decoding the / in batch ID/expiry is necessary
			// Render batch information form
			renderOrderAllocate('#orderallocate', params, data.options);
		} else if ( u.hash.search( /^#orderfulfilment/ ) != -1 ) { //////////////////// Batch info. /////////////////
				// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
				e.preventDefault();
				// Get the params.
				var params = getURLParams( u.hash ); // encoding/decoding the / in batch ID/expiry is necessary
				// Render batch information form
				renderOrderFulfilment( '#orderfulfilment', params, data.options );
		}  else if ( u.hash.search( /^#orderstatuschange/ ) != -1 ) { //////////////////// Batch info. /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the params.
			var params = getURLParams( u.hash ); // encoding/decoding the / in batch ID/expiry is necessary
			// Render batch information form
			renderOrderStatusChange( '#orderstatuschange', params, data.options );
		} else if ( u.hash.search( /^#shipmentinfo/ ) != -1 ) { //////////////////// Batch info. /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the params.
			var params = getURLParams(u.hash); // encoding/decoding the / in batch ID/expiry is necessary
			// Render batch information form
			renderShipmentInfo('#shipmentinfo', params, data.options);
		}
		else if ( u.hash.search( /^#orderiteminfo/ ) != -1 ) { //////////////////// Batch info. /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the params.
			var params = getURLParams( u.hash ); // encoding/decoding the / in batch ID/expiry is necessary
			//
			renderOrderItemInfo( '#orderiteminfo', params, data.options );
		} else if ( u.hash.search( /^#ordercomments/ ) != -1 ) { //////////////////// Batch info. /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the params.
			var params = getURLParams( u.hash ); // encoding/decoding the / in batch ID/expiry is necessary
			// Render batch information form
			renderOrderComments( '#ordercomments', params, data.options );
		} else if ( u.hash.search( /^#relatedentities/ ) != -1 ) { ///////////////// Related entities /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the command from the URL
			var params = getURLParams( u.hash );
			// IMPORTANT: set the current operation, if passed; else leave it alone
			if ( params.op )
				$currentOperation = params.op;
			 if ( params.kid )
			 	{
			    	 $entity = getEntity($uid,params.kid);
			    	 initMaterialData( $entity.mt, $entity.cu );
			    	 updateRelatedEntityIndices();
			  	}
			// Render order info.
			renderRelatedEntities( '#relatedentities', params, data.options );
		} else if ( u.hash.search( /^#review/ ) != -1 ) { ////////// Review ///////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			var params = getURLParams( u.hash );
			//added for notification panel
			if ( params.op )
				$currentOperation = params.op;
			 if ( params.kid )
		 	{
		    	 $entity = getEntity($uid,params.kid);
		    	 initMaterialData( $entity.mt, $entity.cu );
		  	}
			//
			// Render review page
			renderReview( '#review', params, data.options );
		} else if ( u.hash.search( /^#orders/ ) != -1 ) { ///////////////// Orders /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			var params = getURLParams( u.hash );
			if ( params.op )
				$currentOperation = params.op;
			// Render orders
			renderOrders( '#orders', params, data.options );
		} else if ( u.hash.search( /^#orderinfo/ ) != -1 ) { ///////////////// Order info. /////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			var params = getURLParams( u.hash );
			renderOrderInfo( '#orderinfo', params, data.options );
		} else if ( u.hash.search( /^#addmaterialstoorder/ ) != -1 ) { //////////////// Add Materials to Order ////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Set current operation
			$currentOperation = 'ao'; // add to order (this is used to index local changes for adding to order)
			// Get the command from the URL
			var params = getURLParams( u.hash );
			// Render materials
			renderAddMaterialsToOrder( '#addmaterialstoorder', params, data.options );
		} else if ( u.hash.search( /^#setupentity/ ) != -1 ) {
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get params
			var params = getURLParams( u.hash );
			// Go to login page
			renderSetupEntity( '#setupentity', params, data.options );
		} else if ( u.hash.search( /^#entityinfo/ ) != -1 ) {
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get params
			var params = getURLParams( u.hash );
			if (params.kid)
			{
				kid = params.kid;
				$entity = getEntity($uid,kid);
			}
			// Go to login page
			renderEntityInfo( '#entityinfo', params, data.options );
		} else if ( u.hash.search( /^#imagegallery/ ) != -1 ) {
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get params
			var params = getURLParams( u.hash );
			// Go to login page
			renderImageGallery( '#imagegallery', params, data.options );
		} else if ( u.hash.search( /^#myentityrelationships/ ) != -1 ) {
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get params
			var params = getURLParams( u.hash );
			// Go to login page
			renderRelatedEntities( '#myentityrelationships', params, data.options );
		} else if ( u.hash == '#logout' ) {
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Logout
			//logout();
			confirmLogout();
			 // Go to login page
			 //$.mobile.changePage( '#login', data.options );
		} else if ( u.hash.search( /^#export/ ) != -1 ) {
			e.preventDefault();
			// Get params
			var params = getURLParams( u.hash );
			// Go to login page
			renderExport( '#export', params, data.options );			
		} else if (u.hash == '#printstock') {
			e.preventDefault();
			var params = getURLParams( u.hash );
			// Go to login page
			renderPrintStock( '#printstock', params, data.options );
		}else if (u.hash == '#printtransactions') {
				e.preventDefault();
				var params = getURLParams( u.hash );
				// Go to login page
				renderPrintTransactions( '#printtransactions', params, data.options );	
		} else if ( u.hash == '#photoreview' ) {
			e.preventDefault();
			// Go to login page
			renderPhotoReview( '#photoreview', undefined, data.options );
		} else if ( u.hash == '#transfer' ) {
			e.preventDefault();
			// Go to login page
			renderTransfer( '#transfer', undefined, data.options );
		}	else if ( u.hash.search( /^#ordertypeselection/ ) != -1 ) {
			var params = getURLParams( u.hash );
			if (params.kid)
			{
				kid = params.kid;
				$entity = getEntity($uid,kid);
				updateRelatedEntityIndices();
			}
			e.preventDefault();
			// Go to login page
			renderOrderTypeSelection( '#ordertypeselection', undefined, data.options );
		}else if ( u.hash.search( /^#unsententities/ ) != -1 ) { //////////////// Entities page ////////////////// NOTE: included mainly to support back navigation and passing of page offset
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the page params, if any
			var params = getURLParams( u.hash ); // looking for page offset, primarily	
			if ( params.op )
				eventType = params.op;
			if (params.kid)
			{
				kid = params.kid;
			}
			// Get the list of entity ids
			  var entityIds = $ntfyMdl[eventType].kids;
			// Render entities
			renderUnsentEntities( '#unsententities', $uid,entityIds, params, data.options );
		       }else if ( u.hash.search( /^#unsentrelatedentities/ ) != -1 ) { //////////////// Entities page ////////////////// NOTE: included mainly to support back navigation and passing of page offset
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the page params, if any
			var params = getURLParams( u.hash ); // looking for page offset, primarily	
			if ( params.op )
				eventType = params.op;
			if (params.kid)
			{
				kid = params.kid;
			}
			if (kid)
			{
				var linkedIds =  getUnsentRelatedEntityIds($uid,kid,eventType);
			    params.linkedIds = linkedIds;
			}
			// Render entities
			  renderUnsentRelatedEntities("#unsentrelatedentities", $uid,linkedIds,kid,params,  data.options );
		}else if ( u.hash.search( /^#transactions/ ) != -1 ) { //////////////// Materials page //////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the command from the URL
			var params = getURLParams( u.hash );
			// IMPORTANT: set the current operation, if passed; else leave it alone
			
			// Load the materials (either locally or over network)
			renderTransactions( '#transactions', params, data.options );
		}else if ( u.hash.search( /^#forgotpassword/ ) != -1 ) { //////////////// Materials page //////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the command from the URL
			var params = getURLParams(u.hash);
			renderForgotPasswordPage('#forgotpassword',params,data.options );
		}
		else if ( u.hash.search( /^#feedbackpage/ ) != -1 ) { //////////////// Materials page //////////////////
			// Make sure to tell changePage() we've handled this call so it doesn't have to do anything.
			e.preventDefault();
			// Get the command from the URL
			var params = getURLParams( u.hash );
			renderFeedbackPage('#feedbackpage',params,data.options );
		}
		
		
	} // end if ( typeof data.toPage == 'string' )
} );


