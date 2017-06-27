<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@page import="com.logistimo.auth.SecurityConstants" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.SecurityUtil" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.config.models.BBoardConfig" %>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.constants.Constants"%>
<%@page import="com.logistimo.domains.entity.IDomain"%>
<%@page import="com.logistimo.domains.service.DomainsService" %>
<%@page import="com.logistimo.domains.service.impl.DomainsServiceImpl" %>
<%@page import="com.logistimo.pagination.PageParams" %>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.services.Services" %>
<%@page import="com.logistimo.services.utils.ConfigUtil" %>
<%@page import="com.logistimo.users.entity.IUserAccount" %>
<%@page import="com.logistimo.users.service.UsersService" %>
<%@page import="com.logistimo.users.service.impl.UsersServiceImpl" %>
<%@page import="com.logistimo.utils.LocalDateUtil"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ page import="java.util.Locale" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<!-- Get the locale-specific JS messages -->
<%
    if(ConfigUtil.getBoolean("force.newui", false)){
        response.sendRedirect( Constants.NEWUI_HOME_URL );
        return;
    }

	final int OFFSET_MONTHS = 5;
	// Initialize the session with default admin user (only the first time)
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	Long domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	// Get the user's locale
	Locale locale = sUser.getLocale();
	String localeStr = locale.toString();
	if ( localeStr.isEmpty() )
		localeStr = "en";
	// Check role
	boolean isSuperUser = ( SecurityConstants.ROLE_SUPERUSER.equals( sUser.getRole() ) );
	boolean isManager = ( SecurityConstants.ROLE_SERVICEMANAGER.equals( sUser.getRole() ) );
	// Check if dev/prod server
	////boolean isDev = ( com.google.appengine.api.utils.SystemProperty.environment.value() == com.google.appengine.api.utils.SystemProperty.Environment.Value.Development );
	// Get the domain config.
	DomainConfig dc = DomainConfig.getInstance( domainId );

	// Get the bulletin-board config
	BBoardConfig bbc = dc.getBBoardConfig();
	// Allow posting to pboard?
	boolean allowPost = ( bbc != null && bbc.isEnabled() && SecurityUtil
			.compareRoles(sUser.getRole(), SecurityConstants.ROLE_DOMAINOWNER) >= 0 );
	boolean isOrdersEnabled = !dc.isCapabilityDisabled( DomainConfig.CAPABILITY_ORDERS );
	String currency = dc.getCurrency();
	if ( currency != null && currency.isEmpty() )
		currency = null;
	String timeZone = dc.getTimezone();
	String domainName = null;
	String errMsg = null;
	IDomain d = null;
	try {
		// Set the domain name
		DomainsService as = Services.getService( DomainsServiceImpl.class );
		UsersService us = Services.getService( UsersServiceImpl.class );
			d = as.getDomain( domainId );
			if ( d != null )
				domainName = d.getName();
            IUserAccount ua = us.getUserAccount( userId );
            // If the domain is configured with ui preference as true, then redirect to new UI
            if ( dc.getUiPreference() ) {
                response.sendRedirect( Constants.NEWUI_HOME_URL );
                return;
            } else {
                // Domain has no UI preference or old UI preference. But user has new UIp preference. Redirect to new UI
                if (ua.getUiPref()) {
                    response.sendRedirect(Constants.NEWUI_HOME_URL);
                    return;
                }
            }
		} catch ( Exception e ) {
			errMsg = e.getMessage();
		}

    //Redirecting to temperature dashboard for temperature only domain.
    if(dc != null && dc.isTemperatureMonitoringEnabled()){
        //response.reset();
        response.sendRedirect("/s/temperature/temperature.jsp");
    }

	Date selectedDate;
	Calendar today = GregorianCalendar.getInstance();
	today.set( Calendar.DAY_OF_MONTH, 1 );
	LocalDateUtil.resetTimeFields( today );
	selectedDate = today.getTime();
	String selectedDateStr = LocalDateUtil.formatCustom( selectedDate, Constants.DATE_FORMAT, timeZone );
	Date offsetDate = LocalDateUtil.getOffsetDate( selectedDate, -1 * OFFSET_MONTHS, Calendar.MONTH );
	String offsetDateStr = LocalDateUtil.formatCustom( offsetDate, Constants.DATE_FORMAT, timeZone );
%>
<jsp:include page="JSMessages.jsp" flush="true" />

