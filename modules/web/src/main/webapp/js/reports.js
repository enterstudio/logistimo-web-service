// Javascript functions used for SG reporting

// Aggregate a (Google visualization) Datatable over time
// data - Datatable object sorted in *desc* order of time
// freq - 'weekly', 'monthly'
// Returns an array of ( array of aggregated row data - size of this array being same as aggColIndexes size ) 
function getTimeAggregates( data, dateColIndex, aggColIndexes, freq ) {
	var aggdata = [];
	var numRows = data.getNumberOfRows();
	// Get first date
	var prevDate = data.getValue( 0, dateColIndex );
	// Init. agg data
	var aggrow = zeros( aggColIndexes.length );
	// Iterator on the next set of dates
	for ( var i = 0; i < numRows; i++ ) {
		// Get date
		var thisDate = data.getValue( i, dateColIndex );
		// Flag to indicate if a push of an aggregate entry was done
		var pushed = false;
		if ( freq == 'weekly' ) {
			var day = thisDate.getDay(); // day of week (0-6)
			if ( day == 0 && i > 0 ) { // Sunday - now add the aggregates to the aggdata object
									   // and not the first day (esp. need to check if the first day happens to a Sunday)
				aggdata.push( getRow( prevDate, aggrow ) );
				pushed = true;
				prevDate = thisDate; // reset the previous date (this is needed, given data is in desc order of dates)
				aggrow = zeros( aggColIndexes.length ); // reset counts for next iteration
			}
			// Add the data
			aggrow = add( aggrow, data, i, aggColIndexes );
		} else if ( freq == 'monthly' ) {
			var month = thisDate.getMonth();
			if ( month != prevDate.getMonth() ) {
				aggdata.push( getRow( prevDate, aggrow ) );
				pushed = true;
				prevDate = thisDate;
				aggrow = zeros( aggColIndexes.length ); // reset counts for next iteration
			}
			// Add
			aggrow = add( aggrow, data, i, aggColIndexes );
		} else if ( freq == 'daily' ) {
			if ( thisDate != prevDate ) {
				aggdata.push( getRow( prevDate, aggrow ) );
				pushed = true;
				prevDate = thisDate;
				aggrow = zeros( aggColIndexes.length );
			}
			aggrow = add( aggrow, data, i, aggColIndexes );
		}
		/*
		// If its the last entry, and a push is pending, then do a push
		if ( i == ( numRows - 1 ) && !pushed )
			aggdata.push( getRow( prevDate, aggrow ) );
		*/
		if ( i == ( numRows - 1 ) && pushed )
			aggdata.push( getRow( thisDate, aggrow ) );
	}
	return aggdata;
}

// Sum the data on the given columns
function sumData( data, cols ) {
	var aggdata = zeros( cols.length );
	var numRows = data.getNumberOfRows();
	for ( var i = 0; i < numRows; i++ ) {
		for ( var j = 0; j < cols.length; j++ ) {
			aggdata[ j ] += data.getValue( i, cols[ j ] );
		}
	}
	return aggdata;
}

// Get a 0's array of given length
function zeros( len ) {
	var arr = [];
	for ( var i = 0; i < len; i++ )
		arr.push( 0 );
	return arr;
}

// Add to agg. data row
function add( aggrow, data, rowIndex, colIndexes ) {
	for ( var j = 0; j < colIndexes.length; j++ )
		aggrow[ j ] += data.getValue( rowIndex, colIndexes[ j ] );
	return aggrow;
}

// Get a aggregate row
function getRow( date, aggrow ) {
	var row = [];
	row.push( date );
	for ( var i = 0; i < aggrow.length; i++ )
		row.push( aggrow[ i ] );
	return row;
}

