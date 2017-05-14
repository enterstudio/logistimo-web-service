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

/**
 * Functions to synchronize various types of data with server
 */

//////////////////////////////////////////////////////////////////
/// Logging functions
//////////////////////////////////////////////////////////////////


// Remote logging via local file log
// NOTE: logType: i = info, e = error, w = warning; uid = userId; kid = kioskId; msgType: i = inv. transaction, o = order transaction, e = entity update, l = login, m = media upload, sy = sync., n = network
function logR( logType, functionName, message, stackTrace ) {
    if ( !isDevice() )
        return;
    try {
        // Create a log message with timestamp and relevant format
        // Format: timestamp    functionName    message     error/other-code    stackTrace
        var msg = logType + '\t' + new Date().getTime() + '\t' + functionName + '\t' + ( $uid ? $uid : '' ) + '\t' + ( $entity && $entity.kid ? $entity.kid : '' ) + '\t' + message + '\t' + ( stackTrace ? stackTrace : '' );
        if (!$logCache)
            initLogCache();
        // Update log
        $logCache.messages += ( msg + '\n' ); // adding a new line after each message
        $logCache.size += 1;
        // If cache has reached limit, flush it to file, and re-initalize cache
        if ($logCache.size == $LOG_CACHE_MAX_SIZE)
            flushLog(null);
    } catch ( e ) {
        console.log( 'logR: ERROR: ' + e.message + ', ' + e.stack );
    }
}

// Flush the log to file, if any in cache
function flushLog( callback ) {
    if ( !isDevice() )
        return;
    try {
        if (!$logCache || !$logCache.messages || $logCache.messages == '') {
            if (callback)
                callback();
            return;
        }
        if (!$logFile) {
            ///console.log('flushLog: ERROR: No log file found');
            if (callback)
                callback();
            return;
        }
        try {
            // Write to log file
            writeFile($logFile, $logCache.messages, true, callback);
            // Reset cache
            initLogCache();
        } catch (e) {
            console.log('flushLog: ERROR: ' + e.message + ', ' + e.stack);
        }
        if (callback)
            callback();
    } catch ( e ) {
        console.log( 'flushLog: ERROR: ' + e.message + ', ' + e.stack );
    }
}

//////////////// Config. functions ///////////////////

// Write configuration attributes to the config file
function writeSyncConfig( config, headers, syncIntervalsConfig ) {
    if ( !isDevice() )
        return;
    try {
        var configFilePath = cordova.file.dataDirectory + "/config";
        var data = getSyncConfigData( config, headers, syncIntervalsConfig );
        // Initialize log file
        window.resolveLocalFileSystemURL( cordova.file.dataDirectory, function (dirEntry) {
                var fileName = 'config';
                dirEntry.getFile(fileName, {create: true}, function (fileEntry) {
                        try {
                            writeFile(fileEntry, JSON.stringify( data ), false, null);
                        } catch ( e ) {
                            ///console.log( 'writeSyncConfig: ERROR: ' + e.message + ', ' + e.stack );
                            logR( 'e', 'writeSyncConfig', e.message, e.stack );
                        }
                    },
                    fileErrorHandler );
        }, fileErrorHandler );
    } catch ( e ) {
        ///console.log( 'writeSyncConfig: ERROR: ' + e.message + ', ' + e.stack );
        logR( 'e', 'writeSyncConfig', e.message, e.stack );
    }
}


// Get the sync. config data
function getSyncConfigData( config, headers, syncIntervalsConfig ) {
    var language = getUserLanguage();
    var cfg = {
        host: $host,
        headers: headers,
        version: $version,
        fetchSize: $FETCH_SIZE,
        userId: $uid,
        language: language,
        allowOffline: !isLoginAsReconnect(),
        intervals: ( syncIntervalsConfig ? syncIntervalsConfig : $syncConfigDefault ),
        lastConfigTime: new Date().getTime()
    };
    /* DEBUG - defaults for dev.
    if ( !config || !config.sms ) {
        cfg.sms = { /// including gateway properties until enabled on server-side configuration
            gwph: "9246356765", // SMS Country
            gwky: "EVIN",
            sndid: "EVININ"
        }
    } else
    */
    if ( config && config.sms )
        cfg.sms = config.sms;

    return cfg;
}

