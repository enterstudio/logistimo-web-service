<%@page import="com.logistimo.auth.SecurityConstants" %>
<%@page import="com.logistimo.auth.SecurityMgr" %>
<%@page import="com.logistimo.auth.utils.SessionMgr" %>
<%@page import="com.logistimo.config.models.DomainConfig" %>
<%@page import="com.logistimo.constants.Constants"%>
<%@page import="com.logistimo.pagination.PageParams" %>
<%@page import="com.logistimo.security.SecureUserDetails"%>
<%@page import="com.logistimo.services.Services"%>
<%@page import="com.logistimo.users.entity.IUserAccount" %>
<%@page import="com.logistimo.users.service.UsersService" %>
<%@page import="com.logistimo.users.service.impl.UsersServiceImpl" %>
<%@page import="com.logistimo.utils.LocalDateUtil" %>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ page import="java.util.Locale" %>
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

<fmt:bundle basename="Messages">
<%
	// Get the desired subview
	String subview = request.getParameter( "subview" );
	if ( subview == null || subview.isEmpty() )
			subview = "dashboard";
	
	final int OFFSET_MONTHS = 5; // This constant is used for getting the usage statistics until now.
	// The active months in the last 6 months are displayed. 
	String startDateStr = request.getParameter( "startdate" ); // startdate is present when the month selection is changed in the dashboard.
	boolean hasStartDate = ( startDateStr != null && !startDateStr.isEmpty() );
	Date defaultDate = null;
	if ( !hasStartDate ) {
		// Get current month by default
		Calendar cal = GregorianCalendar.getInstance();
		LocalDateUtil.resetTimeFields( cal ); // Reset time fields
		cal.set( Calendar.DATE, 01 ); // Set the date to first of the month selected.
		defaultDate = cal.getTime();
		startDateStr = LocalDateUtil.formatCustom( defaultDate, Constants.DATE_FORMAT, null ); // Format the date to dd/MM/yyyy format
		hasStartDate = true;
	} else {
		defaultDate = LocalDateUtil.parseCustom( startDateStr, Constants.DATE_FORMAT, null );
	}
	
	String url = "/s/dashboard?action=getmonthlyusagestatsacrossdomains&startdate=" + startDateStr;
	// Get the logged in user
	SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
	String userId = sUser.getUsername();
	// Get the user's locale
	Locale locale = sUser.getLocale();
	String timezone = sUser.getTimezone();
	String role = sUser.getRole();
	boolean isManager = ( SecurityConstants.ROLE_SERVICEMANAGER.equals( role ) );
	// Get domain Id
	String domainIdStr = request.getParameter( "domainid" );
	Long domainId = null;
	if ( domainIdStr == null || domainIdStr.isEmpty() ) {
		domainId = SessionMgr.getCurrentDomain( request.getSession(), userId );
	} else {
		domainId = Long.parseLong( domainIdStr ); // TODO: Exception handling
	}
	DomainConfig dc = DomainConfig.getInstance( domainId );
	String currency = dc.getCurrency();
	if ( currency != null && currency.isEmpty() )
		currency = null;
	
	UsersService as;
	as = Services.getService(UsersServiceImpl.class,locale);
	IUserAccount user = as.getUserAccount( userId );
	
%>
<jsp:include page="../JSMessages.jsp" flush="true" />
<!--  Scripts for Google Data Visualization -->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>	
<!-- JQuery UI css and scripts -->
<script src="../../js/jquery-1.11.0.min.js" type="text/javascript" charset="utf-8"></script>

<link type="text/css" href="../../jqueryui/css/redmond/jquery-ui-1.10.4.custom.min.css" rel="stylesheet" />
<script type="text/javascript" src="../../jqueryui/js/jquery-ui-1.10.4.custom.min.js"></script>

<!--  Special style required to make the datepicker select only month and year -->
<style type="text/css">
	#filters input {border:0;}
	.hideDates .ui-datepicker-calendar {
	    display: none;
	}
	.showDates .ui-datepicker-calendar {
	    display: block;
	}
	.hide-calendar .ui-datepicker-calendar {
	    display: none;
	}
	
	.right-text {
    text-align: right;
  	}
</style>

<!--  Style for the charts in the chart view -->
<!-- <style>
 .axis {
   font: 10px sans-serif;
 }

 .axis path,
 .axis line {
   fill: none;
   stroke: #000;
   shape-rendering: crispEdges;
 }
