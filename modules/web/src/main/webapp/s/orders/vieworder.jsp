<%@page contentType="text/html; charset=UTF-8" language="java"%>
<%@page import="com.logistimo.api.util.APIUtil" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.SecurityUtil" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.config.models.AccountingConfig" %>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.config.models.EventsConfig" %>
<%@page import="com.logistimo.config.models.FieldsConfig" %>
<%@page import="com.logistimo.constants.Constants" %>
<%@page import="com.logistimo.constants.SourceConstants" %>
<%@page import="com.logistimo.dao.DaoFactory" %>
<%@page import="com.logistimo.dao.JDOUtils" %>
<%@page import="com.logistimo.entities.entity.AccountsService"%>
<%@page import="com.logistimo.entities.service.AccountsServiceImpl" %>
<%@page import="com.logistimo.inventory.dao.ITransDao" %>
<%@page import="com.logistimo.inventory.entity.IInvntry" %>
<%@page import="com.logistimo.inventory.service.InventoryManagementService"%>
<%@page import="com.logistimo.inventory.service.impl.InventoryManagementServiceImpl" %>
<%@page import="com.logistimo.materials.service.MaterialCatalogService"%>
<%@page import="com.logistimo.materials.service.impl.MaterialCatalogServiceImpl"%>
<%@page import="com.logistimo.orders.OrderResults"%>
<%@page import="com.logistimo.orders.OrderUtils" %>
<%@page import="com.logistimo.orders.service.OrderManagementService" %>
<%@page import="com.logistimo.orders.service.impl.OrderManagementServiceImpl" %>
<%@page import="com.logistimo.pagination.Results"%>
<%@page import="com.logistimo.proto.JsonTagsZ"%>
<%@page import="com.logistimo.security.SecureUserDetails" %>
<%@page import="com.logistimo.services.Resources" %>
<%@page import="com.logistimo.services.ServiceException" %>
<%@page import="com.logistimo.services.Services" %>
<%@page import="com.logistimo.shipment.entity.ITransporter" %>
<%@page import="com.logistimo.users.entity.IUserAccount" %>
<%@page import="com.logistimo.utils.CommonUtils" %>
<%@ page import="com.logistimo.utils.GeoUtil" %>
<%@ page import="com.logistimo.utils.LocalDateUtil" %>
<%@ page import="com.logistimo.utils.NumberUtil" %>
<%@ page import="com.logistimo.utils.StringUtil" %>
<%@ page import="org.lggi.samaanguru.entity.*" %>
<%@ page import="org.lggi.samaanguru.utils.*" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>
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
	final class MaterialFormData {
	
		private static final String COMMA_PLACEHOLDER = "XCOMMAX";
	
		public List<ITransaction> transactions = new ArrayList<ITransaction>();
		public boolean itemsDirty = false;
		
		public MaterialFormData( IOrder o, String materialsStr, boolean isEdit ) {
			String[] materials = materialsStr.split( ";" );
			Date now = new Date();
			Long domainId = o.getDomainId();
			Long kioskId = o.getKioskId();
			Long orderId = o.getOrderId();
			String userId = o.getUserId();
			Long linkedKioskId = o.getServicingKiosk();
            ITransDao transDao = DaoFactory.getTransDao();
			for ( int i = 0; i < materials.length; i++ ) {
				String[] material = materials[i].split( "," );
				if ( material.length < 2 ) {
					System.out.println( "Error: Too few components in material metadata. Has to be at least 2 for material " + materials[i] );
					continue;
				}
				try {
					// Get basic info.
					Long materialId = Long.valueOf( material[0] );
					Float quantity = Float.valueOf( material[1] );
					Float discount = 0F;
					if ( material.length >= 3 )
						discount = Float.valueOf( material[2] );
					// Update transaction or demand item, as necessary
					ITransaction trans = null;
					IDemandItem item = null;
					if ( isEdit ) { // update demand item
						item = o.getItem(materialId);
						if ( item != null ) {
							// Update quantity
							if ( item.getQuantity() != quantity ) {
								item.setQuantity( quantity );
								itemsDirty = true;
							}
							// Update discount
							if ( item.getDiscount() != discount ) {
								item.setDiscount( discount );
								itemsDirty = true;
							}
						}
					} else { // create transaction for new items
						trans = JDOUtils.createInstance(ITransaction.class);
						trans.setDomainId( domainId );
						trans.setKioskId(kioskId);
						trans.setMaterialId(materialId);
						trans.setQuantity(BigDecimal.valueOf(quantity));
						trans.setType( ITransaction.TYPE_REORDER );
						trans.setTrackingId( String.valueOf(orderId) );
						trans.setSourceUserId( userId );
						trans.setTimestamp( now );
					}
					// Get batch info.
					if ( material.length > 3 ) {
						Set<IDemandItemBatch> itemBatches = new HashSet<IDemandItemBatch>();
						for ( int j = 3; j < material.length; j++ ) {
							String batchStr = material[j];
							if ( batchStr.isEmpty() )
								continue;
							String[] batch = batchStr.split( "\\|" );
							if ( batch.length < 2 ) {
								System.out.println( "Error: batch components should be at least 2 or more: " + batchStr );
								continue;
							}
							// Get batch specific metadata
							String batchId = escapeComma( batch[0] );
							String batchQuantityStr = batch[1];
							Float batchQuantity = 0F;
							if ( !batchQuantityStr.isEmpty() )
								batchQuantity = Float.valueOf( batchQuantityStr );
							Date expiry = LocalDateUtil.parseCustom(batch[2], Constants.DATE_FORMAT, null);
							String manufacturer = null;
							Date manufacturedOn = null;
							if ( batch.length > 3 )
								manufacturer = escapeComma( batch[3] );
							if ( batch.length > 4 )
								manufacturedOn = LocalDateUtil.parseCustom( batch[4], Constants.DATE_FORMAT, null );
							// Add batch info. to transaction, if present
							if ( trans != null ) {
								ITransaction newTrans = trans.clone(); // clone earlier data
								newTrans.setBatchId( batchId );
								newTrans.setQuantity( batchQuantity );
								newTrans.setBatchExpiry( expiry );
								newTrans.setBatchManufacturer( manufacturer );
								newTrans.setBatchManufacturedDate( manufacturedOn );
                                newTrans.setType(ITransaction.TYPE_ORDER);
                                transDao.setKey(newTrans);
								transactions.add( newTrans );
							}
							// Add demand item batch info., as needed
							if ( item != null ) {
								IDemandItemBatch itemBatch = JDOUtils.createInstance(IDemandItemBatch.class);
								itemBatch.setDomainId(domainId);
								itemBatch.setKioskId( kioskId );
								itemBatch.setMaterialId(materialId);
								itemBatch.setBatchId( batchId );
								itemBatch.setQuantity( batchQuantity );
								itemBatch.setBatchExpiry( expiry );
								itemBatch.setBatchManufacturer(  manufacturer );
								itemBatch.setBatchManufacturedDate( manufacturedOn );
								// Update item batches list
								itemBatches.add( itemBatch );
							}
						}
						if ( item != null ) {
							item.setItemBatches(itemBatches);
							itemsDirty = true;
						}
					} else if ( trans != null ) {
                        trans.setType(ITransaction.TYPE_REORDER);
                        transDao.setKey(trans);
						transactions.add( trans );
					}
				} catch ( Exception e ) {
					System.out.println( e.getClass().getName() + " when getting material metadata for " + materials[i] + ": " + e.getMessage() );
					e.printStackTrace();
					continue;
				}
			}
		}
		
		private String escapeComma( String str ) {
			return str.replaceAll( COMMA_PLACEHOLDER, "," );
		}
	}
