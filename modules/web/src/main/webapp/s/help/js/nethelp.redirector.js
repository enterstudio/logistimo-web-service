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

(function(){var f;if(!/\Wnhr=false(\W|$)/i.test(location.href)&&!window.top.nethelp||/\Wnhr=true(\W|$)/i.test(location.href)){var n="../",e=document,u=location,t,o=u.hash,r=u.search||"",s=e.getElementsByTagName("script"),h=s[s.length-1],c=/(.*)nethelp\.redirector\.js$/i.exec(h.src)[1],i=e.createElement("a");i.href=".",f=i.href==="."?function(n){return i.href=n,i.getAttribute("href",4)}:function(n){return i.setAttribute("href",n),i.href},t=f("#").replace(/(\?.*|#)$/,""),n=h.getAttribute("data-target-path")||f((c||"./")+n),window.nethelpRedirect=function(i,e){var s=/(([^?#]+\/)*[^\/?#]*)(\?[^#]*)?(?:#.*)?$/.exec(f(n+(i||"default.htm"))),h=e?e:"#!";s&&(i=s[1],n=s[2]||"",t=t.indexOf(n)===0?t.substring(n.length):t,s[3]&&s[3].length>1&&(r=s[3]+(r.length>1?"&"+r.substring(1):"")),i=i+(r.length>1?r:"")+h+t+(o.length>1?o:""),/\Wnhr=debug(\W|$)/i.test(u.href)?window.console?console.log(i):alert(i):u.replace(i))},e.write('<script type="text/javascript" src="'+n+'nethelppage.js"><\/script>')}})()