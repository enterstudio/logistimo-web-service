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

var fs = require('fs');
require('./logistimoen.js');
stringify("$operations",$operations);
stringify("$orderTypes",$orderTypes);
stringify("$nMsg",$nMsg);
stringify("$sMsg",$sMsg);
stringify("$tMsg",$tMsg);
stringify("$tPMsg",$tPMsg);
stringify("$networkErrMsg",$networkErrMsg);
stringify("$labeltext",$labeltext);
stringify("$buttontext",$buttontext);
stringify("$messagetext",$messagetext);
function stringify(label,data) {
    for(var d in data) {
        data[d] = data[d].replace('Entities','Stores')
        .replace('Entity','Store')
        .replace('entities','stores')
        .replace('entity','store')
        .replace('Customer','Receiving store')
        .replace('customer','receiving store')
        .replace('Vendor','Issuing store')
        .replace('vendor','issuing store')
        .replace('Issues','Issues/Net Utilization')
        .replace('issues','issues/net utilization')
        .replace('An Store','A Store')
        .replace('An store','A store')
        .replace('an store','a store')
        .replace('an Store','a Store')
        .replace('A Issuing store','An Issuing store')
        .replace('A issuing store','An issuing store')
        .replace('a Issuing store','an Issuing store')
        .replace('a issuing store','an issuing store')
        .replace('Purchase orders','Indents - Receipts')
        .replace('purchase orders','indents - receipts')
        .replace('Purchase order','Indent - receipt')
        .replace('purchase order','indent - receipt')
        .replace('Sales orders', 'Indents - issues')
        .replace('Sales Orders', 'Indents - Issues')
        .replace('sales order', 'indents - issue')
        .replace('Sales order', 'Indents - issue')
        .replace('Purchases', 'Indents - receipts')
        .replace('purchases', 'indents - receipts')
        .replace('Sales', 'Indents - issues')
        .replace('sales', 'indents - issues')
        .replace('stock on hand','total stock')
        .replace('Stock on hand','Total stock');
    }
    console.log(label + '=' + JSON.stringify(data) + ';\n');
    fs.appendFileSync("out/logistimoconverted.js",label + "=" + JSON.stringify(data) + ';\n');
}
