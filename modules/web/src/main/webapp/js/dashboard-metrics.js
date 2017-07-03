var dIdDomainStatsMap = {};
var dateDidMap = {};

var jsonData = [];
var jsonDataAggr = {};
var selectedMonth;
var selectedYear;
var selectedDateStr;
var domainId;
var defaultSize; 
var domainName;
var months = ['January','February','March','April','May','June','July','August','September','October','November','December'];
var monthlyChartViewPanel;

function showChartView( chartViewDivId, pageParamsDefaultSize, domId, selectedDomainName, selDateStr, offsetDateStr, showOnIndexPage ) {
	// Set the global variables here.
	defaultSize = pageParamsDefaultSize;
	domainId = domId;
	domainName = selectedDomainName;
	selectedDateStr = selDateStr;
	selectedMonth = getMonth( selectedDateStr );
	selectedYear = getYear( selectedDateStr );
	initJsonData();
	initJsonDataAggr();
	monthlyChartViewPanel = document.getElementById( chartViewDivId );
	// Flags that are used to decide whether to fetch data from server or cache.
	var dateFound = false;
	var dIdFound = false;
	if ( dateDidMap.hasOwnProperty( selectedDateStr ) ) {
		dateFound = true;
		var dIdArray = dateDidMap[selectedDateStr];
		if ( dIdArray.indexOf( domainId ) != - 1 ) {
			dIdFound = true;
			jsonData = dIdDomainStatsMap[domainId].mData; // Set jsonData to the one already there in map
			updateJsonDataAggr(); // Update the counts in jsonDataAggr.
			monthlyChartViewPanel.style.display = 'block'; // Display the chart view.
			drawChartViewPanel( showOnIndexPage ); // Draw the charts
		}
	} 
	// If the date is not found, then re-initialize the dateDidMap.
	if ( !dateFound ) {
		dateDidMap = {};
		dateDidMap[selectedDateStr] = [];
	}
	// If dId is not found in the didDomainStatsMap, then fetch data from the server.
	if ( !dIdFound ) {		
		// Fetch the data from the server.
		// Show the loader
		monthlyChartViewPanel.innerHTML = '<img src="/images/loader.gif" />';
		monthlyChartViewPanel.style.display = 'block';
		// Fetch data from server.
		var baseUrl = '/s/dashboard?action=getmonthlyusagestatsfordomain&domainid=' + domainId + "&startdate=" + offsetDateStr;
		fetchMonthlyUsageStatsForDomain( baseUrl, defaultSize, 0, null, showOnIndexPage );
	}
}

// Fecth the monthly usage stats for the domain from server.
function fetchMonthlyUsageStatsForDomain( baseUrl, size, doneSoFar, cursor, showOnIndexPage ) {
	var url = baseUrl + '&size=' + size;
	if ( cursor )
		url += '&cursor=' + cursor;
	var message = '';
	if ( doneSoFar > 0 )
		message = '<b>' + Object.keys( jsonData ).length + '</b> fetched. '; 
	$.ajax({
		url: url,
		dataType: 'json',
		success: function( o ) {
			// Get the results into statsData.
			var statsData = o.domainUsageStatsList;
			// If statsData is > 0, then update doneSoFar.
			// Push the statsData into jsonData
			if ( statsData && statsData.length > 0 ) {
				doneSoFar += statsData.length;
				// Iterate through statsData and push each into jsonData. This is the master list.
				for ( var i = 0; i < statsData.length; i++ ) {
					var element = statsData[i];
					// Get the month from the startDateStr
					var time = new Date( element.t );
					var month = time.getMonth();
					// The query returns all the results >= the selected date - OFFSETMONTHS. But in cases where selected date is < today, this could cause
					// more results to be returned. So, display only the results only six months older than the selected date
					if ( month < selectedMonth ) {
						jsonData.push( element );
					}
				}
				// Now recursively call fetchMonthlyStatsForDomain again, but this time with a cursor returned from the backend.
				// If the operation is not cancelled by the user or if the data returned is the same size as PageParams.DEFAULT_SIZE, then
				// proceed to recursively fetchData.
				if ( statsData.length == defaultSize ) {	
					fetchMonthlyUsageStatsForDomain( baseUrl, size, doneSoFar, o.cursor );
				}
			}
			
			if ( !statsData || statsData && statsData.length < defaultSize ) {
				sortJsonData();  
				updateJsonDataAggr();
				// Push the dId into the dIdDomainStatsMap
				var domainStats = {};
				domainStats[ "mData" ] = jsonData;
				dIdDomainStatsMap[domainId] = domainStats;
				// Push the dId into dateDidMap
				dateDidMap[selectedDateStr].push( domainId );
				drawChartViewPanel( showOnIndexPage );
			}
		},
		error: function( o ) {
			console.log( 'ERROR: ' + o.msg ); 
			var message = '';
    		if ( Object.keys( jsonData ).length > 0 )
    			message += '<b>' + Object.keys( jsonData ).length + '</b> fetched.';
        	message += ' A system error occured while fetching monthly usage statistics. Please contact your system administrator';
		}
	});
}

