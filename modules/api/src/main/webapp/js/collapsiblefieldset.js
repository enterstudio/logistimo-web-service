function constructCollapsableFieldsets() 
{
    var allFsets = document.getElementsByTagName('fieldset');
    var fset = null;
    for (var i=0; i<allFsets.length; i++)
     {
         fset = allFsets[i];
         if(fset.attributes['collapsed']!=null) {
        	 var hiddendivids = null;
        	 if ( fset.attributes['hiddendivs'] )
        		 hiddendivids = fset.attributes['hiddendivs'].value.split(',');
             constructCollapsableFieldset(fset, fset.attributes['collapsed'].value, 
            		 					  fset.attributes['collapsible'].value,
            		 					 hiddendivids );
         }
     }
 }
  
 //for collapsable fieldset:
 function constructCollapsableFieldset(fset, collapsed, collapsible, hiddendivids)
 {
     //main content:
     //var divContent = fset.getElementsByTagName('div')[0];
	 var divContents = fset.getElementsByTagName('div');
     if (divContents == null)
         return;
     
     if ( hiddendivids ) {   	 
    	 divContents = [];
    	 for ( var i = 0; i < hiddendivids.length; i++ ) {
     		 divContents.push( document.getElementById( hiddendivids[ i ] ) );
    	 }
     }
    	 
     if (collapsed == 'true') {
    	 for ( var i = 0; i < divContents.length; i++ ) {
    		divContents[i].style.display = 'none';
    	 }
     }

     //+/- ahref:
     var ahrefText = getAHrefForToogle(collapsed, collapsible, hiddendivids );
  
     //legend:
     var legend = fset.getElementsByTagName('legend')[0];
     if (legend != null)
         legend.innerHTML = ahrefText + legend.innerHTML;
     else
         fset.innerHTML = '<legend>' + ahrefText + '</legend>' + fset.innerHTML;
 }
  
 function getAHrefForToogle(collapsed, collapsible, hiddendivids )
 {
     var ahrefText; 
     if( collapsible == 'false' )
    	 ahrefText = "";
     else {
    	 ahrefText = '<a onClick="toogleFieldset(this.parentNode.parentNode';
    	 if ( hiddendivids )
    		 ahrefText += ",new Array(" + getLiteralArray( hiddendivids ) + ")";
    	 ahrefText += ');" style="text-decoration: none;">';
    	 
     }
     ahrefText = ahrefText + getExpanderItem(collapsed) + (ahrefText = "" ? "": "</a>&nbsp;");
     return ahrefText;
 }
 
 function getLiteralArray( array ) {
	 for( i = 0; i< array.length; i++ ){
		 array[i] = "'" + array[i] + "'";
	 }
	 return array;
 }
 
 function getExpanderItem(collapsed)
 {
     var ecChar;
     if (collapsed=='true')
         ecChar='<img src="/images/plus.png"/>';
     else
         ecChar='<img src="/images/minus.png"/>';
  
     return ecChar;
 }
 
 function toggleFieldSet(fset) {
	 toggleFieldSet(fset,null);
 }
 function toogleFieldset(fset, hiddendivids)
 {
     var ahref = fset.getElementsByTagName('a')[0];
     //var div = fset.getElementsByTagName('div')[0];
     var divs = fset.getElementsByTagName('div');
     if ( hiddendivids ) {
    	 divs = [];
    	 for ( var i = 0; i < hiddendivids.length; i++ )
    		 divs.push( document.getElementById( hiddendivids[ i ] ) );
     }
     
     if (divs[0].style.display != "none") {
         ahref.innerHTML=getExpanderItem('true');
     } else {
         ahref.innerHTML=getExpanderItem('false');
     }
     
     for ( var i = 0; i < divs.length; i++ ) { 
    	 if (divs[i].style.display != "none") {
	         divs[i].style.display = 'none';
	     } else {
	         divs[i].style.display = '';
	     }
     }
 }
