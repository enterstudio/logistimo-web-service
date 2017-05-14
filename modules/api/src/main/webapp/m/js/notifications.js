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

function updateEvent(userId,kioskId,eventType,eventParams)
{
    var kid = kioskId;
    var type = '';
    var action = '';
    var qty = 0;
	var uid = '';
	var mid = '';
	var lkid = '';
	var otype = '';
    if (eventParams.qty)
      qty = eventParams.qty;
    if (eventParams.lkid)
      lkid = eventParams.lkid;
    if (eventParams.action)
         action = eventParams.action;
    if (eventParams.type)
         type = eventParams.type;
    if (eventParams.mid)
         mid = eventParams.mid;
    if (eventParams.uid)
         uid = eventParams.uid;
	if (eventParams.otype)
	    otype = eventParams.otype;
	 var newManagedEntity = false;
	 if (type && (type == 'ents'))
		  newManagedEntity = true;

    entity = getEntity(userId,kid);


    var oemData  = getlocalOEM( userId );
	if  (!oemData)
		 oemData = {};	
	if (!oemData[kid])
	{
		oemData[kid]= [];
		var k = {};
		k.etyp = eventType;
		k.tot = 0;

		  var lkinfo = {};
		  if (lkid)
		  {
			  lkinfo.kid = lkid;
		   if (action)
		    lkinfo.action = action;
		  if (type)
		    lkinfo.type = type;
		  if (otype)
			  lkinfo.otype = otype;
		  if (uid)
			lkinfo.uid = uid;
		   k.lkid = [];
	       k.lkid.push(lkinfo);
		  }
		  k.tot = k.tot +qty;
		
	    oemData[kid].push(k);
	}
	else
	{
		 var k1 = [];
	       k1 = oemData[kid];
		   var idx = getEventIndex(k1,eventType);
	     if (idx < 0)// if event not found, create a new event entry
		 {
	      	var k = {};
		   	k.etyp = eventType;
		   	k.tot = 0;
			var lkinfo = {};
			if (lkid)
			{
				  lkinfo.kid = lkid;
				  if (action)
					lkinfo.action = action;
				  if (type)
					lkinfo.type = type;
				  if (otype)
					lkinfo.otype = otype;
				  if (uid)
					lkinfo.uid = uid;
				  k.lkid = [];
				  k.lkid.push(lkinfo);
			}
			k.tot = k.tot +qty;
		  	oemData[kid].push(k);
		 } else { //event already stored
			 if (lkid)
			 {
				   lidx = hasLinkedKid(k1[idx].lkid,lkid);
			       if (lidx == -1) //linkedkiosk not found
			       {
			    	  var lkinfo = {};
					  if (lkid)
						  lkinfo.kid = lkid;
					  if (action)
					    lkinfo.action = action;
					  if (type)
						  lkinfo.type = type;
				      if (otype)
						  lkinfo.otype = otype;
					  if (uid)
						lkinfo.uid = uid;
				      k1[idx].lkid.push(lkinfo);
				      if (eventType == 'no' ||eventType == 'ne' )
				    	  k1[idx].tot = k1[idx].tot +qty;
			        }
			       	else //linkedkiosk found modifying more than one user
			    	{
			    	   if (eventType == 'ne')
			    		{
			    		   var lkinfo = {};
							  if (lkid)
								  lkinfo.kid = lkid;
							  if (action)
							    lkinfo.action = action;
							  if (type)
								  lkinfo.type = type;
							  if (uid)
									lkinfo.uid = uid;
							  if (hasModifiedEntity(k1[idx].lkid,lkinfo) < 0)
						      	k1[idx].lkid.push(lkinfo);
			    		}
			    	}
			 }
			 if ( eventType !='no'  && eventType !='ne' )
			 {
					k1[idx].tot = k1[idx].tot +qty;
			 }
	     }
	}
	storelocalOEM( userId, oemData );
}

function getEventIndex(k1,eventType)
{
   for ( var i = 0; i < k1.length; i++ ) {
	 if (k1[i].etyp == eventType)
	 {
		 return i;
	 }
  }
   return -1;
}

function hasLinkedKid(k1,lkid)
{
 for ( var i = 0; i < k1.length; i++ ) {
	 if (k1[i].kid == lkid)
	 {
		 return i;
	 }
	
  }
	
   return -1;
}

