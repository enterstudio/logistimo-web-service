<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.SecurityUtil" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.config.models.DomainConfig"%>
<%@page import="com.logistimo.config.models.FieldsConfig"%>
<%@page import="com.logistimo.constants.Constants" %>
<%@page import="com.logistimo.entities.entity.AccountsService" %>
<%@page import="com.logistimo.entities.service.AccountsServiceImpl" %>
<%@page import="com.logistimo.models.ICounter" %>
<%@page import="com.logistimo.orders.OrderUtils" %>
<%@page import="com.logistimo.pagination.PageParams"%>
<%@page import="com.logistimo.security.SecureUserDetails" %>
<%@page import="com.logistimo.services.ServiceException" %>
<%@page import="com.logistimo.services.Services" %>
<%@page import="com.logistimo.utils.Counter" %>
<%@page import="com.logistimo.utils.LocalDateUtil" %>
<%@page import="org.lggi.samaanguru.entity.*" %>
<%@ page import="org.lggi.samaanguru.utils.*" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
  ~ Copyright © 2017 Logistimo.
  ~
  ~ This file is part of Logistimo.
  ~
  ~ Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
  ~ low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
  ~
  ~ This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
  ~ Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
  ~ later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  ~ warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
  ~ for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
  ~ <http://www.gnu.org/licenses/>.
  ~
  ~ You can be released from the requirements of the license by purchasing a commercial license. To know more about
  ~ the commercial license, please contact us at opensource@logistimo.com
  --%>