// Returns the month given a date string in dd/MM/yyyy format.
function getMonth( date ) {
	if ( date ) {
		return date.substring( 3, 5 );
	}
}
//Returns the year given a date string in dd/MM/yyyy format.
function getYear( date ) {
	if ( date ) {
		return date.substring( 6, 10 );
	}
}
// Initialize the counts in the jsonDataAggr object. This is displayed in the table.
function initJsonDataAggr() {
	jsonDataAggr.uc = 0;
	jsonDataAggr.lnc = 0;
	jsonDataAggr.ec = 0;
	jsonDataAggr.tc = 0;
	jsonDataAggr.ctc = 0;
	jsonDataAggr.oc = 0;
	jsonDataAggr.coc = 0;
	jsonDataAggr.brv = 0;
	jsonDataAggr.mc = 0;
	jsonDataAggr.t = 0;
	jsonDataAggr.cur = '';
}

function initJsonData() {
	if ( jsonData.length > 0 ) {
		jsonData = [];
	}
}

function sortJsonData() {
	jsonData.sort( compareTimestamp );
}

function compareTimestamp(a,b) {
  if ( a.t < b.t )
     return -1;
  if (a.t > b.t)
    return 1;
  return 0;
}

function updateJsonDataAggr() {
	// Iterate through jsonData and add get the total counts across months
	for ( var i = 0; i < jsonData.length; i++ ) {
		var element = jsonData[i];
		jsonDataAggr.uc += element.uc;
		jsonDataAggr.lnc += element.lnc;
		jsonDataAggr.ec += element.ec;
		jsonDataAggr.tc += element.tc;
		jsonDataAggr.ctc += element.ctc;
		jsonDataAggr.oc += element.oc;
		jsonDataAggr.coc += element.coc;
		jsonDataAggr.brv += element.brv;
		jsonDataAggr.mc += element.mc;
		jsonDataAggr.cur = element.cur;
	}
}

function drawChartViewPanel( showOnIndexPage ) {
	// jsonData = [{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":40000,"truc":0,"ec":17,"aec":0,"tc":1100,"oc":300,"evc":0,"brv":16942792.00,"mc":35,"ctc":1979,"coc":3,"cevc":0,"cbrv":27.3,"t":1401580800000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":10009,"truc":0,"ec":17,"aec":0,"tc":800,"oc":2000,"evc":0,"brv":5666790.50,"mc":35,"ctc":1979,"coc":1,"cevc":0,"cbrv":18.9,"t":1404172800000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":2000,"truc":0,"ec":17,"aec":0,"tc":7890,"oc":800,"evc":0,"brv":8765430.50,"mc":35,"ctc":1979,"coc":1,"cevc":0,"cbrv":18.9,"t":1406851200000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":20,"truc":0,"ec":17,"aec":0,"tc":8988,"oc":55,"evc":0,"brv":9999990.80,"mc":35,"ctc":1987,"coc":1,"cevc":0,"cbrv":18.9,"t":1409529600000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":900,"truc":0,"ec":17,"aec":0,"tc":1876,"oc":990,"evc":0,"brv":876543.40,"mc":35,"ctc":1988,"coc":1,"cevc":0,"cbrv":18.9,"t":1412121600000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":1700,"truc":0,"ec":17,"aec":0,"tc":1874,"oc":0,"evc":0,"brv":76565070.00,"mc":35,"ctc":1989,"coc":1,"cevc":0,"cbrv":18.9,"t":1414800000000, "cur":"USD"}]; // TODO: Remove this line later. It is there only for testing...
	// jsonData = [{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":4,"truc":0,"ec":17,"aec":0,"tc":11,"oc":3,"evc":0,"brv":27.53,"mc":35,"ctc":1979,"coc":3,"cevc":0,"cbrv":27.3,"t":1401580800000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":1,"truc":0,"ec":17,"aec":0,"tc":0,"oc":0,"evc":0,"brv":56.60,"mc":35,"ctc":1979,"coc":1,"cevc":0,"cbrv":18.9,"t":1404172800000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":2,"truc":0,"ec":17,"aec":0,"tc":0,"oc":0,"evc":0,"brv":8.76,"mc":35,"ctc":1979,"coc":1,"cevc":0,"cbrv":18.9,"t":1406851200000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":2,"truc":0,"ec":17,"aec":0,"tc":8,"oc":0,"evc":0,"brv":99.90,"mc":35,"ctc":1987,"coc":1,"cevc":0,"cbrv":18.9,"t":1409529600000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":0,"truc":0,"ec":17,"aec":0,"tc":1,"oc":0,"evc":0,"brv":876.54,"mc":35,"ctc":1988,"coc":1,"cevc":0,"cbrv":18.9,"t":1412121600000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":0,"truc":0,"ec":17,"aec":0,"tc":1,"oc":0,"evc":0,"brv":76.56,"mc":35,"ctc":1989,"coc":1,"cevc":0,"cbrv":18.9,"t":1414800000000, "cur":"USD"}]; // TODO: Remove this line later. It is there only for testing...
	// jsonData = [{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":4,"truc":0,"ec":17,"aec":0,"tc":11,"oc":3,"evc":0,"brv":2700.56,"mc":35,"ctc":1979,"coc":3,"cevc":0,"cbrv":27.3,"t":1401580800000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":1,"truc":0,"ec":17,"aec":0,"tc":0,"oc":0,"evc":0,"brv":5699.60,"mc":35,"ctc":1979,"coc":1,"cevc":0,"cbrv":18.9,"t":1404172800000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":2,"truc":0,"ec":17,"aec":0,"tc":0,"oc":0,"evc":0,"brv":800.76,"mc":35,"ctc":1979,"coc":1,"cevc":0,"cbrv":18.9,"t":1406851200000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":2,"truc":0,"ec":17,"aec":0,"tc":8,"oc":0,"evc":0,"brv":990.90,"mc":35,"ctc":1987,"coc":1,"cevc":0,"cbrv":18.9,"t":1409529600000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":0,"truc":0,"ec":17,"aec":0,"tc":1,"oc":0,"evc":0,"brv":8706.54,"mc":35,"ctc":1988,"coc":1,"cevc":0,"cbrv":18.9,"t":1412121600000, "cur":"USD"},{"dId":1083501,"dName":"Made to Order (Dev)","uc":42,"lnc":0,"truc":0,"ec":17,"aec":0,"tc":1,"oc":0,"evc":0,"brv":7600.65,"mc":35,"ctc":1989,"coc":1,"cevc":0,"cbrv":18.9,"t":1414800000000, "cur":"USD"}]; // TODO: Remove this line later. It is there only for testing...
	var monthlyChartViewPanel = document.getElementById( 'monthlychartviewpanel' );
	var html = '';
	var chartTitleHTML = '';
	var domainUsageStatsPanelTableHTML = '';
	if ( !showOnIndexPage ) {
		chartTitleHTML = getChartTitleHTML();
	}
	if ( !showOnIndexPage ) {
		domainUsageStatsPanelTableHTML = getDomainUsageStatsPanelTableHTML();
	}
	var domainUsageStatsPanelChartHTML = getDomainUsageStatsPanelChartHTML();
	html += chartTitleHTML + domainUsageStatsPanelTableHTML + domainUsageStatsPanelChartHTML;
	// Set the innerHTML of the monthlychartviewpanel.  
	monthlyChartViewPanel.innerHTML = html; 
	// Now draw the charts in each of the divs.
	drawMonthlyUsageCharts( showOnIndexPage );
}

