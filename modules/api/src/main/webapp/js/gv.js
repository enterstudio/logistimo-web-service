// Scripts for Google visualization

function drawBarChart( dataTableJson, title, width, height, xAxisTitle, yAxisTitle, divElement ) {
	alert( "drawBarChart" );
	// Load the visualization API and the bar-graph package
	google.load( 'visualization', '1', { 'packages': ['barchart'] } );
	// Create the data object
	var data = new google.visualization.DataTable( dataTableJson );
	// Form the parameter JSON string for the chart
	var params = "{ title: '" + title + "', width: " + width + ", height: " + height +
				   	 ", vAxis: { title: '" + yAxisTitle + "' }, hAxis: { title: '" + xAxisTitle + "' } }";
	// Instantiate and draw the chart
	var chart = new google.visulaization.BarChart( divElement );
	chart.draw( data, params );
}

function drawAnnotatedTimeline( dataTableJson ) {
	// Load the visualization API and the bar-graph package
    google.load( 'visualization', '1', { packages: ['annotatedtimeline'] } );
}

function setOnLoadCallback( func ) {
	google.setOnLoadCallback( func );
}