//////////////////////////////////////////////////////////////////
/// Sync-ing functions
//////////////////////////////////////////////////////////////////

// Write to sync. request file for remote sync-ing
// NOTE: oid is an optional object identifier that will ensure a unique way of identifying the job (e.g. it could be linked entity ID, order ID, and so on); oparams can be any JSON key-value pairs required for processing the response
function syncRequest( op, uid, kid, oid, params, dataObj ) {
    if ( !isDevice() )
        return;
    try {
        ///console.log('syncRequest: op = ' + op + ', uid = ' + uid + ', kid = ' + kid + ', oid = ' + ( oid ? oid : 'none' ));
        // Callback to start network listener after writing to the sync. request file
        var writeCallback = function () {
            // Start network listener
            startNetworkListener();
        };
        // Get the request object
        var req = getSyncRequestObject( op, uid, kid, oid, params, dataObj);
        if (!req)
            return;
        // Write to sync. request file
        updateSyncRequest(req, writeCallback);
    } catch ( e ) {
        ///console.log( 'syncRequest: ERROR: ' + e.message + ', ' + e.stack );
        logR( 'e', 'syncRequest', e.message, e.stack );
    }
}

// Clear a sync. request
function clearSyncRequest( op, uid, kid, oid ) {
    // Get the sync. request ID
    var reqId = getRequestId( op, uid, kid, oid );
    if ( !reqId )
        return;
    // Check if this request exists
    clearSyncData( kid, [ reqId ], false );
}

function initSyncCache() {
    $syncCache = {
        ids: [],
        reqs: {},
        idmap: { // the ID map helps in navigating the requests during reconciliation of responses;
            kiosks: {},     // kiosk specific requests (kid --> [])
            general: []     // general (non-kiosk) specific requests
        }
    };
}

//////////////////////////////////////////////////////////////////
/// Network related functions
//////////////////////////////////////////////////////////////////

// Start network availability listener (use the plugin to start native connectivity broadcast receiver)
function startNetworkListener() {
    if ( !isDevice() || $networkListenerOn )
        return;
    if ( ConnectivityManager ) {
        ConnectivityManager.enableConnectivityChangeReceiver( function() {
                $networkListenerOn = true; // NOTE: This is being reset on app. start and app. resume, just in case, the sync. process had shut off the network listener
                logR( 'i', 'ConnectivityManager.enableConnectivityChangeReceiver', 'Enabled', null );
            },
            function( errMsg ) {
                ///console.log('ConnectivityManager.enableConnectivityChangeReceiver: ERROR: ' + errMsg );
                logR( 'ei', 'ConnectivityManager.enableConnectivityChangeReceiver', errMsg, null );
            }
        );
    }
}

// Stop network availability listener (use the plugin to stop native connectivity broadcast receiver)
function stopNetworkListener() {
    if ( !isDevice() )
        return;
    if ( ConnectivityManager ) {
        ConnectivityManager.disableConnectivityChangeReceiver( function() {
                logR( 'i', 'ConnectivityManager.disableConnectivityChangeReceiver', 'Disabled', null );
                $networkListenerOn = false;
            },
            function( errMsg ){
                ///console.log('ConnectivityManager.disableConnectivityChangeReceiver: ERROR: ' + errMsg );
                logR( 'e', 'ConnectivityManager.disableConnectivityChangeReceiver', errMsg, null );
            }
        );
    }
}

// Check network connection
function hasNetwork() {
    if ( !isDevice() )
        return true;
    try {
        return ( getNetworkType() != Connection.NONE );
    } catch ( e ) {
        logR( 'e', 'hasNetwork', e.message, e.stack );
        return true;
    }
}

// Get the type of network
function getNetworkType() {
    try {
        return navigator.connection.type;
    } catch ( e ) {
        ///console.log( 'getNetworkType: ' + e.message + ', ' + e.stack );
        return 'unknown';
    }
}


//// Alarm registration /////