function getChartTitleHTML() {
	var html = '';
	var monthYearStr = months[selectedMonth - 1] + ", " + selectedYear;
	var chartTitleStr = domainName + ": " + monthYearStr;
	html += '<a href="#" onclick="onClickAll()">All</a> > ' + '<b>' + chartTitleStr + '</b>  &nbsp;&nbsp;(<a href="#" onclick="onClickAll()"><font style="font-size:8pt">back</font></a>)';
	return html;
}

function getDomainUsageStatsPanelTableHTML() {
	var html = '';
	var divClass = 'ui-widget ui-widget-content ui-corner-all';
	var divStyle = 'padding:4px;margin-top:10px;margin-bottom:15px;box-shadow: 0 1px 4px rgba(0,0,0,0.5);-moz-box-shadow: 0 4px 8px rgba(0,0,0,0.5);-webkit-box-shadow: 0 4px 8px rgba(0,0,0,0.5);';
	var divId = 'domainusagestatspaneltable';
	html += '<div id="' + divId + '"' + 'class="' + divClass + '"'+ 'style="' + divStyle + '">'; 
	var str = getDomainUsageStatsTableHTML();
	html += str;
	html += '</div>';
	return html;
}

// Get the HTML for displaying the domain usage stats
/*
function getDomainUsageStatsTableHTML() {
var html = '';
var tableStyle = 'width:100%;';
var cellFontStyle = 'font-size:12pt;font-weight:bold;';
var defaultFontColor = 'color:#00BFFF';
var revenueCellFontColor = 'color:#006400';
html += '<table style="' + tableStyle + '"><tr>';
html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + jsonDataAggr.tc + '</font><br/><font style="font-size:8pt">' + JSMessages.transactions + '</font></td>';
html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + jsonDataAggr.oc + '</font><br/><font style="font-size:8pt">' + JSMessages.orders + '</font></td>';
html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + jsonDataAggr.lnc + '</font><br/><font style="font-size:8pt">' + JSMessages.logins + '</font></td>';
var currencyStr = "";
if ( jsonDataAggr.cur && jsonDataAggr.cur != null && jsonDataAggr.cur != "" ) {
	currencyStr = " (" + jsonDataAggr.cur + ")";
}
html += '<td style="align:center"><font style="' + cellFontStyle + revenueCellFontColor + '">' + roundDecimal( jsonDataAggr.brv ) + '</font><br/><font style="font-size:8pt">' + JSMessages.revenue + currencyStr + '</font></td>';
html += '</tr></table>';
return html;
}
*/
function getDomainUsageStatsTableHTML() {
	var html = '';
	var tableStyle = 'width:100%;';
	var cellFontStyle = 'font-size:12pt;font-weight:bold;';
	var defaultFontColor = 'color:#00BFFF';
	var revenueCellFontColor = 'color:#006400';
	if ( !jsonData || jsonData.length == 0 ) {
		console.log( 'jsonData is undefined or empty' );
		return;
	}
	var i = jsonData.length - 1; // the most recent in time
	html += '<table style="' + tableStyle + '"><tr>';
	html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + jsonData[i].tc + '</font><br/><font style="font-size:8pt">' + JSMessages.transactions + '</font></td>';
	html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + jsonData[i].oc + '</font><br/><font style="font-size:8pt">' + JSMessages.orders + '</font></td>';
	html += '<td style="align:center"><font style="' + cellFontStyle + defaultFontColor + '">' + jsonData[i].lnc + '</font><br/><font style="font-size:8pt">' + JSMessages.logins + '</font></td>';
	var currencyStr = '';
	if ( jsonData[i].cur && jsonData[i].cur != '' ) {
		currencyStr = " (" + jsonData[i].cur + ")";
	}
	html += '<td style="align:center"><font style="' + cellFontStyle + revenueCellFontColor + '">' + roundDecimal( jsonData[i].brv ) + '</font><br/><font style="font-size:8pt">' + JSMessages.revenue + currencyStr + '</font></td>';
	html += '</tr></table>';
	return html;
}