// Fill missing date rows with zero-valued date rows (daily frequency, date column assumed to be first)
function packMissingDates( data, zeroFillColIndexes, lastFillColIndexes ) {
	var numRows = data.getNumberOfRows();
	var numCols = data.getNumberOfColumns();
	var d1 = data.getValue( 0, 0 ); // date
	var i = 1;
	while ( i < numRows ) {
		var d2 = data.getValue( i, 0 ); // date
		// Get the days diff. between dates
		var days = daysDiff( d2, d1 );
		var offset = -1; // a negative offset, assuming descending order of dates
		if ( days < 0 ) {
			days = Math.abs( days ); // get a positive number
			offset = 1; // get a positive offset
		}
		///console.log( 'd1: ' + d1 + ', d2: ' + d2 + ', d2-d1: ' + days );
		if ( days > 1 ) {
			// Add newer rows to data with zero values
			var rows = [];
			for ( var k = 1; k < days; k++ ) {
				var row = [];
				d1 = offsetDate( d1, offset );
				row.push( d1 );
				// Add the other columns to this row, ensuring zeros where needed, else null
				for ( var j = 1; j < numCols; j++ ) {
					var val = null;
					if ( contains( zeroFillColIndexes, j ) )
						val = 0;
					else if ( contains ( lastFillColIndexes, j ) )
						val = data.getValue( i, j ); // get the last value (i.e. from the lower date)
					row.push( val );
				}
				// Add this row
				rows.push( row );
			}
			// Add the rows to the data table
			if ( rows.length > 0 ) {
				data.insertRows( i, rows );
				// Update numRows
				numRows = data.getNumberOfRows();
			}
		} else if ( days == 0 )
			days = 1; // so that the loop moves forward (in case of equal entries)
		i += days;
		d1 = d2;
	}
}

// Get the difference between two dates (in days)
function daysDiff( d1, d2 ) {
	var t2 = d2.getTime();
	var t1 = d1.getTime();
	
	return Math.ceil( (t2-t1)/(24*3600*1000) );
}

// Get a date with +/- n days from given date
function offsetDate( date, offsetDays ) {
	return new Date( date.getTime() + offsetDays * 24 * 3600 * 1000 );
}

// Check whether an array contains an element or not
function contains( array, value ) {
	if ( array ) {
	  var i = array.length;
	  while ( i-- ) {
	    if ( array[i] === value ) {
	      return true;
	    }
	  }
	}
	return false;
}

//Function to compare two dates, ignoring the time
function compareDates( date1, date2 ) {
	var val = 0;
	var y1 = date1.getYear();
	var y2 = date2.getYear();
	if ( y1 > y2 ) {
		val = 1;
	} else if ( y1 < y2 ) {
		val = -1;
	} else { // years are equal, compare months
		var m1 = date1.getMonth();
		var m2 = date2.getMonth();
		if ( m1 > m2 ) {
			val = 1;
		} else if ( m1 < m2 ) {
			val = -1;
		} else { // months equal, compare days
			var d1 = date1.getDate();
			var d2 = date2.getDate();
			if ( d1 > d2 ) {
				val = 1;
			} else if ( d1 < d2 ) {
				val = -1;
			} else {
				val = 0;
			}
		}
	}

	return val;
}

// Merge data (with dates) across two data tables; insert columns [colIndexes2] from data2 into data1 at the given colIndex1 (in data1), only if the date matches
function mergeData( data1, data2, colIndex1, colIndexes2, createColumns ) {
	var colIndexes1 = [];
	// Insert the required rows into data1
	for ( var c = 0; c < colIndexes2.length; c++ ) {
		var col = colIndexes2[ c ];
		colIndexes1.push( colIndex1 );
		if ( createColumns )
			data1.insertColumn( colIndex1, data2.getColumnType( col ), data2.getColumnLabel( col ), data2.getColumnId( col ) );
		colIndex1++;
	}
	// Insert values from data2 into data1, where dates match
	var rows2 = data2.getNumberOfRows();
	for ( var i = 0; i < rows2; i++ ) {
		var date = data2.getValue( i, 0 );
		// Get the values in this row
		var values = [];
		for ( var c = 0; c < colIndexes2.length; c++ )
			values.push( data2.getValue( i, colIndexes2[ c ] ) );
		insertRowData( data1, date, colIndexes1, values );
	}
}

