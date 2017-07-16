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

logistimoApp.constant('OPTIMIZER', {
	compute :  {
		'NONE' : -1,
		'CONSUMPTION' : 0,
		'FORECASTEDDEMAND' : 100,
		'EOQ' : 200
	},
	frequency :  {
		'DAILY' : 'daily',
		'WEEKLY' : 'weekly',
		'MONTHLY' : 'monthly'
	}
});
logistimoApp.constant('INVENTORY', {
	models :  {
		'NONE' : '',
		'SQ' : 'sq',
		'MINMAX' : 'mm',
		'KANBAN' : 'kn'
	},
    stock : {
        'STOCKOUT' : '200',
        'UNDERSTOCK' : '201',
        'OVERSTOCK' : '202'
    }
});
logistimoApp.constant('ORDER', {
	statusTxt : {
		'cn' : 'Cancelled',
		'cf': 'Confirmed' ,
		'cm': 'Shipped',
		'fl':'Fulfilled',
		'pn' : 'Pending',
		'bo': 'Backordered',
		'op': 'Pending',
		'sp': 'Shipped'
	},
    statusLabel : {
        'cn' : 'Cancelled',
        'cf': 'Confirmed' ,
        'cm': 'Completed',
        'fl':'Fulfilled',
        'pn' : 'Pending',
        'bo': 'Backordered',
        'op': 'Open',
        'sp': 'Shipped'
    },
	notifStatusLabel : {
        'cn' : 'Cancelled',
        'cf': 'Confirmed' ,
        'fl':'Fulfilled',
        'pn' : 'Pending',
        'bo': 'Backordered',
        'cm': 'Shipped'
    },
	notifShipmentStatusLabel : {
		'pn' : 'Pending',
		'cn' : 'Cancelled',
		'sp': 'Shipped' ,
		'fl':'Fulfilled'
	},
	'CANCELLED' : 'cn',
	'CONFIRMED' : 'cf',
	'COMPLETED' : 'cm',
	'FULFILLED' : 'fl',
	'PENDING' : 'pn',
	'BACKORDERED': 'bo',
	'OPEN': 'op',
	'SHIPPED': 'sp',
	'EDIT' :  'edit',
    'ALLOCATE' : 'allocate',
    'SHIP' : 'ship',
	'FULFILL' : 'fulfill',
	'CANCEL' : 'cancel',
	'CREATE_SHIPMENT' : 'create_shipment',
	'CONFIRM' : 'confirm',
	'APPROVAL_REQUIRED' : 'required'
});
logistimoApp.constant('ORDERSTATUSTEXT', {
	statusTxt : {
		'cn' : 'Cancel',
		'cf': 'Confirm' ,
		'cm': 'Ship',
		'fl':'Fulfill',
		'pn' : 'Reopen',
		'op': 'Pending',
		'sp': 'Ship'
	}
});
logistimoApp.run(function ($rootScope,INVENTORY,APPTYPE) {
    $rootScope.INVENTORY = INVENTORY;
    $rootScope.APPTYPE = APPTYPE;
});
logistimoApp.constant('NOTIFICATIONS', {
	orders : {
		"%creationTime%": {"key": "createdon", "size": "20"},
		"%customer%": {"key": "customer", "size": "25"},
		"%customerCity%": {"key": "variable.customercity", "size": "15"},
		"%requiredByDate%": {"key": "requiredByDate", "size": "20"},
		"%estimatedDateOfArrival%": {"key": "estimatedDateOfArrival", "size": "20"},
		"%comment%": {"key": "comment", "size": "20"},
		"%commentedOn%": {"key": "commentedOn", "size": "20"},
		"%commentedBy%": {"key": "commentedBy", "size": "20"},
		"%materials%": {"key": "variable.materials", "size": "0"},
		"%materialsWithMetadata%": {"key": "variable.materialswithmetadata", "size": "0"},
		"%numberOfItems%": {"key": "numberofitems", "size": "6"},
		"%orderId%": {"key": "order.id", "size": "7"},
		"%payment%": {"key": "payment", "size": "10"},
		"%orderStatus%": {"key": "status", "size": "7"},
		"%statusChangeTime%": {"key": "variable.statuschangetime", "size": "20"},
		"%vendor%": {"key": "vendor", "size": "25"},
		"%vendorCity%": {"key": "variable.vendorcity", "size": "15"},
		"%updationTime%": {"key": "updatedon", "size": "20"},
		"%user%": {"key": "user", "size": "25"},
		"%userId%": {"key": "user.id", "size": "25"}
	},
    shipments : {
        "%transporter%": {"key": "transporter", "size": "20"},
        "%reasonShipment%": {"key": "reasonForShipment", "size": "20"},
        "%reasonCancel%": {"key": "reasonForCancel", "size": "20"},
        "%reasonFulfil%": {"key": "reasonForFulfil", "size": "20"},
        "%trackingID%": {"key": "trackingid", "size": "20"},
        "%packageSize%": {"key": "packagesize", "size": "20"},
        "%creationTime%": {"key": "createdon", "size": "20"},
        "%updationTime%": {"key": "updatedon", "size": "20"},
        "%comment%": {"key": "comment", "size": "20"},
        "%commentedOn%": {"key": "commentedOn", "size": "20"},
        "%commentedBy%": {"key": "commentedBy", "size": "20"},
        "%updatedBy%": {"key": "updatedby", "size": "20"},
        "%customer%": {"key": "customer", "size": "25"},
        "%customerCity%": {"key": "variable.customercity", "size": "7"},
        "%estimatedDateOfArrival%": {"key": "estimatedDateOfArrival", "size": "20"},
        "%materials%": {"key": "variable.materials", "size": "0"},
        "%materialsWithQuantity%": {"key": "variable.materialswithquantities", "size": "0"},
        "%materialsWithFulfiled%": {"key": "variable.materialswithfulfiled", "size": "0"},
        "%materialsWithAllocated%": {"key": "variable.materialswithallocated", "size": "0"},
        "%numberOfItems%": {"key": "numberofitems", "size": "6"},
        "%shipmentID%": {"key": "shipment.id", "size": "10"},
        "%shipmentStatus%": {"key": "shipmentStatus", "size": "25"},
        "%user%": {"key": "user", "size": "20"},
        "%userId%": {"key": "user.id", "size": "20"},
        "%vendor%": {"key": "vendor", "size": "20"},
        "%vendorCity%": {"key": "variable.vendorcity", "size": "15"}
    },
	inventory : {
		"%abnormalStockDuration%": {"key": "abnormalstockduration", "size": "10"},
		"%abnormalStockEvent%": {"key": "abnormalstockevent", "size": "10"},
		"%batch%": {"key": "batch", "size": "25"},
		"%creationTime%": {"key": "createdon", "size": "20"},
		"%customer%": {"key": "customer", "size": "25"},
		"%customerCity%": {"key": "variable.customercity", "size": "15"},
		"%expiry%": {"key": "expiry", "size": "10"},
		"%material%": {"key": "material", "size": "0"},
		"%maxStock%": {"key": "maxstock", "size": "5"},
		"%minStock%": {"key": "minstock", "size": "5"},
		"%quantity%": {"key": "quantity", "size": "6"},
		"%reason%": {"key": "reason", "size": "6"},
		"%safetyStock%": {"key": "inventory.safetystock", "size": "6"},
		"%transType%": {"key": "transtype", "size": "10"},
		"%vendor%": {"key": "vendor", "size": "25"},
		"%vendorCity%": {"key": "variable.vendorcity", "size": "15"},
		"%updationTime%": {"key": "updatedon", "size": "20"},
		"%user%": {"key": "user", "size": "25"},
		"%userId%": {"key": "user.id", "size": "25"},
		"%status%": {"key": "status", "size": "6"}
	},
	setup : {
		"%creationTime%": {"key": "createdon", "size": "20"},
		"%entity%":{"key":"kiosk","size":"25"},
		"%entityCity%":{"key":"village","size":"15"},
		"%ipAddress%":{"key":"user.ipaddress","size":"16"},
		"%lastLoginTime%":{"key":"lastlogin","size":"18"},
		"%material%":{"key":"material","size":"0"},
		"%mobilePhone%":{"key":"user.mobile","size":"15"},
		"%role%":{"key":"user.role","size":"20"},
		"%user%": {"key": "user", "size": "25"},
		"%userId%": {"key": "user.id", "size": "25"},
		"%updationTime%": {"key": "updatedon", "size": "20"},
		"%registeredBy%": {"key": "registeredby", "size": "20"},
		"%updatedBy%": {"key": "updatedby", "size": "20"},
		"%serialnum%" : {"key":"asset.serial.number","size":"25"},
		"%assetType%" : {"key":"asset.type","size":"25"},
		"%manufacturer%" : {"key":"manufacturer","size":"0"},
		"%model%" : {"key":"asset.model","size":"25"},
		"%relatedAssetSerialNumber%": {"key": "relatedassetserialnumber", "size" :"25"},
		"%relatedAssetType%": {"key": "relatedassetype", "size": "25"},
		"%relatedAssetModel%": {"key": "relatedassetmodel", "size": "25"},
		"%relatedAssetManufacturer%": {"key": "relatedassetmanufacturer", "size": "25"},
		"%manufactureYear%":{"key":"asset.manufacture.year","size":"4"}
	},
    temperature: {
        "%entity%": {"key": "kiosk", "size": "25"},
        "%entityCity%": {"key": "village", "size": "15"},
        "%serialnum%": {"key": "asset.serial.number", "size": "0"},
        "%manufacturer%": {"key": "manufacturer", "size": "0"},
        "%model%": {"key": "asset.model", "size": "0"},
        "%temperature%": {"key": "temperatureeventtime", "size": "20"},
        "%statusUpdatedTime%": {"key": "statusupdatedtime", "size": "20"},
        "%monitoringPoint%": {"key": "monitoringpoint", "size": "20"},
        "%minTemperature%": {"key": "temperaturemin", "size": "6"},
        "%maxTemperature%": {"key": "temperaturemax", "size": "6"},
		"%assetType%":{"key":"asset.type", "size":"20"},
		"%manufactureYear%":{"key":"asset.manufacture.year","size":"4"},
		"%assetStatus%":{"key":"status","size":"10"}
    },
	assetAlarms: {
		"%entity%": {"key": "kiosk", "size": "25"},
		"%entityCity%": {"key": "village", "size": "15"},
		"%serialnum%": {"key": "asset.serial.number", "size": "0"},
		"%manufacturer%": {"key": "manufacturer", "size": "0"},
		"%model%": {"key": "asset.model", "size": "0"},
		"%statusUpdatedTime%": {"key": "statusupdatedtime", "size": "20"},
		"%sensorId%": {"key": "sensor.id", "size": "5"},
		"%assetType%":{"key":"asset.type", "size":"20"},
		"%manufactureYear%":{"key":"asset.manufacture.year","size":"4"},
		"%assetStatus%":{"key":"status","size":"10"}
	},
	accounts: {
		"%creditLimit%":{"key":"creditlimit","size":"10"},
		"%customer%":{"key":"customer","size":"25"},
		"%customerCity%": {"key": "variable.customercity", "size": "15"},
		"%payable%":{"key":"payable","size":"10"},
		"%vendor%": {"key": "vendor", "size": "25"},
		"%vendorCity%": {"key": "variable.vendorcity", "size": "15"}
	},
	vars : {
		'orders' : ["%creationTime%","%comment%","%commentedBy%","%commentedOn%","%customer%","%customerCity%","%requiredByDate%","%estimatedDateOfArrival%","%materials%","%materialsWithMetadata%","%numberOfItems%","%orderId%","%payment%","%orderStatus%","%statusChangeTime%","%vendor%","%vendorCity%","%updationTime%","%user%","%userId%"],
		'shipments' : ["%transporter%","%reasonShipment%","%reasonCancel%","%reasonFulfil%","%trackingID%","%packageSize%","%creationTime%","%updationTime%","%comment%","%commentedOn%","%commentedBy%","%updatedBy%","%customer%","%customerCity%","%estimatedDateOfArrival%","%materials%","%materialsWithQuantity%","%materialsWithQuantity%","%materialsWithAllocated%","%numberOfItems%","%shipmentID%","%shipmentStatus%","%user%","%userId%","%vendor%","%vendorCity%"],
		'inventory' : ["%abnormalStockDuration%","%abnormalStockEvent%","%batch%","%creationTime%","%customer%","%customerCity%","%expiry%","%material%","%maxStock%","%minStock%","%quantity%","%reason%","%safetyStock%","%transType%","%vendor%","%vendorCity%","%updationTime%","%user%","%userId%"],
		'accounts' : ["%creditLimit%","%customer%","%customerCity%","%payable%","%vendor%","%vendorCity%"],
        'setup': ["%creationTime%", "%entity%", "%entityCity%", "%ipAddress%", "%lastLoginTime%", "%material%", "%mobilePhone%", "%role%", "%user%", "%userId%", "%updationTime%","%serialnum%","%assesttype%","%manufacturer%","%assetModel%","%relatedAssetSerialNumber%","%relatedAssetType%","%relatedAssetModel%","%relatedAssetManufacturer%"],
        'temperature': ["%serialnum%", "%manufacturer%", "%model%","%entity%", "%entityCity%", "%temperature%", "%statusUpdatedTime%", "%minTemperature%", "%maxTemperature%","%monitoringPoint%"]
	},
	ordersLabel : {
		'events' : [{"id" : "com.logistimo.orders.entity.Order:100", "name" : "Fulfilment due",
					"params" : [{"id" : "reminddaysbefore", "name" : "Remind days before",
					"type" : "text", "size" : "3", "placeholder" : "days", "prefix" : " before ",
					"value" : "30", "alert" : "Please enter a valid number of days for fulfillment due reminder"}]},
					{"id" : "com.logistimo.orders.entity.Order:6", "name" : "No order activity",
					"params" : [{"id" : "inactiveduration", "name" : "Days since no activity",
					"type" : "text", "size" : "3", "placeholder" : "days", "prefix" : " since ", "value" : "10",
					"alert" : "Please enter a valid number of days for orders inactivity duration"}]},
					{"id" : "com.logistimo.orders.entity.Order:1", "name" : "Order created"},
					{"id" : "com.logistimo.orders.entity.Order:5", "name" : "Order expired/untouched",
					"params" : [{"id" : "inactiveduration", "name" : "Days inactive", "type" : "text",
					"size" : "3", "placeholder" : "days", "prefix" : " for ", "value" : "10",
					"alert" : "Please enter a valid number of days for order inactivity duration"}]},
					{"id" : "com.logistimo.orders.entity.Order:2", "name" : "Order modified"},
					{"id" : "com.logistimo.orders.entity.Order:101", "name" : "Order status changed",
					"params" : [{"id" : "status", "name" : "Status", "type" : "list", "placeholder" : "-- Select status --", "prefix" : "status", "alert": "Please select status"}]},
					{ "id" : "com.logistimo.orders.entity.Order:302", "name" : "Payment made on order" },
					{ "id" : "com.logistimo.orders.entity.Order:150", "name" : "Comment added on order" }
		]
	},
	shipmentsLabel : {
		'events' : [{"id" : "com.logistimo.shipments.entity.Shipment:1", "name" : "Shipment created"},
			{"id" : "com.logistimo.shipments.entity.Shipment:2", "name" : "Shipment modified"},
			{"id" : "com.logistimo.shipments.entity.Shipment:101", "name" : "Shipment status changed",
				"params" : [{"id" : "status", "name" : "Status", "type" : "list", "placeholder" : "-- Select status --", "prefix" : "status", "alert": "Please select status"}]},
			{ "id" : "com.logistimo.shipments.entity.Shipment:150", "name" : "Comment added on shipment" }]
	},
	inventoryLabel : {
		'events' : [
			{"id" : "com.logistimo.inventory.entity.InvntryBatch:5", "name" : "Batch expiry",
			"params" : [{"id" : "expiresindays", "name" : "Expires in [days]", "type" : "text", "size" : "3",
			"placeholder" : "days", "prefix" : "expires in ", "value" : "90",
			"alert" : "Please enter a valid number of days, in which the batch expires"}]},
			{"id" : "com.logistimo.inventory.entity.Transaction:6", "name" : "No inventory activity",
			"params" : [{"id" : "inactiveduration", "name" : "Days since no activity", "type" : "text",
			"size" : "3", "placeholder" : "days", "prefix" : " since ", "value" : "10",
			"alert" : "Please enter a valid number of days for inventory transaction inactivity duration"}]},
			{"id" : "com.logistimo.inventory.entity.Invntry:200", "name" : "Out of stock"},
			{ "id" : "com.logistimo.inventory.entity.Transaction:203", "name" : "Stock count differs from current stock",
			"params" : [{"id" : "stockcountthreshold", "name" : "Stock count threshold [%]", "type" : "text",
			"size" : "3", "placeholder" : "%", "prefix" : " > ", "value" : "10",
			"alert" : "Please enter a valid number (%) for threshold"}]},
			{ "id" : "com.logistimo.inventory.entity.Transaction:204", "name" : "Stock counted",
			"params" : [ { "id" : "reason", "type" : "list", "prefix" : "stockCounted", "placeholder" : "-- Select Reason --", "values" : "", "name" : "Reason"}],
			"extraParams": { "id" : "status","type" : "list", prefix : "stockCounted", "placeholder": "-- Select Status --", name: "Status"}},
			{ "id" : "com.logistimo.inventory.entity.Transaction:205", "name" : "Stock issued",
			"params" : [ { "id" : "reason", "type" : "list", "prefix" : "stockIssued", "placeholder" : "-- Select Reason --", "name" : "Reason"}],
			"extraParams": { "id" : "status","type" : "list", prefix : "stockIssued", "placeholder": "-- Select Status --", name: "Status"}},
			{ "id" : "com.logistimo.inventory.entity.Transaction:209", "name" : "Stock transferred",
			"params" : [ { "id" : "reason", "type" : "list", "prefix" : "stockTransferred", "placeholder" : "-- Select Reason --", "name" : "Reason"}],
			"extraParams": { "id" : "status","type" : "list", prefix : "stockTransferred", "placeholder": "-- Select Status --", name: "Status"}},
			{ "id" : "com.logistimo.inventory.entity.Transaction:206", "name" : "Stock received",
			"params" : [ { "id" : "reason", "type" : "list", "prefix" : "stockReceived", "placeholder" : "-- Select Reason --", "name" : "Reason"}],
			"extraParams": { "id" : "status","type" : "list", prefix : "stockReceived", "placeholder": "-- Select Status --", name: "Status"}},
			{ "id" : "com.logistimo.inventory.entity.Invntry:208", "name" : "Stock back to normal level" },
			{ "id" : "com.logistimo.inventory.entity.Transaction:207", "name" : "Stock discarded",
			"params" : [ { "id" : "reason", "type" : "list", "prefix" : "stockDiscarded", "placeholder" : "-- Select Reason --", "name" : "Reason"}],
			"extraParams": { "id" : "status","type" : "list", prefix : "stockDiscarded", "placeholder": "-- Select Status --", name: "Status"}},
			{ "id" : "com.logistimo.inventory.entity.Invntry:201", "name" : "< Min." },
			{ "id" : "com.logistimo.inventory.entity.Invntry:202", "name" : "> Max." }]
	},
	accountLabel : {
		'events' : [{"id" : "com.logistimo.accounting.entity.Account:300", "name" : "Credit limit exceeded"}]
	},
	setupLabel : {
		'events' : [
			{ "id" : "com.logistimo.entities.entity.Kiosk:1", "name" : "Entity created" },
			{ "id" : "com.logistimo.entities.entity.Kiosk:2", "name":"Entity modified" },
			{ "id" : "com.logistimo.entities.entity.Kiosk:3", "name":"Entity deleted" },
			{ "id" : "com.logistimo.inventory.entity.Invntry:1", "name" : "Inventory created" },
			{ "id" : "com.logistimo.inventory.entity.Invntry:2", "name" : "Inventory modified" },
			{ "id" : "com.logistimo.inventory.entity.Invntry:3", "name" : "Inventory deleted" },
			{ "id" : "com.logistimo.materials.entity.Material:1", "name" : "Material created" },
			{ "id" : "com.logistimo.materials.entity.Material:2", "name" : "Material modified" },
			{ "id" : "com.logistimo.materials.entity.Material:3", "name" : "Material deleted" },
			{ "id" : "com.logistimo.users.entity.UserAccount:6", "name" : "No user logins",
			"params" : [{"id" : "inactiveduration", "name" : "Days since no activity", "type" : "text", "size" : "3",
			"placeholder" : "days", "prefix" : " since ", "value" : "10","alert" : "Please enter a valid number of days for user inactivity duration"}]},
			{ "id" : "com.logistimo.users.entity.UserAccount:1", "name" : "User created" },
			{ "id" : "com.logistimo.users.entity.UserAccount:2", "name" : "User modified" },
			{ "id" : "com.logistimo.users.entity.UserAccount:3", "name" : "User deleted" },
			{ "id" : "com.logistimo.users.entity.UserAccount:5", "name" : "User dormant/inactive",
			"params" : [{"id" : "inactiveduration", "name" : "Days since no activity", "type" : "text", "size" : "3",
			"placeholder" : "days", "prefix" : " since ", "value" : "10", "alert" : "Please enter a valid number of days for user inactivity duration"}] },
			{ "id" : "com.logistimo.users.entity.UserAccount:400", "name" : "User login IP Address Matched",
			"params" : [ { "id" : "ipaddress", "name" : "IP Address starts with", "type" : "text", "size" : "20", "placeholder" : "IP patterns (CSV)", "prefix" : " IP starts with ",
			"alert" : "Please enter a valid string pattern" }]},
			{ "id" : "com.logistimo.assets.entity.Asset:1", "name" : "Asset created" },
			{ "id" : "com.logistimo.assets.entity.Asset:2", "name":"Asset modified" },
			{ "id" : "com.logistimo.assets.entity.Asset:3", "name":"Asset deleted" },
			{ "id" : "com.logistimo.assets.entity.AssetRelation:1", "name" : "Asset relationship created" },
			{ "id" : "com.logistimo.assets.entity.AssetRelation:3", "name":"Asset relationship deleted" }
		]
    },
    temperatureLabel: {
        'events': [
            {
                "id": "com.logistimo.assets.entity.AssetStatus:500",
                "name": "High excursion",
                "params": [{
                    "id": "remindminsafter",
                    "name": "Remind minutes after",
                    "type": "text",
                    "size": "4",
                    "placeholder": "minutes",
                    "prefix": " after ",
                    "value": "30",
                    "alert": "Please enter valid minutes for 'High excursion'"
                }]
            },
            {
                "id": "com.logistimo.assets.entity.AssetStatus:501",
                "name": "Low excursion",
                "params": [{
                    "id": "remindminsafter",
                    "name": "Remind minutes after",
                    "type": "text",
                    "size": "4",
                    "placeholder": "minutes",
                    "prefix": " after ",
                    "value": "30",
                    "alert": "Please enter valid minutes for 'Low excursion'"
                }]
            },
            {
				"id": "com.logistimo.assets.entity.AssetStatus:502",
				"name": "Incursion"
			},
            {
                "id": "com.logistimo.assets.entity.AssetStatus:6",
                "name": "No data from device",
                "params": [{
                    "id": "inactiveduration",
                    "name": "Days since no activity",
                    "type": "text",
                    "size": "3",
                    "placeholder": "days",
                    "prefix": " since ",
                    "value": "10",
                    "alert": "Please enter valid number of days for 'No data from device'"
                }]
            },
            {
                "id": "com.logistimo.assets.entity.AssetStatus:504",
                "name": "High warning",
                "params": [{
                    "id": "remindminsafter",
                    "name": "Remind minutes after",
                    "type": "text",
                    "size": "4",
                    "placeholder": "minutes",
                    "prefix": " after ",
                    "value": "30",
                    "alert": "Please enter valid minutes for 'High warning'"
                }]
            },
            {
                "id": "com.logistimo.assets.entity.AssetStatus:505",
                "name": "Low warning",
                "params": [{
                    "id": "remindminsafter",
                    "name": "Remind minutes after",
                    "type": "text",
                    "size": "4",
                    "placeholder": "minutes",
                    "prefix": " after ",
                    "value": "30",
                    "alert": "Please enter valid minutes for 'Low warning'"
                }]
            },
            {
                "id": "com.logistimo.assets.entity.AssetStatus:506",
                "name": "High alarm",
                "params": [{
                    "id": "remindminsafter",
                    "name": "Remind minutes after",
                    "type": "text",
                    "size": "4",
                    "placeholder": "minutes",
                    "prefix": " after ",
                    "value": "30",
                    "alert": "Please enter valid minutes for 'High alarm'"
                }]
            },
            {
                "id": "com.logistimo.assets.entity.AssetStatus:507",
                "name": "Low alarm",
                "params": [{
                    "id": "remindminsafter",
                    "name": "Remind minutes after",
                    "type": "text",
                    "size": "4",
                    "placeholder": "minutes",
                    "prefix": " after ",
                    "value": "30",
                    "alert": "Please enter valid minutes for 'Low alarm'"
                }]
            },
			{
				"id": "com.logistimo.assets.entity.AssetStatus:101",
				"name": "Status changed",
				"params" : [{"id" : "status", "name" : "Status", "type" : "list", "placeholder" : "-- Select status --", "alert": "Please select status"}]
			}
        ]
    },
	assetAlarmsLabel: {
		'events': [
			{
				"id": "com.logistimo.assets.entity.AssetStatus:508",
				"name": "Sensor disconnected",
				"params": [{
					"id": "remindminsafter",
					"name": "Remind minutes after",
					"type": "text",
					"size": "4",
					"placeholder": "minutes",
					"prefix": " after ",
					"value": "30",
					"alert": "Please enter valid minutes for 'Sensor disconnected'"
				}]
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:509",
				"name": "Sensor connected"
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:510",
				"name": "Battery low - warning",
				"params": [{
					"id": "remindminsafter",
					"name": "Remind minutes after",
					"type": "text",
					"size": "4",
					"placeholder": "minutes",
					"prefix": " after ",
					"value": "30",
					"alert": "Please enter valid minutes for 'Battery low - warning'"
				}]
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:511",
				"name": "Battery low - alarm",
				"params": [{
					"id": "remindminsafter",
					"name": "Remind minutes after",
					"type": "text",
					"size": "4",
					"placeholder": "minutes",
					"prefix": " after ",
					"value": "30",
					"alert": "Please enter valid minutes for 'Battery low - alarm'"
				}]
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:512",
				"name": "Battery normal"
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:513",
				"name": "Device inactive",
				"params": [{
					"id": "remindminsafter",
					"name": "Remind minutes after",
					"type": "text",
					"size": "4",
					"placeholder": "minutes",
					"prefix": " after ",
					"value": "30",
					"alert": "Please enter valid minutes for 'Device inactive'"
				}]
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:514",
				"name": "Device active"
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:515",
				"name": "Power outage",
				"params": [{
					"id": "remindminsafter",
					"name": "Remind minutes after",
					"type": "text",
					"size": "4",
					"placeholder": "minutes",
					"prefix": " after ",
					"value": "30",
					"alert": "Please enter valid minutes for 'Power outage'"
				}]
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:516",
				"name": "Power available"
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:517",
				"name": "Device disconnected",
				"params": [{
					"id": "remindminsafter",
					"name": "Remind minutes after",
					"type": "text",
					"size": "4",
					"placeholder": "minutes",
					"prefix": " after ",
					"value": "30",
					"alert": "Please enter valid minutes for 'Device disconnected'"
				}]
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:518",
				"name": "Device connected"
			},
			{
				"id": "com.logistimo.assets.entity.AssetStatus:102",
				"name": "Status changed",
				"params" : [{"id" : "status", "name" : "Status", "type" : "list", "placeholder" : "-- Select status --", "alert": "Please select status"}]
			}
		]
	}
});
logistimoApp.constant('APPTYPE', {
	'JAVA_FEATURE_PHONE_APP' : 'j2me',
	'ANDROID_SMART_PHONE_APP' :'android'
});
logistimoApp.constant('ASSET', {
	'MONITORING_ASSET' : 1,
	'MONITORED_ASSET' : 2,
	'TEMPERATURE_LOGGER' : 1,
	'EXTERNAL_SENSOR_ALARM_TYPE' : 1,
	'ACTIVITY_ALARM_TYPE' : 4
});
logistimoApp.constant('APPROVAL', {
	'REQUEST_APPROVAL' : 'request',
	'SHOW_HISTORY': 'history',
	'APPROVE' : 'approve',
	'REJECT' : 'reject',
	'CANCEL' : 'cancel'
});
logistimoApp.constant('PATTERNS', {
	'LATITUDE':'^-?(([0-8])?((([0-9])(\\.\\d{1,8})?)|(90(\\.[0]{0,8})?)))$',
	'LONGITUDE':'^-?(((1[0-7][0-9])|(\\d{1,2})|180)(\\.\\d{1,8})?)$',
	'TAX' : '^(\\d{0,2}(\\.\\d{1,2})?|100(\\.00?)?)$',
	'PRICE': '^(\\d{0,9}(\\.\\d{1,2})?|1000000000(\\.00?)?)$',
	'TEMPERATURE' : '^-?(\\d{0,2})(\\.\\d{1,2})?$'
});