function roundDecimal( number ) {
	return parseFloat( Math.round( number * 100 ) / 100 ).toFixed(2);
}

function getDomainUsageStatsPanelChartHTML() {
	var html = '';
	var divClass = 'ui-widget ui-widget-content ui-corner-all';
	var divId = 'domainusagestatspanelchart';
	var divStyle = 'padding:4px;margin-top:10px;margin-bottom:15px;box-shadow: 0 1px 4px rgba(0,0,0,0.5);-moz-box-shadow: 0 4px 8px rgba(0,0,0,0.5);-webkit-box-shadow: 0 4px 8px rgba(0,0,0,0.5);';
	html += '<div id="' + divId + '" class="' + divClass + '" style="' + divStyle + '">';
	var exportTransactionCountsButtonHTML = '';
	var exportOrderCountsButtonHTML = '';
	var exportLoginCountsButtonHTML = '';
	var exportRevenueButtonHTML = '';
	
	if ( hasTransactions() ) {
		exportTransactionCountsButtonHTML = '<div style="float:left"><input type="image" src="/images/down.png" title="Download as PNG" id="exporttransactioncounts" onclick="onClickExport(\'exporttransactioncounts\')"></input></div>';
	}
	
	if ( hasOrders () ) {
		exportOrderCountsButtonHTML = '<div style="float:left"><input type="image" src="/images/down.png" title="Download as PNG"  id="exportordercounts" onclick="onClickExport(\'exportordercounts\')"></input><div>';
	}
	
	if ( hasLogins() ) {
		exportLoginCountsButtonHTML = '<div style="float:left"><input type="image" src="/images/down.png" title="Download as PNG"  id="exportlogincounts" onclick="onClickExport(\'exportlogincounts\')"></input></div>';
	} 
	
	if ( hasRevenue() ) {
		exportRevenueButtonHTML = '<div style="float:left"><input type="image" src="/images/down.png" title="Download as PNG"  id="exportrevenue" onclick="onClickExport(\'exportrevenue\')"></input></div>';
	}
	
	html += '<table style="width:100%">';
	html += '<tr><td style="padding:15px">' + exportTransactionCountsButtonHTML + '</td>';
	html += '<td style="padding:15px">' + exportOrderCountsButtonHTML + '</td></tr>';
	html += '<tr><td><div id="transactioncountsdiv" style="float:left"></div></td>';
	html += '<td><div id="ordercountsdiv" style="float:right"></div></td></tr>';
	html += '<tr><td style="padding:15px">' + exportLoginCountsButtonHTML + '</td>';
	html += '<td style="padding:15px">' + exportRevenueButtonHTML + '</td></tr>';
	html += '<tr><td><div id="logincountsdiv" style="float:left">' + '</div></td><td><div id="revenuediv" style="float:right">' + '</div></td></tr>';
	html += '</table>';
	html += '</div>';
	return html;
}