<jsp:include page="../JSMessages.jsp" flush="true" />
<%
	// Get the user details and domain Id
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	String timezone = sUser.getTimezone();
	Locale locale = sUser.getLocale();
	Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
	// Get the request parameters
	String kioskIdStr = request.getParameter( "kioskid" );
	String orderStatusStr = request.getParameter( "status" );
	String offsetStr = request.getParameter( "o" );
	String sizeStr = request.getParameter( "s" );
	String showStr = request.getParameter( "show" );
	String subview = request.getParameter( "subview" );
	String otype = request.getParameter( "otype" );
	// Get start and end dates, if available
	String startDateStr = request.getParameter( "startdate" );
	String endDateStr = request.getParameter( "enddate" );
	String tag = request.getParameter( "tag" ); // kiosk tag, if any
	String tagType = request.getParameter( "tagtype" );
	
	// Get dates
	Date startDate = null, endDate = null;
	if ( startDateStr != null && !startDateStr.isEmpty() ) {
		try { 
			startDate = LocalDateUtil.parseCustom(startDateStr, Constants.DATE_FORMAT, null);
			System.out.println( "startDate: " + startDate + ", startDateStr: " + startDateStr );
		} catch ( Exception e ) {
			System.out.println( "Exception when parsing start date " + startDateStr + ": " + e.getMessage() );
		}
	}
	if ( endDateStr != null && !endDateStr.isEmpty() ) {
		try { 
			endDate = LocalDateUtil.parseCustom( endDateStr, Constants.DATE_FORMAT, null );
		} catch ( Exception e ) {
			System.out.println( "Exception when parsing end date " + endDateStr + ": " + e.getMessage() );
		}
	}
	// Get tag, if any
	boolean hasTag = ( tag != null && !tag.isEmpty() );
	if ( hasTag )
		tag = URLDecoder.decode( tag, "UTF-8" );
	else
		tag = null;
	// Get kiosk Id
	Long kioskId = null;
	if ( kioskIdStr != null && !kioskIdStr.isEmpty() )
		kioskId = Long.valueOf( kioskIdStr );
	// Flag to show the orders
	boolean show = "true".equals( showStr );
	// Get the role
	String role = sUser.getRole();
	boolean isOwner = ( SecurityUtil.compareRoles(role, IUserAccount.ROLE_DOMAINOWNER) >= 0 );
	boolean isManager = ( IUserAccount.ROLE_SERVICEMANAGER.equals( role ) );
	boolean hasManagableNumberOfKiosks = true;
	if ( isManager ) {
		if ( otype == null || otype.isEmpty() )
			otype = IOrder.TYPE_SALE; // default to sales orders for manager view
		hasManagableNumberOfKiosks = ( Counter.getUserToKioskCounter(domainId, userId).getCount() <= Constants.MAX_LIST_SIZE_FOR_CONTAINS_QUERY );
	}
	if ( isOwner || hasManagableNumberOfKiosks || kioskId != null )
		show = true;
	// Get list of kiosks to present in the select box
	List<IKiosk> kiosks = null;
	IKiosk k = null;
	boolean singleKiosk = false;
	boolean hasSalesOrders = false, hasPurchaseOrders = false;
	DomainConfig dc = DomainConfig.getInstance( domainId );
	FieldsConfig orderFields = dc.getOrderFields();
	IUserAccount user = null;
	boolean kioskSelected = false;
	try {
		AccountsService as = Services.getService( AccountsServiceImpl.class, locale );
		user = as.getUserAccount( userId );
		Long primaryKioskId = user.getPrimaryKiosk();
		kiosks = as.getKiosks( user, domainId, null, new PageParams( null, 2 ) ).getResults();
		if ( kiosks.size() == 1 && kioskId == null ) { // if only 1 kiosk associated, and no kiosk is passed, then set the kiosk ID to be this
			kioskId = kiosks.get(0).getKioskId();
			show = true;
			singleKiosk = true;
		} else if ( kioskId == null && primaryKioskId != null ) {
			kioskId = primaryKioskId;
			show = true;
		}
		if ( kioskId != null ) {
			kioskSelected = true;
			k = as.getKiosk( kioskId, false );
			int numCustomers = Counter.getKioskLinkCounter( domainId, kioskId, IKioskLink.TYPE_CUSTOMER ).getCount();
			int numVendors = Counter.getKioskLinkCounter( domainId, kioskId, IKioskLink.TYPE_VENDOR ).getCount();
			int numSalesOrders = Counter.getOrderCounter(domainId, kioskId, IOrder.TYPE_SALE ).getCount();
			int numPurchaseOrders = Counter.getOrderCounter( domainId, kioskId, IOrder.TYPE_PURCHASE ).getCount();
			// Check if there sales/purchase orders, or both
			hasSalesOrders = ( numSalesOrders > 0 );
			hasPurchaseOrders = ( numPurchaseOrders > 0 );
		}
	} catch ( ServiceException e ) {
		e.printStackTrace();
	}
	// Show sales/purchase orders
	boolean showSalesOrders = ( IOrder.TYPE_SALE.equals( otype ) || ( singleKiosk && hasSalesOrders ) );
	boolean showPurchaseOrders = ( IOrder.TYPE_PURCHASE.equals( otype ) || ( singleKiosk && hasPurchaseOrders && !hasSalesOrders ) );
	boolean showDomainOrders = ( !kioskSelected && !isManager ); /// ( isOwner && !kioskSelected );
	String otypeDisplay = "";
	if ( otype == null || otype.isEmpty() ) {
		if ( showPurchaseOrders ) {
			otype = IOrder.TYPE_PURCHASE;
		} else if ( showSalesOrders ) {
			otype = IOrder.TYPE_SALE;
		} else if ( kioskSelected ) {
			otype = IOrder.TYPE_SALE;
			if ( hasPurchaseOrders && !hasSalesOrders )
				otype = IOrder.TYPE_PURCHASE;
		}
	}
	// Offset for pagination
	int offset = 1;
	if ( offsetStr != null && !offsetStr.isEmpty() ) {
		try {
			offset = Integer.parseInt( offsetStr );
		} catch ( NumberFormatException e ) {
			// do nothing; offset remains as default
		}
	}
	// Size for pagination
	int size = PageParams.DEFAULT_SIZE;
	if ( isManager && kioskId == null )
		size = 100;
	if ( sizeStr != null && !sizeStr.isEmpty() ) {
		try {
			size = Integer.parseInt( sizeStr );
		} catch ( NumberFormatException e ) {
			// do nothing; offset remains as default
		}
	}
	// Previous offset of pagination
	int prevOffset = offset - size;
	// Form the pagination URLs
	String baseUrl = null;
	String baseUrlNoTags = null;
	int nextOffset = 0;
	int maxRange = 0;
	String ordersUrl = null;
	String queryString = "";
	String queryStringWithoutOtype = "";
	if ( show ) {
		// Form the order data URL
		// NOTE: Send a size that is 1 bigger than required to help determine if pagination is required
		ordersUrl = "/s/reports?type=" + ReportsConstants.TYPE_ORDERS +
						  "&size=" + size + "&offset=" + offset +
						  "&cursortype=" + Constants.CURSOR_ORDERS;
		if ( orderStatusStr != null && !orderStatusStr.isEmpty() )
			queryString += "&status=" + orderStatusStr;
		if ( kioskSelected ) {
			queryString += "&kioskid=" + kioskId;
		} else {
			queryString += "&domainid=" + domainId;
		}
		queryStringWithoutOtype = "show=true&subview=" + subview + "&" + queryString; // save a copy of query string without otype (for use in tab URLs)
		if ( ( kioskSelected || isManager ) && otype != null && !otype.isEmpty() ) // add order type to query string, if kiosk is selected
			queryString += "&otype=" + otype;
		if ( startDate != null ) {
			queryString += "&startdate=" + startDateStr;
			queryStringWithoutOtype += "&startdate=" + startDateStr;
		}
		if ( endDate != null ) {
			queryString += "&enddate=" + endDateStr;
			queryStringWithoutOtype += "&enddate=" + endDateStr;
		}
		// Update orders data URL
		if ( !queryString.isEmpty() )
			ordersUrl += queryString;
		// Base URL for pagination
		baseUrl = "orders.jsp?show=true&subview=" + subview;
		if ( startDate != null )
			baseUrl += "&startdate=" + startDateStr;
		if ( endDate != null )
			baseUrl += "&enddate=" + endDateStr;
		if ( !queryString.isEmpty() )
			baseUrl += queryString;
		baseUrlNoTags = baseUrl;
		if ( hasTag ) {
			baseUrl += "&tag=" + tag;
			if ( tagType != null && !tagType.isEmpty() ) {
				baseUrl += "&tagtype=" + tagType;
				if ( TagUtil.TYPE_ENTITY.equals( tagType ) )
					ordersUrl += "&ktag=" + tag;
				else if ( TagUtil.TYPE_ORDER.equals( tagType ) )
					ordersUrl += "&otag=" + tag;
			}
		}
		// Get the next offset
		nextOffset = offset + size;
		// Get the maxRange for display purposes
		maxRange = nextOffset - 1;
	} // end if ( show )
	// Get the URL for selected action (e.g. generate CSV)
	String qString = request.getQueryString();
	String urlSelectActionCSV = "vieworders.jsp?selectaction=csv";
	if ( qString != null && !qString.isEmpty() )
		urlSelectActionCSV += "&" + qString;
	// Get the orders count, depending on what filters are selected
	int total = 0;
	if ( ( orderStatusStr == null || orderStatusStr.isEmpty() ) && startDate == null && endDate == null ) { // let total be 0 in case filters are present
		ICounter counter = null;
		if ( hasTag )
			counter = Counter.getOrderCounter( domainId, tag );
		else
			counter = Counter.getOrderCounter( domainId, kioskId, otype );
		total = counter.getCount();
	}
	// Date format for date filter
	///String dateFormat = ( "us".equalsIgnoreCase( locale.getCountry() ) ? "mm/dd/yy" : "dd/mm/yy" );