<!-- Set the user's locale -->
<fmt:setLocale value="<%= localeStr %>" scope="session"/>
<fmt:bundle basename="Messages">
<jsp:include page="pageheader.jsp" />
	<jsp:include page="menu.jsp">
		<jsp:param name="view" value="home" />
	</jsp:include>
    <script src="../js/jquery-1.11.0.min.js" type="text/javascript" charset="utf-8"></script>
<!-- JQuery Tokeninput -->
<link rel="stylesheet" href="/tokeninput/css/token-input-facebook.css" type="text/css" media="screen" />
<script src="/tokeninput/js/jquery.tokeninput.js" type="text/javascript" charset="utf-8"></script>
<% if ( isSuperUser ) { %>
<!-- Textlistbox scripts -->
<!-- <link rel="stylesheet" href="../autocomplete/TextboxList.css" type="text/css" media="screen" charset="utf-8" />
<link rel="stylesheet" href="../autocomplete/TextboxList.Autocomplete.css" type="text/css" media="screen" charset="utf-8" />
<script src="../autocomplete/GrowingInput.js" type="text/javascript" charset="utf-8"></script>
<script src="../autocomplete/TextboxList.js" type="text/javascript" charset="utf-8"></script>
<script src="../autocomplete/TextboxList.Autocomplete.js" type="text/javascript" charset="utf-8"></script>
<script src="../autocomplete/TextboxList.Autocomplete.Binary.js" type="text/javascript" charset="utf-8"></script> -->
<!-- <style type="text/css">
	.sgForm input {border:0;}
</style>
 -->
<% } %>
<!--
<script src="/js/d3.min.js"></script>
<script type="text/javascript" src="/js/dashboard-metrics.js"></script>
 -->
<!--  Style for the charts in the chart view -->
<style>
 .axis {
   font: 10px sans-serif;
 }

 .axis path,
 .axis line {
   fill: none;
   stroke: #000;
   shape-rendering: crispEdges;
 }
</style>
<!-- JQuery UI css and scripts -->
<link type="text/css" href="../jqueryui/css/redmond/jquery-ui-1.8.22.custom.css" rel="stylesheet" />
<script type="text/javascript" src="../jqueryui/js/jquery-ui-1.8.22.custom.min.js"></script>

<script type="text/javascript">
var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

function initBulletinBoardPost() {
	// Initialize the bboard panel, if needed
	$( '#bboardpanel' ).attr( 'style', 'display:block' );
	$( "#bboardpanel" ).dialog({
        autoOpen: false,
        height: 250,
        width: 400,
        modal: false,
        position: [ 'top','right' ],
        buttons: {
            	Save: function() {
	            	var msg = $('#message').val();
	            	msg = msg.trim();
	            	if ( msg == '' ) {
		            	alert( 'Please enter a valid message' );
		            	return;
		            }
            		document.getElementById('bbloader').style.display='block';
		            var postData = {};
		            postData.action = 'post';
		            postData.message = msg;
	            	$.ajax( {
		            	type: 'POST',
		            	url : '/s/bboardmgr',
		            	data: postData,
		            	dataType: 'json',
		            	success: function(o) {
			            	console.log( o );
			            		if ( o.st && o.st == '1' ) {
			            			$('#message').val( '' );
			            			document.getElementById('bbloader').innerHTML = '<b>Message successfully posted!</b>'; 
			            		} else {
				            		var err = 'An error occurred';
				            		if ( o.ms )
					            		err += ': ' + o.ms;
				            		document.getElementById('bbloader').innerHTML = '<font style="color:red;font-weight:bold;">' + err + '</font>';
			            		}
			            	},
			            errors: function() {
				            	alert( o.responseText );
								$( '#bboardpanel' ).dialog( 'close' );
		            		}
	            		});
	            	},
				Cancel: function() { resetMessageForm(); $( this ).dialog( 'close' ); }
            }
		});
}

function resetMessageForm() {
	document.getElementById('bbloader').innerHTML = '<img src="../../images/loader.gif" />';
	document.getElementById('bbloader').style.display = 'none';
	document.forms['messageform'].reset();
}

// Get monthly stats.
function getMonthlyStats() {
	// Loading indicator
	var activityPanel = document.getElementById( 'activitypanel' );
	activityPanel.innerHTML = '<img src="../images/loader.gif" />';
	var url = '/s/dashboard?action=getmonthlystats&months=2';
	$.ajax( {
			url: url,
			dataType: 'json',
			success: function( o ) {
					console.log( o );
					var str = JSMessages.nodataavailable;
					if ( o && o.length > 0 )
						str = getMonthlyStatsHTML( o );
					activityPanel.innerHTML = str;
				},
			error: function( o ) {
					activityPanel.innerHTML = 'Unable to retrieve data.'
				}
		});
}