// Register alarm for periodic updates (e.g. log uploads)
function registerAlarms( forceRegistration ) {
    if ( !isDevice() )
        return;
    if ( !forceRegistration && getGeneralAttr( KEY_ALARM_REGISTERED ) ) {
        return;
    }
    // Get the interval from config
    var interval = getAlarmInterval();
    if ( interval == 0 ) {
        return;
    }
    // Register the alarm for periodic syncing (e.g. of logs)
    if ( !isDevice() || !AlarmRegistrar ) // AlarmRegistrar is a custom cordova plugin
        return;
    AlarmRegistrar.registerAlarm( function() {
            storeGeneralAttr( KEY_ALARM_REGISTERED, true ); // record that an alarm was already registered; so do not register again
        },
        function( errMsg ) {
            ///console.log('AlarmRegistrar.registerAlarm: ERROR: ' + errMsg );
            logR( 'e', 'AlarmRegistrar.registerAlarm', errMsg, null );
        }, interval
    );
}

// Get the interval for the alarms
function getAlarmInterval() {
    // DEBUG
    ///return 3*60*1000;
    // END DEBUG

    if ( !$config ) {
        return 0;
    }
    // Get the log interval (for now)
    var interval = 0;
    try {
        if ( $config.intrvls ) {
            var intervalAppLogs = 0;
            // Get the minimum of intervals to register the timer event
            if ( $config.intrvls.applgs ) // app log upload interval
                intervalAppLogs = parseInt( $config.intrvls.applgs );
            var intervalMdRefresh = 0;
            if ( $config.intrvls.mdrf ) // config refresh interval
                intervalMdRefresh = parseInt( $config.intrvls.mdrf );
            var intervalSmsWait = 0;
            if ( $config.intrvls.smsw ) // SMS wait interval
                intervalSmsWait = parseInt( $config.intrvls.smsw );
            // Accumulate non-zero intervals
            var intervals = [];
            if ( intervalAppLogs > 0 )
                intervals.push( intervalAppLogs );
            if ( intervalMdRefresh > 0 )
                intervals.push( intervalMdRefresh );
            if ( intervalSmsWait > 0 )
                intervals.push( intervalSmsWait );
            if ( intervals.length == 0 )
                return 0;
            if ( intervals.length == 1 )
                return intervals[0];
            // Get the min. interval
            interval = intervals[0];
            for ( var i = 1; i < intervals.length; i++ ) {
                if ( intervals[i] < interval )
                    interval = intervals[i];
            }
            interval *= ( 60*60*1000 ); // millis
        }
    } catch ( e ) {
        logR( 'e', 'getAlarmInterval', e.message, e.stack );
    }
    return interval;
}


////////////////////// File initialization /////////////////////
function initFiles() {
    initSyncData( null );
    initLogFile();
}


////////////////////////////////////////////////////////////////////////////////////////////
// Helper functions
////////////////////////////////////////////////////////////////////////////////////////////

// Initialize log cache
function initLogCache() {
    $logCache = {
        size: 0,
        messages: ''
    }
}

// Initialize the log file
function initLogFile() {
    // Initialize log file
    window.resolveLocalFileSystemURL(cordova.file.dataDirectory, function (dir) {
        dir.getDirectory('logs', {create: true}, function( dirEntry ) {
            var fileName = 'log_' + getFileSuffix();
            dirEntry.getFile(fileName, {create: true}, function (fileEntry) {
                    $logFile = fileEntry;
                },
                fileErrorHandler );
        }, fileErrorHandler );
    });
}

////////////////////////////// Sync. related functions //////////////////////////

// Initialize sync. data (typically called on start-up)
function initSyncData( callback ) {
    window.resolveLocalFileSystemURL( cordova.file.dataDirectory, function(dir) {
        dir.getDirectory('sync', {create: true}, function (dirEntry) {
            // Get the sync. requests file
            var fileName = 'reqs';
            dirEntry.getFile(fileName, {create: true}, function (fileEntry) {
                $syncFile = fileEntry;
                fileEntry.file( function( file ) {
                    // Start reading the file
                    var reader = new FileReader();
                    // Post reading all file contents, update it and write it back
                    reader.onloadend = function (e) {
                        var existingReqsStr = this.result;
                        if (!existingReqsStr || existingReqsStr == '') {
                            $syncCache = null;
                        } else {
                            try {
                                $syncCache = JSON.parse(existingReqsStr);
                            } catch (e) {
                                ///console.log('ERROR: initSyncData: ' + e.message);
                                logR('e', 'initSyncData', e.message, e.stack);
                            }
                        }
                        if ( callback )
                            callback();
                    };
                    // Read the file text
                    reader.readAsText(file);
                }, function(e) {
                    if ( callback )
                        callback();
                    fileErrorHandler(e)
                } );
            });
        });
    }, function(e) {
        if ( callback )
            callback();
        fileErrorHandler(e);
    } );
    // Check the sync. lock as well
    ///checkSyncLock( 0 );
}