%>
<%
	String orderIdStr = request.getParameter( "oid" );
	String rowIndex = request.getParameter( "row" );
	String action = request.getParameter( "action" );
	String status = request.getParameter( "status" ); // sent if action = 'changestatus'
	String materials = request.getParameter( "materials" ); // sent if action = 'changeitems'
	String message = request.getParameter( "msg" ); // sent if user entered in on either of the above actions 
	String msgUsers = request.getParameter( "msgusers" );
	// URL decode message, if present
	if ( message != null && !message.isEmpty() ) {
		try {
			message = java.net.URLDecoder.decode( message );
		} catch ( Exception e ) {
			System.out.println( "Error when decoding message - " + message + ": " + e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	boolean addNewItems = "additems".equals( action );
	String errMsg = null;
	String autoInvUpdateMsg = null;
	boolean autoInvUpdateError = false;
	IOrder o = null;
	IKiosk k = null;
	IKiosk vendorKiosk = null;
	Locale locale = null;
	String timezone = null;
	MaterialCatalogService mcs = null;
	OrderManagementService oms = null;
	AccountsService as = null;
	boolean statusChanged = false;
	boolean itemsChanged = false;
	DomainConfig dc = null;
	EventsConfig ec = null;
	FieldsConfig orderFieldsConfig = null;
	AccountingConfig ac = null;
	IUserAccount orderedBy = null;
	List<IUserAccount> kioskUsers = null;
	List<IUserAccount> vendorUsers = null;
	Long vendorId = null;
	List<IInvntry> inventories = null;
	HashMap<Long,Float> vendorStockMap = null;
	List<IUserAccount> remainingUsers = null; // all visible users, visible to the logged in user
	boolean showVendorStock = false;
	String vendorStockStyle = "background-color:#FAFAFA;border-color:lightgray;";
	String accountUpdatedMsg = null;
	ResourceBundle messages = null;
	boolean isAdmin = false;
	String userId = null;
	try {
		// Get user's locale and timezone
		SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
		userId = sUser.getUsername();
		locale = sUser.getLocale();
		timezone = sUser.getTimezone();
		String role = sUser.getRole();
		isAdmin = ( SecurityUtil.compareRoles(role, IUserAccount.ROLE_DOMAINOWNER) >= 0 );
		// Get resource bundle
		messages = Resources.get().getBundle( "Messages", locale );
		// Get the domain config
		Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
		dc = DomainConfig.getInstance( domainId );
		ec = dc.getEventsConfig();
		// Get the order custom fields config, if any
		orderFieldsConfig = dc.getOrderFields();
		// Get accounting config, if any
		ac = dc.getAccountingConfig();
		// Get order id
		Long orderId = Long.valueOf( orderIdStr );
		// Get services
		oms = Services.getService( OrderManagementServiceImpl.class, locale );
		as = Services.getService( AccountsServiceImpl.class, locale );
		mcs = Services.getService( MaterialCatalogServiceImpl.class, locale );
		InventoryManagementService ims = Services.getService( InventoryManagementServiceImpl.class, locale );
		// Get order
		o = oms.getOrder( orderId );
		// Get kiosk
		k = as.getKiosk( o.getKioskId() );
		// Get the users of this kiosk
		kioskUsers = (List<IUserAccount>) k.getUsers();
		// Get vendor Id, if any
		vendorId = o.getServicingKiosk();
		// Get logged in user
		IUserAccount u = as.getUserAccount( userId, false );
		// NOTE: block of code for SMS notification moved below the command processing
		// Get the user placing the order
		orderedBy = as.getUserAccount( o.getUserId(), false );
		// Get the inventory associated with this kiosk
		inventories = ims.getInventoryByKiosk( k.getKioskId(), null ).getResults(); // TODO: pagination?
		// COMMAND PROCESSING
		// Check if any action is to be performed
		if ( "changestatus".equals( action ) ) {
			// Get the subscriber list, if any
			// Update order along with status
			OrderManagementService.UpdatedOrder uo = TransactionUtil.updateOrderStatus( orderId, status, userId, message, StringUtil
					.getList(msgUsers), dc, SourceConstants.WEB );
			o = uo.order;
			autoInvUpdateMsg = uo.message;
			autoInvUpdateError = uo.inventoryError;
			statusChanged = ( !o.getStatus().equals( status ) ); /// earlier: true;
			// Set account updated message, if necessary
			if ( dc.isAccountingEnabled() && IOrder.COMPLETED.equals( status ) && o.getTotalPrice() != 0 ) {
				accountUpdatedMsg = messages.getString( "accountupdatedmsg" ) + " <b>" + o.getCurrency() + " " + CommonUtils
						.getFormattedPrice(o.getTotalPrice()) + "</b>";
			}
		} else if ( "changeitems".equals( action ) ) {
			if ( materials != null && !materials.isEmpty() ) {
				MaterialFormData materialFormData = new MaterialFormData( o, materials, true );
				// Save the changes, if any item was changed
				if ( materialFormData.itemsDirty ) {
					o.setUpdatedOn( new Date() );
					o.setUpdatedBy( userId );
					if ( message != null && !message.isEmpty() )
						o.setThreadedMessage( message, userId );
					o = oms.updateOrder( o, false, SourceConstants.WEB ).order;
					itemsChanged = true;
				}
			}
		} else if ( addNewItems ) {
			if ( materials != null && !materials.isEmpty() ) {
				MaterialFormData materialFormData = new MaterialFormData( o, materials, false );
				// Update order
				if ( !materialFormData.transactions.isEmpty() ) {
					OrderResults or = oms.updateOrderTransactions( domainId, userId, ITransaction.TYPE_REORDER, materialFormData.transactions, o.getKioskId(), o.getOrderId(), message, true, o.getServicingKiosk(), null, null, null, null, null, null, 0F, null, null, dc.allowEmptyOrders() );
					o = or.getOrder();
				}
			} // end if ( materials != null ... )
		} else if ( "edittransporter".equals( action ) ) {
			// Transporter parameters
			String transporterType = request.getParameter( "tsptype" );
			String transporterId = request.getParameter( "tspid" );
			if ( transporterId != null ) {
				try {
					transporterId = java.net.URLDecoder.decode( transporterId, "UTF-8" );
				} catch ( Exception e ) {
					System.out.println( "Exception when decoding transporter Id for order " + o.getOrderId() + ": " + e.getClass().getName() + ": " + e.getMessage() );
				}
			}
			boolean isDirty = false;
			if ( transporterId == null || transporterId.isEmpty() ) {
				if ( o.hasTransporters() ) {
					o.clearTransporters();
					isDirty = true;
				}
			} else if ( o.getTransporter( transporterType, transporterId ) == null ) {
				// Reset transporters
				o.clearTransporters();
				isDirty = true;
				// Add the new transporter
				ITransporter t = JDOUtils.createInstance(ITransporter.class);
//                t.updateOrderId(o);
				t.setTransporterId( transporterId );
				t.setType( transporterType );
				t.setDomainId( domainId );
				o.addTransporter( t );
			}
			// Update order, if dirty
			if ( isDirty ) {
				o.setUpdatedBy( userId );
				o.setUpdatedOn( new Date() );
				OrderManagementService.UpdatedOrder uo = oms.updateOrder( o, false, SourceConstants.WEB );
				o = uo.order;
			}
		} else if ( "editpayment".equals( action ) ) {
			// Get amount
			String amountStr = request.getParameter( "amount" );
			// Get payment option
			String paymentOption = request.getParameter( "popt" );
			float amount = 0;
			boolean isDirty = false;
			if ( amountStr != null && !amountStr.isEmpty() ) {
				amount = Float.parseFloat( amountStr );
				o.addPayment( amount );
				isDirty = true;
			}
			if ( paymentOption != null && !paymentOption.equals( o.getPaymentOption() ) ) {
				if ( paymentOption.isEmpty() )
					o.setPaymentOption( null );
				else
					o.setPaymentOption( paymentOption );
				isDirty = true;
			}
			if ( isDirty ) {
				o.setUpdatedBy( userId );
				o.setUpdatedOn( new Date() );
				OrderManagementService.UpdatedOrder uo = oms.updateOrder( o, false, SourceConstants.WEB );
				o = uo.order;
			}
		} else if ( "editpackagesize".equals( action ) ) {
			String packageSize = request.getParameter( "pksz" );
			if ( packageSize != null && !packageSize.equals( o.getPackageSize() ) ) {
				if ( packageSize.isEmpty() )
					packageSize = null;
				o.setPackageSize( packageSize );
				o.setUpdatedBy( userId );
				o.setUpdatedOn( new Date() );
				OrderManagementService.UpdatedOrder uo = oms.updateOrder( o, false, SourceConstants.WEB );
				o = uo.order;
			}
		} else if ( "confirmfulfillmenttime".equals( action ) ) {
			String confirmedFulfillmentTime = request.getParameter( "cft" );
			if ( confirmedFulfillmentTime != null ) {
				if ( confirmedFulfillmentTime.isEmpty() && o.getConfirmedFulfillmentTimeRange() != null )
					o.setConfirmedFulfillmentTimeRange( null );
				else if ( !confirmedFulfillmentTime.isEmpty() )
					o.setConfirmedFulfillmentTimeRange( confirmedFulfillmentTime );
				o.setUpdatedBy( userId );
				o.setUpdatedOn( new Date() );
				OrderManagementService.UpdatedOrder uo = oms.updateOrder( o, false, SourceConstants.WEB );
				o = uo.order;
			}
		} else if ( "changevendor".equals( action ) ) {
			String vendorIdStr = request.getParameter( "vid" );
			if ( vendorIdStr != null && !vendorIdStr.isEmpty() ) {
				o.setServicingKiosk( Long.valueOf( vendorIdStr ) );
				o.setUpdatedBy( userId );
				o.setUpdatedOn( new Date() );
				OrderManagementService.UpdatedOrder uo = oms.updateOrder( o, false,SourceConstants.WEB );
				o = uo.order;
			}
		}
		// END COMMAND PROCESSING
		// Get users for SMS notification purposes
		// Add all the domain owners to list
		Results results = as.getUsers( domainId, IUserAccount.ROLE_DOMAINOWNER, true, null, null ); // no pagination, given domain owners can be expected to be few
		List<IUserAccount> domainOwners = results.getResults();
		// Get vendor kiosk, if present
		List<IInvntry> vendorInventories = null;
		if ( o.getServicingKiosk() != null ) {
			vendorKiosk = as.getKiosk( o.getServicingKiosk() );
			vendorUsers = (List<IUserAccount>) vendorKiosk.getUsers();
			try {
				// Get the vendor inventory - to show vendor stock-on-hand
				vendorInventories = ims.getInventoryByKiosk( o.getServicingKiosk(), null ).getResults();
			} catch ( Exception e ) {
				// do nothing, given vendorInventories will not be shown, if not present
			}
		}
		List<IUserAccount> allUsers = new ArrayList<IUserAccount>();
		if(kioskUsers != null) {
		    allUsers.addAll( kioskUsers );
		}
		if ( vendorUsers != null && !vendorUsers.isEmpty() ) {
			vendorUsers = APIUtil.getDifference( vendorUsers, kioskUsers );
			allUsers.addAll( vendorUsers );
		}
		remainingUsers = APIUtil.getDifference(domainOwners, allUsers);
		// Form a stock map - materialId to stock-on-hand of vendor inventories (to show against the order)
		if ( vendorInventories != null ) {
			vendorStockMap = new HashMap<Long,Float>();
			Iterator<IInvntry> vinvIt = vendorInventories.iterator();
			while ( vinvIt.hasNext() ) {
				IInvntry vinv = vinvIt.next();
				vendorStockMap.put( vinv.getMaterialId(), Float.valueOf( vinv.getStock() ) );
			}
			showVendorStock = dc.autoGI();
		}
	} catch ( NumberFormatException e ) {
		errMsg = "Invalid order Id";
	} catch ( ServiceException e ) {
		errMsg = e.getMessage();
	}
	// Check if items are changeable
	boolean isPending = IOrder.PENDING.equals( o.getStatus() );
	boolean isCancelled = IOrder.CANCELLED.equals( o.getStatus() );
	boolean isCompleted = IOrder.COMPLETED.equals( o.getStatus() );
	boolean isConfirmed = IOrder.CONFIRMED.equals( o.getStatus() );
	boolean isFulfilled = IOrder.FULFILLED.equals( o.getStatus() );
	
	// Get the variable map for SMS message formation
	// NOTE: Order status is deliberately OMITTED so that it can be replaced by the Javascript function
	List<String> excludeVars = new ArrayList<String>();
	excludeVars.add( EventsConfig.VAR_ORDERSTATUS );
	//Get the JSON of status messages (status --> completed status-message)
	String orderStatusMsgJSON = ec.getOrderStatusJSON( o, locale, timezone, excludeVars );
	if ( orderStatusMsgJSON == null )
		orderStatusMsgJSON = "";
	// Get currency
	String currency = ( o.getCurrency() != null ? o.getCurrency() : "" );
	// Convenience flags
	boolean hasVendorUsers = ( vendorUsers != null && !vendorUsers.isEmpty() );
	boolean hasOtherUsers = ( remainingUsers != null && !remainingUsers.isEmpty() );
	// Transporter details
	String transporterInSms = "";
	if ( dc.isTransporterInStatusSms() && o.getTransporter() != null )
		transporterInSms = messages.getString( "transporter" ) + ": " + o.getTransporterName();
	// Accounting
	// Check if accounting is enabled
	boolean accountingEnabled = dc.isAccountingEnabled();
	// Check if credit limit enforcement is required
	boolean enforceConfirmed = ( accountingEnabled && ac.enforceConfirm() );
	boolean enforceShipped = ( enforceConfirmed || ( accountingEnabled && ac.enforceShipped() ) );
	// Get the credit limit and amount paid for the customer
	String creditLimitErr = null;
	float availableCredit = 0;
	// Get the credit limit and check against receivables
	if ( accountingEnabled ) {
		try {
			Long customerId = o.getKioskId();
			// Get the credit limit
			if ( customerId != null && vendorId != null )
				availableCredit = TransactionUtil.getCreditData( customerId, vendorId, dc ).availabeCredit;
		} catch ( Exception e ) {
			creditLimitErr = e.getMessage();
		}
	}
	boolean showCredit = ( enforceConfirmed || enforceShipped );
	// Parameter to pass to changeOrderStatus() regarding availale credit
	String creditExceededStatus = ( enforceConfirmed ? IOrder.CONFIRMED : ( enforceShipped ? IOrder.COMPLETED : null ) );
	String creditExceededStr = null;
	if ( accountingEnabled && ( o.getTotalPrice() > availableCredit ) ) {
		///if ( ( isPending && enforceConfirmed ) || ( ( isPending || isConfirmed ) && enforceShipped ) )
		if ( !(isCompleted || isFulfilled || isCancelled) )
			creditExceededStr = currency + " " + CommonUtils.getFormattedPrice( availableCredit );
	}
	 
	// Check if geo-coordinates exist
	boolean hasGeo = ( o.getLatitude() != null && o.getLongitude() != null );
	String destObj = null;
	if ( hasGeo ) {
		// Get destination coordinates, if any
		if ( k != null && k.getLatitude() != 0 && k.getLongitude() != 0 ) {
			destObj = "{ lat: " + k.getLatitude() + ", lng: " + k.getLongitude() + ", title: '" + k.getName() + ", " + k.getLocation() + "'" + ", ltype:'" + IKioskLink.TYPE_CUSTOMER + "'  }";
		}
	}	
	// Get the configured payment options, if any
	String paymentOptionsCSV = dc.getPaymentOptions();
	List<String> paymentOptionsFromConfig = null;
	if ( paymentOptionsCSV != null && !paymentOptionsCSV.isEmpty() )
		paymentOptionsFromConfig = StringUtil.getList( paymentOptionsCSV );
	// Get the configured package sizes, if any
	String packageSizesCSV = dc.getPackageSizes();
	List<String> packageSizesFromConfig = null;
	if ( packageSizesCSV != null && !packageSizesCSV.isEmpty() )
		packageSizesFromConfig = StringUtil.getList( packageSizesCSV );
%>
<fmt:bundle basename="Messages">
<fmt:message key="invoice" var="invoice"/>
<!-- Dummy field to hold order status to message mapping -->
<textarea id="orderstatustomsg" style="display:none;"><%= orderStatusMsgJSON %></textarea>
<% if ( errMsg != null ) { %>
Error: <%= errMsg %>
<% } else { %>
<div style="float:right">
	<a href="invoice.jsp?oid=<%= orderIdStr %>" target="_new"><img src="../../images/invoice.png" alt="${invoice}" title="${invoice}" width="16px" height="16px"/></a>
	<a href="#" onclick="javascript:window.open('../help/help.jsp?type=purchaseorder','purchaseorder','location=0,resizable=1,scrollbars=1,width=800,height=350');"><img src="../../images/question.png" alt="help" title="help"></a>
</div>
<table id="order" width="100%" style="font-size:12px;">
<% boolean hasPriceInfo = ( o.getTotalPrice() > 0 || creditExceededStr != null ); %>
<% if ( hasPriceInfo ) { %>
	<tr><td>
	<% if ( o.getTotalPrice() > 0 ) { %>
			<%= o.getPriceStatement() %>
	<% } %>
	<% if ( creditExceededStr != null ) { %>
	<br/><font style="color:red">Order cost exceeds available credit <%= creditExceededStr %></font>
	<% } %>
	</td></tr>
<% } // end if (hasPriceInfo) %>
	<tr><td>
	<b><%= OrderUtils.getStatusDisplay(o.getStatus(), locale) %></b>
	<% if ( statusChanged ) { %>
		(<%= messages.getString( "saved" ) %>)
	<% } %>
	 - <a id="change" class="smalltxt" href="#" onclick="if ( this.innerHTML == JSMessages.change ) { this.innerHTML = ''; document.getElementById( 'changestatus' ).style.display = 'block'; } else { this.innerHTML = JSMessages.change; document.getElementById( 'changestatus' ).style.display = 'none'; }"><fmt:message key="change"/></a>
	<% if ( accountUpdatedMsg != null ) { %>
	<br/>[<%= accountUpdatedMsg %>]
	<% } %>
	<% if ( autoInvUpdateError ) { %>
	<font color="red">
	<% } %>
	<%= autoInvUpdateMsg != null && !autoInvUpdateMsg.isEmpty() ? "<br/>[" + autoInvUpdateMsg + "]" : "" %>
	<% if ( autoInvUpdateError ) { %>
	</font>
	<% } %>
	<!-- Display custom field values, if any -->
	<%
	Map<String,String> fieldMap = o.getFields();
	if ( fieldMap != null && !fieldMap.isEmpty() ) {
		%>
		<%= TransactionUtil.renderOrderFieldsInHTML( orderFieldsConfig, o.getFields(), true, null, locale ) %>
		<%
	}
	%>
	<div id="changestatus" class="sgForm" style="margin-top:5px;display:none;">
		<select id="statusselector" name="status" onchange="updateOrderSendMessage('<%= EventsConfig.VAR_ORDERSTATUS %>',this.options[this.selectedIndex].text,document.getElementById('orderstatustomsg'),this.options[this.selectedIndex].value, '<%= transporterInSms %>');">
			<option value="">-- <%= messages.getString( "select" ) %> <fmt:message key="status"/> --</option>
			<% if ( isPending ) { %>
				<option value="<%= IOrder.CANCELLED %>"><%= messages.getString( "order.cancelled" ) %></option>
				<option value="<%= IOrder.CONFIRMED %>"><%= messages.getString( "order.confirmed" ) %></option>
				<option value="<%= IOrder.COMPLETED %>"><%= messages.getString( "order.shipped" ) %></option>
			<% } else if ( isCancelled ) { %>
				<option value="<%= IOrder.PENDING %>"><%= messages.getString( "order.pending" ) %></option>
			<% } else if ( isCompleted ) { %>
				<option value="<%= IOrder.CANCELLED %>"><%= messages.getString( "order.cancelled" ) %></option>
				<option value="<%= IOrder.FULFILLED %>"><%= messages.getString( "order.fulfilled" ) %></option>
				<option value="<%= IOrder.PENDING %>"><%= messages.getString( "order.pending" ) %></option>
			<% } else if ( isConfirmed ) { %>
				<option value="<%= IOrder.CANCELLED %>"><%= messages.getString( "order.cancelled" ) %></option>
				<option value="<%= IOrder.PENDING %>"><%= messages.getString( "order.pending" ) %></option>
				<option value="<%= IOrder.COMPLETED %>"><%= messages.getString( "order.shipped" ) %></option>
			<% } else if ( isFulfilled ) { %>
				<option value="<%= IOrder.CANCELLED %>"><%= messages.getString( "order.cancelled" ) %></option>
			<% } // end if %>
		</select>
		<!-- Custom fields -->
		<!-- TODO: need to move this to the generic order form, and NOT under status change (also temporarily disabled custom field config.
		<% String fieldsHTML = TransactionUtil.renderOrderFieldsInHTML( orderFieldsConfig, o.getFields(), false, null, locale ); 
			if ( !fieldsHTML.isEmpty() ) {
				%>
				<br/><%= fieldsHTML %>
				<%
			}
		%>
		 -->
		<!-- End custom fields -->
		<br/>
		<fmt:message key="message"/> (160 <%= messages.getString( "charactersallowed" ) %>):
		<br/>
		<textarea id="message1" rows=2 cols=50></textarea>
		<br/>
		<!--  SMS notification list(s) -->
		<% if ( kioskUsers != null && !kioskUsers.isEmpty() ) { %>
		<%= messages.getString( "message.sendviasms" ) %>:<br/>
		<table>
		<!--  Customers -->
		<%
		Iterator<IUserAccount> it = kioskUsers.iterator();
		int counter = 0; // show 3 phones in one row to optimize space
		while ( it.hasNext() ) {
			IUserAccount u1 = it.next();
			boolean newRow = ( counter % 3 == 0 );
			++counter;
			if ( newRow ) {
				if ( counter > 0 ) { // close previous row %>
					</tr>
			<%  } %>
			 <tr>
		<%  } %>
			<td><input type="checkbox" name="msgusers" value="<%= u1.getUserId() %>" /> <%= u1.getFullName() %> (<%= u1.getMobilePhoneNumber() %>)</td>
		<%
		} // end while
		%> 
		</table>
		<% } // end if ( kioskUsers != null ... ) %>
		<!-- More users hidden div -->
			<!--  Vendors -->
			<div id="vendorusers";margin-top:2px;">
			<% if ( hasVendorUsers ) { %>
			<fmt:message key="vendors"/>:<br/>
			<table>
				<%
				Iterator<IUserAccount> it = vendorUsers.iterator();
				int counter = 0; // show 3 phones in one row to optimize space
				while ( it.hasNext() ) {
					IUserAccount u1 = it.next();
					boolean newRow = ( counter % 3 == 0 );
					++counter;
					if ( newRow ) {
						if ( counter > 0 ) { // close previous row %>
							</tr>
					<%  } %>
					 <tr>
				<%  } %>
					<td><input type="checkbox" name="msgusers" value="<%= u1.getUserId() %>" /> <%= u1.getFullName() %> (<%= u1.getMobilePhoneNumber() %>)</td>
				<%
				} // end while
				%> 
			</table>
			<% } // end if ( hasVendorUsers ) %>
			</div>
				<!--  Administrators -->
				<div id="otherusers" style="margin-top:2px;">
			<% if ( hasOtherUsers ) { %>
				<fmt:message key="administrators"/>
				<table>
				<%
					Iterator<IUserAccount> itMore = remainingUsers.iterator();
					int counter = 0;
					while ( itMore.hasNext() ) {
						IUserAccount u1 = itMore.next();
						boolean newRow = ( counter % 3 == 0 );
						++counter;
						if ( newRow ) {
							if ( counter > 0 ) { // close previous row %>
								</tr>
						<%  } %>
						 <tr>
					<%  } %>
						<td><input type="checkbox" name="msgusers" value="<%= u1.getUserId() %>" /> <%= u1.getFullName() %> (<%= u1.getMobilePhoneNumber() %>)</td>
					<%
					} // end while
				%>
				</table>
			<% } // end if ( hasOtherUsers ) %>
			 </div>
			 ...<a href="#" onclick="resetMsgUsers()"><fmt:message key="reset.lower"/></a>
		<br/><br/>
		<!-- changeOrderStatus() - data, view and table are global variables defined in vieworders.jsp, and used here to refresh table's change in state -->
		<button id="changeOrderStatus" onclick="changeOrderStatus( '<%= o.getOrderId() %>', document.getElementById( 'statusselector' ).options[ document.getElementById( 'statusselector' ).selectedIndex ].value, document.getElementById( 'statusselector' ).options[ document.getElementById( 'statusselector' ).selectedIndex ].text, <%= rowIndex %>, data, view, table, <%= dc.isTransporterMandatory() %>, <%= o.hasTransporters() %>, <%= creditExceededStr != null ? "'" + creditExceededStr + "'" : "null" %>, <%= creditExceededStatus != null ? "'" + creditExceededStatus + "'" : "null" %>, <%= vendorKiosk == null %>,<%= o.size() %> )"><fmt:message key="save"/></button>
		<button onclick="document.getElementById('changestatus').style.display='none'; document.getElementById('change').innerHTML=JSMessages.change;"><fmt:message key="cancel"/></button>
		<div id="changeOrderStatusLoader" style="display:none;"><img src="../../images/loader.gif"/></div>
	</div>
	<!-- Show map, if needed -->
	<% if ( hasGeo ) { %>
	<div id="mapicon" style="float:right"><a id="mapiconlink" href="#" onmouseover="showMapInline(this,document.getElementById('inlinemap'),document.getElementById( 'mapPointDetails' ), document.getElementById( 'mapPointDetailsLoader' ), document.getElementById( 'mapPointDetailsInfo' ), document.getElementById( 'mapPointDetailsMap' ), <%= o.getLatitude() %>,<%= o.getLongitude() %>,<%= o.getGeoAccuracy() %>,<%= destObj != null ? destObj : null %>);"><img src="../../images/map16.png" alt="Map" title="Map"/></a></div>
	<div id="inlinemap" style="position:relative;width:100%;height:100px;display:none"></div>
	<!-- Map point details div -->
	<div id="mapPointDetails" style="text-align:left;font-size:12px;display:none">
		<div id="mapPointDetailsLoader" style="display:none;"><img src="../../images/loader.gif"/></div>
		<div id="mapPointDetailsInfo" style="display:none;"></div>
		<!--  Map div showing mappoint data -->
		<div id="mapPointDetailsMap" style="width:680px;height:480px"></div>
	</div>
	
	<% } else if ( o.getGeoErrorCode() != null ) { %>
	<br/><i>(<font style="font-size:8pt;color:red"><%= GeoUtil.getGeoErrorMessage(o.getGeoErrorCode(), locale) %></font>)</i>
	<% } %>
	</td></tr>
	
	<tr><td>
	<!--  Details (nested) table -->
	<table id="order" style="width:100%">
	
	<tr><td class="ocell"><fmt:message key="placedby"/></td> <td><a href="../setup/setup.jsp?subview=users&form=userdetails&id=<%= o.getUserId() %>" target="_new"><%= orderedBy != null ? orderedBy.getFullName() : "" %></a>
	</td></tr>
	<tr><td class="ocell"><fmt:message key="customer" /></td><td>
	        <a href="../setup/setup.jsp?subview=kiosks&form=kioskdetails&id=<%= k.getKioskId() %>" target="_new"><%= k.getName() %></a>,
	        <%= k.getCity() %>, <%= k.getState() %></td></tr>
	<tr><td class="ocell"><fmt:message key="vendor"/></td>
		<td>
		<% if ( vendorKiosk != null ) { %>
			<% if ( isAdmin ) { %>
			<a href="../setup/setup.jsp?subview=kiosks&form=kioskdetails&id=<%= vendorKiosk.getKioskId() %>" target="_new">
			<% } %>
			<%= vendorKiosk.getName() %>
			<% if ( isAdmin ) { %>
			</a>
			<% } %>
			, <%= vendorKiosk.getCity() %>, <%= vendorKiosk.getState() %>
		<% } else { %>
			<font style="color:red">No vendor specified. Please associate a vendor with this order.</font>
		<% } %>
		<!-- Show vendor change link, if order is not yet shipped, i.e. pending or confirmed only -->
		<% String orderStatus = o.getStatus();
		   if ( IOrder.PENDING.equals( orderStatus ) || IOrder.CONFIRMED.equals( orderStatus ) ) { %>
	        <div style="float:right"><a class="smalltxt" id="vendorchangelink" href="#" onclick="this.innerHTML='';document.getElementById('vendorpanel').style.display='block';initVendorDropDown(<%= ( vendorKiosk == null ? "null" : "'" + vendorKiosk.getKioskId() + "'" ) %>,'<%= vendorKiosk == null ? "" : vendorKiosk.getName() %>');"><fmt:message key="change" /></a></div>
			<div id="vendorpanel" class="sgForm" style="display:none;margin-top:5px">
				<fmt:message key="select"/> <fmt:message key="vendor"/>:<br/><input type="text" id="vendorid" />
				<br/>
				<button onclick="changeVendor('<%= o.getOrderId() %>');"><fmt:message key="save" /></button>
				<button onclick="document.getElementById('vendorpanel').style.display='none';document.getElementById('vendorchangelink').innerHTML=JSMessages.change;"><fmt:message key="cancel" /></button>
				<div id="vendorLoader" style="display:none;"><img src="../../images/loader.gif"/></div>
			</div>
		<% } %>
	    </td>
	</tr>
	<tr><td class="ocell"><%= messages.getString( "createdon" ) %></td><td> <%= LocalDateUtil.format( o.getCreatedOn(), locale, timezone ) %>
			<% if ( o.getUpdatedOn() != null ) { %>
			<br/><i><font color="blue"><%= messages.getString( "lastupdated" ) %> <%= LocalDateUtil.format( o.getUpdatedOn(), locale, timezone ) %>
				  <% if ( o.getUpdatedBy() != null ) { %>
					<fmt:message key="by"/> <a href="../setup/setup.jsp?subview=users&form=userdetails&id=<%= o.getUpdatedBy() %>" target="_new"><%= o.getUpdatedBy() %></a>
				  <% } %>
			</font></i>
			<% } %></td></tr>
	
	<tr><td class="ocell"><%= messages.getString( "order" ) %> <%= messages.getString( "tags" ) %></td><td>
	<% if ( o.getTags( TagUtil.TYPE_ORDER ) != null && !o.getTags( TagUtil.TYPE_ORDER ).isEmpty() ) { %> 
			<%= o.getTags( TagUtil.TYPE_ORDER ).toString() %>
	<% } else { %>
		<fmt:message key="none"/>
	<% } %>
	</td></tr>
		
	<% if ( o.getMessage() != null && !o.getMessage().isEmpty() ) { %>
	<tr><td class="ocell"><fmt:message key="message"/></td><td>
		<% String truncMsg = o.getTruncatedMessage( 10 ); %>
		<div id="truncmessage">
			<%= truncMsg %>
			<% if ( o.getMessage().length() > truncMsg.length() ) { %>
			<a href="#" onclick="document.getElementById( 'truncmessage' ).style.display = 'none';document.getElementById( 'message' ).style.display = 'block';">...<fmt:message key="more"/></a>
			<% } // end if %>
		</div>
		<div id="message" style="display:none;">
			<%= o.getMessage() %>
			<a href="#" onclick="document.getElementById( 'truncmessage' ).style.display = 'block';document.getElementById( 'message' ).style.display = 'none';">...<fmt:message key="less"/></a>
		</div>
	</td></tr>
	<% } %>
	<!-- Expected fulfillment times -->
	<% 
	String utcExpectedFulfillmentTimesCSV = o.getExpectedFulfillmentTimeRangesCSV();
	String utcConfirmedFulfillmentTimeRange = o.getConfirmedFulfillmentTimeRange();
	boolean hasExpectedFulfillmentTimeRanges = ( utcExpectedFulfillmentTimesCSV != null && !utcExpectedFulfillmentTimesCSV.isEmpty() );
	boolean showFulfillmentTime = ( hasExpectedFulfillmentTimeRanges || ( utcConfirmedFulfillmentTimeRange != null && !utcConfirmedFulfillmentTimeRange.isEmpty() ) );
	if ( showFulfillmentTime ) {
		%>
		<tr><td class="ocell"><fmt:message key="expectedfulfillmenttime" /></td>
		<td>
		<%
		String confirmedFulfillmentTimeStr = null;
		if ( utcConfirmedFulfillmentTimeRange != null && !utcConfirmedFulfillmentTimeRange.isEmpty() ) {
			Map<String,Date> timeRange = null;
			try {
				timeRange = LocalDateUtil.parseTimeRange( utcConfirmedFulfillmentTimeRange, null, null );
				if ( timeRange != null ) {
					Date start = timeRange.get( JsonTagsZ.TIME_START );
					Date end = timeRange.get( JsonTagsZ.TIME_END );
					if ( start != null )
						confirmedFulfillmentTimeStr = LocalDateUtil.format( start, locale, timezone );
					if ( end != null )
						confirmedFulfillmentTimeStr += " - " + LocalDateUtil.format( end, locale, timezone );
				}
			} catch ( Exception e ) {
				System.out.println( "Exception when parsing confirmed fulfillment time range " + utcConfirmedFulfillmentTimeRange + " in order " + o.getOrderId() + ": " + e.getMessage() );
			}
		}
		// Get exepected fulfillment time ranges, if any
		List<Map<String,Date>> utcTimeRanges = null;
		if ( hasExpectedFulfillmentTimeRanges ) {
			try {
				utcTimeRanges = LocalDateUtil.parseTimeRanges( utcExpectedFulfillmentTimesCSV, null, null );
			} catch ( Exception e ) {
				System.out.println( "Exception when parsing expected fulfillment time ranges " + utcExpectedFulfillmentTimesCSV + " in order " + o.getOrderId() + ": " + e.getMessage() );
			}
		}
		hasExpectedFulfillmentTimeRanges = ( utcTimeRanges != null && !utcTimeRanges.isEmpty() );
		%>
		<!-- Show the change button, if expected fulfillment time ranges exist -->
		<% if ( confirmedFulfillmentTimeStr != null ) { %>
			<%= confirmedFulfillmentTimeStr %>
		<% } else { %>
			<fmt:message key="none" />
		<% } %>
		<% if ( hasExpectedFulfillmentTimeRanges ) { %>
		<div style="float:right;"><a id="fulfillmenttimechangelink" href="#" class="smalltxt" onclick="this.html='';document.getElementById('expectedfulfillmenttimespanel').style.display='block'"><fmt:message key="change" /></a></div>
		<% } %>
		<%
		if ( hasExpectedFulfillmentTimeRanges ) {
			%>
			<div id="expectedfulfillmenttimespanel" class="sgForm" style="display:none;margin-top:5px">
				<fmt:message key="expectedfulfillmenttimeselectmsg" />
				<form name="eftsform">
					<table>
					<tr><td style="border:0"><input type="radio" name="efts" value="" <%= utcConfirmedFulfillmentTimeRange == null || utcConfirmedFulfillmentTimeRange.isEmpty() ? "checked" : "" %>/></td><td style="border:0"><fmt:message key="none" /></td></tr>
					<%
					Iterator<Map<String,Date>> it = utcTimeRanges.iterator();
					while ( it.hasNext() ) {
						Map<String,Date> utcTimeRange = it.next();
						Date start = utcTimeRange.get( JsonTagsZ.TIME_START );
						if ( start == null )
							continue;
						String utcTimeRangeVal = LocalDateUtil.format( start, null, null );
						String localTimeRangeStr = LocalDateUtil.format( start, locale, timezone );
						Date end = utcTimeRange.get( JsonTagsZ.TIME_END );
						if ( end != null ) {
							utcTimeRangeVal += "-" + LocalDateUtil.format( end, null, null );
							localTimeRangeStr += " - " + LocalDateUtil.format( end, locale, timezone ); 
						}
						%>
						<tr><td style="border:0"><input type="radio" name="efts" value="<%= utcTimeRangeVal %>" <%= utcTimeRangeVal.equals( utcConfirmedFulfillmentTimeRange ) ? "checked" : "" %> /></td><td style="border:0"><%= localTimeRangeStr %></td></tr>
						<%
					} // end while
					%>
					</table>
				</form>
				<br/>
				<button onclick="confirmFulfillmentTime('<%= o.getOrderId() %>')"><fmt:message key="save" /></button>
				<button onclick="document.getElementById('expectedfulfillmenttimespanel').style.display='none';document.getElementById('fulfillmenttimechangelink').innerHTML=JSMessages.change"><fmt:message key="cancel" /></button>
				<div id="confirmFulfillmentTimeLoader" style="display:none;"><img src="../../images/loader.gif"/></div>
			</div>
			<%
		} // end if ( utcTimeRanges != null ... )
		%>
		</td></tr>
		<%
	} // end if ( showFulfillmentTime )
	%>
	<!--  End expected fulfillment times -->
	
	<!-- Payment -->
	<% if ( accountingEnabled ) { %>
		<tr><td class="ocell">
		<fmt:message key="payment" /></td>
		<td><%= currency %> <%= CommonUtils.getFormattedPrice( o.getPaid() ) %>
		<% if ( !o.isFullyPaid() ) { %>
		&nbsp;&nbsp;[ <fmt:message key="balance" />: 
			<font style="color:red"><%= currency %> <%= CommonUtils.getFormattedPrice( o.getTotalPrice() - o.getPaid() ) %></font>
			<% if ( showCredit ) { %>
			, <fmt:message key="availablecredit"/>: <%= currency %> <%= CommonUtils.getFormattedPrice( availableCredit ) %>
			<% } %>]
		<% } else if ( o.getTotalPrice() > 0 ) { %>
			[<b><font style="color:green"><fmt:message key="paid"/></font></b>]
		<% } // end if ( !o.isFullyPaid() ) %>
		<div style="float:right;"><a class="smalltxt" id="paymentchangelink" href="#" onclick="document.getElementById('paymentpanel').style.display='block';document.getElementById('paymentchangelink').innerHTML='';"><fmt:message key="change"/></a></div>
		<!--  Show the payment option if any -->
		<% if ( o.getPaymentOption() != null && !o.getPaymentOption().isEmpty() ) { %><br/>[<%= o.getPaymentOption() %>]<% } %>
		<!-- Payment panel -->
		<div id="paymentpanel" class="sgForm" style="margin-top:5px;display:none;">
			<table id="order">
			<tr>
				<td><fmt:message key="payment.paymentoption" /></td>
				<td>
				<% if ( paymentOptionsFromConfig == null || paymentOptionsFromConfig.isEmpty() ) { %>
					<input type="text" id="paymentoption" name="paymentoption" maxlength="100" value="<%= o.getPaymentOption() != null ? o.getPaymentOption() : "" %>" />
				<% } else { %>
					<select id="paymentoption" name="paymentoption">
					<%
					Iterator<String> paymentOptionsIter = paymentOptionsFromConfig.iterator();
					while ( paymentOptionsIter.hasNext() ) {
						String val = paymentOptionsIter.next();
						String selected = ( val.equals( o.getPaymentOption() ) ? "selected" : "" );
					%>
					<option value="<%= val %>" <%= selected %>><%= val %></option>
					<% } // end while %>
					</select>
				<% } // end if-else %>
				</td>
			</tr>
			<tr><td><fmt:message key="payment.paidsofar" /></td><td><%= currency %> <%= CommonUtils.getFormattedPrice( o.getPaid() ) %></td></tr>
			<tr><td><fmt:message key="payment.addpayment" /></td><td><%= currency %> <input type="text" id="payment" name="payment" size="10" value="<%= CommonUtils.getFormattedPrice( o.getTotalPrice() - o.getPaid() ) %>" maxlength="10" onblur="if ( isNaN(this.value) || this.value == 0 ) { alert( JSMessages.entervalidnumbermsg ); this.value = ''; }" /></td></tr>
			</table>
			<br/>
			<% if ( paymentOptionsFromConfig == null || paymentOptionsFromConfig.isEmpty() ) { %>
				<button onclick="editPayment( '<%= o.getOrderId() %>', document.getElementById( 'payment' ).value, <%= o.getTotalPrice() %>, <%= o.getPaid() %>, document.getElementById( 'paymentoption' ).value );"><fmt:message key="save"/></button>
			<% } else { %>
				<button onclick="editPayment( '<%= o.getOrderId() %>', document.getElementById( 'payment' ).value, <%= o.getTotalPrice() %>, <%= o.getPaid() %>, document.getElementById( 'paymentoption' ).options[ document.getElementById( 'paymentoption' ).selectedIndex ].value );"><fmt:message key="save"/></button>
			<% } %>
			<button onclick="document.getElementById('paymentpanel').style.display='none'; document.getElementById('paymentchangelink').innerHTML=JSMessages.change;"><fmt:message key="cancel"/></button>
			<div id="paymentloader" style="display:none;"><img src="../../images/loader.gif" alt="Loading..." title="Loading..."/></div>
		</div>
		</td></tr>
	<% } // end if ( accountingEnabled ) %>
	<!--  End payment -->
	<!-- Package size -->
	<tr>
		<td class="ocell"><fmt:message key="packagesize" /></td>
		<td>
			<% if ( o.getPackageSize() != null && !o.getPackageSize().isEmpty() ) { %><%= o.getPackageSize() %><% } else { %><fmt:message key="none" /><% } %>
			<div style="float:right"><a class="smalltxt" id="packagesizechangelink" href="#" onclick="this.innerHTML='';document.getElementById('packagesizepanel').style.display='block'"><fmt:message key="change" /></a></div>
			<div id="packagesizepanel" class="sgForm" style="display:none;margin-top:5px">
			<% if ( packageSizesFromConfig == null || packageSizesFromConfig.isEmpty() ) { %>
				<input type="text" id="packagesize" name="packagesize" value="<%= o.getPackageSize() != null ? o.getPackageSize() : "" %>" />
			<% } else { %>
				<select id="packagesize" name="packagesize">
				<%
				Iterator<String> packageSizesIter = packageSizesFromConfig.iterator();
				while ( packageSizesIter.hasNext() ) {
					String val = packageSizesIter.next();
					String selected = ( val.equals( o.getPackageSize() ) ? "selected" : "" );
				%>
				<option value="<%= val %>" <%= selected %>><%= val %></option>
				<% } // end while %>
				</select>
			<% } // end if-else %>
				&nbsp;<a href="#" onclick="document.getElementById('packagesize').value='';"><fmt:message key="reset.lower"/></a>
				<br/><br/>
				<% if ( packageSizesFromConfig == null || packageSizesFromConfig.isEmpty() ) { %>
					<button onclick="editPackageSize( '<%= o.getOrderId() %>', document.getElementById( 'packagesize' ).value);"><fmt:message key="save"/></button>
				<% } else { %>
					<button onclick="editPackageSize( '<%= o.getOrderId() %>', document.getElementById( 'packagesize' ).options[ document.getElementById( 'packagesize' ).selectedIndex ].value);"><fmt:message key="save"/></button>
				<% } %>
				<button onclick="document.getElementById('packagesizepanel').style.display='none'; document.getElementById('packagesizechangelink').innerHTML=JSMessages.change;"><fmt:message key="cancel"/></button>
				<div id="packagesizeloader" style="display:none;"><img src="../../images/loader.gif" alt="Loading..." title="Loading..."/></div>
			</div>
		</td>
	</tr>
	<!-- End package size -->
	<!-- Transporter -->
	<tr><td class="ocell">
	<% boolean isTransporterBold = dc.isTransporterMandatory(); %>
	<%= isTransporterBold ? "<b>" : "" %><fmt:message key="transporter" /><%= isTransporterBold ? "</b>" : "" %></td><td>
	<% if ( o.hasTransporters() ) {
			Object t = o.getTransporter();
			if ( t instanceof IKiosk) { %>
				<a href="../setup/setup.jsp?subview=kiosks&form=kioskdetails&id=<%= ((IKiosk)t).getKioskId() %>" target="_new"><%= ((IKiosk)t).getName() %></a>
		 <% } else if ( t instanceof IUserAccount) { %>
				<a href="../setup/setup.jsp?subview=users&form=userdetails&id=<%= ((IUserAccount)t).getUserId() %>" target="_new"><%= ((IUserAccount)t).getFullName() %></a>
		 <% } else { // string %>
				<%= t %>
		 <% } // end if ( o instanceof ... )// end if ( o instanceof ... ) %>
	<% } else { %>
		<fmt:message key="none"/>
	<% } // end if ( o.hasTransporers() ) %> 
	<div style="float:right;"><a class="smalltxt" id="transporterchangelink" href="#" onclick="document.getElementById('transporterpanel').style.display='block';document.getElementById('transporterchangelink').innerHTML='';"><fmt:message key="change"/></a></div>
	<% if ( isTransporterBold ) { %>
	<br/><i>(<fmt:message key="mandatory"/>)</i>
	<% } %>
	<div id="transporterpanel" class="sgForm" style="margin-top:5px;display:none;">
		<!-- Free-form transporter type -->
		<input type="text" id="transporterstr" name="transporterstr" value="<%= o.getTransporter() != null ? o.getTransporter() : "" %>" maxlength="20" />
		&nbsp;<a href="#" onclick="document.getElementById('transporterstr').value='';"><fmt:message key="reset.lower"/></a>
		<br/><br/>
		<button onclick="editTransporter( '<%= o.getOrderId() %>', '<%= ITransporter.TYPE_FREEFORM %>', document.getElementById( 'transporterstr' ).value, <%= dc.isTransporterMandatory() %>);"><fmt:message key="save"/></button>
		<button onclick="document.getElementById('transporterpanel').style.display='none'; document.getElementById('transporterchangelink').innerHTML=JSMessages.change;"><fmt:message key="cancel"/></button>
		<div id="transporterloader" style="display:none;"><img src="../../images/loader.gif" alt="Loading..." title="Loading..."/></div>
	</div>
	</td></tr>
	<!-- End Transporter -->
	</table>
	<!-- Order processing times -->
	<div class="smalltxt" style="color:darkgreen;margin-top:5px;">
	<% if ( o.getProcessingTime() > 0 ) { %>
		<fmt:message key="order.processingtime" />: <b><%= LocalDateUtil.getFormattedMillisInHoursDays( o.getProcessingTime(), locale ) %><%=  o.getDeliveryLeadTime() > 0 ? "," : "" %></b>
	<% } %>
	<% if ( o.getDeliveryLeadTime() > 0 ) { %>
		<fmt:message key="order.deliveryleadtime" />: <b><%= LocalDateUtil.getFormattedMillisInHoursDays( o.getDeliveryLeadTime(), locale ) %></b>
	<% } %>
	</div>
	<!-- End details (nested) table -->
	</td></tr>
	
	<!-- Order items -->
	<tr><td>
		<%
		Set<IDemandItem> items = (Set<IDemandItem>) o.getItems();
		if ( items == null || items.size() == 0 ) {
		%>
			No items.
			<% if ( dc.allowEmptyOrders() ) { %>
			<a id="commands" class="smalltxt" style="float:right" id="itemAddLink" href="#" onclick="document.getElementById('commands').style.display='none'; document.getElementById( 'itemTableAddable' ).style.display='block';"><fmt:message key="add"/></a>
			<% } %>
	   <% } else {
			if ( showVendorStock ) { %>
			<input type="hidden" id="vstockerrors" value="
			<%
			}
			ArrayList<HashMap<String,Object>> displayItems = new ArrayList<HashMap<String,Object>>();
			Iterator<IDemandItem> it = items.iterator();
			String unallocatedItemsBatch = "";
			String partiallyAllocatedItemsBatch = "";
			while ( it.hasNext() ) {
				IDemandItem item = it.next();
				Long mid = item.getMaterialId();
				IMaterial m = null;
				try {
					m = mcs.getMaterial( item.getMaterialId() );
				} catch ( Exception e ) {
					System.out.println( "WARNING: " + e.getClass().getName() + " when getting material " + item.getMaterialId()  + ": " + e.getMessage() );
					continue;
				}
				String name = m.getName();
				boolean isBinaryValued = m.isBinaryValued();
				float quantity = item.getQuantity();
				HashMap<String,Object> itemMap = new HashMap<String,Object>();
				itemMap.put( "materialid", mid );
				itemMap.put( "name", name );
				itemMap.put( "quantity", new Float( quantity ) );
				itemMap.put( "price", item.getFormattedPrice() );
				float itemTax = item.getTax();
				if ( itemTax != 0F )
					itemMap.put( "tax", new Float( itemTax ) );
				itemMap.put( "discount", item.getDiscount() );
				itemMap.put( "amount", CommonUtils.getFormattedPrice( item.computeTotalPrice( false ) ) );
				itemMap.put( "status", ( item.getStatus() == null ? "" : item.getStatus() ) );
				itemMap.put( "isbinaryvalued", new Boolean( isBinaryValued ) );
				boolean isBatchEnabled = m.isBatchEnabled();
				itemMap.put( "isbatchenabled", new Boolean( isBatchEnabled ) );
				if ( isBatchEnabled && item.getItemBatches() != null && !item.getItemBatches().isEmpty()  ) {
					itemMap.put( "batches", item.getBatchesAsMap() );
				}
				float orderedQuantity = item.getOriginalQuantity();
				if ( orderedQuantity > 0F && orderedQuantity != quantity )
					itemMap.put( "orderedquantity", new Float( orderedQuantity ) );
				displayItems.add( itemMap );
				// Check if allocated quantity is less than ordered quantity in case of batch items
				if ( isBatchEnabled ) {
					if ( item.getQuantity() != 0F ) {
						float quantityByBatches = item.getQuantityByBatches();
						if ( quantityByBatches == 0 ) { // check unallocated
							if ( !unallocatedItemsBatch.isEmpty() )
								unallocatedItemsBatch += ", ";
							unallocatedItemsBatch += name;
						}
						if ( quantityByBatches < orderedQuantity ) { // check partially allocated (this will allow shipping to go thru' with warning) 
							if ( !partiallyAllocatedItemsBatch.isEmpty() )
								partiallyAllocatedItemsBatch += ", ";
							partiallyAllocatedItemsBatch += name;
						}
					}
				}
				// Check if quantity exceeds vendor stock, if so then place it in the vstockerrors global JS variable (defined in vieworders.jsp)
				if ( showVendorStock ) {
					Float vstck = vendorStockMap.get( mid );
					if ( vstck == null )
						vstck = new Float(0);
					if ( quantity > vstck.floatValue() ) {
						%>
						<%= name %>,<%= quantity %>,<%= NumberUtil.getFormattedValue( vstck.floatValue() ) %>;
						<%
					}
				}
			}
			if ( showVendorStock ) {
				%>
				" />
				<%
			}
			// Sort display items
			Collections.sort( displayItems, new java.util.Comparator<HashMap<String,Object>>() {
				public int compare( HashMap<String,Object> item1, HashMap<String,Object> item2 ) {
					return ((String)item1.get( "name" )).compareTo( (String) item2.get( "name" ) );
				}
			} );
		%>
		<b><%= o.size() %></b> item(s)
		<% if ( isPending || isConfirmed ) { %> 
		<div id="commands" style="float:right;">
		 <a id="itemChangeLink" class="smalltxt" href="#" onclick="document.getElementById('commands').style.display='none'; document.getElementById( 'itemTableNormal' ).style.display='none'; document.getElementById( 'itemTableEditable' ).style.display='block';"><fmt:message key="change"/></a>
		 &nbsp; <a class="smalltxt" id="itemAddLink" href="#" onclick="document.getElementById('commands').style.display='none'; document.getElementById( 'itemTableAddable' ).style.display='block';"><fmt:message key="add"/></a>
		</div>
		<% } // end if %>
		<br/>
		<div id="itemTableNormal" style="margin-top:5px;">
			<table id="kioskstable" width="100%">
			   <tr>
			   	<th><%= messages.getString( "material" ) %></th>
			   	<th><%= messages.getString( "quantity" ) %></th>
			   	<th><%= messages.getString( "price" ) + ( currency.isEmpty() ? "" : " (" + currency + ")" ) %></th>
			   	<th><%= messages.getString( "amount" ) + ( currency.isEmpty() ? "" : " (" + currency + ")" ) %></th>
			   	<% if ( showVendorStock ) { %>
			   	<th><%= messages.getString( "vendor" ) + " " + messages.getString( "stock" ) %></th>
			   	<% } %>
			   </tr>
			<%  Iterator<HashMap<String,Object>> it2 = displayItems.iterator();
				while ( it2.hasNext() ) { 
				 HashMap<String,Object> item = it2.next();
				 // Change color of cell border if this item was changed
				 String cellstyle = "";
				 if ( IOrder.CHANGED.equals( (String) item.get( "status" ) ) )
					 cellstyle = "style=\"border:2px solid blue;\"";
				 Long mid = (Long) item.get( "materialid" );
				 String name = (String) item.get( "name" );
				 Float quantity = (Float) item.get("quantity");
				 Float discount = (Float) item.get( "discount" );
				 Float itemTax = (Float) item.get( "tax" );
				 Boolean isBatchEnabled = (Boolean) item.get( "isbatchenabled" );
				 List<Map<String,Object>> batches = (List<Map<String,Object>>) item.get( "batches" );
				 Float orderedQuantity = (Float) item.get( "orderedquantity" );
				 String thisVendorStockStyle = vendorStockStyle;
			%>
				<tr>
				 <td><%= name %></td>
				 <td <%= cellstyle %>>
				 	<%= NumberUtil.getFormattedValue( quantity ) %>
				 	<% if ( orderedQuantity != null ) { %>(ordered: <%= NumberUtil.getFormattedValue( orderedQuantity ) %>)<% } %>
				 	<% if ( batches != null && !batches.isEmpty() ) { %>
				 		<table >
				 			<tr>
				 				<td style="background-color:lightgray"><fmt:message key="batch"/></td>
				 				<td style="background-color:lightgray"><fmt:message key="expiry" /></td>
				 				<td style="background-color:lightgray"><fmt:message key="quantity" /></td>
				 			</tr>
				 		<%
				 		Iterator<Map<String,Object>> batchesIt = batches.iterator();
				 		while ( batchesIt.hasNext() ) {
				 			Map<String,Object> batch = batchesIt.next();
				 			%>
				 			<tr>
				 				<td><%= batch.get( IDemandItemBatch.BATCH_ID ) %></td>
				 				<td><%= LocalDateUtil.format( (Date) batch.get( IDemandItemBatch.EXPIRY ), locale, timezone, true ) %></td>
				 				<td><%= NumberUtil.getFormattedValue( (Float) batch.get( IDemandItemBatch.QUANTITY ) ) %></td>
				 			</tr>
				 			<%
				 		} // end while %>
				 		</table>
				 	<% } // end if ( batches != null ... ) %>
				 </td>
				 <td  align="right">
				 	<%= item.get( "price" ) %>
				 	<% if ( discount != null && discount != 0 ) { %>
				 	<br/>[<font style="font-size:10px;color:green;"><%= discount %>% <fmt:message key="discount"/></font>]
				 	<% } %>
				 	<% if ( itemTax != null && itemTax != 0F ) { %>
				 	<br/>[<font style="font-size:10px;color:blue;"><%= itemTax %>% <fmt:message key="tax"/></font>]
				 	<% } %>
				 </td>
				 <td  align="right"><%= item.get( "amount" ) %></td>
				 <% if ( showVendorStock ) {
					 Float vstock = vendorStockMap.get( (Long) item.get( "materialid" ) );
					 if ( vstock == null )
						 vstock = new Float(0);
					 if ( quantity != null && quantity > vstock )
						 thisVendorStockStyle += "color:red;";
				 	%>
				 <td style="<%= thisVendorStockStyle %>"  align="right"><%= NumberUtil.getFormattedValue( vstock.floatValue() ) %></td>
				 <% } %>
				</tr>
			<% } // end while %>
			<tr>
				<td colspan="3"></td><td align="right"><b><%= o.getPriceStatement() %></b></td>
			</tr>
			</table>
			<%= itemsChanged ? "(saved)" : "" %>
			<!-- Include a field for indicating unallocated and partially-allocated items -->
			<% if ( !unallocatedItemsBatch.isEmpty() ) { %>
			<input type="hidden" id="unallocateditemsbatch" value="<%= unallocatedItemsBatch %>" />
			<% } %>
			<% if ( !partiallyAllocatedItemsBatch.isEmpty() ) { %>
			<input type="hidden" id="partiallyallocateditemsbatch" value="<%= partiallyAllocatedItemsBatch %>" />
			<% } %>
		</div>
		<!--  Editable table -->
		<div id="itemTableEditable" class="sgForm" style="display:none;">
			<table id="kioskstable" width="100%">
			   <tr>
			   	<th><%= messages.getString( "material" ) %></th>
			   	<th><%= messages.getString( "quantity" ) %></th>
			   	<th><%= messages.getString( "price" ) + ( currency.isEmpty() ? "" : " (" + currency + ")" ) %></th>
			   	<% if ( showVendorStock ) { %>
			   	<th><%= messages.getString( "vendor" ) + " " + messages.getString( "stock" ) %></th>
			   	<% } %>
			   </tr>
			<%  
				it2 = displayItems.iterator();
				while ( it2.hasNext() ) { 
				 HashMap<String,Object> item = it2.next();
				 String thisVendorStockStyle = vendorStockStyle;
				 String name = (String) item.get( "name" );
				 Float quantity = (Float) item.get( "quantity" );
				 Float discount = (Float) item.get( "discount" );
				 Long mid = (Long) item.get( "materialid" );
				 Boolean isBatchEnabled = (Boolean) item.get( "isbatchenabled" );
				 List<Map<String,Object>> batches = (List<Map<String,Object>>) item.get( "batches" );
				 Float orderedQuantity = (Float) item.get( "orderedquantity" );
				 Float vstock = null;
				 if ( showVendorStock ) {
					 vstock = vendorStockMap.get( mid );
					 if ( vstock == null )
						 vstock = new Float(0);
				 }
				 boolean isBinary = (Boolean) item.get("isbinaryvalued");
			%>
				<tr>
				 <td><%= item.get( "name" ) %></td>
				 <td>
			 		<% if ( isBatchEnabled ) { %>
			 			<div id="quantitydisplay_<%= mid %>"><%= NumberUtil.getFormattedValue( quantity ) %></div>
			 			<% if ( orderedQuantity != null ) { %>(ordered: <%= NumberUtil.getFormattedValue( orderedQuantity ) %>)<% } %>
						<input type="hidden" name="<%= mid %>" value="batch" /> <!-- indicates that this material has batch data -->
						<div id="batchselectpanel_<%= mid %>"></div>
						<div id="batchselectedpanel_<%= mid %>">
							<% if ( batches != null && !batches.isEmpty() )  {
								Iterator<Map<String,Object>> batchesIt = batches.iterator();
								String bidsCSV = ""; 
								while ( batchesIt.hasNext() ) {
									Map<String,Object> batch = batchesIt.next();
									String bid = (String) batch.get( IDemandItemBatch.BATCH_ID );
									bidsCSV += ( bidsCSV.isEmpty() ? "" : "," );
									bidsCSV += bid;
									%>
									<input type="hidden" id="batchnumber_<%= mid %>_<%= bid %>" value="<%= bid %>" />
									<input type="hidden" id="batchquantity_<%= mid %>_<%= bid %>" value="<%= batch.get( IDemandItemBatch.QUANTITY ) %>" />
									<input type="hidden" id="batchexpiry_<%= mid %>_<%= bid %>" value="<%= LocalDateUtil.format( (Date) batch.get( IDemandItemBatch.EXPIRY ), locale, null, true ) %>" />
									<% if ( batch.get( IDemandItemBatch.MANUFACTURER ) != null ) { %>
										<input type="hidden" id="batchmanufacturer_<%= mid %>_<%= bid %>" value="<%= batch.get( IDemandItemBatch.MANUFACTURER ) %>" />
									<% } %>
									<% if ( batch.get( IDemandItemBatch.MANUFACTURED_ON ) != null ) { %>
										<input type="hidden" id="batchmanufactured_<%= mid %>_<%= bid %>" value="<%= LocalDateUtil.format( (Date) batch.get( IDemandItemBatch.MANUFACTURED_ON ), locale, null, true ) %>" />
									<% } %>
									<%
								} // end while
								%>
								<input type="hidden" id="batchnumbers_<%= mid %>" value="<%= bidsCSV %>" />
							<% } // end if %>
						</div>
						<div id="batchentercmdpanel_<%= mid %>">(<a href="javascript:showBatches( 'batchselectpanel_<%= mid %>', '<%= userId %>', '<%= vendorId == null ? "" : vendorId %>', '<%= mid %>', '<%= name %>', true, <%= quantity %>, true, true, true )"><fmt:message key="batch.enter" /></a>)</div>
					<% } else { %>
						 <input type="text" size="3" name="<%= mid %>" value="<%= NumberUtil.getFormattedValue( quantity ) %>" maxlength="5" onblur="validateTransEntry(this,true<%= isBinary ? ",1,1" : "" %>)"/>
						 <% if ( isBinary ) { %>
							 <br/><i>(<fmt:message key="isbinaryvalued.indicator"/>)</i>
						 <% }
					   } %>
				 </td>
				 <td  align="right">
				 	<%= item.get( "price" ) %> 
				 	<% if ( discount != null && discount != 0 ) { %>
				 	&nbsp;[<font style="font-size:10px;color:green"><%= discount %>% <fmt:message key="discount"/></font>]
				 	<% } %>
				 	&nbsp;<a id="<%= mid %>_discountlink" href="#" onclick="document.getElementById('<%= mid %>_discountfield').style.display='block';document.getElementById('<%= mid %>_discountlink').style.display='none';">[<fmt:message key="discount"/>]</a>
				 	<div id="<%= mid %>_discountfield" style="display:none;float:right;"><fmt:message key="discount"/>: <input type="text" size="5" maxlength="6" value="<%= discount %>" id="<%= mid %>_discount" onblur="validateTransEntry(this,true)" />%</div>
				 </td>
				 <% if ( showVendorStock ) { 
					 if ( quantity != null && quantity > vstock )
						 thisVendorStockStyle += "color:red;";
				 	%>
				 <td style="<%= thisVendorStockStyle %>"  align="right"><%= NumberUtil.getFormattedValue( vstock.floatValue() ) %></td>
				 <% } %>
				</tr>
			<% } // end while %>
			</table>
			<fmt:message key="message"/> (160 <%= messages.getString( "charactersallowed" ) %>):
			<br/>
			<textarea id="message2" rows=3 cols=50></textarea>
			<br/><br/>
			<button id="saveItemChanges" onclick="saveOrderItemChanges( 'itemTableEditable', '<%= o.getOrderId() %>', false )"><fmt:message key="save"/></button>
			<button id="cancelItemChanges" onclick="document.getElementById( 'commands' ).style.display='block'; document.getElementById( 'itemTableNormal' ).style.display='block'; document.getElementById( 'itemTableEditable' ).style.display='none';"><fmt:message key="cancel"/></button>
			<div id="editItemsLoader" style="display:none;"><img src="../../images/loader.gif"/></div>
		</div>
		<% } // end if ( items == null ) %>
		<!--  Add the items addable table -->
		<% if ( ( items != null && !items.isEmpty() ) || ( ( items == null || items.isEmpty() ) && dc.allowEmptyOrders() ) ) { %>
		<div id="itemTableAddable" class="sgForm" style="display:none;">
			<br/>
			<b><fmt:message key="materials.addnew"/></b>:
			<table id="kioskstable" width="100%">
			   <tr>
			   	<th><%= messages.getString( "material" ) %></th>
			   	<th><%= messages.getString( "quantity" ) %></th>
			   	<th><%= messages.getString( "price" ) + ( currency.isEmpty() ? "" : " (" + currency + ")" ) %></th>
			   	<% if ( showVendorStock ) { %>
			   	<th><%= messages.getString( "vendor" ) + " " + messages.getString( "stock" ) %></th>
			   	<% } %>
			   </tr>
			<%  Iterator<IInvntry> itInv = inventories.iterator();
				boolean noItems = true;
				while ( itInv.hasNext() ) {
				 IInvntry inv = itInv.next();
				 Long mid = inv.getMaterialId();
				 if ( o.getItem( mid ) == null ) { // i.e. item is not in this order
					 IMaterial m = null;
				     try {
				 	 	m = mcs.getMaterial( mid );
				     } catch ( Exception e ) {
				    	 System.out.println( "WARNING: " + e.getClass().getName() + " when getting material " + mid + ": " + e.getMessage() );
				    	 continue;
				     }
				     boolean isBatchEnabled = m.isBatchEnabled();
				     String name = m.getName();
				 	 noItems = false;
				 	 String thisVendorStockStyle = vendorStockStyle;
				 	 Float vstock = null;
				 	 if ( showVendorStock ) {
				 		 vstock = vendorStockMap.get( mid );
				 		 if ( vstock == null )
				 			 vstock = new Float(0);
				 	 }
			%>
						<tr>
						 <td><%= m.getName() %></td>
						 <td>
						 <% if ( isBatchEnabled ) { %>
							<input type="hidden" name="<%= mid %>" value="batch" /> <!-- indicates that this material has batch data -->
							<div id="batchselectpanel_<%= mid %>"></div>
							<div id="batchselectedpanel_<%= mid %>"></div>
							<div id="batchentercmdpanel_<%= mid %>">(<a href="javascript:showBatches( 'batchselectpanel_<%= mid %>', '<%= userId %>', '<%= k.getKioskId() %>', '<%= mid %>', '<%= name %>', true, 0, true, true, true )"><fmt:message key="batch.enter" /></a>)</div>
						<% } else { %>
							 <input type="text" size="3" name="<%= m.getMaterialId() %>" value="" maxlength="5" onblur="validateTransEntry(this,false<%= m.isBinaryValued() ? ",1,1" : "" %>)"/>
							 <% if ( m.isBinaryValued() ) { %>
							 <br/><i>(<fmt:message key="isbinaryvalued.indicator"/>)</i>
							 <% } %>
						<% } %>
						 </td>
						 <td align="right"><%= CommonUtils.getFormattedPrice( m.getRetailerPrice() ) %></td>
						 <% if ( showVendorStock ) { 
							 if ( vstock == 0 )
								 thisVendorStockStyle += "color:red;";
						 	%>
						 <td style="<%= thisVendorStockStyle %>" align="right"><%= NumberUtil.getFormattedValue( vstock.floatValue() ) %></td>
						 <% } %>
						</tr>
			<%   } // end if
				} // end while
				if ( noItems ) { %>
					<tr>
						<td colspan="<%= showVendorStock ? "4" : "3" %>"><fmt:message key="nomoretoadd" /></td>
					</tr>
			<%  } %>	 
			</table>
			<% if ( !noItems ) { %>
			<fmt:message key="message"/> (160 <%= messages.getString( "charactersallowed" ) %>):
			<br/>
			<textarea id="message3" rows=3 cols=50></textarea>
			<br/><br/>
			<button id="saveItemChanges" onclick="saveOrderItemChanges( 'itemTableAddable', '<%= o.getOrderId() %>', true )"><fmt:message key="save"/></button>
			<% } else { %>
			<br/>
			<% } %>
			<button id="cancelItemChanges" onclick="document.getElementById( 'commands' ).style.display = 'block'; document.getElementById( 'itemTableAddable' ).style.display='none';"><fmt:message key="cancel"/></button>
			<div id="addItemsLoader" style="display:none;"><img src="../../images/loader.gif"/></div>
		</div>
		<% } %>
</table>
<% } // end if ( errMsg != null ) %>
</fmt:bundle>