</style> -->
<script src="/js/d3.min.js"></script>
<!-- <script type="text/javascript" src="/js/dashboard-metrics.js"></script>-->
<!-- <script type="text/javascript" src="/js/saveSvgAsPng.js"></script>-->
<script type="text/javascript">
	var months = ['January','February','March','April','May','June','July','August','September','October','November','December'];
	var cancel = false;
	
	var monthlyUsageStatsFromServer = []; // Not used - Contains the data sent from the server for date >= specified date in datepicker
	var untilNowUsageStatsFromServer = []; // Contains the data sent from the server for a date >= 3 months before today.
	var globalMonthlyUsageStats = {}; // The global usage stats for the selected month. Format: {"tc":<transaction count>, "ctc": <cumulative transaction count>, "oc":<order count>, "coc": <cumulative order count>, "lnc": <login count>, "clnc": <cumulative login count", "tuc": <transactive user count>, "ec": <entity count>, "aec": <active entity count>, "mc": <material count>, "t": <time stamp>}
	var perDomainMonthlyUsageStats = {}; //per domain monthly stats. Format: {"dId1": {"dId":<domainid>, "dName":<domain name>,"tc":<transaction count>, "ctc": <cumulative transaction count>, "oc":<order count>, "coc": <cumulative order count>, "lnc": <login count>, "clnc": <cumulative login count">, "tuc": <transactive user count>, "ec": <entity count>, "aec": <active entity count>, "mc": <material count>, "t": <time stamp>}, ...}
	var sortedPerDomainMonthlyUsageStats = []; // perDomainMonthlyUsageStats sorted based on descending order of transaction counts.
	var globalUntilNowUsageStats = {}; // The global usage stats until now. Format: {"tc":<transaction count>, "ctc": <cumulative transaction count>, "oc":<order count>, "coc": <cumulative order count>, "lnc": <login count>, "clnc": <cumulative login count>, "tuc": <transactive user count>, "ec": <entity count>, "aec": <active entity count>, "mc": <material count>, "t": <time stamp>}
	var perDomainUntilNowUsageStats = {};//per domain until now stats. Format: {"dId1": {"dId":<domainid>, "dName":<domain name>,"tc":<transaction count>, "ctc": <cumulative transaction count>, "oc":<order count>, "coc": <cumulative order count>, "lnc": <login count>, "tuc": <transactive user count>, "ec": <entity count>, "aec": <active entity count>, "mc": <material count>, "t": <time stamp>}, ...}
	var sortedPerDomainUntilNowUsageStats = []; // perDomainUntilNowUsageStats sorted based on the descending order of cumulative transaction counts.
	
	//Load the Visualization API and the ready-made Google table visualization.
	google.load('visualization', '1', {packages: ['table']}); 
	// Set a callback to run when the API is loaded - this will run the summary count table visualization.
	// google.setOnLoadCallback( drawMonthlyUsageStatsDashboard );
	google.setOnLoadCallback( drawMonthlyUsageDashboards );
	
	// Function that draws the monthly usage dashboards (in the monthly tab)
	function drawMonthlyUsageDashboards() {
		// If the data is alerady fetched once, don't fetch it again.
		initDashboardTabs();
		initGlobalMonthlyUsageStats();
		// initGlobalUntilNowUsageStats();
		initDateFilter();
		setMonthlyDashboardTitle();
		
		var globalMonthlyUsageStatsPanel = document.getElementById( 'globalmonthlyusagestatspanel' );
		globalMonthlyUsageStatsPanel.innerHTML = '<img src="/images/loader.gif" />';
		var perdomainMonthlyUsageStatsPanel = document.getElementById( 'perdomainmonthlyusagestatspanel' );
		perdomainMonthlyUsageStatsPanel.innerHTML = '<img src="/images/loader.gif" />';
		fetchData('<%=url%>', <%=defaultDate.getTime()%>, <%= PageParams.DEFAULT_SIZE %>, 0 , null );
		
	}
	
	function setMonthlyDashboardTitle() {
		var iMonth = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
        var iYear = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
		// var d = $( '#ui-datepicker-div' ).datepicker( "getDate" );
		var monthlyTitleStr = months[iMonth] + ", " + iYear;
		var monthlyTitleDiv = document.getElementById( "monthlytitle" );
		monthlyTitleDiv.innerHTML = '<b>' + monthlyTitleStr + '</b>';
	}
	
	// This function initialized the global variables globalMonthlyUsageStats and globalUntilNowUsageStats
	function initGlobalMonthlyUsageStats() {
		// Init globalMonthlyUsageStats
		globalMonthlyUsageStats.uc = 0;
		globalMonthlyUsageStats.lnc = 0;
		globalMonthlyUsageStats.clnc = 0;
		globalMonthlyUsageStats.ec = 0;
		globalMonthlyUsageStats.tc = 0;
		globalMonthlyUsageStats.ctc = 0;
		globalMonthlyUsageStats.oc = 0;
		globalMonthlyUsageStats.coc = 0;
		globalMonthlyUsageStats.brv = 0;
		globalMonthlyUsageStats.mc = 0;
		globalMonthlyUsageStats.t = 0;
	}
	
	function initGlobalUntilNowUsageStats() {
		// Init globalUntilNowUsageStats
		globalUntilNowUsageStats.uc = 0;
		globalUntilNowUsageStats.lnc = 0;
		globalUntilNowUsageStats.clnc = 0;
		globalUntilNowUsageStats.ec = 0;
		globalUntilNowUsageStats.tc = 0;
		globalUntilNowUsageStats.ctc = 0;
		globalUntilNowUsageStats.oc = 0;
		globalUntilNowUsageStats.coc = 0;
		globalUntilNowUsageStats.cbrv = 0;
		globalUntilNowUsageStats.mc = 0;
		globalUntilNowUsageStats.t = 0;
	}
	
	// This function fetches the monthly usage statistics from the server. It makes an AJAX call, gets the result from the server in a paginated manner and stores it in monthUsageStatsFromServer.
	// The server returns data for any date >= selected date in date picker. This function picks the results for the selected month and stores it in perDomainMonthlyUsageStats and updates the 
	// globalMonthlyUsageStats object. As the data is being fetched the view (google table) is refreshed. 
	function fetchData( incomingUrl, defaultTimeInMillis, size, doneSoFar ) {
		// var url = '<%=url%>&size=' + size;
		var url = incomingUrl + '&size=' + size;	
		if ( doneSoFar )
			url += '&offset=' + doneSoFar;
		// Show loading info.
		var message = '';
		if ( doneSoFar > 0 )
			message = '<b>' + Object.keys( perDomainMonthlyUsageStats ).length + '</b> fetched. '; // doneSoFar
		///message += JSMessages.fetching + ' ' + size + ( doneSoFar > 0 ? ' ' + JSMessages.more + '...' : ' ' + JSMessages.items + '...' );
		message += JSMessages.fetching + ( doneSoFar > 0 ? ' ' + JSMessages.more : '' ) + '...';
		message += ' &nbsp;&nbsp;<a onclick="cancelFetching()">' + JSMessages.cancel + '</a>';
		showLoadingInfo( 'monthlyinfo', true, message );
		
		$.ajax({
			url: url,
			dataType: 'json',
			success: function( o ) {
				// Get the results into statsData.
				var statsData = o.domainUsageStatsList;
				// If statsData is > 0, then update doneSoFar.
				// Push the statsData into monthlyUsageStatsFromServer
				if ( statsData && statsData.length > 0 ) {
					doneSoFar += statsData.length;
					// Iterate through statsData and push each into monthlyUsageStatsFromServer. This is the master list. Although not used, it is retained in case it might be needed later.
					for ( var i = 0; i < statsData.length; i++ ) {
						var element = statsData[i];
						monthlyUsageStatsFromServer.push( element );
						// Add this to the perDomainMonthlyUsageStats if the month matches with the selected/default month in the date picker.
						var selectedDate = new Date( defaultTimeInMillis );
						var selectedMonth = selectedDate.getMonth(); // Is not zero based
						var time = new Date( element.t );
						var month = time.getMonth();
						
						if ( selectedMonth == month ) {
							updatePerDomainMonthlyUsageStats( element );
							updateGlobalMonthlyUsageStats( element );
						}
					}
					// Sort the perDomainMonthlyUsageStats object based on descending order of transaction counts
					sortPerDomainMonthlyUsageStats();
					
					// Now draw the table showing the per domain monthly usage statistics, using the perDomainMonthlyUsageStats
					drawPerDomainMonthlyUsageDashboard();
					 
					// Also draw the global monthly usage dashboard to show data across all domains for the selected month.
					drawGlobalMonthlyUsageDashboard();
					
					// Now recursively call fecthData again, but this time with a cursor returned from the backend.
					// If the operation is not cancelled by the user or if the data returned is the same size as PageParams.DEFAULT_SIZE, then
					// proceed to recursively fetchData.
					if ( statsData.length == <%= PageParams.DEFAULT_SIZE %> ) {	
						if ( !cancel )
							fetchData( incomingUrl, defaultTimeInMillis, size, doneSoFar );
					}
				}
				// If statsData is undefined or statsData size is < default size or if cancel button is clicked.
				if ( !statsData || statsData && statsData.length < <%= PageParams.DEFAULT_SIZE %> || cancel ) {
					// Stop fetching from backend. Render the stats
					showLoadingInfo( 'monthlyinfo', false, Object.keys( perDomainMonthlyUsageStats ).length + " active domain(s)" );
					// If there are no active domains, then a message should be displayed.
					if ( sortedPerDomainMonthlyUsageStats.length == 0 ) {
						showNoMonthlyDataAvailableMsg();
					} 
				}
			},
			error: function( o ) {
				console.log( 'ERROR: ' + o.msg ); 
				var message = '';
        		if ( Object.keys( perDomainMonthlyUsageStats ).length > 0 )
        			message += '<b>' + Object.keys( perDomainMonthlyUsageStats ).length + '</b> fetched.';
            	message += ' A system error occured while fetching monthly usage statistics. Please contact your system administrator';
    			showLoadingInfo( 'monthlyinfo', false, message );
			}
		});
	}
	
	// Function that updates the perDomainUsageStats object.
	function updatePerDomainMonthlyUsageStats( data ) {
		if ( data ) {
			perDomainMonthlyUsageStats[ data.dId ] = data;
		}
	}
	// Function that updates the counts in the globalMonthlyUsageStats object
	function updateGlobalMonthlyUsageStats( data ) {
		if ( data ) {
			globalMonthlyUsageStats.uc += data.uc;
 			globalMonthlyUsageStats.lnc +=  data.lnc;
 			globalMonthlyUsageStats.ec += data.ec;
 			globalMonthlyUsageStats.tc += data.tc;
 			globalMonthlyUsageStats.oc += data.oc;
 			globalMonthlyUsageStats.brv += data.brv;
 			globalMonthlyUsageStats.mc += data.mc;
 			globalMonthlyUsageStats.t = data.t;
		}
	}
	
	// Sort the perDomainMonthlyUsageStats by transaction counts and store it in global variable sortedPerDomainUsageStats
	// so that the table is displayed in the descending order of transaction counts.
	function sortPerDomainMonthlyUsageStats() {
		sortedPerDomainMonthlyUsageStats = []; // Initialize this global variable every time. Otherwise, there will be repititions form pervious iterations.
		for ( var dId in perDomainMonthlyUsageStats )
			sortedPerDomainMonthlyUsageStats.push( perDomainMonthlyUsageStats[dId] );
		sortedPerDomainMonthlyUsageStats.sort( compareTC );
	}
	
	// Helper function to sort the perDomainMonthlyUsageStats in descending order of transaction counts tc
 	function compareTC(a, b ) {
 		if (a.tc < b.tc)
 		     return 1;
 		  if (a.tc > b.tc)
 		    return -1;
 		  return 0;
 	} 
	
	// Function that draws the per domain monthly usage dashboard using sortedPerDomainMonthlyUsageStats object. The keys in the object become the rows for the table.
	function drawPerDomainMonthlyUsageDashboard() {
		// Display the table
		var data = new google.visualization.DataTable();
 		data.addColumn( 'string', JSMessages.domain );
 		data.addColumn( 'number', JSMessages.transactions );
 		data.addColumn( 'number', JSMessages.orders );
 		data.addColumn( 'number', JSMessages.logins );
 		data.addColumn( 'number', JSMessages.revenue );
 		data.addColumn( 'string', JSMessages.currency );
 		// Now add rows to the table from sortedPerDomainMonthlyUsageStats
 		for ( var i = 0; i < sortedPerDomainMonthlyUsageStats.length; i++ ) {
 			var usageStats = sortedPerDomainMonthlyUsageStats[i];
 			// Get the selected start date from the date filter
 			// var domainName = '<a href=\'#\' onclick=\'onClickDomain(' +  JSON.stringify( usageStats )  + ')\'>' + usageStats.dName + '</a>';
 			var domainName = usageStats.dName; // Removed onclick because it was giving a javascript error
 			// Round off the revenue in the 5th column to two places after decimal, using NumbeFormat.
 			var formatter = new google.visualization.NumberFormat();
	 		data.addRow( [ domainName, usageStats.tc, usageStats.oc, usageStats.lnc, usageStats.brv, usageStats.cur ] );
	 		formatter.format( data, 4 ); // The 5th column is revenue. But the column index is zero based.
	 		
 		}
 		var table = new google.visualization.Table( document.getElementById( 'perdomainmonthlyusagestatspanel' ) );
        table.draw (data, {showRowNumber: true, allowHtml: true} );
	}
	
	function hideFilterPanel() {
		var filterPanelDiv = document.getElementById( 'filterpanel' );
		filterPanelDiv.style.display ='none';
	}
	
	// Function that draws the global usage dashboard using the globalMonthlyUsageStats object. 
	function drawGlobalMonthlyUsageDashboard() {
 		var globalMonthlyUsageStatsDiv = document.getElementById( 'globalmonthlyusagestatspanel' );
 		//globalMonthlyUsageStatsDiv.style.display = 'block';
 		var str = getGlobalMonthlyUsageStatsHTML();
 		globalMonthlyUsageStatsDiv.innerHTML = str;
 	}
	
	function initDashboardTabs() {
		$(function() {
			// Tab initialization
		    $('#dashboardtabs').tabs({
		        activate: function(event, ui){
		        	cancel = false; // Reset the global variable cancel.
		            var selectedTab = ui.newTab.index();
		            if ( selectedTab == 1 ) {
		            	// load the until now dashboard statistics view
		            	showUntilNowUsageTab();
		            }
		            
		        }
		    });
		  });
	}
	
	// Init. the startDate datepicker
	function initDateFilter() {
		var startDate = $( "#startdate" );
		var today = new Date();
		if ( startDate ) {
			startDate.datepicker( {
										dateFormat: 'MM yy',
										changeMonth: true,
										changeYear: true,
										showButtonPanel: true,
										maxDate: today,
										beforeShow: function( input, inst ) {
											$('#ui-datepicker-div')[ $(input).is('[data-calendar="false"]') ? 'addClass' : 'removeClass' ]('hide-calendar');
											
											// If there is a date already set, then ensure that is shown by default in the picker menu
											if ((selDate = $(this).val()).length > 0) {
												iYear = selDate.substring(selDate.length - 4, selDate.length);
												iMonth = jQuery.inArray(selDate.substring(0, selDate.length - 5),
									                   $(this).datepicker('option', 'monthNames'));
									          	$(this).datepicker('option', 'defaultDate', new Date(iYear, iMonth, 1));
									          	$(this).datepicker('setDate', new Date(iYear, iMonth, 1));
									       }
									    },
									    onClose: function( input, inst ) {
											// Set the date picker's default date to the just selected date
									        var iMonth = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
									        var iYear = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
								          	$(this).datepicker('option', 'defaultDate', new Date(iYear, iMonth, 1));
									        $(this).datepicker('setDate', new Date(iYear, iMonth, 1));
									        // url = "/s/admin/domains.jsp?subview=dashboard&startdate=" + getFormattedDate( 'startdate', 0 );
									        
									        //refresh the dashboard.jsp with the new startdate
									        // url = "/s/admin/domains.jsp?subview=dashboard&startdate=" + getFormattedDate( 'startdate', 0 );
									        // location.href=url;
									        var url = '/s/dashboard?action=getmonthlyusagestatsacrossdomains&startdate=' + getFormattedDate( 'startdate', 0 );
									        reloadMonthlyUsageTab( url );
									     },
									 } );
			// If the startdate is set, set the datepicker to show it.
			<% if( hasStartDate ) { %>
				var date = $.datepicker.parseDate( 'dd/mm/yy', '<%= startDateStr %>' );
				startDate.datepicker( 'option', 'defaultDate', date );
				startDate.datepicker( 'setDate', date );
			<% } %>
		} 
	}
	
	// Get the datepicker's selction in dd/MM/yyyy format
	function getFormattedDate( dateId, offsetMonths ) {
		var d = $( '#' + dateId ).datepicker( "getDate" );
		d.setMonth( d.getMonth() + offsetMonths );
		var date = d.getDate() + '';
		if ( date.length == 1 )
			date = '0' + date;
		var month = d.getMonth() + 1 + '';
		if ( month.length == 1 )
			month = '0' + month;
		return date + '/' + month + '/' + d.getFullYear();
	}
	
	// Show/hide loader. Since there are two loaders one in each tab, this function takes the infoId to show or hide the appropriate loader.
    function showLoadingInfo( infoId, show, message ) {
        var info = document.getElementById( infoId );
        var loader = '<img src="../../images/loader.gif" />';
        if ( show )
            info.innerHTML = loader;
        else
            info.innerHTML = '';
        info.innerHTML += ( message ? ' ' + message : '' );
    }
	
 	// Cancel fetching
	function cancelFetching() {
		cancel = true;
	}
 	
 	// Get the HTML for displaying the global monthly usage stats
 	function getGlobalMonthlyUsageStatsHTML() {
 		var html = '';
		var tableStyle = 'width:100%;';
		var cellFontStyle = 'font-size:12pt;font-weight:bold;';
		var defaultFontColor = 'color:#00BFFF';
		var revenueCellFontColor = 'color:#006400';
		
		html += '<table style="' + tableStyle + '"><tr>';
		html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + globalMonthlyUsageStats.tc + '</font><br/><font style="font-size:8pt">' + JSMessages.transactions + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + globalMonthlyUsageStats.oc + '</font><br/><font style="font-size:8pt">' + JSMessages.orders + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + globalMonthlyUsageStats.lnc + '</font><br/><font style="font-size:8pt">' + JSMessages.logins + '</font></td>';
		// Since currency across domains are different, we do not show the global revenue.
		// html += '<td style="align:center"><font style="' + cellFontStyle + revenueCellFontColor + '">' + roundDecimal( globalMonthlyUsageStats.brv ) + '</font><br/><font style="font-size:8pt">' + JSMessages.revenue + '</font></td>';
		html += '</tr></table>';
 		
 		return html;
 	}
 	
 	function roundDecimal( number ) {
 		return parseFloat( Math.round( number * 100 ) / 100 ).toFixed(2);
 	}
 	
 	// Function that exports the usage statistics as a CSV.
 	function exportUsageStats() {
 		var fromDate = getFormattedDate( 'startdate', 0 );
 		fromDate += ' 00:00:00'; // From date format as required by the ExportServlet
 		// var todate = date selected in date picker + 1 month
 		var toDate = getFormattedDate( 'startdate', 1 );
 		toDate += ' 00:00:00'; // Form the to date format as required by the ExportServlet.
 		var url = '/s/export?action=sbe&type=usagestatistics&from='+ fromDate + '&to=' + toDate + '&sourceuserid=<%= userId %>&userids=<%= userId %>&domainid=<%=domainId%>'; 
 		console.log( 'Export url: ' + url );
 		
 		var d = $( '#startdate' ).datepicker( "getDate" );
		var month = months[d.getMonth()];
		var year = d.getFullYear();
    	var msg = 'You have chosen to export usage statistics for ' + month + ', ' + year + '. Data will be emailed to <%= user.getEmail() %>. Continue?';
    	if ( !confirm( msg ) )
    		return false;
    	// Add the view and subview to url
    	url += '&view=<%= Constants.VIEW_DOMAINS %>&subview=dashboard';
    	location.href = url;
 	}
 	
 	// This function is called when the Until Now tab is clicked. It displays the dashboard that displays usage statistics till date.
 	function showUntilNowUsageTab() {
 		// Only if not initialized before, initialize the globalUntilNowUsageStats object. And fetch data from server.
 		// Otherwise, just display the cached data
 		if ( Object.keys( globalUntilNowUsageStats ).length == 0 ) {
 			initGlobalUntilNowUsageStats();
 			// Show loading indicatior inside the panels before fetching data.
 			var globalUntilNowUsageStatsPanel = document.getElementById( 'globaluntilnowusagestatspanel' );
 			globalUntilNowUsageStatsPanel.innerHTML = '<img src="/images/loader.gif" />';
 		
 			var perdomainUntilNowUsageStatsPanel = document.getElementById( 'perdomainuntilnowusagestatspanel' );
 			perdomainUntilNowUsageStatsPanel.innerHTML = '<img src="/images/loader.gif" />';
 			// Now fetch data.
 			fetchDataUntilNow( <%= PageParams.DEFAULT_SIZE %>, 0 , null );
 		} else {
 			drawPerDomainUntilNowUsageDashboard();
			// Also draw the globalUntilNowUsageDashboard object to contain data across all domains until now.
			drawGlobalUntilNowUsageDashboard();
 		}
 	}
 	
 	function reloadMonthlyUsageTab( url ) {
 		initDashboardTabs();
		initGlobalMonthlyUsageStats();
		perDomainMonthlyUsageStats = [];
		sortedPerDomainMonthlyUsageStats = [];
		
		setMonthlyDashboardTitle();
		// fetch data across domains
		var globalMonthlyUsageStatsPanel = document.getElementById( 'globalmonthlyusagestatspanel' );
		globalMonthlyUsageStatsPanel.innerHTML = '<img src="/images/loader.gif" />';
	
		var perdomainMonthlyUsageStatsPanel = document.getElementById( 'perdomainmonthlyusagestatspanel' );
		perdomainMonthlyUsageStatsPanel.innerHTML = '<img src="/images/loader.gif" />';
		// Send the startDate in milliseconds to fetchData
		var d = $( '#startdate' ).datepicker( "getDate" );
		fetchData(url, d.getTime(), <%= PageParams.DEFAULT_SIZE %>, 0 , null );
		
 	}
 	
 	// Function that fetches all active domains in the last 4 months. It displays the latest statistics for the active domains. 
 	function fetchDataUntilNow( size, doneSoFar ) {
 		// Set the start date to be OFFSET_MONTHS months older than today.
 		var currentDate = new Date();
 		var currentMonth = currentDate.getMonth();
 		currentDate.setMonth( currentMonth - <%=OFFSET_MONTHS%> );
 		var startDateStr = currentDate.getDate() + '/' + currentDate.getMonth() + '/' + currentDate.getFullYear();
 		var url = '/s/dashboard?action=getmonthlyusagestatsacrossdomains&startdate=' + startDateStr + '&size=' + size;
		if ( doneSoFar )
			url += '&offset=' + doneSoFar;
		// Show loading info.
		var message = '';
		if ( doneSoFar > 0 )
			message = '<b>' + Object.keys( perDomainUntilNowUsageStats ).length + '</b> fetched. ';
		///message += JSMessages.fetching + ' ' + size + ( doneSoFar > 0 ? ' ' + JSMessages.more + '...' : ' ' + JSMessages.items + '...' );
		message += JSMessages.fetching + ( doneSoFar > 0 ? ' ' + JSMessages.more : '' ) + '...';
		message += ' &nbsp;&nbsp;<a onclick="cancelFetching()">' + JSMessages.cancel + '</a>';
		showLoadingInfo( 'untilnowinfo', true, message );
		
		$.ajax({
			url: url,
			dataType: 'json',
			success: function( o ) {
				// Get the results into statsData.
				var statsData = o.domainUsageStatsList;
				// If statsData is > 0, then update doneSoFar.
				// Push the statsData into untilNowUsageStatsFromServer
				if ( statsData && statsData.length > 0 ) {
					doneSoFar += statsData.length;
					// Iterate through statsData and push each into untilNowUsageStatsFromServer. This is the master list
					for ( var i = 0; i < statsData.length; i++ ) {
						untilNowUsageStatsFromServer.push( statsData[i] );
					}
					// Sort the untilNowUsageStatsFromServer in the desc order of timestamp
					untilNowUsageStatsFromServer.sort( compare );
					// Now form perDomainUntilNowUsageStats
					updatePerDomainUntilNowUsageStats();
					// Sort the perDomainUntilNowUsageStats based on cumulative transaction counts
					sortPerDomainUntilNowUsageStats();
					// Now draw the table showing the per domain monthly usage statistics, using the perDomainMonthlyUsageStatsList
					drawPerDomainUntilNowUsageDashboard();
					// Also draw the globalUntilNowUsageDashboard object to contain data across all domains until now.
					drawGlobalUntilNowUsageDashboard();
					
					// Now recursively call fetchDataUntilNow again, but this time with a cursor returned from the backend.
					// If the operation is not cancelled by the user or if the data returned is the same size as PageParams.DEFAULT_SIZE, then
					// proceed to recursively call fetchDataUntilNow.
					if ( statsData.length == <%= PageParams.DEFAULT_SIZE %> ) {	
						if ( !cancel )
							fetchDataUntilNow( size, doneSoFar );
					}
				}
				// If statsData is undefined or if the statsData.length < the default size
				if ( !statsData || statsData && statsData.length < <%= PageParams.DEFAULT_SIZE %> || cancel ) {
						showLoadingInfo( 'untilnowinfo', false, Object.keys( perDomainUntilNowUsageStats ).length + " active domain(s)" );
						// If there are no active domains, then a message should be displayed.
						if ( sortedPerDomainUntilNowUsageStats.length == 0 ) {
							showNoUntilNowDataAvailableMsg();
						} 
				}
			},
			error: function( o ) {
				console.log( 'ERROR: ' + o.msg );
				var message = '';
        		if ( Object.keys( perDomainUntilNowUsageStats ).length > 0 )
        			message += '<b>' + Object.keys( perDomainUntilNowUsageStats ).length + '</b> fetched.';
            	message += ' A system error occured while fetching usage statistics. Please contact your system administrator';
    			showLoadingInfo( 'untilnowinfo', false, message );
			}
		});
 	}
 	
 	// Sorts in descending order of timestamp
 	function compare(a, b ) {
 		if (a.t < b.t)
 		     return 1;
 		  if (a.t > b.t)
 		    return -1;
 		  return 0;
 	} 
 	
 	// Function that is a subset of untilNowUsageStatsfromServer. This has one entry per domain and that entry is the latest statistics for that domain.
 	function updatePerDomainUntilNowUsageStats() {
 		// The untilNowUsageStatsfromServer is already sorted.
 		// Iterate through untilNowUsageStatsfromServer
 		for ( var i = 0; i < untilNowUsageStatsFromServer.length; i++ ) {
 			var element = untilNowUsageStatsFromServer[i];
 			// Add it to perDomainUntilNowUsageStats only if not already present.
 			if ( !perDomainUntilNowUsageStats[element.dId] ) {
 				perDomainUntilNowUsageStats[element.dId] = element;
 				// Update the globalUntilNowUsageStats object
 				updateGlobalUntilNowUsageStats( element );
 			}
 		}
 	}
 	
 	// Sort the perDomainUntilNowUsageStats by cumulative transaction counts and store it in global variable sortedPerDomainUntilNowUsageStats
	// so that the table is displayed in the descending order of cumulative transaction counts.
	function sortPerDomainUntilNowUsageStats() {
		sortedPerDomainUntilNowUsageStats = []; // Initialize this global variable every time. Otherwise, there will be repititions fro the pervious iterations.
		// First, push the objects from perDomainUntilNowUsageStats into sortedPerDomainUntilNowUsageStats
		for ( var dId in perDomainUntilNowUsageStats )
			sortedPerDomainUntilNowUsageStats.push( perDomainUntilNowUsageStats[dId] );
		// Now sort it based on descending order of cumulative transaction counts
		sortedPerDomainUntilNowUsageStats.sort( compareCTC );
	}

 	// Helper function to sort the perDomainUntilNowUsageStats in descending order of cumulative transaction counts ctc
	function compareCTC(a, b ) {
		if (a.ctc < b.ctc)
		     return 1;
		  if (a.ctc > b.ctc)
		    return -1;
		  return 0;
	} 
 	
 	// Draw the Google Visualization table to show per domain usage statistics until now (cumulative). Only active domains (in the last 4 months) are displayed in the table. 
 	function drawPerDomainUntilNowUsageDashboard() {
 		var data = new google.visualization.DataTable();
 		data.addColumn( 'string', JSMessages.domain );
 		data.addColumn( 'number', JSMessages.transactions );
 		data.addColumn( 'number', JSMessages.orders );
 		data.addColumn( 'number', JSMessages.logins );
 		data.addColumn( 'number', JSMessages.revenue );
 		data.addColumn( 'string', JSMessages.currency );
 		data.addColumn( 'number', JSMessages.users );
 		data.addColumn( 'number', JSMessages.kiosks );
 		data.addColumn( 'number', JSMessages.materials );
 		// Now add rows to the table from sortedPerDomainUntilNowUsageStats. For every entry in the perDomainUntilNowUsageStats, a row is created.
 		for ( var i = 0; i < sortedPerDomainUntilNowUsageStats.length; i++ ) {
 			var usageStats = sortedPerDomainUntilNowUsageStats[i];
 			var formatter = new google.visualization.NumberFormat();
 			data.addRow( [ usageStats.dName, usageStats.ctc, usageStats.coc, usageStats.clnc, usageStats.cbrv , usageStats.cur, usageStats.uc, usageStats.ec, usageStats.mc ] );
 			formatter.format( data, 4 ); // The 5th column is revenue. But the column index is zero based.
 		}
 		
 		var table = new google.visualization.Table(document.getElementById('perdomainuntilnowusagestatspanel'));
        table.draw(data, {showRowNumber: true});
 	}
 	
 	// Draw the global until now usage dashboard.
 	function drawGlobalUntilNowUsageDashboard() {
 		var globalUntilNowUsageStatsDiv = document.getElementById( 'globaluntilnowusagestatspanel' );
 		var str = getGlobalUntilNowUsageStatsHTML();
 		globalUntilNowUsageStatsDiv.innerHTML = str;
 	}
 	
 	// Get the HTML for global usage stats
 	function getGlobalUntilNowUsageStatsHTML() {
 		var html = '';
		var tableStyle = 'width:100%;';
		var cellFontStyle = 'font-size:12pt;font-weight:bold;';
		var defaultCellFontColor = 'color:#00BFFF';
		var revenueCellFontColor = 'color:#006400';
		var usersCellFontColor = 'color:#830300';
		
		html += '<table style="' + tableStyle + '"><tr>';
		html += '<td style="align:center"><font style="' + cellFontStyle + defaultCellFontColor + '">' + globalUntilNowUsageStats.ctc + '</font><br/><font style="font-size:8pt">' + JSMessages.transactions + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + defaultCellFontColor + '">' + globalUntilNowUsageStats.coc + '</font><br/><font style="font-size:8pt">' + JSMessages.orders + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + defaultCellFontColor + '">' + globalUntilNowUsageStats.clnc + '</font><br/><font style="font-size:8pt">' + JSMessages.logins + '</font></td>';
		// html += '<td style="align:center"><font style="' + cellFontStyle + revenueCellFontColor + '">' + roundDecimal( globalUntilNowUsageStats.cbrv ) + '</font><br/><font style="font-size:8pt">' + JSMessages.revenue + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + usersCellFontColor + '">' + globalUntilNowUsageStats.uc + '</font><br/><font style="font-size:8pt">' + JSMessages.users + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + usersCellFontColor + '">' + globalUntilNowUsageStats.ec + '</font><br/><font style="font-size:8pt">' + JSMessages.kiosks + '</font></td>';
		html += '<td style="align:center"><font style="' + cellFontStyle + usersCellFontColor + '">' + globalUntilNowUsageStats.mc + '</font><br/><font style="font-size:8pt">' + JSMessages.materials + '</font></td>';
		html += '</tr></table>';
 		
 		return html;
 	}
 	
 	// Update the counts in the globalUntilNowUsageStats object.
 	function updateGlobalUntilNowUsageStats( data ) {
 		if ( data ) {
			globalUntilNowUsageStats.uc += data.uc;
			globalUntilNowUsageStats.lnc +=  data.lnc;
			globalUntilNowUsageStats.clnc += data.clnc;
			globalUntilNowUsageStats.ec += data.ec;
			globalUntilNowUsageStats.tc += data.tc;
			globalUntilNowUsageStats.ctc += data.ctc;
			globalUntilNowUsageStats.coc += data.coc;
			globalUntilNowUsageStats.oc += data.oc;
			globalUntilNowUsageStats.cbrv += data.cbrv;
			globalUntilNowUsageStats.mc += data.mc;
			globalUntilNowUsageStats.t = data.t;
		}
 	}
 	
 	function showNoMonthlyDataAvailableMsg() {
		var perDomainMonthlyStatsPanelDiv = document.getElementById( 'perdomainmonthlyusagestatspanel' );
		perDomainMonthlyStatsPanelDiv.innerHTML = 'No active domains';
		
		var globalMonthlyStatsUsagePanelDiv = document.getElementById( 'globalmonthlyusagestatspanel' );
		globalMonthlyStatsUsagePanelDiv.innerHTML = 'No data  available';
 	}
 	
 	function showNoUntilNowDataAvailableMsg() {
		var perDomainUntilNowStatsPanelDiv = document.getElementById( 'perdomainuntilnowusagestatspanel' );
		perDomainUntilNowStatsPanelDiv.innerHTML = 'No active domains';
		
		var globalUntilNowStatsUsagePanelDiv = document.getElementById( 'globaluntilnowusagestatspanel' );
		globalUntilNowStatsUsagePanelDiv.innerHTML = 'No data  available';
 	}
 	
 	 function onClickDomainOld( usageStats ) {
 		var selectedDate = getFormattedDate( 'startdate', 0 ); // Date selected in the datepicker
 		var offsetDate = getFormattedDate( 'startdate', -5 ); // 5 months offset because charts display stats six months from the selected date backwards.
 		showChartView( 'monthlychartviewpanel', <%=PageParams.DEFAULT_SIZE%>, usageStats.dId, usageStats.dName, selectedDate, offsetDate );
 		hideMonthlyTabularView();
 	}
 	 
 	/*function onClickDomain( usageStats ) {
 		console.log( 'Domain name is clicked. Showing domaindashboard.jsp' );
 		$( '#monthlychartviewpanel' ).show();
 		initDomainDashboard(); // This function is inside domaindashboard.jsp
 		hideMonthlyTabularView(); // When charts are displayed, hide the table.
 	}*/
 	
 	function hideMonthlyTabularView() {
 		// Hide the tabular view
 		var monthlyTabularViewPanel = document.getElementById( 'monthlytabularviewpanel' );
 		monthlyTabularViewPanel.style.display = 'none';
 	}
 	
 	function showMonthlyTabularView() {
 	// Hide the tabular view
 		var monthlyTabularViewPanel = document.getElementById( 'monthlytabularviewpanel' );
 		monthlyTabularViewPanel.style.display = 'block';
 	}
 	
 	