// Get the HTML for monthly stats
function getMonthlyStatsHTML( data ) {
	console.log( data );
	var html = '';
	for ( var i = 0; i < data.length; i++ ) {
		var tableStyle = 'width:100%;';
		var cellFontStyle = 'font-size:12pt;font-weight:bold;';
		if ( i == 1 ) // previous month
			tableStyle += 'color:#A4A4A4;';
		else
			cellFontStyle += 'color:#00BFFF';
		if ( i == 1 ) // insert divider
			html += '<div style="border-top:0px solid #8C8C8C;margin:10px"></div>';
		html += '<table style="' + tableStyle + '"><tr>';
		var date = new Date(data[i].t);
		// Advance this date by a day, so it stumbles into the first day of the month irrespective of the timezone
		date.setDate( date.getDate() + 1 );
		html += '<td style="width:120px"><font style="font-weight:bold;">' + months[ date.getMonth() ] + ' ' + date.getFullYear() + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + '">' + data[i].lns + '</font><br/><font style="font-size:8pt">' + JSMessages.logins + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + '">' + data[i].tc + '</font><br/><font style="font-size:8pt">' + JSMessages.transactions + '</font></td>';
		<% if ( isOrdersEnabled ) { %>
		html += '<td style="align:center"><font style="' + cellFontStyle + '">' + data[i].oc + '</font><br/><font style="font-size:8pt">' + JSMessages.orders + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + '">' + roundDecimal( data[i].rvn ) + '</font><br/><font style="font-size:8pt">' + JSMessages.revenue + '<%= currency != null ? " (" + currency  + ")" : "" %></font></td>';
		<% } %>
		html += '</tr></table>';
	}
	return html;
}

function roundDecimal( number ) {
	return parseFloat( Math.round( number * 100 ) / 100 ).toFixed(2);
}

function initDropDowns() {
	initDomainDropDown( 'domainid', <%= d !=null ? "'" + d.getId() + "'" : "undefined" %>, <%= d != null ? "'" + d.getName() + "'" : "undefined" %> );
}
//Init. the domain drop down
function initDomainDropDown( id, domainId, domainName ) {
	var url = '/s/list.jsp?type=domains';
	var domainDropDown = $( '#' + id ).tokenInput( url, {
															theme: 'facebook',
															tokenLimit: 1,
															onAdd: function( item ) {
																if ( !domainId || ( item.id != domainId ) )
																	location.href = '/s/createentity?action=switch&type=domain&view=domains&userid=' + '<%=userId%>' + '&domainid=' + item.id;
															}
													    } );
}

function initFunction() {
	<% if ( isSuperUser ) { %>
	initDropDowns();
	<% } %>
	<% if ( allowPost ) { %>
	initBulletinBoardPost();
	<% } %>
	///getMonthlyStats();
	// Show the charts only if errMsg is null
	<% if ( errMsg == null ) { %>
		// showCharts
		///showChartView( 'monthlychartviewpanel', <%=PageParams.DEFAULT_SIZE%>, <%=domainId%>, "<%=domainName%>", '<%=selectedDateStr%>', '<%=offsetDateStr%>', true );
	<% } %>
	<% if ( !isManager ) { %>
	initDomainDashboard();
	<% } %>
}

window.onload = initFunction;
</script>
<div id="doc3">
<h1 class="sgHeading"><fmt:message key="login.welcome" />!</h1>

<!-- 
<div class="ui-corner-all" style="margin-top:10px;font-size:8pt;border:1px solid #8C8C8C;background-color:#E1F5FB;padding:5px">
The dashboard provides you with a quick view of recent performance. The links on the right 
offer shortcuts and downloads for easy access. Setup Logistimo in a way that works for you.
</div>
 -->
