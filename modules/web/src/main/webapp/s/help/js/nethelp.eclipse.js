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

(function(n,t,i,r){i.defaultSettings({topic:{topicLinkPattern:"\\.x?html?((\\?|#).*)?$"}}),i.ready(function(){n('meta[name="NetHelpPlugin"][content="sandcastle"]').length?(i.cancelBody(),i.setting(!0,"topic.applyStylesheet",!1),n("head link.from-settings").remove(),n("body").attr("class","content-topic abs fill-h fill-v scroll").attr("id","topicBlock").css("height","100%")):i.writeBody(),i.driver("popup").create(),i.driver("theme").create(),i.plugin("collapsibleSection").create(),i.plugin("inlineText").create(),i.plugin("relatedTopics").create(),i.plugin("popupText").create(),i.plugin("topicLinks").create(),i.plugin("k-link").create(),i.plugin("a-link").create(),i.trigger("topicupdate");var t=i.topicLink;i.topicLink=function(n,i){if(n&&typeof n=="string"){i=i||{};var u=i.target,e=i.element,f=i.event;if(f&&f.isDefaultPrevented()||!i.href||u==="popup")return u!=="popup"&&(i.target=r),t.apply(this,arguments);e&&u&&!/^_(blank|self|parent|top)$/.test(u)&&e.removeAttr("target")}}})})(jQuery,nethelp,nethelpshell)