</script>
<!--  Have two tabs display monthly and cumulative statistics -->
<div id="dashboardtabs" style="margin-top:2%;"><!-- Removed  class="sgForm" so that the buttons in the domaindashboard.jsp appear with correct size. -->
	<ul>
		<li><a href="#dashboardmonthly">Monthly</a></li>
	</ul>
	<div id="dashboardmonthly" >
		<div id="monthlytabularviewpanel">
			<i>Note: Only domains active (have at least one login) in the given month are shown in this view.</i>
			<div id="filterpanel" class="sgForm" style="margin-top:10px">
				<b style="font-size:10pt"><fmt:message key="month.upper"/>:</b> 
				<input type="text" id="startdate" data-calendar="false" readonly />
				<button type="button" style="float:right" id="exportusagestats" onclick="exportUsageStats()"><fmt:message key="export"/></button>
			</div>
			<br/>
			<!--  Title that shows the selected month -->
			<div id="monthlytitle"><b></b></div>
			<!--  Activity across all domains -->
			<div id="globalmonthlyusagestatspanel" class="ui-widget ui-widget-content ui-corner-all" style="padding:4px;margin-top:10px;margin-bottom:15px;box-shadow: 0 1px 4px rgba(0,0,0,0.5);-moz-box-shadow: 0 4px 8px rgba(0,0,0,0.5);-webkit-box-shadow: 0 4px 8px rgba(0,0,0,0.5);">
			</div>
			<div id="monthlyinfo" class="sgForm" style="float:left;"></div>
			<br/>
			<!--  Per domain usage statistics for all active domains -->
			<div id="perdomainmonthlyusagestatspanel" class="ui-widget ui-widget-content ui-corner-all" style="padding:4px;margin-top:10px;margin-bottom:15px;box-shadow: 0 1px 4px rgba(0,0,0,0.5);-moz-box-shadow: 0 4px 8px rgba(0,0,0,0.5);-webkit-box-shadow: 0 4px 8px rgba(0,0,0,0.5);">
			</div>
		</div>
	</div>
	 
</div>
</fmt:bundle>