// Get the current sync. requests JSON, if any
// NOTE: oid is an optional identifier to uniquely identify this request
function getSyncRequestObject( op, uid, kid, oid, params, dataObj ) {
    try {
        var reqObj = {
            id: getRequestId( op, uid, kid, oid ), // req. ID, which can uniquely identify a job
            t: new Date().getTime(), // timestamp of request
            op: op, // ei, es, etc.
            uid: uid, // user ID
            kid: kid, // kiosk ID
            oid: ( oid ? oid : '' ), // object identifier, if any
            params: ( params ? params : {} ),
            urlreq: dataObj, // ajax post object including URL, headers, payload, and so on
        };
        // Check if pre-processing is needed; its need to refresh stock and check
        /*
        var preProcessor;
        if ( ( op == 'ei' || op == 'ew' || op == 'ts') ) {
            preProcessor = {
                processor: "com.logistimo.sync.CheckStockErrors", // note: in Java, this needs to implement IProcessor
                urlreq: {
                    method: 'GET',
                    url: getRefreshInventoryURL(),
                    headers: getAuthenticationHeader()    // TODO: do we need to send Basic auth. headers too?
                }
            };
        }
        if ( preProcessor )
            reqObj.preprocessor = preProcessor;
        */
        return reqObj;
    } catch ( e ) {
        ///console.log( 'ERROR: getSyncRequestObject : ' + e.message, e );
        logR( 'e', 'getSyncRequestObject', e.message, e.stack );
    }
    return null;
}

// Update a sync. req. file
// NOTE: Sync. requests data format: { ids: [<id1>,<id2<...], reqs: { <id1>: { id: "<id1>", t: "<timestamp>", op: "ei|er|es|ew|et|no|uo|uos", uid: "<userId>", "kid": "<kioskId>, data: { url: "<url>", type: "POST", headers: {<headers>}, data: <post-data> } }, <id2>: {...}, ...  } }
function updateSyncRequest( req, callback ) {
    if ( !$syncFile ) {
        logR( 'e', 'updateSyncRequest', 'Sync. file not initialized', null );
        if ( callback )
            callback();
        return;
    }
    if ( !$syncCache )
        initSyncCache();
    // Update the sync. reqs.
    try {
        // Update the syncData cache
        if ( !contains( $syncCache.ids, req.id ) )
            $syncCache.ids.push( req.id );
        $syncCache.reqs[ req.id ] = req; // include new or replace old (this helps in capturing repeated updates before sync.)
        // Update the ID map for later reconciliation
        if ( req.kid ) {
            if ( !$syncCache.idmap.kiosks[ req.kid ] )
                $syncCache.idmap.kiosks[ req.kid ] = [];
            if ( !contains( $syncCache.idmap.kiosks[ req.kid ], req.id ) )
                $syncCache.idmap.kiosks[ req.kid ].push( req.id );
        } else {
            if ( !$syncCache.idmap.general )
                $syncCache.idmap.general = [];
            if ( !contains( $syncCache.idmap.general, req.id ) )
                $syncCache.idmap.general.push( req.id );
        }
        // Now write this file back
        writeFile( $syncFile, JSON.stringify( $syncCache ), false, callback );
    } catch ( e ) {
        ///console.log( 'updateSyncRequest: ERROR: ' + e.message + ', ' + e.stack );
        logR( 'e', 'updateSyncRequest', e.message, e.stack );
    }
}

