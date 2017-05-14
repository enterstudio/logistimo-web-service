

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

function deleteMedia(id, domainKey) {
    var url = $host + $MEDIA_DELETE_URL + '/' + id;
    var request = {
        url: url,
        type: 'DELETE',
        dataType: 'json',
        cache: false,
        success: function (resp) {
            localStorage.removeItem('media.' + id);
            var localMediaItems = getObject(domainKey + '.mediaItems');
            if (localMediaItems) {
                for (var i = 0; i < localMediaItems.length; i++) {
                    var media = localMediaItems[i];
                    if (media.id == id) {
                        localMediaItems.splice(i, 1);
                        storeObject(domainKey + '.mediaItems', localMediaItems);
                        break;
                    }
                }
            }
            refreshPage();
        },
        error: function( resp ) {
            if ( resp && resp.responseText ) {
                try {
                    var respObj = JSON.parse( resp.responseText );
                    toast( respObj.message );
                } catch ( e ) {
                    logR( 'e', 'deleteMedia', e.message, e.stack );
                }
            }
        }
    };
    // Log data
    var logData = {
        functionName: 'deleteMedia',
        message: ''
    };
    // Request server
    requestServer( request, $buttontext.delete + '...', logData, true, true );
}

function uploadPhoto(domainKey, backUrl) {
    if ( isDevice() && !hasNetwork() ) {
        toast( $networkErrMsg );
        return;
    }
    uploadPhotoRetries( domainKey, backUrl, 0 );
}

function uploadPhotoRetries( domainKey, backUrl, retries ) {
    //since in the end page is refreshed, no need to hide the loader
    if ( $selectedPhotoUri && hasNetwork() ) {
        try {
            showLoader($messagetext.uploadingphoto + '...');
            // Set file upload options
            var options = new FileUploadOptions();
            options.fileKey = 'file';
            options.fileName = $selectedPhotoUri.substr($selectedPhotoUri.lastIndexOf('/') + 1);
            options.mimeType = 'image/jpeg';
            options.params = {};
            options.headers = getAuthenticationHeader();
            // File transfer with progress indiation
            var ft = new FileTransfer();
            ft.onprogress = function (progressEvent) {
                var progText = $messagetext.uploadingphoto + '...';
                var index = 0;
                if (progressEvent.lengthComputable)
                    progText += Math.round(progressEvent.loaded / progressEvent.total) + '%';
                else
                    progText += ++index;
            };
            // Upload file
            var uploadUrl = encodeURI($host + $MEDIA_UPLOAD_URL + '/' + domainKey);
            ft.upload($selectedPhotoUri, uploadUrl, function (response) { // success
                var respObj = getMediaObjectResponse(response.response);
                hideLoader();
                storeString('media.' + respObj.id, $selectedPhotoUri);
                $selectedPhotoUri = null;
                addToLocalMediaItems( domainKey, respObj );
                cleanUpCameraCache();
                refreshPhotos( backUrl );
            }, function (error) { // error
                logR('e', 'uploadPhotoRetries', 'code:' + error.code + ',source:' + error.source + ',target:' + error.target + ', backUrl: ' + backUrl );
                try {
                    if (retries == 0 && hasNetwork()) {
                        setTimeout(function () { // wait and then try again
                            hideLoader();
                            uploadPhotoRetries(domainKey, backUrl, ++retries);
                        }, 1000);
                    } else {
                        hideLoader();
                        toast('There was an error uploading photo. Please try again later.'); // TODO: i18n
                        addToFailedUploads(domainKey, $selectedPhotoUri);
                        $selectedPhotoUri = null;
                        cleanUpCameraCache();
                        refreshPhotos(backUrl);
                    }
                } catch ( e ) {
                    logR( 'e', 'uploadRetries', e.message, e.stack );
                }
            }, options);
        } catch ( e ) {
            console.log( 'ERROR: ' + e.message + ', ' + e.stack );
            logR( 'e', 'uploadPhotoRetries', e.message, e.stack );
        }
    } else {
        console.log( 'ERROR: photo URL not selected' );
        logR( 'e', 'uploadPhotoRetries', 'Unable to select photo URI', null );
    }
}

function addToFailedUploads(domainKey, localPhotoHandle) {
    var failures = getObject(domainKey + '.failedUploads');
    if (!failures){
        failures = [];
    }
    failures.push(localPhotoHandle);
    storeObject(domainKey + '.failedUploads', failures);
}

function addToLocalMediaItems(domainKey, uploadResp ){
    var localMediaItems = getObject(domainKey + '.mediaItems');
    if (!localMediaItems){
        localMediaItems = [];
    }
    var item = {};
    item.id = uploadResp.id; /// uploadResp.id;
    item.servingUrl = uploadResp.servingUrl; /// uploadResp.servingUrl;
    item.domainKey = domainKey; /// uploadResp.domainKey;
    localMediaItems.push(item);
    storeObject(domainKey + '.mediaItems', localMediaItems);
}

// Get the array of media items (file-handles) for a given object
function getNumMediaItems( domainKey ) {
	var mediaItems = getObject(domainKey + '.mediaItems');
	return ( mediaItems ? mediaItems.length : 0 );
}

// Called if something bad happens. 
function onPhotoCaptureFail(message) {
    logR( 'w', 'onPhotoCaptureFail', message, null );
}

