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

(function(n){function l(n){for(u=0;u<n.length;u++){if(r=n[u],f='<link type="text/css" rel="stylesheet" ',typeof r=="object")for(i in r)f+=i+'="'+(i==="href"?c:"")+r[i]+(i==="href"&&s?"?r="+ +new Date:"")+'" ';else f+='href="'+c+r+(s?"?r="+ +new Date:"")+'" ';document.write(f+"/>")}}function y(n){for(u=0;u<n.length;u++){if(r=n[u],f='<script type="text/javascript"',typeof r=="object")for(i in r)f+=" "+i+'="'+(i==="src"?o:"")+r[i]+(i==="src"&&s?"?r="+ +new Date:"")+'"';else f+=' src="'+o+r+(s?"?r="+ +new Date:"")+'"';document.write(f+"><\/script>")}}var e={meta:[],css:["nethelp.css"],js:["jquery.js","jquery-ui.js","nethelp.js"],css2:[],js2:[]},t=document.getElementsByTagName("script");t=t[t.length-1];var o=t.getAttribute("data-js")||/^(.*\/)?[^\/]*$/.exec(t.getAttribute("src"))[1],h=/(^|\/)js\/$/i.test(o)?o.replace(/js\/$/i,""):o+"../",c=t.getAttribute("data-css")||h+"css/",k=t.getAttribute("data-themes")||h+"themes/",w=t.getAttribute("data-docs")||n,p=t.getAttribute("data-settings")||h+"settings.xml",d=t.getAttribute("data-placeholder")||'[data-c1-role="nethelp"], body',tt=t.getAttribute("data-start")||n,b=/true/i.test(t.getAttribute("data-topiconly")),it=/true/i.test(t.getAttribute("data-istopic")),v=/true/i.test(t.getAttribute("data-responsive")),nt=/true/i.test(t.getAttribute("data-server")),g=/true/i.test(t.getAttribute("data-manual")),s=/false/i.test(t.getAttribute("data-cache")),u,i,r,f,a;for(v&&(e.js=["jquery.js","nethelp.responsive.jqm.js","jquery.mobile.js","nethelp.js","nethelp.responsive.js"],e.css=["nethelp.responsive.css"],e.css2.push("jquerymobile/jquery.mobile.structure.css")),nt&&e.js.push("nethelp.server.js"),a=function(){l(e.css2),y(e.js2)},u=0;u<e.meta.length;u++){r=e.meta[u],f="<meta ";for(i in r)f+=i+'="'+r[i]+'" ';document.write(f+"/>")}l(e.css),y(e.js),window.nethelpOptions={paths:{js:o,css:c,themes:k,docs:w},autostart:!g,pureNetHelp:!v,settings:p,placeholder:d,start:tt,topiconly:b,istopic:it,disableCache:s,headCallback:a}})()