%>
<!-- Our JS -->
<script type="text/javascript" src="../../js/sg.js"></script>
<script language="javascript" type="text/javascript" src="../../js/validate.js"></script>
<!-- Maps JS to show map of order placement, if needed -->
<script type="text/javascript" src="https://maps.google.com/maps/api/js?key=AIzaSyCWMVddkt2dvTfDrZikF77Mana4g8mZ7Vg&sensor=false"></script>
<script src="../../js/oms.min.js"></script> <!--  Overlapping Marker Spiderfier script -->
<!-- Autocomplete box -->
<link rel="stylesheet" href="../../autocomplete/TextboxList.css" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="../../autocomplete/TextboxList.Autocomplete.css" type="text/css" media="screen" charset="utf-8" />
<script src="../../js/jquery-1.3.2.min.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/GrowingInput.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.Autocomplete.js" type="text/javascript" charset="utf-8"></script>
<script src="../../autocomplete/TextboxList.Autocomplete.Binary.js" type="text/javascript" charset="utf-8"></script>
<!-- JQuery UI css and scripts -->
<link type="text/css" href="../../jqueryui/css/redmond/jquery-ui-1.8.22.custom.css" rel="stylesheet" />
<script type="text/javascript" src="../../jqueryui/js/jquery-ui-1.8.22.custom.min.js"></script>
<style type="text/css">
	.kioskfilter input {border:0;}
</style>
<fmt:bundle basename="Messages">
<fmt:message key="add" var="add"/>
<fmt:message key="new" var="new"/>
<fmt:message key="order" var="order"/>
<fmt:message key="exportspreadsheet" var="exportspreadsheet" />
<fmt:message key="typetogetsuggestions" var="typetogetsuggestions" />
<fmt:message key="onlyoneitemcanbeselected" var="onlyoneitemcanbeselected" />
<script type="text/javascript">
var vendorDropDown = null; // Global variable to holding the vendor drop-down element (e.g. TextboxList)
var selectedOrders = []; // a list of selected order Ids, to be exported 
// Default format for all datepickers
$.datepicker.setDefaults( { dateFormat: 'dd M yy' } );
// Batch form data, if batch number used in order edit/add forms
var $batches = {};

function addOrders() {
	// Get the kioskId and name
	var kioskIdE = document.getElementById( "kioskid" );
	if ( kioskIdE.value == '' ) {
		alert( JSMessages.selectkioskmsg );
		return;
	}
	location.href = "orders.jsp?show=false&action=create<%= subview != null ? "&subview=" + subview : "" %>&kioskid=" + kioskIdE.value;
}

// Update the order send message, by replacing template variables
// NOTE: THIS function is also used in TransactionUtil.renderOrderFieldsInHTML(); any changes to this function are to be reflected there as well
function updateOrderSendMessage( varId, val, statusToMsgElem, statusId, transporterDetails ) {
	var statusToMsg = null;
	if ( statusToMsgElem.value != '' )
		statusToMsg = JSON.parse( statusToMsgElem.value );
	var msg = '';
	var subscribers = null;
	if ( statusToMsg != null && statusToMsg[ statusId ] ) {
		msg = statusToMsg[ statusId ].message;
		subscribers = statusToMsg[ statusId ].subscribers;
	}
	if ( ( !msg || msg == '' ) && statusToMsg != null && statusToMsg[''] ) {
		msg = statusToMsg[''].message; // check if there is a status message matching any status
		subscribers = statusToMsg[''].subscribers;
	}
	if ( !msg )
		msg = '';
	// Replace the given variable with the value
	document.getElementById( 'message1' ).value = msg.replace( varId, val );
	// Add transporter detail to the message, as needed
	if ( transporterDetails != null && transporterDetails != '' && statusId == 'cm' ) // on shipped status and transport details being present
		document.getElementById( 'message1' ).value += ' [' + transporterDetails + ']';
	// Auto-check users for SMS notification, if configured
	if ( subscribers && subscribers != null && subscribers.length > 0 ) {
		// Get the array of subscriber ids
		var subscriberIds = [];
		for ( var i = 0; i < subscribers.length; i++ )
			subscriberIds.push( subscribers[i].id );
		// Get the user check boxes
		var msgUsers = document.getElementsByName( 'msgusers' );
		if ( msgUsers != null ) {
			for ( var i = 0; i < msgUsers.length; i++ ) {
				if ( $.inArray( msgUsers[i].value, subscriberIds ) != -1 )
					msgUsers[i].checked = true;
				else
					msgUsers[i].checked = false;
			}
		}
	}
	// Enable/disable fields asscoiated with a given order status
	<%
	for ( int i = 0; i < IOrder.STATUSES.length; i++ ) {
		%>
		var div = document.getElementById( 'fields_<%= IOrder.STATUSES[i] %>' );
		if ( div != null && statusId != null ) {
			if ( statusId && statusId == '<%= IOrder.STATUSES[i] %>' )
				div.style.display = 'block';
			else
				div.style.display = 'none';
		} // end div
		<%
	} // end for
	%>
}

