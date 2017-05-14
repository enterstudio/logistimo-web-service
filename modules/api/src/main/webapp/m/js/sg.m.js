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

// Pop-up a dialog, without history tracking
function showDialog( title, message, okButtonUrl ) {
	var page = $( '#messagedialog' );
	if (title == null)
		title = $labeltext.oops;
	page.find('#title_d').html( title );
	if ( message != null )
		page.find('#message_d').html( message );
	var okButton = page.find( '#okbutton_d' );
	if ( okButtonUrl != null ) {
		okButton.attr('href', okButtonUrl );
		// Remove the data-rel="back", if present
		if ( hasAttr( okButton, 'data-rel' ) )
			okButton.removeAttr( 'data-rel' );
	} else {
		okButton.attr('href', '#' );
		if ( !hasAttr( okButton, 'data-rel' ) )
			okButton.attr( 'data-rel', 'back' );		
	}
	// Enhance the page
	page.page();
	page.trigger('create');
	// Change to page
	$.mobile.changePage( page, { reverse: false } );
	page.find( '#okbutton_d .ui-btn-text').text($buttontext.ok);

}

function showConfirm( title, message, okButtonUrl, okButtonClickCallBack,cancelButtonUrl ) {
	var page = $( '#messageconfirm' );
	if ( title == null )
	 title = $labeltext.confirm;
	 page.find('#title').html( title );
	if ( message != null )
		page.find('#message').html( message );
	var okButton = page.find( '#okbutton' );
	if ( okButtonUrl != null )
	   okButton.attr( 'href', okButtonUrl );
	var cancelButton = page.find( '#cancelbutton' );
	if ( cancelButton != null )
	    cancelButton.attr( 'href', cancelButtonUrl );
	// Enhance the page
	page.page();
	page.trigger('create');
	// Bind a callback to OK button click, if given
	if ( okButtonClickCallBack != null ) {
		okButton.unbind( 'click.messageconfirm' ).on( 'click.messageconfirm', function() { 
			okButtonClickCallBack();
			$(this).off( 'click.messageconfirm' );
		} );
	} else
		okButton.unbind( 'click.messageconfirm' );
	// Change to page
	$.mobile.changePage( page, {
		//transition: "none",
		reverse: false
	});
	page.find( '#okbutton .ui-btn-text').text($buttontext.yes);
	page.find( '#cancelbutton .ui-btn-text').text($buttontext.no);
}

function showLoader( msg ) {
	$.mobile.loading( 'show', { theme: 'b', text: msg, textVisible: true } );
}

function hideLoader() {
	$.mobile.loading( 'hide' );
}

function hasAttr( node, attrName ) {
	return ( node.attr( attrName ) !== undefined && node.attr( attrName ) !== false );
}

function getPrevPage() {
	return $(this).prev('div[data-role="page"]');
}

function showPopupDialog (header, message,okButtonFunction,cancelButtonFunction,extraHtml,popupId, alertOnly){
	if (isDevice ())
		showCordovaPopupDialog (header, message,okButtonFunction, cancelButtonFunction, alertOnly)
	else
		showBrowserPopupDialog(header, message,okButtonFunction,cancelButtonFunction,extraHtml,popupId, alertOnly)
}

function showCordovaPopupDialog (header, message,okButtonFunction, cancelButtonFunction, alertOnly) {
	var buttonLabels = $buttontext.yes+','+$buttontext.no;
	if ( !alertOnly )
		navigator.notification.confirm(message, confirmCallback, header, buttonLabels);
	else // show alert message
		navigator.notification.alert( message, null, header, $buttontext.ok);

	function confirmCallback(buttonIndex) {
		if (buttonIndex == 1)
			okButtonFunction();
		else if (buttonIndex == 2)
			cancelButtonFunction();
	}

}


function showBrowserPopupDialog (header, message,okButtonFunction,cancelButtonFunction,extraHtml,popupId,alertOnly) {
	$('#'+popupId+'-popup').remove();
	$('#'+popupId+'-screen').remove();
	$('input[id^="popup"]').remove();

	$.mobile.activePage.find().remove('#'+popupId);
	//create a div for the popup
	var odiv = ($('#'+popupId).length) ? $('#'+popupId) : $("<div id='"+popupId+"' data-role='popup'></div>");
	var popUp = odiv.popup({
		dismissible : false,
		transition : "pop",
		overlayTheme : "a",
		history:false
	}).on("popupafterclose", function() {
		//remove the popup when closing
		$(this).remove();
		$('#'+popupId+'-popup').remove();
		$('#'+popupId+'-screen').remove();
	});

	$('#'+popupId +'-popup').css("max-width","150px");

	//create a title for the popup
	var $divHeader = $("<div/>", {"class":"ui-header ui-bar-a"});
	$("<p style='text-align:center'>" + header + "</p>").appendTo($divHeader);
	$divHeader.appendTo(popUp);

	//create content
	var $divContent = $("<div class='ui-content'>" + message + "</div>");
	$divContent.appendTo(popUp);

	if (extraHtml && extraHtml != '') {
		popUp.append(extraHtml);
	}
	okButtonFunction = (okButtonFunction) ? okButtonFunction : function(){};
	cancelButtonFunction = (cancelButtonFunction) ? cancelButtonFunction : function(){};

	//Create a submit button
	$("<a id='popupokbutton'>", {
		text : "Ok",
		"data-jqm-rel" : "back"
	}).buttonMarkup({
		inline : true,
		theme: "b",
	}).on("click", function() {
		//close
		popUp.detach();
		popUp.popup("close");
		setTimeout(function(){okButtonFunction();},200);
	}).appendTo(popUp);

	//create a back button
	if ( !alertOnly ) {
		$("<a id='popupcancelbutton'>", {
			text: "Cancel",
			"data-jqm-rel": "back"
		}).buttonMarkup({
			inline: true
		}).on("click", function () {
			//close
			popUp.detach();
			popUp.popup("close");
			setTimeout(function () {
				cancelButtonFunction();
			}, 200);
		}).appendTo(popUp);
	}

	popUp.trigger("create");
	popUp.popup("open");
	//setTimeout(function(){popUp.popup("open");},100);
	$( '#popupcancelbutton .ui-btn-text').text($buttontext.no);
	$( '#popupokbutton .ui-btn-text').text($buttontext.yes);
}


//////////////// General ////////////////////
// Login or reconnect
function login( isReconnect ) {
	$uid = $('#login').find('#userid').val().trim().toLowerCase();
	$pwd = $('#login').find('#pwd').val();
	// Validate form fields
	if ( $uid == '' || $pwd == '' ) {
		showDialog( null, $messagetext.invalidnameorpassword, '#login' );
		return;
	}
	// Set global reconnect flag
	$reconnect = ( isLoginAsReconnect() ? true : isReconnect );
	doLogin( undefined );
}

//Do a local or remote login and init. data
function doLogin( options ) {
	// Set loader
	var loadingmsg = $messagetext.authenticating;
	showLoader( loadingmsg+'...' );
	// Get the geo-location - only if geo-coding strategy is not strict (if strict, get it just before sending)
	if ( !isGeoStrategyStrict() )
		acquireGeoLocation( gotPosition, errorGettingPosition );
	// Check local versus remote login, and process appropriately
	if ( !$reconnect ) { // regular login; check locally first
		// Check if user's local data exists
		var loginData = getLoginData( $uid );
		var authToken = getAuthenticationToken();
		if ( loginData && authToken ) { // previous login exists and token is valid; authenticate locally
			// Hide loader
			hideLoader();
			if ( !authenticate( loginData, $pwd ) ) {
				showDialog( null, $messagetext.invalidnameorpasswordverify, '#login' );
				logR( 'w', 'doLogin', 'Invalid local login credentials, uid: ' + $uid, null );
			} else { // locally authenticated
				logR( 'i', 'doLogin', 'authenticated (local), uid: ' + $uid, null );
				// Load user data and show screens
				processUserData( $uid, $pwd, $version, null, null, options );
			}
		} else {
			$reconnect = true; // for some reason login data not available; so reconnect
		}
	}
	// Reconnect or first time login (NOTE: this should NOT be an 'else' block, given $reconnect can become true in the above block)
	if ( $reconnect ) {
		if ( isDevice && !hasNetwork() ) {
			hideLoader();
			toast( $messagetext.nonetworkconnection );
			return;
		}
		initEventModel($uid,null);
		if ( !isLoginAsReconnect() && getNotificationCount() > 0 ){
				var cancelUrl = "#login";
				showConfirm($messagetext.warning, $messagetext.somedataenterednotsentrefresh, null,
					function () {
						clearLocalDataAndAuthenticate($FETCH_SIZE, options);
					}, cancelUrl);
		} else {
			clearLocalDataAndAuthenticate($FETCH_SIZE, options);
		}
	} // end if ( reconnect )
}


// Clear local data and authenticate with server
function clearLocalDataAndAuthenticate( fetchSize, options ) {
	// Reconcile synced data, if any
	reconcileAllSyncedData( $uid, true );
	// Authenticate with server and fetch kiosk data
	fetchAuthData(fetchSize, undefined, undefined, options);
}

//Forgot password
function processForgotPassword(resetPassword) {
	var puid = $('#forgotpassword').find('#puserid').val().trim().toLowerCase();
	// Validate form fields
	if ( puid == '' ) {
		showDialog( $labeltext.forgotpassword, $messagetext.enterusername, '#forgotpassword' );
		return;
	}
	var otp = $('#forgotpassword').find('#otpfield').val().trim();
	if ( resetPassword && otp == '' ) {
		showDialog( $labeltext.forgotpassword, $messagetext.enterotp, '#forgotpassword' );
		return;
	}
	var url = $host + '/api/l?a=fp&uid=' + puid + '&ty=p&au=o';
	if (resetPassword)
     url = url+ '&otp='+otp;
	var loadingMsg = $messagetext.sending + '...';
	var request = {
		url: url,
		dataType: 'json',
		cache:false,
		success: function(o){
			if ( o.st == '0') {
				if (!resetPassword)
					showDialog($labeltext.forgotpassword, $messagetext.otpsenttomobile, '#forgotpassword?otp=rs');
				else
					showDialog($labeltext.forgotpassword, $messagetext.passwordsenttomobile, '#login');
			} else { // possible error
				showDialog( null, o.ms, '#forgotpassword' );
			}
		}
	};
	var logData = {
		functionName: 'processForgotPassword',
		message: ''
	};
	// Request server
	requestServer( request, loadingMsg, logData, false, false );
}

//Forgot password
function processSendFeedback(nextUrl) {
	var feedbackMessage = $('#feedbackpage').find('#feedbackmessage').val().trim();
	//var feedbackTopic = $('#feedbackpage').find('#feedbacktopic').val().trim();
	// Validate form fields
	if ( feedbackMessage == '' ) {
		showDialog( $labeltext.feedback, $messagetext.pleaseenterfeedback, '#feedbackpage' );
		return;
	}
	var userid = $uid;
	if (!userid)
	 userid = getLastUser();
	if (!userid) {
		showDialog($labeltext.feedback, $messagetext.pleaseloginbeforesendingfeedback, '#login');
		return;
	}
	var url = $host + '/api/feedback';
	var loadingMsg = $messagetext.sending + '...';
	var postData = {};
	postData.uid = $uid;
	//if (feedbackTopic)
	//	postData.title = feedbackTopic;
	if (feedbackMessage)
		postData.text = feedbackMessage;
	var request = {
		type: 'POST',
		url: url,
		data: postData,
		dataType: 'json',
		cache: false,
		success: function(o) {
			//clear title and topic
			$('#feedbackpage').find('#feedbackmessage').val('');
			//$('#feedbackpage').find('#feedbacktopic').val('');
			showDialog($labeltext.feedback,$messagetext.feedbackacknowledge, nextUrl);
		}
	};
	var logData = {
		functionName: 'processSendFeedback',
		message: ''
	};
	// Request server
	requestServer( request, loadingMsg, logData, false, false );
}

function setupDistrictAndTalukAutocomplete(state){
    var userData = getUserData( $uid );
    var country = userData.cn;
    var locationMetadata = getObject(country);
    if (locationMetadata && locationMetadata[country].states[state] && (Object.keys(locationMetadata[country].states[state].districts)).length > 0){
	    	$("#district").autocomplete({
	    		target: $('#districtSuggestions'),
	    		source: Object.keys(locationMetadata[country].states[state].districts).sort(),
	    		callback: function(e) {
	    			var $a = $(e.currentTarget);
	    			$('#district').val( $a.text() );
	    			$("#district").autocomplete('clear');
	    			var taluks = locationMetadata[country].states[state].districts[$a.text()].taluks;
	    			if (taluks && taluks.length > 0){
	    				$("#taluk").autocomplete({
	    					target: $('#talukSuggestions'),
	    					source: taluks.sort(),
	    					callback: function(e) {
	    						var $t = $(e.currentTarget);
	    						$('#taluk').val( $t.text() );
	    						$("#taluk").autocomplete('clear');
	    					},
	    					minLength: 1
	    				});				
	    			} else {
	    				$("#taluk").autocomplete('destroy');				
	    			}
	    		},
	    		minLength: 1
	    	});    	
	} else {
			$("#district").autocomplete('destroy');	
			$("#taluk").autocomplete('destroy');
	}
}
function fetchLocationInformation(country) {
	var url = $host + '/api/loc?a=getlocationconfig&uid=' + $uid + '&key=' + country;
	var request = {
		url: url,
		dataType: 'json',
		cache:false,
		headers: { X_ACCESS_INITIATOR: '1' }, // system initiated request
		success: function(o) {
			storeObject(country, o.data);
		}
	};			// TODO: add sync
	var logData = {
		functionName: 'fetchLocationInformation',
		message: country
	};
	// Request server
	requestServer( request, null, logData, true, false );
}
// Authenticate and get kiosks incrementally in authenticate-output (ao) object
function fetchAuthData( size, cursor, aoSoFar, options ) {
	var loadingmsg = $labeltext.loading;
	showLoader( loadingmsg + '... ' + ( aoSoFar && aoSoFar.ki.length > 0 ? '(' + aoSoFar.ki.length + ')' : '' ) );
	var language = getUserLanguage();
	// Load user data via a remote login
	var url = $host + '/api/l?a=li&lcl=' + language + '&mrsp=2&v=' + $version + '&s=' + size; // mrsp: min. response code of 2 implies fetch related-entities also incrementally
	if ( cursor )
		url += '&cs=' + cursor;
	if ( aoSoFar && aoSoFar.ki && aoSoFar.ki.length > 0)
	    url += '&o='+ aoSoFar.ki.length;
	$.ajax( {
		url: url,
		dataType: 'json',
		cache: false,
		timeout: REQUEST_TIMEOUT_DEFAULT,
		headers: getBasicAuthenticationHeader(),	// NOTE: This should be basic authentication (and NOT token-based authentication), given token does not exist for the login call
		success: function( o, status, xhr ) {
			// Hide loader
			hideLoader();
			// Get the authentication data (for token-authentication)
			var authTokenData = getAuthTokenFromResponse( xhr );
			if ( o.st == '0' || ( o.st == '1' && aoSoFar && aoSoFar.ki && aoSoFar.ki.length > 0 ) ) {
				if ( !aoSoFar ) {
					aoSoFar = o;
					// Clear local data
					clearLocalData();
					// Log
					logR( 'i', 'fetchAuthData', 'authenticated (server), uid: ' + $uid, null );
				} else if ( o.ki ) { // update kiosks so far
					for ( var i = 0; i < o.ki.length; i++ )
						aoSoFar.ki.push( o.ki[ i ] );
				}
				var hasMoreData = ( o.ki && o.ki.length == size );
				if ( hasMoreData ) { // has more data	
					// Fetch the next page (recursive call)
					fetchAuthData( size, o.cs, aoSoFar, options );
				} else {
					// Process authentication response data
					processUserData( $uid, $pwd, $version, authTokenData, aoSoFar, options );
					// Fetch location configuration information
					fetchLocationInformation(aoSoFar.cn);
				}
			} else { // possible error, or no more kiosks
				if ( !aoSoFar ) {
					showDialog( null, o.ms, '#login' );
					logR( 'e', 'fetchAuthData', 'error: status: ' + o.st + ', err: ' + o.ms, null );
					return;
				} else {
					// Process authentication response data
					processUserData( $uid, $pwd, $version, authTokenData, aoSoFar, options );
				}
			}
		},
		error: function(o) {
			// Hide the page loader
			hideLoader();
			var errMsg = handleResponseCodes( o );
			// Show error
			showDialog( null, errMsg, '#login' );
			// Log
			logR( 'e', 'fetchAuthData', 'ajax response err: ' + ( errMsg ? errMsg : '' ), null );
		}
	} );
}

// Handle user data (expecting AuthenticateOutput JSON as parameter)
// NOTE: authTokenData is a JSON object containing the server-token and an expiry
function processUserData( userId, password, version, authTokenData, ao, options ) {
	// Reconcile generic (non-kiosk) sync-ed data, if any
	reconcileSyncedData( null );
	// Check for login error
	var errMsg = getDataError( ao );
	if ( errMsg != null ) {
		showDialog( null, errMsg, '#login' );
		return;
	}
	var userData = null;
	// If authentication response exists, then update local storage
	if ( ao != null ) {
		storeAuthData( userId, password, version, authTokenData, ao );
		userData = ao;
	} else {
		userData = getUserData( userId );
	}
	if ( userData == null ) {
		showDialog( null, $messagetext.unabletofinduserdata, '#login' );
		return;	
	}
	if ( userData.df) {
		 var dateFormat = userData.df;
		$userDateFormat = dateFormat.toLowerCase();
		if (dateFormat.indexOf(' ') > 0 && userData.lg && userData.lg.toLowerCase() == 'en' ) {
			//remove the time format
			 $userDateFormat = dateFormat.substr(0, dateFormat.indexOf(' ')).toLowerCase();
			setDatePickerFormat($userDateFormat);
		}
	}
	if (getUserPermissions(userId) == $ASSETS_ONLY) {
		showDialog( null, $messagetext.userhasassetonlypermission, '#login' );
		return;
	}

	// Update search indices, if required
	$searchIndex = {}; // reset the search index
	// If there are multiple entities, then render entity list; else, go display main menu
	$hasMultipleEntities = hasMultipleEntities( userData );
	// Update this user's role
	if ( userData.rle )
		$role = userData.rle;
	// Update the global config.
	if ( userData.cf )
		$config = userData.cf;
	if ( isDevice()) {  //upgrade app check
		checkAppVersion();
	}
	if ($config && $config.dop)
	   $disableOrderPrice = true;
	else
		$disableOrderPrice = null;
	// Register alarms (for log. sync., etc.), if needed
	if ( isDevice() && getAlarmInterval() > 0 ) // alarm registered only if one valid alarm interval exists
		registerAlarms( $reconnect );
	// Set login as reconnect, if required
	storeLoginAsReconnect( $config.lasr );
	storeSupportInfo( userId );
	if ( $hasMultipleEntities ) {
		// Update the entities search/tag indices, as appropriate
		updateManagedEntitiesIndices( userData );
		//see if there is a primary entity configured.
		if (userData.kid) {
			doInitKiosk(userData.kid, 0, '#login', options);
		} else {
			// Render entities
			renderEntities( '#entities', userData.ki, null, options );			
		}
	} else {
		if ( userData.ki && userData.ki.length == 1 )
			doInitKiosk( userData.ki[0], 0, '#login', options );
		else
			showDialog( null, $messagetext.unabletofinduserdata, '#login' );
	}
}


// Init. materials and render
function doInitKiosk( kid, pageOffset, prevPageId, options ) {
	// Show loading
       var loadingmsg = $labeltext.loadingmaterials;
	showLoader(loadingmsg +'...' );
	var currentParentEntityId;
	//Drill down of related entity 
	if ($entity && prevPageId.indexOf('myentityrelationships') != -1 )
		currentParentEntityId = $entity.kid; //Store parent entity id
	if ( kid ) {
		$entity = getEntity($uid, kid);
	}
	//Check if kid is linked/related Entity ID
	var relationType;
	if (currentParentEntityId && currentParentEntityId != kid) {
		relationType = getSelectedLinkedEntityType(kid, currentParentEntityId);
	}
    if (!$entity){
			showDialog(null, $messagetext.unabletofindentitydata, getCurrentUrl());
			return;
	}
	// Get the params. object with a back page offset (so back URL of next page can point to this page)
	var params = {};
	params.bo = pageOffset;
	var entityId = kid;
	if ( !entityId )
		entityId = $entity.kid;
	params.kid = entityId;
	if (relationType) {
		//$parentEntityId = currentParentEntityId;
		//$parentRelationType = relationType;
		var p = {};
		p.kid = currentParentEntityId;
		p.type = relationType;
		$parentEntity.push(p);
	}
	else{
		//$parentEntityId = null;
		//$parentRelationType = null;
		clearParentEntity();
	}

	// Reset current operation
	$currentOperation = null;
	// Fetch and initialize related-entities and materials data, and move to next screen
	fetchKioskData( params, prevPageId, options );
}

// Fetch kiosk data (related-entities and materials)
function fetchKioskData( params, prevPageId, options ) {
	fetchRelatedEntities( 'csts', $FETCH_SIZE, undefined, 0, params, function() {
				fetchRelatedEntities( 'vnds', $FETCH_SIZE, undefined, 0, params, function() {
								fetchMaterials( $FETCH_SIZE, undefined, 0, params, prevPageId, options );
							});
			});
}

// Fetch data associated with a given entity
function fetchMaterials( size, cursor, gotSoFar, params, prevPageId, options ) {
	// Get and store materials, if not locally available
	var entity;
	if (params && params.kid)
		entity = getEntity( $uid, params.kid);
	if (!entity)
		return;
	if ( !entity.mt || cursor || gotSoFar > 0 ) {
		//Load user data via a remote login
		var url = $host + '/api/i?a=gi&uid=' + $uid + '&kid=' + entity.kid + '&s=' + size; // earlier passing uid, p, but now using BasicAuth header
		if ( cursor )
			url += '&cs=' + cursor;
		// Loading message
		var loadingMsg = $labeltext.loadingmaterials + '...';
		if ( gotSoFar > 0 ) {
			loadingMsg += ' (' + gotSoFar + ')';
			url += '&o=' + gotSoFar;
		}
		var request = {
			url: url,
			dataType: 'json',
			cache: false,
			success: function(o) {
				// Get the materials
				if ( o && ( o.st == '0' || ( o.st == '1' && gotSoFar > 0 ) ) ) { // TODO: right now, error is sent when there are no more materials; so handle it as above
					if ( o.mt ) {
						if ( !entity.mt )
							entity.mt = [];
						for ( var i = 0; i < o.mt.length; i++ )
							entity.mt.push( o.mt[i] );
						// Persist material data within entity
						storeEntity( $uid, entity );
					}
					var hasMoreData = ( o.mt && o.mt.length == size  );
					//var hasMoreData = ( o.mt && o.mt.length == size && (o.cs || ( gotSoFar + size ) > 0));
					if ( hasMoreData ) {
						// Fetch more materials
						fetchMaterials( size, o.cs, ( gotSoFar + size ), params, prevPageId, options  );
					} else {
						if (( prevPageId.indexOf('materialinfo') == -1 ) && ( prevPageId.indexOf('orderinfo') == -1 ) ) {
							$entity = getEntity($uid, entity.kid);
							// Init. the data for this entity (materialData, search indices)
							initKioskData(); /// earlier: $materialData = initMaterialData( $entity.mt, $entity.cu );
							// Hide the page loader
							hideLoader();
							// Set the flag to indicate that inventory is refreshed
							setInventoryRefreshed();
							// Render menu
							renderMenu('#menu', params, prevPageId, options);
						}
						else {
							hideLoader();
							showRelatedEntityStock( entity.kid );
						}
					}
				} else {
					// Show error
					//showDialog( null, o.ms, null );
					showPopupDialog( $messagetext.warning, o.ms, null, null, null, "errorpopupm", true);
					//Error in navigating to related entity move back to parent
					var parentEntity = getParentEntity();
					if (parentEntity && prevPageId.indexOf('materialinfo') == -1)  {
						if (entity.kid != parentEntity.kid)
							$entity = getEntity( $uid,parentEntity.kid );
					}
				}
			}
		};														// TODO; sync?
		var logData = {
			functionName: 'fetchMaterials',
			message: ( gotSoFar ? gotSoFar : 0 )
		};
		// Request server
		requestServer( request, loadingMsg, logData, true, true );
	} else { // materials already available locally
		if ( prevPageId.indexOf('materialinfo') == -1 ) {
			$entity = getEntity($uid, entity.kid);
			// Init. the data for this entity (materialData, search indices)
			initKioskData(); /// earlier: $materialData = initMaterialData( $entity.mt, $entity.cu );
			// Hide loader
			hideLoader();
			// Render menu
			renderMenu('#menu', params, prevPageId, options);
		}
	}
}

// Fetch related entities
function fetchRelatedEntities( relationshipType, size, cursor, gotSoFar, params, callback ) {
	var entity;
	if (params && params.kid)
		entity = getEntity( $uid, params.kid);
	if (!entity)
		return;
	// Check if config. allows fetching of entities
	var allowFetchRelatedEntities = ( relationshipType == 'vnds' ? $config.vndm : $config.cstm ); ///// ensure that the related entities are fetched only if config. allows it
	//if ( !allowFetchRelatedEntities || ( $entity[ relationshipType ] && !cursor  ) ) {
	if ( !allowFetchRelatedEntities || ( entity[ relationshipType ] && ( !cursor && gotSoFar == 0 ) )) {
		if ( callback )
			callback();
		return;
	}
	var loadingMsg = $labeltext.loading+' ' + getRelationshipName( relationshipType, true ) + '...';
	if ( gotSoFar > 0 )
		loadingMsg += ' (' + gotSoFar + ')';
	///showLoader( loadingMsg );
	var linkType = ( relationshipType == 'vnds' ? 'v' : 'c' );
	// Load user data via a remote login
	var url = $host + '/api/s?a=gre&uid=' + $uid + '&kid=' + entity.kid + '&r=' + linkType + '&s=' + size;
	if ( cursor )
		url += '&cs=' + cursor;
	if (gotSoFar > 0)
		url += '&o=' + gotSoFar;
	var request = {
		url: url,
		dataType: 'json',
		cache: false,
		success: function(o) {
			// Get the materials
			if ( o && ( o.st == '0' || ( o.st == '1' && gotSoFar > 0 ) ) ) {
				var relatedEntities = o.ki;
				if ( relatedEntities ) {
					if ( !entity[ relationshipType ] )
						entity[ relationshipType ] = [];
					// Update relationships within entity
					for ( var i = 0; i < relatedEntities.length; i++ ) {
						entity[relationshipType].push(relatedEntities[i]);
					}
					// Persist entity
					storeEntity( $uid, entity );
				}
				var hasMoreData = ( relatedEntities && ( relatedEntities.length == size )  );
				//var hasMoreData = ( relatedEntities && ( relatedEntities.length == size ) && (o.cs || (gotSoFar + size) > 0));
				if ( hasMoreData ) { // fetch the next set
					fetchRelatedEntities( relationshipType, size, o.cs, ( gotSoFar + size ), params, callback );
				} else if ( callback ) {
					callback();
				}
			} else {
				// Show error
				if ( o && o.ms )
					showPopupDialog( $messagetext.warning, o.ms, null, null, null, "errorpopup", true);
					//showDialog( null, o.ms, getCurrentUrl() );
				// Call callback, if any
				if ( callback )
					callback();
			}
		},
		error: function(o) {
			// Call callback, if any
			if ( callback )
				callback();
		}
	};																						// TODO: sync?
	var logData = {
		functionName: 'fetchRelatedEntities',
		message: ( gotSoFar ? gotSoFar : 0 )
	}
	// Request server
	requestServer( request, loadingMsg, logData, true, true );
}

// Check if single or multiple kiosks exist
function hasMultipleEntities( userData ) {
	return ( userData.ki.length > 1 );
}

// Init. all data required for a kiosk
function initKioskData() {
	///console.log( $entity );
	// Init. material data (NOTE: materials search index is updated within)
	initMaterialData( $entity.mt, $entity.cu );
	// Update related entity indices
	updateRelatedEntityIndices();
	// Update order indices
	updateOrdersSearchIndex();
	// Reconcile sync-ed data for this kiosk, if any
	reconcileSyncedData( $entity.kid );
	// Reset partial-errors-shown flag
	$partialErrorsShown = false;
}

function switchEntityContext( entityId ) {
	$entity = getEntity( $uid, entityId );
	var parentEntity = getParentEntity();
	if (parentEntity) {
		if (parentEntity.kid == $entity.kid) {
			//$parentEntityId = null;
			//$parentRelationType = null;
			spliceParentEntity();
		}
	}
	$currentOperation = null;
	initKioskData();
}

// Given a set of materials, return a material list, tag object as follows: 
// { tags: ['tag1','tag2',...], tagmap: { 'tag1' : ['material1', 'material2', ...], },
//   materialmap: { mid: { <material-metadata>, ... } } }
function initMaterialData( materials, currency ) {
	$materialData = {};
	if ( !materials || materials == null )
		return null;
	// Init. material data structures
	var tagList = [];
	var tagMap = {};
	var materialMap = {};
	// Init. the material search index
	$searchIndex.materials = {};
	// Scan the material list to see if there are partial tags
	var numMaterialsWithTags = 0;
	var hasSingleTag = true;
	var prevTag;
	for ( var i = 0; i < materials.length; i++ ) {
		var material = materials[i]; 
		if ( material.tg && material.tg != '' )
			numMaterialsWithTags++;
		if ( i > 0 && hasSingleTag && ( ( prevTag && !material.tg ) || ( !prevTag && material.tg ) || ( prevTag != material.tg ) ) )
			hasSingleTag = false;
		prevTag = material.tg;
	}
	var hasPartialTags = ( numMaterialsWithTags > 0 && ( numMaterialsWithTags != materials.length ) ); // this means some materials have tags and some don't
	// Update the tag/mid mapping and search indices
	for ( var i = 0; i < materials.length; i++ ) {
		var material = materials[i];
		materialMap[ material.mid ] = material;
		var tagsCSV = material.tg;
		if ( ( !tagsCSV || tagsCSV == '' ) && hasPartialTags )
			tagsCSV = $TAG_UNTAGGED;
		else if ( hasSingleTag )
			tagsCSV = ''; // reset this, so that the single tag is not display - it is as if there are no tags
		if ( tagsCSV && tagsCSV != '' ) {
			var tags = tagsCSV.split(',');
			for ( var j = 0; j < tags.length; j++ ) {
				// Update tag list
				if ( $.inArray( tags[j], tagList ) == -1 )
					tagList.push( tags[j] );
				// Update tag map
				var tagMaterials = tagMap[ tags[j] ];
				if ( !tagMaterials )
					tagMaterials = [];
				tagMaterials.push( material.mid );
				tagMap[ tags[j] ] = tagMaterials;
				// Update the material search index
				updateMaterialSearchIndex( material.mid, material.n, tags[j] );
			}
		} else {
			// Update the material search index
			updateMaterialSearchIndex( material.mid, material.n, null );
		}
	}
	// Update $materialData
	$materialData.materialmap = materialMap;
	if ( currency && currency != null )
		$materialData.currency = currency;
	if ( tagList.length > 0 ) {
		tagList.sort(); // Sort the tag list
		$materialData.tags = tagList;
		$materialData.tagmap = tagMap;
	}
}

// Get sorted material metadata, given a list of mids
function getSortedMaterials( mids, materialMap ) {
	var materials = [];
	for ( var i = 0; i < mids.length; i++ )
		materials.push( materialMap[ mids[i] ] );
	materials.sort( function(a,b) {
		if ( a.n > b.n )
			return 1;
		else if ( a.n < b.n )
			return -1;
		else
			return 0;
	});
	return materials;
}

//////////// Rendering functions /////////////////////

// Get the ID of the previous page
function getPrevPageId( options ) {
	var prevPageId = null;
	if ( options && options.fromPage ){
	    if (options.fromPage[0].dataset.role == 'page')
		prevPageId = '#' + options.fromPage.attr('id');
	}
	return prevPageId;
}

function getCurrentUrl() {
   var currentUrl = '#'+$.mobile.urlHistory.getActive().url;
   return currentUrl;
}



// Show/hide elements
function showElement( element, show ) {
	if ( !element )
		return;
	var style = 'display:block';
	if ( !show )
		style = 'display:none';
	element.attr( 'style', style );
}

// Set title bar
function setTitleBar( header, title, isHyperlinked ) {
	var html = '';
	if ( isHyperlinked )
		html += '<a href="#entityinfo" data-transition="slidedown" data-corners="false" data-theme="b">';
	html += title;
	if ( isHyperlinked )
		html += '</a>';
	header.children(':jqmData(role=navbar)').find('li').empty().append( html );
}

function renderPrimaryEntity(kid){
	doInitKiosk(kid, 0, '#entities', undefined);
}
// Render a list of entities
function renderEntities( pageId, entityIds, params, options ) {
	// Get the page offset, if any
	var pageOffset = 0;
	if ( params && params.o )
		pageOffset = parseInt( params.o );
	// Check if currently selected route tag has to be reset
	var prevPageId = getPrevPageId( options );
	if ( prevPageId == null )
		$rtag = null;
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Update the header with the title and number of entities
	var headerTxt = $labeltext.entities+' (' + entityIds.length + ')'
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Enable adding of entities, if permitted
	if ( canAddEntity( 'ents' ) )
		header.find( '#entitiessetup' ).attr( 'style', 'display:block' );
	else
		header.find( '#entitiessetup' ).attr( 'style', 'display:none' );
	// Check if route tags are present
	var hasTags = ( $entitiesData.ents ); // entities have been classified into tags
	var tagList = getRouteTags();
	if ( tagList && $entitiesData.ents && $entitiesData.ents[ $ROUTETAG_UNTAGGED ] ) // add the Un-tagged tag item to the end of the list, if needed
		tagList.push( $ROUTETAG_UNTAGGED );
	// Form the markup for the entity listview, either tagged or directly listed
	var listview;
	if ( hasTags && tagList ) {
		listview = getTagEntityView( 'entities', tagList, $rtag, pageOffset, 'ents', undefined );
	} else {
		listview = getEntityListview( 'entities', entityIds.length, undefined, true );
	}
	// Update content part
	// Check if primary entity is configured; if so, get a switcher between primary entity and managed entities
	var userData = getUserData( $uid );
	var primaryEntitySwitcherMarkup = ( userData.kid ? getPrimaryEntitySwitcherMarkup( false, userData.kid ) : undefined );
	// Update content (first empty)
	content.empty();
	// Event notification panel
	initEventModel($uid,null);
	content.append( getNotificationPanelMarkup(null));
	if ( primaryEntitySwitcherMarkup )
		content.append( $( primaryEntitySwitcherMarkup ) );
	// Append listview
	content.append( listview );
	// Enhance the page
	page.page();
	page.trigger( 'create' );
	// Update the material list, given page is now created
	if ( !hasTags ) {
		updateEntitiesListview( listview, entityIds, undefined, pageOffset, undefined ); // update entity list and refresh
	} else if ( $rtag != null ) {
		// Expand the collapsible for the given tag
		listview.find( pageId + '_' + getId( $rtag ) ).trigger( 'expand' ); // expand the tag; material list is added and refreshed within the tag
	}
	// SEARCH FILTER
	// Loop through tags list and set the list view filter callback function for all the list controls
	if ( hasTags ) {
		for ( var i = 0; i < tagList.length; i++ ) {
			var tagId = pageId.substring( 1 ) + '_' + getId( tagList[i] ) + '_ul';
			// Set the list view filter callback function
			setListViewFilterCallback( tagId, startsWithEntities );
		}
	} else {
		setListViewFilterCallback( pageId.substring( 1 ) + '_ul', startsWithEntities );
	}
	// SEARCH FILTER
	// Now call changePage() and tell it to switch to the page we just modified.
	// To be the url that shows up in the browser's location field,
	// so set the dataUrl option to the URL of the entities
	if ( options ) {
		options.dataUrl = '#entities?o=' + pageOffset;
		$.mobile.changePage( page, options );
	} else {
		$.mobile.changePage( page );
	}
	header.find( '#entitiessetup .ui-btn-text').text($buttontext.addentity);
	header.find( '#entitieslogout .ui-btn-text').text($buttontext.logout);
}

// Get the route tags
function getRouteTags() {
	if ( !$config.rtt || $config.rtt == '' )
		return undefined;
	return $config.rtt.split( ',' );
}

// Update entities list view
function updateEntitiesListview( listview, allEntityIds, rtag, pageOffset, idsToAdd ) {
	var entityIds = [];
	// Get the entities to be added to the list view
	if ( !idsToAdd || idsToAdd == null ) { // get the subset of entities for this page
		var size = 0;
		if ( rtag ) {
			if ( $entitiesData.ents[ rtag ] )
				size = $entitiesData.ents[ rtag ].length;
		} else
			size = allEntityIds.length;
		// Set the listview navigation bar
		var range = setListviewNavBar( listview, pageOffset, size, null, function() {
			updateEntitiesListview( listview, allEntityIds, rtag, ( pageOffset - 1 ), idsToAdd );
		}, function() {
			updateEntitiesListview( listview, allEntityIds, rtag, ( pageOffset + 1 ), idsToAdd );
		} );
		var start = range.start;
		var end = range.end;
		// Get the list of entities for rendering 
		if ( rtag ) {
			var taggedEntityIds = $entitiesData.ents[ rtag ];
			if ( taggedEntityIds ) {
				for ( var i = ( start - 1 ); i < end; i++ )
					entityIds.push( taggedEntityIds[ i ] );
			}
		} else { 
			for ( var i = ( start - 1 ); i < end; i++ ) {
				entityIds.push( allEntityIds[i] );
			}
		}
	} else { // use the specified entity ids
		entityIds = idsToAdd;
	}
	// Render the list items
	for ( var i = 0; i < entityIds.length; i++ ) {
		var entity = getEntity( $uid, entityIds[i] );
		if ( !entity )
			continue;
		if ( 'ne' in entity )
			if (entity.ne) 
			 continue;
		var hasGeo = ( entity.lat && entity.lng );
		var id = 'entities_' + entity.kid;
		var onclick = 'doInitKiosk(\'' + entity.kid + '\',' + pageOffset + ',\'#entities\',undefined)';
		// NOTE: id and href are required below for enabling starts-with search; href along with params. is used below ONLY because the search filter callback expects the kid in the params. to determine which items are on the list; otherwise, href is redundant here and can be set of '#';
		var markup = '<li id="' + id + '"><a href="#dummy?kid=' + entity.kid + '" onclick="' + onclick + '"><h3>' + entity.n + ( hasGeo ? ' <img src="jquerymobile/icons/map_pin.png"/>' : '' ) + '</h3>' +
				  '<p>' + ( entity.sa ? entity.sa + ', ' : '' ) + entity.cty + ( entity.pc ? ', ' + entity.pc : '' ) + ( entity.tlk ? ', ' + entity.tlk : '' ) + '</p></a></li>';
		// Append item to listview
		listview.append( $( markup ) ); 
	}
	// Refresh the view
	listview.listview('refresh');
}

function renderPrintStock( pageId, params, options ) {
	if ( !$entity || $entity == null )
		return;
	var materials = $entity.mt;
	if ( !materials || materials.length == 0 )
		return;
	var page = $( pageId );
	// Get the page header and content
	var content = page.children( ':jqmData(role=content)' );
	// Page text
	var text = '<a href="javascript:history.go(-1)" style="font-size:small">'+$buttontext.back+'</a><p/>';
	text += '<font style="font-size:14pt;font-weight:bold"> '+$labeltext.physicalstock+' - ' + $entity.n + '</font><br/>';
	text += '<font style="font-size:small">' + getFormattedDate(new Date()) + '</font><p/>';
	// Need to build the table and append
	var materialsTable = '<table class="printtable" data-role="table" id="printStockCountTable" data-mode="reflow">\
						<thead>\
					    <tr>\
					      	<th>#</th>\
					      	<th>'+$labeltext.name+'</th>\
					      	<th>'+$labeltext.stockonhand+'</th>\
	      					<th>'+$labeltext.lastupdated+'</th>\
					    </tr>\
					  </thead>\
					  <tbody>';
	
	var tableRows = '';
	for (var i=0; i<materials.length; i++) {
		var m = materials[i];
       /// var tableRow = '<tr class="'+getEventStyleClass(m)+'" >';
		var tableRow = '<tr>';
		tableRow += '<td>' + Number(i+1) + '</td>';
		tableRow += '<td>' + m.n + '</td>';
		tableRow += '<td><font style="color:' + getStockColor(m) + '">' + Number(m.q) + '</font></td>';
		tableRow += '<td>' + m.t + '</td>';
		tableRow += '</tr>';
		tableRows += tableRow;
	}
	materialsTable += tableRows;
	materialsTable += '</tbody></table>';
	// Update DOM
	content.empty();
	content.append(text);
	content.append(materialsTable);
	// Change page
	if ( options ) {
		// Now call changePage() and tell it to switch to the page we just modified.
		$.mobile.changePage( page, options );
	} else
		$.mobile.changePage( page );
}

// Create the layout-view for switching between primary-entity and managed-entities
function getPrimaryEntitySwitcherMarkup( isPrimaryEntity, primaryEntityId ) {
	if ( isPrimaryEntity && $entity.kid != primaryEntityId ) // only show switcher for primary entity and entity list
		return '';
	var primaryEntityChecked = ( isPrimaryEntity ? 'checked' : '' );
	var otherEntitiesChecked = ( isPrimaryEntity ? '' : 'checked' );
	var primaryEntityOnClick = ( isPrimaryEntity ? '' : 'renderPrimaryEntity(\'' + primaryEntityId + '\');' );
	var otherEntitiesOnClick = ( isPrimaryEntity ? '$.mobile.changePage( \'#entities\' );' : '' );
	var markup = '<div data-role="fieldcontain">';
    markup += '<fieldset data-role="controlgroup" data-type="horizontal" data-mini="true">';
    markup += '<input type="radio" name="primaryentityswitcher" id="primaryentityselector" ' + primaryEntityChecked + ' onclick="' + primaryEntityOnClick + '" />';
    markup += '<label for="primaryentityselector">'+$labeltext.myentity+'</label>';
    markup += '<input type="radio" name="primaryentityswitcher" id="otherentitiesselector" ' + otherEntitiesChecked + ' onclick="' + otherEntitiesOnClick + '" />';
    markup += '<label for="otherentitiesselector">'+$labeltext.otherentities+'</label>';
    markup += '</fieldset>';
    markup += '</div>';
    return markup;
}

// Render a list of entities
function renderRelatedEntities( pageId, params, options ) {
	// Get the relationship type
	if ( params && params.ty ) // NOTE: this check is needed, given the Back URLs may not be passing the relation type
		$currentRelationType = params.ty;
	if ( params && params.otype ) // NOTE: this check is needed, given the Back URLs may not be passing the relation type
		$newOrderType = params.otype;
	if (params.kid) {
		//$entity = getEntity($uid, params.kid);
		switchEntityContext ( params.kid );
	}
	if ( pageId != '#myentityrelationships' )
  	 if (unsentInventoryOperations($entity,$currentOperation)) 
	 	return;
	if (unsentOrders($entity,$currentOperation))
	   return;
	// Get the page offset
	var pageOffset = 0;
	if ( params && params.o )
		pageOffset = parseInt( params.o );
	// Get the previous page ID
	var prevPageId = getPrevPageId( options );
	if ( prevPageId != null && prevPageId == '#menu' )
		$rtag_re = null; // reset the tag, if used at all
	// Get the relationships
	///var relationType = getRelationshipTypeForOperation( $currentOperation );
	var relationships = $entity[ $currentRelationType ];
	// Get the back page offset for the back URL
	var backPageOffset = 0;
	if ( params && params.bo )
		backPageOffset = params.bo;
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Get the relationship name
	var relationshipName = getRelationshipName( $currentRelationType, false );
	// Update the header text
	var headerTxt;
	var allowRelatedEntitiesAdd = false;
	if ( pageId == '#myentityrelationships' ) {
		var size = ( relationships ? relationships.length : 0 ); // num relationships
		// Set header title
		headerTxt = getRelationshipName( $currentRelationType, true ) + ' (' + size + ')';
		// Check permissions for Add customer/vendor button
		var setupEntityButton = header.find( '#myentityrelationshipssetup' );
		if ( canAddEntity( $currentRelationType ) ) {
			setupEntityButton.attr( 'style', 'display:block' );
			var kid;
			if ($entity)
				kid = $entity.kid;
			setupEntityButton.attr( 'href', '#setupentity?ty=' + $currentRelationType );
		} else {
			setupEntityButton.attr( 'style', 'display:none' );
		}
	} else {
		headerTxt = getOperationName( $currentOperation );
	}
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update header title
	setTitleBar( header, $entity.n, false );
	var footer = page.children( ':jqmData(role=footer)' );
	var onSkipClick =  'skipLinkedEntitySelection('+ pageOffset + ');';
	//var onClearClick = 'clearLinkedEntitySelection();';
	var msg = '';
	if ($currentOperation == 'ei')
		msg =$messagetext.clearcustomerselected;
	else if ($currentOperation == 'er' )
	    msg = $messagetext.clearvendorselected;
	else if ( $currentOperation == 'no')
		msg = $messagetext.clearvendorselectedpo;
	var onClearClick = 'showConfirm( null, \''+ msg +'\',null, function() {clearLinkedEntitySelection();}, getCurrentUrl())';
	var footerMarkup = '';
	if ( $currentOperation == 'er' || $currentOperation == 'ei' || ($currentOperation == 'no' && $newOrderType == 'prc' )) {
		if (hasLocalModificationsForLinkedEntity($currentOperation, $entity, null))
			footerMarkup = '<a href="#" data-role="button" data-mini="false" data-icon="delete" onclick="' + onClearClick + '" >' + $buttontext.clear + '</a>';
		else
			footerMarkup = '<a href="#" data-role="button" data-mini="false" data-icon="arrow-r" onclick="' + onSkipClick + '">' + $buttontext.skip + '</a>';
	}
	footer.empty().append($(footerMarkup));
	if ( relationships && relationships.length > 0 ) {
		// Get div ID
		var id = pageId.substring( 1 )
		// Get the page Id for the items page, on clicking an entity - whether materials or entityinfo
		var linkedPageId = 'materials';
	    if ( pageId == '#myentityrelationships' ) {
			linkedPageId = 'entityinfo'; // sent by, say, My Customers / My Vendors, where the entity has to be viewed/edited
		}

		// Form the markup for the entity listview
		var infoText;
		if ($currentRelationType == 'csts' )
		   	infoText = $( '<div>'+$messagetext.selectacustomer+'</div><p/>' );
		else
			infoText = $( '<div>'+$messagetext.selectavendor+'</div><p/>' );
		// Get the tag list
		var hasTags = $entitiesData[ $currentRelationType ];
		var tagList = getRouteTags();
		var listview;
		if ( hasTags && tagList ) {
			///tagList = getRouteTags();
			if ( $entitiesData[$currentRelationType] && $entitiesData[$currentRelationType][ $ROUTETAG_UNTAGGED ] ) // add the Un-tagged tag item to the end of the list, if needed
				tagList.push( $ROUTETAG_UNTAGGED );
			listview = getTagEntityView( id, tagList, $rtag_re, pageOffset, $currentRelationType, linkedPageId );
		} else {
			listview = getEntityListview( id, relationships.length, undefined, true );
		}
		// Update content part
		content.empty();
		content.append( infoText );
		content.append( listview );
		// Enhance the page
		page.page();
		page.trigger( 'create' );
		// Update the list, given page is now created
		if ( !hasTags ) {
			updateRelatedEntitiesListview( listview, $currentRelationType, undefined, pageOffset, null, id, linkedPageId ); // update entity list and refresh
		} else if ( $rtag_re != null ) {
			// Expand the collapsible for the given tag
			listview.find( pageId + '_' + getId( $rtag_re ) ).trigger( 'expand' ); // expand the tag; material list is added and refreshed within the tag
		}
		//updateRelatedEntitiesListview( listview, $currentRelationType, null, pageOffset, null, nextPageId );
		// SEARCH FILTER	
		// Set the list view filter callback function
		var startsWithCallback;
		if ( pageId == '#relatedentities' )
			startsWithCallback = startsWithRelatedEntities;
		else
			startsWithCallback = startsWithMyEntityRelationships;
		// Loop through tags list and set the list view filter callback function for all the list controls
		if ( hasTags ) {
			for ( var i = 0; i < tagList.length; i++ ) {
				var tagId = id + '_' + getId( tagList[i] ) + '_ul';
				// Set the list view filter callback function
				setListViewFilterCallback( tagId, startsWithCallback );
			}
		} else {
			setListViewFilterCallback( id + '_ul', startsWithCallback );
		}
		// SEARCH FILTER END
		//setListViewFilterCallback( id + '_ul', startsWithCallback );
	} else {
		//var noneMsg = $buttontext.no+' ' + getRelationshipName( $currentRelationType, true ) + '.';
		var noneMsg ='' ;
		if ($currentRelationType == 'csts')
		 noneMsg = $messagetext.nocustomers;
		else
		  noneMsg = $messagetext.novendors;
		if ( allowRelatedEntitiesAdd )
			noneMsg += $labeltext.clicktoaddnew + relationshipName + '.';
		content.empty().append( noneMsg );
		// Enhance the page
		page.page();
		page.trigger( 'create' );
	}
	if ( options ) {
		// Update options data url
		options.dataUrl = pageId + '?op=' + $currentOperation + '&ty=' + $currentRelationType + '&o=' + pageOffset;
		// Now call changePage() and tell it to switch to the page we just modified.
		$.mobile.changePage( page, options );
	} else
		$.mobile.changePage( page );
	header.find( '#relatedentitiesback .ui-btn-text').text($buttontext.back);
	header.find( '#relatedentitieslogout .ui-btn-text').text($buttontext.logout);
	header.find( '#myentityrelationshipsback .ui-btn-text').text($buttontext.back);
	header.find( '#myentityrelationshipssetup .ui-btn-text').text($buttontext.add);
}

// Update material list view
function updateRelatedEntitiesListview( listview, linkType, rtag, pageOffset, idsToAdd, pageId, linkedPageId ) {
	var relatedEntities = [];
	var allRelatedEntities = $entity[ linkType ];
	// Get the related entities to be added to the list view
	if ( !idsToAdd || idsToAdd == null ) { // get the subset of related entities for this page
		var size = 0;
		if ( rtag ) {
			if ( $entitiesData[ linkType ][ rtag ] )
				size = $entitiesData[ linkType ][ rtag ].length;
		} else
			size = allRelatedEntities.length;
		// Set the listview navigation bar
		var range = setListviewNavBar( listview, pageOffset, size, null, function() {
			updateRelatedEntitiesListview( listview, linkType, rtag, ( pageOffset - 1 ), idsToAdd, pageId, linkedPageId );
		}, function() {
			updateRelatedEntitiesListview( listview, linkType, rtag, ( pageOffset + 1 ), idsToAdd, pageId, linkedPageId );
		} );

		var start = range.start;
		var end = range.end;
		// Get the list of related entities for rendering 
		if ( rtag ) {
			var taggedEntities = $entitiesData[ linkType ][ rtag ];
			if ( taggedEntities ) {
				for ( var i = ( start - 1 ); i < end; i++ ) // get the subset of related entities for this page
					relatedEntities.push( taggedEntities[i] );
			}
		} else {
			for ( var i = ( start - 1 ); i < end; i++ ) // get the subset of related entities for this page
				relatedEntities.push( allRelatedEntities[i] );
		}
	} else { // Get the related entity info. for the given set of ids to be added to listview
		for ( var i = 0; i < allRelatedEntities.length; i++ ) {
			var relatedEntity = allRelatedEntities[i];
			if ( $.inArray( relatedEntity.kid, idsToAdd ) != -1 )
				relatedEntities.push( relatedEntity );
		}
	}


	// Render the list items
	var urlBase = '#' + linkedPageId + '?';
	if ( linkedPageId != 'materials' ) // add the relation type, if entityinfo page
		urlBase += 'ty=' + $currentRelationType + '&rel=true&'; // NOTE: rel (relations page) is required so that we don't show the next level of relationships (for the related entity)
	urlBase	+= 'bo=' + pageOffset +'&kid='+$entity.kid +'&lkid='; // back-offset, an offset to be given to the back URL only in materials page

	for ( var i = 0; i < relatedEntities.length; i++ ) {
		//var relatedEntity = relatedEntities[i];
		var lkid = relatedEntities[i].kid;
		var relatedEntity = getRelatedEntity( lkid, $currentRelationType );
		if (!relatedEntity)
			return;
		var id = pageId + '_' + relatedEntity.kid; /// 'relatedentities_' + relatedEntity.kid;
		var url = urlBase + relatedEntity.kid;
		var onclick = "";
		//HMA 454
		if (linkedPageId != 'materials'){
			var prms = getRelatedEntityPermissions(relatedEntity.kid, $currentRelationType,$entity.kid)
			if (prms == $VIEW_INVENTORY || prms == $MANAGE_ENTITY) {
				url = '#dummy?kid=' + relatedEntity.kid +'&ty=' + $currentRelationType;
				var backPageId = '#myentityrelationships?ty=' + $currentRelationType + '&o=' + pageOffset +  '&kid=' + $entity.kid ;
				onclick = 'doInitKiosk(\'' + relatedEntity.kid + '\',' + pageOffset + ',\''+ backPageId + '\',undefined)';
			}
		}
		var markup = '<li id="' + id + '"><a href="' + url + '"';
		if (onclick != '')
			markup += ' onclick="' + onclick + '"';
		markup += '>';
		markup += '<h3>' + relatedEntity.n + '</h3><p>' + ( relatedEntity.sa ? relatedEntity.sa + ', ' : '' ) + relatedEntity.cty + ( relatedEntity.pc ? ', ' + relatedEntity.pc : '' ) + ( relatedEntity.tlk ? ', ' + relatedEntity.tlk : '' ) + ( relatedEntity.dst ? ', ' + relatedEntity.dst : '' );
		if ( hasLocalModificationsForLinkedEntity($currentOperation,$entity,relatedEntity.kid)  && linkedPageId != 'entityinfo' && linkedPageId != 'menu')
			markup += '<br/><font style="color:red;font-size:9pt">'+$labeltext.selected+ '</font>';
		// Include available credit, if provided
		var availableCredit = getAvailableCredit( relatedEntity );
		if ( availableCredit && !$disableOrderPrice ) {
			markup += ' (<font style="color:green">' + $labeltext.availablecredit + '</font>: ';
			var currency = '';
			if ($entity.cu)
				currency = $entity.cu + ' ';
			if (availableCredit <= 0)
				markup += '<font style="color:red">' + currency + availableCredit + '</font>';
			else
				markup += '<font style="color:green">' + currency + availableCredit + '</font>';
			if (relatedEntity.crl && relatedEntity.crl != 0)
				markup += ', &nbsp;Limit: ' + relatedEntity.crl;
			markup += ')';

		}
			markup += '</p></a></li>';
		// Append item to listview
		listview.append( $( markup ) );
	} // end for
	// Refresh the view
	listview.listview('refresh');
}

// Add entity
function renderSetupEntity( pageId, params, options ) {
	// Get the page first
	var page = $( pageId );
	var type = params.ty;
	var action = 'add'; // add or edit
	if ( params.action )
		action = params.action;
	var isAdd = ( action == 'add' );
	var linkedKioskId = params.lkid; // sent if if linked entity is being edited
	var uid = params.uid; // sent if user is being edited
	var kid = params.kid;
	var newEntity = false;
	if (params.ne)
		newEntity = params.ne;
	var backPageOffset = 0;
	if ( params && params.bo )
		backPageOffset = params.bo;
	if (kid)
		$entity = getEntity($uid,kid);
	var typeName = $labeltext.entity;
	if ( type && ( type == 'csts' || type == 'vnds' ) )
		typeName = getRelationshipName( type, false );
	if (type && type == 'ents' && action == 'add') {
		//$parentEntityId = null;
		//$parentRelationType = null;
		clearParentEntity();
	}
	var editHeader = $buttontext.editentity;
	if ( uid ) {
		typeName = $labeltext.user;
		editHeader = $buttontext.edituser;
	}
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Update the header with the title and number of entities
	header.find( 'h3' ).empty().append( ( isAdd ? $buttontext.addentity  : editHeader )  );
	// Update the back button URL
	var prevPageId = getPrevPageId( options );
	var backUrl = '#entityinfo';
	if ( prevPageId != null && isAdd )
		backUrl = prevPageId;
	backUrl += '?ty=' + type;
	backUrl += ( linkedKioskId ? '&lkid=' + linkedKioskId : '' );
	backUrl += ( kid ? '&kid=' + kid : '' );
	backUrl += ( uid? '&uid=' + uid : '' );
	if (backPageOffset)
	  backUrl += '&bo='+backPageOffset;
	if (params.SendNow && params.ne)
	    backUrl += "&SendNow='Yes'";
	// Update the back URL with the entities page and its offset
	header.find( pageId + 'back' ).attr( 'href', backUrl );
	// Update content
	content.empty().append( $( getSetupEntityFormMarkup( action, type, typeName,kid, linkedKioskId,newEntity, uid, backPageOffset ) ) );
	page.page();
	page.trigger( 'create' );
	// Change to this page
	options.dataUrl = '#setupentity?ty=' + type;
	if ( linkedKioskId )
		options.dataUrl += '&lkid=' + linkedKioskId;
	if ( kid )
		options.dataUrl += '&kid=' + kid;
	if ( action )
		options.dataUrl += '&action=' + action;
	$.mobile.changePage( page, options );
	header.find( '#setupentityback .ui-btn-text').text($buttontext.back);
	if (params.ne)
	    $('#entitysavebutton').prop('disabled', false).removeClass('ui-disabled');
}

// Get Add Entity form markup
function getSetupEntityFormMarkup( action, type, typeName,kid, linkedKioskId,newEntity,uid, backPageOffset ) {
	var isEdit = ( action == 'edit' );
	var isObjectEntity = ( !uid );
	var entity;
	var user;
	// Get user data to enable defaults
	var userData = getUserData( $uid );
	// Get entity and/or user
	if ( isEdit ) {
		if ( type == 'ents' )
		{
			entity = $entity;
			if (kid)
 	  	 	 	entity = getEntity($uid,kid);
			else
				kid = $entity.kid;
		}
		else
		{
			if ($entity)
				entity = getRelatedEntity( linkedKioskId, type );
			else
				entity = getRelatedEntityForKioskId(linkedKioskId,type,kid);
		    if (!entity)
		    	entity = getEntity($uid,linkedKioskId);
		}
		// Get the user to be edited, if any
		if ( uid )
			user = getEntityUser( type, linkedKioskId, uid );
	}
   // Added for editing new entity
	if (newEntity)
	{
	  if (entity )
	  {
		if (type == 'ents')
		{
			 if (entity.us.length>1 );
		       user = entity.us[1];
		}
		else
		{
			if (entity.us.length>0 );
	      		user = entity.us[0];
		}
	  }
	}
	var readOnly = '';
	if (action == 'edit')
	  readOnly = ' readonly';
	var showEntityDetails = ( !isEdit || isObjectEntity );
	var showUserDetails = ( !isEdit || ( isEdit && !isObjectEntity  )); //||(isEdit && (type != 'ents') && newEntity) );
	var markup = '<div class="ui-body ui-body-d" style="display:' + ( showEntityDetails ? 'block' : 'none' ) + '">';
	//var entityUpdatePrompt = ( isEdit ? 'Edit ' + entity.n : 'Enter ' + ( type == 'ents' ? 'entity details' : '\'' + getRelationshipName( type, false ) + '\'' ) );
        var entityUpdatePrompt = '';
        if (isEdit)
            entityUpdatePrompt = $buttontext.edit+' ' + entity.n;
        else {//enter new 
         if (type == 'ents')
	      entityUpdatePrompt = $labeltext.enterentitydetails;
	    else if (type == 'csts')
	       entityUpdatePrompt = $labeltext.entercustomer;
	    else if (type == 'vnds')
		       entityUpdatePrompt = $labeltext.entervendor; 	  
	      	    
        }
	
	markup += '<strong>' + entityUpdatePrompt + '</strong><br/><font style="font-size:small;font-style:italic">'+$messagetext.fieldsmarkedmandatory+'</font>';
	markup += '<div data-role="fieldcontain"><label for="entityname">'+$labeltext.entityname+' *:</label><input type="text" id="entityname" '+readOnly+' value="' + ( entity ? entity.n : '' )+'" onblur="validateFormField( \'entityname\', \'text\', 1, 200, this.value, \'entitynamestatus\' )" onfocus="$(\'#entitynamestatus\').hide()" placeholder="'+$messagetext.enteruniqueentityname+'" /></div>';
	markup += '<div id="entitynamestatus" class="fieldstatus">'+$messagetext.pleaseentervalidentityname+'</div>';
	if (action =='edit')
	{
	   var newEntityName = '';
	   if (entity)
	   {
  		if (entity.nn)
		   newEntityName = entity.nn;
	   }	  
	 markup += '<div data-role="fieldcontain"><label for="newentityname">'+$labeltext.newentityname+':</label><input type="text" id="newentityname" value="' + ( entity ? newEntityName : '' ) + '" onblur="validateFormField( \'entityname\', \'text\', 1, 200, this.value, \'entitynamestatus\' )" onfocus="$(\'#entitynamestatus\').hide()" placeholder="'+$messagetext.enteruniqueentityname+'" /></div>';
	}
	// Entity's State
	var entityState = ( isEdit && entity && entity.ste ? entity.ste : ( !isEdit && userData.ste ? userData.ste : '' ) );
	markup += getStateDropdownDataEntryMarkup(userData.cn, entityState, 'state');
	markup += '<div id="statestatus" class="fieldstatus">'+$messagetext.pleaseentervalidstatename+'</div>';
	markup += '<div data-role="fieldcontain"><label for="city">'+$labeltext.cityvillage+' *:</label><input type="text" id="city" value="' + ( entity ? entity.cty : '' ) + '" onblur="validateFormField( \'city\', \'text\', 1, 200, this.value, \'citystatus\' )" onfocus="$(\'#citystatus\').hide()" /></div>';
	markup += '<div id="citystatus" class="fieldstatus">'+$messagetext.pleaseentervalidcityname+'</div>';
	markup += '<div data-role="fieldcontain"><label for="street">'+$labeltext.street+':</label><input type="text" id="street" value="' + ( entity && entity.sa ? entity.sa : '' ) + '" /></div>';
	markup += '<div data-role="fieldcontain"><label for="pincode">'+$labeltext.pincode+':</label><input type="text" id="pincode" value="' + ( entity && entity.pc ? entity.pc : '' ) + '" /></div>';
	markup += '<div data-role="fieldcontain"><label for="district">'+$labeltext.districtcounty+':</label><input type="text" id="district" value="' + ( entity && entity.dst ? entity.dst : '' ) + '" /><ul id="districtSuggestions" data-theme="d" data-role="listview" data-inset="true"></ul></div>';
	markup += '<div data-role="fieldcontain"><label for="taluk">'+$labeltext.taluk+':</label><input type="text" id="taluk" value="' + ( entity && entity.tlk ? entity.tlk : '' ) + '"/><ul id="talukSuggestions" data-theme="d" data-role="listview" data-inset="true"></ul></div>';
	// Allow selection of route segment, if any
	if ( $config.rtt )
		markup += getRouteTagSelectorMarkup( ( entity ? entity.rtt : undefined ) );
	// Get geo-codes, if any
	var geo = getGeo();
	var gotGeoNow = ( geo != null && !geo.err );
	var entityHasGeo = ( isEdit && entity && entity.lat && entity.lng );
	if ( entityHasGeo || gotGeoNow ) {
		markup += '<div class="ui-body ui-body-d ui-corner-all ui-shadow" style="padding:0.75em;">';
		var showUseGeoCodesPanel = 'block';
		if ( entityHasGeo ) {
			markup += '<img src="jquerymobile/icons/map_edit.png" valign="middle"/> '+$messagetext.entitymappedlocation+ '(' + entity.lat + ', ' + entity.lng + ') <a href="#" id="changegeo" data-role="button" data-mini="true" data-inline="true" data-theme="b" onclick="document.getElementById(\'usegeocodespanel\').style.display=\'block\'">'+$labeltext.change+'</a>';
			showUseGeoCodesPanel = 'none';
		} else if ( gotGeoNow ) {
			markup += '<img src="jquerymobile/icons/map_add.png" valign="middle"/> '+$messagetext.yourcurrentlocationis+ ' (' + geo.lat + ', ' + geo.lng + ').';
		}
		markup += '<div id="usegeocodespanel" data-role="fieldcontain" style="display:' + showUseGeoCodesPanel + '"><label for="usegeocodes"><font style="font-size:small">'+$messagetext.assigncurrentlocation+' <font color="blue">'+$messagetext.physicallyatentitynow+'</font>.</font></label><input type="checkbox" data-theme="d" id="usegeocodes"' + ( isEdit ? '' : 'checked' ) + ' /></div>';
		markup += '</div>';
	}
	markup += '</div>';
	// Manager details
	if ( !isEdit ) {
		if ( type == 'ents' ) {
			markup += '<br/><b>'+$messagetext.automaticallymanagerofentity+'</b>.'+$messagetext.wishtoaddoperator;
		} else { // if customer/vendor, allow making myself a manager of this entity
			markup += '<div data-role="fieldcontain"><label for="makememanager"><font style="font-size:small">'+$messagetext.makememanager+ '<font color="blue">'+' '+$messagetext.checkthisforagent+'</font></label><input type="checkbox" data-theme="d" id="makememanager" /></div>';
		}
	}
	// User/operator details
	var userUpdatePrompt = $labeltext.enteruserdetails;
	if ( isEdit && user )
		userUpdatePrompt =$buttontext.edit+ ' \'' + user.fn + ( user.ln ? ' ' + user.ln : '' ) + '\'';  
	markup += '<div id="operatordetails" class="ui-body ui-body-d" style="margin-bottom:2%;display:' + ( showUserDetails ? 'block' : 'none' ) + '">';
	markup += '<strong>' + userUpdatePrompt + '</strong><br/><font style="font-size:small;font-style:italic;">'+$messagetext.fieldsmarkedmandatory+'</font>';
	markup += '<div data-role="fieldcontain"><label for="userfirstname">'+$labeltext.firstname+' *:</label><input type="text" id="userfirstname" value="' + ( user ? user.fn : '' ) + '" onblur="validateFormField( \'userfirstname\', \'text\', 1, 20, this.value, \'userfirstnamestatus\' ); generateUserCredentials(this.value)" onfocus="$(\'#userfirstnamestatus\').hide()" placeholder="'+$messagetext.enternameforuser+'"/></div>';
	markup += '<div id="userfirstnamestatus" class="fieldstatus">'+$messagetext.pleaseentervalidusername+'</div>';
	markup += '<div data-role="fieldcontain"><label for="userlastname">'+$labeltext.lastname+':</label><input type="text" id="userlastname" value="' + ( user && user.ln ? user.ln : '' ) + '" /></div>';
	markup += getUserMobileDataEntryMarkup(user, userData.cn);
	// User's State (now mandatory)
	var userState = ( user && user.ste ? user.ste : ( entityState != '' ? entityState : ( userData.ste ? userData.ste : '' ) ) );
	markup += getStateDropdownDataEntryMarkup(userData.cn, userState, 'userstate');
	markup += '<div id="userstatestatus" class="fieldstatus">'+$messagetext.pleaseentervalidstatename+'</div>';
	if ( !isEdit )
		markup += $messagetext.useridpasswordgenerated+' <a href="#" onclick="$( \'#useridpasswordpanel\' ).show()">'+$labeltext.clickhere+'</a>.';
	markup += '<div id="useridpasswordpanel" style="display:none">';
	markup += '<div data-role="fieldcontain"><label for="entityuserid">'+$labeltext.userid+':</label><input type="text" id="entityuserid" value="' + ( user ? user.uid : '' ) + '" placeholder="'+$messagetext.fieldsmarkedmandatory+'" /></div>';
	markup += '<div id="entityuseridstatus" class="fieldstatus">'+$messagetext.pleaseentervaliduserid+'</div>';
	markup += '<div data-role="fieldcontain"><label for="entitypassword">'+$labeltext.password+':</label><input type="text" autocomplete="off" id="entitypassword" value="' + ( user ? user.p : '' ) + '" onblur="if ( $( \'#entityuserid\' ).val() != \'\' ) { validateFormField( \'entitypassword\', \'text\', 4, 20, this.value, \'entitypasswordstatus\' ); }" onfocus="$( \'#entitypasswordstatus\').hide();"/></div>';
	markup += '<div id="entitypasswordstatus" class="fieldstatus">'+$messagetext.pleaseentervalidpassword+'</div>';
	markup += '<div data-role="fieldcontain"><label for="entityconfirmpassword">'+$labeltext.confirmpassword+':</label><input type="text" autocomplete="off" value="' + ( user ? user.p : '' ) + '" id="entityconfirmpassword" onblur="var pwd = $( \'#entitypassword\' ).val(); if ( pwd != \'\' && pwd != this.value ) { $( \'#entityconfirmpasswordstatus\' ).show(); }" onfocus="$( \'#entityconfirmpasswordstatus\' ).hide()"/></div>';
	markup += '<div id="entityconfirmpasswordstatus" class="fieldstatus">'+$messagetext.passwordsdonotmatch+'</div>';
	markup += '</div>'; // end useridpasswordpanel
	if ( !isEdit )
		markup += '<div data-role="fieldcontain"><label for="sendpasswordtouser"><font style="font-size:small">'+$messagetext.sendlogininfosms+'</font></label><input type="checkbox" data-theme="d" id="sendpasswordtouser" /></div>';
	markup += '</div>'; // end operatordetails
	//var saveAction = action;
//	var onSubmitClick = 'if ( getValidatedEntityFormData(\'' + action + '\',\'' + type + '\',' + ( uid ? '\'' + uid + '\'' : 'undefined' ) + ') ) { sendWithLocation( function() { sendSetupEntity(\'' + action + '\',\'' + type + '\',\'' + typeName + '\',\'' + ( entity ? entity.n : '' )  + '\',\'' +( entity ? kid: '' ) + '\',' + ( linkedKioskId ? '\'' + linkedKioskId + '\'' : 'undefined' ) + ',' + ( uid ? '\'' + uid + '\'' : 'undefined' ) + '); } ); }';
	//var onSubmitClick = ' sendWithLocation( function() { sendSetupEntity(\'' + action + '\',\'' + type + '\',\'' + typeName   + '\',\'' +( entity ? kid: '' ) + '\',' + ( linkedKioskId ? '\'' + linkedKioskId + '\'' : 'undefined' ) + ',' + ( uid ? '\'' + uid + '\'' : 'undefined' ) + '); } ); ';
	
	var nextUrl = "'"+'#entityinfo?ty='+ type + '&lkid='+ kid;
	if ( type == 'ents' )
		nextUrl = "'#entityinfo?ty=ents";
	if ( uid )
		nextUrl += '&uid=' + uid;
	if (backPageOffset != null)
	    nextUrl += '&bo=' +backPageOffset;
	nextUrl += "'";
	var onSendClick =  'saveSetupEntity(\'' + action + '\',\'' + type + '\',\'' + typeName + '\',\'' +  ( entity ? entity.n : '' )  + '\',\'' +( entity ? kid: '' ) + '\',' + ( linkedKioskId ? '\'' + linkedKioskId + '\'' : 'undefined' ) + ',' + ( uid ? '\'' + uid + '\'' : 'undefined' )+','+nextUrl+','+newEntity+ ')';	

	markup += '<a data-role="button" id="entitysavebutton" data-theme="b" data-inline="true" onclick="' + onSendClick  + '">'+$buttontext.send+'</a>';	
	///markup += '<a data-role="button" data-rel="back" data-inline="true">Cancel</a>'; (removing Cancel, given data-rel=back does not work in APK)

	return markup;
}

function getUserMobileDataEntryMarkup(user, country){
	var markup = '';
	if (user && user.mob) {
		//value exists. split and display
		var parts = user.mob.split( ' ' );
		markup += '<div data-role="fieldcontain"><label for="mobilephonecontainer">'+$labeltext.phone+' *:</label><div id="mobilephonecontainer" style="text-align:right"><label>' + parts[0] + '</label><input type="hidden" id="countryCode" value="' + parts[0] + '" /> <input type="text" id="mobilephone" value="' + parts[1] + '" placeholder="9834568420" onblur="validateMobilePhoneOnBlur( this.value);" onfocus="$(\'#mobilephonestatus\').hide()" placeholder="'+$messagetext.enteramobilephonenumber+'" /></div></div>';				
		
		markup += '<div id="mobilephonestatus" class="fieldstatus">'+$messagetext.pleaseentervalidphone+'</div>';
	} else {
		var locationMetadata = getObject(country);
		var countryPhoneCode = null;
		if (locationMetadata && locationMetadata[country] && locationMetadata[country].countryPhoneCode){
			countryPhoneCode = locationMetadata[country].countryPhoneCode;
			markup += '<div data-role="fieldcontain"><label for="mobilephonecontainer">'+$labeltext.phone+' *:</label><div id="mobilephonecontainer" style="text-align:right"><label>' + countryPhoneCode + '</label><input type="hidden" id="countryCode" value="' + countryPhoneCode + '" /> <input type="text" id="mobilephone" value="" placeholder="e.g. 9834568420" onblur="validateMobilePhoneOnBlur( this.value);" onfocus="$(\'#mobilephonestatus\').hide()" placeholder="'+$messagetext.enteramobilephonenumber+'" /></div></div>';				
			markup += '<div id="mobilephonestatus" class="fieldstatus">'+$messagetext.pleaseentervalidphone+'</div>';
		} else {
			markup += '<div data-role="fieldcontain"><label for="mobilephonecontainer">'+$labeltext.phone+' *:</label><div id="mobilephonecontainer" style="text-align:right"><input type="text" size="3" maxlength="6" style="width:50px" id="countryCode" placeholder="91" onblur="validateCountryCodeOnBlur( this.value);" onfocus="$(\'#mobilephonestatus\').hide()"/> <input type="text" id="mobilephone" value="" placeholder="9834568420" onblur="validateMobilePhoneOnBlur( this.value);" onfocus="$(\'#mobilephonestatus\').hide()" placeholder="'+$messagetext.enteramobilephonenumber+'" /></div></div>';
			markup += '<div id="mobilephonestatus" class="fieldstatus">'+$messagetext.pleaseentervalidphone+'</div>';
		}
	}
	return markup;
}
function getStateDropdownDataEntryMarkup(country, state, fieldName){
    var locationMetadata = getObject(country);
	var markup = '';
	if ( locationMetadata && locationMetadata[country].states) {
		var states = Object.keys(locationMetadata[country].states).sort();
		markup += '<div data-role="fieldcontain"><label for="'+ fieldName + '">'+$labeltext.state+' *:</label><select id="' + fieldName + '" data-mini="false">';
		var selectedOption = null;
		if ( state )
			selectedOption = state;
		for ( var i = 0; i < states.length; i++ ) {
			var sel = '';
			if ( selectedOption != null && selectedOption == states[i] )
				sel = ' selected';
			markup += '<option value="' + states[i] + '"' + sel + '>' + states[i] + '</option>';
		}
		markup += '</select></div>';		
	} else {
		markup += '<div data-role="fieldcontain"><label for="' + fieldName + '">'+$labeltext.phone+' *:</label><input type="text" value="' + state + '" id="' + fieldName + '" onblur="validateFormField( ' + fieldName + ', \'text\', 1, 200, this.value, \'statestatus\' )" onfocus="$(\'#statestatus\').hide()" /></div>';		
	}	
	return markup;
}
// Get the route tag selector markup
function getRouteTagSelectorMarkup( rtag ) {
	var rtags = getRouteTags();
	// Show/hide this field based on route tag edit perission; the field should be there, given it needs to be sent via edit
	var showField = ( $config.arte ? 'block' : 'none' ); // allow-route-tag-editing?
	var markup = '<div class="ui-body-b ui-corner-all ui-shadow" style="padding:0.75em;margin-bottom:1%;display:' + showField + '">';
	markup += $messagetext.selectroutetagtext;
	markup += '<select name="routetag" id="routetag" data-theme="c">';
	markup += '<option value="">-'+$labeltext.selectroutetag+'-</option>';
	for ( var i = 0; i < rtags.length; i++ )
		markup += '<option value="' + rtags[i] + '"' + ( rtag && rtag == rtags[i] ? 'selected' : '' ) + '>' + rtags[i] + '</option>';
	markup += '</select>';
	markup += '</div>';
	return markup;
}

// Render the menu based on the configuration
function renderMenu( pageId, params, prevPageId, options ) {
	// Get the backpage offset, if any (present in case the entities page is present)
	var backPageOffset = 0
	if ( params && params.bo )
		backPageOffset = params.bo;

	if ( isManagedEntity ($entity.kid) == true && $hasMultipleEntities) {
		prevPageId = '#entities'; //clicked on managed entity go back to main menu
	}
	var parentEntity = getParentEntity();
	if (parentEntity){
		if ( isManagedEntity ($entity.kid))
			clearParentEntity();
		else if (parentEntity.kid == $entity.kid) {
			spliceParentEntity();
		}
		parentEntity = getParentEntity();
	}
	else
		$currentRelationType = '';
	//Reset Order tag search
	resetOrderSearch();
	// Init. page
	var page = $( pageId );
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Update header nav. buttons
	if ( prevPageId != null && prevPageId == '#entities') {
		showElement( header.find( pageId + 'back' ), true );
		var backUrl = prevPageId + '?o=' + backPageOffset;
		// Update the back URL with the entities page and its offset
		header.find( '#menuback' ).attr( 'href', backUrl );
		// Hide logout button here, given it is shown in the entity listing page
		showElement( header.find( '#menulogout' ), false );
	}
	else if ( parentEntity ) {
		showElement( header.find( pageId + 'back' ), true );
		var backUrl = '#myentityrelationships?ty=' + parentEntity.type  +  '&kid=' + parentEntity.kid + '&o=' + backPageOffset ;
		// Update the back URL with the entities page and its offset
		header.find( '#menuback' ).attr( 'href', backUrl );
		// Given its is a related entity, do not show the logout on the menu page
		showElement( header.find( '#menulogout' ), false );
	}
	else {
		if ($hasMultipleEntities){
			showElement( header.find( pageId + 'back' ), true );
			var backUrl = '#entities?o=' + backPageOffset;
			// Update the back URL with the entities page and its offset
			header.find( '#menuback' ).attr( 'href', backUrl );
			// Hide logout button here, given it is shown in the entity listing page
			showElement( header.find( '#menulogout' ), false );
		} else { // hide back button
			showElement( header.find( pageId + 'back' ), false );
			// Show the logout button, just in case it was hidden earlier
			showElement( header.find( '#menulogout'), true );
		}
	}
	
	// Get the list of inventory and order operations to be shown
	var validOps = getValidOperations( $config );
	//Get valid operations based on user permissions
	validOps = getUserValidOperations($uid,validOps);
	//HMA-454 add valid ops for related entities
	var parentEntity = getParentEntity();
	if (parentEntity) {
		validOps = getRelatedEntityValidOperations($entity.kid, parentEntity, validOps);
	}
	var size = getObjectSize( validOps );
	if ( size == 0 ) {
		showDialog( null, $messagetext.nooperationsenabled,  getCurrentUrl() );
		return;
	}
	// Set title bar
	setTitleBar( header, $entity.n, false );
	// Form the markup for the menu page
	var markup = '';
	// Event notification panel
	initEventModel($uid,$entity.kid);
	markup = getNotificationPanelMarkup($entity.kid);

	// Get the primary enity markup, if any
	var userData = getUserData( $uid );
	var primaryEntitySwitcherMarkup = ( $hasMultipleEntities && userData.kid ? getPrimaryEntitySwitcherMarkup( true, userData.kid ) : '' );
	markup += primaryEntitySwitcherMarkup;
	markup += '<div data-role="collapsible-set">';
	// Check configured operationsf
	var hasInvOps = hasInventoryOperations( validOps );
	var hasOrderOps = hasOrderOperations( validOps );
	// Check the operation type to determine whether to collapse or open the set
	var invDataCollapsed = 'true', orderDataCollapsed = 'true';
	var profileCollapsed = ( !params || !params.profile );
	if ( profileCollapsed ) {
		if ( $currentOperation ) { // check the type of op. to determine section to keep open (esp. on a back)
			if ( isOperationOrder( $currentOperation ) )
				orderDataCollapsed = 'false';
			else
				invDataCollapsed = 'false';
		} else {
			if ( hasInvOps && !hasOrderOps )
				invDataCollapsed = 'false';
			else if ( hasOrderOps && !hasInvOps )
				orderDataCollapsed = 'false';
		}
	}
	// Render the ops.
	if (  hasInvOps ) { // Add enabled inventory ops.
		markup += '<div data-role="collapsible" data-theme="a" data-collapsed="' + invDataCollapsed + '">';
		markup += '<h3>'+$labeltext.inventory+ '</h3>';
		markup += '<ul data-role="listview">';
		if ( validOps.vs )
			markup += '<li><a href="#materials?op=vs"><img src="jquerymobile/icons/viewstock.png" alt="'+$labeltext.viewstock+'" class="ui-li-icon" />'+$labeltext.viewstock+'</a></li>';
		if ( validOps.ei ) {
			var url = '#materials?op=ei';
			var relationType = getRelationshipTypeForOperation( 'ei' );
			if ( relationType != null )
				url = '#relatedentities?op=ei&ty=' + relationType;
			markup += '<li><a href="' + url + '"><img src="jquerymobile/icons/issues.png" alt="'+$labeltext.enterissues+ '" class="ui-li-icon" />'+$labeltext.enterissues+'<img src="jquerymobile/icons/redflag.png" title="modified" align="right" alt=" (modified)" id="ei_modified" style="display:none" /></a></li>';
		}
		if ( validOps.er ) {
			var url = '#materials?op=er';
			var relationType = getRelationshipTypeForOperation( 'er' ); 
			if ( relationType != null )
				url = '#relatedentities?op=er&ty=' + relationType;
			markup += '<li><a href="' + url + '"><img src="jquerymobile/icons/receipts.png" alt="'+$labeltext.enterreceipts+ '"class="ui-li-icon" />'+$labeltext.enterreceipts+'<img src="jquerymobile/icons/redflag.png" title="modified" align="right" alt=" (modified)" id="er_modified" style="display:none" /></a></li>';
		}
		if ( validOps.es )
			markup += '<li><a href="#materials?op=es"><img src="jquerymobile/icons/stockcounts.png" alt="'+$labeltext.enterphysicalstock+ '" class="ui-li-icon" />'+$labeltext.enterphysicalstock+'<img src="jquerymobile/icons/redflag.png" title="modified" align="right" alt=" (modified)" id="es_modified" style="display:none" /></a></li>';
		if ( validOps.ew )
			markup += '<li><a href="#materials?op=ew"><img src="jquerymobile/icons/wastage.png" alt="'+$labeltext.enterdiscards+ '" class="ui-li-icon" />'+$labeltext.enterdiscards+'<img src="jquerymobile/icons/redflag.png" title="modified" align="right" alt=" (modified)" id="ew_modified" style="display:none" /></a></li>';
		// Transfer stock
		if (validOps.ts){
			if (($entity.csts && $entity.csts.length > 0) || ($entity.vnds && $entity.vnds.length > 0)){
				markup += '<li><a href="#transfer"><img src="jquerymobile/icons/transfer.png" alt="'+$labeltext.transferstock+ '" class="ui-li-icon" />'+$labeltext.transferstock+'<img src="jquerymobile/icons/redflag.png" title="modified" align="right" alt=" (modified)" id="ts_modified" style="display:none" /></a></li>';											
			}
		}
		// Export
		if (validOps.xi){
		  markup += '<li><a href="#export?ty=inventory"><img src="jquerymobile/icons/export.png" alt="'+$labeltext.exportinventory+ '" class="ui-li-icon" />'+$labeltext.exportinventory+'</a></li>';
		}
		// Transactions
		if (validOps.vt){
	   	  markup += '<li><a href="#transactions?op=vt""><img src="jquerymobile/icons/transactions.png" alt="'+$labeltext.transactionhistory+ '" class="ui-li-icon" />'+$labeltext.transactionhistory+'</a></li>';
		}
		markup += '</ul></div>';
	}
	if ( hasOrderOps ) { // Add enabled order ops.
		markup += '<div data-role="collapsible" data-theme="a" data-collapsed="' + orderDataCollapsed + '">';
	    markup += '<h3>'+$labeltext.orders+'</h3>';
	    markup += '<ul data-role="listview">';
	    if ( validOps.vo )
	   		markup += '<li><a href="#orders?op=vo"><img src="jquerymobile/icons/orders.png" alt="'+$labeltext.vieworders+ '" class="ui-li-icon" />'+$labeltext.vieworders+'</a></li>';
	    if ( validOps.no ) {
	   		markup += '<li><a href="#ordertypeselection"><img src="jquerymobile/icons/neworder.png" alt="'+$labeltext.neworder+ '"class="ui-li-icon" />'+$labeltext.neworder+'<img src="jquerymobile/icons/redflag.png" title="modified" align="right" alt=" (modified)" id="no_modified" style="display:none" /></a></li>';
	    }
		// Export
		markup += '<li><a href="#export?ty=orders"><img src="jquerymobile/icons/export.png" alt="'+$labeltext.exportorders+ '" class="ui-li-icon" />'+$labeltext.exportorders+'</a></li>';
		if (validOps.tr){
			var labelTransfers = $labeltext.transfers;
			if (isReleases($config)){
					labelTransfers = $labeltext.releases;
			}
			markup += '<li><a href="#orders?op=vo&ty=trf"><img src="jquerymobile/icons/orders.png" alt="' + labelTransfers + '" class="ui-li-icon" />'+ labelTransfers +'</a></li>';
		}
		markup += '</ul></div>';
	}
	// Add profile page
	if (validOps.vp) {
		markup += '<div data-role="collapsible" data-theme="a" data-collapsed="' + ( profileCollapsed ? 'true' : 'false' ) + '">';
		markup += '<h3>' + $labeltext.profile + '</h3>';
		markup += '<ul data-role="listview">';
		markup += '<li><a href="#entityinfo?ty=ents"><img src="jquerymobile/icons/profile.png" alt="' + $labeltext.profile + '" class="ui-li-icon" />' + $labeltext.entitydetails + '</a></li>';
		//Customers
		var hasCustomers = ($entity.csts && $entity.csts.length > 0);
		if ((hasCustomers || canAddEntity('csts')) )
			markup += '<li><a href="#myentityrelationships?ty=csts"><img src="jquerymobile/icons/customers.png" alt="' + $labeltext.customers + '"  class="ui-li-icon" />' + $labeltext.customers + '</a></li>';
		//Vendors
		var hasVendors = ($entity.vnds && $entity.vnds.length > 0);
		if ((hasVendors || canAddEntity('vnds')) )
			markup += '<li><a href="#myentityrelationships?ty=vnds"><img src="jquerymobile/icons/vendors.png" alt="' + $labeltext.vendors + '"  class="ui-li-icon" />' + $labeltext.vendors + '</a></li>';
		markup += '</ul></div>';
	}
	markup += '</div>';
	markup += '<p><br/><br/></p>'; // give some space before support
	 //Add support information in the footer
	var footerMarkup = '<span id="needhelptext">Need help?</span>';
	footerMarkup += '<p><a id ="calltext" alt="call" onclick="callSupportPhone()" style="display:inline;margin-right:20px;"><img src="jquerymobile/icons/phone.png"/></a>';
	footerMarkup += '<a  id ="emailtext" alt="email" style="display:inline;" onclick="sendSupportEmail()"><img src="jquerymobile/icons/mail.png"/></a><br/>';
	footerMarkup += '<span id="contactname" style="font-weight:normal;font-size:small"></span></p>';
	footer.empty().append( footerMarkup );
	//feedback
	var feedbackMarkup;
	feedbackMarkup = '<div id="feedbackdiv">';
	feedbackMarkup += '</br>';
	feedbackMarkup += '<a  id="feedbacklink" alt="feedback"  href="#feedbackpage"style="margin-left:20px;display:inline"><img src="jquerymobile/icons/comments.png" /></a>';
	//feedbackMarkup += '</br>';
	feedbackMarkup += '<a  id="feedbacktext" alt="feedback"  href="#feedbackpage"style="margin-left:3px;font-size:8pt;display:inline;text-decoration:none">Feedback</a>';
	feedbackMarkup += '</div>';
	// Update content and enhance page
	content.html( markup );
	// Update the dirty flags
	updateMenuDirtyFlags();
	page.find( '#feedbackdiv').remove();
	page.append(feedbackMarkup);
	// Enhance page
	page.page();
	page.trigger('create'); // also enhances the listview we just injected
	// Change to page
	if ( options )
		$.mobile.changePage( page, options );
	else
		$.mobile.changePage( page );
	header.find( '#menuback .ui-btn-text').text($buttontext.back);
	header.find( '#menulogout .ui-btn-text').text($buttontext.logout);
	header.find( '#operationsheader').text($labeltext.operationstext);
    page.find( '#feedbacktext').text($labeltext.feedback);
	
}

// Update dirty flags on menu
function updateMenuDirtyFlags() {
	// Mark operations as dirty, if applicable
	for ( var op in $operations ) {
		if ( !$operations.hasOwnProperty( op ) )
			continue;
		var attrVal = '';
		if ( hasLocalModifications( op, $entity, false ) )
			attrVal = 'display:block';
		else
			attrVal = 'display:none';
		var dirtyFlag = $('#menu').find( '#' + op + '_modified' );
		if ( dirtyFlag )
			dirtyFlag.attr( 'style', attrVal );
	}
}

// Is material under tag dirty?
function isTagDirty ( tagName, op ) {
	// Mark operations as dirty, if applicable
	var dirtyFlag = false;
	if ( hasLocalModifications( op, $entity, false ) ) {
		var tagList = $materialData.tagmap;
		if (tagList) {
			var materialIds = $materialData.tagmap[tagName];
			if (materialIds) {
				for (var i = 0; i < materialIds.length; i++) {
					op = $currentOperation;
					if (haslocalModificationsForMid( op, $entity, materialIds[i], false)) {
						dirtyFlag = true;
						return dirtyFlag;
					}

				}
			}
		}
	}

	return dirtyFlag

}

function exportData(type){
	var page = $('#export');
	// Get url params for export call
	var params = '&kid=' + $entity.kid + '&ty=' + type; // earlier: also including '&uid=' + $uid + '&p=' + $pwd; now BasicAuth will take care
	// Get user's email
	var email = undefined;
	var userData = getUserData( $uid );
	// Get email from field, if any
	var emailElement = page.find('#exportToEmail');
	if ( emailElement ) {
		email = emailElement.val();
		if ( email == '' ) {
			if (type == 'inventory')
			 showDialog( $messagetext.invalidemail, $messagetext.enteremailaddresstoexportinventory+'.', '#export?ty=' + type );
			else
				showDialog( $messagetext.invalidemail, $messagetext.enteremailaddresstoexportorder+'.', '#export?ty=' + type );
			return;
		} else {
			// Update params
			params += '&email=' + email;
			// Store this email into user's local data
			userData.eml = email;
			storeUserData( $uid, userData, false, false );
		}
	}
	var otype = "prc";
	if ( type == "orders") {
		otype = page.find('#exportordertype').val();
	}
	//make  call to the api
	var url = '';
	var typetext = '';
	var changeUrl = '#menu';
	if (type == 'orders') {
		url = $host + '/api/o?uid=' + $uid + '&a=exp' + params;
		typetext = $labeltext.orders_lower;
		url += '&oty='+otype;
	} else if (type == 'inventory') {
		url = $host + '/api/i?uid=' + $uid + '&a=exp' + params;
		typetext = $labeltext.inventory_lower;
	} else if (type == 'transactions') {
		var size = $transExportLimit;
		//params += '&s=' + size;
		url = $host + '/api/i?uid=' + $uid + '&a=exp' + params;
		typetext = $labeltext.transactions_lower;
		var header = page.children( ':jqmData(role=header)' );
		changeUrl = header.find( '#exportback' ).attr( 'href');
	}
	// Show the page loading message
	var loadingMsg = $labeltext.submittingexportrequest;
	// For server request, with sync.
	var request = {
		url: url,
		dataType: 'json',
		cache: false,
		success: function(o) {
			if ( o && o.st == '0' ) {
				showDialog($messagetext.success, $messagetext.exportinitiated + " - " + typetext + '. ' + $messagetext.receiveemailat + ' ' + email + '.', changeUrl );
			} else {
				showDialog( null, o.ms,  getCurrentUrl() );
			}
		},
		sync: {
			op: 'xi', 	// export inventory/orders
			uid: $uid,
			kid: $entity.kid,
			callback: function() {
				$.mobile.changePage( changeUrl );
			},
			syncOnNetworkError: true
		}
	};
	// Form log inputs
	var logData = {
		functionName: 'exportData',
		message: type
	};
	// Request server
	requestServer( request, loadingMsg, logData, true, true );
}

function renderExport( pageId, params, options ) {
	var type = params.ty;
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Update header
	var headerTxt = '';
	if (type == 'inventory')
		headerTxt = $labeltext.exportinventory;
	else if (type == 'orders')
		headerTxt = $labeltext.exportorders;
	else
		headerTxt = $labeltext.exporttransactions;
	// Update header of page
	header.find('h3').empty().append( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update header title
	setTitleBar( header, $entity.n, false );
	header.find( pageId + 'back' ).attr( 'href', '#menu' );
	// Update content
	var userData = getUserData( $uid );
	var markup = '';
	if (type == 'inventory')
		markup = $messagetext.enteremailaddresstoexportinventory + ':';
	else if ( type == 'orders')
		markup = $messagetext.enteremailaddresstoexportorder+ ':';
	else if ( type == 'transactions' ){
		var prevPageId = getPrevPageId( options );
		var backPageOffset = 0;
		if ( params && params.bo )
			backPageOffset = params.bo;
		if ( prevPageId != null ) {
			if ( prevPageId == '#transactions' ) {
				var backUrl = prevPageId + '?o=' + backPageOffset;
				header.find( pageId + 'back' ).attr( 'href', backUrl );
				$mtag = null;
			}
		}
		markup = $messagetext.enteremailaddresstoexporttransactions+ ':';
	}

	var value = "";
	if (userData.eml)
		value = userData.eml;
	markup += '<input type="email" name="email" id="exportToEmail" value="' + value + '"'+ 'placeholder="'+$messagetext.enteremailaddress+'" />';
	markup += '<i>' + $messagetext.verifybeforeexporting + '</i>';
	if (type == "orders"){
		markup += '<p><label for="exportordertype">'+$messagetext.selecttypeoforder+'</label>';
		markup += '<select id="exportordertype"> <option value="prc" selected>' + $labeltext.purchaseorder + '</option>';
		markup += '<option value="sle">' + $labeltext.salesorder + '</option></select></p>';
	}
	var onClick = 'exportData(\'' + type + '\')';
	markup += '<a href="#" data-role="button" data-mini="false" onclick="' + onClick + '">'+$buttontext.exporttext+'</a>';

	content.html(markup);
	page.page();
	page.trigger('create');
	$.mobile.changePage( page, options );
	header.find( '#exportback .ui-btn-text').text($buttontext.back);

}

function renderTransfer( pageId, params, options ) {
	var hasCustomers = ($entity.csts && $entity.csts.length > 0);
	var hasVendors = ($entity.vnds && $entity.vnds.length > 0);
	// If either customers or vendors, then directly go to the related entities list
	if ( !hasCustomers || !hasVendors ) {
		var prms = {};
		prms.op = 'ts';
		prms.ty = ( hasVendors ? 'vnds' : 'csts' );
		$currentOperation = prms.op;
		renderRelatedEntities( '#relatedentities', prms, options );
		return;
	}
	if (unsentInventoryOperations($entity,'ts')) 
		return;
	// Show the list of entity types to select
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var markup = '';
	// Update header text
	var headerTxt = getOperationName( 'ts' );
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to correct update browser-displayed title case of HTML5 views in browser)
	page.attr( 'data-title', headerTxt );
	var infoText = '';	
	if ( hasCustomers && hasVendors ) {
		var listItemText = $labeltext.customers+' (' + $entity.csts.length + ')';
		markup = '<ul data-role="listview">';
		markup += '<li><a href="#relatedentities?op=ts&ty=csts">' + listItemText + '</a></li>';
		listItemText = $labeltext.vendors+' (' + $entity.vnds.length + ')';
		markup += '<li><a href="#relatedentities?op=ts&ty=vnds">' + listItemText + '</a></li>';
		markup += '</ul>';
		infoText = '<div>'+$messagetext.selecttypeofentity+':</div><p/>';
	} else {
		infoText = '<div>'+$messagetext.nocustomerorvendor+'</div>';
	}
	content.empty();
	content.append( $( infoText ) );
	if ( markup != '' )
		content.append(markup);
	page.page();
	page.trigger('create');
	$.mobile.changePage( page, options );
	header.find( '#transferback .ui-btn-text').text($buttontext.back);
}

//Check for unsent Operations
function getLocalModificationsForOperation (entity,currentOperation) {
	var localData = entity.localdata;
	if ( !localData )
		return null;
	if ( localData[ currentOperation ])
	  if (localData[currentOperation].materials)
		 if( Object.keys(entity.localdata[ currentOperation ].materials).length > 0)		
			return true;
	return false;
}

function unsentInventoryOperations(entity,currentOperation) {	
	//Check for Receipts
	if (!currentOperation)
		return false;
	if (currentOperation != 'er' && currentOperation != 'vs' && currentOperation != 'vo' && currentOperation != 'no') {		
		if (getLocalModificationsForOperation(entity,'er')){
			showDialog($messagetext.warning,$messagetext.clearenterreceipts, getCurrentUrl());
		return true;
		}
	}
	//Check for issues
	if (currentOperation != 'ei' && currentOperation != 'vs' && currentOperation != 'vo' && currentOperation != 'no' ) {		
		if (getLocalModificationsForOperation(entity,'ei')){
			showDialog($messagetext.warning,$messagetext.clearenterissues, getCurrentUrl());
			return true;
		}
	}

	//Check for Stock Counts
	if (currentOperation != 'es' && currentOperation != 'vs' && currentOperation != 'vo' && currentOperation != 'no') {		
		if (getLocalModificationsForOperation(entity,'es')){
			showDialog($messagetext.warning,$messagetext.clearenterphysicalstock, getCurrentUrl());
			return true;
		}
	}
	//Check for Discards
	if (currentOperation != 'ew' && currentOperation != 'vs' && currentOperation != 'vo' && currentOperation != 'no') {		
		if (getLocalModificationsForOperation(entity,'ew')){
			showDialog($messagetext.warning,$messagetext.clearenterdiscards, getCurrentUrl());
			return true;
		}
	}
	//Check for Transfer stock
	if (currentOperation != 'ts' && currentOperation != 'vs' && currentOperation != 'vo' && currentOperation != 'no') {		
		if (getLocalModificationsForOperation(entity,'ts')){
			showDialog($messagetext.warning,$messagetext.clearentertransferstock, getCurrentUrl());
			return true;
		}
	}
	return false;
}



function unsentOrders( entity,currentOperation) {
	//Check for unsent purchase orders
	if (currentOperation == 'no') {
		var linkedEntity;
		if (getLocalModificationsForOperation(entity,currentOperation)) {
			if (linkedEntity = getSelectedLinkedEntity(currentOperation, entity)) {
				if (linkedEntity) {
					if ($newOrderType == 'sle' && linkedEntity.type == 'vnds') {
						showDialog($messagetext.warning, $messagetext.unsentpurchaseorder, getCurrentUrl());
						return true;
					}
					else if ($newOrderType == 'prc' && linkedEntity.type == 'csts') {
						showDialog($messagetext.warning, $messagetext.unsentsalesorder, getCurrentUrl());
						return true;
					}
				}
				else {
					if ($newOrderType == 'sle') {
						showDialog($messagetext.warning, $messagetext.unsentpurchaseorder, getCurrentUrl());
						return true;
					}
				}
			}
		}
	}
	return false;
}


function renderOrderTypeSelection(pageId, params, options){
	$newOrderType = 'prc';
	var hasCustomers = ($entity.csts && $entity.csts.length > 0);
	var hasVendors = ($entity.vnds && $entity.vnds.length > 0);
	// If has customers and no vendors then otype should be sales order
	if (hasCustomers && !hasVendors){
		$newOrderType = 'sle';		
	}
	var prms = {};
	prms.otype = $newOrderType;
	prms.op = 'no';
	$currentOperation = prms.op;
	
	// If no customers or vendors are present, then render materials
	if ( !hasCustomers && !hasVendors ) {
		renderMaterials( '#materials', prms, options );
		return;
	}
	// If at least one of customer/vendors is present, then show the list of customers/vendors
	if ( !hasCustomers || !hasVendors ) {
		prms.ty = ( hasVendors ? 'vnds' : 'csts' );
		renderRelatedEntities( '#relatedentities', prms, options );
		return;
	}
	// Show the list of entity types to select
	var page = $( pageId );
	// Get the page header and content
	var content = page.children( ':jqmData(role=content)' );
	var markup = '';
	var infoText = '';	
	if ( hasCustomers && hasVendors ) { // show a choice between purchase/sales order to be taken anew
		var listItemText = $labeltext.salesorder+' (' + $entity.csts.length + ')';
		markup = '<ul data-role="listview">';
		markup += '<li><a href="#relatedentities?op=no&ty=csts&otype=sle">' + listItemText + '</a></li>';
		listItemText = $labeltext.purchaseorder+' (' + $entity.vnds.length + ')';
		markup += '<li><a href="#relatedentities?op=no&ty=vnds&otype=prc">' + listItemText + '</a></li>';
		markup += '</ul>';
		infoText = '<div>'+$messagetext.selecttypeoforder+':</div><p/>';
	} else {
		infoText = '<div>'+$messagetext.nocustomerorvendortoorder+'</div>';
	}
	// Update the header with the title
	var header = page.children( ':jqmData(role=header)' );
	header.find( 'h3' ).empty().append( $labeltext.neworder );
	page.attr( 'data-title', $labeltext.neworder );
	content.empty();
	content.append( $( infoText ) );
	if ( markup != '' )
		content.append(markup);
	page.page();
	page.trigger('create');
	$.mobile.changePage( page, options );	
}

// Render a list of materials
function renderMaterials( pageId, params, options ) {
	if ( !$entity || $entity == null )
		return;
	if (unsentInventoryOperations($entity,$currentOperation)) 
		return;
	var pageOffset = 0;
	if ( params && params.o )
		pageOffset = parseInt( params.o ); // NOTE: has to be int. given it is used for range calculations
	var backPageOffset = 0; // offset for the back page (esp. if it is entities or related entities)
	if ( params && params.bo )
		backPageOffset = params.bo; // NOTE: can be in string form, given it is used as back URL param only
	// Get the materials
	var materials = $entity.mt;
	var size = ( materials ? materials.length : 0 );
	// Get tags, if any
	var tags = null;
	if ( $materialData.tags ) {
		if (!isOperationOrder( $currentOperation ))
		 tags = getValidTagsByOperation($materialData.tags); //tags by operation overides inventory tags
		var hasTags = ( tags != null && tags.length > 0 );
		 if (!getInventoryTagsToHide($currentOperation,$config)){
			tags = getValidTags($materialData.tags);
			hasTags = ( tags != null && tags.length > 0 );
		}
	}
	if (isOperationOrder( $currentOperation ) && $newOrderType == 'sle') {
		var lkid;
		if ( params && params.lkid) {
			lkid = params.lkid;
			if ( lkid && getRelatedEntityPermissions(lkid, 'csts', $entity.kid) != $NO_PERMISSIONS )
				getInventoryForRelatedEntity(lkid,'csts', null);
		}
	}
	var numItemsInView = 0;
	if ( hasTags && ( tags.length < $materialData.tags.length ) ) // i.e. some tags are hidden from this operation's view
		size = getNumMaterialsInValidTags( tags );
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Update the header with the title and number of entities
	var headerTxt =  getOperationName( $currentOperation ) +  ' (' + size + ')';
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to correct update browser-displayed title case of HTML5 views in browser)
	page.attr( 'data-title', headerTxt );
	// Update home button
	if ( $hasMultipleEntities )
		header.find( '#materialshome' ).attr( 'href', '#entities' );
	else
		header.find( '#materialshome' ).attr( 'href', '#menu' );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update header nav. button
	var prevPageId = getPrevPageId( options ); 
	if ( prevPageId != null ) {
		if ( prevPageId == '#relatedentities' || prevPageId == '#menu' ) {
			var backUrl = prevPageId + '?o=' + backPageOffset;
			header.find( pageId + 'back' ).attr( 'href', backUrl );
			$mtag = null;
		}
	}
	
	var hasMaterials = ( materials && materials.length > 0 );
	// Check if materials are present
	var allTagsNotVisible = false;
	if ($materialData.tags)
	 allTagsNotVisible =  (tags.length ==0)  && ($materialData.tags.length> 0);
	if ( !hasMaterials || allTagsNotVisible) {
		content.empty().append( $( '<p>'+$messagetext.nomaterials+'.</p>' ) );
		hasMaterials = false;
	} else {
		// Update footer and content, first with tags (if any), and then with material list
		var footer = page.children( ':jqmData(role=footer)' );
		var footerMarkup = '';
		// Check if local modifications exist; if so, show the Send/Clear-All buttons
		  if ( hasLocalModifications( $currentOperation, $entity, true ) || ( $currentOperation == 'no' && allowEmptyOrders() ) ) {
			var clearLocalModificationsArgs = '\'' + $uid + '\',\'' + $currentOperation + '\',null,true,false';
			var thisUrl = '#materials?op=' + $currentOperation + '&o=' + pageOffset;
			var reviewUrl = '#review';
			if ( $mtag != null ) {
				thisUrl += '&tag=' + $mtag;
				reviewUrl += '?tag=' + $mtag;
			}
			footerMarkup += '<div data-role="controlgroup" data-type="horizontal">';
			footerMarkup += '<a href="' + reviewUrl + '" data-role="button" data-icon="arrow-u" data-mini="false">'+$buttontext.send+'</a>'; // Send
			footerMarkup += '<a href="#" data-role="button" data-icon="delete" data-mini="false" onclick="clearLocalModifications(' + clearLocalModificationsArgs + '); $.mobile.changePage(\'' + thisUrl + '\');">'+$buttontext.clearall+'</a>'; // Clear All
			footerMarkup += '</div>';
		} else {
			footerMarkup = '<a href="#" data-role="button" data-mini="false" data-icon="refresh" onclick="refreshInventory(true);">'+$buttontext.refresh+'</a>';
		}		
		footer.empty().append( $( footerMarkup ) );
		// Update content, reset first
		content.empty();
		// Form the markup for the materials listview
		var dataEntryText = getDataEntryText( null );
		if ( dataEntryText != null )
			content.append( $( '<div">' + dataEntryText + '.</div><p/>' ) );
		// Append the listview
		var materialsview;
		if ( hasTags ) {
			materialsview = getTagMaterialView( 'materials', tags, $mtag, pageOffset, null, null, null ); // returns a collapsible-set
		} else {
			materialsview = getMaterialListview( 'materials', materials.length, null ); // returns a list view
		}
		// Update content part
		content.append( materialsview );
	} // end if ( !hasMaterials )
	// Enhance the page
	page.page();
	page.trigger( 'create' );
	// Update the material list, given page is now created
	if ( hasMaterials ) {
		if ( !hasTags ) {
			updateMaterialListview( 'materials', materialsview, null, pageOffset, null, null, null, null ); // update material list and refresh
		} else if ( $mtag != null ) {
			// Expand the collapsible for the given tag
			materialsview.find( '#' + pageId + '_' + getId( $mtag ) ).trigger( 'expand' ); // expand the tag; material list is added and refreshed within the tag
		}
		// SEARCH FILTER
		// Loop through tags list and set the list view filter callback function for all the list controls
		if ( tags != null && tags.length > 0 ) {
			for ( var i = 0; i < tags.length; i++ ) {
				var tagId = pageId.substring( 1 ) + '_' + getId( tags[i] ) + '_ul';
				// Set the list view filter callback function
				setListViewFilterCallback( tagId, startsWithMaterials );
			}
		} else {
			setListViewFilterCallback( pageId.substring( 1 ) + '_ul', startsWithMaterials );
		}
		// SEARCH FILTER
	}
	// Now call changePage() and tell it to switch to the page we just modified.
	if ( options ) {
		$.mobile.changePage( page, options );
	} else
		$.mobile.changePage( page );
	header.find( '#materialsback .ui-btn-text').text($buttontext.back);
	header.find( '#materialsoptions .ui-btn-text').text($buttontext.options);
	page.find( '#materialshome').text($buttontext.home);
	page.find( '#materialsprintstock').text($buttontext.print);
	// do not show print in mobile or if configuration print inventory is disabled
	if ( isOperationDisabled( 'pi' ) || isDevice() ) {
		page.find('#materialsprint').hide();
		page.find('#materialsprintstock').hide();
	}
	else {
		page.find('#materialsprint').show();
		page.find('#materialsprintstock').show();
	}

	
}

function refreshInventory( changePage ) {
	// Refresh only if network connection available
	if ( !hasNetwork() && !isInventoryRefreshed() ) {
		setInventoryRefreshed(); // set this so that the toast below does not show up repeatedly
		toast( $messagetext.nonetworkconnection );
		return;
	}

	var url = getRefreshInventoryURL();
	// Show the page loading message
	var loadingmsg = $labeltext.refreshinginventory;
	// Load the inventory from server
	var request = {
		url: url,
		dataType: 'json',
		cache: false,
		success: function( o ) {
			// Set the fact that inventory was refreshed in this session
			setInventoryRefreshed();
			// Check status and process
			if ( o && o.st == '0' ) { // success
				// Update inventory locally
				updateLocalInventory( o, false ); //// true );
				// refresh materials page
				if ( changePage )
					$.mobile.changePage( '#materials' );
			} else {
				// Show error message
				toast( o.ms );
			}
		},
		error: function( o ) {
			// Set the fact that inventory was refreshed in this session
			setInventoryRefreshed();
		}
	};
	var logData = {
		functionName: 'refreshInventory',
		message: ''
	};
	// Request server
	requestServer( request, loadingmsg, logData, true, true );
}

// Get refresh inventor URL
function getRefreshInventoryURL() {
	return $host + '/api/i?a=gi&uid=' + $uid + '&kid=' + $entity.kid;
}


function getInventoryForRelatedEntity( entityId, relationType, showStock ) {
	var url = $host + '/api/i?a=gi&uid=' + $uid + '&kid=' + entityId;
	// Show the page loading message
	var loadingmsg = $labeltext.refreshinginventory;
	// Load the inventory from server
	var request = {
		url: url,
		dataType: 'json',
		cache: false,
		success: function( o ) {
			// Check status and process
			if ( o && o.st == '0' ) { // success
				// Update inventory for related Entity
				updateLocalInventoryForRelatedEntity( entityId, relationType, o );
				if (showStock)
					showRelatedEntityStock( entityId );
			} else {
				// Show error message
				toast( o.ms );
			}
		},
		error: function( o ) {
			//
		}
	};
	var logData = {
		functionName: 'getInventoryForRelatedEntity',
		message: ''
	};
	// Request server
	requestServer( request, loadingmsg, logData, true, true );
}

// Refresh transactions in trans. history from server
function refreshTransactions() {

	// Refresh only if network connection available
	/*
	if ( !hasNetwork() && !isTransactionsRefreshed() ) {
		setTransactionsRefreshed(); // set this so that the toast below does not show repeatedly on going to the page in the same session
		toast( $messagetext.nonetworkconnection );
		return;
	}
	*/
	var url = $host + '/api/t?a=gtrn&uid='+ $uid + '&kid=' + $entity.kid + '&s=50'; // earlier: uid and p were passed, but now done via BasicAuth
	// Show the page loading message
	var loadingmsg = $labeltext.refreshingtransactions;
	// Load the orders from server
	var request = {
		url: url,
		dataType: 'json',
		cache: false,
		success: function( o ) {
			// Set the flag to indicate that transactions have been refreshed in this session (so no need to auto-refresh)
			setTransactionsRefreshed();
			// Check status and process
			if ( o && o.st == '0' ) { // success
				// Update transactions locally
				updateLocalTransactions( o );
				// Refresh the transactions page
				$.mobile.changePage( getCurrentUrl() );
			} else {
				toast( o.ms );
			}
		},
		error: function( o ) {
			// Set the flag to indicate that transactions have been refreshed in this session (so no need to auto-refresh)
			setTransactionsRefreshed();
		},
		sync: {
			op: 'rtrns',
			uid: $uid,
			kid: $entity.kid,
			callback: function() {
				// Set the flag to indicate that transactions have been refreshed in this session (so no need to auto-refresh)
				setTransactionsRefreshed();
			}
		}
	};
	var logData = {
		functionName: 'refreshTransactions',
		message: ''
	};
	// Request server
	requestServer( request, loadingmsg, logData, true, true );
}


// Get the material list markup
function getMaterialListview( id, size, tag ) {
	var listId = id + '_ul';
	if ( tag != null )
		listId = id + '_' + getId( tag ) + '_ul';
	var markup = '<ul id="' + listId + '" data-role="listview" data-theme="d" data-divider-theme="d"'; 
	// SEARCH FILTER
	// Add the data filter and placeholder attributes only if the number of materials > $PAGE_SIZE
	if ( size > $PAGE_SIZE )
		markup += ' data-filter="true" data-filter-placeholder="'+$messagetext.enteramaterialname+'">';
	else
		markup += '>';
	// SEARCH FILTER
	markup += '</ul>';
	return $( markup );
}

// Update material list view
function updateMaterialListview( id, listview, tag, pageOffset, excludeMids, midsToAdd, oid, otype ) {
	var mids = [];
	// Append the navigation bar
	if ( !midsToAdd || midsToAdd == null ) {
		var size = 0;
		if ( tag == null )
			size = $entity.mt.length; // get from all materials
		else
			size = $materialData.tagmap[ tag ].length; // get only from materials for this tag
		// Set the listview navigation bar
		var range = setListviewNavBar( listview, pageOffset, size, tag, function() {
			updateMaterialListview( id, listview, tag, ( pageOffset - 1 ), excludeMids, midsToAdd, oid, otype );
		}, function() {
			updateMaterialListview( id, listview, tag, ( pageOffset + 1 ), excludeMids, midsToAdd, oid, otype );
		} );
		var start = range.start;
		var end = range.end;
		// Get the list of material Ids for rendering
		if ( tag == null ) {
			var materials = $entity.mt;
			for ( var i = ( start - 1 ); i < end; i++ ) // get the subset of materials for this page
				mids.push( materials[i].mid );
		} else {
			var materialIds = $materialData.tagmap[ tag ];
			for ( var i = ( start - 1 ); i < end; i++ ) // get the subset of materials for this page under this tag
				mids.push( materialIds[i] ); 
		}
	} else
		mids = midsToAdd;
	// Render the list items
	for ( var i = 0; i < mids.length; i++ ) {
		var material = $materialData.materialmap[ mids[i] ];
		// Check if this material is to be excluded
		var excludeFromList = ( excludeMids != null && excludeMids && excludeMids.length > 0 && $.inArray( parseFloat(material.mid), excludeMids ) != -1 );
		var url = '#materialinfo?mid=' + material.mid;
		if ( tag != null )
			url += '&tag=' + tag;
		if ( oid && oid != null )
			url += '&oid=' + oid;
		if ( otype && otype != null )
			url += '&oty=' + otype;
		// Add page offset param.
		url += '&o=' + pageOffset;
		// Get local modifications, if any
		var localData = getLocalModifications( material.mid, $currentOperation, $entity );
		// Update material markup
       // var markup = '<li class="'+getEventStyleClass(material)+'" id="' + id + '_' + material.mid + '"';
		var markup = '<li  id="' + id + '_' + material.mid + '"';
        var showUrlLink = true;
		var onclick = null;
		var orderType = 'prc';
		if ( $currentOperation == 'no')
			orderType = $newOrderType;
		else if ( $currentOperation == 'ao' || $currentOperation == 'vo'){
			var params = getURLParams(getCurrentUrl());
			if ( params.oty )
				orderType = params.oty;
		}

		if (material.q == 0 && ($currentOperation == 'ei' || $currentOperation == 'ew' || $currentOperation == 'ts' )) {
			url = '#';
			onclick = 'toast(\'' + $messagetext.nostockavailable + '\')';
		}
		if (!excludeFromList)
			markup += '><a href="' + url +'"';
		else
			markup += ' url="' + url +'"'; // this is used updateListviewSearchResults to get the URL params.
		if (onclick)
		 markup += '" onclick="' + onclick+'"';
		markup += ' >';
		var stockColor = getStockColor( material );
		//	markup += '<h3>' + material.n +'<span style="font-size:8pt;float:right;border-radius:25px; border:2px solid #D3D3D3;padding:5px;">' + getFormattedNumber( material.q )  + '</span></h3>'+ '<p>';
		markup += '<h3>' + material.n +'<span style="font-size:8pt;float:right;border-radius:25px; border:2px solid #D3D3D3;padding:5px;color:' + stockColor + '">' + getFormattedNumber( material.q )  + '</span></h3>'+ '<p>';

		if ( material.rp && Number( material.rp )  != 0 && !$disableOrderPrice  ) {
			var currency = getCurrency( material );
			if ( currency != null && currency != '' )
				markup += currency + ' ';
			markup += getFormattedPrice( material.rp)  + ', ';
		}
		markup += ' ' + material.t;
		// Add local modifications, if any
		if ( localData != null ) {
			markup += '<br/><font style="color:red !important;font-size:9pt">'+$labeltext.quantityentered+': ' + getFormattedNumber( localData.q );
			///var reasonStatusText = getReasonStatusText(localData.reason,localData.mst);
			///if (reasonStatusText!= '')
			///	markup += reasonStatusText;
			if (localData.atd)
				markup += ', '+localData.atd;
			// Show any partial errors in updating inventory
			if ( localData.error )
				markup += '<br/><b>' + $messagetext.error + '...' + $messagetext.correctandresend + '</b>';
			markup += '</font>';
		}
		markup += '</p>';
		if ( !excludeFromList )
			markup += '</a>';
		markup += '</li>';
		// Append item to listview
		listview.append( $( markup ) );
	} // end for
	// Refresh the view
	listview.listview('refresh');
}

// Function to create a navigation markup for a listview
function setListviewNavBar( listview, pageOffset, size, tag, prevClickCallback, nextClickCallback ) {
	// Empty the listview
	listview = listview.empty();
	// Get the navigation start and end
	var start = ( pageOffset * $PAGE_SIZE ) + 1;
	var end = start + $PAGE_SIZE - 1;
	if ( end > size )
		end = size;
	// Flags for prev./next
	var hasPrev = ( pageOffset > 0 );
	var hasNext = ( end < size );
	// Get the prev/next button ids, with or w/o tag
	var id = listview.attr( 'id' );
	var prevId = id + '_prev', nextId = id + '_next';
	if ( tag != null ) {
		prevId += '_' + getId( tag );
		nextId += '_' + getId( tag );
	}
	// Add the nav. info to the list view
	var navMarkup = '<li data-role="list-divider">';
	if ( hasPrev ) // put prev. button
		navMarkup += '<a id="' + prevId + '" href="#"><img src="jquerymobile/icons/left.png" valign="middle" title="previous" alt="'+$buttontext.previous+'" /></a>&nbsp;&nbsp;';
	navMarkup += '<font color="green">' + start + '</font>-<font color="green">' + end + '</font>'+' '+$labeltext.of+'<font color="green">' + ' '+size + '</font>';
	if ( hasNext ) // put next button
		navMarkup += '&nbsp;&nbsp;<a id="' + nextId + '" href="#"><img src="jquerymobile/icons/right.png" valign="middle" title="next" alt="'+$buttontext.next+'" /></a>';
	navMarkup += '</li>';
	listview.append( $( navMarkup ) );
	// Bind click events for prev/next, as needed
	if ( hasPrev ) {
		listview.find( '#' + prevId ).off('click').on( 'click', function( e, ui ) {
			e.preventDefault();
			prevClickCallback(); 
		});
	}
	if ( hasNext ) {
		listview.find( '#' + nextId ).off('click').on( 'click', function( e, ui ) {
			e.preventDefault();
			nextClickCallback();
		});
	}
	// Return the range for this page
	var range = {};
	range.start = start;
	range.end = end;
	return range;
}

// Get the tag entity list markup
function getTagEntityView( id, tags, tag, pageOffset, type, linkedPageId ) {
	var collapsibleSet = $ ( '<div data-role="collapsible-set" data-theme="a" data-content-theme="d" data-collapsed-icon="arrow-r" data-expanded-icon="arrow-d"></div>' );
	for ( var i = 0; i < tags.length; i++ ) {
		var offset = 0;
		var collapsed = true;
		if ( tag != null && tag == tags[i] ) {
			offset = pageOffset;
			collapsed = false;
		}
		// Get the number of entities under a given tag
		var entityOrIds = $entitiesData[ type ][ tags[i] ];
		var itemsUnderTag = 0;
		if ( entityOrIds )
			itemsUnderTag = entityOrIds.length;
		if ( itemsUnderTag == 0 )
			continue;
		// Render a collapsible list with tags
		var divId = id + '_' + getId( tags[i] );
		var markup = '<div id="' + divId + '" data-role="collapsible" offset="' + offset + '" tag="' + tags[i] + '">';
		markup += '<h2>' + tags[i] + ' (' + itemsUnderTag + ')</h2>';
		markup += '</div>';
		var collapsible = $( markup );
		var listview = getEntityListview( id, itemsUnderTag, tags[i], false ); // gets an empty list;
		collapsible.append( listview );
		// Attach an expand event
		collapsible.off('expand').on( 'expand', function( e, ui ) {
			var thisOffset = parseInt( $(this).attr('offset') );
			if ( type == 'ents') {
				$rtag = $(this).attr( 'tag' ); // update the route tag
				updateEntitiesListview( $(this).find(':jqmData(role=listview)'), null, $rtag, thisOffset, null );
			} else {
				$rtag_re = $(this).attr( 'tag' ); // update the route tag
				updateRelatedEntitiesListview( $(this).find(':jqmData(role=listview)'), type, $rtag_re, thisOffset, null, id, linkedPageId );
			}
		});
		// Append to set
		collapsibleSet.append( collapsible );
	} // end for
	return collapsibleSet;
}

// Get the entity list markup
function getEntityListview( id, size, tag, isDataInset ) {
	var listId = id + '_ul';
	if ( tag != null )
		listId = id + '_' + getId( tag ) + '_ul';
	var markup = '<ul id="' + listId + '" data-role="listview" data-theme="d" data-inset="' + ( isDataInset ? 'true' : 'false' ) + '" data-divider-theme="d"'; 
	// SEARCH FILTER
	// Add the data filter and placeholder attributes only if the number of materials > $PAGE_SIZE
	if ( size > $PAGE_SIZE )
		markup += ' data-filter="true" data-filter-placeholder="'+$labeltext.enterentityname+'">';
	else
		markup += '>';
	// SEARCH FILTER
	markup += '</ul>';
	return $( markup );
}

// Get the tag material list markup
function getTagMaterialView( id, tags, tag, pageOffset, excludeMaterialIds, oid, otype ) {
	var collapsibleSet = $ ( '<div data-role="collapsible-set" data-theme="a" data-content-theme="d" data-collapsed-icon="arrow-r" data-expanded-icon="arrow-d"></div>' );
	for ( var i = 0; i < tags.length; i++ ) {
		var offset = 0;
		var collapsed = true;
		if ( tag != null && tag == tags[i] ) {
			offset = pageOffset;
			collapsed = false;
		}
		// Get the number of items under a given tag
		var materialIds = $materialData.tagmap[ tags[i] ];
		var itemsUnderTag = 0;
		if ( materialIds )
			itemsUnderTag = materialIds.length;
		if ( itemsUnderTag == 0 )
			continue;
		// Form a material list
		var materialsUnderTag = [];
		for ( var j = 0; j < materialIds.length; j++ )
			materialsUnderTag.push( $materialData.materialmap[ materialIds[ j ] ] );
		// Render a collapsible list with tags
		var divId = id + '_' + getId( tags[i] );
		var markup = '<div id="' + divId + '" data-role="collapsible" offset="' + offset + '" tag="' + tags[i] + '">';
		markup += '<h2>' + tags[i] + ' (' + itemsUnderTag + ')' ;
		if ( isTagDirty( tags[i], $currentOperation))
		 markup += '<img src="jquerymobile/icons/redflag.png" title="modified" align="right" alt=" (modified)" id='+ tags[i]+'"_modified" style="display:block" />';
	    else
			markup += '<img src="jquerymobile/icons/redflag.png" title="modified" align="right" alt=" (modified)" id='+ tags[i]+'"_modified" style="display:none" />';
		markup += '</h2>';
		markup += '</div>';
		var collapsible = $( markup );
		var listview = getMaterialListview( id, materialsUnderTag.length, tags[i] ); // gets an empty list; can be updated using updateMaterialListview 
		collapsible.append( listview );
		// Attach an expand event
		collapsible.off('expand').on( 'expand', function( e, ui ) {
			$mtag = $(this).attr( 'tag' );
			var thisOffset = parseInt( $(this).attr('offset') );
			updateMaterialListview( id, $(this).find(':jqmData(role=listview)'), $mtag, thisOffset, excludeMaterialIds, null, oid, otype );
		});
		// Append to set
		collapsibleSet.append( collapsible );
	} // end for
	return collapsibleSet;
}

// Render a list of materials to be added to an order
function renderAddMaterialsToOrder( pageId, params, options ) {
	if ( !$entity || $entity == null )
		return;
	// Get the materials
	var materials = $entity.mt;
	if ( !materials || materials.length == 0 )
		return;
	var oid = null, otype = null;
	if ( params.oid )
		oid = params.oid;
	if ( params.oty )
		otype = params.oty;
	var pageOffset = 0;
	if ( params && params.o )
		pageOffset = parseInt( params.o );
	var order = getOrder( oid, otype );
	var hasModifications = hasLocalModifications( $currentOperation, $entity, true );
	var tags = null;
	// Get tags, if any
	if ( $materialData.tags )
		tags = getValidTags($materialData.tags);
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Update the header with the title and number of entities
	var headerTxt =$labeltext.addtoorder+' ' + oid;
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update entity title
	setTitleBar( header, $entity.n, false );
	var backButton = header.find( '#addmaterialstoorderback' );
	if ( hasModifications ) {
		backButton.attr( 'style', 'display:none' );
	} else {
		backButton.attr( 'href', '#orderinfo?oid=' + oid + '&oty=' + otype ); // go back to order
		backButton.attr( 'style', 'display:block' );
	}
	// Update content, first with tags (if any), and then with material list
	var footer = page.children( ':jqmData(role=footer)' );
	var footerMarkup = '';
	// Check if local modifications exist; if so, show the Send/Clear-All buttons
	if ( hasModifications ) {
		var thisUrl = '#addmaterialstoorder?oid=' + oid + '&oty=' + otype + '&o=' + pageOffset;
		var orderUrl = '#orderinfo?oid=' + oid + '&oty=' + otype;
		if ( $mtag != null )
			thisUrl += '&tag=' + $mtag;
		var clearLocalModificationsArgs = '\'' + $uid + '\',\'' + $currentOperation + '\',null,true,false';
		var addToOrderOnClick = 'saveOrderAdds(\'' + oid + '\',\'' + otype + '\'); $.mobile.changePage(\'' + orderUrl + '\')'; 
		footerMarkup = '<div data-role="controlgroup" data-type="horizontal">';
		footerMarkup += '<a href="#" data-role="button" data-icon="arrow-u" data-mini="false" onclick="' + addToOrderOnClick + '">'+$labeltext.addtoorder+' ' + oid + '</a>'; // Add to order
		footerMarkup += '<a href="#" data-role="button" data-icon="delete" data-mini="false" onclick="clearLocalModifications(' + clearLocalModificationsArgs + '); $.mobile.changePage(\'' + thisUrl + '\');">'+$buttontext.clearall+'</a>'; // Clear All
		footerMarkup += '</div>';
	}
	// Update footer
	footer.empty();
	if ( footerMarkup != '' )
		footer.append( $( footerMarkup ) );
	// Update the content
	content.empty();
	// Add the prompt
	content.append( '<p>'+$labeltext.additemstoorder+' ' + order.tid + '</p>' );
	// Get materials to be excluded from rendering, i.e. those already in order
	var excludeMaterialIds = getMidsInOrder( order );
	// Form the listview
	var materialsview;
	var hasTags = ( tags != null && tags.length > 0 );
	if ( materials.length == excludeMaterialIds.length ) { // all materials already in order
		materialsview = $( '<ul data-role="listview" data-theme="d" data-divider-theme="d"><li>'+$messagetext.allitemsinorder+' ' + oid + '</li></ul>' );
	} else {
		if ( hasTags )
			materialsview = getTagMaterialView( 'addmaterialstoorder', tags, $mtag, pageOffset, excludeMaterialIds, oid, otype ); // returns a collapsible-set
		else
			materialsview = getMaterialListview( 'addmaterialstoorder', materials.length, null ); // returns a list view
	}
	// Update content part
	content.append( materialsview );
	// Enhance the page
	page.page();
	page.trigger( 'create' );
	// Update the material list, given page is now created
	if ( !hasTags ) {
		updateMaterialListview( 'addmaterialstoorder', materialsview, null, pageOffset, excludeMaterialIds, null, oid, otype ); // update material list and refresh
	} else if ( $mtag != null ) {
		// Expand the collapsible for the given tag
		materialsview.find( '#' + pageId + '_' + getId( $mtag ) ).trigger( 'expand' ); // expand the tag; material list is added and refreshed within the tag
	}
	// Set the dataUrl option to the URL of the entities
	options.dataUrl = '#addmaterialstoorder?oid=' + oid + '&oty=' + otype + '&o=' + pageOffset;
	if ( $mtag != null )
		options.dataUrl += '&tag=' + $mtag;
	
	// SEARCH FILTER
	// Loop through tags list and set the list view filter callback function for all the list controls
	if ( tags != null && tags.length > 0 ) {
		for ( var i = 0; i < tags.length; i++ ) {
			var tagId = 'addmaterialstoorder_' + getId( tags[i] ) + '_ul'; /// TODO: use getID and form the correct IDs
			// Set the list view filter callback function
			setListViewFilterCallback( tagId, startsWithAddMaterialsToOrder );
		}
	} else {
		setListViewFilterCallback( 'addmaterialstoorder_ul', startsWithMaterials );
	}
	// SEARCH FILTER
	
	// Now call changePage() and tell it to switch to the page we just modified.
	$.mobile.changePage( page, options );
	header.find( '#addmaterialstoorderback .ui-btn-text').text($buttontext.back);
}

// Render the review page for either inventory update, or new order
function renderReview( pageId, params, options ) {
	// Get the parameters
	var tag = null;
	if ( params && params != null && params.tag )
		tag = params.tag;
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Update the header title
	var headerTxt = $labeltext.review+' ' + getOperationName( $currentOperation );
	header.children( 'h3' ).html( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update the back button and cancel URLs
	var prevPageId = getPrevPageId( options );
	if ( prevPageId != null ) {
		var backUrl = prevPageId + '?op=' + $currentOperation + ( tag ? '&tag=' + tag : '' );
		// Update the back URL with the entities page and its offset
		header.find( pageId + 'back' ).attr( 'href', backUrl );
		// Update the Cancel button click/URL in foote
		footer.find( '#reviewcancelbutton' ).attr( 'href', backUrl );
		page.find('#ordertags').val(''); //reset order tag selection
	}
	// Get the local modifications for this operation
	var linkedEntityId = '';
	//get order type
	var otype = 'prc';
	if ($currentOperation == 'no') {
		var orderLinkedEntity = getSelectedLinkedEntity($currentOperation, $entity);
		if ( orderLinkedEntity )
			otype = orderLinkedEntity.otype;
	}
	if (params.lkid)
	   linkedEntityId = params.lkid;
    if (linkedEntityId)
    {
    	var linkedEntity = {};
    	var linkedEntityType = getSelectedLinkedEntityType(linkedEntityId,$entity.kid);
    	linkedEntity.kid = linkedEntityId;
    	linkedEntity.type = linkedEntityType;
		if ($currentOperation == 'no' ) {
			if (otype)
			 linkedEntity.otype = otype;
		}
		$entity.localdata[$currentOperation ].linkedentity = linkedEntity;
		storeEntity( $uid, $entity );
    }
	var localModificationList = getLocalModificationsSorted( $currentOperation, $entity, $materialData.materialmap );
	var markup = '';
	var noItems = ( !localModificationList || localModificationList == null || localModificationList.length == 0 );
	var orderSize = 0;
	if ( !noItems )
		orderSize = localModificationList.length;
	if ( !noItems || ( $currentOperation == 'no' && allowEmptyOrders() ) ) {
		// Message at top
		markup = '<div>' + getDataEntryText( $messagetext.reviewandconfirm+' ' ) + '.</div></p>';
		var isNewOrder = ( $currentOperation == 'no' );
		// Message field / Related entities section
		if ( isNewOrder ) {
			// Get order review form
			markup += getOrderReviewForm( null, orderSize, true );
			// Get order price
			var orderPrice = 0;
			if ( localModificationList )
				orderPrice = computeOrderPrice( localModificationList, false );
			// Get the credit info display
			if (!$disableOrderPrice)
				markup += getCreditInfoDisplay( orderPrice );
		}
		// Update the content page
		if ( noItems ) {
			markup += '<p>'+$messagetext.noitems+'</p>';
		} else {
			markup += '<p><strong>' + localModificationList.length + '</strong> '+$labeltext.items+':</p>';
			markup += '<ul data-role="listview">';
			for ( var i = 0; i < localModificationList.length; i++ ) {
				var material = localModificationList[i];
				markup+= '<li>' + material.n;
				if (!$disableOrderPrice) {
					var formattedPrice = getMaterialPrice(material.mid, true); // get formatted material price, if any
					if (formattedPrice != '')
						markup += ', <font style="color:green">' + formattedPrice + '</font>';
					// Check if tax is specified at inventory level
					var m = $materialData.materialmap[material.mid];
					if (m.tx && m.tx != 0)
						markup += ' [<font style="color:blue">' + getFormattedPrice(m.tx) + '% tax</font>]';
				}
				var reasonStatusText = getReasonStatusText(material.reason,material.mst);
				if (reasonStatusText!= '')
					markup += '<br/><font style="font-size:9pt;font-weight:normal">'+reasonStatusText+'</font>';
				if (material.atd)
					markup += '<br/><font style="font-size:9pt;font-weight:normal">' + getATDText($currentOperation) + ': ' + material.atd + '</font>';
				markup += '<span class="ui-li-count" style="font-size:12pt">' + getFormattedNumber( material.q ) + '</span>';
				markup += getBatchDataDisplayMarkup(material);
				markup += '</li>';
			}
			// If it is a new order, show total price
			if ( isNewOrder && !$disableOrderPrice) {
				var totalPrice = computeOrderPrice( localModificationList, true );
				if ( totalPrice != '' ) {
					markup += '<li></li>';
					markup += '<li style="float:right"><strong>'+$labeltext.total+': <font style="color:green">' + totalPrice + '</font></strong></li>';
				}
			} 
			markup += '</ul>';
		}
	} else {
		markup = '<p>' + $messagetext.noitemstoreview  + '</p>';
	}
	// Update content
	content.html( markup );
	// Enhance page
	page.page();
	page.trigger('create');
	// Update options URL and change to page
	options.dataUrl = '#review';
	if ( tag != null )
		options.dataUrl += '?tag=' + tag;
	$.mobile.changePage( page, options );
	footer.find( '#sendnowbutton .ui-btn-text').text($buttontext.sendnow);
	footer.find( '#reviewcancelbutton .ui-btn-text').text($buttontext.cancel);
	header.find( '#reviewback .ui-btn-text').text($buttontext.back);
}

function getBatchDataDisplayMarkup(material) {
	var markup = '';
	if (material.bt && material.bt.length > 0) {
		for ( var i=0; i< material.bt.length; i++) {
			//if (i > 0)
			// 	markup+='<hr style="width:70%;" />';
			if (i == material.bt.length - 1) //no border for the last batch
				markup += '<div>';
			else
				markup += '<div style="border-bottom:1px #d3d3d3 solid;width:70%">';
			markup += '<font style="font-size:8pt;font-weight:normal">' + $labeltext.batch + ': ' + material.bt[i].bid + ', '
				 		+ $labeltext.quantity + ': <b>' + material.bt[i].q + '</b></br>';
			markup += $labeltext.manufacturedby + ': ' + material.bt[i].bmfnm;
			if ( material.bt[i].bmfdt )
				markup +=  ', ' + material.bt[i].bmfdt;
			markup += '</br>';
			markup +=  $labeltext.batchexpirydate + ': ' + material.bt[i].bexp ;
			markup += '</br>';
			if (material.bt[i].mst)
				markup +=  $labeltext.status + ': ' + material.bt[i].mst ;
				markup += '</font>';
			markup += '</div>';
		}
	}
	return markup;
}

// Get the display for available credit and credit balance in order review
function getCreditInfoDisplay( orderPriceFloat ) {
	var linkedEntity = getSelectedLinkedEntity( $currentOperation, $entity );
	if ( !linkedEntity || linkedEntity == null )
		return '';
	var relatedEntityInfo = getRelatedEntity( linkedEntity.kid, linkedEntity.type );
	var markup = '';
	if (relatedEntityInfo){
		var creditLimit = relatedEntityInfo.crl;
		var availableCredit = getAvailableCredit( relatedEntityInfo );
		var currency = ( $entity.cu ? currency = $entity.cu + ' ' : '' );
		if ( creditLimit ) {
			var creditAfterOrder = round( parseFloat(availableCredit - orderPriceFloat), 2 );
			var colorCreditNow = ( availableCredit > 0 ? 'green' : 'red' );
			var colorCreditAfterOrder = ( creditAfterOrder > 0 ? 'green' : 'red' );
			markup = '<div class="ui-body-d ui-corner-all ui-shadow" style="padding:0.75em;margin-top:2%;">';
			markup += $messagetext.creditavailablenow+': <font style="color:' + colorCreditNow + '">' + currency + availableCredit +  '</font> ('+$labeltext.limit+': <font style="color:blue">' + currency + creditLimit + '</font>)';
			markup += '<br/>'+$messagetext.creditavailableafterorderships+': <font style="color:' + colorCreditAfterOrder + '">' + currency + creditAfterOrder + '</font>';
			markup += '</div>';
		}		
	}
	return markup;
}

function getPostOperationQuantity(currentQuantity, transactionQuantity){
	if ($currentOperation == 'ei' || $currentOperation == 'ts'){
		return parseFloat(currentQuantity - transactionQuantity);
	}
	if ($currentOperation == 'er'){
		return parseFloat(currentQuantity + transactionQuantity);
	}
	if ($currentOperation == 'es'){
		return parseFloat(transactionQuantity);
	}
	return null;
}

// Render the info. for a given material; if 'form' is given, then render appropriate form (without any detailed info.) 
function renderMaterialInfo( pageId, params, options ) {
	var material = $materialData.materialmap[ params.mid ];
	var tag = null;
	// Get tag, if any
	if ( params.tag ) {
		$mtag = params.tag;
		tag = $mtag;
	}
	// Get page offset, if any (for back button)
	var pageOffset = 0;
	if ( params.o )
		pageOffset = params.o;
	var  refreshBatchInfo = false;
	if (params.rb)
		refreshBatchInfo = true;

	if ( !material || material == null )
		return;
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Get the back button URL
	var backUrl = '#materials';
	var queryString = '?o=' + pageOffset;
	// Update header
	var headerTxt = '';
	if ($currentOperation != 'no' && $currentOperation != 'vs' && $currentOperation != 'vo') {
	  if ( haslocalModificationsForMid( $currentOperation,$entity,material.mid, true ))  {
		  if ($currentOperation == 'er')
			showDialog($messagetext.warning,$messagetext.clearenterreceiptsformaterial, getCurrentUrl());
		  else if ($currentOperation == 'ei')
			showDialog($messagetext.warning,$messagetext.clearenterissuesformaterial, getCurrentUrl());
		  else if ($currentOperation == 'ts')
			showDialog($messagetext.warning,$messagetext.clearentertransferstockformaterial, getCurrentUrl());
		  else if ($currentOperation == 'es')
				showDialog($messagetext.warning,$messagetext.clearenterphysicalstockformaterial, getCurrentUrl());
		  else if ($currentOperation == 'ew')
				showDialog($messagetext.warning,$messagetext.clearenterdiscardsformaterial, getCurrentUrl());						
		  return;
	   }
	}
	if ( $currentOperation == 'ao' ) { // add to order
		headerTxt = $labeltext.addtoorder+' ' + params.oid ;
		backUrl = '#addmaterialstoorder';
		if ( params.oid )
			queryString += '&oid=' + params.oid;
		if ( params.oty )
			queryString += '&oty=' + params.oty;
	} else {
		headerTxt = getOperationName( $currentOperation );
	}
	// Get the back/save URLs
	backUrl += queryString;
	header.find( 'h3' ).html( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update the back button
	header.find( '#materialinfoback' ).attr( 'href', backUrl );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update content	
	var markup = '<table style="width:100%"><tbody><tr><td style="width:97%">';
	// Update title
	markup += '<h3>' + material.n + '</h3>';
	markup += '</td><td style="text-align:right;width:3%">';
	// Show the title and camera icon for photo-capture (if op. is view-stock); allow access only to administrators
	var maxPhotos = 3; // allow 3 photos per material
	var domainKey = material.mid;
	var numPhotos = getNumMediaItems( domainKey );
	if ( isDevice() && $currentOperation == 'vs' && isAdministrator( $role ) && numPhotos < maxPhotos ) {
		// Set the params globally, in case it is used by photo/media service
		$lastPageParams = params;
		var onClick = 'capturePhoto(\'' + domainKey + '\',\'' + material.n + '\',' + maxPhotos + ')';
		markup += '<a href="#" onclick="' + onClick + '"><img src="jquerymobile/icons/camera.png" /></a>';
	}
	markup += '</td></tr></tbody></table>';
   // markup += '<div class="'+getEventStyleClass(material)+'" ui-body-d ui-corner-all ui-shadow" style="padding:0.75em">';
	markup += '<div ui-body-d ui-corner-all ui-shadow" style="padding:0.75em">';
	markup += '<table data-role="table" data-mode="reflow" class="infotable"><tbody>';
 	if ( material.rp && !$disableOrderPrice) {
		markup += '<tr><th>'+$labeltext.price+':</th><td>';
		var currency = getCurrency( material );
		if ( currency != null )
			markup += currency + ' ';
		markup += getFormattedPrice( material.rp );
		if ( material.tx && material.tx != 0 )
			markup += ' [<font style="color:blue">' + getFormattedPrice(material.tx) + '% tax</font>]';
		markup += '</td></tr>';
	}

	var crType;
	if ( material.crD )
		crType = 'd';
	else if ( material.crW )
		crType = 'w';
	else if ( material.crM)
		crType = 'm';
	var stFreq = getFrequencyText( "stock", crType ); //Stock frequency and crFrequency are the same
	var stockColor = getStockColor( material );
	//markup += '<tr><th>'+$labeltext.quantity+':</th><td>' + getFormattedNumber( material.q ) + '</td></tr>';
	markup += '<tr><th class="material-info-header">'+$labeltext.stockonhand+':</th><td>' + '<font style="color:' + stockColor + '">'  + getFormattedNumber( material.q ) + '</font></td></tr>';

	if ( stFreq && material.dsq ) {
		var durationText;
		durationText = $labeltext.durationofstock.replace('drtn',stFreq);
		markup += '<tr><th class="material-info-header">' + durationText  + ':</th><td>' + getFormattedNumber(material.dsq) + '</td></tr>';
	}
	var minDuration = '';
	var maxDuration = '';
	var minmaxFreq = getFrequencyText( "minmax", $config.frmx );
	if ( minmaxFreq && material.dmin )
		minDuration = ' ('  + material.dmin + ' ' + minmaxFreq + ')';
	if ( minmaxFreq && material.dmax )
		maxDuration = ' ('  + material.dmax + ' ' + minmaxFreq + ')';
	if ( material.min )
		markup += '<tr><th class="material-info-header">'+$labeltext.min+':</th><td>' + getFormattedNumber( material.min ) +  minDuration +'</td></tr>';
	if ( material.max )
		markup += '<tr><th class="material-info-header">'+$labeltext.max+':</th><td>' +getFormattedNumber( material.max )  + maxDuration + '</td></tr>';
	if ( isAutoPostInventory( $config ) ) {
		if (material.alq)
			markup += '<tr><th class="material-info-header">' + $labeltext.allocated + ':</th><td>' + getFormattedNumber(material.alq) + '</td></tr>';
		if (material.avq)
			markup += '<tr><th class="material-info-header">' + $labeltext.available + ':</th><td>' + getFormattedNumber(material.avq) + '</td></tr>';
		if (material.itq)
			markup += '<tr><th class="material-info-header">' + $labeltext.intransit + ':</th><td>' + getFormattedNumber(material.itq) + '</td></tr>';

	}
	var crFreq = getFrequencyText ( "cr", crType );
	var crFreqStr = '';
	if ( crFreq )
		crFreqStr = ' / ' + crFreq;
	if (material.crD)
	    markup += '<tr><th class="material-info-header">'+$labeltext.consumptionrate+':</th><td>' + material.crD + crFreqStr + '</td></tr>' ;
	else if (material.crW )
	    markup += '<tr><th class="material-info-header">'+$labeltext.consumptionrate+':</th><td>' + material.crW + crFreqStr + '</td></tr>';
	else if (material.crM)
	    markup += '<tr><th class="material-info-header">'+$labeltext.consumptionrate+':</th><td>' + material.crM + crFreqStr + '</td></tr>';
	if (material.dfr)
	    markup += '<tr><th class="material-info-header">'+$labeltext.demandforecast+':</th><td>' +  material.dfr + '</td></tr>';
	markup += '<tr><th <th class="material-info-header">'+$labeltext.updated+':</th><td>' + material.t + '</td></tr>';
	markup += '</tbody></table>';
	markup += '</div>';
	// Get data entry form, if write op.
	if ( !isOperationReadOnly( $currentOperation ) ) // get form data markup (including any locally modified data)
		markup += getDataEntryFormMarkup( material.mid, backUrl ,refreshBatchInfo);
	else if ( material.ben && $currentOperation == 'vs' )
		markup += getBatchListViewMarkup( material, $currentOperation, pageOffset );
	$existingQuantity = material.q;
     // Fetch related entity stock
	if ($currentOperation == 'ei' || $currentOperation =='er' || $currentOperation == 'ts' || isOperationOrder( $currentOperation )){
		var relatedEntity = getSelectedLinkedEntity($currentOperation,$entity);
		if (!relatedEntity && isOperationOrder)
			relatedEntity = getOrderRelatedEntity($currentOperation,$newOrderType );
		if (relatedEntity)
		  markup += getFetchRelatedEntityInventoryMarkup( relatedEntity, material.mid )
	}
	// Show the photos of this material, if any
	var photoGalleryMarkup = doGalleryMarkupMagic( material.mid );
	if ( photoGalleryMarkup != '' ) {
		markup += '<div class="ui-body-d ui-corner-all ui-shadow" style="padding:0.75em;margin-top:2%;">';
		markup += photoGalleryMarkup;
		markup += '</div>';
	}
	// Message, if any
	if ( material.ms && material.ms != '' ) { // Show the message/description on viewing stock
		markup+= '<div class="ui-body-d ui-corner-all ui-shadow" style="padding:0.75em;margin-top:2%;">';
		markup += material.ms;
		markup += '</div>';
	}
	// Update content part
	content.html( markup );
	// Enhance the page
	page.page();
	page.trigger('create'); // this will ensure that all internal components are inited./refreshed
	// Update options data url
	options.dataUrl = '#materialinfo?o=' + pageOffset + '&mid=' + params.mid;
	if ( tag && tag != null )
		options.dataUrl += '&tag=' + tag;
	if ( params.oid )
		options.dataUrl += '&oid=' + params.oid;
	if ( params.oty )
		options.dataUrl += '&oty=' + params.oty;
	// Now call changePage() and tell it to switch to the page we just modified.
	$.mobile.changePage( page, options );
	header.find( '#materialinfoback .ui-btn-text').text($buttontext.back);

	// Focus on the first available quantity field
	var quantityFields = page.find( 'input[id^="quantity_"]' );
	var quantityFieldsEmpty = true;
	if ( quantityFields && quantityFields.length > 0 ) {
		for ( var i = 0; i < quantityFields.length; i++ ) {
			var quantityField = page.find( '#quantity_' + i );
			var quantityFieldVal = '';
			if (quantityField) {
				if (quantityField.val())
					quantityFieldVal = quantityField.val().trim();
				if (quantityFieldVal != '') {
					quantityFieldsEmpty = false;
					break;
				}
			}
		}
	}

	var quantity = page.find( '#quantity').val();
	var materialBatchEnabled = false;
	if (material.ben && !isOperationOrder($currentOperation))
       materialBatchEnabled = true;
	if (( quantity == '' && !materialBatchEnabled)|| (materialBatchEnabled && quantityFieldsEmpty && $currentOperation != 'er' && $currentOperation != 'es') ||
		(( materialBatchEnabled && $currentOperation =='er') && !isNewBatchAdded(material))
		|| (materialBatchEnabled && $currentOperation == 'es' && !quantityFields.length >0 && !isNewBatchAdded(material) ))
	 	$('#datasavebutton').prop('disabled', true).addClass('ui-disabled');
	if ( quantityFields && quantityFields.length > 0 )
		$(quantityFields[0]).focus();
	if (isOperationOrder($currentOperation))
		page.find('#irreasonsdiv_'+ material.mid).attr( 'style', 'display:none' );

}

// Render batch info. form
function renderBatchInfo( pageId, params, options ) {
	var material = $materialData.materialmap[ params.mid ];
	// Get the query string for this and back URL
	var queryString = 'mid=' + params.mid + ( params.o ? '&o=' + params.o : '' ) + ( params.tag ? '&tag=' + params.tag : '' );
	if ( !material || material == null )
		return;
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Get the back button URL
	var backUrl = '#materialinfo?' + queryString;
	// Update header
	var headerTxt = getOperationName( $currentOperation );
	header.find( 'h3' ).html( headerTxt );
	// Update the back button
	header.find( '#batchinfoback .ui-btn-text').text($buttontext.back);
	header.find( '#batchinfoback' ).attr( 'href', backUrl );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update content
	var markup = '<b>' + material.n + ': ' + $buttontext.addnewbatch + '</b>';
	markup += getAddBatchDataMarkup( material, backUrl, params );
	// Update content part
	content.empty().append( $( markup )  );
	// Enhance the page
	page.page();
	page.trigger('create'); // this will ensure that all internal components are inited./refreshed
	// Update options data url
	options.dataUrl = '#batchinfo?' + queryString;
	// Now call changePage() and tell it to switch to the page we just modified.
	$.mobile.changePage( page, options );
	if ( params && params.bid && params.bid != '' ) {
		page.find('#batchNumber').addClass('ui-disabled');
	}
	else {
		page.find('#batchNumber').removeClass('ui-disabled');
	}
	if (params.bmfnm) {
		page.find('#manufacturerName').val(decodeURIComponent(params.bmfnm));
	}
}


// Render batch info. form for orders
function renderOrderAllocate( pageId, params, options ) {
	var orderId = '';
	if ( params.oid )
		orderId = params.oid;
	var oType;
	if ( params.oty )
		oType = params.oty;
	var mid;
	if ( params.mid )
		mid = params.mid;
	var material = null;
	var batchDisabled = false;
	if ( oType == 'prc'){
		var order = getOrder( orderId, oType);
		var lkid = order.vid;
		if ( lkid && getRelatedEntityPermissions(lkid,'vnds', $entity.kid) == $MANAGE_ENTITY) {
			material = getRelatedEntityMaterial(lkid, mid);
			if (!material) {
				showPopupDialog($labeltext.confirm, $messagetext.vendorstocknotavailable, function () {
						getInventoryForRelatedEntity(lkid, 'vnds', null);
					},
					null, null, "popupAllocationStock", false);
				material = getRelatedEntityMaterial(lkid, mid);
			}
		}
		var  entity = getRelatedEntity(lkid, 'vnds')
		if (entity.dbm)
			batchDisabled = entity.dbm;
	}
	else { //sales order
		material = $materialData.materialmap[params.mid];
		if ($entity.dbm)
			batchDisabled = $entity.dbm;
	}
	if ( !material || material == null ) {
		return;
		}
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Update header
	// Get the query string for this and back URL
	var queryString = 'mid=' + params.mid + ( params.o ?'&o=' + params.o : '' ) + '&oid=' + orderId+ '&oty=' + oType;
	// Get the back button URL
	var backUrl = '#orderinfo?' + queryString + '&edit=true';
	var headerTxt = orderId;
	header.find( 'h3' ).html( headerTxt );
	// Update the back button
	header.find( '#orderallocateback .ui-btn-text').text($buttontext.back);
	header.find( '#orderallocateback' ).attr( 'href', backUrl );
	// Update header title
	setTitleBar( header, $entity.n, false );
	var msg =  $messagetext.availablestockiszero.replace('<material>',material.n);
	//check current allocation
	if ( material.avq == 0 ) {
		if ((material.alq && material.alq == 0)  ){
			showPopupDialog($messagetext.warning, msg, null, null, null, "popupnostock", true);
			return;
		}
	}
	// Update content
	var markup = '<b>' + material.n + ': ' + $buttontext.allocate + '</b>';
	if ( material.ben && !batchDisabled)
		markup += getOrderBatchListViewMarkup(material, $currentOperation, params.o);
	else {
		var qty = 0;
		var item = getOrderItem(orderId, oType, mid);
		if ( item && item.q )
			qty = item.q;
		if ( item && item.alq )
			qty = item.alq;
		if ( item && item.nalq )
			qty = item.nalq;
		var quantityId = 'quantity_' + mid;
		markup += ' <input type="number" min="0"  oninput="maxLengthCheck(\'' + quantityId + '\')" max="1000000"  id="' + quantityId + '" value="';
		markup += getFormattedNumber(qty) + '"/>';
		var qtyErrMsgId = "qtyerrmsg_" + mid;
		markup += '<div id="' + qtyErrMsgId + '" style="display:none"><font style="color:red;font-size:8pt;font-weight:normal;white-space:initial"></font></div>';
		markup += getMaterialStatusMarkup( 'ei', mid, item.mst, material.istmp, true);

	}

	// Buttons
	markup += '<a id="orderallocatesavebutton" href="#" data-role="button" data-mini="true" data-inline="true" data-icon="plus" data-theme="b" data-disabled="true" onclick="checkOrderAllocationData(\'' + material.mid + '\',\'' + orderId + '\',\'' + oType + '\',\'' + backUrl + '\')">' + $buttontext.save + '</a>';
	markup += '<a id="orderallocatecancelbutton" href="' + backUrl + '" data-role="button" data-mini="true" data-icon="delete" data-inline="true" data-theme="c" class="checkbuttonclick" data-direction="reverse"  >' + $buttontext.cancel + '</a>';
	/*var onDeallocateClick = 'confirmClearOrderAllocation(\'' + orderId + '\',\'' + oType + '\',\'' + item.mid + '\',\'' + backUrl  + '\')';
	markup += '<a id="deallocatebutton_' + item.mid + '" data-role="button"   class ="inputfieldchange';
	if (!isQtyAllocatedForMaterial(item, material.ben))
		markup += ' ui-disabled ';
	markup += ' " data-mini="true" data-inline="true" onclick="' + onDeallocateClick + '" > ' + $buttontext.deallocate + '</a>';*/

	// Update content part
	content.empty().append( $( markup )  );
	// Enhance the page
	page.page();
	page.trigger('create'); // this will ensure that all internal components are inited./refreshed
	// Update options data url
	options.dataUrl = '#orderallocate?' + queryString ;
	// Before page change save changes made to order info
	var changed = $('#orderinfo').find('#fieldchange').val();
	if ( changed == 1){
		var saveButtonFunction = function() { saveOrderEdits( orderId, oType, options.dataUrl) };
		showPopupDialog($buttontext.save + '?', $messagetext.changesmadepleasesave,saveButtonFunction,function(){$.mobile.changePage( page, options );},null,"popuponcancel");

	}
	else
	// Now call changePage() and tell it to switch to the page we just modified.
	 $.mobile.changePage( page, options );
}





function changeOrderStatusToFulfilled ( orderId, orderType, orderStatus, shipId, fflag ) {
	var confirmMsg = $messagetext.areyousureyouwantomarkorderasfulfilled;
	if ( shipId )
		confirmMsg = confirmMsg.replace('<order>', $labeltext.shipment_lower);
	else
		confirmMsg = confirmMsg.replace('<order>', $labeltext.order_lower);
	var page = $('#orderfulfilment');
	var order = getOrder(orderId, orderType);
	var editDisabled = false;
	var saveButtonFunction = function() { saveReceivedEdits( orderId, orderType, orderStatus, shipId, fflag ) };
	//showPopupDialog($buttontext.save + '?', $messagetext.changesmadepleasesave,saveButtonFunction,function(){return;},null,"popuponcancel");

	showPopupDialog ( $labeltext.confirm,confirmMsg,saveButtonFunction,null,null,"popuponfulfillment",null);

}


function saveReceivedEdits ( orderId, orderType, orderStatus, shipId, fflag ) {
	var orderFulfil;
	var shps = getOrderShipment(orderId, orderType,shipId )
	var page = $('#orderfulfilment');
	var items = shps.mt;
	var isError = false;
	if (page.find('#dar')) {
		var dar = $('#dar').val();
		if (!dar || dar == '') {
			$('#dar_error' + orderId).attr('style', 'display:block');
			isError = true;;
		}
	}
	for ( var i = 0; i < items.length; i ++) {
		var mid = items[i].mid
		var msg = '';
		if ( items[i].bt ) {
			var batch = items[i].bt;
			var batchErr = false;
			var totalrq = 0;
			for (j = 0; j < batch.length; j++) {
				var bid = batch[j].bid;
				var btrq = page.find('#rq_' + mid + '_' + j).val();
				if (btrq) {
					msg = validateStockQuantity(btrq, null, false, mid);
					if (msg) {
						page.find('#qtyerrmsg_' + mid + '_' + j).html('<font style="color:red;font-size:8pt;">' + msg + '</font>');
						page.find('#qtyerrmsg_' + mid + '_' + j).attr('style', 'display:block');
						batchErr = true;
					}
					var materialStatus = page.find('#materialstatus_' + mid + '_' + j).val();
					var msgDiv = page.find('#materialstatus_err_'+ mid + '_'+ j);
					if ( !materialStatus &&  page.find('#materialstatus_'+ mid + '_'+ j).is(":visible") && btrq != '' & btrq != '' ) {
						msgDiv.attr('style', 'display:block');
						batchErr = true;
					}
					else {
						msgDiv.attr('style', 'display:none');
						if ( materialStatus )
							batch[j].fmst = materialStatus;
					}
					var mnd = isOrderReasonsMandatory($config, 'fl');
					var othersSelected = '';
					if (btrq != null && btrq != batch[j].q) {
						if (page.find('#flreason_' + mid + '_' + j).length) {
							if (page.find('#flreason_' + mid + '_' + j).val() == $labeltext.others) {
								batch[j].rsnpf = page.find('#flreasonmsg_' + mid + '_' + j).val();
								othersSelected = true;
							}
							else
								batch[j].rsnpf = page.find('#flreason_' + mid + '_' + j).val();
						}
						else if (page.find('#flreasonmsg_' + mid + '_' + j).length){
							batch[j].rsnpf = page.find('#flreasonmsg_' + mid + '_' + j).val();
						}
					}
					var isReasonsVisible = ((page.find('#flreason_' + mid + '_' + j)).is(":visible") || (page.find('#flreasonmsg_' + mid + '_' + j)).is(":visible"));
					if ((othersSelected || mnd === true) && (!batch[j].rsnpf || batch[j].rsnpf == '') && isReasonsVisible ) {
						$('#flreasonerr_' + mid + '_' + j).attr('style', 'display:block');
						batchErr = true;
					}
					else
						$('#flreasonerr_' + mid + '_' + j).attr('style', 'display:none');

					if( !batchErr) {
						if (parseFloat(btrq) > 0)
							batch[j].flq = btrq;
						totalrq += parseFloat(btrq);
						page.find('#qtyerrmsg_' + mid + '_' + j).attr('style', 'display:none');
					}
				}
			}
			if (batchErr)
				isError = true;
			else {
				if (totalrq && totalrq > 0) {
					items[i].flq = totalrq;
				}

			}

		}
		else {
			var rq = page.find('#rq_' + mid).val();
			var rqError = false;
			if ( rq ) {
				msg = validateStockQuantity(rq, null, false, mid);
				if (msg) {
					page.find('#qtyerrmsg_' + mid).html('<font style="color:red;font-size:8pt;">' + msg + '</font>');
					page.find('#qtyerrmsg_' + mid).attr('style', 'display:block');
					rqError = true;
				}
				var materialStatus = page.find('#materialstatus_' + mid).val();
				var msgDiv = page.find('#materialstatus_err_'+ mid);
				if ( !materialStatus &&  page.find('#materialstatus_' + mid).is(":visible") && rq != null & rq != '' ) {
					msgDiv.attr('style', 'display:block');
					rqError = true;
				}
				else
					msgDiv.attr('style', 'display:none');
				if (!rqError) {
					page.find('#qtyerrmsg_' + mid).attr('style', 'display:none');
					if (parseFloat(rq) > 0) {
						items[i].flq = rq;
						if ( materialStatus )
							items[i].fmst = materialStatus;
					}
				}
				if (rqError)
					isError = true;
			}
			var mnd = isOrderReasonsMandatory($config, 'fl');
			var othersSelected;
			if (rq != items[i].q) {
				if (page.find('#flreason_' + mid).length) {
					if (page.find('#flreason_' + mid).val() == $labeltext.others) {
						items[i].rsnpf = page.find('#flreasonmsg_' + mid).val();
						othersSelected = true;
					}
					else
						items[i].rsnpf = page.find('#flreason_' + mid).val();
				}
				else if (page.find('#flreasonmsg_' + mid).length) {
					items[i].rsnpf = page.find('#flreasonmsg_' + mid).val();
				}
				var isReasonsVisible = ((page.find('#flreason_' + mid )).is(":visible") || (page.find('#flreasonmsg_' + mid)).is(":visible"));
				if ((othersSelected || mnd === true) && (!items[i].rsnpf || items[i].rsnpf == '')  && isReasonsVisible ) {
					$('#flreasonerr_' + mid).attr('style', 'display:block');
					isError = true;
				}
				else
					$('#flreasonerr_' + mid).attr('style', 'display:none');

			}
		}


	}
	if ( isError )
		return;
	storeEntity($uid, $entity);
	changeOrderStatus(orderId, orderType,orderStatus, shipId, fflag);
}


// Render order  partial fulfillment  form for orders
	function renderOrderFulfilment( pageId, params, options ) {
		// Get the entity page first
		var page = $( pageId );
		// Get the page header and content
		var header = page.children( ':jqmData(role=header)' );
		var content = page.children( ':jqmData(role=content)' );
		// Update header
		var orderId = '';
		if ( params.oid )
			orderId = params.oid;
		var oType;
		if ( params.oty )
			oType = params.oty;
		var oStatus;
		if ( params.status )
			oStatus = params.status;
		var shipId = '';
		if ( params.sid )
			shipId = params.sid;
		var fflag = null;
		if ( params.fflag )
			fflag = params.fflag;
		// Get the query string for this and back URL
		var queryString = 'oid=' + orderId + ( params.o ?'&o=' + params.o : '' ) + '&oty=' + oType;
		// Get the back button URL
		var backUrl = '#orderinfo?' + queryString;
		var headerTxt = $labeltext.order +  ' ' + orderId;
		header.find( 'h3' ).html( headerTxt );
		// Update the back button
		header.find( '#orderfulfilmentback .ui-btn-text').text($buttontext.back);
		header.find( '#orderfulfilmentback' ).attr( 'href', backUrl );
		// Update header title
		setTitleBar( header, $entity.n, false );
		// Update content
		var markup =  '<h4>' + $labeltext.fulfil + ' ' ;
		if (shipId)
			markup += $labeltext.shipment_lower + ' ' + shipId ;
		else
			markup += $labeltext.order +  '- ' + orderId ;

		markup += '<a style="  margin-top: -5px;float:right" href ="#" onclick="enableOrderReceivedEdit(\'' + orderId + '\',\'' + oType + '\')"  data-role="button" data-mini="true">' + $buttontext.edit + '</a>';
		markup += '</h4> ';

		markup += getOrderItemsFulfilledMarkup ( orderId, oType, oStatus, shipId );
		// Buttons
		var onFulfilledClick = 'changeOrderStatusToFulfilled(\'' + orderId + '\',\'' + oType + '\',\'' + oStatus + '\',\'' + shipId + '\',\'' + fflag + '\' )';
		markup += ' </br> <a id="pfsavebutton" href="#" data-role="button" data-inline="true" data-icon="plus" data-theme="b" data-disabled="true" onClick ="' + onFulfilledClick + ' ">' + $labeltext.fulfil + '</a>';
		markup += '<a id="pfcancelbutton" href="' + backUrl+ '" data-role="button" data-icon="arrow-r" data-inline="true" data-theme="c"  data-direction="reverse" >' + $buttontext.cancel + '</a>';

		// Update content part
		content.empty().append( $( markup )  );
		// Enhance the page
		page.page();
		page.trigger('create'); // this will ensure that all internal components are inited./refreshed
		// Update options data url
		options.dataUrl = '#orderfulfilment?' + queryString ;
		$.mobile.changePage( page, options );
		//page.find('#orderfulfilmentmessage').text(msg);
	}


function getOrderItemsFulfilledMarkup ( orderId, orderType, orderStatus, shipId ) {
	var markup = '';
	var order = getOrder ( orderId, orderType );
	var items;
	if ( !order )
		return;
	var shps;
	if ( shipId ) {
		shps = getOrderShipment(orderId, orderType, shipId);
		items = shps.mt;
	}
	else {
		items = order.mt;
	}

	var reasonType = 'fl';
	markup += '<ul data-role="listview"  data-inset="true" data-theme="c"  >';
	markup += '<li data-role="list-divider">' + items.length + ' ' + $labeltext.items + '</li> ' ;
	for (var i = 0; i < items.length; i++) {
		var item = items[i];
		var material = $materialData.materialmap[item.mid];
		var itemName = $labeltext.unknown;//'Unknown';
		if (material)
			itemName = material.n;
		else {
			itemName = $messagetext.nosuchmaterial;
			markup += '<li>' + itemName;
			continue;
		}
		markup += '<li  data-icon="false" >';
		markup +=   '<font style="font-size:large;font-weight:bold;">' + itemName + '</font>';
		markup += '<br / style="line-height:150%"> <font style="font-weight:normal;color:lightslategray">' + $labeltext.ordered + ': ' + getFormattedNumber(item.q) + '</font>';
		markup +=   '</font>';
		if (item.bt  ) {
			var batchAllocateMarkup = '';
			//batchAllocateMarkup += getOrderBatchAllocatedMarkup(item.bt, item.mid, orderType, orderStatus, false );
			batchAllocateMarkup += getBatchOrderFulfillmentMarkup(item.bt, item.mid, orderType, orderStatus);
			if (batchAllocateMarkup)
				markup += batchAllocateMarkup;
		}
		else {
			var fieldText = $labeltext.received_upper;
			var qty;
			if ( item.flq && item.flq > 0)
				qty = item.flq;
			else
				qty = item.q;
			markup += '<br / style="line-height:150%"><label for=id="rq_' + item.mid + '"><font style="font-size:medium;font-weight:normal;">' + fieldText + ':</font></label>';
			var quantityId = "rq_" + item.mid;
			markup += ' <input type="number" min="0" oninput="maxLengthCheck(\'' + quantityId + '\')" max="1000000" id="' + quantityId + '" value="';
			markup += getFormattedNumber(qty) + '"class="ui-disabled" />';
			var qtyErrMsgId = "qtyerrmsg_" + item.mid;
			markup += '<div id="' + qtyErrMsgId + '" style="display:none"><font style="color:red;font-size:8pt;font-weight:normal;white-space:initial"></font></div>';
			var fieldid = material.mid;
			markup += getMaterialStatusMarkup('er', fieldid, null, material.istmp, false);
			markup += getOrderReasonsMarkup($config, reasonType, material.mid, null, false, false, false);
		}


		markup += '</div>';
		markup += '</li>';

	}
	markup += '</ul>';
	markup += '<label id="fulfilmentdate" for="dar">' + $messagetext.dateofactualreceipt + ':</label>';
	markup += '<a href="#" data-role="button" data-icon="delete" data-iconpos="notext"  style="float: right;margin-top: -.01em;" onclick ="deleteDate(\'#dar\')"></a>';
	markup += '<div style="overflow: hidden; padding-right: .75em;">';
	var today = getStrFromDate(new Date());
	var d = new Date();
	var ardValue = '';
	d.setMonth(d.getMonth() - 3); //set min date 3 months from today
	var shipDate;
	if ( shps && shps.ssht )
		 shipDate = shps.ssht;
	else
		shipDate = order.osht;
	if (shipDate)
		shipDate = getDateWithLocaleFormat(shipDate, $userDateFormat);
	else
		shipDate = '';
	markup += '<input name="fulfilmentdatepicker" class ="mobidatepicker" id="dar" min="'+ shipDate + '" max="' + today + '" style= "margin-top: -.02em;" type="text" onfocus="$(\'#dar_error\').hide()" value="' + ( ardValue ? getDatePickerFormat(decodeURIComponent(ardValue), $userDateFormat) : '' ) + '" />';
	markup += '</div>';
	markup += '<span id="dar_error' + orderId + '" style="display:none;"> <font style="color:red;font-size:small">' + $messagetext.enteractualdate + '</font></span>';
	markup += '</div>';
	markup += getOrderReasonsMarkup($config, reasonType, orderId, null, true, false, true);
    return markup;
}



function enableOrderReceivedEdit ( orderId, orderType ) {
	var page = $('#orderfulfilment');
	var order = getOrder ( orderId, orderType );
	var items = order.mt;
	if (!items)
		return;
	for ( i = 0; i < items.length; i++ ) {
		var id = items[i].mid;
		page.find('input[id^="rq_"]').removeClass('ui-disabled');
		//page.find('div[id^="flreasonsdiv_"]').attr('style', 'display:block');
	}

}





function isAutoPostInventory ( config ){
	var isAPI = false;
	if (!config)
		return;
	if ( config.ords){
		if ( config.ords.api )
			isAPI = true;
	}
	return isAPI;
}




function isReleases (config) {
	if ( config.ords ){
		if ( config.ords.tr ){
			return true;
		}
	}
	return false;
}

function getOrderQuantitesTable ( mid, orderId, orderType, orderStatus, shipId){
	var order = getOrder( orderId, orderType);
	var item = getOrderItem( orderId, orderType, mid);
	var shpItem;
	var orderColor = "lightslategray";
	var shipColor = "lightslategray";
	var fulfillColor = "lightslategray";
	var shippedQty;
	var shipment;
	var shippedLabel = $labeltext.shipped;
	if ( item.q ) {
		if (  item.oq && item.q != item.oq )
			orderColor = $discrepancyColor;
		else if ( item.roq >= 0 && item.q != item.roq)
			orderColor = $discrepancyColor;
	}

	if (shipId) {
		shpItem = getOrderShipmentItem(orderId, orderType, shipId, mid, null);
		shipment = getOrderShipment(orderId, orderType, shipId);
		if (shipment && shipment.st == 'op')
			shippedLabel = $labeltext.ordered;
	}

	var markup = '<div>';
	if ( !shipId ) {
		markup += '<font style="font-size:10pt;font-weight:normal;color:lightslategray">' + $labeltext.ordered + ': </font>';
		if (isOrderEdited(order) && item.nq) { // ensure that red is shown only for edited quantities, i.e those that are different from earlier
			markup += '<font style="font-size:10pt;font-weight:normal;color:red">' + getFormattedNumber(item.nq) + '</font>';
			if (item.q)
				markup += ' (' + '<font style="font-size:10pt;font-weight:normal;color: ' + orderColor + '" >' + +getFormattedNumber(item.q) + '</font>' + ')';
		}
		else
			markup += '<font style="font-size:10pt;font-weight:normal;color:' + orderColor + '" >' + item.q + '</font>';
	}
	if ( item.alq && orderStatus != 'cm' && orderStatus != 'fl' && !shipId) {
		markup += '<font style="font-size:9pt;font-weight:normal;color:lightslategray">, ' + $labeltext.allocated + ': ' + item.alq + '</font>';
	}
	if ( order.shps ) {
		if ( !shipId  &&  (order.ost == 'cm' || order.ost == 'fl' || order.ost == 'bo')) {
			shippedQty = getShippedQty(orderId, orderType, mid);
			if (shippedQty && shippedQty != item.oq && order.ost != 'bo')
				shipColor = $discrepancyColor;
			markup += '<font style="font-size:9pt;font-weight:normal;color:lightslategray">, </font>';
		}
		else {
			if ( shpItem ) {
				shippedQty = shpItem.q;
				shipColor = "black";
			}
		}
		if (shippedQty ) {
			markup += '<font style="font-size:9pt;font-weight:normal;color:lightslategray"> ' + shippedLabel + ': </font>'
			markup += '<font style="font-size:10pt;font-weight:normal;color:' + shipColor + '">' + shippedQty + '</font>';
			var fulfillQty ;
			if (!shipId )
				fulfillQty = item.flq;
			else {
				if ( shpItem && shpItem.flq)
					fulfillQty = shpItem.flq;
			}
			if ( (fulfillQty != shippedQty) &&  orderStatus == 'fl')
				fulfillColor = $discrepancyColor;
			var shps = getShipmentsForMaterial(orderId, orderType, mid);
			var showFulfilled = false;
			if (shps)
				showFulfilled = isAnyShipmentFulfilled(shps)
			if (fulfillQty != null && showFulfilled) {
				markup += '<font style="font-size:9pt;font-weight:normal;color:lightslategray">, ' + $labeltext.fulfilled + ': </font>';
				markup += '<font style="font-size:10pt;font-weight:normal;color:' + fulfillColor + '">' + fulfillQty + '</font>';
			}
		}
	}
	markup += '</div>';
	return markup;

}
function checkOrderAllocationData ( mid, orderId, oType, nextUrl ) {
	var page = $('#orderallocate');
	var order = getOrder( orderId, oType);
	var orderItem = getOrderItem( orderId, oType, mid);
	// Get all the quantity field nodes
	var quantityFields = page.find('input[id^="quantity_"]');
	if (!quantityFields)
		return false;
	var material = null;
	if ( oType == 'prc') {
		var lkid = order.vid;
		if (lkid)
		 material = getRelatedEntityMaterial(lkid, mid)
	}
	else {
		material = $materialData.materialmap[mid];
	}
	if ( material == null ) {
		return;
	}
	var hasErrors = false;
	if (material.bt) {//batch
		for (var i = 0; i < quantityFields.length; i++) {
			var bIndex = $(quantityFields[i]).attr('bindex');
			var batchStock =   material.bt[i].q;
			if ( material.bt[i].avq != null) {
				var prevAlloc = 0;
				if (orderItem.bt)
				for (var j=0 ; j< orderItem.bt.length ; j++) {
					if ( orderItem.bt[j].bid == material.bt[i].bid) {
						if (orderItem.bt[j].alq)
							prevAlloc = orderItem.bt[j].alq;
					}
				}
				batchStock = parseFloat(material.bt[i].avq) + parseFloat(prevAlloc) ;
			}
			var isError = !validateStockQuantityOnBlur(batchStock, quantityFields[i].id, 'quantity_error_'+ bIndex + '_'+ orderId, mid);
			var materialStatus = page.find('#materialstatus_' + bIndex + '_' + orderId).val();
			var qtyVal = $( '#' + quantityFields[i].id).val();
			if ( !materialStatus &&  page.find('#materialstatus_'+ bIndex + '_'+ orderId).is(":visible") && qtyVal != '' & qtyVal != 0 ) {
				var msgDiv = page.find('#materialstatus_err_'+ bIndex + '_'+ orderId);
				msgDiv.attr('style', 'display:block');
				isError = true;
			}
			if (isError)
				return;
		}
		var okButtonCallBackFunction = function () {
			saveOrderAllocation(mid, orderId, oType, nextUrl);
		};
		if (!isOrderEEFOValid(page, orderId)) {
			showPopupDialog($messagetext.warning, $messagetext.fefomessage, okButtonCallBackFunction, null, null, "popupfefo", false);
		}
		else {
			saveOrderAllocation(mid, orderId, oType, nextUrl);
		}
	}
	else { //no batch
		var qty = page.find('input[id^="quantity_"]').val();
		var errorNode = page.find('#qtyerrmsg_'+mid);
		var stockQty = material.q;
		if ( material.avq != null) {
			var prevAlloc = 0
			if ( orderItem.alq )
				prevAlloc = orderItem.alq;
			if (isNaN(prevAlloc))
				prevAlloc = 0;
			stockQty = parseFloat(material.avq) + parseFloat(prevAlloc);
		}
		var errMsg = validateStockQuantity(qty, stockQty, true, mid);
		var isError = false;
		if ( errMsg ) {
			errorNode.attr( 'style', 'display:block' );
			errorNode.html( '<font style="color:red;font-size:small;">' + errMsg + '</font>' );
			isError = true;
		}
		var materialStatus = page.find('#materialstatus_'+ mid).val();
		if ( !materialStatus &&  page.find('#materialstatus_' + mid).is(":visible") && qty != '') {
			var msgDiv = page.find('#materialstatus_err_' + mid);
			msgDiv.attr('style', 'display:block');
			isError = true;
		}
		if (isError)
			return;
		else
			saveOrderAllocation(mid, orderId, oType, nextUrl);
	}



}

function saveOrderAllocation( mid, orderId, oType, nextUrl) {
	// batch enabled inventory operation with multiple batch adds/edits
	var page = $('#orderallocate');
	var batches = [];
	var totalq = 0;
	var quantityFields = page.find('input[id^="quantity_"]');
	var orderItem = getOrderItem( orderId, oType, mid );
	var material = null;
	var order = getOrder(orderId, oType);
	if ( oType == 'prc') {
		var lkid = order.vid;
		if (lkid)
			material = getRelatedEntityMaterial(lkid, mid)
	}
	else {
		material = $materialData.materialmap[mid];
	}
	if ( !material )
		return;
	var qty = parseFloat(material.q);
	if ( material.avq ) {
		qty = parseFloat(material.avq);
	}
	if (material.bt) { //batch
		for (var i = 0; i < quantityFields.length; i++) {
			var quantity = $(quantityFields[i]).val().trim();
			var bIndex = $(quantityFields[i]).attr('bindex');
			if (quantity == '')
				continue;
			var bid = page.find('#bid_' + bIndex + '_' + orderId).val();
			var bexp = page.find('#bexp_' + bIndex + '_' + orderId).val();
			var bmfnm = page.find('#bmfnm_' + bIndex + '_' + orderId).val();
			var bmfdt = page.find('#bmfdt_' + bIndex + '_' + orderId).val();
			var nmst = page.find('#materialstatus_' + bIndex + '_' + orderId).val();
			var bavq = material.bt[i].avq;
			var balq = null;
			var bmst = null;
			if (orderItem.bt) {
				for (var j = 0; j < orderItem.bt.length; j++) {
					var bt = orderItem.bt[j];
					if ( bt.bid == bid) {
						balq = bt.alq;
						if (bt.mst)
							bmst = bt.mst;
						break;
					}

				}
			}
			var batch = {
				bid: bid,
				bexp: bexp,
				bmfnm: bmfnm,
				q: bavq,
				alq: balq,
				nalq: quantity
			};
			if ( !quantity  )
			 batch.mst = '';
			if (bmfdt)
				batch.bmfdt = bmfdt;
			if (bmst)
				batch.mst = bmst;
			if (nmst)
				batch.nmst = nmst;
			// Add to list
			batches.push(batch);
			// Increment total quantity count
			if (quantity)
				totalq += parseFloat(quantity);

		}

	}
	else { // no batches
		var qty = page.find('input[id^="quantity_"]').val();
		var materialStatus = page.find('#materialstatus_'+ mid ).val();
		if (qty == 0)
			materialStatus = '';
		totalq = qty;
	}
	if (orderItem) {
		var oq;
		if ( orderItem.nq )
			oq = orderItem.nq;
		else
			oq = orderItem.q;
		totalq = parseFloat(totalq);
		if (totalq > oq) {
			var msg =  $messagetext.allocationscannotbegreaterthanordered;
			showPopupDialog($messagetext.warning, msg, null, null, null, "poupqtyorderalloc", true);
		}
		else {

			updateOrderAllocationQty( orderId, oType, mid, totalq, batches, materialStatus, nextUrl);
		}
	}
	else {

		saveOrderPage(nextUrl);
	}
}



function isQtyAllocatedForMaterial ( orderItem, batchMaterial ) {
	if (!orderItem)
		return false;
	if ( orderItem.q == 0 )
	 	return true;
	if ( batchMaterial ){
		if (orderItem.bt) {
			for (var i = 0; i < orderItem.bt.length; i++)
				if (orderItem.bt[i].alq || orderItem.bt[i].nalq)
					return true;
		}

	}
	else {
		if ( (orderItem.alq && orderItem.alq != 0) || (orderItem.nalq && orderItem.nalq != 0)  )
			return true;
	}
	return false;
}

function updateOrderAllocationQty( orderId, oType, mid, newOq, batches,mst, nextUrl) {
	var orderItem = getOrderItem(orderId, oType, mid);
	if ( orderItem && newOq != null ) {
		//orderItem.alq = newOq;
		orderItem.nalq = newOq;
		if (orderItem.nalq == 0)
			orderItem.nmst = '';
		if ( mst )
			orderItem.nmst = mst;
		if ( batches && batches.length > 0 ) {
				orderItem.bt = batches;
		}
	}
	 var order = getOrder(orderId, oType);
	// Mark the dirty quantity flag
	if ( order ) {
		if (!order.dirty)
			order.dirty = {};
		//order.dirty.alq ; // implies allocation edited
		if (!order.dirty.alq)
			 order.dirty.alq =[];
		if ( $.inArray(mid,order.dirty.alq) == -1 )
			order.dirty.alq.push(mid);

	}
     saveOrderPage(nextUrl)
}



function getOrderBatchAllocatedMarkup ( batchOrderItem, mid , orderType, orderStatus, viewOnly ) {
	var markup = '';
	var item = batchOrderItem;
	if (!batchOrderItem)
		return markup;
	var tableHeaderDisplayed = false;
	var q;
	var qtyLabel;
	var alqColor = "black";
	for ( var j= 0; j < item.length; j++ ) {
		if ( item[j].nalq != null) {
			q = item[j].nalq;
			qtyLabel = $labeltext.allocated;
			alqColor = "black";
		}
		else if ( item[j].alq != null) {
			q = item[j].alq;
			qtyLabel = $labeltext.allocated;
			alqColor = "black";
		}
		else  {
			q = item[j].q;
			qtyLabel = $labeltext.shipped;
			alqColor = "black";
		}

		if (q > 0) {
			if ( !tableHeaderDisplayed ) {
				markup += '<table id = "batchallocationtable_' + mid + '" data-role="table" data-mode="reflow" class="infotable" width="100%"><tbody>';
				markup += '<tr><th><font style="font-size:9pt;font-weight:normal;">' + $labeltext.batch + '</font></th>';
				markup += '<th><font style="font-size:9pt;font-weight:normal;">' + qtyLabel + '</font></th>';
				if ( orderStatus == 'fl') {
					markup += '<th><font style="font-size:9pt;font-weight:normal;">' + $labeltext.received_upper + '</font></th></tr>';
				}
				tableHeaderDisplayed = true;
			}
			markup += '<tr><td><font style="font-size:9pt;font-weight:normal;">' +item[j].bid + '  </font></br><font style="font-size:7pt;font-weight:normal;">'+  item[j].bexp + ' </font></td> ';
			var materialStatus = '';
			if ( item[j].nmst ){
				materialStatus = '</br>' + item[j].nmst + '';
			}
			else if ( item[j].mst ){
				materialStatus = '</br>' + item[j].mst + '';
			}
			markup += '<td id= "talq_' + j + '"><font style="font-size:9pt;font-weight:normal;color:' + alqColor + '">' + q + '   </font><font style="font-size:7pt;font-weight:normal;">'+  materialStatus + ' </font></td> ';

			if ( orderStatus == 'fl') {
				if (!viewOnly) {
					var receivedId = 'rq_' + mid + '_' + j;
					markup += '<td id= "trq_' + mid + '_' + j + '"> <input type="number" min="0"  oninput="maxLengthCheck(\'' + receivedId + '\')" max="1000000" id="' + receivedId + '" value="';
					markup += getFormattedNumber(q) + '" />';
					var qtyErrMsgId = "qtyerrmsg_" + mid + '_' + j;
					markup += '<div id="' + qtyErrMsgId + '" style="display:none"><font style="color:red;font-size:8pt;font-weight:normal;white-space:initial"></font></div>';
					var material = $materialData.materialmap[mid];
					var fieldid = mid + '_' + j;
					markup += getMaterialStatusMarkup('er', fieldid, null, material.istmp, false);
					markup += '</td>';
				}
				else {
					var qty = '';
					var txt = '';
					if (item[j].flq != null) {
						qty = item[j].flq;
						if (item[j].fmst)
							txt += '</br>' + item[j].fmst;
						markup += '<td id= "trq_' + j + '"><font style="font-size:9pt;font-weight:normal;">' + qty + '  </font><font style="font-size:7pt;font-weight:normal;">' + txt + ' </font></td> ';

					}
				}
			}

			markup += '</tr> ';

			if ( orderStatus == 'fl' && item[j].flq != null && item[j].rsnpf) {
				markup += '<tr><td colspan=3><font style="font-size:9pt;font-weight:normal;">' + item[j].rsnpf + '  </font></td></tr>';
			}

		}
	}
	if ( tableHeaderDisplayed )
		markup += '</tbody></table>';
	return markup;
}




function getBatchOrderFulfillmentMarkup(batchOrderItem, mid , orderType, orderStatus ){
	var markup = '';
	var item = batchOrderItem;
	if (!batchOrderItem)
		return markup;
	var q;
	var qtyLabel;
	var reasonType = 'fl';
	markup += '<div class="ui-grid-a">'
	markup += '<div class="ui-block-a" style= "border-bottom: 1px solid lightgray;"><font style="font-size:9pt;font-weight:normal;">'+ $labeltext.batch +'</font></div>';
	//markup += '<div class="ui-block-b"><font style="font-size:9pt;font-weight:normal;">' + $labeltext.shipped  + '</font></div>';
	markup += '<div class="ui-block-b" style= "border-bottom: 1px solid lightgray;"><font style="font-size:9pt;font-weight:normal;">' + $labeltext.received_upper  + '</font></div>';
	for ( var j= 0; j < item.length; j++ ) {
		 markup += '<div class="ui-block-a" ><font style="font-size:9pt;font-weight:normal;">'+ item[j].bid + '</font></br><font style="font-size:7pt;font-weight:normal;">'+  item[j].bexp + ' </font> ' ;
		markup += '</br><font style="font-size:9pt;font-weight:normal;">' + $labeltext.shipped + ':' + item[j].q + '</font></div>';
		var receivedId = 'rq_'+ mid + '_' + j;
		markup += '<div class="ui-block-b"><font style="font-size:9pt;font-weight:normal;">';
		markup += '<input type="number" min="0" class = "ui-disabled" oninput="maxLengthCheck(\'' + receivedId + '\')" max="1000000" id="' + receivedId  + '" value="';
		markup += getFormattedNumber(item[j].q) + '" /></font>';
		var qtyErrMsgId = "qtyerrmsg_" + mid + '_' + j;
		markup += '<div id="' + qtyErrMsgId + '" style="display:none"><font style="color:red;font-size:8pt;font-weight:normal;white-space:initial"></font></div>';
		var material = $materialData.materialmap[mid];
		var fieldid = mid + '_' + j;
		markup += '<font style="font-size:9pt;font-weight:normal;">' + getMaterialStatusMarkup('er',fieldid, null, material.istmp, false) + '</font>';
		markup += getOrderReasonsMarkup($config, reasonType, fieldid , null, false, false, false);
		markup += '</div>';
		markup += '<div class="ui-block-a" style= "border-bottom: 1px solid lightgray;"></br></div>';
		markup += '<div class="ui-block-b" style= "border-bottom: 1px solid lightgray;"></br></div>';
	}
	markup+= '</div>';
	return markup;
}

function confirmClearOrderAllocation ( orderId, orderType, mid, nextUrl ){
	//showPopupDialog ($labeltext.confirm,$messagetext.doyouwanttoclearbatchallocation ,function(){clearOrderAllocation( orderId, orderType , mid);},null,null,"popupclearbatchallocation", null);
	showConfirm( $labeltext.confirm, $messagetext.doyouwanttoclearbatchallocation,null,
		function(){clearOrderAllocation( orderId, orderType , mid);},nextUrl );
}

function clearOrderAllocation ( orderId, orderType, mid ){
  var orderItem;
	orderItem = getOrderItem( orderId, orderType, mid);
	var order = getOrder ( orderId, orderType )
	if (orderItem) {
		if (orderItem.bt) {
			delete orderItem.bt;
			orderItem.alq = 0;
		}
		else
			orderItem.alq = 0;
		// Mark the dirty quantity flag
		if ( order ) {
			if (!order.dirty)
				order.dirty = {};
			if ( !order.dirty.alq )
				order.dirty.alq = []
			if ( $.inArray( mid, order.dirty.alq) == -1 )
				order.dirty.alq.push(mid);// implies allocated quantities edited
		}


	}

	$('#orderinfo').find('#batchallocationtable_'+mid).remove();
	$('#orderinfo').find('#allocatebutton_'+mid).removeClass("ui-disabled");
	//$('#orderinfo').find('#deallocatebutton_'+mid).addClass("ui-disabled");
	$('#orderinfo').find('#q_'+mid).removeClass("ui-disabled");
	$('#orderinfo').find('#alloc_'+mid).html( '<font style="color:red;font-size:small;font-weight:normal">' + $labeltext.allocated +': 0</font>');
}

// Get the color code for the stock quantity
function getStockColor( material ) {
	var q = parseFloat( material.q );
	var min = ( material.min ? parseFloat( material.min ) : undefined );
	var max = ( material.max ? parseFloat( material.max ) : undefined );
	return ( q == 0 ? 'red' : ( ( min && q <= min ) ? 'orange' : ( ( max && q > max ) ? 'violet' : 'black' ) ) );
}

// Get the class name to apply for stock coloring
function getEventStyleClass( material ) {
    var q = parseFloat( material.q );
    var min = ( material.min ? parseFloat( material.min ) : undefined );
    var max = ( material.max ? parseFloat( material.max ) : undefined );
    return ( q == 0 ? 'inv-so' : ( ( min && q <= min ) ? 'inv-min' : ( ( max && q >= max ) ? 'inv-max' : '' ) ) );
}

// Get the form markup to be shown for data collection
// NOTE: oid and otype are optional, and passed only when adding to order, otherwise undefined
function getDataEntryFormMarkup( mid, backUrl, refreshBatchInfo) {
	var value = '';
	var atdValue = '';
	var localData = getLocalModifications( mid, $currentOperation, $entity );
	var reason;
	if ( localData != null ) {
		value = localData.q;
		if (localData.atd)
			atdValue = localData.atd;
		if (localData.reason)
			reason = localData.reason;
	}
	var material = $materialData.materialmap[ mid ];
	//Check if actual  transaction date is required
	var valueCheck = (!material.ben && !value)||(material.ben && $currentOperation != 'es' && $currentOperation != 'er' && !value)|| (material.ben &&
		($currentOperation == 'es' || $currentOperation == 'er') && (isNewBatchAdded(material)|| !value));
	var atdRequired = getCaptureActualTransactionDate($currentOperation,$config)
	if (!atdValue && atdRequired > 0 && valueCheck)
		atdValue = getLastEnteredActualTransactionDate($currentOperation, $entity);

	//show economic order quantity
	var eoq;
	if ((($currentOperation == 'no') || ($currentOperation == 'ao')) ){
		var lkid = null;
		if  ($newOrderType == 'sle') {
			var relatedEntity = getOrderRelatedEntity($currentOperation, $newOrderType);
			if ( relatedEntity)
				lkid = relatedEntity.kid;
			if (lkid)
				eoq = getEoq(mid, lkid, $newOrderType);
		}
		else
			eoq = getEoq(mid, lkid, $newOrderType);

	   if (eoq >= 0) {
	     var eoqstr =$labeltext.recommendedquantity+': '+eoq;
	     if ( value == '' )
	       value = eoq;
	   }
	   
	}
	// Show the quantity form field
	///var markup = '<div id="formpanel" class="ui-body ui-body-b"><form id="dataform">';
	var markup = '<div id="formpanel" class="ui-body-d ui-corner-all ui-shadow" style="padding:0.75em;margin-top:3%">';
	markup += '<form id="dataform">';
	//For batch management do not show qty field
	if (!material.ben  || $currentOperation == 'no' || $currentOperation == 'ao') {
		// Quantity field
		markup += '<label for="quantity">' + getDataEntryText(null) + '</label>';
		var min = '1';
		if ($currentOperation == 'es') // allow zero entry for stock count
			min = '0';
		markup += '<input type="number" name="quantity" id="quantity" min="' + min + '" max="1000000" class ="inputfieldchange" autofocus="autofocus" value="' + value + '"';
		var placeholder = getFormDataEntryTip($currentOperation);
		if (placeholder && placeholder != null)
			markup += ' placeholder="' + placeholder + '"';

		markup += '/>';
		markup += '<div id="quantityerrormessage" style="display:none"></div>';
		markup += '<div id="stockAfterOperation" style="display:none"></div>';
	}
	if ((eoq >= 0) )
	{
	  	markup +=  '<div style="font-size:10pt;font-weight:normal;color:orange">' + eoqstr + '.';
		if (reason)
			markup += '<br/><font style="color:black;font-size:8pt;">('+reason+')</font>';
		markup += '</div>';
	  	markup += '<input type="hidden" name="eoq" id="eoq" value="'+eoq+'"/>';
	}
	var batches = getBatchList(material);
	var noOfBatches = ( batches ? batches.length : 0 );
	// If material is batch enabled, show the batch details
	var pageOffset = 0;
	var params = getURLParams(backUrl);
	if (params.o)
	  pageOffset = params.o;
	if ( material.ben && !isOperationOrder( $currentOperation ) )
		markup += getBatchListViewMarkup( material, $currentOperation ,pageOffset);
	var showDropdowns =  ( (!material.ben ) || (( material.ben ) && ((noOfBatches > 0 &&  $currentOperation != 'er' ) || ( $currentOperation == 'er' && isNewBatchAdded(material)))))
	if (atdRequired && atdRequired > 0 && !isOperationOrder($currentOperation) && showDropdowns) {
		var enterATDText = '';
		enterATDText = getATDText($currentOperation);
		markup += '<label id="actualTransactionDatelabel" for="actualTransactionDate">' + enterATDText + ':</label>';
		markup += '<a href="#" data-role="button" data-icon="delete" data-iconpos="notext"  style="float: right;margin-top: -.01em;" onclick ="deleteDate(\'#actualTransactionDate\')"></a>';
		markup += '<div style="overflow: hidden; padding-right: .75em;">';
		var today = getStrFromDate(new Date());
		var d = new Date();
		d.setMonth(d.getMonth() - 3);
		var minDate =  getStrFromDate(d);
		markup += '<input name="actualTransactionDate" id="actualTransactionDate" min="' + minDate + '" max="' + today + '" style= "margin-top: -.02em;" type="text" onfocus="$(\'#actualTransactionDate_error\').hide()" value="' + ( atdValue ? getDatePickerFormat(decodeURIComponent(atdValue), $userDateFormat) : '' ) + '" />';
		markup += '</div>';
		markup += '<span id="actualTransactionDate_error" style="display:none;color:red;font-size:8pt">' + $messagetext.enteractualdate + '</span>';
	}
	// Check if there are reason codes
	var reasons = null;
	if (material.tg)
		reasons = getReasonsByTag($currentOperation,$config,$mtag);
	if (!reasons)
		reasons = getReasons( $currentOperation, $config );
	// Check if there are material status
	var materialStatus;
	if (!material.ben ) {
		if (localData)
			materialStatus = localData.mst;
		markup += getMaterialStatusMarkup($currentOperation, material.mid, materialStatus, material.istmp, true);
	}
	// Reason field
	 if (reasons != null && showDropdowns ) {
		 markup += '<label id="reasonlabel" for="reason">' + $labeltext.reasons + ':</label>';
		 markup += '<select name="reason" id="reason"  class ="inputfieldchange" data-theme="c" data-mini="true">';
		 markup += '<option value="">'+$messagetext.selectreason+'</option>';
		for (var i = 0; i < reasons.length; i++) {
			var selected = '';
			if (localData != null && localData.reason && localData.reason == reasons[i])
				selected = 'selected';
			markup += '<option value="' + reasons[i].replace(/"/g, '&quot;') + '" ' + selected + '>' + reasons[i] + '</option>';
		}
		 markup += '</select>';
	 }
	if (eoq >= 0)  {
		var reason = null;
		if (localData != null)
			reason = localData.reason;
		//markup += getIgnoreRecommendedOrderReasonsMarkup(reason, null);
		var reasonType = 'ir';
		markup += getOrderReasonsMarkup ( $config, reasonType, material.mid, reason, false, false, true);
	}
	markup += '<input type="hidden" id="fieldchange" value="" />';
	// Save/cancel button
	markup += '<a id="datasavebutton" href="#" data-role="button" data-inline="true" data-icon="plus" data-theme="b" onclick="checkFormData(\'' + mid + '\',\'' + backUrl + '\')">' + $buttontext.save + '</a>';
	markup += '<a id="datacancelbutton" href="' + backUrl + '" data-role="button" data-icon="delete" data-inline="true" data-theme="c" data-direction="reverse" class="checkbuttonclick">' + $buttontext.cancel + '</a>';
	///markup += '<a id="dataclearbutton" href="#" data-role="button" data-icon="delete" data-inline="true" data-theme="c" onclick="clearFormData(\'' + mid + '\',\'' + backUrl + '\')">'+$buttontext.clear+'</a>';
  	// End the form/div
	markup += '</form></div>';
	return markup;
}

function getAddBatchDataMarkup( material, backUrl, params ) {
	var markup = '<div id="batchmetadatadiv" class="ui-body-d ui-corner-all ui-shadow" style="padding:0.75em;margin-top:2%">';
	markup += '<label for="batchNumber">' + $messagetext.enterbatchnumber + '*:</label>';
	markup += '<input type="text" name="batchNumber" id="batchNumber" class ="inputfieldchange" placeholder="'+$messagetext.enterbatchnumber+'" value="' + ( params.bid ? decodeURIComponent( params.bid ) : '' ) + '"';
	markup += ' onfocus="removeFieldHighlight($(this));$(\'#batchNumber_error\').hide()" onblur="validateBatchNumberOnBlur(\'' + material.mid + '\',this)" />';
	markup += '<span id="batchNumber_error" style="display:none;color:red;font-size:8pt">' + $messagetext.enterbatchnumber + '</span>';
	var today = getStrFromDate(new Date());
	var expiryDateLimit = new Date();
	expiryDateLimit.setDate(expiryDateLimit.getDate());
	var sexpiryDateLimit = getStrFromDate(expiryDateLimit);
	markup += '<label for="manufacturedDate"> '+$messagetext.enterbatchmanufactureddate+':</label>';
	markup += '<input name="manufacturedDate" id="manufacturedDate" type="text" class ="inputfieldchange" max="'+today+'" value="' + ( params.bmfdt ? getDatePickerFormat( decodeURIComponent(params.bmfdt) , $batchDateFormat) : '' ) + '"/>';
	markup += '<span id="manufacturedDate_error" style="display:none;color:red;font-size:8pt" />';
	markup += '<label for="manufacturerName"> '+$messagetext.entermanufacturername + '*:</label>';
	markup += '<input type="text" name="manufacturerName" id="manufacturerName"  class ="inputfieldchange" placeholder="'+$messagetext.entermanufacturername+'" value="' + ( params.bmfnm ? decodeURIComponent(params.bmfnm) : '' ) + '"';
	markup += ' onfocus="removeFieldHighlight($(this));;$(\'#manufacturerName_error\').hide()"';
	markup += '/>';
	markup += '<span id="manufacturerName_error" style="display:none;color:red;font-size:8pt">' + $messagetext.entermanufacturername + '</span>';
	markup += '<label for="batchExpiryDate"> '+$messagetext.enterbatchexpirydate+'*:</label>';
	markup += '<input name="batchExpiryDate" id="batchExpiryDate" type="text" class ="inputfieldchange" min="'+sexpiryDateLimit+'" onfocus="$(\'#batchExpiryDate_error\').hide()" value="' + ( params.bexp ? getDatePickerFormat( decodeURIComponent( params.bexp ), $batchDateFormat ) : '' ) + '" />';
	markup += '<span id="batchExpiryDate_error" style="display:none;color:red;font-size:8pt">' + $messagetext.enterbatchexpirydate + '</span>';

	// Quantity field
	markup += '<label for="quantity">' + getDataEntryText(null) + '*:</label>';
	var min = '1';
	if ($currentOperation == 'es') // allow zero entry for stock count
		min = '0';
	var value = ( params.q ? params.q : '' );
	markup += '<input type="number" name="quantity" id="quantity" min="' + min + '" max="1000000"  class ="inputfieldchange" value="' + value + '"';
    markup += ' onfocus="removeFieldHighlight($(this));$(\'#quantity_error\').hide()"';
	var placeholder = getFormDataEntryTip($currentOperation);
	if (placeholder && placeholder != null)
		markup += ' placeholder="' + placeholder + '"';
	markup += '/>';
	markup += '<span id="quantity_error" style="display:none;color:red;font-size:8pt;white-space:initial ">' + placeholder + '</span></br>';
	var materialStatus;
	if ( params.mst )
		materialStatus = decodeURIComponent(params.mst);
	markup += getMaterialStatusMarkup($currentOperation, 'new', materialStatus , material.istmp, true);
	// Buttons
	markup += '<a id="batchdatasavebutton" href="#" data-role="button" data-mini="true" data-inline="true" data-icon="plus" data-theme="b" data-disabled="true" onclick="saveBatchFormData(\'' + material.mid + '\',\'' + backUrl + '\')">' + $buttontext.save + '</a>';
	markup += '<a id="batchdatacancelbutton" href="' + backUrl + '" data-role="button" data-mini="true" data-icon="delete" data-inline="true" data-theme="c" class="checkbuttonclick" data-direction="reverse">' + $buttontext.cancel + '</a>';
	// Provide a delete button to remove the newly entered batch
	if ( params.bid && params.bid != '' ) {
		markup += '<a style="text-align:right;" id="batchdatadeletebutton" data-role="button" data-mini="true" data-icon="delete" data-inline="true" data-theme="c" data-direction="reverse"' +
			' onclick="deleteBatchItem(\'' + material.mid + '\',\'' + params.bid + '\',\'' + backUrl + '\')" >' + $buttontext.delete + '</a>';
	}
	markup += '</div>';
	markup += '<input type="hidden" id="fieldchange" value="" />';
	return markup;
}


function getBatchListViewMarkup( material, op, pageOffset ) {
	var markup = '';
	// Enable adding new batch, if required
	var allowNewBatch = ( op == 'er' || op == 'es' );
	if ( allowNewBatch ) {
		var addBatchUrl = '#batchinfo?o='+pageOffset+'&mid=' + material.mid + ( material.tag ? '&tag=' + material.tag : '' ); // TODO: include material listing page offset here
		markup += '<a id="addbatchbutton" href="' + addBatchUrl + '" data-role="button"  class ="checkbuttonclick" data-mini="true" data-inline="true" data-icon="plus" data-theme="b">' +
			$buttontext.addnewbatch + '</a>';
	}
	// Get locally modified batches, if any
	var batches = getBatchList(material);
	var noOfBatches = ( batches ? batches.length : 0 );
	// Render the batch list
	markup += '<div id="batchpanel" data-role="collapsible" data-theme="c" data-content-theme="d" data-collapsed="false">';
	markup += '<h3>' + $labeltext.batches + '</h3>';
	if ( noOfBatches == 0 ) {
		markup += '<p>' + $messagetext.nobatchesavailable + '</p>';
	} else {
		markup += '<ul id="batchlist" data-role="listview" data-theme="d">';
		var isWriteOp = !isOperationReadOnly(op);
		for (var i = 0; i < noOfBatches; i++) {
			var batch = batches[i];
			var rowId = 'row_' + i;
			var stockFontColor = ( batch.new ? 'red' : 'black' );
			var anchorLinkMarkup;
			if (batch.new) {
				var url = '#batchinfo?o='+pageOffset+'&mid=' + material.mid + '&bid=' + encodeURIComponent( batch.bid ) + '&bexp=' + encodeURIComponent( batch.bexp ) + '&bmfnm=' + encodeURIComponent( batch.bmfnm ) + '&bmfdt=' + ( batch.bmfdt ? encodeURIComponent( batch.bmfdt ) : '' ) + '&q=' + batch.q + '&mst=' + encodeURIComponent(batch.mst); // TODO: material listing offset and tag are not passed here yet
				anchorLinkMarkup = '<a href="' + url + '">';
			} else {
				anchorLinkMarkup = '<a onclick="$(\'#' + rowId + '_details\').show()">';
			}
			var batchq = batch.q;
			markup += '<li id="' + rowId + '">' + anchorLinkMarkup + '<h3>' + batch.bid +'<span style="font-size:8pt;float:right;border-radius:25px; border:2px solid #D3D3D3;padding:5px;color:' + stockFontColor + '">' + batch.q + '</span></h3>';
			//markup += '<li id="' + rowId + '">' + anchorLinkMarkup + '<h3>' + batch.bid + '</h3><span class="ui-li-count" style="color:' + stockFontColor + '">' + batch.q + '</span>';
			markup += '<p style="font-size:9pt">';
			if (batch.avq != null && isAutoPostInventory($config)) {
				batchq = batch.avq;
				markup += $labeltext.available + ': ' + batch.avq + ', ';
				if (batch.alq != null)
					markup += $labeltext.allocated + ': ' + batch.alq;
				markup += '</br>';
			}
			markup +=  $labeltext.batchexpirydate + ': ' + batch.bexp ;
			if ( isWriteOp ) {
				if ( batch.new ) {
					markup += '<br/><span style="color:red">(' + $labeltext.newtext + ')</span>';
				} else if ( op != 'er' ) { // an update or possibly no change at all (batch.mq represents modified quantity; otherwise, there's no change)
					var quantityId = 'quantity_' + i;
					var batchStockId = 'batchstock_' + i;
					var batchNumberId = 'bid_' + i;
					var batchExpiryId = 'bexp_' + i;
					var batchManuNameId = 'bmfnm_' + i;
					var batchManuDateId = 'bmfdt_' + i;
					var batchAvlStockId = 'batchavlstock_' + i ;
					var batchAlqStockId = 'batchalqstock_' + i ;
					var errorId = 'quantity_error_' + i;
					var min = '1';
					if ($currentOperation == 'es') // allow zero entry for stock count
						min = '0';
					markup += '<br/><input  type="number" id="' + quantityId + '" min="' + min + '" max="1000000"  class ="inputfieldchange" placeholder="' +
						$operations[op] + ' " value= "' + ( batch.mq ? batch.mq : '' ) + '"' +
						' oninput="maxLengthCheck(\'' + quantityId + '\')"' +
						' onblur="validateStockQuantityOnBlur(\'' + batchq + '\',\'' + quantityId + '\',\'' + errorId + '\',\'' + material.mid + '\')"' +
						' onfocus="resetErrorHighlightOnFocus(\'' + quantityId + '\',\'' + errorId + '\')" bindex="' + i + '" />';
					markup += '<span id="' + errorId + '" style="display:none;font-size:8pt;color:red;white-space: initial" /><br/>';

					// Add hidden fields for the metadata
					markup += '<input type="hidden" id="' + batchStockId + '" value="' + batch.q + '" />';
					markup += '<input type="hidden" id="' + batchNumberId  + '" value="' + batch.bid + '" />';
					markup += '<input type="hidden" id="' + batchExpiryId  + '" value="' + batch.bexp + '" />';
					markup += '<input type="hidden" id="' + batchManuNameId  + '" value="' + batch.bmfnm + '" />';
					if ( batch.bmfdt )
						markup += '<input type="hidden" id="' + batchManuDateId  + '" value="' + batch.bmfdt + '" />';
					// Mark as updated, if so
					if (batch.mq)
						markup += '<span style="color:red">(' + $labeltext.updated + ')</span>';
					if (batch.avq)
						markup += '<input type="hidden" id="' + batchAvlStockId + '" value="' + batch.avq + '" />'
				}
			}
			markup += '<div id="' + rowId + '_details" style="display:none;font-size:9pt;font-weight:normal;margin-top:-3px">';
			markup += $labeltext.manufacturedby + ': ' + batch.bmfnm;
			if (batch.bmfdt)
				markup += '<br/>' + $labeltext.batchmanufactureddate + ': ' + batch.bmfdt;
			if (batch.t)
				markup += '<br/><font style="font-size:8pt;font-style:italic;margin-top:2%">' + $labeltext.lastupdated + ': ' + batch.t + '</font>';
				//markup += '<div style="font-size:8pt;font-style:italic;margin-top:2%">' + $labeltext.lastupdated + ': ' + batch.t + '</div>';
			markup += '</div>';
			if ($currentOperation != 'er') {
				var fieldid =  i;
				markup += getMaterialStatusMarkup($currentOperation, fieldid, batch.mst, material.istmp, false);
			}
			markup += '</p></a></li>';
		}
		markup += '</ul>';
	}
	markup += '</div>';
	if (op == 'es' || op == 'ew' || op == 'vs')
		markup += getExpiredBatchesMarkup(material);

	return markup;
}


function getOrderBatchListViewMarkup( material, op, pageOffset ) {
	var markup = '';
	var oid = '';

	var batches;
	var orderItem;
	var allocationEdited = false;
	// Get locally modified batches, if any
	batches =  getBatchListviewModel( material.bt, null );
	var params = getURLParams(getCurrentUrl());
	if (params && params.oid && params.oty) {
		var orderBatches = getBatchListviewModel( material.bt, null );
		var obatches;
		obatches = $.extend(true, [], orderBatches);
		oid = params.oid;
		orderItem = getOrderItem(params.oid, params.oty, material.mid);
		if (!orderItem)
			return markup;
		if (orderItem.bt) {
			for (var i = 0; i < obatches.length; i++) {
				obatches[i].alq = 0;
				for (var j = 0; j < orderItem.bt.length; j++) {
					if (obatches[i].bid == orderItem.bt[j].bid) {
						if (orderItem.bt[j].nalq != null) {
							obatches[i].nalq = orderItem.bt[j].nalq;
							allocationEdited = true;
						}
						else if (orderItem.bt[j].alq != null) {
							obatches[i].alq = orderItem.bt[j].alq;
							allocationEdited = true;
						}
						else
							obatches[i].alq = 0;
						if ( orderItem.bt[j].nmst )
							obatches[i].nmst = orderItem.bt[j].nmst;
						else if ( orderItem.bt[j].mst )
							obatches[i].mst = orderItem.bt[j].mst;

					}
				}
			}
		}
		else {
			for (var i = 0; i < obatches.length; i++) {
				obatches[i].alq = 0;
			}
		}

	}

	var noOfBatches = ( batches ? batches.length : 0 );
	// Render the batch list
	markup += '<div id="batchpanel" data-role="collapsible" data-theme="c" data-content-theme="d" data-collapsed="false">';
	markup += '<h3>' + $labeltext.batches + '</h3>';
	if (noOfBatches == 0) {
		markup += '<p>' + $messagetext.nobatchesavailable + '</p>';
	} else {
		markup += '<ul id="batchlist" data-role="listview" data-theme="d">';
		var orderQty;
		if (orderItem.nq)
			orderQty = orderItem.nq;
		else
			orderQty = orderItem.q;
		var allocatedQty = getFormattedNumber(orderQty);
		for (var i = 0; i < noOfBatches; i++) {
			var batch = batches[i];
			var rowId = 'row_' + i;
			var stockFontColor = ( batch.new ? 'red' : 'black' );
			var anchorLinkMarkup;
			anchorLinkMarkup = '<a onclick="$(\'#' + rowId +'_' + oid + '_details\').show()">';
			var batchQty = batch.q;
			if (batch.avq) {
				batchQty = batch.avq;
			}
			var batchavlstock = batch.avq;
			if (batch.avq && obatches[i].alq && allocationEdited == false)
				batchavlstock = parseFloat(batch.avq) + parseFloat(obatches[i].alq);
			markup += '<li id="' + rowId + '">' + anchorLinkMarkup + '<h3>' + batch.bid + '<span style="font-size:8pt;float:right;border-radius:25px; border:2px solid #D3D3D3;padding:5px;color:' + stockFontColor + '">' + batch.q + '</span></h3>';
			//markup += '<li id="' + rowId + '">' + anchorLinkMarkup + '<h3>' + batch.bid + '</h3><span class="ui-li-count" style="color:' + stockFontColor + '">' + batch.q + '</span>';
			markup += '<p style="font-size:9pt">'
			if (batch.avq != null && isAutoPostInventory($config)) {
				markup += $labeltext.available + ': ' + batch.avq + ', ';
				if (batch.alq != null)
					markup += $labeltext.allocated + ': ' + batch.alq;
				markup += '</br>';
			}
			markup += $labeltext.batchexpirydate + ': ' + batch.bexp;
			var quantityId = 'quantity_' + i + '_' + oid;
			var batchStockId = 'batchstock_' + i + '_' + oid;
			var batchNumberId = 'bid_' + i  + '_' + oid;
			var batchExpiryId = 'bexp_' + i + '_' + oid;
			var batchManuNameId = 'bmfnm_' + i + '_' + oid;
			var batchManuDateId = 'bmfdt_' + i + '_' + oid;
			var batchAvlStockId = 'batchavlstock_' + i + '_' + oid;
			var batchAlqStockId = 'batchalqstock_' + i + '_' + oid;
			var batchMaterialStatusId = 'mst_' + i + oid;
			var errorId = 'quantity_error_' + i + '_' + oid;

			var min = '1';
			//var onBlurFunction = 'validateStockQuantityOnBlur(\'' + batchQty + '\',\'' + quantityId + '\',\'' + errorId + '\',\'' + material.mid + '\')';
			var onBlurFunction = '';
			if (allocationEdited == false) {
				if (!obatches[i].alq || obatches[i].alq == 0) {
					var baq;
					if (allocatedQty > 0) {
						baq = ( (batchQty >= allocatedQty) ? allocatedQty : batchQty );
						allocatedQty = ( (allocatedQty > baq) ? (allocatedQty - baq) : 0);
						if (baq)
							obatches[i].alq = baq;
					}
					else
						obatches[i].alq = '';
				}
				else {
					//batch.mq = batch.alq;
					allocatedQty = ((allocatedQty > obatches[i].alq) ? (allocatedQty - obatches[i].alq) : 0);
				}

			}
			markup += '<br/><input  type="number" id="' + quantityId + '" min="' + min + '" max="1000000"  class ="inputfieldchange" placeholder="';
			markup += '""  value= "' + ( obatches[i].nalq ? obatches[i].nalq : obatches[i].alq ) + '"';
			markup += ' oninput="maxLengthCheck(\'' + quantityId + '\')"' +
				' onfocus="resetErrorHighlightOnFocus(\'' + quantityId + '\',\'' + errorId + '\')" bindex="' + i + '" />';
			markup += '<span id="' + errorId + '" style="display:none;font-size:8pt;color:red;white-space: initial" /><br/>';

			// Add hidden fields for the metadata
			markup += '<input type="hidden" id="' + batchStockId + '" value="' + batchQty + '" />';
			markup += '<input type="hidden" id="' + batchNumberId + '" value="' + batch.bid + '" />';
			markup += '<input type="hidden" id="' + batchExpiryId + '" value="' + batch.bexp + '" />';
			markup += '<input type="hidden" id="' + batchManuNameId + '" value="' + batch.bmfnm + '" />';
			markup += '<input type="hidden" id="' + batchAvlStockId + '" value="' + batchavlstock + '" />';

			markup += '<div id="' + rowId +'_' + oid + '_details" style="display:none;font-size:9pt;font-weight:normal;margin-top:-3px">';
			markup += $labeltext.manufacturedby + ': ' + batch.bmfnm;
			if (batch.bmfdt)
				markup += '<br/>' + $labeltext.batchmanufactureddate + ': ' + batch.bmfdt;
			if (batch.t)
				markup += '<div style="font-size:8pt;font-style:italic;margin-top:2%">' + $labeltext.lastupdated + ': ' + batch.t + '</div>';
			markup += '</div>';
			var fieldid = i + '_' + oid;
			var materialStatus = obatches[i].mst;
			if ( obatches[i].nmst )
				materialStatus = obatches[i].nmst;
			markup += getMaterialStatusMarkup('ei', fieldid, materialStatus, material.istmp, false);
			markup += '</p></a></li>';
		}
		markup += '</ul>';
	}
	markup += '</div>';
	return markup;
}

function getBatchList (material) {
	var modifiedMaterial = getLocalModifications( material.mid, $currentOperation, $entity );
	var modifiedBatches = ( modifiedMaterial ? modifiedMaterial.bt : null );
	// Merge existing and modified batches, if any
	var batches = getBatchListviewModel( material.bt, modifiedBatches );
	return batches;
}



function isNewBatchAdded(material) {
	var modifiedMaterial = getLocalModifications( material.mid, $currentOperation, $entity );
	var modifiedBatches = ( modifiedMaterial ? modifiedMaterial.bt : null );
	if (modifiedBatches) {
		for (var i = 0; i < modifiedBatches.length; i++) {
			if (modifiedBatches[i].new)
				return true;
		}
	}
	return false;
}

// Create Batch object
function createBatchObject(  batchId, batchExpiryDate, manufacturerName, manufacturedDate,stockQuantity,lastUpdated,modifiedQuantity,newBatch, materialStatus) {
	var b = {};
	if ( batchId )
		b.bid = batchId;
	if ( batchExpiryDate )
		b.bexp = batchExpiryDate;
	if ( manufacturerName )
		b.bmfnm = manufacturerName;
	if ( manufacturedDate )
		b.bmfdt = manufacturedDate;
	if (stockQuantity != null)
		b.q = stockQuantity;
	if (lastUpdated)
		b.t = lastUpdated;
	if (modifiedQuantity)
		b.mq = modifiedQuantity;
	if (newBatch)
		b.new = true;
	if (materialStatus)
		b.mst = materialStatus;
	return b;
}


// Merge batch lists, so that they are sorted by expiry date (asc), and form the data model for the batch listview
function getBatchListviewModel( existingBatches, modifiedBatches ) {
	var list = [];
	if ( !modifiedBatches || modifiedBatches.length == 0 )
		return sortFilterBatches( existingBatches );
	if ( !existingBatches || existingBatches.length == 0 )
		return sortFilterBatches( modifiedBatches );
	for ( var i = 0; i < existingBatches.length; i++ ) {
		var existingBtch = existingBatches[ i ];
		var modifiedBtch = null;
		for ( var j = 0; j < modifiedBatches.length; j++ ) {
			if ( existingBtch.bid == modifiedBatches[j].bid ) {
				modifiedBtch = modifiedBatches[j];
				break;
			}
		}
		if ( modifiedBtch ) {
			var newObject = $.extend( true, {}, existingBtch );
			newObject.mq = modifiedBtch.q; // modified material quantity is in 'mq'
			newObject.mst = modifiedBtch.mst;
			list.push( newObject );
		} else {
			list.push( existingBtch );
		}
	}
	// If there are any new batches, push those too
	for ( var i = 0; i < modifiedBatches.length; i++ ) {
		if ( modifiedBatches[i].new )
			list.push( modifiedBatches[i] );	// new batches have the 'new' key set to true
	}
	// Sort asc. by expiry date
	list = sortFilterBatches( list );
	return list;
}

// Sort batches in asc order of expiry; if there are any expired batches, do not include them in the view (this can happen in the offline situation, but an edge case)
function sortFilterBatches( batches ) {
	if ( !batches || batches.length == 0 )
		return;
	// Sort asc. by expiry date
	batches.sort( function( b1, b2 ) {
		var d1 = getDateFromStr( b1.bexp );
		var d2 = getDateFromStr( b2.bexp );
		if ( d1 > d2 )
			return 1;
		else if ( d1 < d2 )
			return -1;
		else
			return 0;
	});
	// If any batches have already expired, do not include them in the view (edge case possible in an offline situation)
	for ( var i = 0; i < batches.length; i++ ) {
		var expiry = batches[i].bexp + ' 23:59:00'; // end of the day of expiry
		if ( getDateFromStr( expiry ) < new Date() ) // this batch has already expired
			batches.splice( i, 1 );
	}
	return batches;
}

function getExpiredBatchesMarkup(material) {
	// Get expired  batches, if any
	var expiredBatches = material.xbt;
	var markup = '';
	var noOfBatches = ( expiredBatches ? expiredBatches.length : 0 );
	if (noOfBatches == 0)
		return markup;
	else {
		// Render the batch list
		markup += '<div id="expiredbatchpanel" data-role="collapsible" data-theme="c" data-content-theme="d" data-collapsed="false">';
		markup += '<h3>' + $labeltext.expiredbatches + '</h3>';

		markup += '<ul id="expiredbatchlist"  data-role="listview" data-inset="true" class="mylist"  data-theme="d" data-split-theme="b" data-split-icon="refresh">';
		for (var i = 0; i < noOfBatches; i++) {
			var batch = expiredBatches[i];
			if (batch.q == 0)
			 continue   //expired batch with value 0
			var rowId = 'row_' + i;
			var stockFontColor = 'black';
			var anchorLinkMarkup;
			anchorLinkMarkup = '<a onclick="$(\'#' + rowId + '_exp\').show()">';
			markup += '<li data-icon="false" id="' + rowId + '">' + anchorLinkMarkup + '<h3>' + batch.bid + '</h3><span class="ui-li-count" style="color:' + stockFontColor + '">' + batch.q ;
			markup += '</span>';
			markup += '<p style="font-size:9pt">' + $labeltext.batchexpirydate + ': ' + batch.bexp;
			var batchResetId = "batchreset_" + i;
			var batchResetIconId = "batchreseticon_" +i;
			var batchReset = null;
			var qty;
			if (checkExpiredBatchReset(material,batch.bid,i) && ($currentOperation != 'vs')) {
				markup += '<span id= "' + batchResetId + '" style="font-weight:normal;font-size:9pt;color:red;display:block">(' + $labeltext.reset + ')</span>';
				batchReset = true;
				if ($currentOperation  == 'ew')
					qty = batch.q;
				else // physical stock count
				 	qty = 0;
			}
			else {
				markup += '<span id= "' + batchResetId + '" style="font-weight:normal;font-size:9pt;color:red;display:none">(' + $labeltext.reset + ')</span>';
				qty = '';
			}

			markup += '<div id="' + rowId + '_exp" style="display:none;font-size:9pt;font-weight:normal;margin-top:-3px">';
			markup += $labeltext.manufacturedby + ': ' + batch.bmfnm;
			if (batch.bmfdt)
				markup += '<br/>' + $labeltext.batchmanufactureddate + ': ' + batch.bmfdt;
			if (batch.t)
				markup += '<div style="font-size:8pt;font-style:italic;margin-top:2%">' + $labeltext.lastupdated + ': ' + batch.t + '</div>';
			markup += '</div>';
			markup += '</p></a>';
            if (!batchReset && ($currentOperation != 'vs'))
			 markup += '<a href="#" id="'+ batchResetIconId + '" data-theme="b"  onclick='+'"resetBatchStockDialog('+material.mid +','+i+')">'+$labeltext.reset+'</a>';
			// Add hidden fields for the metadata
			var quantityId = 'expquantity_' + i;
			var batchStockId = 'expbatchstock_' + i;
			var batchNumberId = 'expbid_' + i;
			var batchExpiryId = 'expbexp_' + i;
			var batchManuNameId = 'expbmfnm_' + i;
			var batchManuDateId = 'expbmfdt_' + i;
			markup += '<input type="hidden" id="' + quantityId + '" value="' + qty + '" bindex="' + i + '" />';
			markup += '<input type="hidden" id="' + batchStockId + '" value="' + batch.q + '" />';
			markup += '<input type="hidden" id="' + batchNumberId  + '" value="' + batch.bid + '" />';
			markup += '<input type="hidden" id="' + batchExpiryId  + '" value="' + batch.bexp + '" />';
			markup += '<input type="hidden" id="' + batchManuNameId  + '" value="' + batch.bmfnm + '" />';
			if ( batch.bmfdt )
				markup += '<input type="hidden" id="' + batchManuDateId  + '" value="' + batch.bmfdt + '" />';
			markup += '</li>';
		}
		markup += '</ul>';
		markup += '</div>';
	}
	return markup;
}
function resetBatchStockDialog(mid,batchIndex){
	var markup = '';
	/*$( '<div>' ).simpledialog2( {
		mode: 'blank',
		headerClose: true,
		headerText: $labeltext.confirm,
		themeDialog: 'd',
		showModal: false,
		blankContent:
		'<div class="ui-body-d" style="padding:2px 2px 2px 2px;">' +
		'<p>' + $messagetext.resetbatchconfirm + '</p>' + markup+
		'<a data-role="button" onclick="$.mobile.sdCurrentDialog.close();resetBatchForMaterial('+mid+','+batchIndex+');" data-inline="true" data-theme="b">' + $buttontext.yes + '</a> &nbsp;' +
		'<a data-role="button" onclick="$.mobile.sdCurrentDialog.close();" data-inline="true" data-theme="c" rel="close">' + $buttontext.no + '</a>' +
		'</div>'
	} );*/

	showPopupDialog ($labeltext.confirm,$messagetext.resetbatchconfirm ,function(){resetBatchForMaterial(mid,batchIndex);},null,null,"popupresetbatch", null);
}

function resetBatchForMaterial(mid,bIndex) {
	if (bIndex == null)
		return;
	var op = $currentOperation;
	var batches = [];
	var quantity = 0;
	var page = $('#materialinfo');
	var batchOpeningStock = page.find('#expbatchstock_' + bIndex).val();
	if (op == 'ew') //discard all
		quantity = batchOpeningStock;
	page.find('#expquantity_' + bIndex).val(quantity);
	$('#batchreset_'+ bIndex).attr( 'style', 'display:block' );
	$('#batchreset_'+ bIndex).html('<font style="color:red;font-size:9pt;">(' + $labeltext.reset + ')</font>');
	page.find( '#fieldchange' ).val('1'); // for cancel button warning message to save changes
	$('#datasavebutton').prop('disabled', false).removeClass('ui-disabled');
	$('#batchreseticon_'+ bIndex).remove();
	$('#expiredbatchlist').listview('refresh');
   return;
}


function checkExpiredBatchReset(material,batchId,rowId) {
	var modifiedMaterial = getLocalModifications( material.mid, $currentOperation, $entity );
	var modifiedBatches = ( modifiedMaterial ? modifiedMaterial.bt : null )
	if (modifiedBatches){
		for ( var j = 0; j < modifiedBatches.length; j++ ) {
			var modifiedBatch = null;
			if ( batchId == modifiedBatches[j].bid ) {
				modifiedBatch = modifiedBatches[j];
					return true;
			}
		}
	}
	return false;
}


function getFetchRelatedEntityInventoryMarkup( relatedEntity , mid){
	var markup = '';
	if (!relatedEntity )
		return markup;
	if (getRelatedEntityPermissions(relatedEntity.kid, relatedEntity.type, $entity.kid) != $NO_PERMISSIONS ) {
		var entity = getRelatedEntity(relatedEntity.kid, relatedEntity.type);
		if (entity) {
			var msg = $messagetext.viewstockatrelatedentity;
			msg = msg.replace('relatedkid', entity.n);
			var onClick;
			//if ( mid )
			onClick = 'fetchRelatedInventoryStock(' + relatedEntity.kid + ',\'' + relatedEntity.type + '\',' + mid + ',false)';

			markup += '<br /> <div data-role="fieldcontain"><label id="fetchstocklabel" for="fetchstockcheckbox"><font style="font-size:small;font-weight:normal">'+msg+'</font></label><input type="checkbox" data-theme="d" id="fetchstockcheckbox" onclick="' + onClick + '"/>';
			if ( mid != null)
				markup += getInventoryStockDisplayMarkup( relatedEntity, mid );
			markup += '</div>';
		}
	}
	return markup;
}


function getInventoryStockDisplayMarkup ( relatedEntity ,mid ) {
	var rowId;
	var markup ='';
	rowId = '_' + mid ;
	var onRefreshClick = 'fetchRelatedInventoryStock(' + relatedEntity.kid + ',\'' + relatedEntity.type + '\',' + mid + ',true)';
	markup += '<div id="rquantityDiv' +  rowId + '" class="ui-body-d ui-corner-all ui-shadow" style="display:none";><font style="font-size:small"><label id="rstock' + rowId + '"  style="vertical-align:top;"; for="refreshlink">Stock</label> </font>&nbsp;<a  id="refreshlink" alt="refresh"  href="#" onclick="' + onRefreshClick + '" style="display:inline;margin-top: -.5em"><img style="margin-top: -.2em;" src="jquerymobile/icons/refresh.png" title="' + $buttontext.refresh + '" alt="Refresh" /></a>';
	markup += '<table id = "relatedquantityTable' + rowId + '" data-role="table" data-mode="reflow" class="infotable"><tbody>';
	markup += '<tr><th><font style="font-size:small;font-weight:normal;">'+$labeltext.stockonhand+':</font></th><td id ="rqty' + rowId + '" > Quantity</td></tr>';
	markup += '<tr><th id="hdsq' +  rowId + '"><font style="font-size:small;font-weight:normal;">'+$labeltext.durationofstock+':</font></th><td id ="rdsq' + rowId + '" > Duration</td></tr>';
	if ( isAutoPostInventory( $config ) ) {
		markup += '<tr><th id="havq' + rowId + '"><font style="font-size:small;font-weight:normal;">' + $labeltext.available + ':</font></th><td id ="ravq' + rowId + '" > Available</td></tr>';
	}
	markup += '<tr><th id="hmin' +  rowId + '"><font style="font-size:small;font-weight:normal;">'+$labeltext.min+':</font></th><td id="rmin' +  rowId + '" >min</td></tr>';
	markup += '<tr><th id="hmax' +  rowId + '"><font style="font-size:small;font-weight:normal;">'+$labeltext.max+':</font></th><td id="rmax' + rowId + '" >max</td></tr>';
	markup += '</tbody></table>'
	markup += '</div>';
	return markup;
}



function fetchRelatedInventoryStock ( relatedEntityId, relationType,  mid , refreshStock){
	if ($( $.mobile.activePage[0] ).find('#fetchstockcheckbox').is(":checked") || refreshStock) {
		//$('#fetchstock').attr('disabled', true);
		var entity = getRelatedEntity(relatedEntityId, relationType);
		if (entity) {
			var quantity;
			if (!entity.mt || refreshStock) { //no material info available , fetch stock from server
				var msg = $messagetext.fetchstockatrelatedentity;
				if ( mid ) {
					var material = $materialData.materialmap[mid];
					msg = msg.replace('mn', material.n);
				}
				else
					 msg = msg.replace('mn', $messagetext.orderitems);
				msg = msg.replace('relatedkid', entity.n);
				var params = {};
				params.kid = relatedEntityId;
				var okFunction = function () {fetchKioskData(params, '#materialinfo', null);};
				if ( refreshStock )
					okFunction = function () {getInventoryForRelatedEntity(relatedEntityId, relationType, true);};
					showPopupDialog($labeltext.confirm, msg, okFunction,
					function () {
						checkBoxUncheck('#fetchstockcheckbox');
					}, null, "popupFetchStock", false);
			}
			else {
				showRelatedEntityStock( relatedEntityId );
			}

		}
		else
			$('#materialinfo').find('#rquantityDiv'+'_' + mid).attr('style', 'display:none');
	}
}


function showRelatedEntityStock(  relatedEntityId ) {
	var params = getURLParams(getCurrentUrl());
	var mid;
	var page = $(".ui-page-active").attr("id");
	if (page == 'orderinfo') {
		var oid = params.oid;
		var otype = params.oty;
		var order = getOrder(oid, otype);
		var items = order.mt;
		if ( items ){
			for ( var i=0 ; i < items.length; i++){
				displayStock(page, relatedEntityId, items[i].mid);
			}
		}
	}

	else {
		if (!mid && params)
			mid = params.mid;
		displayStock( page, relatedEntityId, mid);
	}
}

function displayStock ( page, relatedEntityId, mid ) {
	if (mid) {
		var entity = getEntity($uid,relatedEntityId);
		var quantity;
		var min;
		var max;
		var dsq;
		var crType;
		var stFreq;
		var durationText= '';
		var avq;
		page = $('#'+page);
		if (entity) {
			if (entity.mt) {
				for (var i = 0; i < entity.mt.length; i++) {
					if (entity.mt[i].mid == mid) {
						quantity = entity.mt[i].q;
						min = entity.mt[i].min;
						max = entity.mt[i].max;
						avq = entity.mt[i].avq;
						if ( entity.mt[i].crD )
							crType = 'd';
						else if ( entity.mt[i].crW )
							crType = 'w';
						else if ( entity.mt[i].crM)
							crType = 'm';
						stFreq = getFrequencyText( "stock", crType ); //Stock frequency and crFrequency are the same
						if ( stFreq && entity.mt[i].dsq ) {
							durationText = $labeltext.durationofstock.replace('drtn', stFreq);
							dsq = getFormattedNumber(entity.mt[i].dsq);
						}
						break;
					}
				}
			/*	if (page[0].id == 'materialinfo' ) {
					$('#materialinfo').find('#fetchstocklabel').hide();
					$('#materialinfo').find('#fetchstockcheckbox').hide();
				}
				else if (page[0].id == 'orderinfo' ) {
					$('#orderinfo').find('#fetchstocklabelorder').hide();
					$('#orderinfo').find('#fetchstockcheckboxorder').hide();
				}*/
				page.find('#fetchstocklabel').hide();
				page.find('#fetchstockcheckbox').hide();
				page.find('#rquantityDiv'+ '_' + mid ).attr('style', 'padding:0.5em;display:block;vertical-align:top"');
				if ( quantity != null ) {
					var stockText = $labeltext.stockatrelatedkid;
					stockText = stockText.replace('relatedkid', entity.n) ;
					page.find('#rstock'+ '_' + mid ).text(stockText);
					var stockColor = getStockColor( entity.mt[i] );
					page.find('#rqty'+ '_' + mid).html('<font style="font-size:small;color:' + stockColor + '">'+ quantity + '</font>');
					if ( avq != null)
						page.find('#ravq'+ '_' + mid).html('<font style="font-size:small;">'+ avq + '</font>');
					else {
						page.find('#havq' + '_' + mid).hide();
						page.find('#ravq' + '_' + mid).hide();
					}
					if ( dsq != null) {
						page.find('#rdsq' + '_' + mid).html('<font style="font-size:small;font-weight:normal">' + dsq + '</font>');
						page.find('#hdsq' + '_' + mid).html('<font style="font-size:small;font-weight:normal">' + durationText + ': </font>');
						page.find('#rdsq' + '_' + mid).show();
						page.find('#hdsq'+ '_' + mid).show();
					}
					else {
						page.find('#rdsq' + '_' + mid).hide();
						page.find('#hdsq' + '_' + mid).hide();
					}
					if ( min != null ) {
						page.find('#rmin' + '_' + mid ).show();
						page.find('#hmin'+ '_' + mid ).show();
						page.find('#rmin'+ '_' + mid ).html('<font style="font-size:small">' + min + '</font>');
					}
					else {
						page.find('#rmin'+ '_' + mid ).hide();
						page.find('#hmin'+ '_' + mid ).hide();
					}
					if ( max != null ) {
						page.find('#rmax'+ '_' + mid).html('<font style="font-size:small">' + max + '</font>');
						page.find('#rmax'+ '_' + mid ).show();
						page.find('#hmax'+ '_' + mid ).show();
					}
					else {
						page.find('#rmax'+ '_' + mid ).hide();
						page.find('#hmax'+ '_' + mid ).hide();
					}

				}
				else {
					page.find('#refreshlink'+ '_' + mid ).hide();
					page.find('#relatedquantityTable'+ '_' + mid ).hide();
					var stockText = $messagetext.nosuchmaterial;
					page.find('#rstock'+ '_' + mid).text(stockText);
				}
			}
		}
	}

}


function checkBoxUncheck (checkBoxId) {
	$( $.mobile.activePage[0] ).find( '#fetchstockcheckbox' ).prop("checked", false).checkboxradio("refresh");;
}

function getReasonStatusText(reason,materialStatus){
	var reasonStatusText = '';
	if (materialStatus)
		reasonStatusText = materialStatus;
	if ( reason ) {
		if (reasonStatusText != '')
			reasonStatusText += ', ';
		reasonStatusText += reason;
	}
	return reasonStatusText;
}

// Function get the material price
function getMaterialPrice( mid, formatted ) {
	var material = $materialData.materialmap[ mid ];
	var price = 0;
	if ( material && material.rp )
		price = material.rp;
	if ( formatted ) {
		var formattedPrice = '';
		if ( price != 0 ) {
			var currency = getCurrency( material );
			if ( currency )
				formattedPrice = currency + ' ';
			formattedPrice += getFormattedPrice( price );
		}
		return formattedPrice;
	} else {
		return price;
	}
}

function getMaterialName(mid) {
    var material = $materialData.materialmap[ mid ];
   // return material.n;
	return (material == undefined || material == null || material == '') ? 'N/A': material.n;
}

// Check for errors in stock (typically used before sending transactions); returns mids in error
function getStockIssueErrors( currentOperation, entity ) {
	if ( !(currentOperation == 'ei' || currentOperation == 'ew' || currentOperation == 'ts') ) // issues, discards/wastage and transfer stock
		return null;
	var localModifications = getLocalModifications( null, currentOperation, entity );
	if ( !localModifications )
		return null;
	var mids = Object.keys( localModifications );
	if ( !mids || mids.length == 0 )
		return null;
	var errMids;
	// Check if the quantity has exceeded current stock
	for ( i = 0; i < mids.length; i ++ ) {
		var mid = mids[i];
		var modifiedMaterial = localModifications[ mid ];
		var materialData = $materialData.materialmap[ mid ];
		if ( !materialData || !modifiedMaterial )
			continue;
		if (modifiedMaterial.bt){
			for (j = 0; j < modifiedMaterial.bt.length; j++) {
				//for (k = 0; k < materialData.bt.length; k++){
				  var batchQty = getBatchQty (modifiedMaterial.bt[j].bid, materialData) ;
				   if (batchQty != null){
						if (parseFloat(modifiedMaterial.bt[j].q) > parseFloat(batchQty)) {
							if (!errMids)
								errMids = {};
							if (!errMids[i])
								errMids[i] = {};
							if (!errMids[i].mid)
								errMids[i].mid = mid;
							if (!errMids[i].bt)
								errMids[i].bt = [];
							errMids[i].bt.push(modifiedMaterial.bt[j]);
						}
					}

				//}
			}
		}
		else {
			if (parseFloat(modifiedMaterial.q) > parseFloat(materialData.q)) {
				if (!errMids)
				errMids = {};
				if (!errMids[i])
				  errMids[i]= {};
				errMids[i].mid = mid;
			}
		}
	}
	return errMids;
}

// Render a message field
function getOrderReviewForm( order, orderSize, includeMetadata ) {
	// Check to see if fields are configured
	var paymentOptions = null;
	var packageSizes = null;
	var markup = '<div class="ui-body ui-body-b">';
	if ( includeMetadata && $config ) {
		if ( $config.popt )
			paymentOptions = $config.popt.split( ',' );
		if ( $config.pksz )
			packageSizes = $config.pksz.split( ',' );
	}
   if ( includeMetadata) {
	   markup += '<label for="referenceid">' + $labeltext.referenceid + ':</label>';
	   markup += '<input type="text" name="referenceid" id="referenceid"  value="">';
	   var orderLinkedEntity = getSelectedLinkedEntity('no', $entity);
	   var otype = 'prc';
	   if ( orderLinkedEntity )
		   otype = orderLinkedEntity.otype;
	   if (otype == 'prc') {
		   markup += '<label  for="rbd">' + $labeltext.requiredbydate + ':</label>';
		   markup += '<a href="#" data-role="button" data-icon="delete" data-iconpos="notext"  style="float: right;margin-top: -.01em;" onclick ="deleteDate(\'#rbd\')"></a>';
		   markup += '<div style="overflow: hidden; padding-right: .75em;">';
		   var today = getStrFromDate(new Date());
		   markup += '<input name="rbddatepicker" class ="mobidatepicker" id="rbd" min="' + today + '" style= "margin-top: -.02em;" type="text"  value="" />';
		   markup += '</div>';
	   }
	   var orderTags = '';
	   if ($config.otg) {
		   markup += '<label id="ordertagslabel" for="ordertags">' + $labeltext.ordertag + ':</label>';
		   markup += getOrderTagSelectMarkup(order, 'ordertags');
	   }
   }
	markup += '<label for="transmessage">'+$labeltext.comments+':</label><textarea id="transmessage"></textarea>';
	// Check if 'mark as fulfilled' option is to be shown, as well as ensure that there are items in the order
	//Discussed with Charan, this will be looked at in the next version.
	//if ( allowMarkOrderAsFulfilled( order, orderSize ) )
	//	markup += '<label for="initialorderstatus"><input type="checkbox" id="initialorderstatus" data-theme="d" value="fl" checked />'+$messagetext.markorderasfulfilled+'</label>';
	// Add field for payment, if new order only
	if ( !order && !$disableOrderPrice ) { // implies new order mode, given no order
		var currencyText = ( $entity.cu ? ' in ' + $entity.cu : '' );
		var placeholder = $messagetext.enteramount + currencyText;
		markup += '<label for="payment">'+$labeltext.addpayment+' ' + currencyText + ':</label><input type="number" id="payment" placeholder="' + placeholder + '" />';
	}
	// Payment options
	if ( paymentOptions != null && !$disableOrderPrice ) {
		markup += '<label for="paymentoption">'+$labeltext.paymentoption+':</label><select id="paymentoption" data-mini="false">';
		var selectedOption = null;
		if ( order != null && order.popt )
			selectedOption = order.popt;
		for ( var i = 0; i < paymentOptions.length; i++ ) {
			var sel = '';
			if ( selectedOption != null && selectedOption == paymentOptions[i] )
				sel = ' selected';
			markup += '<option value="' + paymentOptions[i] + '"' + sel + '>' + paymentOptions[i] + '</option>';
		}
		markup += '</select>';
	}
/*	if ( packageSizes != null ) {
		markup += '<label for="packagesize">'+$labeltext.packagesize+':</label><select id="packagesize" data-mini="false">';
		var selectedOption = null;
		if ( order != null && order.pksz )
			selectedOption = order.pksz;
		for ( var i = 0; i < packageSizes.length; i++ ) {
			var sel = '';
			if ( selectedOption != null && selectedOption == packageSizes[i] )
				sel = ' selected';
			markup += '<option value="' + packageSizes[i] + '"' + sel + '>' + packageSizes[i] + '</option>';
		}
		markup += '</select>';
	}*/

	markup += '</div>';
	return markup;
}


function getOrderTagSelectMarkup(order, orderTagsId) {
	var markup = '';
	if (!$config.otg && !order.tg)
		return markup;
	var orderTags = '';
	if ($config.otg) {
		orderTags = $config.otg;
		orderTags = orderTags.split(',').filter(function (v) {
			return v !== ''
		});
	}
	var selectedTags = [];
	var editedTag;
	if (order != null && order.dirty)
		editedTag = order.dirty.tg
	if (order != null && ( order.tg || (editedTag != null) )) {
		var tg;
		if (editedTag != null)
			tg = editedTag;
		else
			tg = order.tg;
		if (tg)
			selectedTags = tg.split(',').filter(function (v) {
				return v !== ''
			});
	}
	if (orderTagsId == 'ordertagsearch') {
		selectedTags[0] = $orderTagFilter;
		markup += '<div class="ui-corner-all">';
		markup += '<select name="ordertags" id="' + orderTagsId + '"   data-theme="c" >';
	}
	else
		markup += '<select name="ordertags" class="multipleselectelement" id="ordertags" multiple="multiple"  data-theme="c" data-native-menu="false">';
	markup += '<option value ="">' + $labeltext.selecttag + '</option>';
	for (var i = 0; i < orderTags.length; i++) {
		var tagSel = '';
		if (selectedTags != null) {
			if ( $.inArray(orderTags[i], selectedTags) != -1 ) {
				tagSel = ' selected';
			}
		}
		markup += '<option value="' + orderTags[i] + '"' + tagSel + '>' + orderTags[i] + '</option>';
	}
	if (orderTagsId != 'ordertagsearch') {
		if (selectedTags != null) {
			for (var i = 0; i < selectedTags.length; i++) {
				var tagSel = '';
				var addTag = false;
				if (orderTags) {
					if ($.inArray(selectedTags[i], orderTags) == -1)
						addTag = true;
				}
				else
					addTag = true;

				if (addTag)
					markup += '<option value="' + selectedTags[i] + '" selected >' + selectedTags[i] + '</option>';
			}
		}
	}
	markup += '</select>';
	if (orderTagsId == 'ordertagsearch')
		markup += '</div>';
	return markup;
}




// Check if sales order can be allowed to be marked as fulfilled
function allowMarkOrderAsFulfilled( order, orderSize ) {
	// Check if 'mark as fulfilled' option is to be shown, as well as ensure that there are items in the order
	var hasVendor = ( order && order.vid );
	var isStatusAllowable = true;
	if ( !order )
		hasVendor = getSelectedLinkedEntity( $currentOperation, $entity );
	else
		isStatusAllowable = ( order.ost != 'fl' && order.ost != 'cn' ); // no fulfilled and not cancelled, then allow
	return ( $config.omaf && $config.omaf == '0' && orderSize > 0 && hasVendor && isStatusAllowable );
}

//Save form data
function checkFormData( mid, nextUrl ) {
	// Get the data from the form
	var page = $('#materialinfo');
	var quantity = page.find('#quantity').val();
	var materialid = mid;
	var nextPage = nextUrl;
	// Get this material's metadata
	var material = $materialData.materialmap[mid];

	if ( !material.ben || isOperationOrder( $currentOperation ) ) {
		if (quantity == '') {
			clearLocalModifications($uid, $currentOperation, mid, true, false);
			// Change page
			if (nextUrl)
				$.mobile.changePage(nextUrl);
			return;
		} else {
			quantity = parseFloat(quantity);
		}
		// Validate quantity
		var errMsg = null;
		var aQty;
		var msgCannotExceedStock = $messagetext.cannotexceedcurrentstock;
		if ( material ) {
			if (material.avq) {
				aQty = material.avq;
				msgCannotExceedStock = $messagetext.cannotexceedavailablestock;
			}
			else
				aQty = material.q;
		}
		if (isNaN(quantity))
			errMsg = $messagetext.notvalidnumber; //'You have not entered a valid number';
		else if (quantity < 0)
			errMsg = $messagetext.notpositivenumber; //'Please enter a positive number'
		else if (!isInteger(quantity)) // not an integer
			errMsg = $messagetext.notinteger;
		else if ($currentOperation != 'es' && quantity == 0)
			errMsg = $messagetext.positivenumbergreaterthanzero; //'Please enter a positive number greater than zero'
		else if (( $currentOperation == 'ei' || $currentOperation == 'ew' || $currentOperation == 'ts') &&  aQty && ( quantity > parseFloat(aQty) ))
			errMsg = $labeltext.quantity + ' (' + quantity + ') ' + msgCannotExceedStock + ' (' + parseFloat(aQty) + ')';
		else if (material.ehuc && material.hu) {//handling unit enforced HMA-455
			if (material.hu.length > 0) {
				if (material.hu[0].q) {
					if (quantity % material.hu[0].q != 0) {
						var msg = $messagetext.numunitsdoesnotmatch.replace('quantity', quantity);
						msg = msg.replace('hq', parseFloat(material.hu[0].q));
						msg = msg.replace('mn', material.n);
						msg = msg.replace('mn', material.n);
						errMsg = msg.replace('hnm', material.hu[0].hnm);
					}
				}
			}

		}

 		hasErrors = false;
		if (errMsg != null) {
			var msgDiv = page.find('#quantityerrormessage');
			msgDiv.attr('style', 'display:block');
			msgDiv.html('<font style="color:red;font-size:8pt;">' + errMsg + '</font>');
			hasErrors = true;
		}
		var atdRequired = getCaptureActualTransactionDate($currentOperation,$config);
		if (atdRequired == 2) {
			var actualTransactionDate = page.find('#actualTransactionDate').val();
			if (!actualTransactionDate || actualTransactionDate == ''){
				var msgDiv = page.find('#actualTransactionDate_error');
				msgDiv.attr('style', 'display:block');
				msgDiv.html('<font style="color:red;font-size:8pt;">' + $messagetext.enteractualdate + '</font>')
				hasErrors = true;;
			}
		}
		var materialStatus = page.find('#materialstatus_'+ mid).val();
		if ( !materialStatus &&  page.find('#materialstatus_'+ mid).is(":visible")) {
			var msgDiv = page.find('#materialstatus_err_' + mid);
			msgDiv.attr('style', 'display:block');
			hasErrors = true;
		}
		if (hasErrors)
		  return;

	} else if ( !isMultiBatchFormDataValid( mid ) ) { //validate batch quantity entered
		return;
	}

	if ((($currentOperation == 'no') || ($currentOperation == 'ao')) ){
	    var eoq = page.find('#eoq').val();
	    if ((eoq >= 0) && (quantity > 0 )) {
			if (quantity != eoq) {
				//var msg = $messagetext.changedorderquantityfromrecommendedquantity+' ('+eoq+'). '+$messagetext.doyouwanttocontinue;
				//showConfirm( $messagetext.warning, msg,null,  function() {
				//	saveFormData( mid, nextUrl );},getCurrentUrl() );
				var modifiedMaterial = getLocalModifications( mid, $currentOperation, $entity );
				var reason;
				/*if (modifiedMaterial){
					reason = modifiedMaterial.reason
				}*/
				var reason = page.find('#irreason_' + mid).val();
				var othersSelected;
				if (reason && reason == $labeltext.others) {
					reason = page.find('#irreasonmsg_' + mid).val();
					othersSelected = true;
				}
				if (!reason) {
					if (page.find('#irreasonmsg_' + mid).length) {
						reason = page.find('#irreasonmsg_' + mid).val();
					}
					else if (modifiedMaterial)
						reason = modifiedMaterial.reason;
				}
				if ( (reason == undefined || reason == null || reason == '' ) && (othersSelected || isOrderReasonsMandatory( $config, 'ir'))) {
						$('#irreasonerr_' + mid).attr('style', 'display:block');
						return;
				}
				else {
					//saveFormData(mid,nextUrl);
					//	var okButtonCallBackFunction = function(){checkReasonsMandatory(function(){saveFormData( mid , nextUrl );});};
					var okButtonCallBackFunction = function () {
						saveFormData(mid, nextUrl);
					};
					var cancelButtonCallBackFunction = '';
					//recommendedQtyReasonsDialog('#materialinfo',$messagetext.changedorderquantityfromrecommendedquantity + ' ('+eoq+'). ' + '<p>'+
					// $messagetext.doyouwanttocontinue + '</p>',
					//	reason, okButtonCallBackFunction,cancelButtonCallBackFunction);
					//var extraHtml = getIgnoreRecommendedOrderReasonsMarkup(reason);
					var warnMsg = '';
					if ( isDevice () )
						 warnMsg = $messagetext.changedorderquantityfromrecommendedquantity + ' (' + eoq + ').' + ' '  + $messagetext.doyouwanttocontinue;
					else
						warnMsg = $messagetext.changedorderquantityfromrecommendedquantity + ' (' + eoq + '). ' + '<p>' + $messagetext.doyouwanttocontinue + '</p>';
					showPopupDialog($labeltext.confirm, warnMsg, okButtonCallBackFunction, null, null, "popuporderreason", null);
				}

			}
			else
				saveFormData(mid,nextUrl);
	    }
	    else
		  saveFormData(mid,nextUrl);
	} else {
		// Save the form data
		if ( material.ben ) {
			saveFormData( mid, null ); // do not change page, since EEFO check is to be made and user warned
			if ( !isEEFOValid($('#materialinfo') ) )
				showConfirm($messagetext.warning, $messagetext.fefomessage, nextUrl, null, getCurrentUrl() );
			else
				$.mobile.changePage( nextUrl );
		} else {
			saveFormData( mid, nextUrl );
		}
	}
}


function maxLengthCheck(fieldId)
{
	var field = $( '#' + fieldId );
	var quantity = field.val().trim();
	if (quantity.length > $maxNumberLength)
		field.val(field.val().slice(0, $maxNumberLength));
}



function maxFieldLengthCheck(fieldId, maximumLengthAllowed)
{
	var field = $( '#' + fieldId );
	var anyField = field.val();
	if (anyField.length > maximumLengthAllowed)
		field.val(field.val().slice(0, maximumLengthAllowed));
}

function validateStockQuantityOnBlur( materialStock, fieldId, errorNodeId, mid ) {
	var field = $( '#' + fieldId );
	var errorNode = $( '#' + errorNodeId );
	var quantity = field.val().trim();
	if ( quantity == '' )
		return true;
	field.attr( 'value', quantity ); // set it to the trimmed value
	var errMsg = validateStockQuantity( quantity, materialStock, false, mid );
	if ( errMsg && errMsg != '' ) {
		if ( field )
			addFieldHighlight( field );
		if ( errorNode ) {
			errorNode.text( errMsg );
			errorNode.show();
		}
		return false;
	} else if ( quantity != '' ) {
		// Enable the save button, if disabled
		$('#datasavebutton').prop('disabled', false).removeClass('ui-disabled');
	}
	return true;
}

function resetErrorHighlightOnFocus( fieldId, errorNodeId ) {
	var field = $( '#' + fieldId );
	var errorNode = $( '#' + errorNodeId );
	if ( field )
		removeFieldHighlight( field );
	if ( errorNode ) {
		errorNode.empty();
		errorNode.hide();
	}
    //enable save button on focus
	$('#datasavebutton').prop('disabled', false).removeClass('ui-disabled');
}

// Validate batch quantity; if invalid, return the error message otherwise undefined
// NOTE: materialStock is optional
function validateStockQuantity( quantity, materialStock, disallowZeroStockCount, mid ) {
	var msg;
	try {
		//var s = ( materialStock ? parseFloat(materialStock) : undefined );
		var s = null;
		var msgCannotExceedStock = $messagetext.cannotexceedcurrentstock;
		if (isAutoPostInventory($config))
			msgCannotExceedStock = $messagetext.cannotexceedavailablestock;

		if (materialStock != null)
		 	s = parseFloat(materialStock);
		if (quantity == undefined || quantity == null || quantity == '' || isNaN(quantity)) {
			msg = $messagetext.notpositivenumber;
		} else {
			var q = parseFloat(quantity);
			var material = $materialData.materialmap[mid];
			if (!isInteger(q))
				msg = $messagetext.notinteger;
			else if (q < 0)
				msg = $messagetext.positivenumbergreaterthanzero;
			else if ( q == 0 && ( disallowZeroStockCount || $currentOperation != 'es' ) && !isOperationOrder( $currentOperation ) )
				msg = $messagetext.positivenumbergreaterthanzero;
			else if ( materialStock != null && ( $currentOperation == 'ei' || $currentOperation == 'ew' || $currentOperation == 'ts' || isOperationOrder($currentOperation)) && ( q > s ))
				msg = $labeltext.quantity + ' ' + msgCannotExceedStock  + '('+ s + ')';
			else if (material.ehuc && material.hu) {//handling unit enforced HMA-455
				if (material.hu.length > 0) {
					if (material.hu[0].q) {
						if (quantity % material.hu[0].q != 0) {
							msg = $messagetext.numunitsdoesnotmatch.replace('quantity', quantity);
							msg = msg.replace('hq', parseFloat(material.hu[0].q));
							msg = msg.replace('mn', material.n);
							msg = msg.replace('mn', material.n);
							msg = msg.replace('hnm', material.hu[0].hnm);
						}
					}
				}
			}
		}
	} catch (e ) {
		msg = $messagetext.notvalidnumber;
	}
	return msg;
}


function enableButtonsOnQuantityKeyUp(fieldId) {
	var page = $('#materialinfo');
	var quantityval = page.find('#'+fieldId).val();
	if ( quantityval != '')
		$('#datasavebutton').prop('disabled', false).removeClass('ui-disabled');
}

function isMultiBatchFormDataValid( mid ) {
	 // Get the data from the form
	var page = $('#materialinfo');
	// Get all the quantity field nodes
	var quantityFields = page.find( 'input[id^="quantity_"]' );
	if ( !quantityFields )
		return false;
	 var hasErrors = false;
	 for (var i = 0; i < quantityFields.length; i++) {
		 var bIndex = $( quantityFields[i] ).attr( 'bindex' );
		 var batchStock = page.find( '#batchstock_' + bIndex ).val();
		 if (page.find('#batchavlstock_' + bIndex).length > 0)
			 batchStock = page.find( '#batchavlstock_' + bIndex ).val();
		 var isError = !validateStockQuantityOnBlur( batchStock, quantityFields[i].id, 'quantity_error_' + bIndex , mid );
		 if ( !hasErrors )
		 	hasErrors = isError;
		 var materialStatus = page.find('#materialstatus_' + bIndex ).val();
		 var quantity =  $( quantityFields[i] ).val().trim();
		 if ( !materialStatus &&  quantity != '' && page.find('#materialstatus_' + bIndex ).is(":visible")) {
			 var msgDiv = page.find('#materialstatus_err_' +bIndex);
			 msgDiv.attr('style', 'display:block');
			 if ( !hasErrors )
			 	hasErrors = true;
		 }
	 }
	 var atdRequired = getCaptureActualTransactionDate($currentOperation,$config);
	if (atdRequired == 2) {
		var actualTransactionDate = page.find('#actualTransactionDate').val();
		if (!actualTransactionDate || actualTransactionDate == ''){
			var msgDiv = page.find('#actualTransactionDate_error');
			msgDiv.attr('style', 'display:block');
			msgDiv.html('<font style="color:red;font-size:8pt;">' + $messagetext.enteractualdate + '</font>')
			hasErrors = true;;
		}
	 }


	 return !hasErrors;
}

// Save batch form data
function saveBatchFormData( mid, nextUrl ) {
	var page = $( '#batchinfo' );
	if ( !isBatchMetadataValid( mid, page ) )
		return;
	// Get local modifications to mid
	var modifiedMaterial = getLocalModifications( mid, $currentOperation, $entity );
	if ( !modifiedMaterial ) {
		modifiedMaterial = {};
		modifiedMaterial.q = 0;
	}
	// Get linked entity, if any
	var linkedEntity = getSelectedLinkedEntity($currentOperation, $entity);
	// Get batch metadata from the form
	var batchNumber = page.find('#batchNumber').val().trim().toUpperCase(); // making batch number uppercase all the time (in case it was not done earlier)
	var batchExpiryDate = page.find('#batchExpiryDate').val();
	var manufacturerName = page.find('#manufacturerName').val().trim();
	var manufacturedDate = page.find('#manufacturedDate').val();
	var materialStatus = page.find('#materialstatus_new').val();
	var bQuantity = page.find('#quantity').val();

	var b = createBatchObject( batchNumber,batchExpiryDate,manufacturerName,manufacturedDate,bQuantity, null, null, true ,materialStatus);
	if (!modifiedMaterial.bt)
		modifiedMaterial.bt = [];
	var isReplaced = false;
	// Check if this batch already exists (i.e. was created and saved, but edited before sending)
	for ( var i = 0; i < modifiedMaterial.bt.length; i++ ) {
		if ( batchNumber == modifiedMaterial.bt[i].bid ) {
			modifiedMaterial.q -= parseInt(modifiedMaterial.bt[i].q); // decrement from the total quantity entered (displayed in material listing view)
			modifiedMaterial.bt[i] = b;
			isReplaced = true;
			break;
		}
	}
	if ( !isReplaced )
		modifiedMaterial.bt.push(b);
	// Increment total quantity entered for this material, across new/updated batches
	modifiedMaterial.q += parseInt( b.q );
	//Reset field changed field
	page.find( '#fieldchange' ).val('');
	// Store modifications
	storeLocalModifications($currentOperation, $entity, $uid, mid, modifiedMaterial, linkedEntity); // don't change linked entity Id, if present - so pass null
	// Go to next URl
	$.mobile.changePage( nextUrl );
}

// Does this batch already exist
function hasBatch( mid, bid ) {
	var material = $materialData.materialmap[ mid ];
	if ( !material || !material.bt || material.bt.length == 0 )
		return false;
	for ( var i = 0; i < material.bt.length; i++ ) {
		if ( material.bt[i].bid.toUpperCase() === bid.toUpperCase() )
			return true;
	}
	return false;
}

//Delete date entered
function deleteDate(dateField) {
	// Get the quantity fields
	var picker = $( dateField ).mobipick();
	//var picker = page.find('#actualTransactionDate');
	if (picker)
	picker.mobipick("option", "date", null).mobipick("updateDateInput");
}

// Validate a batch number and set the error, if any
function validateBatchNumberOnBlur( mid, batchNumberField ) {
	var bid = batchNumberField.value.toUpperCase();
	if (bid != null && bid.length > 0)
	if (bid.indexOf("\"") >= 0){
		showBatchNumberQuotesError();
	}
	if ( $currentOperation == 'es' && hasBatch( mid, bid ) )
		showBatchNumberExistsError();
	batchNumberField.value = bid;
}

// Delete a batch item (that might have been just newly entered)
function deleteBatchItem( mid, bid, nextUrl ) {
	// Delete this
	var batchId = decodeURIComponent(bid);
	deleteLocalNewBatchData( $uid, $currentOperation, mid, batchId, 0 );
	// Change page
	$.mobile.changePage( nextUrl );
}



function isEEFOValid( pageId) {
	if ( !( $currentOperation == 'ei' || $currentOperation == 'ts' || isOperationOrder($currentOperation)) )
		return true;
	var page = pageId;
	// Get the quantity fields
	var quantityFields = page.find( 'input[id^="quantity_"]' );
	if ( !quantityFields || quantityFields.length == 0 || quantityFields.length == 1 ) // EEFO applies only if there is more than 1 batch
		return true;
	var prevQuantity = $( quantityFields[0] ).val().trim();
	prevQuantity = ( prevQuantity == '' ? '0' : prevQuantity );
	var bIndex = $( quantityFields[0] ).attr( 'bindex' );
	var prevStock = page.find('#batchstock_' + bIndex).val();
	if (page.find('#batchavlstock_' + bIndex).length > 0)
		prevStock = page.find('#batchavlstock_' + bIndex).val();
	for ( var i = 1; i < quantityFields.length; i++ ) {
		var prevBatchNotFullyAllocated = ( parseFloat( prevQuantity ) < parseFloat( prevStock ) );
		var quantity = $( quantityFields[i] ).val().trim();
		quantity = ( quantity == '' ? '0' : quantity );
		bIndex = $( quantityFields[i] ).attr( 'bindex' );
		var stock = page.find( '#batchstock_' + bIndex ).val();
		if (page.find('#batchavlstock_' + bIndex).length > 0)
			stock = page.find('#batchavlstock_' + bIndex).val();
		if ( parseFloat( quantity ) > 0 ) {
			if ( prevBatchNotFullyAllocated )
				return false;
		} else {
			continue;
		}
		prevQuantity = quantity;
		prevStock = stock;
	}
	return true;
}


function isOrderEEFOValid( pageId, oid) {
	if ( ! isOperationOrder($currentOperation))
		return true;
	var page = pageId;
	// Get the quantity fields
	var quantityFields = page.find( 'input[id^="quantity_"]' );
	if ( !quantityFields || quantityFields.length == 0 || quantityFields.length == 1 ) // EEFO applies only if there is more than 1 batch
		return true;
	var prevQuantity = $( quantityFields[0] ).val().trim();
	prevQuantity = ( prevQuantity == '' ? '0' : prevQuantity );
	var bIndex = $( quantityFields[0] ).attr( 'bindex' );
	var	prevStock = page.find('#batchavlstock_' + bIndex + '_' + oid).val();

	for ( var i = 1; i < quantityFields.length; i++ ) {
		var prevBatchNotFullyAllocated = ( parseFloat( prevQuantity ) < parseFloat( prevStock ) );
		var quantity = $( quantityFields[i] ).val().trim();
		quantity = ( quantity == '' ? '0' : quantity );
		bIndex = $( quantityFields[i] ).attr( 'bindex' );
		var stock = page.find( '#batchavlstock_' + bIndex+ '_'+ oid ).val()
		if ( parseFloat( quantity ) > 0 ) {
			if ( prevBatchNotFullyAllocated )
				return false;
		} else {
			continue;
		}
		prevQuantity = quantity;
		prevStock = stock;
	}
	return true;
}

function saveFormData( mid, nextUrl ) {
	// Get the data from the form
	var page = $('#materialinfo');
	var material = $materialData.materialmap[ mid ];
	var formData = { mid: mid };
	var reason = page.find('#reason');
	if (reason)
		reason = reason.val();
	else {
		modifiedMaterial = getLocalModifications(mid, $currentOperation, $entity);
		if (modifiedMaterial) {
			reason = modifiedMaterial.reason
		}
	}
	if (reason != null )
		formData.reason = reason;
	if (isOperationOrder($currentOperation)){
		var reason = page.find('#irreason_'+mid).val();
		if (reason && reason == $labeltext.others)
		   reason =page.find('#irreasonmsg_'+mid).val();
		if (!reason)
			reason = page.find('#irreasonmsg_'+mid).val();
		if (reason != null )
			formData.reason = reason;
	}
	var materialstatus = page.find('#materialstatus_'+ material.mid);
	if (materialstatus)
		materialstatus = materialstatus.val();
	if (materialstatus != null )
		formData.mst = materialstatus;
	var actualTransactionDate = page.find('#actualTransactionDate')
	if (actualTransactionDate)
	 	actualTransactionDate = actualTransactionDate.val();
	if (actualTransactionDate != null)
	   formData.atd = actualTransactionDate;
	// Short material Id (for use in SMS message, if required)
	if ( material.smid )
		formData.smid = material.smid;
	if ( !material.ben || isOperationOrder( $currentOperation ) ) {
		var quantity = page.find('#quantity').val();
		// Save the data as integer
		quantity = parseInt(quantity);
		formData.q = quantity;
		//opening stock before transaction
		formData.ostk = material.q;
		// Store to local modifications
		var linkedEntity = getSelectedLinkedEntity( $currentOperation, $entity );
		storeLocalModifications( $currentOperation, $entity, $uid, mid, formData, linkedEntity ); // don't change linked entity Id, if present - so pass null
	} else { // batch enabled inventory operation with multiple batch adds/edits
		var batches = [];
		var totalq = 0;
		// Get the batch data from the form, if any
		var quantityFields = page.find( 'input[id^="quantity_"]' );
		if ( quantityFields && quantityFields.length > 0 ) {
			for ( var i = 0; i < quantityFields.length; i++ ) {
				var quantity = $( quantityFields[i] ).val().trim();
				var bIndex = $( quantityFields[i] ).attr( 'bindex' );
				if ( quantity == '' )
					continue;
				var bid = page.find( '#bid_' + bIndex ).val();
				var bexp = page.find( '#bexp_' + bIndex ).val();
				var bmfnm = page.find( '#bmfnm_' + bIndex ).val();
				var bmfdt = page.find( '#bmfdt_' + bIndex ).val();
				var batchOpeningStock = page.find( '#batchstock_' + bIndex ).val();
				var mst = page.find( '#materialstatus_' + bIndex ).val();
				var batch = {
					bid: bid,
					bexp: bexp,
					bmfnm: bmfnm,
					q: quantity,
					ostk: batchOpeningStock
				};
				if ( bmfdt )
					batch.bmfdt = bmfdt;
				if ( mst )
					batch.mst = mst;
				// Add to list
				batches.push( batch );
				// Increment total quantity count
				totalq += parseFloat( quantity );
			}
		}
		// Get newly added batches, if any, in case of stock counts or receipts
		if ( $currentOperation == 'es' || $currentOperation == 'er' ) {
			var modifiedMaterial = getLocalModifications(mid, $currentOperation, $entity);
			if ( modifiedMaterial && modifiedMaterial.bt ) {
				for ( var i = 0; i < modifiedMaterial.bt.length; i++ ) {
					if ( modifiedMaterial.bt[i].new ) {
						batches.push(modifiedMaterial.bt[i]);
						totalq += parseFloat( modifiedMaterial.bt[i].q );
					}
				}
			}
		}
		// Get the expired batch data from the form, if any
		var expquantityFields = page.find( 'input[id^="expquantity_"]' );
		if ( expquantityFields && expquantityFields.length > 0 ) {
			for ( var i = 0; i < expquantityFields.length; i++ ) {
				var quantity = $( expquantityFields[i] ).val().trim();
				var bIndex = $( expquantityFields[i] ).attr( 'bindex' );
				if ( quantity == '' )
					continue;
				var bid = page.find( '#expbid_' + bIndex ).val();
				var bexp = page.find( '#expbexp_' + bIndex ).val();
				var bmfnm = page.find( '#expbmfnm_' + bIndex ).val();
				var bmfdt = page.find( '#expbmfdt_' + bIndex ).val();
				var batchOpeningStock = page.find( '#expbatchstock_' + bIndex ).val();
				var batch = {
					bid: bid,
					bexp: bexp,
					bmfnm: bmfnm,
					q: quantity,
					ostk: batchOpeningStock
				};
				if ( bmfdt )
					batch.bmfdt = bmfdt;
				// Add to list
				batches.push( batch );
				// Increment total quantity count
				totalq += parseFloat( quantity );
			}
		}
		// Update form data for batches
		formData.bt = batches;
		formData.q = totalq;
		// Store to local modifications
		var linkedEntity = getSelectedLinkedEntity($currentOperation, $entity);
		if ( formData.bt.length > 0 )
			storeLocalModifications($currentOperation, $entity, $uid, mid, formData, linkedEntity); // don't change linked entity Id, if present - so pass null
		else
			deleteLocalNewBatchData($uid,$currentOperation,mid,null,0);
	}
	//Reset field changed field
	page.find( '#fieldchange' ).val('');
	// Change page
	if ( nextUrl )
		$.mobile.changePage( nextUrl );
}

// Clear form data
function clearFormData( mid, nextUrl ) {
	// Get the data from the form
		clearLocalModifications( $uid, $currentOperation, mid, true, false );
		// Change page
		if ( nextUrl && nextUrl != '')
		 $.mobile.changePage( nextUrl );
		else {
			var page = $('#materialinfo');
			page.find('#quantity').val('');
			var stockAfterOperationDiv = page.find('#stockAfterOperation');
			if (stockAfterOperationDiv)
				page.find('#stockAfterOperation').attr('style', 'display:none');
			var quantityerrormessageDiv = page.find('#quantityerrormessage');
			if (quantityerrormessage)
				page.find('#quantityerrormessage').attr('style', 'display:none');
			var material = $materialData.materialmap[mid];
			if (material.ben && !isOperationOrder($currentOperation)) {
				if ($batchMetaData) {
					var noOfBatches = $batchMetaData.length;
					if (noOfBatches > 0) {
						for (i = 0; i < noOfBatches; i++) {
							var columnid = 'quantity_' + i;
							$batchMetaData[i].mq = '';
							page.find('#' + columnid).val('');
						}
					}
				}


			}

		}
	}

function isBatchMetadataValid( mid, page ) {
	var batchNumberNode = page.find('#batchNumber');
	var batchExpiryDateNode = page.find('#batchExpiryDate');
	var manufacturerNameNode = page.find('#manufacturerName');
	var quantityNode = page.find("#quantity");
	var hasErrors = false;
	var batchNumber = batchNumberNode.val();
	if (batchNumber == '') {
		addFieldHighlight( batchNumberNode ); /// .addClass("error-highlight");
		var batchErrorNode = page.find( '#batchNumber_error' );
		batchErrorNode.text( $messagetext.enterbatchnumber );
		batchErrorNode.show();
		hasErrors = true;
	} else if ( $currentOperation == 'es' && hasBatch( mid, batchNumber ) ) { // disallow adding a new batch for stock counts, if it already exists
		addFieldHighlight( batchNumberNode );
		showBatchNumberExistsError();
		hasErrors = true;
	} else if  (batchNumber != '' && batchNumber.indexOf("\"") >= 0){
		showBatchNumberQuotesError();
		hasErrors = true;;
	}
	if (batchExpiryDateNode.val() == '') {
		addFieldHighlight( batchExpiryDateNode ); /// .addClass("error-highlight");
		page.find( '#batchExpiryDate_error').show();
		hasErrors = true;
	}
	if ( manufacturerNameNode.val() == '' ) {
		addFieldHighlight( manufacturerNameNode ); /// .addClass("error-highlight");
		page.find( '#manufacturerName_error').show();
		hasErrors = true;
	}
	var quantity = quantityNode.val().trim();
	var quantityErrMsg = '';
	if ( quantity == '' )
		quantityErrMsg = $messagetext.positivenumbergreaterthanzero;
	else
		quantityErrMsg = validateStockQuantity( quantity, null, true, mid );
	// If quantity error, highlight that
	if ( quantityErrMsg && quantityErrMsg != '' ) {
		addFieldHighlight( quantityNode ); /// .addClass("error-highlight");
		var quantityErrNode = page.find('#quantity_error');
		quantityErrNode.text( quantityErrMsg );
		quantityErrNode.show();
		hasErrors = true;
	}
	var materialStatus = page.find('#materialstatus_new').val()
	if ( !materialStatus &&  page.find('#materialstatus_new').is(":visible") ) {
		var msgDiv = page.find('#materialstatus_err_new');
		msgDiv.attr('style', 'display:block');
		hasErrors = true;
	}
	return !hasErrors;
}


function removeFieldHighlight( field ){
	field.removeClass( 'error-highlight' );
}

function addFieldHighlight( field ) {
	field.addClass("error-highlight");
}

function showBatchNumberExistsError() {
	addFieldHighlight( $( '#batchNumber' ) );
	var batchErrorNode = $( '#batchNumber_error' );
	batchErrorNode.text( $messagetext.batchalreadyexists );
	batchErrorNode.show();
}

function showBatchNumberQuotesError() {
	addFieldHighlight( $( '#batchNumber' ) );
	var batchErrorNode = $( '#batchNumber_error' );
	batchErrorNode.text( $messagetext.batchshouldnothavequotes );
	batchErrorNode.show();
}

// Get the entry tip for the form
function getFormDataEntryTip( currentOperation ) {
	if ( currentOperation == 'es' ) // enter stock counts
		return $messagetext.positivenumbergreatenthanorequaltozero;
	else
		return $messagetext.positivenumbergreaterthanzero;
}

// Render the photo review page (params: domainkey, name)
function renderPhotoReview( pageId, params, options ) {
	if ( !$selectedPhotoUri ) {
		console.log( 'No photo selection available' );
		return;
	}
	if ( !$currentPhotoObject || !$currentPhotoObject.domainkey ) {
		console.log( 'Current photo object\'s ID is not available' );
		return;
	}
	// Get the gallery page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Update header title
	setTitleBar( header, $entity.n, false );
	var prevPageId = getPrevPageId( options );
    var pageParams = $lastPageParams;
	var backUrl = prevPageId ;
	if ( prevPageId != null ) {
		if (pageParams && (backUrl == '#materialinfo')) {
			var mid = '';
			if (pageParams.mid)
			 mid = pageParams.mid;
			var pageOffset = '';
			if (pageParams.pageOffset)
			 pageOffset = pageParams.pageOffset;
		    var tag ='';
		    if (pageParams.tag)
		    	tag = pageParams.tag;
		    if (mid)
		    	backUrl = backUrl + '?mid=' +mid  + ( tag ? '&tag=' + tag : '' );
		    if (pageOffset)
		    	backUrl = backUrl + '&o=' + pageOffset;
		} else if ( pageParams && pageParams.ty && pageParams.lkid ) {
			backUrl += '?ty=' + pageParams.ty + '&lkid=' + pageParams.lkid;
		}
		// Update the back URL with the entities page and its offset
		header.find( pageId + 'back' ).attr( 'href', backUrl );
		// Update the Cancel button click/URL in footer
		content.find( '#photoreviewcancel' ).attr( 'href', backUrl );
	}
	// Set the content title
	if ( $currentPhotoObject.name )
		content.find( 'h4' ).empty().append( $labeltext.photo+' - \'' + $currentPhotoObject.name + '\'' );
	// Update the Upload button's action
	var uploadOnClick = 'uploadPhoto(\'' + $currentPhotoObject.domainkey + '\',\'' + backUrl + '\');';
	content.find( '#photoreviewupload' ).attr( 'onclick', uploadOnClick );
	// Update the image
	var img = content.find( '#largeImage' );
	img.attr( 'src', $selectedPhotoUri );
	// Change to page
	$.mobile.changePage( page, options );
	header.find( '#photoreviewback .ui-btn-text').text($buttontext.back);
	page.find( '#photoreviewcancel .ui-btn-text').text($buttontext.cancel);
	page.find( '#photoreviewupload .ui-btn-text').text($buttontext.upload);
}

// Render an entity's metadata and connections
function renderEntityInfo( pageId, params, options ) {
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Get the page offset of previous page, if any
	var backPageOffset = 0;
	if ( params && params.bo )
		backPageOffset = params.bo;
	// Linked entity id, if any
	var linkedEntityId;
	if ( params && params.lkid )
		linkedEntityId = params.lkid;
	// Get the entity type
	var type = 'ents';
	if ( params && params.ty )
		type = params.ty;
	var newEntity = false;
	if ( params && params.ne )
		newEntity = params.ne;
	// Check if relationships are NOT to be shown
	var entity = $entity;
	var kid = null;
	if ( params && params.kid )
		 kid = params.kid;	 
	if ( params && params.action )
		 action = params.action;
	if ( type == 'csts' || type == 'vnds' )
	{
		entity = getRelatedEntity( linkedEntityId, type );
		if (!entity) //new related entity not stored along with entity
			entity = getEntity($uid,linkedEntityId);	
	}
	else if (!entity)
		entity = getEntity($uid,params.kid);
	if ('ne' in entity)
	   if(entity.ne)
		 newEntity = true;
	// Get previous page
	var prevPageId = getPrevPageId( options );
	var headerTxt = $labeltext.entity;
	// Update header and back buttons
	if ( prevPageId != null ) {
		if ( prevPageId == '#myentityrelationships' ) {
			headerTxt =  getRelationshipName( $currentRelationType, false );
			header.find( 'h3' ).empty().append( headerTxt );
			header.find( '#entityinfoback' ).attr( 'href', '#myentityrelationships?ty=' + type + '&o=' + backPageOffset );
		} else if ( prevPageId == '#menu' && type == 'ents'  ) {
			headerTxt =$labeltext.entitydetails;
			header.find( 'h3' ).empty().append( headerTxt );
			var backButton = header.find( '#entityinfoback' );
			backButton.attr( 'href', '#menu?profile=true' );
		}
		else if ( prevPageId == '#setupentity' && type == 'ents'){
			var backButton = header.find( '#entityinfoback' );
			headerTxt = header.find('h3').text();
			backButton.attr( 'href', '#menu?profile=true' );
			//backButton.attr( 'href', '#entities' );
		}
		else if ( prevPageId == '#setupentity'  && ( type == 'csts' || type == 'vnds' )){
			var backButton = header.find( '#entityinfoback' );
			headerTxt = header.find('h3').text();
			header.find( '#entityinfoback' ).attr( 'href', '#myentityrelationships?ty=' + type + '&o=' + backPageOffset );
			//backButton.attr( 'href', '#entities' );
		}
	}
	// Update page title (to correct update browser-displayed title case of HTML5 views in browser)
	page.attr( 'data-title', headerTxt );
	// Set the title bar
	setTitleBar( header, entity.n, false );
	// Get entity profile info.
	var markup = '<div class="ui-bar-c ui-corner-all ui-shadow" style="padding:0.75em;">'; // '<ul data-role="listview" data-inset="true"><li>'
	// Check entity editing permissions for related entity drill down
	var noPermissions = false;
	if ( $currentRelationType  ) {
		var pKid = $entity.kid
		var parentEntity = getParentEntity();
		if ( parentEntity)
		    pKid = parentEntity.kid;
		if (getRelatedEntityPermissions(entity.kid, $currentRelationType, pKid) != $MANAGE_ENTITY)
			noPermissions = true;
	}
	var isEditEntityAllowed = canEditEntity( type ) && (getUserPermissions($uid) != $VIEW_ONLY) &&  !noPermissions;
	// Put the camera icon, if required
	var maxPhotos = 3; // allow 3 photos per entity
	var domainKey = entity.kid;
	var numPhotos = getNumMediaItems( domainKey );
	if ( isDevice() && isEditEntityAllowed && ( numPhotos < maxPhotos ) ) {
		// Set the params globally, in case it is used by photo/media service
		$lastPageParams = params;
		var onClick = 'capturePhoto(\'' + domainKey + '\',\'' + entity.n + '\',' + maxPhotos + ')';
		markup += '<div style="text-align:right"><a href="#" onclick="' + onClick + '"><img src="jquerymobile/icons/camera.png" title="'+$labeltext.photo+'" alt="Photo" /></a></div>';
	}
	// Get the metadata display markup
	markup += '<table data-role="table" data-mode="reflow" class="infotable" style="margin-bottom:0.5%"><tbody>';
	markup += '<tr><th>'+$labeltext.name+':</th><td>' + entity.n + '</td></tr>';
	if (entity.nn)
	 markup += '<tr><th>'+$labeltext.newname+':</th><td>' + entity.nn + '</td></tr>';
	markup += '<tr><th>'+$labeltext.location+':</th><td>';
	if ( entity.sa )
		markup += entity.sa + ', ';
	markup += entity.cty;
	if ( entity.pc )
		markup += ' ' + entity.pc;
	if ( entity.tlk )
		markup += ', ' + entity.tlk;
	if ( entity.dst )
		markup += ', ' + entity.dst;
	if ( entity.ste )
		markup += ', ' + entity.ste;
	if ( entity.lat && entity.lng ) {
		var altText = 'Mapped (' + entity.lat + ', ' + entity.lng + ')';
		markup += ' <img src="jquerymobile/icons/map_pin.png" title="' + altText + '" alt="' + altText + '" />';
	}
	markup += '</td></tr>';
	if ( entity.rtt )
		markup += '<tr><th>'+$labeltext.routetag+':</th><td>' + entity.rtt + '</td></tr>';
	markup += '</tbody></table>';
	// Add the edit entity button
	if ( isEditEntityAllowed ) {
		var editUrl = '#setupentity?action=edit&ty=' + type + '&bo=' + backPageOffset;
		if ( linkedEntityId )
			editUrl +='&kid=' + kid+
			'&lkid=' + linkedEntityId;	
		if (kid)
			editUrl += '&kid=' + kid;	
		if (newEntity)
			editUrl += '&ne='+newEntity;
		var sendnow = 'yes';
		editUrl +='&SendNow='+sendnow;
		markup += '<a href="' + editUrl + '" data-role="button" data-inline="true" data-mini="true" data-theme="b">'+$buttontext.edit+'</a>';
	}
	// Show the photos of this entity, if any
	var photoGalleryMarkup = doGalleryMarkupMagic( entity.kid );
	if ( photoGalleryMarkup != '' ) {
		markup += '<div class="ui-body-d ui-corner-all ui-shadow" style="padding:0.75em;margin-top:2%;">';
		markup += photoGalleryMarkup;
		markup += '</div>';
	}
	// Show users, if present
	if ( entity.us && entity.us.length > 0 )
		markup += getUserDetailsListMarkup( entity.us, type, entity.kid, linkedEntityId );

	markup += '</div>';
	var typeName = $labeltext.entity;
	var uid ;
	if ( params && params.SendNow )
	{
		var onSendNowClick = ' sendWithLocation( function() { sendEntities('+kid+') } ); ';
		markup += '<a data-role="button" data-theme="b" data-inline="true"  onclick="' + onSendNowClick  + '">'+$buttontext.sendnow+'</a>';
	    markup += '<a data-role="button"  href="#menu" data-direction="reverse" data-inline="true">'+$buttontext.cancel+'</a>';
	}
	// Update content part
	content.empty().append( $( markup ) );
	// Enhance the page
	page.page();
	page.trigger('create'); // this will ensure that all internal components are inited./refreshed
	// Update options data url
	options.dataUrl = '#entityinfo?ty=' + type + '&bo=' + backPageOffset;
	if ( linkedEntityId )
		options.dataUrl += '&lkid=' + linkedEntityId;
	if ( kid )
		options.dataUrl += '&kid=' + kid;
	var sendnow = 'yes';
	if (params.SendNow )
		options.dataUrl += '&SendNow='+sendnow;
	// Now call changePage() and tell it to switch to the page we just modified.
	$.mobile.changePage( page, options );
	header.find( '#entityinfoback .ui-btn-text').text($buttontext.back);
}

// Get user details markup
function getUserDetailsListMarkup( users, type,kid, linkedEntityId ) {
	if ( !users || users.length == 0 )
		return '';
	var markup = '<div class="ui-body-d ui-corner-all ui-shadow" style="padding:0.75em;margin-top:2%;">';
	markup += '<table data-role="table" data-mode="reflow" class="infotable" style=";margin-bottom:0.5%;width:100%"><tbody>';
	markup += '<tr><th colspan="3">'+$labeltext.users+'</th></tr>';
	var isAdmin = isAdministrator( $role ); // domain-owner or superuser
	var maxPhotos = 1; // allow 1 photo per user
	for ( var i = 0; i < users.length; i++ ) {
		var user = users[i];
		markup += '<tr><td>';
		// Get the user metadata
		markup += user.fn + ( user.ln ? ' ' + user.ln : '' ) + ' (' + user.uid + ')<br/><font style="font-size:small">' + user.mob + '</font>';
		markup += '</td><td>';
		// Get user photos, if any
		markup += doGalleryMarkupMagic( user.uid );
		markup += '</td><td>';
		// Command/actions, if permissions exist
		var noPermissions = false;
		if ( $currentRelationType  ) {
			var pKid = $entity.kid
			var parentEntity = getParentEntity();
			if ( parentEntity)
				pKid = parentEntity.kid;
			if (getRelatedEntityPermissions(kid, $currentRelationType, pKid) != $MANAGE_ENTITY)
				noPermissions = true;
		}
		if ( canEditUser( user ) && (getUserPermissions($uid) != $VIEW_ONLY) && !noPermissions) { /// isAdmin || $uid == user.uid || compareRoles( $role, user.rle ) > 0 ) { // has admin. privileges, or is same user or has a role higher
			// Add the capture photo icon, if picture-capture source available
			var domainKey = user.uid;
			var numPhotos = getNumMediaItems( domainKey );
			if ( isDevice() && numPhotos < maxPhotos ) {
				var onClick = 'capturePhoto(\'' + domainKey + '\',\'' + user.fn + '\','  + maxPhotos + ')';
				markup += '<a href="#" onclick="' + onClick + '"><img src="jquerymobile/icons/camera.png" alt="Capture Photo" title="'+$labeltext.photo+'" align="right" /></a>';
				markup += '<img src="jquerymobile/icons/placeholder.png" align="right" />';
			}
			// Edit metadata command icon
			var userEditUrl = '#setupentity?action=edit&ty=' + type + '&uid=' + user.uid;
			if ( kid )
				userEditUrl += '&kid=' + kid;
			if ( linkedEntityId )
				userEditUrl += '&lkid=' + linkedEntityId;
			markup += '<a href="' + userEditUrl + '"><img src="jquerymobile/icons/edit.png" alt="'+$buttontext.edit +'" title="'+$buttontext.edit+'" align="right" /></a>';
		}
		markup += '</td></tr>';
	}
	markup += '</tbody></table>';
	markup += '</div>';
	return markup;
}

function isAdministrator( role ) {
	return ( role == 'ROLE_do' || role == 'ROLE_su' );
}


function isOperator( role ) {
	return ( role == 'ROLE_ko' );
}

function compareRoles( role1, role2 ) {
	var r1 = ( role1 == 'ROLE_su' ? 3 : ( role1 == 'ROLE_do' ? 2 : ( role1 == 'ROLE_sm' ? 1 : ( role1 == 'ROLE_ko' ? 0 : -1 ) ) ) );
	var r2 = ( role2 == 'ROLE_su' ? 3 : ( role2 == 'ROLE_do' ? 2 : ( role2 == 'ROLE_sm' ? 1 : ( role2 == 'ROLE_ko' ? 0 : -1 ) ) ) );
	if ( r1 > r2 )
		return 1;
	else if ( r1 < r2 )
		return -1;
	else
		return 0;
}

function doGalleryMarkupMagic(domainKey){
	var markup = '';
	$mediaItems = getObject(domainKey + '.mediaItems');
	var failures = getObject(domainKey + '.failedUploads');
	var onclick = 'pictureClick()';
	if ($mediaItems || failures){
		markup = '<ul class="gallery">';
		if ($mediaItems && $mediaItems.length > 0) {
			markup = '<ul class="gallery">';
			for( var i=0; i<$mediaItems.length; i++){
				var media = $mediaItems[i];
				var localPhotoHandle = getString('media.' + media.id);
				var mediaUrl = '';
				if (isDevice())
				 mediaUrl = $host+'/';
				mediaUrl += media.servingUrl;
				if (localPhotoHandle){
					markup += '<li><a href="' + localPhotoHandle +'" onclick="' + onclick  +  '" rel="external"' + '><img class="galleryImageItem" src="' + localPhotoHandle + '"' + 'id="' + media.id + '" domainKey="' + domainKey+  '"/></a></li>';
				} else {
					markup += '<li><a href="' + mediaUrl +'" onclick="' + onclick  +  '" rel="external"' + '><img class="galleryImageItem" src="' + mediaUrl + '" id="' + media.id + '" domainKey="' + domainKey  +'"/></a></li>';
					//markup += '<li><a href="' + media.servingUrl +'" onclick="' + onclick  +  '" rel="external"' + '><img class="galleryImageItem" src="' + media.servingUrl + '=s128"' + 'id="' + media.id + '" domainKey="' + domainKey  +'"/></a></li>';
				}
			}
		}
		if (failures && failures.length > 0){
			for( var i=0; i<failures.length; i++){
				var localPhotoHandle = failures[i];
				var uploadOnClick = 'uploadRetry(\'' + domainKey + '\',\'' + localPhotoHandle + '\');';
				markup += '<div>';
				markup += '<li><a href="' + localPhotoHandle +'" onclick="' + onclick  + '" rel="external"' + '><img src="' + localPhotoHandle + '"' + ' domainKey="' + domainKey + '" style="border:2px solid #FF0000" /></a>';
				markup += '<a href="#" class="ui-mini-smaller" data-role="button" data-mini="true" onclick="' + uploadOnClick + '">'+$buttontext.upload+'</a></li>';
//				markup += '<li><img src="' + localPhotoHandle + '"' + ' domainKey="' + domainKey + '" style="border:2px solid #FF0000" />';
//				markup += '<a href="#" class="ui-mini-smaller" data-role="button" data-mini="true" onclick="' + uploadOnClick + '">'+$buttontext.upload+'</a></li>';
				markup += '</div>';
			}
		}
		markup += '</ul>';
	} else {
		fetchMediaItems(domainKey);
	}
	return markup;
}

function pictureClick()
{
	$pictureClicked = true;
 }
function refreshPage() {
	$.mobile.changePage(
	    window.location.href,
	    {
	      allowSamePageTransition : true,
	      transition              : 'none',
	      showLoadMsg             : false,
	      reloadPage              : true
	    }
  );
}

// Render orders
function renderOrders( pageId, params, options ) {
	// Get order type & view
	var otype = null;
	var status;
	if ( params.oty )
		otype = params.oty;
	if ( params.st )
		status = params.st;
	var pageOffset = 0;
	if ( params.o )
		pageOffset = parseInt( params.o );
	if ( params.ty )
		$transferOrder = params.ty;

	else
		$transferOrder = null;
	var orders = null;
	if ( otype == null ) { // Check which type of orders exists, if at all
		otype = 'prc';
		orders = getOrders( otype );
		if ( orders == null || orders.length == 0 ) {
			otype = 'sle';
			orders = getOrders( otype );
			if ( orders == null || orders.length == 0 )
				otype = 'prc'; // default to this, if no type available
		}
	} else {
		orders = getOrders( otype );
	}
	// Set the global order type, used in startsWithOrders filter search
	$osearch.oty = otype; 
	// Get number of orders
	var size = 0;
	if ( orders != null )
		size = orders.length;
	// Form page
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Header text
	var headerTxt =  $orderTypes[ otype ] + ' (' + size + ')';
	if ( $transferOrder ){
		var transferType = $labeltext.incoming;
		if ( otype == 'sle')
			transferType = $labeltext.outgoing;
		if (isReleases($config) ){
			headerTxt = $labeltext.releases + ' - ' + transferType  + ' (' + size + ')';
		}
		else {
			headerTxt = $labeltext.transfers + ' - ' + transferType  + ' (' + size + ')';
		}

	}
	// Update the header with the title and number of entities
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to correct update browser-displayed title case of HTML5 views in browser)
	page.attr( 'data-title', headerTxt );
	// Update home button
	if ( $hasMultipleEntities )
		header.find( '#ordershome' ).attr( 'href', '#entities' );
	else
		header.find( '#ordershome' ).attr( 'href', '#menu' );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update the footer with the order type selects
	var footerMarkup = '<a href="#" data-role="button" data-mini="false" data-icon="refresh" onclick="loadOrders(\'' + otype + '\',0, null);">'+$buttontext.refresh+'</a>';
	footer.empty().append( $( footerMarkup ) );
	var oTag;
	oTag = $('#orders').find('#ordertagsearch').val();
	if (oTag || oTag == '')
		$orderTagFilter = oTag;
	else
		oTag = $orderTagFilter;
	var oSearch;
	oSearch = $('#orders').find('#ordersearch').val();
	if (oSearch || oSearch == '')
		$orderSearchFilter = oSearch;
	else
		oSearch = $orderSearchFilter;

	// Update content
	content.empty();
	// Get the status filter
	content.append( $( getOrderControlPanel( otype, size ) ) );
	// Update orders tabbed view
	var ordersView = getOrdersViewByStatus( otype, pageOffset, oTag, oSearch );
	content.append( ordersView );
	// Enhance page
	page.page();
	page.trigger( 'create' );
	// Add the click even for the order type selector
	createOrderTypeSwitcherClickEvents( pageId, params, options );
	// Expand a status collapsible, if status is given
	if ( status )
		ordersView.find( '#orders_' + status ).trigger( 'expand' ); // expand the status; orders list is updated within the expand event handler
	// SEARCH FILTER	
	// Set the list view filter callback function
	for ( var i = 0; i < $orderStates.length; i++ )
		setListViewFilterCallback( 'orders_' + $orderStates[i] + '_ul', startsWithOrders );
	// SEARCH FILTER
	// Set the dataUrl option to the URL of the entities
	options.dataUrl = '#orders?oty=' + otype + '&o=' + pageOffset;
	if ( status )
		options.dataUrl += '&st=' + status;
	if ($transferOrder)
		options.dataUrl += '&ty=trf';
	// Now call changePage() and tell it to switch to the page we just modified.
	$.mobile.changePage( page, options );	
	page.find( '#ordersback .ui-btn-text').text($buttontext.back);
	page.find( '#ordershome .ui-btn-text').text($buttontext.home);
}


// Create order type selector click events to switch between purchase and sales orders
function createOrderTypeSwitcherClickEvents( pageId, params, options ) {
	var button = $('#purchaseorderselector');
	if (button) {
		button.unbind();
		button.click( function (e) {
			params.oty = 'prc';
			$newOrderType = 'prc';
			if (!$transferOrder) {
				if (!isOrdersRefreshed(params.oty))
					loadOrders(params.oty, 0, null);
				else
					renderOrders(pageId, params, options);
			}
			else {
				if (!isTransfersRefreshed(params.oty))
					loadOrders(params.oty, 0, null);
				else
					renderOrders(pageId, params, options);
			}
		});
	}
	button = $('#salesorderselector');
	if ( button ) {
		button.unbind();
		button.click( function(e) {
			params.oty = 'sle';
			$newOrderType = 'sle';
			if (!$transferOrder) {
				if (!isOrdersRefreshed(params.oty))
					loadOrders(params.oty, 0, null);
				else
					renderOrders(pageId, params, options);
			}
			else {
				if (!isTransfersRefreshed(params.oty))
					loadOrders(params.oty, 0, null);
				else
					renderOrders(pageId, params, options);
			}
		});
	}
	var tagSearch = $('#ordertagsearch');
	if ( tagSearch ){
		tagSearch.unbind();
		tagSearch.change ( function(e) {
			renderOrders(pageId, params, options);
		});
	}
	var orderSearch = $('#ordersearch');
	if ( orderSearch ){
		orderSearch.unbind();
		orderSearch.change ( function(e) {
			renderOrders(pageId, params, options);
		});
	}
}

// Get order listview by the given status
function getOrdersViewByStatus( otype, pageOffset, oTag, oSearch ) {
	// Get the orders map by status
	var orders = getOrders( otype );
	var  ordersByStatus = getOrdersByStatus( orders, oTag, oSearch );
	var collapsibleSet = $( '<div data-role="collapsible-set" data-theme="a" data-content-theme="d" data-collapsed-icon="arrow-r" data-expanded-icon="arrow-d"></div>' );
	// Check if any orders exist at all
	if ( !orders || orders.length == 0 ) {
		collapsibleSet.append( '<p>'+$messagetext.noordersavailable+'</p>' );
	} else {
		// Form a collapsible for each status
		for ( var i = 0; i < $orderStates.length; i++ ) {
			var status = $orderStates[i];
			var size = 0;
			//To Do - back orders are not displayed for evin. Use config "dcs" to enable/disable view backorders
			///if ($orderStates[i] == 'bo')
			///	continue;
			if ( ordersByStatus && ordersByStatus[ status ] )
				size = ordersByStatus[ status ].length;
			// Get the collapsible for this status
			var markup = '<div id="orders_' + status + '" data-role="collapsible" offset="' + pageOffset + '" status="' + status + '">';
			markup += '<h3>' + getOrderStatusDisplay( status ) + ' (' + size + ')</h3>';
			markup += '</div>';
			var collapsible = $( markup );
			// Get the markup for the orders list view
			var listviewMarkup = '<ul id="orders_' + status + '_ul" data-role="listview" data-theme="d" data-divider-theme="d"';
			// Add the data filter and placeholder attributes only if the number of orders > $PAGE_SIZE
			if ( size > $PAGE_SIZE )
				listviewMarkup += ' data-filter="true" data-filter-placeholder="'+$messagetext.enterordernumber+'">';
			else
				listviewMarkup += '></ul>';
			// Add listview to collapsible
			collapsible.append( $( listviewMarkup ) );
			// Add a handler for 'expand' event (to show the orders listview)
			collapsible.off('expand').on('expand', function( e, ui ) {
				var thisStatus = $(this).attr( 'status' );
				// Update global status for searching
				$osearch.ost = thisStatus;
				var thisOffset = parseInt( $(this).attr('offset') );
				updateOrdersListview( $(this).find(':jqmData(role=listview)'), otype, thisStatus, thisOffset );
			});
			// Append the collapsible to the set
			collapsibleSet.append( collapsible );
		} // end for
	} // end if
	return collapsibleSet;
}

// Update an orders listview with the list of orders for a given page
function updateOrdersListview( listview, otype, status, pageOffset, idsToAdd ) {
	// Get the orders by status
	var oTag = $orderTagFilter;
	var oSearch = $orderSearchFilter;
	var ordersByStatus = getOrdersByStatus( getOrders( otype ), oTag, oSearch );
	var orders;
	if ( ordersByStatus )
		orders = ordersByStatus[ status ];	
	var size = 0;
	if ( orders )
		size = orders.length;
	var thisOrders = [];
	if ( size == 0 ) {
		listview.empty().append( $( '<li><p> '+$messagetext.noorders+' - ' + getOrderStatusDisplay( status ) + '</p></li>' ) );
	} else {
		// Render orders list
		// Get the orders to be rendered
		if ( !idsToAdd || idsToAdd == null ) {
			// Set the listview navigation bar
			var range = setListviewNavBar( listview, pageOffset, size, status, function() {
				updateOrdersListview( listview, otype, status, ( pageOffset - 1 ), null );
			}, function() {
				updateOrdersListview( listview, otype, status, ( pageOffset + 1 ), null );
			} );
			var start = range.start;
			var end = range.end;
			// Get the list of orders for rendering on this page
			for ( var i = ( start - 1 ); i < end; i++ ) // get the subset of orders for this page
				thisOrders.push( orders[i] );
		} else {
			for ( var i = 0; i < size; i++ ) {
				var order = orders[i];
				if ( $.inArray( order.tid, idsToAdd ) != -1 )
					thisOrders.push( order );
			}
		}
		// Render orders for this page
		for ( var i = 0; i < thisOrders.length; i++ )
			listview.append( $( getOrderListItemMarkup( thisOrders[i], otype, status, pageOffset ) ) );
	}
	// Refresh the listview
	listview.listview( 'refresh' );
}

// Get an order's list entry markup
function getOrderListItemMarkup( order, otype, status, pageOffset ) {
	var url = '#orderinfo?oid=' + order.tid + '&oty=' + otype + '&bo=' + pageOffset;
	if ( status )
		url += '&st=' + status;
	var markup = '<li id="orders_' + order.tid + '"><a href="' + url + '"><h3>';
	// Get the order's title
	markup += getOrderTitle( order, otype );
	var size = 0;
	if ( order.mt )
		size = order.mt.length;
	markup += '</h3><span class="ui-li-count" style="font-size:12pt">' + size + '</span>'; // number of items in order
	markup += '<p>';
	markup += '<strong>' + getOrderStatusDisplay( order.ost )+ '</strong>, '+$labeltext.created +' ' +order.t;
	// Add local modifications, if any
	if ( isOrderEdited( order ) )
		markup += ' <font style="color:red">(modified)</font>';
	markup += '</p></a></li>';
	return markup;
}

// Get the title of an order
function getOrderTitle( order, otype ) {
	//var title ='Order ' + order.tid;
	var title = order.tid;
	if ( otype == 'prc' && order.vid ) {
		var vendor = getRelatedEntity( order.vid, 'vnds' );
		if ( vendor != null )
			title += '<br /><font style="font-size:small">'+ vendor.n + ', ' + vendor.cty + '</font>';
		else
		    {
		     var defaultVendor = getDefaultVendor(order.vid);
		     if (defaultVendor)
		       title += '<br /><font style="font-size:small">'+defaultVendor.n + ', ' + defaultVendor.cty + '</font>';
		    }
	} else if ( otype == 'sle' ) {
		var customer = getRelatedEntity(order.kid, 'csts');
		if (customer != null) {
			title += '<br /><font style="font-size:small">'+customer.n + ', ' + customer.cty + '</font>';
		}
	}

	return title;
}

// Get the order filter selectors
function getOrderControlPanel( orderType, size ) {
	// Get the markup for purchase/sales
	var otype = orderType;
	if ( !otype || otype == null )
		otype = 'prc';
	var isPurchaseOrder = (otype == 'prc');
	var ordersUrl = '#orders?op=vo&oty=';
	if ($transferOrder)
		ordersUrl += '&ty=trf';
	var purchaseOrderChecked = ( isPurchaseOrder ? 'checked' : '' );
	var salesOrderChecked = ( isPurchaseOrder ? '' : 'checked' );
	var markup = '<div id="orderselectordiv" data-role="fieldcontain">';
	var labelTextSales = $labeltext.sales;
	var labelTextPurchases = $labeltext.purchases;
	if ( $transferOrder ){
		labelTextSales = $labeltext.outgoing;
		labelTextPurchases = $labeltext.incoming;
	}
	markup += '<fieldset id="radiogroupordertypeselector" data-role="controlgroup" data-type="horizontal" data-mini="true">';
	markup += '<input type="radio" name="ordertypeswitcher" id="purchaseorderselector" ' + purchaseOrderChecked + '/>';
	markup += '<label for="purchaseorderselector">' + labelTextPurchases + '</label>';
	markup += '<input type="radio" name="ordertypeswitcher" id="salesorderselector" ' + salesOrderChecked + '/>';
	markup += '<label for="salesorderselector">'+ labelTextSales +'</label>';
	markup += '</fieldset>';
	markup += '</div>';
	if ( $config.otg ) {
		markup += getOrderTagSelectMarkup(null, 'ordertagsearch');
	}
	var orderSearchValue = '';
	if ( $orderSearchFilter )
		orderSearchValue = $orderSearchFilter;
	markup += '<input type="search" name="ordersearch" id="ordersearch" placeholder="' + $messagetext.enterordernumber + '" value="' + orderSearchValue + '">';
	return markup;
}

// Create the layout-view for switching between primary-entity and managed-entities
function getPrimaryEntitySwitcherMarkup( isPrimaryEntity, primaryEntityId ) {
	if ( isPrimaryEntity && $entity.kid != primaryEntityId ) // only show switcher for primary entity and entity list
		return '';
	var primaryEntityChecked = ( isPrimaryEntity ? 'checked' : '' );
	var otherEntitiesChecked = ( isPrimaryEntity ? '' : 'checked' );
	var primaryEntityOnClick = ( isPrimaryEntity ? '' : 'renderPrimaryEntity(\'' + primaryEntityId + '\');' );
	var otherEntitiesOnClick = ( isPrimaryEntity ? '$.mobile.changePage( \'#entities\' );' : '' );
	var markup = '<div data-role="fieldcontain">';
	markup += '<fieldset data-role="controlgroup" data-type="horizontal" data-mini="true">';
	markup += '<input type="radio" name="primaryentityswitcher" id="primaryentityselector" ' + primaryEntityChecked + ' onclick="' + primaryEntityOnClick + '" />';
	markup += '<label for="primaryentityselector">'+$labeltext.myentity+'</label>';
	markup += '<input type="radio" name="primaryentityswitcher" id="otherentitiesselector" ' + otherEntitiesChecked + ' onclick="' + otherEntitiesOnClick + '" />';
	markup += '<label for="otherentitiesselector">'+$labeltext.otherentities+'</label>';
	markup += '</fieldset>';
	markup += '</div>';
	return markup;
}

// Render orders
function renderOrderInfo( pageId, params, options ) {
	var page = $(pageId);
	// Get the page header and content
	var header = page.children(':jqmData(role=header)');
	var content = page.children(':jqmData(role=content)');
	var footer = page.children(':jqmData(role=footer)');
	// Get the order parameters
	var oid = params.oid;
	var otype = params.oty;
	if (otype)
		$newOrderType = otype;
	var status;
	if (params.st)
		status = params.st;
	var backPageOffset = 0;
	if (params.o)
		backPageOffset = params.o;
	var isEdit = ( params.edit && params.edit == 'true' && (getUserPermissions($uid) != $VIEW_ONLY)); // Edit mode, i.e. show the edit form to allow changing quantities, etc.
	var isPayment = false;
	if (!$disableOrderPrice)
		isPayment = ( params.pay && params.pay == 'true' && (getUserPermissions($uid) != $VIEW_ONLY)); // payment mode, for un-editable orders (shipped state onwards)
	// Get the order
	var order = getOrder(oid, otype);

	if (!order || order == null) {
		showDialog(null, $messagetext.unabletofindorderid + ' ' + oid, getCurrentUrl());
		return;
	}
	if (!status) // take it from this order (this is passed to the back URL)
		status = order.ost;
	// Flag to indicate that his order is already edited
	var orderIsEdited = isOrderEdited(order); // whether this is an already edited order, ready to send
	// Update the header titles
	var headerTxt = $labeltext.order + ' ' + oid;
	if ( $transferOrder ) {
		if (isReleases($config))
			headerTxt = $labeltext.release + ' ' + oid;
		else
			headerTxt = $labeltext.transfer + ' ' + oid;
	}
	header.find('h3').empty().append(headerTxt);
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr('data-title', headerTxt);
	// Update header title
	setTitleBar(header, $entity.n, false);
	// Update back button URL
	var backUrl = '#orders?oty=' + otype + '&o=' + backPageOffset;
	if ($transferOrder)
		backUrl += '&ty=trf';
	if (status)
		backUrl += '&st=' + status;
	if ($transferOrder)
		backUrl += '&ty=trf';
	header.find('#orderinfoback').attr('href', backUrl);
	// Update footer (to include order-related commands)
	footer.empty();
	if (!isEdit && (getUserPermissions($uid) != $VIEW_ONLY))
		footer.append($(getOrderFooterMarkup(order, otype, orderIsEdited, isPayment)));
	// Update the content
	var markup = '<ul data-role="listview" data-inset="true"><li>';
	markup += '<table data-role="table" data-mode="reflow" class="infotable"><tbody>';
	markup += '<tr><th style="vertical-align:middle">' + $labeltext.status + ':</th><td>';
	// Show order status change button, if needed
	var nextStates = $orderStateTransitions[order.ost];
	if (!isEdit && !orderIsEdited && nextStates && (getUserPermissions($uid) != $VIEW_ONLY))
		markup += '<table><tr><td style="border-bottom:0;padding:0 5px 0 0;white-space:nowrap;vertical-align:middle">';
	markup += getOrderStatusDisplay(order.ost);
	if (order.osht && order.ost == 'cm')
		markup += '</br><font style="color:black;font-size:8pt;font-weight:normal">' + order.osht + '</font>';
	else if (order.rsnco && order.ost == 'cn') {
		markup += '<br/><font style="font-weight:normal;font-size:small;color:lightslategray">(' + order.rsnco + ')</font>';
	}
	if (!isEdit && !orderIsEdited && nextStates && (getUserPermissions($uid) != $VIEW_ONLY))
		markup += '</td><td style="border-bottom:0;padding:0 0 0 0">' + getOrderStatusSelector(order, otype, nextStates, null) + '</td></tr></table>';

	markup += '</td></tr>';
	if (isEdit && ( $config.otg || order.tg ))//display order tag
		markup += '<tr><th>' + $labeltext.ordertag + '</th><td style=" max-width: 200px;  text-overflow: ellipsis;  white-space: nowrap;  overflow: hidden;">' + getOrderTagSelectMarkup(order, 'ordertags') + '</td></tr>';
	else {
		if (order.tg) { //order tag
			markup += '<tr><th style="vertical-align:middle">' + $labeltext.tag + ':</th>';
			markup += '<td style=" max-width: 200px;word-wrap: break-word;">' + order.tg + '</td></tr>';
		}
	}
	// Get currency
	var currency = ( order.cu ? order.cu : ( $entity.cu ? $entity.cu : $config.cu ) );
	if (order.tp && order.tp != '' && !$disableOrderPrice) {
		var price = parseFloat(order.tp);
		if (price != 0) {
			markup += '<tr><th>' + $labeltext.price + ':</th><td><font style="color:green">' + ( currency ? currency + ' ' : '' ) + getFormattedPrice(order.tp);
			if ($entity.tx && $entity.tx != 0)
				markup += ' (' + getFormattedPrice($entity.tx) + '% ' + $labeltext.tax + ' )';
			markup += '</font></td></tr>';
		}
	}
	// Show the pymt
	if (!$disableOrderPrice) {
		var val = '';
		var curPaid = ( order.pymt ? parseFloat(order.pymt) : 0 );
		var isDirty = ( order.dirty && order.dirty.pymt );
		if (isDirty) {
			val += '<font style="color:red">' + ( currency ? currency + ' ' : '' ) + ( curPaid + parseFloat(order.dirty.pymt) ) + '</font>';
			val += ' (' + $labeltext.earlier + ' ' + curPaid + ', ' + $labeltext.added + '<font style="color:red"> ' + order.dirty.pymt + '</font>)';
		} else {
			val = '<font style="color:blue">' + ( currency ? currency + ' ' : '' ) + curPaid + '</font>';
		}
		markup += '<tr><th>' + $labeltext.paid + ':</th><td>' + val;
		if (isEdit || isPayment) { // show additional payment entry field
			markup += '<br/>' + $labeltext.addpayment + ': ';
			markup += '<input type="number" id="payment" class ="inputfieldchange" placeholder="' + $messagetext.enteranumber + '" value="';
			if (order && order.dirty && order.dirty.pymt) {
				markup += order.dirty.pymt;
			}
			markup += '"/>';
		}
		markup += '</td></tr>';
	}
	// Other order attribtues
	var userName = '';
	if ( order.cbn )
		userName = order.cbn + ' ';
	markup += '<tr><th>' + $labeltext.created + ':</th><td>'+  order.t ;
	if (userName)
		markup += '</br><font style="color:lightslategray;font-size:8pt;font-weight:normal">' + userName + '</font>';
	markup+= ' </td></tr>';
	if (order.ut)
		markup += '<tr><th>' + $labeltext.updated + ':</th><td>' + order.ut + '</td></tr>';
	var customer;
	var relatedEntity;
	if (otype == 'prc')
		customer = $entity;
	else
		customer = getRelatedEntity(order.kid, 'csts');
	if ($transferOrder && !customer){
		customer = {};
		customer.n = order.knm;
		customer.cty = order.kcty;
	}
	if (customer) {
		markup += '<tr><th>' + $labeltext.customer + ':</th><td>' + customer.n + ', ' + customer.cty + '</td></tr>';
		relatedEntity = customer.n;
	}
	var vendor;
	var defaultVendor;
	if (otype == 'prc') {
		vendor = getRelatedEntity(order.vid, 'vnds');
		if (!vendor && !$transferOrder) {
			defaultVendor = getDefaultVendor(order.vid);
		}
		else if(!vendor && $transferOrder){
			vendor = {};
			vendor.n = order.vnm;
			vendor.cty = order.vcty;
		}
	}
	else
		vendor = $entity;
	if (vendor) {
		markup += '<tr><th>' + $labeltext.vendor + ':</th><td>' + vendor.n + ', ' + vendor.cty + '</td></tr>';
		relatedEntity = vendor.n;
	}
	else if (defaultVendor) {
		markup += '<tr><th>' + $labeltext.vendor + ':</th><td>' + defaultVendor.n + ', ' + defaultVendor.cty + ' </td></tr>';
		relatedEntity = defaultVendor.n;
	}
	//Transporter
	/*if (order.trsp)
	 markup += '<tr><th>' + $labeltext.transporter + ':</th><td>' + order.trsp + ' </td></tr>';*/
	if (order.eta && order.eta != '')
		markup += '<tr><th>' + $labeltext.estimateddateofarrival + ':</th><td>' + getDateOnly(order.eta) + '</td></tr>';


	if (isEdit ) {
		if (otype == 'prc') {
			markup += '<tr><th>' + $labeltext.requiredbydate + ':</th><td>';
			markup += '<a href="#" data-role="button" data-icon="delete" data-iconpos="notext"  class ="inputfieldchange" style="float: right;margin-top: -.01em;" onclick ="deleteDate(\'#rbd\')"></a>';
			markup += '<div style="overflow: hidden; padding-right: .75em;">';
			var rbdValue = '';
			var today = getStrFromDate(new Date());
			var rbd;
			if (order.dirty != null && order.dirty.rbd != null)
				rbd = order.dirty.rbd;
			else if (order.rbd)
				rbd = order.rbd;
			if (rbd) {
				//rbdValue = getFourDigitDateFormat(rbd, $userDateFormat);
				rbdValue = rbd;
				var d = getDateWithLocaleFormat(rbd,$userDateFormat);
				if ( d < new Date())
					today = d;
			}
			markup += '<input name="rbddatepicker" class ="mobidatepicker inputfieldchange" id="rbd"  onfocus="$(\'#rbd_error\').hide()"  min="' + today + '"  style= "margin-top: -.02em;" type="text"  value="' + ( rbdValue ? getDatePickerFormat(decodeURIComponent(rbdValue), $userDateFormat) : '' ) + '" />';
			markup += '<span id="rbd_error" style="display:none;"> <font style="color:red;font-size:small">' + $messagetext.requireddatecannotbeearlierthantoday + '</font></span>';
			markup += '</div>';
			markup += '</td></tr>';

		}
		else if (otype == 'sle') {
			var rbd = order.rbd;
			if (rbd) {
				markup += '<tr><th>' + $labeltext.requiredbydate + ':</th><td>';
				markup += '<font style="color:' + rbdColor + '">' + getDateOnly(rbd) + '</font></td></tr>';
			}
		}
		var rid = '';
		if (order.dirty && order.dirty.rid != null)
			rid = order.dirty.rid;
		else if (order.rid && order.rid != '')
			rid = order.rid;

		markup += '<tr><th>' + $labeltext.referenceid + ':</th><td>';
		markup += '<input type="text" name="referenceid" class ="inputfieldchange" id="referenceid"  value="' + rid + '">' + '</td></tr>';

	}
	else {

		if ((order.rbd && order.rbd != '') || (order.dirty && order.dirty.rbd != null)) {
			var rbd = order.rbd;
			var rbdColor = "black";
			if ( order.dirty != null && order.dirty.rbd != null) {
				rbd = order.dirty.rbd;
				rbdColor = "red";
			}
			markup += '<tr><th>' + $labeltext.requiredbydate + ':</th><td>';
			markup += '<font style="color:' + rbdColor +  '">'+ getDateOnly(rbd) + '</font></td></tr>';
		}
		if ( order.rid || (order.dirty && order.dirty.rid != null)) {
			var rid = order.rid;
			var ridColor = "black";
			if (order.dirty != null && order.dirty.rid != null) {
				rid = order.dirty.rid;
				ridColor = "red";
			}
			markup += '<tr><th>' + $labeltext.referenceid + ':</th><td><font style="color:'+ ridColor  +'">' + rid + '</font></td></tr>';
		}

	}



	if (isEdit) { // editing mode
		// Payment option
		if ($config && $config.popt) {
			var val = null;
			if (order.dirty && order.dirty.popt)
				val = order.dirty.popt;
			else
				val = order.popt;
			if (!$disableOrderPrice)
				markup += '<tr><th>' + $labeltext.paymentoption + ':</th><td>' + getSelectorMarkup('paymentoption', $config.popt.split(','), val) + '</td></tr>';
		} else if (order.popt && !$disableOrderPrice) {
			markup += '<tr><th>' + $labeltext.paymentoption + ':</th><td>' + order.popt + '</td></tr>';
		}
		markup += '<input type="hidden" id="fieldchange" value="" />';
		// Package size
		/*if ($config && $config.pksz) {
			var val = null;
			if (order.dirty && order.dirty.pksz)
				val = order.dirty.pksz;
			else
				val = order.pksz;
			markup += '<tr><th>' + $labeltext.packagesize + ':</th><td>' + getSelectorMarkup('packagesize', $config.pksz.split(','), val) + '</td></tr>';
		} else if (order.pksz) {
			markup += '<tr><th>' + $labeltext.packagesize + ':</th><td>' + order.pksz + '</td></tr>';
		}*/

	}
	else { // regular mode
		// Payment option
		if (order.popt || ( order.dirty && order.dirty.popt )) {
			var val = '';
			var isDirty = ( order.dirty && order.dirty.popt );
			if (isDirty)
				val += '<font style="color:red">' + order.dirty.popt + '</font>';
			if (order.popt) {
				if (isDirty)
					val += ' [' + $labeltext.earlier + ': ';
				val += order.popt;
				if (isDirty)
					val += ']';
			}
			if (!$disableOrderPrice)
				markup += '<tr><th>' + $labeltext.paymentoption + ':</th><td>' + val + '</td></tr>';
		}
		// Package size
		if (order.pksz || ( order.dirty && order.dirty.pksz )) {
			var val = '';
			var isDirty = ( order.dirty && order.dirty.pksz );
			if (isDirty)
				val += '<font style="color:red">' + order.dirty.pksz + '</font>';
			if (order.pksz) {
				if (isDirty)
					val += ' [' +  $labeltext.earlier + ': ';
				val += order.pksz;
				if (isDirty)
					val += ']';
			}
			markup += '<tr><th>' + $labeltext.packagesize + ':</th><td>' + val + '</td></tr>';
		}

	}
	// Message
	//if (order.ms && order.ms != '')
	//	markup += '<tr><th>' + $labeltext.message + ':</th><td>' + order.ms + '</td></tr>';

	//markup += '<tr><th>' + $labeltext.message + ':</th><td>' + order.ms + '</td></tr>';
	markup += '</tbody></table>';
	markup += '</li></ul>';
	if (!isEdit && order.cmnts && !orderIsEdited) {
		var url = "#ordercomments?oid=" + oid + '&oty=' + otype + '&o=' + backPageOffset;
		markup += '<a href="' + url + '" data-role="button" data-mini="true" data-inline="true"  data-icon="false">' + $labeltext.viewcomments + '</a>';
	}
	// Show the message field and any other form elements here
	if (orderIsEdited && !isEdit) {
		var orderSize = 0;
		if (order && order.mt)
			orderSize = order.mt.length;
		markup += getOrderReviewForm(order, orderSize, false);
	}

	// Show the items ordered
	var size = 0;
	if (order.mt)
		size = order.mt.length;
	// Add the display of number of items and add/edit buttons
	if (size == 0) {
		markup += '<ul data-role="listview" data-inset="true" >';
		markup += '<li>' + $messagetext.noitemsinorder + '</li></ul>';
	} else {
		// Render materials
		markup += '<ul data-role="listview" data-inset="true" data-icon="false">';
		markup += '<li data-role="list-divider">' + size + ' ' + $labeltext.items + ' </li>';
		for (var i = 0; i < size; i++) {
			var item = order.mt[i];
			var material = $materialData.materialmap[item.mid];
			var itemName = $labeltext.unknown;//'Unknown';
			if (material)
				itemName = material.n;
			else {
				itemName = $messagetext.nosuchmaterial;
				markup += '<li>' + ' ' +itemName;
				continue;
			}
			markup += '<li>' ;
			var itemInfoUrl = '#orderiteminfo?oid=' + order.tid  + '&oty='+ otype + '&mid='+ item.mid + '&ost='+order.ost+ '&o='+backPageOffset;
			if (!isEdit && !orderIsEdited)
				markup += '<a href="' + itemInfoUrl +'">' ;
			 markup += itemName;
			//markup += '<li>' +  itemName;
			if (!isEdit) {
				if ( (item.roq != -1 && item.q != item.roq) || (item.oq != null && item.q != item.oq ) )
					qtyColor = $discrepancyColor;
				else
					qtyColor = "black";
				/*markup += '<span id style="font-size:8pt;float:right;border-radius:25px; border:2px solid #D3D3D3;padding:5px;">' ;
				if (item.nq != null) {
					var itemEdited = ( !item.q || ( parseFloat(item.nq) != parseFloat(item.q) ) );
					if (itemEdited) { // ensure that red is shown only for edited quantities, i.e those that are different from earlier
						markup += '<font style="color:red">' + getFormattedNumber(item.nq) + '</font>';
						if (item.q)
							markup += ' ('+ '<font style="color: ' + qtyColor + '" >' + + getFormattedNumber(item.q) + '</font>'+ ')' ;
					} else if (item.q) {
						markup += '<font style="color: ' + qtyColor + '" >' + getFormattedNumber(item.q) + '</font>';
					}
				} else {
					markup += '<font style="color:' + qtyColor + '" >' +getFormattedNumber(item.q) + '</font>';
				}

				markup +=  '</span>';*/
			}
			var eoq = -1 ;
			if ( item.roq != null )
				eoq = item.roq;
			else
				eoq = getFormattedNumber(getEoq(item.mid), order.kid,otype );
			var eoqstr =$labeltext.recommendedquantity+': '+ eoq;
			//var reason = item.rsn;
			var reason = item.rsnirq;
			if ( material && material.rp && !$disableOrderPrice )
				markup += ', <font style="color:green">' + ( currency ? currency + ' ' : '' ) + getFormattedPrice( material.rp ) + '</font>';
			if ( material && material.tx && material.tx != 0 && !$disableOrderPrice )
				markup += ' [<font style="color:blue">' + getFormattedPrice(material.tx) + '% tax</font>]';

			if ( isEdit ) { // edit mode
				var quantityId = 'q_' + item.mid;
				markup += '<input type="number" min="0" oninput="maxLengthCheck(\'' + quantityId + '\')" class ="inputfieldchange ';
				//if ( isQtyAllocatedForMaterial(item, material.bt) != '')
				//	markup += 'ui-disabled ';
				markup += '" id="' + quantityId + '" value="';
				if (item.nq != null) {
					markup += getFormattedNumber(item.nq);
				}
				else {
					markup += getFormattedNumber(item.q);

				}
				markup += '"/>';
				var qtyErrMsgId = "qtyerrmsg_" + item.mid;
				markup += '<div id="' + qtyErrMsgId + '" style="display:none"><font style="color:red;font-size:8pt;font-weight:normal;white-space:initial"></font></div>';
				// Invalid quantities (< 0 or -ve)
				var invalidQuantity = false;
				if (item.nq != null) {
					if (item.nq < 0) {
						markup += '<font style="font-weight:normal;font-size:10pt;color:red">';
						markup += $messagetext.positivenumbergreaterthanzero;
						markup += '</font><br/>';
						invalidQuantity = true;
					} else if (!isInteger(item.nq)) {
						markup += '<font style="font-weight:normal;font-size:10pt;color:red">';
						markup += $messagetext.notinteger;
						markup += '</font><br/>';
						invalidQuantity = true;
					}
				}
				//Recommended order qty
				if (item.mid && !invalidQuantity ) {
					//Recommended order qty
					if ((eoq >= 0) ) {
						/*if (item.nq != null && item.nq != eoq) {
							markup += '<font style="font-weight:normal;font-size:10pt;color:orange">';
							markup += $labeltext.quantity + ' (' + getFormattedNumber(item.nq) + ') ' + $messagetext.differentfromrecommendedqty + ' ' + eoq + '</font>';
							markup += '<br/>';
						}
						else { // new; not edited
							markup += '<font style="font-weight:normal;font-size:10pt;color:orange">';
							markup += ' ' + eoqstr;
							markup += '</font>';
							//markup += '<br/>';
						}*/
						if (!reason) {
							reason = '';
							markup += '<div id="reasonDiv_' + item.mid + '"  style="display:none"><font style="color:lightslategray;font-size:8pt;font-weight:normal">(' + reason + ')</font></div>';
						}
						else
							markup += '<div id="reasonDiv_' + item.mid + '" style="display:none"><font style="color:lightslategray;font-size:8pt;font-weight:normal">(' + reason + ')</font></div>';
						markup += '<input type="hidden" name="eoq" id="eoq_' + item.mid + '"value="' + eoq + '"/>';
						//markup += '<input type="hidden"  name="reason" id="reason_' + item.mid +'"value="'+reason+'"/>';
					}
				}
				if ( item.nq != null ) {
					markup += '<font style="font-weight:normal;color:red">';
					if (item.q && ( parseFloat(item.q) != parseFloat(item.nq) ))
						markup += '(' + $labeltext.earlier + ' ' + getFormattedNumber(item.q) + ')';
					else if (!item.q)
						markup += '(' + $labeltext.newtext + ')';
					markup += '</font>';
				}
				if (otype == 'sle' ||( otype == 'prc' && order.ost == 'pn')) {
					if (item.mid) {
						if ((eoq >= 0)) {
							var qtyVal = '';
							if (item.nq != null)
								qtyVal = item.nq;
							else
								qtyVal = item.q;
							//markup += getIgnoreRecommendedOrderReasonsMarkup(reason,item.mid);
							markup += getOrderReasonsMarkup($config, 'ir', item.mid, reason, false, false, true);
						}
					}
				}
				var eoReason = item.rsneoq;
				if ( otype == 'sle'  || ( otype == 'prc' && order.ost != 'pn'))
					markup += getOrderReasonsMarkup( $config, 'eo', item.mid, eoReason, false, false, true );
				if ( (otype == 'sle'  || ( otype == 'prc' &&  getRelatedEntityPermissions(order.vid, 'vnds',order.kid ) == $MANAGE_ENTITY) ) && isAutoPostInventory($config) ) {
					var batchAllocateMarkup = '';
					var alqColor = "lightslategray";
					var alq = '';
					if (item.nalq != null) {
						alq = item.nalq;
						alqColor = "red";
						var materialStatus = '';
						if (item.nmst && !item.bt) {
							materialStatus = ' (' + item.nmst + ')';
						}
						else if (item.mst && !item.bt) {
							materialStatus = ' (' + item.mst + ')';
						}
						markup += '<div id= "alloc_' + item.mid + '" <font style="color:' + alqColor + ';font-size:small;font-weight:normal"> ' + $labeltext.allocated + ': ' + alq + materialStatus + '</font></div>';
						if (item.bt) {
							batchAllocateMarkup += getOrderBatchAllocatedMarkup(item.bt, item.mid, otype, order.ost, true);
							if (batchAllocateMarkup)
								markup += batchAllocateMarkup;
						}
					}


					var queryString = '&oty=' + otype + '&st= ' + status + '&o=' + backPageOffset;
					var allocateUrl = '#orderallocate?mid=' + item.mid + '&oid=' + oid + queryString;
					if (item.q != 0) {
						markup += '<div> <a id="allocatebutton_' + item.mid + '" href="' + allocateUrl + '" data-role="button"  class="checkbuttonclick" ';
						markup += '  data-mini="true" data-inline="true" > ' + $buttontext.allocate + '</a>';
						markup += '</div>';
					}
				}
				if ( order.vid ) {
					var relationType = 'vnds';
					var relatedId = order.vid;
					if (otype == 'sle') {
						relationType = 'csts';
						relatedId = order.kid;
					}
					var linkedEntity = {};
					linkedEntity.kid = relatedId;
					linkedEntity.type = relationType;
					markup += getInventoryStockDisplayMarkup( linkedEntity, item.mid);
				}

			}

	    else
		{ // view mode
			/*var originalQty = null;
			if (item.q)
				originalQty = item.q;
			if (item.oq)
				originalQty = item.oq;
			if (originalQty != null) {
				if (item.oq && ((parseFloat(item.oq) != parseFloat(item.q) )) || (item.nq && (parseFloat(item.nq) != parseFloat(item.q) ))) {
					markup += '<div id="eoreasonDiv_' + item.mid + '" style="display:block"><font style="font-weight:normal;font-size:small;color:lightslategray">(' + $labeltext.original + ': ' + originalQty;
					if (item.rsneoq)
					 markup += ', ' + item.rsneoq;
					markup += ')</font></div>';
				}
			}
			if (order.shps) {
				var shpQty = getShippedQty(oid, otype, item.mid);
				if (shpQty) {
					var fulfillColor = "lightslategray";
					markup += '<div><font style="font-weight:normal;font-size:small;color:lightslategray">' + $labeltext.shipped + ': ' + shpQty
					if (item.flq != null)
					{
						if (item.flq != item.q  && item.flq != 0)
							fulfillColor = $discrepancyColor;
						markup += ', ' + $labeltext.fulfilled + ': </font><font style="font-weight:normal;font-size:small;color:' + fulfillColor +'">' + item.flq + '</font>';
					}
					markup += '</div>';
				}
			}
			if (item.rsnpf)
				markup += '<div><font style="font-size:small;color:lightslategray;font-weight:normal;"> ( ' + item.rsnpf + ' )</font></div>';
			if ((eoq >= 0) ) {
				markup += '<div><font style="font-weight:normal;font-size:10pt;color:orange">';
				markup += ' ' + eoqstr;
				markup += '</font>';
				//	markup += '<br/>';
					if (reason)
						markup += '<br/><div id="reasonDiv_' + item.mid + '" style="display:block"><font style="color:lightslategray;font-size:small;font-weight:normal;">(' + reason + ')</font></div>';
				markup += '</div>'
			}*/
				//if (item.rsneoq )
				//	markup += '<div><font style="font-size:small;color:lightslategray;font-weight:normal;"> (' + item.rsneoq + ')</font> </div>';
				if ( item.q != 0)
			     markup += getOrderQuantitesTable(item.mid, order.tid, otype, order.ost, null);
				if ((otype == 'sle' || ( otype == 'prc' &&  getRelatedEntityPermissions(order.vid, 'vnds',order.kid ) == $MANAGE_ENTITY) ) && (order.ost == 'pn' || order.ost == 'cf')  && isAutoPostInventory($config)) {
					var alqColor = "lightslategray";
					var alq = '';
					if (item.nalq != null)
					{
						alq = item.nalq;
						alqColor = "red";
						var materialStatus = '';
						if (item.nmst && !item.bt) {
							materialStatus = ' (' + item.nmst + ')';
						}
						else if ( item.mst && !item.bt) {
							materialStatus = ' (' + item.mst + ')';
						}
						markup += '<div id= "alloc_' + item.mid + '"<font style="color:'+ alqColor +';font-size:small;font-weight:normal;"> ' + $labeltext.allocated + ': ' + alq + materialStatus +  '</font></div>';
						if (item.bt) {
							markup += getOrderBatchAllocatedMarkup(item.bt, item.mid, otype, status, true);
						}

					}

				}
			}
			if (!isEdit && !orderIsEdited)
				markup += '</a>';
			markup += '</li>';

		}
		if ( orderIsEdited && !$disableOrderPrice ) {
			var newPrice = computeOrderPrice( order.mt, true );
			if ( newPrice != '' ) {
				markup += '<li></li>';
				markup += '<li style="float:right;font-weight:bold">'+$labeltext.total+': <font style="color:green">' + newPrice + '</font></li>';
			}
		}
		markup += '</ul>';
		if ( !isEdit  ) {
			markup += getShipmentsMarkup(oid, otype, backPageOffset);
		}

		// Add save/cancel buttons on edit
		if ( isEdit || isPayment ) {
			var thisUrl = '#orderinfo?oid=' + oid + '&oty=' + otype;
			var saveOnClick = 'saveOrderEdits(\'' + oid + '\',\'' + otype + '\',\'' + thisUrl +'\'); ';

			var clearOnClick = 'clearOrderEdits(\'' + oid + '\',\'' + otype + '\'); $.mobile.changePage( \'' + thisUrl + '\' );';
			markup += '<br/>';
			markup += '<a href="#" data-role="button" id="orderdatasavebutton" data-inline="true" data-theme="b" onclick="' + saveOnClick + '">'+$buttontext.save+'</a>';
			if ( orderIsEdited )
				markup += '<a href="#"  id="orderclearallbutton" data-role="button" data-inline="true" onclick="' + clearOnClick + '">'+$buttontext.clearall+'</a>';
			markup += '<a href="' + thisUrl + '" data-role="button" id="orderdatacancelbutton" data-inline="true" class="checkbuttonclick" data-direction="reverse">'+$buttontext.cancel+'</a>';
		}
	}
	if (isEdit) {
		if (order.vid) {
			var relationType = 'vnds';
			var relatedId = order.vid;
			var relatedEntityName = relatedEntity;
			if (otype == 'sle') {
				relationType = 'csts';
				relatedId = order.kid;
				var customerEntity = getRelatedEntity(order.kid, 'csts');
				if (customerEntity)
					relatedEntityName = customerEntity.n;
			}
			var linkedEntity = {};
			linkedEntity.kid = relatedId;
			linkedEntity.type = relationType;
			markup += getFetchRelatedEntityInventoryMarkup( linkedEntity, null );
		}
	}

	content.empty().append( markup );
	// Enhance page
	page.page();
	page.trigger( 'create' );
	// Add an event handler for status change selector, if it exists on page
	var orderStatusSelector = page.find( '#orderstatusselector' );
	if ( orderStatusSelector ) {
		orderStatusSelector.off('change').on( 'change', function() {
			var value = orderStatusSelector.val();
			if ( value == '' )
				return;
			confirmOrderStatusChange( oid, value, otype,  null );

		});
	}

	// Update URL in options
	options.dataUrl = '#orderinfo?oid=' + oid + '&oty=' + otype + '&o=' + backPageOffset;
	if ( status )
		options.dataUrl += '&st=' + status;
	$.mobile.changePage( page, options );
	header.find( '#orderinfoback .ui-btn-text').text($buttontext.back);
	page.find('div[id^="reasonsdiv"]').attr( 'style', 'display:none' );
	page.find('#rbd').mobipick({
		dateFormat: $datePickerFormat,
		locale: getUserLanguage()
	});
}

// Get the order status selector
function getOrderStatusSelector( order, orderType, nextStates, shipId ) {
	// Update the order popup list markup
	var markup = '<select id="orderstatusselector" data-theme="b" data-mini="true">';
	markup += '<option value="">'+$labeltext.change+'</option>';
	var numOptions = 0;
	for ( var i = 0; i < nextStates.length; i++ ) {
		///if ( $role == 'ROLE_ko' ) { // for an entity operator, disable either shipping or fulfillment depending on the type of order
		// For all roles, disable either shipping/confirmation for purchase orders, and fulfillment for sales orders
		//if ( ( orderType == 'prc' && ( nextStates[i] == 'cm'  || nextStates[i] == 'cf' || nextStates[i] == 'fl' ) ) || ( orderType == 'sle' && nextStates[i] == 'fl'  && !$config.eshp) )
		//	continue;
		////}
		if (  orderType == 'prc' ) {
			if (nextStates[i] == 'cf' && ( getRelatedEntityPermissions(order.vid, 'vnds',order.kid ) != $MANAGE_ENTITY))
				continue;
			else if (nextStates[i] == 'cm' &&  (getRelatedEntityPermissions(order.vid, 'vnds',order.kid ) != $MANAGE_ENTITY))
				continue;
			//else if (nextStates[i] == 'cm'  && !shipId)
			//	continue;
			else if ( nextStates[i] == 'cn' && order.ost != 'pn' )
				continue;
			else if (nextStates[i] == 'pn' &&  (getRelatedEntityPermissions(order.vid, 'vnds',order.kid ) != $MANAGE_ENTITY))
				continue;

		}

		if ( orderType == 'sle') {
			// Sales orders cannot be fulfilled without manage entity permissions
			if (nextStates[i] == 'fl' && getRelatedEntityPermissions(order.kid, 'csts', order.vid) != $MANAGE_ENTITY)
				continue;
			/*if (nextStates[i] == 'cn' && (order.ost == 'cm' || order.ost == 'bo') && getRelatedEntityPermissions(order.kid, 'csts', order.vid) != $MANAGE_ENTITY )
			 continue;*/
		}
		//if ( nextStates[i] == 'cf' && shipId)
		//	continue;

		if ( nextStates[i] == 'cm' &&  order.shps && order.shps.length > 0 && !shipId ) //shipments are present on mobile, show ship option only for shipments
		 	continue;

		if ( nextStates[i] == 'fl'  && order.shps  && order.shps.length > 1 && !shipId)//shipments are present on mobile, show fulfillment option only f
			continue;

		if ($transferOrder  && nextStates[i] == 'cn' && $uid != order.cbid && !isAdministrator($role))
			continue;
		markup += '<option value="' + nextStates[i] + '">' + getOrderStatusSelection( nextStates[i] ) + '</option>';
		numOptions++;
	}
	var orderSize = ( order && order.mt ? order.mt.length : 0 );
	/*if ( orderType == 'prc' && allowMarkOrderAsFulfilled( order, orderSize ) && $.inArray( 'fl', nextStates ) != -1 ) { // purchase order, allow order to be marked as fulfilled and next states does not have 'fulfilled' status
		markup += '<option value="fl">' + getOrderStatusDisplay('fl') + '</option>'; // add the fulfulled status
		numOptions++;
	}*/
	markup += '</select>';
	if ( numOptions == 0 )
		markup = '';
	return markup;
}

// Get the order footer markup
function getOrderFooterMarkup( order, orderType, orderIsEdited, isPayment ) {
	var markup = '';
	var urlBase = '#orderinfo?oid=' + order.tid + '&oty=' + orderType;
	// Get the order state transitions
	var nextStates = $orderStateTransitions[ order.ost ];
	if ( nextStates && order.ost != 'cm' ) {
		markup += '<div id="ordereditpanel" data-role="controlgroup" data-type="horizontal">';
		if ( orderIsEdited ) {
			markup += getOrderSendButtonMarkup( order, orderType );
		}
		// Edit/add buttons
		var editUrl = urlBase + '&edit=true';
		var addUrl = '#addmaterialstoorder?oid=' + order.tid + '&oty=' + orderType;
		var editOrder = true;
		if ( (orderType == 'prc') && ((order.ost == 'cf') || ( order.ost == 'bo'))  && (getRelatedEntityPermissions(order.vid, 'vnds' ,order.kid) != $MANAGE_ENTITY) ){// customer cannot edit a confirmed order.
			editOrder =  false;
		}
		//if (order.shps && order.shps.length > 0)
		//	editOrder = false;
		if ($transferOrder && $uid != order.cbid && !isAdministrator($role)  )
			editOrder = false;
		if (getUserPermissions($uid) != $VIEW_ONLY) {
			if (order.mt && order.mt.length > 0 && editOrder == true)
				markup += '<a href="' + editUrl + '" data-role="button" data-mini="false">' + $buttontext.edit + '</a>';
			if ( editOrder == true)
				markup += '<a href="' + addUrl + '" data-role="button"  data-mini="false">' + $buttontext.add + '</a>';
		}
		markup += '</div>';
	} else { // add payment button
		if ( orderIsEdited ) {
			markup += getOrderSendButtonMarkup( order, orderType );
		} else if ( !isPayment && order.ost != 'cn' && !$disableOrderPrice) { // if this is not the payment edit form mode, and order is not cancelled
			var url = urlBase + '&pay=true';
			markup = '<a href="' + url + '" data-role="button" data-mini="false">'+$labeltext.addpayment+'</a>';
		}
	}
	return markup;
}

// Send/clear buttons for order footer
function getOrderSendButtonMarkup( order, orderType ) {
	// Send button
	var sendOnClick = 'updateOrderRemote(\'' + order.tid + '\',\'' + orderType + '\')';
	var markup = '<a href="#" data-role="button" data-mini="false" onclick="' + sendOnClick + '">'+$buttontext.send+'</a>';
	// Clear all button
	var thisUrl = '#orderinfo?oid=' + order.tid + '&oty=' + orderType;
	var clearOnClick = 'clearOrderEdits(\'' + order.tid + '\',\'' + orderType + '\'); $.mobile.changePage( \'' + thisUrl + '\' );';
	markup += '<a href="#" id="orderclearbutton" data-role="button" data-mini="false" onclick="' + clearOnClick + '">'+$buttontext.clear+'</a>';
	return markup;
}

// Get the markup for a generic selector
function getSelectorMarkup( id, options, selectedOption ) {
	var markup = '';
	if ( options && options != null ) {
		markup += '<select id="' + id + '" class ="inputfieldchange" >';
		for ( var i = 0; i < options.length; i++ ) {
			var sel = '';
			if ( selectedOption && selectedOption != null && selectedOption == options[i] )
				sel = ' selected';
			markup += '<option value="' + options[i] + '"' + sel + '>' + options[ i ] + '</option>'; 
		}
		markup += '</select>';
	}
	return markup;
}


// Render order  status change  for orders
function renderOrderStatusChange( pageId, params, options ) {
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	// Update header
	var orderId;
	if ( params.oid )
		orderId = params.oid;
	var oType;
	if ( params.oty )
		oType = params.oty;
	var oStatus;
	if ( params.status )
		oStatus = params.status;
	var fflag = "false";
	if ( params.fflag )
		fflag = params.fflag;
	var shipId = '';
	var trsp = '';
	var trid = '';
	var pksz = '';
	var eadValue = '';
	if ( params.sid) {
		shipId = params.sid;
		var shps = getOrderShipment(orderId, oType, shipId)
		if (shps.trsp)
			trsp = shps.trsp;
		if (shps.trid)
			trid = shps.trid;
		if (shps.pksz)
			pksz = shps.pksz;
		if (shps.ead)
			eadValue = shps.ead
	}

	// Get the query string for this and back URL
	var queryString = 'oid=' + orderId + ( params.o ?'&o=' + params.o : '' ) + '&oty=' + oType;
	// Get the back button URL
	var backUrl = '#orderinfo?' + queryString;
	var headerTxt = $labeltext.order + ' ' + orderId;
	header.find( 'h3' ).html( headerTxt );
	// Update the back button
	header.find( '#orderstatuschangetback .ui-btn-text').text($buttontext.back);
	header.find( '#orderstatuschangeback' ).attr( 'href', backUrl );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update content
	var markup = '<b>' + getOrderStatusSelection(oStatus) + ' ' + $labeltext.order_lower + ' - ' + orderId + '</b> ';
	var commentsOnly = true;
	var commentsWithReasons = false;
	if ( oStatus == 'cn' ) {
		commentsOnly = false;
		commentsWithReasons = true;
	}
	if ( oStatus == 'cm') {
		if ( shipId && shipId != '')
			markup += '<p>' + $labeltext.shipmentid + ':'+ shipId + '</p>';
		markup += '<p><label for="transporter">' + $labeltext.transporter + ':</label>';
		markup += '<input type="text" name="transporter" id="transporter"  value="' + trsp + '">';
		markup += '<span id="trsp_error" style="display:none;"> <font style="color:red;font-size:small">' + $messagetext.pleaseentertransporter + '</font></span>';
		markup += '<label for="trackingid">' + $labeltext.trackingid + ':</label>';
		markup += '<input type="text" name="trackingid" id="trackingid"  value="' + trid + '"></p>';
		markup += '<p><label for="packagesize">' + $labeltext.packagesize + ':</label>';
		if ($config && $config.pksz) {
			markup +=  getSelectorMarkup('packagesize', $config.pksz.split(','), pksz);
		} else  {
			markup += '<input type="text" name="packagesize" id="packagesize"  value="' + pksz + '">';
		}
		markup += '</p>';
		markup += '<label id="expectedfulfillmenttime" for="ead">' + $labeltext.estimateddateofarrival + ':</label>'

		markup += '<a href="#" data-role="button" data-icon="delete" data-iconpos="notext"  style="float: right;margin-top: -.01em;" onclick ="deleteDate(\'#ead\')"></a>';
		markup += '<div style="overflow: hidden; padding-right: .75em;">';
		var today = getStrFromDate(new Date());
		markup += '<input name="ead" class ="mobidatepicker" id="ead" min="' + today + '" style= "margin-top: -.02em;" type="text" onfocus="$(\'#actualTransactionDate_error\').hide()" value="' + ( eadValue ? getDatePickerFormat(decodeURIComponent(eadValue),$userDateFormat) : '' ) + '" />';
		markup += '</div>';

	}
	markup += '<p>' + getOrderReasonsMarkup( $config, oStatus, orderId, null, commentsOnly, commentsWithReasons, true) + '</p>';
	// Buttons
	//var onStatusChangeClick = 'confirmOrderStatusChange(\'' + orderId + '\',\'' + oStatus + '\',\'' + oType + '\',\'' + backUrl + '\' )';
	var msg = $messagetext.areyousureyouwantomarkorder;
	msg = msg.replace('<status>', getOrderStatusConfirmation(oStatus));
	if ( shipId )
		msg = msg.replace('<order>', $labeltext.shipment_lower);
	else
		msg = msg.replace('<order>', $labeltext.order_lower);
	var onStatusChangeClick = 'showPopupDialog($labeltext.confirm, \''+ msg +'\', function() {changeOrderStatus( \'' + orderId + '\',\'' + oType +
		'\',\''+ oStatus + '\',\''+ shipId + '\',\''+ fflag + '\');},null,null,\'ospopup\',false)';
	var buttonLabel = getOrderStatusSelection(oStatus);
	if (oStatus == 'cn') {
		buttonLabel = $buttontext.cancelorder;
		if ( shipId )
			buttonLabel = buttonLabel.replace('<order>', $labeltext.shipment_lower);
		else
			buttonLabel = buttonLabel.replace('<order>', $labeltext.order_lower);
	}
	markup += ' </br> <a id="ossavebutton" href="#" data-role="button" data-inline="true" data-icon="plus" data-theme="b" data-disabled="true" onClick ="' + onStatusChangeClick + ' ">' + buttonLabel + '</a>';
	markup += '<a id="oscancelbutton" href="' + backUrl+ '" data-role="button" data-icon="arrow-r" data-inline="true" data-theme="c"  data-direction="reverse" >' + $buttontext.cancel + '</a>';

	// Update content part
	content.empty().append( $( markup )  );
	// Enhance the page
	page.page();
	page.trigger('create'); // this will ensure that all internal components are inited./refreshed
	// Update options data url
	options.dataUrl = '#orderstatuschange?' + queryString ;
	$.mobile.changePage( page, options );
	page.find('div[id$="reasonsdiv_' + orderId + '"]').attr('style', 'display:block');
}

// Function to get confirmation of order status change
function confirmOrderStatusChange( oid, status, orderType, sid ) {
	// Check whether conditions for status change are appropriate
	var fflag ="false";
	var fromOrder = null;
	var order = getOrder( oid, orderType );
	if (!order) {
		showDialog(null, $messagetext.unabletofindorderid +' - ' + oid,  getCurrentUrl() );
		return;
	}
	if ( status == 'cm' || status == 'fl' ) {
		if ( order ) {
			// Check for an empty order (this is a possibility only if empty-orders are allowed, but checking just the same)
			if ( !order.mt || order.mt.length == 0 ) {
				showDialog( null, $messagetext.emptyordercannotbe + getOrderStatusDisplay( status ) + '.'+ $messagetext.pleaseadditemstoorder, getCurrentUrl());
				return;
			}
			// Check if vendor is present
			if ( status == 'cm' && !order.vid ) {
				showDialog( null, $messagetext.thisordercannotbe + getOrderStatusDisplay( status ) + ', '+$messagetext.novendorspecified,  getCurrentUrl() );
				return;
			}
			/*if (orderType == 'sle'){
				/* for ( var i = 0; i < order.mt.length; i++ ) {
					var item = order.mt[i];
					var material = $materialData.materialmap[ item.mid ];
					if (parseFloat(item.q) > parseFloat(material.q)){
						//stock is less than what is being sold
						showDialog( null, $messagetext.thisordercannotbe + getOrderStatusDisplay( status ) + ', '+$labeltext.materials +' '+ material.n +' '+ $messagetext.stockonhand +' '+ material.q +' '+ $messagetext.lessthanquantityof +' '+ item.q,  getCurrentUrl() );
						return;
					}
				}
			} */

			if ( status == 'cm'){
				if ( isAutoPostInventory($config)) {
					if (orderType == 'prc') {
						var lkid = order.vid;
						if ( lkid && getRelatedEntityPermissions(lkid, 'vnds', $entity.kid) == $MANAGE_ENTITY) {
							var entity = getRelatedEntity(lkid, 'vnds');
							 if (!entity.mt) {
								 showPopupDialog($labeltext.confirm, $messagetext.vendorstocknotavailableforcheck, function () {
										 getInventoryForRelatedEntity(lkid, 'vnds', null);
									 },
									 null, null, "popupShipStock", false);
								 $( $.mobile.activePage[0] ).find('#orderstatusselector').val(order.ost);
								 return;
							 }

						}

					}

					//check if ordered quantity matches allocation if there is a batch material
					if (isOrderItemsHaveBatch(oid, orderType, sid)) {
						//check if item is allocated before shipping /fulfilled
						if (!isOrderItemsAllocated(oid,orderType, sid)) {
							showDialog(null, $messagetext.allocatebeforeshipping, getCurrentUrl());
							return;
						}
						//check if all items are fully allocated before shipping
						if (!isOrderedQtyFullyAllocated(oid, orderType, sid)) {
							showDialog(null, $messagetext.allocatefullybeforeshipping, getCurrentUrl());
							return;
						}

					}
					else { //no batch item check if stock is available
						var msg = isOrderedQtyAvailable(oid, orderType, sid)
						if (msg) {
							showDialog(null, msg, getCurrentUrl());
							return;
						}
					}
				}

			}
		}

	}
	// Seek confirmation for change
	if ( status == 'fl')  {
		var orderFulfilPage = '#orderfulfilment?oid=' + oid + '&oty=' + orderType + '&status='+ status;
		if (sid ) {
			orderFulfilPage += '&sid=' + sid;
			fflag = "true"; //don't send orderid
		}
		else {
			if (order.shps) {
				sid = order.shps[0].sid;
				if (!sid) {
					showDialog(null, $messagetext.unabletofindshipmenid,  getCurrentUrl() );
					return;
				}
				else
					orderFulfilPage += '&sid=' + sid;
			}

		}
		if ( fflag )
			orderFulfilPage += '&fflag='+fflag;

		$.mobile.changePage(  orderFulfilPage );
	}
	else if ( status == 'cf' || status == 'cn' || status == 'cm' || status == 'pn') {
		var orderStatusChangePage = '#orderstatuschange?oid=' + oid + '&oty=' + orderType + '&status='+ status;
		  if (sid ) {
			  fflag = "true"; //don't send orderid
			  orderStatusChangePage += '&sid=' + sid;
			  if ( fflag )
				  orderStatusChangePage += '&fflag='+fflag;
		  }
		if ( status == 'cm'){
			if ( fflag )
				orderStatusChangePage += '&fflag='+fflag;
		}
		$.mobile.changePage(  orderStatusChangePage  );
	}

}

function isOrderItemsAllocated ( oid, orderType, sid ){
	var order = getOrder( oid, orderType);
	if (!order)
		return false;
	var items;
	if (sid) {
		var shps = getOrderShipment(oid, orderType, sid);
		if (shps)
			items = shps.mt;
	}
	else
		items = order.mt;
	for ( var i=0; i <items.length; i++  ) {
		if (items[i].alq || items[i].q == 0)
			continue;
		else
			return false;

	}

	return true;
}

function isOrderItemsHaveBatch( oid, orderType,sid ) {
	var order = getOrder(oid, orderType);
	if (!order)
		return false;
	var items;
	if (sid) {
		var shps = getOrderShipment(oid, orderType, sid);
		if (shps)
			items = shps.mt;
	}
	else
	  items = order.mt;
		for (var i = 0; i < items.length; i++) {
			if ( orderType == 'prc') {
				var lkid = order.vid;
				if (lkid)
					material = getRelatedEntityMaterial(lkid, items[i].mid);
			}
			else {
				material = $materialData.materialmap[items[i].mid];
			}
			if (!material)
				return;
			//check if any batch material is present
			if (material.ben) {
				return true;
			}
		}

	return false;
}


function isOrderedQtyFullyAllocated ( oid, orderType, sid ){
	var order = getOrder( oid, orderType);
	if (!order)
		return false;
	var items ;
	if (sid) {
		var shps = getOrderShipment(oid, orderType, sid);
		if (shps)
			items = shps.mt;
	}
	else
		items = order.mt;
	for (var i = 0; i < items.length; i++) {
		if ((items[i].alq != items[i].q) && items[i].q != 0) {
			return false;
		}

	}
	return true;
}


function isOrderedQtyAvailable( oid, orderType, sid) {

	var order = getOrder( oid, orderType);
	if (!order)
		return null;
	var material = null;
	var items;
	if (sid) {
		var shps = getOrderShipment(oid, orderType, sid);
		if (shps)
			items = shps.mt;
	}
	else
		items = order.mt;
	for (var i = 0; i < items.length; i++) {
		var prevAlloc = 0;
		if ( orderType == 'prc') {
			var lkid = order.vid;
			if (lkid)
				material = getRelatedEntityMaterial(lkid, items[i].mid);
		}
		else {
			material = $materialData.materialmap[items[i].mid];
		}
		if (!material)
			continue;

		if (items[i].alq);
			prevAlloc = items[i].alq;
		if (isNaN(prevAlloc))
			prevAlloc = 0;
		if (material.avq != null) {
			var stockQty = parseFloat(material.avq) + parseFloat(prevAlloc);
			if (stockQty < items[i].q){
				var msg = $messagetext.availablestockisless;
				msg = msg.replace('<st>', stockQty);
				msg = msg.replace('<material>',material.n);
				msg = msg.replace('<oq>',items[i].q);
				return msg;
			}

		}

	}
	return null;
}
// Change a given order's status
function changeOrderStatus( oid,  orderType, orderStatus, shipId, fflag ) {
	// Update the status with a loading indicator
	var loadingMsg = $messagetext.changingstatusoforder + ' ' + oid + '...';
	var sid = null;
	var dar = null;
	var trid = null;
	var trsp = null;
	var ead = null;
	var pksz = null;
	var rsnco = null;
	var orderMsg = null;
	if ($('#' + orderStatus + 'comments_' + oid))
		orderMsg = $('#' + orderStatus + 'comments_' + oid).val();

	if (orderStatus == 'cn') {
		if ($('#' + orderStatus + 'reason_' + oid).length) {
			var othersSelected;
			if ($('#' + orderStatus + 'reason_' + oid).val() == $labeltext.others) {
				rsnco = $('#' + orderStatus + 'reasonmsg_' + oid).val();
				othersSelected = true;
			}
			else
				rsnco = $('#' + orderStatus + 'reason_' + oid).val();

		}
		else if ($('#' + orderStatus + 'reasonmsg_' + oid).length){
			rsnco = $('#' + orderStatus + 'reasonmsg_' + oid).val();
		}
		var mnd = isOrderReasonsMandatory($config, 'cn');
		var isReasonsVisible = $('#' + orderStatus + 'reason_' + oid).is(":visible") || $('#' + orderStatus + 'reasonmsg_' + oid).is(":visible");
		if (!rsnco && rsnco == '' && (othersSelected || mnd === true) && isReasonsVisible) {
			$('#cnreasonerr_' + oid).attr('style', 'display:block');
			return;
		}
	}
	else if (orderStatus == 'cm' || orderStatus == 'sp') {
		if ($('#transporter'))
			trsp = $('#transporter').val();
		if ( trsp)
			trsp.trim();
		if ($config.ords && $config.ords.trspm) {
			if (!trsp || trsp == '') {
				$($.mobile.activePage[0]).find('#trsp_error').attr('style', 'display:block');
				return;
			}
		}

		if ($('#trackingid'))
			trid = $('#trackingid').val();
		if ( trid )
			trid.trim();
		if ($('#packagesize'))
			pksz = $('#packagesize').val();
		if (pksz)
			pksz.trim();
		if ($('#ead'))
			ead = $('#ead').val();

	}
	else if (orderStatus == 'fl') {
		if ($('#dar')) {
			dar = $('#dar').val();
		}
	}
	if (shipId != null && shipId != '') {
		sid = shipId;
		if (orderStatus == 'cm')
			orderStatus = 'sp';//shipment status is shipped
	}
	var orderStatusChangeInput = getOrderStatusChangeInput( oid, orderType, orderStatus, orderMsg, rsnco, sid, trsp, trid, pksz, ead, dar, fflag,$uid );
	var url = $host + '/api/o';
	var postData = {};
	postData.a = 'uos';
	///postData.p = $pwd; // password is sent via BasicAuth header
	postData.j = JSON.stringify( orderStatusChangeInput );
	// Update
	var request = {
		type: 'POST',
		url: url,
		data: postData,
		dataType: 'json', // expected return data type
		cache: false,
		success: function(o) {
			if ( o && o.st == '0' ) {
				updateLocalOrderStatus( o, orderType, false );
				if ( isAutoPostInventory($config)) {
					refreshInventory(null);
					var lkid = o.vid;
					if (orderType == 'sle')
						lkid = o.kid;
					if (getRelatedEntityPermissions(lkid, 'vnds' , $entity.kid) != $NO_PERMISSIONS ) {
						getInventoryForRelatedEntity(lkid, orderType, null);
					}
				}
				// Go back to order view
				$.mobile.changePage( '#orderinfo?oid=' + oid + '&oty=' + orderType + '&st=' + orderStatus );
			} else {
				var orderUrl = '#orderinfo?oid=' + oid + ( orderType ? '&oty=' + orderType : '' ) + (  orderStatus ? '&st=' + orderStatus : '' );
				showDialog( null, o.ms, orderUrl );
			}
		},
		sync: {
			op: 'uos',
			uid: $uid,
			kid: $entity.kid,
			oid: oid,
			params: { otype: orderType },
			callback: function() {
				$.mobile.changePage( '#orderinfo?oid=' + oid + '&oty=' + orderType + '&st=' + status ); // go back to order view
			},
			syncOnNetworkError: true
		}
	};
	var logData = {
		functionName: 'changeOrderStatus',
		message: ''
	};
	// Request server
	requestServer( request, loadingMsg, logData, true, true );
}

function getOrderStatusChangeInput( orderId, orderType, orderStatus, orderMsg, rsnco, sid, trsp, trid, pksz, ead, dar, fflag,  uid ){
	var orderStatusChangeInput = {};
	orderStatusChangeInput.v = '03';
	orderStatusChangeInput.uid = uid;
	if ( orderStatus )
		orderStatusChangeInput.ost = orderStatus;
	if ( orderType )
		orderStatusChangeInput.oty = orderType;
	if ( orderId && fflag == "false")
		orderStatusChangeInput.tid = orderId;
	if (  sid && sid != null )
		orderStatusChangeInput.sid = sid;
	if ( orderMsg && orderMsg != null && orderMsg != undefined)
		orderStatusChangeInput.ms = orderMsg;
	if ( dar && dar != null && dar != undefined )
		orderStatusChangeInput.dar = getDateStrInServerFormat(dar, $userDateFormat);
	if ( rsnco && rsnco != null && rsnco != undefined )
		orderStatusChangeInput.rsnco = rsnco;
	if ( trsp && trsp != null && trsp != undefined )
		orderStatusChangeInput.trsp = trsp;
	if ( trid && trid != null && trid != undefined )
		orderStatusChangeInput.trid = trid;
	if ( pksz && pksz != null && pksz != undefined )
		orderStatusChangeInput.pksz = pksz;
	if ( ead && ead != null && ead != undefined )
		orderStatusChangeInput.ead = getDateStrInServerFormat(ead, $userDateFormat);


	var order = getOrder(orderId, orderType);
	orderStatusChangeInput.vid = order.vid;
	orderStatusChangeInput.kid = order.kid;

	if ( orderStatus == 'fl') { //fulfilled
		orderStatusChangeInput.mt = [];
		var items;
		if ( sid ) {
			items = getOrderShipment(orderId, orderType, sid)
			for (i = 0; i < items.mt.length; i++) {
				var item = items.mt[i];
				var mData = {};
				mData.mid = item.mid;
				mData.q = item.flq;
				if (item.rsnpf)
					mData.rsnpf = item.rsnpf;
				if (item.fmst)
					mData.fmst = item.fmst;
				if (item.bt) {
					mData.bt = [];
					for (var j = 0; j < item.bt.length; j++) {
						var bt = {}
						bt.bid = item.bt[j].bid;
						bt.q = item.bt[j].flq;
						if (item.bt[j].rsnpf)
							bt.rsnpf = item.bt[j].rsnpf;
						if (item.bt[j].fmst)
							bt.fmst = item.bt[j].fmst;
						mData.bt.push(bt);
					}
				}
				orderStatusChangeInput.mt.push( mData );
			}
		}

	}
	return orderStatusChangeInput;
}

// Render an shipmentinfo
function renderShipmentInfo( pageId, params, options ) {
	// Render orders
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Get the order parameters
	var oid = params.oid;
	var otype = params.oty;
	var sid = params.sid;
	var status = params.st;
	var backPageOffset = 0;
	if ( params.o )
		backPageOffset = params.o;
	var isEdit = ( params.edit && params.edit == 'true' && (getUserPermissions($uid) != $VIEW_ONLY)); // Edit mode, i.e. show the edit form to allow changing quantities, etc.
	// Get the order
	var order = getOrder( oid, otype );
	if ( !order || order == null ) {
		showDialog( null, $messagetext.unabletofindorderid +' ' + oid,  getCurrentUrl() );
		return;
	}
	var shps = getOrderShipment( oid, otype, sid );
	if ( !shps || shps == null ) {
		showDialog( null, $messagetext.unabletofindorderid +' ' + sid,  getCurrentUrl() );
		return;
	}
	// Update the header titles
	var headerTxt = $labeltext.shipment +' ' + sid;
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update back button URL
	status = shps.st;
	if ( status == 'sp')
	  status = 'cm';
	var backUrl = '#orderinfo?oid=' + oid + '&oty=' + otype  + '&st=' + status +  '&o=' + backPageOffset;
	header.find( '#shipmentinfoback' ).attr( 'href', backUrl );
	// Update the content
	var markup = '<ul data-role="listview" data-inset="true"><li>';
	markup += '<table data-role="table" data-mode="reflow" class="infotable"><tbody>';
	markup += '<tr><th style="vertical-align:middle">'+$labeltext.status+':</th><td>';
	// Show order status change button, if needed
	var nextStates = $orderStateTransitions[ status ];
	if (  nextStates && (getUserPermissions($uid) != $VIEW_ONLY))
		markup += '<table><tr><td style="border-bottom:0;padding:0 5px 0 0;white-space:nowrap;vertical-align:middle">'
	markup += getOrderStatusDisplay(shps.st);
    if (shps.rsnco && shps.st == 'cn') {
		markup += '<br/><font style="font-weight:normal;font-size:small;color:lightslategray">(' + shps.rsnco + ')</font>';
	}
	if (shps.ssht)
		markup += '</br><font style="color:black;font-size:8pt;font-weight:normal">' + shps.ssht + '</font>';

	if (  nextStates && (getUserPermissions($uid) != $VIEW_ONLY))
		markup += '</td><td style="border-bottom:0;padding:0 0 0 0">' + getOrderStatusSelector(order, otype, nextStates, shps.sid) + '</td></tr></table>';

	markup += '</td></tr>';


	// Show the price
	if (!$disableOrderPrice && shps.tp) {
		markup += '<tr><th>' + $labeltext.price + ':</th><td>' + shps.tp;
		markup += '</td></tr>';
	}
	// Other shipment attribtues
	markup += '<tr><th>'+$labeltext.updated+':</th><td>'  + shps.t + ' ' + $labeltext.by + ' ' +  shps.n +  '</td></tr>';
	//if ( shps.ut )
	//	markup += '<tr><th>'+$labeltext.updated+':</th><td>' + shps.ut + '</td></tr>';
	var customer;
	var relatedEntity;
	if ( otype == 'prc' )
		customer = $entity;
	else
		customer = getRelatedEntity( order.kid, 'csts' );
	if ( customer ) {
		markup += '<tr><th>' + $labeltext.customer + ':</th><td>' + customer.n + ', ' + customer.cty + '</td></tr>';
		relatedEntity = customer.n;
	}
	var vendor;
	var defaultVendor;
	if ( otype == 'prc' )
	{
		vendor = getRelatedEntity( order.vid, 'vnds' );
		if (!vendor)
		{
			defaultVendor = getDefaultVendor(order.vid);
		}
	}
	else
		vendor = $entity;
	if ( vendor ) {
		markup += '<tr><th>' + $labeltext.vendor + ':</th><td>' + vendor.n + ', ' + vendor.cty + '</td></tr>';
		relatedEntity = vendor.n;
	}
	else if (defaultVendor) {
		markup += '<tr><th>' + $labeltext.vendor + ':</th><td>' + defaultVendor.n + ', ' + defaultVendor.cty + ' </td></tr>';
		relatedEntity = defaultVendor.n;
	}
	//Transporter
	if (shps.trsp)
		markup += '<tr><th>'+$labeltext.transporter+':</th><td>' +shps.trsp + ' </td></tr>';
	if (shps.trid)
		markup += '<tr><th>'+$labeltext.trackingid+':</th><td>' +shps.trid + ' </td></tr>';
	if (shps.pksz)
		markup += '<tr><th>'+$labeltext.packagesize+':</th><td>' +shps.pksz + ' </td></tr>';
	var ead = '';
	if (shps.ead)
		ead = shps.ead;
	if (ead)
		markup += '<tr><th>'+$labeltext.estimateddateofarrival +':</th><td>' + getDateOnly( ead ) + '</td></tr>';
	if (shps.dar)
		markup += '<tr><th>'+$messagetext.dateofactualreceipt +':</th><td>' + getDateOnly( shps.dar ) + '</td></tr>';
	if (shps.rsnps)
		markup += '<tr><th>'+$labeltext.reasons +':</th><td>' + shps.rsnps + '</td></tr>';
	markup += '</tbody></table>';
	markup += '</li></ul>';
    if ( shps.cmnts ) {
		var url = "#ordercomments?oid=" + oid + '&oty=' + otype + '&st=' + status + '&sid=' + sid + '&o=' + backPageOffset;
		markup += '<a href="' + url + '" data-role="button" data-mini="true" data-inline="true"  data-icon="false">' + $labeltext.viewcomments + '</a>';
	}
	// Show the items ordered
	var size = 0;
	if ( shps.mt )
		size = shps.mt.length;
	// Add the display of number of items and add/edit buttons
	if ( size == 0 ) {
		markup += '<ul data-role="listview" data-inset="true">';
		markup += '<li>'+$messagetext.noitemsinorder+'</li></ul>';
	} else {
		// Render materials
		markup += '<ul data-role="listview" data-inset="true">';
		markup += '<li data-role="list-divider">' + size +' '+$labeltext.items +' </li>';
		for ( var i = 0; i < size; i++ ) {
			var item = shps.mt[i];
			var material = $materialData.materialmap[ item.mid ];
			var itemName = $labeltext.unknown;//'Unknown';
			if ( material )
				itemName = material.n;
			else {
				itemName = $messagetext.nosuchmaterial;
				markup += '<li>' + itemName;
				continue;
			}
			var qty = item.q;
			if (item.flq)
				qty = item.flq;
			markup += '<li>' + itemName;
			//markup += '<span style="font-size:8pt;float:right;border-radius:25px; border:2px solid #D3D3D3;padding:5px;">' + getFormattedNumber(qty) + '</span>';

			var currency = ( order.cu ? order.cu : ( $entity.cu ? $entity.cu : $config.cu ) );
			if ( material && material.rp && !$disableOrderPrice )
				markup += ', <font style="color:green">' + ( currency ? currency + ' ' : '' ) + getFormattedPrice( material.rp ) + '</font>';
			if ( material && material.tx && material.tx != 0 && !$disableOrderPrice )
				markup += ' [<font style="color:blue">' + getFormattedPrice(material.tx) + '% tax</font>]';
			//markup += '<span class="ui-li-count" style="font-size:12pt">';

			//markup += getFormattedNumber(qty);
			//markup += '</span>';

			var orderItem = getOrderItem( oid, otype, item.mid);
			markup += getOrderQuantitesTable(item.mid, oid, otype, status, sid);
			markup += '<div id="ordereddiv' + item.mid +'" style="display:block"><font style="color:lightslategray;font-size:small;font-weight:normal;">';
			if (item.mst)
				markup += '(' + item.mst + ') ';
			if ((item.fmst || item.rsnpf) && !item.bt)
				markup += '(';
			if ( item.fmst && !item.bt)
				markup +=  item.fmst ;
			if ((item.fmst && item.rsnpf) && !item.bt)
				markup += ',';
			if ( item.rsnpf && !item.bt)
			   markup += item.rsnpf;
			if ((item.fmst || item.rsnpf) && !item.bt)
				   markup += ')';
			markup += '</font></div>';
			if ( item.bt ) {
				var batchAllocateMarkup = '';
				batchAllocateMarkup += getOrderBatchAllocatedMarkup(item.bt, item.mid, oid, status, true );
				if (batchAllocateMarkup)
					markup += batchAllocateMarkup;
			}

			markup += '</li>';
		}
		markup += '</ul>';

	}
	content.empty().append( markup );
	// Enhance page
	page.page();
	page.trigger( 'create' );
// Add an event handler for status change selector, if it exists on page
	var orderStatusSelector = page.find( '#orderstatusselector' );
	if ( orderStatusSelector ) {
		orderStatusSelector.off('change').on( 'change', function() {
			var value = orderStatusSelector.val();
			if ( value == '' )
				return;
			confirmOrderStatusChange( oid, value, otype, sid );

		});
	}
	// Update URL in options
	options.dataUrl = '#shipmentinfo?oid=' + oid + '&oty=' + otype + '&sid=' + sid + '&st=' + status + '&o=' + backPageOffset;
	$.mobile.changePage( page, options );
	header.find( '#orderinfoback .ui-btn-text').text($buttontext.back);
}

function getShipmentsMarkup ( orderId, orderType, backPageOffset ){
	var order = getOrder( orderId, orderType );
	var markup = '';
	if ( !order )
		return markup;
	var shps = order.shps;
	if ( !shps )
		return markup;
	markup +=  '<ul data-role="listview" data-inset="true" data-icon="false">' ;
	markup += '<li data-role="list-divider">' + $labeltext.shipments +' </li>';
	for ( var i = 0; i < shps.length; i++ ) {
		var url = '#shipmentinfo?oid=' + orderId  + '&oty='+ orderType + '&sid='+shps[i].sid+ '&st='+shps[i].st+ '&o='+backPageOffset;
		markup += '<li data-icon="false"><a href="' + url +'" >';
		markup += '<font style="color:black;font-size:9pt;font-weight:normal">';
		var size = 0;
		if (shps[i].mt)
			size = shps[i].mt.length;
		markup += shps[i].sid  + ', ' + size + ' ' + $labeltext.items  + ', '  + getOrderStatusDisplay( shps[i].st );
		markup += ', ' + shps[i].t;
		markup += ', ' + shps[i].n;
		markup += '</font></li></a>';
	}
	markup += '</tbody></table>';
 	markup += '</ul>';
	return markup;
}




// Render an order item info
function renderOrderItemInfo( pageId, params, options ) {
	// Render orders
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Get the order parameters
	var oid = params.oid;
	var otype = params.oty;
	var ost = params.ost;
	var mid = params.mid;
	var sid = params.sid;
	var backPageOffset = 0;
	if ( params.o )
		backPageOffset = params.o;
	// Get the order
	var order = getOrder( oid , otype);
	var item = getOrderItem( oid, otype, mid );
	var shipments, shippedItem;

	if ( !item || item == null ) {
		showDialog( null, $messagetext.unabletofindorderid +' ' + oid,  getCurrentUrl() );
		return;
	}
	if (!mid)
		return;
	 var material =  $materialData.materialmap[ mid ];

	// Update the header titles
	var headerTxt = $labeltext.order + ' ' + oid;
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update back button URL
	var backUrl = '#orderinfo?oid=' + oid + '&oty=' + otype  + '&st=' + status +  '&o=' + backPageOffset;
	if ( sid )
		 backUrl = '#shipmentinfo?oid=' + oid + '&oty=' + otype  + '&st=' + status +  '&o=' + backPageOffset + '&sid=' + sid;
	header.find( '#orderiteminfoback' ).attr( 'href', backUrl );
	// Update the content
	//var markup = '<ul data-role="listview" data-inset="true"><li>';
	var markup = '<h4>' + material.n + '</h4>';

	shipments = getShipmentsForMaterial(oid, otype, mid);
	var shippedShipment = isAnyShipmentShipped(shipments);
	var fulfilledShipment = isAnyShipmentFulfilled(shipments);
	var orderColor = "black";
	var shipColor = "black";
	var fulfillColor = "black";
	var originalOrderColor = "black";
	if ( item.q ) {
		if (  item.oq && item.q != item.oq )
			orderColor = $discrepancyColor;
		else if ( item.roq >= 0 && item.q != item.roq)
			orderColor = $discrepancyColor;
	}
	if ( item.oq ){
		if ( item.roq >= 0 && item.oq != item.roq)
			originalOrderColor = $discrepancyColor;
	}

	markup += '<table data-role="table"  data-mode="reflow" class="infotable" width="100%" ><tbody>';

	if ( item.roq != -1 ) {
		markup +=  '<tr><th class="order-info-header">' + $labeltext.recommendedquantity  +':</th><td class="order-info">' + item.roq + '</td></tr>';
	}
	if (item.oq)
		markup += '<tr><th class="order-info-header">' +$labeltext.originallyordered +':</th><td><font style="font-size:10pt;font-weight:normal;color:' + originalOrderColor + '" >' +  item.oq + '</font></td></tr>';
	if (item.rsnirq) {
		markup += '<tr><th class="order-info-header">' +$labeltext.orderingdiscrepancyreason +':</th>' ;
		markup += '<td class="order-info">'+ item.rsnirq + '</td></tr>';
	}
	if (item.q != item.oq && (!shippedShipment && !fulfilledShipment)) {
		markup += '<tr><th class="order-info-header">' +$labeltext.ordered + ': </th><td><font style="font-size:10pt;font-weight:normal;color:' + orderColor + '" >' + item.q + '</font></td></tr>';
		if (item.rsneoq) {
			markup += '<tr><th class="order-info-header">' +$labeltext.ordermodificationreason +':</th>' ;
			markup += '<td class="order-info">' + item.rsneoq + '</td></tr>';
		}

	}
	if ( item.q > 0) {
		if ((otype == 'sle' || ( otype == 'prc' && getRelatedEntityPermissions(order.vid, 'vnds', order.kid) == $MANAGE_ENTITY) ) && (order.ost == 'pn' || order.ost == 'cf') && isAutoPostInventory($config)) {
			if (item.alq && !sid) {
				markup += '<tr><th class="order-info-header">' + $labeltext.allocated + ':</th> <td class="order-info">' + item.alq + '</td></tr>';
				if (item.mst && !item.bt)
					markup += '<tr><th class="order-info-header">' + $labeltext.status + ':</th><td class="order-info">' + item.mst + '</td></tr>';
				if (item.bt) {
					//markup += '</tbody></table><hr style="visibility:hidden;" />';
					//markup += '<table data-role="table"  data-mode="reflow" class="infotable""><tbody>';
					markup += '<tr><hr style="visibility:hidden;" /></tr>';
					markup += '<tr><th class="order-info-header">' + $labeltext.batches + ':</th></td>';
					markup += '<td>' + getOrderBatchAllocatedMarkup(item.bt, mid, otype, ost, true) + '</td>';
					markup += '</tr>';
					//markup += '</tbody></table>';
					//markup += '<table data-role="table"  data-mode="reflow" class="infotable""><tbody>';
				}

			}

		}

		var shipmentStatus = ost;
		if (shippedShipment)
			shipmentStatus = 'cm';
		if (fulfilledShipment)
			shipmentStatus = 'fl';
		if (shipments) {
			var sq = getShippedQty(oid, otype, mid);
			if (sq != null && ( shippedShipment || fulfilledShipment)) {
				if (sq != item.oq && (order.ost == 'cm' || order.ost == 'fl'))
					shipColor = $discrepancyColor;
				if ((item.flq != null && item.flq != sq) && shipmentStatus == 'fl')
					fulfillColor = $discrepancyColor;
				markup += '<tr><th class="order-info-header">' + $labeltext.shipped + ':</th><td><font style="font-size:10pt;font-weight:normal;color:' + shipColor + '" >' + sq + '</font></td></tr>';
				//if (shipments.length == 1 && item.mst) {
				//	markup += '<tr><th class="order-info-header">' + $labeltext.status + ':</th><td><font style="font-size:10pt;font-weight:normal;color:black" >' + item.mst + '</font></td></tr>';
				//}
				if (item.rsneoq)
					markup += '<tr><th class="order-info-header">' + $labeltext.shipmentdiscrepancyreason + ':</th> <td class="order-info">' + item.rsneoq + '</td></tr>';
				if (item.flq != null && fulfilledShipment)
					markup += '<tr><th class="order-info-header">' + $labeltext.fulfilled + ':</th> <td> <font style="font-size:10pt;font-weight:normal;color:' + fulfillColor + '" >' + item.flq + '</font></td></tr>';
				//if (shipments.length == 1 && item.fmst) {
				//	markup += '<tr><th class="order-info-header">' + $labeltext.status + ':</th><td><font style="font-size:10pt;font-weight:normal;color:black" >' + item.fmst + '</font></td></tr>';
				//}
				var shbt = getShippedQtyByBatch(oid, otype, mid);
				var batch = false;
				if (shbt && shbt.length > 0)
					batch = true;
				if (shipments.length == 1) {
					var sh = getOrderShipmentItem(oid, otype, shipments[0].sid, mid, true);
					if (sh && sh.rsnpf && !batch)
						markup += '<tr><th class="order-info-header">' + $labeltext.fulfillmentdiscrepancyreason + ':</th> <td class="order-info">' + sh.rsnpf + '</td></tr>';
				}
				if (shbt != null && shbt.length != 0) {
					//markup += '</tbody></table><hr style="visibility:hidden;" />';
					//markup += '<table data-role="table"  data-mode="reflow" class="infotable""><tbody>';
					markup += '<tr><hr style="visibility:hidden;" /></tr>';
					markup += '<tr><th class="order-info-header">' + $labeltext.batches + ':</th></td>';
					markup += '<td>' + getOrderBatchAllocatedMarkup(shbt, mid, otype, shipmentStatus, true) + '</td>';
					markup += '</tr>';
					//markup += '</tbody></table>';
					//markup += '<table data-role="table"  data-mode="reflow" class="infotable""><tbody>';
				}
			}


		}


	}
	markup += '</tbody>';
	markup += '</table>';

	content.empty().append( markup );
	// Enhance page
	page.page();
	page.trigger( 'create' );
	// Update URL in options
	options.dataUrl = '#orderiteminfo?oid=' + oid + '&oty=' + otype + '&mid=' + sid + '&ost=' + ost + '&o=' + backPageOffset;
	if ( sid )
		options.dataUrl += '&sid = ' + sid;
	$.mobile.changePage( page, options );
	header.find( '#orderinfoback .ui-btn-text').text($buttontext.back);
}

//Render a list of order/shipment comments
function renderOrderComments (pageId, params, options ) {
	if ( !$entity || $entity == null )
		return;

	var pageOffset = 0;
	if ( params && params.o )
		pageOffset = parseInt( params.o ); // NOTE: has to be int. given it is used for range calculations
	var backPageOffset = 0; // offset for the back page (esp. if it is entities or related entities)
	if ( params && params.bo )
		backPageOffset = params.bo; // NOTE: can be in string form, given it is used as back URL param only
	// Get page
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Update the header with the title and number of entities
	var sid;
	if ( params.sid)
	  sid = params.sid;
	var oid;
	if ( params.oid)
		oid = params.oid;
	var otype;
	if ( params.oty)
		otype = params.oty;
	var status;
	if ( params.st)
		status = params.st;
	var headerText = '';
	if ( sid )
		headerText = $labeltext.shipment + '  ' + sid ;
	else if ( oid )
		headerText = $labeltext.order + ' ' + oid;
	header.find( 'h3' ).empty().append( headerText );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerText );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update header nav. buttons

	var backUrl = '#orderinfo?oid=' + oid + '&oty=' + otype  + '&st=' + status +  '&o=' + backPageOffset;
	header.find( '#ordercommentsback' ).attr( 'href', backUrl );


	var markup = '<b>' + $labeltext.comments + '<b>';
	var listId = 'comments_ul';
	content.empty();
	markup = '<ul id="' + listId + '" data-role="listview" data-theme="d" data-divider-theme="d" ></ul>';
	var commentsListview = $(markup);
	content.append( commentsListview);
	// Enhance the page
	page.page();
	page.trigger( 'create' );
	// Update content
	if (updateOrderCommentsListview('ordercomments', commentsListview, oid, otype, sid,  pageOffset )) // returns a collapsible-set
		content.append( commentsListview );
	else
		content.empty().append( '' );

	// Now call changePage() and tell it to switch to the page we just modified.k
	if ( options ) {
		options.dataUrl = '#ordercomments?o=' + pageOffset+ '&oid='+oid + '&otype =' + otype;
		if ( sid )
		 options.dataUrl += '&sid=' + sid;
		$.mobile.changePage( page, options );

	} else
		$.mobile.changePage( page );
	header.find( '#ordercommentsback .ui-btn-text').text($buttontext.back);
}

// Update order comments list view
function updateOrderCommentsListview( id,listview, orderId, orderType, shipId,  pageOffset ) {
	// Append the navigation bar
	var cmnts;
	if (!orderId && !orderType)
		return false;
	if ( shipId ) {
		var shps = getOrderShipment(orderId, orderType, shipId);
		if ( shps.cmnts)
			cmnts = shps.cmnts;
	}
	else {
		var order = getOrder( orderId, orderType)
		if ( order.cmnts)
			cmnts = order.cmnts;
	}
	var size = ( cmnts ? cmnts.cnt : 0 );
	// Set the listview navigation bar
	if ( size == 0 ) {
		return false;
	}
	else
	{
		// Set the listview navigation bar
		var range = setListviewNavBar( listview, pageOffset, size, null, function() {
			updateOrderCommentsListview( id, listview, orderId, orderType, shipId, (pageOffset-1) );
		}, function() {
			updateOrderCommentsListview( id, listview,orderId, orderType, shipId, (pageOffset+1) );
		} );
		var start = range.start - 1;
		var end = range.end;
		var offset = pageOffset;
		// Get the list of orders for rendering on this page
		var markup = '';
		if ( !cmnts.msgs )
			return false;
		if ( cmnts.msgs.length == 0)
			return false;
		for ( var i = start; i < end; i++ ) {
			var cmnt = cmnts.msgs[i];
			var commentsText='';
			var id= 'comments_'+i;
			if (cmnt.msg)
			commentsText += '<font style="font-size:11pt;font-weight:normal;color:black">'+ cmnt.msg +'</font>'+' ';
			if (cmnt.n) {
				commentsText += '<br/><font style="font-size:9pt;font-weight:normal; font-color:color:darkslategray">' + $labeltext.by + ' ' + cmnt.n ;
			}
			if (cmnt.t)
				commentsText += ', '  + cmnt.t + '</font>' + ' ';
			// Update markup
			markup +=' <li id="' + id + '">'+commentsText+'</li>';
		}
		// Append item to listview
		listview.append( $( markup ) );
	}
	// Refresh the view
	listview.listview('refresh');
	return true;
}

// Get an ID value without spaces
function getId( tag ) {
	return tag.replace( / /g, '-' ); // replace spaces with -
}

// Get available credit
function getAvailableCredit( relatedEntity ) {
	if ( relatedEntity.crl ) { // credit limit and payable are given
		var creditLimit = parseFloat( relatedEntity.crl );
		if ( relatedEntity.pyb )
			return round( ( creditLimit - parseFloat( relatedEntity.pyb ) ), 2 );
		else
			return round( creditLimit, 2 );
	}
	return undefined;
}

// Get the order status display
function getOrderStatusDisplay( statusCode ) {
	 if ( 'cn' == statusCode )
	     return $labeltext.cancelled;
	 else if ( 'cm' == statusCode || 'sp' == statusCode )
	     return $labeltext.shipped;
	 else if ( 'cf' == statusCode )
	     return $labeltext.confirmed;
	 else if ( 'fl' == statusCode )
	     return $labeltext.fulfilled;
	 else if ( 'pn' == statusCode )
	     return $labeltext.pending;
	 else if ( 'op' == statusCode )
		 return $labeltext.pending;
	 else if ( 'bo' == statusCode )
		 return $labeltext.backordered;
	 else
	     return $labeltext.unknown;
}

function getOrderStatusSelection( statusCode ) {
	if ( 'cn' == statusCode )
		return $buttontext.cancel;
	else if ( 'cm' == statusCode || 'sp' == statusCode )
		return $labeltext.ship;
	else if ( 'cf' == statusCode )
		return $labeltext.confirm;
	else if ( 'fl' == statusCode )
		return $labeltext.fulfil;
	else if ( 'pn' == statusCode )
		return $labeltext.reopen;
	else if ( 'op' == statusCode)
		return $labeltext.pending;
	else if ( 'bo' == statusCode )
		return $labeltext.backordered;
	else
		return $labeltext.unknown;
}

function getOrderStatusConfirmation( statusCode ) {
	if ( 'cn' == statusCode )
		return $labeltext.cancel_lower;
	else if ( 'cm' == statusCode || 'sp' == statusCode )
		return $labeltext.ship_lower;
	else if ( 'cf' == statusCode )
		return $labeltext.confirm_lower;
	else if ( 'fl' == statusCode )
		return $labeltext.fulfil_lower;
	else if ( 'pn' == statusCode )
		return $labeltext.reopen_lower;
	else
		return $labeltext.unknown;
}


// Determine the first order type, which has orders
function getOrderTypeWithOrders() {
	var otype = 'prc';
	var	orders = getOrders( otype );
	if ( orders == null || orders.length == 0 ) {
		otype = 'sle';
		orders = getOrders( otype );
		if ( orders == null || orders.length == 0 )
			return null;
	}
	return otype;
}

// Get orders organized by status
function getOrdersByStatus( orders, oTag, oSearch ) {
	if ( !orders || orders == null || orders.length == 0 )
		return null;
	var ordersByStatus = {};
	for ( var i = 0; i < orders.length; i++ ) {
		var status = orders[i].ost;
		if ( !ordersByStatus[ status ] )
			ordersByStatus[ status ] = [];
		if ( oTag  ) {
			if (!orders[i].tg)
				continue;
			if ( (orders[i].tg.toLowerCase().indexOf (oTag.toLowerCase()) ==  -1 ))
				continue;
		}
		else if ( oSearch ) {
			if ( (orders[i].tid  !=  oSearch ))
				continue;
		}
 		ordersByStatus[ status ].push( orders[i] );
	}
	return ordersByStatus;
}


// Get the reason codes (as array) from config, if any
function getReasons( currentOperation, config ) {
	var key = '';
	if ( currentOperation == 'ei' )
		key = 'irsns';
	else if ( currentOperation == 'er' )
		key = 'rrsns';
	else if ( currentOperation == 'es' )
		key = 'prsns';
	else if ( currentOperation == 'ew' )
		key = 'wrsns';
	else if ( currentOperation == 'ts' )
		key = 'trsns';
	if ( key == '' )
		return null;
	var reasonsCSV = config[ key ];
	if ( reasonsCSV && reasonsCSV != '' )
	return reasonsCSV.split(',').filter(function(v){return v!==''});
	return null;
}


// Get the reason codes (as array) from config, if any
function getReasonsByTag( currentOperation, config,tagName ) {
	var key = 'rsnsbytg';
	if (!config[key])
	  return null;
	var reasonsCSVByTag = null;
	var okey = '';
	okey = getTransactionType(currentOperation);
	if (okey != '')
		reasonsCSVByTag = config[key][okey];
	var reasonsCSV ='';
	if (tagName && reasonsCSVByTag != null && reasonsCSVByTag != '' )
	  reasonsCSV = reasonsCSVByTag[tagName];
	if (reasonsCSV && reasonsCSV != '')
		return reasonsCSV.split(',').filter(function(v){return v!==''});
	return null;
}

// Get the reason codes (as array) from config, if any
function getIgnoreRecommendedOrderReasons( currentOperation, config ) {
	var key = 'igorrsns';
	var reasonsCSV = config[ key ];
	if (!reasonsCSV)
	  return null;
	else
	 reasonsCSV = config[ key ]['rsns'];
	if ( reasonsCSV && reasonsCSV != '' )
		return reasonsCSV.split( ',' ).filter(function(v){return v!==''});
	return null;
}



// Get the reason codes (as array) from config, if any
function getOrderReasonsErrMsg(  reasonType ) {
	var errMsg;
	if ( reasonType == 'ir') {
		errMsg = $messagetext.specifyreasonrecommendedquantity
	}
	else if ( reasonType == 'fl') {
		errMsg = $messagetext.specifyreasonforpartialfulfilment;
	}
	else if ( reasonType == 'eo') {
		errMsg = $messagetext.specifyreasonforeditingquantity;
	}
	else if ( reasonType == 'cn') {
		errMsg = $messagetext.specifyreasonforcancellingorder;
	}
	else
	 	errMsg = $messagetext.selectreason;

	return errMsg;
}




// Get the reason codes (as array) from config, if any
function getOrderReasons( config, reasonType ) {
	var reasonsCSV;
	if ( reasonType == 'ir') {
		if ( config.igorrsns )
			reasonsCSV = config.igorrsns.rsns ;
	}
	else if ( reasonType == 'fl') {
		if ( config.rsnspf )
		 	reasonsCSV = config.rsnspf.rsns ;
	}
	else if ( reasonType == 'eo') {
		if ( config.rsnseoq )
			reasonsCSV = config.rsnseoq.rsns ;
	}
	else if ( reasonType == 'cm') {
		if ( config.rsnsps )
			reasonsCSV = config.rsnsps.rsns ;
	}
	else if ( reasonType == 'cn') {
		if ( config.rsnsco )
			reasonsCSV = config.rsnsco.rsns ;
	}
	if (reasonsCSV)
		reasonsCSV = reasonsCSV + ',' + $labeltext.others;

	if ( reasonsCSV && reasonsCSV != '' )
		return reasonsCSV.split( ',' ).filter(function(v){return v!==''});
	return null;
}



function getOrderReasonsMarkup( config, reasonType, id, reason, commentsOnly, commentsWithReasons, showLabel ){
	var reasons = getOrderReasons( $config, reasonType );
	var errMsg = getOrderReasonsErrMsg( reasonType );
	// Reason field
	var markup = '';
	var divId = reasonType +"reasonsdiv";
	var lblId = reasonType +"reasonlabel";
	var selectId = reasonType +"reason";
	var reasonMsgId = reasonType + "reasonmsg";
	var reasonErrId = reasonType + "reasonerr";
	var commentsId = reasonType + "comments";
	if (id){
		divId += '_'+id;
		lblId += '_'+ id;
		selectId += '_'+ id;
		reasonMsgId += '_'+ id;
		commentsId += '_' + id;
		reasonErrId += '_' + id;
	}
	markup += '<div style="display:none" id= "'+ divId +'" >';
	var lblreasons = $labeltext.reasons;
	var mnd = isOrderReasonsMandatory($config, reasonType);
	var starText = '';
	if ( mnd === true )
		starText = '*'
	if (showLabel && !commentsOnly) {
		markup += '<label style="font-weight:normal;" id="' + lblId + '" for= "' + selectId + '">' + lblreasons + starText + ':</label>';
	}
	if (reasons != null && !commentsOnly ) {
		markup += '<select name="reason" id="'+ selectId +'" data-theme="c" data-mini="true" onfocus="$(\'#' + reasonErrId + '\').hide()"  onchange="checkReasonsSelected(\'' + id + '\',this, \''+ reasonType + '\') ">';

		markup += '<option value="">'+$messagetext.selectreason+'</option>';
		var selected = '';
		var othersSelected = '';
		var reasonInArray = $.inArray(reason,reasons);
		for (var i = 0; i < reasons.length; i++) {
			selected = '';
			if (reason) {
				if (reason == reasons[i]) {
					selected = 'selected';
				}
				else if (( reasons[i] == $labeltext.others) && (reasonInArray == -1 )){
					selected = 'selected';
					othersSelected  = 'selected';
				}

			}
			markup += '<option value="' + reasons[i].replace(/"/g, '&quot;')  + '" ' + selected + '>' + reasons[i] + '</option>';
		}
		markup += '</select>'

	}
	if (!commentsOnly) {
		var otherReasons = '';
		var displayStyle = 'none';
		if ( (othersSelected && reason) || !reasons ) {
			if ( reason )
				otherReasons = reason;
			displayStyle ='block';
		}
		markup += '<textarea  placeholder = "' +  $messagetext.enterreason  + '" style="display:' + displayStyle + '" oninput="maxFieldLengthCheck(\'' + reasonMsgId + '\',' + '255' + ')" id="' + reasonMsgId + '">' + otherReasons + '</textarea>';
	}
	markup += '</div>';
	markup += '<div>';
	if (!commentsOnly) {
		var errMsg = getOrderReasonsErrMsg(reasonType);
		markup += '<div id="' + reasonErrId + '" style="display:none"><font style="color:red;font-size:small;font-weight:normal">' + errMsg + '</font></div>';
	}
	if ( commentsWithReasons || commentsOnly) {
		markup += '<label id="' + lblId + '" for= "' + commentsId + '">' + $labeltext.comments + ':</label>';
		markup += '<textarea  style="display:block;" id="' + commentsId + '"></textarea>';
	}
	markup += '</div>';

	return markup;

}



function checkReasonsSelected ( id, reasonField, reasonType ) {
	//if ( reasonType == 'ir' || reasonType == 'eo') {
		if (reasonField.value && reasonField.value == $labeltext.others) {
			$( $.mobile.activePage[0] ).find('#' + reasonType + 'reasonmsg_' + id).attr('style', 'display:block');
		}
		else
			$( $.mobile.activePage[0] ).find('#' + reasonType + 'reasonmsg_' + id).attr('style', 'display:none');
	//}
}

function getInventoryTagsToHide(currentOperation,config) {
	var key = 'tgiov';
	var tagsToHideCSV = '';
	if (!config[key])
	return null;
	var okey = '';
	okey = getTransactionType(currentOperation);
	if (okey != '')
		tagsToHideCSV = config[key][okey];
	if (tagsToHideCSV && tagsToHideCSV != '')
		return tagsToHideCSV.split(',').filter(function(v){return v!==''});
	return null;

}

// Get the material status (as array) from config, if any
function getMaterialStatus( currentOperation, config, materialtsm ) {
	var key = 'mtst';
	var materialStatusCSV = '';
	if (!config[key])
	return null;
	var okey = '';
	okey = getTransactionType(currentOperation);
	if (okey != '') {
		if (config[key][okey]) {
			if (materialtsm)
				materialStatusCSV = config[key][okey].tsm;
			if (!materialStatusCSV || materialStatusCSV == '')
				materialStatusCSV = config[key][okey].all;
		}
	}

	if ( materialStatusCSV && materialStatusCSV != '' )
		return materialStatusCSV.split( ',' ).filter(function(v){return v!==''});
	return null;
}

function getMaterialStatusMarkup ( currentOperation, fieldId, mst, isTmp, showLabel) {
	var markup = '';
	var materialStatus = getMaterialStatus(currentOperation, $config, isTmp);
	// materialStatus field
	if (materialStatus != null ) {
		markup += '<div>';
		if (showLabel)
			markup += '<label id="materialStatuslabel" for="materialstatus' + fieldId  + '" >' + $labeltext.status + '*:</label>';
		markup += '<select name="materialStatus" id="materialstatus_' + fieldId   + '" class ="inputfieldchange" ' +
			'onfocus="$(\'#materialstatus_err_' + fieldId + '\').hide()" data-theme="c" data-mini="true">';
		markup += '<option value="">' + $messagetext.selectstatus + '</option>';
		for (var i = 0; i < materialStatus.length; i++) {
			var selected = '';
			if (mst == materialStatus[i])
				selected = 'selected';
			markup += '<option value="' + materialStatus[i].replace(/"/g, '&quot;') + '" ' + selected + '>' + materialStatus[i] + '</option>';
		}
		markup += '</select>';
		markup += '<div id="materialstatus_err_' + fieldId + '" style="display:none"><font style="color:red;font-size:small;font-weight:normal">' + $messagetext.pleaseselectamaterialstatus + '</font></div>';
		markup += '</div>';
	}
	return markup;
}
// Get the capture actual transcation date options from config, if any
function getCaptureActualTransactionDate( currentOperation, config) {
	var key = 'catd';
	var atdRequired = 0;
	if (!config[key])
		return null;
	var okey = '';
	okey = getTransactionType(currentOperation);
	if (okey != '') {
		if (config[key][okey]) {
			try {
				atdRequired = parseInt(config[key][okey].ty);
			} catch ( e ) {
				logR( 'e', 'getCaptureActualTransactionDate', e.message, e.stack );
			}
		}
	}
	return atdRequired;
}


function getATDText(currentOperation){
	var atdText = '';
	if (currentOperation == 'ei')
		atdText = $messagetext.dateofactualissue;
	else if (currentOperation == 'er')
		atdText = $messagetext.dateofactualreceipt;
	else if (currentOperation == 'ew')
		atdText = $messagetext.dateofactualdiscard;
	else if (currentOperation == 'ts')
		atdText = $messagetext.dateofactualtransfer;
	else if (currentOperation == 'es')
		atdText = $messagetext.dateofactualphysicalstock;
	return atdText;
}

function getLastEnteredActualTransactionDate(currentOperation,entity){
	var actualTransactionDate;
	var localData = entity.localdata;
	if ( !localData )
		return null;
	var materialModifications = localData[ currentOperation ];
	if (materialModifications == null || materialModifications.length == 0 )
	  return null;
	else {
		if (materialModifications.latd)
			actualTransactionDate = materialModifications.latd;
	}
	return actualTransactionDate;
}



// Get the operation name
function getOperationName( op ) {
	if ( op == 'ao' ) // add to order
		return $labeltext.orders; //'Orders'
	var name = $operations[ op ];
	if ( name )
		return name;
	return '';
}

// Get the transaction type for a given operatin
function getTransactionType( operation ) {
	if ( 'ei' == operation )
		return 'i';
	else if ( 'er' == operation )
		return 'r';
	else if ( 'es' == operation )
		return 'p';
	else if ( 'ew' == operation )
		return 'w';
	else if ( 'ts' == operation )
		return 't';
	else if ( 'no' == operation )
		return 'o';
	else
		return '';
}


// Get operation type from transaction type
function getOperationType( transType ) {
	if ( transType == 'i' )
		return 'ei';
	else if ( transType == 'r' )
		return 'er';
	else if ( transType == 'p' )
		return 'es';
	else if ( transType == 'w' )
		return 'ew';
	else if ( transType == 't' )
		return 'ts';
	else
		return '';
}

//Check if the current operation is read-only
function isOperationReadOnly( currentOperation ) {
	if ( currentOperation == 'vs' || currentOperation == 'vo' )
		return true;
	return false;
}

// Is this operation order related
function isOperationOrder( op ) {
	return ( op == 'vo' || op == 'no' || op == 'ao' );
}


// Is this operation inventory related
function isOperationInventoryTransaction( op ) {
	return ( op == 'ei' || op == 'er' || op == 'es' || op == 'ew' || op == 'ts' );
}

// Get a list of valid operations, based on configuration
function getValidOperations( config ) {
	if ( !config || !config.trn || config.trn == '' )
		return $operations;
	var disableOps = config.trn.split(',');
	var validOps = $.extend( true, {}, $operations ); // do a deep copy
	for ( var i = 0; i < disableOps.length; i++ ) {
		var op = disableOps[i];
		// Translate to our terminology, where required
		if ( op == 'es' ) // disable issues
			op = 'ei';
		if ( op == 'er' ) // disable receipts
			op = 'er';
		else if ( op == 'sc' )
			op = 'es';
		else if ( op == 'wa' )
			op = 'ew';
		else if ( op == 'ts' )
			op = 'ts';
		else if ( op == 'ns' )
			op = 'no';
		else if (op == 'vh')
			op = 'vt';
		else if ( op == 'vt' ) //view transfer
			op = 'tr';
        /*
		else if (op == 'pi') // print inventory
		     $printInventoryDisabled = true;
		else if (op == 'ep') //edit profile
		     $editProfileDisabled = true;
		*/
		// Remove this from valid operations
		delete validOps[ op ];
	}
	return validOps;
}


// Get disabled operations (array), if any
function getDisabledOperations() {
	if ( !$config || !$config.trn || $config.trn == '' )
		return undefined;
	var disabledOps = $config.trn.split(',');
	return disabledOps;
}

// Check if this operation is a valid operation
function isOperationDisabled( op ) {
    var disabledOps = getDisabledOperations();
    if ( !disabledOps )
        return false;
    for ( var i = 0; i < disabledOps.length; i++ ) {
        if ( op == disabledOps[i] )
            return true;
    }
    return false;
}

// Function check if operations of a particular type exist
function hasInventoryOperations( operations ) {
	return ( operations.vs !== undefined || operations.ei !== undefined || operations.er !== undefined || operations.es !== undefined || operations.ew !== undefined || operations.ts !== undefined );
}
function hasOrderOperations( operations ) {
	return ( operations.vo !== undefined || operations.no !== undefined );
}

// Get the currency of a given material
function getCurrency( material ) {
	if ( material.cu )
		return material.cu;
	if ( $entity.cu )
		return $entity.cu;
	if ( $config.cu )
		return $config.cu;
	return null;
}

function getFrequencyText ( type, freq) {
	var freqType = null;
	if ( !type || !freq)
	  return null;
	if ( freq == "d") {
		if ( type == "stock")
			freqType = $labeltext.days;
		else if ( type == "cr" )
			freqType = $labeltext.day;
		else
			freqType = $labeltext.days_lower;
	}
	else if ( freq == "w") {
		if ( type == "stock")
			freqType = $labeltext.weeks;
		else if (type == "cr")
			freqType = $labeltext.week;
		else
			freqType = $labeltext.weeks_lower;
	}
	else if ( freq == "m") {
		if ( type == "stock")
			freqType = $labeltext.months;
		else if ( type == 'cr')
			freqType = $labeltext.month;
		else
			freqType = $labeltext.months_lower;
	}
	return freqType;

}

/*function getFrequency( type ) {
	var freq;
	var freqType = null;
	if (!$config.frinv)
		return freqType;
	if (type == 'stock') {
		if ($config.frinv.frst)
			freq = $config.frinv.frst;
	}
	else if (type == 'minmax') {
		if ($config.frinv.frmx)
			freq = $config.frinv.frmx;
	}
	else if (type = 'cr') {
		if ($config.frinv.frcr)
			freq = $config.frinv.frcr;
	}
	if ( freq == "d") {
		if ( type == "stock")
			freqType = $labeltext.days;
		else if ( type == "cr" )
			freqType = $labeltext.day;
		else
		 freqType = $labeltext.days_lower;
	}
	else if ( freq == "w") {
		if ( type == "stock")
			freqType = $labeltext.weeks;
		else if (type == "cr")
			freqType = $labeltext.week;
		else
			freqType = $labeltext.weeks_lower;
	}
	else if ( freq == "m") {
		if ( type == "stock")
			freqType = $labeltext.months;
		else if ( type == 'cr')
			freqType = $labeltext.month;
		else
			freqType = $labeltext.months_lower;
	}
	else if ( freq == "q") {
		if ( type == "stock")
			freqType = $labeltext.quarter;
		else
			freqType = $labeltext.quarter_lower;
	}
	else if ( freq == "h") {
		if ( type == "stock")
			freqType = $labeltext.halfyear;
		else
			freqType = $labeltext.halfyear_lower;
	}
	return freqType;
}

*/


// Check if add entity permissions exist for the given entity type
function canAddEntity( type ) {
	return ( $config.crents && $config.crents.indexOf( type ) != -1 ); // has the permission to add this type of entity (earier: also checking $role != 'ROLE_ko' && )
}

// Check if edit entity permissions exist for the given entity type
function canEditEntity( type ) {
	return ( !isOperationDisabled( 'ep' ) ); // if operation is not disabled for this user, then allow editing
}

// Check if editing of users is allowed
function canEditUser( user ) {
	return ( !isOperationDisabled( 'ep' ) && ( $uid == user.uid || compareRoles( $role, user.rle ) > 0 ) ); // op. not disabled, or is same user or has a higher role  (earlier: ( isAdmin || $uid == user.uid || compareRoles( $role, user.rle ) > 0 ))
}


function isManagedEntity  ( entityId ){
	//check if user has manage permissions for the entity
	var userData = getUserData($uid);
	var idx = $.inArray(entityId.toString(), userData.ki); // index of kid in user ki
	if (idx == -1)
		 return false;
	else
		return true;
}


function getOrderRelatedEntity(currentOperation , orderType){
	var relatedEntity = null;
	 relatedEntity = getSelectedLinkedEntity(currentOperation, $entity);//new order
	if (!relatedEntity){
		var params = getURLParams(getCurrentUrl());
		var oid;
		var oty;
		if ( params.oid)
			oid = params.oid;
		if (params.oty)
			oty = params.oty;
		if ( oid && oty )
			var order = getOrder(oid, oty);
		if ( !order)
			return;
		relatedEntity = {};
		if ( orderType == 'sle') {
			relatedEntity.kid = order.kid;
			relatedEntity.type = 'csts';
		}
		else {
			if ( order.vid) {
				relatedEntity.kid = order.vid;
				relatedEntity.type = 'vnds';
			}
		}
	}
	return relatedEntity;
}


function getRelatedEntityPermissions (linkedEntityId, relationType,entityId) {
	if (!linkedEntityId || !entityId)
		return $NO_PERMISSIONS;
	///Check if user has read only permissions
	if (getUserPermissions($uid) == $VIEW_ONLY)
		return $NO_PERMISSIONS;
	if (isManagedEntity (linkedEntityId) == true)
		return $MANAGE_ENTITY; //Managed entity

	//Check if entity is customer's customer and not a managed entity by the user
	var parentEntity = getParentEntity();
	var parentEntityId;
	if (parentEntity)
		parentEntityId = parentEntity.kid;
	if (parentEntityId != null && entityId != parentEntityId && !isManagedEntity(entityId) == true)
		return $NO_PERMISSIONS;

    //Get Related entity permissions
	var entity = getEntity($uid, entityId);
	var isRelatedEntity = getRelatedEntityForKioskId(linkedEntityId, relationType, entityId);
	if (!isRelatedEntity)
		return $NO_PERMISSIONS;
	if (entity && entity.prms) {
		if (relationType == 'csts' && entity.prms.csts) {
			if (entity.prms.csts) {
				if (entity.prms.csts.vinv)
					return $VIEW_INVENTORY;
				else if (entity.prms.csts.mgmd)
					return $MANAGE_ENTITY;
			}



		}
		else if (relationType == 'vnds' && entity.prms.vnds) {
			if (entity.prms.vnds) {
				if (entity.prms.vnds.vinv)
					return $VIEW_INVENTORY;
				else if (entity.prms.vnds.mgmd)
					return $MANAGE_ENTITY;
			}
		}
	}
	return $NO_PERMISSIONS;
}
function getRelatedEntityMaterial (lkid, mid) {
	var material = null
	var entity = getEntity($uid, lkid);
	if (entity.mt) {
		for (var i = 0; i < entity.mt.length; i++) {
			if (entity.mt[i].mid == mid) {
				material = entity.mt[i];
				break;
			}
		}
	}
	return material;
}

function getUserPermissions (user){
	if (!user)
		return ;
	 var userData = getUserData(user);
	if (userData.prms) {
		if (userData.prms == 'v')
			return $VIEW_ONLY;
		else if (userData.prms == 'a')
			return $ASSETS_ONLY;
	}
	return ;
}

function getParentEntity() {
	var p;
	if ($parentEntity && $parentEntity.length > 0){
		idx = $parentEntity.length;
		p = $parentEntity[idx-1];
	}
	return p;
}

function spliceParentEntity() {
	if ($parentEntity && $parentEntity.length > 0){
		var idx = $parentEntity.length;
		$parentEntity.splice(idx-1);
	}
}

function clearParentEntity() {
	$parentEntity = [];
}

function getUserValidOperations (user,validOperations){
	if (!user)
		return validOperations;
	var validOps = $.extend( true, {}, validOperations ); // do a deep copy
	if (getUserPermissions(user) == $VIEW_ONLY){
		var op ='ei'; //delete issues
		delete validOps[ op ];
		op = 'er';
		delete validOps[ op ];
		op = 'es';
		delete validOps[ op ];
		op = 'ew';
		delete validOps[ op ];
		op = 'ts';
		delete validOps[ op ];
		op = 'no';
		delete validOps[ op ];
	}
	return validOps;
}

function getRelatedEntityValidOperations (linkedEntityId,parentEntity,validOperations){
	var relationType;
	var entityId;
	if (parentEntity){
		entityId = parentEntity.kid;
		relationType = parentEntity.type;
	}
if (!linkedEntityId  || !entityId || !relationType )
		return validOperations;
	var validOps = $.extend( true, {}, validOperations ); // do a deep copy
	if (getRelatedEntityPermissions(linkedEntityId, relationType,entityId) == $VIEW_INVENTORY){
		var op ='ei'; //delete issues
		delete validOps[ op ];
		op = 'er';
		delete validOps[ op ];
		op = 'es';
		delete validOps[ op ];
		op = 'ew';
		delete validOps[ op ];
		op = 'ts';
		delete validOps[ op ];
		op = 'vt';
		delete validOps[ op ];
		op = 'xi';
		delete validOps[ op];
		op = 'no';
		delete validOps[ op ];
		op = 'vo';
		delete validOps[ op];
	}
	return validOps;
}


function sendNow()
{
	var kid = $entity.kid;
    var currentOperation = $currentOperation;
	var currentIndex = 0;
	var linkedEntityIds=[];
	if ($entity.localdata[ currentOperation ] && $entity.localdata[ currentOperation ].linkedentity)
	{
	    	var linkedEntity = {};
	    	linkedEntity = $entity.localdata[ currentOperation ].linkedentity;
	    	linkedEntityIds.push[linkedEntity];
	 }
	 var currentLkidIndex =0;
	 var entityIds= [];
	 entityIds.push(kid);
	 sendWithLocation( function(){sendTransaction(currentOperation,entityIds,currentIndex,linkedEntityIds,currentLkidIndex);});
}



// Acquire location, if necessary, and then send transaction
function sendWithLocation( callback ) {
	// Check if geo-codes are to be acquired; required, if the geo-coding strategy is strict, or if geo expired
	if ( isGeoStrategyStrict() || isGeoExpired( getGeo() ) ) {
	      var loadingmsg = $labeltext.acquiringlocation;
		showLoader(loadingmsg+ '...' );
		acquireGeoLocation( function(pos) {
			gotPosition( pos ); // get the position data
			hideLoader(); // hide loading indicator
			callback(); // now callback/send
		}, function(err) {
			hideLoader(); // hide loading indicator
			errorGettingPosition( err ); // process error
			callback(); // callback/send anyway
		} );
	} else {
		callback(); // callback/send
	}
}




function sendTransaction(currentOperation,entityIds,currentIndex,linkedEntityIds,currentLIndex) {
	var tag = $mtag;
	//get entity information
	var entityId = entityIds[currentIndex];
	var linkedIds = linkedEntityIds;
	var kids = entityIds;

	 $entity = getEntity( $uid, entityId );
	 if (!$entity)
		 return;
	 initMaterialData( $entity.mt, $entity.cu );
	 //set selected linked entity for batch send operation
	 if (linkedEntityIds)
	 {
		 if (linkedEntityIds[currentLIndex] )
		  	$entity.localdata[ currentOperation ].linkedentity = linkedEntityIds[currentLIndex];
	 }
	// Get the locally modified data
	var localModifications = getLocalModifications( null, $currentOperation, $entity );
	var noItems = ( !localModifications || localModifications == null || localModifications.length == 0 );
	if ( ( noItems && ( ( $currentOperation == 'no' && !allowEmptyOrders() ) || $currentOperation != 'no' ) ) ) {
		showDialog( null, $messagetext.nothingtosend,  getCurrentUrl() );
		return;
	}

	// Check for stock issue errors
	var errMids = getStockIssueErrors( $currentOperation, $entity );
	if ( errMids && !$.isEmptyObject(errMids)) {
		var msgExcessStock = $messagetext.materialsexcessofstock;
		if (isAutoPostInventory($config))
			msgExcessStock = $messagetext.materialsexcessofavailablestock;
		var msg = '<b>' + $entity.n + '</b><br/><br/>' + msgExcessStock + '<br/>';
		for (var i in errMids ) {
			if ( !errMids.hasOwnProperty( i ) )
				continue;
			var m = errMids[ i ];
			var modifiedMaterial = localModifications[m.mid ];
			if (modifiedMaterial.bt)
			{
				msg += '<br/> - ' + getMaterialName(m.mid );
				msg += '<br/>';
				for ( var j = 0; j < m.bt.length; j++ ) {
					msg += m.bt[j].bid + ( m.bt[j].q ? ' (' + m.bt[j].q + ')' : '' )+'<br/>';
				}
			}
			else
				msg += '<br/> - ' + getMaterialName(m.mid ) + ( modifiedMaterial.q ? ' (' + modifiedMaterial.q + ')' : '' );

		}
		showDialog( $messagetext.warning, msg,  getCurrentUrl() );
		return;
	}

	var reviewPage = $( '#review' );
	// Check if linked entity ID exists
	var linkedEntityId = null;
	var linkedEntityName = '';
	var linkedEntity = getSelectedLinkedEntity( $currentOperation, $entity );
	var orderType = 'prc';
	if ( linkedEntity != null)
	{
		linkedEntityId = linkedEntity.kid;
		if (!linkedEntity.type || currentOperation == 'no') {//type is changed or not available for orders
			linkedEntity.type = getSelectedLinkedEntityType(linkedEntity.kid, $entity.kid);
			var lkid = getRelatedEntity(linkedEntityId, linkedEntity.type);
			linkedEntityName = lkid.n;
			//if (linkedEntity.type == 'csts')
			 //  orderType = 'sle';
			if (linkedEntity.otype)
			  orderType = linkedEntity.otype;
		}
	}
	// Get message in case of order
	var isOrder = isOperationOrder( $currentOperation);
	var message = null;
	var orderStatus = null; // initial order status
	var paymentOption = null, packageSize = null;
	var payment = null;
	var orderTags = null;
	var referenceId = null;
	var requiredByDate = null;
	if ( isOrder ) {
		message = reviewPage.find( '#transmessage' ).val();
		/*var orderStatusElement = reviewPage.find( '#initialorderstatus' );
		if ( orderStatusElement && orderStatusElement.attr( 'checked' ) )
			orderStatus = orderStatusElement.val();*/
		var paymentOptionElement = reviewPage.find( '#paymentoption' );
		if ( paymentOptionElement )
			paymentOption = paymentOptionElement.val();
		var packageSizeElement = reviewPage.find( '#packagesize' );
		if ( packageSizeElement )
			packageSize = packageSizeElement.val();
		payment = reviewPage.find( '#payment' ).val();
		var otg = reviewPage.find( '#ordertags' ).val();
		if ( otg )
		  orderTags = otg.join(",");
		referenceId = reviewPage.find( '#referenceid' ).val();
		requiredByDate = reviewPage.find( '#rbd' ).val();

	}

	// Get UpdateInventoryInput
	var updInventoryInput = getUpdateInventoryInput( localModifications, $uid, $entity, linkedEntityId,orderType, message, orderStatus, null, paymentOption, packageSize, orderTags, referenceId, requiredByDate );
	if ( updInventoryInput ) {
		if ( payment )
			updInventoryInput.pymt = payment;
		var url = $host;
		///var type = 'inventory';
		if ( isOrder ) {
			url += '/api/o';
			///type = 'order';
		} else {
			url += '/api/i';
		}
		var postData = {};
		if ( isOrder )
			postData.a = 'uo';
		else
			postData.a = 'ui';
		postData.ty = getTransactionType( $currentOperation );
		postData.j = JSON.stringify( updInventoryInput );

		var loadingMsg = $messagetext.sending + '...';
		// Do a server post - prepare data for this
		var request = {
			type: 'POST',
			url: url,
			data: postData,
			dataType: 'json', // expected return data type
			cache: false,
			success: function(o) {
				// Clear the request saved time, given this request succeeded
				resetRequestSavedTime( $currentOperation, $uid, $entity.kid, null );
				// Process response JSON
				if ( o && ( o.st == '0' || o.st == '2' ) ) { // success or partial errors
					if ( isOrder ) {
						var ordersUrl = '#orders?oty=' + orderType +'&st=' + o.ost;
						// Save order data locally
						processOrderTransactionResponse( o, orderType, true,
							function() {
								// Show a message and go to orders page
								showDialog( $messagetext.done, $messagetext.ordersuccessfullycreatewithid+' ' + o.tid, ordersUrl );
							}, function() {
								showDialog( $messagetext.donewitherrors, $messagetext.ordersuccessfullycreatewithid+' ' + o.tid +$messagetext.couldnotstoreorderlocally, ordersUrl );
							});
					} else {
						// Get the URL of the screen to move to after processing
						var materialsUrl = '#materials?op=' + $currentOperation;
						if ( tag && tag != null )
							materialsUrl += '&tag=' + tag;
						// Update local inventory and related data (e.g. transasction history, and so on)
						processInventoryTransactionResponse( o, entityId, currentOperation, linkedEntityName, localModifications,
							function() {
								if ( o.st == '0' ) // success
									showDialog( $messagetext.done, $messagetext.inventorysuccessfullyupdated, materialsUrl );
								else // partial errors
									showPartialErrors( null, materialsUrl );
							}, function() {
								showDialog( $messagetext.donewitherrors,$messagetext.inventorysuccessfullyupdated + $messagetext.couldnotstorelocally, materialsUrl );
							} );
					}
				} else if ( o && o.st == '1' ) { // server error
					showDialog( null, o.ms, getCurrentUrl() );
				}
				var hasmoreEntities = false;
				var hasmorelinkedEntities = false;
				if (linkedIds)
				{
					 currentLIndex = currentLIndex +1;
					 $entity = getEntity( $uid, entityId );
					 hasmorelinkedEntities = (currentLIndex < linkedIds.length);
				}
				  if(!hasmorelinkedEntities)
				  {
			    	currentIndex = currentIndex + 1;
			    	currentLIndex = 0;
			    	hasmoreEntities = (currentIndex < kids.length);
			    	if (hasmoreEntities)
			    		linkedIds= getUnsentRelatedEntityIds($uid,kids[currentIndex],currentOperation);
				  }
				  // Send more transactions;
				  if (hasmorelinkedEntities || hasmoreEntities)
					  sendTransaction(currentOperation,kids,currentIndex,linkedIds,currentLIndex);			    			
			},
			sync: {
				op: currentOperation,
				uid: $uid,
				kid: $entity.kid,
				oid: linkedEntityId,
				params: ( isOrder ? { otype: orderType } : { lkn: linkedEntityName } ),
				callback: function() {
					$.mobile.changePage( '#menu' );
				},
				syncOnNetworkError: true
			}
		};
		// Log data
		var logMsg = ( isOrder ? 'o' : 'i' ) + ',' + postData.j.length;
		var logData = {
			functionName: 'sendTransaction',
			message: logMsg
		};
		// Send request
		requestServer( request, loadingMsg, logData, true, true );
	}
}

// Process the inventory transaction response
function processInventoryTransactionResponse( o, entityId, currentOperation, linkedEntityName, localModifications, successCallback, errorCallback ) {
	// Update local inventory
	var inventoryUpdated = false;
	// Log transaction history
	if ( o && o.mt ) {
		try {
			logInventoryTransaction($uid, entityId, currentOperation, linkedEntityName, localModifications, o, o.t);
		} catch (e) {
			logR('w', 'processInventoryTransactionResponse', 'Error when logging inventory transaction: ' + e.message, e.stack);
		}
	}
	try {
		// Update local inventory
		if (updateLocalInventory(o, true)) {
			if (successCallback)
				successCallback();
		} else if (errorCallback) {
			errorCallback();
		}
	} catch ( e ) {
		logR('e', 'processInventoryTransactionResponse', e.message, e.stack);
	}
}

// Preprocess inventory transaction response; returns the old state of $entity and $currentOperation
function preprocessInventoryTransactionResponse( req ) {
	var oldState = {};
	oldState.op = $currentOperation;
	// Init op
	$currentOperation = req.op;
	return oldState;
}

// Post process inventory transaction response
function postprocessInventoryTransactionResponse( oldState ) {
	$currentOperation = oldState.op;
}

// Pre-process order transaction response - returns previous state, while resetting it to the required state to process the orders
function changeTransactionContext( kid, op ) {
	var prevState = {};
	if ( $entity.kid != kid ) {
		prevState.entity = $entity;
		$entity = getEntity($uid, kid);
	}
	prevState.op = $currentOperation;
	if ( op )
		$currentOperation = op;
	return prevState;
}

// Pre-process order transaction response - returns previous state {entity, op}, while resetting it to the required state to process the orders
function resetTransactionContext( prevState ) {
	if ( !prevState )
		return;
	if ( prevState.entity )
		$entity = prevState.entity;
	if ( prevState.op )
		$currentOperation = prevState.op;
}


// Process order transaction response
function processOrderTransactionResponse( o, orderType, isNew, successCallback, errorCallback ) {
	if ( updateLocalOrder( o, orderType, isNew ) ) {
		if ( isAutoPostInventory($config)) {
			refreshInventory(null);
			var lkid = o.vid;
			if (orderType == 'sle')
				lkid = o.kid;
			if (getRelatedEntityPermissions(lkid, 'vnds' , $entity.kid) != $NO_PERMISSIONS ) {
				getInventoryForRelatedEntity(lkid, orderType, null);
			}
		}
		if ( successCallback )
			successCallback();
	} else if ( errorCallback ) {
		errorCallback();
	}
}


function saveSetupEntity( action, type, typeName,entityName, kid, linkedKioskId, uid,nextUrl,newEntity ) {

	var formData = getValidatedEntityFormData( action, type, uid );
	if ( !formData ) {
		return;
	}
	// Get the logged in user's data
	 var userData = getUserData( $uid );
	 var actionForEntity = action;
	 if ( newEntity && ( action == 'edit' ) )
		 actionForEntity = 'add';
	 var jsonData = getSetupEntityJSONInput( actionForEntity, formData, type, userData, kid, linkedKioskId, uid );
  	 var isEdit = ( action == 'edit' );
	 ///var newEntity = false;
	 var newKid = null;
	 if (action == 'add')
	  {
	  //  var kid = Math.random() * 10000000000;
		var min = 999999999;
		var max = 9999999999;
		newKid = Math.floor(Math.random() * (max - min + 1)) + min;	
		if (type == 'ents')
		  kid = newKid;
		else if ( type == 'csts' || type == 'vnds' ) 
		{
	      linkedKioskId = newKid;
	      if ($entity)
	       kid = $entity.kid;
		}
	  }
	    var entityNotSent = true;
	    
	    saveLocalEntity( action, type, jsonData, kid, linkedKioskId,uid, userData, formData.makememanager,entityNotSent);
    // update OEM	
		var eventParams ={};
		eventType = 'ne';
		eventParams.qty = 1;	
		eventParams.action = action;
        eventParams.type = type; 
        if ( type && ( type == 'csts' || type == 'vnds' ) )
        {
         	eventParams.lkid = linkedKioskId;
        }
        else
        	eventParams.lkid = kid;
        if (uid)
          eventParams.uid = uid;
        updateEvent($uid,kid,eventType,eventParams);
        var sendnow = 'yes';
	    nextUrl +='&kid='+kid +'&lkid='+linkedKioskId+'&action='+action+'&SendNow='+sendnow ;
		$entity = getEntity($uid,kid);
	 // Change page
	  $.mobile.changePage( nextUrl );
	}

// Add an entity
function sendSetupEntity( typeName, entityIds,currentIndex,linkedEntityIds,currentLIndex, uid ) {
	var newEntity = false;
	var entityId = entityIds[currentIndex];
	var linkedIds = linkedEntityIds;
	var kids = entityIds;
	var linkedKioskId;
	var eventType = 'ne';
	var currentOperation = eventType;
	var linkedEntity = getlinkedEntity($uid,entityId,linkedEntityIds[currentLIndex].kid,eventType);
	var action =linkedEntity.action;
	var type = linkedEntity.type;
	if (linkedEntity.uid)
		 uid = linkedEntity.uid;
	else
		uid = null;
    var kid = entityId;
	if (type != 'ents')
	{
		linkedKioskId = linkedEntity.kid;
	}
	$entity = getEntity($uid,kid);
	
	var formData = getValidatedEntityData(action,type,entityId,linkedEntityIds[currentLIndex].kid,uid);
	if ( !formData ) {
		return;
	}
	// Get the logged in user's data
	var userData = getUserData( $uid );
	// Submit form to create entity
	var url = $host + '/api/s';
	var postData = {};
	var newTempKid = kid;
	var isEdit = ( action == 'edit' );
	if (action == 'add')
	{
			if (type == 'ents')
			   newTempKid = kid;
		  	else
			   newTempKid = linkedKioskId ;
		    newEntity = true;
	}

	postData.a = ( isEdit ? 'uuk' : 'cuk' );
	postData.ty = ( isEdit ? ( uid ? 'u' : 'k' ) : ( formData.hasuserdata ? 'uk' : 'k' ) );
	postData.uid = $uid;
	///postData.p = $pwd; // password now sent via BasicAuth
	if ( formData.sendpasswordtouser )
		postData.np = 'true';
	var jsonData = getSetupEntityJSONInput( action, formData, type, userData,kid, linkedKioskId, uid );
	postData.j = JSON.stringify( jsonData );
	// Show page loader
	var actionText = ( isEdit ? $messagetext.updating+' ' : $messagetext.adding+' ' );
	var loadingMsg = actionText + typeName + '...';
	// Do an server post - prepare data for this
	var request = {
		type: 'POST',
		url: url,
		data: postData,
		dataType: 'json', // expected return data type
		cache: false,
		success: function(o) {
			// Process response JSON
			if ( o && o.st == '0' ) { // success
				var entitiesUrl;
				if (o.kid)
				{
			          if (!linkedKioskId)
						  kid = o.kid;
					  else
						  linkedKioskId = o.kid;
				}
				if ( type == 'ents' )
					entitiesUrl = ( isEdit ? ('#entityinfo?ty=ents&kid='+kid) : ('#entities?ty=ents') );
				else
					entitiesUrl = ( isEdit ? '#entityinfo?ty=' + type +'&kid='+kid+ '&lkid=' + linkedKioskId : '#myentityrelationships?ty=' + type );
				var entityNotSent = false;
	
				 //remove notification event
			  	var closeEventKid = kid;
			  	var linkedEntity = {};
			   	if (action == 'add')
			   	{
                	  if (type == 'ents')
                	  {
                		closeEventKid = newTempKid;
                		linkedEntity.kid = newTempKid;            		
                	  }
                	  else //csts or vnds
                	  {
                		  closeEventKid = kid;
                		  linkedEntity.kid = newTempKid;
                	  }
				}
				else // action is edit
				{
				   closeEventKid = kid;
				   if (type == 'ents')
					   linkedEntity.kid = kid;
				   else // csts or vnds
					   linkedEntity.kid = linkedKioskId;
				}
				var eventType = 'ne';
				var eventParams ={};
			    eventParams.qty = 1;
				linkedEntity.type = type;
				eventParams.linkedEntity = linkedEntity;
				eventParams.type= type;
				closeEvent($uid,closeEventKid,eventType,eventParams);
	 		   	if (action == 'add')
	 		      	removeEntity($uid,newTempKid);
			   	if ( updateLocalEntity( action, type, jsonData, kid,linkedKioskId, o.uid, userData, formData.makememanager,entityNotSent ) ) {
					// Show done dialog
					var actionName = ( action == 'add' ? $messagetext.successfullycreated : $messagetext.successfullyupdated );
					var msg =  actionName + ' <strong>' + ( uid ? formData.userfirstname + ( formData.userlastname ? ' ' + formData.userlastname : '' ) : formData.entityname ) + '</strong>.';
					if ( !isEdit && o.uid )
						msg += $messagetext.operatorassignedtothisentity+'- <strong>' + formData.userfirstname + '.</strong> (' + o.uid + ')';
					showDialog( $messagetext.done, msg, entitiesUrl );
				} else {
					showDialog( $messagetext.donewitherrors, $messagetext.entityupdatedonserver+$messagetext.couldnotstoreentitylocally , entitiesUrl );
				}
			} else if ( o && o.st == '1' ) { // server error
				var errMsg = $messagetext.entityerrorencountered; //'An error was encountered when ' + actionText + ' entity';
				if ( o.er && o.er.length > 0 )
					errMsg = o.er[0];
				if (action == 'add')
				{
			       	var editaction = 'edit';
			       	var newentitystr = 'true';
					actionparam = '&action='+editaction+'&ne='+newentitystr;
				}
				else
					actionparam = '&action='+action;
				var url = '#setupentity?ty=' + type + '&kid=' + kid + '&lkid=' + linkedKioskId + actionparam;
				if ( uid )
					url += '&uid=' + uid;
				showDialog( null, errMsg, url );
			}
				
			var hasmoreEntities = false;
			var hasmorelinkedEntities = false;
			if (linkedIds)
			{
				 currentLIndex = currentLIndex +1;
				 $entity = getEntity( $uid, entityId );
				 hasmorelinkedEntities = (currentLIndex < linkedIds.length);
			}
		   if(!hasmorelinkedEntities)
		   {
				currentIndex = currentIndex + 1;
				currentLIndex = 0;
			 	hasmoreEntities = (currentIndex < kids.length);
				if (hasmoreEntities)
					linkedIds= getUnsentRelatedEntityIds($uid,kids[currentIndex],currentOperation);
		   }
		   // Send more transactions;
		   if (hasmorelinkedEntities || hasmoreEntities)
			   sendSetupEntity(typeName, entityIds,currentIndex,linkedIds,currentLIndex, uid );
		}
		/*
		error: function(o) {
			var errMsg = $networkErrMsg;
			if ( o.responseText != '' )
				errMsg += ' [' + o.responseText + ']';
			// Show error message
			showDialog( null, errMsg,  getCurrentUrl() );
		}
		*/
	};
	var logData = {
		functionName: 'sendSetupEntity',
		message: ''
	};
	// Request server
	requestServer( request, loadingMsg, logData, true, true );
}

// Get validated add-entity form data; if only user is being edited, then uid is sent
function getValidatedEntityData( action, type,kid,linkedKioskId,uid) {
	var isEdit = ( action == 'edit' );
	var isUserObject = uid;
	var page = $( '#setupentity' );	
	 if (type == 'ents')
    	var entity = getEntity($uid,kid);
	 else
	 {
		 if (action == 'add')
		   var entity = getEntity($uid,linkedKioskId);
		 else
		  var entity = getRelatedEntityForKioskId(linkedKioskId,type,kid);
	 }
	 
	if (!entity)
	{
		  return false;
	}
	var entityName = entity.n;
	var newEntityName = null;
	if (action == 'edit')
		newEntityName = entity.nn;
	var state = entity.ste;
	var district = entity.dst;
	var taluk = entity.tlk;	
	var city = entity.cty;
	var street =entity.sa;
	var pincode = entity.pc;
	var ne = entity.ne;
	var routeTag = '';
	var hasEntityData = ( entityName != '' );
	var hasUserData = false;
	var idx;
	if (uid)
	{
	  for (i=0;i<entity.us.length;i++){
            if (uid == entity.us[i].uid)
	        idx = i;
		}
	}
	else 
		idx = 0;
	if ('husr' in entity || uid)
	{
	 if (entity.husr || uid)
	  {
	    
		if (idx >= 0)	
		{
		 if (entity.us[idx])
		 {
			var userFirstName =entity.us[idx].fn;
			var userLastName =entity.us[idx].ln;
			var countryCode = entity.us[idx].cc;
			var formattedPhone = entity.us[idx].mob;
			var userState = entity.us[idx].ste;
			var userId = entity.us[idx].uid;
			var password = entity.us[idx].p;
		 }
	   }
	  }
	 hasUserData = true;
	 var useGeoCodes = false;
	 useGeoCodes = entity.geo;
	 var makeMeManager = false;
	 makeMeManager = entity.mmm;
	 var sendPasswordToUser = false;
	 sendPasswordToUser = entity.pwu;
	}
	if ( entity.rtt )
		routeTag = entity.rtt;

	var data = {};
	// Create an intermediate JSON data object
	data.entityname = uppercaseFirstChar( entityName );
	if (newEntityName != '')
		data.newentityname = uppercaseFirstChar( newEntityName );
	data.state = uppercaseFirstChar( state );
	if ( userState && userState != '' )
		data.userstate = uppercaseFirstChar( userState ); // user's state, if user details specified
	if ( district != '' )
		data.district = uppercaseFirstChar( district );
	if ( taluk != '' )
		data.taluk = uppercaseFirstChar( taluk );
	data.city = uppercaseFirstChar( city );
	if ( street != '' )
		data.street = street;
	if ( pincode != '' )
		data.pincode = pincode;
	if ( userFirstName != '' )
		data.userfirstname = uppercaseFirstChar( userFirstName );
	if ( userLastName != '' )
		data.userlastname = uppercaseFirstChar( userLastName );
	if ( formattedPhone != '' ){
		data.mobilephone = formattedPhone;				
	}
	if ( userId != '' )
		data.userid = userId;
	if ( password != '' )
		data.password = password;
	 data.hasentitydata = hasEntityData;
	 data.hasuserdata = hasUserData;
	// Whether geo-codes are to be sent or not
	 data.usegeocodes = useGeoCodes;
	 data.makememanager = makeMeManager;
	 data.sendpasswordtouser = ( hasUserData && sendPasswordToUser );
	data.routetag = routeTag;

	return data;
}

//Get validated add-entity form data; if only user is being edited, then uid is sent
function getValidatedEntityFormData( action, type, uid) {
	var isEdit = ( action == 'edit' );
	var isUserObject = uid;
	var page = $( '#setupentity' );
	var entityName = page.find( '#entityname' ).val().trim();
	var newEntityName =null;
	if (action == 'edit')
		newEntityName = page.find( '#newentityname' ).val().trim();
	var state =page.find( '#state' ).val().trim();
	var district = page.find( '#district' ).val().trim();
	var taluk = page.find( '#taluk' ).val().trim();		
	var city = page.find( '#city' ).val().trim();
	var street = page.find( '#street' ).val().trim();
	var pincode = page.find( '#pincode' ).val().trim();
	var userFirstName = page.find( '#userfirstname' ).val().trim();
	var userLastName = page.find( '#userlastname' ).val().trim();
	var countryCode = page.find('#countryCode').val().trim();
	var mobilePhone = page.find( '#mobilephone' ).val().trim();
	var userState = page.find( '#userstate' ).val().trim();
	var userId = page.find( '#entityuserid' ).val().trim();
	var password = page.find( '#entitypassword' ).val();
	var confirmPassword = page.find( '#entityconfirmpassword' ).val();
	var useGeoCodes = page.find( '#usegeocodes' ).is(':checked');
	var makeMeManager = page.find( '#makememanager' ).is(':checked');
	var sendPasswordToUser = page.find( '#sendpasswordtouser' ); // present only during Add (not Edit)
	var routeTag = page.find( '#routetag option:selected' );
	if ( routeTag )
		routeTag = routeTag.val();
	if ( sendPasswordToUser )
		sendPasswordToUser = sendPasswordToUser.is(':checked');
	// Validate mandatory fields
	if ( countryCode.indexOf( '+' ) == -1 ) // append a + to country code, if necessary
		countryCode = '+' + countryCode;
	var formattedPhone = countryCode + ' ' + mobilePhone;
	var valid = true;
	if ( !isEdit || !isUserObject ) {
		valid = validateFormField( 'entityname', 'text', 1, 200, entityName, 'entitynamestatus' );
		valid = validateFormField( 'state', 'text', 1, 200, state, 'statestatus' ) && valid;
		valid = validateFormField( 'city', 'text', 1, 200, city, 'citystatus' ) && valid;
	}
	var hasUserData = ( userFirstName != ''); //&& mobilePhone != '' );
	var hasEntityData = ( entityName != '' );
	if ( ( isEdit && isUserObject ) || ( !isEdit && ( type == 'csts' || type == 'vnds' || hasUserData ) ) ) { // user details is mandatory
		valid = validateFormField( 'userfirstname', 'text', 1, 20, userFirstName, 'userfirstnamestatus' ) && valid;
		valid = validateFormField( 'mobilephone', 'text', 8, 15, formattedPhone, 'mobilephonestatus' ) && valid;
		if ( !isEdit ) {
			valid = validateFormField( 'entityuserid', 'text', 4, 50, userId, 'entityuseridstatus' ) && valid;
			valid = validateFormField( 'entitypassword', 'text', 4, 20, password, 'entitypasswordstatus' ) && valid;
			if ( password != confirmPassword )
				$( '#entityconfirmpasswordstatus' ).show();
		}
	}
	if ( !valid )
		return undefined;
	var data = {};
	// Create an intermediate JSON data object
	data.entityname = uppercaseFirstChar( entityName );
	if (newEntityName != '')
		data.newentityname = uppercaseFirstChar( newEntityName );
	data.state = uppercaseFirstChar( state );
	if ( userState && userState != '' )
		data.userstate = uppercaseFirstChar( userState ); // user's state, if user details specified
	if ( district != '' )
		data.district = uppercaseFirstChar( district );
	if ( taluk != '' )
		data.taluk = uppercaseFirstChar( taluk );
	data.city = uppercaseFirstChar( city );
	if ( street != '' )
		data.street = street;
	if ( pincode != '' )
		data.pincode = pincode;
	if ( userFirstName != '' )
		data.userfirstname = uppercaseFirstChar( userFirstName );
	if ( userLastName != '' )
		data.userlastname = uppercaseFirstChar( userLastName );
	if ( formattedPhone != '' ){
		data.mobilephone = formattedPhone;				
	}
	if ( userId != '' )
		data.userid = userId;
	if ( password != '' )
		data.password = password;
	data.hasentitydata = hasEntityData;
	// Whether geo-codes are to be sent or not
	data.usegeocodes = useGeoCodes;
	data.routetag = routeTag;
//	if (type != 'ents')
	//{
	data.makememanager = makeMeManager;
	data.sendpasswordtouser = ( hasUserData && sendPasswordToUser );
	data.hasuserdata = hasUserData;
//	}


	return data;
}



// Get the first character in uppercase
function uppercaseFirstChar( str ) {
	if ( !str || str.length == 0 )
		return str;
	return str.substring(0,1).toUpperCase() + str.substring(1);
}

// Get the setup entity JSON input
function getSetupEntityJSONInput( action, data, type, userData,kid, linkedKioskId, uid ) {
	var isEdit = ( action == 'edit' );
	var isUserObject = uid;
	var json = {};
	json.v = '01';
	if ( !isEdit || !isUserObject ) { // fill kiosk data
		// Kiosk data
		json.k = {};
		json.k.n = data.entityname;
		if ( isEdit && data.entityName != data.newentityname ) { // implies edit, so send new name, if differnet from original name
			json.k.n = data.entityname;
			json.k.nn = data.newentityname;
		}
		json.k.cn = userData.cn;
		json.k.ste = data.state;
		json.k.cty = data.city;
		// Add currency, if any
		if ( $config.cu )
			json.k.cu = $config.cu;
		// Add route tag, if any
		if ( data.routetag )
			json.k.rtt = data.routetag;
		// Add entity manager, if needed
		var geo; // geo-codes object
		if ( !isEdit ) { // Add
			if ( data.usegeocodes )
				geo = getGeo();
			if ( type == 'ents' ) {
				json.k.us = [];
				json.k.us.push( $uid ); // make current user the entity manager for this entity
			} else if ( data.makememanager ) { // make this user the manager of this customer/vendor
				json.k.us = [];
				json.k.us.push( $uid );
			}
			// Add entity operator, if needed
			if ( data.userid ) {
				if ( !json.k.us )
					json.k.us = [];
				json.k.us.push( data.userid );
			}
			// Add default route index
			if ( data.routetag )
				json.k.rti = $ROUTEINDEX_DEFAULT + '';
			// added for saving new entity locally
			
		} else { // Edit
			// Get the current users for this entity and place them here
			var entity;		
			if ( type == 'ents' )
			 {
				entity = $entity;
			 }
			else if ( linkedKioskId )
				{
				   if (action == 'edit')
				     entity =  getRelatedEntityForKioskId(linkedKioskId,type,kid);
				   else 
					  entity = getEntity($uid,linkedKioskId);
				}
			
			if ( data.usegeocodes )
				geo = getGeo();
			else if ( entity.lat && entity.lng ) {
				geo = {};
				geo.lat = entity.lat;
				geo.lng = entity.lng;
				geo.accuracy = entity.gacc;
				geo.err = entity.gerr;
			}
			if ('ne' in entity)
			{
		  	 if (entity.ne)
		  	 {
			   json.k.ne = entity.ne;
			   isEdit = false;//add user;
		  	 }
			}
			// Get user IDs array
			json.k.us = [];
			if ( entity.us ) {
				var hasMeAsUser = false;
				for ( var i = 0; i < entity.us.length; i++ ) {
					json.k.us.push( entity.us[i].uid );
					if ( !hasMeAsUser )
						hasMeAsUser = ( $uid == entity.us[i].uid );
				}
				// If this user has to be made a manager of a customer/vendor, then do so, if he's not already a user on this entity
				if ( type != 'ents' && data.makememanager && !hasMeAsUser )
					json.k.us.push( $uid );
			}
			// Update route index, if any
			if ( entity.rti )
				json.k.rti = entity.rti + '';
		}
		// Add geo-codes if needed
		if ( geo ) {
			if ( !geo.err ) {
				json.k.lat = geo.lat + '';
				json.k.lng = geo.lng + '';
				if ( geo.accuracy )
					json.k.gacc = geo.accuracy + '';
			} else {
				json.k.gerr = geo.err + '';
			}
		}
		json.k.aam = 'yes'; // add all materials by default
		// Add relationships, if needed
		if ( type == 'csts' ) {
			json.k.vnds = [];
			json.k.vnds.push( $entity.n );
		} else if ( type == 'vnds' ) {
			json.k.csts = [];
			json.k.csts.push( $entity.n );
		}
	}
	if ( (action == 'add') && (data.entityName != data.newentityname )) { 
		json.k.n = data.newentityname;
	}
	// User data, if provided
	if ( data.hasuserdata ) {
		json.u = {};
		if ( isEdit ) {
			var user = getEntityUser( type, linkedKioskId, uid );
			json.u = user;
			json.u.p = ''; // send no password, but just the field given its required
			json.u.fn = data.userfirstname;
			if ( data.userlastname )
				json.u.ln = data.userlastname;
			json.u.mob = data.mobilephone;
			if ( data.userstate )
				json.u.ste = data.userstate;
		} else {
			json.u.uid = data.userid;
			json.u.p = data.password;
			json.u.rle = 'ROLE_ko'; // default to kiosk operator
			json.u.fn = data.userfirstname;
			if ( data.userlastname )
				json.u.ln = data.userlastname;
			json.u.mob = data.mobilephone;
			if ( data.userstate )
				json.u.ste = data.userstate;
			else
				json.u.ste = data.state;
			json.u.cty = data.city;
		}
		// Locale (common to add/edit)
		json.u.lg = userData.lg;
		json.u.tz = userData.tz;
		json.u.cn = userData.cn;
	}
	// Common fields
	if ( data.district ) {
		if ( json.k )
			json.k.dst = data.district;
		if ( json.u )
			json.u.dst = data.district;
	}
	if ( data.taluk ) {
		if ( json.k )
			json.k.tlk = data.taluk;
		if ( json.u )
			json.u.tlk = data.taluk;
	}
	if ( data.street ) {
		if ( json.k )
			json.k.sa = data.street + '';
		if ( json.u )
			json.u.sa = data.street + '';
	}
	if ( data.pincode ) {
		if ( json.k )
			json.k.pc = data.pincode + '';
		if ( json.u )
			json.u.pc = data.pincode + '';
	}
	if(data.hasuserdata && !isEdit) //only for new entity
	{
	json.k.husr = data.hasuserdata;
	json.k.geo =  data.usegeocodes;
	json.k.mmm =  data.makememanager ;
	json.k.pwu  = data.sendpasswordtouser;
	}
	// Kiosk data
	return json;
}

// Get an entity's user depending on the type of entity (managed-entity or customer/vendor)
function getEntityUser( type, linkedKioskId, uid ) {
	var entity;
	if ( type == 'ents' )
		entity = $entity;
	else
		entity = getRelatedEntity( linkedKioskId, type );
	if ( !entity || !entity.us )
		return undefined;
	for ( var i = 0; i < entity.us.length; i++ ) {
		if ( entity.us[i].uid == uid )
			return entity.us[i];
	}
	return undefined;
}




//Update local entity, using form-data 'data'
function updateLocalEntity( action, type, jsonData, kid, linkedKioskId,uid, userData,entityNotSent ) {
	if ( !jsonData )
		return false;
	var isAdd = ( action == 'add' );
	// Is route enabled?
	var isRouteEnabled;
	if ( type == 'ents' )
		isRouteEnabled = userData.rteu;
	else
		isRouteEnabled = $entity[ 'rte' + type ];
	// Add or edit
	if ( isAdd ) { // Add
		// Get entity
		var entity = jsonData.k;
		// Remove unecessary fields
		if ( entity.aam ) // add all materials
			delete entity.aam;
		if ( entity.csts )
			delete entity.csts;
		if ( entity.vnds )
			delete entity.vnds;
		if ( entity.us )
			delete entity.us;
		if ( entity.husr )
			delete entity.husr;
		makeMeManager = entity.mmm;
		if ( entity.mmm )
			delete entity.mmm;
		if ( entity.geo )
			delete entity.geo;
		if ( entity.pwu )
			delete entity.pwu;
		if ( entity.ne )
			delete entity.ne;
		// Assign entity id
	   if (type == 'ents')
		entity.kid = kid;
	   else
		   entity.kid = linkedKioskId;
		// Update the user data for this entity, if needed
		// NOTE: For an entity, us is an array of user metadata objects (not user IDs alone)
		if ( jsonData.u ) {
			entity.us = [];
			// Remove password, if any (will not allow password storing/editing here)
			if ( jsonData.u.p )
				delete jsonData.u.p;
			// Enable edit permissions
			jsonData.u.prms = 'e';
			// Add to the list
			entity.us.push( jsonData.u );
		}
		// Persist
		  entity.ty = type;
		if ( type == 'ents' ) { // store an managed entity
			if ( !entity.us )
				entity.us = [];
			// Update and persist entity locally
			updateLocalManagedEntity( kid, entity, userData );
		} else { // store as a related entity
			if ( !$entity[ type ] )
				$entity[ type ] = [];
			//store only credit limit, route parameters in main entity
			//$entity[ type ].push( entity );
		    var linkedEntity = getRelatedEntityFields(entity)
		    $entity[ type ].push (linkedEntity);
			//Remove related entity fields
			linkedEntity = removeRelatedEntityFields( entity );
			storeEntity( $uid,linkedEntity );
			// Check if routing is enabled or not
			var routingKey = 'rte' + type;
			var isRouteEnabled = $entity[ routingKey ];
			// Sort entities
			if ( !isRouteEnabled )
				sortEntities( $entity[ type ], type, false );
			// Persist entity
			storeEntity( $uid, $entity );
			// Update the corresponding search index
			updateRelatedEntitiesIndices( $entity[ type ], type, isRouteEnabled );
			// Update the managed entity info., if needed
			if ( makeMeManager )
				updateLocalManagedEntity( linkedKioskId, entity, userData );
		}
	} else { // Edit
		var entity;
		if ( type == 'ents' )
			entity = $entity;
		else
			entity = getRelatedEntity( linkedKioskId, type );
		if ( !entity )
			return false;
		entity.ty = type;
		var updateEntityIndices = false;			  
		var k = jsonData.k;
		if ( k ) {
			  if ( k.nn && k.nn != '' && k.nn != entity.n ) // name
			 {
			   if (entityNotSent)
				entity.nn = k.nn;
			   else
				{
				 entity.n = k.nn;
				 entity.nn = null;
				}
			 }
			entity.cn = ( k.cn ? k.cn : '' ); // country
			entity.ste = ( k.ste ? k.ste : '' ); // state
			entity.dst = ( k.dst ? k.dst : '' ); // district
			entity.tlk = ( k.tlk ? k.tlk : '' ); // taluk
			entity.cty = k.cty; // city
			entity.sa = ( k.sa ? k.sa : '' ); // street address
			entity.pc = ( k.pc ? k.pc : '' ); // pincode
			// Location data
			if ( k.lat )
				entity.lat = k.lat;
			if ( k.lng )
				entity.lng = k.lng;
			if ( k.gacc )
				entity.gacc = k.gacc;
			if ( k.gerr )
				entity.gerr = k.gerr;
			// Update route tag and corresponding indices, as required
			if ( k.rtt || entity.rtt ) {
				var noTagChange = ( k.rtt && entity.rtt && k.rtt == entity.rtt );
				if ( !noTagChange ) { // tag has changed
					if ( k.rtt ) {
						entity.rtt = k.rtt;
					} else {
						delete entity.rtt;
					}
					updateEntityIndices = true;
				}
			}
		}
		// Update user, if needed
		if ( jsonData.u ) {
			var users = [];
			if ( entity.us ) {
				for ( var i = 0; i < entity.us.length; i++ ) {
					if ( entity.us[i].uid == jsonData.u.uid )
						users.push( jsonData.u );
					else
						users.push( entity.us[i] );
				}
			}
			entity.us = users;
		}
		// Persist - for managed-entity/related-entity, simply persist current entity
		storeEntity( $uid, $entity );
		if ( $entity.kid != entity.kid ) {//store related entity
			var linkedEntity = removeRelatedEntityFields(entity)
			storeEntity($uid, linkedEntity);
		}
		// Update the search/tag indices for this entity
		if ( updateEntityIndices ) {
			if ( type == 'ents' ) {
				updateManagedEntitiesIndices(userData);
			}
			else
				updateRelatedEntitiesIndices( $entity[type], type, isRouteEnabled );
		}
	}
	
	// Store user data (not the entities)
	return true;
}



//Update local entity, using form-data 'data'
function saveLocalEntity( action, type, jsonData, kid, linkedKioskId,uid, userData, makeMeManager,entityNotSent ) {
	if ( !jsonData )
		return false;
	var isAdd = ( action == 'add' );
	// Is route enabled?
	var isRouteEnabled;
	if ( type == 'ents' )
		isRouteEnabled = userData.rteu;
	else
		isRouteEnabled = $entity[ 'rte' + type ];
	var newRelatedEntity = false;
	// Add or edit
	if ( isAdd ) { // Add
		// Get entity
		var entity = jsonData.k;
		// Remove unecessary fields
		if ( entity.aam ) // add all materials
			delete entity.aam;
		if ( entity.csts )
			delete entity.csts;
		if ( entity.vnds )
			delete entity.vnds;
		if ( entity.us )
			delete entity.us;
		// Assign entity id
		if (type == 'ents')
		 entity.kid = kid;
		else
		 {
			entity.kid = linkedKioskId;
		 }
		  entity.ne = true;
		  entity.ty = type;

		// Update the user data for this entity, if needed
		// NOTE: For an entity, us is an array of user metadata objects (not user IDs alone)
		if ( jsonData.u ) {
			entity.us = [];
			// Remove password, if any (will not allow password storing/editing here)
			//if ( jsonData.u.p )
			//	delete jsonData.u.p;
			// Enable edit permissions
			jsonData.u.prms = 'e';
			// Add to the list
			entity.us.push( jsonData.u );
		}
		// Persist
		  entity.ty = type;
		if ( type == 'ents' ) { // store an managed entity
			if ( !entity.us )
				entity.us = [];
			 entity.us.push( getUserFromUserData( $uid ) );

		} 
		entity.mmm = makeMeManager;
		// Store entity
		storeEntity( $uid, entity );

	} else { // Edit
		var entity;
		if ( type == 'ents' )
			entity = $entity;
		else
		{
			  entity = getRelatedEntity( linkedKioskId, type );
			 if (!entity)
			 {
			  entity = getEntity ($uid, linkedKioskId );
			  newRelatedEntity = true;
			 }
		}
		if ( !entity )
			return false;
		entity.ty = type;
		var updateEntityIndices = false;			  
		var k = jsonData.k;
		if ( k ) {
			 entity.n = k.n;
			  if ( k.nn && k.nn != '' && k.nn != entity.n ) // name
			 {
			   if (entityNotSent)
				entity.nn = k.nn;
			   else
				{
				 entity.n = k.nn;
				 entity.nn = null;
				}
			 }
			entity.cn = ( k.cn ? k.cn : '' ); // country
			entity.ste = ( k.ste ? k.ste : '' ); // state
			entity.dst = ( k.dst ? k.dst : '' ); // district
			entity.tlk = ( k.tlk ? k.tlk : '' ); // taluk
			entity.cty = k.cty; // city
			entity.sa = ( k.sa ? k.sa : '' ); // street address
			entity.pc = ( k.pc ? k.pc : '' ); // pincode
			// Location data
			if ( k.lat )
				entity.lat = k.lat;
			if ( k.lng )
				entity.lng = k.lng;
			if ( k.gacc )
				entity.gacc = k.gacc;
			if ( k.gerr )
				entity.gerr = k.gerr;
			// Update route tag and corresponding indices, as required
			if ( k.rtt || entity.rtt ) {
				var noTagChange = ( k.rtt && entity.rtt && k.rtt == entity.rtt );
				if ( !noTagChange ) { // tag has changed
					if ( k.rtt ) {
						entity.rtt = k.rtt;
					} else {
						delete entity.rtt;
					}
					updateEntityIndices = true;
				}
			}
		}
		// Update user, if needed
		if ( jsonData.u ) {
			var users = [];
			if ( entity.us ) {
				for ( var i = 0; i < entity.us.length; i++ ) {
					if ( entity.us[i].uid == jsonData.u.uid )
						users.push( jsonData.u );
					else
						users.push( entity.us[i] );
				}
			}
			entity.us = users;
		}
		if ( newRelatedEntity )
			$entity = entity;
		else if ( $entity.kid != entity.kid ) { //store related entity
			var linkedEntity = removeRelatedEntityFields(entity)
			storeEntity($uid, linkedEntity);
		}
		// Persist - for managed-entity/related-entity, simply persist current entity
		storeEntity( $uid, $entity );
	}
	
	// Store user data (not the entities)
	return true;
}

// Update the local managed entity
function updateLocalManagedEntity( kid, entity, userData ) {
	entity.us.push( getUserFromUserData( $uid ) );
	// Store entity
	storeEntity( $uid, entity );
	// Add this entity to userData
	userData.ki.push( kid );
	var isRouteEnabled= userData.rteu;
	 if ( !$entitiesData.ents && !isRouteEnabled ) // no tags
		sortEntities( userData.ki, 'ents', false ); // sort entities alphabetically
	// Persist entity ordering
	 storeUserData( $uid, userData, false, false );
	// Update managed entities indices
	 updateManagedEntitiesIndices( userData );
	
}

// Get user from userData
function getUserFromUserData( uid ) {
	var userData = getUserData( uid );
	if ( !userData )
		return undefined;
	var user = {};
	user.uid = uid;
	user.fn = userData.fn;
	if ( userData.ln )
		user.ln = userData.ln;
	user.mob = userData.mob;
	user.rle = userData.rle;
	user.cn = userData.cn;
	user.tz = userData.tz;
	user.lg = userData.lg;
	return user;
}

// Sort an entity list of a given type, by route or alphabetically
function sortEntities( list, type, sortByRoute ) {
	if ( !list )
		return;
	list.sort( function(a,b) {
		var eA, eB;
		if ( type == 'ents' ) {
			eA = getEntity( $uid, a );
			eB = getEntity( $uid, b );
		} else {
			if ( typeof a == 'string' ) {
				eA = getRelatedEntity( a, type );
				eB = getRelatedEntity( b, type );
			} else {
				eA = a;
				eB = b;
			}
		}
		var a1, b1;
		if ( eA && eB ) {
			var a1, b1;
			if ( sortByRoute ) {
				a1 = ( eA.rti ? parseInt( eA.rti ) : 0 );
				b1 = ( eB.rti ? parseInt( eB.rti ) : 0 );
			} else {
				a1 = eA.n.toLowerCase();
				b1 = eB.n.toLowerCase();
			}
			if ( a1 > b1 )
				return 1;
			else if ( a1 < b1 )
				return -1;
			else
				return 0;
		}
	});
}

// Generate user Id
function generateUserCredentials( userName ) {
	if ( userName == '' )
		return;
	// Get the first 4 chars.
	var id = userName.toLowerCase();
	if ( userName.length > 4 )
		id = id.substring( 0, 4 );
	id = id.replace( / /g, '' ); // remove space
	id = id.replace( /\./g, '' ); // remove .
	id = id.replace( /,/g, '' ); // remove ,
	// Generate 4 random numbers between 0-1 and append
	id += getRandomStr( 3 );
	// Get the password
	var password = getRandomStr( 6 ); // a 5-digit number
	// Set it in the form fields
	$( '#entityuserid' ).val( id );
	$( '#entitypassword' ).val( password );
	$( '#entityconfirmpassword' ).val( password );
}

// Generate random number
function getRandomStr( numDigits ) {
	var num = '';
	for ( var i = 0; i < numDigits; i++ )
		num += Math.floor( Math.random() * 10 );
	return num;
}

// Clear all edits to a given order
function clearOrderEdits( oid, orderType ) {
	var order = getOrder( oid, orderType );
	if ( !order )
		return;
	var matsToRemove = [];
	for ( var i = 0; i < order.mt.length; i++ ) {
		if (order.mt[i].nq)
			delete order.mt[i].nq;
		if (!order.mt[i].q) // i.e. no quantity, which means it is a newly added item to order
			matsToRemove.push(i);
		if (order.dirty && order.dirty.alq) { // remove order allocation
			if ($.inArray(order.mt[i].mid.toString(), order.dirty.alq) != -1) {
				if (order.mt[i].nalq != null) {
					delete order.mt[i].nalq;
					delete order.mt[i].nmst;
				}
				if (order.mt[i].bt) {
					var batchesToRemove = [];
					for (var j = 0; j < order.mt[i].bt.length; j++) {
						if (order.mt[i].bt[j].nalq != null) {
							delete order.mt[i].bt[j].nalq;
							delete order.mt[i].bt[j].nmst;
						}
						if (order.mt[i].bt[j].alq == null) {
							batchesToRemove.push(j);
						}
					}
					for ( var j = 0; j < batchesToRemove.length; j++ ) {
						var bindex = batchesToRemove[j] - j;
						order.mt[i].bt.splice( bindex, 1 );
					}
				}
			}
		}

	}
	// Remove materials entirely from edited order, if required (esp. when a clear all is done, and newly added items are to be removed)
	for ( var i = 0; i < matsToRemove.length; i++ ) {
		var index = matsToRemove[i] - i;
		order.mt.splice( index, 1 );
	}
	if ( order.dirty )
		delete order.dirty;
	// Persist changes
	storeEntity( $uid, $entity );
}

// Check to see if an material exists in the order
function isMaterialInOrder( mid, order ) {
	if ( !order.mt )
		return false;
	for ( var i = 0; i < order.mt.length; i++ )
		if ( order.mt[i].mid == mid )
			return true;
	return false;
}

// Get the materials Ids in an order
function getMidsInOrder( order ) {
	var mids = [];
	if ( order.mt && order.mt.length > 0 ) {
		for ( var i = 0; i < order.mt.length; i++ )
			mids.push( order.mt[i].mid );
	}
	return mids;
}

// Update an order with the server
function updateOrderRemote( oid, orderType ) {
	// Get the order
	var order = getOrder( oid, orderType );	
	if (order.kid)
		var linkedEntityId = order.kid;
    else
 	   var linkedEntityId = null;
	if ( !order )
		return;
	if ( !isOrderEdited( order ) ) {
		showDialog( $messagetext.nochanges, $messagetext.nochangestosend,  getCurrentUrl() );
		return;
	}
	// Get the local modification data object from an order
	var localModifications = {};
	for ( var i = 0; i < order.mt.length; i++ ) {
		var material = order.mt[i];
		var materialQtyModified = false;
		var invMaterial = $materialData.materialmap[ material.mid ];
		var modQuantity = {};
		if ( material.nq != null ) {
			modQuantity.q = material.nq;
			if (material.rsn)
			  modQuantity.reason = material.rsn;
			if (material.rsneoq)
				modQuantity.rsneoq = material.rsneoq;
			if (material.rsnirq)
				modQuantity.rsnirq = material.rsnirq;
			// Remove this from the order object
			delete material.nq;
			materialQtyModified = true;
		}
9
		if  (order.dirty && order.dirty.alq &&  ( $.inArray( material.mid.toString(), order.dirty.alq) != -1 ) || (materialQtyModified)) {
			if (material.alq == 0)
				modQuantity.alq = 0;
			if (  material.nalq != null ){
				modQuantity.alq = material.nalq;
			}
			else if ( material.alq != null)
				modQuantity.alq = material.alq;
			if (material.nmst)
				modQuantity.mst = material.nmst;
			else if (material.mst)
				modQuantity.mst = material.mst;
			if (material.bt) {
				var orderbt = material.bt;
				modQuantity.bt = [];
				for (var j = 0; j < orderbt.length; j++) {
					if (orderbt[j].alq != null || orderbt[j].nalq != null) {
						var bt = {};
						bt.bid = orderbt[j].bid;
						if (orderbt[j].nalq != null )
							bt.alq = orderbt[j].nalq;
						else if (orderbt[j].alq != null  )
							bt.alq = orderbt[j].alq;
						if (orderbt[j].nmst)
							bt.mst = orderbt[j].nmst;
						else if (orderbt[j].mst)
							bt.mst = orderbt[j].mst;
						modQuantity.bt.push(bt);
					}
				}
			}
			else {//material batch is not present
				if (invMaterial.ben && !material.bt) //material is batch enabled but not order batches, allocation removed.
					delete modQuantity.bt;
			}
			if (modQuantity){
				if ( material.q != null && !materialQtyModified)
					modQuantity.q = material.q;
			}

		}

		if (modQuantity && !$.isEmptyObject( modQuantity ))
			localModifications[ material.mid ] = modQuantity;
	}
	// Persist changes (of deleting nq)
	storeEntity( $uid, $entity );
	// Get any form fields (such as message or initial status)
	var orderInfoPage = $( '#orderinfo' );
	var message = orderInfoPage.find( '#transmessage' ).val();
	var orderStatus = null;
	var orderStatusElement = orderInfoPage.find( '#initialorderstatus' );
	if ( orderStatusElement && orderStatusElement.attr( 'checked' ) )
		orderStatus = orderStatusElement.val();
	var paymentOption = null;
	if ( order.dirty && order.dirty.popt != undefined )
		paymentOption = order.dirty.popt;
	else if ( order.popt )
		paymentOption = order.popt;
	var packageSize = null;
	if ( order.dirty && order.dirty.pksz != undefined )
		packageSize = order.dirty.pksz;
	else if ( order.pksz )
		packageSize = order.pksz;
	var orderTags = null;
	if ( order.dirty  && order.dirty.tg != undefined)
		orderTags = order.dirty.tg;
	else if ( order.tg)
		orderTags = order.tg;
	var referenceId = null;
	if ( order.dirty && order.dirty.rid != undefined)
		referenceId = order.dirty.rid;
	else if (order.rid)
		referenceId = order.rid;
	var requiredByDate = null;
	if ( order.dirty && order.dirty.rbd != undefined)
		requiredByDate = order.dirty.rbd;
	else if (order.rbd)
		requiredByDate = order.rbd;
	// Get the UpdateInventory JSON
	var updInventoryInput = getUpdateInventoryInput( localModifications, $uid, $entity, linkedEntityId, orderType, message, orderStatus, oid, paymentOption, packageSize, orderTags, referenceId, requiredByDate  );
	if ( updInventoryInput ) {
		if (order.dirty && order.dirty.pymt != undefined){
			updInventoryInput.pymt = order.dirty.pymt.toString();
		}
		var url = $host + '/api/o';
		var postData = {};
		postData.a = 'uo';
		///postData.p = $pwd; // password is sent via BasicAuth header
		postData.ty = 'oo'; // update order
		postData.j = JSON.stringify( updInventoryInput );
		// Page loader message
		var loadingmsg = $labeltext.updatingorder;
		// Send to server - preapare data for this
		var request = {
			type: 'POST',
			url: url,
			data: postData,
			dataType: 'json', // expected return data type
			cache: false,
			success: function(o) {
				// Process response JSON
				if ( o && o.st == '0' ) { // success
					processOrderTransactionResponse( o, orderType, false, function() {
						// Show a message and go to orders page
						var ordersUrl = '#orderinfo?oid=' + oid + '&oty=' + orderType;
						showDialog( $messagetext.done,$messagetext.ordersuccessfullyupdated+' ' + o.tid , ordersUrl );
					}, function() {
						showDialog( $messagetext.donewitherrors, $messagetext.ordersuccessfullyupdated+' ' + o.tid+ ' ' + $messagetext.couldnotstoreorderlocally, ordersUrl );
					});
					/*
					if ( updateLocalOrder( o, orderType, false ) ) { // update purchase orders
						// Show a message and go to orders page
						var ordersUrl = '#orderinfo?oid=' + oid + '&oty=' + orderType;
						showDialog( $messagetext.done,$messagetext.ordersuccessfullyupdated+' ' + o.tid , ordersUrl );
					} else {
						showDialog( $messagetext.donewitherrors, $messagetext.ordersuccessfullyupdated+' ' + o.tid+ ' ' + $messagetext.couldnotstoreorderlocally, ordersUrl );
					}
					*/
				} else if ( o && o.st == '1' ) { // server error
					showDialog( null, o.ms,  getCurrentUrl() );
				}
			},
			sync: {
				op: 'uo', // update order
				uid: $uid,
				kid: $entity.kid,
				oid: oid,
				params: { otype: orderType },
				syncOnNetworkError: true
			}
			/*
			error: function(o) {
				var errMsg = $networkErrMsg;
				if ( o.responseText != '' )
					errMsg += ' [' + o.responseText + ']';
				// Show error message
				showDialog( null, errMsg, null );
			}
			*/
		};
		var logData = {
			functionName: 'updateOrderRemote',
			message: oid + ',' + orderType
		};
		// Request server
		requestServer( request, loadingmsg, logData, true, true );
	}
}

// Compute the price of an order
function computeOrderPrice( items, formatted ) {
	var price = 0, priceWithTax = 0;
	var currency = ( $entity.cu ? $entity.cu : $config.cu );
	var tax = $entity.tx;
	if ( !items || items.length == 0 )
		return ( formatted ? '0' : 0 );
	for ( var i = 0; i < items.length; i++ ) {
		var item = items[i];
		var material = $materialData.materialmap[ item.mid ];
		if ( material && material.rp && material.rp != 0 ) {
			var q = 0;
			if ( item.nq )
				q = item.nq;
			else
				q = item.q;
			// Some materials may have separate tax; apply that, if needed
			if ( material.tx && material.tx != 0 ) {
				var p = ( q * material.rp );
				priceWithTax += p + ( p * parseFloat( material.tx ) / 100 );
			} else
				price += ( q * material.rp );
			if ( !currency && material.cu ) // if nothing, get the currency from one of the materials
				currency = material.cu;
		}
	}
	// Apply tax, if necessary
	if ( tax && tax != 0 && price != 0 )
		price += ( price * tax ) / 100;
	// Update price with tax
	price += priceWithTax;
	// Return price
	if ( formatted ) {
		var formattedPrice = '';
		if ( price != 0 ) {
			if ( currency )
				formattedPrice = currency + ' ';
			formattedPrice += getFormattedPrice( price );
			if ( tax && tax != 0 )
				formattedPrice += ' (' + tax + '% tax)';
		}
		return formattedPrice;
	} else {
		return price;
	}
}

// Get formatted price to two decimals
function getFormattedPrice( price ) {
	var roundedPrice = round( price, 2 ); /// Math.round( price * Math.pow(10,2) ) / Math.pow(10,2);
	var priceStr = roundedPrice + '';
	var decimalIndex = priceStr.indexOf( '.' );
	if ( decimalIndex != -1 && priceStr.substring( decimalIndex + 1 ).length == 1 )
		priceStr += '0';
	return priceStr;
}

// Update the local inventory based on UpdateInventoryOutput JSON
function updateLocalInventory( updInventoryOutput, clearLocalData ) {
	if ( !updInventoryOutput || !( updInventoryOutput.mt || updInventoryOutput.er ) ) {
		showDialog( null, $messagetext.didnotreceivecorrectresponse,  getCurrentUrl() );
		return false;
	}
	var updatedMaterials = updInventoryOutput.mt;
	var materialMap = $materialData.materialmap;
	var foundBatch = false;
	if ( updatedMaterials ) {
		for (var i = 0; i < updatedMaterials.length; i++) {
			var m = updatedMaterials[i];
			var localMaterial = materialMap[m.mid];
			if (!localMaterial)
				continue;
			localMaterial.q = m.q;
			if (m.alq)
				localMaterial.alq = m.alq;
			if (m.avq)
				localMaterial.avq = m.avq;
			if (m.itq)
				localMaterial.itq = m.itq;
			localMaterial.t = ( updInventoryOutput.t ? updInventoryOutput.t : m.t );
			if ( m.min )
				localMaterial.min = m.min;
			if ( m.max )
				localMaterial.max = m.max;
			if ( m.dmin )
				localMaterial.dmin = m.dmin;
			if ( m.dmax )
				localMaterial.dmax = m.dmax;
			if ( m.dsq )
				localMaterial.dsq = m.dsq;
			if ( m.crD )
				localMaterial.crD = m.crD;
			if ( m.crM )
				localMaterial.crM = m.crM;
			if ( m.crW )
				localMaterial.crW = m.crW;

			//Update batch quantity
			if (m.bt) {
				var noOfBatches = m.bt.length;
				localMaterial.bt = [];
				for (var j = 0; j < noOfBatches; j++) {
					var item = m.bt[j];
					var b = {};
					for (var k in item) {
						if (!item.hasOwnProperty(k))
							continue;
						b[k] = item[k];
					}
					localMaterial.bt.push(b);
				}

			}
			else { //all batches reset
				if (localMaterial.bt)
					localMaterial.bt = [];
			}
			//Update expired batch quantity
			if (m.xbt) {
				var noOfBatches = m.xbt.length;
				localMaterial.xbt = [];
				for (var j = 0; j < noOfBatches; j++) {
					var item = m.xbt[j];
					var b = {};
					for (var k in item) {
						if (!item.hasOwnProperty(k))
							continue;
						b[k] = item[k];
					}
					localMaterial.xbt.push(b);
				}

			}
			else { //all expired batches reset
				if (localMaterial.xbt)
					localMaterial.xbt = [];
			}
		}
	}
	// Mark partial errors, if any
	var hasPartialErrors = markPartialErrorsLocally( updInventoryOutput );
	// Clear local data for the current operation, if needed
	if ( updatedMaterials && clearLocalData ) {
		for ( var i = 0; i < updatedMaterials.length; i++ )
			clearLocalModifications($uid, $currentOperation, updatedMaterials[i].mid, true, hasPartialErrors);
	}
	// Persist the changes
	storeEntity( $uid, $entity ); // NOTE: entity has a material list, who object pointers are in materialData.materialmap
	return true;
}


// Update the  inventory based on UpdateInventoryOutput JSON
function updateLocalInventoryForRelatedEntity( entityId, relationType, updInventoryOutput ) {
	if (!updInventoryOutput || !( updInventoryOutput.mt || updInventoryOutput.er )) {
		showDialog(null, $messagetext.didnotreceivecorrectresponse, getCurrentUrl());
		return false;
	}
	var entity = getRelatedEntity( entityId, relationType );
	if ( entity ) {
		entity.mt = updInventoryOutput.mt;
		storeEntity($uid, entity);
	}
	return true;
}


// Update the local order for the given entity
function updateLocalOrder( order, orderType, isNew ) {
	if ( !order )
		return false;
	// Get the current set of orders, if any
	var allOrders = $entity.orders;
	if ( !allOrders ) {
		allOrders = {};
		$entity.orders = allOrders;
	}
	if (!$transferOrder) {
		var orders = allOrders[orderType];
		if (!orders) {
			orders = [];
			allOrders[orderType] = orders;
		}
	}
	else {
		var orders = allOrders[$transferOrder][orderType];
		if (!orders) {
			orders = [];
			allOrders[$transferOrder][orderType] = orders;
		}
	}
	// Clean up the order, if necessary (version and status are present in a JSON order response, say, a new order or update order REST command)
	if ( order.v )
		delete order.v;
	if ( order.st )
		delete order.st;
	// Sort the material list in the order
	if ( order.mt )
		sortOrderMaterials( order );
	// Update credit info., if needed
	updateLocalCredit( order, orderType );
	// Add the order at the beginning of the array (so it is in descending order of time)
	if ( isNew ) {
		orders.unshift( order );
	} else { // edit
		// Create an array with the new order
		var editedOrders = [];
		for ( var i = 0; i < orders.length; i++ ) {
			if ( orders[i].tid == order.tid ) {
				editedOrders.push( order );
			} else {
				editedOrders.push( orders[i] );
			}
		}
		if (!$transferOrder) {
			allOrders[orderType] = editedOrders;
		}
		else {
			allOrders[$transferOrder][orderType] = editedOrders;
		}
		$entity.orders = allOrders;

	}
	var persist = true;
	// Clear local data for the current operation
	clearLocalModifications( $uid, $currentOperation, null, true, false );
	// Remove dirty flag on order, if any
	if ( order.dirty )
		delete order.dirty;
	// Persist
	storeEntity( $uid, $entity );
	return true;
}

// Update local order status, and optionally, update stock
function updateLocalOrderStatus( o, orderType ) {
	var order = getOrder( o.tid, orderType );
	if ( !order )
		return;
	order.ost = o.ost;
	if ( o.osht )
		order.osht = o.osht;
	if ( o.cmnts )
		order.cmnts = o.cmnts;
	if ( o.shps ) {
		order.shps = o.shps;
		if (o.shps.cmnts)
			order.shps.cmnts = o.shps.cmnts;
	}
	if ( o.mt )
		order.mt = o.mt;
	if (o.rsnco)
		order.rsnco = o.rsnco;
	if	(o.ead)
		order.ead = o.ead;


	if ( order.ost == 'fl' && $config.usof && $config.usof == 'true' && orderType =='prc') // update stock on fulfilled
		updateStockFromOrder( order );
	// Update local credit, if needed
	updateLocalCredit( o, orderType );
	// Persist
	storeEntity( $uid, $entity );
}




// Update local stock from a given order (persists changes)
function updateStockFromOrder( order ) {
	if ( !order.mt || order.mt.length == 0 )
		return;
	var orderitems=[];
	var lkid = getRelatedEntity(order.vid,'vnds');
	for ( var i = 0; i < order.mt.length; i++ ) {
		var item = order.mt[i];
		var material = $materialData.materialmap[ item.mid ];
		if ( material ) {
			material.q = ( parseFloat(material.q) + parseFloat(item.q) ) + '';
		}
		orderitems[item.mid] ={};
		orderitems[item.mid].q = item.q;
	}
	var  linkedEntityName ='';
	if (lkid)
	   linkedEntityName = lkid.n;
	// Persist the changes
	storeEntity( $uid, $entity );
	// Log receipt transaction in trans. history
	try {
		logInventoryTransaction($uid, $entity.kid, 'vo', linkedEntityName, orderitems, order, order.ut);
	} catch ( e ) {
		logR( 'e', 'updateStockFromOrder', e.message, e.stack );
	}
}

// Update local credit, if needed (updates only in cache, and return dirty flag if updated)
function updateLocalCredit( order, orderType ) {
	var relType = 'vnds';
	var lkid = order.vid;
	if ( orderType && orderType == 'sle' ) { // sales order
		relType = 'csts';
		lkid = order.kid;
	}
	var dirty = false;
	if ( order.vid && ( order.crl || order.pyb ) ) { // has credit-limit or payable
		var relatedEntity = getRelatedEntity( lkid, relType );
		if ( !relatedEntity )
			return;
		if ( order.crl ) { // update credit limit, if present
			var creditLimit = parseFloat( order.crl );
			if ( creditLimit != 0 ) {
				relatedEntity.crl = order.crl;
				dirty = true;
			};
		}
		if ( order.pyb ) { // update payable, if present
			relatedEntity.pyb = order.pyb;
			dirty = true;
		};
	}
	return dirty;
}




// Save edits to an order
function saveOrderEdits( oid, orderType,nextPage ) {
	// Get the order
	var order = getOrder( oid, orderType );
	var items = order.mt;
	// Get the set of edits
	var page = $( '#orderinfo' );
	var warnMsg = '';
	var errMsg = ''; // if there was any errors in the quantities entered
	var batchAllocMsg = '';
	// Check for payment first
	var paymentElement = page.find( '#payment' );
	if (paymentElement){
		var paymentValue = paymentElement.val();
		if ( paymentValue && paymentValue != '' ) {
			if ( !order.dirty )
				order.dirty = {};
			order.dirty.pymt = paymentValue;
		}
	}
	if ( !order.dirty || !order.dirty.pymt ) { // do the item quantities validation, only if payment was not specified
		var hasErrors = false;
		var zeroQuantity = true;
		for (var i = 0; i < items.length; i++) {
			var q = page.find('#q_' + items[i].mid).val();
			if ( q == 0)
				zeroQuantity = zeroQuantity && true;
			else
				zeroQuantity =  zeroQuantity && false;
			// Add new quantity (nq) to order list
			if (q != undefined && q != null && q != '' && q != items[i].q) {
				var materialName = getMaterialName(items[i].mid);
				var material = $materialData.materialmap[items[i].mid];
				if (q < 0)
					errMsg += ' - ' + materialName + ' ' + $labeltext.quantity_lower + ' (' + q + ') ' + $messagetext.mustbepositiveinteger + '.<br/>';
				else if (!isInteger(q))
					errMsg += ' - ' + materialName + ' ' + $labeltext.quantity_lower + ' (' + q + ') ' + $messagetext.mustbepositiveinteger + '.<br/>';
				else if (material.ehuc && material.hu) { //handling unit enforced HMA-455
					if (material.hu.length > 0) {
						if (material.hu[0].q) {
							if (q % material.hu[0].q != 0) {
								var msg = $messagetext.numunitsdoesnotmatch.replace('quantity', q);
								msg = msg.replace('hq', parseFloat(material.hu[0].q));
								msg = msg.replace('mn', material.n);
								msg = msg.replace('mn', material.n);
								msg = msg.replace('hnm', material.hu[0].hnm);
								page.find('#qtyerrmsg_' + items[i].mid).html('<font style="color:red;font-size:8pt;">' + msg + '</font>');
								page.find('#qtyerrmsg_' + items[i].mid).attr('style', 'display:block');
								hasErrors = true;
								///return;
							}
							else {
								page.find('#qtyerrmsg_' + items[i].mid).attr('style', 'display:none');
							}
						}
					}
				}
				else {
					var alq = 0;
					if (items[i].nalq != null)
						alq = items[i].nalq;
					else if (items[i].alq != null)
						alq = items[i].alq;
					if (parseFloat(q) < parseFloat(alq)) {
						page.find('#qtyerrmsg_' + items[i].mid).html('<font style="color:red;font-size:8pt;">' + $messagetext.orderedcannotbelessthanallocated + '</font>');
						page.find('#qtyerrmsg_' + items[i].mid).attr('style', 'display:block');
						hasErrors = true;
					}
					var sq = getShippedQty(oid, orderType, items[i].mid);
					if (sq != null && (parseFloat(q) < parseFloat(sq))) {
						page.find('#qtyerrmsg_' + items[i].mid).html('<font style="color:red;font-size:8pt;">' + $messagetext.orderedcannotbelessthanshipped + '</font>');
						page.find('#qtyerrmsg_' + items[i].mid).attr('style', 'display:block');
						hasErrors = true;
					}
				}
				if (hasErrors)
					continue;

				// Mark new item quantity
				items[i].nq = q;
				// Mark the dirty quantity flag
				if (!order.dirty)
					order.dirty = {};
				order.dirty.q = true; // implies quantities edited

				var eoq = -1;
				if (items[i].roq != null)
					eoq = items[i].roq;
				else
					eoq = getEoq(items[i].mid, order.kid, orderType);
				if ((errMsg == '') && (eoq >= 0) && (q != eoq) && order.dirty.q && (orderType == 'prc' && order.ost == 'pn')) {
					if (isDevice()) // html code for <br> is not recognized by cordova dialog
						warnMsg += materialName + ' ' + $labeltext.quantity_lower + ' (' + q + '), ' + $labeltext.recommendedquantity_lower + ' (' + eoq + ')\n\n';
					else
						warnMsg += ' - ' + materialName + ' ' + $labeltext.quantity_lower + ' (' + q + '), ' + $labeltext.recommendedquantity_lower + ' (' + eoq + ')<br/>';
				}
				//save ignore recommended order reasons
				if (orderType == 'prc') {
					var rsnirq;
					var othersSelected;
					if (page.find('#irreason_' + items[i].mid).length) {
						if (page.find('#irreason_' + items[i].mid).val() == $labeltext.others) {
							rsnirq = page.find('#irreasonmsg_' + items[i].mid).val();
							othersSelected = true;
						}
						else
							rsnirq = page.find('#irreason_' + items[i].mid).val();
					}
					else if (page.find('#irreasonmsg_' + items[i].mid).length) {
						rsnirq = page.find('#irreasonmsg_' + items[i].mid).val();
					}
					if ((eoq >= 0) && (q != eoq)) {
						if (rsnirq != '' && rsnirq != null && rsnirq != undefined)
							items[i].rsnirq = rsnirq;
						else {
							var isReasonsVisible = ((page.find('#irreason_' + items[i].mid)).is(":visible") ||(page.find('#irreasonmsg_' + items[i].mid)).is(":visible") );
							if ((othersSelected || isOrderReasonsMandatory($config, 'ir')) && isReasonsVisible ) {
								page.find('#irreasonerr_' + items[i].mid).attr('style', 'display:block');
								return;
							}
							else
								items[i].rsnirq = '';
						}
					}
				}
				//save order edit reasons
				var rsneoq;
				var othersSelected;
				if (page.find('#eoreason_' + items[i].mid).length) {
					if (page.find('#eoreason_' + items[i].mid).val() == $labeltext.others) {
						rsneoq = page.find('#eoreasonmsg_' + items[i].mid).val();
						othersSelected = true;
					}
					else
						rsneoq = page.find('#eoreason_' + items[i].mid).val();
				}
				else if (page.find('#eoreasonmsg_' + items[i].mid).length) {
					rsneoq = page.find('#eoreasonmsg_' + items[i].mid).val();
				}
				if (rsneoq != '' && rsneoq != null && rsneoq != undefined)
					items[i].rsneoq = rsneoq;
				else {
					var isReasonsVisible = ((page.find('#eoreason_' + items[i].mid)).is(":visible") || (page.find('#eoreasonmsg_' + items[i].mid)).is(":visible"));
					if ((othersSelected || isOrderReasonsMandatory($config, 'eo')) && isReasonsVisible) {
						page.find('#eoreasonerr_' + items[i].mid).attr('style', 'display:block');
						return;
					}
					else
						items[i].rsneoq = '';
				}

			}
		}
		if ( hasErrors )
			return;
	}
	if ( zeroQuantity ===  true){
		showDialog( $messagetext.warning, $messagetext.allitemscannotbezero , getCurrentUrl() +'&edit=true' );
		return;
	}
	// Check errors and give a message
	if ( errMsg != '' ) {
		errMsg = $messagetext.errorsinquantities + '<br/><br/>' + errMsg;
		showDialog( $messagetext.warning, errMsg, getCurrentUrl() +'&edit=true' );
		return;
	}

	// Save any order metadata, if present
	var paymentOptionElement = page.find( '#paymentoption' );
	if ( paymentOptionElement ) {
		var val = paymentOptionElement.val();
		var dirty = ( ( !order.popt && val && val != '' ) || ( order.popt && val && order.popt != val ) );
		if ( dirty ) {
			if ( !order.dirty )
				order.dirty = {};
			order.dirty.popt = val;
		}
	}
	var packageSizeElement = page.find( '#packagesize' );
	if ( packageSizeElement ) {
		var val = packageSizeElement.val();
		var dirty = ( ( !order.pksz && val && val != '' ) || ( order.pksz && val && order.pksz != val ) );
		if ( dirty ) {
			if ( !order.dirty )
				order.dirty = {};
			order.dirty.pksz = val;
		}
	}
	var orderTagElement = page.find( '#ordertags' );
	if ( orderTagElement ) {
		var val = '';
		var orderTags = '';
		 if ( orderTagElement.val() ) {
			 val = orderTagElement.val();
			 orderTags = val.join(",");
		 }
		var dirty = ( ( !order.tg && orderTags && orderTags != '' ) || ( order.tg && order.tg != orderTags ) );
		if ( dirty ) {
			if ( !order.dirty )
				order.dirty = {};
			order.dirty.tg = orderTags;
		}
	}

	var referenceId = page.find( '#referenceid' );
	if ( referenceId ) {
		var val = referenceId.val();
		var dirty = ( (( order.rid == ''|| !order.rid) && val != '' ) || ( order.rid &&  val != null && order.rid != val ) );
		if ( dirty ) {
			if ( !order.dirty )
				order.dirty = {};
			order.dirty.rid = val;
		}
	}

	var reqByDate = page.find( '#rbd' );
	if ( reqByDate ) {
		var val = reqByDate.val();
		var enteredDate = getDatePickerFormat(val, $userDateFormat);
		var originalDate;
		if ( order.rbd) {
			originalDate = getDatePickerFormat(getDateOnly(order.rbd),$userDateFormat);
		}
		var dirty =  (( !order.rbd && val != null ) || (order.rbd && val != null && (originalDate != enteredDate  )));
		if (dirty) {
			var today = new Date();
			today.setHours(0, 0, 0, 0)
			if ( val != '' && getDateWithLocaleFormat(val, $userDateFormat) < today) {
				$('#rbd_error').attr('style', 'display:block');
				return;
			}
			if (!order.dirty)
				order.dirty = {};
			order.dirty.rbd = val;
		}


	}

	var cancelUrl = getCurrentUrl()+'&edit=true';
	if ( warnMsg != '' ) {
		if ( isDevice() )
			warnMsg = $messagetext.orderquantitydifferentfromrecommendedquantity + '\n\n' + warnMsg; // cordova dialog does not take html code
		else
			warnMsg = $messagetext.orderquantitydifferentfromrecommendedquantity + '<br/><br/><font style="font-size:small">' + warnMsg + '</font>';

		showPopupDialog($buttontext.save + '?', warnMsg,function(){saveOrderPage(nextPage);},null,null,"popupeditorderreason", null);

		//showConfirm($messagetext.warning, warnMsg, null, function () {
		//	saveOrderPage(nextPage);
		//}, cancelUrl);
	}
	else {
	   page.find( '#fieldchange' ).val('');
		saveOrderPage(nextPage);
	}
}

function saveOrderPage(nextPage)
{
 	// Persist changes
	storeEntity( $uid, $entity );
	$.mobile.changePage(nextPage);
}

function getIgnoreRecommendedOrderReasonsMarkup(reason,id) {
	var reasons = getIgnoreRecommendedOrderReasons( $currentOperation, $config );
	// Reason field
	var markup = '';
	if (reasons != null ) {
		var divId = "reasonsdiv";
		var lblId = "reasonlabel";
		var selectId = "reason";
		var reasonMsgId = "reasonmsg";
		if (id){
			divId += '_'+id;
			lblId += '_'+ id;
			selectId += '_'+ id;
			reasonMsgId += '_'+ id;
		}
		markup += '<div id= "'+ divId +'" >';
		markup += '<label id="'+ lblId +'" for= "'+ selectId +'">' + $labeltext.reasons + ':</label>';
		markup += '<select name="reason" id="'+ selectId +'" data-theme="c" data-mini="true">';
		markup += '<option value="">'+$messagetext.selectreason+'</option>';
		for (var i = 0; i < reasons.length; i++) {
			var selected = '';
			if (reason) {
				if (reason == reasons[i])
					selected = 'selected';
			}
			markup += '<option value="' + reasons[i] + '" ' + selected + '>' + reasons[i] + '</option>';
		}
		markup += '</select>'
		markup += '<div id="'+ reasonMsgId +'" style="display:none"><font style="color:red;font-size:8pt;font-weight:normal">' + $messagetext.pleaseselectareason+ '</font></div>';
		markup += '<font style="font-size:small;font-weight:normal">'+$messagetext.specifyreasonrecommendedquantity+'</font>';
		markup += '</div>';
	}
	return markup;
}


/*function checkReasonsMandatory(	okButtonClickCallBack){
	var key = 'igorrsns';
	var reasonsMandatory;
	var reasonsKey = $config[key];
	if (reasonsKey) {
		reasonsMandatory = $config[key]['mnd'];
		if (reasonsMandatory === "true") {
			var reason = $('#ignoreReason').val();
			if (reason)
				$( $.mobile.activePage[0] ).find('#reason').val(reason);
			if (getIgnoreRecommendedOrderReasons($currentOperation, $config) && (!reason )) {
				$('#reasonmsg').attr('style', 'display:block');
				return;
			}
		}
	}
	if (okButtonClickCallBack)
	  okButtonClickCallBack();
	return;

}*/


function isOrderReasonsMandatory( config, reasonType ){
	var mnd = 0;
	if ( reasonType == 'ir') {
		if ( config.igorrsns )
			mnd = config['igorrsns']['mnd'] ;
	}
	else if ( reasonType == 'fl') {
		if ( config.rsnspf )
			mnd = config['rsnspf']['mnd'] ;
	}
	else if ( reasonType == 'cm') {
		if ( config.rsnsps )
			mnd = mnd = config['rsnsps']['mnd'] ;
	}
	else if ( reasonType == 'eo') {
		if ( config.rsnseoq )
			 mnd = config['rsnseoq']['mnd'] ;
	}
	else if ( reasonType == 'cn') {
		if ( config.rsnsco )
			mnd = config['rsnsco']['mnd'] ;
	}
	return mnd;
}

/*function checkRecommendedOrderQtyOnChange(mid,reason,prevValue) {
	var orderinfopage = $( '#orderinfo' );
	var q = orderinfopage.find( '#q_'+mid ).val();
	if (q < 0 || !isInteger(q))
	     return;
	var eoq = orderinfopage.find( '#eoq_'+mid );//  recommended order quantity for the material
	if (eoq) {
		eoq = eoq.val();
		if (eoq) {
			if (q != eoq) { //quantity different from recommended order quanity
				var materialName = '';
				if (mid) {
					var material = $materialData.materialmap[mid];
					materialName = material.n;
				}
				var okButtonCallBackFunction = function(){checkReasonsMandatory(function(){updateRecommendedQtyReason( mid )});};
				var cancelButtonCallBackFunction = function(){clearOrderQtyEdit(mid,prevValue );};
				var extraHtml = getIgnoreRecommendedOrderReasonsMarkup(reason);
				recommendedQtyReasonsDialog('#orderinfo',
				 material.n + ' - ' + $messagetext.changedorderquantityfromrecommendedquantity + ' (' + eoq + '). ' + '<p>' + $messagetext.doyouwanttocontinue + '</p>',
					reason, okButtonCallBackFunction, cancelButtonCallBackFunction);
				}
		}
	}
	return true;
}
*/

function updateRecommendedQtyReason(mid) {
	var orderinfopage = $( '#orderinfo' );
	var reason = orderinfopage.find( '#reason'); //Reason selected in the pop-up dialog
	if (reason)
		reason = orderinfopage.find( '#reason').val();
	var newreason = orderinfopage.find( '#reason_'+mid )
	if (newreason)
		orderinfopage.find( '#reason_'+mid).val(reason);
	if (reason && orderinfopage.find('#reasonDiv_'+mid)) {
		$('#reasonDiv_'+mid).html('<font style="color:black;font-size:8pt;">(' + reason + ')</font>');
		$('#reasonDiv_'+mid).attr( 'style', 'display:block' );
	}
}

 function clearOrderQtyEdit(mid,prevValue){
	 var orderinfopage = $( '#orderinfo' );
	 var qty = orderinfopage.find( '#q_'+mid);
	 if (qty)
		 qty = orderinfopage.find( '#q_'+mid).val(prevValue);
 }


// Save new items added to order
function saveOrderAdds( oid, orderType ) {
	var order = getOrder( oid, orderType );
	if ( !order )
		return;
	// Get material modifications
	var localModifications = getLocalModifications( null, $currentOperation, $entity );
	if ( !localModifications ) {
		showDialog( $messagetext.nochanges, $messagetext.noitemshavebeenedited+' ' + oid,  getCurrentUrl() );
		return;
	}
	// Update the order with the local modifications
	var materials = order.mt;
	if ( !materials ) {
		materials = [];
		order.mt = materials;
	}
	// Add to order's material list
	for ( var mid in localModifications  ) {
		if ( !localModifications.hasOwnProperty( mid ) )
			continue;
		var m = {};
		m.mid = mid;
		m.nq = localModifications[ mid ].q; // NOTE: add to 'nq', i.e. as a new quantity
		if (localModifications[mid].reason)
			m.rsn = localModifications[mid].reason;
		materials.push( m );
	}
	// Update dirty flag
	if ( !order.dirty )
		order.dirty = {};
	order.dirty.q = true;
	// Sort the material list
	sortOrderMaterials( order );
	// Clear local modifications
	clearLocalModifications( $uid, $currentOperation, null, false, false );
	// Persist
	storeEntity( $uid, $entity );

}

// Sort materials in an order
function sortOrderMaterials( order ) {
	if ( !order.mt || order.mt.length == 0 )
		return;
	order.mt.sort( function( o1, o2 ) {
		// Get the corresponding material objects
		var m1 = $materialData.materialmap[ o1.mid ];
		var m2 = $materialData.materialmap[ o2.mid ];
		if ( !m1 || !m2 )
			return 0;
		if ( m1.n > m2.n )
			return 1;
		else if ( m1.n < m2.n )
			return -1;
		else
			return 0;
	});
}

// Check if an order is edited
function isOrderEdited( order ) {
	return ( order.dirty );
}

// Update a list of orders of a given type (NOTE: this will overwrite existing orders)
function updateOrders( orders, orderType ) {
	if ( !orders )
		return;
	// Get the current set of orders, if any
	var allOrders = $entity.orders;
	if ( !allOrders ) {
		allOrders = {};
		$entity.orders = allOrders;
	}
	if (!$transferOrder) {
		allOrders[orderType] = orders;
	}
	else{
		if (!allOrders[$transferOrder]) {
			allOrders[$transferOrder] = {};
		}
		allOrders[$transferOrder][orderType] = orders;

	}

	// Reset the orders' search index for the given type
	resetOrdersSearchIndex( orderType );
	// Sort the orders' materials
	for ( var i = 0; i < orders.length; i++ ) {
		var order = orders[i];
		// Sort items within an order
		sortOrderMaterials( order );
		// Update the order's search index
		updateOrderSearchIndex( order, orderType );
		// Update local credit with vendor, if order has credit info.
		updateLocalCredit( order, orderType );
	}
	// Persist
	storeEntity( $uid, $entity );
}

// Get orders of a given type
function getOrders( orderType ) {
	if (!$entity.orders)
		return null;
	if (!$transferOrder) {
		if (!$entity.orders[orderType])
			return null;
		return $entity.orders[orderType];
	}
	else {
		if (!$entity.orders[$transferOrder])
			return null;
		if (!$entity.orders[$transferOrder][orderType])
			return null;
		return $entity.orders[$transferOrder][orderType];
	}
}

// Get an order of a given id
function getOrder( oid, orderType ) {
	var orders = getOrders( orderType );
	if ( orders == null || orders.length == 0 )
		return null;
	for ( var i = 0; i < orders.length; i++ )
		if ( orders[i].tid == oid )
			return orders[i];
	return null;
}


//get order item given id , type, mid
function getOrderItem ( oid, orderType, mid) {
	var order = getOrder( oid, orderType );
	if (!order)
		return null;
	var items = order.mt;
	if (!items)
		return null;
	for (var i = 0; i < items.length; i++) {
		if ( items[i].mid == mid )
			return items[i];
	}
}

//get shipment item given id , type, sid
function getOrderShipment ( oid, orderType, sid) {
	var order = getOrder( oid, orderType );
	if (!order)
		return null;
	var shps = order.shps;
	if (!shps)
		return null;
	for (var i = 0; i < shps.length; i++) {
		if ( shps[i].sid == sid )
			return shps[i];
	}
}

//get shipment item given id , type, sid
function getOrderShipmentItem ( oid, orderType, sid, mid, shipped) {
	var order = getOrder( oid, orderType );
	if (!order)
		return null;
	var shps = order.shps;
	if (!shps)
		return null;
	for (var i = 0; i < shps.length; i++) {
		if ( shps[i].sid == sid ){
			var items = shps[i].mt;
			if (!items || ((shps[i].st == 'op' || shps[i].st == 'cn') && shipped) )
				continue;
			for (var j = 0; j < items.length; j++) {
				if ( items[j].mid == mid ) {
					return items[j];
					break;
				}
			}
		}

	}
	return null;
}



function getShippedQty (orderId, orderType, mid){
	var order = getOrder( orderId, orderType );
	if (!order)
		return null;
	var shps = order.shps;
	if (!shps)
		return null;
	var q = 0 ;
	for (var i = 0; i < shps.length; i++) {
			var items = shps[i].mt;
			if (!items || (shps[i].st == 'op' ||  shps[i].st == 'cn') )
				continue;
			for (var j = 0; j < items.length; j++) {
				if ( items[j].mid == mid ) {
					if (items[j].q)
						q += items[j].q;
					break;
				}
			}
	}
	return q;
}

function getShippedQtyByBatch (orderId, orderType, mid){
	var order = getOrder( orderId, orderType );
	if (!order)
		return null;
	var shps = order.shps;
	if (!shps)
		return null;
	var q = 0 ;
	var bsq = [];
	for (var i = 0; i < shps.length; i++) {
		var items = shps[i].mt;
		if (!items || shps[i].st == 'op' || shps[i].st == 'cn' )
			continue;
		for (var j = 0; j < items.length; j++) {
			if ( items[j].mid == mid ) {
				if (items[j].q)
					q += items[j].q;
				if ( items[j].bt ) {
					for (var k = 0; k < items[j].bt.length; k++) {
						var bq = getShippedBatch(items[j].bt[k].bid, bsq);
						if (bq == null)
							bsq.push(items[j].bt[k]);
						else {
							bsq[bq].q = parseFloat(bsq[bq].q) + parseFloat(items[j].bt[k].q);
							bsq[bq].flq = parseFloat(bsq[bq].flq) + parseFloat(items[j].bt[k].flq);
						}
					}
				}
				break;
			}
		}
	}
	return bsq;
}

function getShippedBatch(bid, batches) {
	if (!bid || !batches)
		return null;
	var i, len = batches.length;

	for (i = 0; i < len; i++) {
		if ( batches[i].bid == bid) {
			return i;
		}
	}
	return null;
}

function isAnyShipmentFulfilled ( shps ){
	if (!shps)
		return false;
	for (var i = 0; i < shps.length; i++) {
	 	if (shps[i].st == 'fl')
			return true;
	}
	return false;
}


function isAnyShipmentShipped ( shps ){
	if (!shps)
		return false;
	for (var i = 0; i < shps.length; i++) {
		if (shps[i].st == 'sp')
			return true;
	}
	return false;
}

function getShipmentsForMaterial (orderId, orderType, mid){
	var order = getOrder( orderId, orderType );
	if (!order)
		return null;
	var shps = order.shps;
	if (!shps)
		return null;
	var shipments= [];
	for (var i = 0; i < shps.length; i++) {
		if ( shps[i].st == 'op' || shps[i].st == 'cn')
			continue;
		shipments.push(shps[i]);
	}
	return shipments;
}


// Get the related entity object for the current entity
function getRelatedEntity( kid, relationType ) {
	var entities = $entity[ relationType ];
	if ( !entities || entities.length == 0 )
           return null;
	  for ( var i = 0; i < entities.length; i++ ) {
		if ( entities[i].kid == kid ) {
			var entity = getEntity($uid, kid);

			if (entity) {
				if (entities[i].crl)
					entity.crl = entities[i].crl;
				else
					delete entity.crl;
				if (entities[i].rtt)
					entity.rtt = entities[i].rtt;
				else
					delete entity.rtt;

				if (entities[i].rti)
					entity.rti = entities[i].rti;
				else
					delete entity.rti;
			}
			else {//related entity not yet stored locally
				storeEntity( $uid,entities[i] );
				entity = entities[i];
			}
			return entity;
			//return entities[i];
		}
	  }
          return null;
}


function getRelatedEntityFields (entity){
	var linkedEntity = {};
	if (!entity)
		return null;
	linkedEntity.kid = entity.kid;
	linkedEntity.n = entity.n;
	if ( entity.crl )
		linkedEntity.crl = entity.crl;
	if ( entity.rtt )
		linkedEntity.rtt = entity.rtt;
	if ( entity.rti)
		linkedEntity.rti = entity.rti;
	return linkedEntity;
}


function removeRelatedEntityFields(entity)
{
	if (!entity)
		return null;
	var linkedEntity = $.extend( true, {}, entity );
	if (linkedEntity.crl)
		delete linkedEntity.crl;
	if (linkedEntity.rtt)
		delete linkedEntity.rtt;
	if (linkedEntity.rti)
		delete linkedEntity.rti;
	return linkedEntity;
}

function getDefaultVendor(kid){    
	//check if vendor is default vendor
	var defaultVendor = null;
	if ($config.vid)  {
		if (kid == $config.vid) {
	     defaultVendor = {};
	     defaultVendor.n = $config.vndr;
	     defaultVendor.cty = $config.vcty;
	    }
	}
	return defaultVendor;
 }
//Get the related entity object for the current entity
function getRelatedEntityForKioskId( lkid, relationType,kid ) {
	var entity = getEntity($uid,kid);
	var entities = entity[ relationType ];
	if ( !entities || entities.length == 0 )
		return null;
	for ( var i = 0; i < entities.length; i++ ) {
		if ( entities[i].kid == lkid ) {
			var entity = getEntity($uid, lkid);
			if (entities[i].crl)
			  	entity.crl = entities[i].crl;
			else
				delete entity.crl;
			if (entities[i].rtt)
				entity.rtt = entities[i].rtt;
			else
				delete entity.rtt;
			if (entities[i].rti)
				entity.rti = entities[i].rti;
			else
				delete entity.rti;
			return entity;
			//return entities[i];
		}

	}
	return null;
}


function getRelationshipTypeForOperation( currentOperation ) {
	//ts one is dummy not really used
	if ( currentOperation == 'ei' && $entity.csts && $entity.csts.length > 0 )
		return 'csts';
	else if (  currentOperation == 'er' && $entity.vnds && $entity.vnds.length > 0 )
		return 'vnds';
	else if ( currentOperation == 'ts' )
		return $currentRelationType;
	else if (currentOperation == 'no' && $newOrderType == 'sle')
		return 'csts';
	else if (currentOperation == 'no' && $newOrderType == 'prc')
		return 'vnds';
	else 
		return null;
}

function getRelationshipName( relationType, plural ) {
	var str = '';
	if ( relationType == 'csts' ) {
	    if (plural)
		str = $labeltext.customers; //'Customers'
	    else
		str = $labeltext.customer; //'Customer'
	}
	else if ( relationType == 'vnds' ){
	      if (plural)
		     str = $labeltext.vendors; //'Vendors'
	        else
	            str = $labeltext.vendor; //'Vendor'
	}
	return str;
}



function getDataEntryText( prefixText ) {
	if ( isOperationReadOnly( $currentOperation ) )
		return null;
	var str='';
	if (prefixtext = null)
	{
	    if ($currentoperation ='ei')
	      str = $labeltext.enterissues;
	    else if ($currentoperation = 'er')
		str = $labeltext.enterreceipts;
	    else if ($currentoperation = 'es')
		str = $labeltext.enterphysicalstock;
	    else if ($currentoperation = 'ew')
		str = $labeltext.enterdiscards;
	    else if ($currentoperation = 'no')
		str = $labeltext.neworders;
	    else if ($currentoperation = 'ts')
		str = $labeltext.transferstock;
	}
	if ( prefixText != null )
		str = prefixText;
	str += getOperationName( $currentOperation );
	var linkedEntity = getSelectedLinkedEntity( $currentOperation, $entity );
	var relatedEntityInfo;
	if ( linkedEntity != null ) {
		if ( $currentOperation == 'no' )
			str = $messagetext.orderitems;// 'Order items';
		relatedEntityInfo = getRelatedEntity( linkedEntity.kid, linkedEntity.type );
		var toFrom = '';
		if ( linkedEntity.type == 'csts' )
			toFrom = ' - '; //' to ';
		else 
			toFrom = ' - '; //' from ';
		if ( relatedEntityInfo != null )
			str += toFrom + '\'' + relatedEntityInfo.n + '\'';
	}
	/*if ($currentOperation == 'no' && $newOrderType == 'sle'){
		relatedEntityInfo = getRelatedEntity( linkedEntity.kid, 'csts' );
		if ( relatedEntityInfo != null )
		  str += ' - ' + '\'' + relatedEntityInfo.n + '\'';	
	}*/
	if ( $currentOperation == 'ts' ) {
	       str = $labeltext.transferstock+' ( \'' + $entity.n + '\'';
		//str = 'Transfer from \'' + $entity.n + '\'';
		if ( relatedEntityInfo )
		    str += ' &#x2192; \'' + relatedEntityInfo.n + '\' )';
	}
	return str;
}


///////////////// REST API & its utilities /////////////////////
// Get error messge, if any
function getDataError( o ) {
	if ( o && o.st && o.st == '1' ) {
		if ( o.ms )
			return o.ms;
	}
	return null;
}

// Get UpdateInventoryInput
function getUpdateInventoryInput( materialMap, uid, entity, linkedEntityId, orderType, message, orderStatus, tid, paymentOption, packageSize, orderTags, referenceId, requiredByDate ) {
	// Loop over the materials, and form a JSON UpdateInventoryInput object
	var updInventoryInput = {};
	updInventoryInput.v = '03';
	updInventoryInput.uid = uid;
	updInventoryInput.kid = entity.kid;
	// Add transaction save timestamp (note: a new timestamp is sent each time, however, the repeat request (on timeout) or sync-request (on no network) will need to take care of sending the same timestamp, if this a repeat send/save)
	updInventoryInput.svtm = getRequestSavedTime( $currentOperation, uid, entity.kid, ( tid ? tid : linkedEntityId ) ); // NOTE: send as string; tid is present when order items of an order are being edited, or linkted entity ID for transactions or new orders
	if ( !updInventoryInput.svtm ) {
		updInventoryInput.svtm = new Date().getTime() + ''; // this needs to be a string
		// Update inventory transaction timestamp
		updateRequestSavedTime( $currentOperation, uid, entity.kid, ( tid ? tid : linkedEntityId ), updInventoryInput.svtm ); // NOTE: This needs to cleared in a 'clear all' or once the send was successful
	}
	var oType = 'prc';
	// Add linked entity Id, if present
	if ( linkedEntityId != null ) {
		updInventoryInput.lkid = linkedEntityId;
		/*var linkedEntityType = getSelectedLinkedEntityType(linkedEntityId,entity.kid)
		if (linkedEntityType  == 'csts')
		    orderType = 'sle';*/
	}
	if (orderType != null && isOperationOrder($currentOperation)) {
		oType = orderType;
		updInventoryInput.oty = oType;
		if ($transferOrder) {
			updInventoryInput.trf = 0;
		}

	}

	//sales order so switch the ids.
	if (isOperationOrder( $currentOperation ) && oType == 'sle'){
		updInventoryInput.kid = linkedEntityId;
		updInventoryInput.lkid = entity.kid;
	} 
	// Tracking or Order Id
	if ( tid != null )
		updInventoryInput.tid = tid;
	if ( paymentOption != null )
		updInventoryInput.popt = paymentOption;
	if ( packageSize != null )
		updInventoryInput.pksz = packageSize;
	// Add geo-coordinates, if present
	var geo = getGeo();
	if ( geo != null ) {
		if ( geo.err ) {
			updInventoryInput.gerr = geo.err;
		} else {
			updInventoryInput.lat = geo.lat;
			updInventoryInput.lng = geo.lng;
			updInventoryInput.gacc = geo.accuracy;
			if ( geo.altitude )
				updInventoryInput.galt = geo.altitude;
		}
	}
	if ( message != null && message != '' )
		updInventoryInput.ms = message;
	if ( orderStatus != null && orderStatus != '' )
		updInventoryInput.ost = orderStatus;
	if ( orderTags != null )
		updInventoryInput.tg = orderTags;
	if ( referenceId != null )
		updInventoryInput.rid = referenceId;
	if ( requiredByDate )
		updInventoryInput.rbd = getDateStrInServerFormat(requiredByDate, $userDateFormat);
		//updInventoryInput.rbd = getFourDigitDateFormat(requiredByDate, $userDateFormat);
	if ( materialMap && materialMap != null && !$.isEmptyObject( materialMap ) ) { // check to ensure materials not empty (possible with empty orders)
		updInventoryInput.mt = [];
		for ( var mid in materialMap ) {
			if ( !materialMap.hasOwnProperty( mid ) )
				continue;
			var material = materialMap[ mid ];
			if ( !material.bt || isOperationOrder($currentOperation) ) {
				var mData = {};
				mData.mid = mid;
				mData.smid = material.smid; // short material ID to be used in SMS, if needed
				mData.q = getFormattedNumber( material.q ).toString();
				if ( material.reason )
					mData.rsn = material.reason;
				if (material.ostk)
				    mData.ostk = getFormattedNumber( material.ostk );
				if (material.mst)
				   	mData.mst = material.mst;
				if (material.atd)
				   mData.atd = getDateStrInServerFormat(material.atd, $userDateFormat);
				if (material.ms)
				  mData.ms = material.ms;
				//orders
				if (material.rsneoq)
					mData.rsneoq = material.rsneoq;
				if (material.rsnirq)
					mData.rsn = material.rsnirq;
				if (material.alq != null)
				  	mData.alq = material.alq;
				if ( isOperationOrder($currentOperation) && material.bt )
				{
					if (material.bt.length > 0)
						mData.bt = [];
					for ( var i=0; i < material.bt.length; i++ ) {
						var bt = {}
					  	bt.bid = material.bt[i].bid;
						bt.alq = material.bt[i].alq;
						if ( material.bt[i].mst )
						bt.mst = material.bt[i].mst;
						mData.bt.push(bt);
					}

				}
				updInventoryInput.mt.push( mData );
			} else if ( material.bt.length > 0 ) {
					for ( var i=0; i < material.bt.length; i++ ) {
						var mData = {};
						mData.mid = mid;
						mData.smid = material.smid; // short material ID to be used in SMS, if needed
						if (material.bt[i].q != null)
							mData.q = getFormattedNumber( material.bt[i].q ).toString();
						if (material.bt[i].bid)
							mData.bid = material.bt[i].bid;
						if (material.bt[i].bmfnm)
							mData.bmfnm = material.bt[i].bmfnm;
						if (material.bt[i].bexp)
							mData.bexp = material.bt[i].bexp;
						if (material.bt[i].bmfdt)
							mData.bmfdt = material.bt[i].bmfdt;
						if ( material.reason )
							mData.rsn = material.reason;
						if (material.ostk)
							mData.ostk = getFormattedNumber( material.bt[i].ostk );
						if (material.bt[i].mst)
							mData.mst = material.bt[i].mst;
						if (material.atd)
							mData.atd = getDateStrInServerFormat(material.atd, $userDateFormat);
						updInventoryInput.mt.push( mData );
					}
			}
		}
	}
	return updInventoryInput;
}

// Load orders from server and persist locally
function loadOrders( orderType, fetchOffset, ordersFetched ) {

	/*
	// Refresh only if network connection available
	if ( !hasNetwork() && !isOrdersRefreshed( orderType ) ) {
		setOrdersRefreshed( orderType ); // set this so that the toast below does not show each time
		toast( $messagetext.nonetworkconnection );
		return;
	}
	*/
	var url = $host + '/api/o?a=gos&uid=' + $uid + '&kid=' + $entity.kid + '&oty=' + orderType + '&ld=0&o=' + fetchOffset +'&r=' + $ORDERS_FETCH_SIZE ; // earlier: uid=' + $uid + '&p=' + $pwd, but now using BasicAuth
	if ($transferOrder)
		url += '&transfers=1';
	// The page loading message
	var loadingmsg = $labeltext.loadingorders;
	// Load the orders from server
	var request = {
		url: url,
		dataType: 'json',
		cache: false,
		success: function( o ) {
			// Set the flag to indicate that auto-load of orders is done
			if ($transferOrder) {
				setTransfersRefreshed(orderType);
			}
			else {
				setOrdersRefreshed(orderType);
			}
			// Check status and process
			if ( o && o.st == '0' ) { // success
				if ( fetchOffset == 0 ) {
					ordersFetched = o.os;
				   if (ordersFetched && ordersFetched.length == $ORDERS_FETCH_SIZE) {
					   loadOrders(orderType, $ORDERS_FETCH_SIZE, ordersFetched);
				   }
				} else if ( fetchOffset == $ORDERS_FETCH_SIZE ) { // update kiosks so far
					if (ordersFetched && o.os != null)
						ordersFetched.push.apply(ordersFetched, o.os);
				}
				if (ordersFetched ) {
					// Update orders locally
					updateOrders(ordersFetched, orderType);
					// Go to the  orders screen
					var url = '#orders?oty=' + orderType;
					if ($transferOrder)
						url += '&ty=trf';
					$.mobile.changePage(url);
				}

			} else {
				///showDialog( null, o.ms, '#orders?oty=' + orderType );
				// Go to the orders screen
				var url = '#orders?oty=' + orderType;
				if ( $transferOrder)
					url += '&ty=trf';
				$.mobile.changePage( url );
				if (!ordersFetched)
					toast( o.ms );
			}
		},
		error: function( o ) {
			// Set the flag to indicate that auto-load of orders is done
			if ($transferOrder) {
				setTransfersRefreshed(orderType);
			}
			else {
				setOrdersRefreshed(orderType);
			}
		},
		sync: {
			op: 'rords',
			uid: $uid,
			kid: $entity.kid,
			oid: orderType,
			params: { otype: orderType },
			callback: function() {
				// Set the flag to indicate that orders have been refreshed in this session (so no need to auto-refresh)
				if ($transferOrder) {
					setTransfersRefreshed(orderType);
				}
				else {
					setOrdersRefreshed(orderType);
				}
			}
		}
	};
	var logData = {
		functionName: 'loadOrders',
		message: orderType
	};
	// Request server
	requestServer( request, loadingmsg, logData, true, true );
}

// Set the orders loaded flag
function setOrdersRefreshed( otype ) {
	var key = "orders_" + otype;
	setRefreshedFromServer( $entity.kid, key );
}

// Are orders loaded
function isOrdersRefreshed( otype ) {
	///return ( $entity.ordersloaded && $entity.ordersloaded[ otype ] );
	var key = "orders_" + otype;
	return isRefreshedFromServer( $entity.kid, key );
}

// Set the transfers loaded flag
function setTransfersRefreshed( otype ) {
	var key = "transfers_" + otype;
	setRefreshedFromServer( $entity.kid, key );
}

// Are transfers loaded
function isTransfersRefreshed( otype ) {
	///return ( $entity.ordersloaded && $entity.ordersloaded[ otype ] );
	var key = "transfers_" + otype;
	return isRefreshedFromServer( $entity.kid, key );
}

// Are transactions refreshed?
function isTransactionsRefreshed() {
	return isRefreshedFromServer( $entity.kid, "transactions" );
}
function setTransactionsRefreshed() {
	setRefreshedFromServer($entity.kid, "transactions" );
}

// areInventoryRefreshed?
function isInventoryRefreshed() {
	return isRefreshedFromServer( $entity.kid, "inventory" );
}
function setInventoryRefreshed() {
	setRefreshedFromServer($entity.kid, "inventory" );
}


// Set a particular item as loaded/refreshed from server
// NOTE: Type can be one of: "inventory", "orders", "transactions"
function setRefreshedFromServer( kid, type ) {
	var refreshedEntity = $refreshed.server[ kid ];
	if ( !refreshedEntity ) {
		refreshedEntity = {};
		$refreshed.server[ kid ] = refreshedEntity; // empty init.
	}
	refreshedEntity[ type ] = true;
}

// Is this type of data already refreshed from server
function isRefreshedFromServer( kid, type ) {
	var refreshedEntity = $refreshed.server[ kid ];
	if ( !refreshedEntity )
		return false;
	return refreshedEntity[ type ];
}

// Reset refreshed states
function resetRefreshedStates() {
	$refreshed.server= {};
}


// Get valid non-hidden tags
function getValidTags( tags ) {
	var isOrder = isOperationOrder( $currentOperation );
	var validTags = tags;
	if ( isOrder ) {
		if ( $config.tgo ) {
			validTags = [];
			var tagsToBeHidden = $config.tgo.split( ',' );
			for ( var i = 0; i < tags.length; i++ ) {
				if ( $.inArray( tags[i], tagsToBeHidden ) == -1 )
					validTags.push( tags[i] );
			}
		}
	} else {
		if ( $config.tgi ) {
			validTags = [];
			var tagsToBeHidden = $config.tgi.split( ',' );
			for ( var i = 0; i < tags.length; i++ ) {
				if ( $.inArray( tags[i], tagsToBeHidden ) == -1 )
					validTags.push( tags[i] );
			}
		}
	}
	return validTags;
}


// Get valid non-hidden tags
function getValidTagsByOperation( tags ) {
	var isOrder = isOperationOrder( $currentOperation );
	var validTags = tags;
	var inventoryTagsCSV = '';
	if (!isOrder){
		inventoryTagsCSV = getInventoryTagsToHide($currentOperation,$config);
		if ( inventoryTagsCSV ) {
			validTags = [];
			var tagsToBeHidden = inventoryTagsCSV;
			for ( var i = 0; i < tags.length; i++ ) {
				if ( $.inArray( tags[i], tagsToBeHidden ) == -1 )
					validTags.push( tags[i] );
			}
		}
		else //no operation override tags;
			validTags = null;
	}
	return validTags;
}


// Get the number of materials in valid tags only
function getNumMaterialsInValidTags( tags ) {
	var size = 0;
	for ( var i = 0; i < tags.length; i++ ) {
		var materialIds = $materialData.tagmap[ tags[i] ];
		if ( materialIds )
			size += materialIds.length;
	}
	return size;
}

function allowEmptyOrders() {
	return ( $config.emor && $config.emor == '0' );
}

/*
function isHome( pageId ) {
	return ( $home.indexOf( pageId ) != -1 );
}
*/

//////////////// Security ////////////////////////
// Authenticate a user
function authenticate( loginData, password ) {
	return ( loginData.password == MD5( password ) );
}

function confirmLogout(){
    initEventModel($uid,null);
    if ( getNotificationCount() > 0) {
		var cancelUrl = getCurrentUrl(); /// earlier: '#entities';
		var message = ( isLoginAsReconnect() ? $messagetext.somedataenterednotsentlogout_loginasreconnect : $messagetext.somedataenterednotsentlogout );
	    showConfirm( $messagetext.warning, message,null,
		    function() {
		     logout();
		   }, cancelUrl);
	}
	else {
		logout();
	}
}

// Logout a user by clearing the session
function logout() {
	clearSession();
	clearPasswordField();
	$.mobile.changePage( '#login', { reverse: false } );
}

// Clear password on login page
function clearPasswordField() {
	$('#login').find('#pwd').val('');
}

// Exit the application
function exit() {
	//navigator.app.exitApp();
	if ( isDevice() ) {
		showLoader( $messagetext.exiting + '...');
		// Flush logs before exit
		flushLog(function () {
			hideLoader();
			// Exit app.
			navigator.app.exitApp();
		});
	} else {
		navigator.app.exitApp();
	}
}

//////////// Geo-location ////////////////////
//Get the geo-location; optionally store it in session storage
function acquireGeoLocation( gotPositionCallback, errorGettingPositionCallback ) {
	// If we need to get geo-codes now, get'em
	navigator.geolocation.getCurrentPosition(
			gotPositionCallback, errorGettingPositionCallback, { 'enableHighAccuracy': true, 'timeout': 60000, 'maximumAge': 0 }
	);
}

// Handle the case when we got geo
function gotPosition( pos ) {
	// Store position data in session
	var geo = {};
	geo.lat = pos.coords.latitude;
	geo.lng = pos.coords.longitude;
	geo.accuracy = pos.coords.accuracy; // in meters
	geo.altitude = pos.coords.altitude; // altitude
	geo.t = new Date().getTime();
	storeGeo( geo );
	// DEBUG
	/*
	var outputStr =
		"latitude:"+ pos.coords.latitude +"<br>"+
		"longitude:"+ pos.coords.longitude +"<br>"+
		"accuracy:"+ pos.coords.accuracy +"<br>"+
		"altitude:"+ pos.coords.altitude +"<br>"+
		"altitudeAccuracy:"+ pos.coords.altitudeAccuracy +"<br>"+
		"heading:"+ pos.coords.heading +"<br>"+
		"speed:"+ pos.coords.speed + "";
	console.log( 'GEO: ' + outputStr );
	*/
	// END DEBUG
}

// Handle the geo-acquisition error case
function errorGettingPosition( err ) {
	var geo = getGeo();
	if ( geo == null )
		geo = {};
	geo.err = err.code;
	geo.t = new Date().getTime();
	if ( err.code == 1 ) { // rejected by user
		///showDialog( 'Location request', 'Please reload the application or page, and accept the request to capture your position.', null );
		logR( 'w', 'errorGettingPosition', 'Position request rejected by user', null );
	} else if ( err.code == 2 ) {
		logR( 'w', 'errorGettingPosition', 'Position unavailable', null );
	} else if ( err.code == 3 ) {
		logR( 'w', 'errorGettingPosition', 'Timeout expired', null );
	} else {
		logR( 'w', 'errorGettingPosition', 'ERROR: ' + err.message, null );
	}
	// Store geo
	storeGeo( geo );
}

// Check if geo-codes have expired
function isGeoExpired( geo ) {
	if ( geo != null && geo.t ) {
		var now = new Date().getTime();
		return ( ( now - geo.t ) > $GEO_CODE_EXPIRY ); // if we got it > $GEO_CODE_EXPIRY ms ago, then get'em again
	}
	return false;
}

// Check if the geo-acquisition strategy is string
function isGeoStrategyStrict() {
	return ( $config && $config.gcds && $config.gcds == 's' );
}

//////////// Storage functions /////////////////
// Store authenticated user data
function storeAuthData( userId, password, version, authTokenData, ao ) {
	// Update the last user
	storeLastUser( userId );
	// Store the login data
	storeLoginData( userId, password, version, authTokenData );
	// Store user data
	storeUserData( userId, ao, true, false );
	//storehostDomain
	///storeHostDomain($host);
	// Store config. for offline sync., if needed
	// Store config parameters for syncing.
	if ( isDevice() && ao && ao.cf ) {
		var headers = getAuthenticationHeader();
		var syncIntervalsConfig = ( ao.cf.intrvls ? ao.cf.intrvls : $syncConfigDefault );
		writeSyncConfig( ao.cf, headers, syncIntervalsConfig );
	}
}

// Store/get last logged in user ID
function storeLastUser( userId ) {
	storeString( 'lastUser', userId );
}
function getLastUser() {
	return getString( 'lastUser' );
}

function getUserLanguage(){
    var language = getString('userlanguage');
	if ( !language || language == '' )
		language = 'en';
	return language;
}

function storeUserLanguage(localeLanguage) {
    storeString('userlanguage',localeLanguage);
}

// Store general params.
function storeGeneralAttr( key, value ) {
	var id = "gparams";
	var json = getObject( id );
	if ( !json )
		json = {};
	json[ key ] = value;
	storeObject( id, json );
}

// Get general params.
function getGeneralAttr( key ) {
	var id = "gparams";
	var json = getObject( id );
	if ( !json )
		return null;
	return json[ key ];
}

// Store/get login-as-reconnect
function storeLoginAsReconnect( loginAsReconnect ) {
	var val = ( loginAsReconnect ? 'true' : 'false' );
	storeString( 'loginAsReconnect', val );
}
function isLoginAsReconnect() {
	var loginAsReconnect = getString( 'loginAsReconnect' );
	return ( loginAsReconnect && loginAsReconnect == 'true' );
}

function storeSupportInfo( userId ){
    var supportObj = {};
    if ($config.seml)
		supportObj.email = $config.seml;
    if ($config.sphn)
		supportObj.phone = $config.sphn;
    if ($config.scnm)
		supportObj.contactname = $config.scnm;
	// Store support info. by user (given it can vary by role)
	var supportInfoKey = 'supportInfo.' + userId;
    storeObject(supportInfoKey,supportObj);

}

function getSupportInfo( userId ) {
    var key = 'supportInfo.' + userId;
	return getObject( key );
}



function storeHostDomain(hostDomainName){
    storeString('hostDomain',hostDomainName);
}

function getHostDomain(){
    return getString('hostDomain');
}

// Store/update login data
function storeLoginData( userId, password, version, authTokenData ) {
	// Get the existing login object, if any
	var key = 'l.' + userId;
	var lObj = getObject( key );
	if ( lObj == null )
		lObj = {};
	// Encode password that is stored on disk
	lObj.password = MD5( password );
	lObj.version = version;
	lObj.lastLogin = new Date().getTime(); /// earlier: getStrFromDate( new Date() ); // NOTE: Not used anywhere, as yet (as noted on Nov 20, 2015)
	// Store auth token (with token and expiry), if it exists
	if ( authTokenData ) {
		if ( authTokenData.expiry && ( typeof authTokenData.expiry === 'string' ) ) {
			try {
				authTokenData.expiry = parseInt( authTokenData.expiry );
			} catch ( e ) {
				logR( 'e', 'storeLoginData', 'ERROR: when parsing expiry ' + authTokenData.expiry + ', ' + e.message, e.stack );
			}
		}
		lObj.authToken = authTokenData;
		// Add the time of token acquisition as well
		lObj.authToken.acquiredOn = new Date().getTime();
	}

	// Store object
	storeObject( key, lObj );
}

function getLoginData( userId ) {
	var key = 'l.' + userId;
	return getObject( key );
}

// Store user data from authenticated JSON output
function storeUserData( userId, ao, isStoreEntities, keepLocalData ) {
	if ( !ao )
		return false;
	var key = 'u.' + userId;
	// Store entities separately; replace the key in this object with simply a list of entity IDs (for space saving)
	if ( isStoreEntities && ao.ki ) {
		ao.ki = storeEntities( userId, ao.ki, keepLocalData );
	}
	// Store user data
	storeObject( key, ao );
}

// Get a given user's data (except entity details, but including list of associated entity IDs)
function getUserData( userId ) {
	var key = 'u.' + userId;
	return getObject( key );
}

// Get the user's password
function getUserPassword( userId ) {
	var l = getLoginData( userId );
	if ( l.password )
		return l.password;
	return null;
}

// Get an entity associated with a given user
function getEntity( userId, kid ) {
	var key = 'k.' + userId + '.' + kid;
	return getObject( key );
}

// Store entity
function storeEntity( userId, entity ) {
	var key = 'k.' + userId + '.' + entity.kid;
	storeObject( key, entity );
}

// Store entities separately mapped to each entity's key; return an array of entity IDs (kid) in order 
function storeEntities( userId, entities, keepLocalData ) {
	var entityIds = [];
	if ( !entities || entities.length == 0 )
		return entityIds;
	// Get the existing materials data
	for ( var i = 0; i < entities.length; i++ ) {
		entityIds.push( entities[i].kid );
		var key = 'k.' + userId + '.' + entities[i].kid;
		// Check if existing local data, if any, is to be preserved
		if ( keepLocalData ) {
			var entity = getObject( key );
			if ( entity && entity.localdata )
				entities[i].localdata = entity.localData;
		}
		// Get the materials and store
		storeObject( key, entities[i] );
	}
	return entityIds;
}


function storeRelatedEntities( userId, relatedEntities, relationshipType,entityId ) {
	var relatedEntityIds = [];
	if ( !relatedEntities || relatedEntities.length == 0 )
		return null;
	// Get the existing materials data
	for ( var i = 0; i < relatedEntities.length; i++ ) {
		relatedEntityIds.push( relatedEntities[i].kid );
		}
	var key = 'rk.' + userId + '.' + entityId+'.'+relationshipType;
	// Get the materials and store
	storeObject( key, relatedEntityIds );

}


// Store/get geo-location
// format: { lat: <lat>, lng: <lng>, t: <timestamp>, err: <error-code-if-any> } 
function storeGeo( geo ) {
	storeObject( 'geo', geo );
}

// Get geo-location
function getGeo() {
	return getObject( 'geo' );
}

// Process entities across tags, and build their tag and starts-with-search indicies as required
function updateManagedEntitiesIndices( userData ) {
	var kids = userData.ki;
	var isRouteEnabled = userData.rteu;
	if ( !kids )
		return;
	if ( !$config.rtt || $config.rtt == '' ) {
		updateManagedEntitiesSearchIndex( kids, undefined );
		return;
	}
	// Process tagged entities
	if ( $entitiesData.ents )
		delete $entitiesData.ents;
	$entitiesData.ents = {};
	if ( !$searchIndex.entities )
		$searchIndex.entities = {}; // init. search index
	var untaggedIds = [];
	for ( var i = 0; i < kids.length; i++ ) {
		var kid = kids[i];
		// Get entity
		var entity = getEntity( $uid, kid );
		if ( !entity )
			continue;
		// Get route tag
		var rtag = entity.rtt;
		if ( !rtag ) {
			untaggedIds.push( kid );
			continue;
		}
		// Update entities data (tag to entity-ID mapping)
		if ( !$entitiesData.ents[ rtag ] )
			$entitiesData.ents[ rtag ] = [];
		$entitiesData.ents[ rtag ].push( kid );
	}
	// Update the corresponding search index
	if ( untaggedIds.length == kids.length ) {
		delete $entitiesData.ents; // reset the tag to kids mapping
		updateManagedEntitiesSearchIndex( kids, undefined ); // update search index, as normally without tags
	} else {
		// Add untagged Ids to the route tag map
		if ( untaggedIds.length > 0 )
			$entitiesData.ents[ $ROUTETAG_UNTAGGED ] = untaggedIds;
		for ( var rtag in $entitiesData.ents ) {
			if ( !$entitiesData.ents.hasOwnProperty( rtag ) )
				continue;
			// Sort the entities per tag
			sortEntities( $entitiesData.ents[ rtag ], 'ents', isRouteEnabled );
			// Update the search index
			updateManagedEntitiesSearchIndex( $entitiesData.ents[ rtag ], rtag );
		}
	}
}

// Function to update search index for all managed entities; if entity tag is given, then update under this tag
function updateManagedEntitiesSearchIndex( kids, rtag ) {
	if ( kids.length <= $PAGE_SIZE )
		return; // no index required, if only one entity
	// Init. search index
	if ( rtag ) {
		if ( !$searchIndex.entities )
			$searchIndex.entities = {};
		$searchIndex.entities[ rtag ] = {};
	} else {
		$searchIndex.entities = {};
	}
	for ( var i = 0; i < kids.length; i++ ) {
		var kid = kids[i];
		var entity = getEntity( $uid, kid );
		///var text = entity.n.substring( 0, 1 ).toLowerCase(); // first character
		updateEntitySearchIndex( entity, rtag );
	}
}

// Update search index for all related entities; if entity tag is given, then update under this tag
function updateRelatedEntitiesSearchIndex( type, rtag, relatedEntities ) {
	if ( relatedEntities.length <= $PAGE_SIZE )
		return; // no index required, if only one entity
	var key = type;
	if ( rtag )
		key = type + '.' + rtag;
	if ( !$searchIndex.relatedentities )
		$searchIndex.relatedentities = [];
	if ( $searchIndex.relatedentities[ key ] )
		delete $searchIndex.relatedentities[ key ];
	$searchIndex.relatedentities[ key ] = {};
	// Init. search index
	for ( var i = 0; i < relatedEntities.length; i++ ) {
		var entity = getEntity($uid,relatedEntities[i].kid);
		if (!entity)
			 entity = relatedEntities[i];
		var text = entity.n.substring( 0, 1 ).toLowerCase(); // first character
		if ( !$searchIndex.relatedentities[ key ][text] )
			$searchIndex.relatedentities[ key ][text] = [];
		$searchIndex.relatedentities[ key ][text].push( entity.kid );
	}
}

// Update a single entity's search index
function updateEntitySearchIndex( entity, rtag ) {
	var kid = entity.kid;
	var text = entity.n.substring( 0, 1 ).toLowerCase(); // first character
	if ( rtag ) {
		if ( !$searchIndex.entities[ rtag ] )
			$searchIndex.entities[ rtag ] = {};
		if ( !$searchIndex.entities[ rtag ][text] )
			$searchIndex.entities[ rtag ][ text ] = [];
		$searchIndex.entities[ rtag ][ text ].push( kid );
	} else {
		if ( !$searchIndex.entities[ text ] )
			$searchIndex.entities[ text ] = [];
		$searchIndex.entities[ text ].push( kid );
	}
}

// Update all the indices required for a given kiosk
function updateRelatedEntityIndices() {
	// Update related entities search indices, if present
	if ( $entity.vnds ) // vendors
		updateRelatedEntitiesIndices( $entity.vnds, 'vnds', $entity.rtevnds );
	if ( $entity.csts ) // customers
		updateRelatedEntitiesIndices( $entity.csts, 'csts', $entity.rtecsts );
}

// Update a given material search index
function updateMaterialSearchIndex( mid, name, tag ) {
	var text = name.substring( 0, 1 ).toLowerCase(); // first character, in lower case
	var hasTag = ( tag && tag != null );
	// Get the first character text to mids mapping
	var txt2mids;
	if ( hasTag ) {
		txt2mids = $searchIndex.materials[ tag ];
		if ( !txt2mids ) {
			txt2mids = {};
			$searchIndex.materials[ tag ] = txt2mids;
		}
	} else {
		txt2mids = $searchIndex.materials;
		if ( !txt2mids ) {
			txt2mids = {};
			$searchIndex.materials = txt2mids;
		}
	}
	// Get the mids mapping to this text
	var mids = txt2mids[ text ];
	if ( !mids ) {
		mids = [];
		txt2mids[ text ] = mids;
	}
	// Add the new mid to this mapping
	mids.push( mid );
}

// Update the orders search index for a given order-type (otype)
function updateOrdersSearchIndex() {
	if ( $searchIndex.orders )
		delete $searchIndex.orders;
	$searchIndex.orders = {};
	// Update index for purchase orders
	var otype = 'prc';
	var orders = getOrders( otype );
	if ( orders != null ) {
		for ( var i = 0; i < orders.length; i ++ ) {
			var order = orders[i];
			updateOrderSearchIndex( order, otype );
		}
	}
	// Update index for sales orders
	otype = 'sle';
	orders = getOrders( otype );
	if ( orders != null ) {
		for ( var i = 0; i < orders.length; i ++ ) {
			var order = orders[i];
			updateOrderSearchIndex( order, otype );
		}
	}
}

function resetOrdersSearchIndex( otype ) {
	if ( !$searchIndex.orders )
		return;
	for ( var i = 0; i < $orderStates.length; i++ ) {
		var key = otype + '_' + $orderStates[i];
		if ( $searchIndex.orders[ key ] )
			delete $searchIndex.orders[ key ];
	}
}

// Update a given order's search index
function updateOrderSearchIndex( order, otype ) {
	var oid = order.tid;
	var title = getOrderTitle( order, otype );
	//var text = title.substring( 0, 1 ).toLowerCase(); // first character, in lower case
	var text = oid;
	var key = otype + '_' + order.ost;
	var txt2oids = $searchIndex.orders[ key ];
	if ( !txt2oids ) {
		txt2oids = {};
		$searchIndex.orders[ key ] = txt2oids;
	}
	// Get the oids starting with title's first character
	var oids = txt2oids[ text ];
	if ( !oids ) {
		oids = [];
		txt2oids[ text ] = oids;
	}
	// Add this oid to the list
	oids.push( oid );
}

// Get order Ids from index
function getOidsFromSearchIndex( text, otype, status ) {
	if ( !$searchIndex.orders )
		return undefined;
	var key = otype + '_' + status;
	return $searchIndex.orders[ key ];
}

function resetOrderSearch () {
	$orderTagFilter = '';
	$('#orders').find('#ordertagsearch').val('');
	$orderSearchFilter = '';
	$('#orders').find('#ordersearch').val('');
}

// Get the mids starting with a given character (returns undefined, if nothing found)
// (indexType can be 'materials', 'entities' or 'csts' or 'vnds'; tag should be passed only where relevant (say, for materials)
function getIdsStartingWith( type, character, tag ) {
	var sIndex = $searchIndex[ type ];
	if ( !sIndex )
		return undefined;
	var txt2mids;
	if ( tag && tag != null )
		txt2mids = sIndex[ tag ];
	else
		txt2mids = sIndex;
	if ( !txt2mids )
		return undefined;
	return txt2mids[ character ];
}

// Update related entities search index (linkType can be csts or vnds, for customer and vendors respectively)
function updateRelatedEntitiesIndices( relatedEntities, linkType, isRouteEnabled ) {
	// Init. tag index
	if ( $entitiesData[ linkType ] )
		delete $entitiesData[ linkType ]; // free the old object, if any
	$entitiesData[ linkType ] = {};
	var untaggedEntities = [];
	for ( var i = 0; i < relatedEntities.length; i++ ) {
		var relatedEntity = relatedEntities[i];
		
		// Update tag index
		var rtag = relatedEntity.rtt;
		if ( !rtag || rtag == '' ) {
			untaggedEntities.push( relatedEntity );
			continue;
		}
		// Update tag index now
		if ( !$entitiesData[ linkType ][ rtag ] )
			$entitiesData[linkType][rtag] = [];
		$entitiesData[linkType][rtag].push( relatedEntity ); // NOTE: here the entire entity reference has to be added, and NOT ids, given related entities are not stored anywhere else
	}
	// Update un-tagged entities, as needed
	if ( untaggedEntities.length == relatedEntities.length ) {
		delete $entitiesData[linkType];
		updateRelatedEntitiesSearchIndex( linkType, undefined, relatedEntities );
	} else if ( untaggedEntities.length > 0 ) { // add untagged Ids to the route tag map under the UNTAGGED (Others) tag
		$entitiesData[ linkType ][ $ROUTETAG_UNTAGGED ] = untaggedEntities;
		for ( var rtag in $entitiesData[ linkType ] ) {
			if ( !$entitiesData[ linkType ].hasOwnProperty( rtag ) )
				continue;
			var tagEntities = $entitiesData[ linkType ][rtag];
			if ( !tagEntities || tagEntities.length == 0 )
				continue;
			// Sort the tag list
			sortEntities( tagEntities, linkType, isRouteEnabled );
			// Update search index for this tagged list of entities
			updateRelatedEntitiesSearchIndex( linkType, rtag, tagEntities );
		}
	}
}

// Storing local modifications
//  - formData format is { q : <quantity>, reason: "<reason>" }
//  - selectedLinkedEntity format is { kid : "<linked kiosk id>", type: "<customer(csts)-or-vendor(vnds)>" }
// NOTE: Local modifications format is:
//		  - { 'ei' : { materials : { mid : { q : "", reason: "" }, ...}, ... } OR
//		  - { 'ei' : { materials : { kid : { mid : { q : "", reason: "" },... }, linkedentity : { kid: "...", type : "<csts-or-vnds>" } }, ... }, where kid represents a linked entity (materials modified per linked entity); and linkedentity is the currently selected linked-entity for this entity
function storeLocalModifications( currentOperation, entity, userId, mid, formData, selectedLinkedEntity ) {
	var localData = entity.localdata;
	if ( !localData )
		localData = {};
	// Set selected linked entity, if given
	if ( selectedLinkedEntity != null ) {
		if ( !localData[ currentOperation ] )
			localData[ currentOperation ] = {};
		localData[ currentOperation ].linkedentity = selectedLinkedEntity;
		if (currentOperation == 'no')
			localData[ currentOperation ].linkedentity.otype = $newOrderType;
	}
	// Update materials specific to an operation and/or linked-entity 
	var materialModifications = localData[ currentOperation ];
	if ( !materialModifications ) {
		materialModifications = {};
		localData[ currentOperation ] = materialModifications;
	}
	if ( mid != null && formData != null ) {
		var modifiedMaterials = materialModifications.materials;
		// Check if form data has to be stored against a selected linked entity
		if ( materialModifications.linkedentity ) {
			if ( !materialModifications.materials )
				materialModifications.materials = {};
			modifiedMaterials = materialModifications.materials[ materialModifications.linkedentity.kid ];
		}
		if ( !modifiedMaterials )
			modifiedMaterials = {};
		var modifiedMaterial = modifiedMaterials[ mid ];
		var updateNotification = false;
		if ( !modifiedMaterial )
		 {
			modifiedMaterial = { mid: mid };
			updateNotification = true;
		 }

		if (formData.bt)
			modifiedMaterial.bt = formData.bt;

		modifiedMaterial.q = formData.q;
		if ( formData.reason != null ) {
			if (formData.reason != '')
				modifiedMaterial.reason = formData.reason;
			else if (modifiedMaterial.reason)
				delete modifiedMaterial.reason;
		}
		if ( formData.ostk )
			modifiedMaterial.ostk = formData.ostk;
		if ( formData.mst != null ) {
			if (formData.mst != '')
			 	modifiedMaterial.mst = formData.mst;
			else if (modifiedMaterial.mst)
				delete modifiedMaterial.mst;
		}
		if ( formData.atd != null) {
			if (formData.atd != '') {
				modifiedMaterial.atd = formData.atd;
				localData[currentOperation].latd = formData.atd; //last actual transaction date entered
			} else if (modifiedMaterial.atd) {
				delete modifiedMaterial.atd;
			}
		}
		if ( formData.smid )
			modifiedMaterial.smid = formData.smid;
		//delete error data for material
		delete modifiedMaterial.error;
		modifiedMaterials[ mid ] = modifiedMaterial;
				
		if ( materialModifications.linkedentity )
			localData[ currentOperation ].materials[ materialModifications.linkedentity.kid ] = modifiedMaterials;
		else
			localData[ currentOperation ].materials = modifiedMaterials;
	}
	entity.localdata = localData;
	// Persist entity	
	storeEntity( userId, entity );
	// update OEM
	if (updateNotification)
	{
		var eventParams ={};
		eventType = currentOperation;
		eventParams.qty = 1;
		if (selectedLinkedEntity)
		{
		  eventParams.lkid = selectedLinkedEntity.kid;
		  var type = selectedLinkedEntity.type;
		  if (!type || currentOperation == 'no')
			 type = getSelectedLinkedEntityType( selectedLinkedEntity.kid, entity.kid );
		  eventParams.type = type;
		  if (currentOperation == 'no')
			  eventParams.otype = $newOrderType;
		}
		eventParams.mid = mid;
        updateEvent($uid,entity.kid,eventType,eventParams);
	}

}

// Get the local modifications a given material and operation
function getLocalModifications( mid, currentOperation, entity ) {
	var localData = entity.localdata;
	if ( !localData )
		return null;
	var materialModifications = localData[ currentOperation ];
	if ( materialModifications ) {
		var modifiedMaterials = materialModifications.materials;
		if ( materialModifications.linkedentity && materialModifications.materials )
			modifiedMaterials = materialModifications.materials[ materialModifications.linkedentity.kid ];
		if ( modifiedMaterials ) {
			if ( mid == null )
				return modifiedMaterials;
			var modifiedMaterial = modifiedMaterials[ mid ];
			if ( modifiedMaterial )
				return modifiedMaterial;
		}
	}
	return null;
}

// Mark partial errors in local modifications
function markPartialErrorsLocally( updInventoryOutput ) {
	// Check if there are partial errors
	if ( !updInventoryOutput || !updInventoryOutput.er  || updInventoryOutput.er.length == 0 )
		return false;
	var modifiedMaterials = getLocalModifications( null, $currentOperation, $entity );
	if ( !modifiedMaterials )
		return false;
	// Mark materials with the error data
	for ( var i = 0; i < updInventoryOutput.er.length; i++ ) {
		var error = updInventoryOutput.er[ i ];
		if ( error.mid && modifiedMaterials[ error.mid ] ) {
			// Mark this as error
			modifiedMaterials[error.mid].error = error;
			// Update correct stock sent from server locally
			if ( $materialData.materialmap[ error.mid ] ) {
				if (error.q != null && error.q != undefined) {
					$materialData.materialmap[error.mid].q = parseInt(error.q);
					$materialData.materialmap[error.mid].t = error.t
					//Update batch quantity
					var m = error.mid;
					if (m.bt && $materialData.materialmap[error.mid].bt) {
						var noOfErrBatches = m.bt.length;
						var noOfBatches = $materialData.materialmap[error.mid].bt.legnth;
						for (var j = 0; j < noOfErrBatches; j++) {
							for (var l = 0; l < noOfBatches; l++) {
								var item = $materialData.materialmap[error.mid].bt[l];
								for (var k in item) {
									if (!item.hasOwnProperty(k))
										continue;
									if ($materialData.materialmap[error.mid].bt[l].bid == m.bt[j].bid) {
										if (m.bt[j].q != null && m.bt[j].q != undefined) {
											$materialData.materialmap[error.mid].bt[l].q = m.bt[j].q;
											$materialData.materialmap[error.mid].bt[l].t = m.bt[j].t;
											$materialData.materialmap[error.mid].bt[l].bexp = m.bt[j].bexp;
											$materialData.materialmap[error.mid].bt[l].bmfdt = m.bt[j].bmfdt;
											$materialData.materialmap[error.mid].bt[l].bmfnm = m.bt[j].bmfnm;
										}
									}
								}

							}
						}

					}
				}
			}
		}
	}
	// Persist the stock from server
	storeEntity( $uid, $entity );
	return true;
}

// Show partial errors in a dialog
function showPartialErrors( msgPrefix, okUrl ) {
	var errors = getPartialErrorMaterialsByOp( $currentOperation );
	if ( !errors )
		return;
	var msg = $messagetext.errorsinquantities + '<br/>';
	if ( !errors || errors.length == 0 )
		return;
	for ( var i = 0; i < errors.length; i++ ) {
		var modifiedMaterial = errors[ i ];
		var material = $materialData.materialmap[ modifiedMaterial.mid ];
		if ( !material || !modifiedMaterial.error || !modifiedMaterial.error.ms )
			continue;
		var errorQty = modifiedMaterial.error.q;
		var serrorQty = '';
		if (errorQty) {
			errorQty = parseInt(modifiedMaterial.error.q);
			serrorQty = errorQty ;
		}
		var errorMsg = '';
		if (material.bt && modifiedMaterial.error.bt)
		{
			for ( var j = 0; j < modifiedMaterial.error.bt.length; j++ ) {
			  errorMsg += modifiedMaterial.error.bt[j].bid + '- ' + modifiedMaterial.error.bt[j].ms+'<br/>';
			}
		}
		else
			errorMsg = modifiedMaterial.error.ms;
		msg += '<br/> <b>' + material.n+'</b>';
		if (serrorQty != '')
			msg += ' (' + serrorQty +')';
		msg += '<br/> ' + errorMsg  + '<br/>';
	}
	if ( msg != '' ) {
		$partialErrorsShown = true;
		msg = ( msgPrefix ? msgPrefix + msg : msg );
		showDialog($labeltext.error, msg, ( okUrl ? okUrl : getCurrentUrl() ) );
	} else {
		$partialErrorsShown = false;
	}
}

// Get local materials with errors (i.e. partial errors from server)
// returns a mapping of op to an array of error materials: op: [ <modifiedMaterial> ]
/* DO NOT REMOVE - MAY BE USED LATER
function getPartialErrorMaterials() {
	var ops = [ 'ei', 'ew', 'ts', 'er', 'es' ];		// issues, discards, transfers, receipts, stock-counts
	var errors = {};	// op --> [ <modifiedMaterial> ]
	for ( var i = 0; i < ops.length; i++ ) {
		var errorMaterials = getPartialErrorMaterialsByOp( ops[ i ] );
		if ( errorMaterials && errorMaterials.length > 0 )
			errors[ ops[i] ] = errorMaterials;
	}
	if ( $.isEmptyObject( errors ) )
		return null;
	return errors;
}
*/

// Returns an array of error materials (modifiedMaterials) for this operation (these are the partial errors after an inventory update)
function getPartialErrorMaterialsByOp( op ) {
	var modifiedMaterials = getLocalModifications(null, op, $entity);
	var errorMaterials = [];
	if ( modifiedMaterials ) {
		for ( var mid in modifiedMaterials ) {
			if ( !modifiedMaterials.hasOwnProperty( mid ) )
				continue;
			var modifiedMaterial = modifiedMaterials[ mid ];
			if ( modifiedMaterial.error ) {
				if ( !modifiedMaterial.mid )
					modifiedMaterial.mid = mid;
				errorMaterials.push(modifiedMaterial);
			}
		}
	}
	return errorMaterials;
}

// Get the partial error material
function getPartialErrorMaterial( op, mid ) {
	var modifiedMaterial = getLocalModifications( mid, op, $entity );
	if ( modifiedMaterial && modifiedMaterial.error )
		return modifiedMaterial;
	return null;
}

// Get selected linked kiosk, if any
function getSelectedLinkedEntity( currentOperation, entity ) {
	if ( !entity.localdata )
		return null;
	var localData = entity.localdata[ currentOperation ];
	if ( !localData )
		return null;
	if ( localData.linkedentity )
		return localData.linkedentity;
	else
		return null;
}

//Get type of linked kiosk, if any
function getSelectedLinkedEntityType( linkedKioskId, kioskId ) {
	var entity = getEntity($uid,kioskId);
	if (!entity)
		return null;
	var entityType = '';
	if(entity.csts && entity.csts.length > 0) 
	{
		var cstsData = entity.csts;
		if (!cstsData)
			return null;
	   for ( var i = 0; i < cstsData.length; i++ )
		{
		   if (cstsData[i].kid == linkedKioskId)
		   {
			  entityType = 'csts';
			  return entityType;
		   }
		}
	}
	if(entity.vnds && entity.vnds.length > 0) 
	{
		var vndsData = entity.vnds;
		if (!vndsData)
			return null;
	   for ( var i = 0; i < vndsData.length; i++ )
		{
		   if (vndsData[i].kid == linkedKioskId)
		   {
			  entityType = 'vnds';
			  return entityType;
		   }
		}
	}
	return entityType;
}




function skipLinkedEntitySelection(pageOffset) {
	nextUrl = '#materials?'+'bo=' + pageOffset +'&kid='+$entity.kid;
	deleteSelectedLinkedEntity($currentOperation);
	$.mobile.changePage( nextUrl);
}

function clearLinkedEntitySelection() {
	nextUrl = '#relatedentities?'+'&op='+$currentOperation+'&ty='+getRelationshipTypeForOperation($currentOperation);
	var localModifications = hasLocalModifications($currentOperation,$entity,true);
	togglelinkedEntityForMaterials($currentOperation,null);
	$.mobile.changePage( nextUrl);
}

function deleteSelectedLinkedEntity (currentOperation) {
	if (!$entity.localdata)
		return null;
	var localData = $entity.localdata[ currentOperation ];
	if ( !localData )
		return null;
	if ($entity.localdata[currentOperation].linkedentity)
	 delete $entity.localdata[currentOperation].linkedentity;
	storeEntity($uid,$entity);
	return true;
}



function togglelinkedEntityForMaterials(currentOperation,linkedEntity) {
	var modifiedMaterialList ={};
	if ($entity.localdata) {
		if ($entity.localdata[currentOperation]) {
			var modifiedMaterials = $entity.localdata[currentOperation]; //get materials for current operation
			if (modifiedMaterials.materials) {
				if (!linkedEntity) { //Related entity to be removed from materials
					for (var lkid in modifiedMaterials.materials) {
						if (!modifiedMaterials.materials.hasOwnProperty(lkid))
							continue;
						for (var materialId in modifiedMaterials.materials[lkid]) {
							if (!modifiedMaterials.materials[lkid].hasOwnProperty(materialId))
								continue;
							modifiedMaterialList[materialId] = modifiedMaterials.materials[lkid][materialId];
						}
						if (modifiedMaterialList){
							var lentity = {};
							lentity.kid = lkid;
							lentity.type = getSelectedLinkedEntityType(lkid,$entity.kid);
							$entity.localdata[currentOperation].linkedentity = lentity;
							clearLocalModifications( $uid, currentOperation, null, true, false ); //clears local data for each related entity selected earlier
						}
					}
				}
				else {   // add related entity to the materials
					if (linkedEntity.kid) {
						var lkid = linkedEntity.kid;
						modifiedMaterialList[lkid] = {};
						for (var materialId in modifiedMaterials.materials) {
							if (!modifiedMaterials.materials.hasOwnProperty(materialId))
								continue;
							modifiedMaterialList[lkid][materialId] = modifiedMaterials.materials[materialId];
						}
					}
				}
				if (modifiedMaterialList) {
					if (!linkedEntity) {
						deleteSelectedLinkedEntity(currentOperation); //clear selected linked entity saved in local data earlier
					}
					else {
						clearLocalModifications( $uid, currentOperation, null, true, false ); //clears local data and notification panel
						$entity.localdata[currentOperation].linkedentity = linkedEntity; //set selected linked entity
					}
					$entity.localdata[currentOperation].materials = modifiedMaterialList;
					storeEntity($uid, $entity);
					//update notification panel
					var eventParams = {};
					eventParams.qty = 1;
					if (linkedEntity) {
						for (var materialId in modifiedMaterialList[lkid]) {
							if (!modifiedMaterialList[lkid].hasOwnProperty(materialId))
								continue;
							eventParams.mid = materialId;
							eventParams.lkid = linkedEntity.kid;
							eventParams.type = linkedEntity.type;
							updateEvent($uid, $entity.kid, currentOperation, eventParams);
						}
					}
					else {
						for (var materialId in modifiedMaterialList) {
							if (!modifiedMaterialList.hasOwnProperty(materialId))
							continue;
							eventParams.mid = materialId;
							updateEvent($uid, $entity.kid, currentOperation, eventParams);
						}

					}
				}
				return true;
			}

		}
	}
	return false;
}

// Get the local modifications for a given operation, in a sorted array along with material names
function getLocalModificationsSorted( currentOperation, entity, materialMap ) {
	var localModificationsOrig = getLocalModifications( null, currentOperation, entity ); // NOTE: these objects will not have the material name
	if ( !localModificationsOrig || localModificationsOrig == null )
		return null;
	// Clone this object
	var localModifications = $.extend( true, {}, localModificationsOrig ); // do a deep copy
	// Get the mid list, and insert a material name into the localModifications; then sort
	var mids = [];
	for ( var mid in localModifications ) {
		if ( !localModifications.hasOwnProperty(mid) )
			continue;
		mids.push( mid );
		localModifications[ mid ].n = materialMap[ mid ].n;
		localModifications[ mid ].mid = mid;
	}
	// Get sorted materials
	return getSortedMaterials( mids, localModifications );
}

// Clear local modifications
function clearLocalModifications( userId, currentOperation, mid, persist, keepPartialErrors ) {
	if ( !$entity.localdata )
		return;
	var qty = 0;
	// Remove relevant op data
	var materialModifications = $entity.localdata[ currentOperation ];
	if ( materialModifications ) {
		var linkedEntity = materialModifications.linkedentity;
		var lkid; // linked kiosk Id (will be inited. if specified)
		if ( linkedEntity ) {
			lkid = linkedEntity.kid;
			if ( $entity.localdata[ currentOperation ].materials && $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ] ) {
				if ( mid != null ) {
					var modifiedMaterial = $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ][mid];
					if ( modifiedMaterial && ( !keepPartialErrors || !modifiedMaterial.error ) ) {
						qty =  1;
						delete $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ][mid];					
					}
					else if (modifiedMaterial && modifiedMaterial.error && keepPartialErrors) {
						if (modifiedMaterial.error.bt){
							for (i =0; i < modifiedMaterial.bt.length; i++)  {
								if (!isBatchInError(modifiedMaterial.bt[i].bid,modifiedMaterial.error)) {
									var totalq = modifiedMaterial.q;
									var batchq = modifiedMaterial.bt[i].q;
									if (totalq > batchq)
										totalq = totalq - batchq;
									$entity.localdata[currentOperation].materials[linkedEntity.kid][mid].q = totalq;
									$entity.localdata[currentOperation].materials[linkedEntity.kid][mid].bt.splice(i, 1);
								}
							}
							if (!modifiedMaterial.bt) {
								qty = 1;
								delete $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ][mid];
							}
						}

					}
					// If the materials object is empty, then remove this operation's object completely
					if ( $.isEmptyObject( $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ] ) )
						delete $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ];
				} else {
					if ( !keepPartialErrors ) {
						qty = Object.keys($entity.localdata[currentOperation].materials[linkedEntity.kid]).length;
						delete $entity.localdata[currentOperation].materials[linkedEntity.kid];
					} else { // keep the changes with partial errors
						var modifiedMaterials = $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ];
						for ( var mid in modifiedMaterials ) {
							if ( !modifiedMaterials.hasOwnProperty( mid ) )
								continue;
							 if (!modifiedMaterials[mid].error) {
									delete $entity.localdata[currentOperation].materials[linkedEntity.kid][mid];
									qty++;
								}
							  else if (modifiedMaterials[mid].error.bt) { //error and material has batches , partial error within the batches in a material
								    for (i =0; i < modifiedMaterials[mid].bt.length; i++)  {
										if (!isBatchInError(modifiedMaterials[mid].bt[i].bid,modifiedMaterials[mid].error)) {
											var totalq = modifiedMaterials[mid].q;
											var batchq = modifiedMaterials[mid].bt[i].q;
											if (totalq > batchq)
												totalq = totalq - batchq;
											$entity.localdata[currentOperation].materials[linkedEntity.kid][mid].q = totalq;
											$entity.localdata[currentOperation].materials[linkedEntity.kid][mid].bt.splice(i, 1);
										}
									}
								    if (!$entity.localdata[currentOperation].materials[linkedEntity.kid][mid].bt) {
										delete $entity.localdata[currentOperation].materials[linkedEntity.kid][mid];
										qty++;
									}
							 }
						}
					}
				}
			}
		} else {
			if ( $entity.localdata[ currentOperation ].materials ) {
				if ( mid != null ) {
					var modifiedMaterial = $entity.localdata[ currentOperation ].materials[mid];
					if ( modifiedMaterial && ( !keepPartialErrors || !modifiedMaterial.error ) ) {
						qty = 1;
						delete $entity.localdata[ currentOperation ].materials[mid];
					}
					else if (modifiedMaterial && modifiedMaterial.error && keepPartialErrors) {
						if (modifiedMaterial.error.bt){
							for (i =0; i < modifiedMaterial.bt.length; i++)  {
								if (!isBatchInError(modifiedMaterial.bt[i].bid,modifiedMaterial.error)) {
									var totalq = modifiedMaterial.q;
									var batchq = modifiedMaterial.bt[i].q;
									if (totalq > batchq)
										totalq = totalq - batchq;
									$entity.localdata[currentOperation].materials[mid].q = totalq;
									$entity.localdata[currentOperation].materials[mid].bt.splice(i, 1);
								}
							}
							if (!$entity.localdata[currentOperation].materials[mid].bt) {
								qty = 1;
								delete $entity.localdata[currentOperation].materials[mid];
							}
						}

					}
					// If the materials object is empty, then remove this operation's object completely
					if ( $.isEmptyObject( $entity.localdata[ currentOperation ].materials ) )
						delete $entity.localdata[ currentOperation ];
				} else {
					if ( !keepPartialErrors ) {
						qty = Object.keys($entity.localdata[currentOperation].materials).length;
						delete $entity.localdata[currentOperation].materials;
					} else { // keep the changes with partial errors
						var modifiedMaterials = $entity.localdata[ currentOperation ].materials;
						for ( var mid in modifiedMaterials ) {
							if ( !modifiedMaterials.hasOwnProperty( mid ) )
								continue;
							if ( !modifiedMaterials[ mid ].error ) {
								delete $entity.localdata[ currentOperation ].materials[mid];
								qty++;
							}
							else if (modifiedMaterials[mid].error.bt) { //error and material has batches , partial error within the batches in a material
								for (i =0; i < modifiedMaterials[mid].bt.length; i++)  {
									if (!isBatchInError(modifiedMaterials[mid].bt[i].bid,modifiedMaterials[mid].error)){
										var totalq = modifiedMaterials[mid].q;
										var batchq = modifiedMaterials[mid].bt[i].q;
										if (totalq > batchq)
											totalq = totalq - batchq;
										$entity.localdata[currentOperation].materials[mid].q = totalq;
										$entity.localdata[currentOperation].materials[mid].bt.splice(i, 1);
									}
								}
								if (!$entity.localdata[currentOperation].materials[mid].bt) {
									delete $entity.localdata[ currentOperation ].materials[mid];
									qty++;
								}
							}
						}
					}
				}
			}
		}
		// Clear sync. requests, if any
		if ( mid == null && persist ) // that is, it is clearing all material modifications and not just one material
			clearSyncRequest( currentOperation, userId, $entity.kid, lkid );
		// Reset request saved times, if any
		resetRequestSavedTime( currentOperation, userId, $entity.kid, lkid );
		//delete last entered actual date after trasnaction is sent.
		if ( $entity.localdata[currentOperation] && $entity.localdata[currentOperation].latd)
			delete $entity.localdata[currentOperation].latd;
	}
	// Persist changes
	if ( persist )
		storeEntity( userId, $entity );
	// Clear event data
	var eventParams ={};
	/*
	if (!qty && !keepPartialErrors)
	 qty = 1;
	 */
	if (currentOperation == 'no')
		qty = 1;
	eventParams.qty = qty;	
	if (linkedEntity)
	{
		eventParams.linkedEntity = linkedEntity;
	}

	if (qty != 0)
	 closeEvent($uid,$entity.kid,$currentOperation,eventParams);
}


function isBatchInError(bid, errorMaterial){
	var noOfBatches = 0;;
	if (errorMaterial.bt)
		noOfBatches = errorMaterial.bt.length;
	for (var i = 0; i < noOfBatches; i++) {
		if (errorMaterial.bt[i].bid == bid)
		  return bid;
	}
	return null;
}


function getBatchQty(bid, batches){
	var noOfBatches = 0;;
	if (batches.bt)
		noOfBatches = batches.bt.length;
	for (var i = 0; i < noOfBatches; i++) {
		if (batches.bt[i].bid == bid)
			return batches.bt[i].q;
	}
	return null;
}

// Check if local data exists
function hasLocalModifications( currentOperation, entity, checkForSelectedLinkedEntity ) {
	if ( isOperationReadOnly( currentOperation) )
		return false;
	if ( entity.localdata && !$.isEmptyObject( entity.localdata ) ) {
		var modifiedMaterials = entity.localdata[ currentOperation ];
		if ( modifiedMaterials && modifiedMaterials.materials && !$.isEmptyObject( modifiedMaterials.materials ) ) {
			if ( checkForSelectedLinkedEntity && modifiedMaterials.linkedentity ) {
				return ( modifiedMaterials.materials[ modifiedMaterials.linkedentity.kid ] );
			} else
				return true;
		} else
			return false;
	}
	return false;
}

// Check if local data exists
	function hasLocalModificationsForLinkedEntity( currentOperation, entity, linkedEntityId ) {
		if ( isOperationReadOnly( currentOperation) )
			return false;
		if ( entity.localdata && !$.isEmptyObject( entity.localdata ) ) {
			var modifiedMaterials = entity.localdata[ currentOperation ];
			if ( modifiedMaterials && modifiedMaterials.materials && !$.isEmptyObject( modifiedMaterials.materials ) ) {
				if (modifiedMaterials.linkedentity)
				{
					for (var lkid in modifiedMaterials.materials )
					{
						if ( !modifiedMaterials.materials.hasOwnProperty( lkid ) )
							continue;
						if (!linkedEntityId) // has modifications for related entities
							return true;
						if (lkid == linkedEntityId) // has modifications for linkedEntityId
							return true;
					}
				}
			}
			return false;
		}
	}


function haslocalModificationsForMid(currentOperation,entity,mid, checkLinkedEntity) {
	  if ( isOperationReadOnly( currentOperation) )
		  return false;
	  if ( entity.localdata && !$.isEmptyObject( entity.localdata ) ) {
		 var modifiedMaterials = entity.localdata[ currentOperation ];
		 if ( modifiedMaterials && modifiedMaterials.materials && !$.isEmptyObject( modifiedMaterials.materials ) ) {
			 if (modifiedMaterials.linkedentity) {
				 for (var lkid in modifiedMaterials.materials) {
					 if (!modifiedMaterials.materials.hasOwnProperty(lkid))
						 continue;
					 for (var materialId in modifiedMaterials.materials[lkid]) {
						 if (!modifiedMaterials.materials[lkid].hasOwnProperty(materialId))
							 continue;
						 var selectedLinkedEntity = $entity.localdata[$currentOperation].linkedentity.kid;
						 if (checkLinkedEntity) {
							 if ((materialId == mid) && (selectedLinkedEntity != lkid ))
								 return true;
						 }
						 else {
							 if (materialId == mid && (selectedLinkedEntity == lkid ))
								 return true;
						 }
					 }
				 }
			 }
			 else if (modifiedMaterials.materials && !checkLinkedEntity) { // no linked entity selected (skip)
				 for (var materialId in modifiedMaterials.materials) {
					 if (!modifiedMaterials.materials.hasOwnProperty(materialId))
						 continue;
					 if (materialId == mid)
						 return true;
				 }

			 }
		 }
			return false;
	  }
}

// Clear local modifications
function deleteLocalNewBatchData( userId, currentOperation, mid,bid,qty ) {
	if ( !$entity.localdata )
		return;
	var qty = 0;
	var deleteevent = false;
	// Remove relevant op data
	var materialModifications = $entity.localdata[ currentOperation ];
	if ( materialModifications ) {
		var linkedEntity = materialModifications.linkedentity;
		if ( linkedEntity ) {
			if ( $entity.localdata[ currentOperation ].materials && $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ] ) {
				if ( mid != null && bid != null) {
					if ( $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ][mid] ) {
						var nb = 0;
						var m = $entity.localdata[currentOperation].materials[linkedEntity.kid][mid];
						if (m && m.bt) {
							nb = m.bt.length;
							for ( var i = 0; i < nb; i++) {
								if (m.bt[i].bid == bid) {
									var newq = m.q - m.bt[i].q;
									if (newq < 0)
										newq = 0;
									m.q = newq;
									m.bt.splice(i, 1);
									break;
								}
							}
							if (m.bt.length == 0) {
								delete $entity.localdata[currentOperation].materials[linkedEntity.kid][mid];
								deleteevent = true;
							}
						}
					}
				} else if (bid == null){
					delete $entity.localdata[ currentOperation ].materials[ linkedEntity.kid][mid];
					deleteevent = true;
				}
				// If the materials object is empty, then remove this operation's object completely
				if ( $.isEmptyObject( $entity.localdata[ currentOperation ].materials[ linkedEntity.kid ] ) ) {
					delete $entity.localdata[currentOperation].materials[linkedEntity.kid];
					deleteevent = true;
				}
			}

		} else {
			if ( $entity.localdata[ currentOperation ].materials ) {
				if ( mid != null && bid !=null ) {
					if ( $entity.localdata[ currentOperation ].materials[mid] ) {
						var nb = 0;
						var m = $entity.localdata[ currentOperation ].materials[mid];
						if ( m && m.bt ) {
							nb = m.bt.length;
							for (var i = 0; i < nb; i++) {
								if (m.bt[i].bid == bid) {
									var newq = m.q - m.bt[i].q;
									if (newq < 0)
										newq = 0;
									m.q = newq;
									m.bt.splice(i, 1);
									break;
								}
							}
							if (m.bt.length == 0) { /// $.isEmptyObject( m.bt ) ) {
								delete $entity.localdata[currentOperation].materials[mid];
								deleteevent = true;
							}
						}
					}
				} else if (bid == null) {
					delete $entity.localdata[ currentOperation ].materials[mid];
					deleteevent = true;
				}
				// If the materials object is empty, then remove this operation's object completely
				if ( $.isEmptyObject( $entity.localdata[ currentOperation ].materials ) ) {
					delete $entity.localdata[currentOperation];
                    deleteevent = true;
				}
			}
		}
	}
	storeEntity( userId, $entity );
	//clear event data
	//var eventType = getEventType(currentOperation);
	var eventParams ={};
	if (!qty)
		qty = 1;
	eventParams.qty = qty;
	if (linkedEntity)
	{
		eventParams.linkedEntity = linkedEntity;
	}
	if (deleteevent)
		closeEvent($uid,$entity.kid,$currentOperation,eventParams);
}



// Generic function to store and retrieve JSON objects
function storeObject( id, o ) {
	var s = o;
	if ( o != null ) {
		s = JSON.stringify( o );
		localStorage.setItem( id, s );
	} else {
		localStorage.removeItem( id );
	}
}

function getObject( id ) {
	var s = localStorage.getItem( id );
	if ( s != null )
		return JSON.parse( s );
	return null;
}

function removeObject( id ) {
	localStorage.removeItem( id );
}

//Store entity
function removeEntity( userId, kid ) {
	var key = 'k.' + userId + '.' + kid;
	localStorage.removeItem(key);
}

function clearStorage() {
	//since the local media associations should not be cleared, we need to enumerate the keys and only delete non media ones
    ///var key;
   // for (var i = 0, len = localStorage.length; i < len; i++) {
     //   key = localStorage.key(i);
	for (var key in localStorage){
        if ( !(/^media/).test(key) && !(/^trans/).test(key) && !(/^gparams/).test(key) &&  !(/^userlanguage/).test(key)) {
            localStorage.removeItem(key);
        }
    }
    //Remove events stored
    ///key = 'e.'+$uid;
    localStorage.removeItem( 'e.' + $uid );
}

// Clear local data
function clearLocalData() {
	clearStorage();
	clearSession();
}



// Functions to store/retrieve non-JSON objects (string, int, etc.)
function storeString( id, s ) {
	localStorage.setItem( id, s );
}
function getString( id ) {
	return localStorage.getItem( id );
}

// Functions to get/set into a session
function storeStringSession( id, value ) {
	sessionStorage.setItem( id, value );
}

function getStringSession( id ) {
	return sessionStorage.getItem( id );
}

function clearSession() {
	// Clear session storage
	sessionStorage.clear();
	// Clear global variables
	$currentOperation = $entity = $materialData = $config = $role = null;
	$hasMultipleEntities = false;
	$searchIndex = {};
	$osearch = {};
	resetRefreshedStates();
}


/////////////// Utilities ///////////////
// Get the date/time, given a string of format yyyy/MM/dd hh:mm:ss
function getDateFromStr( dateStr ) {
	if (!dateStr)
		return dateStr;
	var dateTime = dateStr.split(' ');
	var dateParts = dateTime[0].split('/');
	var today = new Date();
	var thisYear = today.getFullYear();
	if ( dateParts[2].length == 2){
		if (thisYear)
			dateParts[2] = thisYear.toString().substr(0,2)+dateParts[2];
	}
	var date = new Date( dateParts[2], dateParts[1] - 1, dateParts[0] );
	if ( dateTime.length > 1 ) {
		var timeParts = dateTime[1].split(':');
		if ( timeParts.length >= 1 )
			date.setHours( timeParts[0] );
		if ( timeParts.length >=2 )
			date.setMinutes( timeParts[1] );
		if ( timeParts.length == 3 )
			date.setSeconds( timeParts[2] );
	}
	return date;
}

// Get a formatted date as string
function getStrFromDate( date ) {
	var str = date.getFullYear() + '/' + (date.getMonth()+1) + '/' + date.getDate() + ' ' + date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();

	return str;
}

function getFormattedDate(date) {
	var userData = getUserData($uid);
	var country = userData.cn;
	//var str = date.toLocaleString(country).replace(/:\d{2}\s/,' ');
	var h = date.getHours();
	var ampm = h >= 12 ? 'PM' : 'AM';
	var newh = h >12 ? h-12: h;
	var t = [ newh,addZero(date.getMinutes()), addZero(date.getSeconds())];
	var str = date.toLocaleDateString(country)+ " " + t.join(":")+' '+ampm;
	
	return str;
}



function getDateOnly(dateStr) {
    var str = dateStr;
	if ( str){
		var idx = str.indexOf(' ');
		if (idx > 0){
			str = str.substr(0,idx);
		}
	}
	return str;
}



//server sends date in 2 digit year format needs to be converted to four digit format for date picker
function getFourDigitDateFormat( dateStr, dateFormat ) {
	if (!dateStr)
		return dateStr;
	var newDateStr = dateStr;
	var dateOnlyStr = getDateOnly(dateStr);
	var dateDelimiter = '/';
	var formatItems=dateFormat.split(dateDelimiter);

	var monthIndex=formatItems.indexOf("m");
	var dayIndex=formatItems.indexOf("d");
	var yearIndex=formatItems.indexOf("yy");

	var dateTime = dateOnlyStr.split(' ');
	var dateParts = dateTime[0].split(dateDelimiter);

	var today = new Date();
	var thisYear = today.getFullYear();
	if (yearIndex != -1 && dateParts[yearIndex].length == 2) {
		if (thisYear)
			dateParts[yearIndex] = thisYear.toString().substr(0, 2) + dateParts[yearIndex];
	}
	if (dayIndex != -1 && dateParts[dayIndex].length == 1) {
		dateParts[dayIndex] = '0' + dateParts[dayIndex];
	}
	if (dayIndex == 0 )
		newDateStr = dateParts[dayIndex] + '/' + dateParts[monthIndex] + '/' +dateParts[yearIndex] ;
	else if (dayIndex == 1)
		newDateStr = dateParts[monthIndex] + '/' + dateParts[dayIndex] + '/' +dateParts[yearIndex] ;
	else if (dayIndex == 2)
		newDateStr = dateParts[yearIndex] + '/' + dateParts[monthIndex] + '/' +dateParts[dayIndex] ;
	return newDateStr;

}

function addZero(i) {
	if (i < 10) {
		i = "0" + i;
	}
	return i;
}


// convert string to date using date format
function getDateWithLocaleFormat(dateStr, dateFormat)
{
	var today = new Date();
	if (dateFormat == null || dateStr == null || dateStr == '')
		return today;
	var dateDelimiter = '/';
	var df = dateFormat;
	var dateOnlyStr = getDateOnly(dateStr);
	var formatItems=df.split(dateDelimiter);
	var dateItems=dateOnlyStr.split(dateDelimiter);
	var monthIndex=formatItems.indexOf("m");
	var dayIndex=formatItems.indexOf("d");
	var yearIndex=formatItems.indexOf("yy");

	var today = new Date();
	var thisYear = today.getFullYear();
	if (yearIndex != -1 && dateItems[yearIndex].length == 2) {
		if (thisYear)
			dateItems[yearIndex] = thisYear.toString().substr(0, 2) + dateItems[yearIndex];
	}
	var yearIndex=formatItems.indexOf("yy");
	var month=parseInt(dateItems[monthIndex]);
	month-=1;
	var formattedDate = new Date(dateItems[yearIndex],month,dateItems[dayIndex]);
	if (formattedDate)
		formattedDate.setHours(0,0,0,0);
	return formattedDate;
}

//Date picker format for mobidatepicker
function setDatePickerFormat(dateFormat){
	if (dateFormat == null)
		return null;
	var dateDelimiter = '/';
	var df = dateFormat;
	var formatItems=df.split(dateDelimiter);
	var dayIndex=formatItems.indexOf("d");
	if (dayIndex == 0 )
		$datePickerFormat = 'dd/MM/yyyy';
	else if (dayIndex == 1)
		$datePickerFormat = 'MM/dd/yyyy';
	else if (dayIndex == 2)
		$datePickerFormat = 'yyyy/MM/dd';

}

//server requires dates always is in dd/mm/yyyy format
function getDateStrInServerFormat(dateStr, dateFormat){
	if (dateFormat == null || dateStr == null || dateStr == '')
		return dateStr;
	var newDateStr = dateStr
	var dateDelimiter = '/';
	var df = dateFormat;
	var formatItems=df.split(dateDelimiter);
	var dateItems=dateStr.split(dateDelimiter);
	var monthIndex=formatItems.indexOf("m");
	var dayIndex=formatItems.indexOf("d");
	var yearIndex=formatItems.indexOf("yy");
	var year = parseInt(dateItems[yearIndex]);
	var month=parseInt(dateItems[monthIndex]);
	var day = parseInt(dateItems[dayIndex]);
	var today = new Date();
	var thisYear = today.getFullYear();
	if (year.length == 2) {
		if (thisYear)
			year = thisYear.toString().substr(0, 2) + year;
	}
    if (dayIndex != 0) // else datestring is in correct format
		newDateStr = day + dateDelimiter + month + dateDelimiter + year;

	return newDateStr;
}

// Get formatted time range
function getFormattedTimeRange( timeRange ) {
	if ( timeRange == null || timeRange == '' )
		return null;
	var times = timeRange.split( '-' ); // split start and end time in the range, if any
	var str = times[0];
	if ( times.length == 2 ) {
		var startDate = times[0].split( ' ' );
		var endDate = times[1].split( ' ' );
		if ( ( startDate[0] == endDate[0] ) && endDate.length > 1 ) {
			str += ' - ' + endDate[1];
			if ( endDate.length == 3 )
				str += ' ' + endDate[2];
		} else {
			str += ' - ' + times[1];
		}
	}
	return str;
}


// Get formatted number - if integer, then an integer is returned, else float (as is)
function getFormattedNumber( num ) {
	if ( isInteger( num ) )
		return parseInt( num );
	else
		return num;
}

// Check if the number is an integer or not
function isInteger( num ) {
	return ( num % 1 === 0 );
}

// Get the number of keys in a JSON object
function getObjectSize( o ) {
	var size = 0;
	for ( var key in o ) {
		if ( o.hasOwnProperty(key) )
			size++;
	}
	return size;
}

// Get URL parameters (returns a JSON of URL parameters ) - assumes URL has a ? followed by a query string
function getURLParams( url ) {
	var qs = url.replace( /.*\?/, '' );
	var pairs = qs.split( '&' );
	var ret = {};
	for ( var i = 0; i < pairs.length; i++ ) {
		var nval = pairs[i].split( '=' );
		ret[ nval[0] ] = nval[1];
	}
	return ret;
}

// Validate form fields
function validateFormField( fieldId, type, lowRange, highRange, value, statusId ) {
	if ( !value ) {
		$( '#' + statusId ).show();
		return false;
	}
	if ( type == 'text' ) {
		if ( value.length < lowRange || value.length > highRange ) {
			$( '#' + statusId ).show();
			return false;
		}
		// Special checks
		// Mobile phone
		if ( fieldId == 'mobilephone' && !validateMobilePhone( value ) ) {
			$( '#' + statusId ).show();
			return false;
		}
	}
	return true;
}

function validateCountryCodeOnBlur(value){
	if ( !value ) {
		$( '#mobilephonestatus' ).show();
		return false;
	}
	if (value.length != 2 || isNaN(value)){
		$( '#mobilephonestatus' ).show();
		return false;		
	}
	return true;
}
function validateMobilePhoneOnBlur(value) {
	if ( !value ) {
		$( '#mobilephonestatus' ).show();
		return false;
	}
	if ( value.length < 8 || value.length > 15 ) {
		$( '#mobilephonestatus' ).show();
		return false;
	}
	var formattedPhone = $('#countryCode').val() + ' ' + value;
	if (!validateMobilePhone( formattedPhone ) ) {
		$( '#mobilephonestatus' ).show();
		return false;
	}
	return true;
}

// Validate mobile phone number: <country-code><space><phone-number>
function validateMobilePhone( phoneNumber ) {
	var parts = phoneNumber.split( ' ' );
	return ( parts.length == 2 && !isNaN( parts[0] ) && !isNaN( parts[1] ) );
}

// Round to decimal place
function round( value, decimalPlaces ) {
	var pow = Math.pow( 10, decimalPlaces );
	return Math.round( value * pow ) / pow;
}

/////////////////////////////// Search filter functions //////////////////////////////

// SEARCH FILTER
//This javascript function sets the callback for the list view control's ( specified by id ) filter to "startsWith"
function setListViewFilterCallback( id, callback ) {
	var callbackFunc = callback;
	if ( callback == null )
		callbackFunc = startsWith;
	// Sets the callback to startsWith for the listview specified by id
	var elem = $( '#' + id );
	if ( elem && elem.attr( 'data-filter' ) )
		elem.listview( 'option', 'filterCallback', callbackFunc );
}

//The callback function associated with the materials and entities list view control's filter
//This function shows only items beginning with the search string and hides other items.
function startsWith( text, searchValue ) {
	// only show items that *begin* with the search string
	// A truthy value will result in a hidden list item
	return  text.toLowerCase().substring( 0, searchValue.length ) !== searchValue.toLowerCase();
}
// SEARCH FILTER

// Callback for entities search
function startsWithEntities( text, searchValue ) {
	if ( !$searchIndex.entities || searchValue.length != 1 ) // no search index, or search value has more than 1 character
		return startsWith( text, searchValue ); // do default starts-with search
	// Get all items that start with 'searchValue', and enable them to be added to listview
	// Get the current listview
	var id = 'entities';
	if ( $rtag ) // route entity tag is selected
		id += '_' + getId( $rtag );
	id += '_ul';
	var listview = $( '#' + id );
	updateListviewSearchResults( listview, 'entities', 'kid', searchValue, $rtag );
	return startsWith( text, searchValue );
}

// Callback for materials search
function startsWithMaterials( text, searchValue ) {
	if ( !$searchIndex.materials || searchValue.length != 1 ) // no search index, or search value has more than 1 character
		return startsWith( text, searchValue ); // do default starts-with search
	// Get all items that start with 'searchValue', and enable them to be added to listview
	// Get the current listview
	var id = 'materials';
	if ( $mtag != null )
		id += '_' + getId( $mtag );
	id += '_ul';
	var listview = $( '#' + id );
	updateListviewSearchResults( listview, 'materials', 'mid', searchValue, $mtag );
	return startsWith( text, searchValue );
}

// Callback for add-materials-to-order search
function startsWithAddMaterialsToOrder( text, searchValue ) {
	if ( !$searchIndex.materials || searchValue.length != 1 ) // no search index, or search value has more than 1 character
		return startsWith( text, searchValue ); // do default starts-with search
	// Get all items that start with 'searchValue', and enable them to be added to listview
	// Get the current listview
	var id = 'addmaterialstoorder';
	if ( $mtag != null )
		id += '_' + getId( $mtag );
	id += '_ul';
	var listview = $( '#' + id );
	updateListviewSearchResults( listview, 'addmaterialstoorder', 'mid', searchValue, $mtag );
	return startsWith( text, searchValue );
}

// Callback for related entities search
function startsWithRelatedEntities( text, searchValue ) {
	return startsWithRelationships( 'relatedentities', text, searchValue );
}
// Callback for myentityrelationships search
function startsWithMyEntityRelationships( text, searchValue ) {
	return startsWithRelationships( 'myentityrelationships', text, searchValue );
}

function startsWithRelationships( id, text, searchValue ) {
	// Get relationship type
	var relationType = $currentRelationType; /// getRelationshipTypeForOperation( $currentOperation );
	var key = relationType;
	var hasTag = ( $rtag_re != null && $entitiesData[relationType] && $entitiesData[relationType][$rtag_re] );
	if ( hasTag )
		key += '.' + $rtag_re;
	if ( !$searchIndex.relatedentities || !$searchIndex.relatedentities[ key ] || searchValue.length != 1 ) // no search index, or search value has more than 1 character
		return startsWith( text, searchValue ); // do default starts-with search
	// Get all items that start with 'searchValue', and enable them to be added to listview
	// Get the current listview
	var eId = id;
	if ( hasTag )
		eId += '_' + getId( $rtag_re );
	eId += '_ul';
	var listview = $( '#' + eId );
	updateListviewSearchResults( listview, id, 'lkid', searchValue, relationType );
	return startsWith( text, searchValue );
} 

// Callback for orders search
function startsWithOrders( text, searchValue ) {
	if ( searchValue.length != 1 || !getOidsFromSearchIndex( text, $osearch.oty, $osearch.ost ) ) // no search index, or search value has more than 1 character
		return startsWith( text, searchValue ); // do default starts-with search
	// Get all items that start with 'searchValue', and enable them to be added to listview
	// Get the current listview
	var id = 'orders_' + $osearch.ost + '_ul';
	var listview = $( '#' + id );
	var key = $osearch.oty + '_' + $osearch.ost;
	updateListviewSearchResults( listview, 'orders', 'oid', searchValue, key );
	return startsWith( text, searchValue );
}

// Update the list view with search results obtained from $searchIndex
function updateListviewSearchResults( listview, type, idParamName, searchValue, tag ) {
	// Initialize the list for search once
	if ( !hasAttr( listview, 'searchinit' ) ) {
		// Get the list of ids that starts-with searchValue
		var thisType = type;
		var thisTag = tag;
		if ( type == 'addmaterialstoorder' )
			thisType = 'materials'; // use materials index for addmaterialstoorder
		else if ( type == 'myentityrelationships' )
			thisType = 'relatedentities';
		var hasTag = ( $rtag_re != null && $entitiesData[tag] && $entitiesData[tag][$rtag_re] );
		if ( thisType == 'relatedentities' && hasTag )
			thisTag = tag + '.' + $rtag_re; // if there are route tags under related entities, then re-form the tag for the search index (linkType.rtag)
		var ids = getIdsStartingWith( thisType, searchValue.toLowerCase(), thisTag ); // get all ids starting with searchValue
		if ( !ids )
			return;
		// Ids are found, so we need to append them to the list
		ids = ids.slice(); // make a copy
		var parent = listview.parent(); 
		// Get the relevant URL parameters from the URL of the first element (which is of index 1, given index 0 is list-divider with navigation text/links, i.e. prev/next)
		var listItems = listview.children('li');
		var params = {};
		var url;
		for ( var i = 1; i < listItems.length; i++ ) { // NOTE: ignore the first item which is a nav. area; so starting with 1
			var listAnchor = listItems[i].getElementsByTagName( 'a' );
			if ( listAnchor.length == 0 ) // say, in the case of 'addmaterialstoorders' items already in order are not hyperlinked, and so will not have an '<a>...</a>', but the URL is added as an attribute on the <li url="..."> 
				url = listItems[i].getAttribute( 'url' );
			else
				url = listAnchor[0].href;
			if ( !url ) // just safety check
				continue;
			params = getURLParams( url );
			var idx = $.inArray( params[ idParamName ], ids ); // index of occurance of mid in ids
			if ( params && idx != -1 )
				ids.splice( idx, 1 );
		}
		var offset = 0;
		if ( params && params.o )
			offset = parseInt( params.o );
		// Update the list view with the items starting with searchValue (as found in ids)
		if ( type == 'entities' ) {
			updateEntitiesListview( listview, null, $rtag, offset, ids );
		} else if ( type == 'materials' ) {
			updateMaterialListview( type, listview, tag, offset, null, ids, params.oid, params.oty );
		} else if ( type == 'addmaterialstoorder' ) {
			var excludeMaterialIds = getMidsInOrder( getOrder( params.oid, params.oty ) ); // get the materials Ids already in order to be excluded
			updateMaterialListview( type, listview, tag, offset, excludeMaterialIds, ids, params.oid, params.oty );
		} else if ( type == 'relatedentities' || type == 'myentityrelationships' ) {
			// Get the items page Id
			var linkedPageId = 'materials';
			if ( url ) {
				var endIndex = url.indexOf( '?' );
				var startIndex = url.indexOf( '#' ) + 1;
				linkedPageId = ( endIndex == -1 ? url.substring( startIndex ) : url.substring( startIndex, endIndex ) );
			}
			updateRelatedEntitiesListview( listview, tag, $rtag_re, offset, ids, type, linkedPageId );
		} else if ( type == 'orders' ) {
			updateOrdersListview( listview, params.oty, params.st, offset, ids );
		}
		// Add the searchinit attribute to listview - a CSV of ids that start with searchValue
		var idsCSV = ids.join(',');
		listview.attr( 'searchinit', idsCSV );
		// Hide the nav. area (first list item)
		listItems[0].style.display = 'none';
		// Handle the keyup in the search bar: 
		parent.delegate('input:jqmData(type="search")', 'keyup', function() {
			if ( $(this).val() == '' ) {
				var idsStr = listview.attr( 'searchinit' );
				if ( idsStr ) {
					var ids = idsStr.split(',');
					// Remove the list elements we just added
					for ( var i = 0; i < ids.length; i++ )
						listview.find( '#' + type + '_' + ids[i] ).remove(); // NOTE: all list item ids should follow format <pageId>_<item-identifier>
				}
				// Remove search init attr.
				listview.removeAttr( 'searchinit' );
				// Show the nav. area
				listItems[0].style.display = 'block';
			}
			/*
		    if (listview.children(':visible').not('#no-results').length === 0) {
		        ///$('#no-results').fadeIn(500);
		    	console.log( 'no results');
		    } else {
		        ///$('#no-results').fadeOut(250);
		    	console.log( 'has results' );
		    }
		    */
		});
		// For the clear-text event on the data filter, trigger a keyup which will take care of the item reseting
		parent.find('a.ui-input-clear').off('click').on( 'click', function( e, ui ) {
		    $("input[data-type='search']").val('');
		    $("input[data-type='search']").trigger('keyup');
		});
	} // end if ( !hasAttr(...) )
}

function getEoq(mid, lkid, orderType)
{
    var material = null;
    var eoq = -1;
	if ( lkid && orderType == 'sle'){ //sales order
		if (getRelatedEntityPermissions(lkid, 'csts', $entity.kid) != $NO_PERMISSIONS ) {
			var entity = getEntity($uid, lkid);
			if ( entity.mt) {
				for (var i = 0; i < entity.mt.length; i++) {
					if (entity.mt[i].mid == mid) {
						material = entity.mt[i];
						break;
					}
				}
			}
		}
	}
	else if ( orderType == 'prc')  { //purchase order
		material = $materialData.materialmap[ mid ];
	}
	if(!material)
		return eoq;
    if (parseFloat(material.max) > 0) {
		var itq = 0;
		var min = 0;
		if ( material.itq )
			itq = material.itq;
		if (parseFloat(material.q) < parseFloat(material.max))
			eoq = material.max - material.q - itq ;
		if (eoq < 0)
			eoq = 0;
	}

	if( !material.max  )//no max or eoq defined
	 	eoq = -1; //do not display recommended quantity
    return eoq;
    
   }

function logInventoryTransaction(userId,kioskId,currentOperation,linkedEntityName,localModifications,updInventoryOutput,timestamp)	{
	if (currentOperation == 'no')
		return false;
    var kid = kioskId;
    var transData  = getlocalTransactions( userId, kioskId );
    var updatedMaterials = updInventoryOutput.mt;
	var errors = updInventoryOutput.er;
	var materialMap = $materialData.materialmap;
    var linkedEntity = getSelectedLinkedEntity( $currentOperation, $entity );
    var ln = linkedEntityName;
    var dt = timestamp; // update time of an inventory transaction or order
	if ( !dt )
		dt = getFormattedDate( new Date() );
    var userdata = getUserData(userId);
    var usr = '';
    var orderFulfilled = false;
    if (currentOperation == 'vo') {
		currentOperation = 'er';
		orderFulfilled = true;
    }
    if (userdata.fn)
    	usr = userdata.fn;
    if (userdata.ln)
    	usr += ' '+ userdata.ln;
	
	if  (!transData)
		transData = [];	
	for ( var i = 0; i < updatedMaterials.length; i++ ) {
		var m = updatedMaterials[i];
		if (!m)
			continue;
		var localMaterial = materialMap[ m.mid ];
		var mid = m.mid;
		if ( !localMaterial )
			continue;
		//Check if material is in error list, if yes do not log transaction locally
		var error;
		if ( errors && errors.length != 0 ) {
			for (var j = 0; j < errors.length; j++) {
				var errorMaterial = errors[j];
				if (errorMaterial.mid == mid) {
					error = true;
					break;
				}
			}
		}
		if (error) // do not log transaction
			continue;
		var materialModifications = localModifications[m.mid];
		if ( !materialModifications )
			continue;
	  	var mn = localMaterial.n;
		var os = localMaterial.q;
		var cs = m.q;
		var qty = 0;
		var rsn = materialModifications.reason;
		var mst = materialModifications.mst;
		var atd = materialModifications.atd;
		if (materialModifications)
			qty = materialModifications.q;
		if (orderFulfilled) //view orders - orders fulfilled , create a receipt transaction
		{
			cs = localMaterial.q;
			os = localMaterial.q - qty;
		}
		if (materialModifications.bt) {
			for (j = 0; j < materialModifications.bt.length; j++ ) {
				var bid = materialModifications.bt[j].bid;
				var bexp = materialModifications.bt[j].bexp;
				var bmfnm =materialModifications.bt[j].bmfnm;
				var bmfdt = materialModifications.bt[j].bmfdt;
				qty = materialModifications.bt[j].q;
				var ostkb = getBatchQuantity(materialModifications.bt[j].bid, localMaterial);
				var cstkb = getBatchQuantity(materialModifications.bt[j].bid, m);
				var materialqty = parseFloat(os);
				var batchqty = parseFloat(ostkb);
				var transactionqty = parseFloat(qty);
				if ( materialModifications.bt[j].mst ){
					mst = materialModifications.bt[j].mst;
				}
				cs = getQuantityAfterBatchTransaction(currentOperation,materialqty,batchqty,transactionqty);
				var t = createTransactionObject(currentOperation, usr, dt, mid, mn, os, qty, cs, linkedEntityName,rsn,mst, atd,bid, bexp, bmfnm, bmfdt, ostkb, cstkb);     	// TODO: Batch details can be passed to this function once those are available locally
				transData.unshift(t); // adds to the beginning of the array
				os = cs;
			}
		}
		else {
			var t = createTransactionObject(currentOperation, usr, dt, mid, mn, os, qty, cs, linkedEntityName,rsn,mst,atd);
			transData.unshift(t); // adds to the beginning of the array;
		}
	}
	// Persist
	storelocalTransactions( userId, kioskId, transData );
	return true;
}


// Update location inventory transactions from server response object after refreshing from server
function updateLocalTransactions( o ) {
	var transactions = [];
	if ( o.trn && o.trn.length > 0 ) {
		for ( var i = 0; i < o.trn.length; i++ ) {
			var t = o.trn[i];
			transactions.push( createTransactionObject( getOperationType( t.ty ), t.u, t.t, t.mid, getMaterialName( t.mid ), t.ostk, t.q, t.cstk, t.lknm, t.rsn, t.mst, t.atd,t.bid, t.bexp, t.bmfnm,  t.bmfdt, t.ostkb, t.cstkb ) );
		}
	}
	// Persist transactions (will overwrite what exists)
	storelocalTransactions( $uid, o.kid, transactions );
}


// Create transaction object
function createTransactionObject( operation, userName, timestamp, materialId, materialName, openingStock, quantity, closingStock, linkedEntityName, reason, materialStatus, actualTransactionDate,batchId, batchExpiryDate, manufacturerName, manufacturedDate, openingStockInBatch, closingStockInBatch ) {
	var t = {};
	t.op = operation;
	t.usr = userName;
	t.dt = timestamp;
	if (materialId)
		t.mid = materialId;
	if (materialName)
		t.mn = materialName;
	if (openingStock != null)
		t.os = openingStock;
	if(quantity != null)
		t.qty = quantity;
	if(closingStock != null )
		t.cs= closingStock;
	if ( linkedEntityName )
		t.ln = linkedEntityName;
	if ( batchId )
		t.bid = batchId;
	if ( batchExpiryDate )
		t.bexp = batchExpiryDate;
	if ( manufacturerName )
		t.bmfnm = manufacturerName;
	if ( manufacturedDate )
		t.bmfdt = manufacturedDate;
	if ( openingStockInBatch != null )
		t.ostkb = openingStockInBatch;
	if ( closingStockInBatch != null )
		t.cstkb = closingStockInBatch;
	if (reason)
		t.rsn = reason;
	if (materialStatus)
		t.mst = materialStatus;
	if (actualTransactionDate)
		t.atd = actualTransactionDate;
	return t;
}


function getlocalTransactions( userId, kioskId )
{
	var key = 'trans.'+userId+'.'+kioskId;
	return getObject( key );
}

function storelocalTransactions( userId, kioskId,transData)
{
	// Ensure that the transaction store does not exceed limits; trim/splice it if it does
	spliceTransactions(transData);
	// Persist
	var key = 'trans.' + userId + '.' + kioskId;
	storeObject(key,transData);
}

function spliceTransactions(transData)
{
    if (transData)
    {
    	//Check if there are more than maximum limit of 300 tranasactions
		if (transData.length > $transLimit)
		{
		  	var num = transData.length - $transLimit;
		  	transData.splice( $transLimit, num );
		}
    }
}

function clearLocalTransactionLog() {
         for (var i = 0, len = localStorage.length; i < len; i++) {
             key = localStorage.key(i);
             if ((/^trans/).test(key)) {
                 localStorage.removeItem(key);
             } 
         }
}


function getBatchQuantity(bid,material) {
	var m = material;
	var noOfBatches = 0;
	var qty = 0;
	if (m.bt)
	   noOfBatches = m.bt.length;
	for (var i = 0; i < noOfBatches; i++ ){
		if (bid == m.bt[i].bid ){
			qty = m.bt[i].q;
			return qty;
		}
	}
	return qty;
}


function getQuantityAfterBatchTransaction(currentOperation,materialQty,batchQty,transactionQty) {
	var qty = 0;
	if (currentOperation == 'ei' || currentOperation == 'ts' || currentOperation == 'ew')
		qty = parseFloat(materialQty - transactionQty);
	else if (currentOperation == 'er')
	  qty = parseFloat(materialQty + transactionQty);
	else if (currentOperation == 'es')
	  qty = parseFloat(materialQty -batchQty + transactionQty);

	if (qty < 0)
	 qty = 0;
	return qty;

}

//Render a list of transactions
function renderTransactions (pageId, params, options ) {
	if ( !$entity || $entity == null )
		return;

  var pageOffset = 0;
	if ( params && params.o )
		pageOffset = parseInt( params.o ); // NOTE: has to be int. given it is used for range calculations
	var backPageOffset = 0; // offset for the back page (esp. if it is entities or related entities)
	if ( params && params.bo )
		backPageOffset = params.bo; // NOTE: can be in string form, given it is used as back URL param only
	// Get page
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Update the header with the title and number of entities
	var currentOperation = 'vs';
	var headerTxt =  getOperationName( 'vt' );
	header.find( 'h3' ).empty().append( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update header title
	setTitleBar( header, $entity.n, false );
	// Update header nav. buttons
	var prevPageId = getPrevPageId( options ); 
	if ( prevPageId != null ) {
		if ( prevPageId == '#menu' ) {
			var backUrl = prevPageId + '?o=' + backPageOffset;
			header.find( pageId + 'back' ).attr( 'href', backUrl );
			$mtag = null;
		}
	}
	// Disable print transactions on mobile devices or if print inventory in configuration is disabled
	if ( isOperationDisabled( 'pi' ) || isDevice() ) {
		page.find('#transactionsPrint').hide();
		page.find('#listItemPrintTransactions').hide();
	}
	else {
		page.find('#transactionsPrint').show();
		page.find('#listItemPrintTransactions').show();
	}

	// Update footer
	var footerMarkup = '<a href="#" data-role="button" data-mini="false" data-icon="refresh" onclick="refreshTransactions();">'+$buttontext.refresh+'</a>';
	footer.empty().append( footerMarkup );

	// Update the material list, given page is now created
	var transText;

    var markup = '';
    var listId = 'transactions_ul';
	content.empty();
	markup = '<ul id="' + listId + '" data-role="listview" data-theme="d" data-divider-theme="d" ></ul>';
	var translistview = $(markup);
	content.append( translistview);
	// Enhance the page
	page.page();
	page.trigger( 'create' );
	// Update content
	if (updateTransactionsListview('transactions', translistview, pageOffset )) // returns a collapsible-set
		content.append( translistview );
	else
		content.empty().append( $messagetext.notransactions );

	// Now call changePage() and tell it to switch to the page we just modified.k
	if ( options ) {
		options.dataUrl = '#transactions?o=' + pageOffset;		
		$.mobile.changePage( page, options );
		
	} else
		$.mobile.changePage( page );
	header.find( '#transactionsback .ui-btn-text').text($buttontext.back);
	header.find( '#transactionsoptions .ui-btn-text').text($buttontext.options);
	page.find( '#transactionsHome').text($buttontext.home);
	page.find( '#transactionsPrint').text($buttontext.print);
	page.find( '#transactionsExport').text($buttontext.exporttext);
}

// Update transactions list view
function updateTransactionsListview( id,listview, pageOffset ) {
	// Append the navigation bar
	var transData  = getlocalTransactions( $uid, $entity.kid );
	var size = ( transData ? transData.length : 0 );
	// Set the listview navigation bar
	if ( size == 0 ) {
		return false;
	}
	else
	{
		// Set the listview navigation bar
		var range = setListviewNavBar( listview, pageOffset, size, null, function() {
			updateTransactionsListview( id, listview, (pageOffset-1) );
		}, function() {
			updateTransactionsListview( id, listview, (pageOffset+1) );
		} );
		var start = range.start - 1;
		var end = range.end;
		var offset = pageOffset;
		// Get the list of orders for rendering on this page
		var markup = '';
		var transText='';
		var batchMgmtDisabledInKiosk = ( $entity.dbm && $entity.dbm == 'true' );
		for ( var i = start; i < end; i++ ) {
			var trans = transData[i];
			var transText='';
			var verb = '';
			var currentOperation = trans.op;
			var linkedEntityType = '';
			if (currentOperation == 'ei' || currentOperation =='ts')
				linkedEntityType = $labeltext.customer;
			else if (currentOperation == 'er')
				linkedEntityType = $labeltext.vendor;
			else
				verb = ' ';
			if ( !trans.ln || trans.ln == '' )
				verb ='';
			var id= 'transactions_'+trans.mid+'_'+i;
			transText += '<font style="font-size:11pt;font-weight:normal;color:black">'+ getFormattedNumber( trans.qty ) +'</font>'+' ';
			transText += '<font style="font-size:11pt;font-weight:bold">'+ trans.mn + '</font>';
			transText += '<font style="font-size:11pt;font-weight:normal">'+', '  + $tPMsg[currentOperation] + ',</font>' + ' ';
			transText += '<font style="font-size:9pt;font-weight:normal"> '+ trans.dt + '</font>'+' ';
			// Add a line for batch details, if any
			if ( trans.bid ) { // showing transactions indendepent of whether batch enabled/disabled, so that batch received info. can be view
				transText += '<br/><font style="font-size:9pt;font-weight:normal;color:gray">';
				transText += $labeltext.batch + ': ' + trans.bid; // batch ID
				transText += ', ' + $labeltext.batchexpirydate + ': ' + ( trans.bexp ? trans.bexp : $labeltext.unknown ); // batch expiry
				transText += ', ' + $labeltext.manufacturedby + ': ' + ( trans.bmfnm ? trans.bmfnm : $labeltext.unknown ); // manufacturer
				transText += '</font>';
			}
			// Add user
			if (trans.usr)
				transText += '<br/><font style="font-size:9pt;font-weight:normal">' + $labeltext.by + ' ' +  trans.usr + '</font>';
			// Add reason/status, if present
			var reasonStatusText = getReasonStatusText(trans.rsn,trans.mst);
			if (reasonStatusText !=  '') {
				transText += '<font style="font-size:9pt;font-weight:normal">' + ( trans.usr ? ', ' : '</br>' );
				transText += reasonStatusText + '</font>';
			}
			// Add a line for opening stock and closing stock
			var os = ( transData[i].os ? transData[i].os : 0 );
			transText += '<br/><font style="font-size:9pt;font-weight:normal;color:darkslategray">' + $labeltext.openingstock + ': ' + getFormattedNumber(os) + ',</font>';
			transText += '<font style="font-size:9pt;font-weight:normal;color:darkslategray"> ' + $labeltext.closingstock + ': '+ getFormattedNumber(transData[i].cs) + '</font>';
			// Show customer/vendor details
			if ( trans.ln )
				transText += '<br/><font style="font-size:9pt;font-weight:normal;color:darkslategray">' + linkedEntityType + ': ' + trans.ln + '</font>';
			if (trans.atd)
				transText += '<br/><font style="font-size:9pt;font-weight:normal;color:darkslategray">'+ getATDText(currentOperation) + ': ' + trans.atd + '</font>';
			// Update markup
			markup +=' <li id="' + id + '">'+transText+'</li>';
		}
		// Append item to listview
		listview.append( $( markup ) );
	}
	// Refresh the view
	listview.listview('refresh');
	return true;
}

function renderPrintTransactions( pageId, params, options ) {
	if ( !$entity || $entity == null )
		return;
	var transData  = getlocalTransactions( $uid, $entity.kid );
	if (transData.length == 0 )
		return;
	var page = $( pageId );
	// Get the page header and content
	var content = page.children( ':jqmData(role=content)' );
	// Page text
	var text = '<a href="javascript:history.go(-1)" style="font-size:small">'+$buttontext.back+'</a><p/>';
	text += '<font style="font-size:14pt;font-weight:bold"> '+$labeltext.transactions +' - '+ $entity.n + '</font><br/>';
	text += '<font style="font-size:small">' + getFormattedDate(new Date())+ '</font><p/>';
	// Need to build the table and append
	var transactionsTable = '<table class="printtable" data-role="table" id="printTransactionsTable" data-mode="reflow">\
						<thead>\
					    <tr>\
					      	<th>#</th>\
	                    	<th>'+$labeltext.name+'</th>\
					      	<th>'+$labeltext.openingstock+'</th>\
			                <th>'+$labeltext.transaction+'</th>\
					      	<th>'+$labeltext.stockonhand+'</th>\
	      					<th>'+$labeltext.closingstock+'</th>\
	                     	<th>'+$labeltext.date+'</th>\
					    </tr>\
					    \
					  </thead>\
					  <tbody>';
	
	var tableRows = '';
	var num = 0;
	for ( var i = 0; i < transData.length; i++ ) {
		var tableRow = '<tr>';
		tableRow += '<td>' + Number(num+1) + '</td>';
		tableRow += '<td>' + transData[i].mn + '</td>';
		var os = ( transData[i].os ? transData[i].os : 0 );
		tableRow += '<td>' + getFormattedNumber( os ) + '</td>';
		tableRow += '<td>' +$tPMsg[transData[i].op];
		if ( transData[i].ln && transData[i].ln != '')
			tableRow += '<br/>('+ transData[i].ln+')';
		tableRow += '</td>';
		tableRow += '<td>' + getFormattedNumber( transData[i].qty ) + '</td>';
		tableRow += '<td>' + getFormattedNumber( transData[i].cs ) + '</td>';
		tableRow += '<td>' + transData[i].dt + '</td>';
		tableRow += '</tr>';
		tableRows += tableRow;
		num++;
	}
	transactionsTable += tableRows;
	transactionsTable += '</tbody></table>';
	// Update DOM
	content.empty();
	content.append(text);
	content.append(transactionsTable);
	// Change page
	if ( options ) {
		// Now call changePage() and tell it to switch to the page we just modified.
		$.mobile.changePage( page, options );
	} else
		$.mobile.changePage( page );
}


//Render feedbackpage
function renderFeedbackPage (pageId, params, options) {
	// Get page
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Update header nav. buttons
	var prevPageId = getPrevPageId( options );

	$.mobile.changePage( page );
	header.find( 'h3' ).empty().append( $labeltext.feedback);
	header.find( '#fbbackbutton .ui-btn-text').text($buttontext.back);
	//page.find('#feedbacktopic').attr("placeholder", $labeltext.topic);
	page.find('#feedbacktext1').text($messagetext.pleaseenterfeedback);
	page.find('#feedbackmessage').attr("placeholder", $labeltext.feedbacktext);
	page.find('#feedbacktext2').html( '<font style="color:black;font-size:9pt;">' + $messagetext.feedbacknote + '</font>' );
	page.find('#fbsendbutton').prop('disabled', true).addClass('ui-disabled');
	page.find( '#fbsendbutton .ui-btn-text').text($buttontext.send);
	page.find( '#fbcancelbutton .ui-btn-text').text($buttontext.cancel);
	if ( prevPageId != null ) {
		page.find('#fbbackbutton').attr("href",  prevPageId  );
		page.find('#fbcancelbutton').attr("href", prevPageId   );
		var onclick = 'processSendFeedback( "'+ prevPageId + '")';
		page.find('#fbsendbutton').attr("onclick", onclick);
	}
	//clear title and topic
	page.find('#feedbackmessage').val('');
	//page.find('#feedbacktopic').val('')
}



//Render forgotpassword
function renderForgotPasswordPage (pageId, params, options) {
	// Get page
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	// Update header nav. buttons
	var prevPageId = getPrevPageId( options );

	$.mobile.changePage( page );
	var header = page.children( ':jqmData(role=header)' );
	header.find( 'h3' ).empty().append( $labeltext.forgotpassword);
	header.find( '#forgotpasswordback .ui-btn-text').text($buttontext.back);
	page.find('#puserid').attr("placeholder", $messagetext.enterusername);
	var userName = $uid;
	if (!userName)
		userName = getLastUser();
	var userNameEntered = page.find('#puserid').val();
	if (userName && !userNameEntered)
		page.find('#puserid').val(userName);
	page.find('#otpfield').val('');
	page.find('#forgotpasswordmessagetext').text($messagetext.pleaseenteryourusernameandsend);
	if (params && params.otp)
		page.find('#fpsendotpbutton .ui-btn-text').text($buttontext.resendOTP);
	else
		page.find('#fpsendotpbutton .ui-btn-text').text($buttontext.sendOTP);
	page.find('#fpcancelbutton .ui-btn-text').text($buttontext.cancel);
    page.find('#generateOTPmessage').text($messagetext.generateotpmessage);
	page.find('#enterOTPmessage').text($messagetext.enterotpmessage);
	page.find('#fpresetpassword .ui-btn-text').text($buttontext.resetpassword);
	page.find('#fpcancelbutton1 .ui-btn-text').text($buttontext.cancel);
	page.find('#otpfield').attr("placeholder", $messagetext.enterotp);
	if ( prevPageId != null ) {
		page.find('#fpbackbutton').attr("href",  prevPageId  );
		page.find('#fpcancelbutton').attr("href", prevPageId   );
	}
}


loadLanguage = function (localeLanguage,fn) {
    storeUserLanguage(localeLanguage);
    var fileref = document.createElement("script");
    fileref.onload = fn;
    var lang = localeLanguage.substring(0,2);
    fileref.setAttribute("type","text/javascript");
    if(lang == "en")
    {
        fileref.setAttribute("src", "res/logistimoen.js");
    }
    else if (lang  == "hi")
    {

        fileref.setAttribute("src", "res/logistimohi.js");
    }
    else 
    {
        //default language
	 	fileref.setAttribute("src", "res/logistimoen.js");
    }
   	// alert(fileref.type);
    if (typeof fileref!="undefined"){
	   fileref.async = true;
	   document.getElementsByTagName("head")[0].appendChild(fileref);
    }
    else
        alert("file undefined");
};


function isDevice() {
    var app = document.URL.indexOf( 'http://' ) === -1 && document.URL.indexOf( 'https://' ) === -1;
    if ( app ) {
        return true;
    } else {
       return false;
    }
}

function initializeLoginPage(localeLanguage){
    $("#login").find( '#loginbutton .ui-btn-text').text($buttontext.login);
    $("#login").find( '#welcometext').text($labeltext.welcome);
    $("#login").find( '#loginmessagetext').text($messagetext.pleasenteryourusernameandlogin);
    $("#login").find('#userid').attr("placeholder", $messagetext.enterusername);
    $("#login").find('#pwd').attr("placeholder", $messagetext.enterpassword);
	$("#login").find( '#emailtext').attr("alt",$labeltext.email);
	$("#login").find( '#calltext').attr("alt",$labeltext.call);
	$("#login").find( '#needhelptext').text($labeltext.needhelptext);
	$("#login").find( '#forgotpasswordtext').text($labeltext.forgotpassword+'?');
	$("#login").find( '#feedbacktext').text($labeltext.feedback);

	if ( !isLoginAsReconnect() ) {
		$("#login").find('#refreshtext').text($messagetext.refreshlogintext);
		$("#login").find('#refreshbutton .ui-btn-text').text($buttontext.refresh);
	}

	// Update support information if such info. is cached locally
	updateSupportInformation( '#login' );

	var markup='';
	if (( $("#login").find("#selectlanguage").length)==0)
	{
		markup += '<div id = "selectlanguage" data-role="fieldcontain" align="right">';
		markup += '<select name="languagename" id="languagename">';
        markup += '<option value="en-US">English</option>';
        markup += '<option value="hi">Hindi</option>';
        markup += '</select></div>';
        $("#login").find("#loginmessagetext").before(markup);
        $("#login").find('select[name^="languagename"] option[value="'+localeLanguage+'"]').attr("selected","selected");
	}
}


function updateSupportInformation( Url ) {
	// Get last user
	var userId = getLastUser();
	//update support phone and email from config
	var supportInfo = getSupportInfo( userId );
	if (supportInfo)
	{
		// NOTE: The support phone/email click is now handled via a JS function on those HTML elements (wherever they occur, e.g. home, login, etc.)
		if (supportInfo.contactname && supportInfo.contactname != '')
			$(Url).find( '#contactname').text( supportInfo.contactname );
	}
}


function checkAppVersion() {
	try {
		if (!$config)
			return;
		var newVersion;
		if ($config.aupg)
			newVersion = $config.aupg.v;
		var idx = 0;
		idx =  $version.lastIndexOf(".");
		var currentVersion = $version.substring(0,idx);
		if (newVersion)
			newVersion = parseInt(newVersion.replace(/\./g, ''));
		currentVersion = parseInt(currentVersion.replace(/\./g, ''));
		if (newVersion > currentVersion) {
			var dueDate = $config.aupg.t;
			var noOfDays;
			var today = new Date();
			var diffDate = dueDate - today.getTime();
			var days = Math.floor(((diffDate % 31536000000) % 2628000000) / 86400000) + 1;
			if (days < 0)
				days = 0;
			if (dueDate)
				dueDate = parseFloat(dueDate);
			var dueDateStr = new Date(dueDate);
			//dueDateStr = dueDateStr.toLocaleDateString(country);
			var msg = $messagetext.newversionofappavailable;
			msg =  msg.replace('X', days);
			if (days == 0){
				msg = $messagetext.upgradeappnow;
				logout();
				toast(msg);
			}
			else {
				toast(msg);
				return;
			}
		}
	} catch ( e ) {
		logR( 'e', 'checkAppVersion', e.message, e.stack );
	}
}



// Get the Basic authentication header
function getBasicAuthenticationHeader() {
	var header = {};
	header[ AUTHORIZATION ] = 'Basic ' + btoa( $uid + ':' + $pwd );
	return header;
}

// Has authentication token
function hasAuthenticationToken() {
	var loginData = getLoginData( $uid );
	return ( loginData && loginData.authToken );
}

// Get a valid authentication token; if invalid, nothing is returned
function getAuthenticationToken() {
	var loginData = getLoginData( $uid );
	if ( !loginData || !loginData.authToken )
		return null;
	// Check the expiry of the token
	var expiryTime = getAdjustedTokenExpiry( loginData.authToken );
	var isValid =  ( expiryTime ? ( new Date().getTime() < expiryTime ) : false );
	if ( isValid )
		return loginData.authToken.token;
	return null;
}

// Get adjusted expiry time - if expiry duration is > 10 days, then expire 1% of the time earlier to facilitate background sync)
function getAdjustedTokenExpiry( authToken ) {
	if ( !authToken )
		return null;
	var expiry = authToken.expiry;
	if ( !expiry || !isDevice() || isLoginAsReconnect() )
		return expiry;
	var acquiredOn = authToken.acquiredOn;
	if ( !acquiredOn )
		return expiry;
	var numDays = Math.round( ( expiry - acquiredOn ) / ( 24 * 60 * 60 * 1000 ));
	if ( numDays >= 10 )
		return ( expiry - ( Math.round( 0.1 * numDays ) * ( 24 * 60 * 60 * 1000 ) ) );
	else
		return expiry;
}


// Get authentication headers
function getAuthenticationHeader() {
	var authHeaders = {};
	if ( hasAuthenticationToken() )
		authHeaders[ X_ACCESS_TOKEN ] = getAuthenticationToken();
	else
		authHeaders = getBasicAuthenticationHeader();
	return authHeaders;
}

// Retrieve the authentication token and expiry from the response header
function getAuthTokenFromResponse( xhr ) {
	if ( !xhr )
		return null;
	var token = xhr.getResponseHeader( X_ACCESS_TOKEN );
	var expiry = xhr.getResponseHeader( EXPIRES );
	if ( !token || !expiry )
		return null;
	// Return token data
	return {
		token: token,
		expiry: expiry
	}
}

// Array contains
function contains(a, obj) {
	for (var i = 0; i < a.length; i++) {
		if (a[i] === obj) {
			return true;
		}
	}
	return false;
}

// Generic send/request server function, that does a few things: logs request, check whether internet exists, whether auth. token is valid, and writes to sync. queue or actually sends to server
// Input formats are as follows:
//	request = {
//		url: url,
//		type: type, // 'GET' or 'POST' or 'DELETE'
//		data: postData, // JSON (key-value pair)
//		headers: headers, // JSON (optional)
//		cache: false, // (optional) true or false
//		dataType: response-type, // (optional) defaults to 'json'
//		success: function( response, status, xhr ) {...},	// mandatory success callback function which can take 3 params: response, status, xhr
//		error: function( response ),		// optional, error callback function with parameter response object
//		sync: {				 // (optional) if this request needs offline sync-ing.
//			op: currentOperation
//			uid: userId,
//			kid: kioskId,
//			oid: additional object ID, (optional: for syncing, as needed - e.g. linked-entity Id for inv. transaction, order-id for order update, and so on)
//			params: additional params for syncing (optional: as needed, e.g. order type, or linked entity name, and so on)
//			callback: callback (optional: callback function to call after sync request)
//		}
//  }
//
// NOTE: If logData are given, it should include 'functionName' and 'message' keys. This function appends the network type to each log message along with a ','. So, it is
//		 preferable that log messages do NOT contain a comma (or a quoted, if they do) for easier server side processing later.
function requestServer( request, loadingMessage, logData, includeAuthToken, logoutOnAuthFailure ) {
	try {
		if (!request || !request.url || !request.success) {
			console.log('requestServer: ERROR: No request or related parameters provided');
			return false;
		}
		if (!request.type)
			request.type = 'GET'; // default to GET
		if (request.type == 'POST' && !request.data) {
			console.log('requestServer: ERROR: No post data provided');
			return false;
		}

		// Log request, if needed and if device
		if (isDevice() && logData && logData.functionName) {
			var logMsg = 'msg:' + ( logData.message ? logData.message : '' ) + ';net:' + getNetworkType();
			logR('i', logData.functionName, logMsg, null);
		}

		// Authenticate user locally, i.e. verify his token is still valid
		if ( hasAuthenticationToken() ) {
			var authToken = getAuthenticationToken();
			var authenticated = ( authToken != null );
			// Check if authentication token is valid
			if ( includeAuthToken && !authenticated) { // auth. token has expired
				logR('i', 'requestServer', 'Invalid token: expired, uid: ' + $uid, null);
				if (logoutOnAuthFailure) {
					logout();
					toast($messagetext.loginsessionhasexpired);
				}
				return false;
			}
		}

		// Prepare request headers
		var headers = request.headers;
		// Get auth. token header
		var authHeader = getAuthenticationHeader();
		if ( !headers ) {
			headers = authHeader;
		} else {
			// Update the request header with the auth. header(s)
			for ( var key in authHeader ) {
				if ( !authHeader.hasOwnProperty( key ) )
					continue;
				headers[ key ] = authHeader[ key ];
			}
		}

		// Check if network is available and update sync. data, if not available
		if ( isDevice() && !isLoginAsReconnect() && !hasNetwork() && request.sync ) {
			return createSyncRequest( request, headers, loadingMessage );
		}

		// Show loading message
		if (loadingMessage)
			showLoader(loadingMessage);
		// Add a timestamp to the URL so that it is not cached
		if ( request.type == 'POST' && !request.cache )
			request.url = uniquifyUrl( request.url );
		// Make the call to server
		$.ajax({
			url: request.url,
			type: request.type,
			timeout: REQUEST_TIMEOUT_DEFAULT,
			dataType: ( request.dataType ? request.dataType : 'json' ),
			data: request.data,
			cache: ( request.cache ? request.cache : false ),
			headers: headers,
			success: function (response, status, xhr) {
				if (loadingMessage)
					hideLoader();
				request.success(response, status, xhr);
				logR( 'i', ( logData && logData.functionName ? logData.functionName : 'requestServer' ), 'success', null );
			},
			error: function (response) {
				var functionName = ( logData ? logData.functionName : 'requestServer' );
				logR( 'e', functionName, 'status=' + response.status + ',err=' + response.responseText, null );
				// Hide the loader, if started
				if (loadingMessage)
					hideLoader();
				// Check if the request has to be put into sync. queue, due to network error
				if ( isNetworkTimeoutError( response ) && request.sync && request.sync.syncOnNetworkError ) {
					return createSyncRequest( request, headers, loadingMessage );
				}
				// Check for invalid auth. token response, if any
				var message = handleResponseCodes( response );
				if ( message && loadingMessage )
					toast( message ); // display toast notification
				// Handle error, if error function is given
				if (request.error && loadingMessage) { // show the error message only when any form of loading message is requested; otherwise, assume background job and silent failure
					request.error(response);
				}
			}
		});

		return true;
	} catch ( e ) {
		logR( 'e', 'requestServer', e.message, e.stack );
		return false;
	}
}

// Create a sync request, using request as sent to requestServer() call
function createSyncRequest( request, headers, loadingMessage ) {
	if (!request.sync.op || !request.sync.uid) {
		return false;
	}
	// Sync. request to offline sync. queue
	syncRequest(request.sync.op, request.sync.uid, request.sync.kid, request.sync.oid, request.sync.params, {
		method: request.type,
		url: request.url,
		data: ( request.data ? request.data : {} ),
		headers: headers
	});
	// Given notification about network connection, only if a loadingMessage is given - otherwise, we assume it is a background operation that does not require user notifications
	if (loadingMessage)
		toast($messagetext.nonetworkconnection + ' ' + $messagetext.willsendlater + '...');
	// If sync. callback, then call it
	if (request.sync.callback)
		request.sync.callback();
	return true;
}

// Handle response codes; respond with the error message for display
function handleResponseCodes( response ) {
	var message;
	if ( response.status == 401 ) { // unauthorized
		logout();
		message = $messagetext.loginsessionhasexpired;
	} else if ( response.status == 408 ) { // request timeout
		message = $networkErrMsg;
	} else if ( response.status == 409 ) { // redirect to another server
		return; // if a host switch happens automatically from ajaxSetup (in events.js), when 409 is returned; do nothing and return here
	} else if ( response.status == 500 ) {	// internal server error
		message = $messagetext.didnotreceivecorrectresponse;
	} else if ( response.status == 503 ) {
		message = $messagetext.serviceisdown;	// Service unavailable or is down for maintenance
	} else { // Unable to connect message will be the default, along with server response text
		message = $networkErrMsg;
		if ( response.responseText && response.responseText != '' )
			message += '<br/><br/>' + response.responseText;
	}
	return message;
}

// Check if network error
function isNetworkTimeoutError( response ) {
	return ( response.status == 408 || !( response.status == 401 || response.status == 409 || response.status == 500 || response.status == 503 ) );
}

// Request saved time management
// NOTE: oid is a generic object id (can be linkedKioskId for inventory or order transactions; orderId for order updates; normalized entity name (without spaces or special chars) for new entities; and so on
function getRequestSavedTime( op, uid, kid, oid ) {
	var id = 'req.svtm';
	var obj = getObject( id );
	return ( obj ? obj[ getRequestId( op, uid, kid, oid ) ] : null );
}

function updateRequestSavedTime( op, uid, kid, oid, time ) {
	var id = 'req.svtm';
	var obj = getObject( id );
	if ( !obj )
		obj = {};
	obj[ getRequestId( op, uid, kid, oid ) ] = time;
	storeObject( id, obj );
}

function resetRequestSavedTime( op, uid, kid, oid ) {
	var id = 'req.svtm';
	var obj = getObject( id );
	if ( obj )
		delete obj[ getRequestId( op, uid, kid, oid ) ];
	storeObject( id, obj );
}

function callSupportPhone() {
	var lastUserId = getLastUser();
	var supportInfo = getSupportInfo( lastUserId );
	var phone = ( supportInfo ? supportInfo.phone : $SUPPORT_PHONE_DEFAULT );
	var logMsg = phone + ( supportInfo? ',' + ( supportInfo.contactname ? supportInfo.contactname : '' ) : '' );
	logR( 'i', 'call', logMsg, null );
	window.location.href = 'tel:' + phone;
}

function sendSupportEmail() {
	var lastUserId = getLastUser();
	var supportInfo = getSupportInfo( lastUserId );
	var email = ( supportInfo ? supportInfo.email : $SUPPORT_EMAIL_DEFAULT );
	var logMsg = email + ( supportInfo? ',' + ( supportInfo.contactname ? supportInfo.contactname : '' ) : '' );
	logR( 'i', 'email', logMsg, null );
	window.location.href = 'mailto:' + email;
}

// Get date picker's format based on user date format
function getDatePickerFormat( dateStr, dateFormat ) {
	if (dateFormat == null || dateStr == null || dateStr == '')
		return dateStr;
	var dateDelimiter = '/';
	var dateOnlyStr = getDateOnly(dateStr);
	var parts = dateOnlyStr.split(dateDelimiter );
	if ( !parts || parts.length < 3 )
		return dateStr;
	var formatItems=dateFormat.split(dateDelimiter);
	var monthIndex=formatItems.indexOf("m");
	var dayIndex=formatItems.indexOf("d");
	var yearIndex=formatItems.indexOf("yy");
	var today = new Date();
	var thisYear = today.getFullYear();
	if (yearIndex != -1 && parts[yearIndex].length == 2) {
		if (thisYear)
			parts[yearIndex] = thisYear.toString().substr(0, 2) + parts[yearIndex];
	}
	if (parts[dayIndex].length == 1) {
		parts[dayIndex] = '0' + parts[dayIndex];
	}
	if (parts[monthIndex].length == 1) {
		parts[monthIndex] = '0' + parts[monthIndex];
	}
	return parts[yearIndex] + '-' + parts[monthIndex] + '-' + parts[dayIndex];
}

// Add a random number as parameter to the URL to make it unique (so it is not cached)
function uniquifyUrl( url ) {
	var qIndex = url.indexOf( '?' );
	if ( qIndex == -1 )
		url += '?';
	else if ( qIndex < ( url.length - 1 ) ) // ? is not the last character => there is a query string
		url += '&';
	url += '__=' + new Date().getTime();
	return url;
}

// Check and enable location using the diagnostic plugin
function enableLocationService() {
	if ( !cordova.plugins.diagnostic ) {
		logR( 'w', 'enableLocationService', 'cordova.plugins.diagnostic not initialized', null );
		return;
	}
	try {
		cordova.plugins.diagnostic.isLocationEnabled(function (enabled) {
				if (enabled) {
					// Check authorization (useful for API 23 - Lollipop onwards)
					cordova.plugins.diagnostic.isLocationAuthorized(function (authorized) {
						if (authorized)
							return;
						// Request location authorization, if not authorized
						cordova.plugins.diagnostic.requestLocationAuthorization(function (status) {
							if (status === cordova.plugins.diagnostic.runtimePermissionStatus.GRANTED)
								return;
							else if (status === cordova.plugins.diagnostic.runtimePermissionStatus.NOT_REQUESTED)
								logR('w', 'requestLocationAuthorization', 'status: NOT_REQUESTED', null);
							else if (status === cordova.plugins.diagnostic.runtimePermissionStatus.DENIED)
								logR('w', 'requestLocationAuthorization', 'status: DENIED', null);
							else if (status === cordova.plugins.diagnostic.runtimePermissionStatus.DENIED_ALWAYS)
								logR('w', 'requestLocationAuthorization', 'status: DENIED_ALWAYS', null);
							else
								logR('w', 'requestLocationAuthorization', 'status: UNKNOWN', null);
						}, function (error) {
							logR('e', 'requestLocationAuthorization', error, null);
						});
					}, function (error) {
						logR('e', 'isLocationAuthorized', error, null);
					});
				} else {
					//showDialog('Location', 'Please enable location services', "javascript:cordova.plugins.diagnostic.switchToLocationSettings()");
					showConfirm('Location', $messagetext.enablelocationservice, null, function () {
						cordova.plugins.diagnostic.switchToLocationSettings();
						$.mobile.changePage('#login');
					}, '#login');
				}
			},
			function (error) {
				logR('e', 'enableLocationService', error, null);
			}
		);
	} catch ( e ) {
		logR( 'e', 'enableLocationService', e.message, e.stack );
	}
}