function drawMonthlyUsageCharts( showOnIndexPage ) {
	var margin;
	if ( !showOnIndexPage ) {
		margin = {top: 40, right: 20, bottom: 70, left: 50},
		width = 400 - margin.left - margin.right,
		height = 300 - margin.top - margin.bottom;
	} else {
		margin = {top: 40, right: 20, bottom: 70, left: 50},
		width = 300 - margin.left - margin.right,
		height = 200 - margin.top - margin.bottom;
	}
	// Parse the date / time into Year-Month format. This should be changed to Month-Year format later.
	var parseDate = d3.time.format("%m-%Y").parse;
	// Set the x scale
	var x = d3.scale.ordinal().rangeRoundBands([0, width], .05);
	// Set the y scale
	var y = d3.scale.linear().range([height, 0]);
	// Set the xAxis to show the time format, set it's scale to y and let it be oriented to bottom of the axis line.
	var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom")
    .tickFormat(d3.time.format("%b-%Y"));
	// Set the yAxis to show the data, set it's scale to y let it be oriented to the left of the axis line.
	// Set the number of ticks to 10, although this is actually overwritten if required by d3 js. 
	var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left")
    .ticks(5); // Number of points on the y axis
	
	// Create the svg element here. And append it to the body tag. Set the width and height attributes of the svg element.
	// Transform the origin to margin.left, margin.top
	var svg1 = d3.select("#transactioncountsdiv").append("svg")
	.attr( "id", "transactioncountssvg" )
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  	.append("g")
    .attr("transform", 
          "translate(" + margin.left + "," + margin.top + ")");
	
	// Iterate through the jsonData and parse the date from milliseconds to "month Year" format
	for ( var i = 0; i < jsonData.length; i++ ) {
		var newDate = new Date( jsonData[i].t );
		var month = newDate.getMonth() + 1;
		var fullYear = newDate.getFullYear();
		jsonData[i].t = parseDate( month + "-" + fullYear ); 
	}
	
	// Set the x and y domain
	x.domain( jsonData.map(function(d) { return d.t; }));
	y.domain([0, d3.max(jsonData, function(d) { return d.tc; })]);
	// If there are any transactions to show, append the axes
	if ( hasTransactions() ) {
	svg1.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
    .selectAll("text")
      .style("text-anchor", "end")
      .attr("dx", "-.8em")
      .attr("dy", "-.55em")
      .attr("transform", "rotate(-90)" );
	
	// Since transaction counts can be big numbers,
	// append prefixes so that the y axis ticks do not have big numbers that interfere with the y axis title
	var tcFormatValue = d3.format("s"); // This function adds prefix of k or M. Otherwise, ugly big numbers appear on the y axis.
	var tcYAxis = yAxis;
	tcYAxis.tickFormat(function(d){ return tcFormatValue(d);});
	
	// Since transaction counts are whole numbers, the ticks on the transaction counts y axis should not display decimal numbers. 
	// var tcYAxis = yAxis;
	// tcYAxis.tickFormat(d3.format("d"))
	// .tickSubdivide(0);
	
	svg1.append("g")
    .attr("class", "y axis")
    .call(tcYAxis)
  	.append("text")
    .attr("transform", "rotate(-90)")
    .attr("y", -1 * margin.left )
    .attr("dy", "1.5em") //.71em
    .style("text-anchor", "end")
    .text("Transaction Counts");
	} // end if hasTransactions()
		
	// Code to add a title to the bar graph
	svg1.append("text")
    .attr("x", (width / 2))             
    .attr("y", 0 - (margin.top / 2))
    .attr("text-anchor", "middle")  
    .style("font-size", "16px") 
    .text("#Transactions");
	
	// If there are transactions only then append rectangles. Otherwise so an image indicating "No data available"
	if ( hasTransactions() ) {
		// Create a g and add rect and text to it.
		// And append that g to svg
		var bar = svg1.selectAll("rect")
	    .data( jsonData )
	    .enter().append("g");
		bar.append("rect")
		.attr("x", function(d) { return x(d.t); })
	    .attr("width", x.rangeBand())
	    .attr("y", function(d) { return y(d.tc); })
	    .attr("height", function(d) { return height - y(d.tc); })
	    .attr("fill", function(d) {
						return "rgb(46, 110, 158)";
		 });
		bar.append("text")
	    .attr("x", function(d) {
	    	var rectWidth = x.rangeBand();
	    	return ( x(d.t) + x.rangeBand()/2 ); })
	    .attr("y", function(d) { return y(d.tc) - 3; })
	    .text(function(d) { 
	    	if ( d.tc != 0 ) { 
	    		var prefix = d3.formatPrefix( d.tc ); // Adds a perfix k, M or G
	    		return  prefix.scale( d.tc ).toFixed() + prefix.symbol; // Round off big numbers and append prefix
	    	}
	    }) // Do not print 0
		.attr("text-anchor", "middle")
		.attr("font-family", "sans-serif")
	    .attr("font-size", "11px")
	    .attr("fill", "black");
  } else {
	  svg1.append("svg:image")
	  .attr("x",0)
	  .attr("y",0)
	  .attr("width",width)
	  .attr("height",height)
	  .attr("xlink:href","/images/nodataavailable.jpeg");
  }
	
	
	// Create the second svg element here. And append it to the body tag. Set the width and height attributes of the svg element.
	// Transform the origin to margin.left, margin.top
	var svg2 = d3.select("#ordercountsdiv").append("svg")
	.attr( "id", "ordercountssvg" )
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  	.append("g")
    .attr("transform", 
          "translate(" + margin.left + "," + margin.top + ")");
	
	// Iterate through the jsonData and parse the date from milliseconds to "month Year" format
	for ( var i = 0; i < jsonData.length; i++ ) {
		var newDate = new Date( jsonData[i].t );
		var month = newDate.getMonth() + 1;
		var fullYear = newDate.getFullYear();
		jsonData[i].t = parseDate( month + "-" + fullYear ); 
	}
	
	// Set the x and y domain
	x.domain( jsonData.map(function(d) { return d.t; }));
	y.domain([0, d3.max(jsonData, function(d) { return d.oc; })]);
	
	if ( hasOrders() ) {
	svg2.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
    .selectAll("text")
      .style("text-anchor", "end")
      .attr("dx", "-.8em")
      .attr("dy", "-.55em")
      .attr("transform", "rotate(-90)" );
	
	// Since order counts can be big numbers,
	// append prefixes so that the y axis ticks do not have big numbers that interfere with the y axis title
	var ocFormatValue = d3.format("s"); // This function adds prefix of k or M. Otherwise, ugly big numbers appear on the y axis.
	var ocYAxis = yAxis;
	ocYAxis.tickFormat(function(d){ return ocFormatValue(d);});
	
	// Since order counts are whole numbers, the ticks on the order counts y axis should not display decimal numbers. 
	// var ocYAxis = yAxis;
	// ocYAxis.tickFormat(d3.format("d"))
	// .tickSubdivide(0);

	svg2.append("g")
    .attr("class", "y axis")
    .call(ocYAxis)
  	.append("text")
    .attr("transform", "rotate(-90)")
    .attr("y", -1 * margin.left )
    .attr("dy", "1.5em") //.71em
    .style("text-anchor", "end")
    .text("Order Counts");
	}
	
	// Code to add a title to the bar graph
	svg2.append("text")
    .attr("x", (width / 2))             
    .attr("y", 0 - (margin.top / 2))
    .attr("text-anchor", "middle")  
    .style("font-size", "16px") 
    // .style("text-decoration", "underline")  
    .text("#Orders");
	if ( hasOrders() ) {	
		// Create a g and add rect and text to it.
		// And append that g to svg
		var bar = svg2.selectAll("rect")
	    .data( jsonData )
	    .enter().append("g");
		bar.append("rect")
		.attr("x", function(d) { return x(d.t); })
	    .attr("width", x.rangeBand())
	    .attr("y", function(d) { return y(d.oc); })
	    .attr("height", function(d) { return height - y(d.oc); })
	    .attr("fill", function(d) {
						return "rgb(225,112,9)";
		 });
		bar.append("text")
	    .attr("x", function(d) {
	    	var rectWidth = x.rangeBand();
	    	return ( x(d.t) + x.rangeBand()/2 ); })
	    .attr("y", function(d) { return y(d.oc) - 3; })
	    .text(function(d) { 
	    	if ( d.oc != 0 ) { 
	    		var prefix = d3.formatPrefix( d.oc ); // Adds a perfix k, M or G
	    		return  prefix.scale( d.oc ).toFixed() + prefix.symbol; // Round off big numbers and append prefix
	    	}
	    }) //  Do not print 0
		.attr("text-anchor", "middle")
		.attr("font-family", "sans-serif")
	    .attr("font-size", "11px")
	    .attr("fill", "black");
	} else {
		svg2.append("svg:image")
		  .attr("x",0)
		  .attr("y",0)
		  .attr("width",width)
		  .attr("height",height)
		  .attr("xlink:href","/images/nodataavailable.jpeg");
	}
	
	// Create the third svg element here. And append it to the logincountsdiv. Set the width and height attributes of the svg element.
	// Transform the origin to margin.left, margin.top
	var svg3 = d3.select("#logincountsdiv").append("svg")
	.attr( "id", "logincountssvg" )
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    // .attr("style", "outline: thin solid blue;") 
  	.append("g")
    .attr("transform", 
          "translate(" + margin.left + "," + margin.top + ")");
	
	// Iterate through the jsonData and parse the date from milliseconds to "month Year" format
	for ( var i = 0; i < jsonData.length; i++ ) {
		var newDate = new Date( jsonData[i].t );
		var month = newDate.getMonth() + 1;
		var fullYear = newDate.getFullYear();
		jsonData[i].t = parseDate( month + "-" + fullYear ); 
	}
	
	
	// Set the x and y domain
	x.domain( jsonData.map(function(d) { return d.t; }));
	y.domain([0, d3.max(jsonData, function(d) { return d.lnc; })]);
	if ( hasLogins() ) {	  
		svg3.append("g")
	      .attr("class", "x axis")
	      .attr("transform", "translate(0," + height + ")")
	      .call(xAxis)
	    .selectAll("text")
	      .style("text-anchor", "end")
	      .attr("dx", "-.8em")
	      .attr("dy", "-.55em")
	      .attr("transform", "rotate(-90)" );
		
		// Since login counts can be large numbers,
		// append k or M prefix to prevent the ticks on the y axis from interfering with the y axis title. 
		var lncFormatValue = d3.format("s"); // This function adds prefix of k or M. Otherwise, ugly big numbers appear on the y axis.
		var lncYAxis = yAxis;
		lncYAxis.tickFormat(function(d){ return lncFormatValue(d);});
		
		// Since login counts are whole numbers, the ticks on the login counts y axis should not display decimal numbers. 
		//var lncYAxis = yAxis;
		//lncYAxis.tickFormat(d3.format("d"))
		//.tickSubdivide(0);
		
		svg3.append("g")
	    .attr("class", "y axis")
	    .call(lncYAxis)
	  	.append("text")
	    .attr("transform", "rotate(-90)")
	    .attr("y", -1 * margin.left )
	    .attr("dy", "1.5em") //.71em
	    .style("text-anchor", "end")
	    .text("Login Counts");
	}
	
	// Code to add a title to the bar graph
	svg3.append("text")
    .attr("x", (width / 2))             
    .attr("y", 0 - (margin.top / 2))
    .attr("text-anchor", "middle")  
    .style("font-size", "16px") 
    .text("#Logins");
	
	// Only if there is login data, append rectangles. Otherwise, show No data available image.
	if ( hasLogins() ) {
		// Create a g and add rect and text to it.
		// And append that g to svg
		var bar = svg3.selectAll("rect")
	    .data( jsonData )
	    .enter().append("g");
		bar.append("rect")
		.attr("x", function(d) { return x(d.t); })
	    .attr("width", x.rangeBand())
	    .attr("y", function(d) { return y(d.lnc); })
	    .attr("height", function(d) { return height - y(d.lnc); })
	    .attr("fill", function(d) {
						return "rgb( 0, 191, 255 )"; 
		 });
		bar.append("text")
	    .attr("x", function(d) {
	    	var rectWidth = x.rangeBand();
	    	return ( x(d.t) + x.rangeBand()/2 ); })
	    .attr("y", function(d) { return y(d.lnc) - 3; })
	    .text(function(d) { if ( d.lnc != 0 ) { 
    		var prefix = d3.formatPrefix( d.lnc ); // Adds a perfix k, M or G
    		return  prefix.scale( d.lnc ).toFixed() + prefix.symbol; // Round off big numbers and append prefix
	    	}
	    }) //  Do not print 0
		.attr("text-anchor", "middle")
		.attr("font-family", "sans-serif")
	    .attr("font-size", "11px")
	    .attr("fill", "black");
	} else {
		svg3.append("svg:image")
		  .attr("x",0)
		  .attr("y",0)
		  .attr("width", width)
		  .attr("height", height)
		  .attr("xlink:href","/images/nodataavailable.jpeg");
	}
	
	// Create the fourth svg element here. And append it to the revenuediv. Set the width and height attributes of the svg element.
	// Transform the origin to margin.left, margin.top
	var svg4 = d3.select("#revenuediv").append("svg")
	.attr( "id", "revenuesvg" )
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  	.append("g")
    .attr("transform", 
          "translate(" + margin.left + "," + margin.top + ")");
	
	// Iterate through the jsonData and parse the date from milliseconds to "month Year" format
	for ( var i = 0; i < jsonData.length; i++ ) {
		var newDate = new Date( jsonData[i].t );
		var month = newDate.getMonth() + 1;
		var fullYear = newDate.getFullYear();
		jsonData[i].t = parseDate( month + "-" + fullYear ); 
	}

	// Set the x and y domain
	x.domain( jsonData.map(function(d) { return d.t; }));
	y.domain([0, d3.max(jsonData, function(d) { return d.brv; })]);
	
	if ( hasRevenue() ) {
		svg4.append("g")
	      .attr("class", "x axis")
	      .attr("transform", "translate(0," + height + ")")
	      .call(xAxis)
	    .selectAll("text")
	      .style("text-anchor", "end")
	      .attr("dx", "-.8em")
	      .attr("dy", "-.55em")
	      .attr("transform", "rotate(-90)" );
		
		var formatValue = d3.format("s");
		var brvYAxis = yAxis;
		brvYAxis.tickFormat(function(d){ return formatValue(d);});
		
		var svg4YAxisTitleTxt = "Revenue";
		if ( jsonDataAggr.cur && jsonDataAggr.cur != null && jsonDataAggr.cur != "" )
			svg4YAxisTitleTxt += " (" + jsonDataAggr.cur + ")";
		svg4.append("g")
	    .attr("class", "y axis")
	    .call(brvYAxis)
	  	.append("text")
	    .attr("transform", "rotate(-90)")
	    .attr("y", -1 * margin.left )
	    .attr("dy", "1.5em") // .71em
	    .style("text-anchor", "end")
	    .text( svg4YAxisTitleTxt );
		
	}
	// Code to add a title to the bar graph
	var revenueTxt = "#Revenue";
	if ( jsonDataAggr.cur && jsonDataAggr.cur != null && jsonDataAggr.cur != "" )
		revenueTxt += " (" + jsonDataAggr.cur + ")";
	svg4.append("text")
    .attr("x", (width / 2))             
    .attr("y", 0 - (margin.top / 2))
    .attr("text-anchor", "middle")  
    .style("font-size", "16px") 
    .text( revenueTxt );
	
	// Only if there is revenue data to be shown, append rectangles. Otherwise, just show
	// a No data available image.
	if ( hasRevenue() ) {
		// Create a g and add rect and text to it.
		// And append that g to svg - Green color
		var bar = svg4.selectAll("rect")
	    .data( jsonData )
	    .enter().append("g");
		bar.append("rect")
		.attr("x", function(d) { return x(d.t); })
	    .attr("width", x.rangeBand())
	    .attr("y", function(d) { return y(d.brv); })
	    .attr("height", function(d) { return height - y(d.brv); })
	    .attr("fill", function(d) {
						return "rgb(0,100,0)";
		 });
		bar.append("text")
	    .attr("x", function(d) {
	    	var rectWidth = x.rangeBand();
	    	return ( x(d.t) + x.rangeBand()/2 ); })
	    .attr("y", function(d) { return y(d.brv) - 3; })
	    .text(function(d) { if ( d.brv != 0 ) { 
    		var prefix = d3.formatPrefix( d.brv ); // Adds a perfix k, M or G
    		return  prefix.scale( d.brv ).toFixed() + prefix.symbol; // Round off big numbers and append prefix
	    	}
	    }) // Do not print 0
		.attr("text-anchor", "middle")
		.attr("font-family", "sans-serif")
	    .attr("font-size", "11px")
	    .attr("fill", "black");
	} else {
		svg4.append("svg:image")
		  .attr("x",0)
		  .attr("y",0)
		  .attr("width", width)
		  .attr("height", height)
		  .attr("xlink:href","/images/nodataavailable.jpeg");
	}
}