//Change the status of an order
function changeOrderStatus( orderId, newStatusCode, newStatusName, rowIndex, data, view, table, isTransporterMandatory, hasTransporter, availableCredit, creditEnforcementStatus, hasNoVendor, orderSize ) {
	// Check if a vendor is present for this order
	if ( hasNoVendor ) {
		alert( 'No vendor was specified for this order. Please associate a vendor before processing this further.' );
		return false;
	}
	if ( newStatusCode == 'cm' || newStatusCode == 'fl' ) {
		if ( orderSize == 0 ) {
			alert( 'You cannot ship or fulfill an order with no items. Please add items to this order.' );
			return false;
		}
	}
	// If available credit is specified, then throw alert
	var enforceCreditCheck = false;
	if ( creditEnforcementStatus != null && ( ( creditEnforcementStatus == newStatusCode ) || ( creditEnforcementStatus == 'cf' && newStatusCode == 'cm' ) ) )
		enforceCreditCheck = true;
	if ( enforceCreditCheck && availableCredit != null ) {
		alert( JSMessages.ordercannotbe + ' ' + newStatusName + '. ' + JSMessages.ordercostexceedscredit + ' ' + availableCredit );
		return false;
	}
	// Check if batch-enabled items have been allocated - either not at all (not allowed) or partially allocated (allowed)
	if ( newStatusCode == 'cm' || newStatusCode == 'fl' ) {
		var unallocatedItemsBatch = document.getElementById( 'unallocateditemsbatch' );
		if ( unallocatedItemsBatch ) {
			alert( JSMessages.batch_ordernotallocatedfully1 + ': ' + unallocatedItemsBatch.value + '. ' + JSMessages.batch_ordernotallocatedfully2 );
			return false;
		}
		var partiallyAllocatedItemsBatch = document.getElementById( 'partiallyallocateditemsbatch' );
		if ( partiallyAllocatedItemsBatch ) {
			if ( !confirm( JSMessages.batch_orderallocatedpartially + ': ' + partiallyAllocatedItemsBatch.value + '. ' + JSMessages._continue + '?' ) )
				return false;
		}
	}
	// Check stock errors or other mandatory attributes in order, if order is being shipped
	if ( newStatusCode == 'cm' ) { // shipped/completed
		// Check stock errors
		if ( !validateOrderQuantities( document.getElementById('vstockerrors') ) )
			return false;
		// Check if transporter is mandatory on shipping
		if ( isTransporterMandatory && !hasTransporter ) {
			alert( JSMessages.transportermandatory );
			return false;
		}
	}
	// Show the loading indicator (esp. needed if sending message)
	document.getElementById('changeOrderStatusLoader').style.display = 'block';
	if ( newStatusCode == '' ) {
		alert( JSMessages.selectstatusmsg );
		document.getElementById('changeOrderStatusLoader').style.display = 'none';
		return false;
	}
	// Get the message, if any
	var message = document.getElementById( 'message1' ).value;
	var msgusers = '';
	if ( message && message != '' ) {
		if ( message.length > 160 )
			message = message.substring( 0, 160 );
		message = encode( message ); // URL encode the message
		// Check if messages has to be sent via SMS
		var msguserElements = document.getElementsByName( 'msgusers' );
		for ( var i = 0; i < msguserElements.length; i++ ) {
			if ( msguserElements[i].checked ) {
				if ( msgusers != '' )
					msgusers += ',';
				msgusers += msguserElements[i].value;
			}
		}
	}
	// Update the order status and display the new order 
	var url = 'vieworder.jsp?oid=' + orderId + '&action=changestatus&status=' + newStatusCode + '&row=' + rowIndex + '&msg=' + message;
	var errMsg = '';
	<%
	// Check if any custom fields were present
	if ( orderFields != null ) {
		for ( int i = 0; i < IOrder.STATUSES.length; i++ ) {
			List<FieldsConfig.Field> list = orderFields.getByStatus( IOrder.STATUSES[i] );
			if ( list != null && !list.isEmpty() ) {
				Iterator<FieldsConfig.Field> fieldsIt = list.iterator();
				while ( fieldsIt.hasNext() ) {
					FieldsConfig.Field f = fieldsIt.next();
					if ( f == null )
						continue;
					%>
					if ( newStatusCode == '<%= IOrder.STATUSES[i] %>' ) {
						var fieldElem = document.getElementById( '<%= f.getId() %>_<%= IOrder.STATUSES[i] %>' );
						if ( fieldElem && fieldElem != null ) {
							fieldElem.value = trim( fieldElem.value );
							<% if ( f.mandatory ) { %>
							if ( fieldElem.value == '' ) {
								if ( errMsg != '' )
									errMsg += ', ';
								errMsg += '<%= f.name %>';
							}
							<% } %>
							url += '&' + fieldElem.name + '=' + encode( fieldElem.value );
						}
					}
					<%
				} // end while
			} // end if ( list != null ... )
		} // end for
	} // end if ( orderFields != null )
	%>
	if ( errMsg != '' ) {
		alert( JSMessages.entermandatoryfields + ': ' + errMsg );
		document.getElementById('changeOrderStatusLoader').style.display = 'none';
		return false;
	}
	if ( msgusers != '' )
		url += '&msgusers=' + msgusers;
	getOrderData( url, newStatusName, rowIndex, 3, data, view, table );  // col. 3 is the status column in 'data'
}

// Update the package size in the order
function editPackageSize( oid, packageSize ) {
	packageSize = encode( trim( packageSize ) );
	// Start the loading indicator
	document.getElementById('packagesizeloader').style.display='block';
	// Update transporter details
	var orderUrl = 'vieworder.jsp?action=editpackagesize&oid=' + oid + '&pksz=' + packageSize; 
	getOrderData( orderUrl );
}

