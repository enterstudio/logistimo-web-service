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

function start(n,t,i){function r(i,r){var u="";i.setEncoding("utf8"),i.addListener("data",function(n){u+=n}),i.addListener("end",function(){n(t,i,r,u)})}http.createServer(r).on("error",function(n){n.code=="EADDRINUSE"&&console.log("Port number "+i.port+" is already in use. Change port number in the settings file or release the port. ")}).on("listening",function(){console.log("Server has started.")}).listen(i.port,i.host)}var http=require("http"),url=require("url");exports.start=start