<div style="margin-top:15px">
	<!-- Dashboard -->
    <div style="float:left;width:80%;font-size:9pt">
    	<% if ( !isManager ) { %>
    	<jsp:include page="domaindashboard.jsp">
    		<jsp:param name="domainid" value="<%=domainId%>" />
    	</jsp:include>
    	<% } %>
	    <!-- Events -->
	    <!-- 
	   	<font style="font-size:8pt;font-weight:normal">Watch out for these outstanding events.</font><br/>
	    <div class="ui-corner-all" style="padding:6px;margin-top:5px;margin-bottom:15px;background-color:#FFF700;box-shadow: 0 1px 4px rgba(0,0,0,0.5);-moz-box-shadow: 0 4px 8px rgba(0,0,0,0.5);-webkit-box-shadow: 0 4px 8px rgba(0,0,0,0.5);">
	        <table style="width:100%;margin-top:5px;">
	        	<tr>
	        		<td style="align:center"><font style="font-size:12pt;font-weight:bold;color:#FF8356">50</font><br/><font style="font-size:8pt">stock outs</font></td>
	        		<td style="align:center"><font style="font-size:12pt;font-weight:bold;color:#FF8356">234</font><br/><font style="font-size:8pt">&lt; Min.</font></td>
	        		<td style="align:center"><font style="font-size:12pt;font-weight:bold;color:#FF8356">1200</font><br/><font style="font-size:8pt">&gt; Max.</font></td>
	        		<td style="align:center"><font style="font-size:12pt;font-weight:bold;color:#FF8356">1200</font><br/><font style="font-size:8pt">wasted</font></td>
	        		<td style="align:center"><font style="font-size:12pt;font-weight:bold;color:#FF8356">85</font><br/><font style="font-size:8pt">users &gt; credit limit</font></td>
	        		<td style="align:center"><font style="font-size:12pt;font-weight:bold;color:#FF8356">45</font><br/><font style="font-size:8pt">orders expired</font></td>
	        	</tr>
	        </table>
	    </div>
	     -->
	    <!-- Object-specific counts -->
	    <!-- <font style="font-weight:bold;font-size:11pt;">What's happening?</font><br/>
	    <font style="font-size:8pt">Activity of users and transactions in recent months.</font> -->
	    <!-- Overview -->
	    <!-- <div id="activitypanel" class="ui-widget ui-widget-content ui-corner-all" style="padding:4px;margin-top:10px;margin-bottom:15px;box-shadow: 0 1px 4px rgba(0,0,0,0.5);-moz-box-shadow: 0 4px 8px rgba(0,0,0,0.5);-webkit-box-shadow: 0 4px 8px rgba(0,0,0,0.5);">
	    </div>
	    -->
	    <% if ( isManager ) { %>
	    <div style="margin-top:20px;">
	    	<img src="../images/front-pic.jpg" width="100%"/>
	    </div>
	    <% } %>
	    <!-- Monthly charts view -->
	    <!-- <div id="monthlychartviewpanel" style="margin-top:20px;"></div>  -->
	    <div style="margin-top:20px;">
	    Questions? Email <a href="mailto:support@logistimo.com">support@logistimo.com</a>
	    </div>
	    <!--
	    <div class="ui-corner-all" style="border:1px solid #8C8C8C;padding:4px;margin-top:5px;margin-bottom:20px;background-color:#E1F8FF;;box-shadow: 0 1px 4px rgba(0,0,0,0.5);-moz-box-shadow: 0 4px 8px rgba(0,0,0,0.5);-webkit-box-shadow: 0 4px 8px rgba(0,0,0,0.5);">
	    	<div style="margin-top:5px">
	    		<table style="width:100%;">
		        	<tr>
		        		<td style="width:120px"><font style="font-weight:bold;"><img src="../images/icon/material.png"  style="width:32px;height:32px;" align="middle"/> Materials</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">50</font><br/><font style="font-size:8pt">updated</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">123</font><br/><font style="font-size:8pt">not updated</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">2</font><br/><font style="font-size:8pt">newly added</font></td>
		        	</tr>
	    			<tr><td colspan="4"><div style="border-top:1px solid #8C8C8C;margin-top:10px;margin-bottom:10px;"></div></td></tr>
		        	<tr>
		        		<td style="width:120px"><font style="font-weight:bold;"><img src="../images/icon/entity.png"  style="width:32px;height:32px;" align="middle"/> Entities</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">22</font><br/><font style="font-size:8pt">have transactions</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">34</font><br/><font style="font-size:8pt">have no transactions</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">4</font><br/><font style="font-size:8pt">newly added</font></td>
		        	</tr>
	    			<tr><td colspan="4"><div style="border-top:1px solid #8C8C8C;margin-top:10px;margin-bottom:10px;"></div><img src="../images/icon/user.png" style="width:32px;height:32px;" align="middle"/> <b>Users</b></td></tr>
		        	<tr>
		        		<td style="width:120px">&nbsp;&nbsp;&nbsp;Managers</td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">50</font><br/><font style="font-size:8pt">logged in</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">32</font><br/><font style="font-size:8pt">never logged in</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">34</font><br/><font style="font-size:8pt">entered data</font></td>
		        	</tr>
		        	<tr><td colspan="4"><br/></td></tr>
		        	<tr>
		        		<td style="width:120px">&nbsp;&nbsp;&nbsp;Operators</td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">50</font><br/><font style="font-size:8pt">logged in</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">32</font><br/><font style="font-size:8pt">never logged in</font></td>
		        		<td style="align:center"><font style="font-size:10pt;font-weight:bold;color:#31B404">34</font><br/><font style="font-size:8pt">entered data</font></td>
		        	</tr>
		        </table>
	    	</div>
	    	 -->
	    </div>
    </div>
    <!-- Side bars -->
    <div style="float:right;width:20%;font-size:9pt">
    	<font style="font-size:11pt;font-weight:bold">Shortcuts</font> <!-- class="ui-widget-content ui-corner-all"  -->
        <div id="shortcuts" style="margin-bottom:10px;background-color:white;">
	        <ul>
	        	<% if ( dc.isBBoardEnabled() ) { %>
	        	<li style="padding:4px;"><img src="../images/icon/bulletin_board.png" align="top"/> <a href="/pub/board?domainid=<%= domainId %>" target="_new">Bulletin Board</a>
	        	<% if ( allowPost ) { %>
	        	(<a onclick="resetMessageForm(); $('#bboardpanel').dialog('open'); return false;">post</a>)
	        	<% } %>  
	        	</li>
	        	<% } %>
	        	<li style="padding:4px;"><img src="../images/map16.png" align="top"/> <a href="orders/orders.jsp?maps=true&subview=demandboard" target="_new">Demand map</a></li>        	 
	        	<li style="padding:4px;"><img src="../images/map16.png" align="top"/> <a href="inventory/inventory.jsp?maps=true" target="_new">Inventory map</a></li>
	        	<li style="padding:4px;"><img src="../images/icon/list16.png" align="top"/> <a href="inventory/inventory.jsp?subview=Inventory Transactions" target="_new"><fmt:message key="inventory"/> <fmt:message key="transactions"/></a></li>        	 
	        </ul>
        </div>
        <font style="font-size:11pt;font-weight:bold">Mobile Apps.</font>
        <div id="downloads" style="margin-bottom:10px">
	        <ul>
	        	<li style="padding:4px;"><img src="../images/icon/featurephone.png" align="top"/> <a href="http://m.logistimo.net/en/logistimo.jad">Feature phone (Java)</a></li>
	        	<li style="padding:4px;"><img src="../images/icon/smartphone.png" align="top"/> <a href="https://play.google.com/store/apps/details?id=com.logistimo.android#?t=W251bGwsMSwxLDIxMiwiY29tLmxvZ2lzdGltby5hbmRyb2lkIl0." target="_new">Smart phone (Android)</a></li>        	 
	        	<li style="padding:4px;"><img src="../images/icon/html5.png" align="top"/> <a href="/m/index.html" target="_new">HTML5 Browser</a></li>        	 
	        </ul>
        </div>
        <font style="font-size:11pt;font-weight:bold">Help</font>
        <div id="userguide">
	        <ul>
	        	<li style="padding:4px;"><img src="../images/question.png" align="top"/> <a href="http://www.samaanguru.org/UserManual.pdf" target="_new"><fmt:message key="setup.instruction" /></a></li>
	        </ul>
        </div>
        <% if ( isSuperUser ) { %>
		<div class="sgForm" style="margin-top:20px">
			Switch domain: <input type="text" id="domainid" style="height:16px;font-size:8pt;" />
			<div id="loader" style="display:none"><img src="../images/loader.gif" /></div>
		</div>
		<% } %>
    </div>
    <br style='clear: both'/>
</div>
</div>
<!-- Bulletin board post form -->
<div id="bboardpanel" class="sgForm" style="text-align:left;display:none;">
	<form id="messageform">
		<p>Message to post (&lt; 200 characters):</p>
		<br/>
		<textarea id="message" rows="5" cols="50"></textarea>
		<br/>
		<i>(you can post either plain text or HTML text)</i>
	</form>
	<div id="bbloader" style="display:none"><img src="../../images/loader.gif" /></div>
</div>
<jsp:include page="pagefooter.html" />
</fmt:bundle>