// Update the transporter in the order
function editTransporter( oid, transporterType, transporterId, mandatory ) {
	transporterId = trim( transporterId );
	if ( transporterId == '' && mandatory ) {
		alert( 'Transporter has to be specified' );
		return;
	}
	transporterId = encode( transporterId );
	// Start the loading indicator
	document.getElementById('transporterloader').style.display='block';
	// Update transporter details
	var orderUrl = 'vieworder.jsp?action=edittransporter&oid=' + oid + '&tsptype=' + transporterType + '&tspid=' + transporterId; 
	getOrderData( orderUrl );
}

// Update payment in the order
function editPayment( oid, payment, totalPrice, paidSoFar, paymentOption ) {
	payment = trim( payment );
	if ( payment == '' ) {
		alert( JSMessages.nopayment );
		return;
	}
	payment = parseFloat( payment );
	paidSoFar = parseFloat( paidSoFar );
	totalPrice = parseFloat( totalPrice );
	if ( ( paidSoFar + payment ) > totalPrice ) {
		if ( !confirm( JSMessages.paymenthigher ) )
			return;
	}
	// Get the payment option
	paymentOption = encode( trim( paymentOption ) );
	// Start the loading indicator
	document.getElementById('paymentloader').style.display='block';
	// Update payment details
	var orderUrl = 'vieworder.jsp?action=editpayment&oid=' + oid + '&amount=' + payment + '&popt=' + paymentOption; 
	getOrderData( orderUrl );
}

// Confirm fulfillment time in the order
function confirmFulfillmentTime( oid ) {
	// Get the selected confirmed fulfillment time
	var confirmedFulfillmentTime = null;
	var radios = document.eftsform.efts;
	for ( var i = 0; i < radios.length; i++ ) {
		if ( radios[i].checked ) {
			confirmedFulfillmentTime = radios[i].value;
			break;
		}
	}
	if ( confirmedFulfillmentTime == null ) {
		alert( 'Please select a fulfillment time' );
		return;
	}
	// Start the loading indicator
	document.getElementById('confirmFulfillmentTimeLoader').style.display='block';
	// Update payment details
	var orderUrl = 'vieworder.jsp?action=confirmfulfillmenttime&oid=' + oid + '&cft=' + confirmedFulfillmentTime; 
	getOrderData( orderUrl );
}

// Change the vendor
function changeVendor( oid ) {
	var vendorId = null;
	var vendorElement = document.getElementById('vendorid');
	if ( vendorElement )
		vendorId = trim( vendorElement.value );
	if ( vendorId == null || vendorId == '' ) {
		alert( 'Please select a vendor' );
		return;
	}
	// Reset the vendor drop-down
	resetVendorDropDown();
	// Start the loading indicator
	document.getElementById('vendorLoader').style.display='block';
	// Update payment details
	var orderUrl = 'vieworder.jsp?action=changevendor&oid=' + oid + '&vid=' + vendorId; 
	getOrderData( orderUrl );
}

// Reset the global vendor drop-down
function resetVendorDropDown() {
	vendorDropDown = null;
}

function resetMsgUsers() {
	var boxes = document.getElementsByName( 'msgusers' );
	if ( boxes == null )
		return;
	for ( var i = 0; i < boxes.length; i++ )
		boxes[ i ].checked = false;
}

//Show a CSV export panel where orders can be selected
function exportCSV() {
	if ( selectedOrders.length == 0 ) { // do batch export
		<% if ( isManager ) { %>
		var kioskId = document.getElementById( 'kioskid' ).value;
		if ( kioskId == '' ) {
			alert( 'Please select an entity and then export.' );
			return;
		}
		<% } %>
		if ( confirm( JSMessages.export_confirmall1 + '\n\n' + JSMessages.export_confirmall2 + ' <%= user.getEmail() %> ' + JSMessages.export_confirmall3 ) ) {
			var url = '/s/export?action=sbe&type=orders&sourceuserid=<%= userId %>&userids=<%= userId %>&domainid=<%= domainId %>';
			<% if ( !queryString.isEmpty() ) { %>
			url += '<%= queryString %>';
			<% } %>
			// Add start & end dates, if specified
			var startDate = $( '#startdate' ).datepicker( 'getDate' );
			if ( startDate )
				url += '&from=' + startDate.getDate() + '/' + ( startDate.getMonth() + 1 ) + '/' + startDate.getFullYear() + ' 00:00:00';
			var endDate = $( '#enddate' ).datepicker( 'getDate' );
			if ( endDate )
				url += '&to=' + endDate.getDate() + '/' + ( endDate.getMonth() + 1 ) + '/' + endDate.getFullYear() + ' 00:00:00';
			// Get the status, if selected
			var statusE = document.getElementById( 'status' );
			var status = statusE.options[ statusE.selectedIndex ].value; 
			if ( status != '' )
				url += '&status=' + status;
			// Export all orders
			url += '&view=<%= Constants.VIEW_ORDERS %>&subview=<%= subview %>';
			location.href = url;
		}
	} else if ( selectedOrders.length > 0 ) {
		if ( confirm( selectedOrders.length + ' ' + JSMessages.exportedorders ) ) {
			// export to spreadsheet
			// Get CSV order IDs
			var csv = '';
			for ( var i = 0; i < selectedOrders.length; i++ ) {
				if ( csv != '' )
					csv += ',';
				csv += selectedOrders[i];
			}
			// Send data to server	
			location.href = '/s/export?type=orders&attachtoemail&format=csv&values=' + csv;
		}
	}
}

// Validate viewing of orders
function viewOrders() {
	///if ( isManager && document.getElementById( "kioskid" ).value == '' ) {
	///	alert( JSMessages.selectkioskmsg );
	///	return false;
	///}
	// Get the URL for view along with filters
	var url = 'orders.jsp?subview=orders&show=true' + getFilterQueryString( false );
	location.href = url;
}

