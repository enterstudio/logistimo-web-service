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

(function(n,t,i,r){i.mergeSettings({sandcastle:{importPlugins:"a-link k-link inlineText popupText".split(" "),frameTemplate:'<div class="c1-topic-frame"><iframe class="sandcastle" title="Sandcastle frame" style="border:0; width:100%; height:100%;" frameborder="0" /></div>'}}),i.plugin({name:"sandcastle",create:function(){var f=i.topic,u=f?f.element:n("body"),e=i.settings.sandcastle;i.bind("topicvalidate",function(o,s){var c=s.document,a=n.ui&&n.ui.position.c1flip,h,l;s.document.find('meta[name="NetHelpPlugin"][content="sandcastle"]')[0]?(o.originalEvent.originalEvent&&(o.originalEvent.originalEvent={}),o.preventDefault(),s.frame.remove(),s.frame.remove=n.noop,s.frame=!0,u.html(e.frameTemplate),h=u.find("iframe"),h.load(function(){var w=t.URL(s.src),v=h[0].contentWindow.document,p=n(v.head),u=n(v.body),y;c=n(v),p[0]||(p=n(v.documentElement).children("head")),y=p.find("link, script, style").eq(0),y[0]||(y=n(v.createElement("meta")).appendTo(p)),n('link[rel~="stylesheet"]').each(function(){var r=n(this),i;r.hasClass("for-topic")&&(i=v.createElement("link"),i.rel=this.rel||"Stylesheet",i.type=this.type||"text/css",y.before(i),i.href=t.expandUrl(this.href))}),f.trigger("load",o,n.extend(s,{title:c.title||s.title||"NetHelp",frame:h})),a&&(a.container=u),u.c1popupGlobals||(u.bind("mousedown keydown",function(i,r){r=r||{},t.popup.globalEvent.call(this,i),r.frame=!0,r.main||n("body").trigger(n.Event(i),r)}),u.c1popupGlobals=!0),u.css({position:"relative",width:"100%",height:"100%"}),u.find("#devlangsMenu").css("z-index",1),u.find("#mainSection").css("position","relative"),n.each(e.importPlugins||[],function(n,t){i.plugin(t).create(u)}),u.delegate("a, area, .topic-link, .external-link","click",function(t){var u=n(this);if(!u.is(".inline-text, .popup-text, .k-link, .a-link, .service")){var e=u.is("a, area"),o=u.data("target")||e&&u.attr("target"),r=u.data("ref"),f=!r&&u.attr("href");if((r||f||"").charAt(0)==="#"){r&&(u.attr("href",r),u.removeData("ref"));return}(r=r||f,(r||"").toLowerCase().indexOf("mailto:"))&&(e&&t.preventDefault(),r&&(r=u.closest(".aklinks-menu, .popup-page").length?r:w.resolve(r).toString(),i.topicLink(r,{event:t,element:u,external:u.hasClass("external-link"),target:o})))}}),u.find("area[data-ref]").each(function(){var t=n(this);t.attr("href",t.attr("data-ref"))}),s.afterLoad=!0,c.inTopic=!0,c.title=v.title,c.links=c.find("link"),f.html(c,o,s),l&&clearTimeout(l),l=setTimeout(function(){h.width(h.width()),l=setTimeout(function(){l=r,h.width("100%")},10)},100)}),h[0].src=s.src):a&&(a.container=i.topic.element)}),n("body").bind("mousedown keydown",function(t,i){i=i||{};var r=n("iframe.sandcastle",u)[0];r&&!i.frame&&(i.main=!0,n("body",r.contentWindow.document).trigger(n.Event(t),i))}),i.print=function(){i.trigger("beforeprint");var n=u.find("iframe.sandcastle")[0];(n&&n.contentWindow||window).print(),i.trigger("afterprint")}}})})(jQuery,nethelp,nethelpshell)