// Get the sync. lock file
function checkSyncLock( retryNumber ) {
    var lockFilePath = cordova.file.dataDirectory + 'sync/lock';
    window.resolveLocalFileSystemURL( lockFilePath, function( fileEntry ) {
        $syncLocked = true;
        // If we've tried to wait for another 15 seconds and still the lock is not unlocked; move on assuming that there is no lock
        if ( retryNumber == 2 ) {
            $syncLocked = false;
            hideLoader();
            return;
        }
        // Sync. is locked; wait for sometime before checking again
        showLoader( $messagetext.refreshingofflinedata + '...' );
        // Wait for 15 seconds and check again
        setTimeout( function() {
            hideLoader();
            checkSyncLock( retryNumber + 1 );
        }, 15000 );
    }, function(e) {
        $syncLocked = false;
        hideLoader();
    } );
}

// Get an 8-digit random number as an ID
function getRequestId( op, uid, kid, oid ) {
    return uid + ( kid ? '_' + kid : '' ) + '_' + op + ( oid ? '_' + oid : '' );
}

////////////////// File operations ///////////////////////////////

// Write or append to file (callback, if specified, is called on either success or error
function writeFile( file, msg, append, callback ) {
    var errorHandler = ( callback ? function(e) { callback(); fileErrorHandler(e); } : fileErrorHandler );
    file.createWriter( function( fileWriter ) {
        // Seek to the end of file, if append
        if ( append )
            fileWriter.seek( fileWriter.length );
        // Write to file
        var blob = new Blob( [msg], { type: 'text/plain' } );
        fileWriter.write( blob );
        if ( callback )
            callback();
    }, errorHandler );
}

// Remove a file
function removeFile( file ) {
    var filePath = file.fullPath;
    file.remove( function() {
        console.log( 'File removed: ' + filePath );
    }, fileErrorHandler );
}

// Get the full file path
function getFullFilePath( file ) {
    return cordova.file.dataDirectory + file.fullPath;
}

// Get file suffix by date
function getFileSuffix() {
    return getDateAsStr( new Date() );
}

// Get date as string
function getDateAsStr( date ) {
    return date.getDate() + '_' + ( date.getMonth() + 1 ) + '_' + date.getFullYear();
}

// File error handler
function fileErrorHandler( e ) {
    var msg = '';

    switch (e.code) {
        case FileError.QUOTA_EXCEEDED_ERR:
            msg = 'QUOTA_EXCEEDED_ERR';
            break;
        case FileError.NOT_FOUND_ERR:
            msg = 'NOT_FOUND_ERR';
            break;
        case FileError.SECURITY_ERR:
            msg = 'SECURITY_ERR';
            break;
        case FileError.INVALID_MODIFICATION_ERR:
            msg = 'INVALID_MODIFICATION_ERR';
            break;
        case FileError.INVALID_STATE_ERR:
            msg = 'INVALID_STATE_ERR';
            break;
        default:
            msg = 'Unknown Error';
            break;
    };
    console.log('File handling error: ' + msg + ', code: ' + e.code );
}


///////////////////// Reconciling synced data //////////////////////////////