// Get the filter query string
function getFilterQueryString( includeTimeInDate ) {
	var qs = '';
	var kioskId = document.getElementById( 'kioskid' ).value;
	if ( kioskId != '' )
		qs = '&kioskid=' + kioskId;
	// Add start & end dates, if specified
	var startDate = $( '#startdate' ).datepicker( 'getDate' );
	if ( startDate ) {
		qs += '&startdate=' + startDate.getDate() + '/' + ( startDate.getMonth() + 1 ) + '/' + startDate.getFullYear();
		if ( includeTimeInDate )
			qs += ' 00:00:00';
	}
	var endDate = $( '#enddate' ).datepicker( 'getDate' );
	if ( endDate ) {
		qs += '&enddate=' + endDate.getDate() + '/' + ( endDate.getMonth() + 1 ) + '/' + endDate.getFullYear();
		if ( includeTimeInDate )
			qs += ' 00:00:00';
	}
	// Get the status, if selected
	var statusE = document.getElementById( 'status' );
	var status = statusE.options[ statusE.selectedIndex ].value; 
	if ( status != '' )
		qs += '&status=' + status;
	return qs;
}

function initDropDowns() {
	<% if ( k == null ) { %>
	var placeholder = '${typetogetsuggestions}';
	<% } else { %>
	var placeholder = '${onlyoneitemcanbeselected}';	
	<% } %>
	// Init. kiosk selector
	var kioskid = new $.TextboxList('#kioskid', { unique: true,
								    max: 1,
								    bitsOptions: {
										 box: { deleteButton: false }
									 },
								    plugins: { 	autocomplete: 
												{ 	method:'binary',
										  			onlyFromValues: true,
										  			placeholder: placeholder,
										  			maxResults: 50,
										  			minLength: 1,
										  			queryRemote: true,
										  			remote: { url: '/s/list.jsp?type=kiosks' }
											  	}
  											}
								  } );
	  kioskid.clear();
	<% if ( k != null ) { %>
	kioskid.add( '<%= k.getName() %>', '<%= k.getKioskId() %>', null );
	<% } %>
}

// Initialize vendor selection drop-down
function initVendorDropDown( vendorId, vendorName ) {
	var placeholder = '';
	if ( vendorId == null )
		placeholder = JSMessages.typetogetsuggestions;
	else
		placeholder = JSMessages.onlyoneitemcanbeselected;	
	// Init. kiosk selector
	if ( vendorDropDown == null ) {
		vendorDropDown = new $.TextboxList('#vendorid', { unique: true,
							    				max: 1,
							    				bitsOptions: {
													 box: { deleteButton: false }
												 },
											    plugins: { 	autocomplete: 
															{ 	method:'binary',
													  			onlyFromValues: true,
													  			placeholder: placeholder,
													  			maxResults: 50,
													  			minLength: 1,
													  			queryRemote: true,
													  			remote: { url: '/s/list.jsp?type=kiosks' }
														  	}
			  											}
											  } );
		 vendorDropDown.clear();
	}
	if ( vendorId != null )
		vendorDropDown.add( vendorName, vendorId, null );
}

// Init. date fiters
function initDateFilters() {
	// Get today's date
	var today = new Date();
	var older = new Date();
	older.setFullYear( today.getFullYear() - 5 );
	// Initialize from and to dates
	var fromDate = $( "#startdate" ).datepicker( {
									maxDate: today,
									minDate: older
								 } );
	var toDate = $( "#enddate" ).datepicker( {
		maxDate: today,
		minDate: older
	 } );
	 // Set start/end dates, if pre-selected
	 <% if ( startDate != null ) { 
		 Calendar cal = GregorianCalendar.getInstance();
		 cal.setTime( startDate );
		 %>
		 var sDate = new Date( <%= cal.get( Calendar.YEAR ) %>, <%= cal.get( Calendar.MONTH ) %>, <%= cal.get( Calendar.DATE ) %> );
		 fromDate.datepicker( 'option', 'defaultDate', sDate );
		 fromDate.datepicker( 'setDate', sDate );
	 <% } // end if ( startDate != null ) %>
	 <% if ( endDate != null ) { 
		 Calendar cal = GregorianCalendar.getInstance();
		 cal.setTime( endDate );
		 %>
		 var eDate = new Date( <%= cal.get( Calendar.YEAR ) %>, <%= cal.get( Calendar.MONTH ) %>, <%= cal.get( Calendar.DATE ) %> );
		 toDate.datepicker( 'option', 'defaultDate', eDate );
		 toDate.datepicker( 'setDate', eDate );
	 <% } // end if ( startDate != null ) %>
}

