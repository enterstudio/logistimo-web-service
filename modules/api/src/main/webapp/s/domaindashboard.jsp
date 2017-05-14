<%@ page contentType="text/html; charset=UTF-8" language="java" %>
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

<%
	String domainId = request.getParameter( "domainid" );
%>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" type="text/css" href="/css/bootstrap.min.css">
<!-- fullCalendar -->
<link href="/fullcalendar/fullcalendar.css" rel="stylesheet" type="text/css" />
<!-- Theme style -->
<link href="/font-awesome-4.3.0/css/font-awesome.min.css" rel="stylesheet" type="text/css">
<style type="text/css">
	/*
	  		Component: Small boxes
	*/
	.small-box {
	  position: relative;
	  display: block;
	  -webkit-border-radius: 2px;
	  -moz-border-radius: 2px;
	  border-radius: 2px;
	  margin-bottom: 15px;
	}
	.small-box > .inner {
	  padding: 10px;
	}
	.num{
		font-size:150%;
	}
	.inum{
		margin-top: -5px;
		font-size:80%;
		float: right;
	}
	.small-box > .small-box-footer {
	  position: relative;
	  text-align: center;
	  padding: 3px 0;
	  color: #fff;
	  color: rgba(255, 255, 255, 0.8);
	  display: block;
	  z-index: 10;
	  background: rgba(0, 0, 0, 0.1);
	  text-decoration: none;
	}
	.small-box > .small-box-footer:hover {
	  color: #fff;
	  background: rgba(0, 0, 0, 0.15);
	}
	.small-box h3 {
	  font-size: 25px;
	  font-weight: bold;
	  margin: 0 0 10px 0;
	  /* white-space: nowrap;*/
	  padding: 0;
	}
	.small-box p {
	  font-size: 10px;
	}
	.small-box p > small {
	  display: block;
	  color: #f9f9f9;
	  font-size: 8px;
	  margin-top: 5px;
	}
	.small-box h3,
	.small-box p {
	  z-index: 5;
	}
	
	.small-box .icon {
	    box-sizing: border-box;
	    bottom: 5px;
	    color: rgba(0, 0, 0, 0.15);
	    font-size: 25px;
	    position: absolute;
	    right: 8px;
	    top: 5px;
	    z-index: 0;
	}
	.small-box a {
		font-size: 8px;
	}
	/* Background colors */
	.bg-red,
	.bg-yellow,
	.bg-aqua,
	.bg-blue,
	.bg-light-blue,
	.bg-green,
	.bg-navy,
	.bg-teal,
	.bg-olive,
	.bg-lime,
	.bg-orange,
	.bg-fuchsia,
	.bg-purple,
	.bg-maroon,
	.bg-black {
	  color: #f9f9f9 !important;
	}
	.bg-gray {
	  background-color: #eaeaec !important;
	}
	.bg-black {
	  background-color: #222222 !important;
	}
	.bg-red {
	  background-color: #f56954 !important;
	}
	.bg-yellow {
	  background-color: #f39c12 !important;
	}
	.bg-aqua {
	  background-color: #00c0ef !important;
	}
	.bg-blue {
	  background-color: #0073b7 !important;
	}
	.bg-light-blue {
	  background-color: #3c8dbc !important;
	}
	.bg-green {
	  background-color: #00a65a !important;
	}
	.bg-navy {
	  background-color: #001f3f !important;
	}
	.bg-teal {
	  background-color: #39cccc !important;
	}
	.bg-olive {
	  background-color: #3d9970 !important;
	}
	.bg-lime {
	  background-color: #01ff70 !important;
	}
	.bg-orange {
	  background-color: #ff851b !important;
	}
	.bg-fuchsia {
	  background-color: #f012be !important;
	}
	.bg-purple {
	  background-color: #932ab6 !important;
	}
	.bg-maroon {
	  background-color: #85144b !important;
	}
	/* Text colors */
	.text-red {
	  color: #f56954 !important;
	}
	.text-yellow {
	  color: #f39c12 !important;
	}
	.text-aqua {
	  color: #00c0ef !important;
	}
	.text-blue {
	  color: #0073b7 !important;
	}
	.text-light-blue {
	  color: #3c8dbc !important;
	}
	.text-green {
	  color: #00a65a !important;
	}
	.text-navy {
	  color: #001f3f !important;
	}
	.text-teal {
	  color: #39cccc !important;
	}
	.text-olive {
	  color: #3d9970 !important;
	}
	.text-lime {
	  color: #01ff70 !important;
	}
	.text-orange {
	  color: #ff851b !important;
	}
	.text-fuchsia {
	  color: #f012be !important;
	}
	.text-purple {
	  color: #932ab6 !important;
	}
	.text-maroon {
	  color: #85144b !important;
	}
	/*Hide elements by display none only*/
	.hide {
	  display: none !important;
	}
	/* Remove borders */
	.no-border {
	  border: 0px !important;
	}
	/* Remove padding */
	.no-padding {
	  padding: 0px !important;
	}
	/* Remove margins */
	.no-margin {
	  margin: 0px !important;
	}
	/* Remove box shadow */
	.no-shadow {
	  box-shadow: none!important;
	}
	.top-row {background-color: #F2F2F2;
			  margin: 10px;
				}
	.panel-heading {background-color: #F2F2F2!important}
	.panel {border-color: #F2F2F2!important}
	.btn:focus {
		  background-color: #D8D8D8;
		}
	.lgcontainer {
		padding: 0px 15px 0px 0px;
	}
	
</style>

<script src="/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/fusioncharts/fusioncharts.js"></script>
<script type="text/javascript" src="/fusioncharts/themes/fusioncharts.theme.fint.js"></script>
<script src="/js/moment.js"></script>

<div class="lgcontainer" >
    	<div class="row">
    		<div class="col-sm-12">
    			<div class="fc fc-ltr">
    			<table class="fc-header" style="width:100%;">
    				<tbody>
    					<tr>
    						<td class="fc-header-left">
    							<button class="fc-button fc-button-prev fc-state-default fc-corner-left" id="button-left-arrow" onclick="onClickArrow( <%= domainId %>, 'button-left-arrow','left');">
    								<span class="fc-icon fc-icon-left-single-arrow"></span>
    							</button>
    							<button class="fc-button fc-button-next fc-state-default fc-corner-right" id="button-right-arrow" onclick="onClickArrow( <%= domainId %>, 'button-right-arrow','right');">
    								<span class="fc-icon fc-icon-right-single-arrow"></span>
    							</button>
    						</td>
    						<td class="fc-header-center">
    							<span class="fc-header-title" id="fc-header-title">
    							</span>
    						</td>
    						<td class="fc-header-right">
    							<button id="day" class="fc-button fc-state-default fc-corner-left" value="1" onclick="onClickPeriodType( <%= domainId %>, 1 );">
    								Day
    							</button>
    							<!-- 
    							<button id="week" class="fc-button fc-state-default">
    								Week
    							</button>
    							 -->
    							<button id="month" class="fc-button fc-state-default fc-state-active fc-corner-right" value="3" onclick="onClickPeriodType( <%= domainId %>, 3 );">
    								Month
    							</button>
    							<!-- 
    							<button id="quarter" class="fc-button fc-state-default">
    								Quarter
    							</button>
    							 
    							<button id="year" class="fc-button fc-state-default fc-corner-right">
    								Year
    							</button>
    							-->
    						</td>
    					</tr>
    				</tbody>
    			</table>
    			</div>
    		</div>
    	</div>
    	<div class="row" id="mainloader" style="text-align:center;">
			<h2 class="text-center"><img src="/images/loader.gif"/> Loading... Please wait</h2>
    	</div>
    	<div class="row" id="panelwithoutdata" style="text-align:center;display:none">
    			<img src="/images/nodataavailable.jpeg"/>
    	</div>
    	
    	<div class="panel" id="activitypanel" style="display:none">
    		<div class="panel-default panel-heading"><b>Activity</b></div>	
    		<!-- <div class="panel-body" id="activitypanelwithoutdata" style="text-align:center;display:none">
    			<img src="/images/nodataavailable.jpeg"/>
    		</div>
    		<div class="panel-body" id="activitypanelloader"></div>
    		 -->
    		<div class="panel-body" id="activitypanelwithdata">
		    	<div class="row">
			    		<div class="col-sm-12">
		    				<div class="row">
		    					<div class="col-sm-5">
		    						<div class="row">
			    						<div class="col-sm-6">
			    							<div class="small-box bg-teal">
								    			<div class="inner" id="small-box-transactions">
						                        </div>
								    			
								    			<div class="icon c-teal">
								    				
					                            	<i class="fa fa-exchange"></i>
					                        	</div>
						                        
						                        <a href="#activitypanel" class="small-box-footer" onclick="onClickMoreInfo( 1, 'activitychart', 'Column2D', 'activityChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
					                        </div>
			    						</div>
			    						<div class="col-sm-6">
			    							<div class="small-box bg-green">
								    			<div class="icon c-green">
					                            	<i class="fa fa-users"></i>
					                        	</div>
						                        <div class="inner" id="small-box-users">
						                        </div>
						                        <a href="#activitypanel" class="small-box-footer" onclick="onClickMoreInfo( 2, 'activitychart', 'Column2D', 'activityChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
		    						</div>
		    						<div class="row">
			    						<div class="col-sm-6">
			    							<div class="small-box bg-red">
								    			<div class="icon c-red">
					                            	<i class="fa fa-home"></i>
					                        	</div>
						                        <div class="inner" id="small-box-entities">
						                        </div>
						                        <a href="#activitypanel" class="small-box-footer" onclick="onClickMoreInfo( 3, 'activitychart', 'Column2D', 'activityChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
			    						<div class="col-sm-6">
			    						<!-- 
											<div class="small-box bg-yellow">
								    			<div class="icon c-yellow">
					                            	<i class="fa fa-users"></i>
					                        	</div>
						                        <div class="inner" id="small-box-materials">
						                        </div>
						                        <a href="#activitypanel" class="small-box-footer" onclick="onClickMoreInfoActivity(4);">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>-->
			    						</div>
		    						</div>
		    					</div>
		    					<div id="activityChartContainer" class="col-sm-7">
		    					</div>
		    				</div>
			    		</div>
			    	
		    	</div>
    	</div>
    	</div>
    	<div class="panel" id="revenuepanel" style="display:none">
    		<div class="panel-heading"><b>Revenue</b></div>
    		<!-- <div class="panel-body" id="revenuepanelwithoutdata" style="text-align:center;display:none">
    			<img src="/images/nodataavailable.jpeg"/>
    		</div>
    		<div class="panel-body" id="revenuepanelloader"></div>
    		 -->
    		<div class="panel-body" id="revenuepanelwithdata">
		    	<div class="row">
			    		<div class="col-sm-12">
		    				<div class="row">
		    					<div class="col-sm-5">
		    						<div class="row">
			    						<div class="col-sm-6">
											<div class="small-box bg-green">
								    			<div class="icon c-green">
					                            	<i class="fa fa-money"></i>
					                        	</div>
						                        <div class="inner" id="small-box-revenue">
						                        </div>
						                        <a href="#revenuepanel" class="small-box-footer" onclick = "onClickMoreInfo( 5, 'revenuechart', 'Area2D', 'revenueChartContainer' );" >More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
			    						<div class="col-sm-6"><!-- 
											<div class="small-box bg-red">
								    			<div class="icon c-red">
					                            	<i class="fa fa-home"></i>
					                        	</div>
						                        <div class="inner" id="small-box-entitiesexceedingcreditlimit">
						                        </div>
						                        <a href="#revenuepanel" class="small-box-footer" onclick = "onClickMoreInfoRevenue(6);" >More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div> -->
			    						</div>
		    						</div>
		    						
		    					</div>
		    					<div id="revenueChartContainer" class="col-sm-7">
		    					</div>
		    				</div>
			    		</div>
			    	
		    	</div>
	    	</div>
    	</div>
    	<div class="panel" id="orderpanel" style="display:none">
    		<div class="panel-default panel-heading"><b>Orders</b></div>
    		<!-- <div class="panel-body" id="orderpanelwithoutdata" style="text-align:center;display:none">
    			<img src="/images/nodataavailable.jpeg"/>
    		</div>
    		<div class="panel-body" id="orderpanelloader"></div>
    		 -->	
    		<div class="panel-body" id="orderpanelwithdata">
		    	<div class="row">
			    		<div class="col-sm-12">
		    				<div class="row">
		    					<div class="col-sm-5">
		    						<div class="row">
			    						<div class="col-sm-6">
			    							<div class="small-box bg-teal">
								    			<div class="icon c-teal">
					                            	<i class="fa fa-list"></i>
					                        	</div>
						                        <div class="inner" id="small-box-orders">
						                        </div>
						                        <a href="#orderpanel" class="small-box-footer" onclick="onClickMoreInfo( 7, 'orderchart', 'Column2D', 'orderChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
					                        </div>
			    						</div>
			    						<div class="col-sm-6">
			    							<div class="small-box bg-green">
								    			<div class="icon c-green">
					                            	<i class="fa fa-list"></i>
					                        	</div>
						                        <div class="inner" id="small-box-fulfilledorders">
						                        </div>
						                        <a href="#orderpanel" class="small-box-footer" onclick="onClickMoreInfo( 8, 'orderchart', 'Column2D', 'orderChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
		    						</div>
		    						<div class="row">
			    						<div class="col-sm-6">
			    							<div class="small-box bg-red">
								    			<div class="icon c-red">
					                            	<i class="fa fa-list"></i>
					                        	</div>
						                        <div class="inner" id="small-box-pendingorders">
						                        </div>
						                        <a href="#orderpanel" class="small-box-footer" onclick="onClickMoreInfo( 9, 'orderchart', 'Column2D', 'orderChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
			    						<div class="col-sm-6">
											<div class="small-box bg-yellow">
								    			<div class="icon c-yellow">
					                            	<i class="fa fa-clock-o"></i>
					                        	</div>
						                        <div class="inner" id="small-box-orderresponsetime">
						                        </div>
						                        <a href="#orderpanel" class="small-box-footer" onclick="onClickMoreInfo( 10, 'orderchart', 'Column2D', 'orderChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
		    						</div>
		    					</div>
		    					<div id="orderChartContainer" class="col-sm-7">
		    					</div>
		    				</div>
			    		</div>
			    	
		    	</div>
    	</div>
    	</div>
    	<div class="panel" id="inventorypanel" style="display:none">
    		<div class="panel-default panel-heading"><b>Inventory</b></div>	
    		<!-- <div class="panel-body" id="inventorypanelwithoutdata" style="text-align:center;display:none">
    			<img src="/images/nodataavailable.jpeg"/>
    		</div>
    		<div class="panel-body" id="inventorypanelloader"></div>
    		 -->
    		<div class="panel-body" id="inventorypanelwithdata">
		    	<div class="row">
			    		<div class="col-sm-12">
		    				<div class="row">
		    					<div class="col-sm-5">
		    						<div class="row">
			    						<div class="col-sm-6">
			    							<div class="small-box bg-red">
								    			<div class="icon c-red">
					                            	<i class="fa fa-exclamation-circle"></i>
					                        	</div>
						                        <div class="inner" id="small-box-stockouts">
						                        </div>
						                        <a href="#inventorypanel" class="small-box-footer" onclick="onClickMoreInfo( 11, 'inventorychart', 'Column2D', 'inventoryChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
					                        </div>
			    						</div>
			    						<div class="col-sm-6">
			    							<div class="small-box bg-red">
								    			<div class="icon c-red">
					                            	<i class="fa fa-exclamation-circle"></i>
					                        	</div>
						                        <div class="inner" id="small-box-lessthanmin">
						                        </div>
						                        <a href="#inventorypanel" class="small-box-footer" onclick="onClickMoreInfo( 12, 'inventorychart', 'Column2D', 'inventoryChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
		    						</div>
		    						<div class="row">
			    						<div class="col-sm-6">
			    							<div class="small-box bg-red">
								    			<div class="icon c-red">
					                            	<i class="fa fa-exclamation-circle"></i>
					                        	</div>
						                        <div class="inner" id="small-box-greaterthanmax">
						                        </div>
						                        <a href="#inventorypanel" class="small-box-footer" onclick="onClickMoreInfo( 13, 'inventorychart', 'Column2D', 'inventoryChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
			    						<div class="col-sm-6">
											<div class="small-box bg-yellow">
								    			<div class="icon c-yellow">
					                            	<i class="fa fa-clock-o"></i>
					                        	</div>
						                        <div class="inner" id="small-box-replenishmentresponsiveness">
						                        </div>
						                        <a href="#inventorypanel" class="small-box-footer" onclick="onClickMoreInfo( 14, 'inventorychart', 'Column2D', 'inventoryChartContainer' );">More info
						                        	<i class="fa fa-arrow-circle-right"></i>
						                        </a>
						                        
					                        </div>
			    						</div>
		    						</div>
		    					</div>
		    					<div id="inventoryChartContainer" class="col-sm-7">
		    					</div>
		    				</div>
			    		</div>
			    	
		    	</div>
    	</div>
    	</div>
    </div>
    
    
 <script type="text/javascript">
    // var chart;
    var domainCountsMonthly = {};
    var domainCountsDaily = {};
    
   	 var today = moment({hour: 0, minute: 0, seconds: 0, milliseconds: 000});
   	 var offsetFromUtc = today.utcOffset() * 60000;
   	 console.log( 'offsetFromUtc: ' + offsetFromUtc );
   	 
   //	var today = moment.utc( {hour: 0, minute: 0, seconds: 0, milliseconds: 000} );
   	
    var eDateForMonth = today.clone();
    eDateForMonth.add(1, 'months'); // eDateForMonth = 1st day of next month
    eDateForMonth.set('date', 1); // Set the date to first of next month
    var eDateForDay = eDateForMonth.clone(); // eDateForDay = 1st day of next month
    // If today is not end of the month, then eDateForDay should be today.
    var endOfThisMonth = getEndOfMonth( today.clone() );
    if ( today != endOfThisMonth )
    	eDateForDay = today.clone();
   
    var METRIC_TYPE_TRANSACTIONS = 1;
    var METRIC_TYPE_ACTIVEUSERS = 2;
    var METRIC_TYPE_ACTIVEENTITIES = 3;
    // var METRIC_TYPE_ACTIVEMATERIALS = 4;
    var METRIC_TYPE_REVENUEBOOKED = 5;
   // var METRIC_TYPE_ENTITIESEXCEEDINGCREDITLIMIT = 6;
    var METRIC_TYPE_ORDERS = 7;
    var METRIC_TYPE_FULFILLEDORDERS = 8;
    var METRIC_TYPE_PENDINGORDERS = 9;
    var METRIC_TYPE_ORDERRESPONSETIME = 10;
    var METRIC_TYPE_STOCKOUTS = 11;
    var METRIC_TYPE_LESSTHANMIN = 12;
    var METRIC_TYPE_GREATERTHANMAX = 13;
    var METRIC_TYPE_REPLENISHMENTRESPONSIVENESS = 14;
    
    var DAILY = 1;
    var WEEKLY = 2;
    var MONTHLY = 3;
    var QUARTERLY = 4;
    var YEARLY = 5;
    
    var TRANSACTIONS = 'Transactions';
    var ACTIVE_USERS = 'Active Users';
    var ACTIVE_ENTITIES = 'Active Entities';
    // var ACTIVE_MATERIALS = 'Active Materials';
    var REVENUE = 'Revenue';
    // var ENTITIES_EXCEEDING_CREDIT_LIMIT = "Entities > credit limit";
    var ORDERS = 'Orders';
    var FULFILLED_ORDERS = 'Fulfilled Orders';
    var PENDING_ORDERS = 'Pending Orders';
    var ORDER_RESPONSE_TIME = 'Order Response Time';
    var STOCKOUTS = "Stock Out Events";
    var LESS_THAN_MIN = "< Min Events";
    var GREATER_THAN_MAX = "> Max Events";
    var REPLENISHMENT_RESPONSIVENESS = "Replenishment Time";
    
    var ACTIVITY_PANEL = 0;
    var REVENUE_PANEL = 1;
    var ORDER_PANEL = 2;
    var INVENTORY_PANEL = 3; 
    
    var currentActivityMetric = METRIC_TYPE_TRANSACTIONS; // The metric that is currently displayd in the activityChartContainer
    var currentRevenueMetric = METRIC_TYPE_REVENUEBOOKED; // The metric that is currently displayd in the revenieChartContainer
    var currentOrderMetric = METRIC_TYPE_ORDERS; // The metric that is currently displayd in the orderChartContainer
    var currentInventoryMetric = METRIC_TYPE_STOCKOUTS; // The metric that is currently displayd in the inventoryChartContainer
    
    var FETCH_PERIOD = 20; // 25 periods (e.g. 25 months, 25 days)
    var DISPLAY_PERIOD = 6; // show 6 periods of data
    
    // Init method initializes the domainDashboard
    function initDomainDashboard() { 
    	initFcHeader( MONTHLY ); // Initialize the header with the display date.
    	getData( <%=domainId%>, eDateForMonth, MONTHLY, FETCH_PERIOD, updateView );
    } // End init
	
    // Get the data and update the model, and then call the callback for rendering purpose
    function getData( domainId, endDate, periodType, fetchPeriod, callback ) {
		// Check if data already exists
		var fetchDate = countsAlreadyExist( periodType, getDisplayDate( periodType, endDate ), DISPLAY_PERIOD  ); // TODO: process display period here, maybe pass the domain creation date???
    	if ( fetchDate == null ) {
    		var data = getDisplayData( periodType, getDisplayDate( periodType, endDate ), DISPLAY_PERIOD );
    		callback( data, periodType );
    	} else {
    		var displayDate =  getDisplayDate( periodType, endDate );
    		fetchData( domainId, periodType, fetchDate, displayDate, FETCH_PERIOD, callback );
    	}
    }
    
    function updateView( data, periodType ) {
    	var domainCounts = getDomainCountsModel( periodType );
    	if ( data && data.length >= 1 ) {
    		initFcHeader( periodType );
    		// If the left button was disabled, enable it now.
    		if ( data.length > 1 )
    			enableButton( 'button-left-arrow', true );
    		else
    			enableButton( 'button-left-arrow', false );
    		showPanelsWithData( true, domainCounts.dshbrdCfg ); // Show the data panels
    		showPanelWithoutData( false ); // Hide the panels that have no data available image
    		showData( data, periodType, domainCounts.dshbrdCfg ); // Show  data
    	} else {
    		// Disable the left button navigator
    		enableButton( 'button-left-arrow', false );
    		// Show no data available
    		if ( domainCounts && !domainCounts.counts ) {
	    		// If there is no data to display, show the no data panels
				showPanelWithoutData( true );
    		}
			// Hide the panels with data
			// showPanelsWithData( false, domainCounts.dshbrdCfg );
    	}
    }
    
    function getDomainCountsModel( periodType ) {
    	var domainCounts;
    	if ( periodType == MONTHLY )
    		domainCounts = domainCountsMonthly;
    	else if ( periodType == DAILY )
    		domainCounts = domainCountsDaily;
    	else
    		console.log( 'Invalid period type' );
    	return domainCounts;
    }
    
    function setDomainCountsModel( periodType, domainCounts ) {
    	if ( domainCounts && domainCounts.counts ) {
    		// Iterate through domainCounts and convert the t in milliseconds to a moment object
    		for ( var i = 0; i < domainCounts.counts.length; i++ ) {
    			var temp = domainCounts.counts[i].t - offsetFromUtc;
    			// Add the offset to whatever comes from the server and then store in the model
    			domainCounts.counts[i].t = temp ;
    		}
    		// Added to store dCrTime in the model
    		if ( domainCounts.dCrTime ) {
    			var tempDCrTime = domainCounts.dCrTime - offsetFromUtc;
    			// Add the offset to whatever comes from the server and then store in the model
    			domainCounts.dCrTime = tempDCrTime ;
    		}
    	}
    	if ( periodType == MONTHLY )
    		domainCountsMonthly = domainCounts;
    	else if ( periodType == DAILY )
    		domainCountsDaily = domainCounts;
    	else
    		console.log( 'Invalid period type' );
    	/*if ( domainCounts.counts.length < FETCH_PERIOD ) {
    		domainCounts.noMoreData = true; // Not used. TODO: remove it
    	}*/
    }
    
 	// Will return the date from which data should be fetched in case the counts do not exist. Otherwise, it returns null.
    function countsAlreadyExist( periodType, t, displayPeriod ) { // TODO: process the display period
    	var domainCounts = getDomainCountsModel( periodType );
 		if ( domainCounts && domainCounts.counts ) {
 				var alreadyExists = false;
 				// Proceed to check if the counts exist
				var index = 0;
 				var count = 0;
				for ( var i = 0; i < domainCounts.counts.length; i++ ) {
					if ( t.valueOf() == domainCounts.counts[i].t ) {
						alreadyExists = true;
						count++;
						break;
					}
					index++;
				}
				if ( !alreadyExists ) {
					// If check is being made for t which is less than domain creation time, return null.
					// Earlier, domainCounts.noMoreData was being used. TODO: Remove old code.
					if ( t.valueOf() < domainCounts.dCrTime )//if ( domainCounts.noMoreData )
						return null;//	return null;
					else {
						if ( periodType == MONTHLY ) {
							t.add( 1, 'months' );
						} else if ( periodType == DAILY ) {
							t.add( 1, 'days' );
						}
						return new moment(t);
					}
				} else {
					var maxIndex = index + displayPeriod;
					maxIndex = ( maxIndex > domainCounts.counts.length ? domainCounts.counts.length : maxIndex );

					// from index onwards, iterate through domainCounts.counts, maxIndex number of times.
					var nonMatchingIndex = index;
					for( var j = index + 1; j < maxIndex; j++ ) {
						if ( periodType == MONTHLY ) {
							t.add( -1, 'months' );
						} else if ( periodType == DAILY ) {
							t.add( -1, 'days' );
						}
						if ( domainCounts.counts[j].t != t.valueOf() ) {
							break;
						}
						count++;
						if ( count == displayPeriod ) {
							break;
						}
						nonMatchingIndex++;
					}
					if ( count == displayPeriod )
						return null;
					else {
						/* Old code - Was using noMoreData flag in domainCounts
							if ( domainCounts.noMoreData ) {
							return null;
						} Old Code*/
						// If the check is for a t whose value is less than domain creation time, then return null.
						if ( t.valueOf() < domainCounts.dCrTime ) {
							return null;
						} else {
							// Otherwise, return 1 period more than t (because that will become the the fetch date. Otherwise, t's count will not be fetched.)
							if ( periodType == MONTHLY ) {
								t.add( 1, 'months' );
							} else if ( periodType == DAILY ) {
								t.add( 1, 'days' );
							}
							return new moment( t );
						}
					}
 			}
 		} else {
 			if ( periodType == MONTHLY ) {
				t.add( 1, 'months' );
			} else if ( periodType == DAILY ) {
				t.add( 1, 'days' );
			}
 			return new moment( t );
 		}
 	}
 
    // Function that returns data between startDate and endDate
    function getDisplayData( periodType, displayDate, displayPeriod ) {
    	var counts = getDomainCountsModel( periodType ).counts;
    	if ( !counts )
    		return null;
    	
		var subset = [];
		// Look for the selectedDate counts first. From that onwards, show the next 6 counts
		var index = 0;
		for ( var j = 0; j < counts.length; j++ ) {
			if ( counts[j].t == displayDate.valueOf() )
				break;
			index++;
		}
		// var maxIndex = ( index + displayPeriod - 1 );
		var maxIndex = index + displayPeriod;
		maxIndex = ( maxIndex > counts.length ? counts.length : maxIndex );
		
		// Add displayPeriod number of counts, starting from index
		for ( var i = index; i < maxIndex; i++ ) {
			subset.push( counts[i] );
		}
		return subset;
    }
 	
    // Initializes the FcHeader. 
    function initFcHeader( periodType ) {
    	var disable = false;
    	if ( periodType == MONTHLY ) {
    		// Do not allow scrolling beyond today's month
    		if ( getDisplayDate( periodType, eDateForMonth ).get( 'year') == today.get( 'year') && ( getDisplayDate( periodType, eDateForMonth ).get('month') == today.get('month' ) ) ) {
    		// if ( getDisplayDate( periodType, eDateForMonth ).diff( today, 'months' ) == 0 ) {
    			// moment.js month and year diffs function need a fix. For that reason, the above check is made to check a month difference of 0 months between two dates
    			disable = true;
    		}
    	} else if ( periodType == DAILY ) {
    		// If display date is one day less than today, disable the right arrow.
    		if ( today.diff(getDisplayDate( periodType, eDateForDay ), 'days') == 1 )	
    			disable = true;
    	} else {
    		console.log( 'Invalid periodType: ' + periodType );
    		return;
    	}
    	// console.log( 'disable: ' + disable );
    	enableButton( 'button-right-arrow', !disable ); // Pass !disable to enableButton function
    	setFcHeaderTitle( periodType ); // Sets the Fc header based on selected date and period option.
    }
    
    function setFcHeaderTitle( periodType ) {
    	var format = '';
    	var endDate;
    	if ( periodType == MONTHLY ) {
    		format = 'MMMM, YYYY';
    		endDate = eDateForMonth;
    	} else if ( periodType == DAILY ) {
    		format = 'MMMM D, YYYY';
    		endDate = eDateForDay;
    	}
		var title = getDisplayDate( periodType, endDate ).format( format );
    	$( '#fc-header-title' ).html( '<h2>' + title +'</h2>' );
    }
    
    function getDisplayDate( periodType, endDate ) {
    	var type = '';
    	if ( periodType == MONTHLY )
    		type = 'months';
    	else if ( periodType == DAILY )
    		type = 'days';
    	else
    		return null;
		return endDate.clone().add( -1, type );
    }
    
    function fetchData( domainId, periodType, fetchDate, displayDate, fetchPeriod, callback ) {
    	// Get the domain counts model
    	var domainCounts = getDomainCountsModel( periodType );
    	/*if ( domainCounts && domainCounts.noMoreData )
    		return;
    	*/
    	
    	// Fetch more data
    	showLoader( true );
    	
    	var endDateServerStr = getDateInDDMMYYYYFormat( fetchDate );
    	var periodTypeServer = '';
    	if ( periodType == MONTHLY )
    		periodTypeServer = 'monthly';
    	else if ( periodType == DAILY )
    		periodTypeServer = 'daily';
    	
    	// Construct the url
    	var url = '/s/dashboard?action=getdomaincounts' + '&domainid=' + domainId + '&period=' + fetchPeriod + '&periodtype=' + periodTypeServer + '&enddate=' + endDateServerStr;
    	console.log( 'url: ' + url );
    	$.ajax({
    		url: url,
    		dataType: 'json',
    		success: function( o ) {
   				console.log( o );
   				if ( o && o.counts ) {
    				if ( domainCounts && domainCounts.counts ) {
    					// Push the counts one by one after checking for duplicates.
    					for ( var i = 0; i < o.counts.length; i++ ) {
    						addCountToModel( periodType, o.counts[i] ); // Adding one count at  time, to the model
    					}
    					/*if ( o.counts.length < FETCH_PERIOD ) { // Added this to set noMoreData to true if future fetches result in o.counts < FETCH_PERIOD 
    			    		domainCounts.noMoreData = true; // Not used. TODO: Remove it
    			    	}*/
    				}
    				else
    					setDomainCountsModel( periodType, o );
    				// Sort the DomainCounts model here.
    				sortDomainCountsModel( periodType );
    			} else {
    				console.log( 'No data available' );
    				// domainCounts.noMoreData = true; // Not used. TODO: Remove it
    	   		}
    			showLoader( false );
    			
    			callback( getDisplayData( periodType, displayDate, DISPLAY_PERIOD ), periodType );
    		},
    		error: function( o ) {
    			console.log( 'ERROR: ' + o.msg ); 
    			var message = '';
        		if ( Object.keys( jsonData ).length > 0 )
        			message += '<b>' + Object.keys( jsonData ).length + '</b> fetched.';
            	message += ' A system error occured while fetching domain counts. Please contact your system administrator';
            	alert( message );
    		}
    	});
    }
    
    function showLoader( show ) {
    	var loaderDiv = document.getElementById( 'mainloader' );
    	if ( loaderDiv ) {
    		if ( show )
    			loaderDiv.innerHTML = '<h2 class="text-center"><img src="/images/loader.gif"/> Loading... Please wait</h2>';//'<img src="/images/loader.gif"/>';
    		else
    			loaderDiv.innerHTML = '';
    	}
    }
    
    
   function showPanelsWithData( show, dashboardConfig ) {
 		 if ( show ) {
 			if ( dashboardConfig && dashboardConfig.actPnlCfg ) {
 				if ( dashboardConfig.actPnlCfg.show == true )
 					$( '#activitypanel' ).show();
 				else
 					$( '#activitypanel' ).hide();
 			}
 			if ( dashboardConfig && dashboardConfig.rvnPnlCfg ) {
 				if ( dashboardConfig.rvnPnlCfg.show == true )
 					$( '#revenuepanel' ).show();
 				else
 					$( '#revenuepanel' ).hide();
 			}
 			if ( dashboardConfig && dashboardConfig.ordPnlCfg ) {
 				if ( dashboardConfig.ordPnlCfg.show == true )
 					$( '#orderpanel' ).show();
 				else 
 					$( '#orderpanel' ).hide();
 			}
 			if ( dashboardConfig && dashboardConfig.invPnlCfg ) {
 				if ( dashboardConfig.invPnlCfg.show == true )
 					$( '#inventorypanel' ).show();
 				else
 					$( '#inventorypanel' ).hide();
 			}
 		 } else {
 			// Hide all panels with data, irrespective of dashboardConfig
 		 	$( '#activitypanel' ).hide();
 			$( '#revenuepanel' ).hide();
 			$( '#orderpanel' ).hide();
 			$( '#inventorypanel' ).hide();
 		 }
     }
 	 
 	function showPanelWithoutData( show ) {
		 if ( show ) {
		 	$( '#panelwithoutdata' ).show();
		 } else {
			 $( '#panelwithoutdata' ).hide();
		 }
    } 
 	
    // Function that returns a date in dd/mm/yyyy format as required by the servlet.
    function getDateInDDMMYYYYFormat( date ) {
    	if ( date )
    		return ( date.get('date') + '/' + ( date.get('month') + 1 ) + '/' + date.get('year') );
    }
   
    function showData(  data, periodType, dshbrdCfg ) {
    	if ( data && data.length >= 1 ) {
    		if ( dshbrdCfg && dshbrdCfg.actPnlCfg && dshbrdCfg.actPnlCfg.show == true )	
    			showPanel( data, periodType, ACTIVITY_PANEL );
    		if ( dshbrdCfg && dshbrdCfg.rvnPnlCfg && dshbrdCfg.rvnPnlCfg.show == true )
	    		showPanel( data, periodType, REVENUE_PANEL );
    		if ( dshbrdCfg && dshbrdCfg.ordPnlCfg && dshbrdCfg.ordPnlCfg.show == true )
	    		showPanel( data, periodType, ORDER_PANEL );
    		if ( dshbrdCfg && dshbrdCfg.invPnlCfg && dshbrdCfg.invPnlCfg.show == true )
	    		showPanel( data, periodType, INVENTORY_PANEL );
    	} else {
    		console.log( 'No data available to show' );
    	}
    }
    
    
    // Called when the More Info link is clicked on the panel boxes on the left. Displays the graph for the selected metric 
    function onClickMoreInfo( metric, chartId, chartType, chartContainerId ) {
    	// currentActivityMetric = metric; // Fix for the bug that was causing stock replenishment graph in the activity panel
    	var endDate;
    	var periodType = getPeriodType();
    	if ( periodType == DAILY )
    		endDate = eDateForDay;
    	else if ( periodType == MONTHLY )
    		endDate = eDateForMonth;
    	else
    		console.log( 'Invalid periodType: ' + periodType );
    	var data = getDisplayData( periodType, getDisplayDate( periodType, endDate ), DISPLAY_PERIOD );
    	var chartId;
    	var chartType;
    	var chartContainer;
    	
    	if ( metric == METRIC_TYPE_TRANSACTIONS || metric == METRIC_TYPE_ACTIVEUSERS || metric == METRIC_TYPE_ACTIVEENTITIES ) {
    		chartId = 'activityChartId';
    		chartType = 'Column2D';
    		chartContainer = 'activityChartContainer';
			currentActivityMetric = metric;
    	} else if ( metric == METRIC_TYPE_REVENUEBOOKED ) {
    		chartId = 'revenueChartId';
       		chartType = 'Area2D';
       		chartContainer = 'revenueChartContainer';
			currentRevenueMetric = metric;

    	} else if ( metric == METRIC_TYPE_ORDERS || metric == METRIC_TYPE_FULFILLEDORDERS ||  metric == METRIC_TYPE_PENDINGORDERS || metric == METRIC_TYPE_ORDERRESPONSETIME ) {
    		chartId = 'orderChartId';
       		chartType = 'Column2D';
       		chartContainer = 'orderChartContainer';
			currentOrderMetric = metric;
    	} else if ( metric == METRIC_TYPE_STOCKOUTS || metric == METRIC_TYPE_LESSTHANMIN || metric == METRIC_TYPE_GREATERTHANMAX || metric == METRIC_TYPE_REPLENISHMENTRESPONSIVENESS ) {
    		chartId = 'inventoryChartId';
        	chartType = 'Column2D';
        	chartContainer = 'inventoryChartContainer';
			currentInventoryMetric = metric;
    	}
    		
    	// showChart( chartId, chartType, chartContainer, data, periodType, currentActivityMetric );
		showChart( chartId, chartType, chartContainer, data, periodType, metric );
    }
    
    // Based on the panel type, show the appropriate chart on the right hand side and the latest month's data in the panel boxes on the left hand side.
   function showPanel( data, periodType, panelType ) {
 		var dataInBox = data[0];
	    var showPerChng = true; // This was introduced because for the current month, percentage change will always be negative (ideally it should be computed correctly in the backend and sent to the UI)
	   // So, do not show percentage change if periodType is MONTHLY and the month is current month
	   	if ( periodType == MONTHLY && dataInBox.t == getStartOfMonth( today.clone()).valueOf() ) {
			showPerChng = false;
		}

 		if ( panelType == ACTIVITY_PANEL ) {
 			showChart( 'activityChartId', 'Column2D', 'activityChartContainer', data, periodType, currentActivityMetric );
 			updatePanelBoxes( 'small-box-transactions', showPerChng,  dataInBox.trans, dataInBox.transChngPer, METRIC_TYPE_TRANSACTIONS, TRANSACTIONS );
 	    	updatePanelBoxes( 'small-box-users', showPerChng, dataInBox.transUsrs, dataInBox.transUsrsChngPer, METRIC_TYPE_ACTIVEUSERS, ACTIVE_USERS );
 	    	updatePanelBoxes( 'small-box-entities', showPerChng, dataInBox.transEnts, dataInBox.transEntsChngPer, METRIC_TYPE_ACTIVEENTITIES, ACTIVE_ENTITIES );
 		} else if ( panelType == REVENUE_PANEL ) {
 			showChart( 'revenueChartId', "Area2D", 'revenueChartContainer', data, periodType, currentRevenueMetric );
 			// Get the currency for revenue and append it to dataInBox.rvn
 			var model = getDomainCountsModel( periodType );
 			var currency = null;
 			var displayText = REVENUE;
 			if ( model ) {
 				currency = model.currency;
 				if ( currency != null )
 					displayText += ' (' + currency + ')'; 
 			}
 	    	updatePanelBoxes( 'small-box-revenue', showPerChng, dataInBox.rvn, dataInBox.rvnChngPer, METRIC_TYPE_REVENUEBOOKED, displayText );
 		} else if ( panelType == ORDER_PANEL ) {
 			showChart( 'orderChartId', 'Column2D', 'orderChartContainer', data, periodType, currentOrderMetric );
 	    	updatePanelBoxes( 'small-box-orders', showPerChng, dataInBox.ordrs, dataInBox.ordrsChngPer, METRIC_TYPE_ORDERS, ORDERS );
 	    	updatePanelBoxes( 'small-box-fulfilledorders', showPerChng, dataInBox.fulOrdrs, dataInBox.fulOrdrsChngPer, METRIC_TYPE_FULFILLEDORDERS, FULFILLED_ORDERS );
 	    	updatePanelBoxes( 'small-box-pendingorders', showPerChng, dataInBox.pndgOrdrs, dataInBox.pndgOrdrsChngPer, METRIC_TYPE_PENDINGORDERS, PENDING_ORDERS );
 	    	updatePanelBoxes( 'small-box-orderresponsetime', showPerChng, dataInBox.ordrRspTime, dataInBox.ordrRspTimeChngPer, METRIC_TYPE_ORDERRESPONSETIME, ORDER_RESPONSE_TIME );
 		} else if ( panelType == INVENTORY_PANEL ) {
 			showChart( 'inventoryChartId', 'Column2D', 'inventoryChartContainer', data, periodType, currentInventoryMetric );
 	    	updatePanelBoxes( 'small-box-stockouts', showPerChng, dataInBox.stckOuts, dataInBox.stckOutsChngPer, METRIC_TYPE_STOCKOUTS, STOCKOUTS );
 	    	updatePanelBoxes( 'small-box-lessthanmin', showPerChng, dataInBox.lessThanMin, dataInBox.lessThanMinChngPer, METRIC_TYPE_LESSTHANMIN, LESS_THAN_MIN );
 	    	updatePanelBoxes( 'small-box-greaterthanmax', showPerChng, dataInBox.grtThanMax, dataInBox.grtThanMaxChngPer, METRIC_TYPE_GREATERTHANMAX, GREATER_THAN_MAX );
 	    	updatePanelBoxes( 'small-box-replenishmentresponsiveness', showPerChng, dataInBox.rplRspTime, dataInBox.rplRspTimeChngPer, METRIC_TYPE_REPLENISHMENTRESPONSIVENESS, REPLENISHMENT_RESPONSIVENESS );
 		}
 	}
 	
    // Displays the chart for the selected metric on the right hand side.
 	function showChart( chartId, chartType, chartContainer, data, periodType, metric  ) {
 		// Activity Chart is a column chart. Specify the chartId, width and height.
    	var chart = document.getElementById( chartId );
    	if ( !chart )
    		chart = new FusionCharts( chartType, chartId, "100%", "216px" );
    	
    	var data = getFusionChartJsonData( data, periodType, metric ); // Get the fusionChartJsonData
    	chart.setJSONData( data );
    	chart.render( chartContainer ); 
 	}
 	
    // Updates the panel boxes with count and variation
 	function updatePanelBoxes( panelBoxId, showPerChng, count, variation, metric, displayText ) {
    	var small_box;
    	var text = displayText;
    	var small_box = document.getElementById( panelBoxId );
    	
    	var arrowClass = "fa fa-arrow-circle-up";
    	
    	if ( variation < 0 ) {
    		arrowClass = "fa fa-arrow-circle-down";	
    	}
    	// Do not show the variation if it is 0 or if showPerChng is false.
    	small_box.innerHTML = '<span class="num">' + count + '</span><p>' + text + ( variation == 0 || !showPerChng ? '' : '</p><span class="inum"><i class="' + arrowClass + '"></i> ' + variation + '%' + '</span>' );
    }
    
    
	// Function that returns json data as required by fusion charts for the selected activity and selected period
    function getFusionChartJsonData( data, periodType, metric ) {
	 	var fusionChartJson = {};
	 	var chartJson = getChartJson( data, periodType, metric ); // The "chart" variable that requires details about the xAxis, yAxis, caption etc.
	 	var dataJson = getFCJsonData( data, periodType, metric ); // The actual data displayed.
	 	
    	// Form the fusionChartJson here.
    	fusionChartJson = { "chart" : chartJson,
    						"data": dataJson
    	};
    	return fusionChartJson;
    	
    }
 
 	// Returns "chart" json variable as required by Fusion Charts
 	function getChartJson( data, periodType, metric ) {
 		// Get the units for revenue	
 		var model = getDomainCountsModel( periodType );
 		var currency = null;
 		if ( model ) {
 			currency = model.currency;
 		}
 		var chartJson = {};
 		var yAxisName = '';
    	var barColor = "#39CCCC";
    	
    	var periodStr = '';
    	var caption = '';
    	if ( periodType == DAILY )
    		periodStr = 'days';
    	else if ( periodType == MONTHLY )
    		periodStr = 'months';
    	else if ( periodType == QUARTERLY )
    		periodStr = 'quarters';
    	else if ( periodType == YEARLY )
    		periodStr = 'years';
    	
    	// Decide the caption and yAxis name to show on the graph.
    	if ( metric == METRIC_TYPE_TRANSACTIONS ) {
    		yAxisName = '#' + TRANSACTIONS;
    		caption = TRANSACTIONS + ' in the last six ' + periodStr;
    	} else if ( metric == METRIC_TYPE_ACTIVEUSERS ) {
    		yAxisName = '#' + ACTIVE_USERS;
    		caption = ACTIVE_USERS + ' in the last six ' + periodStr;
    		barColor = '#00A65A'
    	} else if ( metric == METRIC_TYPE_ACTIVEENTITIES ){
    		yAxisName = '#' + ACTIVE_ENTITIES;
    		caption = ACTIVE_ENTITIES + ' in the last six ' + periodStr;
    		barColor="#F56954";
    	} /*else if ( metric == METRIC_TYPE_ACTIVEMATERIALS) {
    		yAxisName = '#' + ACTIVE_MATERIALS;
    		caption = ACTIVE_MATERIALS + ' in the last six ' + periodStr;
    		barColor="#F39C12";
    	} */else if ( metric == METRIC_TYPE_REVENUEBOOKED ) {
    		yAxisName = '#' + REVENUE + ' (' + currency + ')';
    		caption = REVENUE + ' in the last six ' + periodStr;
    		barColor = '#00A65A';
    	} /*else if ( metric == METRIC_TYPE_ENTITIESEXCEEDINGCREDITLIMIT ) {
    		yAxisName = '#' + ENTITIES_EXCEEDING_CREDIT_LIMIT;
    		caption = ENTITIES_EXCEEDING_CREDIT_LIMIT + ' in the last six ' + periodStr;
    		barColor="#F56954";
    	} */else if ( metric == METRIC_TYPE_ORDERS ) {
    		yAxisName = '#' + ORDERS;
    		caption = ORDERS + ' in the last six ' + periodStr;
    		barColor = "#39CCCC";
    	} else if ( metric == METRIC_TYPE_FULFILLEDORDERS ) {
    		yAxisName = '#' + ORDERS;
    		caption = FULFILLED_ORDERS + ' in the last six ' + periodStr;
    		barColor = "#00A65A";
    	} else if ( metric == METRIC_TYPE_PENDINGORDERS ) {
    		yAxisName = '#' + PENDING_ORDERS;
    		caption = PENDING_ORDERS + ' in the last six ' + periodStr;
    		barColor = "#F56954";
    	} else if ( metric == METRIC_TYPE_ORDERRESPONSETIME ) {
    		yAxisName = '#' + ORDER_RESPONSE_TIME;
    		caption = ORDER_RESPONSE_TIME + ' in the last six ' + periodStr;
    		barColor = "#F39C12";
    	} else if ( metric == METRIC_TYPE_STOCKOUTS ) {
    		yAxisName = '#' + STOCKOUTS;
    		caption = STOCKOUTS + ' in the last six ' + periodStr;
    		barColor = "#F56954";
    	}  else if ( metric == METRIC_TYPE_LESSTHANMIN ) {
    		yAxisName = '#' + LESS_THAN_MIN;
    		caption = LESS_THAN_MIN + ' in the last six ' + periodStr;
    		barColor = "#F56954";
    	}  else if ( metric == METRIC_TYPE_GREATERTHANMAX) {
    		yAxisName = '#' + GREATER_THAN_MAX;
    		caption = GREATER_THAN_MAX + ' in the last six ' + periodStr;
    		barColor = "#F56954";
    	} else if ( metric == METRIC_TYPE_REPLENISHMENTRESPONSIVENESS ) {
    		yAxisName = '#' + REPLENISHMENT_RESPONSIVENESS;
    		caption = REPLENISHMENT_RESPONSIVENESS + ' in the last six ' + periodStr;
    		barColor = "#F39C12";
    	}   
    	
    	
    	chartJson.caption = caption;
    	chartJson.exportenabled = "1";
    	chartJson.xaxisName = "Time";
    	chartJson.yaxisName = yAxisName;
    	chartJson.theme = "fint";
    	chartJson.animation = "1";
    	chartJson.paletteColors = barColor;
    	chartJson.rotatevalues = "0";
    	chartJson.labeldisplay = "ROTATE";
    	chartJson.showvalues = "1";
    	chartJson.slantlabels = "1";
    	return chartJson;
 	}
 	
 	// Function that returns json data as required by fusion charts for the selected activity and selected period
    function getFCJsonData( data, periodType, metric ) {
    	var fcData = [];
       		if ( data ) {
          			for ( var i = data.length - 1; i >= 0; i-- ) {
           			var element = {};
           			element.label = getLabel( periodType, data[i].t ); // Form the element label from t in milliseconds
           			if ( metric == METRIC_TYPE_TRANSACTIONS )
           				element.value = data[i].trans;
           			else if ( metric == METRIC_TYPE_ACTIVEUSERS )
           				element.value = data[i].transUsrs;
           			else if ( metric == METRIC_TYPE_ACTIVEENTITIES )
           				element.value = data[i].transEnts;
           			else /*if ( metric == METRIC_TYPE_ACTIVEMATERIALS )
           				element.value = countsMonthlySubset[i].actInvItems;
           			else */if ( metric == METRIC_TYPE_REVENUEBOOKED )
           				element.value = data[i].rvn;
           			else /*if ( metric == METRIC_TYPE_ENTITIESEXCEEDINGCREDITLIMIT )
           				element.value = countsMonthlySubset[i].numEntsExcCrLmt;
           			else */if ( metric == METRIC_TYPE_ORDERS )
           				element.value = data[i].ordrs;
           			else if ( metric == METRIC_TYPE_FULFILLEDORDERS )
           				element.value = data[i].fulOrdrs;
           			else if ( metric == METRIC_TYPE_PENDINGORDERS )
           				element.value = data[i].pndgOrdrs;
           			else if ( metric == METRIC_TYPE_ORDERRESPONSETIME )
           				element.value = data[i].ordrRspTime;
           			else if ( metric == METRIC_TYPE_STOCKOUTS )
           				element.value = data[i].stckOuts;
           			else if ( metric == METRIC_TYPE_LESSTHANMIN )
           				element.value = data[i].lessThanMin;
           			else if ( metric == METRIC_TYPE_GREATERTHANMAX )
           				element.value = data[i].grtThanMax;
           			else if ( metric == METRIC_TYPE_REPLENISHMENTRESPONSIVENESS )
           				element.value = data[i].rplRspTime;
           			
           			fcData.push( element );
           		}
           		return fcData;
           	}
    	
    }
 
    function onClickPeriodType( domainId, periodType ) {
    	var endDate;
		if ( periodType == MONTHLY ) {
	    	removeClassActive( ['day'] ); // ,'week','quarter','year'] ); // Make the other buttons normal.
	    	addClassActive( 'month' ); // Highlight the Day button
	    	endDate = eDateForMonth;
		} else if ( periodType == DAILY ) {
    		removeClassActive( ['month'] );// ['week','month' ,'quarter','year'] ); // Make the other buttons normal.
    		addClassActive( 'day' ); // Highlight the Day button
    		// eDateForDay as the last day of the displayed date.
    		// if ( eDateForMonth.diff( today,'months' ) == 1 )
    		// moment.js month and year diffs function need a fix. For that reason, the following check is made to check a month difference of 1 month
    		if ( eDateForMonth.get( 'year' ) == today.get( 'year' ) && ( eDateForMonth.get( 'month' ) - today.get( 'month' ) == 1 ) )
    			eDateForDay = today.clone();
    		else
    			eDateForDay = eDateForMonth.clone();
    		endDate = eDateForDay;
		}
    	getData( domainId, endDate, periodType, FETCH_PERIOD, updateView );
    }
        
    function onClickArrow( domainId, arrowButtonId, scrollDirection ) {
    	if ( $('#' + arrowButtonId ).hasClass( 'fc-state-disabled' ) ) {
			return;
		}
    	var offset;
    	if ( scrollDirection == 'left' )
    		offset = -1;
    	else if ( scrollDirection == 'right' )
    		offset = 1;
    	else {
    		console.log( 'Invalid scrollType' );
    		return;
    	}
    	var periodType = getPeriodType();
    	modifyEndDates( periodType, offset );
    	// initFcHeader( periodType ); Init the header in updateView
		var endDate;
    	if ( periodType == DAILY )
    		endDate = eDateForDay;
    	else if ( periodType == MONTHLY )
    		endDate = eDateForMonth;
    	else {
    		console.log( 'Invalid periodType: ' + periodType );
    		return;
    	}
    	getData( domainId, endDate, periodType, DISPLAY_PERIOD, updateView );	
    }
    
    function modifyEndDates( periodType, offset ) {
    	// console.log( 'Exiting modifyEndDates, eDateForDay: ' + eDateForDay.toString() + ', eDateForMonth: ' + eDateForMonth.toString() );
    	if ( periodType == DAILY ) {
    		var monthOfPrevDisplayDate = getDisplayDate( periodType, eDateForDay ).get( 'month' );
    		eDateForDay.add(offset, 'days' );
    		if ( monthOfPrevDisplayDate != getDisplayDate( periodType, eDateForDay ).get('month') ) {
    			eDateForMonth.add( offset, 'months' );
    		}
    	} else if ( periodType == MONTHLY ) {
    		eDateForMonth.add( offset, 'months');
    		// if ( eDateForMonth.diff( today, 'months') == 1 )
    		// moment.js month and year diffs function need a fix. For that reason, the following check is made to check a month difference of 1 month
    		if ( eDateForMonth.get( 'year' ) == today.get('year') && ( eDateForMonth.get( 'month' ) - today.get( 'month' ) == 1 ) )
    			eDateForDay = today.clone();
    		else
    			eDateForDay = eDateForMonth.clone();
    	}
    	// console.log( 'Exiting modifyEndDates, eDateForDay: ' + eDateForDay.toString() + ', eDateForMonth: ' + eDateForMonth.toString() );
    }
    
    // Adds the class fc-state-active to make the selected button active
	function addClassActive( selectedButtonId ) {
		$( '#' + selectedButtonId ).addClass( 'fc-state-active' );
	}
	
	// Removes the class fc-state-active if present from the buttons with the specified ids.
	function removeClassActive( selectedButtonIds ) {
		if ( selectedButtonIds && selectedButtonIds != null ) {
			for ( var i = 0; i < selectedButtonIds.length; i++ ) {
				var buttonId = selectedButtonIds[i];
				if ( $('#' + buttonId).hasClass( 'fc-state-active' ) ) {
					$('#' + buttonId).removeClass( 'fc-state-active' );
				}
			}
		}
		
	}
	
	// Given time in milliseconds get the time in string format for display in fusion charts. 
	// The string format also depends on the period selection.
	function getLabel( periodType, t ) {
		var date = new moment( t );
		var tstr = '';
		var format = '';
		if ( periodType == MONTHLY )
			format = 'MMM YYYY';
		else if ( periodType == DAILY )
			format = 'DD MMM YYYY';
		tstr = date.format( format );
		return tstr;
	}
	
	function getFirstDayOfNextMonth( date ) {
		date.add(1, 'months'); // eDateForMonth = 1st day of next month
	    date.set('date', 1); // Set the date to first of next month
	    return date;
	}
	
	function getEndOfMonth( date ) {
		date.endOf('month');
		date.set( 'hour',0 );
		date.set( 'minute', 0 );
		date.set( 'second', 0 );
		date.set( 'millisecond', 0 );
		return date;
	}

	function getStartOfMonth( date ) {
		date.startOf('month');
		date.set( 'hour',0 );
		date.set( 'minute', 0 );
		date.set( 'second', 0 );
		date.set( 'millisecond', 0 );
		return date;
	}
	
	function getPeriodType() {
		var periodType = MONTHLY;
		if ( $('#day').hasClass( 'fc-state-active' ) )
			periodType = DAILY;
		else if ( $('#month').hasClass( 'fc-state-active' ) )
			periodType = MONTHLY;
		return periodType;
	}
	
	function sortDomainCountsModel( periodType ) {
		var model = getDomainCountsModel( periodType );
		if ( model && model.counts ) {
			model.counts.sort( compareTimestamp );
		}
		else
			console.log( 'DomainCounts model is null' );
	}

	// Sort in the descending order of timestamp
	function compareTimestamp(a,b) {
	  if ( a.t > b.t )
	     return -1;
	  if (a.t < b.t)
	    return 1;
	  return 0;
	}
	
	function enableButton( buttonId, enable ) {
		if ( enable ) {
			if ( $('#' + buttonId ).hasClass( 'fc-state-disabled' ) ) {
				$('#' + buttonId ).removeClass( 'fc-state-disabled' );
			}
		} else {
			$('#' + buttonId ).addClass( 'fc-state-disabled' );
		}
	}
	
	// This method adds just one count at a time to the model.
	// It checks if the count is not already present in the model and only then adds it.
	// Otherwise, just returns.
	function addCountToModel( periodType, count ) {
		var domainCounts = getDomainCountsModel( periodType );
		// count.t = new moment ( count.t ).toDate().valueOf();
		count.t = count.t - offsetFromUtc;
		if ( domainCounts && domainCounts.counts ) {
			var found = false;
			for ( var j = 0; j < domainCounts.counts.length; j++ ) {
				if ( domainCounts.counts[j].t == count.t ) {
					found = true;
					break;
				}
			}
			if ( found == false ) {
				domainCounts.counts.push( count );
			}
		}
	}
	
    </script>