// Reconcile sync. data
function reconcileSyncedData( kid ) {
    if ( !isDevice() )
        return false;
    if ( !$syncCache || !$syncCache.ids || $syncCache.ids.length == 0 || !$syncCache.reqs ) {
        ///console.log( 'reconcileSyncData: Nothing to reconcile' );
        return false;
    }
    // Start the reconcilition process, one request at a time
    var reqIds = ( kid ? $syncCache.idmap.kiosks[ kid ] : $syncCache.idmap.general );
    if ( !reqIds || reqIds.length == 0 ) {
        ///console.log( 'reconcileSyncData: Nothing to reconcile for - ' + ( kid ? kid : 'general' ) );
        return false;
    }
    // Check whether sync-ing is going on now
    var reqsDone = [];
    for ( var i = 0; i < reqIds.length; i++ ) {
        var reqId = reqIds[i];
        var req = $syncCache.reqs[ reqId ];
        if ( !req || !req.resp ) {
            ///console.log( 'reconcileSyncedData: WARNING: No response object for req ' + reqId );
            continue;
        }
        if ( kid && req.kid && req.kid != kid ) {
            continue;
        }
        if ( req.resp.data ) {
            try {
                var resp = JSON.parse( req.resp.data );
                // Check type of operation
                if ( isOperationInventoryTransaction( req.op ) ) { // inventory transaction
                    if ( resp.st && ( resp.st == '0' || resp.st == '2' ) ) { // success
                        var linkedEntityName = ( req.params && req.params.lkn ? req.params.lkn : '' );
                        var localModifications = getLocalModifications( null, req.op, getEntity( $uid, req.kid ) );
                        // Pre-process
                        var oldState = changeTransactionContext( req.kid, req.op );
                        // Update error messages, if needed (esp. if it was based on SMS response)
                        if ( resp.st == '2' )
                            addInvTransErrorMessage( resp );
                        // Process response
                        processInventoryTransactionResponse( resp, req.kid, req.op, linkedEntityName, localModifications, null, null ); // TODO: error handling via callback
                        // Post-process
                        resetTransactionContext( oldState );
                    } else if ( resp.st && resp.st == '1' ) { // error
                        logR( 'e', 'reconcileSyncedData: Error in inventory transaction response: op: ' + req.op + ', err: ' + ( resp.st.ms ? resp.st.ms : ( resp.st.cd ? resp.st.cd : '' ) ) );  // TODO: Error handling
                        var msg = ( resp.ms ? resp.ms : addInvTransErrorMessage( resp ) );
                        if ( msg )
                            showDialog( null, msg, getCurrentUrl() );
                    }
                } else if ( req.op == 'no' || req.op == 'uo' ) { // new order creation or update of an existing order
                    if (resp.st && resp.st == '0') { // success
                        var otype = ( req.params && req.params.otype ? req.params.otype : 'prc' );
                        var isNew = ( req.op == 'no' );
                        // Get the current state of entity and op
                        var oldState = changeTransactionContext( req.kid, req.op );
                        // Process the saving of new/updated order locally
                        processOrderTransactionResponse(resp, otype, isNew, null, null); // TODO: error handling via callback
                        // Update to the previous state of entity and op
                        resetTransactionContext( oldState );
                        //$currentOperation = prevOp; // reset $currentOperation
                    } else {
                        logR( 'e', 'reconcileSyncedData: Error in new order (no) response: ' + ( resp.st.ms ? resp.st.ms : ( resp.st.cd ? resp.st.cd : '' ) ), null );  // TODO: Error handling
                    }
                } else if ( req.op == 'li' ) { // Login to download the configuration and master data
                    // Update user data locally
                    if ( resp.st == '0' )
                        storeUserData($uid, resp, true, true);
                    else
                        logR( 'e', 'reconcileSyncedData', 'Error when sync-ing master-data/configuration: ' + ( resp.ms ? resp.ms : '' ), null );
                } else if ( req.op == 'uos' ) { // update order status
                    if ( resp.st == '0' ) {
                        var orderType = ( req.params && req.params.otype ? req.params.otype : null );
                        updateLocalOrderStatus( resp, orderType );
                    } else {
                        logR( 'e', 'reconcileSyncedData', 'Error when syncing order status: orderType: ' + orderType + ', err:' + ( resp.ms ? resp.ms : '' ), null );
                    }
                } else if ( req.op == 'rtrns' ) { // refresh transactions
                    // Update transactions locally
                    if ( resp.st == '0' )
                        updateLocalTransactions( resp );
                    else
                        logR( 'e', 'reconcileSyncedData', 'Error when refreshing transactions: ' + ( resp.ms ? resp.ms : '' ), null );
                } else if ( req.op == 'rords' ) { // refresh orders
                    if ( resp.st == '0' ) {
                        var orderType = ( req.params && req.params.otype ? req.params.otype : null );
                        updateOrders(resp.os, orderType);
                    } else {
                        logR( 'e', 'reconcileSyncedData', 'Error when refreshing orders: ' + ( resp.ms ? resp.ms : '' ), null );
                    }
                } else if ( req.op == 'xi' ) { // export inventory
                    // Do nothing, if success; but log errors, if any
                    if ( resp.st && resp.st == '1' )
                        logR( 'e', 'reconcileSyncedData', 'Error when exporting inventory: ' + ( resp.ms ? resp.ms : '' ), null );
                } else {
                    console.log( 'reconcileSyncedData: Unsupported op: ' + req.op );
                }
                // Schedule this for deletion
                reqsDone.push( req.id );
            } catch ( e ) {
                ///console.log( 'reconcileSyncedData: ERROR: for req with ID: ' + req.id + ': ' + e.message + ', ' + e.stack );
                logR( 'e', 'reconcileSyncedData', 'Error for req with Id ' + req.id + ': ' + e.message, e.stack );
            }
        } else if ( req.resp.error ) {
            ///console.log( 'reconcileSyncedData: ERROR: response error for req ' + req.id + ': status = ' + ( req.resp.error.status ? req.resp.error.status : 'NONE' ) + ', errMsg = ' + ( req.resp.error.message ? req.resp.error.message : '' ) ); // TODO: Error handling
            logR( 'e', 'reconcileSyncedData', 'Response error for req ' + req.id + ': status: ' + ( req.resp.error.status ? req.resp.error.status : 'NONE' ) + ', err: ' + ( req.resp.error.message ? req.resp.error.message : '' ), null );
        } else {
            ///console.log( 'reconcileSyncedData: ERROR: Neither success data nor error for req ' + reqId );
            logR( 'e', 'reconcileSyncedData', 'ERROR: Neither success data nor error for req ' + reqId, null );
        }
    }
    // Clear completed requests from the cache, if any were processed in this run
    if ( reqsDone.length > 0 ) {
        clearSyncData(kid, reqsDone, false);
        return true;
    } else {
        return false;
    }
}