window.onload = function() {
	initDropDowns();
	initDateFilters();
}
</script>
<div class="yui-g" style="border-bottom:1px solid #E6E6E6;">
	<form id="sgFilterByKiosk" class="sgForm" action="orders.jsp">
	<table class="kioskfilter" style="width:100%">
	<tr>
	<td>
			<fieldset>			
				<legend><fmt:message key="kiosk"/>:</legend>
				<input type="text" name="kioskid" id="kioskid" />
			</fieldset>	
	</td>
	<td valign="top">
		<fieldset>			
			<legend><fmt:message key="status"/>:</legend>
			<select id="status" name="status">
				<option value="" <%= "".equals( orderStatusStr ) ? "selected" : "" %>><fmt:message key="all"/></option>
				<%
				for ( int j = 0; j < IOrder.STATUSES.length; j++ ) {
					%>
					<option value="<%= IOrder.STATUSES[j] %>" <%= IOrder.STATUSES[j].equals( orderStatusStr ) ? "selected" : "" %>><%= OrderUtils
							.getStatusDisplay(IOrder.STATUSES[j], locale) %></option>
					<%
				}
				%>
			</select>
			<input type="hidden" name="subview" value="<%= subview %>" />
			<input type="hidden" name="show" value="true" />
			<% if ( otype != null && !otype.isEmpty() ) { %>
			<input type="hidden" name="otype" value="<%= otype %>" />
			<% } %>		
			&nbsp;
			<button type="button" onclick="viewOrders();"><fmt:message key="view" /></button>
			&nbsp;
			<button type="button" onclick="addOrders()">${add}</button>
			&nbsp;
			<button type="button" onclick="exportCSV()"><fmt:message key="export" /></button>
		</fieldset>
	</td>
	<td style="text-align:right">
		<a href="orders.jsp?maps=true&subview=demandboard"><img src="../../images/map.png" title="Map" valign="middle" align="right" width="30" height="30" alt="Map"/></a>
	</td>
	</tr>
	</table>
	<table style="margin-top:10px;">
		<tr>
	  	 <td>
  			<!--  Date range -->
		  	<fieldset>
			<legend><fmt:message key="time"/>:</legend>
				<b><fmt:message key="from"/></b>: <input type="text" name="startdate" id="startdate" /> &nbsp;
				<b><fmt:message key="to"/></b>: <input type="text" name="enddate" id="enddate" />
			</fieldset>
		 </td>
		</tr>
	</table>
	</form>