function hasModifiedEntity(k1,lkinfo)
{
	for ( var i = 0; i < k1.length; i++ ) {
		 if (k1[i].kid == lkinfo.kid)
		 {
			 if (lkinfo.uid)
			 {
				 if (k1[i].uid)
				  if (k1[i].uid == lkinfo.uid)
					  return i;
			 }
			 else
				 return i;

		 }
		 
	}
	
   return -1;
}

	


function closeEvent(userId,kioskId,eventType,eventParams)
{
    var kid = kioskId;
	var qty = eventParams.qty;
	var oemData  = getlocalOEM(userId);
	var entity = getEntity(userId,kioskId);
	var linkedEntity = '';
	var lkid = '';
	if (eventParams.linkedEntity)
      linkedEntity = eventParams.linkedEntity;
	var type = '';
	if (eventParams.type)
	     type = eventParams.type;
	if (linkedEntity)
	 {
		 lkid = linkedEntity.kid;
	 }
	 if (eventType == 'no')
	 {
		 if (lkid)
		  entity.localdata[ eventType ].linkedentity = linkedEntity;
		 if (getLocalModifications( null, eventType, entity )) //if more materials on order, event is not closed
			return false;		 
	 }
	if ( !oemData )
		return false;
	if (oemData[kid])
    {
    	var k1 = [];
		k1 = oemData[kid];
    	var idx = getEventIndex(k1,eventType);
    	if (idx >= 0)
    	{
    	 if (eventType != 'ne')
    	 {
	 	      k1[idx].tot = k1[idx].tot-qty;
    		 if (lkid)
    		 {
    		  entity.localdata[ eventType ].linkedentity = linkedEntity;
    		  if (!getLocalModifications( null, eventType, entity ))
    		 {
    		  lidx = hasLinkedKid(k1[idx].lkid,lkid);
    		  if (lidx >=0)
    		   k1[idx].lkid.splice(lidx,1);
    		  }
    		 }
    	 }
    	 else //for entity
    	{
    		 if (lkid)
    		 {
    		  lidx = hasLinkedKid(k1[idx].lkid,lkid);
    		  if (lidx >=0)
    		   k1[idx].lkid.splice(lidx,1);
    		  //check for more lkids - editing users along with entities
    		   lidx = hasLinkedKid(k1[idx].lkid,lkid);
    		   if (lidx <0)
    			   k1[idx].tot = k1[idx].tot-qty;
    		 } 
    		 
    		  
    	}
    	 if (k1[idx].tot <= 0)
   			 k1.splice(idx,1); 
    	  oemData[kid]= k1;
    	 if (oemData[kid].length ==0)
    			 delete oemData[kid];
    	}
    	  
    }
    storelocalOEM(userId,oemData);

    return true;
}

function setEventTypeCount(k1,kioskId,countEntityEvent)
{
	var idx;
	var kid = kioskId;
	for ( var op in $nMsg ) {
		if ( !$nMsg.hasOwnProperty( op ) )
			continue;
		 idx = getEventIndex(k1,op);
		 if (idx >= 0)
		 {
			 if (!$ntfyMdl[op])
			 {
				 $ntfyMdl[op] = {}; /// earlier: [];
				 $ntfyMdl[op].evct = 0;
				 $ntfyMdl[op].kct = 0;
				 $ntfyMdl[op].kids = [];
			 }
			 $ntfyMdl[op].evct = $ntfyMdl[op].evct + k1[idx].tot;
			 $ntfyMdl[op].kids.push(kid);
			 $ntfyMdl[op].kct = $ntfyMdl[op].kct + 1;
		 }
	}
}

 function getEventTypeCountForEntity(userId,kioskId,eventType)
 {	 
	    var kid = kioskId;
		var oemData  = getlocalOEM(userId);
		var eventCount = 0;
		if ( !oemData )
			return false;
		if (oemData[kid])
	    {
			var k1 = [];
			k1 = oemData[kid];
			var idx = getEventIndex(k1,eventType);
			if (idx >= 0)
			{
			  eventCount = k1[idx].tot;
			}
				
	    }
		return eventCount;
 }

 function  getEventTypeCountForRelatedEntity(userId,kioskId,linkedId,eventType)
 {
	 var ct = 0;
	 var entity = getEntity(userId,kioskId);
	 var materials = entity.localdata[ eventType ].materials;
	  for ( var k1 in materials ) {
		if ( !materials.hasOwnProperty( k1 ) )
			continue;
		 if (k1 == linkedId)
		 {	  	
		 for (var k2 in materials[k1]){
			 if ( !materials[k1].hasOwnProperty( k2 ) )
					continue;
		    ct = ct +1;
		  }
		 }
	  } 
	  return ct;
 }