// Reconcile all synced data by user
function reconcileAllSyncedData( uid, clearSyncQueue ) {
    if ( !isDevice() )
        return;
    if ( !uid ) {
        if ( clearSyncQueue )
            clearSyncData( null, null, false );
    }
    // Get user data
    var userData = getUserData( uid );
    if ( !userData ) {
        if ( clearSyncQueue )
            clearSyncData( null, null, false );
        return;
    }
    try {
        // Reconcile the synced. data for non-kiosk syncs. (e.g. new entity registration)
        reconcileSyncedData(null);
        // Reconcile by kiosk, if any
        var kids = userData.ki;
        if ( !kids ) {
            if ( clearSyncQueue )
                clearSyncData( null, null, false );
            return;
        }
        for ( var i = 0; i < kids.length; i++ )
            reconcileSyncedData( kids[i] );
        // Clear all data in sync. queue, if needed
        if ( clearSyncQueue )
            clearSyncData( null, null, false );
    } catch ( e ) {
        logR( 'e', 'reconcileAllSyncedData', e.message, e.stack );
        if ( clearSyncQueue )
            clearSyncData( null, null, false );
    }
}

// Clear sync. data persistently
// NOTE: If neither kid or reqIds are passed, then everything is cleared! Be careful. If only kid is passed, then requests only for this kiosk are cleared. If only reqIds are passed (i.e. general, non-kiosk requests), then only these requests are cleared (from general).
function clearSyncData( kid, reqIds, omitUnsentReqs ) {
    try {
        var clearAll = ( !kid && !reqIds && !omitUnsentReqs );
        if ( clearAll ) { // clear everything
            initSyncCache();
        } else {
            var requestIds = ( reqIds ? reqIds : getSyncRequestIds(kid) );
            if ( !requestIds ) { // nothing to clear
                ///console.log( 'clearSyncData: Nothing to clear' );
                return;
            }
            // Clear each request from the sync. cache
            for ( var i = 0; i < requestIds.length; i++ ) {
                if ( omitUnsentReqs ) {
                    var req = $syncCache.reqs[ requestIds[i] ];
                    if ( req && !req.resp )
                        continue;
                }
                removeSyncReqFromCache(kid, requestIds[i]);
            }
        }
        // Write to the sync. file or remove file if nothing to write
        if ($syncFile) {
            if ( !clearAll && ( !$syncCache || !$syncCache.ids || $syncCache.ids.length == 0 ) ) { // if all syncing is complete, then reset the sync. cache (NOTE: do not remove file, since the next sync. within the same app. session will have a problem finding the sync. file without full re-initialization of the file)
                initSyncCache(); // resetting the sync. cache
            }
            // Write back sync. data
            writeFile($syncFile, JSON.stringify($syncCache), false, null);
        } else {
            ///console.log( 'clearSyncData: ERROR: No sync. file found to write the cleared sync. cache' );
            logR('e', 'clearSyncData', 'No sync. file found to write the cleared sync. cache: reqIds: ' + ( reqIds ? reqIds : 'NONE' ) + ', kid: ' + ( kid ? kid : 'NONE'), null);
        }
    } catch ( e ) {
        ///console.log( 'clearSyncData: ERROR: ' + e.message + ', ' + e.stack );
        logR( 'e', 'clearSyncData', e.message, e.stack );
    }
}