function showNoChartViewAvailableMsg() {
	var monthlyChartView = document.getElementById( 'monthlychartviewpanel' );
	monthlyChartView.innerHTML = 'No data available';
}
function hideChartView() {
	monthlyChartViewPanel.style.display = 'none';
}

function onClickAll() {
	// Hide the chart view
	hideChartView();
	// Show the tabular view
	showMonthlyTabularView();
}

function hasTransactions() {
	// Iterate through jsonData. Check if transaction counts are there at all. If yes, then return true.
	// Otherwise, return false.
	var hasTransactions = false;
	for ( var i = 0; i < jsonData.length; i++ ) {
		if ( jsonData[i].tc > 0 ) {
			hasTransactions = true;
			break;
		}
	}
	return hasTransactions;
}

function hasOrders() {
	// Iterate through jsonData. Check if order counts are there at all. If yes, then return true.
	// Otherwise, return false.
	var hasOrders = false;
	for ( var i = 0; i < jsonData.length; i++ ) {
		if ( jsonData[i].oc > 0 ) {
			hasOrders = true;
			break;
		}
	}
	return hasOrders;
}

function hasLogins() {
	// Iterate through jsonData. Check if login counts are there at all. If yes, then return true.
	// Otherwise, return false.
	var hasLogins = false;
	for ( var i = 0; i < jsonData.length; i++ ) {
		if ( jsonData[i].lnc > 0 ) {
			hasLogins = true;
			break;
		}
	}
	return hasLogins;
}