function initEventModel(userId,kioskId)
{
	var oemData  = getlocalOEM(userId);
	var kid = kioskId;
	$ntfyMdl = {};
	if (!oemData)
		return;
	if (!kid)
	{
		var countEntityEvent = true;
		for ( var kid in oemData ) {
			if ( !oemData.hasOwnProperty( kid ) )
				continue;
			var k1 = oemData[kid];
			if (k1)
				setEventTypeCount(k1,kid,countEntityEvent);
		}
	} else { //single entity
	   	var countEntityEvent = true;
		var k1 = oemData[kid];
		if (k1)
			setEventTypeCount(k1,kid,countEntityEvent);
	}
}

function getNotificationCount()
{
  	var nCount = 0;
	if ($ntfyMdl) {
		 for ( var etype in $ntfyMdl ) {
				if ( !$ntfyMdl.hasOwnProperty( etype ) )
					continue;
			 nCount++;
		 }
   	}
	return nCount;
}

function getUnsentRelatedEntityIds(userId,kioskId,eventType)
{
	var oemData  = getlocalOEM(userId);
	var kid = kioskId;
	var linkedKid;
	if ( !oemData )
		return false;
	if (oemData[kid])
    {
		 k1 = oemData[kid];
		 idx = getEventIndex(k1,eventType);
		 if (idx >= 0)
		 {
			 linkedKid =[]; 
			 linkedKid=k1[idx].lkid;
		 }
    }
	return linkedKid;
}

function getlinkedEntity(userId,kioskId,linkedkid,eventType)
{
	var oemData  = getlocalOEM(userId);
	var kid = kioskId;
	var type = 'ents';
	var linkedEntity;
	if ( !oemData )
		return false;
	if (oemData[kid])
    {
		 k1 = oemData[kid];
		 idx = getEventIndex(k1,eventType);
		 if (idx >= 0)
		 {
			 var linkedKid =[]; 
			 var linkedKid=k1[idx].lkid;
			 for (i=0;i<linkedKid.length;i++)
		      { 
		 		if (linkedKid[i].kid == linkedkid)
					{
					  linkedEntity = linkedKid[i];
					  return linkedEntity;
					}
			  }
		 }
    }

	return null;
}

function getUnsentEnititieslist(entityIds)
{
  var eventType = 'ne';
  var unsentEntityIds = [];
  for ( var i = 0; i < entityIds.length; i++ ) {
	  linkedKids= getUnsentRelatedEntityIds($uid,entityIds[i],eventType);
	  if (linkedKids)
	  {
 		for ( var j = 0; j < linkedKids.length; j++ ){
 		  var linkedEntity = getlinkedEntity($uid,entityIds[i],linkedKids[j].kid,eventType);
 		  var k = {};
 		  if (linkedEntity)
     		{
 			   k.kid = entityIds[i];
 			   k.lkid =linkedEntity.kid;
   			   k.typ = linkedEntity.type;
 			   k.act = linkedEntity.action;
 			   if (linkedEntity.uid)
 				   k.uid = linkedEntity.uid;
 			   unsentEntityIds.push(k);	
     		}
 		}
	  }
   }
  return unsentEntityIds;
	
}