// Remove a request from the sync. cache
function removeSyncReqFromCache( kid, reqId ) {
    if ( !$syncCache || !$syncCache.ids || !$syncCache.reqs || !reqId )
        return;
    // Remove from the ids list
    var index = $syncCache.ids.indexOf( reqId );
    if ( index > -1 ) {
        $syncCache.ids.splice( index, 1 ); // remove this req. ID
        if ( $syncCache.index  && ( index <= $syncCache.index ) ) // re-adjust the current request index, if needed
            $syncCache.index--;
    }
    if ( $syncCache.reqs[ reqId ] ) {
        delete $syncCache.reqs[reqId];
    }
    // Clear the idmap
    var ids;
    if ( kid )
        ids = $syncCache.idmap.kiosks[ kid ];
    else
        ids = $syncCache.idmap.general;
    if ( ids && ids.length > 0 ) {
        var idx = ids.indexOf( reqId );
        if ( idx > -1 )
            ids.splice( idx, 1 );
    }
}

// Get all sync. request IDs for a given kiosk
function getSyncRequestIds( kid ) {
    if ( !$syncCache || !$syncCache.ids || !$syncCache.reqs || !$syncCache.idmap )
        return null;
    if ( kid ) {
        if ( $syncCache.idmap.kiosks )
            return $syncCache.idmap.kiosks[ kid ];
    } else if ( $syncCache.idmap.general ) {
        return $syncCache.idmap.general;
    }
    return null;
}

// Get the error message given the error code
function addInvTransErrorMessage( updInvTransOutput ) {
    if ( !updInvTransOutput || !updInvTransOutput.st )
        return null;
    if ( updInvTransOutput.st == '1' && !updInvTransOutput.ms && updInvTransOutput.cd ) { // no error message, but error code exists
        if ( updInvTransOutput.cd.toUpperCase() == 'M004' ) // system error
            updInvTransOutput.ms = $messagetext.error_system;
        else if ( updInvTransOutput.cd.toUpperCase() == 'M003' ) // no customers/vendors for a transfer
            updInvTransOutput.ms = $messagetext.nocustomerorvendor;
    } else if ( updInvTransOutput.st == '2' ) { // partial errors
        if ( !updInvTransOutput.er || updInvTransOutput.er.length == 0 )
            return;
        for ( var i = 0; i < updInvTransOutput.er.length; i++ ) {
            var error = updInvTransOutput.er[i];
            if ( !error.ms && error.cd ) {
                if ( error.cd.toUpperCase() == 'M001' )
                    error.ms = $messagetext.mustbepositiveinteger;
                else if ( error.cd.toUpperCase() == 'M002' )
                    error.ms = $messagetext.cannotexceedcurrentstock;
                else if ( error.cd.toUpperCase() == 'M003' )
                    error.ms = $messagetext.nocustomerorvendor;
                else if ( error.cd.toUpperCase() == 'M004' )
                    error.ms = $messagetext.error_system;
                else if ( error.cd.toUpperCase() == 'M005' )
                    error.ms = $messagetext.nosuchmaterial;
                else if ( error.cd.toUpperCase() == 'M006' )
                    error.ms = $messagetext.actualdatenotinfuture;
                else if ( error.cd.toUpperCase() == 'M007' )
                    error.ms = $messagetext.materialdoesnotexistdestentity;
                else if ( error.cd.toUpperCase() == 'M008' )
                    error.ms = $messagetext.transferfailedfrombatchenabledentity;
                else
                    error.ms = '';
            }
        }
    }
}