// Insert data into a row of data table that matches the given date
function insertRowData( data, date, colIndexes, values ) {
	var rows = data.getNumberOfRows();
	for ( var i = 0; i < rows; i++ ) {
		var date0 = data.getValue( i, 0 );
		if ( compareDates( date, date0 ) == 0 ) {
			for ( var c = 0; c < colIndexes.length; c++ ) {
				data.setValue( i, colIndexes[ c ], values[ c ] );
			}
			return;
		}
	}
}

// Add rows from data2 to data1, at the end of data1 (note: the number of columns in each table should be identical)
function addRows( data1, data2 ) {
	var numRows = data2.getNumberOfRows();
	var numCols = data2.getNumberOfColumns();
	if ( data1.getNumberOfColumns() != numCols ) {
		console.log( 'addRows: data1 and data2 have differening number of columns: ' + data1.getNumberOfColumns() + ' & ' + numCols );
		return;
	}
	var rows = [];
	for ( var i = 0; i < numRows; i++ ) {
		var row = [];
		for ( var j = 0; j < numCols; j++ ) {
			var value = {};
			value.v = data2.getValue( i, j );
			value.f = data2.getFormattedValue( i, j );
			row.push( value );
		}
		rows.push( row );
	}
	// Add rows to data1
	if ( rows.length > 0 )
		data1.addRows( rows );
}

// Fill a given column with the given value
function fillColumn( data, colIndex, value ) {
	var rows = data.getNumberOfRows();
	for ( var i = 0; i < rows; i++ ) {
		data.setValue( i, colIndex, value );
	}
}

// Show the loading indicator
function showLoader( id ) {
	var div = document.getElementById( id );
	if ( div )
		div.innerHTML = '<img src="/images/loader.gif"/>';
}

// Round to decimal places
function roundDecimal( number ) {
	return parseFloat( parseFloat( Math.round( number * 100 ) / 100 ).toFixed(2) );
}

// Get formatted date for reporting URL, given a Javascript Date object
function getURLFormattedDate( date ) {
	return date.getDate() + '/' + ( date.getMonth() + 1 ) + '/' + date.getFullYear();
}

// Get a date starting from the 1st of next month
function getFirstOfNextMonth( date ) {
	var month = date.getMonth();
	if ( month == 11 )
		return new Date( date.getFullYear() + 1, 0, 1 );
	else
		return new Date( date.getFullYear(), month + 1, 1 );
}

// Get month/year key
function getMonthKey( date ) {
    return ( date.getMonth() + 1 ) + '/' + date.getFullYear();
}

// Check if a JSON object is empty
function isEmptyObject( obj ) {
	for ( var key in obj )
		return false;
	return true;
}

// Plot the stock event graph
function plotStockEvent( divId, url, eventName, materialName, color ) {
	console.log( 'plotStockEvent: url = ' + url );
	var div = document.getElementById( divId );
	div.innerHTML = '<img src="/images/loader.gif" />';
	var title = JSMessages.recent + ' ' + eventName + ' ' + JSMessages.incidents + ' ' + JSMessages._for + ' ' + materialName;
	// Show the modal screen
	$( '#' + divId ).dialog( {
								modal: true,
								width: 720,
								height: 200,
								modal: true,
								title: title,
								position: {
								    my: 'left',
								    at: 'right',
								  }
							   } );
	// Form the query with the reports URL
  	var query = new google.visualization.Query( url );
  	query.send( function( response ) {
    		if ( response.isError() ) {
        		div.innerHTML = '<font style="font-family:Arial">' + response.getDetailedMessage() + '</font>';
         	} else {
	    		// Get the data table and render the Table report
	       	 	var data = response.getDataTable();
	       	 	// Hide the irrelevant columns
	       	 	var stockEventView = new google.visualization.DataView( data );
	       	 	// stockEventView.hideColumns( [ 1,2,3,6,7 ] ); // hide the kiosk-name, stock, duration and Ids columns
	       	 	stockEventView.hideColumns( [ 1,2,3,4,5,8,9 ] ); // hide the kiosk-name, stock, min, max, duration and Ids columns (materialId and kioskiId)
	       	 	// Draw the chart
	       	 	var chart = new google.visualization.Timeline( div );
	       	 	chart.draw( stockEventView, { colors: [ color ], width: 680  } );
        	}
    	} );
}