</div>
<% if ( show ) { %>
<div id="doc3">
	<h2 class="sgHeading"><fmt:message key="orders"/>
	<% if ( k != null ) { %>
	<fmt:message key="for"/> <a href="../setup/setup.jsp?subview=kiosks&form=kioskdetails&id=<%= kioskId %>" target="_new"><%= k.getName() %></a>
	<% } %>
	<% if ( startDate != null ) { %>
		<fmt:message key="from" /> <%= LocalDateUtil.format( startDate, locale, timezone, true ) %> 
	<% } %>
	<% if ( endDate != null ) { %>
		<fmt:message key="until" /> <%= LocalDateUtil.format( endDate, locale, timezone, true ) %>
	<% } %>
	<% if ( orderStatusStr != null && !orderStatusStr.isEmpty() ) { %>
			(<fmt:message key="status"/>: <%= OrderUtils.getStatusDisplay(orderStatusStr, locale).toUpperCase() %>)
	<% } %>
	</h2>
</div>
<% if ( !showDomainOrders ) { %>
	<!--  Sales/purchase order menu -->
	<div style="margin-top:15px;">
		<ul id="nmenu">
		  <li class="<%= IOrder.TYPE_SALE.equals( otype ) ? "selected" : "" %>"><a href="orders.jsp?<%= queryStringWithoutOtype %>&otype=<%= IOrder.TYPE_SALE %>"><fmt:message key="salesorders" /></a> (<fmt:message key="from"/> <fmt:message key="customers"/>)</li>
		  <li class="<%= IOrder.TYPE_PURCHASE.equals( otype ) ? "selected" : "" %>"><a href="orders.jsp?<%= queryStringWithoutOtype %>&otype=<%= IOrder.TYPE_PURCHASE %>"><fmt:message key="purchaseorders" /></a> (<fmt:message key="to"/> <fmt:message key="vendors"/>)</li>
		</ul>
	</div>
<% } // end if ( !showDomainOrders ) %>
<!--  Scripts for Google Data Visualization -->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
    // Load the Visualization API and the ready-made Google table visualization.
    google.load('visualization', '1', {packages: ['table,piechart,annotatedtimeline']});
    
    // Set a callback to run when the API is loaded - this will run the summary count table/chart visualization.
  	google.setOnLoadCallback( drawOrdersTable );

    var data; // declare globally, so that vieworder.jsp (which shows an order in a div orderDetails of this page) can also access that
    var view; // view of data that is shown
    var table; // table object to be drawn
	// Send the queries to the data sources and draw visualization
    function drawOrdersTable() {
    	// Show the loading gif
        showLoader( 'dataTable' );
    	// Form the query with the reports URL
      	var query = new google.visualization.Query( '<%= ordersUrl %>' );
      	// Send the data to the Summary Counts Table visualizer	
    	query.send( handleOrdersResponse );   
    }
    
    // Handle the data source query response for summary count Tabular report
    function handleOrdersResponse( response ) {
    	if (response.isError()) {
        	// Hide the navigation panels
        	document.getElementById( 'dataNavigatorTop' ).style.display = '<%= hasTag ? "block" : "none" %>';
        	// Stop the loading indicator and show error
        	document.getElementById( 'dataTable' ).innerHTML = response.getDetailedMessage();
      		return;
    	}
		// Get the data table and render the Table report
   	 	data = response.getDataTable();
   	 	// Create a view on this data (with the first few columns hidden)
   	 	view = new google.visualization.DataView( data );
   	 	view.hideColumns( [ 5,7,8,9,10,11,12,13,14 ] );
   		// Hide the last row, which may contain the cursor
   	 	var lastrow = data.getNumberOfRows() - 1; // NOTE: this is also the number of valid data records (it is 1 more than required - to determine pagination)
   	 	view.hideRows( [ lastrow ] ); // hide this row which has the cursor
   	 	var hasMoreResults = false;
   	 	if ( lastrow >= <%= size %> ) { 
   	   	 	hasMoreResults = true;
   	 	}
   		// Update the table page navigator
   	 	updateNavigator( data.getNumberOfRows() - 1, data.getFormattedValue( lastrow, 0 ), hasMoreResults ); // NOTE: numeberOfRows() - 1: -1 on number of rows to account for the last row with a cursor cell
   	 	// Init. table
   	 	table = new google.visualization.Table( document.getElementById( 'dataTable' ) );
   		// Add a listner to notify when the data is ready - this can help stop a loading screen, for instance
 	    // or show the navigation bar
 	    // NOTE: THIS SHOULD BE ADDED BEFORE THE TABLE IS DRAWN
   	 	google.visualization.events.addListener( table, 'ready',
   		    function(event) {
   		      // Show the table navigator (which is initially hidden)
     		  document.getElementById( 'dataNavigatorTop' ).style.display = 'block';
   		    });
		// Draw the table
   	 	table.draw( view, { allowHtml: true } );
   		// Add a select event handler, to show all the details
   		// NOTE: THIS SHOULD BE ADDED ONLY AFTER THE TABLE IS DRAWN
   	 	google.visualization.events.addListener(table, 'select',
   	      function(event) {
   	 		processSelectedOrder( data, table.getSelection() );
   	      });
    }

	// Update the page range indication and navigation details
	function updateNavigator( numResults, nextCursor, hasMoreResults ) {
		// Update range of values displayed
		var maxRange = <%= maxRange %>;
		if ( numResults < <%= size %> )
			maxRange = <%= offset %> + numResults - 1;
		// Get the next/prev. offsets
		var nextOffset = <%= offset + size %>;
		var prevOffset = <%= offset - size %>;
		var htmlString = '<b><%=offset%>-' + maxRange + '</b>';
		<% if ( total > 0 ) { %>
		htmlString += ' ' + JSMessages.of + ' <b><%= total %></b>';
		<% } %>
		// If manager, and not for a specific kiosk, then show the last few entries
		var noPagination = ( <%= isManager %> && <%= kioskId == null %> );
		// Update next URL
		if ( !noPagination && hasMoreResults ) {
			var nextUrl = '<%= baseUrl %>&o=' + nextOffset;
			htmlString += '&nbsp;&nbsp;&nbsp;<a href="' + nextUrl + '">' + JSMessages.next +  '&gt;</a>';
		}
		// Show prev. and first URLs, if previous offset is non-negative
		if ( !noPagination && prevOffset > 0 ) {
			var prevUrl = '<%= baseUrl %>&o=' + prevOffset;
			var firstUrl = '<%= baseUrl %>';
			htmlString = '<a href="' + firstUrl + '">&lt;&lt;' + JSMessages.first + '</a>&nbsp;&nbsp;&nbsp;<a href="' + prevUrl + '">&lt;' + JSMessages.prev + '</a>&nbsp;&nbsp;&nbsp;'
						 + htmlString;
		}
		if ( noPagination )
			htmlString = JSMessages.recent + ' <b>' + maxRange + '</b>';
		document.getElementById( 'navigator' ).innerHTML = htmlString;
	}

	// Show the details of a given order
	function drawSelectedOrderDetails( data, selection ) {
		if ( !selection || selection == '' )
			return;
		// Get the order details
		var orderUrl = null;
		var orderId = '';
		if ( selection.length > 0 ) {
			var cell = selection[ 0 ];
			if ( cell.row != null ) {
				// Get order id
				orderId = data.getFormattedValue( cell.row, 0 );
				// Form urls
				orderUrl = 'vieworder.jsp?oid=' + orderId + '&row=' + cell.row;
			}
		}
		// Show the modal screen
		$('#orderDetails').dialog( {
									modal: true,
									position: 'top',
									width: 600,
									height: 500,
									modal: true,
									title: JSMessages.order + ' ' + orderId,
									close: function( event, ui ) {
												// Reset the global vendor drop-down list for this order
												resetVendorDropDown();
										   }
								   });
		showLoader( 'orderDetails' );
		// Get the order data and display
		if ( orderUrl != null ) {
			getOrderData( orderUrl );
		}
	}

	// Process selected order; if single selection, show order; else, if mulitple selections, add to seletedOrders list for later processing (e.g. export spreadsheet)
	function processSelectedOrder( data, selection ) {
		selectedOrders = [];
		// Reset batch data for this order selection
		$batches = {};
		if ( selection.length == 1 ) {
			// allow order viewing
			drawSelectedOrderDetails( data, selection );
		} else if ( selection.length > 1 ) { // allow multiple selection
			for ( var i = 0; i < selection.length; i++ ) {
				var cell = selection[ i ];
				// Get order id
				var orderId = data.getFormattedValue( cell.row, 0 );
				// Add it to the list
				selectedOrders.push( orderId );
			}
		}
	}

	function showLoader( id ) {
        document.getElementById( id ).innerHTML = '<img src="../../images/loader.gif"/>';
	}
</script>
<div id="dataContainer" style="width:100%;margin-right:1%;float:left;margin-top:1%;margin-bottom:10px;">
	<div id="dataNavigatorTop" style="display:none;">
		<div style="clear:both;">
			<table id="kioskstable" width="100%">
				<tr><td style="text-align:right"><div id="navigator"></div></td></tr>
			</table>
			<% if ( kioskId == null ) { %>
			<%= ( !isManager ? TagUtil.getTagHTML( domainId, TagUtil.TYPE_ENTITY, tag, locale, baseUrlNoTags ) : "" ) %>
			<% } %>
			<%= ( !isManager ? TagUtil.getTagHTML( domainId, TagUtil.TYPE_ORDER, tag, locale, baseUrlNoTags ) : "" ) %>
		</div>
	</div>
	<div id="dataTable" style="height:300px;width:100%;overflow:auto;margin-bottom:10px;"></div>
</div>
<!-- Order details div -->
<div id="orderDetails" style="text-align:left;font-size:12px;display:none"><img src="../../images/loader.gif"/></div>
</div>
<% } // end if ( show ) %>
</fmt:bundle>