//Render a list of entities
function renderUnsentEntities(pageId, userId,entityIds,params, options ) {
	/*
	var tag = null;
	if ( params && params != null && params.tag )
		tag = params.tag;
	*/
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	var eventType = '';
	if ( params.op )
	   eventType = params.op;
	// Update the header title
	var headerTxt = $operations[eventType];
	header.children( 'h3' ).html( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	// Update back button URL
	var prevPageId = getPrevPageId( options );
	if ( prevPageId != null && !( prevPageId == '#review'  || prevPageId == '#unsentrelatedentities' ) ) { // so that an infinte reverse back loop is not setup with the review/unsentrelatedentities page
		var backUrl = prevPageId + ( params.op ? '?op=' + params.op : '' );
		header.find('#unsententitiesback').attr('href', backUrl );
		if (!$entity)
			footer.find('#unsententitiescancelbutton').attr('href', backUrl );
	}
	// Update header title
	setTitleBar( header, '', false );
    var reviewUrl ='';
	// Update content
	content.append( markup );
	var pageOffset = 0;
	var markup = '';
    var listId = 'unsententities'+'_list_01';
    markup += '<ul id="' + listId +'" data-role="listview"  class="mylist"  data-theme="d" data-divider-theme="d" data-count-theme="e" data-split-icon="arrow-u"></ul>';
    var entitieslistview = $(markup);    
    content.empty();
	content.append( entitieslistview);
	// Enhance page
	page.page();
	page.trigger('create');
	if (eventType != 'ne')
	 	updateEntitesWithModsListview('unsententities', entitieslistview, entityIds,eventType,pageOffset ); // returns a collapsible-set
	else {
		var unsentEntityIds = getUnsentEnititieslist(entityIds);
		updateUnsentEntitiesListview('unsententities', entitieslistview, unsentEntityIds,eventType,pageOffset );
	 }
	// Update content
	content.append( entitieslistview );
    
	if ( options ) {
		// Update content
		// Update options URL and change to page
		options.dataUrl = '#unsententities?o=' + pageOffset+'&op='+eventType;
	   $.mobile.changePage( page, options );
		
	} else
		$.mobile.changePage( page );
	header.find( '#unsententitiesback .ui-btn-text').text($buttontext.back);
	footer.find( '#unsententitiescancelbutton .ui-btn-text').text($buttontext.cancel);

}

//Update unsent entities list view
function updateEntitesWithModsListview( id, listview,entityIds,eventType, pageOffset ) {
	// Append the navigation bar
	    var userId=$uid;
	    var size = 0;    
         if (entityIds)
	 	 size = entityIds.length; // get for all transactions for entity
		// Set the listview navigation bar
		if ( size == 0 ) {
			listview.empty().append( $( '<li><p>No Entities.</p></li>' ) );
	 	}
		else
		{					  
				// Set the listview navigation bar
		  var range = setListviewNavBar( listview, pageOffset, size, null, function() {
			  updateEntitesWithModsListview( id, listview,entityIds,eventType, (pageOffset-1) );
		   }, function() {
			   updateEntitesWithModsListview( id, listview,entityIds,eventType, (pageOffset+1) );
		   } );
		   var start = range.start;
		   var end = range.end;	
		  // Get the list of entities for rendering on this page
		    var markup = '';
		    for ( var i = ( start - 1 ); i < end; i++ ) {
		 		var eventCount = getEventTypeCountForEntity($uid,entityIds[i],eventType);
		 		var entity = getEntity( $uid, entityIds[i] );
		 		var id = 'entities_' + entity.kid;
		 		var kioskId = entity.kid;
		 		var hasGeo = ( entity.lat && entity.lng );
		 		var onclick ='';
				if (eventType != 'ne')
				{
		 			 var reviewUrl = getReviewUrl(eventType,kioskId,null);
		 			 markup += '<li id="' + id + '"><a href="'+reviewUrl+ '" onclick="' + onclick + '"><h3>' + entity.n + ( hasGeo ? ' <img src="jquerymobile/icons/map_pin.png"/>' : '' ) + '</h3>' +
		 					  '<p>' + ( entity.sa ? entity.sa + ', ' : '' ) + entity.cty + ( entity.pc ? ', ' + entity.pc : '' ) + ( entity.tlk ? ', ' + entity.tlk : '' ) +'</p>';
					 markup += '<a href="#" data-theme="b"  onclick='+'"showConfirm('+"'"+$buttontext.sendnow+"'" +",'"+$sMsg[eventType]+"?',"+'null,'+ 'function(){sendTransactionForEntities('+"'"+eventType+"'"+','+kioskId+')},getCurrentUrl())">'+$buttontext.sendnow+'</a>';
				}
				if (eventType != 'ne')
		 		   markup += '<span class="ui-li-count" style="font-size:12pt">'+eventCount+'</span>';
				markup += '</a></li>';
		    }
		   // Append item to listview
		  listview.append( $( markup ) );	
	// Refresh the view
	 listview.listview('refresh');
    }
}

//Update unsent entities list view
function updateUnsentEntitiesListview( id, listview,entityIds,eventType, pageOffset ) {
	// Append the navigation bar
	    var userId=$uid;
	    var size = 0;    
        if (entityIds)
	 	 size = entityIds.length; // get for all transactions for entity
		// Set the listview navigation bar
		if ( size == 0 ) {
			listview.empty().append( $( '<li><p>No Entities.</p></li>' ) );
	 	}
		else
		{					  
				// Set the listview navigation bar
		  var range = setListviewNavBar( listview, pageOffset, size, null, function() {
			  updateUnsentEntitiesListview( id, listview,entityIds,eventType, (pageOffset-1) );
		   }, function() {
			   updateUnsentEntitiesListview( id, listview,entityIds,eventType, (pageOffset+1) );
		   } );
		   var start = range.start;
		   var end = range.end;	
		  // Get the list of entities for rendering on this page 
		   var prevLkid = '';
		    var markup = '';
		    for ( var i = ( start - 1 ); i < end; i++ ) {
		 		var entity = getEntity( $uid, entityIds[i].kid );
		 		if (prevLkid == entityIds[i].lkid)
		 			continue;
		 		else
		 			prevLkid = entityIds[i].lkid;
		 		var id = 'entities_' + entity.kid;
		 		var kioskId = entity.kid;
		 		var hasGeo = ( entity.lat && entity.lng );
		 		var onclick ='';
		   		var entityType = entityIds[i].typ;
		 		var action = entityIds[i].act;
		 		var lkid = entityIds[i].lkid;
		 		var lentity;
				if( entityType != 'ents')
	 			 {
				   if (action == 'edit')
		 			  lentity = getRelatedEntityForKioskId(lkid,entityType,entity.kid);
		 			else if (action == 'add')//new related entity stored separetely.
		 			   lentity = getEntity(userId,lkid);
		 	   	 }
				 else {
					  lentity = getEntity(userId,lkid);
				 }
				 var sendNow = "Yes";
				 var newEntity = "true";
		 		 var reviewUrl = '#entityinfo?kid=' + entity.kid +'&ty='+entityType+'&ne='+newEntity+'&SendNow='+sendNow ;
   			     reviewUrl +='&lkid=' + lentity.kid+'&action=edit';
   			     markup += '<li id="' + id + '"><a href="'+reviewUrl+ '" onclick="' + onclick + '"><h3>' + lentity.n + ( hasGeo ? ' <img src="jquerymobile/icons/map_pin.png"/>' : '' ) + '</h3>' +
			      '<p>' + ( lentity.sa ? lentity.sa + ', ' : '' ) + lentity.cty + ( lentity.pc ? ', ' + lentity.pc : '' ) + ( lentity.tlk ? ', ' + lentity.tlk : '' ) +'</p>';
   			     var entitydetails = '';
   			     if (action == 'add')
		 			 entitydetails = 'New';
		 		// else
		 		//	var entitydetails = 'Changed';
		 		 if (entityType == 'csts')
		 			entitydetails +=  ' Customer';
	    	      else if (entityType == 'vnds')
	    	    	  entitydetails +=  ' Vendor';
		 		  else if (entityType == 'ents')
		 			 entitydetails += ' Entity';
		 		   if ( entityType != 'ents')
		 			  entitydetails += ' for ' +entity.n;	
		 		   markup +='<p><font color="green">';
		 		   markup += entitydetails+'</font></p>';
		 		}

		 		  markup += '</a></li>';
		 	    
		   // Append item to listview
		  listview.append( $( markup ) );	
	// Refresh the view
	 listview.listview('refresh');
    }
}

//Render a list of entities
function renderUnsentRelatedEntities(pageId, userId,linkedIds,kioskId,params, options ) {
	/*
	var tag = null;
	if ( params && params != null && params.tag )
		tag = params.tag;
	*/
	// Get the entity page first
	var page = $( pageId );
	// Get the page header and content
	var header = page.children( ':jqmData(role=header)' );
	var content = page.children( ':jqmData(role=content)' );
	var footer = page.children( ':jqmData(role=footer)' );
	var eventType = '';
	if ( params.op )
	   eventType = params.op;
	// Update the header title
	var headerTxt = $operations[eventType];
	//headerTxt += $nMsg[eventType];
	header.children( 'h3' ).html( headerTxt );
	// Update page title (to update browser-displayed title case of HTML5 view in browser)
	page.attr( 'data-title', headerTxt );
	var prevPageId = getPrevPageId( options );
	if ( prevPageId != null && prevPageId != '#review' ) {
		var backUrl = prevPageId + ( params.op ? '?op=' + params.op : '' );
		header.find('#unsentrelatedentitiesback').attr('href', backUrl );
		if (!$entity)
			footer.find('#unsentrelatedentitescancel').attr('href', backUrl );
	}
	// Update header title
	setTitleBar( header, '', false );
    var reviewUrl ='';
	var markup = '';
	var pageOffset = 0;
	var markup = '';
    var listId = 'unsententities'+'_list_01';
    markup += '<ul id="' + listId +'" data-role="listview"  class="mylist"  data-theme="d" data-divider-theme="d" data-count-theme="e" data-split-icon="arrow-u"></ul>';
    var entitieslistview = $(markup);
    ///var backUrl = '#unsententities?&op='+eventType;
    ///header.find( pageId + 'back' ).attr( 'href', backUrl );
    content.empty();
    content.append( entitieslistview);
	// Enhance page
	page.page();
	page.trigger('create');
	updateUnsentRelatedEntitiesListview('unsentrelatedetities', entitieslistview, linkedIds,kioskId,eventType,pageOffset ); // returns a collapsible-set
	// Update content
	content.html( entitieslistview );
    
	if ( options ) {
		// Update content
		// Update options URL and change to page
		options.dataUrl = '#unsentrelatedentities?o=' + pageOffset+'&op='+eventType;
	   $.mobile.changePage( page, options );
		
	} else
		$.mobile.changePage( page );

	header.find( '#unsentrelatedentitiesback .ui-btn-text').text($buttontext.back);
	footer.find( '#unsentrelatedentitescancel .ui-btn-text').text($buttontext.cancel);

}

	
//Update unsent related entities list view
function updateUnsentRelatedEntitiesListview( id, listview,linkedIds,kioskId,eventType, pageOffset ) {
		// Append the navigation bar
	    var size = 0;    
	    var userId = $uid;
	  
	 	 size = linkedIds.length; // get for all transactions for entity
		// Set the listview navigation bar
		if ( size == 0 ) {
			listview.empty().append( $( '<li><p>No entities.</p></li>' ) );
	 	}
		else
		{					  
			// Set the listview navigation bar
		  	var range = setListviewNavBar( listview, pageOffset, size, null, function() {
			  	updateUnsentRelatedEntitiesListview('unsentrelatedentities', enttitieslistview, linkedIds,kioskid,eventType,(pageOffset-1) );
		 	}, function() {
			 	updateUnsentRelatedEntitiesListview('unsentrelatedentities', enttitieslistview, linkedIds,kioskid,eventType,(pageOffset+1) );
		 	} );
		  	var start = range.start;
		  	var end = range.end;
		 	// Get the list of orders for rendering on this page
		    var markup = '';
			for ( var i = ( start - 1 ); i < end; i++ ) {
				var linkedId = linkedIds[i].kid;
		        var type = linkedIds[i].type;
				var otype = linkedIds[i].otype;
				var eventCount = getEventTypeCountForRelatedEntity($uid,kioskId,linkedId,eventType);
				var entity = getRelatedEntityForKioskId(linkedId,type,kioskId);
				var id = 'entities_' + entity.kid;
				var hasGeo = ( entity.lat && entity.lng );
				var onclick ='';
				var reviewUrl = getReviewUrl(eventType,kioskId,linkedId);
				markup += '<li id="' + id + '"><a href="'+reviewUrl+ '" onclick="' + onclick + '"><h3>' + entity.n + ( hasGeo ? ' <img src="jquerymobile/icons/map_pin.png"/>' : '' ) + '</h3>' +
								  '<p>' + ( entity.sa ? entity.sa + ', ' : '' ) + entity.cty + ( entity.pc ? ', ' + entity.pc : '' ) + ( entity.tlk ? ', ' + entity.tlk : '' ) +'</p>';

				if (eventType == 'no') {
					if (otype == 'sle')
			        	 markup += $labeltext.salesorder;
					else
			            markup += $labeltext.purchaseorder;
				}
				markup += '<span class="ui-li-count" style="font-size:12pt">'+eventCount+'</span>';
				markup += '</a></li>';
			}
			// Append item to listview
			listview.append( $( markup ) );
		}
		// Refresh the view
	 	listview.listview('refresh');
}

function getNotificationPanelMarkup(kioskId)
{
	// Form the markup for the menu page
	var markup = '';
	var ct = getNotificationCount();
	if (ct > 0) {
		markup += '<div data-role="collapsible" data-theme="e" data-collapsed="true">';
	 	if (ct == 1)
	  		markup += '<h3>' +ct + ' '+$labeltext.notification+'</h3>';
	 	else
	   		markup += '<h3>' +ct + ' '+$labeltext.notifications+'</h3>';
	 	markup += '<ul data-role="listview" data-inset="true" class="mylist"  data-split-theme="b" data-split-icon="arrow-u">';
	 	if ($ntfyMdl) {
		 	for ( var etype in $ntfyMdl ) {
			 	if ( !$ntfyMdl.hasOwnProperty( etype ) )
				 	continue;
			 	markup += ' <li data-icon="false"> ';
			 	var reviewUrl = '';
			 	if (kioskId) {
				  	if (etype == 'ne')
					   reviewUrl = '#unsententities?op=' + etype;
				  	else
					 	reviewUrl = getReviewUrl(etype,kioskId,null);
				  	var onViewClick = "window.location.href='"+reviewUrl+"'";
				  	markup += '<a href="' + reviewUrl + '"><img src="jquerymobile/icons/'+getEventIcon(etype)+ '" alt="'+getEventMessage(etype) +'" class="ui-li-icon"  />'+ $ntfyMdl[etype].evct + ' ' + getEventMessage(etype) + '</a>';
            	  	if (etype != 'ne')
				   		markup += '<a href="#" data-theme="b"  onclick='+'"showConfirm('+"'"+$buttontext.sendnow+"'" +",'"+$sMsg[etype]+"?',"+'null,' + 'function(){sendTransactionForEntities('+"'"+etype+"'"+','+kioskId+')},getCurrentUrl())">'+$buttontext.sendnow+'</a>';
            	  	else {
            		  	var entityid=0;
            	  	  	markup += '<a href="#" data-theme="b"  onclick='+'"showConfirm('+"'"+$buttontext.sendnow+"'" +",'"+$sMsg[etype]+"?',"+'null,'+ 'function(){sendEntities('+"'"+entityid+"'"+')},getCurrentUrl())">'+$buttontext.sendnow+'</a>';
				  	}
				} else {
				    reviewUrl = '#unsententities?op=' + etype;		
				    var txtEntity = ' '+$labeltext.entities;
				   	if ($ntfyMdl[etype].kct == 1)
					    txtEntity = ' ' + $labeltext.entity;
					var onViewClick = "window.location.href='"+reviewUrl+"'";
					markup += ' <a href="' + reviewUrl + '"><img src="jquerymobile/icons/'+getEventIcon(etype)+ '" alt="'+getEventMessage(etype) +'" class="ui-li-icon" />'+ $ntfyMdl[etype].evct + ' ' + getEventMessage(etype);
					if (etype != 'ne')
				    	  markup += ' ( '+$ntfyMdl[etype].kct+txtEntity+' )</a>';
					else
				    	  markup += '</a>';
					if (etype != 'ne')
				       markup += '<a href="#" data-theme="b"  onclick='+'"showConfirm('+"'"+$buttontext.sendnow+"'" +",'"+$sMsg[etype]+"?',"+'null,'+ 'function(){sendTransactionForEntities('+"'"+etype+"'"+',null)},getCurrentUrl())">'+$buttontext.sendnow+'</a>';
					else {
				    	  var entityid = 0;
					  	  markup += '<a href="#" data-theme="b"  onclick='+'"showConfirm('+"'"+$buttontext.sendnow+"'" +",'"+$sMsg[etype]+"?',"+'null,'+ 'function(){sendEntities('+"'"+entityid+"'"+')},getCurrentUrl())">'+$buttontext.sendnow+'</a>';
					}
				}
				markup += '</li>';
	 		}
	 		markup += '</ul></div>';
     	}
    }
	return markup;
}
	

function getReviewUrl(eType,kioskId,linkedKid)
{
	var entity = getEntity($uid,kioskId);
	var hasCustomers = entity.csts && entity.csts.length > 0 ;
	var hasVendors = entity.vnds && entity.vnds.length > 0 ;
	var reviewUrl = '#';
    if (eType == 'ei')
	{
  	   	if (linkedKid) {
  		 	reviewUrl =  '#review?op=' + eType+'&kid='+kioskId+'&lkid='+linkedKid;
  		} else {
			var linkedEntity = hasLocalModificationsForLinkedEntity( eType, entity ,null);
			if ( !linkedEntity || linkedEntity == null )
				reviewUrl = '#review?op=' + eType+'&kid='+kioskId;
    		else
			  reviewUrl = '#unsentrelatedentities?op=' + eType+'&kid='+kioskId;

		}
	} else if (eType == 'er') {
		if (linkedKid)
   		{
   		 	reviewUrl =  '#review?op=' + eType+'&kid='+kioskId+'&lkid='+linkedKid;
   		}
		else {
			var linkedEntity = hasLocalModificationsForLinkedEntity( eType, entity ,null);
			if ( !linkedEntity || linkedEntity == null )
				reviewUrl = '#review?op=' + eType+'&kid='+kioskId;
     		else
			    reviewUrl = '#unsentrelatedentities?op=' + eType+'&kid='+kioskId;
     	}
	} else if (eType == 'ts') {
		if (linkedKid)
			reviewUrl = '#review?op=' + eType + '&kid=' + kioskId + '&lkid=' + linkedKid;
		else
			reviewUrl = '#unsentrelatedentities?op=' + eType + '&kid=' + kioskId;
	} else if (eType == 'no') {
		if (linkedKid) {
			reviewUrl = '#review?op=' + eType + '&kid=' + kioskId + '&lkid=' + linkedKid;
		} else {
			var linkedEntity = hasLocalModificationsForLinkedEntity( eType, entity ,null);
			if ( !linkedEntity || linkedEntity == null )
				reviewUrl = '#review?op=' + eType+'&kid='+kioskId;
			else
				reviewUrl = '#unsentrelatedentities?op=' + eType+'&kid='+kioskId;
		}
	} else
    	reviewUrl = '#review?op=' + eType+'&kid='+kioskId;
	return reviewUrl;
}



function sendTransactionForEntities(currentOperation,kioskId)
{
	var entityIds;
	if (currentOperation )
		$currentOperation = currentOperation;
	var currentIndex = 0;
	if (kioskId)
	{
		  entityIds = [];
		  entityIds.push(kioskId);
	}
	else if ($ntfyMdl[currentOperation])
	{
	    entityIds = $ntfyMdl[currentOperation].kids;
	}
	var currentLkidIndex = 0;
	var linkedEntityIds= getUnsentRelatedEntityIds($uid,entityIds[currentIndex],currentOperation);
	sendWithLocation( function(){sendTransaction(currentOperation,entityIds,currentIndex,linkedEntityIds,currentLkidIndex);});
} 

function sendEntities(kid)
{
	var typeName= 'entity';
	var currentOperation = 'ne';
	var currentIndex = 0;
	var entityIds = [];
	if (kid > 0)
		entityIds.push(kid);
	else
		entityIds = $ntfyMdl[currentOperation].kids;
	var typeName = $labeltext.entity;
	var uid = null;
	///numEntities = entityIds.length;
	var entityId = entityIds[currentIndex];
	var currentLkidIndex = 0;
    var linkedEntityIds= getUnsentRelatedEntityIds($uid,entityId,currentOperation);
	sendWithLocation( function(){ sendSetupEntity( typeName,entityIds,currentIndex,linkedEntityIds,currentLkidIndex, uid);});
}
    
    
//Get the operation message
function getEventMessage( op ) {
	var msg = $nMsg[ op ];
	if ( msg )
		return msg;
	return '';
}

//Get the operation icon
function getEventIcon( op ) {
	var icn = $etIcon[ op ];
	if ( icn )
		return icn;
	return '';
}

function getEventType(currentOperation)
{
	if (currentOperation =='ei')
		return 1;
	else if (currentOperation =='er')
		return 2;
	else if (currentOperation =='ew')
		return 3;
	else if (currentOperation =='es')
		return 4;
	else if (currentOperation =='ts')
		return 5;
	else if (currentOperation =='no')
		return 6;
	return 0;
}

function getlocalOEM( userId )
{
	var key = 'e.' + userId;
	return getObject( key );
}

function storelocalOEM( userId, oemData )
{
	var key = 'e.' + userId;
	storeObject( key, oemData );
}


// Android toast style notifications
function toast(message) {
	var $toast = $('<div class="ui-loader ui-overlay-shadow ui-body-e ui-corner-all">' + message + '</div>');

	$toast.css({
		display: 'block',
		background: '#595959', /// '#fff',
		color: '#ffffff',
		opacity: 0.90,
		position: 'fixed',
		padding: '7px',
		'text-align': 'center',
		width: '270px',
		left: ($(window).width() - 284) / 2,
		top: $(window).height() / 2 - 20
	});

	var removeToast = function(){
		$(this).remove();
	};

	$toast.click(removeToast);

	$toast.appendTo($.mobile.pageContainer).delay(3000);
	$toast.fadeOut(400, removeToast);
}
