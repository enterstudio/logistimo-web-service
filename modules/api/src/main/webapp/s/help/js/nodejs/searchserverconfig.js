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

function updateTargetSettings(n){var r=n.port,f=n.host,e=n.disableJSONP,o=n.targets||[{path:"",index:"..\\..\\searchindex.js"}],i,t;if(r||f||e){for(console.log("Starting to update target configurations..."),i=0;i<o.length;i++)if(t=path.dirname(o[i].index),path.existsSync(t)){console.log("Updating target: "+t);var l=path.join(t,"searchServer.xml"),u="            ",a=f?util.format("%s<serverUrl>%s</serverUrl>\r\n",u,f):"",c=r?util.format("%s<serverPort>%s</serverPort>\r\n",u,r):"",s=e?util.format("%s<disableJSONP>true</disableJSONP>\r\n",u):"",h='<?xml version="1.0" encoding="utf-8"?>\r\n<settings>\r\n    <search>\r\n        <options>\r\n            <serverSide>true</serverSide>\r\n%s%s%s        </options>\r\n    </search>\r\n</settings>';fs.writeFileSync(l,util.format(h,a,c,s))}else console.log("Target not found: "+t);console.log("Updating target configurations is completed.")}else console.log("Server doesn't have settings to write to targets.")}var path=require("path"),fs=require("fs"),util=require("util"),settingsFile=process.argv[2]||"./settings.json",serverSettings;path.existsSync(settingsFile)?(serverSettings=require(settingsFile),updateTargetSettings(serverSettings)):console.log("The file with settings cannot be found: "+settingsFile)