function capturePhotoSuccess(imageUri) {
    $selectedPhotoUri = imageUri;
    $.mobile.changePage( '#photoreview' );
}

function capturePhoto( domainKey, name, allowedPhotosLimit ) {
	if ( !$pictureSource ) {
		showDialog( $messagetext.nocamera, $messagetext.nocameradetected, getCurrentUrl() );
		return;
	}
    if (hasPassedAllowedPhotosLimit(domainKey, allowedPhotosLimit)){
        showDialog( $messagetext.limitexceeded, $messagetext.sorryonly+' ' + allowedPhotosLimit + ' '+$labeltext.photos+' '+$messagetext.areallowedtobeuploaded+' '+$messagetext.deleteoneofthem, null );
        return;
    }
    try {
        // Check if camera is enabled and then capture photo (this check is required esp. for Android API 23 - Lollipop onwards)
        enableCamera(function () {
            initPhotoCapture(domainKey, name);
            // Capture the picture/photo
            navigator.camera.getPicture(capturePhotoSuccess, onPhotoCaptureFail, {
                quality: 50,
                allowEdit: false,
                destinationType: $destinationType.FILE_URI
            });
        });
    } catch ( e ) {
        logR( 'e', 'capturePhoto', e.message, e.stack );
    }
}

function hasPassedAllowedPhotosLimit(domainKey, allowedPhotosLimit){
    var mediaItems = getObject(domainKey + '.mediaItems');    
    return (mediaItems && mediaItems.length >= allowedPhotosLimit);
}

function initPhotoCapture( domainKey, name ) {
	$currentPhotoObject = {};
	$currentPhotoObject.domainkey = domainKey;
	$currentPhotoObject.name = name;
}

 // Cordova is ready to be used!
function cameraStateInit() {
    $pictureSource=navigator.camera.PictureSourceType;
    $destinationType=navigator.camera.DestinationType;
}

function fetchMediaItems(domainKey) {
    var url = $host + $MEDIA_METADATA_URL + '/' + domainKey;
    var request = {
        url: url,
        dataType: 'json',
        cache: false,
        success: function( response ) {
            if ( response.items && response.items.length > 0 ) {
                storeObject(domainKey + '.mediaItems', response.items);
                refreshPhotos();
            }
        }
    };
    var logData = {
        functionName: 'fetchMediaItems',
        message: ''
    };
    // Request server
    requestServer( request, null, logData, true, false );
}

function refreshPhotos( backUrl ) {
    if ( backUrl ) {
        $.mobile.changePage(backUrl);
    } else {
        refreshPage();
    }
}

function uploadRetry(domainKey, localPhotoHandle){
    $selectedPhotoUri = localPhotoHandle;
    //remove local photohandle from the list of failed uploads
    var failures = getObject(domainKey + '.failedUploads');
    var index = failures.indexOf(localPhotoHandle);
    failures.splice(index, 1);
    storeObject(domainKey + '.failedUploads', failures);
    $pictureClicked = true;
    uploadPhoto(domainKey, getCurrentUrl());
}

// Cleanup the camera cache
function cleanUpCameraCache() {
    navigator.camera.cleanup();
}

// Get the media object Id
function getMediaObjectResponse( mediaResponse ) {
    if ( !mediaResponse )
        return null;
    try {
        var respObj = JSON.parse( mediaResponse );
        if ( !respObj.items || respObj.items.length == 0 )
            return null;
        return respObj.items[0];
    } catch ( e ) {
        console.log( 'ERROR: getMediaObjectResponse: ' + e.message + ', ' + e.stack );
        return null;
    }
}

// Enable camera permissions
function enableCamera( callback ) {
    if ( !cordova.plugins.diagnostic ) {
        console.log( 'enableLocationService: cordova.plugins.diagnostic not initialized' );
        return;
    }
    cordova.plugins.diagnostic.isCameraAuthorized( function( authorized ) {
        console.log( 'isCameraAuthorized: ' + authorized );
        if ( authorized ) {
            if ( callback )
                callback();
        } else {
            // Request for camera authorization
            cordova.plugins.diagnostic.requestCameraAuthorization( function( status ) {
                if ( status === cordova.plugins.diagnostic.runtimePermissionStatus.GRANTED ) {
                    if ( callback )
                        callback();
                    return;
                } else if ( status === cordova.plugins.diagnostic.runtimePermissionStatus.NOT_REQUESTED ) {
                    console.log('requestLocationAuthorization: status: NOT_REQUESTED');
                } else if ( status === cordova.plugins.diagnostic.runtimePermissionStatus.DENIED ) {
                    console.log('requestLocationAuthorization: status: DENIED');
                } else if ( status === cordova.plugins.diagnostic.runtimePermissionStatus.DENIED_ALWAYS ) {
                    console.log('requestLocationAuthorization: status: DENIED_ALWAYS');
                } else {
                    console.log('requestLocationAuthorization: status: UNKNOWN');
                }
            }, function( error ) {
                console.log( 'requestCameraAuthorization: error: ' + error );
                logR( 'e', 'requestCameraAuthorization', error, null );
            } );
        }
    }, function( error ) {
        console.log( 'isCameraAuthorized: error: ' + error );
        logR( 'e', 'isCameraAuthorized', error, null );
    } );
}