function hasRevenue() {
	// Iterate through jsonData. Check if login counts are there at all. If yes, then return true.
	// Otherwise, return false.
	var hasRevenue = false;
	for ( var i = 0; i < jsonData.length; i++ ) {
		if ( jsonData[i].brv > 0 ) {
			hasRevenue = true;
			break;
		}
	}
	return hasRevenue;
}

function onClickExport( buttonId ){
	buttonElement = document.getElementById( buttonId );
	if ( buttonId == 'exporttransactioncounts' ) {
		var transactionCountsSvg = document.getElementById( 'transactioncountssvg' );
		saveSvgAsPng( transactionCountsSvg, 'transactioncounts.png', 1 );
	} else if ( buttonId == 'exportordercounts' ) {
		var orderCountsSvg = document.getElementById( 'ordercountssvg' );
		saveSvgAsPng( orderCountsSvg, 'ordercounts.png', 1 );
	} else if ( buttonId == 'exportlogincounts' ) {
		var loginCountsSvg = document.getElementById( 'logincountssvg' );
		saveSvgAsPng( loginCountsSvg, 'logincounts.png', 1 );
	} else if ( buttonId == 'exportrevenue' ) {
		var revenueSvg = document.getElementById( 'revenuesvg' );
		saveSvgAsPng( revenueSvg, 'revenue.png', 1 );
    }
} // End of function onclick exportSVG
